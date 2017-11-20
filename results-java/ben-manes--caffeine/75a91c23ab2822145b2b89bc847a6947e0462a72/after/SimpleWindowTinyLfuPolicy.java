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
package com.github.benmanes.caffeine.cache.simulator.policy.sketch;

import static com.google.common.base.Preconditions.checkState;

import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.Admittor;
import com.github.benmanes.caffeine.cache.simulator.admission.TinyLfu;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.MoreObjects;
import com.typesafe.config.Config;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * The Window TinyLfu algorithm where the eden and main spaces implement LRU. This simpler version
 * comes at the cost of not capturing recency as effectively as Segmented LRU does.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class SimpleWindowTinyLfuPolicy implements Policy {
  private final Long2ObjectMap<Node> data;
  private final PolicyStats policyStats;
  private final int recencyMoveDistance;
  private final Admittor admittor;
  private final Node headEden;
  private final Node headMain;
  private final int maxEden;
  private final int maxMain;

  private int sizeEden;
  private int sizeMain;
  private int mainRecencyCounter;

  public SimpleWindowTinyLfuPolicy(String name, Config config) {
    SimpleWindowTinyLfuSettings settings = new SimpleWindowTinyLfuSettings(config);
    this.maxMain = (int) (settings.maximumSize() * settings.percentMain());
    this.maxEden = settings.maximumSize() - maxMain;
    this.recencyMoveDistance = (int) (maxMain * settings.percentFastPath());
    this.data = new Long2ObjectOpenHashMap<>();
    this.policyStats = new PolicyStats(name);
    this.admittor = new TinyLfu(config);
    this.headEden = new Node();
    this.headMain = new Node();
  }

  @Override
  public PolicyStats stats() {
    return policyStats;
  }

  @Override
  public void record(long key) {
    policyStats.recordOperation();
    Node node = data.get(key);
    if (node == null) {
      admittor.record(key);

      node = new Node(key, Status.EDEN);
      node.appendToTail(headEden);
      data.put(key, node);
      sizeEden++;
      evict();

      policyStats.recordMiss();
    } else if (node.status == Status.EDEN) {
      admittor.record(key);
      node.moveToTail(headEden);
      policyStats.recordHit();
    } else if (node.status == Status.MAIN) {
      // Fast path skips the hottest entries, useful for concurrent caches
      if (node.recencyMove <= (mainRecencyCounter - recencyMoveDistance)) {
        node.recencyMove = ++mainRecencyCounter;
        node.moveToTail(headMain);
        admittor.record(key);
      }
      policyStats.recordHit();
    } else {
      throw new IllegalStateException();
    }
  }

  /** Evicts if the map exceeds the maximum capacity. */
  private void evict() {
    if (sizeEden <= maxEden) {
      return;
    }

    Node candidate = headEden.next;
    candidate.remove();
    sizeEden--;

    candidate.appendToTail(headMain);
    candidate.status = Status.MAIN;
    sizeMain++;

    if (sizeMain > maxMain) {
      Node victim = headMain.next;
      Node evict = admittor.admit(candidate.key, victim.key) ? victim : candidate;
      data.remove(evict.key);
      evict.remove();
      sizeMain--;

      policyStats.recordEviction();
    }

    if (candidate.isInQueue()) {
      candidate.recencyMove = ++mainRecencyCounter;
    }
  }

  @Override
  public void finished() {
    checkState(data.values().stream().filter(n -> n.status == Status.EDEN).count() == sizeEden);
    checkState(sizeEden + sizeMain == data.size());
    checkState(data.size() <= maxEden + maxMain);
  }

  enum Status {
    EDEN, MAIN
  }

  /** A node on the double-linked list. */
  static final class Node {
    final long key;

    int recencyMove;
    Status status;
    Node prev;
    Node next;

    /** Creates a new sentinel node. */
    public Node() {
      this.key = Integer.MIN_VALUE;
      this.prev = this;
      this.next = this;
    }

    /** Creates a new, unlinked node. */
    public Node(long key, Status status) {
      this.status = status;
      this.key = key;
    }

    public boolean isInQueue() {
      return next != null;
    }

    public void moveToTail(Node head) {
      remove();
      appendToTail(head);
    }

    /** Appends the node to the tail of the list. */
    public void appendToTail(Node head) {
      Node tail = head.prev;
      head.prev = this;
      tail.next = this;
      next = head;
      prev = tail;
    }

    /** Removes the node from the list. */
    public void remove() {
      prev.next = next;
      next.prev = prev;
      next = prev = null;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("key", key)
          .toString();
    }
  }

  static final class SimpleWindowTinyLfuSettings extends BasicSettings {
    public SimpleWindowTinyLfuSettings(Config config) {
      super(config);
    }
    public double percentMain() {
      return config().getDouble("simple-window-tiny-lfu.percent-main");
    }
    public double percentFastPath() {
      return config().getDouble("simple-window-tiny-lfu.percent-fast-path");
    }
  }
}