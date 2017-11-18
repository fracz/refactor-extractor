/*
 *  Copyright 2009-2016 Weibo, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.weibo.api.motan.registry.zookeeper;

import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.registry.NotifyListener;
import com.weibo.api.motan.registry.support.AbstractRegistry;
import com.weibo.api.motan.rpc.URL;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ZookeeperRegistryTest {
    private static JUnit4Mockery mockery = null;
    private ZookeeperRegistry registry;

    @Before
    public void setUp() throws Exception {
        // zookeeper://127.0.0.1:2181/com.weibo.api.motan.registry.RegistryService?group=yf_rpc
        URL url = new URL("zookeeper", "127.0.0.1", 2181, "com.weibo.api.motan.registry.RegistryService");
        mockery = new JUnit4Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        ZkClient mockZkClient = mockery.mock(ZkClient.class);
        registry = new ZookeeperRegistry(url, mockZkClient);

        final List<String> currentChilds = new ArrayList<String>();

        mockery.checking(new Expectations() {
            {
                allowing(any(ZkClient.class)).method("exists");
                will(returnValue(false));
                allowing(any(ZkClient.class)).method("delete");
                will(returnValue(true));
                allowing(any(ZkClient.class)).method("createPersistent");
                will(returnValue(null));
                allowing(any(ZkClient.class)).method("createEphemeral");
                will(returnValue(null));
                allowing(any(ZkClient.class)).method("subscribeChildChanges");
                will(returnValue(currentChilds));
                allowing(any(ZkClient.class)).method("unsubscribeChildChanges");
                will(returnValue(null));
                allowing(any(ZkClient.class)).method("readData");
                will(returnValue("motan://127.0.0.1:8001/com.weibo.motan.demo.service.MotanDemoService?export=demoMotan:8002&protocol=motan&module=motan-demo-rpc&application=myMotanDemo&refreshTimestamp=1459216241466&maxContentLength=1048576&id=com.weibo.api.motan.config.springsupport.ServiceConfigBean&maxServerConnection=80000&maxWorkerThread=800&accessLog=true&requestTimeout=200&isDefault=true&minWorkerThread=20&group=motan-demo-rpc&nodeType=service&shareChannel=true&"));
                allowing(any(ZkClient.class)).method("getChildren");
                will(returnValue(currentChilds));
            }
        });
    }

    @Test
    public void testDoRegister() {
        URL url = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 8001, "com.weibo.motan.demo.service.MotanDemoService");

        registry.register(url);
        Collection<URL> registeredUrls = registry.getRegisteredServiceUrls();
        assertTrue(registeredUrls.contains(url));
    }

    @Test
    public void testDoUnregister() {
        URL url = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 8001, "com.weibo.motan.demo.service.MotanDemoService");
        registry.doUnregister(url);
        Collection<URL> registeredUrls = registry.getRegisteredServiceUrls();
        assertFalse(registeredUrls.contains(url));
    }

    @Test
    public void testDoSubscribe() {
        URL url = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 0, "com.weibo.motan.demo.service.MotanDemoService");
        NotifyListener notifyListener = new NotifyListener() {
            @Override
            public void notify(URL registryUrl, List<URL> urls) {

            }
        };
        registry.doSubscribe(url, notifyListener);
        ConcurrentHashMap<URL, ConcurrentHashMap<NotifyListener, IZkChildListener>> urlListeners = registry.getUrlListeners();
        assertTrue(urlListeners.containsKey(url));
    }

    @Test
    public void testDoUnsubscribe() {
        URL url = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 0, "com.weibo.motan.demo.service.MotanDemoService");
        NotifyListener notifyListener = new NotifyListener() {
            @Override
            public void notify(URL registryUrl, List<URL> urls) {

            }
        };
        registry.doUnsubscribe(url, notifyListener);
        ConcurrentHashMap<URL, ConcurrentHashMap<NotifyListener, IZkChildListener>> urlListeners = registry.getUrlListeners();
        assertFalse(urlListeners.containsKey(url));
    }

    @Test
    public void testDoDiscover() {
        URL url = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 0, "com.weibo.motan.demo.service.MotanDemoService");
        registry.doDiscover(url);
    }

    @Test
    public void testDoAvailable() throws Exception {
        final Set<URL> urls = new HashSet<URL>();
        URL url1 = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 8001, "com.weibo.motan.demo.service.MotanDemoService");
        URL url2 = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 8002, "com.weibo.motan.demo.service.MotanDemoService");
        URL url3 = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 8003, "com.weibo.motan.demo.service.MotanDemoService");
        urls.add(url1);
        urls.add(url2);
        urls.add(url3);

        for (URL u : urls) {
            registry.register(u);
        }
        registry.available(url1);
        registry.available(null);
    }

    @Test
    public void testDoUnavailable() throws Exception {
        final Set<URL> urls = new HashSet<URL>();
        URL url1 = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 8001, "com.weibo.motan.demo.service.MotanDemoService");
        URL url2 = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 8002, "com.weibo.motan.demo.service.MotanDemoService");
        URL url3 = new URL(MotanConstants.PROTOCOL_MOTAN, "127.0.0.1", 8003, "com.weibo.motan.demo.service.MotanDemoService");
        urls.add(url1);
        urls.add(url2);
        urls.add(url3);

        for (URL u : urls) {
            registry.register(u);
        }
        registry.unavailable(url1);
        registry.unavailable(null);
    }
}