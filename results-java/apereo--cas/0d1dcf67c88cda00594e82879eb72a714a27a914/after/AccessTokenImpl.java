package org.jasig.cas.support.oauth.ticket.accesstoken;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.support.oauth.ticket.code.OAuthCodeImpl;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * An OAuth access token implementation.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
@Entity
@Table(name="OAUTH_TOKENS")
@DiscriminatorColumn(name="TYPE")
@DiscriminatorValue(AccessToken.PREFIX)
public class AccessTokenImpl extends OAuthCodeImpl implements AccessToken {

    private static final long serialVersionUID = 2339545346159721563L;

    /**
     * Instantiates a new OAuth access token.
     */
    public AccessTokenImpl() {
        // exists for JPA purposes
    }

    /**
     * Constructs a new access token with unique id for a service and authentication.
     *
     * @param id the unique identifier for the ticket.
     * @param service the service this ticket is for.
     * @param authentication the authentication.
     * @param expirationPolicy the expiration policy.
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public AccessTokenImpl(final String id, @NotNull final Service service, @NotNull final Authentication authentication,
                           final ExpirationPolicy expirationPolicy) {
        super(id, service, authentication, expirationPolicy);
    }
}