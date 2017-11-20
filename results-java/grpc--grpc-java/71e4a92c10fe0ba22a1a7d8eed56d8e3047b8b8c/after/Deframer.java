package com.google.net.stubby.transport;

import com.google.common.io.ByteStreams;
import com.google.net.stubby.GrpcFramingUtil;
import com.google.net.stubby.Operation;
import com.google.net.stubby.Status;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base implementation that joins a sequence of framed GRPC data produced by a {@link Framer},
 * reconstructs their messages and hands them off to a receiving {@link Operation}
 */
// TODO(user): Either make this an interface of convert Framer -> AbstractFramer for consistency
public abstract class Deframer<F> {

  /**
   *  Unset frame length
   */
  private static final int LENGTH_NOT_SET = -1;

  private boolean inFrame;
  private byte currentFlags;
  private int currentLength = LENGTH_NOT_SET;

  public Deframer() {}

  /**
   * Consume a frame of bytes provided by the transport. Note that transport framing is not
   * aligned on GRPC frame boundaries so this code needs to do bounds checking and buffering
   * across transport  frame boundaries.
   *
   * @return the number of unconsumed bytes remaining in the buffer
   */
  public int deframe(F frame, Operation target) {
    try {
      frame = decompress(frame);
      DataInputStream grpcStream = prefix(frame);
      // Loop until no more GRPC frames can be fully decoded
      while (true) {
        if (!inFrame) {
          // Not in frame so attempt to read flags
          if (!ensure(grpcStream, GrpcFramingUtil.FRAME_TYPE_LENGTH)) {
            return consolidate();
          }
          currentFlags = grpcStream.readByte();
          inFrame = true;
        }
        if (currentLength == LENGTH_NOT_SET) {
          // Read the frame length
          if (!ensure(grpcStream, GrpcFramingUtil.FRAME_LENGTH)) {
            return consolidate();
          }
          currentLength = grpcStream.readInt();
        }
        // Ensure that the entire frame length is available to read
        InputStream framedChunk = ensureMessage(grpcStream, currentLength);
        if (framedChunk == null) {
          // Insufficient bytes available
          return consolidate();
        }
        if (GrpcFramingUtil.isPayloadFrame(currentFlags)) {
          // Advance stream now, because target.addPayload() may not or may process the frame on
          // another thread.
          framedChunk = new ByteArrayInputStream(ByteStreams.toByteArray(framedChunk));
          try {
            // Report payload to the receiving operation
            target.addPayload(framedChunk, Operation.Phase.PAYLOAD);
          } finally {
            currentLength = LENGTH_NOT_SET;
            inFrame = false;
          }
        } else if (GrpcFramingUtil.isStatusFrame(currentFlags)) {
          int code = framedChunk.read() << 8 | framedChunk.read();
          // TODO(user): Resolve what to do with remainder of framedChunk
          try {
            target.close(Status.fromCodeValue(code));
          } finally {
            currentLength = LENGTH_NOT_SET;
            inFrame = false;
          }
        }
        if (grpcStream.available() == 0) {
          // We've processed all the data so consolidate the underlying buffers
          return consolidate();
        }
      }
    } catch (IOException ioe) {
      Status status = Status.UNKNOWN.withCause(ioe);
      target.close(status);
      throw status.asRuntimeException();
    }
  }

  /**
   * Return a stream view over the current buffer prefixed to the input frame
   */
  protected abstract DataInputStream prefix(F frame) throws IOException;

  /**
   * Consolidate the underlying buffers and return the number of buffered bytes remaining
   */
  protected abstract int consolidate() throws IOException;

  /**
   * Decompress the raw frame buffer prior to prefixing it.
   */
  protected abstract F decompress(F frame) throws IOException;

  /**
   * Ensure that {@code len} bytes are available in the buffer and frame
   */
  private boolean ensure(InputStream input, int len) throws IOException {
    return (input.available() >= len);
  }

  /**
   * Return a message of {@code len} bytes than can be read from the buffer. If sufficient
   * bytes are unavailable then buffer the available bytes and return null.
   */
  private InputStream ensureMessage(InputStream input, int len)
      throws IOException {
    if (input.available() < len) {
      return null;
    }
    return ByteStreams.limit(input, len);
  }
}