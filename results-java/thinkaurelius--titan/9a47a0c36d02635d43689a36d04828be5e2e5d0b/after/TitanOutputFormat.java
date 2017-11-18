package com.thinkaurelius.titan.hadoop.formats.titan;

import com.thinkaurelius.titan.hadoop.FaunusVertex;
import com.thinkaurelius.titan.hadoop.Holder;
import com.thinkaurelius.titan.hadoop.formats.MapReduceFormat;
import com.thinkaurelius.titan.hadoop.formats.noop.NoOpOutputFormat;
import com.thinkaurelius.titan.hadoop.mapreduce.FaunusCompiler;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TitanOutputFormat extends NoOpOutputFormat implements MapReduceFormat {

    public static final String FAUNUS_GRAPH_OUTPUT_TITAN = "faunus.graph.output.titan";
    public static final String FAUNUS_GRAPH_OUTPUT_TITAN_INFER_SCHEMA = "faunus.graph.output.titan.infer-schema";

    @Override
    public void addMapReduceJobs(final FaunusCompiler compiler) {
        if (compiler.getConf().getBoolean(FAUNUS_GRAPH_OUTPUT_TITAN_INFER_SCHEMA, true)) {
            compiler.addMapReduce(SchemaInferencerMapReduce.Map.class,
                    null,
                    SchemaInferencerMapReduce.Reduce.class,
                    LongWritable.class,
                    FaunusVertex.class,
                    NullWritable.class,
                    FaunusVertex.class,
                    SchemaInferencerMapReduce.createConfiguration());
        }
        compiler.addMapReduce(TitanGraphOutputMapReduce.VertexMap.class,
                null,
                TitanGraphOutputMapReduce.Reduce.class,
                LongWritable.class,
                Holder.class,
                NullWritable.class,
                FaunusVertex.class,
                TitanGraphOutputMapReduce.createConfiguration());
        compiler.addMap(TitanGraphOutputMapReduce.EdgeMap.class,
                NullWritable.class,
                FaunusVertex.class,
                TitanGraphOutputMapReduce.createConfiguration());
    }
}