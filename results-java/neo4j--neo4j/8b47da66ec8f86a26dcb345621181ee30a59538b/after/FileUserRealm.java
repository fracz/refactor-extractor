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
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.neo4j.graphdb.security.AuthorizationViolationException;
import org.neo4j.kernel.api.security.AuthSubject;
import org.neo4j.kernel.api.security.AuthToken;
import org.neo4j.kernel.api.security.exception.IllegalCredentialsException;
import org.neo4j.kernel.api.security.exception.InvalidAuthTokenException;
import org.neo4j.server.security.auth.AuthenticationStrategy;
import org.neo4j.server.security.auth.Credential;
import org.neo4j.server.security.auth.PasswordPolicy;
import org.neo4j.server.security.auth.User;
import org.neo4j.server.security.auth.UserRepository;
import org.neo4j.server.security.auth.exception.ConcurrentModificationException;

/**
 * Shiro realm wrapping FileUserRepository
 */
public class FileUserRealm extends AuthorizingRealm implements NeoLifecycleRealm, EnterpriseUserManager
{
    /**
     * This flag is used in the same way as User.PASSWORD_CHANGE_REQUIRED, but it's
     * placed here because of user suspension not being a part of community edition
     */
    public static final String IS_SUSPENDED = "is_suspended";

    private final CredentialsMatcher credentialsMatcher =
            ( AuthenticationToken token, AuthenticationInfo info ) ->
            {
                UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
                String infoUserName = (String) info.getPrincipals().getPrimaryPrincipal();
                Credential infoCredential = (Credential) info.getCredentials();

                boolean userNameMatches = infoUserName.equals( usernamePasswordToken.getUsername() );
                boolean credentialsMatches =
                        infoCredential.matchesPassword( new String( usernamePasswordToken.getPassword() ) );

                return userNameMatches && credentialsMatches;
            };

    private final RolePermissionResolver rolePermissionResolver = new RolePermissionResolver()
    {
        @Override
        public Collection<Permission> resolvePermissionsInRole( String roleString )
        {
            SimpleRole role = roles.get( roleString );
            if ( role != null )
            {
                return role.getPermissions();
            }
            else
            {
                return Collections.emptyList();
            }
        }
    };

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordPolicy passwordPolicy;
    private final AuthenticationStrategy authenticationStrategy;
    private final boolean authenticationEnabled;
    private final Map<String,SimpleRole> roles;

    public FileUserRealm( UserRepository userRepository, RoleRepository roleRepository, PasswordPolicy passwordPolicy,
            AuthenticationStrategy authenticationStrategy, boolean authenticationEnabled )
    {
        super();

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordPolicy = passwordPolicy;
        this.authenticationStrategy = authenticationStrategy;
        this.authenticationEnabled = authenticationEnabled;
        setCredentialsMatcher( credentialsMatcher );
        setRolePermissionResolver( rolePermissionResolver );

        roles = new PredefinedRolesBuilder().buildRoles();
    }

    @Override
    public void initialize() throws Throwable
    {
        userRepository.init();
        roleRepository.init();
    }

    @Override
    public void start() throws Throwable
    {
        userRepository.start();
        roleRepository.start();

        if ( numberOfUsers() == 0 )
        {
            newUser( "neo4j", "neo4j", true );

            if ( numberOfRoles() == 0 )
            {
                // Make the default user admin for now
                newRole( PredefinedRolesBuilder.ADMIN, "neo4j" );
            }
        }
    }

    @Override
    public void stop() throws Throwable
    {
        userRepository.stop();
        roleRepository.stop();
    }

    @Override
    public void shutdown() throws Throwable
    {
        userRepository.shutdown();
        roleRepository.shutdown();
        setCacheManager( null );
    }

    @Override
    public boolean supports( AuthenticationToken token )
    {
        try
        {
            if ( token instanceof ShiroAuthToken )
            {
                return ((ShiroAuthToken) token).getScheme().equals( "basic" );
            }
            return false;
        }
        catch( InvalidAuthTokenException e )
        {
            return false;
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals ) throws AuthenticationException
    {
        String username = (String) getAvailablePrincipal( principals );
        if ( username == null )
        {
            return null;
        }

        User user = userRepository.getUserByName( username );
        if ( user == null )
        {
            return null;
        }

        if ( user.passwordChangeRequired() || user.hasFlag( IS_SUSPENDED ) )
        {
            return new SimpleAuthorizationInfo();
        }
        else
        {
            Set<String> roles = roleRepository.getRoleNamesByUsername( user.name() );
            return new SimpleAuthorizationInfo( roles );
        }
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token ) throws AuthenticationException
    {
        ShiroAuthToken shiroAuthToken = (ShiroAuthToken) token;

        String username;
        try
        {
            username = AuthToken.safeCast( AuthToken.PRINCIPAL, shiroAuthToken.getMap() );
        }
        catch ( InvalidAuthTokenException e )
        {
            throw new UnsupportedTokenException( e );
        }

        User user = userRepository.getUserByName( username );
        if ( user == null )
        {
            throw new AuthenticationException( "User " + username + " does not exist" );
        }

        SimpleAuthenticationInfo authenticationInfo =
                new SimpleAuthenticationInfo( user.name(), user.credentials(), getName() );

        // TODO: This will not work if AuthenticationInfo is cached,
        // unless you always do SecurityManager.logout properly (which will invalidate the cache)
        // For REST we may need to connect HttpSessionListener.sessionDestroyed with logout
        if ( user.hasFlag( FileUserRealm.IS_SUSPENDED ) )
        {
            // We don' want un-authenticated users to learn anything about user suspension state
            // (normally this assertion is done by Shiro after we return from this method)
            assertCredentialsMatch( token, authenticationInfo );
            throw new AuthenticationException( "User " + user.name() + " is suspended" );
        }

        return authenticationInfo;
    }

    int numberOfUsers()
    {
        return userRepository.numberOfUsers();
    }

    int numberOfRoles()
    {
        return roleRepository.numberOfRoles();
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
    public User newUser( String username, String initialPassword, boolean requirePasswordChange )
            throws IOException, IllegalCredentialsException
    {
        assertValidUsername( username );

        User user = new User.Builder()
                .withName( username )
                .withCredentials( Credential.forPassword( initialPassword ) )
                .withRequiredPasswordChange( requirePasswordChange )
                .build();
        userRepository.create( user );

        return user;
    }

    RoleRecord newRole( String roleName, String... usernames ) throws IOException
    {
        assertValidRoleName( roleName );
        for ( String username : usernames )
        {
            assertValidUsername( username );
        }

        SortedSet<String> userSet = new TreeSet<String>( Arrays.asList( usernames ) );

        RoleRecord role = new RoleRecord.Builder().withName( roleName ).withUsers( userSet ).build();
        roleRepository.create( role );

        return role;
    }

    @Override
    public void addUserToRole( String username, String roleName ) throws IOException
    {
        checkValidityOfUsernameAndRoleName( username, roleName );

        synchronized ( this )
        {
            User user = userRepository.getUserByName( username );
            if ( user == null )
            {
                throw new IllegalArgumentException( "User " + username + " does not exist." );
            }
            RoleRecord role = roleRepository.getRoleByName( roleName );
            if ( role == null )
            {
                throw new IllegalArgumentException( "Role " + roleName + " does not exist." );
            }
            else
            {
                RoleRecord newRole = role.augment().withUser( username ).build();
                try
                {
                    roleRepository.update( role, newRole );
                }
                catch ( ConcurrentModificationException e )
                {
                    // Try again
                    addUserToRole( username, roleName );
                }
            }
        }
        clearCachedAuthorizationInfoForUser( username );
    }

    @Override
    public void removeUserFromRole( String username, String roleName ) throws IOException
    {
        checkValidityOfUsernameAndRoleName( username, roleName );

        synchronized ( this )
        {
            User user = userRepository.getUserByName( username );
            if ( user == null )
            {
                throw new IllegalArgumentException( "User " + username + " does not exist." );
            }
            RoleRecord role = roleRepository.getRoleByName( roleName );
            if ( role == null )
            {
                throw new IllegalArgumentException( "Role " + roleName + " does not exist." );
            }
            else
            {
                RoleRecord newRole = role.augment().withoutUser( username ).build();
                try
                {
                    roleRepository.update( role, newRole );
                }
                catch ( ConcurrentModificationException e )
                {
                    // Try again
                    removeUserFromRole( username, roleName );
                }
            }
        }
        clearCachedAuthorizationInfoForUser( username );
    }

    @Override
    public boolean deleteUser( String username ) throws IOException
    {
        boolean result = false;
        synchronized ( this )
        {
            User user = userRepository.getUserByName( username );
            if ( user != null && userRepository.delete( user ) )
            {
                removeUserFromAllRoles( username );
                result = true;
            }
            else
            {
                throw new IllegalArgumentException( "The user '" + username + "' does not exist" );
            }
        }
        clearCacheForUser( username );
        return result;
    }

    @Override
    public User getUser( String username )
    {
        return null;
    }

    @Override
    public void setUserPassword( String username, String password ) throws IOException, IllegalCredentialsException
    {
        User existingUser = userRepository.getUserByName( username );
        if ( existingUser == null )
        {
            throw new IllegalCredentialsException( "User " + username + " does not exist" );
        }

        passwordPolicy.validatePassword( password );

        if ( existingUser.credentials().matchesPassword( password ) )
        {
            throw new IllegalCredentialsException( "Old password and new password cannot be the same." );
        }

        try
        {
            User updatedUser = existingUser.augment()
                    .withCredentials( Credential.forPassword( password ) )
                    .withRequiredPasswordChange( false )
                    .build();
            userRepository.update( existingUser, updatedUser );
        } catch ( ConcurrentModificationException e )
        {
            // try again
            setUserPassword( username, password );
        }
    }

    @Override
    public void suspendUser( String username ) throws IOException
    {
        // This method is not synchronized as it only modifies the UserRepository, which is synchronized in itself
        // If user is modified between getUserByName and update, we get ConcurrentModificationException and try again
        User user = userRepository.getUserByName( username );
        if ( user == null )
        {
            throw new IllegalArgumentException( "User " + username + " does not exist." );
        }
        if ( !user.hasFlag( IS_SUSPENDED ) )
        {
            User suspendedUser = user.augment().withFlag( IS_SUSPENDED ).build();
            try
            {
                userRepository.update( user, suspendedUser );
            }
            catch ( ConcurrentModificationException e )
            {
                // Try again
                suspendUser( username );
            }
        }
        clearCacheForUser( username );
    }

    @Override
    public void activateUser( String username ) throws IOException
    {
        // This method is not synchronized as it only modifies the UserRepository, which is synchronized in itself
        // If user is modified between getUserByName and update, we get ConcurrentModificationException and try again
        User user = userRepository.getUserByName( username );
        if ( user == null )
        {
            throw new IllegalArgumentException( "User " + username + " does not exist." );
        }
        if ( user.hasFlag( IS_SUSPENDED ) )
        {
            User activatedUser = user.augment().withoutFlag( IS_SUSPENDED ).build();
            try
            {
                userRepository.update( user, activatedUser );
            }
            catch ( ConcurrentModificationException e )
            {
                // Try again
                activateUser( username );
            }
        }
        clearCacheForUser( username );
    }

    @Override
    public Set<String> getAllRoleNames()
    {
        return roleRepository.getAllRoleNames();
    }

    @Override
    public Set<String> getRoleNamesForUser( String username )
    {
        if ( userRepository.getUserByName( username ) == null )
        {
            throw new IllegalArgumentException( "User " + username + " does not exist." );
        }
        return roleRepository.getRoleNamesByUsername( username );
    }

    @Override
    public Set<String> getUsernamesForRole( String roleName )
    {
        RoleRecord role = roleRepository.getRoleByName( roleName );
        if ( role == null )
        {
            throw new IllegalArgumentException( "Role " + roleName + " does not exist." );
        }
        return role.users();
    }

    @Override
    public Set<String> getAllUsernames()
    {
        return userRepository.getAllUsernames();
    }

    User findUser( String username )
    {
        return userRepository.getUserByName( username );
    }

    private void removeUserFromAllRoles( String username ) throws IOException
    {
        try
        {
            roleRepository.removeUserFromAllRoles( username );
        }
        catch ( ConcurrentModificationException e )
        {
            // Try again
            removeUserFromAllRoles( username );
        }
    }

    private void checkValidityOfUsernameAndRoleName( String username, String roleName ) throws IllegalArgumentException
    {
        assertValidUsername( username );
        assertValidRoleName( roleName );
    }

    private void assertValidUsername( String name )
    {
        if ( !userRepository.isValidUsername( name ) )
        {
            throw new IllegalArgumentException(
                    "User name contains illegal characters. Please use simple ascii characters and numbers." );
        }
    }

    private void assertValidRoleName( String name )
    {
        if ( !roleRepository.isValidRoleName( name ) )
        {
            throw new IllegalArgumentException(
                    "Role name contains illegal characters. Please use simple ascii characters and numbers." );
        }
    }

    private void clearCachedAuthorizationInfoForUser( String username )
    {
        clearCachedAuthorizationInfo( new SimplePrincipalCollection( username, this.getName() ) );
    }

    private void clearCacheForUser( String username )
    {
        clearCache( new SimplePrincipalCollection( username, this.getName() ) );
    }
}