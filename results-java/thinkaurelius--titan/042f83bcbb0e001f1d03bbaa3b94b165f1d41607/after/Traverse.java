package com.thinkaurelius.faunus.mapreduce.steps;

import com.thinkaurelius.faunus.FaunusEdge;
import com.thinkaurelius.faunus.FaunusVertex;
import com.thinkaurelius.faunus.util.TaggedHolder;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Traverse {

    public static final String FIRST_DIRECTION = Tokens.makeNamespace(Traverse.class) + ".firstDirection";
    public static final String FIRST_LABEL = Tokens.makeNamespace(Traverse.class) + ".firstLabel";
    public static final String SECOND_DIRECTION = Tokens.makeNamespace(Traverse.class) + ".secondDirection";
    public static final String SECOND_LABEL = Tokens.makeNamespace(Traverse.class) + ".secondLabel";
    public static final String NEW_LABEL = Tokens.makeNamespace(Traverse.class) + ".newLabel";

    public enum Counters {
        EDGES_CREATED
    }

    public static class Map extends Mapper<NullWritable, FaunusVertex, LongWritable, TaggedHolder> {

        private Direction firstDirection;
        private String firstLabel;
        private Direction secondDirection;
        private String secondLabel;
        private String newLabel;

        @Override
        public void setup(final Mapper.Context context) throws IOException, InterruptedException {
            this.firstDirection = Direction.valueOf(context.getConfiguration().get(FIRST_DIRECTION));
            this.firstLabel = context.getConfiguration().get(FIRST_LABEL);
            this.secondDirection = Direction.valueOf(context.getConfiguration().get(SECOND_DIRECTION));
            this.secondLabel = context.getConfiguration().get(SECOND_LABEL);
            this.newLabel = context.getConfiguration().get(NEW_LABEL);
        }

        @Override
        public void map(final NullWritable key, final FaunusVertex value, final org.apache.hadoop.mapreduce.Mapper<NullWritable, FaunusVertex, LongWritable, TaggedHolder>.Context context) throws IOException, InterruptedException {
            final Set<Long> firstVertexIds = new HashSet<Long>();
            final Set<Long> secondVertexIds = new HashSet<Long>();

            // emit original incoming edges
            for (final Edge edge : value.getEdges(IN)) {
                context.write(value.getIdAsLongWritable(), new TaggedHolder<FaunusEdge>('i', (FaunusEdge) edge));
            }
            // emit original outgoing edges
            for (final Edge edge : value.getEdges(OUT)) {
                context.write(value.getIdAsLongWritable(), new TaggedHolder<FaunusEdge>('o', (FaunusEdge) edge));
            }

            if (firstDirection.equals(OUT)) {
                for (final Edge edge : value.getEdges(IN)) {
                    if (edge.getLabel().equals(this.firstLabel)) {
                        firstVertexIds.add((Long) edge.getVertex(OUT).getId());
                    }
                }
            } else if (firstDirection.equals(IN)) {
                for (final Edge edge : value.getEdges(OUT)) {
                    if (edge.getLabel().equals(this.firstLabel)) {
                        firstVertexIds.add((Long) edge.getVertex(IN).getId());
                    }
                }
            } else {
                throw new IOException("A direction of " + this.firstDirection + " is not a legal direction for this operation");
            }

            //////////

            if (secondDirection.equals(OUT)) {
                for (final Edge edge : value.getEdges(OUT)) {
                    if (edge.getLabel().equals(this.secondLabel)) {
                        secondVertexIds.add((Long) edge.getVertex(IN).getId());
                    }
                }
            } else if (secondDirection.equals(IN)) {
                for (final Edge edge : value.getEdges(IN)) {
                    if (edge.getLabel().equals(this.secondLabel)) {
                        secondVertexIds.add((Long) edge.getVertex(OUT).getId());
                    }
                }
            } else {
                throw new IOException("A direction of " + this.firstDirection + " is not a legal direction for this operation");
            }


            for (final Long firstId : firstVertexIds) {
                for (final Long secondId : secondVertexIds) {
                    final FaunusEdge edge = new FaunusEdge(new FaunusVertex(firstId), new FaunusVertex(secondId), this.newLabel);
                    context.write(new LongWritable(firstId), new TaggedHolder<FaunusEdge>('o', edge));
                    context.write(new LongWritable(secondId), new TaggedHolder<FaunusEdge>('i', edge));
                    context.getCounter(Counters.EDGES_CREATED).increment(2);
                }
            }

            context.write(value.getIdAsLongWritable(), new TaggedHolder<FaunusVertex>('v', value.cloneIdAndProperties()));
        }
    }

    public static class Reduce extends Reducer<LongWritable, TaggedHolder, NullWritable, FaunusVertex> {
        @Override
        public void reduce(final LongWritable key, final Iterable<TaggedHolder> values, final org.apache.hadoop.mapreduce.Reducer<LongWritable, TaggedHolder, NullWritable, FaunusVertex>.Context context) throws IOException, InterruptedException {
            final FaunusVertex vertex = new FaunusVertex(key.get());
            for (final TaggedHolder holder : values) {
                final char tag = holder.getTag();
                if (tag == 'v') {
                    vertex.setProperties(WritableUtils.clone(holder.get(), context.getConfiguration()).getProperties());
                } else if (tag == 'i') {
                    vertex.addEdge(IN, WritableUtils.clone((FaunusEdge) holder.get(), context.getConfiguration()));
                } else if (tag == 'o') {
                    vertex.addEdge(OUT, WritableUtils.clone((FaunusEdge) holder.get(), context.getConfiguration()));
                } else {
                    throw new IOException("A tag of " + tag + " is not a legal tag for this operation");
                }
            }
            context.write(NullWritable.get(), vertex);
        }
    }
}