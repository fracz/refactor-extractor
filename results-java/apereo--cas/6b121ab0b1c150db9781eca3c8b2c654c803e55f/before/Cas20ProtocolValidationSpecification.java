package org.jasig.cas.validation;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Validation specification for the CAS 2.0 protocol. This specification extends
 * the Cas10ProtocolValidationSpecification, checking for the presence of
 * renew=true and if requested, succeeding only if ticket validation is
 * occurring from a new login.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RefreshScope
@Component("cas20ProtocolValidationSpecification")
@Scope(value = "prototype")
public class Cas20ProtocolValidationSpecification extends AbstractCasProtocolValidationSpecification {

    /**
     * Instantiates a new cas20 protocol validation specification.
     */
    public Cas20ProtocolValidationSpecification() {
        super();
    }

    /**
     * Instantiates a new cas20 protocol validation specification.
     *
     * @param renew the renew
     */
    public Cas20ProtocolValidationSpecification(final boolean renew) {
        super(renew);
    }

    @Override
    protected boolean isSatisfiedByInternal(final Assertion assertion) {
        return true;
    }
}