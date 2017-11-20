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
package com.github.benmanes.caffeine.cache.simulator.policy.irr;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.MoreObjects;
import com.typesafe.config.Config;

/**
 * Low Inter-reference Recency Set (LIRS) algorithm. This algorithm organizes blocks by their
 * inter-reference recency (IRR) and groups entries as either having a low (LIR) or high (HIR)
 * recency. A LIR entry is preferable to retain in the cache and evicted HIR entries may be retained
 * as non-resident HIR entries. This allows a non-resident HIR entry to be promoted to a LIR entry
 * upon immediately upon a cache miss. The authors recommend sizing the maximum number of LIR blocks
 * to 99% of the cache's total size (1% remaining for HIR blocks). The authors do not provide a
 * recommendation for setting the maximum number of non-resident HIR blocks.
 * <p>
 * The algorithm is explained by the authors in
 * <a href="http://web.cse.ohio-state.edu/hpcs/WWW/HTML/publications/papers/TR-02-6.pdf">LIRS: An
 * Efficient Low Inter-reference Recency Set Replacement Policy to Improve Buffer Cache
 * Performance</a> and
 * <a href="http://web.cse.ohio-state.edu/hpcs/WWW/HTML/publications/papers/TR-05-11.pdf">Making LRU
 * Friendly to Weak Locality Workloads: A Novel Replacement Algorithm to Improve Buffer Cache
 * Performance</a>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class LirsPolicy implements Policy {
  private final PolicyStats policyStats;
  private final Map<Object, Node> data;
  private final List<Object> evicted;
  private final int maximumHotSize;
  private final int maximumSize;
  private final Node headS;
  private final Node headQ;

  private int sizeS;
  private int sizeQ;
  private int sizeHot;
  private int residentSize;

  // Enable to print out the internal state
  private final boolean debug = false;

  public LirsPolicy(String name, Config config) {
    LirsSettings settings = new LirsSettings(config);
    this.maximumHotSize = (int) (settings.maximumSize() * settings.percentHot());
    this.maximumSize = settings.maximumSize();
    this.policyStats = new PolicyStats(name);
    this.evicted = new ArrayList<>();
    this.data = new HashMap<>();
    this.headS = new Node();
    this.headQ = new Node();
  }

  @Override
  public void record(Comparable<Object> key) {
    Node node = data.get(key);
    if (node == null) {
      node = new Node(key);
      data.put(key,node);
      onNonResidentHir(node);
    } else if (node.status == Status.LIR) {
      onLir(node);
    } else if (node.status == Status.HIR_RESIDENT) {
      onResidentHir(node);
    } else if (node.status == Status.HIR_NON_RESIDENT) {
      onNonResidentHir(node);
    } else {
      throw new IllegalStateException();
    }
  }

  private void onLir(Node node) {
    // Upon accessing an LIR block X. This access is guaranteed to be a hit in the cache. We move
    // it to the top of stack S. If the LIR block is originally located at the bottom of the
    // stack, we conduct a stack pruning. This case is illustrated in the transition from state
    // (a) to state (b) in Fig. 2.
    policyStats.recordHit();

    boolean wasBottom = (headS.prevS == node);
    node.moveToTop(StackType.S);
    if (wasBottom) {
      pruneStack();
    }
  }

  private void onResidentHir(Node node) {
    // Upon accessing an HIR resident block X. This is a hit in the cache. We move it to the top
    // of the stack S. There are two cases for the original location of block X: a) If X is in
    // stack S, we change its status to LIR. This block is also removed from stack Q. The LIR
    // block at the bottom of S is moved to the top of stack Q with its status changed to HIR. A
    // stack pruning is then conducted. This case is illustrated in the transition from state (a)
    // to state (c) in Fig. 2. b) If X is not in stack S, we leave its status unchanged and move
    // it to the top of stack Q.
    policyStats.recordHit();

    boolean isInStack = node.isInStack(StackType.S);
    node.moveToTop(StackType.S);

    if (isInStack) {
      sizeHot++;
      node.status = Status.LIR;
      node.removeFrom(StackType.Q);

      Node bottom = headS.prevS;
      sizeHot--;

      bottom.status = Status.HIR_RESIDENT;
      bottom.removeFrom(StackType.S);
      bottom.moveToTop(StackType.Q);

      pruneStack();
    } else {
      node.moveToTop(StackType.Q);
    }
  }

  private void onNonResidentHir(Node node) {
    // When LIR block set is not full, all the referenced blocks are given an LIR status until its
    // size reaches Llirs. After that, HIR status is given to any blocks that are referenced for the
    // first time, and to the blocks that have not been referenced for a long time so that they are
    // not in stack S any longer.
    policyStats.recordMiss();

    if (sizeHot < maximumHotSize) {
      onWarmupMiss(node);
    } else {
      onFullMiss(node);
    }
    residentSize++;
  }

  /** Records a miss when the hot set is not full. */
  private void onWarmupMiss(Node node) {
    node.moveToTop(StackType.S);
    node.status = Status.LIR;
    sizeHot++;
  }

  /** Records a miss when the hot set is full. */
  private void onFullMiss(Node node) {
    // Upon accessing an HIR non-resident block X. This is a miss. We remove the HIR resident block
    // at the bottom of stack Q (it then becomes a non-resident block) and evict it from the cache.
    // Then, we load the requested block X into the freed buffer and place it at the top of stack
    // S. There are two cases for the original location of block X: a) If X is in the stack S, we
    // change its status to LIR and move the LIR block at the bottom of stack S to the top of
    // stack Q with its status changed to HIR. A stack pruning is then conducted. This case is
    // illustrated in the transition from state(a) to state(d) in Fig.2. b) If X is not in stack S,
    // we leave its status unchanged and place it at the top of stack Q. This case is illustrated in
    // the transition from state (a) to state (e) in Fig. 2.

    node.status = Status.HIR_RESIDENT;
    if (residentSize >= maximumSize) {
      evict();
    }

    boolean isInStack = node.isInStack(StackType.S);
    node.moveToTop(StackType.S);

    if (isInStack) {
      node.status = Status.LIR;
      sizeHot++;

      Node bottom = headS.prevS;
      checkState(bottom.status == Status.LIR);

      bottom.status = Status.HIR_RESIDENT;
      bottom.removeFrom(StackType.S);
      bottom.moveToTop(StackType.Q);
      sizeHot--;

      pruneStack();
    } else {
      node.moveToTop(StackType.Q);
    }
  }

  private void pruneStack() {
    // In the LIRS replacement, there is an operation called “stack pruning” on LIRS stack S, which
    // removes the HIR blocks at the stack bottom until a LIR block sits there. This operation
    // serves two purposes: 1) We ensure the block at the stack bottom always belongs to the LIR
    // block set. 2) After the LIR block in the bottom is removed, those HIR blocks contiguously
    // located above it will not have a chance to change their status from HIR to LIR since their
    // recencies are larger than the new maximum recency of the LIR blocks.
    for (;;) {
      Node bottom = headS.prevS;
      if ((bottom == headS) || (bottom.status == Status.LIR)) {
        break;
      } else if (bottom.status == Status.HIR_NON_RESIDENT) {
        // the map only needs to hold non-resident entries that are on the stack
        data.remove(bottom.key);
      }
      bottom.removeFrom(StackType.S);
    }
  }

  private void evict() {
    // Once a free block is needed, the LIRS algorithm removes a resident HIR block from the bottom
    // of stack Q for replacement. However, the replaced HIR block remains in stack S with its
    // residence status changed to “non resident” if it is originally in the stack. We ensure the
    // block in the bottom of the stack S is an LIR block by removing HIR blocks after it.

    policyStats.recordEviction();

    residentSize--;
    Node bottom = headQ.prevQ;
    bottom.removeFrom(StackType.Q);
    bottom.status = Status.HIR_NON_RESIDENT;
    if (!bottom.isInStack(StackType.S)) {
      // the map only needs to hold non-resident entries that are on the stack
      data.remove(bottom.key);
    }
    pruneStack();

    if (debug) {
      evicted.add(bottom.key);
    }
  }

  @Override
  public PolicyStats stats() {
    return policyStats;
  }

  @Override
  public void finished() {
    long resident = data.values().stream()
        .filter(node -> node.status != Status.HIR_NON_RESIDENT)
        .count();

    checkState(resident == residentSize);
    checkState(sizeHot <= maximumHotSize);
    checkState(residentSize <= maximumSize);
    checkState(sizeS == data.values().stream().filter(node -> node.isInS).count());
    checkState(sizeQ == data.values().stream().filter(node -> node.isInQ).count());

    if (debug) {
      printLirs();
    }
  }

  /** Prints out the internal state of the policy. */
  private void printLirs() {
    List<Object> stack = new ArrayList<>(sizeS);
    List<Object> queue = new ArrayList<>(sizeQ);

    for (Node n = headS.nextS; n != headS; n = n.nextS) {
      checkState(n.isInS);
      if (n.status == Status.HIR_NON_RESIDENT) {
        stack.add("NR_" + n.key);
      } else if (n.status == Status.HIR_RESIDENT) {
        stack.add("RH_" + n.key);
      } else {
        stack.add("RL_" + n.key);
      }
    }
    for (Node n = headQ.nextQ; n != headQ; n = n.nextQ) {
      checkState(n.isInQ);
      queue.add(n.key);
    }

    System.out.println("Stack: " + stack);
    System.out.println("Queue: " + queue);
    System.out.println("Evicted: " + evicted);
  }

  enum Status {
    LIR,
    HIR_RESIDENT,
    HIR_NON_RESIDENT;
  }

  // S holds three types of blocks, LIR blocks, resident HIR blocks, non-resident HIR blocks
  // Q holds all of the resident HIR blocks
  enum StackType {
    // We store LIR blocks and HIR blocks with their recencies less than the maximum recency of the
    // LIR blocks in a stack called LIRS stack S. S is similar to the LRU stack in operation but has
    // a variable size.
    S,
    // To facilitate the search of the resident HIR blocks, we link all these blocks into a small
    // stack, Q, with its size of Lhirs.
    Q;
  }

  // Each entry in the stack records the LIR/HIR status of a block and its residence status,
  // indicating whether or not the block resides in the cache.
  final class Node {
    final Object key;

    Status status;

    Node prevS;
    Node nextS;
    Node prevQ;
    Node nextQ;

    boolean isInS;
    boolean isInQ;

    Node() {
      key = null;
      prevS = nextS = this;
      prevQ = nextQ = this;
    }

    Node(Object key) {
      this.key = key;
    }

    public boolean isInStack(StackType stackType) {
      requireNonNull(key);

      if (stackType == StackType.S) {
        return isInS;
      } else if (stackType == StackType.Q) {
        return isInQ;
      } else {
        throw new IllegalArgumentException();
      }
    }

    public void moveToTop(StackType stackType) {
      if (isInStack(stackType)) {
        removeFrom(stackType);
      }

      if (stackType == StackType.S) {
        Node next = headS.nextS;
        headS.nextS = this;
        next.prevS = this;
        this.nextS = next;
        this.prevS = headS;
        isInS = true;
        sizeS++;
      } else if (stackType == StackType.Q) {
        Node next = headQ.nextQ;
        headQ.nextQ = this;
        next.prevQ = this;
        this.nextQ = next;
        this.prevQ = headQ;
        isInQ = true;
        sizeQ++;
      } else {
        throw new IllegalArgumentException();
      }
    }

    public void removeFrom(StackType stackType) {
      checkState(isInStack(stackType));

      if (stackType == StackType.S) {
        prevS.nextS = nextS;
        nextS.prevS = prevS;
        prevS = nextS = null;
        isInS = false;
        sizeS--;
      } else if (stackType == StackType.Q) {
        prevQ.nextQ = nextQ;
        nextQ.prevQ = prevQ;
        prevQ = nextQ = null;
        isInQ = false;
        sizeQ--;
      } else {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("key", key)
          .add("type", status)
          .toString();
    }
  }

  static final class LirsSettings extends BasicSettings {

    public LirsSettings(Config config) {
      super(config);
    }

    public double percentHot() {
      return config().getDouble("lirs.percent-hot");
    }
  }
}