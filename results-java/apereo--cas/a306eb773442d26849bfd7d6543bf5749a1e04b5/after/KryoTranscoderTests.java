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
package org.jasig.cas.ticket.registry.support.kryo;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.FieldSerializer;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.CredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link KryoTranscoder} class.
 *
 * @author Marvin S. Addison
 */
@RunWith(Parameterized.class)
public class KryoTranscoderTests {

    private final KryoTranscoder transcoder;

    public KryoTranscoderTests(final int bufferSize) {
        transcoder = new KryoTranscoder(bufferSize);
        final Map<Class<?>, Serializer> serializerMap = new HashMap<Class<?>, Serializer>();
        serializerMap.put(
                MockServiceTicket.class,
                new FieldSerializer(transcoder.getKryo(), MockServiceTicket.class));
        serializerMap.put(
                MockTicketGrantingTicket.class,
                new FieldSerializer(transcoder.getKryo(), MockTicketGrantingTicket.class));
        transcoder.setSerializerMap(serializerMap);
        transcoder.initialize();
    }

    @Parameterized.Parameters
    public static List<Object[]> getTestParms() {
        final List<Object[]> params = new ArrayList<Object[]>(6);

        // Test case #1 - Buffer is bigger than encoded data
        params.add(new Object[] {1024});

        // Test case #2 - Buffer overflow case
        params.add(new Object[] {10});
        return params;
    }

    @Test
    public void testEncodeDecode() throws Exception {
        final ServiceTicket expectedST =
                new MockServiceTicket("ST-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK");
        assertEquals(expectedST, transcoder.decode(transcoder.encode(expectedST)));

        final TicketGrantingTicket expectedTGT =
                new MockTicketGrantingTicket("TGT-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK-cas1");
        expectedTGT.grantServiceTicket("ST-1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890ABCDEFGHIJK", null, null, false);
        assertEquals(expectedTGT, transcoder.decode(transcoder.encode(expectedTGT)));
    }

    static class MockServiceTicket implements ServiceTicket {
        private String id;

        MockServiceTicket() { /* for serialization */ }

        public MockServiceTicket(final String id) {
            this.id = id;
        }

        public Service getService() {
            return null;
        }

        public boolean isFromNewLogin() {
            return false;
        }

        public boolean isValidFor(final Service service) {
            return false;
        }

        public TicketGrantingTicket grantTicketGrantingTicket(final String id, final Authentication authentication,
                final ExpirationPolicy expirationPolicy) {
            return null;
        }

        public String getId() {
            return id;
        }

        public boolean isExpired() {
            return false;
        }

        public TicketGrantingTicket getGrantingTicket() {
            return null;
        }

        public long getCreationTime() {
            return 0;
        }

        public int getCountOfUses() {
            return 0;
        }

        public boolean equals(final Object other) {
            return other instanceof MockServiceTicket && ((MockServiceTicket) other).getId().equals(id);
        }
    }

    static class MockTicketGrantingTicket implements TicketGrantingTicket {
        private String id;

        private int usageCount = 0;

        private Date creationDate = new Date();

        private final Authentication authentication;

        /** Constructor for serialization support. */
        MockTicketGrantingTicket() {
            this(null);
        }

        public MockTicketGrantingTicket(final String id) {
            this.id = id;
            final Credential credental = new UsernamePasswordCredential("handymanbob", "foo");
            final CredentialMetaData credentialMetaData = new BasicCredentialMetaData(credental);
            final AuthenticationBuilder builder = new AuthenticationBuilder();
            final Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("nickname", "bob");
            builder.setPrincipal(new SimplePrincipal("handymanbob", attributes));
            builder.setAuthenticationDate(new Date());
            builder.addCredential(credentialMetaData);
            final AuthenticationHandler handler = new MockAuthenticationHandler();
            try {
                builder.addSuccess(handler.getName(), handler.authenticate(credental));
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
            this.authentication = builder.build();
        }

        @Override
        public Authentication getAuthentication() {
            return this.authentication;
        }

        @Override
        public List<Authentication> getSupplementalAuthentications() {
            return Collections.emptyList();
        }

        @Override
        public ServiceTicket grantServiceTicket(
                final String id,
                final Service service,
                final ExpirationPolicy expirationPolicy,
                final boolean credentialsProvided) {
            this.usageCount++;
            return new MockServiceTicket(id);
        }

        @Override
        public Map<String, Service> getServices() {
            return Collections.emptyMap();
        }

        @Override
        public void removeAllServices() {}

        @Override
        public void markTicketExpired() {}

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public TicketGrantingTicket getRoot() {
            return this;
        }

        @Override
        public List<Authentication> getChainedAuthentications() {
            return Collections.emptyList();
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public TicketGrantingTicket getGrantingTicket() {
            return this;
        }

        @Override
        public long getCreationTime() {
            return this.creationDate.getTime();
        }

        @Override
        public int getCountOfUses() {
            return this.usageCount;
        }

        public boolean equals(final Object other) {
            return other instanceof MockTicketGrantingTicket
                    && ((MockTicketGrantingTicket) other).getId().equals(this.id)
                    && ((MockTicketGrantingTicket) other).getCountOfUses() == this.usageCount
                    && ((MockTicketGrantingTicket) other).getCreationTime() == this.creationDate.getTime()
                    && ((MockTicketGrantingTicket) other).getAuthentication().equals(this.authentication);
        }
    }

    public static class MockAuthenticationHandler implements AuthenticationHandler {

        @Override
        public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException, PreventedException {
            return new HandlerResult(this, new BasicCredentialMetaData(credential));
        }

        @Override
        public boolean supports(final Credential credential) {
            return true;
        }

        @Override
        public String getName() {
            return "MockAuthenticationHandler";
        }
    }
}