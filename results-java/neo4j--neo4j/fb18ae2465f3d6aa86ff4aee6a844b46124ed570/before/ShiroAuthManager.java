/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.security.enterprise.auth;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.security.AuthorizationViolationException;
import org.neo4j.kernel.api.security.AuthSubject;
import org.neo4j.kernel.api.security.AuthToken;
import org.neo4j.kernel.api.security.AuthenticationResult;
import org.neo4j.kernel.api.security.exception.IllegalCredentialsException;
import org.neo4j.kernel.api.security.exception.InvalidAuthTokenException;
import org.neo4j.server.security.auth.AuthenticationStrategy;
import org.neo4j.server.security.auth.BasicAuthManager;
import org.neo4j.server.security.auth.PasswordPolicy;
import org.neo4j.server.security.auth.RateLimitedAuthenticationStrategy;
import org.neo4j.server.security.auth.User;
import org.neo4j.server.security.auth.UserRepository;

public class ShiroAuthManager extends BasicAuthManager implements RoleManager
{
    private final SecurityManager securityManager;
    private final EhCacheManager cacheManager;
    private final FileUserRealm realm;
    private final RoleRepository roleRepository;

    public ShiroAuthManager( UserRepository userRepository, RoleRepository roleRepository,
            PasswordPolicy passwordPolicy, AuthenticationStrategy authStrategy, boolean authEnabled )
    {
        super( userRepository, passwordPolicy, authStrategy, authEnabled );

        realm = new FileUserRealm( userRepository, roleRepository );
        // TODO: Maybe MemoryConstrainedCacheManager is good enough if we do not need timeToLiveSeconds?
        // It would be one less dependency.
        // Or we could try to reuse Hazelcast which is already a dependency, but we would need to write some
        // glue code or use the HazelcastCacheManager from the Shiro Support repository.
        cacheManager = new EhCacheManager();
        securityManager = new DefaultSecurityManager( realm );
        this.roleRepository = roleRepository;
    }

    public ShiroAuthManager( UserRepository userRepository, RoleRepository roleRepository,
            PasswordPolicy passwordPolicy, AuthenticationStrategy authStrategy )
    {
        this( userRepository, roleRepository, passwordPolicy, authStrategy, true );
    }

    public ShiroAuthManager( UserRepository userRepository, RoleRepository roleRepository,
            PasswordPolicy passwordPolicy, Clock clock, boolean authEnabled )
    {
        this( userRepository, roleRepository, passwordPolicy, new RateLimitedAuthenticationStrategy( clock, 3 ),
                authEnabled );
    }

    @Override
    public void init() throws Throwable
    {
        super.init();

        roleRepository.init();
        cacheManager.init();
        realm.setCacheManager( cacheManager );
        realm.init();
    }

    @Override
    public void start() throws Throwable
    {
        users.start();
        roleRepository.start();

        if ( authEnabled )
        {
            if ( realm.numberOfRoles() == 0 )
            {
                for ( String role : new PredefinedRolesBuilder().buildRoles().keySet() )
                {
                    realm.newRole( role );
                }
            }
            if ( realm.numberOfUsers() == 0 )
            {
                realm.newUser( "neo4j", "neo4j", true );
                // Make the default user admin for now
                realm.addUserToRole( "neo4j", PredefinedRolesBuilder.ADMIN );
            }
        }
    }

    @Override
    public void stop() throws Throwable
    {
        super.stop();

        roleRepository.stop();
    }

    @Override
    public void shutdown() throws Throwable
    {
        super.shutdown();

        roleRepository.shutdown();
        realm.setCacheManager( null );
        cacheManager.destroy();
    }

    @Override
    public User newUser( String username, String initialPassword, boolean requirePasswordChange ) throws IOException,
            IllegalCredentialsException
    {
        assertAuthEnabled();

        passwordPolicy.validatePassword( initialPassword );

        return realm.newUser( username, initialPassword, requirePasswordChange );
    }

    public RoleRecord newRole( String roleName, String... users ) throws IOException, IllegalCredentialsException
    {
        assertAuthEnabled();

        return realm.newRole( roleName, users );
    }

    @Override
    public AuthSubject login( Map<String,Object> authToken ) throws InvalidAuthTokenException
    {
        assertAuthEnabled();

        String username = AuthToken.safeCast( AuthToken.PRINCIPAL, authToken );
        String password = AuthToken.safeCast( AuthToken.CREDENTIALS, authToken );

        // Start with an anonymous subject
        Subject subject = buildSubject( null );

        UsernamePasswordToken token = new UsernamePasswordToken( username, password );
        AuthenticationResult result = AuthenticationResult.SUCCESS;

        if ( !authStrategy.isAuthenticationPermitted( username ) )
        {
            result = AuthenticationResult.TOO_MANY_ATTEMPTS;
        }
        else
        {
            try
            {
                subject.login( token );
                if ( realm.findUser( username ).passwordChangeRequired() )
                {
                    result = AuthenticationResult.PASSWORD_CHANGE_REQUIRED;
                }
            }
            catch ( AuthenticationException e )
            {
                result = AuthenticationResult.FAILURE;
            }
            authStrategy.updateWithAuthenticationResult( result, username );
        }
        return new ShiroAuthSubject( this, subject, result );
    }

    @Override
    public void setPassword( AuthSubject authSubject, String username, String password ) throws IOException,
            IllegalCredentialsException
    {
        ShiroAuthSubject shiroAuthSubject = ShiroAuthSubject.castOrFail( authSubject );

        if ( !shiroAuthSubject.doesUsernameMatch( username ) )
        {
            throw new AuthorizationViolationException( "Invalid attempt to change the password for user " + username );
        }

        setUserPassword( username, password );

        // This will invalidate the auth cache
        authSubject.logout();
    }

    @Override
    public void addUserToRole( String username, String roleName ) throws IOException
    {
        assertAuthEnabled();
        realm.addUserToRole( username, roleName );
    }

    @Override
    public void removeUserFromRole( String username, String roleName ) throws IOException
    {
        assertAuthEnabled();
        realm.removeUserFromRole( username, roleName );
    }

    @Override
    public boolean deleteUser( String username ) throws IOException
    {
        assertAuthEnabled();
        return realm.deleteUser( username );
    }

    void suspendUser( String username ) throws IOException
    {
        assertAuthEnabled();
        realm.suspendUser( username );
    }

    void activateUser( String username ) throws IOException
    {
        assertAuthEnabled();
        realm.activateUser( username );
    }

    @Override
    public Set<String> getAllRoleNames()
    {
        assertAuthEnabled();
        return roleRepository.getAllRoleNames();
    }

    @Override
    public Set<String> getRoleNamesForUser( String username )
    {
        assertAuthEnabled();
        if (users.getUserByName( username ) == null)
        {
            throw new IllegalArgumentException( "User " + username + " does not exist." );
        }
        return roleRepository.getRoleNamesByUsername( username );
    }

    @Override
    public Set<String> getUsernamesForRole( String roleName )
    {
        assertAuthEnabled();
        RoleRecord role = roleRepository.getRoleByName( roleName );
        if (role == null)
        {
            throw new IllegalArgumentException( "Role " + roleName + " does not exist." );
        }
        return role.users();
    }

    public Set<String> getAllUsernames()
    {
        assertAuthEnabled();
        return realm.getAllUsernames();
    }

    private Subject buildSubject( String username )
    {
        Subject.Builder subjectBuilder = new Subject.Builder( securityManager );

        if ( username != null )
        {
            PrincipalCollection identity = new SimplePrincipalCollection( username, realm.getName() );
            subjectBuilder = subjectBuilder.principals( identity );
        }

        return subjectBuilder.buildSubject();
    }
}