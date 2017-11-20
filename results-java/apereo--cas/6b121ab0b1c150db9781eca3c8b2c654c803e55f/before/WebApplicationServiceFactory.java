package org.jasig.cas.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.validation.ValidationResponseType;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;

/**
 * The {@link WebApplicationServiceFactory} is responsible for
 * creating {@link WebApplicationService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component("webApplicationServiceFactory")
public class WebApplicationServiceFactory extends AbstractServiceFactory<WebApplicationService> {

    @Override
    public WebApplicationService createService(final HttpServletRequest request) {

        final String method = request.getParameter(CasProtocolConstants.PARAMETER_METHOD);
        final String format = request.getParameter(CasProtocolConstants.PARAMETER_FORMAT);

        final String serviceToUse = getRequestedService(request);
        if (StringUtils.isBlank(serviceToUse)) {
            logger.debug("No service is specified in the request. Skipping service creation");
            return null;
        }

        final String id = AbstractServiceFactory.cleanupUrl(serviceToUse);
        final String artifactId = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);

        final Response.ResponseType type = HttpMethod.POST.name().equalsIgnoreCase(method) ? Response.ResponseType.POST
                : Response.ResponseType.REDIRECT;

        final SimpleWebApplicationServiceImpl webApplicationService =
                new SimpleWebApplicationServiceImpl(id, serviceToUse,
                        artifactId, new WebApplicationServiceResponseBuilder(type));

        try {
            if (StringUtils.isNotBlank(format)) {
                final ValidationResponseType formatType = ValidationResponseType.valueOf(format.toUpperCase());
                webApplicationService.setFormat(formatType);
            }
        } catch (final Exception e) {
            logger.error("Format specified in the request [{}] is not recognized", format);
            return null;
        }
        return webApplicationService;
    }

    /**
     * Gets requested service.
     *
     * @param request the request
     * @return the requested service
     */
    protected String getRequestedService(final HttpServletRequest request) {
        final String targetService = request.getParameter(CasProtocolConstants.PARAMETER_TARGET_SERVICE);
        final String service = request.getParameter(CasProtocolConstants.PARAMETER_SERVICE);
        final Object serviceAttribute = request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE);

        String serviceToUse = null;
        if (StringUtils.isNotBlank(targetService)) {
            serviceToUse = targetService;
        } else if (StringUtils.isNotBlank(service)) {
            serviceToUse = service;
        } else if (serviceAttribute != null) {
            if (serviceAttribute instanceof Service) {
                serviceToUse = ((Service) serviceAttribute).getId();
            } else {
                serviceToUse = serviceAttribute.toString();
            }
        }

        if (StringUtils.isBlank(serviceToUse)) {
            return null;
        }
        return serviceToUse;
    }

    @Override
    public WebApplicationService createService(final String id) {
        return new SimpleWebApplicationServiceImpl(id, id, null,
                new WebApplicationServiceResponseBuilder(Response.ResponseType.REDIRECT));
    }
}