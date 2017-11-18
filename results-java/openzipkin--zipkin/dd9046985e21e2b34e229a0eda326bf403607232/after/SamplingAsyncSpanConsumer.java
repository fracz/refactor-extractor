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

import java.util.ArrayList;
import java.util.List;
import zipkin.Sampler;
import zipkin.Span;

import static zipkin.internal.Util.checkNotNull;

/** Writes spans to storage, subject to sampling policy. */
public final class SamplingAsyncSpanConsumer implements AsyncSpanConsumer {

  public static AsyncSpanConsumer create(Sampler sampler, AsyncSpanConsumer asyncConsumer) {
    return new SamplingAsyncSpanConsumer(sampler, asyncConsumer);
  }

  final Sampler sampler;
  final AsyncSpanConsumer asyncConsumer;

  SamplingAsyncSpanConsumer(Sampler sampler, AsyncSpanConsumer asyncConsumer) {
    this.sampler = checkNotNull(sampler, "sampler");
    this.asyncConsumer = checkNotNull(asyncConsumer, "asyncConsumer");
  }

  @Override
  public void accept(List<Span> input, Callback<Void> callback) {
    List<Span> sampled = new ArrayList<>(input.size());
    for (Span s : input) {
      if ((s.debug != null && s.debug) || sampler.isSampled(s.traceId)) sampled.add(s);
    }
    asyncConsumer.accept(sampled, callback);
  }
}