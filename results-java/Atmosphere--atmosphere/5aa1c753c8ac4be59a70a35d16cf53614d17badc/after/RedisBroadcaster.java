/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 */
package org.atmosphere.plugin.redis;


import org.atmosphere.util.AbstractBroadcasterProxy;
import org.atmosphere.util.LoggerUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple {@link org.atmosphere.cpr.Broadcaster} implementation based on Jedis
 *
 * @author Jeanfrancois Arcand
 */
public class RedisBroadcaster extends AbstractBroadcasterProxy {
    private final Jedis jedisSubscriber;
    private final Jedis jedisPublisher;
    private final URI uri;
    static final Logger logger = LoggerUtils.getLogger();

    public RedisBroadcaster() {
        this(RedisBroadcaster.class.getSimpleName(), URI.create("http://localhost:6379"));
    }

    public RedisBroadcaster(String id) {
        this(id, URI.create("http://localhost:6379"));
    }

    public RedisBroadcaster(URI uri) {
        this(RedisBroadcaster.class.getSimpleName(), uri);
    }

    public RedisBroadcaster(String id, URI uri) {
        super(id);
        this.uri = uri;

        jedisSubscriber = new Jedis(uri.getHost(), uri.getPort(), 500);
        try {
            jedisSubscriber.connect();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "", e);
        }

        jedisSubscriber.auth("atmosphere");
        jedisSubscriber.flushAll();

        jedisPublisher = new Jedis(uri.getHost(), uri.getPort(), 500);
        try {
            jedisPublisher.connect();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "", e);
        }
        jedisPublisher.auth("atmosphere");
        jedisPublisher.flushAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        try {
            jedisPublisher.disconnect();
            jedisSubscriber.disconnect();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incomingBroadcast() {
        jedisSubscriber.subscribe(new JedisPubSub() {
            public void onMessage(String channel, String message) {
                broadcastReceivedMessage(message);
            }

            public void onSubscribe(String channel, int subscribedChannels) {
            }

            public void onUnsubscribe(String channel, int subscribedChannels) {
            }

            public void onPSubscribe(String pattern, int subscribedChannels) {
            }

            public void onPUnsubscribe(String pattern, int subscribedChannels) {
            }

            public void onPMessage(String pattern, String channel, String message) {
            }
        }, getID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outgoingBroadcast(Object message) {
        jedisPublisher.publish(getID(), message.toString());
    }

}