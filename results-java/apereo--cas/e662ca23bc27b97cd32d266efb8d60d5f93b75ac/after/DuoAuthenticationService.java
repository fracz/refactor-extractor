package org.apereo.cas.adaptors.duo.authn;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.adaptors.duo.DuoIntegration;
import org.apereo.cas.authentication.Credential;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link DuoAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface DuoAuthenticationService extends Serializable {

    /**
     * Verify the authentication response from Duo.
     *
     * @param credential signed request token
     * @return authenticated user / verified response.
     * @throws Exception if response verification fails
     */
    Pair<Boolean, String> authenticate(Credential credential) throws Exception;

    /**
     * Ping provider.
     *
     * @return true /false.
     */
    boolean ping();

    /**
     * Gets api host.
     *
     * @return the api host
     */
    String getApiHost();

    /**
     * Sign request token.
     *
     * @param uid the uid
     * @return the signed token
     */
    String signRequestToken(String uid);


    /**
     * Gets duo user account.
     *
     * @param username the actual user name
     * @return the duo user account
     */
    Optional<DuoUserAccount> getDuoUserAccount(String username);

    /**
     * Gets duo user enrollment policy.
     *
     * @return the duo user enrollment policy
     */
    Optional<DuoIntegration> getDuoIntegrationPolicy();

}