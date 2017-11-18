/**
 * Copyright 2015-2016 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin.cassandra;

import com.datastax.driver.core.Session;
import java.io.Closeable;
import zipkin.internal.Lazy;

final class LazySession extends Lazy<Session> implements Closeable {
  private final SessionProvider sessionProvider;

  LazySession(CassandraStorage.Builder builder) {
    this.sessionProvider = new SessionProvider(builder);
  }

  @Override protected Session compute() {
    return sessionProvider.get();
  }

  @Override
  public void close() {
    Session maybeNull = maybeGet();
    if (maybeNull != null) maybeNull.close();
  }
}