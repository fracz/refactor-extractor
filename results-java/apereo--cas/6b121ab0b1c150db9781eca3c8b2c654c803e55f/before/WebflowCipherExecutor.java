package org.jasig.cas;

import org.jasig.cas.util.BinaryCipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * This is {@link WebflowCipherExecutor}, that reads webflow keys
 * from CAS configuration and presents a cipher.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("webflowCipherExecutor")
public class WebflowCipherExecutor extends BinaryCipherExecutor {

    /**
     * Instantiates a new webflow cipher executor.
     *
     * @param secretKeyEncryption the secret key encryption
     * @param secretKeySigning    the secret key signing
     * @param secretKeyAlg        the secret key alg
     * @param signingKeySize      the signing key size
     * @param encryptionKeySize   the encryption key size
     */
    @Autowired
    public WebflowCipherExecutor(@Value("${webflow.encryption.key:}")
                                 final String secretKeyEncryption,
                                 @Value("${webflow.signing.key:}")
                                 final String secretKeySigning,
                                 @Value("${webflow.secretkey.alg:AES}")
                                 final String secretKeyAlg,
                                 @Value("${webflow.signing.key.size:512}")
                                 final int signingKeySize,
                                 @Value("${webflow.encryption.key.size:16}")
                                 final int encryptionKeySize){
        super(secretKeyEncryption, secretKeySigning, signingKeySize, encryptionKeySize);
        setSecretKeyAlgorithm(secretKeyAlg);
    }
}