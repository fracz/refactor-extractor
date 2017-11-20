package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketMetadataCatalog;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link JpaTicketRegistryTicketMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("hazelcastTicketRegistryMapsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaTicketRegistryTicketMetadataConfiguration extends CasProtocolCoreTicketMetadataCatalogConfiguration {
    @Override
    protected void buildAndRegisterTicketGrantingTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setCascade(true);
        super.buildAndRegisterTicketGrantingTicketMetadata(plan, metadata);
    }
}