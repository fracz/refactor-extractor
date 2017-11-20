package org.jasig.cas.authentication;

import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link PolicyBasedAuthenticationManager}.
 *
 * @author Marvin S. Addison
 */
public class PolicyBasedAuthenticationManagerTests {

    @Test
    public void testAuthenticateAnySuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(true),
                newMockHandler(false));
        final Authentication auth = manager.authenticate(mock(Credentials.class), mock(Credentials.class));
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test
    public void testAuthenticateAnyButTryAllSuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(true),
                newMockHandler(false));
        final AnyAuthenticationPolicy any = new AnyAuthenticationPolicy();
        any.setTryAll(true);
        manager.setAuthenticationPolicy(any);
        final Authentication auth = manager.authenticate(mock(Credentials.class), mock(Credentials.class));
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticateAnyFailure() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(false),
                newMockHandler(false));
        manager.authenticate(mock(Credentials.class), mock(Credentials.class));
        fail("Should have thrown AuthenticationException");
    }

    @Test
    public void testAuthenticateAllSuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(true),
                newMockHandler(true));
        manager.setAuthenticationPolicy(new AllAuthenticationPolicy());
        final Authentication auth = manager.authenticate(mock(Credentials.class), mock(Credentials.class));
        assertEquals(2, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticateAllFailure() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler(false),
                newMockHandler(false));
        manager.setAuthenticationPolicy(new AllAuthenticationPolicy());
        manager.authenticate(mock(Credentials.class), mock(Credentials.class));
        fail("Should have thrown AuthenticationException");
    }

    @Test
    public void testAuthenticateRequiredHandlerSuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler("HandlerA", true),
                newMockHandler("HandlerB", false));
        manager.setAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy("HandlerA"));
        final Authentication auth = manager.authenticate(mock(Credentials.class), mock(Credentials.class));
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(0, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticateRequiredHandlerFailure() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler("HandlerA", true),
                newMockHandler("HandlerB", false));
        manager.setAuthenticationPolicy(new RequiredHandlerAuthenticationPolicy("HandlerB"));
        manager.authenticate(mock(Credentials.class), mock(Credentials.class));
        fail("Should have thrown AuthenticationException");
    }

    @Test
    public void testAuthenticateRequiredHandlerTryAllSuccess() throws Exception {
        final PolicyBasedAuthenticationManager manager = new PolicyBasedAuthenticationManager(
                newMockHandler("HandlerA", true),
                newMockHandler("HandlerB", false));
        final RequiredHandlerAuthenticationPolicy policy = new RequiredHandlerAuthenticationPolicy("HandlerA");
        policy.setTryAll(true);
        manager.setAuthenticationPolicy(policy);
        final Authentication auth = manager.authenticate(mock(Credentials.class), mock(Credentials.class));
        assertEquals(1, auth.getSuccesses().size());
        assertEquals(1, auth.getFailures().size());
        assertEquals(2, auth.getCredentials().size());
    }

    /**
     * Creates a new mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param success True to authenticate all credentials, false to fail all credentials.
     *
     * @return New mock authentication handler instance.
     *
     * @throws Exception On errors.
     */
    private static AuthenticationHandler newMockHandler(final boolean success) throws Exception {
        return newMockHandler("MockAuthenticationHandler" + System.nanoTime(), success);
    }

    /**
     * Creates a new named mock authentication handler that either successfully validates all credentials or fails to
     * validate all credentials.
     *
     * @param name Authentication handler name.
     * @param success True to authenticate all credentials, false to fail all credentials.
     *
     * @return New mock authentication handler instance.
     *
     * @throws Exception On errors.
     */
    private static AuthenticationHandler newMockHandler(final String name, final boolean success) throws Exception {
        final AuthenticationHandler mock = mock(AuthenticationHandler.class);
        when(mock.getName()).thenReturn(name);
        when(mock.supports(any(Credentials.class))).thenReturn(true);
        if (success) {
            final HandlerResult result = new HandlerResult(
                    mock,
                    mock(CredentialMetaData.class),
                    new SimplePrincipal("nobody"));
            when(mock.authenticate(any(Credentials.class))).thenReturn(result);
        } else {
            when(mock.authenticate(any(Credentials.class))).thenThrow(new FailedLoginException());
        }
        return mock;
    }

}