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
 */
package com.ning.http.client.async;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncCompletionHandlerBase;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ConnectionsPool;
import com.ning.http.client.Response;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

public class ConnectionPoolTest extends AbstractBasicTest {
    protected final Logger log = LoggerFactory.getLogger(AbstractBasicTest.class);

    @Test(groups = "standalone")
    public void testMaxTotalConnections() {
        AsyncHttpClient client = new AsyncHttpClient(
                new AsyncHttpClientConfig.Builder()
                        .setConnectionTimeoutInMs(100)
                        .setRequestTimeoutInMs(100)
                        .setAllowPoolingConnection(true)
                        .setMaximumConnectionsTotal(1)
                        .build()
        );

        String url = getTargetUrl();
        int i;
        Exception exception = null;
        for (i = 0; i < 3; i++) {
            try {
                log.info("{} requesting url [{}]...", i, url);
                Response response = client.prepareGet(url).execute().get();
                log.info("{} response [{}].", i, response);
            } catch (Exception ex) {
                ex.printStackTrace();
                exception = ex;
            }
        }
        assertNull(exception);
    }

    @Test(groups = {"standalone", "async"}, enabled = true, invocationCount = 10, alwaysRun = true)
    public void asyncDoGetKeepAliveHandlerTest_channelClosedDoesNotFail() throws Throwable {
        AsyncHttpClient client = new AsyncHttpClient();

        // Use a l in case the assert fail
        final CountDownLatch l = new CountDownLatch(2);

        final Map<String, Boolean> remoteAddresses = new
                ConcurrentHashMap<String, Boolean>();

        AsyncCompletionHandler<Response> handler = new
                AsyncCompletionHandlerAdapter() {

                    @Override
                    public Response onCompleted(Response response) throws
                            Exception {
                        System.out.println("ON COMPLETED INVOKED " +
                                response.getHeader("X-KEEP-ALIVE"));
                        try {
                            assertEquals(response.getStatusCode(), 200);
                            remoteAddresses.put(response.getHeader("X-KEEP-ALIVE"), true);
                        } finally {
                            l.countDown();
                        }
                        return response;
                    }
                };

        client.prepareGet(getTargetUrl()).execute(handler).get();
        server.stop();
        server.start();
        client.prepareGet(getTargetUrl()).execute(handler);

        if (!l.await(TIMEOUT, TimeUnit.SECONDS)) {
            Assert.fail("Timed out");
        }

        assertEquals(remoteAddresses.size(), 2);
    }

    @Test(groups = "standalone")
    public void testInvalidConnectionsPool() {

        ConnectionsPool<String, Channel> cp = new ConnectionsPool<String, Channel>() {

            public boolean offer(String key, Channel connection) {
                return false;
            }

            public Channel poll(String connection) {
                return null;
            }

            public boolean removeAll(Channel connection) {
                return false;
            }

            public boolean canCacheConnection() {
                return false;
            }

            public void destroy() {

            }
        };

        AsyncHttpClient client = new AsyncHttpClient(
                new AsyncHttpClientConfig.Builder()
                        .setConnectionsPool(cp)
                        .build()
        );

        Exception exception = null;
        try {
            client.prepareGet(getTargetUrl()).execute().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(exception.getMessage(), "Too many connections -1");
    }

    @Test(groups = "standalone")
    public void testValidConnectionsPool() {

        ConnectionsPool<String, Channel> cp = new ConnectionsPool<String, Channel>() {

            public boolean offer(String key, Channel connection) {
                return true;
            }

            public Channel poll(String connection) {
                return null;
            }

            public boolean removeAll(Channel connection) {
                return false;
            }

            public boolean canCacheConnection() {
                return true;
            }

            public void destroy() {

            }
        };

        AsyncHttpClient client = new AsyncHttpClient(
                new AsyncHttpClientConfig.Builder()
                        .setConnectionsPool(cp)
                        .build()
        );

        Exception exception = null;
        try {
            client.prepareGet(getTargetUrl()).execute().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
            exception = ex;
        }
        assertNull(exception);
    }


    @Test(groups = "standalone")
    public void multipleMaxConnectionOpenTest() throws Throwable {
        AsyncHttpClientConfig cg = new AsyncHttpClientConfig.Builder().setAllowPoolingConnection(true)
                .setConnectionTimeoutInMs(5000).setMaximumConnectionsTotal(1).build();
        AsyncHttpClient c = new AsyncHttpClient(cg);

        String body = "hello there";

        // once
        Response response = c.preparePost(getTargetUrl())
                .setBody(body)
                .execute().get(TIMEOUT, TimeUnit.SECONDS);

        assertEquals(response.getResponseBody(), body);

        // twice
        Exception exception = null;
        try {
            c.preparePost(String.format("http://127.0.0.1:%d/foo/test", port2)).setBody(body).execute().get(TIMEOUT, TimeUnit.SECONDS);
            fail("Should throw exception. Too many connections issued.");
        } catch (Exception ex) {
            ex.printStackTrace();
            exception = ex;
        }
        assertNotNull(exception);
        assertEquals(exception.getMessage(), "Too many connections 1");
    }

    @Test(groups = "standalone")
    public void multipleMaxConnectionOpenTestWithQuery() throws Throwable {
        AsyncHttpClientConfig cg = new AsyncHttpClientConfig.Builder().setAllowPoolingConnection(true)
                .setConnectionTimeoutInMs(5000).setMaximumConnectionsTotal(1).build();
        AsyncHttpClient c = new AsyncHttpClient(cg);

        String body = "hello there";

        // once
        Response response = c.preparePost(getTargetUrl() + "?foo=bar")
                .setBody(body)
                .execute().get(TIMEOUT, TimeUnit.SECONDS);

        assertEquals(response.getResponseBody(), "foo_" + body);

        // twice
        Exception exception = null;
        try {
            response = c.preparePost(getTargetUrl()).setBody(body).execute().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception ex) {
            ex.printStackTrace();
            exception = ex;
        }
        assertNull(exception);
        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test(groups = {"online", "async"})
    public void asyncDoGetMaxConnectionsTest() throws Throwable {
        AsyncHttpClient client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setMaximumConnectionsTotal(2).build());

        // Use a l in case the assert fail
        final CountDownLatch l = new CountDownLatch(2);

        AsyncCompletionHandler<Response> handler = new AsyncCompletionHandlerAdapter() {

            @Override
            public Response onCompleted(Response response) throws Exception {
                l.countDown();
                return response;
            }

            @Override
            public void onThrowable(Throwable t) {
                try {
                    Assert.fail("Unexpected exception", t);
                } finally {
                    l.countDown();
                }
            }
        };

        client.prepareGet("http://www.oracle.com/index.html").execute(handler).get();
        client.prepareGet("http://www.apache.org/").execute(handler).get();

        try {
            client.prepareGet("http://www.ning.com/").execute(handler).get();
            Assert.fail();
        } catch (IOException ex) {
            String s = ex.getMessage();
            assertEquals(s, "Too many connections 2");
        }

        if (!l.await(TIMEOUT, TimeUnit.SECONDS)) {
            Assert.fail("Timed out");
        }
        client.close();
    }

    /**
     * This test just make sure the hack used to catch disconnected channel under win7 doesn't throw any exception.
     * The onComplete method must be only called once.
     *
     * @throws Throwable if something wrong happens.
     */
    @Test(groups = "standalone")
    public void win7DisconnectTest() throws Throwable {
        final AtomicInteger count = new AtomicInteger(0);

        AsyncHttpClient client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build());
        AsyncCompletionHandler<Response> handler = new
                AsyncCompletionHandlerAdapter() {

                    @Override
                    public Response onCompleted(Response response) throws
                            Exception {

                        count.incrementAndGet();
                        StackTraceElement e = new StackTraceElement("sun.nio.ch.SocketDispatcher", "read0", null, -1);
                        IOException t = new IOException();
                        t.setStackTrace(new StackTraceElement[]{e});
                        throw t;
                    }
                };

        try {
            client.prepareGet(getTargetUrl()).execute(handler).get();
            fail("Must have received an exception");
        } catch (ExecutionException ex) {
            assertNotNull(ex);
            assertNotNull(ex.getCause());
            assertEquals(ex.getCause().getCause().getClass(), IOException.class);
            assertEquals(count.get(), 1);
        }
    }

    @Test(groups = "standalone")
    public void asyncHandlerOnThrowableTest() throws Throwable {
        AsyncHttpClient client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build());
        final AtomicInteger count = new AtomicInteger();
        final String THIS_IS_NOT_FOR_YOU = "This is not for you";
        final CountDownLatch latch = new CountDownLatch(16);
        for (int i = 0; i < 16; i++) {
            client.prepareGet(getTargetUrl()).execute(new AsyncCompletionHandlerBase() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    throw new Exception(THIS_IS_NOT_FOR_YOU);
                }
            });

            client.prepareGet(getTargetUrl()).execute(new AsyncCompletionHandlerBase() {
                /* @Override */
                public void onThrowable(Throwable t) {
                    if (t.getMessage() != null && t.getMessage().equalsIgnoreCase(THIS_IS_NOT_FOR_YOU)) {
                        count.incrementAndGet();
                    }
                }

                @Override
                public Response onCompleted(Response response) throws Exception {
                    latch.countDown();
                    return response;
                }
            });
        }
        latch.await(TIMEOUT, TimeUnit.SECONDS);
        assertEquals(count.get(), 0);
    }
}
