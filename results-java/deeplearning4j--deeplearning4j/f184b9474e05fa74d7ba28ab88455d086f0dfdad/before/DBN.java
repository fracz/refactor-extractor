package org.deeplearning4j.dbn;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.linalg.api.activation.ActivationFunction;
import org.deeplearning4j.linalg.api.activation.Activations;
import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.dataset.DataSet;
import org.deeplearning4j.linalg.factory.NDArrays;
import org.deeplearning4j.linalg.sampling.Sampling;
import org.deeplearning4j.linalg.transformation.MatrixTransform;
import org.deeplearning4j.nn.*;

import org.deeplearning4j.rbm.RBM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Deep Belief Network. This is a MultiLayer Perceptron Model
 * using Restricted Boltzmann Machines.
 *  See Hinton's practical guide to RBMs for great examples on
 *  how to train and tune parameters.
 *
 * @author Adam Gibson
 *
 */
public class DBN extends BaseMultiLayerNetwork {

    private static final long serialVersionUID = -9068772752220902983L;
    private static Logger log = LoggerFactory.getLogger(DBN.class);
    private RBM.VisibleUnit visibleUnit = RBM.VisibleUnit.BINARY;
    private RBM.HiddenUnit hiddenUnit = RBM.HiddenUnit.BINARY;
    private Map<Integer,RBM.VisibleUnit> visibleUnitByLayer = new HashMap<>();
    private Map<Integer,RBM.HiddenUnit> hiddenUnitByLayer = new HashMap<>();
    private boolean useRBMPropUpAsActivations = false;

    public DBN() {}


    public DBN(int nIns, int[] hiddenLayerSizes, int nOuts, int nLayers,
               RandomGenerator rng, INDArray input,INDArray labels) {
        super(nIns, hiddenLayerSizes, nOuts, nLayers, rng, input,labels);
    }



    public DBN(int nIns, int[] hiddenLayerSizes, int nOuts, int nLayers,
               RandomGenerator rng) {
        super(nIns, hiddenLayerSizes, nOuts, nLayers, rng);
    }





    /**
     * Compute activations from input to output of the output layer
     * @return the list of activations for each layer
     */
    public List<INDArray> feedForward() {
        INDArray currInput = this.input;
        if(this.input.columns() != nIns)
            throw new IllegalStateException("Illegal input length");
        List<INDArray> activations = new ArrayList<>();
        activations.add(currInput);
        for(int i = 0; i < getnLayers(); i++) {
            NeuralNetwork layer = getLayers()[i];
            HiddenLayer l = getSigmoidLayers()[i];

            layer.setInput(currInput);
            l.setInput(currInput);

            if(useRBMPropUpAsActivations) {
                RBM r = (RBM) layer;
                if(sampleFromHiddenActivations)
                    currInput = layer.sampleHiddenGivenVisible(currInput).getSecond();

                else
                    currInput = r.propUp(currInput);
            }

            else if(getSampleOrActivate() != null && getSampleOrActivate().get(i) != null && getSampleOrActivate().get(i))
                currInput = getSigmoidLayers()[i].activate(layer.reconstruct(currInput));
            else  if(sampleFromHiddenActivations)
                currInput = layer.sampleHiddenGivenVisible(l.getActivationFunction().apply(currInput)).getSecond();
            else
                currInput = layer.sampleHiddenGivenVisible(currInput).getSecond();
            activations.add(currInput);
        }
        if(getOutputLayer() != null) {
            getOutputLayer().setInput(activations.get(activations.size() - 1));
            if(getOutputLayer().getActivationFunction() == null)
                if(outputActivationFunction != null)
                    outputLayer.setActivationFunction(outputActivationFunction);
                else
                    outputLayer.setActivationFunction(Activations.sigmoid());

            activations.add(getOutputLayer().output(activations.get(activations.size() - 1)));

        }
        return activations;
    }

    /**
     * Creates a hidden layer with the given parameters.
     * The default implementation is a binomial sampling
     * hidden layer, but this can be overriden
     * for other kinds of hidden units
     * @param nIn the number of inputs
     * @param nOut the number of outputs
     * @param activation the activation function for the layer
     * @param rng the rng to use for sampling
     * @param layerInput the layer starting input
     * @param dist the probability distribution to use
     * for generating weights
     * @return a hidden layer with the given paremters
     */
    public HiddenLayer createHiddenLayer(int index,int nIn,int nOut,ActivationFunction activation,RandomGenerator rng,INDArray layerInput,RealDistribution dist) {
        return super.createHiddenLayer(index, nIn, nOut, activation, rng, layerInput, dist);

    }


    @Override
    public void pretrain(DataSetIterator iter, Object[] otherParams) {
        int k = (Integer) otherParams[0];
        double lr = (Double) otherParams[1];
        int epochs = (Integer) otherParams[2];
        int passes = otherParams.length > 3 ? (Integer) otherParams[3] : 1;
        for(int i = 0; i < passes; i++)
            pretrain(iter,k,lr,epochs);

    }

    @Override
    public void pretrain(INDArray input, Object[] otherParams) {
        int k = (Integer) otherParams[0];
        double lr = (Double) otherParams[1];
        int epochs = (Integer) otherParams[2];
        pretrain(input,k,lr,epochs);

    }

    /**
     * This unsupervised learning method runs
     * contrastive divergence on each RBM layer in the network.
     * @param iter the input to train on
     * @param k the k to use for running the RBM contrastive divergence.
     * The typical tip is that the higher k is the closer to the model
     * you will be approximating due to more sampling. K = 1
     * usually gives very good results and is the default in quite a few situations.
     * @param learningRate the learning rate to use
     * @param epochs the number of epochs to train
     */
    public void pretrain(DataSetIterator iter,int k,double learningRate,int epochs) {


        INDArray layerInput;

        for(int i = 0; i < getnLayers(); i++) {
            if(i == 0) {
                while(iter.hasNext()) {
                    DataSet next = iter.next();
                    this.input = next.getFeatureMatrix();
                      /*During pretrain, feed forward expected activations of network, use activation functions during pretrain  */
                    if(this.getInput() == null || this.getLayers() == null || this.getLayers()[0] == null || this.getSigmoidLayers() == null || this.getSigmoidLayers()[0] == null) {
                        setInput(input);
                        initializeLayers(input);
                    }
                    else
                        setInput(input);
                    //override learning rate where present
                    double realLearningRate = layerLearningRates.get(i) != null ? layerLearningRates.get(i) : learningRate;
                    if(isForceNumEpochs()) {
                        for(int epoch = 0; epoch < epochs; epoch++) {
                            log.info("Error on epoch " + epoch + " for layer " + (i + 1) + " is " + getLayers()[i].getReConstructionCrossEntropy());
                            getLayers()[i].train(next.getFeatureMatrix(), realLearningRate,new Object[]{k,learningRate});
                            getLayers()[i].iterationDone(epoch);
                        }
                    }
                    else
                        getLayers()[i].trainTillConvergence(next.getFeatureMatrix(), realLearningRate, new Object[]{k,realLearningRate,epochs});

                }

                iter.reset();
            }

            else {
                boolean activateOnly = getSampleOrActivate() != null && getSampleOrActivate().get(i) != null ? getSampleOrActivate().get(i) : !sampleFromHiddenActivations;
                while (iter.hasNext()) {
                    DataSet next = iter.next();
                    layerInput = next.getFeatureMatrix();
                    for(int j = 1; j <= i; j++) {
                        if(useRBMPropUpAsActivations) {
                            RBM r = (RBM) layers[i];
                            layerInput = r.propUp(layerInput);
                        }
                        else if(activateOnly)
                            layerInput = getSigmoidLayers()[j - 1].activate(layerInput);
                        else if(isSampleFromHiddenActivations())
                            layerInput = getLayers()[j - 1].sampleHiddenGivenVisible(getSigmoidLayers()[j - 1].getActivationFunction().apply(layerInput)).getSecond();
                        else
                            layerInput = getLayers()[j - 1].sampleHiddenGivenVisible(layerInput).getSecond();

                    }


                    log.info("Training on layer " + (i + 1));
                    //override learning rate where present
                    double realLearningRate = layerLearningRates.get(i) != null ? layerLearningRates.get(i) : learningRate;
                    if(isForceNumEpochs()) {
                        for(int epoch = 0; epoch < epochs; epoch++) {
                            log.info("Error on epoch " + epoch + " for layer " + (i + 1) + " is " + getLayers()[i].getReConstructionCrossEntropy());
                            getLayers()[i].train(layerInput, realLearningRate,new Object[]{k,learningRate});
                            getLayers()[i].iterationDone(epoch);
                        }
                    }
                    else
                        getLayers()[i].trainTillConvergence(layerInput, realLearningRate, new Object[]{k,realLearningRate,epochs});

                }

                iter.reset();


            }
        }
    }


    /**
     * This unsupervised learning method runs
     * contrastive divergence on each RBM layer in the network.
     * @param input the input to train on
     * @param k the k to use for running the RBM contrastive divergence.
     * The typical tip is that the higher k is the closer to the model
     * you will be approximating due to more sampling. K = 1
     * usually gives very good results and is the default in quite a few situations.
     * @param learningRate the learning rate to use
     * @param epochs the number of epochs to train
     */
    public void pretrain(INDArray input,int k,double learningRate,int epochs) {
        if(isUseGaussNewtonVectorProductBackProp()) {
            log.warn("WARNING; Gauss newton back vector back propagation is primarily used for hessian free which does not involve pretrain; just finetune. Use this at your own risk");
        }


        /*During pretrain, feed forward expected activations of network, use activation functions during pretrain  */
        if(this.getInput() == null || this.getLayers() == null || this.getLayers()[0] == null || this.getSigmoidLayers() == null || this.getSigmoidLayers()[0] == null) {
            setInput(input);
            initializeLayers(input);
        }
        else
            setInput(input);

        INDArray layerInput = null;

        for(int i = 0; i < getnLayers(); i++) {
            if(i == 0)
                layerInput = getInput();
            else {
                boolean activateOnly = getSampleOrActivate() != null && getSampleOrActivate().get(i) != null ? getSampleOrActivate().get(i) : !sampleFromHiddenActivations;
                if(useRBMPropUpAsActivations) {
                    RBM r = (RBM) layers[i - 1];
                    layerInput = r.propUp(layerInput);
                    if(sampleFromHiddenActivations)
                        layerInput = Sampling.binomial(layerInput, 1, getRng());
                }
                else if(activateOnly)
                    layerInput = getSigmoidLayers()[i - 1].activate(layerInput);
                else if(isSampleFromHiddenActivations())
                    layerInput = getLayers()[i - 1].sampleHiddenGivenVisible(getSigmoidLayers()[i - 1].getActivationFunction().apply(layerInput)).getSecond();
                else
                    layerInput = getLayers()[i - 1].sampleHiddenGivenVisible(layerInput).getSecond();

            }
            log.info("Training on layer " + (i + 1));
            //override learning rate where present
            double realLearningRate = layerLearningRates.get(i) != null ? layerLearningRates.get(i) : learningRate;
            if(isForceNumEpochs()) {
                for(int epoch = 0; epoch < epochs; epoch++) {
                    log.info("Error on epoch " + epoch + " for layer " + (i + 1) + " is " + getLayers()[i].getReConstructionCrossEntropy());
                    getLayers()[i].train(layerInput, realLearningRate,new Object[]{k,learningRate});
                    getLayers()[i].iterationDone(epoch);
                }
            }
            else
                getLayers()[i].trainTillConvergence(layerInput, realLearningRate, new Object[]{k,realLearningRate,epochs});


        }
    }

    public Map<Integer, RBM.VisibleUnit> getVisibleUnitByLayer() {
        return visibleUnitByLayer;
    }

    public void setVisibleUnitByLayer(Map<Integer, RBM.VisibleUnit> visibleUnitByLayer) {
        this.visibleUnitByLayer = visibleUnitByLayer;
    }

    public Map<Integer, RBM.HiddenUnit> getHiddenUnitByLayer() {
        return hiddenUnitByLayer;
    }

    public void setHiddenUnitByLayer(Map<Integer, RBM.HiddenUnit> hiddenUnitByLayer) {
        this.hiddenUnitByLayer = hiddenUnitByLayer;
    }

    public RBM.VisibleUnit getVisibleUnit() {
        return visibleUnit;
    }

    public void setVisibleUnit(RBM.VisibleUnit visibleUnit) {
        this.visibleUnit = visibleUnit;
    }

    public RBM.HiddenUnit getHiddenUnit() {
        return hiddenUnit;
    }

    public void setHiddenUnit(RBM.HiddenUnit hiddenUnit) {
        this.hiddenUnit = hiddenUnit;
    }

    public void pretrain(int k,double learningRate,int epochs) {
        pretrain(this.getInput(),k,learningRate,epochs);
    }


    @Override
    public NeuralNetwork createLayer(INDArray input, int nVisible,
                                     int nHidden, INDArray W, INDArray hBias,
                                     INDArray vBias, RandomGenerator rng,int index) {
        RBM.HiddenUnit h = hiddenUnitByLayer.get(index) != null ? hiddenUnitByLayer.get(index) : hiddenUnit;
        RBM.VisibleUnit v =visibleUnitByLayer.get(index) != null ? visibleUnitByLayer.get(index) : visibleUnit;
        RBM ret = new RBM.Builder().constrainGradientToUnitNorm(constrainGradientToUnitNorm).weightInit(weightInitByLayer.get(index) != null ? weightInitByLayer.get(index) : weightInit)
                .withHidden(h)
                .withVisible(v)
                .useRegularization(isUseRegularization()).withOptmizationAlgo(getOptimizationAlgorithm()).withL2(getL2())
                .useAdaGrad(isUseAdaGrad()).normalizeByInputRows(isNormalizeByInputRows())
                .withLossFunction(lossFunctionByLayer.get(index) != null ? lossFunctionByLayer.get(index) : getLossFunction())
                .withMomentum(getMomentum()).withSparsity(getSparsity()).withDistribution(getDist()).normalizeByInputRows(normalizeByInputRows)
                .numberOfVisible(nVisible).numHidden(nHidden).withWeights(W).withDropOut(dropOut)
                .momentumAfter(momentumAfterByLayer.get(index) != null ? momentumAfterByLayer.get(index) : momentumAfter)
                .resetAdaGradIterations(resetAdaGradIterationsByLayer.get(index) != null ? resetAdaGradIterationsByLayer.get(index) : resetAdaGradIterations)
                .withInput(input).withVisibleBias(vBias).withHBias(hBias).withDistribution(getDist())
                .withRandom(rng).renderWeights(renderByLayer.get(index) != null ? renderByLayer.get(index) : renderWeightsEveryNEpochs)
                .build();

        return ret;
    }



    @Override
    public NeuralNetwork[] createNetworkLayers(int numLayers) {
        return new RBM[numLayers];
    }


    public static class Builder extends BaseMultiLayerNetwork.Builder<DBN> {

        private RBM.VisibleUnit visibleUnit = RBM.VisibleUnit.BINARY;
        private RBM.HiddenUnit hiddenUnit = RBM.HiddenUnit.BINARY;
        private Map<Integer,RBM.VisibleUnit> visibleUnitByLayer = new HashMap<>();
        private Map<Integer,RBM.HiddenUnit> hiddenUnitByLayer = new HashMap<>();
        private boolean useRBMPropUpAsActivation = false;

        public Builder() {
            this.clazz = DBN.class;
        }


        /**
         * Output layer weight initialization
         *
         * @param outputLayerWeightInit
         * @return
         */
        @Override
        public Builder outputLayerWeightInit(WeightInit outputLayerWeightInit) {
            super.outputLayerWeightInit(outputLayerWeightInit);
            return this;
        }

        /**
         * Layer specific weight init
         *
         * @param weightInitByLayer
         * @return
         */
        @Override
        public Builder weightInitByLayer(Map<Integer, WeightInit> weightInitByLayer) {
            super.weightInitByLayer(weightInitByLayer);
            return this;
        }

        /**
         * Default weight init scheme
         *
         * @param weightInit
         * @return
         */
        @Override
        public Builder weightInit(WeightInit weightInit) {
            super.weightInit(weightInit);
            return this;
        }


        /**
         * Whether to constrain gradient to unit norm or not
         * @param constrainGradientToUnitNorm
         * @return
         */
        public Builder constrainGradientToUnitNorm(boolean constrainGradientToUnitNorm) {
            super.constrainGradientToUnitNorm(constrainGradientToUnitNorm);
            return this;
        }
        public Builder useGaussNewtonVectorProductBackProp(boolean useGaussNewtonVectorProductBackProp) {
           super.useGaussNewtonVectorProductBackProp(useGaussNewtonVectorProductBackProp);
            return this;
        }

        /**
         * Use drop connect on activations or not
         *
         * @param useDropConnect use drop connect or not
         * @return builder pattern
         */
        @Override
        public  Builder useDropConnection(boolean useDropConnect) {
            super.useDropConnection(useDropConnect);
            return this;
        }


        /**
         * Output layer drop out
         *
         * @param outputLayerDropout
         * @return
         */
        @Override
        public Builder outputLayerDropout(double outputLayerDropout) {
             super.outputLayerDropout(outputLayerDropout);
            return this;
        }

        /**
         * Reset the adagrad epochs  after n iterations
         *
         * @param resetAdaGradIterations the number of iterations to reset adagrad after
         * @return
         */
        @Override
        public  Builder resetAdaGradIterations(int resetAdaGradIterations) {
            super.resetAdaGradIterations(resetAdaGradIterations);
            return this;
        }

        /**
         * Reset map for adagrad historical gradient by layer
         *
         * @param resetAdaGradEpochsByLayer
         * @return
         */
        @Override
        public Builder resetAdaGradEpochsByLayer(Map<Integer, Integer> resetAdaGradEpochsByLayer) {
            super.resetAdaGradEpochsByLayer(resetAdaGradEpochsByLayer);
            return this;
        }

        /**
         * Sets the momentum to the specified value for a given layer after a specified number of iterations
         *
         * @param momentumAfterByLayer the by layer momentum changes
         * @return
         */
        @Override
        public Builder momentumAfterByLayer(Map<Integer, Map<Integer, Double>> momentumAfterByLayer) {
            super.momentumAfterByLayer(momentumAfterByLayer);
            return this;
        }

        /**
         * Set the momentum to the value after the desired number of iterations
         *
         * @param momentumAfter the momentum after n iterations
         * @return
         */
        @Override
        public Builder momentumAfter(Map<Integer, Double> momentumAfter) {
            super.momentumAfter(momentumAfter);
            return this;
        }

        public Builder useRBMPropUpAsActivation(boolean useRBMPropUpAsActivation) {
            this.useRBMPropUpAsActivation = useRBMPropUpAsActivation;
            return this;
        }

        @Override
        public Builder lineSearchBackProp(boolean lineSearchBackProp) {
            super.lineSearchBackProp(lineSearchBackProp);
            return this;
        }

        /**
         * Loss function by layer
         *
         * @param lossFunctionByLayer the loss function per layer
         * @return builder pattern
         */
        @Override
        public Builder lossFunctionByLayer(Map<Integer, NeuralNetwork.LossFunction> lossFunctionByLayer) {
            super.lossFunctionByLayer(lossFunctionByLayer);
            return this;
        }

        /**
         * Sample or activate by layer allows for deciding to sample or just pass straight activations
         * for each layer
         *
         * @param sampleOrActivateByLayer
         * @return
         */
        @Override
        public Builder sampleOrActivateByLayer(Map<Integer, Boolean> sampleOrActivateByLayer) {
            super.sampleOrActivateByLayer(sampleOrActivateByLayer);
            return this;
        }

        @Override
        public Builder renderByLayer(Map<Integer, Integer> renderByLayer) {
            super.renderByLayer(renderByLayer);
            return this;
        }

        @Override
        public Builder learningRateForLayer(Map<Integer, Double> learningRates) {
            super.learningRateForLayer(learningRates);
            return this;
        }

        public Builder withVisibleUnitsByLayer(Map<Integer,RBM.VisibleUnit> visibleUnitByLayer) {
            this.visibleUnitByLayer.putAll(visibleUnitByLayer);
            return this;
        }

        public Builder withHiddenUnitsByLayer(Map<Integer,RBM.HiddenUnit> hiddenUnitByLayer) {
            this.hiddenUnitByLayer.putAll(hiddenUnitByLayer);
            return this;
        }

        public Builder withVisibleUnits(RBM.VisibleUnit visibleUnit) {
            this.visibleUnit = visibleUnit;
            return this;
        }


        public Builder withHiddenUnits(RBM.HiddenUnit hiddenUnit) {
            this.hiddenUnit = hiddenUnit;
            return this;
        }



        public Builder activateForLayer(Map<Integer,ActivationFunction> activationForLayer) {
            super.activateForLayer(activationForLayer);
            return this;
        }

        public Builder activateForLayer(int layer,ActivationFunction function) {
            super.activateForLayer(layer,function);
            return this;
        }

        /**
         * Activation function for output layer
         * @param outputActivationFunction the output activation function to use
         * @return builder pattern
         */
        public Builder withOutputActivationFunction(ActivationFunction outputActivationFunction) {
            super.withOutputActivationFunction(outputActivationFunction);
            return this;
        }


        /**
         * Output loss function
         * @param outputLossFunction the output loss function
         * @return
         */
        public Builder withOutputLossFunction(OutputLayer.LossFunction outputLossFunction) {
            super.withOutputLossFunction(outputLossFunction);
            return this;
        }

        /**
         * Which optimization algorithm to use with neural nets and Logistic regression
         * @param optimizationAlgo which optimization algorithm to use with
         * neural nets and logistic regression
         * @return builder pattern
         */
        public Builder withOptimizationAlgorithm(NeuralNetwork.OptimizationAlgorithm optimizationAlgo) {
            super.withOptimizationAlgorithm(optimizationAlgo);
            return this;
        }

        /**
         * Loss function to use with neural networks
         * @param lossFunction loss function to use with neural networks
         * @return builder pattern
         */
        public Builder withLossFunction(NeuralNetwork.LossFunction lossFunction) {
            super.withLossFunction(lossFunction);
            return this;
        }

        /**
         * Whether to use drop out on the neural networks or not:
         * random zero out of examples
         * @param dropOut the dropout to use
         * @return builder pattern
         */
        public Builder withDropOut(double dropOut) {
            super.withDropOut(dropOut);
            return this;
        }

        /**
         * Whether to use hidden layer activations or neural network sampling
         * on feed forward pass
         * @param useHiddenActivationsForwardProp true if use hidden activations, false otherwise
         * @return builder pattern
         */
        public Builder sampleFromHiddenActivations(boolean useHiddenActivationsForwardProp) {
            super.sampleFromHiddenActivations(useHiddenActivationsForwardProp);
            return this;
        }

        /**
         * Turn this off for full dataset training
         * @param normalizeByInputRows whether to normalize the changes
         * by the number of input rows
         * @return builder pattern
         */
        public Builder normalizeByInputRows(boolean normalizeByInputRows) {
            super.normalizeByInputRows(normalizeByInputRows);
            return this;
        }




        public Builder useAdaGrad(boolean useAdaGrad) {
            super.useAdaGrad(useAdaGrad);
            return this;
        }

        public Builder withSparsity(double sparsity) {
            super.withSparsity(sparsity);
            return this;
        }


        public Builder withVisibleBiasTransforms(Map<Integer,MatrixTransform> visibleBiasTransforms) {
            super.withVisibleBiasTransforms(visibleBiasTransforms);
            return this;
        }

        public Builder withHiddenBiasTransforms(Map<Integer,MatrixTransform> hiddenBiasTransforms) {
            super.withHiddenBiasTransforms(hiddenBiasTransforms);
            return this;
        }

        /**
         * Forces use of number of epochs for training
         * SGD style rather than conjugate gradient
         * @return
         */
        public Builder forceEpochs() {
            shouldForceEpochs = true;
            return this;
        }

        /**
         * Disables back propagation
         * @return
         */
        public Builder disableBackProp() {
            backProp = false;
            return this;
        }

        /**
         * Transform the weights at the given layer
         * @param layer the layer to applyTransformToOrigin
         * @param transform the function used for transformation
         * @return
         */
        public Builder transformWeightsAt(int layer,MatrixTransform transform) {
            weightTransforms.put(layer,transform);
            return this;
        }

        /**
         * A map of transformations for transforming
         * the given layers
         * @param transforms
         * @return
         */
        public Builder transformWeightsAt(Map<Integer,MatrixTransform> transforms) {
            weightTransforms.putAll(transforms);
            return this;
        }

        /**
         * Probability distribution for generating weights
         * @param dist
         * @return
         */
        public Builder withDist(RealDistribution dist) {
            super.withDist(dist);
            return this;
        }

        /**
         * Specify momentum
         * @param momentum
         * @return
         */
        public Builder withMomentum(double momentum) {
            super.withMomentum(momentum);
            return this;
        }

        /**
         * Use l2 reg
         * @param useRegularization
         * @return
         */
        public Builder useRegularization(boolean useRegularization) {
            super.useRegularization(useRegularization);
            return this;
        }

        /**
         * L2 coefficient
         * @param l2
         * @return
         */
        public Builder withL2(double l2) {
            super.withL2(l2);
            return this;
        }

        /**
         * Whether to plot weights or not
         * @param everyN
         * @return
         */
        public Builder renderWeights(int everyN) {
            super.renderWeights(everyN);
            return this;
        }


        /**
         * Pick an activation function, default is sigmoid
         * @param activation
         * @return
         */
        public Builder withActivation(ActivationFunction activation) {
            super.withActivation(activation);
            return this;
        }


        @Override
        public Builder numberOfInputs(int nIns) {
            super.numberOfInputs(nIns);
            return this;
        }

        @Override
        public Builder hiddenLayerSizes(Integer[] hiddenLayerSizes) {
            super.hiddenLayerSizes(hiddenLayerSizes);
            return this;
        }

        @Override
        public Builder hiddenLayerSizes(int[] hiddenLayerSizes) {
            super.hiddenLayerSizes(hiddenLayerSizes);
            return this;
        }

        @Override
        public Builder numberOfOutPuts(int nOuts) {
            super.numberOfOutPuts(nOuts);
            return this;
        }

        @Override
        public Builder withRng(RandomGenerator gen) {
            super.withRng(gen);
            return this;
        }

        @Override
        public Builder withInput(INDArray input) {
            super.withInput(input);
            return this;
        }

        @Override
        public Builder withLabels(INDArray labels) {
            super.withLabels(labels);
            return this;
        }

        @Override
        public Builder withClazz(Class<? extends BaseMultiLayerNetwork> clazz) {
            this.clazz =  clazz;
            return this;
        }



        @Override
        public DBN build() {
            DBN ret = super.build();
            ret.useRBMPropUpAsActivations = useRBMPropUpAsActivation;
            ret.hiddenUnit = hiddenUnit;
            ret.visibleUnit = visibleUnit;
            ret.visibleUnitByLayer.putAll(visibleUnitByLayer);
            ret.hiddenUnitByLayer.putAll(hiddenUnitByLayer);
            ret.initializeLayers(NDArrays.zeros(1, ret.getnIns()));
            return ret;
        }
    }




}