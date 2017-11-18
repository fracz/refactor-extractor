package org.deeplearning4j.nn;


import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.ops.transforms.Transforms;

/**
 * Central class for loss functions
 */
public class LossFunctions {


    /**
     * MSE: Mean Squared Error: Linear Regression
     * EXPLL: Exponential log likelihood: Poisson Regression
     * XENT: Cross Entropy: Binary Classification
     * SOFTMAX: Softmax Regression
     * RMSE_XENT: RMSE Cross Entropy
     *
     *
     */
    public static enum LossFunction {
        MSE,EXPLL,XENT,MCXENT,RMSE_XENT,SQUARED_LOSS,RECONSTRUCTION_CROSSENTROPY,NEGATIVELOGLIKELIHOOD
    }


    /**
     * Generic scoring function
     * @param input the inputs to score
     * @param labels the labels to score
     * @param lossFunction the loss function to use
     * @param output the output function
     * @param l2 the l2 coefficient
     * @param useRegularization  whether to use regularization
     * @return the score for the given parameters
     */
    public static double score(INDArray input,INDArray labels,LossFunction lossFunction,Output output,double l2,boolean useRegularization) {
        double ret = 0.0;
        double reg = 0.5 * l2;
        INDArray z = output.output(input);
        switch (lossFunction) {
            case MCXENT:
                INDArray mcXEntLogZ = Transforms.log(z);
                ret = - (double) labels.mul(mcXEntLogZ).sum(1).sum(Integer.MAX_VALUE).element() / labels.rows();
                break;
            case XENT:
                INDArray xEntLogZ = Transforms.log(z);
                INDArray xEntOneMinusLabelsOut = labels.rsub(1);
                INDArray xEntOneMinusLogOneMinusZ = Transforms.log(z).rsubi(1);
                ret = -(double) labels.mul(xEntLogZ).add(xEntOneMinusLabelsOut).mul(xEntOneMinusLogOneMinusZ).sum(1).sum(Integer.MAX_VALUE).element() / labels.rows();
                break;
            case RMSE_XENT:
                ret = (double) Transforms.pow(labels.sub(z),2).sum(1).sum(Integer.MAX_VALUE).element() / labels.rows();
                break;
            case MSE:
                INDArray mseDelta = labels.sub(z);
                ret = 0.5 * (double) Transforms.pow(mseDelta, 2).sum(1).sum(Integer.MAX_VALUE).element() / labels.rows();
                break;
            case EXPLL:
                INDArray expLLLogZ = Transforms.log(z);
                ret =  - (double)z.sub(labels.mul(expLLLogZ)).sum(1).sum(Integer.MAX_VALUE).element() / labels.rows();
                break;
            case SQUARED_LOSS:
                ret = (double) Transforms.pow(labels.sub(z), 2).sum(1).sum(Integer.MAX_VALUE).element() / labels.rows();


        }

        if(useRegularization && l2 > 0)
            ret += reg;


        return ret;

    }

}