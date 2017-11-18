/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package com.intellij.vcs.log.graph.parser;

import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.vcs.log.graph.api.LinearGraph;
import com.intellij.vcs.log.graph.api.elements.GraphEdge;
import com.intellij.vcs.log.graph.api.elements.GraphEdgeType;
import com.intellij.vcs.log.graph.api.elements.GraphNode;
import com.intellij.vcs.log.graph.utils.LinearGraphUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.intellij.vcs.log.graph.parser.CommitParser.nextSeparatorIndex;
import static com.intellij.vcs.log.graph.parser.CommitParser.toLines;
import static com.intellij.vcs.log.graph.parser.EdgeNodeCharConverter.parseGraphEdgeType;
import static com.intellij.vcs.log.graph.parser.EdgeNodeCharConverter.parseGraphNodeType;

public class LinearGraphParser {

  public static LinearGraph parse(@NotNull String in) {
    List<GraphNode> graphNodes = new ArrayList<GraphNode>();

    Map<GraphNode, List<String>> edges = ContainerUtil.newHashMap();
    Map<Integer, Integer> nodeIdToNodeIndex = ContainerUtil.newHashMap();

    for (String line : toLines(in)) { // parse input and create nodes
      Pair<GraphNode, List<String>> graphNodePair = parseLine(line, graphNodes.size());
      GraphNode graphNode = graphNodePair.first;

      edges.put(graphNode, graphNodePair.second);
      nodeIdToNodeIndex.put(graphNode.getNodeId(), graphNodes.size());
      graphNodes.add(graphNode);
    }

    MultiMap<Integer, GraphEdge> upEdges = MultiMap.create();
    MultiMap<Integer, GraphEdge> downEdges = MultiMap.create();
    for (GraphNode graphNode : graphNodes) { // create edges
      for (String strEdge : edges.get(graphNode)) {
        Pair<Integer, Character> pairEdge = parseNumberWithChar(strEdge);
        GraphEdgeType type = parseGraphEdgeType(pairEdge.second);

        GraphEdge edge;
        switch (type) {
          case USUAL:
          case DOTTED:
            Integer downNodeIndex = nodeIdToNodeIndex.get(pairEdge.first);
            assert downNodeIndex != null;
            edge = new GraphEdge(graphNode.getNodeIndex(), downNodeIndex, type);
            downEdges.putValue(edge.getUpNodeIndex(), edge);
            upEdges.putValue(edge.getDownNodeIndex(), edge);
            break;

          case NOT_LOAD_COMMIT:
          case DOTTED_ARROW_DOWN:
            edge = new GraphEdge(graphNode.getNodeIndex(), null, pairEdge.first, type);
            downEdges.putValue(edge.getUpNodeIndex(), edge);
            break;

          case DOTTED_ARROW_UP:
            edge = new GraphEdge(null, graphNode.getNodeIndex(), pairEdge.first, type);
            upEdges.putValue(edge.getDownNodeIndex(), edge);
            break;

          default:
            throw new IllegalStateException("Unknown type: " + type);
        }
      }
    }

    return new TestLinearGraphWithElementsInfo(graphNodes, upEdges, downEdges);
  }

  /**
   * Example input line:
   * 0_U|-1_U 2_D
   */
  public static Pair<GraphNode, List<String>> parseLine(@NotNull String line, int lineNumber) {
    int separatorIndex = nextSeparatorIndex(line, 0);
    Pair<Integer, Character> pair = parseNumberWithChar(line.substring(0, separatorIndex));

    GraphNode graphNode = new GraphNode(pair.first, lineNumber, parseGraphNodeType(pair.second));

    String[] edges = line.substring(separatorIndex + 2).split("\\s");
    List<String> normalEdges = ContainerUtil.mapNotNull(edges, new Function<String, String>() {
      @Nullable
      @Override
      public String fun(String s) {
        if (s.isEmpty()) return null;
        return s;
      }
    });
    return Pair.create(graphNode, normalEdges);
  }

  private static Pair<Integer, Character> parseNumberWithChar(@NotNull String in) {
    return new Pair<Integer, Character>(Integer.decode(in.substring(0, in.length() - 2)), in.charAt(in.length() - 1));
  }

  private static class TestLinearGraphWithElementsInfo implements LinearGraph {

    private final List<GraphNode> myGraphNodes;
    private final MultiMap<Integer, GraphEdge> myUpEdges;
    private final MultiMap<Integer, GraphEdge> myDownEdges;

    private TestLinearGraphWithElementsInfo(List<GraphNode> graphNodes,
                                            MultiMap<Integer, GraphEdge> upEdges,
                                            MultiMap<Integer, GraphEdge> downEdges) {
      myGraphNodes = graphNodes;
      myUpEdges = upEdges;
      myDownEdges = downEdges;
    }

    @Override
    public int nodesCount() {
      return myGraphNodes.size();
    }

    @NotNull
    @Override
    public List<Integer> getUpNodes(int nodeIndex) {
      return LinearGraphUtils.getUpNodes(this, nodeIndex);
    }

    @NotNull
    @Override
    public List<Integer> getDownNodes(int nodeIndex) {
      return LinearGraphUtils.getDownNodes(this, nodeIndex);
    }

    @NotNull
    @Override
    public List<GraphEdge> getAdjacentEdges(int nodeIndex) {
      return ContainerUtil.newArrayList(ContainerUtil.concat(myUpEdges.get(nodeIndex), myDownEdges.get(nodeIndex)));
    }

    @NotNull
    @Override
    public GraphNode getGraphNode(int nodeIndex) {
      return myGraphNodes.get(nodeIndex);
    }

    @Override
    @Nullable
    public Integer getNodeIndexById(int nodeId) {
      if (nodeId >= 0 && nodeId < nodesCount())
        return nodeId;
      return null;
    }
  }
}