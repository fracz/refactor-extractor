/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel.socket.sctp;

import com.sun.nio.sctp.Association;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.SocketChannel;
import org.jboss.netty.channel.socket.SocketChannelConfig;
import org.jboss.netty.channel.socket.nio.NioSocketChannelConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://github.com/jestan">Jestan Nirojan</a>
 */
public interface SctpChannel extends Channel {

    /**
     * Return the primary local address of the SCTP channel.
     */
    @Override
    InetSocketAddress getLocalAddress();

    /**
     * Return all local addresses of the SCTP channel.
     */
    Set<InetSocketAddress> getAllLocalAddresses();

    /**
     * Returns the configuration of this channel.
     */
    @Override
    NioSctpChannelConfig getConfig();

    /**
     * Return the primary remote address of the SCTP channel.
     */
    @Override
    InetSocketAddress getRemoteAddress();


    /**
     * Return all remote addresses of the SCTP channel.
     */
    Set<InetSocketAddress> getAllRemoteAddresses();

    /**
     * Get the underlying SCTP association
     */
    Association association();
}