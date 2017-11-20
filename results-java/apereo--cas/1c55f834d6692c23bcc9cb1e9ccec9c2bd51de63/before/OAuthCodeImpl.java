package org.jasig.cas.support.oauth.ticket.code;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.AbstractTicket;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * An OAuth code implementation.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
@Entity
@Table(name="OAUTH_TOKENS")
@DiscriminatorColumn(name="TYPE")
@DiscriminatorValue(OAuthCode.PREFIX)
public class OAuthCodeImpl extends AbstractTicket implements OAuthCode {

    /** The service this ticket is valid for. */
    @Lob
    @Column(name="SERVICE", nullable=false)
    private Service service;

    /** The authenticated object for which this ticket was generated for. */
    @Lob
    @Column(name="AUTHENTICATION", nullable=false, length = 1000000)
    private Authentication authentication;

    /**
     * Instantiates a new OAuth code impl.
     */
    public OAuthCodeImpl() {
        // exists for JPA purposes
    }

    /**
     * Constructs a new OAuth code with unique id for a service and authentication.
     *
     * @param id the unique identifier for the ticket.
     * @param service the service this ticket is for.
     * @param authentication the authentication.
     * @param expirationPolicy the expiration policy.
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public OAuthCodeImpl(final String id, @NotNull final Service service, @NotNull final Authentication authentication,
                         final ExpirationPolicy expirationPolicy) {
        super(id, null, expirationPolicy);

        Assert.notNull(service, "service cannot be null");
        Assert.notNull(authentication, "authentication cannot be null");
        this.service = service;
        this.authentication = authentication;
    }

    @Override
    public boolean isFromNewLogin() {
        return true;
    }

    @Override
    public Service getService() {
        return this.service;
    }

    @Override
    public boolean isValidFor(final Service serviceToValidate) {
        updateState();
        return serviceToValidate.matches(this.service);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof OAuthCode)) {
            return false;
        }

        final Ticket ticket = (Ticket) object;

        return new EqualsBuilder()
                .append(ticket.getId(), this.getId())
                .isEquals();
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(
            final String id, final Authentication authentication,
            final ExpirationPolicy expirationPolicy) {
        throw new UnsupportedOperationException("No PGT grant is available in OAuth");
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        return null;
    }
}