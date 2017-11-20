package org.jasig.cas.web.view;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.CasViewConstants;
import org.jasig.cas.web.AbstractServiceValidateControllerTests;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * Unit tests for {@link Cas20ResponseView}.
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class Cas20ResponseViewTests extends AbstractServiceValidateControllerTests {


    @Test
    public void verifyView() throws Exception {
        final ModelAndView modelAndView = this.getModelAndViewUponServiceValidationWithSecurePgtUrl();
        final MockHttpServletRequest req = new MockHttpServletRequest(new MockServletContext());
        req.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                new GenericWebApplicationContext(req.getServletContext()));

        final Cas20ResponseView view = new Cas20ResponseView();
        final MockHttpServletResponse resp = new MockHttpServletResponse();

        view.setView(new View() {
            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public void render(final Map<String, ?> map, final HttpServletRequest request, final HttpServletResponse response)
                    throws Exception {
                map.forEach(request::setAttribute);
            }
        });
        view.render(modelAndView.getModel(), req, resp);

        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_CHAINED_AUTHENTICATIONS));
        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRIMARY_AUTHENTICATION));
        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL));
        assertNotNull(req.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET_IOU));
    }

}