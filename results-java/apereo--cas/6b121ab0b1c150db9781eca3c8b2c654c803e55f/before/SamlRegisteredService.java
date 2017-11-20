package org.jasig.cas.support.saml.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * The {@link SamlRegisteredService} is responsible for managing the SAML metadata for a given SP.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue("saml")
public class SamlRegisteredService extends RegexRegisteredService {
    private static final long serialVersionUID = 1218757374062931021L;

    private String metadataLocation;
    private long metadataMaxValidity;
    private String requiredAuthenticationContextClass;
    private String metadataSignatureLocation;
    private boolean signAssertions;
    private boolean signResponses = true;
    private boolean encryptAssertions;

    /**
     * Instantiates a new Saml registered service.
     */
    public SamlRegisteredService() {
        super();
    }

    public void setMetadataLocation(final String metadataLocation) {
        this.metadataLocation = metadataLocation;
    }

    public String getMetadataLocation() {
        return this.metadataLocation;
    }

    public boolean isSignAssertions() {
        return this.signAssertions;
    }

    public void setSignAssertions(final boolean signAssertions) {
        this.signAssertions = signAssertions;
    }

    public boolean isSignResponses() {
        return this.signResponses;
    }

    public void setSignResponses(final boolean signResponses) {
        this.signResponses = signResponses;
    }

    public String getRequiredAuthenticationContextClass() {
        return this.requiredAuthenticationContextClass;
    }

    public void setRequiredAuthenticationContextClass(final String requiredAuthenticationContextClass) {
        this.requiredAuthenticationContextClass = requiredAuthenticationContextClass;
    }

    public String getMetadataSignatureLocation() {
        return this.metadataSignatureLocation;
    }

    public void setMetadataSignatureLocation(final String metadataSignatureLocation) {
        this.metadataSignatureLocation = metadataSignatureLocation;
    }

    public boolean isEncryptAssertions() {
        return this.encryptAssertions;
    }

    public void setEncryptAssertions(final boolean encryptAssertions) {
        this.encryptAssertions = encryptAssertions;
    }

    public long getMetadataMaxValidity() {
        return this.metadataMaxValidity;
    }

    public void setMetadataMaxValidity(final long metadataMaxValidity) {
        this.metadataMaxValidity = metadataMaxValidity;
    }

    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        try {
            final SamlRegisteredService samlRegisteredService = (SamlRegisteredService) source;
            samlRegisteredService.setMetadataLocation(this.metadataLocation);
            samlRegisteredService.setSignAssertions(this.signAssertions);
            samlRegisteredService.setSignResponses(this.signResponses);
            samlRegisteredService.setRequiredAuthenticationContextClass(this.requiredAuthenticationContextClass);
            samlRegisteredService.setMetadataMaxValidity(this.metadataMaxValidity);
            samlRegisteredService.setMetadataSignatureLocation(this.metadataSignatureLocation);
            samlRegisteredService.setEncryptAssertions(this.encryptAssertions);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new SamlRegisteredService();
    }



    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final SamlRegisteredService rhs = (SamlRegisteredService) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.metadataLocation, rhs.metadataLocation)
                .append(this.metadataMaxValidity, rhs.metadataMaxValidity)
                .append(this.requiredAuthenticationContextClass, rhs.requiredAuthenticationContextClass)
                .append(this.metadataSignatureLocation, rhs.metadataSignatureLocation)
                .append(this.signAssertions, rhs.signAssertions)
                .append(this.signResponses, rhs.signResponses)
                .append(this.encryptAssertions, rhs.encryptAssertions)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.metadataLocation)
                .append(this.metadataMaxValidity)
                .append(this.requiredAuthenticationContextClass)
                .append(this.metadataSignatureLocation)
                .append(this.signAssertions)
                .append(this.signResponses)
                .append(this.encryptAssertions)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("metadataLocation", this.metadataLocation)
                .append("metadataMaxValidity", this.metadataMaxValidity)
                .append("requiredAuthenticationContextClass", this.requiredAuthenticationContextClass)
                .append("metadataSignatureLocation", this.metadataSignatureLocation)
                .append("signAssertions", this.signAssertions)
                .append("signResponses", this.signResponses)
                .append("encryptAssertions", this.encryptAssertions)
                .toString();
    }
}