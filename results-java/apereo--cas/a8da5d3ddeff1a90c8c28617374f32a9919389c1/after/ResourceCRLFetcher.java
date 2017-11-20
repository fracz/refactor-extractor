/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.adaptors.x509.authentication.handler.support;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509CRL;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the fetching of CRL objects based on resources.
 * Supports http/ldap resources.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class ResourceCRLFetcher implements CRLFetcher {
    /** Logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates a new instance using the specified resources for CRL data.
     */
    public ResourceCRLFetcher() {

    }

    @Override
    public final Map<URL, X509CRL> fetch(@NotNull final Resource[] crls) throws Exception {
        if (crls.length == 0) {
            throw new IllegalArgumentException("Must provide at least one non-null CRL resource.");
        }

        final Map<URL, X509CRL> results = new HashMap<>();
        for (final Resource r : crls) {
            logger.debug("Fetching CRL data from {}", r);
            results.put(r.getURL(), fetchInternal(r));
        }
        return results;
    }

    @Override
    public X509CRL fetch(@NotNull final Resource crl) throws Exception {
        return fetch(new Resource[] {crl}).entrySet().iterator().next().getValue();
    }

    /**
     * Fetch the resource. Designed so that extensions
     * can decide what and how the resource should be retrieved.
     *
     * @param r the r
     * @return the x 509 cRL
     * @throws Exception the exception
     */
    protected X509CRL fetchInternal(final Resource r) throws Exception {
        final InputStream in = r.getInputStream();
        if (r.getInputStream() != null) {
            return (X509CRL) CertUtils.getCertificateFactory().generateCRL(in);
        }

        try (final InputStream ins = r.getURL().openStream()) {
            return (X509CRL) CertUtils.getCertificateFactory().generateCRL(ins);
        }
    }
}