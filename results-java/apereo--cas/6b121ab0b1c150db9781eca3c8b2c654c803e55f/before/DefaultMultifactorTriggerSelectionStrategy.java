package org.jasig.cas.authentication;

import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceMultifactorPolicy;
import org.jasig.cas.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Default MFA Trigger selection strategy. This strategy looks for valid triggers in the following order: request
 * parameter, RegisteredService policy, principal attribute.
 *
 * @author Daniel Frett
 * @since 5.0.0
 */
@RefreshScope
@Component("defaultMultifactorTriggerSelectionStrategy")
public class DefaultMultifactorTriggerSelectionStrategy implements MultifactorTriggerSelectionStrategy {
    private static final Splitter ATTR_NAMES = Splitter.on(',').trimResults().omitEmptyStrings();

    @Value("${cas.mfa.request.parameter:authn_method}")
    private String requestParameter;

    @Value("${cas.mfa.principal.attributes:}")
    private String principalAttribute;

    public void setRequestParameter(final String parameter) {
        this.requestParameter = parameter;
    }

    public void setPrincipalAttribute(final String attribute) {
        this.principalAttribute = attribute;
    }

    @Override
    public Optional<String> resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                    final HttpServletRequest request, final RegisteredService service, final Principal principal) {
        Optional<String> provider = Optional.empty();

        // short-circuit if we don't have any available MFA providers
        if (providers == null || providers.isEmpty()) {
            return provider;
        }
        final Set<String> validProviderIds = providers.stream()
                .map(MultifactorAuthenticationProvider::getId)
                .collect(Collectors.toSet());

        // check for an opt-in provider id parameter trigger, we only care about the first value
        if (!provider.isPresent() && request != null) {
            provider = Optional.ofNullable(request.getParameter(this.requestParameter)).filter(validProviderIds::contains);
        }

        // check for a RegisteredService configured trigger
        if (!provider.isPresent() && service != null) {
            final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
            if (shouldApplyRegisteredServiceMultifactorPolicy(policy, principal)) {
                provider = policy.getMultifactorAuthenticationProviders().stream()
                        .filter(validProviderIds::contains)
                        .findFirst();
            }
        }

        // check for principal attribute trigger
        if (!provider.isPresent() && principal != null && StringUtils.hasText(this.principalAttribute)) {
            provider = StreamSupport.stream(ATTR_NAMES.split(this.principalAttribute).spliterator(), false)
                    // principal.getAttribute(name).values
                    .map(principal.getAttributes()::get).filter(Objects::nonNull)
                    .map(CollectionUtils::convertValueToCollection).flatMap(Set::stream)
                    // validProviderIds.contains((String) value)
                    .filter(String.class::isInstance).map(String.class::cast).filter(validProviderIds::contains)
                    .findFirst();
        }

        // return the resolved trigger
        return provider;
    }

    private boolean shouldApplyRegisteredServiceMultifactorPolicy(final RegisteredServiceMultifactorPolicy policy,
                                                                  final Principal principal) {
        final String attrName = policy.getPrincipalAttributeNameTrigger();
        final String attrValue = policy.getPrincipalAttributeValueToMatch();

        // Principal attribute name and/or value is not defined
        if (!StringUtils.hasText(attrName) || !StringUtils.hasText(attrValue)) {
            return true;
        }

        // no Principal, we should enforce policy
        if (principal == null) {
            return true;
        }

        // check to see if any of the specified attributes match the attrValue pattern
        return StreamSupport.stream(ATTR_NAMES.split(attrName).spliterator(), false)
                // principal.getAttribute(name).values
                .map(principal.getAttributes()::get).filter(Objects::nonNull)
                .map(CollectionUtils::convertValueToCollection).flatMap(Set::stream)
                // value =~ /attrValue/
                .filter(String.class::isInstance).map(String.class::cast)
                .anyMatch(Predicates.containsPattern(attrValue)::apply);
    }
}