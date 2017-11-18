package org.deeplearning4j.nn.api;

import java.io.Serializable;

import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.gradient.NeuralNetworkGradient;
import org.nd4j.linalg.learning.AdaGrad;
import org.deeplearning4j.optimize.api.IterationListener;
/**
 * Single layer neural network, this is typically one that has
 * the objective function of reconstruction the input: also called feature detectors
 * @author Adam Gibson
 *
 */
public interface NeuralNetwork extends Serializable,Cloneable,IterationListener,Model {



    /**
     * Optimization algorithm to use
     * @author Adam Gibson
     *
     */
    public static enum OptimizationAlgorithm {
        GRADIENT_DESCENT,CONJUGATE_GRADIENT,HESSIAN_FREE
    }


    /**
     * Clears the input from the neural net
     */
    public void clearInput();

    /**
     * Backprop with the output being the reconstruction
     * @param lr the learning rate to use
     * @param epochs the max number of epochs to run
     * @param extraParams implementation specific params
     */
    public void backProp(double lr,int epochs,Object[] extraParams);


    public INDArray getW();

    public  void setW(INDArray w);

    public  INDArray gethBias();

    public  void sethBias(INDArray hBias);

    public  INDArray getvBias();

    public  void setvBias(INDArray vBias);

    public  INDArray getInput();

    public  void setInput(INDArray input);


    INDArray hiddenActivation(INDArray input);


    INDArray hBiasMean();

    public AdaGrad getAdaGrad();
    public void setAdaGrad(AdaGrad adaGrad);



    public AdaGrad gethBiasAdaGrad();
    public void setHbiasAdaGrad(AdaGrad adaGrad);


    public AdaGrad getVBiasAdaGrad();
    public void setVBiasAdaGrad(AdaGrad adaGrad);


    public NeuralNetworkGradient getGradient(Object[] params);


    public NeuralNetwork transpose();
    public  NeuralNetwork clone();

    /**
     * Sample hidden mean and sample
     * given visible
     * @param v the  the visible input
     * @return a pair with mean, sample
     */
    public Pair<INDArray,INDArray> sampleHiddenGivenVisible(INDArray v);


    /**
     * Sample visible mean and sample
     * given hidden
     * @param h the  the hidden input
     * @return a pair with mean, sample
     */
    public Pair<INDArray,INDArray> sampleVisibleGivenHidden(INDArray h);


    void iterationDone(int iteration);


    /**
     * Performs a network merge in the form of
     * a += b - a / n
     * where a is a matrix here
     * b is a matrix on the incoming network
     * and n is the batch size
     * @param network the network to merge with
     * @param batchSize the batch size (number of training examples)
     * to average by
     */
    void merge(NeuralNetwork network,int batchSize);


    void setConf(NeuralNetConfiguration conf);

    /**
     * The configuration for the neural network
     * @return the configuration for the neural network
     */
    NeuralNetConfiguration conf();

    /**
     * Params with only hidden bias
     * @return
     */
    INDArray params();

    /**
     * Params with only hidden bias
     * @return
     */
    INDArray paramsWithVisible();


}