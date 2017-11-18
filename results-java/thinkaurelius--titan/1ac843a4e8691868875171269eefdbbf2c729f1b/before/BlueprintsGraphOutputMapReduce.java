package com.thinkaurelius.faunus.formats;

import com.thinkaurelius.faunus.FaunusGraph;
import com.thinkaurelius.faunus.FaunusVertex;
import com.thinkaurelius.faunus.Holder;
import com.thinkaurelius.faunus.Tokens;
import com.thinkaurelius.faunus.formats.titan.GraphFactory;
import com.thinkaurelius.faunus.formats.titan.TitanOutputFormat;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;

import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BlueprintsGraphOutputMapReduce {

    public enum Counters {
        VERTICES_WRITTEN,
        VERTEX_PROPERTIES_WRITTEN,
        EDGES_WRITTEN,
        EDGE_PROPERTIES_WRITTEN,
        NULL_VERTEX_EDGES_IGNORED,
        NULL_VERTICES_IGNORED,
        SUCCESSFUL_TRANSACTIONS,
        FAILED_TRANSACTIONS
    }

    public static final String FAUNUS_GRAPH_OUTPUT_BLUEPRINTS_TX_COMMIT = "faunus.graph.output.blueprints.tx-commit";
    // some random property that will 'never' be used by anyone
    public static final String BLUEPRINTS_ID = "_bId0192834";

    public static Graph generateGraph(final Configuration config) {
        final Class<? extends OutputFormat> format = config.getClass(FaunusGraph.FAUNUS_GRAPH_OUTPUT_FORMAT, OutputFormat.class, OutputFormat.class);
        if (TitanOutputFormat.class.isAssignableFrom(format)) {
            return GraphFactory.generateGraph(config, TitanOutputFormat.FAUNUS_GRAPH_OUTPUT_TITAN);
        } else {
            // TODO: this is where Rexster can come into play here
            throw new RuntimeException("The provide graph output format is not supported: " + format.getName());
        }
    }

    ////////////// MAP/REDUCE WORK FROM HERE ON OUT

    // WRITE ALL THE VERTICES AND THEIR PROPERTIES
    public static class Map extends Mapper<NullWritable, FaunusVertex, LongWritable, Holder<FaunusVertex>> {

        Graph graph;
        private long mutations = 0;
        private long commitTx = 5000;

        private final Holder<FaunusVertex> vertexHolder = new Holder<FaunusVertex>();
        private final LongWritable longWritable = new LongWritable();
        private final FaunusVertex shellVertex = new FaunusVertex();

        @Override
        public void setup(final Mapper.Context context) throws IOException, InterruptedException {
            this.graph = BlueprintsGraphOutputMapReduce.generateGraph(context.getConfiguration());
            this.commitTx = context.getConfiguration().getLong(FAUNUS_GRAPH_OUTPUT_BLUEPRINTS_TX_COMMIT, 5000);
        }

        @Override
        public void map(final NullWritable key, final FaunusVertex value, final Mapper<NullWritable, FaunusVertex, LongWritable, Holder<FaunusVertex>>.Context context) throws IOException, InterruptedException {

            // Write FaunusVertex (and respective properties) to Blueprints Graph
            final Vertex blueprintsVertex = this.graph.addVertex(null);
            context.getCounter(Counters.VERTICES_WRITTEN).increment(1l);
            for (final String property : value.getPropertyKeys()) {
                blueprintsVertex.setProperty(property, value.getProperty(property));
                context.getCounter(Counters.VERTEX_PROPERTIES_WRITTEN).increment(1l);
            }

            // Propagate shell vertices with Blueprints ids
            this.shellVertex.reuse(value.getIdAsLong());
            this.shellVertex.setProperty(BLUEPRINTS_ID, blueprintsVertex.getId());
            // TODO: Might need to be OUT for the sake of unidirectional edges in Titan
            for (final Edge faunusEdge : value.getEdges(IN)) {
                this.longWritable.set((Long) faunusEdge.getVertex(OUT).getId());
                context.write(this.longWritable, this.vertexHolder.set('s', this.shellVertex));
            }

            this.longWritable.set(value.getIdAsLong());
            value.getProperties().clear();  // no longer needed in reduce phase
            value.setProperty(BLUEPRINTS_ID, blueprintsVertex.getId()); // need this for id resolution in reduce phase
            value.removeEdges(Tokens.Action.DROP, IN); // no longer needed in reduce phase
            context.write(this.longWritable, this.vertexHolder.set('v', value));

            // after so many mutations, successfully commit the transaction (if graph is transactional)
            if (this.graph instanceof TransactionalGraph && (++this.mutations % this.commitTx) == 0) {
                try {
                    ((TransactionalGraph) graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                    context.getCounter(Counters.SUCCESSFUL_TRANSACTIONS).increment(1l);
                } catch (Exception e) {
                    context.getCounter(Counters.FAILED_TRANSACTIONS).increment(1l);
                }
            }
        }

        @Override
        public void cleanup(final Mapper<NullWritable, FaunusVertex, LongWritable, Holder<FaunusVertex>>.Context context) throws IOException, InterruptedException {
            try {
                if (this.graph instanceof TransactionalGraph) {
                    ((TransactionalGraph) this.graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                    context.getCounter(Counters.SUCCESSFUL_TRANSACTIONS).increment(1l);
                }
            } catch (Exception e) {
                context.getCounter(Counters.FAILED_TRANSACTIONS).increment(1l);
            }
            this.graph.shutdown();
        }
    }

    // WRITE ALL THE EDGES CONNECTING THE VERTICES
    public static class Reduce extends Reducer<LongWritable, Holder<FaunusVertex>, NullWritable, FaunusVertex> {

        Graph graph;
        private long mutations = 0;
        private long commitTx = 5000;
        private final static FaunusVertex DEAD_FAUNUS_VERTEX = new FaunusVertex();

        @Override
        public void setup(final Reduce.Context context) throws IOException, InterruptedException {
            this.graph = BlueprintsGraphOutputMapReduce.generateGraph(context.getConfiguration());
            this.commitTx = context.getConfiguration().getLong(FAUNUS_GRAPH_OUTPUT_BLUEPRINTS_TX_COMMIT, 5000);
        }

        @Override
        public void reduce(final LongWritable key, final Iterable<Holder<FaunusVertex>> values, final Reducer<LongWritable, Holder<FaunusVertex>, NullWritable, FaunusVertex>.Context context) throws IOException, InterruptedException {
            FaunusVertex faunusVertex = null;
            // generate a map of the faunus id with the blueprints id for all shell vertices (vertices incoming adjacent)
            final java.util.Map<Long, Object> faunusBlueprintsIdMap = new HashMap<Long, Object>();
            for (final Holder<FaunusVertex> holder : values) {
                if (holder.getTag() == 's') {
                    faunusBlueprintsIdMap.put(holder.get().getIdAsLong(), holder.get().getProperty(BLUEPRINTS_ID));
                } else {
                    final FaunusVertex toClone = holder.get();
                    faunusVertex = new FaunusVertex(toClone.getIdAsLong());
                    faunusVertex.setProperty(BLUEPRINTS_ID, toClone.getProperty(BLUEPRINTS_ID));
                    faunusVertex.addEdges(OUT, toClone);
                }
            }
            // this means that the vertex receiving adjacent vertex messages wasn't created
            if (null != faunusVertex) {
                final Object blueprintsId = faunusVertex.getProperty(BLUEPRINTS_ID);
                Vertex blueprintsVertex = null;
                if (null != blueprintsId)
                    blueprintsVertex = this.graph.getVertex(blueprintsId);
                // this means that an adjacent vertex to this vertex wasn't created
                if (null != blueprintsVertex) {
                    for (final Edge faunusEdge : faunusVertex.getEdges(OUT)) {
                        final Object otherId = faunusBlueprintsIdMap.get(faunusEdge.getVertex(IN).getId());
                        Vertex otherVertex = null;
                        if (null != otherId)
                            otherVertex = this.graph.getVertex(otherId);
                        if (null != otherVertex) {
                            final Edge blueprintsEdge = this.graph.addEdge(null, blueprintsVertex, otherVertex, faunusEdge.getLabel());
                            context.getCounter(Counters.EDGES_WRITTEN).increment(1l);
                            for (final String property : faunusEdge.getPropertyKeys()) {
                                blueprintsEdge.setProperty(property, faunusEdge.getProperty(property));
                                context.getCounter(Counters.EDGE_PROPERTIES_WRITTEN).increment(1l);
                            }
                            // after so many mutations, successfully commit the transaction (if graph is transactional)
                            // for titan, if the transaction is committed, need to 'reget' the vertex
                            if (this.graph instanceof TransactionalGraph && (++this.mutations % this.commitTx) == 0) {
                                try {
                                    ((TransactionalGraph) graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                                    context.getCounter(Counters.SUCCESSFUL_TRANSACTIONS).increment(1l);
                                } catch (Exception e) {
                                    context.getCounter(Counters.FAILED_TRANSACTIONS).increment(1l);
                                }
                                blueprintsVertex = this.graph.getVertex(blueprintsVertex.getId());
                            }
                        } else {
                            context.getCounter(Counters.NULL_VERTEX_EDGES_IGNORED).increment(1l);
                        }
                    }
                } else {
                    context.getCounter(Counters.NULL_VERTICES_IGNORED).increment(1l);
                }
            } else {
                context.getCounter(Counters.NULL_VERTICES_IGNORED).increment(1l);
            }

            // the emitted vertex is not complete -- assuming this is the end of the stage and vertex is dead
            context.write(NullWritable.get(), DEAD_FAUNUS_VERTEX);
        }

        @Override
        public void cleanup(final Reducer<LongWritable, Holder<FaunusVertex>, NullWritable, FaunusVertex>.Context context) throws IOException, InterruptedException {
            try {
                if (this.graph instanceof TransactionalGraph) {
                    ((TransactionalGraph) this.graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                    context.getCounter(Counters.SUCCESSFUL_TRANSACTIONS).increment(1l);
                }
            } catch (Exception e) {
                context.getCounter(Counters.FAILED_TRANSACTIONS).increment(1l);
            }
            this.graph.shutdown();
        }
    }
}