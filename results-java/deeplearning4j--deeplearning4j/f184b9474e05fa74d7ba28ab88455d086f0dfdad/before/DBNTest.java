package org.deeplearning4j.dbn;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.berkeley.Triple;

import org.deeplearning4j.datasets.fetchers.MnistDataFetcher;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.SamplingDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.linalg.api.activation.Activations;
import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.dataset.DataSet;
import org.deeplearning4j.linalg.jblas.NDArray;
import org.deeplearning4j.nn.NeuralNetwork;
import org.deeplearning4j.rbm.RBM;
import org.jblas.DoubleMatrix;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class DBNTest {

    private static Logger log = LoggerFactory.getLogger(DBNTest.class);

    @Test
    public void testParams() {
        double preTrainLr = 0.01;
        int preTrainEpochs = 10000;
        int k = 1;
        int nIns = 4,nOuts = 3;
        int[] hiddenLayerSizes = new int[] {4,3,2};
        double fineTuneLr = 0.01;
        int fineTuneEpochs = 10000;


        DBN dbn = new DBN.Builder().withHiddenUnits(RBM.HiddenUnit.RECTIFIED)
                .withVisibleUnits(RBM.VisibleUnit.GAUSSIAN)
                .numberOfInputs(nIns).numberOfOutPuts(nOuts).withActivation(Activations.tanh())
                .hiddenLayerSizes(hiddenLayerSizes)
                .build();

        INDArray params = dbn.params();
        assertEquals(1,params.rows());
        assertEquals(params.columns(),params.length());
        dbn.setLabels(new NDArray(new DoubleMatrix(1,nOuts)));
        assertEquals(RBM.HiddenUnit.RECTIFIED,dbn.getHiddenUnit());
        assertEquals(RBM.VisibleUnit.GAUSSIAN,dbn.getVisibleUnit());
        RBM r = (RBM) dbn.getLayers()[0];
        assertEquals(RBM.HiddenUnit.RECTIFIED,r.getHiddenType());
        assertEquals(RBM.VisibleUnit.GAUSSIAN,r.getVisibleType());



    }

    @Test
    public void testParamLength() {
        double preTrainLr = 0.01;
        int preTrainEpochs = 10000;
        int k = 1;
        int nIns = 4,nOuts = 3;
        int[] hiddenLayerSizes = new int[] {4,3,2};
        double fineTuneLr = 0.01;
        int fineTuneEpochs = 10000;

        DBN dbn = new DBN.Builder().withHiddenUnits(RBM.HiddenUnit.RECTIFIED).withVisibleUnits(RBM.VisibleUnit.GAUSSIAN)
                .numberOfInputs(nIns).numberOfOutPuts(nOuts).withActivation(Activations.tanh())
                .hiddenLayerSizes(hiddenLayerSizes)
                .build();


        INDArray params = dbn.params();
        List<Pair<INDArray,INDArray>> unpacked = dbn.unPack(params);


    }


    @Test
    public void testDbnStructure() {
        RandomGenerator rng = new MersenneTwister(123);

        double preTrainLr = 0.01;
        int preTrainEpochs = 10000;
        int k = 1;
        int nIns = 4,nOuts = 3;
        int[] hiddenLayerSizes = new int[] {4,3,2};
        double fineTuneLr = 0.01;
        int fineTuneEpochs = 10000;

        DBN dbn = new DBN.Builder().withHiddenUnits(RBM.HiddenUnit.RECTIFIED).withVisibleUnits(RBM.VisibleUnit.GAUSSIAN)
                .numberOfInputs(nIns).numberOfOutPuts(nOuts).withActivation(Activations.tanh())
                .hiddenLayerSizes(hiddenLayerSizes)
                .withRng(rng)
                .build();



        DataSetIterator iter = new IrisDataSetIterator(150, 150);

        DataSet next = iter.next(150);
        next.shuffle();
        dbn.init();
        dbn.feedForward(next.getFeatureMatrix());

        for(int i = 0; i < dbn.getnLayers(); i++) {
            assertEquals(dbn.getLayers()[i].gethBias(),dbn.getSigmoidLayers()[i].getB());
        }

    }

    @Test
    public void testLFW() throws Exception {
        //batches of 10, 60000 examples total
        DataSetIterator iter = new LFWDataSetIterator(10,10,14,14);

        //784 input (number of columns in mnist, 10 labels (0-9), no regularization
        DBN dbn = new DBN.Builder().withHiddenUnits(RBM.HiddenUnit.RECTIFIED).withVisibleUnits(RBM.VisibleUnit.GAUSSIAN)
                .hiddenLayerSizes(new int[]{250, 150, 100}).withOptimizationAlgorithm(NeuralNetwork.OptimizationAlgorithm.CONJUGATE_GRADIENT)
                .numberOfInputs(iter.inputColumns()).numberOfOutPuts(iter.totalOutcomes())
                .build();

        while(iter.hasNext()) {
            DataSet next = iter.next();
            dbn.pretrain(next.getFeatureMatrix(), 1, 1e-5, 1000);
        }

        iter.reset();
        while(iter.hasNext()) {
            DataSet next = iter.next();
            dbn.setInput(next.getFeatureMatrix());
            dbn.finetune(next.getLabels(), 1e-4, 1000);
        }



        iter.reset();

        Evaluation eval = new Evaluation();

        while(iter.hasNext()) {
            DataSet next = iter.next();
            INDArray predict = dbn.output(next.getFeatureMatrix());
            INDArray labels = next.getLabels();
            eval.eval(labels, predict);
        }

        log.info("Prediction f scores and accuracy");
        log.info(eval.stats());
    }


    @Test
    public void testIris() {
        RandomGenerator rng = new MersenneTwister(123);

        double preTrainLr = 0.01;
        int preTrainEpochs = 10000;
        int k = 1;
        int nIns = 4,nOuts = 3;
        int[] hiddenLayerSizes = new int[] {4,3,3};
        double fineTuneLr = 0.01;
        int fineTuneEpochs = 10000;

        DBN dbn = new DBN.Builder().withHiddenUnits(RBM.HiddenUnit.RECTIFIED).withVisibleUnits(RBM.VisibleUnit.GAUSSIAN)
                .numberOfInputs(nIns).numberOfOutPuts(nOuts).withActivation(Activations.hardTanh())
                .hiddenLayerSizes(hiddenLayerSizes)
                .withRng(rng)
                .build();



        DataSetIterator iter = new IrisDataSetIterator(150, 150);

        DataSet next = iter.next(150);
        next.normalizeZeroMeanZeroUnitVariance();
        next.shuffle();

        List<DataSet> finetuneBatches = next.dataSetBatches(10);



        DataSetIterator sampling = new SamplingDataSetIterator(next, 150, 3);

        List<DataSet> miniBatches = new ArrayList<DataSet>();

        while(sampling.hasNext()) {
            next = sampling.next();
            miniBatches.add(next.copy());
        }

        log.info("Training on " + miniBatches.size() + " minibatches");

        for(int i = 0; i < miniBatches.size(); i++) {
            DataSet curr = miniBatches.get(i);
            dbn.pretrain(curr.getFeatureMatrix(), k, preTrainLr, preTrainEpochs);

        }





        Evaluation eval = new Evaluation();


        for(int i = 0; i < miniBatches.size(); i++) {
            DataSet curr = miniBatches.get(i);
            dbn.setInput(curr.getFeatureMatrix());
            dbn.finetune(curr.getLabels(),fineTuneLr, fineTuneEpochs);

        }






        for(int i = 0; i < miniBatches.size(); i++) {
            DataSet test = miniBatches.get(i);
            INDArray predicted = dbn.output(test.getFeatureMatrix());
            INDArray real = test.getLabels();


            eval.eval(real, predicted);

        }

        log.info("Evaled " + eval.stats());


    }


    @Test
    public void testMnist() throws IOException {
        MnistDataFetcher fetcher = new MnistDataFetcher(true);
        fetcher.fetch(100);
        DataSet d = fetcher.next();
        d.filterAndStrip(new int[]{0,1});
        log.info("Training on " + d.numExamples());
        StopWatch watch = new StopWatch();

        log.info("Data applyTransformToDestination " + d);

        DBN dbn = new DBN.Builder()
                .withActivation(Activations.sigmoid())
                .hiddenLayerSizes(new int[]{500,250,100})
                .withMomentum(0.5).normalizeByInputRows(true)
                .numberOfInputs(784).useAdaGrad(true)
                .numberOfOutPuts(2)
                .build();

        watch.start();

        dbn.pretrain(d.getFeatureMatrix(), 1, 1e-2, 300);
        dbn.finetune(d.getLabels(), 1e-2, 100);

        watch.stop();

        log.info("Took " + watch.getTime());
        Evaluation eval = new Evaluation();
        INDArray predict = dbn.output(d.getFeatureMatrix());
        eval.eval(d.getLabels(), predict);
        log.info(eval.stats());
    }

    @Test
    public void testROPerator() throws Exception {
        MnistDataFetcher fetcher = new MnistDataFetcher(true);
        fetcher.fetch(100);
        DataSet d = fetcher.next();

        DBN dbn = new DBN.Builder()
                .withActivation(Activations.sigmoid())
                .hiddenLayerSizes(new int[]{500,250,100})
                .withMomentum(0.5).normalizeByInputRows(true)
                .numberOfInputs(784).useAdaGrad(true)
                .numberOfOutPuts(2)
                .build();

        dbn.setInput(d.getFeatureMatrix());
        INDArray params = dbn.params();
        List<INDArray> activations = dbn.feedForwardR(params);
        List<INDArray> activationsNorm = dbn.feedForward();
        assertEquals(activations.size(),activationsNorm.size());
    }



}