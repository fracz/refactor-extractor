package org.hanuna.gitalk.graphmodel.impl;

import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.vcs.log.CommitParents;
import com.intellij.vcs.log.Ref;
import org.hanuna.gitalk.common.compressedlist.UpdateRequest;
import org.hanuna.gitalk.graph.Graph;
import org.hanuna.gitalk.graph.elements.Node;
import org.hanuna.gitalk.graph.mutable.GraphBuilder;
import org.hanuna.gitalk.graph.mutable.MutableGraph;
import org.hanuna.gitalk.graphmodel.FragmentManager;
import org.hanuna.gitalk.graphmodel.GraphModel;
import org.hanuna.gitalk.graphmodel.fragment.FragmentManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author erokhins
 */
public class GraphModelImpl implements GraphModel {
  private final MutableGraph graph;
  private final List<Ref> myRefs;
  private final FragmentManagerImpl fragmentManager;
  private final BranchVisibleNodes visibleNodes;
  private final List<Consumer<UpdateRequest>> listeners = new ArrayList<Consumer<UpdateRequest>>();
  private final GraphBranchShowFixer branchShowFixer;

  private Function<Node, Boolean> isStartedBranchVisibilityNode = new Function<Node, Boolean>() {
    @NotNull
    @Override
    public Boolean fun(@NotNull Node key) {
      return true;
    }
  };

  public GraphModelImpl(MutableGraph graph, List<Ref> allRefs) {
    this.graph = graph;
    myRefs = allRefs;
    this.fragmentManager = new FragmentManagerImpl(graph, new FragmentManagerImpl.CallBackFunction() {
      @Override
      public UpdateRequest runIntermediateUpdate(@NotNull Node upNode, @NotNull Node downNode) {
        return GraphModelImpl.this.updateIntermediate(upNode, downNode);
      }

      @Override
      public void fullUpdate() {
        GraphModelImpl.this.fullUpdate();
      }
    });
    this.visibleNodes = new BranchVisibleNodes(graph);
    visibleNodes.setVisibleNodes(visibleNodes.generateVisibleBranchesNodes(isStartedBranchVisibilityNode));
    branchShowFixer = new GraphBranchShowFixer(graph, fragmentManager);
    graph.setGraphDecorator(new GraphDecoratorImpl(fragmentManager.getGraphPreDecorator(), new Function<Node, Boolean>() {
      @NotNull
      @Override
      public Boolean fun(@NotNull Node key) {
        return visibleNodes.isVisibleNode(key);
      }
    }));
    graph.updateVisibleRows();
  }

  @NotNull
  private UpdateRequest updateIntermediate(@NotNull Node upNode, @NotNull Node downNode) {
    int upRowIndex = upNode.getRowIndex();
    int downRowIndex = downNode.getRowIndex();
    graph.updateVisibleRows();

    UpdateRequest updateRequest = UpdateRequest.buildFromToInterval(upRowIndex, downRowIndex, upNode.getRowIndex(), downNode.getRowIndex());
    callUpdateListener(updateRequest);
    return updateRequest;
  }

  private void fullUpdate() {
    int oldSize = graph.getNodeRows().size();
    graph.updateVisibleRows();
    UpdateRequest updateRequest = UpdateRequest.buildFromToInterval(0, oldSize - 1, 0, graph.getNodeRows().size() - 1);
    callUpdateListener(updateRequest);
  }

  @NotNull
  @Override
  public Graph getGraph() {
    return graph;
  }

  @Override
  public void appendCommitsToGraph(@NotNull List<CommitParents> commitParentses) {
    int oldSize = graph.getNodeRows().size();
    GraphBuilder.addCommitsToGraph(graph, commitParentses, myRefs);
    visibleNodes.setVisibleNodes(visibleNodes.generateVisibleBranchesNodes(isStartedBranchVisibilityNode));
    graph.updateVisibleRows();

    UpdateRequest updateRequest = UpdateRequest.buildFromToInterval(0, oldSize - 1, 0, graph.getNodeRows().size() - 1);
    callUpdateListener(updateRequest);
  }

  @Override
  public void setVisibleBranchesNodes(@NotNull Function<Node, Boolean> isStartedNode) {
    this.isStartedBranchVisibilityNode = isStartedNode;
    Set<Node> prevVisibleNodes = visibleNodes.getVisibleNodes();
    Set<Node> newVisibleNodes = visibleNodes.generateVisibleBranchesNodes(isStartedNode);
    branchShowFixer.fixCrashBranches(prevVisibleNodes, newVisibleNodes);
    visibleNodes.setVisibleNodes(newVisibleNodes);
    fullUpdate();
  }

  @NotNull
  @Override
  public FragmentManager getFragmentManager() {
    return fragmentManager;
  }

  private void callUpdateListener(@NotNull UpdateRequest updateRequest) {
    for (Consumer<UpdateRequest> listener : listeners) {
      listener.consume(updateRequest);
    }
  }

  @Override
  public void addUpdateListener(@NotNull Consumer<UpdateRequest> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeAllListeners() {
    listeners.clear();
  }
}