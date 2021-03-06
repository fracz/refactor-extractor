package com.thinkaurelius.titan.hadoop.formats.titan.cassandra;

import com.thinkaurelius.titan.hadoop.HadoopVertex;
import com.thinkaurelius.titan.hadoop.formats.VertexQueryFilter;

import org.apache.cassandra.hadoop.ColumnFamilyRecordReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TitanCassandraRecordReader extends RecordReader<NullWritable, HadoopVertex> {

    private ColumnFamilyRecordReader reader;
    private TitanCassandraHadoopGraph graph;
    private VertexQueryFilter vertexQuery;
    private Configuration configuration;
    private HadoopVertex vertex;

    public TitanCassandraRecordReader(final TitanCassandraHadoopGraph graph, final VertexQueryFilter vertexQuery, final ColumnFamilyRecordReader reader) {
        this.graph = graph;
        this.vertexQuery = vertexQuery;
        this.reader = reader;
    }

    @Override
    public void initialize(final InputSplit inputSplit, final TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        this.reader.initialize(inputSplit, taskAttemptContext);
        this.configuration = taskAttemptContext.getConfiguration();
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        while (this.reader.nextKeyValue()) {
            // TODO titan05 integration -- the duplicate() call may be unnecessary
            final HadoopVertex temp = this.graph.readHadoopVertex(this.configuration, this.reader.getCurrentKey().duplicate(), this.reader.getCurrentValue());
            if (null != temp) {
                this.vertex = temp;
                this.vertexQuery.defaultFilter(this.vertex);
                return true;
            }
        }
        return false;
    }

    @Override
    public NullWritable getCurrentKey() throws IOException, InterruptedException {
        return NullWritable.get();
    }

    @Override
    public HadoopVertex getCurrentValue() throws IOException, InterruptedException {
        return this.vertex;
    }

    @Override
    public void close() throws IOException {
        this.graph.close();
        this.reader.close();
    }

    @Override
    public float getProgress() {
        return this.reader.getProgress();
    }
}