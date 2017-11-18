commit 807760488eaaab1ced435ec07dab6be7e991fc47
Author: jasexton <jasexton@google.com>
Date:   Fri Mar 18 19:22:02 2016 -0700

    Major refactoring of the com.google.common.graph APIs (including updates to client code):
    -Remove DirectedGraph and UndirectedGraph interfaces; a Graph's directedness can now be queried with graph.isDirected().
    -Remove GraphConfig class; use the query methods directly on the Graph interface instead.
    -Add GraphBuilder; use this instead of Graphs.create[Un]Directed(GraphConfig)
    -Remove ImmutableGraph.Builder; ImmutableGraph construction is now done with ImmutableGraph.copyOf(Graph).
    -Rename multigraph() to allowsParallelEdges(boolean).
    -Rename noSelfLoops() to allowsSelfLoops(boolean).

    Old example syntax for creating a graph:
      DirectedGraph<N, E> graph = Graphs.createDirected(GraphConfig.multigraph());

    New example syntax for creating a graph:
      Graph<N, E> graph = GraphBuilder.directed().allowsParallelEdges(true).build();
    -------------
    Created by MOE: https://github.com/google/moe
    MOE_MIGRATED_REVID=117606125