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

public final class Constants {
  /**
   * The client sent ("cs") a request to a server. There is only one send per span. For example, if
   * there's a transport error, each attempt can be logged as a {@link #WIRE_SEND} annotation.
   *
   * <p/>If chunking is involved, each chunk could be logged as a separate {@link
   * #CLIENT_SEND_FRAGMENT} in the same span.
   *
   * <p/>{@link Annotation#endpoint} is not the server. It is the host which logged the send event,
   * almost always the client. When logging CLIENT_SEND, instrumentation should also log the {@link
   * #SERVER_ADDR}.
   */
  public static final String CLIENT_SEND = "cs";

  /**
   * The client received ("cr") a response from a server. There is only one receive per span. For
   * example, if duplicate responses were received, each can be logged as a {@link #WIRE_RECV}
   * annotation.
   *
   * <p/>If chunking is involved, each chunk could be logged as a separate {@link
   * #CLIENT_RECV_FRAGMENT} in the same span.
   *
   * <p/>{@link Annotation#endpoint} is not the server. It is the host which logged the receive
   * event, almost always the client. The actual endpoint of the server is recorded separately as
   * {@link #SERVER_ADDR} when {@link #CLIENT_SEND} is logged.
   */
  public static final String CLIENT_RECV = "cr";

  /**
   * The server sent ("ss") a response to a client. There is only one response per span. If there's
   * a transport error, each attempt can be logged as a {@link #WIRE_SEND} annotation.
   *
   * <p/>Typically, a trace ends with a server send, so the last timestamp of a trace is often the
   * timestamp of the root span's server send.
   *
   * <p/>If chunking is involved, each chunk could be logged as a separate {@link
   * #SERVER_SEND_FRAGMENT} in the same span.
   *
   * <p/>{@link Annotation#endpoint} is not the client. It is the host which logged the send event,
   * almost always the server. The actual endpoint of the client is recorded separately as {@link
   * #CLIENT_ADDR} when {@link #SERVER_RECV} is logged.
   */
  public static final String SERVER_SEND = "ss";

  /**
   * The server received ("sr") a request from a client. There is only one request per span.  For
   * example, if duplicate responses were received, each can be logged as a {@link #WIRE_RECV}
   * annotation.
   *
   * <p/>Typically, a trace starts with a server receive, so the first timestamp of a trace is often
   * the timestamp of the root span's server receive.
   *
   * <p/>If chunking is involved, each chunk could be logged as a separate {@link
   * #SERVER_RECV_FRAGMENT} in the same span.
   *
   * <p/>{@link Annotation#endpoint} is not the client. It is the host which logged the receive
   * event, almost always the server. When logging SERVER_RECV, instrumentation should also log the
   * {@link #CLIENT_ADDR}.
   */
  public static final String SERVER_RECV = "sr";

  /**
   * Optionally logs an attempt to send a message on the wire. Multiple wire send events could
   * indicate network retries. A lag between client or server send and wire send might indicate
   * queuing or processing delay.
   */
  public static final String WIRE_SEND = "ws";

  /**
   * Optionally logs an attempt to receive a message from the wire. Multiple wire receive events
   * could indicate network retries. A lag between wire receive and client or server receive might
   * indicate queuing or processing delay.
   */
  public static final String WIRE_RECV = "wr";

  /**
   * Optionally logs progress of a ({@linkplain #CLIENT_SEND}, {@linkplain #WIRE_SEND}). For
   * example, this could be one chunk in a chunked request.
   */
  public static final String CLIENT_SEND_FRAGMENT = "csf";

  /**
   * Optionally logs progress of a ({@linkplain #CLIENT_RECV}, {@linkplain #WIRE_RECV}). For
   * example, this could be one chunk in a chunked response.
   */
  public static final String CLIENT_RECV_FRAGMENT = "crf";

  /**
   * Optionally logs progress of a ({@linkplain #SERVER_SEND}, {@linkplain #WIRE_SEND}). For
   * example, this could be one chunk in a chunked response.
   */
  public static final String SERVER_SEND_FRAGMENT = "ssf";

  /**
   * Optionally logs progress of a ({@linkplain #SERVER_RECV}, {@linkplain #WIRE_RECV}). For
   * example, this could be one chunk in a chunked request.
   */
  public static final String SERVER_RECV_FRAGMENT = "srf";

  /**
   * The {@link BinaryAnnotation#value value} of "lc" is the component or namespace of a local
   * span.
   *
   * <p/>{@link BinaryAnnotation#endpoint} adds service context needed to support queries.
   *
   * <p/>Local Component("lc") supports three key features: flagging, query by service and filtering
   * Span.name by namespace.
   *
   * <p/>While structurally the same, local spans are fundamentally different than RPC spans in how
   * they should be interpreted. For example, zipkin v1 tools center on RPC latency and service
   * graphs. Root local-spans are neither indicative of critical path RPC latency, nor have impact
   * on the shape of a service graph. By flagging with "lc", tools can special-case local spans.
   *
   * <p/>Zipkin v1 Spans are unqueryable unless they can be indexed by service name. The only path
   * to a {@link Endpoint#serviceName service name} is via {@link BinaryAnnotation#endpoint
   * host}. By logging "lc", a local span can be queried even if no other annotations are logged.
   *
   * <p/>The value of "lc" is the namespace of {@link Span#name}. For example, it might be
   * "finatra2", for a span named "bootstrap". "lc" allows you to resolves conflicts for the same
   * Span.name, for example "finatra/bootstrap" vs "finch/bootstrap". Using local component, you'd
   * search for spans named "bootstrap" where "lc=finch"
   */
  public static final String LOCAL_COMPONENT = "lc";

  /**
   * When present, {@link BinaryAnnotation#endpoint} indicates a client address ("ca") in a span.
   * Most likely, there's only one. Multiple addresses are possible when a client changes its ip or
   * port within a span.
   */
  public static final String CLIENT_ADDR = "ca";

  /**
   * When present, {@link BinaryAnnotation#endpoint} indicates a server address ("sa") in a span.
   * Most likely, there's only one. Multiple addresses are possible when a client is redirected, or
   * fails to a different server ip or port.
   */
  public static final String SERVER_ADDR = "sa";

  private Constants() {
  }
}