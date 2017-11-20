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
package org.jasig.cas.authentication;

import java.security.GeneralSecurityException;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.NamedAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;

/**
 * Adapts a CAS 3.x {@link org.jasig.cas.authentication.handler.AuthenticationHandler} onto a CAS 4.x
 * {@link AuthenticationHandler}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class LegacyAuthenticationHandlerAdapter implements AuthenticationHandler {

    private final org.jasig.cas.authentication.handler.AuthenticationHandler legacyHandler;

    public LegacyAuthenticationHandlerAdapter(final org.jasig.cas.authentication.handler.AuthenticationHandler legacy) {
        this.legacyHandler = legacy;
    }

    @Override
    public HandlerResult authenticate(final Credentials credential) throws GeneralSecurityException, PreventedException {
        try {
            if (this.legacyHandler.authenticate(credential)) {
                return new HandlerResult(this, new BasicCredentialMetaData(credential));
            } else {
                throw new GeneralSecurityException(String.format("%s failed to authenticate %s", this.getName(), credential));
            }
        } catch (AuthenticationException e) {
            throw new GeneralSecurityException(String.format("%s failed to authenticate %s", this.getName(), credential), e);
        }
    }

    @Override
    public boolean supports(final Credentials credential) {
        return this.legacyHandler.supports(credential);
    }

    @Override
    public String getName() {
        if (this.legacyHandler instanceof NamedAuthenticationHandler) {
            return ((NamedAuthenticationHandler) this.legacyHandler).getName();
        } else {
            return this.legacyHandler.getClass().getSimpleName();
        }
    }
}