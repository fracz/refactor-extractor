package org.jasig.cas.support.saml.web.idp.profile.builders;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlProfileSamlConditionsBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("samlProfileSamlConditionsBuilder")
public class SamlProfileSamlConditionsBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Conditions> {
    private static final long serialVersionUID = 126393045912318783L;

    @Value("${cas.samlidp.response.skewAllowance:0}")
    private int skewAllowance;

    @Override
    public final Conditions build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                                  final Assertion assertion, final SamlRegisteredService service, final SamlMetadataAdaptor adaptor)
            throws SamlException {
        return buildConditions(authnRequest, assertion, service, adaptor);
    }

    private Conditions buildConditions(final AuthnRequest authnRequest, final Assertion assertion,
                                       final SamlRegisteredService service, final SamlMetadataAdaptor adaptor) throws SamlException {

        final DateTime currentDateTime = DateTime.now();
        final Conditions conditions = newConditions(currentDateTime,
                currentDateTime.plusSeconds(this.skewAllowance),
                adaptor.getEntityId());
        return conditions;
    }
}