/*
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.nn.layers.feedforward.rbm;

import java.util.Arrays;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.datasets.fetchers.IrisDataFetcher;
import org.deeplearning4j.datasets.fetchers.MnistDataFetcher;
import org.deeplearning4j.datasets.iterator.impl.LFWDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.layers.factory.LayerFactories;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ComposableIterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.plot.iterationlistener.NeuralNetPlotterIterationListener;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;



/**
 * Created by agibsonccc on 8/27/14.
 */
public class RBMTests {
    private static final Logger log = LoggerFactory.getLogger(RBMTests.class);

    @Test
    public void testLfw() {
        LFWDataSetIterator iter = new LFWDataSetIterator(10,10,28,28);
        DataSet d = iter.next();

        d.normalizeZeroMeanZeroUnitVariance();

        int nOut = 600;

        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
                .hiddenUnit(org.deeplearning4j.nn.conf.layers.RBM.HiddenUnit.RECTIFIED).weightInit(WeightInit.VI)
                .visibleUnit(org.deeplearning4j.nn.conf.layers.RBM.VisibleUnit.GAUSSIAN)
                .layer(new org.deeplearning4j.nn.conf.layers.RBM())
                .optimizationAlgo(OptimizationAlgorithm.ITERATION_GRADIENT_DESCENT)
                .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .learningRate(1e-3f)
                .nIn(d.numInputs()).nOut(nOut).build();

        RBM rbm = LayerFactories.getFactory(conf)
                .create(conf, Arrays.<IterationListener>asList(new ScoreIterationListener(1)));

        rbm.fit(d.getFeatureMatrix());

    }

    @Test
    public void testIrisGaussianHidden() {
        IrisDataFetcher fetcher = new IrisDataFetcher();
        fetcher.fetch(150);
        DataSet d = fetcher.next();
        d.normalizeZeroMeanZeroUnitVariance();

        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder().lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .visibleUnit(org.deeplearning4j.nn.conf.layers.RBM.VisibleUnit.GAUSSIAN).hiddenUnit(org.deeplearning4j.nn.conf.layers.RBM.HiddenUnit.GAUSSIAN).learningRate(1e-1f)
                .nIn(d.numInputs()).nOut(3)
                .layer(new org.deeplearning4j.nn.conf.layers.RBM())
                .build();

        RBM r = LayerFactories.getFactory(conf).create(conf);
        r.fit(d.getFeatureMatrix());

    }


    @Test
    public void testIris() {
        IrisDataFetcher fetcher = new IrisDataFetcher();
        fetcher.fetch(150);
        DataSet d = fetcher.next();
        d.normalizeZeroMeanZeroUnitVariance();

        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder().lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .visibleUnit(org.deeplearning4j.nn.conf.layers.RBM.VisibleUnit.GAUSSIAN).hiddenUnit(org.deeplearning4j.nn.conf.layers.RBM.HiddenUnit.RECTIFIED).learningRate(1e-1f)
                .nIn(d.numInputs()).nOut(3)
                .layer(new org.deeplearning4j.nn.conf.layers.RBM())
                .build();

        RBM r = LayerFactories.getFactory(conf).create(conf);
        r.fit(d.getFeatureMatrix());

    }

    @Test
    public void testBasic() {
        float[][] data = new float[][]
                {
                        {1,1,1,0,0,0},
                        {1,0,1,0,0,0},
                        {1,1,1,0,0,0},
                        {0,0,1,1,1,0},
                        {0,0,1,1,0,0},
                        {0,0,1,1,1,0},
                        {0,0,1,1,1,0}
                };

        INDArray input = Nd4j.create(data);

        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
                .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .learningRate(1e-1f).nIn(6).nOut(4).layer(new org.deeplearning4j.nn.conf.layers.RBM()).build();
        RBM rbm = LayerFactories.getFactory(conf).create(conf);
        rbm.fit(input);

        assertEquals(24, rbm.gradient().getGradientFor("W").length());
    }

    @Test
    public void testMnist() throws Exception {
        MnistDataFetcher fetcher = new MnistDataFetcher(true);
        RandomGenerator gen = new MersenneTwister(123);
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;

        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
                .iterations(30).constrainGradientToUnitNorm(true).weightInit(WeightInit.DISTRIBUTION).dist(new NormalDistribution(1, 1e-5))
                .optimizationAlgo(OptimizationAlgorithm.ITERATION_GRADIENT_DESCENT)
                .lossFunction(LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY)
                .learningRate(1e-1f).nIn(784).nOut(600)
                .layer(new org.deeplearning4j.nn.conf.layers.RBM())
                .build();

        fetcher.fetch(10);
        DataSet d2 = fetcher.next();

        org.nd4j.linalg.api.rng.distribution.Distribution dist = Nd4j.getDistributions().createNormal(1, 1e-5);
        System.out.println(dist.sample(new int[]{conf.getNIn(), conf.getNOut()}));

        INDArray input = d2.getFeatureMatrix();

        RBM rbm = LayerFactories.getFactory(conf).create(conf,
                Arrays.<IterationListener>asList(new ComposableIterationListener(new NeuralNetPlotterIterationListener(10), new ScoreIterationListener(5))));

        rbm.fit(input);

    }

    @Test
    public void testSetGetParams() {
        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
                .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .learningRate(1e-1f).nIn(6).nOut(4)
                .layer(new org.deeplearning4j.nn.conf.layers.RBM())
                .build();

        RBM rbm = LayerFactories.getFactory(conf).create(conf);
        INDArray rand2 = Nd4j.rand(new int[]{1, rbm.numParams()});
        rbm.setParams(rand2);
        INDArray getParams = rbm.params();
        assertEquals(rand2,getParams);
    }

    @Test
    public void testCg() {
        float[][] data = new float[][]
                {
                        {1,1,1,0,0,0},
                        {1,0,1,0,0,0},
                        {1,1,1,0,0,0},
                        {0,0,1,1,1,0},
                        {0,0,1,1,0,0},
                        {0,0,1,1,1,0},
                        {0,0,1,1,1,0}
                };

        INDArray input = Nd4j.create(data);

        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
                .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .learningRate(1e-1f).nIn(6).nOut(4)
                .layer(new org.deeplearning4j.nn.conf.layers.RBM())
                .build();
        RBM rbm = LayerFactories.getFactory(conf).create(conf);
        double value = rbm.score();
        rbm.fit(input);
        value = rbm.score();

    }

    @Test
    public void testGradient() {
        float[][] data = new float[][]
                {
                        {1,1,1,0,0,0},
                        {1,0,1,0,0,0},
                        {1,1,1,0,0,0},
                        {0,0,1,1,1,0},
                        {0,0,1,1,0,0},
                        {0,0,1,1,1,0},
                        {0,0,1,1,1,0}
                };


        INDArray input = Nd4j.create(data);

        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
                .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .learningRate(1e-1f).nIn(6).nOut(4)
                .layer(new org.deeplearning4j.nn.conf.layers.RBM())
                .build();

        RBM rbm = LayerFactories.getFactory(conf).create(conf);

        rbm.fit(input);
        double value = rbm.score();

        Gradient grad2 = rbm.gradient();

    }



}