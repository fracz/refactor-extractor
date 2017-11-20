/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HttpBasedServiceCredential;

/**
 * HttpBasedServiceCredentialsToPrincipalResolver extracts the callbackUrl from
 * the HttpBasedServiceCredential and constructs a SimpleService with the
 * callbackUrl as the unique Id.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class HttpBasedServiceCredentialsToPrincipalResolver implements
    CredentialsToPrincipalResolver {

    /**
     * Method to return a simple Service Principal with the identifier set to be
     * the callback url.
     * @return the resolved principal object
     */
    public Principal resolvePrincipal(final Credential credential) {
        final HttpBasedServiceCredential serviceCredentials = (HttpBasedServiceCredential) credential;
        return new SimpleWebApplicationServiceImpl(serviceCredentials.getCallbackUrl().toExternalForm());
    }

    /**
     * @return true if the credential provided are not null and are assignable
     * from HttpBasedServiceCredential, otherwise returns false.
     */
    public boolean supports(final Credential credential) {
        return credential != null
            && HttpBasedServiceCredential.class.isAssignableFrom(credential
                .getClass());
    }
}