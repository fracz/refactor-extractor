package org.deeplearning4j.rbm;


import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.nn.BaseNeuralNetwork;
import org.deeplearning4j.nn.NeuralNetwork;
import org.deeplearning4j.nn.WeightInit;
import org.deeplearning4j.nn.gradient.NeuralNetworkGradient;
import org.deeplearning4j.optimize.NeuralNetworkOptimizer;
import org.deeplearning4j.plot.NeuralNetPlotter;
import org.deeplearning4j.util.MatrixUtil;
import org.deeplearning4j.util.RBMUtil;
import org.jblas.DoubleMatrix;
import org.jblas.SimpleBlas;

import java.util.Map;

import static org.deeplearning4j.util.MatrixUtil.*;


/**
 * Restricted Boltzmann Machine.
 *
 * Markov chain with gibbs sampling.
 *
 * Supports the following visible units:
 *
 *     binary
 *     gaussian
 *     softmax
 *     linear
 *
 * Supports the following hidden units:
 *     rectified
 *     binary
 *     gaussian
 *     softmax
 *     linear
 *
 * Based on Hinton et al.'s work
 *
 * Great reference:
 * http://www.iro.umontreal.ca/~lisa/publications2/index.php/publications/show/239
 *
 *
 * @author Adam Gibson
 *
 */
@SuppressWarnings("unused")
public class RBM extends BaseNeuralNetwork {

    public  static enum VisibleUnit {
        BINARY,GAUSSIAN,SOFTMAX,LINEAR


    }

    public  static enum HiddenUnit {
        RECTIFIED,BINARY,GAUSSIAN,SOFTMAX
    }
    /**
     *
     */
    private static final long serialVersionUID = 6189188205731511957L;
    protected NeuralNetworkOptimizer optimizer;
    protected VisibleUnit visibleType = VisibleUnit.BINARY;
    protected HiddenUnit  hiddenType = HiddenUnit.BINARY;
    protected DoubleMatrix sigma,hiddenSigma;



    protected RBM() {}




    protected RBM(DoubleMatrix input, int nVisible, int nHidden, DoubleMatrix W,
                  DoubleMatrix hBias, DoubleMatrix vBias, RandomGenerator rng,double fanIn,RealDistribution dist) {
        super(input, nVisible, nHidden, W, hBias, vBias, rng,fanIn,dist);

    }

    /**
     * Trains till global minimum is found.
     * @param learningRate
     * @param k
     * @param input
     */
    public void trainTillConvergence(double learningRate,int k,DoubleMatrix input) {
        if(input != null && cacheInput)
            this.input = input;
        if(visibleType == VisibleUnit.GAUSSIAN)
            this.sigma = MatrixUtil.columnVariance(input).divi(input.rows);

        optimizer = new RBMOptimizer(this, learningRate, new Object[]{k,learningRate}, optimizationAlgo, lossFunction);
        optimizer.train(input);
    }


    /**
     * Contrastive divergence revolves around the idea
     * of approximating the log likelihood around x1(input) with repeated sampling.
     * Given is an energy based model: the higher k is (the more we sample the model)
     * the more we lower the energy (increase the likelihood of the model)
     *
     * and lower the likelihood (increase the energy) of the hidden samples.
     *
     * Other insights:
     *    CD - k involves keeping the first k samples of a gibbs sampling of the model.
     *
     * @param learningRate the learning rate to scale by
     * @param k the number of iterations to do
     * @param input the input to sample from
     */
    public void contrastiveDivergence(double learningRate,int k,DoubleMatrix input) {
        if(input != null && cacheInput)
            this.input = input;
        this.lastMiniBatchSize = input.rows;
        NeuralNetworkGradient gradient = getGradient(new Object[]{k,learningRate,-1});
        getW().addi(gradient.getwGradient());
        gethBias().addi(gradient.gethBiasGradient());
        getvBias().addi(gradient.getvBiasGradient());

    }

    /**
     * Contrastive divergence revolves around the idea
     * of approximating the log likelihood around x1(input) with repeated sampling.
     * Given is an energy based model: the higher k is (the more we sample the model)
     * the more we lower the energy (increase the likelihood of the model)
     *
     * and lower the likelihood (increase the energy) of the hidden samples.
     *
     * Other insights:
     *    CD - k involves keeping the first k samples of a gibbs sampling of the model.
     *
     * @param learningRate the learning rate to scale by
     * @param k the number of iterations to do
     * @param input the input to sample from
     * @param iteration  the iteration to use
     */
    public void contrastiveDivergence(double learningRate,int k,DoubleMatrix input,int iteration) {
        if(input != null && cacheInput)
            this.input = input;
        this.lastMiniBatchSize = input.rows;
        NeuralNetworkGradient gradient = getGradient(new Object[]{k,learningRate,iteration});
        getW().addi(gradient.getwGradient());
        gethBias().addi(gradient.gethBiasGradient());
        getvBias().addi(gradient.getvBiasGradient());

    }


    @Override
    public NeuralNetworkGradient getGradient(Object[] params) {



        int k = (int) params[0];
        double learningRate = (double) params[1];
        int iteration = params[params.length - 1] == null ? 0 : (int) params[params.length - 1];

        if(wAdaGrad != null)
            wAdaGrad.setMasterStepSize(learningRate);
        if(hBiasAdaGrad != null )
            hBiasAdaGrad.setMasterStepSize(learningRate);
        if(vBiasAdaGrad != null)
            vBiasAdaGrad.setMasterStepSize(learningRate);

		/*
		 * Cost and updates dictionary.
		 * This is the update rules for weights and biases
		 */

        //POSITIVE PHASE
        Pair<DoubleMatrix,DoubleMatrix> probHidden = sampleHiddenGivenVisible(input);

		/*
		 * Start the gibbs sampling.
		 */
        DoubleMatrix chainStart = probHidden.getSecond();

		/*
		 * Note that at a later date, we can explore alternative methods of
		 * storing the chain transitions for different kinds of sampling
		 * and exploring the search space.
		 */
        Pair<Pair<DoubleMatrix,DoubleMatrix>,Pair<DoubleMatrix,DoubleMatrix>> matrices;
        //negative visible means or expected values
        DoubleMatrix nvMeans = null;
        //negative value samples
        DoubleMatrix nvSamples = null;
        //negative hidden means or expected values
        DoubleMatrix nhMeans = null;
        //negative hidden samples
        DoubleMatrix nhSamples = null;

		/*
		 * K steps of gibbs sampling. This is the positive phase of contrastive divergence.
		 *
		 * There are 4 matrices being computed for each gibbs sampling.
		 * The samples from both the positive and negative phases and their expected values
		 * or averages.
		 *
		 */

        for(int i = 0; i < k; i++) {

            //NEGATIVE PHASE
            if(i == 0)
                matrices = gibbhVh(chainStart);
            else
                matrices = gibbhVh(nhSamples);

            //getFromOrigin the cost updates for sampling in the chain after k iterations
            nvMeans = matrices.getFirst().getFirst();
            nvSamples = matrices.getFirst().getSecond();
            nhMeans = matrices.getSecond().getFirst();
            nhSamples = matrices.getSecond().getSecond();
        }

		/*
		 * Update gradient parameters
		 */
        DoubleMatrix wGradient = input.transpose().mmul(probHidden.getSecond()).sub(
                nvSamples.transpose().mmul(nhMeans)
        );



        DoubleMatrix hBiasGradient;

        if(sparsity != 0)
            //all hidden units must stay around this number
            hBiasGradient = mean(scalarMinus(sparsity,probHidden.getSecond()),0);
        else
            //update rule: the expected values of the hidden input - the negative hidden  means adjusted by the learning rate
            hBiasGradient = mean(probHidden.getSecond().sub(nhMeans), 0);




        //update rule: the expected values of the input - the negative samples adjusted by the learning rate
        DoubleMatrix  vBiasGradient = mean(input.sub(nvSamples), 0);
        NeuralNetworkGradient ret = new NeuralNetworkGradient(wGradient, vBiasGradient, hBiasGradient);

        updateGradientAccordingToParams(ret, iteration,learningRate);

        return ret;
    }


    @Override
    public NeuralNetwork transpose() {
        RBM r = (RBM) super.transpose();
        HiddenUnit h = RBMUtil.inverse(visibleType);
        VisibleUnit v = RBMUtil.inverse(hiddenType);
        if(h == null)
            h = hiddenType;
        if(v == null)
            v = visibleType;

        r.setHiddenType(h);
        r.setVisibleType(v);
        r.sigma = sigma;
        r.hiddenSigma = hiddenSigma;
        return r;
    }

    @Override
    public NeuralNetwork clone() {
        RBM r = (RBM) super.clone();
        r.setHiddenType(hiddenType);
        r.setVisibleType(visibleType);
        r.sigma = sigma;
        r.hiddenSigma = hiddenSigma;
        return r;
    }

    /**
     * Free energy for an RBM
     * Lower energy models have higher probability
     * of activations
     * @param visibleSample the sample to test on
     * @return the free energy for this sample
     */
    public double freeEnergy(DoubleMatrix visibleSample) {
        DoubleMatrix wxB = visibleSample.mmul(W).addiRowVector(hBias);
        double vBiasTerm = SimpleBlas.dot(visibleSample, vBias);
        double hBiasTerm = log(exp(wxB).add(1)).sum();
        return -hBiasTerm - vBiasTerm;
    }


    /**
     * Binomial sampling of the hidden values given visible
     * @param v the visible values
     * @return a binomial distribution containing the expected values and the samples
     */
    @Override
    public Pair<DoubleMatrix,DoubleMatrix> sampleHiddenGivenVisible(DoubleMatrix v) {
        if(hiddenType == HiddenUnit.RECTIFIED) {
            DoubleMatrix h1Mean = propUp(v);
            DoubleMatrix sigH1Mean = sigmoid(h1Mean);
		/*
		 * Rectified linear part
		 */
            DoubleMatrix sqrtSigH1Mean = sqrt(sigH1Mean);
            //NANs here with Word2Vec
            DoubleMatrix h1Sample = h1Mean.addi(normal(getRng(), h1Mean,1).mul(sqrtSigH1Mean));
            MatrixUtil.max(0.0, h1Sample);
            //apply dropout
            applyDropOutIfNecessary(h1Sample);


            return new Pair<>(h1Mean,h1Sample);

        }

        else if(hiddenType == HiddenUnit.GAUSSIAN) {
            DoubleMatrix h1Mean = propUp(v);
            this.hiddenSigma = columnVariance(h1Mean);

            DoubleMatrix h1Sample =  h1Mean.addi(normal(getRng(),h1Mean,this.hiddenSigma));

            //apply dropout
            applyDropOutIfNecessary(h1Sample);
            return new Pair<>(h1Mean,h1Sample);
        }

        else if(hiddenType == HiddenUnit.SOFTMAX) {
            DoubleMatrix h1Mean = propUp(v);
            DoubleMatrix h1Sample = softmax(h1Mean);
            applyDropOutIfNecessary(h1Sample);
            return new Pair<>(h1Mean,h1Sample);
        }



        else if(hiddenType == HiddenUnit.BINARY) {
            DoubleMatrix h1Mean = propUp(v);
            DoubleMatrix h1Sample = binomial(h1Mean, 1, rng);
            applyDropOutIfNecessary(h1Sample);
            return new Pair<>(h1Mean,h1Sample);
        }



        throw new IllegalStateException("Hidden unit type must either be rectified linear or binary");

    }

    /**
     * Gibbs sampling step: hidden ---> visible ---> hidden
     * @param h the hidden input
     * @return the expected values and samples of both the visible samples given the hidden
     * and the new hidden input and expected values
     */
    public Pair<Pair<DoubleMatrix,DoubleMatrix>,Pair<DoubleMatrix,DoubleMatrix>> gibbhVh(DoubleMatrix h) {

        Pair<DoubleMatrix,DoubleMatrix> v1MeanAndSample = sampleVisibleGivenHidden(h);
        DoubleMatrix vSample = v1MeanAndSample.getSecond();

        Pair<DoubleMatrix,DoubleMatrix> h1MeanAndSample = sampleHiddenGivenVisible(vSample);
        return new Pair<>(v1MeanAndSample,h1MeanAndSample);
    }


    /**
     * Guess the visible values given the hidden
     * @param h the hidden units
     * @return a visible mean and sample relative to the hidden states
     * passed in
     */
    @Override
    public Pair<DoubleMatrix,DoubleMatrix> sampleVisibleGivenHidden(DoubleMatrix h) {
        DoubleMatrix v1Mean = propDown(h);

        if(visibleType == VisibleUnit.GAUSSIAN) {
            DoubleMatrix v1Sample = v1Mean.add(MatrixUtil.randn(rng,v1Mean.rows,v1Mean.columns));
            return new Pair<>(v1Mean,v1Sample);

        }

        else if(visibleType == VisibleUnit.LINEAR) {
            DoubleMatrix v1Sample = normal(getRng(),v1Mean,1);
            return new Pair<>(v1Mean,v1Sample);
        }

        else if(visibleType == VisibleUnit.SOFTMAX) {
            DoubleMatrix v1Sample = softmax(v1Mean);
            return new Pair<>(v1Mean,v1Sample);
        }

        else if(visibleType == VisibleUnit.BINARY) {
            DoubleMatrix v1Sample = binomial(v1Mean, 1, rng);
            return new Pair<>(v1Mean,v1Sample);
        }


        throw new IllegalStateException("Visible type must either be binary,gaussian, softmax, or linear");

    }

    /**
     * Calculates the activation of the visible :
     * sigmoid(v * W + hbias)
     * @param v the visible layer
     * @return the approximated activations of the visible layer
     */
    public DoubleMatrix propUp(DoubleMatrix v) {
        if(visibleType == VisibleUnit.GAUSSIAN)
            this.sigma = columnVariance(v).divi(input.rows);

        DoubleMatrix preSig = v.mmul(W);
        if(concatBiases)
            preSig = DoubleMatrix.concatHorizontally(preSig,hBias);
        else
            preSig.addiRowVector(hBias);


        if(hiddenType == HiddenUnit.RECTIFIED) {
            max(0, preSig);
            return preSig;
        }

        else if(hiddenType == HiddenUnit.GAUSSIAN) {
            DoubleMatrix add =  preSig.add(MatrixUtil.randn(rng, preSig.rows, preSig.columns));
            preSig.addi(add);
            return preSig;
        }

        else if(hiddenType == HiddenUnit.BINARY) {
            return sigmoid(preSig);
        }

        else if(hiddenType == HiddenUnit.SOFTMAX)
            return softmax(preSig);

        throw new IllegalStateException("Hidden unit type should either be binary, gaussian, or rectified linear");

    }


    @Override
    public void iterationDone(int epoch) {
        int plotEpochs = getRenderIterations();
        if(plotEpochs <= 0)
            return;
        if(epoch % plotEpochs == 0 || epoch == 0) {
            NeuralNetPlotter plotter = new NeuralNetPlotter();
            plotter.plotNetworkGradient(this,this.getGradient(new Object[]{1,0.001,1000}),getInput().rows);
        }
    }

    /**
     * Calculates the activation of the hidden:
     * sigmoid(h * W + vbias)
     * @param h the hidden layer
     * @return the approximated output of the hidden layer
     */
    public DoubleMatrix propDown(DoubleMatrix h) {
        DoubleMatrix vMean = h.mmul(W.transpose());
        if(concatBiases)
            vMean = DoubleMatrix.concatHorizontally(vMean,vBias);
        else
            vMean.addiRowVector(vBias);

        if(visibleType  == VisibleUnit.GAUSSIAN) {
            vMean.addi(normal(rng,vMean,1.0));
            return vMean;
        }
        else if(visibleType == VisibleUnit.LINEAR) {
             vMean = normal(getRng(),vMean,1);
            return vMean;
        }

        else if(visibleType == VisibleUnit.BINARY) {
            return sigmoid(vMean);
        }

        else if(visibleType == VisibleUnit.SOFTMAX) {
            return softmax(vMean);
        }


        throw new IllegalStateException("Visible unit type should either be binary or gaussian");

    }

    /**
     * Reconstructs the visible input.
     * A reconstruction is a propdown of the reconstructed hidden input.
     * @param v the visible input
     * @return the reconstruction of the visible input
     */
    @Override
    public DoubleMatrix reconstruct(DoubleMatrix v) {
        //reconstructed: propUp ----> hidden propDown to reconstruct
        return propDown(propUp(v));
    }



    /**
     * Note: k is the first input in params.
     */
    @Override
    public void trainTillConvergence(DoubleMatrix input, double lr,
                                     Object[] params) {
        if(input != null && cacheInput)
            this.input = MatrixUtil.stabilizeInput(input,1);
        this.lastMiniBatchSize = input.rows;

        if(visibleType == VisibleUnit.GAUSSIAN) {
            this.sigma = MatrixUtil.columnVariance(input);
            if(normalizeByInputRows)
                this.sigma.divi(input.rows);
        }

        optimizer = new RBMOptimizer(this, lr, params, optimizationAlgo, lossFunction);
        optimizer.train(input);
    }


    @Override
    public String toString() {
        return "RBM{" +
                "optimizer=" + optimizer +
                ", visibleType=" + visibleType +
                ", hiddenType=" + hiddenType +
                ", sigma=" + sigma +
                ", hiddenSigma=" + hiddenSigma +
                "} " + super.toString();
    }

    public VisibleUnit getVisibleType() {
        return visibleType;
    }

    public void setVisibleType(VisibleUnit visibleType) {
        this.visibleType = visibleType;
    }

    public HiddenUnit getHiddenType() {
        return hiddenType;
    }

    public void setHiddenType(HiddenUnit hiddenType) {
        this.hiddenType = hiddenType;
    }

    @Override
    public double lossFunction(Object[] params) {
        return getReConstructionCrossEntropy();
    }

    @Override
    public void train(DoubleMatrix input,double lr, Object[] params) {
        if(visibleType == VisibleUnit.GAUSSIAN)
            this.sigma = MatrixUtil.columnVariance(input).divi(input.rows);


        int k = (int) params[0];
        contrastiveDivergence(lr, k, input);
    }

    public static class Builder extends BaseNeuralNetwork.Builder<RBM> {
        private VisibleUnit visible = VisibleUnit.BINARY;
        private HiddenUnit hidden = HiddenUnit.BINARY;

        public Builder() {
            clazz =  RBM.class;
        }

        /**
         * Weight initialization scheme
         * @param weightInit the weight initialization scheme
         * @return
         */
        public Builder weightInit(WeightInit weightInit) {
            this.weightInit = weightInit;
            return this;
        }

        /**
         * Constrains gradient to unit norm when updating parameters
         *
         * @param constrainGradientToUnitNorm whether to constrain the gradient to unit norm or not
         * @return
         */
        @Override
        public Builder constrainGradientToUnitNorm(boolean constrainGradientToUnitNorm) {
             super.constrainGradientToUnitNorm(constrainGradientToUnitNorm);
            return this;
        }

        @Override
        public Builder concatBiases(boolean concatBiases) {
            super.concatBiases(concatBiases);
            return this;
        }

        @Override
        public Builder resetAdaGradIterations(int resetAdaGradIterations) {
            super.resetAdaGradIterations(resetAdaGradIterations);
            return this;
        }

        @Override
        public Builder momentumAfter(Map<Integer, Double> momentumAfter) {
            super.momentumAfter(momentumAfter);
            return this;
        }

        @Override
        public Builder  cacheInput(boolean cacheInput) {
            super.cacheInput(cacheInput);
            return this;
        }

        @Override
        public Builder applySparsity(boolean applySparsity) {
            super.applySparsity(applySparsity);
            return this;
        }

        @Override
        public Builder withOptmizationAlgo(OptimizationAlgorithm optimizationAlgo) {
            super.withOptmizationAlgo(optimizationAlgo);
            return this;
        }

        @Override
        public Builder withLossFunction(LossFunction lossFunction) {
            super.withLossFunction(lossFunction);
            return this;
        }

        @Override
        public Builder withDropOut(double dropOut) {
            super.withDropOut(dropOut);
            return this;
        }

        @Override
        public Builder normalizeByInputRows(boolean normalizeByInputRows) {
            super.normalizeByInputRows(normalizeByInputRows);
            return this;
        }

        @Override
        public Builder useAdaGrad(boolean useAdaGrad) {
            super.useAdaGrad(useAdaGrad);
            return this;
        }

        @Override
        public Builder withDistribution(RealDistribution dist) {
            super.withDistribution(dist);
            return this;
        }

        @Override
        public Builder useRegularization(boolean useRegularization) {
            super.useRegularization(useRegularization);
            return this;
        }

        @Override
        public Builder fanIn(double fanIn) {
            super.fanIn(fanIn);
            return this;
        }

        @Override
        public Builder withL2(double l2) {
            super.withL2(l2);
            return this;
        }

        @Override
        public Builder renderWeights(int numEpochs) {
            super.renderWeights(numEpochs);
            return this;
        }

        @Override
        public RBM buildEmpty() {
            return super.buildEmpty();
        }

        @Override
        public Builder withClazz(Class<? extends BaseNeuralNetwork> clazz) {
            super.withClazz(clazz);
            return this;
        }

        @Override
        public Builder withSparsity(double sparsity) {
            super.withSparsity(sparsity);
            return this;
        }

        @Override
        public Builder withMomentum(double momentum) {
            super.withMomentum(momentum);
            return this;
        }

        @Override
        public Builder withInput(DoubleMatrix input) {
            super.withInput(input);
            return this;
        }

        @Override
        public Builder asType(Class<RBM> clazz) {
            super.asType(clazz);
            return this;
        }

        @Override
        public Builder withWeights(DoubleMatrix W) {
            super.withWeights(W);
            return this;
        }

        @Override
        public Builder withVisibleBias(DoubleMatrix vBias) {
            super.withVisibleBias(vBias);
            return this;
        }

        @Override
        public Builder withHBias(DoubleMatrix hBias) {
            super.withHBias(hBias);
            return this;
        }

        @Override
        public Builder numberOfVisible(int numVisible) {
            super.numberOfVisible(numVisible);
            return this;
        }

        @Override
        public Builder numHidden(int numHidden) {
            super.numHidden(numHidden);
            return this;
        }

        @Override
        public Builder withRandom(RandomGenerator gen) {
            super.withRandom(gen);
            return this;
        }

        public Builder withVisible(VisibleUnit visible) {
            this.visible = visible;
            return this;
        }

        public Builder withHidden(HiddenUnit hidden) {
            this.hidden = hidden;
            return this;
        }


        public RBM build() {
            RBM ret = super.build();
            ret.hiddenType = hidden;
            ret.visibleType = visible;
            return ret;
        }

    }






}