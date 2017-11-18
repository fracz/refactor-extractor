package com.thinkaurelius.titan.hadoop.mapreduce.sideeffect;

import com.thinkaurelius.titan.hadoop.BaseTest;
import com.thinkaurelius.titan.hadoop.HadoopEdge;
import com.thinkaurelius.titan.hadoop.HadoopVertex;
import com.thinkaurelius.titan.hadoop.Tokens;
import com.thinkaurelius.titan.hadoop.compat.HadoopCompatLoader;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CommitEdgesMapTest extends BaseTest {

    MapReduceDriver<NullWritable, HadoopVertex, NullWritable, HadoopVertex, NullWritable, HadoopVertex> mapReduceDriver;

    public void setUp() {
        mapReduceDriver = new MapReduceDriver<NullWritable, HadoopVertex, NullWritable, HadoopVertex, NullWritable, HadoopVertex>();
        mapReduceDriver.setMapper(new CommitEdgesMap.Map());
        mapReduceDriver.setReducer(new Reducer<NullWritable, HadoopVertex, NullWritable, HadoopVertex>());
    }

    public void testDropAllEdges() throws Exception {
        Configuration config = CommitEdgesMap.createConfiguration(Tokens.Action.DROP);
        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = runWithGraph(startPath(generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config), Edge.class), mapReduceDriver);
        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 0);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        for (HadoopVertex vertex : graph.values()) {
            assertFalse(vertex.getEdges(Direction.BOTH).iterator().hasNext());
        }

//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.OUT_EDGES_DROPPED).getValue(), 6);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.OUT_EDGES_DROPPED), 6);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.OUT_EDGES_KEPT).getValue(), 0);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.OUT_EDGES_KEPT), 0);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.IN_EDGES_DROPPED).getValue(), 6);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.IN_EDGES_DROPPED), 6);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.IN_EDGES_KEPT).getValue(), 0);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.IN_EDGES_KEPT), 0);
    }

    public void testKeepAllEdges() throws Exception {
        Configuration config = CommitEdgesMap.createConfiguration(Tokens.Action.KEEP);
        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = runWithGraph(startPath(generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config), Edge.class), mapReduceDriver);
        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 0);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        for (HadoopVertex vertex : graph.values()) {
            assertTrue(vertex.getEdges(Direction.BOTH).iterator().hasNext());
        }

//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.OUT_EDGES_DROPPED).getValue(), 0);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.OUT_EDGES_DROPPED), 0);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.OUT_EDGES_KEPT).getValue(), 6);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.OUT_EDGES_KEPT), 6);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.IN_EDGES_DROPPED).getValue(), 0);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.IN_EDGES_DROPPED), 0);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.IN_EDGES_KEPT).getValue(), 6);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.IN_EDGES_KEPT), 6);
    }

    public void testDropAllCreatedEdge() throws Exception {
        Configuration config = CommitEdgesMap.createConfiguration(Tokens.Action.DROP);
        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config);
        for (HadoopVertex vertex : graph.values()) {
            for (Edge edge : vertex.getEdges(Direction.BOTH, "created")) {
                ((HadoopEdge) edge).startPath();
            }
        }
        graph = runWithGraph(graph, mapReduceDriver);
        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 0);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        int counter = 0;
        for (HadoopVertex vertex : graph.values()) {
            assertFalse(vertex.getEdges(Direction.BOTH, "created").iterator().hasNext());
            if (vertex.getEdges(Direction.BOTH, "knows").iterator().hasNext())
                counter++;
        }
        assertEquals(counter, 3);

//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.OUT_EDGES_DROPPED).getValue(), 4);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.OUT_EDGES_DROPPED), 4);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.OUT_EDGES_KEPT).getValue(), 2);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.OUT_EDGES_KEPT), 2);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.IN_EDGES_DROPPED).getValue(), 4);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.IN_EDGES_DROPPED), 4);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.IN_EDGES_KEPT).getValue(), 2);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.IN_EDGES_KEPT), 2);
    }

    public void testKeepAllCreatedEdge() throws Exception {
        Configuration config = new Configuration();
        config.set(CommitEdgesMap.ACTION, Tokens.Action.KEEP.name());

        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config);
        for (HadoopVertex vertex : graph.values()) {
            for (Edge edge : vertex.getEdges(Direction.BOTH, "created")) {
                ((HadoopEdge) edge).startPath();
            }
        }
        graph = runWithGraph(graph, mapReduceDriver);
        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 0);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        int counter = 0;
        for (HadoopVertex vertex : graph.values()) {
            assertFalse(vertex.getEdges(Direction.BOTH, "knows").iterator().hasNext());
            if (vertex.getEdges(Direction.BOTH, "created").iterator().hasNext())
                counter++;
        }
        assertEquals(counter, 5);

//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.OUT_EDGES_DROPPED).getValue(), 2);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.OUT_EDGES_DROPPED), 2);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.OUT_EDGES_KEPT).getValue(), 4);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.OUT_EDGES_KEPT), 4);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.IN_EDGES_DROPPED).getValue(), 2);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.IN_EDGES_DROPPED), 2);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CommitEdgesMap.Counters.IN_EDGES_KEPT).getValue(), 4);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CommitEdgesMap.Counters.IN_EDGES_KEPT), 4);
    }

}