package org.jasig.cas.web;

import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.validation.ValidationSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component("legacyValidateController")
@Controller
public class LegacyValidateController extends AbstractServiceValidateController {

    /**
     * Handle model and view.
     *
     * @param request the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(path="/validate", method = RequestMethod.GET)
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response)
                throws Exception {
        return super.handleRequestInternal(request, response);
    }

    @Override
    @Autowired
    @Scope("prototype")
    public void setValidationSpecification(
            @Qualifier("cas10ProtocolValidationSpecification")
            final ValidationSpecification validationSpecification) {
        super.setValidationSpecification(validationSpecification);
    }

    @Override
    @Autowired
    public void setFailureView(@Value("cas1ServiceFailureView") final String failureView) {
        super.setFailureView(failureView);
    }

    @Override
    @Autowired
    public void setSuccessView(@Value("cas1ServiceSuccessView") final String successView) {
        super.setSuccessView(successView);
    }

    @Override
    @Autowired
    public void setProxyHandler(@Qualifier("proxy10Handler") final ProxyHandler proxyHandler) {
        super.setProxyHandler(proxyHandler);
    }
}