/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.internal.resource.transport.http;


import org.apache.http.auth.AuthScope;
import org.gradle.internal.resource.PasswordCredentials;

import java.util.Collections;
import java.util.Set;

public class DefaultHttpSettings implements HttpSettings {
    private final PasswordCredentials passwordCredentials;
    private final HttpProxySettings proxySettings = new JavaSystemPropertiesHttpProxySettings();
    private final Set<String> authSchemes;

    public DefaultHttpSettings(PasswordCredentials passwordCredentials) {
        this.passwordCredentials = passwordCredentials;
        this.authSchemes = Collections.singleton(AuthScope.ANY_SCHEME);
    }

    public DefaultHttpSettings(PasswordCredentials passwordCredentials, Set<String> authSchemes) {
        this.passwordCredentials = passwordCredentials;
        this.authSchemes = authSchemes;
    }

    @Override
    public PasswordCredentials getCredentials() {
        return passwordCredentials;
    }

    @Override
    public HttpProxySettings getProxySettings() {
        return proxySettings;
    }

    @Override
    public Set<String> getAuthSchemes() {
        return authSchemes;
    }
}