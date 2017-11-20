package org.apereo.cas.ticket;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This is {@link DefaultTicketDefinitionProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultTicketDefinitionProperties implements TicketDefinitionProperties {

    private boolean cascadeTicket;
    private String cacheName;

    @Override
    public boolean isCascade() {
        return cascadeTicket;
    }

    @Override
    public void setCascade(final boolean cascadeTicket) {
        this.cascadeTicket = cascadeTicket;
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    @Override
    public void setCacheName(final String cacheName) {
        this.cacheName = cacheName;
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
        final DefaultTicketDefinitionProperties rhs = (DefaultTicketDefinitionProperties) obj;
        return new EqualsBuilder()
                .append(this.cascadeTicket, rhs.cascadeTicket)
                .append(this.cacheName, rhs.cacheName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(cascadeTicket)
                .append(cacheName)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("cascadeTicket", cascadeTicket)
                .append("cacheName", cacheName)
                .toString();
    }
}