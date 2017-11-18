/*
 * Copyright 2012-2014 TORCH GmbH
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.graylog2.shared.bindings;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.graylog2.inputs.gelf.gelf.GELFChunkManager;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.shared.bindings.providers.GELFChunkManagerProvider;
import org.graylog2.shared.bindings.providers.NodeIdProvider;
import org.graylog2.shared.bindings.providers.ObjectMapperProvider;
import org.graylog2.shared.bindings.providers.ProcessBufferProvider;
import org.graylog2.shared.buffers.ProcessBuffer;
import org.graylog2.shared.buffers.ProcessBufferWatermark;
import org.graylog2.shared.stats.ThroughputStats;

/**
 * @author Dennis Oelkers <dennis@torch.sh>
 */
public class GenericBindings extends AbstractModule {
    private final InstantiationService instantiationService;

    public GenericBindings(InstantiationService instantiationService) {
        this.instantiationService = instantiationService;
    }

    @Override
    protected void configure() {
        // This is holding all our metrics.
        bind(MetricRegistry.class).toInstance(new MetricRegistry());
        bind(ThroughputStats.class).toInstance(new ThroughputStats());
        bind(ProcessBufferWatermark.class).toInstance(new ProcessBufferWatermark());

        bind(InstantiationService.class).toInstance(instantiationService);

        install(new FactoryModuleBuilder().build(ProcessBuffer.Factory.class));

        bind(ProcessBuffer.class).toProvider(ProcessBufferProvider.class);
        bind(GELFChunkManager.class).toProvider(GELFChunkManagerProvider.class);
        bind(NodeId.class).toProvider(NodeIdProvider.class);
        bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class);
    }
}