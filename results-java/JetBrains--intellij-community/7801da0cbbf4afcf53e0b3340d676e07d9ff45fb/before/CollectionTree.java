/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.debugger.streams.ui;

import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.memory.utils.InstanceJavaValue;
import com.intellij.debugger.memory.utils.InstanceValueDescriptor;
import com.intellij.debugger.streams.resolve.ResolvedCall;
import com.intellij.debugger.ui.impl.watch.DebuggerTreeNodeImpl;
import com.intellij.debugger.ui.impl.watch.MessageDescriptor;
import com.intellij.debugger.ui.impl.watch.NodeManagerImpl;
import com.intellij.debugger.ui.tree.NodeDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.impl.actions.XDebuggerActions;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Vitaliy.Bibaev
 */
public class CollectionTree extends XDebuggerTree implements ValuesHighlightingListener {
  private static final ValuesHighlightingListener EMPTY_LISTENER = (values, direction) -> {
  };

  private final NodeManagerImpl myNodeManager;
  private final Project myProject;
  private final EvaluationContextImpl myEvaluationContext;
  private final ResolvedCall myResolvedCall;
  private final Map<Value, TreePath> myValue2Path = new HashMap<>();
  private final Map<TreePath, Value> myPath2Value = new HashMap<>();
  private Set<TreePath> myHighlighted = Collections.emptySet();

  private ValuesHighlightingListener myBackwardListener = EMPTY_LISTENER;
  private ValuesHighlightingListener myForwardListener = EMPTY_LISTENER;
  private boolean myIgnoreSelectionEvents = false;

  public CollectionTree(@NotNull Project project,
                        @NotNull ResolvedCall call,
                        @NotNull EvaluationContextImpl evaluationContext) {
    super(project, new JavaDebuggerEditorsProvider(), null, XDebuggerActions.INSPECT_TREE_POPUP_GROUP, null);

    myProject = project;
    myEvaluationContext = evaluationContext;
    myNodeManager = new MyNodeManager(project);
    myResolvedCall = call;
    final List<Value> values = call.getValues();
    final XValueNodeImpl root = new XValueNodeImpl(this, null, "root", new MyRootValue(call.getValues()));
    setRoot(root, false);
    root.setLeaf(false);

    setCellRenderer(new ColoredTreeCellRenderer() {
      @Override
      public void customizeCellRenderer(@NotNull JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        if (value instanceof XValueNodeImpl) {
          final XValueNodeImpl node = (XValueNodeImpl)value;
          final TreePath path = node.getPath();
          append(value.toString());
          if (myHighlighted.contains(path)) {
            setIcon(AllIcons.Debugger.ThreadStates.Idle);
          }
        }
      }
    });

    getTreeModel().addTreeModelListener(new TreeModelAdapter() {
      @Override
      public void treeNodesInserted(TreeModelEvent event) {
        final Object[] children = event.getChildren();
        for (int i = 0; i < children.length; i++) {
          Object child = children[i];
          if (child instanceof XValueNodeImpl) {
            final XValueNodeImpl node = (XValueNodeImpl)child;
            myValue2Path.put(call.getValues().get(i), node.getPath());
            myPath2Value.put(node.getPath(), values.get(i));
          }
        }

        getTreeModel().removeTreeModelListener(this);
      }
    });

    addTreeSelectionListener(e -> {
      if (myIgnoreSelectionEvents) {
        return;
      }

      final TreePath[] paths = e.getPaths();
      final List<Value> values1 =
        Arrays.stream(paths).filter(e::isAddedPath).map(myPath2Value::get).filter(Objects::nonNull).collect(Collectors.toList());
      if (values1.isEmpty()) {
        return;
      }

      myHighlighted = values1.stream().map(myValue2Path::get).collect(Collectors.toSet());

      propagateBackward(values1);
      propagateForward(values1);

      repaint();
    });

    setSelectionRow(0);
    expandNodesOnLoad(node -> node == root);
  }

  @Override
  public void highlightingChanged(@NotNull List<Value> values, @NotNull PropagationDirection direction) {
    myIgnoreSelectionEvents = true;
    clearSelection();

    myHighlighted = values.stream().map(myValue2Path::get).collect(Collectors.toSet());
    if (direction == PropagationDirection.BACKWARD) {
      propagateBackward(values);
    }
    else {
      propagateForward(values);
    }
    repaint();
    myIgnoreSelectionEvents = false;
  }

  public void setBackwardListener(@NotNull ValuesHighlightingListener listener) {
    myBackwardListener = listener;
  }

  public void setForwardListener(@NotNull ValuesHighlightingListener listener) {
    myForwardListener = listener;
  }

  private void propagateBackward(@NotNull List<Value> values) {
    final List<Value> prevValues = values.stream().flatMap(x -> myResolvedCall.getPreviousValues(x).stream()).collect(Collectors.toList());

    myBackwardListener.highlightingChanged(prevValues, PropagationDirection.BACKWARD);
  }

  private void propagateForward(@NotNull List<Value> values) {
    final List<Value> nextValues = values.stream().flatMap(x -> myResolvedCall.getNextValues(x).stream()).collect(Collectors.toList());

    myForwardListener.highlightingChanged(nextValues, PropagationDirection.FORWARD);
  }

  private class MyRootValue extends XValue {
    private final List<Value> myValues;

    MyRootValue(@NotNull List<Value> values) {
      myValues = values;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
      final XValueChildrenList children = new XValueChildrenList();
      for (Value value : myValues) {
        children.add(new InstanceJavaValue(new InstanceValueDescriptor(myProject, value), myEvaluationContext, myNodeManager));
      }

      node.addChildren(children, true);
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
      node.setPresentation(null, "", "", true);
    }
  }

  private final static class MyNodeManager extends NodeManagerImpl {
    MyNodeManager(Project project) {
      super(project, null);
    }

    @Override
    public DebuggerTreeNodeImpl createNode(final NodeDescriptor descriptor, EvaluationContext evaluationContext) {
      return new DebuggerTreeNodeImpl(null, descriptor);
    }

    @Override
    public DebuggerTreeNodeImpl createMessageNode(MessageDescriptor descriptor) {
      return new DebuggerTreeNodeImpl(null, descriptor);
    }

    @Override
    public DebuggerTreeNodeImpl createMessageNode(String message) {
      return new DebuggerTreeNodeImpl(null, new MessageDescriptor(message));
    }
  }
}