package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataCatalogRegistrationPlan;
import org.apereo.cas.ticket.TicketMetadataRegistrationConfigurer;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasProtocolCoreTicketMetadataRegistrationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casProtocolCoreTicketMetadataRegistrationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasProtocolCoreTicketMetadataRegistrationConfiguration implements TicketMetadataRegistrationConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasProtocolCoreTicketMetadataRegistrationConfiguration.class);

    @Override
    public void configureTicketMetadataRegistrationPlan(final TicketMetadataCatalogRegistrationPlan plan) {
        LOGGER.debug("Registering core CAS protocol ticket metadata types...");

        registerTicketMetadata(plan, new TicketMetadata(TicketGrantingTicketImpl.class, TicketGrantingTicket.PREFIX, true));
        registerTicketMetadata(plan, new TicketMetadata(ServiceTicketImpl.class, ServiceTicket.PREFIX));
        registerTicketMetadata(plan, new TicketMetadata(ProxyGrantingTicketImpl.class, ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX));
        registerTicketMetadata(plan, new TicketMetadata(ProxyTicketImpl.class, ProxyTicket.PROXY_TICKET_PREFIX));
    }

    /**
     * Register ticket metadata.
     *
     * @param plan     the plan
     * @param metadata the metadata
     */
    protected void registerTicketMetadata(final TicketMetadataCatalogRegistrationPlan plan, final TicketMetadata metadata) {
        plan.registerTicketMetadata(metadata);
    }
}