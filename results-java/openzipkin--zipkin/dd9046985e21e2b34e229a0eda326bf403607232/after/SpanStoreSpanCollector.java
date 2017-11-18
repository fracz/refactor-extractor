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
package zipkin.server.brave;

import com.github.kristofa.brave.SpanCollector;
import com.twitter.zipkin.gen.AnnotationType;
import java.io.Flushable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import zipkin.Annotation;
import zipkin.BinaryAnnotation;
import zipkin.BinaryAnnotation.Type;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.SpanStore;
import zipkin.async.AsyncSpanConsumer;

/**
 * A Brave {@link SpanCollector} that forwards to the local {@link SpanStore}.
 */
public class SpanStoreSpanCollector implements SpanCollector, Flushable {
  private final AsyncSpanConsumer consumer;
  // TODO: should we put a bound on this queue?
  // Since this is only used for internal tracing in zipkin, maybe it's ok
  private final BlockingQueue<Span> queue = new LinkedBlockingQueue<>();
  private final int limit = 200;

  public SpanStoreSpanCollector(AsyncSpanConsumer consumer) {
    this.consumer = consumer;
  }

  public void collect(Span span) {
    if (queue.offer(span) && queue.size() >= limit) {
      flush();
    }
  }

  @Override
  public void collect(com.twitter.zipkin.gen.Span span) {
    collect(convert(span));
  }

  @Override
  public void flush() {
    List<Span> spans = new ArrayList<>(queue.size());
    while (!queue.isEmpty()) {
      Span span = queue.poll();
      if (span != null) {
        spans.add(span);
      }
    }
    if (!spans.isEmpty()) {
      consumer.accept(spans, AsyncSpanConsumer.NOOP_CALLBACK);
    }
  }

  private Span convert(com.twitter.zipkin.gen.Span span) {
    Span.Builder builder = new Span.Builder();
    builder.name(span.getName())
        .id(span.getId())
        .parentId(span.getParent_id() != null ? span.getParent_id() : null)
        .traceId(span.getTrace_id())
        .timestamp(span.getTimestamp())
        .duration(span.getDuration())
        .debug(span.isDebug());
    List<com.twitter.zipkin.gen.Annotation> annotations = span.getAnnotations();
    if (annotations != null) {
      for (com.twitter.zipkin.gen.Annotation annotation : annotations) {
        builder.addAnnotation(convert(annotation));
      }
    }
    List<com.twitter.zipkin.gen.BinaryAnnotation> binaries = span.getBinary_annotations();
    if (binaries != null) {
      for (com.twitter.zipkin.gen.BinaryAnnotation annotation : binaries) {
        builder.addBinaryAnnotation(convert(annotation));
      }
    }
    return builder.build();
  }

  @Override
  public void addDefaultAnnotation(String key, String value) {
  }

  static Annotation convert(com.twitter.zipkin.gen.Annotation annotation) {
    return new Annotation.Builder()
        .timestamp(annotation.timestamp)
        .value(annotation.value)
        .endpoint(convert(annotation.host))
        .build();
  }

  static Endpoint convert(com.twitter.zipkin.gen.Endpoint endpoint) {
    return new Endpoint.Builder()
        .serviceName(endpoint.service_name)
        .port(endpoint.port)
        .ipv4(endpoint.ipv4).build();
  }

  static BinaryAnnotation convert(com.twitter.zipkin.gen.BinaryAnnotation annotation) {
    return new BinaryAnnotation.Builder()
        .key(annotation.key)
        .value(annotation.getValue())
        .type(convert(annotation.type))
        .endpoint(convert(annotation.host))
        .build();
  }

  static Type convert(AnnotationType type) {
    switch (type) {
      case STRING:
        return Type.STRING;
      case DOUBLE:
        return Type.DOUBLE;
      case BOOL:
        return Type.BOOL;
      case I16:
        return Type.I16;
      case I32:
        return Type.I32;
      case I64:
        return Type.I64;
      default:
        return Type.BYTES;
    }
  }
}