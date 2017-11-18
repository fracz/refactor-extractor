package com.thinkaurelius.titan.hadoop.formats;

import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.hadoop.FaunusVertex;
import com.thinkaurelius.titan.hadoop.StandardFaunusEdge;
import com.thinkaurelius.titan.hadoop.Holder;
import com.thinkaurelius.titan.hadoop.compat.HadoopCompatLoader;
import com.thinkaurelius.titan.hadoop.mapreduce.util.EmptyConfiguration;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.util.ExceptionFactory;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeCopyMapReduce {

    public static final String TITAN_HADOOP_GRAPH_INPUT_EDGE_COPY_DIRECTION = "titan.hadoop.input.edge-copy.direction";

    public enum Counters {
        EDGES_COPIED,
        EDGES_ADDED
    }

    public static Configuration createConfiguration(final Direction direction) {
        final Configuration configuration = new EmptyConfiguration();
        configuration.setEnum(TITAN_HADOOP_GRAPH_INPUT_EDGE_COPY_DIRECTION, direction);
        return configuration;
    }

    public static class Map extends Mapper<NullWritable, FaunusVertex, LongWritable, Holder<FaunusVertex>> {

        private final Holder<FaunusVertex> vertexHolder = new Holder<FaunusVertex>();
        private final LongWritable longWritable = new LongWritable();
        private Direction direction = Direction.OUT;

        @Override
        public void setup(final Mapper.Context context) throws IOException, InterruptedException {
            this.direction = context.getConfiguration().getEnum(TITAN_HADOOP_GRAPH_INPUT_EDGE_COPY_DIRECTION, Direction.OUT);
            if (this.direction.equals(Direction.BOTH))
                throw new InterruptedException(ExceptionFactory.bothIsNotSupported().getMessage());
        }

        @Override
        public void map(final NullWritable key, final FaunusVertex value, final Mapper<NullWritable, FaunusVertex, LongWritable, Holder<FaunusVertex>>.Context context) throws IOException, InterruptedException {
            long edgesInverted = 0;
            for (final Edge edge : value.getEdges(this.direction)) {
                final long id = (Long) edge.getVertex(this.direction.opposite()).getId();
                this.longWritable.set(id);
                final FaunusVertex shellVertex = new FaunusVertex(context.getConfiguration(), id);
                shellVertex.addEdge(this.direction.opposite(), (StandardFaunusEdge) edge);
                context.write(this.longWritable, this.vertexHolder.set('s', shellVertex));
                edgesInverted++;
            }
            this.longWritable.set(value.getLongId());
            context.write(this.longWritable, this.vertexHolder.set('r', value));
            HadoopCompatLoader.getDefaultCompat().incrementContextCounter(context, Counters.EDGES_COPIED, edgesInverted);
            //context.getCounter(Counters.EDGES_COPIED).increment(edgesInverted);
        }

    }

    public static class Reduce extends Reducer<LongWritable, Holder<FaunusVertex>, NullWritable, FaunusVertex> {

        private Direction direction = Direction.OUT;

        @Override
        public void setup(final Reduce.Context context) throws IOException, InterruptedException {
            this.direction = context.getConfiguration().getEnum(TITAN_HADOOP_GRAPH_INPUT_EDGE_COPY_DIRECTION, Direction.OUT);
            if (this.direction.equals(Direction.BOTH))
                throw new InterruptedException(ExceptionFactory.bothIsNotSupported().getMessage());
        }

        @Override
        public void reduce(final LongWritable key, final Iterable<Holder<FaunusVertex>> values, final Reducer<LongWritable, Holder<FaunusVertex>, NullWritable, FaunusVertex>.Context context) throws IOException, InterruptedException {
            long edgesAggregated = 0;
            final FaunusVertex vertex = new FaunusVertex(context.getConfiguration(), key.get());
            for (final Holder<FaunusVertex> holder : values) {
                if (holder.getTag() == 's') {
                    edgesAggregated = edgesAggregated + Iterables.size(holder.get().getEdges(direction.opposite()));
                    vertex.addEdges(direction.opposite(), holder.get());
                } else {
                    vertex.addAll(holder.get());
                }
            }
            context.write(NullWritable.get(), vertex);
            HadoopCompatLoader.getDefaultCompat().incrementContextCounter(context, Counters.EDGES_ADDED, edgesAggregated);
            //context.getCounter(Counters.EDGES_ADDED).increment(edgesAggregated);
        }
    }
}