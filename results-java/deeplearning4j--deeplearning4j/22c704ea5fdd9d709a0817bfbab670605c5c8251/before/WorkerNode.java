package org.deeplearning4j.iterativereduce.impl.single;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.deeplearning4j.iterativereduce.impl.ParameterVectorUpdateable;
import org.deeplearning4j.iterativereduce.runtime.ComputableWorker;
import org.deeplearning4j.iterativereduce.runtime.io.RecordParser;
import org.deeplearning4j.iterativereduce.runtime.io.TextRecordParser;
import org.deeplearning4j.iterativereduce.runtime.yarn.appworker.ApplicationWorker;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.scaleout.conf.DeepLearningConfigurable;
import org.nd4j.linalg.dataset.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Base IterativeReduce worker node
 *
 * @author josh
 *
 */
public class WorkerNode implements ComputableWorker<ParameterVectorUpdateable>,DeepLearningConfigurable {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerNode.class);
    private Layer neuralNetwork;
    private RecordParser recordParser;


    /**
     * Run a training pass of a single batch of input records on the DBN
     *
     * TODO
     * - dileneate between pre-train and finetune pass through data
     * 		- how?
     *
     * - app.iteration.count
     * 		- indicates how many times we're going to call the workers
     *
     * - tv.floe.metronome.dbn.conf.batchSize=10
     * 		- indicates that we're going to only process 10 records in a call to a worker
     *
     * - we could either
     *
     * 		1. make a complete pass through the batches in a split between iterations
     *
     * 			- tends to skew away from good solutions
     *
     * 		2. parameter average between batches
     *
     *			-	better quality, but more network overhead
     *
     * - if we paramete avg between batches, then our passes over the dataset become
     *
     * 		- total number of examples / batch size
     *
     * - might be pragmatic to let a command line tool calculate iterations
     *
     * 		- given we need to know how many fine tune passes to make as well
     *
     *
     *
     *
     *
     *
     *
     *
     */
    @Override
    public ParameterVectorUpdateable compute() {
        while(recordParser.hasMoreRecords()) {
            DataSet params = (DataSet) recordParser.nextRecord();
            neuralNetwork.fit(params.getFeatureMatrix());
        }

        return new ParameterVectorUpdateable(neuralNetwork.params());
    }



    @Override
    public ParameterVectorUpdateable compute(List<ParameterVectorUpdateable> arg0) {

        return compute();

    }

    @Override
    public ParameterVectorUpdateable getResults() {
        return new ParameterVectorUpdateable(neuralNetwork.params());
    }

    /**
     * TODO: re-wire this to read blocks of records into a Matrix
     *
     */
    @Override
    public void setRecordParser(RecordParser lineParser) {
        this.recordParser = lineParser;

    }

    /**
     * Setup the local DBN instance based on conf params
     *
     */
    @Override
    public void setup(Configuration conf) {
        NeuralNetConfiguration conf2 = NeuralNetConfiguration.fromJson(conf.get(NEURAL_NET_CONF));

        neuralNetwork = conf2.getLayerFactory().create(conf2);


    }

    /**
     * Collect the update from the master node and apply it to the local
     * parameter vector
     *
     * TODO: check the state changes of the incoming message!
     *
     */
    @Override
    public void update(ParameterVectorUpdateable masterUpdateUpdateable) {
        neuralNetwork.setParams(masterUpdateUpdateable.get());
    }




    public static void main(String[] args) throws Exception {

        TextRecordParser parser = new TextRecordParser();
        WorkerNode wn = new WorkerNode();
        ApplicationWorker<ParameterVectorUpdateable> aw = new ApplicationWorker<>(parser, wn, ParameterVectorUpdateable.class);

        ToolRunner.run(aw, args);

    }


    @Override
    public void setup(org.deeplearning4j.scaleout.conf.Configuration conf) {

    }
}