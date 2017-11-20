/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import com.github.benmanes.caffeine.base.UnsafeAccess;

/**
 * A concurrent benchmark for read buffer implementation options. A read buffer may be a lossy,
 * bounded, non-blocking, multiple-producer / single-consumer unordered queue. These buffers are
 * used to record a memento of the read operation that occured on the cache, which is later replayed
 * by a single thread on the eviction policy to predict the optimal victim when an entry must be
 * removed.
 * <p>
 * The implementations here are per-segment buffers. In practice the unordered characteristic allows
 * multiple buffers to be used, chosen by a strategy such as hashing on the thread id. A primary
 * goal of the buffer is to maximize read throughput of the cache.
 * <p>
 * The buffer should not create garbage to manage its internal state, such as link nodes. This
 * optimization avoids additional garbage collection pauses that reduces overall throughput.
 * <p>
 * The buffer should try to maintain some bounding constraint to avoid memory pressure under
 * synthetic workloads.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@State(Scope.Benchmark)
public class ReadBufferBenchmark {
  static final int READ_BUFFER_SIZE = 128;
  static final int READ_BUFFER_INDEX_MASK = READ_BUFFER_SIZE - 1;

  @Param BufferType bufferType;
  Buffer buffer;

  @Setup
  public void setup() {
    buffer = bufferType.create();
  }

  @Benchmark @Group @GroupThreads(8)
  public void record() {
    buffer.record();
  }

  @Benchmark @Group @GroupThreads(1)
  public void drain() {
    buffer.drain();
  }

  interface Buffer {
    void record();
    void drain();
  }

  public enum BufferType {
    LOSSY { @Override public Buffer create() { return new LossyBuffer(); } },
    ONE_SHOT { @Override public Buffer create() { return new OneShotBuffer(); } },
    CLQ { @Override public Buffer create() { return new QueueBuffer(); } };

    public abstract Buffer create();
  }

  /** A bounded buffer that uses lossy writes and may race with the reader. */
  static final class LossyBuffer implements Buffer {
    final RelaxedAtomic<Boolean>[] buffer;
    final AtomicInteger writeCounter;
    final AtomicInteger readCounter;

    @SuppressWarnings("unchecked")
    LossyBuffer() {
      readCounter = new AtomicInteger();
      writeCounter = new AtomicInteger();
      buffer = new RelaxedAtomic[READ_BUFFER_SIZE];
      for (int i = 0; i < READ_BUFFER_SIZE; i++) {
        buffer[i] = new RelaxedAtomic<Boolean>();
      }
    }

    @Override
    public void record() {
      final int writeCount = writeCounter.get();
      writeCounter.lazySet(writeCount + 1);
      buffer[writeCount & READ_BUFFER_INDEX_MASK].lazySet(Boolean.TRUE);
    }

    @Override
    public void drain() {
      int readCount = readCounter.get();
      for (int i = 0; i < READ_BUFFER_SIZE; i++) {
        int index = readCount & READ_BUFFER_INDEX_MASK;
        Boolean value = buffer[index].getRelaxed();
        if (value == null) {
          break;
        }
        buffer[index].lazySet(null);
        readCount++;
      }
      readCounter.set(readCount);
    }
  }

  /** A bounded buffer that attempts to record once. */
  static final class OneShotBuffer implements Buffer {
    final RelaxedAtomic<Boolean>[] buffer;
    final AtomicInteger writeCounter;
    final AtomicInteger readCounter;

    @SuppressWarnings("unchecked")
    OneShotBuffer() {
      readCounter = new AtomicInteger();
      writeCounter = new AtomicInteger();
      buffer = new RelaxedAtomic[READ_BUFFER_SIZE];
      for (int i = 0; i < READ_BUFFER_SIZE; i++) {
        buffer[i] = new RelaxedAtomic<Boolean>();
      }
    }

    @Override
    public void record() {
      final int writeCount = writeCounter.get();
      final int index = writeCount & READ_BUFFER_INDEX_MASK;
      if ((buffer[index].getRelaxed() == null) && buffer[index].compareAndSet(null, Boolean.TRUE)) {
        writeCounter.lazySet(writeCount + 1);
      }
    }

    @Override
    public void drain() {
      int readCount = readCounter.get();
      for (int i = 0; i < READ_BUFFER_SIZE; i++) {
        int index = readCount & READ_BUFFER_INDEX_MASK;
        Boolean value = buffer[index].getRelaxed();
        if (value == null) {
          break;
        }
        buffer[index].lazySet(null);
        readCount++;
      }
      readCounter.lazySet(readCount);
    }
  }

  static final class QueueBuffer implements Buffer {
    final Queue<Boolean> buffer = new ConcurrentLinkedQueue<>();

    @Override
    public void record() {
      buffer.add(Boolean.TRUE);
    }

    @Override
    public void drain() {
      while (buffer.poll() != null) {}
    }
  }

  /** An {@link AtomicReference} like holder that supports relaxed reads. */
  static final class RelaxedAtomic<E> {
    static final long VALUE_OFFSET = UnsafeAccess.objectFieldOffset(RelaxedAtomic.class, "value");

    @SuppressWarnings("unused")
    private volatile E value;

    @SuppressWarnings("unchecked")
    public E getRelaxed() {
      return (E) UnsafeAccess.UNSAFE.getObject(this, VALUE_OFFSET);
    }

    public boolean compareAndSet(E expect, E update) {
      return UnsafeAccess.UNSAFE.compareAndSwapObject(this, VALUE_OFFSET, expect, update);
    }

    public final void lazySet(E newValue) {
      UnsafeAccess.UNSAFE.putOrderedObject(this, VALUE_OFFSET, newValue);
    }
  }
}