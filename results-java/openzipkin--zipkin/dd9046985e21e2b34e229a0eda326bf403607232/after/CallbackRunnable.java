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
package zipkin.async;

import static zipkin.internal.Util.checkNotNull;

abstract class CallbackRunnable<V> implements Runnable {
  final Callback<V> callback;

  protected CallbackRunnable(Callback<V> callback) {
    this.callback = checkNotNull(callback, "callback");
  }

  abstract V complete();

  @Override public void run() {
    try {
      callback.onSuccess(complete());
    } catch (RuntimeException | Error e) {
      callback.onError(e);
      if (e instanceof Error) throw e;
    }
  }
}