package org.deeplearning4j.da;

import static org.deeplearning4j.util.MathUtils.binomial;
import static org.deeplearning4j.util.MatrixUtil.oneMinus;
import static org.deeplearning4j.util.MatrixUtil.sigmoid;

import java.io.Serializable;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.nn.BaseNeuralNetwork;
import org.deeplearning4j.nn.gradient.NeuralNetworkGradient;
import org.deeplearning4j.sda.DenoisingAutoEncoderOptimizer;
import org.jblas.DoubleMatrix;


/**
 * Denoising Autoencoder.
 * Add Gaussian noise to input and learn
 * a reconstruction function.
 *
 * @author Adam Gibson
 *
 */
public class DenoisingAutoEncoder extends BaseNeuralNetwork implements Serializable  {


    private static final long serialVersionUID = -6445530486350763837L;

    public DenoisingAutoEncoder() {}


    public DenoisingAutoEncoder(DoubleMatrix input, int nVisible, int nHidden,
                                DoubleMatrix W, DoubleMatrix hbias, DoubleMatrix vbias,
                                RandomGenerator rng,double fanIn,RealDistribution dist) {
        super(input, nVisible, nHidden, W, hbias, vbias, rng,fanIn,dist);
    }

    /**
     * Corrupts the given input by doing a binomial sampling
     * given the corruption level
     * @param x the input to corrupt
     * @param corruptionLevel the corruption value
     * @return the binomial sampled corrupted input
     */
    public DoubleMatrix getCorruptedInput(DoubleMatrix x, double corruptionLevel) {
        DoubleMatrix tilde_x = DoubleMatrix.zeros(x.rows,x.columns);
        for(int i = 0; i < x.rows; i++)
            for(int j = 0; j < x.columns; j++)
                tilde_x.put(i,j,binomial(rng,1,1 - corruptionLevel));
        DoubleMatrix  ret = tilde_x.mul(x);
        return ret;
    }




    @Override
    public Pair<DoubleMatrix, DoubleMatrix> sampleHiddenGivenVisible(
            DoubleMatrix v) {
        DoubleMatrix ret = getHiddenValues(v);
        return new Pair<>(ret,ret);
    }


    @Override
    public Pair<DoubleMatrix, DoubleMatrix> sampleVisibleGivenHidden(
            DoubleMatrix h) {
        DoubleMatrix ret = this.getReconstructedInput(h);
        return new Pair<>(ret,ret);
    }




    // Encode
    public DoubleMatrix getHiddenValues(DoubleMatrix x) {
        DoubleMatrix ret =  sigmoid(x.mmul(W).addRowVector(hBias));
        applyDropOutIfNecessary(ret);
        return ret;
    }

    // Decode
    public DoubleMatrix getReconstructedInput(DoubleMatrix y) {
        return sigmoid(y.mmul(W.transpose()).addRowVector(vBias));
    }


    /**
     * Run a network optimizer
     * @param x the input
     * @param lr the learning rate
     * @param corruptionLevel the corruption level
     */
    public void trainTillConvergence(DoubleMatrix x, double lr,double corruptionLevel) {
        if(x != null)
            this.input = x;
        this.lastMiniBatchSize = x.rows;
        optimizer = new DenoisingAutoEncoderOptimizer(this,lr,new Object[]{corruptionLevel}, optimizationAlgo, lossFunction);
        optimizer.train(x);
    }

    /**
     * Perform one iteration of training
     * @param x the input
     * @param lr the learning rate
     * @param corruptionLevel the corruption level to train with
     */
    public void train(DoubleMatrix x,double lr,double corruptionLevel) {
        if(x != null && cacheInput)
            this.input = x;
        this.lastMiniBatchSize = x.rows;
        NeuralNetworkGradient gradient = getGradient(new Object[]{corruptionLevel,lr});
        vBias.addi(gradient.getvBiasGradient());
        W.addi(gradient.getwGradient());
        hBias.addi(gradient.gethBiasGradient());

    }

    @Override
    public DoubleMatrix reconstruct(DoubleMatrix x) {
        DoubleMatrix y = getHiddenValues(x);
        return getReconstructedInput(y);
    }



    public static class Builder extends BaseNeuralNetwork.Builder<DenoisingAutoEncoder> {
        public Builder()  {
            this.clazz = DenoisingAutoEncoder.class;
        }
    }



    @Override
    public void trainTillConvergence(DoubleMatrix input,double lr,Object[] params) {
        if(input != null && cacheInput)
            this.input = input;
        this.lastMiniBatchSize = input.rows;
        optimizer = new DenoisingAutoEncoderOptimizer(this,lr, params, optimizationAlgo, lossFunction);
        optimizer.train(input);
    }




    @Override
    public double lossFunction(Object[] params) {
        return negativeLogLikelihood();
    }




    @Override
    public void train(DoubleMatrix input,double lr,Object[] params) {
        double corruptionLevel = (double) params[0];
       if(input != null && cacheInput)
        this.input = input;
        this.lastMiniBatchSize = input.rows;
        NeuralNetworkGradient gradient = getGradient(new Object[]{corruptionLevel,lr});

        vBias.addi(gradient.getvBiasGradient());
        W.addi(gradient.getwGradient());
        hBias.addi(gradient.gethBiasGradient());
    }




    @Override
    public synchronized NeuralNetworkGradient getGradient(Object[] params) {

        double corruptionLevel = (double) params[0];
        double lr = (double) params[1];

        if(wAdaGrad != null)
            this.wAdaGrad.setMasterStepSize(lr);
        if(hBiasAdaGrad != null )
            this.hBiasAdaGrad.setMasterStepSize(lr);
        if(vBiasAdaGrad != null)
            vBiasAdaGrad.setMasterStepSize(lr);


        DoubleMatrix corruptedX = getCorruptedInput(input, corruptionLevel);
        DoubleMatrix y = getHiddenValues(corruptedX);
        DoubleMatrix z = getReconstructedInput(y);

        DoubleMatrix L_h2 =  input.sub(z);

        DoubleMatrix L_h1 = sparsity == 0 ? L_h2.mmul(W).mul(y).mul(oneMinus(y)) : L_h2.mmul(W).mul(y).mul(y.add(- sparsity));

        DoubleMatrix L_vbias = L_h2;
        DoubleMatrix L_hbias = L_h1;

        DoubleMatrix wGradient = corruptedX.transpose().mmul(L_h1).add(L_h2.transpose().mmul(y));

        DoubleMatrix hBiasGradient = L_hbias.columnMeans();
        DoubleMatrix vBiasGradient = L_vbias.columnMeans();

        NeuralNetworkGradient gradient = new NeuralNetworkGradient(wGradient,vBiasGradient,hBiasGradient);
        triggerGradientEvents(gradient);
        updateGradientAccordingToParams(gradient, lr);

        return gradient;
    }







}