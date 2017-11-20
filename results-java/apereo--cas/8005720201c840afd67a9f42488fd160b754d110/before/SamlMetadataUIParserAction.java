package org.apereo.cas.support.saml.mdui.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.MetadataUIUtils;
import org.apereo.cas.support.saml.mdui.SimpleMetadataUIInfo;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link SamlMetadataUIParserAction} that attempts to parse
 * the mdui extension block for a SAML SP from the provided metadata locations.
 * The result is put into the flow request context under the parameter
 * {@link MetadataUIUtils#MDUI_FLOW_PARAMETER_NAME}. The entity id parameter is
 * specified by default at {@link SamlProtocolConstants#PARAMETER_ENTITY_ID}.
 * <p>
 * <p>This action is best suited to be invoked when the CAS login page
 * is about to render so that the page, once the MDUI info is obtained,
 * has a chance to populate the UI with relevant info about the SP.</p>
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class SamlMetadataUIParserAction extends AbstractAction {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String entityIdParameterName;

    private final MetadataResolverAdapter metadataAdapter;

    private ServicesManager servicesManager;

    private ServiceFactory<WebApplicationService> serviceFactory;

    /**
     * Instantiates a new SAML mdui parser action.
     * Defaults the parameter name to {@link SamlProtocolConstants#PARAMETER_ENTITY_ID}.
     *
     * @param metadataAdapter the metadata resources
     */
    public SamlMetadataUIParserAction(final MetadataResolverAdapter metadataAdapter) {
        this(SamlProtocolConstants.PARAMETER_ENTITY_ID, metadataAdapter);
    }

    /**
     * Instantiates a new SAML mdui parser action.
     *
     * @param entityIdParameterName the entity id parameter name
     * @param metadataAdapter       the metadata adapter
     */
    public SamlMetadataUIParserAction(final String entityIdParameterName,
                                      final MetadataResolverAdapter metadataAdapter) {
        this.entityIdParameterName = entityIdParameterName;
        this.metadataAdapter = metadataAdapter;
    }

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final String entityId = request.getParameter(this.entityIdParameterName);
        if (StringUtils.isBlank(entityId)) {
            logger.debug("No entity id found for parameter [{}]", this.entityIdParameterName);
            return success();
        }
        final WebApplicationService service = this.serviceFactory.createService(entityId);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            logger.debug("Entity id [{}] is not recognized/allowed by the CAS service registry", entityId);

            if (registeredService != null) {
                WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(requestContext,
                        registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            }
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    "Entity " + entityId + " not recognized");
        }

        final SimpleMetadataUIInfo mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(this.metadataAdapter, entityId, registeredService);

        return success();
    }


    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setServiceFactory(final ServiceFactory<WebApplicationService> serviceFactory) {
        this.serviceFactory = serviceFactory;
    }
}