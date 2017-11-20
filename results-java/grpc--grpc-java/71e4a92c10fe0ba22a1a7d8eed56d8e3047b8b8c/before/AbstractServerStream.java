package com.google.net.stubby.newtransport;

import static com.google.net.stubby.newtransport.StreamState.CLOSED;
import static com.google.net.stubby.newtransport.StreamState.OPEN;
import static com.google.net.stubby.newtransport.StreamState.WRITE_ONLY;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.net.stubby.Metadata;
import com.google.net.stubby.Status;
import com.google.net.stubby.transport.Transport;

import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.annotation.concurrent.GuardedBy;

/**
 * Abstract base class for {@link ServerStream} implementations.
 */
public abstract class AbstractServerStream extends AbstractStream implements ServerStream {

  private ServerStreamListener listener;

  private final Object stateLock = new Object();
  private volatile StreamState state = StreamState.OPEN;
  /** Whether listener.closed() has been called. */
  @GuardedBy("stateLock")
  private boolean listenerClosed;
  /** Whether the stream was closed gracefull by the application (vs. a transport-level failure). */
  private boolean gracefulClose;
  /** Saved trailers from close() that need to be sent once the framer has sent all messages. */
  private Metadata.Trailers stashedTrailers;

  public final void setListener(ServerStreamListener listener) {
    this.listener = Preconditions.checkNotNull(listener, "listener");
  }

  @Override
  protected ListenableFuture<Void> receiveMessage(InputStream is, int length) {
    inboundPhase(Phase.MESSAGE);
    return listener.messageRead(is, length);
  }

  /** gRPC protocol v1 support */
  @Override
  protected void receiveStatus(Status status) {
    Preconditions.checkState(status == Status.OK, "Received status can only be OK on server");
  }

  @Override
  public final void close(Status status, Metadata.Trailers trailers) {
    Preconditions.checkNotNull(status, "status");
    Preconditions.checkNotNull(trailers, "trailers");
    outboundPhase(Phase.STATUS);
    synchronized (stateLock) {
      Preconditions.checkState(!status.isOk() || state == WRITE_ONLY,
          "Cannot close with OK before client half-closes");
      state = CLOSED;
    }
    gracefulClose = true;
    trailers.removeAll(Status.CODE_KEY);
    trailers.removeAll(Status.MESSAGE_KEY);
    trailers.put(Status.CODE_KEY, status.getCode());
    if (status.getDescription() != null) {
      trailers.put(Status.MESSAGE_KEY, status.getDescription());
    }
    this.stashedTrailers = trailers;
    closeFramer(status);
    dispose();
  }

  @Override
  protected final void internalSendFrame(ByteBuffer frame, boolean endOfStream) {
    if (!GRPC_V2_PROTOCOL) {
      sendFrame(frame, endOfStream);
    } else {
      sendFrame(frame, false);
      if (endOfStream) {
        sendTrailers(stashedTrailers);
        stashedTrailers = null;
      }
    }
  }

  /**
   * Sends an outbound frame to the remote end point.
   *
   * @param frame a buffer containing the chunk of data to be sent.
   * @param endOfStream if {@code true} indicates that no more data will be sent on the stream by
   *        this endpoint.
   */
  protected abstract void sendFrame(ByteBuffer frame, boolean endOfStream);

  /**
   * Sends trailers to the remote end point. This call implies end of stream.
   *
   * @param trailers metadata to be sent to end point
   */
  protected abstract void sendTrailers(Metadata.Trailers trailers);

  /**
   * The Stream is considered completely closed and there is no further opportunity for error. It
   * calls the listener's {@code closed()} if it was not already done by {@link #abortStream}. Note
   * that it is expected that either {@code closed()} or {@code abortStream()} was previously
   * called, since {@code closed()} is required for a normal stream closure and {@code
   * abortStream()} for abnormal.
   */
  public void complete() {
    synchronized (stateLock) {
      if (listenerClosed) {
        return;
      }
      listenerClosed = true;
    }
    if (!gracefulClose) {
      listener.closed(new Status(Transport.Code.INTERNAL, "successful complete() without close()"));
      throw new IllegalStateException("successful complete() without close()");
    }
    listener.closed(Status.OK);
  }

  @Override
  public StreamState state() {
    return state;
  }

  /**
   * Called when the remote end half-closes the stream.
   */
  @Override
  protected final void remoteEndClosed() {
    synchronized (stateLock) {
      Preconditions.checkState(state == OPEN, "Stream not OPEN");
      state = WRITE_ONLY;
    }
    inboundPhase(Phase.STATUS);
    listener.halfClosed();
  }

  /**
   * Aborts the stream with an error status, cleans up resources and notifies the listener if
   * necessary.
   *
   * <p>Unlike {@link #close(Status, Metadata.Trailers)}, this method is only called from the
   * transport. The transport should use this method instead of {@code close(Status)} for internal
   * errors to prevent exposing unexpected states and exceptions to the application.
   *
   * @param status the error status. Must not be Status.OK.
   * @param notifyClient true if the stream is still writable and you want to notify the client
   *                     about stream closure and send the status
   */
  public final void abortStream(Status status, boolean notifyClient) {
    Preconditions.checkArgument(!status.isOk(), "status must not be OK");
    boolean closeListener;
    synchronized (stateLock) {
      if (state == CLOSED) {
        // Can't actually notify client.
        notifyClient = false;
      }
      state = CLOSED;
      closeListener = !listenerClosed;
      listenerClosed = true;
    }

    try {
      if (notifyClient) {
        closeFramer(status);
      }
      dispose();
    } finally {
      if (closeListener) {
        listener.closed(status);
      }
    }
  }
}