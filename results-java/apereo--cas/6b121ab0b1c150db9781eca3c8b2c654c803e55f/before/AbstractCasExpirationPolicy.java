package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * This is an {@link org.jasig.cas.ticket.support.AbstractCasExpirationPolicy}
 * that serves as the root parent for all CAS expiration policies
 * and exposes a few internal helper methods to children can access
 * to objects like the request, etc.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractCasExpirationPolicy implements ExpirationPolicy {

    private static final long serialVersionUID = 8042104336580063690L;

    /**
     * The Logger instance for this class. Using a transient instance field for
     * the Logger doesn't work; On object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCasExpirationPolicy.class);

    /**
     * Gets the http request based on the
     * {@link org.springframework.web.context.request.RequestContextHolder}.
     * @return the request or null
     */
    protected HttpServletRequest getRequest() {
        return WebUtils.getHttpServletRequest();
    }
}