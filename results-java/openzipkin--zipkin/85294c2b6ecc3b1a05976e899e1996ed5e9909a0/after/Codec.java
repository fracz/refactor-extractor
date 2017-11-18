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
package zipkin;

import java.util.List;
import zipkin.internal.JsonCodec;
import zipkin.internal.Nullable;
import zipkin.internal.ThriftCodec;

/**
 * Methods make an attempt to perform codec operations, failing to null.
 */
public interface Codec {

  interface Factory {
    /** Returns a codec for the given media type (ex. "application/json") or null if not found. */
    @Nullable
    Codec get(String mediaType);
  }

  Codec JSON = new JsonCodec();
  Codec THRIFT = new ThriftCodec();

  Factory FACTORY = new Factory() {

    @Override
    public Codec get(String mediaType) {
      if (mediaType.startsWith("application/json")) {
        return JSON;
      } else if (mediaType.startsWith("application/x-thrift")) {
        return THRIFT;
      }
      return null;
    }
  };

  /** throws {@linkplain IllegalArgumentException} if the span couldn't be decoded */
  Span readSpan(byte[] bytes);

  byte[] writeSpan(Span value);

  /** throws {@linkplain IllegalArgumentException} if the spans couldn't be decoded */
  List<Span> readSpans(byte[] bytes);

  byte[] writeSpans(List<Span> value);

  byte[] writeTraces(List<List<Span>> value);

  /** throws {@linkplain IllegalArgumentException} if the dependency links couldn't be decoded */
  List<DependencyLink> readDependencyLinks(byte[] bytes);

  byte[] writeDependencyLinks(List<DependencyLink> value);
}