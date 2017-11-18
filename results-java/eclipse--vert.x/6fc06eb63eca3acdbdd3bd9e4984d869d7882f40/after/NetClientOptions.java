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

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class NetClientOptions extends ClientOptionsBase {

  private int reconnectAttempts;
  private long reconnectInterval = 1000;

  public NetClientOptions() {
  }

  public NetClientOptions(NetClientOptions other) {
    super(other);
    this.reconnectAttempts = other.reconnectAttempts;
    this.reconnectInterval = other.reconnectInterval;
  }

  public NetClientOptions setReconnectAttempts(int attempts) {
    if (attempts < -1) {
      throw new IllegalArgumentException("reconnect attempts must be >= -1");
    }
    this.reconnectAttempts = attempts;
    return this;
  }

  public int getReconnectAttempts() {
    return reconnectAttempts;
  }

  public NetClientOptions setReconnectInterval(long interval) {
    if (interval < 1) {
      throw new IllegalArgumentException("reconnect interval nust be >= 1");
    }
    this.reconnectInterval = interval;
    return this;
  }

  public long getReconnectInterval() {
    return reconnectInterval;
  }

  // Override common implementation

  @Override
  public int getConnectTimeout() {
    return super.getConnectTimeout();
  }

  @Override
  public NetClientOptions setConnectTimeout(int connectTimeout) {
    super.setConnectTimeout(connectTimeout);
    return this;
  }

  public boolean isTrustAll() {
    return super.isTrustAll();
  }

  public NetClientOptions setTrustAll(boolean trustAll) {
    super.setTrustAll(trustAll);
    return this;
  }

  @Override
  public NetClientOptions setSendBufferSize(int sendBufferSize) {
    super.setSendBufferSize(sendBufferSize);
    return this;
  }

  @Override
  public NetClientOptions setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }

  @Override
  public NetClientOptions setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }

  @Override
  public NetClientOptions setTrafficClass(int trafficClass) {
    super.setTrafficClass(trafficClass);
    return this;
  }

  @Override
  public NetClientOptions setTcpNoDelay(boolean tcpNoDelay) {
    super.setTcpNoDelay(tcpNoDelay);
    return this;
  }

  @Override
  public NetClientOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    super.setTcpKeepAlive(tcpKeepAlive);
    return this;
  }

  @Override
  public NetClientOptions setSoLinger(int soLinger) {
    super.setSoLinger(soLinger);
    return this;
  }

  @Override
  public NetClientOptions setUsePooledBuffers(boolean usePooledBuffers) {
    super.setUsePooledBuffers(usePooledBuffers);
    return this;
  }

  @Override
  public NetClientOptions setSsl(boolean ssl) {
    super.setSsl(ssl);
    return this;
  }

  @Override
  public NetClientOptions setKeyStorePath(String keyStorePath) {
    super.setKeyStorePath(keyStorePath);
    return this;
  }

  @Override
  public NetClientOptions setKeyStorePassword(String keyStorePassword) {
    super.setKeyStorePassword(keyStorePassword);
    return this;
  }

  @Override
  public NetClientOptions setTrustStorePath(String trustStorePath) {
    super.setTrustStorePath(trustStorePath);
    return this;
  }

  @Override
  public NetClientOptions setTrustStorePassword(String trustStorePassword) {
    super.setTrustStorePassword(trustStorePassword);
    return this;
  }
}