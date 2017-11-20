package org.jasig.cas.adaptors.duo;

import org.jasig.cas.adaptors.duo.web.flow.DuoMultifactorWebflowConfigurer;
import org.jasig.cas.services.AbstractMultifactorAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * This is {@link DuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("duoAuthenticationProvider")
public class DuoMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Value("${cas.mfa.duo.rank:0}")
    private int rank;

    @Autowired
    @Qualifier("duoAuthenticationService")
    private DuoAuthenticationService duoAuthenticationService;


    @Override
    public String getId() {
        return DuoMultifactorWebflowConfigurer.MFA_DUO_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return this.rank;
    }


    @Override
    protected boolean isAvailable() {
        return this.duoAuthenticationService.canPing();
    }
}