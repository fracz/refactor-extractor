package org.jasig.cas.support.saml;

import org.jasig.cas.web.AbstractServletContextInitializer;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * Initializes the CAS root servlet context to make sure
 * SAML IdP can be activated by the main CAS servlet.
 * @author Misagh Moayyed
 * @since 4.2
 */
@WebListener
@Component("samlIdPServletContextListener")
public class SamlIdPServletContextListener extends AbstractServletContextInitializer {

    @Override
    public void initializeServletContext(final ServletContextEvent event) {
        if (WebUtils.isCasServletInitializing(event)) {
            addEndpointMappingToCasServlet(event, SamlIdPConstants.ENDPOINT_IDP_METADATA);
            addEndpointMappingToCasServlet(event, SamlIdPConstants.ENDPOINT_GENERATE_RP_METADATA);
            addEndpointMappingToCasServlet(event, SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK);
            addEndpointMappingToCasServlet(event, SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST);
            addEndpointMappingToCasServlet(event, SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_REDIRECT);
        }
    }

}