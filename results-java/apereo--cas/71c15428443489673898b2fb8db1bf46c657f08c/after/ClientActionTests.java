package org.jasig.cas.support.pac4j.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.pac4j.test.MockFacebookClient;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Test;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This class tests the {@link ClientAction} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@SuppressWarnings("rawtypes")
public final class ClientActionTests {

    private static final String TGT_NAME = "ticketGrantingTicketId";
    private static final String TGT_ID = "TGT-00-xxxxxxxxxxxxxxxxxxxxxxxxxx.cas0";

    private static final String MY_KEY = "my_key";

    private static final String MY_SECRET = "my_secret";

    private static final String MY_LOGIN_URL = "http://casserver/login";

    private static final String MY_SERVICE = "http://myservice";

    private static final String MY_THEME = "my_theme";

    private static final String MY_LOCALE = "fr";

    private static final String MY_METHOD = "POST";

    @Test
    public void verifyStartAuthentication() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(ClientAction.THEME, MY_THEME);
        mockRequest.setParameter(ClientAction.LOCALE, MY_LOCALE);
        mockRequest.setParameter(ClientAction.METHOD, MY_METHOD);

        final MockHttpSession mockSession = new MockHttpSession();
        mockRequest.setSession(mockSession);

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);
        mockRequestContext.getFlowScope().put(ClientAction.SERVICE,
                org.jasig.cas.services.TestUtils.getService(MY_SERVICE));

        final FacebookClient facebookClient = new FacebookClient(MY_KEY, MY_SECRET);
        final TwitterClient twitterClient = new TwitterClient(MY_KEY, MY_SECRET);
        final Clients clients = new Clients(MY_LOGIN_URL, facebookClient, twitterClient);
        final ClientAction action = new ClientAction();
        action.setCentralAuthenticationService(mock(CentralAuthenticationService.class));
        action.setClients(clients);

        final Event event = action.execute(mockRequestContext);
        assertEquals("error", event.getId());
        assertEquals(MY_THEME, mockSession.getAttribute(ClientAction.THEME));
        assertEquals(MY_LOCALE, mockSession.getAttribute(ClientAction.LOCALE));
        assertEquals(MY_METHOD, mockSession.getAttribute(ClientAction.METHOD));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        final Map<String, String> urls = (Map<String, String>) flowScope.get(ClientAction.PAC4J_URLS);
        assertTrue(((String) urls.get("Facebook"))
                .startsWith("https://www.facebook.com/v2.2/dialog/oauth?client_id=my_key&redirect_uri=http%3A%2F%2Fcasserver%2Flogin%3F"
                        + Clients.DEFAULT_CLIENT_NAME_PARAMETER + "%3DFacebookClient&state="));
        assertEquals(MY_LOGIN_URL + '?' + Clients.DEFAULT_CLIENT_NAME_PARAMETER
                + "=TwitterClient&needs_client_redirection=true", urls.get("Twitter"));
    }

    @Test
    public void verifyFinishAuthentication() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, "FacebookClient");

        final MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(ClientAction.THEME, MY_THEME);
        mockSession.setAttribute(ClientAction.LOCALE, MY_LOCALE);
        mockSession.setAttribute(ClientAction.METHOD, MY_METHOD);
        final Service service = org.jasig.cas.authentication.TestUtils.getService(MY_SERVICE);
        mockSession.setAttribute(ClientAction.SERVICE, service);
        mockRequest.setSession(mockSession);

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);

        final FacebookClient facebookClient = new MockFacebookClient();
        final Clients clients = new Clients(MY_LOGIN_URL, facebookClient);

        final TicketGrantingTicket tgt = new TicketGrantingTicketImpl(TGT_ID, mock(Authentication.class), mock(ExpirationPolicy.class));
        final CentralAuthenticationService casImpl = mock(CentralAuthenticationService.class);
        when(casImpl.createTicketGrantingTicket(any(Credential.class))).thenReturn(tgt);
        final ClientAction action = new ClientAction();
        action.setCentralAuthenticationService(casImpl);
        action.setClients(clients);
        final Event event = action.execute(mockRequestContext);
        assertEquals("success", event.getId());
        assertEquals(MY_THEME, mockRequest.getAttribute(ClientAction.THEME));
        assertEquals(MY_LOCALE, mockRequest.getAttribute(ClientAction.LOCALE));
        assertEquals(MY_METHOD, mockRequest.getAttribute(ClientAction.METHOD));
        assertEquals(MY_SERVICE, mockRequest.getAttribute(ClientAction.SERVICE));
        final MutableAttributeMap flowScope = mockRequestContext.getFlowScope();
        final MutableAttributeMap requestScope = mockRequestContext.getRequestScope();
        assertEquals(service, flowScope.get(ClientAction.SERVICE));
        assertEquals(TGT_ID, flowScope.get(TGT_NAME));
        assertEquals(TGT_ID, requestScope.get(TGT_NAME));
    }

    @Test
    public void checkUnautorizedProtocol() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER, "IndirectBasicAuthClient");

        final ServletExternalContext servletExternalContext = mock(ServletExternalContext.class);
        when(servletExternalContext.getNativeRequest()).thenReturn(mockRequest);

        final MockRequestContext mockRequestContext = new MockRequestContext();
        mockRequestContext.setExternalContext(servletExternalContext);

        final IndirectBasicAuthClient basicAuthClient = new IndirectBasicAuthClient();
        final Clients clients = new Clients(MY_LOGIN_URL, basicAuthClient);
        final ClientAction action = new ClientAction();
        action.setCentralAuthenticationService(mock(CentralAuthenticationService.class));
        action.setClients(clients);

        try {
            action.execute(mockRequestContext);
            fail("Should fail as the HTTP protocol is not authorized");
        } catch (final TechnicalException e) {
            assertEquals("Only CAS, OAuth, OpenID and SAML protocols are supported: " + basicAuthClient, e.getMessage());
        }
    }
}