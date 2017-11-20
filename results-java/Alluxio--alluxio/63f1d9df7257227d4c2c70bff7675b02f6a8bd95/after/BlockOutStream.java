/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.client.block.stream;

import alluxio.client.BoundedStream;
import alluxio.client.Cancelable;
import alluxio.client.block.BlockWorkerClient;
import alluxio.client.file.FileSystemContext;
import alluxio.client.file.options.OutStreamOptions;
import alluxio.proto.dataserver.Protocol;
import alluxio.util.CommonUtils;
import alluxio.wire.WorkerNetAddress;

import com.google.common.io.Closer;

import java.io.FilterOutputStream;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Provides a stream API to write a block to Alluxio. An instance of this class can be obtained by
 * calling
 * {@link alluxio.client.block.AlluxioBlockStore#getOutStream(long, long, OutStreamOptions)}.
 */
@NotThreadSafe
public class BlockOutStream extends FilterOutputStream implements BoundedStream, Cancelable {
  private final long mBlockId;
  private final long mBlockSize;
  private final Closer mCloser;
  private final BlockWorkerClient mBlockWorkerClient;
  private final PacketOutStream mOutStream;
  private boolean mClosed;

  /**
   * Creates a new local block output stream.
   *
   * @param blockId the block id
   * @param blockSize the block size
   * @param workerNetAddress the worker network address
   * @param context the file system context
   * @param options the options
   * @return the {@link BlockOutStream} instance created
   */
  public static BlockOutStream createLocalBlockOutStream(long blockId, long blockSize,
      WorkerNetAddress workerNetAddress, FileSystemContext context, OutStreamOptions options)
          throws IOException {
    Closer closer = Closer.create();
    try {
      BlockWorkerClient client = closer.register(context.createBlockWorkerClient(workerNetAddress));
      PacketOutStream outStream = PacketOutStream
          .createLocalPacketOutStream(client, blockId, blockSize, options.getWriteTier());
      closer.register(outStream);
      return new BlockOutStream(outStream, blockId, blockSize, client, options);
    } catch (RuntimeException e) {
      try {
        throw closer.rethrow(e);
      } finally {
        closer.close();
      }
    }
  }

  /**
   * Creates a new remote block output stream.
   *
   * @param blockId the block id
   * @param blockSize the block size
   * @param workerNetAddress the worker network address
   * @param context the file system context
   * @param options the options
   * @return the {@link BlockOutStream} instance created
   */
  public static BlockOutStream createRemoteBlockOutStream(long blockId, long blockSize,
      WorkerNetAddress workerNetAddress, FileSystemContext context, OutStreamOptions options)
          throws IOException {
    Closer closer = Closer.create();
    try {
      BlockWorkerClient client = closer.register(context.createBlockWorkerClient(workerNetAddress));

      PacketOutStream outStream = PacketOutStream
          .createNettyPacketOutStream(context, client.getDataServerAddress(), client.getSessionId(),
              blockId, blockSize, options.getWriteTier(), Protocol.RequestType.ALLUXIO_BLOCK);
      closer.register(outStream);
      return new BlockOutStream(outStream, blockId, blockSize, client, options);
    } catch (RuntimeException e) {
      try {
        throw closer.rethrow(e);
      } finally {
        closer.close();
      }
    }
  }

  // Explicitly overriding some write methods which are not efficiently implemented in
  // FilterOutStream.

  @Override
  public void write(byte[] b) throws IOException {
    mOutStream.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    mOutStream.write(b, off, len);
  }

  @Override
  public long remaining() {
    return mOutStream.remaining();
  }

  @Override
  public void cancel() throws IOException {
    if (mClosed) {
      return;
    }
    IOException exception = null;
    try {
      mOutStream.cancel();
    } catch (IOException e) {
      exception = e;
    }
    try {
      mBlockWorkerClient.cancelBlock(mBlockId);
    } catch (IOException e) {
      exception = e;
    }

    if (exception == null) {
      mClosed = true;
      return;
    }

    CommonUtils.closeQuietly(mCloser);
    mClosed = true;
    throw exception;
  }

  @Override
  public void close() throws IOException {
    if (mClosed) {
      return;
    }
    try {
      mOutStream.close();
      if (remaining() < mBlockSize) {
        mBlockWorkerClient.cacheBlock(mBlockId);
      }
    } catch (Throwable t) {
      mCloser.rethrow(t);
    } finally {
      mCloser.close();
      mClosed = true;
    }
  }

  /**
   * Creates a new block output stream.
   *
   * @param outStream the {@link PacketOutStream} associated with this {@link BlockOutStream}
   * @param blockId the block id
   * @param blockSize the block size
   * @param blockWorkerClient the block worker client
   * @param options the options
   */
  protected BlockOutStream(PacketOutStream outStream, long blockId, long blockSize,
      BlockWorkerClient blockWorkerClient, OutStreamOptions options) {
    super(outStream);

    mOutStream = outStream;
    mBlockId = blockId;
    mBlockSize = blockSize;
    mCloser = Closer.create();
    mBlockWorkerClient = mCloser.register(blockWorkerClient);
    mClosed = false;
  }
}