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
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
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
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.neo4j.kernel.api.security.AuthToken;
import org.neo4j.kernel.api.security.AuthenticationResult;
import org.neo4j.kernel.api.security.exception.InvalidArgumentsException;
import org.neo4j.kernel.api.security.exception.InvalidAuthTokenException;
import org.neo4j.kernel.impl.util.JobScheduler;
import org.neo4j.logging.Log;
import org.neo4j.server.security.auth.AuthenticationStrategy;
import org.neo4j.server.security.auth.Credential;
import org.neo4j.server.security.auth.ListSnapshot;
import org.neo4j.server.security.auth.PasswordPolicy;
import org.neo4j.server.security.auth.User;
import org.neo4j.server.security.auth.UserRepository;
import org.neo4j.server.security.auth.exception.ConcurrentModificationException;

import static java.lang.String.format;
import static org.neo4j.helpers.Strings.escape;

/**
 * Shiro realm wrapping FileUserRepository and FileRoleRepository
 */
public class InternalFlatFileRealm extends AuthorizingRealm implements RealmLifecycle, EnterpriseUserManager
{
    /**
     * This flag is used in the same way as User.PASSWORD_CHANGE_REQUIRED, but it's
     * placed here because of user suspension not being a part of community edition
     */
    public static final String IS_SUSPENDED = "is_suspended";

    private int MAX_READ_ATTEMPTS = 10;

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
    private final boolean authorizationEnabled;
    private final Log securityLog;
    private final Map<String,SimpleRole> roles;
    private final JobScheduler jobScheduler;
    private JobScheduler.JobHandle reloadJobHandle;

    public InternalFlatFileRealm( UserRepository userRepository, RoleRepository roleRepository,
            PasswordPolicy passwordPolicy, AuthenticationStrategy authenticationStrategy,
            JobScheduler jobScheduler, Log securityLog )
    {
        this( userRepository, roleRepository, passwordPolicy, authenticationStrategy, true, true,
                jobScheduler, securityLog );
    }

    InternalFlatFileRealm( UserRepository userRepository, RoleRepository roleRepository,
            PasswordPolicy passwordPolicy, AuthenticationStrategy authenticationStrategy,
            boolean authenticationEnabled, boolean authorizationEnabled, JobScheduler jobScheduler, Log securityLog )
    {
        super();

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordPolicy = passwordPolicy;
        this.authenticationStrategy = authenticationStrategy;
        this.authenticationEnabled = authenticationEnabled;
        this.authorizationEnabled = authorizationEnabled;
        this.jobScheduler = jobScheduler;
        setAuthenticationCachingEnabled( false );
        setAuthorizationCachingEnabled( false );
        this.securityLog = securityLog;
        setCredentialsMatcher( new AllowAllCredentialsMatcher() );
        setRolePermissionResolver( rolePermissionResolver );

        roles = new PredefinedRolesBuilder().buildRoles();
    }

    private void setUsersAndRoles( ListSnapshot<User> users, ListSnapshot<RoleRecord> roles )
            throws IOException, InvalidArgumentsException
    {
        synchronized ( this )
        {
            userRepository.setUsers( users );
            roleRepository.setRoles( roles );
        }
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

        ensureDefaultUsers();
        ensureDefaultRoles();

        reloadJobHandle = jobScheduler.scheduleRecurring(
                JobScheduler.Groups.nativeSecurity,
                () -> readFilesFromDisk( MAX_READ_ATTEMPTS, new LinkedList<>() ),
                5, 5, TimeUnit.SECONDS );
    }

    private void readFilesFromDisk( int attemptLeft, java.util.List<String> failures )
    {
        if ( attemptLeft < 0 )
        {
            throw new RuntimeException( "Unable to load valid flat file repositories! Attempts failed with:\n\t" +
                    String.join( "\n\t", failures ) );
        }

        try
        {
            ListSnapshot<User> users = userRepository.getPersistedSnapshot();
            ListSnapshot<RoleRecord> roles = roleRepository.getPersistedSnapshot();
            if ( RoleRepository.validate( users.values, roles.values ) )
            {
                setUsersAndRoles( users, roles );
            }
            else
            {
                failures.add( "Role-auth file combination not valid." );
                Thread.sleep( 10 );
                readFilesFromDisk( attemptLeft - 1, failures );
            }
        }
        catch ( IOException | IllegalStateException | InterruptedException | InvalidArgumentsException e )
        {
            failures.add( e.getMessage() );
            readFilesFromDisk( attemptLeft - 1, failures );
        }
    }

    /* Adds neo4j user if no users exist */
    private void ensureDefaultUsers() throws IOException, InvalidArgumentsException
    {
        if ( authenticationEnabled || authorizationEnabled )
        {
            if ( numberOfUsers() == 0 )
            {
                newUser( "neo4j", "neo4j", true );
            }
        }
    }

    /* Builds all predefined roles if no roles exist. Adds 'neo4j' to admin role if no admin is assigned */
    private void ensureDefaultRoles() throws IOException, InvalidArgumentsException
    {
        if ( authenticationEnabled || authorizationEnabled )
        {
            if ( numberOfRoles() == 0 )
            {
                for ( String role : roles.keySet() )
                {
                    newRole( role );
                }
            }
            if ( this.getUsernamesForRole( PredefinedRolesBuilder.ADMIN ).size() == 0 )
            {
                if ( getAllUsernames().contains( "neo4j" ) )
                {
                    addRoleToUser( PredefinedRolesBuilder.ADMIN, "neo4j" );
                }
            }
        }
    }

    @Override
    public void stop() throws Throwable
    {
        userRepository.stop();
        roleRepository.stop();

        if ( reloadJobHandle != null )
        {
            reloadJobHandle.cancel( true );
            reloadJobHandle = null;
        }
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
                ShiroAuthToken shiroAuthToken = (ShiroAuthToken) token;
                return shiroAuthToken.getScheme().equals( AuthToken.BASIC_SCHEME ) &&
                       (shiroAuthToken.supportsRealm( AuthToken.NATIVE_REALM ));
            }
            return false;
        }
        catch ( InvalidAuthTokenException e )
        {
            return false;
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals ) throws AuthenticationException
    {
        if ( !authorizationEnabled )
        {
            return null;
        }

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
        if ( !authenticationEnabled )
        {
            return null;
        }

        ShiroAuthToken shiroAuthToken = (ShiroAuthToken) token;

        String username;
        String password;
        try
        {
            username = AuthToken.safeCast( AuthToken.PRINCIPAL, shiroAuthToken.getAuthTokenMap() );
            password = AuthToken.safeCast( AuthToken.CREDENTIALS, shiroAuthToken.getAuthTokenMap() );
        }
        catch ( InvalidAuthTokenException e )
        {
            throw new UnsupportedTokenException( e );
        }

        User user = userRepository.getUserByName( username );
        if ( user == null )
        {
            throw new UnknownAccountException();
        }

        AuthenticationResult result = authenticationStrategy.authenticate( user, password );

        switch ( result )
        {
        case FAILURE:
            throw new IncorrectCredentialsException();
        case TOO_MANY_ATTEMPTS:
            throw new ExcessiveAttemptsException();
        default:
            break;
        }

        // TODO: This will not work if AuthenticationInfo is cached,
        // unless you always do SecurityManager.logout properly (which will invalidate the cache)
        // For REST we may need to connect HttpSessionListener.sessionDestroyed with logout
        if ( user.hasFlag( InternalFlatFileRealm.IS_SUSPENDED ) )
        {
            throw new DisabledAccountException( "User '" + user.name() + "' is suspended." );
        }

        if ( user.passwordChangeRequired() )
        {
            result = AuthenticationResult.PASSWORD_CHANGE_REQUIRED;
        }

        return new ShiroAuthenticationInfo( user.name(), user.credentials(), getName(), result );
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
    public User newUser( String username, String initialPassword, boolean requirePasswordChange )
            throws IOException, InvalidArgumentsException
    {
        try
        {
            passwordPolicy.validatePassword( initialPassword );

            User user = new User.Builder()
                    .withName( username )
                    .withCredentials( Credential.forPassword( initialPassword ) )
                    .withRequiredPasswordChange( requirePasswordChange )
                    .build();
            userRepository.create( user );

            securityLog.info( "User created: `%s`" + (requirePasswordChange ? " (password change required)" : ""),
                    username );
            return user;
        }
        catch ( InvalidArgumentsException e )
        {
            securityLog.error( "User creation failed for user `%s`: %s", escape( username ), e.getMessage() );
            throw e;
        }
    }

    @Override
    public RoleRecord newRole( String roleName, String... usernames ) throws IOException, InvalidArgumentsException
    {
        assertValidRoleName( roleName );
        for ( String username : usernames )
        {
            assertValidUsername( username );
        }

        SortedSet<String> userSet = new TreeSet<>( Arrays.asList( usernames ) );

        RoleRecord role = new RoleRecord.Builder().withName( roleName ).withUsers( userSet ).build();
        roleRepository.create( role );

        return role;
    }

    @Override
    public boolean deleteRole( String roleName ) throws IOException, InvalidArgumentsException
    {
        assertNotPredefinedRoleName( roleName );

        boolean result = false;
        synchronized ( this )
        {
            RoleRecord role = getRole( roleName );  // asserts role name exists
            if ( roleRepository.delete( role ) )
            {
                result = true;
            }
            else
            {
                // We should not get here, but if we do the assert will fail and give a nice error msg
                getRole( roleName );
            }
        }
        return result;
    }

    @Override
    public RoleRecord getRole( String roleName ) throws InvalidArgumentsException
    {
        RoleRecord role = roleRepository.getRoleByName( roleName );
        if ( role == null )
        {
            throw new InvalidArgumentsException( "Role '" + roleName + "' does not exist." );
        }
        return role;
    }

    @Override
    public void addRoleToUser( String roleName, String username ) throws IOException, InvalidArgumentsException
    {
        assertValidRoleName( roleName );
        assertValidUsername( username );

        synchronized ( this )
        {
            getUser( username );
            RoleRecord role = getRole( roleName );
            RoleRecord newRole = role.augment().withUser( username ).build();
            try
            {
                roleRepository.update( role, newRole );
            }
            catch ( ConcurrentModificationException e )
            {
                // Try again
                addRoleToUser( roleName, username );
            }
        }
        clearCachedAuthorizationInfoForUser( username );
    }

    @Override
    public void removeRoleFromUser( String roleName, String username ) throws IOException, InvalidArgumentsException
    {
        assertValidRoleName( roleName );
        assertValidUsername( username );

        synchronized ( this )
        {
            getUser( username );
            RoleRecord role = getRole( roleName );

            RoleRecord newRole = role.augment().withoutUser( username ).build();
            try
            {
                roleRepository.update( role, newRole );
            }
            catch ( ConcurrentModificationException e )
            {
                // Try again
                removeRoleFromUser( roleName, username );
            }
        }
        clearCachedAuthorizationInfoForUser( username );
    }

    @Override
    public boolean deleteUser( String username ) throws IOException, InvalidArgumentsException
    {
        boolean result = false;
        try
        {
            synchronized ( this )
            {
                User user = getUser( username );
                if ( userRepository.delete( user ) )
                {
                    removeUserFromAllRoles( username );
                    result = true;
                }
                else
                {
                    // We should not get here, but if we do the assert will fail and give a nice error msg
                    getUser( username );
                }
            }
            clearCacheForUser( username );
            securityLog.info( "User deleted: `%s`", username );
        }
        catch ( InvalidArgumentsException e )
        {
            securityLog.error( "User deletion failed for user `%s`: %s", username, e.getMessage() );
        }
        return result;
    }

    @Override
    public User getUser( String username ) throws InvalidArgumentsException
    {
        User u = userRepository.getUserByName( username );
        if ( u == null )
        {
            throw new InvalidArgumentsException( "User '" + username + "' does not exist." );
        }
        return u;
    }

    @Override
    public void setUserPassword( String username, String password, boolean requirePasswordChange )
            throws IOException, InvalidArgumentsException
    {
        User existingUser = getUser( username );

        passwordPolicy.validatePassword( password );

        if ( existingUser.credentials().matchesPassword( password ) )
        {
            throw new InvalidArgumentsException( "Old password and new password cannot be the same." );
        }

        try
        {
            User updatedUser = existingUser.augment()
                    .withCredentials( Credential.forPassword( password ) )
                    .withRequiredPasswordChange( requirePasswordChange )
                    .build();
            userRepository.update( existingUser, updatedUser );
        }
        catch ( ConcurrentModificationException e )
        {
            // try again
            setUserPassword( username, password, requirePasswordChange );
        }

        clearCacheForUser( username );
    }

    @Override
    public void suspendUser( String username ) throws IOException, InvalidArgumentsException
    {
        // This method is not synchronized as it only modifies the UserRepository, which is synchronized in itself
        // If user is modified between getUserByName and update, we get ConcurrentModificationException and try again
        User user = getUser( username );
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
    public void activateUser( String username, boolean requirePasswordChange )
            throws IOException, InvalidArgumentsException
    {
        // This method is not synchronized as it only modifies the UserRepository, which is synchronized in itself
        // If user is modified between getUserByName and update, we get ConcurrentModificationException and try again
        User user = getUser( username );
        if ( user.hasFlag( IS_SUSPENDED ) )
        {
            User activatedUser = user.augment()
                    .withoutFlag( IS_SUSPENDED )
                    .withRequiredPasswordChange( requirePasswordChange )
                    .build();
            try
            {
                userRepository.update( user, activatedUser );
            }
            catch ( ConcurrentModificationException e )
            {
                // Try again
                activateUser( username, requirePasswordChange );
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
    public Set<String> getRoleNamesForUser( String username ) throws InvalidArgumentsException
    {
        getUser( username );
        return roleRepository.getRoleNamesByUsername( username );
    }

    @Override
    public Set<String> getUsernamesForRole( String roleName ) throws InvalidArgumentsException
    {
        RoleRecord role = getRole( roleName );
        return role.users();
    }

    @Override
    public Set<String> getAllUsernames()
    {
        return userRepository.getAllUsernames();
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

    private void assertValidUsername( String name ) throws InvalidArgumentsException
    {
        if ( name.isEmpty() )
        {
            throw new InvalidArgumentsException( "The provided user name is empty." );
        }
        if ( !userRepository.isValidUsername( name ) )
        {
            throw new InvalidArgumentsException(
                    "User name '" + name + "' contains illegal characters. Use simple ascii characters and numbers." );
        }
    }

    private void assertValidRoleName( String name ) throws InvalidArgumentsException
    {
        if ( name.isEmpty() )
        {
            throw new InvalidArgumentsException( "The provided role name is empty." );
        }
        if ( !roleRepository.isValidRoleName( name ) )
        {
            throw new InvalidArgumentsException(
                    "Role name '" + name + "' contains illegal characters. Use simple ascii characters and numbers." );
        }
    }

    private void assertNotPredefinedRoleName( String roleName ) throws InvalidArgumentsException
    {
        if ( PredefinedRolesBuilder.roles.keySet().contains( roleName ) )
        {
            throw new InvalidArgumentsException(
                    format( "'%s' is a predefined role and can not be deleted.", roleName ) );
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