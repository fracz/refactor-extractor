package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReturnAllAttributeReleasePolicy;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.authentication.OAuthAuthentication;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.services.OAuthWebApplicationService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessTokenImpl;
import org.jasig.cas.support.oauth.ticket.code.DefaultOAuthCodeFactory;
import org.jasig.cas.support.oauth.ticket.code.OAuthCodeImpl;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20AccessTokenController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/oauth-context.xml", "classpath:/META-INF/spring/cas-servlet-oauth.xml"})
@DirtiesContext()
public final class OAuth20AccessTokenControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String CLIENT_ID = "1";

    private static final String CLIENT_SECRET = "secret";

    private static final String WRONG_CLIENT_SECRET = "wrongSecret";

    private static final String CODE = "ST-1";

    private static final String REDIRECT_URI = "http://someurl";

    private static final String OTHER_REDIRECT_URI = "http://someotherurl";

    private static final int TIMEOUT = 7200;

    private static final String ID = "1234";

    private static final String NAME = "attributeName";

    private static final String NAME2 = "attributeName2";

    private static final String VALUE = "attributeValue";

    @Autowired
    private DefaultOAuthCodeFactory oAuthCodeFactory;

    @Autowired
    private OAuth20AccessTokenController oAuth20AccessTokenController;

    @Test
    public void verifyNoClientId() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoRedirectUri() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoClientSecret() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoCode() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoCasService() throws Exception {
        clearAllServices();

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();


        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyRedirectUriDoesNotStartWithServiceId() throws Exception {
        clearAllServices();
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        oAuth20AccessTokenController.getServicesManager().save(getRegisteredService(OTHER_REDIRECT_URI, CLIENT_SECRET));

        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyWrongSecret() throws Exception {
        clearAllServices();
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, CODE);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        oAuth20AccessTokenController.getServicesManager().save(getRegisteredService(REDIRECT_URI, WRONG_CLIENT_SECRET));


        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }

    @Test
    public void verifyExpiredCode() throws Exception {
        clearAllServices();
        final RegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        oAuth20AccessTokenController.getServicesManager().save(registeredService);

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = org.jasig.cas.authentication.TestUtils.getPrincipal(ID, map);
        final Authentication authentication = new OAuthAuthentication(new DateTime(), principal);
        final DefaultOAuthCodeFactory expiringOAuthCodeFactory = new DefaultOAuthCodeFactory();
        expiringOAuthCodeFactory.setExpirationPolicy(new ExpirationPolicy() {
            @Override
            public boolean isExpired(final TicketState ticketState) {
                return true;
            }
        });
        final Service service = new OAuthWebApplicationService("" + registeredService.getId(), registeredService.getServiceId());
        final OAuthCodeImpl code = (OAuthCodeImpl) expiringOAuthCodeFactory.create(service, authentication);
        oAuth20AccessTokenController.getTicketRegistry().addTicket(code);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertEquals(400, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_GRANT, mockResponse.getContentAsString());
    }

    @Test
    public void verifyOK() throws Exception {
        clearAllServices();
        final RegisteredService registeredService = getRegisteredService(REDIRECT_URI, CLIENT_SECRET);
        oAuth20AccessTokenController.getServicesManager().save(registeredService);

        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = org.jasig.cas.authentication.TestUtils.getPrincipal(ID, map);
        final Authentication authentication = new OAuthAuthentication(new DateTime(), principal);
        final Service service = new OAuthWebApplicationService("" + registeredService.getId(), registeredService.getServiceId());
        final OAuthCodeImpl code = (OAuthCodeImpl) oAuthCodeFactory.create(service, authentication);
        oAuth20AccessTokenController.getTicketRegistry().addTicket(code);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.ACCESS_TOKEN_URL);
        mockRequest.setParameter(OAuthConstants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuthConstants.REDIRECT_URI, REDIRECT_URI);
        mockRequest.setParameter(OAuthConstants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuthConstants.CODE, code.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20AccessTokenController.handleRequest(mockRequest, mockResponse);
        assertNull(oAuth20AccessTokenController.getTicketRegistry().getTicket((code.getId())));
        assertEquals("text/plain", mockResponse.getContentType());
        assertEquals(200, mockResponse.getStatus());
        final String body = mockResponse.getContentAsString();

        assertTrue(body.contains(OAuthConstants.ACCESS_TOKEN + '='));
        assertTrue(body.contains(OAuthConstants.EXPIRES + '='));

        final String accessTokenId = StringUtils.substringBetween(body, OAuthConstants.ACCESS_TOKEN + '=', "&");
        final AccessTokenImpl accessToken = (AccessTokenImpl) oAuth20AccessTokenController.getTicketRegistry().getTicket(accessTokenId);
        assertEquals(principal, accessToken.getAuthentication().getPrincipal());

        // delta = 2 seconds
        final int delta = 2;
        final int timeLeft = Integer.parseInt(StringUtils.substringAfter(body, '&' + OAuthConstants.EXPIRES + '='));
        assertTrue(timeLeft >= TIMEOUT - 10 - delta);
    }

    private RegisteredService getRegisteredService(final String serviceId, final String secret) {
        final OAuthRegisteredService registeredServiceImpl = new OAuthRegisteredService();
        registeredServiceImpl.setName("The registered service name");
        registeredServiceImpl.setServiceId(serviceId);
        registeredServiceImpl.setClientId(CLIENT_ID);
        registeredServiceImpl.setClientSecret(secret);
        registeredServiceImpl.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        return registeredServiceImpl;
    }

    private void clearAllServices() {
        final Collection<RegisteredService> col  = oAuth20AccessTokenController.getServicesManager().getAllServices();

        for (final RegisteredService r : col) {
            oAuth20AccessTokenController.getServicesManager().delete(r.getId());
        }

    }
}