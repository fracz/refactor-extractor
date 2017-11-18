/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.discovery.shared.transport.decorator;

import java.util.concurrent.atomic.AtomicReference;

import com.netflix.discovery.shared.transport.EurekaHttpClient;
import com.netflix.discovery.shared.transport.EurekaHttpClientFactory;
import com.netflix.discovery.shared.transport.EurekaHttpResponse;
import com.netflix.discovery.shared.transport.TransportUtils;

/**
 * {@link SessionedEurekaHttpClient} enforces full reconnect at a regular interval (a session), preventing
 * a client to sticking to a particular Eureka server instance forever. This in turn guarantees even
 * load distribution in case of cluster topology change.
 *
 * @author Tomasz Bak
 */
public class SessionedEurekaHttpClient extends EurekaHttpClientDecorator {

    private final EurekaHttpClientFactory clientFactory;
    private final long sessionDurationMs;

    private volatile long lastReconnectTimeStamp;
    private final AtomicReference<EurekaHttpClient> eurekaHttpClientRef = new AtomicReference<>();

    public SessionedEurekaHttpClient(EurekaHttpClientFactory clientFactory, long sessionDurationMs) {
        this.clientFactory = clientFactory;
        this.sessionDurationMs = sessionDurationMs;
    }

    @Override
    protected <R> EurekaHttpResponse<R> execute(RequestExecutor<R> requestExecutor) {
        long now = System.currentTimeMillis();
        long delay = now - lastReconnectTimeStamp;
        if (delay >= sessionDurationMs) {
            lastReconnectTimeStamp = now;
            TransportUtils.shutdown(eurekaHttpClientRef.getAndSet(null));
        }

        EurekaHttpClient eurekaHttpClient = eurekaHttpClientRef.get();
        if (eurekaHttpClient == null) {
            eurekaHttpClient = TransportUtils.getOrSetAnotherClient(eurekaHttpClientRef, clientFactory.newClient());
        }
        return requestExecutor.execute(eurekaHttpClient);
    }

    @Override
    public void shutdown() {
        TransportUtils.shutdown(eurekaHttpClientRef.getAndSet(null));
    }
}