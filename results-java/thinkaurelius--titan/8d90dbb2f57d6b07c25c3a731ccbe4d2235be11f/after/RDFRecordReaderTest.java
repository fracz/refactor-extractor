package com.thinkaurelius.titan.hadoop.formats.edgelist.rdf;

import com.thinkaurelius.titan.hadoop.BaseTest;
import com.thinkaurelius.titan.hadoop.compat.HadoopCompatLoader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RDFRecordReaderTest extends BaseTest {

    public void testRecordReader() throws Exception {
        Configuration conf = new Configuration();
        conf.set(RDFInputFormat.TITAN_HADOOP_GRAPH_INPUT_RDF_FORMAT, "n-triples");
        RDFRecordReader reader = new RDFRecordReader(conf);
        reader.initialize(new FileSplit(new Path(RDFRecordReaderTest.class.getResource("graph-example-1.ntriple").toURI()), 0, Long.MAX_VALUE, new String[]{}),
                HadoopCompatLoader.getCompat().newTask(conf, new TaskAttemptID()));
        int counter = 0;
        while (reader.nextKeyValue()) {
            assertEquals(reader.getCurrentKey(), NullWritable.get());
            // reader.getCurrentValue();
            counter++;

        }
        assertEquals(counter, 18 * 3);
        reader.close();
    }
}