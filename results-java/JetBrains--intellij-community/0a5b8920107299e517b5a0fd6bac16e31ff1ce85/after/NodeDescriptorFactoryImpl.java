package com.intellij.debugger.ui.impl.watch;

import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.jdi.LocalVariableProxy;
import com.intellij.debugger.engine.jdi.StackFrameProxy;
import com.intellij.debugger.engine.jdi.ThreadReferenceProxy;
import com.intellij.debugger.engine.StackFrameContext;
import com.intellij.debugger.impl.descriptors.data.*;
import com.intellij.debugger.jdi.LocalVariableProxyImpl;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.debugger.jdi.ThreadGroupReferenceProxyImpl;
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl;
import com.intellij.debugger.ui.tree.NodeDescriptor;
import com.intellij.debugger.ui.tree.NodeDescriptorFactory;
import com.intellij.debugger.ui.tree.UserExpressionDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.sun.jdi.*;

import java.util.HashMap;

/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */

public class NodeDescriptorFactoryImpl implements NodeDescriptorFactory {
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.ui.impl.watch.NodeDescriptorFactoryImpl");
  private DescriptorTree myCurrentHistoryTree = new DescriptorTree(true);

  private DescriptorTreeSearcher myDescriptorSearcher;
  private DescriptorTreeSearcher myDisplayDescriptorSearcher;

  protected final Project      myProject;

  public NodeDescriptorFactoryImpl(Project project) {
    myProject = project;
    myDescriptorSearcher        = new DescriptorTreeSearcher(new MarkedDescriptorTree());
    myDisplayDescriptorSearcher = new DisplayDescriptorTreeSearcher(new MarkedDescriptorTree());
  }

  private <T extends NodeDescriptor> T getDescriptor(NodeDescriptor parent, DescriptorData<T> data) {
    T descriptor = data.createDescriptor(myProject);

    T oldDescriptor = findDescriptor(parent, descriptor, data);

    if(oldDescriptor != null && oldDescriptor.getClass() == descriptor.getClass()) {
      descriptor.setAncestor(oldDescriptor);
    }
    else {
      T displayDescriptor = findDisplayDescriptor(parent, descriptor, data.getDisplayKey());
      if(displayDescriptor != null) {
        descriptor.displayAs(displayDescriptor);
      }
    }

    myCurrentHistoryTree.addChild(parent, descriptor);

    return descriptor;
  }

  protected <T extends NodeDescriptor>T findDisplayDescriptor(NodeDescriptor parent, T descriptor, DisplayKey<T> key) {
    return myDisplayDescriptorSearcher.search(parent, descriptor, key);
  }

  protected <T extends NodeDescriptor> T findDescriptor(NodeDescriptor parent, T descriptor, DescriptorData<T> data) {
    return myDescriptorSearcher.search(parent, descriptor, data);
  }

  public DescriptorTree getCurrentHistoryTree() {
    return myCurrentHistoryTree;
  }

  public void deriveHistoryTree(DescriptorTree tree, final StackFrameContext context) {

    final MarkedDescriptorTree descriptorTree = new MarkedDescriptorTree();
    final MarkedDescriptorTree displayDescriptorTree = new MarkedDescriptorTree();

    tree.dfst(new DescriptorTree.DFSTWalker() {
      public void visit(NodeDescriptor parent, NodeDescriptor child) {
        final DescriptorData<NodeDescriptor> descriptorData = DescriptorData.getDescriptorData(child);
        descriptorTree.addChild(parent, child, descriptorData);
        displayDescriptorTree.addChild(parent, child, descriptorData.getDisplayKey());
      }
    });

    myDescriptorSearcher = new DescriptorTreeSearcher(descriptorTree);
    myDisplayDescriptorSearcher = new DisplayDescriptorTreeSearcher(displayDescriptorTree);

    myCurrentHistoryTree = createDescriptorTree(context, tree);
  }

  private DescriptorTree createDescriptorTree(final StackFrameContext context, final DescriptorTree fromTree) {
    int frameCount = -1;
    int frameIndex = -1;
    final StackFrameProxy frameProxy = context.getFrameProxy();
    if (frameProxy != null) {
      try {
        final ThreadReferenceProxy threadReferenceProxy = frameProxy.threadProxy();
        frameCount = threadReferenceProxy.frameCount();
        frameIndex = frameProxy.getFrameIndex();
       }
       catch (EvaluateException e) {
         // ignored
       }
    }
    final boolean isInitial = !fromTree.frameIdEquals(frameCount, frameIndex);
    DescriptorTree descriptorTree = new DescriptorTree(isInitial);
    descriptorTree.setFrameId(frameCount, frameIndex);
    return descriptorTree;
  }

  public ArrayElementDescriptorImpl getArrayItemDescriptor(NodeDescriptor parent, ArrayReference array, int index) {
    return getDescriptor(parent, new ArrayItemData(array, index));
  }

  public FieldDescriptorImpl getFieldDescriptor(NodeDescriptor parent, ObjectReference objRef, Field field) {
    final DescriptorData<FieldDescriptorImpl> descriptorData;
    if (objRef == null ) {
      if (!field.isStatic()) {
        LOG.assertTrue(false, "Object reference is null for non-static field: " + field);
      }
      descriptorData = new StaticFieldData(field);
    }
    else {
      descriptorData = new FieldData(objRef, field);
    }
    return getDescriptor(parent, descriptorData);
  }

  public LocalVariableDescriptorImpl getLocalVariableDescriptor(NodeDescriptor parent, LocalVariableProxy local) {
    return getDescriptor(parent, new LocalData((LocalVariableProxyImpl)local));
  }

  public StackFrameDescriptorImpl getStackFrameDescriptor(NodeDescriptorImpl parent, StackFrameProxyImpl frameProxy) {
    return getDescriptor(parent, new StackFrameData(frameProxy));
  }

  public StaticDescriptorImpl getStaticDescriptor(NodeDescriptorImpl parent, ReferenceType refType) {//static is unique
    return getDescriptor(parent, new StaticData(refType));
  }

  public ValueDescriptorImpl getThisDescriptor(NodeDescriptorImpl parent, Value value) {
    return getDescriptor(parent, new ThisData(value));
  }

  public ThreadDescriptorImpl getThreadDescriptor(NodeDescriptorImpl parent, ThreadReferenceProxyImpl thread) {
    return getDescriptor(parent, new ThreadData(thread));
  }

  public ThreadGroupDescriptorImpl getThreadGroupDescriptor(NodeDescriptorImpl parent, ThreadGroupReferenceProxyImpl group) {
    return getDescriptor(parent, new ThreadGroupData(group));
  }

  public UserExpressionDescriptor getUserExpressionDescriptor(NodeDescriptor parent, final DescriptorData<UserExpressionDescriptor> data) {
    return getDescriptor(parent, data);
  }

  private static class DescriptorTreeSearcher {
    private final MarkedDescriptorTree myDescriportTree;

    private final HashMap<NodeDescriptor, NodeDescriptor> mySearchedDescriptors = new HashMap<NodeDescriptor, NodeDescriptor>();

    public DescriptorTreeSearcher(MarkedDescriptorTree descriportTree) {
      myDescriportTree = descriportTree;
    }

    public <T extends NodeDescriptor> T search(NodeDescriptor parent, T descriptor, DescriptorKey<T> key) {
      T result = searchImpl(parent, key);
      if(result != null) {
        mySearchedDescriptors.put(descriptor, result);
      }
      return result;
    }

    private <T extends NodeDescriptor> T searchImpl(NodeDescriptor parent, DescriptorKey<T> key) {
      if(parent == null) {
        return myDescriportTree.getChild(null, key);
      }
      else {
        NodeDescriptor parentDescriptor = getSearched(parent);
        return parentDescriptor != null ? myDescriportTree.getChild(parentDescriptor, key) : null;
      }
    }

    protected NodeDescriptor getSearched(NodeDescriptor parent) {
      return mySearchedDescriptors.get(parent);
    }
  }

  private class DisplayDescriptorTreeSearcher extends DescriptorTreeSearcher {
    public DisplayDescriptorTreeSearcher(MarkedDescriptorTree descriportTree) {
      super(descriportTree);
    }

    protected NodeDescriptor getSearched(NodeDescriptor parent) {
      NodeDescriptor searched = super.getSearched(parent);
      if(searched == null) {
        return myDescriptorSearcher.getSearched(parent);
      }
      return searched;
    }
  }
}