package com.thinkaurelius.titan.olap;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.olap.*;
import com.thinkaurelius.titan.graphdb.TitanGraphBaseTest;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public abstract class OLAPTest extends TitanGraphBaseTest {

    private static final double EPSILON = 0.00001;

    protected abstract <S> OLAPJobBuilder<S> getOLAPBuilder(StandardTitanGraph graph, Class<S> clazz);

    @Test
    public void degreeCount() throws Exception {
        mgmt.makePropertyKey("uid").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
        mgmt.makeEdgeLabel("knows").multiplicity(Multiplicity.MULTI).make();
        finishSchema();
        int numV = 400;
        int numE = 0;
        TitanVertex[] vs = new TitanVertex[numV];
        for (int i=0;i<numV;i++) {
            vs[i] = tx.addVertex();
            vs[i].setProperty("uid",i+1);
        }
        Random random = new Random();
        for (int i=0;i<numV;i++) {
            int edges = i+1;
            TitanVertex v = vs[i];
            for (int j=0;j<edges;j++) {
                TitanVertex u = vs[random.nextInt(numV)];
                v.addEdge("knows", u);
                numE++;
            }
        }
        assertEquals(numV*(numV+1),numE*2);
        clopen();

        OLAPJobBuilder<Degree> builder = getOLAPBuilder(graph,Degree.class);
        builder.setInitializer(new StateInitializer<Degree>() {
            @Override
            public Degree initialState() {
                return new Degree();
            }
        });
        builder.setNumProcessingThreads(1);
        builder.setStateKey("degree");
        builder.setJob(new OLAPJob() {
            @Override
            public Degree process(TitanVertex vertex) {
                Degree d = vertex.getProperty("all");
                assertNotNull(d);
                return d;
            }
        });
        builder.addQuery().setName("all").edges(new Gather<Degree, Degree>() {
            @Override
            public Degree apply(Degree state, TitanEdge edge, Direction dir) {
                return new Degree(dir==Direction.IN?1:0,dir==Direction.OUT?1:0);
            }
        }, new Combiner<Degree>() {
            @Override
            public Degree combine(Degree m1, Degree m2) {
                m1.add(m2);
                return m1;
            }
        });
        Stopwatch w = new Stopwatch().start();
        OLAPResult<Degree> degrees = builder.execute().get(200, TimeUnit.SECONDS);
        System.out.println("Execution time (ms) ["+numV+"|"+numE+"]: " + w.elapsed(TimeUnit.MILLISECONDS));
        assertNotNull(degrees);
        assertEquals(numV,degrees.size());
        int totalCount = 0;
        for (Map.Entry<Long,Degree> entry : degrees.entries()) {
            Degree degree = entry.getValue();
            assertEquals(degree.in+degree.out,degree.both);
            Vertex v = tx.getVertex(entry.getKey().longValue());
            int count = v.getProperty("uid");
            assertEquals(count,degree.out);
            totalCount+= degree.both;
        }
        assertEquals(numV*(numV+1),totalCount);
    }

    public class Degree {
        public int in;
        public int out;
        public int both;

        public Degree(int in, int out) {
            this.in=in;
            this.out=out;
            both=in+out;
        }

        public Degree() {
            this(0,0);
        }

        public void add(Degree d) {
            in+=d.in;
            out+=d.out;
            both+=d.both;
        }

    }


    private void expand(TitanVertex v, final int distance, final int diameter, final int branch) {
        v.setProperty("distance",distance);
        if (distance<diameter) {
            TitanVertex previous = null;
            for (int i=0;i<branch;i++) {
                TitanVertex u = tx.addVertex();
                u.addEdge("likes",v);
                if (previous!=null) u.addEdge("knows",previous);
                previous=u;
                expand(u,distance+1,diameter,branch);
            }
        }
    }

    @Test
    public void pageRank() {
        mgmt.makePropertyKey("distance").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
        mgmt.makeEdgeLabel("knows").multiplicity(Multiplicity.MULTI).make();
        mgmt.makeEdgeLabel("likes").multiplicity(Multiplicity.MULTI).make();
        finishSchema();
        final int branch = 5;
        final int diameter = 5;
        final double alpha = 0.85d;
        int numV = (int)((Math.pow(branch,diameter+1)-1)/(branch-1));
        TitanVertex v = tx.addVertex();
        expand(v,0,diameter,branch);
        clopen();
        assertEquals(numV,Iterables.size(tx.getVertices()));
        newTx();

        //Precompute correct PR results:
        double[] correctPR = new double[diameter+1];
        for (int i=diameter;i>=0;i--) {
            double pr = 1.0/numV*(1-alpha);
            if (i<diameter) pr+= alpha*branch*correctPR[i+1];
            correctPR[i]=pr;
        }

        Stopwatch w = new Stopwatch().start();
        OLAPResult<PageRank> ranks = computePageRank(graph,alpha,1,numV,"likes");
        System.out.println(String.format("Computing PR on graph with %s vertices took: %s ms",numV,w.elapsed(TimeUnit.MILLISECONDS)));
        double totalPr = 0.0;
        for (Map.Entry<Long,PageRank> entry : ranks.entries()) {
            Vertex u = tx.getVertex(entry.getKey());
            int distance = u.<Integer>getProperty("distance");
            double pr = entry.getValue().getPr();
            assertEquals(correctPR[distance],pr,EPSILON);
//            System.out.println(distance+" -> "+pr);
            totalPr+=pr;
        }
//        System.out.println("Total PR: " + totalPr);

    }

    private static final double PR_TERMINATION_THRESHOLD = 0.01;

    private OLAPResult<PageRank> computePageRank(final StandardTitanGraph g, final double alpha, final int numThreads,
                                               final int numVertices, final String... labels) {
        //Initializing
        OLAPJobBuilder<PageRank> builder = getOLAPBuilder(graph,PageRank.class);
        builder.setNumProcessingThreads(numThreads);
        builder.setStateKey("pageRank");
        builder.setNumVertices(numVertices);
        OLAPQueryBuilder<PageRank,?,?> query = builder.addQuery().setName("degree").direction(Direction.OUT);
        if (labels!=null && labels.length>0) {
            query.labels(labels);
        }
        query.edges(new Gather<PageRank, Long>() {
            @Override
            public Long apply(PageRank state, TitanEdge edge, Direction dir) {
                return 1l;
            }
        },new Combiner<Long>() {
            @Override
            public Long combine(Long m1, Long m2) {
                return m1+m2;
            }
        });
        builder.setJob(new OLAPJob() {
            @Override
            public PageRank process(TitanVertex vertex) {
                Long degree = vertex.<Long>getProperty("degree");
                return new PageRank(degree==null?0:degree,1.0d/numVertices);
            }
        });
        OLAPResult<PageRank> ranks;
        try {
            ranks = builder.execute().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals(numVertices,ranks.size());
        double totalDelta;
        int iteration = 0;
        do {
            builder = getOLAPBuilder(graph,PageRank.class);
            builder.setNumProcessingThreads(numThreads);
            builder.setStateKey("pageRank");
            builder.setInitialState(ranks);
            query = builder.addQuery().setName("energy").direction(Direction.IN);
            if (labels!=null && labels.length>0) {
                query.labels(labels);
            }
            query.edges(new Gather<PageRank, Double>() {
                @Override
                public Double apply(PageRank state, TitanEdge edge, Direction dir) {
                    return state.getPrFlow();
                }
            },new Combiner<Double>() {
                @Override
                public Double combine(Double m1, Double m2) {
                    return m1+m2;
                }
            });
            builder.setJob(new OLAPJob() {
                @Override
                public PageRank process(TitanVertex vertex) {
                    PageRank pr = vertex.<PageRank>getProperty("pageRank");
                    Double energy = vertex.getProperty("energy");
                    if (energy==null) energy=0.0;
                    pr.setPr(energy, alpha, numVertices);
                    return pr;
                }
            });
            Stopwatch w = new Stopwatch().start();
            try {
                ranks = builder.execute().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            iteration++;
            assertEquals(numVertices,ranks.size());
            totalDelta = 0.0;
            for (PageRank pr : ranks.values()) {
                totalDelta+=pr.completeIteration();
            }
            System.out.println(String.format("Completed iteration [%s] in time %s ms with delta PR=%s",iteration,w.elapsed(TimeUnit.MILLISECONDS),totalDelta));
        } while (totalDelta>PR_TERMINATION_THRESHOLD);
        return ranks;
    }

    static class PageRank {

        private double edgeCount;
        private double oldPR;
        private double newPR;

        public PageRank(long edgeCount, double initialPR) {
            Preconditions.checkArgument(edgeCount>=0 && initialPR>=0);
            this.edgeCount=edgeCount;
            this.oldPR = initialPR;
            this.newPR = -1.0;
        }

        public void setPr(double energy, double alpha, long numVertices) {
            newPR = alpha * energy + (1.0 - alpha) / numVertices;
        }

        public double getPrFlow() {
            Preconditions.checkArgument(oldPR>=0 && edgeCount>0.0);
            return oldPR/edgeCount;
        }

        public double getPr() {
            return oldPR;
        }

        public double completeIteration() {
            double delta = Math.abs(oldPR-newPR);
            oldPR=newPR;
            newPR=0.0;
            return delta;
        }

    }

}