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

package com.google.net.stubby.transport.okhttp;

import com.google.common.base.Preconditions;
import com.google.net.stubby.Metadata;
import com.google.net.stubby.Status;
import com.google.net.stubby.transport.ClientStreamListener;
import com.google.net.stubby.transport.Http2ClientStream;

import com.squareup.okhttp.internal.spdy.ErrorCode;
import com.squareup.okhttp.internal.spdy.Header;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.GuardedBy;

/**
 * Client stream for the okhttp transport.
 */
class OkHttpClientStream extends Http2ClientStream {

  private static int WINDOW_UPDATE_THRESHOLD =
      OkHttpClientTransport.DEFAULT_INITIAL_WINDOW_SIZE / 2;

  /**
   * Construct a new client stream.
   */
  static OkHttpClientStream newStream(final Executor executor, ClientStreamListener listener,
                                       AsyncFrameWriter frameWriter,
                                       OkHttpClientTransport transport,
                                       OutboundFlowController outboundFlow) {
    // Create a lock object that can be used by both the executor and methods in the stream
    // to ensure consistent locking behavior.
    final Object executorLock = new Object();
    Executor synchronizingExecutor = new Executor() {
      @Override
      public void execute(final Runnable command) {
        executor.execute(new Runnable() {
          @Override
          public void run() {
            synchronized (executorLock) {
              command.run();
            }
          }
        });
      }
    };
    return new OkHttpClientStream(synchronizingExecutor, listener, frameWriter, transport,
        executorLock, outboundFlow);
  }

  @GuardedBy("executorLock")
  private int window = OkHttpClientTransport.DEFAULT_INITIAL_WINDOW_SIZE;
  @GuardedBy("executorLock")
  private int processedWindow = OkHttpClientTransport.DEFAULT_INITIAL_WINDOW_SIZE;
  private final AsyncFrameWriter frameWriter;
  private final OutboundFlowController outboundFlow;
  private final OkHttpClientTransport transport;
  // Lock used to synchronize with work done on the executor.
  private final Object executorLock;
  private Object outboundFlowState;

  private OkHttpClientStream(final Executor executor,
                     final ClientStreamListener listener,
                     AsyncFrameWriter frameWriter,
                     OkHttpClientTransport transport,
                     Object executorLock,
                     OutboundFlowController outboundFlow) {
    super(listener, executor);
    this.frameWriter = frameWriter;
    this.transport = transport;
    this.executorLock = executorLock;
    this.outboundFlow = outboundFlow;
  }

  public void transportHeadersReceived(List<Header> headers, boolean endOfStream) {
    synchronized (executorLock) {
      if (endOfStream) {
        transportTrailersReceived(Utils.convertTrailers(headers));
      } else {
        transportHeadersReceived(Utils.convertHeaders(headers));
      }
    }
  }

  /**
   * We synchronized on "executorLock" for delivering frames and updating window size, so that
   * the future listeners (executed by synchronizedExecutor) will not be executed in the same time.
   */
  public void transportDataReceived(okio.Buffer frame, boolean endOfStream) {
    synchronized (executorLock) {
      long length = frame.size();
      window -= length;
      super.transportDataReceived(new OkHttpBuffer(frame), endOfStream);
    }
  }

  @Override
  protected void sendFrame(ByteBuffer frame, boolean endOfStream) {
    Preconditions.checkState(id() != 0, "streamId should be set");
    okio.Buffer buffer = new okio.Buffer();
    // Read the data into a buffer.
    // TODO(user): swap to NIO buffers or zero-copy if/when okhttp/okio supports it
    buffer.write(frame.array(), frame.arrayOffset(), frame.remaining());
    // Write the data to the remote endpoint.
    // Per http2 SPEC, the max data length should be larger than 64K, while our frame size is
    // only 4K.
    Preconditions.checkState(buffer.size() < frameWriter.maxDataLength());
    outboundFlow.data(endOfStream, id(), buffer);
  }

  @Override
  protected void returnProcessedBytes(int processedBytes) {
    synchronized (executorLock) {
      processedWindow -= processedBytes;
      if (processedWindow <= WINDOW_UPDATE_THRESHOLD) {
        int delta = OkHttpClientTransport.DEFAULT_INITIAL_WINDOW_SIZE - processedWindow;
        window += delta;
        processedWindow += delta;
        frameWriter.windowUpdate(id(), delta);
      }
    }
  }

  @Override
  public boolean transportReportStatus(Status newStatus, Metadata.Trailers trailers) {
    synchronized (executorLock) {
      return super.transportReportStatus(newStatus, trailers);
    }
  }

  @Override
  protected void sendCancel() {
    if (transport.finishStream(id(), Status.CANCELLED)) {
      frameWriter.rstStream(id(), ErrorCode.CANCEL);
      transport.stopIfNecessary();
    }
  }

  @Override
  public void remoteEndClosed() {
    super.remoteEndClosed();
    if (transport.finishStream(id(), null)) {
      transport.stopIfNecessary();
    }
  }

  void setOutboundFlowState(Object outboundFlowState) {
    this.outboundFlowState = outboundFlowState;
  }

  Object getOutboundFlowState() {
    return outboundFlowState;
  }
}