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
package org.jasig.cas.adaptors.radius;


import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.jradius.client.RadiusClient;
import net.jradius.dictionary.Attr_NASIPAddress;
import net.jradius.dictionary.Attr_NASIPv6Address;
import net.jradius.dictionary.Attr_NASIdentifier;
import net.jradius.dictionary.Attr_NASPort;
import net.jradius.dictionary.Attr_NASPortId;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.dictionary.vsa_redback.Attr_NASRealPort;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.PreventedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a RadiusServer that utilizes the JRadius packages available
 * at <a href="http://jradius.sf.net">http://jradius.sf.net</a>.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 3.1
 */
public final class JRadiusServerImpl implements RadiusServer {

    /** Default retry count, {@value}. */
    public static final int DEFAULT_RETRY_COUNT = 3;

    /** Logger instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JRadiusServerImpl.class);

    /** RADIUS protocol. */
    @NotNull
    private final RadiusProtocol protocol;

    /** Produces RADIUS client instances for authentication. */
    @NotNull
    private final RadiusClientFactory radiusClientFactory;

    /** Number of times to retry authentication when no response is received. */
    @Min(0)
    private int retries = DEFAULT_RETRY_COUNT;

    private String nasIpAddress = null;

    private String nasIpv6Address = null;

    private long nasPort = -1;

    private long nasPortId = -1;

    private long nasIdentifier;

    private long nasRealPort = -1;

    private long nasPortType = -1;


    /** Load the dictionary implementation. */
    static {
        AttributeFactory
        .loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
    }

    /**
     * Instantiates a new server implementation
     * with the radius protocol and client factory specified.
     *
     * @param protocol the protocol
     * @param clientFactory the client factory
     */
    public JRadiusServerImpl(final RadiusProtocol protocol, final RadiusClientFactory clientFactory) {
        this.protocol = protocol;
        this.radiusClientFactory = clientFactory;
    }

    @Override
    public RadiusResponse authenticate(final String username, final String password) throws PreventedException {

        final AttributeList attributeList = new AttributeList();

        attributeList.add(new Attr_UserName(username));
        attributeList.add(new Attr_UserPassword(password));

        if (StringUtils.isNotBlank(this.nasIpAddress)) {
            attributeList.add(new Attr_NASIPAddress(this.nasIpAddress));
        }
        if (StringUtils.isNotBlank(this.nasIpv6Address)) {
            attributeList.add(new Attr_NASIPv6Address(this.nasIpv6Address));
        }

        if (this.nasPort != -1) {
            attributeList.add(new Attr_NASPort(this.nasPort));
        }
        if (this.nasPortId != -1) {
            attributeList.add(new Attr_NASPortId(this.nasPortId));
        }
        if (this.nasIdentifier != -1) {
            attributeList.add(new Attr_NASIdentifier(this.nasIdentifier));
        }
        if (this.nasRealPort != -1) {
            attributeList.add(new Attr_NASRealPort(this.nasRealPort));
        }
        if (this.nasPortType != -1) {
            attributeList.add(new Attr_NASPortType(this.nasPortType));
        }

        RadiusClient client = null;
        try {
            client = this.radiusClientFactory.newInstance();
            final AccessRequest request = new AccessRequest(client, attributeList);
            final RadiusPacket response = client.authenticate(
                    request,
                    RadiusClient.getAuthProtocol(this.protocol.getName()),
                    this.retries);

            LOGGER.debug("RADIUS response from {}: {}",
                    client.getRemoteInetAddress().getCanonicalHostName(),
                    response.getClass().getName());

            if (response instanceof AccessAccept) {
                final AccessAccept acceptedResponse = (AccessAccept) response;

                return new RadiusResponse(acceptedResponse.getCode(),
                        acceptedResponse.getIdentifier(),
                        acceptedResponse.getAttributes().getAttributeList());
            }
        } catch (final Exception e) {
            throw new PreventedException(e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }


    /**
     * @since 4.1
     */
    public void setNasIpAddress(final String nasIpAddress) {
        this.nasIpAddress = nasIpAddress;
    }

    /**
     * @since 4.1
     */
    public void setNasIpv6Address(final String nasIpv6Address) {
        this.nasIpv6Address = nasIpv6Address;
    }

    /**
     * @since 4.1
     */
    public void setNasPort(final long nasPort) {
        this.nasPort = nasPort;
    }

    /**
     * @since 4.1
     */
    public void setNasPortId(final long nasPortId) {
        this.nasPortId = nasPortId;
    }

    /**
     * @since 4.1
     */
    public void setNasIdentifier(final long nasIdentifier) {
        this.nasIdentifier = nasIdentifier;
    }

    /**
     * @since 4.1
     */
    public void setNasRealPort(final long nasRealPort) {
        this.nasRealPort = nasRealPort;
    }

    /**
     * @since 4.1
     */
    public void setNasPortType(final long nasPortType) {
        this.nasPortType = nasPortType;
    }

    /**
     * @since 4.1
     */
    public void setRetries(final int retries) {
        this.retries = retries;
    }

}