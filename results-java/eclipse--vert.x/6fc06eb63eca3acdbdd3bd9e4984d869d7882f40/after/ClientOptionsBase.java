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
public class ClientOptionsBase extends OptionsBase {

  // Client specific TCP stuff

  private int connectTimeout = 60000;

  // Client specific SSL stuff

  private boolean trustAll;

  public ClientOptionsBase() {
  }

  public ClientOptionsBase(ClientOptionsBase other) {
    super(other);
    this.connectTimeout = other.connectTimeout;
    this.trustAll = other.trustAll;
  }

  public boolean isTrustAll() {
    return trustAll;
  }

  public ClientOptionsBase setTrustAll(boolean trustAll) {
    this.trustAll = trustAll;
    return this;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public ClientOptionsBase setConnectTimeout(int connectTimeout) {
    if (connectTimeout < 0) {
      throw new IllegalArgumentException("connectTimeout must be >= 0");
    }
    this.connectTimeout = connectTimeout;
    return this;
  }

}