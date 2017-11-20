/*
 * Copyright 2013 Jean-Francois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.cpr;

import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.service.Get;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Message;
import org.atmosphere.util.SimpleBroadcaster;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class EncoderDecoderTest {
    private AtmosphereFramework framework;
    private static final AtomicReference<AtmosphereResource> r = new AtomicReference<AtmosphereResource>();
    private static final AtomicReference<String> message = new AtomicReference<String>();

    @BeforeMethod
    public void create() throws Throwable {
        framework = new AtmosphereFramework();
        framework.setDefaultBroadcasterClassName(SimpleBroadcaster.class.getName()) ;
        framework.addAnnotationPackage(ManagedMessage.class);
        framework.setAsyncSupport(new AsynchronousProcessor(framework.getAtmosphereConfig()) {

            @Override
            public Action service(AtmosphereRequest req, AtmosphereResponse res) throws IOException, ServletException {
                return suspended(req, res);
            }

            public void action(AtmosphereResourceImpl r) {
                try {
                    resumed(r.getRequest(), r.getResponse());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServletException e) {
                    e.printStackTrace();
                }
            }
        }).init(new ServletConfig() {
            @Override
            public String getServletName() {
                return "void";
            }

            @Override
            public ServletContext getServletContext() {
                return mock(ServletContext.class);
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        });
    }

    @AfterMethod
    public void after() {
        r.set(null);
        framework.destroy();
    }

    @ManagedService(path = "/f")
    public final static class ManagedMessage {

        @Get
        public void get(AtmosphereResource resource) {
            r.set(resource);
            resource.addEventListener(new AtmosphereResourceEventListenerAdapter() {
                @Override
                public void onSuspend(AtmosphereResourceEvent event) {
                    try {
                        event.getResource().getBroadcaster().broadcast("message").get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }).suspend();

        }

        @Message(encoders = {StringBufferEncoder.class}, decoders = {StringBufferDecoder.class})
        public void message(String m) {
            message.set(m);
        }
    }

    @ManagedService(path = "/g")
    public final static class DecodedMessage {

        @Get
        public void get(AtmosphereResource resource) {
            r.set(resource);
            resource.addEventListener(new AtmosphereResourceEventListenerAdapter() {
                @Override
                public void onSuspend(AtmosphereResourceEvent event) {
                    try {
                        event.getResource().getBroadcaster().broadcast("message").get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }).suspend();

        }

        @Message(decoders = {StringBufferDecoder.class})
        public void decode(StringBuffer m) {
            message.set(m.toString());
        }
    }

    @ManagedService(path = "/h")
    public final static class EncodedMessage {

        @Get
        public void get(AtmosphereResource resource) {
            r.set(resource);
            resource.addEventListener(new AtmosphereResourceEventListenerAdapter() {
                @Override
                public void onSuspend(AtmosphereResourceEvent event) {
                    try {
                        event.getResource().getBroadcaster().broadcast("message").get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }).suspend();

        }

        @Message(encoders = {StringBufferEncoder.class}, decoders = {StringBufferDecoder.class})
        public StringBuffer encode(StringBuffer m) {
            message.set(m.toString());
            return m;
        }
    }

    public final static class StringBufferEncoder implements Encoder<StringBuffer, String>  {

        @Override
        public String encode(StringBuffer s) {
            return s.toString() + "-yo!";
        }
    }

    public final static class StringBufferDecoder implements Decoder<String, StringBuffer> {

        @Override
        public StringBuffer decode(String s) {
            return  new StringBuffer(s);
        }
    }

    @Test
    public void testMessage() throws IOException, ServletException {

        AtmosphereRequest request = new AtmosphereRequest.Builder().pathInfo("/f").method("GET").build();
        framework.doCometSupport(request, AtmosphereResponse.newInstance());
        assertNotNull(r.get());
        r.get().resume();
        assertNotNull(message.get());
        assertEquals(message.get(), "message");

    }

    @Test
    public void testDecoder() throws IOException, ServletException {

        AtmosphereRequest request = new AtmosphereRequest.Builder().pathInfo("/g").method("GET").build();
        framework.doCometSupport(request, AtmosphereResponse.newInstance());
        assertNotNull(r.get());
        r.get().resume();
        assertNotNull(message.get());
        assertEquals(message.get(), "message");

    }

    @Test
    public void testEncoder() throws IOException, ServletException {

        AtmosphereRequest request = new AtmosphereRequest.Builder().pathInfo("/h").method("GET").build();
        AtmosphereResponse response = AtmosphereResponse.newInstance();
        final AtomicReference<String> ref = new AtomicReference();

        response.asyncIOWriter(new AsyncIOWriterAdapter() {
            @Override
            public AsyncIOWriter write(AtmosphereResponse r, String data) throws IOException {
                ref.set(data);
                return this;
            }
        });
        framework.doCometSupport(request, response);
        assertNotNull(r.get());
        r.get().resume();
        assertNotNull(message.get());
        assertEquals(message.get(), "message");
        assertEquals(ref.get(), "message-yo!");

    }
}