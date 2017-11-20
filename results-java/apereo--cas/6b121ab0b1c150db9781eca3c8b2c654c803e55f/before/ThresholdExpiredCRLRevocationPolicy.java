package org.jasig.cas.adaptors.x509.authentication.handler.support;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.security.cert.X509CRL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


/**
 * Implements a policy to handle expired CRL data whereby expired data is permitted
 * up to a threshold period of time but not afterward.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
@RefreshScope
@Component("thresholdExpiredCRLRevocationPolicy")
public class ThresholdExpiredCRLRevocationPolicy implements RevocationPolicy<X509CRL> {
    /** Default threshold is 48 hours. */
    private static final int DEFAULT_THRESHOLD = 172800;

    /** Logger instance. */
    private transient Logger logger = LoggerFactory.getLogger(getClass());


    /** Expired threshold period in seconds. */

    private int threshold = DEFAULT_THRESHOLD;


    /**
     * {@inheritDoc}
     * The CRL next update time is compared against the current time with the threshold
     * applied and rejected if and only if the next update time is in the past.
     *
     * @param crl CRL instance to evaluate.
     *
     * @throws ExpiredCRLException On expired CRL data. Check the exception type for exact details
     *
     * @see org.jasig.cas.adaptors.x509.authentication.handler.support.RevocationPolicy#apply(java.lang.Object)
     */
    @Override
    public void apply(final X509CRL crl) throws ExpiredCRLException {
        final ZonedDateTime cutoff = ZonedDateTime.now(ZoneOffset.UTC);
        if (CertUtils.isExpired(crl, cutoff)) {
            if (CertUtils.isExpired(crl, cutoff.minusSeconds(this.threshold))) {
                throw new ExpiredCRLException(crl.toString(), cutoff, this.threshold);
            }
            logger.info(String.format("CRL expired on %s but is within threshold period, %s seconds.",
                        crl.getNextUpdate(), this.threshold));
        }
    }

    /**
     * Sets the threshold period of time after which expired CRL data is rejected.
     *
     * @param threshold Number of seconds; MUST be non-negative integer.
     */
    @Autowired
    public void setThreshold(@Value("${cas.x509.authn.revocation.policy.threshold:" + DEFAULT_THRESHOLD + '}')
                                 final int threshold) {
        this.threshold = threshold;
    }
}