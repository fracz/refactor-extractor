package com.thinkaurelius.faunus.mapreduce.steps;

import com.thinkaurelius.faunus.FaunusVertex;
import com.thinkaurelius.faunus.util.Tokens;
import com.tinkerpop.blueprints.Edge;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Iterator;

import static com.tinkerpop.blueprints.Direction.BOTH;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Self {

    public static final String ACTION = Tokens.makeNamespace(Self.class) + ".action";

    public enum Counters {
        EDGES_KEPT,
        EDGES_DROPPED
    }

    public static class Map extends Mapper<NullWritable, FaunusVertex, NullWritable, FaunusVertex> {
        private Tokens.Action action;

        @Override
        public void setup(final Mapper.Context context) throws IOException, InterruptedException {
            this.action = Tokens.Action.valueOf(context.getConfiguration().get(ACTION));
        }

        @Override
        public void map(final NullWritable key, final FaunusVertex value, final Mapper<NullWritable, FaunusVertex, NullWritable, FaunusVertex>.Context context) throws IOException, InterruptedException {
            long keptCounter = 0l;
            long droppedCounter = 0l;

            final Iterator<Edge> itty = value.getEdges(BOTH).iterator();
            while (itty.hasNext()) {
                final Edge edge = itty.next();
                if (action.equals(Tokens.Action.KEEP)) {
                    if (!edge.getVertex(IN).getId().equals(edge.getVertex(OUT).getId())) {
                        itty.remove();
                        droppedCounter++;
                    } else {
                        keptCounter++;
                    }
                } else {
                    if (edge.getVertex(IN).getId().equals(edge.getVertex(OUT).getId())) {
                        itty.remove();
                        droppedCounter++;
                    } else {
                        keptCounter++;
                    }
                }
            }

            context.getCounter(Counters.EDGES_KEPT).increment(keptCounter);
            context.getCounter(Counters.EDGES_DROPPED).increment(droppedCounter);
            context.write(NullWritable.get(), value);
        }

    }
}