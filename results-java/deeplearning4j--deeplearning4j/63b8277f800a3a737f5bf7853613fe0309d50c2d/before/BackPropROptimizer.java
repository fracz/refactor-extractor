package org.deeplearning4j.optimize;

import cc.mallet.optimize.Optimizable;
import org.deeplearning4j.nn.BaseMultiLayerNetwork;
import org.deeplearning4j.nn.BaseMultiLayerNetwork.ParamRange;
import org.deeplearning4j.nn.NeuralNetwork;
import org.jblas.DoubleMatrix;
import org.jblas.ranges.RangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Optimizes via back prop gradients with
 * the r operator, used in hessian free operators
 * @author Adam Gibson
 */
public class BackPropROptimizer implements Optimizable.ByGradientValue,Serializable,OptimizableByGradientValueMatrix {

    private BaseMultiLayerNetwork network;
    private int length = -1;
    private double lr  = 1e-1;
    private int epochs = 1000;
    private static Logger log = LoggerFactory.getLogger(BackPropROptimizer.class);
    private int currentIteration = -1;
    private StochasticHessianFree h;


    public BackPropROptimizer(BaseMultiLayerNetwork network, double lr, int epochs) {
        this.network = network;
        this.lr = lr;
        this.epochs = epochs;
    }

    @Override
    public void setCurrentIteration(int value) {
        this.currentIteration = value;
    }

    public void optimize(TrainingEvaluator eval,int numEpochs,boolean lineSearch) {
        if(!lineSearch) {
            log.info("BEGIN BACKPROP WITH SCORE OF " + network.score());

            Double lastEntropy =  network.score();
            //store a copy of the network for when binary cross entropy gets
            //worse after an iteration
            BaseMultiLayerNetwork revert = network.clone();
            //sgd style; only train a certain number of epochs
            if(network.isForceNumEpochs()) {
                for(int i = 0; i < epochs; i++) {
                    if(i % network.getResetAdaGradIterations() == 0)
                        network.getOutputLayer().getAdaGrad().historicalGradient = null;
                    network.backPropStepR(null);
                    log.info("Iteration " + i + " error " + network.score());

                }
            }

            else {


                boolean train = true;
                int count = 0;
                double changeTolerance = 1e-5;
                int backPropIterations = 0;
                while(train) {
                    if(backPropIterations >= epochs) {
                        log.info("Backprop number of iterations max hit; converging");
                        break;

                    }
                    count++;
                /* Trains logistic regression post weight updates */

                    Double entropy = network.score();
                    if(lastEntropy == null || entropy < lastEntropy) {
                        double diff = Math.abs(entropy - lastEntropy);
                        if(diff < changeTolerance) {
                            log.info("Not enough of a change on back prop...breaking");
                            break;
                        }
                        else
                            lastEntropy = entropy;
                        log.info("New score " + lastEntropy);
                        revert = network.clone();
                    }

                    else if(count >= epochs) {
                        log.info("Hit max number of epochs...breaking");
                        train = false;
                    }

                    else if(entropy >= lastEntropy || Double.isNaN(entropy) || Double.isInfinite(entropy)) {
                        train = false;
                        network.update(revert);
                        log.info("Reverting to best score " + lastEntropy);
                    }

                    backPropIterations++;
                }


            }
        }

        else {

            NeuralNetwork.OptimizationAlgorithm optimizationAlgorithm = network.getOptimizationAlgorithm();
            if(optimizationAlgorithm == NeuralNetwork.OptimizationAlgorithm.CONJUGATE_GRADIENT) {
                VectorizedNonZeroStoppingConjugateGradient g = new VectorizedNonZeroStoppingConjugateGradient(this);
                g.setTrainingEvaluator(eval);
                g.setMaxIterations(numEpochs);
                g.optimize(numEpochs);

            }

            else if(optimizationAlgorithm == NeuralNetwork.OptimizationAlgorithm.HESSIAN_FREE) {
                h = new StochasticHessianFree(this,network);
                h.setTrainingEvaluator(eval);
                h.optimize(numEpochs);
            }

            else {
                VectorizedDeepLearningGradientAscent g = new VectorizedDeepLearningGradientAscent(this);
                g.setTrainingEvaluator(eval);
                g.optimize(numEpochs);

            }

        }

    }

    @Override
    public void getValueGradient(double[] buffer) {
        System.arraycopy(network.getBackPropRGradient(network.params()).data,0,buffer,0,buffer.length);
    }

    @Override
    public double getValue() {
        return - (network.score());
    }

    @Override
    public int getNumParameters() {
        if(length < 0)
            length = getParameters().length;
        return length;
    }

    @Override
    public void getParameters(double[] buffer) {
        System.arraycopy(getParameters().data,0,buffer,0,buffer.length);
    }

    @Override
    public double getParameter(int index) {
        return 0;
    }

    @Override
    public void setParameters(double[] params) {
        setParameters(new DoubleMatrix(params).reshape(1,params.length));
    }

    @Override
    public void setParameter(int index, double value) {

    }

    @Override
    public DoubleMatrix getParameters() {
        return network.params();
    }

    @Override
    public void setParameters(DoubleMatrix params) {
        network.setParameters(params);


    }

    @Override
    public DoubleMatrix getValueGradient(int iteration) {
        return network.getBackPropRGradient(network.params());
    }



}