package com.thinkaurelius.titan.hadoop.mapreduce.transform;

import com.thinkaurelius.titan.hadoop.BaseTest;
import com.thinkaurelius.titan.hadoop.StandardFaunusEdge;
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
public class VerticesMapTest extends BaseTest {

    MapReduceDriver<NullWritable, HadoopVertex, NullWritable, HadoopVertex, NullWritable, HadoopVertex> mapReduceDriver;

    public void setUp() {
        mapReduceDriver = new MapReduceDriver<NullWritable, HadoopVertex, NullWritable, HadoopVertex, NullWritable, HadoopVertex>();
        mapReduceDriver.setMapper(new VerticesMap.Map());
        mapReduceDriver.setReducer(new Reducer<NullWritable, HadoopVertex, NullWritable, HadoopVertex>());
    }

    public void testVerticesWithNoPaths() throws Exception {
        Configuration config = new Configuration();
        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = runWithGraph(generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config), mapReduceDriver);

        assertEquals(graph.size(), 6);
        for (HadoopVertex vertex : graph.values()) {
            assertEquals(vertex.pathCount(), 1);
            for (Edge edge : vertex.getEdges(Direction.BOTH)) {
                assertEquals(((StandardFaunusEdge) edge).pathCount(), 0);
            }

            try {
                vertex.getPaths();
                assertTrue(false);
            } catch (IllegalStateException e) {
                assertTrue(true);
            }
        }

//        assertEquals(mapReduceDriver.getCounters().findCounter(VerticesMap.Counters.EDGES_PROCESSED).getValue(), 12);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, VerticesMap.Counters.EDGES_PROCESSED), 12);
//        assertEquals(mapReduceDriver.getCounters().findCounter(VerticesMap.Counters.VERTICES_PROCESSED).getValue(), 6);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, VerticesMap.Counters.VERTICES_PROCESSED), 6);

        identicalStructure(graph, ExampleGraph.TINKERGRAPH);
    }

    public void testVerticesWithPaths() throws Exception {
        Configuration config = new Configuration();
        config.setBoolean(Tokens.TITAN_HADOOP_PIPELINE_TRACK_PATHS, true);

        mapReduceDriver.withConfiguration(config);

        Map<Long, HadoopVertex> graph = runWithGraph(generateGraph(BaseTest.ExampleGraph.TINKERGRAPH, config), mapReduceDriver);

        assertEquals(graph.size(), 6);
        for (HadoopVertex vertex : graph.values()) {
            assertEquals(vertex.pathCount(), 1);
            assertEquals(vertex.getPaths().get(0).size(), 1);
            assertEquals(vertex.getPaths().get(0).get(0).getId(), vertex.getId());
            for (Edge edge : vertex.getEdges(Direction.BOTH)) {
                assertEquals(((StandardFaunusEdge) edge).pathCount(), 0);
            }
        }

//        assertEquals(mapReduceDriver.getCounters().findCounter(VerticesMap.Counters.EDGES_PROCESSED).getValue(), 12);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, VerticesMap.Counters.EDGES_PROCESSED), 12);
//        assertEquals(mapReduceDriver.getCounters().findCounter(VerticesMap.Counters.VERTICES_PROCESSED).getValue(), 6);
        assertEquals(HadoopCompatLoader.getDefaultCompat().getCounter(mapReduceDriver, VerticesMap.Counters.VERTICES_PROCESSED), 6);

        identicalStructure(graph, ExampleGraph.TINKERGRAPH);
    }
}