package org.jasig.cas.web.flow.authentication;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.MultifactorAuthenticationProviderSelector;
import org.jasig.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Set;

/**
 * This is {@link FirstMultifactorAuthenticationProviderSelector}
 * that grabs onto the first authentication provider in the collection.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("firstMultifactorAuthenticationProviderSelector")
public class FirstMultifactorAuthenticationProviderSelector implements MultifactorAuthenticationProviderSelector {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public MultifactorAuthenticationProvider resolve(final Set<MultifactorAuthenticationProvider> providers,
                                                     final RegisteredService service, final Principal principal) {
        final Iterator<MultifactorAuthenticationProvider> it = providers.iterator();
        final MultifactorAuthenticationProvider provider = it.next();
        logger.debug("Selected the first provider {} for service {} out of {} providers", provider, service, providers.size());
        return provider;
    }
}