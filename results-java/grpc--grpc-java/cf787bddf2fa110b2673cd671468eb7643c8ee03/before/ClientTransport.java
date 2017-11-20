/*
 * Copyright 2014, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.grpc.internal;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The client-side transport encapsulating a single connection to a remote server. Allows creation
 * of new {@link Stream} instances for communication with the server. All methods on the transport
 * and its listener are expected to execute quickly.
 *
 * <p>{@link #start} must be the first method call to this interface and return before calling other
 * methods.
 */
@ThreadSafe
public interface ClientTransport {

  /**
   * Creates a new stream for sending messages to the remote end-point.
   *
   * <p>
   * This method returns immediately and does not wait for any validation of the request. If
   * creation fails for any reason, {@link ClientStreamListener#closed} will be called to provide
   * the error information. Any sent messages for this stream will be buffered until creation has
   * completed (either successfully or unsuccessfully).
   *
   * @param method the descriptor of the remote method to be called for this stream.
   * @param headers to send at the beginning of the call
   * @return the newly created stream.
   */
  // TODO(nmittler): Consider also throwing for stopping.
  ClientStream newStream(MethodDescriptor<?, ?> method, Metadata headers);

  /**
   * Starts transport. This method may only be called once.
   *
   * <p>Implementations must not call {@code listener} from within {@link #start}; implementations
   * are expected to notify listener on a separate thread.  This method should not throw any
   * exceptions.
   *
   * @param listener non-{@code null} listener of transport events
   */
  void start(Listener listener);

  /**
   * Pings the remote endpoint to verify that the transport is still active. When an acknowledgement
   * is received, the given callback will be invoked using the given executor.
   *
   * <p>This is an optional method. Transports that do not have any mechanism by which to ping the
   * remote endpoint may throw {@link UnsupportedOperationException}.
   */
  void ping(PingCallback callback, Executor executor);

  /**
   * Initiates an orderly shutdown of the transport. Existing streams continue, but new streams will
   * fail (once {@link Listener#transportShutdown} callback called). This method may only be called
   * once.
   */
  void shutdown();

  /**
   * Receives notifications for the transport life-cycle events. Implementation does not need to be
   * thread-safe, so notifications must be properly sychronized externally.
   */
  interface Listener {
    /**
     * The transport is shutting down. No new streams will be processed, but existing streams may
     * continue. Shutdown could have been caused by an error or normal operation.  It is possible
     * that this method is called without {@link #shutdown} being called.  If the argument to this
     * function is {@link Status#isOk}, it is safe to immediately reconnect.
     *
     * <p>This is called exactly once, and must be called prior to {@link #transportTerminated}.
     *
     * @param s the reason for the shutdown.
     */
    void transportShutdown(Status s);

    /**
     * The transport completed shutting down. All resources have been released.
     *
     * <p>This is called exactly once, and must be called after {@link #transportShutdown} has been
     * called.
     */
    void transportTerminated();

    /**
     * The transport is ready to accept traffic, because the connection is established.  This is
     * called at most once.
     */
    void transportReady();
  }

  /**
   * A callback that is invoked when the acknowledgement to a {@link #ping} is received. Exactly one
   * of the two methods should be called per {@link #ping}.
   */
  interface PingCallback {

    /**
     * Invoked when a ping is acknowledged. The given argument is the round-trip time of the ping,
     * in nanoseconds.
     *
     * @param roundTripTimeNanos the round-trip duration between the ping being sent and the
     *     acknowledgement received
     */
    void onSuccess(long roundTripTimeNanos);

    /**
     * Invoked when a ping fails. The given argument is the cause of the failure.
     *
     * @param cause the cause of the ping failure
     */
    void onFailure(Throwable cause);
  }
}