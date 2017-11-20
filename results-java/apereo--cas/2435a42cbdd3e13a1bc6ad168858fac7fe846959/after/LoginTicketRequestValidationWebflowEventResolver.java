package org.jasig.cas.web.flow.resolver;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link LoginTicketRequestValidationWebflowEventResolver}
 * that evaluates the validity of the CAS login ticket.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("loginTicketRequestValidationWebflowEventResolver")
public class LoginTicketRequestValidationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        if (!checkLoginTicketIfExists(context)) {
            return ImmutableSet.of(returnInvalidLoginTicketEvent(context));
        }
        return null;
    }

    /**
     * Tries to to determine if the login ticket in the request flow scope
     * matches the login ticket provided by the request. The comparison
     * is case-sensitive.
     *
     * @param context the context
     * @return true if valid
     * @since 4.1.0
     */
    protected boolean checkLoginTicketIfExists(final RequestContext context) {
        final String loginTicketFromFlowScope = WebUtils.getLoginTicketFromFlowScope(context);
        final String loginTicketFromRequest = WebUtils.getLoginTicketFromRequest(context);

        logger.trace("Comparing login ticket in the flow scope [{}] with login ticket in the request [{}]",
                loginTicketFromFlowScope, loginTicketFromRequest);
        return StringUtils.equals(loginTicketFromFlowScope, loginTicketFromRequest);
    }

    /**
     * Return invalid login ticket event.
     *
     * @param context the context
     * @return the error event
     * @since 4.1.0
     */
    protected Event returnInvalidLoginTicketEvent(final RequestContext context) {
        final String loginTicketFromRequest = WebUtils.getLoginTicketFromRequest(context);
        logger.warn("Invalid login ticket [{}]", loginTicketFromRequest);
        context.getMessageContext().addMessage(new MessageBuilder().error().code("error.invalid.loginticket").build());
        return newEvent(CasWebflowConstants.TRANSITION_ID_ERROR);
    }
}