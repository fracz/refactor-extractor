/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo.serial;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.esotericsoftware.kryo.Kryo;
import org.jasig.cas.authentication.principal.GoogleAccountsService;
import org.jasig.cas.ticket.registry.support.kryo.FieldHelper;

/**
 * Serializer for {@link GoogleAccountsService}.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class GoogleAccountsServiceSerializer extends AbstractWebApplicationServiceSerializer<GoogleAccountsService> {

    private static final Constructor CONSTRUCTOR;

    private final PrivateKey privateKey;

    private final PublicKey publicKey;

    private final String alternateUsername;

    static {
        try {
            CONSTRUCTOR = GoogleAccountsService.class.getDeclaredConstructor(
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    PrivateKey.class,
                    PublicKey.class,
                    String.class);
            CONSTRUCTOR.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Expected constructor signature not found.", e);
        }
    }

    public GoogleAccountsServiceSerializer(
            final Kryo kryo,
            final FieldHelper helper,
            final PublicKey publicKey,
            final PrivateKey privateKey,
            final String alternateUsername) {

        super(kryo, helper);
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.alternateUsername = alternateUsername;
    }

    public void write(final ByteBuffer buffer, final GoogleAccountsService service) {
        super.write(buffer, service);
        kryo.writeObject(buffer, fieldHelper.getFieldValue(service, "requestId"));
        kryo.writeObject(buffer, fieldHelper.getFieldValue(service, "relayState"));
    }

    protected GoogleAccountsService createService(
            final ByteBuffer buffer,
            final String id,
            final String originalUrl,
            final String artifactId) {

        final String requestId = kryo.readObject(buffer, String.class);
        final String relayState = kryo.readObject(buffer, String.class);
        try {
            final GoogleAccountsService service = (GoogleAccountsService) CONSTRUCTOR.newInstance(
                    id,
                    originalUrl,
                    artifactId,
                    relayState,
                    requestId,
                    privateKey,
                    publicKey,
                    alternateUsername);
            return service;
        } catch (final Exception e) {
            throw new IllegalStateException("Error creating SamlService", e);
        }
    }
}