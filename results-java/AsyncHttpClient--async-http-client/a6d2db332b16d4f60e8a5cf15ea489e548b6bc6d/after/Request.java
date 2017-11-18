/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package com.ning.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * The Request class can be used to construct HTTP request:
 * {@code
 *   Request r = new RequestBuilder().setUrl("url)
 *                      .setRealm((new Realm.RealmBuilder()).setPrincipal(user)
 *                      .setPassword(admin)
 *                      .setRealmName("MyRealm")
 *                      .setScheme(Realm.AuthScheme.DIGEST).build());
 *   r.execute();
 * }
 */
public interface Request {

    /**
     * An entity that can be used to manipulate the Request's body output before it get sent.
     */
    public static interface EntityWriter {
        public void writeEntity(OutputStream out) throws IOException;
    }

    /**
     * Return the request's type (GET, POST, etc.)
     * @return the request's type (GET, POST, etc.)
     */
    public RequestType getType();

    /**
     * Return the decoded url
     * @return  the decoded url
     */
    public String getUrl();

    /**
     * Return the undecoded url
     * @return the undecoded url
     */
    public String getRawUrl();

    /**
     * Return the current set of Headers.
     * @return a {@link FluentCaseInsensitiveStringsMap} contains headers.
     */
    public FluentCaseInsensitiveStringsMap getHeaders();

    /**
     * Return Coookie.
     * @return an unmodifiable Collection of Cookies
     */
    public Collection<Cookie> getCookies();

    /**
     * Return the current request's body as a byte array
     * @return a byte array of the current request's body.
     */
    public byte[] getByteData();

    /**
     * Return the current request's body as a string
     * @return an String representation of the current request's body.
     */
    public String getStringData();

    /**
     * Return the current request's body as an InputStream
     * @return an InputStream representation of the current request's body.
     */
    public InputStream getStreamData();

     /**
     * Return the current request's body as an EntityWriter
     * @return an EntityWriter representation of the current request's body.
     */
    public EntityWriter getEntityWriter();

    /**
     * Return the current size of the content-lenght header based on the body's size.
     * @return the current size of the content-lenght header based on the body's size.
     */
    public long getLength();

    /**
     * Return the current parameters.
     * @return a {@link FluentStringsMap} of parameters.
     */
    public FluentStringsMap getParams();

    /**
     * Return the current {@link Part}
     * @return  the current {@link Part}
     */
    public List<Part> getParts();

    /**
     * Return the virtual host value.
     * @return the virtual host value.
     */
    public String getVirtualHost();

    /**
     * Return the query params.
     * @return {@link FluentStringsMap} of query string
     */
    public FluentStringsMap getQueryParams();

    /**
     * Return the {@link ProxyServer}
     * @return the {@link ProxyServer}
     */
    public ProxyServer getProxyServer();

    /**
     * Return the {@link Realm}
     * @return the {@link Realm}
     */
    public Realm getRealm();
}