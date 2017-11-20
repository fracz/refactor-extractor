package org.jasig.cas.web.flow.resolver;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategySupport;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link InitialAuthenticationAttemptWebflowEventResolver},
 * which handles the initial authentication attempt and calls upon a number of
 * embedded resolvers to produce the next event in the authentication flow.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("initialAuthenticationAttemptWebflowEventResolver")
public class InitialAuthenticationAttemptWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Autowired
    @Qualifier("registeredServiceAuthenticationPolicyWebflowEventResolver")
    private CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver;

    @Autowired
    @Qualifier("principalAttributeAuthenticationPolicyWebflowEventResolver")
    private CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver;

    @Autowired
    @Qualifier("registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
    private CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver;

    @Autowired
    @Qualifier("selectiveAuthenticationProviderWebflowEventResolver")
    private CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver;


    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        try {

            final Credential credential = getCredentialFromContext(context);
            final AuthenticationResultBuilder builder =
                    this.authenticationSystemSupport.handleInitialAuthenticationTransaction(credential);

            WebUtils.putAuthenticationResultBuilder(builder, context);
            WebUtils.putAuthentication(builder.getInitialAuthentication(), context);

            final Service service = WebUtils.getService(context);
            if (service != null) {

                logger.debug("Locating service {} in service registry to determine authentication policy", service);
                final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
                RegisteredServiceAccessStrategySupport.ensureServiceAccessIsAllowed(service, registeredService);

                logger.debug("Evaluating authentication policy for {} based on principal attribute requirements only when accessing {}",
                       registeredService.getServiceId(),  service);
                final Event serviceAttributeEvent =
                        registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver.resolveSingle(context);

                logger.debug("Evaluating authentication policy based on principal attribute requirements for {}", service);
                final Event attributeEvent = principalAttributeAuthenticationPolicyWebflowEventResolver.resolveSingle(context);

                logger.debug("Evaluating authentication policy for {}", service);
                final Event serviceEvent = registeredServiceAuthenticationPolicyWebflowEventResolver.resolveSingle(context);

                final ImmutableSet.Builder<Event> eventBuilder = ImmutableSet.builder();
                if (serviceAttributeEvent != null) {
                    eventBuilder.add(serviceAttributeEvent);
                }
                if (attributeEvent != null) {
                    eventBuilder.add(attributeEvent);
                }
                if (serviceEvent != null) {
                    eventBuilder.add(serviceEvent);
                }

                final Set<Event> resolvedEvents = eventBuilder.build();
                if (!resolvedEvents.isEmpty()) {
                    putResolvedEventsAsAttribute(context, resolvedEvents);
                    final Event finalResolvedEvent = selectiveAuthenticationProviderWebflowEventResolver.resolveSingle(context);
                    if (finalResolvedEvent != null) {
                        return ImmutableSet.of(finalResolvedEvent);
                    }
                }
            }

            return ImmutableSet.of(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception e) {
            Event event = returnAuthenticationExceptionEventIfNeeded(e);
            if (event == null) {
                logger.debug(e.getMessage(), e);
                event = newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, e);
            }
            return ImmutableSet.of(event);
        }
    }



    private Event returnAuthenticationExceptionEventIfNeeded(final Exception e) {

        final AuthenticationException ex;
        if (e instanceof AuthenticationException) {
            ex = (AuthenticationException) e;
        } else if (e.getCause() instanceof AuthenticationException) {
            ex = (AuthenticationException) e.getCause();
        } else {
            return null;
        }

        logger.debug(ex.getMessage(), ex);
        return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, ex);
    }

}