package com.thinkaurelius.titan.hadoop.mapreduce.filter;

import com.thinkaurelius.titan.hadoop.BaseTest;
import com.thinkaurelius.titan.hadoop.HadoopEdge;
import com.thinkaurelius.titan.hadoop.HadoopVertex;
import com.thinkaurelius.titan.hadoop.Holder;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BackFilterMapReduceTest extends BaseTest {

    MapReduceDriver<NullWritable, HadoopVertex, LongWritable, Holder, NullWritable, HadoopVertex> mapReduceDriver;

    public void setUp() {
        mapReduceDriver = new MapReduceDriver<NullWritable, HadoopVertex, LongWritable, Holder, NullWritable, HadoopVertex>();
        mapReduceDriver.setMapper(new BackFilterMapReduce.Map());
        mapReduceDriver.setCombiner(new BackFilterMapReduce.Combiner());
        mapReduceDriver.setReducer(new BackFilterMapReduce.Reduce());
    }

    public void testVerticesFullStart() throws Exception {
        Configuration config = BackFilterMapReduce.createConfiguration(Vertex.class, 0);
        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = runWithGraph(startPath(generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config), Vertex.class), mapReduceDriver);

        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 1);
        assertEquals(graph.get(2l).pathCount(), 1);
        assertEquals(graph.get(3l).pathCount(), 1);
        assertEquals(graph.get(4l).pathCount(), 1);
        assertEquals(graph.get(5l).pathCount(), 1);
        assertEquals(graph.get(6l).pathCount(), 1);

        identicalStructure(graph, ExampleGraph.TINKERGRAPH);
    }

    public void testVerticesBiasedStart() throws Exception {
        Configuration config = BackFilterMapReduce.createConfiguration(Vertex.class, 0);
        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config);

        graph.get(1l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopVertex.MicroVertex(1l)), false);
        graph.get(2l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopVertex.MicroVertex(2l)), false);
        graph.get(3l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(2l), new HadoopVertex.MicroVertex(3l)), false);
        graph.get(4l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(3l), new HadoopVertex.MicroVertex(4l)), false);
        graph.get(5l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(3l), new HadoopVertex.MicroVertex(5l)), false);

        graph = runWithGraph(graph, mapReduceDriver);

        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 2);
        assertEquals(graph.get(2l).pathCount(), 1);
        assertEquals(graph.get(3l).pathCount(), 2);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 0);

        identicalStructure(graph, ExampleGraph.TINKERGRAPH);
    }

    public void testBackingUpToEdgesException() throws Exception {
        Configuration config = BackFilterMapReduce.createConfiguration(Vertex.class, 1);
        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config);

        graph.get(1l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopEdge.MicroEdge(1l)), false);
        graph.get(2l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopEdge.MicroEdge(2l)), false);
        graph.get(3l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(2l), new HadoopEdge.MicroEdge(3l)), false);
        graph.get(4l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(3l), new HadoopEdge.MicroEdge(4l)), false);
        graph.get(5l).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(3l), new HadoopEdge.MicroEdge(5l)), false);

        try {
            graph = runWithGraph(graph, mapReduceDriver);
            assertFalse(true);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testBackingUpToVerticesFromEdges() throws Exception {
        Configuration config = BackFilterMapReduce.createConfiguration(Edge.class, 0);
        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config);

        ((HadoopEdge) graph.get(1l).getEdges(Direction.OUT, "created").iterator().next()).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(1l), new HadoopEdge.MicroEdge(2l)), false);
        ((HadoopEdge) graph.get(6l).getEdges(Direction.OUT, "created").iterator().next()).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(6l), new HadoopEdge.MicroEdge(2l)), false);
        ((HadoopEdge) graph.get(6l).getEdges(Direction.OUT, "created").iterator().next()).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(2l), new HadoopEdge.MicroEdge(2l)), false);
        ((HadoopEdge) graph.get(6l).getEdges(Direction.OUT, "created").iterator().next()).addPath((List) Arrays.asList(new HadoopVertex.MicroVertex(2l), new HadoopEdge.MicroEdge(2l)), false);


        graph = runWithGraph(graph, mapReduceDriver);

        assertEquals(graph.size(), 6);
        assertEquals(graph.get(1l).pathCount(), 1);
        assertEquals(graph.get(2l).pathCount(), 2);
        assertEquals(graph.get(3l).pathCount(), 0);
        assertEquals(graph.get(4l).pathCount(), 0);
        assertEquals(graph.get(5l).pathCount(), 0);
        assertEquals(graph.get(6l).pathCount(), 1);

        for (Vertex vertex : graph.values()) {
            for (Edge e : vertex.getEdges(Direction.BOTH)) {
                assertEquals(((HadoopEdge) e).pathCount(), 0);
            }
        }

        identicalStructure(graph, ExampleGraph.TINKERGRAPH);
    }

}