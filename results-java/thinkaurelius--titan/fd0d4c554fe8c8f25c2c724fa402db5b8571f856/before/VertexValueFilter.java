package com.thinkaurelius.faunus.mapreduce.derivations;

import com.thinkaurelius.faunus.FaunusVertex;
import com.thinkaurelius.faunus.Holder;
import com.thinkaurelius.faunus.Tokens;
import com.thinkaurelius.faunus.mapreduce.ElementChecker;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexValueFilter {

    public static final String KEY = Tokens.makeNamespace(VertexValueFilter.class) + ".key";
    public static final String VALUES = Tokens.makeNamespace(VertexValueFilter.class) + ".values";
    public static final String VALUE_CLASS = Tokens.makeNamespace(VertexValueFilter.class) + ".valueClass";
    public static final String COMPARE = Tokens.makeNamespace(VertexValueFilter.class) + ".compare";
    public static final String NULL_WILDCARD = Tokens.makeNamespace(VertexValueFilter.class) + ".nullWildcard";

    public enum Counters {
        VERTICES_KEPT,
        VERTICES_DROPPED
    }

    public static class Map extends Mapper<NullWritable, FaunusVertex, LongWritable, Holder<FaunusVertex>> {

        private ElementChecker elementChecker;
        private final Holder<FaunusVertex> holder = new Holder<FaunusVertex>();
        private final LongWritable longWritable = new LongWritable();

        @Override
        public void setup(final Mapper.Context context) throws IOException, InterruptedException {
            final String key = context.getConfiguration().get(KEY);
            final Class valueClass = context.getConfiguration().getClass(VALUE_CLASS, String.class);
            final String[] valueStrings = context.getConfiguration().getStrings(VALUES);
            final Object[] values = new Object[valueStrings.length];
            if (valueClass.equals(String.class)) {
                for (int i = 0; i < valueStrings.length; i++) {
                    values[i] = valueStrings[i];
                }
            } else if (Number.class.isAssignableFrom((valueClass))) {
                for (int i = 0; i < valueStrings.length; i++) {
                    values[i] = Float.valueOf(valueStrings[i]);
                }
            } else if (valueClass.equals(Boolean.class)) {
                for (int i = 0; i < valueStrings.length; i++) {
                    values[i] = Boolean.valueOf(valueStrings[i]);
                }
            } else {
                throw new IOException("Class " + valueClass + " is an unsupported value class");
            }

            final Query.Compare compare = Query.Compare.valueOf(context.getConfiguration().get(COMPARE));
            final Boolean nullIsWildcard = context.getConfiguration().getBoolean(NULL_WILDCARD, false);

            this.elementChecker = new ElementChecker(nullIsWildcard, key, compare, values);
        }

        @Override
        public void map(final NullWritable key, final FaunusVertex value, final Mapper<NullWritable, FaunusVertex, LongWritable, Holder<FaunusVertex>>.Context context) throws IOException, InterruptedException {

            if (this.elementChecker.isLegal(value)) {
                this.longWritable.set(value.getIdAsLong());
                context.write(this.longWritable, this.holder.set('v', value));
                context.getCounter(Counters.VERTICES_KEPT).increment(1l);
            } else {
                context.getCounter(Counters.VERTICES_DROPPED).increment(1l);
                this.holder.set('k', value.cloneId());
                for (final Edge edge : value.getEdges(OUT)) {
                    final Long id = (Long) edge.getVertex(IN).getId();
                    if (!id.equals(value.getId())) {
                        this.longWritable.set(id);
                        context.write(this.longWritable, this.holder);
                    }
                }
                for (final Edge edge : value.getEdges(IN)) {
                    final Long id = (Long) edge.getVertex(OUT).getId();
                    if (!id.equals(value.getId())) {
                        this.longWritable.set(id);
                        context.write(this.longWritable, this.holder);
                    }
                }
            }
        }
    }

    public static class Reduce extends VertexFilter.Reduce {
    }
}