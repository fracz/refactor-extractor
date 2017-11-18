package com.thinkaurelius.faunus.mapreduce.steps;

import com.thinkaurelius.faunus.BaseTest;
import com.thinkaurelius.faunus.FaunusVertex;
import com.thinkaurelius.faunus.FaunusEdge;
import com.thinkaurelius.faunus.mapreduce.steps.RetainEdgeLabels;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.types.Pair;

import java.io.IOException;
import java.util.List;

import static com.tinkerpop.blueprints.Direction.OUT;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RetainEdgeLabelsTest extends BaseTest {

    MapDriver<NullWritable, FaunusVertex, NullWritable, FaunusVertex> mapDriver;

    public void setUp() throws Exception {
        mapDriver = new MapDriver<NullWritable, FaunusVertex, NullWritable, FaunusVertex>();
        mapDriver.setMapper(new RetainEdgeLabels.Map());
    }

    /*public void testMap1() throws IOException {
        mapDriver.resetOutput();

        FaunusVertex vertex1 = new FaunusVertex(1);
        vertex1.setProperty("name", "marko");
        vertex1.addEdge(OUT, new FaunusEdge(new FaunusVertex(1), new FaunusVertex(2), "knows"));
        vertex1.addEdge(OUT, new FaunusEdge(new FaunusVertex(1), new FaunusVertex(3), "created"));

        mapDriver.withInput(NullWritable.get(), vertex1);
        List<Pair<NullWritable, FaunusVertex>> list = mapDriver.run();
        assertEquals(list.size(), 1);

        FaunusVertex vertex2 = list.get(0).getSecond();
        List<Edge> edges = BaseTest.asList(vertex2.getEdges(Direction.OUT));
        assertEquals(edges.size(), 2);
        assertTrue(edges.get(0).getLabel().equals("knows") || edges.get(0).getLabel().equals("created"));
        assertTrue(edges.get(1).getLabel().equals("knows") || edges.get(1).getLabel().equals("created"));

        assertEquals(mapDriver.getCounters().findCounter(RetainEdgeLabels.Counters.EDGES_ALLOWED).getValue(), 2);
        assertEquals(mapDriver.getCounters().findCounter(RetainEdgeLabels.Counters.EDGES_FILTERED).getValue(), 0);
    }*/

    public void testMap2() throws IOException {
        mapDriver.resetOutput();

        Configuration config = new Configuration();
        config.setStrings(RetainEdgeLabels.LABELS, "knows");
        mapDriver.withConfiguration(config);

        FaunusVertex vertex1 = new FaunusVertex(1);
        vertex1.setProperty("name", "marko");
        vertex1.addEdge(OUT, new FaunusEdge(new FaunusVertex(1), new FaunusVertex(2), "knows"));
        vertex1.addEdge(OUT, new FaunusEdge(new FaunusVertex(1), new FaunusVertex(3), "created"));

        mapDriver.withInput(NullWritable.get(), vertex1);
        List<Pair<NullWritable, FaunusVertex>> list = mapDriver.run();
        assertEquals(list.size(), 1);

        FaunusVertex vertex2 = list.get(0).getSecond();
        List<Edge> edges = BaseTest.asList(vertex2.getEdges(Direction.OUT));
        assertEquals(edges.size(), 1);
        assertEquals(edges.get(0).getLabel(), "knows");

        assertEquals(mapDriver.getCounters().findCounter(RetainEdgeLabels.Counters.EDGES_ALLOWED).getValue(), 1);
        assertEquals(mapDriver.getCounters().findCounter(RetainEdgeLabels.Counters.EDGES_FILTERED).getValue(), 1);
    }

}