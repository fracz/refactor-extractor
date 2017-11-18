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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.neo4j.kernel.api.security.AuthSubject;
import org.neo4j.kernel.api.security.AuthenticationResult;
import org.neo4j.kernel.api.security.exception.IllegalCredentialsException;
import org.neo4j.server.security.auth.AuthenticationStrategy;
import org.neo4j.server.security.auth.Credential;
import org.neo4j.server.security.auth.PasswordPolicy;
import org.neo4j.server.security.auth.User;

import org.neo4j.server.security.auth.InMemoryUserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.server.security.auth.SecurityTestUtils.authToken;

public class ShiroAuthManagerTest
{
    private InMemoryUserRepository users;
    private InMemoryRoleRepository roles;
    private AuthenticationStrategy authStrategy;
    private PasswordPolicy passwordPolicy;
    private ShiroAuthManager manager;

    @Before
    public void setUp() throws Throwable
    {
        users = new InMemoryUserRepository();
        roles = new InMemoryRoleRepository();
        authStrategy = mock( AuthenticationStrategy.class );
        when( authStrategy.isAuthenticationPermitted( anyString() ) ).thenReturn( true );
        passwordPolicy = mock( PasswordPolicy.class );
        manager = new ShiroAuthManager( users, roles, passwordPolicy, authStrategy );
        manager.init();
    }

    @After
    public void tearDown() throws Throwable
    {
        manager.stop();
        manager.shutdown();
    }

    @Test
    public void shouldCreateDefaultUserIfNoneExist() throws Throwable
    {
        // Given

        // When
        manager.start();

        // Then
        final User user = users.getUserByName( "neo4j" );
        assertNotNull( user );
        assertTrue( user.credentials().matchesPassword( "neo4j" ) );
        assertTrue( user.passwordChangeRequired() );
    }

    @Test
    public void shouldFindAndAuthenticateUserSuccessfully() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123" , false );
        users.create( user );
        manager.start();

        // When
        AuthenticationResult result = manager.login( authToken( "jake", "abc123" ) ).getAuthenticationResult();

        // Then
        assertThat( result, equalTo( AuthenticationResult.SUCCESS ) );
    }

    @Test
    public void shouldFindAndAuthenticateUserAndReturnAuthStrategyResult() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123" , true );
        users.create( user );
        manager.start();
        when( authStrategy.isAuthenticationPermitted( user.name() )).thenReturn( false );

        // When
        AuthSubject authSubject = manager.login( authToken( "jake", "abc123" ) );
        AuthenticationResult result = authSubject.getAuthenticationResult();

        // Then
        assertThat( result, equalTo( AuthenticationResult.TOO_MANY_ATTEMPTS ) );
    }

    @Test
    public void shouldFindAndAuthenticateUserAndReturnPasswordChangeIfRequired() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123" , true );
        users.create( user );
        manager.start();

        // When
        AuthenticationResult result = manager.login( authToken( "jake", "abc123" ) ).getAuthenticationResult();

        // Then
        assertThat( result, equalTo( AuthenticationResult.PASSWORD_CHANGE_REQUIRED ) );
    }

    @Test
    public void shouldFailAuthenticationIfUserIsNotFound() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123" , true );
        users.create( user );
        manager.start();

        // When
        AuthSubject authSubject = manager.login( authToken( "unknown", "abc123" ) );
        AuthenticationResult result = authSubject.getAuthenticationResult();

        // Then
        assertThat( result, equalTo( AuthenticationResult.FAILURE ) );
    }

    @Test
    public void shouldCreateUser() throws Throwable
    {
        // Given
        manager.start();

        // When
        manager.newUser( "foo", "bar", true );

        // Then
        User user = users.getUserByName( "foo" );
        assertNotNull( user );
        assertTrue( user.passwordChangeRequired() );
        assertTrue( user.credentials().matchesPassword( "bar" ) );
    }

    @Test
    public void shouldDeleteUser() throws Throwable
    {
        System.out.println("shouldDeleteUser");

        // Given
        final User user = newUser( "jake", "abc123" , true );
        final User user2 = newUser( "craig", "321cba" , true );
        users.create( user );
        users.create( user2 );
        manager.start();

        // When
        manager.deleteUser( "jake" );

        // Then
        assertNull( users.getUserByName( "jake" ) );
        assertNotNull( users.getUserByName( "craig" ) );
    }

    @Test
    public void shouldFailDeletingUnknownUser() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123" , true );
        users.create( user );
        manager.start();

        // When
        try
        {
            manager.deleteUser( "unknown" );
            fail("Should throw exception on deleting unknown user");
        }
        catch ( IllegalArgumentException e )
        {
            e.getMessage().equals( "User 'unknown' does not exist" );
        }

        // Then
        assertNotNull( users.getUserByName( "jake" ) );
    }

    @Test
    public void shouldSuspendExistingUser() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123" , true );
        users.create( user );
        manager.start();

        // When
        manager.suspendUser( "jake" );

        // Then
        AuthSubject authSubject = manager.login( authToken( "jake", "abc123" ) );
        assertThat( authSubject.getAuthenticationResult(), equalTo( AuthenticationResult.FAILURE ) );
    }

    @Test
    public void shouldActivateExistingUser() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123", false );
        users.create( user );
        manager.start();
        manager.suspendUser( "jake" );

        // When
        manager.activateUser( "jake" );

        // Then
        AuthSubject authSubject = manager.login( authToken( "jake", "abc123" ) );
        assertThat( authSubject.getAuthenticationResult(), equalTo( AuthenticationResult.SUCCESS ) );
    }

    @Test
    public void shouldSuspendSuspendedUser() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123", false );
        users.create( user );
        manager.start();
        manager.suspendUser( "jake" );

        // When
        manager.suspendUser( "jake" );

        // Then
        AuthSubject authSubject = manager.login( authToken( "jake", "abc123" ) );
        assertThat( authSubject.getAuthenticationResult(), equalTo( AuthenticationResult.FAILURE ) );
    }

    @Test
    public void shouldActivateActiveUser() throws Throwable
    {
        // Given
        final User user = newUser( "jake", "abc123", false );
        users.create( user );
        manager.start();

        // When
        manager.activateUser( "jake" );

        // Then
        AuthSubject authSubject = manager.login( authToken( "jake", "abc123" ) );
        assertThat( authSubject.getAuthenticationResult(), equalTo( AuthenticationResult.SUCCESS ) );
    }

    @Test
    public void shouldFailToSuspendNonExistingUser() throws Throwable
    {
        // Given
        manager.start();

        // When
        try
        {
            manager.suspendUser( "jake" );
            fail( "Should throw exception on suspending unknown user" );
        }
        catch ( IllegalArgumentException e )
        {
            // Then
            assertThat(e.getMessage(), containsString("User jake does not exist"));
        }
    }

    @Test
    public void shouldFailToActivateNonExistingUser() throws Throwable
    {
        // Given
        manager.start();

        // When
        try
        {
            manager.activateUser( "jake" );
            fail( "Should throw exception on activating unknown user" );
        }
        catch ( IllegalArgumentException e )
        {
            // Then
            assertThat(e.getMessage(), containsString("User jake does not exist"));
        }
    }

    @Test
    public void shouldSetPassword() throws Throwable
    {
        // Given
        users.create( newUser( "jake", "abc123", true ) );
        manager.start();

        // When
        manager.setUserPassword( "jake", "hello, world!" );

        // Then
        User user = manager.getUser( "jake" );
        assertTrue( user.credentials().matchesPassword( "hello, world!" ) );
        assertThat( users.getUserByName( "jake" ), equalTo( user ) );
    }

    @Test
    public void shouldSetPasswordThroughAuthSubject() throws Throwable
    {
        // Given
        users.create( newUser( "neo", "abc123", true ) );
        manager.start();

        // When
        AuthSubject authSubject = manager.login( authToken( "neo", "abc123" ) );
        assertThat( authSubject.getAuthenticationResult(), equalTo( AuthenticationResult.PASSWORD_CHANGE_REQUIRED ) );

        authSubject.setPassword( "hello, world!" );

        // Then
        User user = manager.getUser( "neo" );
        assertTrue( user.credentials().matchesPassword( "hello, world!" ) );
        assertThat( users.getUserByName( "neo" ), equalTo( user ) );

        authSubject.logout();
        authSubject = manager.login( authToken( "neo", "hello, world!" ) );
        assertThat( authSubject.getAuthenticationResult(), equalTo( AuthenticationResult.SUCCESS ) );
    }

    @Test
    public void shouldNotRequestPasswordChangeWithInvalidCredentials() throws Throwable
    {
        // Given
        users.create( newUser( "neo", "abc123", true ) );
        manager.start();

        // When
        AuthSubject authSubject = manager.login( authToken( "neo", "wrong" ) );

        // Then
        assertThat( authSubject.getAuthenticationResult(), equalTo( AuthenticationResult.FAILURE ) );
    }

    @Test
    public void shouldReturnNullWhenSettingPasswordForUnknownUser() throws Throwable
    {
        // Given
        manager.start();

        // When
        try
        {
            manager.setUserPassword( "unknown", "hello, world!" );
            fail( "exception expected" );
        }
        catch ( IllegalCredentialsException e )
        {
            // expected
        }
    }

    private void createTestUsers() throws Throwable
    {
        manager.newUser( "morpheus", "abc123", false );
        manager.newRole( "admin", "morpheus" );
        manager.newUser( "trinity", "abc123", false );
        manager.newRole( "architect", "trinity" );
        manager.newUser( "tank", "abc123", false );
        manager.newRole( "publisher", "tank" );
        manager.newUser( "neo", "abc123", false );
        manager.newRole( "reader", "neo" );
        manager.newUser( "smith", "abc123", false );
        manager.newRole( "agent", "smith" );
    }

    @Test
    public void defaultUserShouldHaveCorrectPermissions() throws Throwable
    {
        // Given
        manager.start();

        // When
        AuthSubject subject = manager.login( authToken( "neo4j", "neo4j" ) );
        manager.setUserPassword( "neo4j", "1234" );
        subject.logout();
        subject = manager.login( authToken( "neo4j", "1234" ) );

        // Then
        assertTrue( subject.allowsReads() );
        assertTrue( subject.allowsWrites() );
        assertTrue( subject.allowsSchemaWrites() );
    }

    @Test
    public void userWithAdminRoleShouldHaveCorrectPermissions() throws Throwable
    {
        // Given
        createTestUsers();
        manager.start();

        // When
        AuthSubject subject = manager.login( authToken( "morpheus", "abc123" ) );

        // Then
        assertTrue( subject.allowsReads() );
        assertTrue( subject.allowsWrites() );
        assertTrue( subject.allowsSchemaWrites() );
    }

    @Test
    public void userWithArchitectRoleShouldHaveCorrectPermissions() throws Throwable
    {
        // Given
        createTestUsers();
        manager.start();

        // When
        AuthSubject subject = manager.login( authToken( "trinity", "abc123" ) );

        // Then
        assertTrue( subject.allowsReads() );
        assertTrue( subject.allowsWrites() );
        assertTrue( subject.allowsSchemaWrites() );
    }

    @Test
    public void userWithPublisherRoleShouldHaveCorrectPermissions() throws Throwable
    {
        // Given
        createTestUsers();
        manager.start();

        // When
        AuthSubject subject = manager.login( authToken( "tank", "abc123" ) );

        // Then
        assertTrue( "should allow reads", subject.allowsReads() );
        assertTrue( "should allow writes", subject.allowsWrites() );
        assertFalse( "should _not_ allow schema writes", subject.allowsSchemaWrites() );
    }

    @Test
    public void userWithReaderRoleShouldHaveCorrectPermissions() throws Throwable
    {
        // Given
        createTestUsers();
        manager.start();

        // When
        AuthSubject subject = manager.login( authToken( "neo", "abc123" ) );

        // Then
        assertTrue( subject.allowsReads() );
        assertFalse( subject.allowsWrites() );
        assertFalse( subject.allowsSchemaWrites() );
    }

    @Test
    public void userWithNonPredefinedRoleShouldHaveNoPermissions() throws Throwable
    {
        // Given
        createTestUsers();
        manager.start();

        // When
        AuthSubject subject = manager.login( authToken( "smith", "abc123" ) );

        // Then
        assertFalse( subject.allowsReads() );
        assertFalse( subject.allowsWrites() );
        assertFalse( subject.allowsSchemaWrites() );
    }

    @Test
    public void shouldHaveNoPermissionsAfterLogout() throws Throwable
    {
        // Given
        createTestUsers();
        manager.start();

        // When
        AuthSubject subject = manager.login( authToken( "morpheus", "abc123" ) );
        assertTrue( subject.allowsReads() );
        assertTrue( subject.allowsWrites() );
        assertTrue( subject.allowsSchemaWrites() );

        subject.logout();

        // Then
        assertFalse( subject.allowsReads() );
        assertFalse( subject.allowsWrites() );
        assertFalse( subject.allowsSchemaWrites() );
    }

    private User newUser( String userName, String password, boolean pwdChange )
    {
        return new User.Builder( userName, Credential.forPassword( password ))
                    .withRequiredPasswordChange( pwdChange )
                    .build();
    }
}