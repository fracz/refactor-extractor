package org.jasig.cas.support.saml;

import org.jasig.cas.validation.ValidationServiceSelectionStrategy;
import org.jasig.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * This is {@link SamlIdPApplicationContextWrapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component
public class SamlIdPApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Autowired
    @Qualifier("samlIdPEntityIdValidationServiceSelectionStrategy")
    private ValidationServiceSelectionStrategy samlIdPEntityIdValidationServiceSelectionStrategy;

    @Resource(name="validationServiceSelectionStrategies")
    private List<ValidationServiceSelectionStrategy> validationServiceSelectionStrategies;

    @PostConstruct
    private void init() {
        this.validationServiceSelectionStrategies.add(0, this.samlIdPEntityIdValidationServiceSelectionStrategy);
    }
}