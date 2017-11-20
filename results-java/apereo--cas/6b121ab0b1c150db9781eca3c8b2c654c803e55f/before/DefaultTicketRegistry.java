package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the TicketRegistry that is backed by a ConcurrentHashMap.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RefreshScope
@Component("defaultTicketRegistry")
public class DefaultTicketRegistry extends AbstractTicketRegistry {

    /**
     * A HashMap to contain the tickets.
     */
    private Map<String, Ticket> cache;

    /**
     * Instantiates a new default ticket registry.
     */
    public DefaultTicketRegistry() {
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new, empty registry with the specified initial capacity, load
     * factor, and concurrency level.
     *
     * @param initialCapacity  - the initial capacity. The implementation
     *                         performs internal sizing to accommodate this many elements.
     * @param loadFactor       - the load factor threshold, used to control resizing.
     *                         Resizing may be performed when the average number of elements per bin
     *                         exceeds this threshold.
     * @param concurrencyLevel - the estimated number of concurrently updating
     *                         threads. The implementation performs internal sizing to try to
     *                         accommodate this many threads.
     */
    @Autowired
    public DefaultTicketRegistry(@Value("${default.ticket.registry.initialcapacity:1000}")
                                 final int initialCapacity,
                                 @Value("${default.ticket.registry.loadfactor:1}")
                                 final float loadFactor,
                                 @Value("${default.ticket.registry.concurrency:20}")
                                 final int concurrencyLevel) {
        this.cache = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    @Override
    public void addTicket(final Ticket ticket) {
        Assert.notNull(ticket, "ticket cannot be null");

        logger.debug("Added ticket [{}] to registry.", ticket.getId());
        this.cache.put(ticket.getId(), ticket);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (ticketId == null) {
            return null;
        }
        return decodeTicket(this.cache.get(encTicketId));
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        return this.cache.remove(ticketId) != null;
    }


    @Override
    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(this.cache.values());
    }

    @Override
    public long sessionCount() {
        return this.cache.values().stream().filter(t -> t instanceof TicketGrantingTicket).count();
    }

    @Override
    public long serviceTicketCount() {
        return this.cache.values().stream().filter(t -> t instanceof ServiceTicket).count();
    }

    @Override
    public void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }
}