package com.intellij.debugger.ui.impl.watch;

import com.intellij.debugger.ui.tree.NodeDescriptor;

import java.util.*;

/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */

public class DescriptorTree {
  private HashMap<NodeDescriptor, List<NodeDescriptor>> myChildrenMap = new HashMap<NodeDescriptor, List<NodeDescriptor>>();
  private List<NodeDescriptor> myRootChildren = new ArrayList<NodeDescriptor>();
  private final boolean myInitial;
  private int myFrameCount = -1;
  private int myFrameIndex = -1;

  public DescriptorTree() {
    this(false);
  }

  public DescriptorTree(boolean isInitial) {
    myInitial = isInitial;
  }

  public boolean frameIdEquals(final int frameCount, final int frameIndex) {
    return myFrameCount == frameCount && myFrameIndex == frameIndex;
  }

  public void setFrameId(final int frameCount, final int frameIndex) {
    myFrameIndex = frameIndex;
    myFrameCount = frameCount;
  }

  public void addChild(NodeDescriptor parent, NodeDescriptor child) {
    List<NodeDescriptor> children;

    if(parent == null) {
      children = myRootChildren;
    }
    else {
      children = myChildrenMap.get(parent);
      if(children == null) {
        children = new ArrayList<NodeDescriptor>();
        myChildrenMap.put(parent, children);
      }
    }
    children.add(child);
    if (myInitial && child instanceof LocalVariableDescriptorImpl) {
      ((LocalVariableDescriptorImpl)child).setNewLocal(false);
    }
  }

  public List<NodeDescriptor> getChildren(NodeDescriptor parent) {
    if(parent == null) {
      return myRootChildren;
    }

    List<NodeDescriptor> children = myChildrenMap.get(parent);
    return children != null ? children : Collections.EMPTY_LIST;
  }

  public void dfst(DFSTWalker walker) {
    dfstImpl(null, myRootChildren, walker);
  }

  private void dfstImpl(NodeDescriptor descriptor, List<NodeDescriptor> children, DFSTWalker walker) {
    if(children != null) {
      for (Iterator<NodeDescriptor> iterator = children.iterator(); iterator.hasNext();) {
        NodeDescriptor child = iterator.next();
        walker.visit(descriptor, child);
        dfstImpl(child, myChildrenMap.get(child), walker);
      }
    }
  }

  public interface DFSTWalker {
    void visit(NodeDescriptor parent, NodeDescriptor child);
  }
}