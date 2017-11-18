/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.vertx.java.core.net;

import org.vertx.java.core.Handler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

public class NetServerOptions extends TCPOptions {

  // Server specific HTTP stuff

  private int port = 0;
  private String host = "0.0.0.0";
  private int acceptBacklog = 1024;

  // Server specific SSL stuff

  private boolean clientAuthRequired;

  public NetServerOptions() {
    super();
  }

  public NetServerOptions(NetServerOptions other) {
    super(other);
    this.port = other.port;
    this.host = other.host;
    this.acceptBacklog = other.acceptBacklog;
  }

  public boolean isClientAuthRequired() {
    return clientAuthRequired;
  }

  public NetServerOptions setClientAuthRequired(boolean clientAuthRequired) {
    this.clientAuthRequired = clientAuthRequired;
    return this;
  }

  public int getAcceptBacklog() {
    return acceptBacklog;
  }

  public NetServerOptions setAcceptBacklog(int acceptBacklog) {
    this.acceptBacklog = acceptBacklog;
    return this;
  }

  public int getPort() {
    return port;
  }

  public NetServerOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public String getHost() {
    return host;
  }

  public NetServerOptions setHost(String host) {
    this.host = host;
    return this;
  }

  // Override common implementation

  @Override
  public NetServerOptions setSendBufferSize(int sendBufferSize) {
    super.setSendBufferSize(sendBufferSize);
    return this;
  }

  @Override
  public NetServerOptions setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }

  @Override
  public NetServerOptions setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }

  @Override
  public NetServerOptions setTrafficClass(int trafficClass) {
    super.setTrafficClass(trafficClass);
    return this;
  }

  @Override
  public NetServerOptions setTcpNoDelay(boolean tcpNoDelay) {
    super.setTcpNoDelay(tcpNoDelay);
    return this;
  }

  @Override
  public NetServerOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    super.setTcpKeepAlive(tcpKeepAlive);
    return this;
  }

  @Override
  public NetServerOptions setSoLinger(int soLinger) {
    super.setSoLinger(soLinger);
    return this;
  }

  @Override
  public NetServerOptions setUsePooledBuffers(boolean usePooledBuffers) {
    super.setUsePooledBuffers(usePooledBuffers);
    return this;
  }

  @Override
  public NetServerOptions setSsl(boolean ssl) {
    super.setSsl(ssl);
    return this;
  }

  @Override
  public NetServerOptions setKeyStorePath(String keyStorePath) {
    super.setKeyStorePath(keyStorePath);
    return this;
  }

  @Override
  public NetServerOptions setKeyStorePassword(String keyStorePassword) {
    super.setKeyStorePassword(keyStorePassword);
    return this;
  }

  @Override
  public NetServerOptions setTrustStorePath(String trustStorePath) {
    super.setTrustStorePath(trustStorePath);
    return this;
  }

  @Override
  public NetServerOptions setTrustStorePassword(String trustStorePassword) {
    super.setTrustStorePassword(trustStorePassword);
    return this;
  }

}