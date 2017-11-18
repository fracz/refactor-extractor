package org.hanuna.gitalk.graph.mutable_graph.graph_fragment_controller;

import org.hanuna.gitalk.common.compressedlist.Replace;
import org.hanuna.gitalk.graph.GraphFragment;
import org.hanuna.gitalk.graph.graph_elements.Edge;
import org.hanuna.gitalk.graph.graph_elements.GraphElement;
import org.hanuna.gitalk.graph.graph_elements.Node;
import org.hanuna.gitalk.graph.mutable_graph.MutableGraph;
import org.hanuna.gitalk.graph.mutable_graph.MutableGraphUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static org.hanuna.gitalk.graph.mutable_graph.MutableGraphUtils.*;

/**
 * @author erokhins
 */
public class SimpleGraphFragmentController implements GraphFragmentController {
    private final MutableGraph graph;
    private final Map<Edge, HiddenFragment> hiddenFragments = new HashMap<Edge, HiddenFragment>();

    public SimpleGraphFragmentController(MutableGraph graph) {
        this.graph = graph;
    }

    private boolean simpleNode(@NotNull Node node) {
        return node.getUpEdges().size() <= 1 && node.getDownEdges().size() <= 1;
    }

    /**
     * @return null, if node is not simple
     */
    @Nullable
    private Node lowestSimpleNode(@NotNull Node node) {
        if (!simpleNode(node)) {
            return null;
        }
        while (node.getDownEdges().size() == 1) {
            Node nextNode = firstDownEdge(node).getDownNode();
            if (simpleNode(nextNode)) {
                node = nextNode;
            } else {
                return node;
            }
        }
        return node;
    }

    /**
     * @return null, if node is not simple
     */
    @Nullable
    private Node upperSimpleNode(@NotNull Node node) {
        if (!simpleNode(node)) {
            return null;
        }
        while (node.getUpEdges().size() == 1) {
            Node prevNode = firstUpEdge(node).getUpNode();
            if (simpleNode(prevNode)) {
                node = prevNode;
            } else {
                return node;
            }
        }
        return node;
    }

    private boolean isTrivialFragment(@NotNull Node up, @NotNull Node down) {
        assert up.getRowIndex() < down.getRowIndex() : "up: " + up.getRowIndex() + " down: " + down.getRowIndex();
        Edge edge = firstDownEdge(up);
        if (edge.getType() == Edge.Type.USUAL && edge.getDownNode() == down) {
            return true;
        }
        return false;
    }

    private GraphFragment relateFragment(@NotNull Node node) {
        if (node.getDownEdges().size() == 1) {
            Edge edge = firstDownEdge(node);
            if (edge.getType() == Edge.Type.HIDE_FRAGMENT) {
                return new GraphFragmentImpl(edge.getUpNode(), edge.getDownNode());
            }
        }
        if (node.getUpEdges().size() == 1) {
            Edge edge = firstUpEdge(node);
            if (edge.getType() == Edge.Type.HIDE_FRAGMENT) {
                return new GraphFragmentImpl(edge.getUpNode(), edge.getDownNode());
            }
        }
        if (!simpleNode(node)) {
            return null;
        }
        Node up = upperSimpleNode(node);
        Node down = lowestSimpleNode(node);
        assert up != null && down != null;

        if (up != down && firstDownEdge(up).getDownNode() != down) {
            if (isTrivialFragment(up, down)) {
                return null;
            }
            return new GraphFragmentImpl(up, down);
        } else {
            return null;
        }
    }

    private GraphFragment relateFragment(@NotNull Edge edge) {
        if (edge.getType() == Edge.Type.HIDE_FRAGMENT) {
            return new GraphFragmentImpl(edge.getUpNode(), edge.getDownNode());
        }
        Node up = upperSimpleNode(edge.getUpNode());
        Node down = lowestSimpleNode(edge.getDownNode());
        if (up == null || down == null) {
            return null;
        }
        if (isTrivialFragment(up, down)) {
            return null;
        }
        return new GraphFragmentImpl(up, down);
    }



    @Override
    public GraphFragment relateFragment(@NotNull GraphElement graphElement) {
        Node node = graphElement.getNode();
        if (node != null) {
            return relateFragment(node);
        }
        Edge edge = graphElement.getEdge();
        if (edge != null) {
            return relateFragment(edge);
        }
        throw new IllegalStateException("unexpected graphElement");
    }

    @NotNull
    @Override
    public Replace hideFragment(@NotNull GraphFragment fragment) {
        fragment.intermediateWalker(new GraphFragment.Runner() {
            @Override
            public void edgeRun(@NotNull Edge edge) {
                // do nothing
            }

            @Override
            public void nodeRun(@NotNull Node node) {
                MutableGraphUtils.setVisible(node, false);
            }
        });

        Node upNode = fragment.getUpNode();
        Edge upEdge = firstDownEdge(upNode);
        assert upEdge.getType() == Edge.Type.USUAL;
        Node downNode = fragment.getDownNode();
        Edge downEdge = firstUpEdge(downNode);

        // fix edges
        HiddenFragment hiddenFragment = new HiddenFragment(upEdge.getDownNode(), downEdge.getUpNode());
        removeEdge(upEdge);
        removeEdge(downEdge);
        Edge longEdge = createEdge(upNode, downNode, Edge.Type.HIDE_FRAGMENT, upNode.getBranch());
        hiddenFragments.put(longEdge, hiddenFragment);

        return graph.fixRowVisibility(upNode.getRowIndex(), downNode.getRowIndex());
    }

    @NotNull
    @Override
    public Replace showFragment(@NotNull GraphFragment fragment) {
        Node upNode = fragment.getUpNode();
        Edge longEdge = firstDownEdge(upNode);
        final Node downNode = fragment.getDownNode();
        assert longEdge.getType() == Edge.Type.HIDE_FRAGMENT && longEdge.getDownNode() == downNode;
        HiddenFragment hiddenFragment = hiddenFragments.remove(longEdge);
        assert hiddenFragment != null;

        // fix edges
        removeEdge(longEdge);
        createEdge(upNode, hiddenFragment.getUpHiddenNode(), Edge.Type.USUAL, upNode.getBranch());
        final Node downHiddenNode = hiddenFragment.getDownHiddenNode();
        createEdge(downHiddenNode, downNode, Edge.Type.USUAL, downHiddenNode.getBranch());

        fragment.intermediateWalker(new GraphFragment.Runner() {
            @Override
            public void edgeRun(@NotNull Edge edge) {
                // do nothing
            }

            @Override
            public void nodeRun(@NotNull Node node) {
                MutableGraphUtils.setVisible(node, true);
            }
        });

        return graph.fixRowVisibility(upNode.getRowIndex(), downNode.getRowIndex());
    }

    @Override
    public boolean isHidden(@NotNull GraphFragment fragment) {
        Edge first = firstDownEdge(fragment.getUpNode());
        return first.getType() == Edge.Type.HIDE_FRAGMENT;
    }

    private static class HiddenFragment {
        private final Node upHiddenNode;
        private final Node downHiddenNode;

        private HiddenFragment(Node upHiddenNode, Node downHiddenNode) {
            this.upHiddenNode = upHiddenNode;
            this.downHiddenNode = downHiddenNode;
        }

        public Node getUpHiddenNode() {
            return upHiddenNode;
        }

        public Node getDownHiddenNode() {
            return downHiddenNode;
        }
    }
}