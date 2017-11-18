package com.thinkaurelius.titan.graphdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.thinkaurelius.titan.core.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import com.thinkaurelius.titan.graphdb.internal.ElementLifeCycle;
import com.thinkaurelius.titan.graphdb.internal.InternalVertex;
import com.thinkaurelius.titan.graphdb.relations.StandardEdge;
import com.thinkaurelius.titan.testutil.JUnitBenchmarkProvider;
import com.thinkaurelius.titan.testutil.MemoryAssess;
import com.thinkaurelius.titan.testutil.PerformanceTest;
import com.thinkaurelius.titan.testutil.RandomGenerator;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

@BenchmarkOptions(warmupRounds = 0, benchmarkRounds = 3)
public abstract class TitanGraphPerformanceMemoryTest extends TitanGraphTestCommon {

    @Rule
    public TestRule benchmark = JUnitBenchmarkProvider.get();

    public TitanGraphPerformanceMemoryTest(Configuration config) {
        super(config);
    }

    @Test
    public void testMemoryLeakage() {
        long memoryBaseline = 0;
        SummaryStatistics stats = new SummaryStatistics();
        int numRuns = 25;
        for (int r = 0; r < numRuns; r++) {
            if (r == 1 || r == (numRuns - 1)) {
                memoryBaseline = MemoryAssess.getMemoryUse();
                stats.addValue(memoryBaseline);
                //System.out.println("Memory before run "+(r+1)+": " + memoryBaseline / 1024 + " KB");
            }
            for (int t = 0; t < 1000; t++) {
                graph.addVertex(null);
                graph.rollback();
                TitanTransaction tx = graph.newTransaction();
                tx.addVertex();
                tx.rollback();
            }
            if (r == 1 || r == (numRuns - 1)) {
                memoryBaseline = MemoryAssess.getMemoryUse();
                stats.addValue(memoryBaseline);
                //System.out.println("Memory after run " + (r + 1) + ": " + memoryBaseline / 1024 + " KB");
            }
            clopen();
        }
        System.out.println("Average: " + stats.getMean() + " Std. Dev: " + stats.getStandardDeviation());
        assertTrue(stats.getStandardDeviation() < stats.getMin());
    }

    @Test
    public void testTransactionalMemory() throws Exception {
        graph.makeType().name("uid").dataType(Long.class).indexed(Vertex.class).vertexUnique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK)
                .graphUnique(TypeMaker.UniquenessConsistency.NO_LOCK).makePropertyKey();
        graph.makeType().name("name").dataType(String.class).vertexUnique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK)
                .makePropertyKey();
        TitanKey time = graph.makeType().name("time").dataType(Integer.class).vertexUnique(Direction.OUT, TypeMaker.UniquenessConsistency.NO_LOCK).makePropertyKey();
        graph.makeType().name("friend").signature(time).directed().makeEdgeLabel();
        graph.commit();

        final Random random = new Random();
        final int rounds = 100;
        final int commitSize = 1500;
        final AtomicInteger uidCounter = new AtomicInteger(0);
        Thread[] writeThreads = new Thread[4];
        long start = System.currentTimeMillis();
        for (int t = 0; t < writeThreads.length; t++) {
            writeThreads[t] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int r = 0; r < rounds; r++) {
                        TitanTransaction tx = graph.newTransaction();
                        TitanVertex previous = null;
                        for (int c = 0; c < commitSize; c++) {
                            TitanVertex v = tx.addVertex();
                            long uid = uidCounter.incrementAndGet();
                            v.setProperty("uid", uid);
                            v.setProperty("name", "user" + uid);
                            if (previous != null) {
                                v.addEdge("friend", previous).setProperty("time", Math.abs(random.nextInt()));
                            }
                            previous = v;
                        }
                        tx.commit();
                    }
                }
            });
            writeThreads[t].start();
        }
        for (int t = 0; t < writeThreads.length; t++) {
            writeThreads[t].join();
        }
        System.out.println("Write time for " + (rounds * commitSize * writeThreads.length) + " vertices & edges: " + (System.currentTimeMillis() - start));

        final int maxUID = uidCounter.get();
        final int trials = 100000;
        final String fixedName = "john";
        Thread[] readThreads = new Thread[8];
        start = System.currentTimeMillis();
        for (int t = 0; t < readThreads.length; t++) {
            readThreads[t] = new Thread(new Runnable() {
                @Override
                public void run() {
                    TitanTransaction tx = graph.newTransaction();
                    long ruid = random.nextInt(maxUID) + 1;
                    tx.getVertex("uid", ruid).setProperty("name", fixedName);
                    for (int t = 1; t <= trials; t++) {
                        TitanVertex v = tx.getVertex("uid", random.nextInt(maxUID) + 1);
                        assertEquals(2, Iterables.size(v.getProperties()));
                        int count = 0;
                        for (TitanEdge e : v.getEdges()) {
                            count++;
                            assertTrue(((Number) e.getProperty("time")).longValue() >= 0);
                        }
                        assertTrue(count <= 2);
//                        if (t%(trials/10)==0) System.out.println(t);
                    }
                    assertEquals(fixedName, tx.getVertex("uid", ruid).getProperty("name"));
                    tx.commit();
                }
            });
            readThreads[t].start();
        }
        for (int t = 0; t < readThreads.length; t++) {
            readThreads[t].join();
        }
        System.out.println("Read time for " + (trials * readThreads.length) + " vertex lookups: " + (System.currentTimeMillis() - start));

    }

    @Test
    public void elementCreationPerformance() {
        TitanLabel connect = makeSimpleEdgeLabel("connect");
        int noNodes = 20000;
        TitanVertex[] nodes = new TitanVertex[noNodes];
        PerformanceTest p = new PerformanceTest(true);
        for (int i = 0; i < noNodes; i++) {
            nodes[i] = tx.addVertex();
        }
        p.end();
        System.out.println("Time per node in (ns): " + (p.getNanoTime() / noNodes));

        p = new PerformanceTest(true);
        for (int i = 0; i < noNodes; i++) {
            new StandardEdge(i + 1, connect, (InternalVertex) nodes[i], (InternalVertex) nodes[(i + 1) % noNodes], ElementLifeCycle.New);
        }
        p.end();
        System.out.println("Time per edge in (ns): " + (p.getNanoTime() / noNodes));

        p = new PerformanceTest(true);
        for (int i = 0; i < noNodes; i++) {
            nodes[i].addEdge(connect, nodes[(i + 1) % noNodes]);
        }
        p.end();
        System.out.println("Time per edge creation+connection in (ns): " + (p.getNanoTime() / noNodes));
        tx.rollback();
        tx = null;
    }

    @Test
    public void testInTxIndex() throws Exception {
        int trials = 2;
        int numV = 2000;
        int offset = 10000;
        tx.makeType().name("uid").dataType(Long.class).indexed(Vertex.class).vertexUnique(Direction.OUT).makePropertyKey();
        newTx();

        long start = System.currentTimeMillis();
        for (int t = 0; t < trials; t++) {
            for (int i = offset; i < offset + numV; i++) {
                if (Iterables.isEmpty(tx.getVertices("uid", Long.valueOf(i)))) {
                    Vertex v = tx.addVertex();
                    v.setProperty("uid", Long.valueOf(i));
                }
            }
        }
        assertEquals(numV, Iterables.size(tx.getVertices()));
        System.out.println("Total time (ms): " + (System.currentTimeMillis() - start));
    }

}

