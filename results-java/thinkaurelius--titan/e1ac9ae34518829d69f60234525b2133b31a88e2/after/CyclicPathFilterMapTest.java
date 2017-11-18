package com.thinkaurelius.titan.hadoop.mapreduce.filter;

import com.thinkaurelius.titan.hadoop.BaseTest;
import com.thinkaurelius.titan.hadoop.StandardFaunusEdge;
import com.thinkaurelius.titan.hadoop.HadoopVertex;
import com.thinkaurelius.titan.hadoop.compat.HadoopCompatLoader;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CyclicPathFilterMapTest extends BaseTest {

    MapReduceDriver<NullWritable, HadoopVertex, NullWritable, HadoopVertex, NullWritable, HadoopVertex> mapReduceDriver;

    public void setUp() {
        mapReduceDriver = new MapReduceDriver<NullWritable, HadoopVertex, NullWritable, HadoopVertex, NullWritable, HadoopVertex>();
        mapReduceDriver.setMapper(new CyclicPathFilterMap.Map());
        mapReduceDriver.setReducer(new Reducer<NullWritable, HadoopVertex, NullWritable, HadoopVertex>());
    }

    public void testVertices() throws Exception {
        Configuration config = CyclicPathFilterMap.createConfiguration(Vertex.class);
        mapReduceDriver.withConfiguration(config);
        Map<Long, HadoopVertex> graph = generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config);

        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 0);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        graph.get(1l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopVertex.MicroVertex(2l)), false);
        graph.get(1l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopVertex.MicroVertex(1l)), false);
        graph.get(1l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopVertex.MicroVertex(1l)), false);

        assertEquals(graph.get(1l).pathCount(), 3);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        graph = runWithGraph(graph, mapReduceDriver);

        assertEquals(graph.get(1l).pathCount(), 1);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CyclicPathFilterMap.Counters.PATHS_FILTERED), 2);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CyclicPathFilterMap.Counters.PATHS_FILTERED).getValue(), 2);

        identicalStructure(graph, BaseTest.ExampleGraph.TINKERGRAPH);
    }

    public void testEdges() throws Exception {
        Configuration config = CyclicPathFilterMap.createConfiguration(Edge.class);

        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config);

        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 0);
        assertEquals(((StandardFaunusEdge) graph.get(1l).getEdges(Direction.OUT).iterator().next()).pathCount(), 0);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        ((StandardFaunusEdge) graph.get(1l).getEdges(Direction.OUT).iterator().next()).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopVertex.MicroVertex(2l)), false);
        ((StandardFaunusEdge) graph.get(1l).getEdges(Direction.OUT).iterator().next()).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopVertex.MicroVertex(1l)), false);
        ((StandardFaunusEdge) graph.get(1l).getEdges(Direction.OUT).iterator().next()).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopVertex.MicroVertex(1l)), false);

        assertEquals(graph.get(1l).pathCount(), 0);
        assertEquals(((StandardFaunusEdge) graph.get(1l).getEdges(Direction.OUT).iterator().next()).pathCount(), 3);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        graph = runWithGraph(graph, mapReduceDriver);

        assertEquals(graph.get(1l).pathCount(), 0);
        assertEquals(((StandardFaunusEdge) graph.get(1l).getEdges(Direction.OUT).iterator().next()).pathCount(), 1);
        assertEquals(graph.get(2l).pathCount(), 0);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);


        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, CyclicPathFilterMap.Counters.PATHS_FILTERED), 2);
//        assertEquals(mapReduceDriver.getCounters().findCounter(CyclicPathFilterMap.Counters.PATHS_FILTERED).getValue(), 2);

        identicalStructure(graph, BaseTest.ExampleGraph.TINKERGRAPH);
    }
}