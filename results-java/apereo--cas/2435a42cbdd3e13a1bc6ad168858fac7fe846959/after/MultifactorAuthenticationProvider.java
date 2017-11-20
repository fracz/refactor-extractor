package org.jasig.cas.services;

import org.jasig.cas.authentication.AuthenticationException;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProvider}
 * that describes an external authentication entity/provider
 * matched against a registered service. Providers may be given
 * the ability to check authentication provider for availability
 * before actually producing a relevant identifier.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface MultifactorAuthenticationProvider extends Serializable {

    /**
     * Provide string.
     *
     * @param service the service
     * @return the string
     * @throws AuthenticationException the authentication exception
     */
    String buildIdentifier(RegisteredService service) throws AuthenticationException;
}