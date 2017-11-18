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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

import com.netflix.discovery.shared.resolver.ClusterResolver;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import com.netflix.discovery.shared.transport.EurekaHttpClientFactory;
import com.netflix.discovery.shared.transport.EurekaHttpResponse;
import com.netflix.discovery.shared.transport.TransportException;
import com.netflix.discovery.shared.transport.TransportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RetryableEurekaHttpClient} retries failed requests on subsequent servers in the cluster.
 * It maintains also simple quarantine list, so operations are not retried again on servers
 * that are not reachable at the moment.
 * <h3>Quarantine</h3>
 * All the servers to which communication failed are put on the quarantine list. First successful execution
 * clears this list, which makes those server eligible for serving future requests.
 * The list is also cleared once all available servers are exhausted.
 *
 * @author Tomasz Bak
 */
public class RetryableEurekaHttpClient extends EurekaHttpClientDecorator {

    private static final Logger logger = LoggerFactory.getLogger(RetryableEurekaHttpClient.class);

    private final ClusterResolver clusterResolver;
    private final EurekaHttpClientFactory clientFactory;
    private final int numberOfRetries;

    private final AtomicReference<EurekaHttpClient> delegate = new AtomicReference<>();

    private final Set<EurekaEndpoint> quarantineSet = new ConcurrentSkipListSet<>();

    public RetryableEurekaHttpClient(ClusterResolver clusterResolver,
                                     EurekaHttpClientFactory clientFactory,
                                     int numberOfRetries) {
        this.clusterResolver = clusterResolver;
        this.clientFactory = clientFactory;
        this.numberOfRetries = numberOfRetries;
    }

    @Override
    public void shutdown() {
        TransportUtils.shutdown(delegate.get());
    }

    @Override
    protected <R> EurekaHttpResponse<R> execute(RequestExecutor<R> requestExecutor) {
        List<EurekaEndpoint> candidateHosts = null;
        int endpointIdx = 0;
        for (int retry = 0; retry < numberOfRetries; retry++) {
            EurekaHttpClient currentHttpClient = delegate.get();
            EurekaEndpoint currentEndpoint = null;
            if (currentHttpClient == null) {
                if (candidateHosts == null) {
                    candidateHosts = getHostCandidates();
                    if (candidateHosts.isEmpty()) {
                        throw new TransportException("There is no known eureka server; cluster server list is empty");
                    }
                }
                if (endpointIdx >= candidateHosts.size()) {
                    throw new TransportException("Cannot execute request on any known server");
                }

                currentEndpoint = candidateHosts.get(endpointIdx++);
                currentHttpClient = clientFactory.create(currentEndpoint.getServiceUrl());
            }

            try {
                EurekaHttpResponse<R> response = requestExecutor.execute(currentHttpClient);
                delegate.set(currentHttpClient);
                return response;
            } catch (Exception e) {
                delegate.compareAndSet(currentHttpClient, null);
                if (currentEndpoint != null) {
                    quarantineSet.add(currentEndpoint);
                }
                logger.warn("Request execution failure", e);
            }
        }
        throw new TransportException("Retry limit reached; giving up on completing the request");
    }

    private List<EurekaEndpoint> getHostCandidates() {
        List<EurekaEndpoint> candidateHosts = clusterResolver.getClusterServers();
        quarantineSet.retainAll(candidateHosts);

        // If all hosts are bad, we have no choice but start over again
        if (quarantineSet.size() == candidateHosts.size()) {
            quarantineSet.clear();
        } else if (!quarantineSet.isEmpty()) {
            List<EurekaEndpoint> remainingHosts = new ArrayList<>(candidateHosts.size());
            for (EurekaEndpoint endpoint : candidateHosts) {
                if (!quarantineSet.contains(endpoint)) {
                    remainingHosts.add(endpoint);
                }
            }
            candidateHosts = remainingHosts;
        }
        return candidateHosts;
    }
}