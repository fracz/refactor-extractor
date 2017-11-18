package com.thinkaurelius.faunus.mapreduce.steps;

import com.thinkaurelius.faunus.FaunusVertex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MapSequence {

    public static final String MAP_CLASSES = Tokens.makeNamespace(MapSequence.class) + ".mapClasses";

    public static class Map extends Mapper<NullWritable, FaunusVertex, NullWritable, FaunusVertex> {

        private List<Mapper<NullWritable, FaunusVertex, NullWritable, FaunusVertex>> mappers = new ArrayList<Mapper<NullWritable, FaunusVertex, NullWritable, FaunusVertex>>();
        private List<Method> mapperMethods = new ArrayList<Method>();

        @Override
        public void setup(final Mapper.Context context) throws IOException, InterruptedException {
            try {
                final MemoryMapContext memoryContext = new MemoryMapContext(context);
                final String[] classNames = context.getConfiguration().getStrings(MAP_CLASSES);
                for (int i = 0; i < classNames.length; i++) {
                    memoryContext.stageConfiguration(i);
                    final Class<Mapper<NullWritable, FaunusVertex, NullWritable, FaunusVertex>> mapClass = (Class) Class.forName(classNames[i]);
                    final Mapper<NullWritable, FaunusVertex, NullWritable, FaunusVertex> mapper = mapClass.getConstructor().newInstance();
                    try {
                        mapClass.getMethod(Tokens.SETUP, Mapper.Context.class).invoke(mapper, memoryContext);
                    } catch (NoSuchMethodException e) {
                        // there is no setup method and that is okay.
                    }
                    this.mappers.add(mapper);
                    this.mapperMethods.add(mapClass.getMethod(Tokens.MAP, NullWritable.class, FaunusVertex.class, Mapper.Context.class));
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }


        @Override
        public void map(final NullWritable key, final FaunusVertex value, final Mapper<NullWritable, FaunusVertex, NullWritable, FaunusVertex>.Context context) throws IOException, InterruptedException {
            try {
                final MemoryMapContext memoryContext = new MemoryMapContext(context);
                memoryContext.setCurrentValue(value);
                for (int i = 0; i < this.mappers.size(); i++) {
                    this.mapperMethods.get(i).invoke(this.mappers.get(i), key, memoryContext.getCurrentValue(), memoryContext);
                    if (null == memoryContext.getCurrentValue())
                        break;
                    memoryContext.reset();
                }

                final FaunusVertex vertex = memoryContext.getCurrentValue();
                if (null != vertex)
                    context.write(NullWritable.get(), vertex);
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        public class MemoryMapContext extends Mapper.Context {

            private FaunusVertex value;
            private Mapper.Context context;
            private boolean locked = false;
            private Configuration configuration;


            public MemoryMapContext(final Mapper.Context context) throws IOException, InterruptedException {
                super(context.getConfiguration(), context.getTaskAttemptID(), null, null, context.getOutputCommitter(), null, context.getInputSplit());
                this.context = context;
                this.configuration = context.getConfiguration();
            }

            @Override
            public void write(final Object key, final Object value) throws IOException, InterruptedException {
                this.value = (FaunusVertex) value;
            }

            @Override
            public FaunusVertex getCurrentValue() {
                return this.value;
            }

            @Override
            public NullWritable getCurrentKey() {
                return NullWritable.get();
            }

            @Override
            public boolean nextKeyValue() {
                if (this.locked)
                    return false;
                else {
                    this.locked = true;
                    return true;
                }
            }

            public void reset() {

                this.locked = false;
            }

            public void setCurrentValue(final FaunusVertex value) {
                this.value = value;
            }

            @Override
            public Counter getCounter(final String groupName, final String counterName) {
                return this.context.getCounter(groupName, counterName);
            }

            @Override
            public Counter getCounter(final Enum counterName) {
                return this.context.getCounter(counterName);
            }

            @Override
            public Configuration getConfiguration() {
                return this.configuration;
            }

            public void stageConfiguration(final int step) {
                final java.util.Map<String, String> temp = new HashMap<String, String>();
                for (final java.util.Map.Entry<String, String> entry : this.configuration) {
                    final String key = entry.getKey();
                    if (key.endsWith("-" + step)) {
                        temp.put(key.replace("-" + step, ""), entry.getValue());
                    }
                }
                for (final java.util.Map.Entry<String, String> entry : temp.entrySet()) {
                    this.configuration.set(entry.getKey(), entry.getValue());
                }
            }
        }
    }


}