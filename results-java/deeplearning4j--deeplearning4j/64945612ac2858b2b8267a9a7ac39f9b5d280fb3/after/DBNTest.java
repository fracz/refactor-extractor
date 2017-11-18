package org.deeplearning4j.models.classifiers.dbn;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.datasets.fetchers.MnistDataFetcher;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.distributions.Distributions;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.models.featuredetectors.rbm.RBM;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.nd4j.linalg.api.activation.Activations;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.nn.WeightInit;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by agibsonccc on 8/28/14.
 */
public class DBNTest {

    private static Logger log = LoggerFactory.getLogger(DBNTest.class);



    @Test
    public void testIris() {
        RandomGenerator gen = new MersenneTwister(123);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .iterations(1)
                .weightInit(WeightInit.DISTRIBUTION).dist(Distributions.normal(gen, 1e-2))
                .activationFunction(Activations.tanh())
                .visibleUnit(RBM.VisibleUnit.GAUSSIAN).hiddenUnit(RBM.HiddenUnit.RECTIFIED)
                .lossFunction(LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY)
                .optimizationAlgo(NeuralNetwork.OptimizationAlgorithm.GRADIENT_DESCENT)
                .rng(gen)
                .learningRate(1e-2f)
                .nIn(4).nOut(3).list(2).hiddenLayerSizes(new int[]{3})
                .override(new NeuralNetConfiguration.ConfOverride() {
                    @Override
                    public void override(int i, NeuralNetConfiguration.Builder builder) {

                        if (i == 1) {
                            builder.weightInit(WeightInit.ZERO);
                            builder.activationFunction(Activations.softMaxRows());
                            builder.lossFunction(LossFunctions.LossFunction.MCXENT);

                        }
                    }
                }).build();



        DBN d = new DBN.Builder().layerWiseConfiguration(conf)
                .build();



        DataSetIterator iter = new IrisDataSetIterator(150, 150);

        DataSet next = iter.next(100);
        next.normalizeZeroMeanZeroUnitVariance();
        d.fit(next);

        Evaluation eval = new Evaluation();
        INDArray output = d.output(next.getFeatureMatrix());
        eval.eval(next.getLabels(),output);
        log.info("Score " + eval.stats());


    }

    @Test
    public void testDbn() throws IOException {
        RandomGenerator gen = new MersenneTwister(123);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().withActivationType(NeuralNetConfiguration.ActivationType.NET_ACTIVATION)
                .momentum(9e-1f).weightInit(WeightInit.DISTRIBUTION).dist(Distributions.normal(gen,1e-1))
                .lossFunction(LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY).rng(gen).iterations(10)
                .learningRate(1e-1f).nIn(784).nOut(10).list(4).hiddenLayerSizes(new int[]{500, 250, 200}).override(new NeuralNetConfiguration.ConfOverride() {
                    @Override
                    public void override(int i, NeuralNetConfiguration.Builder builder) {
                        if(i == 3) {
                            builder.weightInit(WeightInit.ZERO);
                            builder.activationFunction(Activations.softMaxRows());
                            builder.lossFunction(LossFunctions.LossFunction.MCXENT);

                        }
                    }
                })
                .build();



        DBN d = new DBN.Builder().layerWiseConfiguration(conf)
                .build();

        NeuralNetConfiguration.setClassifier(d.getOutputLayer().conf());
        MnistDataFetcher fetcher = new MnistDataFetcher(true);
        fetcher.fetch(10);
        DataSet d2 = fetcher.next();
        d.fit(d2);


        INDArray predict2 = d.output(d2.getFeatureMatrix());

        Evaluation eval = new Evaluation();
        eval.eval(d2.getLabels(),predict2);
        log.info(eval.stats());
        int[] predict = d.predict(d2.getFeatureMatrix());
        log.info("Predict " + Arrays.toString(predict));


    }

}