package org.deeplearning4j.nn;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.datasets.DataSet;
import org.deeplearning4j.nn.NeuralNetwork.LossFunction;
import org.deeplearning4j.nn.NeuralNetwork.OptimizationAlgorithm;
import org.deeplearning4j.nn.activation.ActivationFunction;
import org.deeplearning4j.nn.activation.Activations;
import org.deeplearning4j.nn.activation.Sigmoid;
import org.deeplearning4j.optimize.MultiLayerNetworkOptimizer;
import org.deeplearning4j.optimize.TrainingEvaluator;
import org.deeplearning4j.rng.SynchronizedRandomGenerator;
import org.deeplearning4j.transformation.MatrixTransform;
import org.deeplearning4j.util.Dl4jReflection;
import org.deeplearning4j.util.MatrixUtil;
import org.deeplearning4j.util.SerializationUtils;
import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;


/**
 * A base class for a multi layer neural network with a logistic output layer
 * and multiple hidden layers.
 * @author Adam Gibson
 *
 */
public abstract class BaseMultiLayerNetwork implements Serializable,Persistable {



    private static Logger log = LoggerFactory.getLogger(BaseMultiLayerNetwork.class);
    private static final long serialVersionUID = -5029161847383716484L;
    //number of columns in the input matrix
    protected int nIns;
    //the hidden layer sizes at each layer
    protected int[] hiddenLayerSizes;
    //the number of outputs/labels for logistic regression
    protected int nOuts;
    //the hidden layers
    protected HiddenLayer[] sigmoidLayers;
    //logistic regression output layer (aka the softmax layer) for translating network outputs in to probabilities
    protected OutputLayer outputLayer;
    protected RandomGenerator rng;
    /* probability distribution for generation of weights */
    protected RealDistribution dist;
    protected double momentum = 0.1;
    //default training examples and associated layers
    protected DoubleMatrix input,labels;
    protected MultiLayerNetworkOptimizer optimizer;
    //activation function for each hidden layer
    protected ActivationFunction activation = new Sigmoid();
    protected boolean buildAutoEncoder;
    //l2 regularization constant for weight decay
    protected double l2 = 0.01;
    //whether to initialize layers
    protected boolean shouldInit = true;
    //fan in for uniform distributions
    protected double fanIn = -1;
    //whether to render weights or not; anything <=0 will not render the weights
    protected int renderWeightsEveryNEpochs = -1;
    protected boolean useRegularization = false;
    //sometimes we may need to transform weights; this allows a
    //weight transform upon layer setup
    protected Map<Integer,MatrixTransform> weightTransforms = new HashMap<>();
    //hidden bias transforms; for initialization
    protected Map<Integer,MatrixTransform> hiddenBiasTransforms = new HashMap<>();
    //visible bias transforms for initialization
    protected Map<Integer,MatrixTransform> visibleBiasTransforms = new HashMap<>();

    protected boolean shouldBackProp = true;
    //whether to only train a certain number of epochs
    protected boolean forceNumEpochs = false;
    //don't use sparsity by default
    protected double sparsity = 0;
    //optional: used in normalizing input. This is used in saving the model for prediction purposes in normalizing incoming data
    protected DoubleMatrix columnSums;
    //subtract input by column means for zero mean
    protected DoubleMatrix  columnMeans;
    //divide by the std deviation
    protected DoubleMatrix columnStds;
    protected boolean initCalled = false;
    protected boolean useHiddenActivationsForwardProp = true;
    /*
     * Use adagrad or not
     */
    protected boolean useAdaGrad = false;
    /**
     * Override the activation function for a particular layer
     */
    protected Map<Integer,ActivationFunction> activationFunctionForLayer = new HashMap<>();

    /*
     * Hinton's Practical guide to RBMS:
     *
     * Learning rate updates over time.
     * Usually when weights have a large fan in (starting point for a random distribution during initialization)
     * you want a smaller update rate.
     *
     * For biases this can be bigger.
     */
    protected double learningRateUpdate = 0.95;
    /*
     * Any neural networks used as layers.
     * This will always have an equivalent sigmoid layer
     * with shared weight matrices for training.
     *
     * Typically, this is some mix of:
     *        RBMs,Denoising AutoEncoders, or their Continuous counterparts
     */
    protected NeuralNetwork[] layers;

    /*
     * The delta from the previous iteration to this iteration for
     * cross entropy must change >= this amount in order to continue.
     */
    protected double errorTolerance = 0.0001;

    /*
     * Drop out: randomly zero examples
     */
    protected double dropOut = 0;

    /*
     * Normalize by input rows with gradients or not
     */
    protected boolean normalizeByInputRows = false;

    /**
     * Which optimization algorithm to use: SGD or CG
     */
    protected OptimizationAlgorithm optimizationAlgorithm;
    /**
     * Which loss function to use:
     * Squared loss, Reconstruction entropy, negative log likelihood
     */
    protected LossFunction lossFunction;

    /**
     * Loss function for output layer
     */
    protected OutputLayer.LossFunction outputLossFunction;
    /**
     * Output activation function
     */
    protected ActivationFunction outputActivationFunction;
    /**
     * Layer specific learning rates
     */
    protected Map<Integer,Double> layerLearningRates = new HashMap<>();



    /* Reflection/factory constructor */
    protected BaseMultiLayerNetwork() {}

    protected BaseMultiLayerNetwork(int nIns, int[] hiddenLayerSizes, int nOuts, int nLayers, RandomGenerator rng) {
        this(nIns,hiddenLayerSizes,nOuts,nLayers,rng,null,null);
    }


    protected BaseMultiLayerNetwork(int nIn, int[] hiddenLayerSizes, int nOuts
            , int nLayers, RandomGenerator rng,DoubleMatrix input,DoubleMatrix labels) {
        this.nIns = nIn;
        this.hiddenLayerSizes = hiddenLayerSizes;
        this.input = input.dup();
        this.labels = labels.dup();

        if(hiddenLayerSizes.length != nLayers)
            throw new IllegalArgumentException("The number of hidden layer sizes must be equivalent to the nLayers argument which is a value of " + nLayers);

        this.nOuts = nOuts;
        this.setnLayers(nLayers);

        this.sigmoidLayers = new HiddenLayer[nLayers];

        if(rng == null)
            this.rng = new SynchronizedRandomGenerator(new MersenneTwister(123));
        else
            this.rng = rng;

        if(input != null)
            initializeLayers(input);
    }



    /* sanity check for hidden layer and inter layer dimensions */
    public void dimensionCheck() {

        for(int i = 0; i < getnLayers(); i++) {
            HiddenLayer h = sigmoidLayers[i];
            NeuralNetwork network = layers[i];
            h.getW().assertSameSize(network.getW());
            h.getB().assertSameSize(network.gethBias());

            if(i < getnLayers() - 1) {
                HiddenLayer h1 = sigmoidLayers[i + 1];
                NeuralNetwork network1 = layers[i + 1];
                if(h1.getnIn() != h.getnOut())
                    throw new IllegalStateException("Invalid structure: hidden layer in for " + (i + 1) + " not equal to number of ins " + i);
                if(network.getnHidden() != network1.getnVisible())
                    throw new IllegalStateException("Invalid structure: network hidden for " + (i + 1) + " not equal to number of visible " + i);

            }
        }

        if(sigmoidLayers[sigmoidLayers.length - 1].getnOut() != outputLayer.getnIn())
            throw new IllegalStateException("Number of outputs for final hidden layer not equal to the number of logistic input units for output layer");


    }


    /**
     * Synchronizes the rng, this is mean for use with scale out methods
     */
    public void synchonrizeRng() {
        RandomGenerator rgen = new SynchronizedRandomGenerator(rng);
        for(int i = 0; i < getnLayers(); i++) {
            layers[i].setRng(rgen);
            sigmoidLayers[i].setRng(rgen);
        }


    }







    /**
     * Base class for initializing the layers based on the input.
     * This is meant for capturing numbers such as input columns or other things.
     * @param input the input matrix for training
     */
    public void initializeLayers(DoubleMatrix input) {
        log.info("Initializing layers with input of dims " + input.rows + " x " + input.columns);
        if(input == null)
            throw new IllegalArgumentException("Unable to initialize layers with empty input");

        if(input.columns != nIns)
            throw new IllegalArgumentException(String.format("Unable to train on number of inputs; columns should be equal to number of inputs. Number of inputs was %d while number of columns was %d",nIns,input.columns));

        if(this.layers == null)
            this.layers = new NeuralNetwork[getnLayers()];

        for(int i = 0; i < hiddenLayerSizes.length; i++)
            if(hiddenLayerSizes[i] < 1)
                throw new IllegalArgumentException("All hidden layer sizes must be >= 1");



        this.input = input.dup();
        if(!initCalled)
            init();
        else
            feedForward(input);

    }

    public void init() {
        DoubleMatrix layerInput = input;
        if(!(rng instanceof SynchronizedRandomGenerator))
            rng = new SynchronizedRandomGenerator(rng);
        int inputSize;
        if(getnLayers() < 1)
            throw new IllegalStateException("Unable to create network layers; number specified is less than 1");

        if(this.dist == null)
            this.dist = new NormalDistribution(rng,0,.01,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);



        if(this.layers == null || sigmoidLayers == null || this.sigmoidLayers[0] == null || this.layers[0] == null) {
            this.layers = new NeuralNetwork[getnLayers()];
            // construct multi-layer
            for(int i = 0; i < this.getnLayers(); i++) {
                ActivationFunction currLayerActivation = activationFunctionForLayer.get(i) != null ?activationFunctionForLayer.get(i) : activation;

                if(i == 0)
                    inputSize = this.nIns;
                else
                    inputSize = this.hiddenLayerSizes[i-1];

                if(i == 0) {
                    // construct sigmoid_layer
                    sigmoidLayers[i] = createHiddenLayer(i,inputSize,this.hiddenLayerSizes[i],currLayerActivation,rng,layerInput,dist);
                }
                else {
                    if(input != null) {
                        if(this.useHiddenActivationsForwardProp)
                            layerInput = sigmoidLayers[i - 1].sampleHiddenGivenVisible();
                        else
                            layerInput = getLayers()[i - 1].sampleHiddenGivenVisible(layerInput).getSecond();

                    }

                    // construct sigmoid_layer
                    sigmoidLayers[i] = createHiddenLayer(i,inputSize,this.hiddenLayerSizes[i],currLayerActivation,rng,layerInput,dist);


                }

                this.layers[i] = createLayer(layerInput,inputSize, this.hiddenLayerSizes[i], this.sigmoidLayers[i].getW(), this.sigmoidLayers[i].getB(), null, rng,i);

            }

        }



        // layer for output using OutputLayer
        this.outputLayer = new OutputLayer.Builder()
                .useAdaGrad(useAdaGrad).optimizeBy(getOptimizationAlgorithm())
                .normalizeByInputRows(normalizeByInputRows).withLossFunction(outputLossFunction)
                .useRegularization(useRegularization).withActivationFunction(outputActivationFunction)
                .numberOfInputs(getSigmoidLayers()[getSigmoidLayers().length - 1].nOut)
                .numberOfOutputs(nOuts).withL2(l2).build();

        synchonrizeRng();

        dimensionCheck();
        applyTransforms();
        initCalled = true;

    }

    /**
     * Triggers the activation of the last hidden layer ie: not logistic regression
     * @return the activation of the last hidden layer given the last input to the network
     */
    public DoubleMatrix activate() {
        return getSigmoidLayers()[getSigmoidLayers().length - 1].activate();
    }

    /**
     * Triggers the activation for a given layer
     * @param layer the layer to activate on
     * @return the activation for a given layer
     */
    public DoubleMatrix activate(int layer) {
        return getSigmoidLayers()[layer].activate();
    }

    /**
     * Triggers the activation of the given layer
     * @param layer the layer to trigger on
     * @param input the input to the hidden layer
     * @return the activation of the layer based on the input
     */
    public DoubleMatrix activate(int layer, DoubleMatrix input) {
        return getSigmoidLayers()[layer].activate(input);
    }


    /**
     * Finetunes with the current cached labels
     * @param lr the learning rate to use
     * @param epochs the max number of epochs to finetune with
     */
    public void finetune(double lr, int epochs) {
        finetune(this.labels,lr,epochs);

    }

    /**
     * Sets the input and labels from this dataset
     * @param data the dataset to initialize with
     */
    public void initialize(DataSet data) {
        setInput(data.getFirst());
        feedForward(data.getFirst());
        this.labels = data.getSecond();
        outputLayer.setLabels(labels);
    }






    /**
     * Compute activations from input to output of the output layer
     * @return the list of activations for each layer
     */
    public  List<DoubleMatrix> feedForward() {
        DoubleMatrix currInput = this.input;

        List<DoubleMatrix> activations = new ArrayList<>();
        activations.add(currInput);
        for(int i = 0; i < getnLayers(); i++) {
            getLayers()[i].setInput(currInput);
            getSigmoidLayers()[i].setInput(input);
            if(useHiddenActivationsForwardProp)
                currInput = getSigmoidLayers()[i].sampleHGivenV(currInput);
            else
                currInput = getLayers()[i].sampleHiddenGivenVisible(currInput).getSecond();
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
     * Compute activations from input to output of the output layer
     * @return the list of activations for each layer
     */
    public  List<DoubleMatrix> feedForward(DoubleMatrix input) {
        if(input == null)
            throw new IllegalStateException("Unable to perform feed forward; no input found");

        else
            this.input = input;
        return feedForward();
    }

    /* delta computation for back prop */
    protected  void computeDeltas(List<Pair<DoubleMatrix,DoubleMatrix>> deltaRet) {
        DoubleMatrix[] gradients = new DoubleMatrix[getnLayers() + 2];
        DoubleMatrix[] deltas = new DoubleMatrix[getnLayers() + 2];
        ActivationFunction derivative = getSigmoidLayers()[0].getActivationFunction();
        ActivationFunction softMaxDerivative = Activations.softmax();
        //- y - h
        DoubleMatrix delta = null;
        List<DoubleMatrix> activations = feedForward();

		/*
		 * Precompute activations and z's (pre activation network outputs)
		 */
        List<DoubleMatrix> weights = new ArrayList<>();
        for(int j = 0; j < getLayers().length; j++)
            weights.add(getLayers()[j].getW());
        weights.add(getOutputLayer().getW());



        //errors
        for(int i = getnLayers() + 1; i >= 0; i--) {
            //output layer
            if(i >= getnLayers() + 1) {
                //-( y - h) .* f'(z^l) where l is the output layer
                delta = labels.sub(activations.get(i)).neg().mul(softMaxDerivative.applyDerivative(activations.get(i)));
                deltas[i] = delta;

            }
            else {
                //W^t * error^l + 1
                deltas[i] =  deltas[i + 1].mmul(weights.get(i).transpose()).muli(derivative.applyDerivative(activations.get(i)));

                //calculate gradient for layer
                DoubleMatrix newGradient = deltas[i + 1].transpose().mmul(activations.get(i));
                gradients[i] = newGradient;
            }

        }

        for(int i = 0; i < gradients.length; i++)
            deltaRet.add(new Pair<>(gradients[i],deltas[i]));

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseMultiLayerNetwork)) return false;

        BaseMultiLayerNetwork that = (BaseMultiLayerNetwork) o;

        if (buildAutoEncoder != that.buildAutoEncoder) return false;
        if (Double.compare(that.dropOut, dropOut) != 0) return false;
        if (Double.compare(that.errorTolerance, errorTolerance) != 0) return false;
        if (Double.compare(that.fanIn, fanIn) != 0) return false;
        if (forceNumEpochs != that.forceNumEpochs) return false;
        if (initCalled != that.initCalled) return false;
        if (Double.compare(that.l2, l2) != 0) return false;
        if (Double.compare(that.learningRateUpdate, learningRateUpdate) != 0) return false;
        if (Double.compare(that.momentum, momentum) != 0) return false;
        if (nIns != that.nIns) return false;
        if (nOuts != that.nOuts) return false;
        if (normalizeByInputRows != that.normalizeByInputRows) return false;
        if (renderWeightsEveryNEpochs != that.renderWeightsEveryNEpochs) return false;
        if (shouldBackProp != that.shouldBackProp) return false;
        if (shouldInit != that.shouldInit) return false;
        if (Double.compare(that.sparsity, sparsity) != 0) return false;
        if (useAdaGrad != that.useAdaGrad) return false;
        if (useHiddenActivationsForwardProp != that.useHiddenActivationsForwardProp) return false;
        if (useRegularization != that.useRegularization) return false;
        if (activation != null ? !activation.equals(that.activation) : that.activation != null) return false;
        if (activationFunctionForLayer != null ? !activationFunctionForLayer.equals(that.activationFunctionForLayer) : that.activationFunctionForLayer != null)
            return false;
        if (columnMeans != null ? !columnMeans.equals(that.columnMeans) : that.columnMeans != null) return false;
        if (columnStds != null ? !columnStds.equals(that.columnStds) : that.columnStds != null) return false;
        if (columnSums != null ? !columnSums.equals(that.columnSums) : that.columnSums != null) return false;
        if (hiddenBiasTransforms != null ? !hiddenBiasTransforms.equals(that.hiddenBiasTransforms) : that.hiddenBiasTransforms != null)
            return false;
        if (!Arrays.equals(hiddenLayerSizes, that.hiddenLayerSizes)) return false;
        if (input != null ? !input.equals(that.input) : that.input != null) return false;
        if (labels != null ? !labels.equals(that.labels) : that.labels != null) return false;
        if (!Arrays.equals(layers, that.layers)) return false;
        if (lossFunction != that.lossFunction) return false;
        if (optimizationAlgorithm != that.optimizationAlgorithm) return false;
        if (optimizer != null ? !optimizer.equals(that.optimizer) : that.optimizer != null) return false;
        if (outputActivationFunction != null ? !outputActivationFunction.equals(that.outputActivationFunction) : that.outputActivationFunction != null)
            return false;
        if (outputLayer != null ? !outputLayer.equals(that.outputLayer) : that.outputLayer != null) return false;
        if (outputLossFunction != that.outputLossFunction) return false;
        if (!Arrays.equals(sigmoidLayers, that.sigmoidLayers)) return false;
        if (visibleBiasTransforms != null ? !visibleBiasTransforms.equals(that.visibleBiasTransforms) : that.visibleBiasTransforms != null)
            return false;
        if (weightTransforms != null ? !weightTransforms.equals(that.weightTransforms) : that.weightTransforms != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = nIns;
        result = 31 * result + (hiddenLayerSizes != null ? Arrays.hashCode(hiddenLayerSizes) : 0);
        result = 31 * result + nOuts;
        result = 31 * result + (sigmoidLayers != null ? Arrays.hashCode(sigmoidLayers) : 0);
        result = 31 * result + (outputLayer != null ? outputLayer.hashCode() : 0);
        temp = Double.doubleToLongBits(momentum);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (input != null ? input.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (optimizer != null ? optimizer.hashCode() : 0);
        result = 31 * result + (activation != null ? activation.hashCode() : 0);
        result = 31 * result + (buildAutoEncoder ? 1 : 0);
        temp = Double.doubleToLongBits(l2);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (shouldInit ? 1 : 0);
        temp = Double.doubleToLongBits(fanIn);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + renderWeightsEveryNEpochs;
        result = 31 * result + (useRegularization ? 1 : 0);
        result = 31 * result + (weightTransforms != null ? weightTransforms.hashCode() : 0);
        result = 31 * result + (hiddenBiasTransforms != null ? hiddenBiasTransforms.hashCode() : 0);
        result = 31 * result + (visibleBiasTransforms != null ? visibleBiasTransforms.hashCode() : 0);
        result = 31 * result + (shouldBackProp ? 1 : 0);
        result = 31 * result + (forceNumEpochs ? 1 : 0);
        temp = Double.doubleToLongBits(sparsity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (columnSums != null ? columnSums.hashCode() : 0);
        result = 31 * result + (columnMeans != null ? columnMeans.hashCode() : 0);
        result = 31 * result + (columnStds != null ? columnStds.hashCode() : 0);
        result = 31 * result + (initCalled ? 1 : 0);
        result = 31 * result + (useHiddenActivationsForwardProp ? 1 : 0);
        result = 31 * result + (useAdaGrad ? 1 : 0);
        result = 31 * result + (activationFunctionForLayer != null ? activationFunctionForLayer.hashCode() : 0);
        temp = Double.doubleToLongBits(learningRateUpdate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (layers != null ? Arrays.hashCode(layers) : 0);
        temp = Double.doubleToLongBits(errorTolerance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(dropOut);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (normalizeByInputRows ? 1 : 0);
        result = 31 * result + (optimizationAlgorithm != null ? optimizationAlgorithm.hashCode() : 0);
        result = 31 * result + (lossFunction != null ? lossFunction.hashCode() : 0);
        result = 31 * result + (outputLossFunction != null ? outputLossFunction.hashCode() : 0);
        result = 31 * result + (outputActivationFunction != null ? outputActivationFunction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BaseMultiLayerNetwork{" +
                "nIns=" + nIns +
                ", hiddenLayerSizes=" + Arrays.toString(hiddenLayerSizes) +
                ", nOuts=" + nOuts +
                ", sigmoidLayers=" + Arrays.toString(sigmoidLayers) +
                ", outputLayer=" + outputLayer +
                ", rng=" + rng +
                ", dist=" + dist +
                ", momentum=" + momentum +
                ", input=" + input +
                ", labels=" + labels +
                ", optimizer=" + optimizer +
                ", activation=" + activation +
                ", l2=" + l2 +
                ", shouldInit=" + shouldInit +
                ", fanIn=" + fanIn +
                ", renderWeightsEveryNEpochs=" + renderWeightsEveryNEpochs +
                ", useRegularization=" + useRegularization +
                ", weightTransforms=" + weightTransforms +
                ", hiddenBiasTransforms=" + hiddenBiasTransforms +
                ", visibleBiasTransforms=" + visibleBiasTransforms +
                ", shouldBackProp=" + shouldBackProp +
                ", forceNumEpochs=" + forceNumEpochs +
                ", sparsity=" + sparsity +
                ", columnSums=" + columnSums +
                ", columnMeans=" + columnMeans +
                ", columnStds=" + columnStds +
                ", initCalled=" + initCalled +
                ", useHiddenActivationsForwardProp=" + useHiddenActivationsForwardProp +
                ", useAdaGrad=" + useAdaGrad +
                ", learningRateUpdate=" + learningRateUpdate +
                ", layers=" + Arrays.toString(layers) +
                ", errorTolerance=" + errorTolerance +
                ", dropOut=" + dropOut +
                ", normalizeByInputRows=" + normalizeByInputRows +
                ", optimizationAlgorithm=" + optimizationAlgorithm +
                ", lossFunction=" + lossFunction +
                '}';
    }

    @Override
    public BaseMultiLayerNetwork clone() {
        BaseMultiLayerNetwork ret = new Builder<>().withClazz(getClass()).buildEmpty();
        ret.update(this);
        return ret;
    }








    /**
     * Backpropagation of errors for weights
     * @param lr the learning rate to use
     * @param epochs  the number of epochs to iterate (this is already called in finetune)
     * @param eval the evaluator for stopping
     */
    public void backProp(double lr,int epochs,TrainingEvaluator eval) {

        Double lastEntropy = this.score();
        //store a copy of the network for when binary cross entropy gets
        //worse after an iteration
        BaseMultiLayerNetwork revert = clone();
        //sgd style; only train a certain number of epochs
        if(forceNumEpochs) {
            for(int i = 0; i < epochs; i++) {
                backPropStep(revert,lr,i);
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
                backPropStep(revert,lr,count);
                /* Trains logistic regression post weight updates */
                getOutputLayer().trainTillConvergence(lr, epochs,eval);
                if(eval != null && eval.shouldStop(count))
                    break;
                Double entropy = score();


                if(lastEntropy == null || entropy < lastEntropy) {

                    if(lastEntropy > 0 && entropy < 0) {
                        log.info("Breaking...change of sign on backprop");
                    }
                    double diff = Math.abs(entropy - lastEntropy);
                    if(diff < changeTolerance) {
                        log.info("Not enough of a change on back prop...breaking");
                        break;
                    }
                    else
                        lastEntropy = entropy;
                    log.info("New negative log likelihood " + lastEntropy);
                    revert = clone();
                }

                else if(entropy >= lastEntropy) {
                    train = false;
                    update(revert);

                }

                backPropIterations++;
            }


        }
    }

    /**
     * Backpropagation of errors for weights
     * @param lr the learning rate to use
     * @param epochs  the number of epochs to iterate (this is already called in finetune)
     */
    public void backProp(double lr,int epochs) {

        Double lastEntropy = this.score();
        //store a copy of the network for when binary cross entropy gets
        //worse after an iteration
        BaseMultiLayerNetwork revert = clone();
        //sgd style; only train a certain number of epochs
        if(forceNumEpochs) {
            for(int i = 0; i < epochs; i++) {
                backPropStep(revert,lr,i);
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
                backPropStep(revert,lr,count);
                /* Trains logistic regression post weight updates */
                getOutputLayer().trainTillConvergence(lr, epochs);

                Double entropy = score();
                if(lastEntropy == null || entropy < lastEntropy) {
                    double diff = Math.abs(entropy - lastEntropy);
                    if(diff < changeTolerance) {
                        log.info("Not enough of a change on back prop...breaking");
                        break;
                    }
                    else
                        lastEntropy = entropy;
                    log.info("New score " + lastEntropy);
                    revert = clone();
                }

                else if(entropy >= lastEntropy) {
                    train = false;
                    update(revert);

                }

                backPropIterations++;
            }


        }
    }

    public Map<Integer, ActivationFunction> getActivationFunctionForLayer() {
        return activationFunctionForLayer;
    }

    public void setActivationFunctionForLayer(Map<Integer, ActivationFunction> activationFunctionForLayer) {
        this.activationFunctionForLayer = activationFunctionForLayer;
    }

    /**
     * Do a back prop iteration.
     * This involves computing the activations, tracking the last layers weights
     * to revert to in case of convergence, the learning rate being used to train
     * and the current epoch
     * @param revert the best network so far
     * @param lr the learning rate to use for training
     * @param epoch the epoch to use
     * @return whether the training should converge or not
     */
    protected void backPropStep(BaseMultiLayerNetwork revert,double lr,int epoch) {
        //feedforward to compute activations
        //initial error
        log.info("Back prop step " + epoch);

        //precompute deltas
        List<Pair<DoubleMatrix,DoubleMatrix>> deltas = new ArrayList<>();
        //compute derivatives and gradients given activations
        computeDeltas(deltas);


        for(int l = 0; l < getnLayers(); l++) {
            DoubleMatrix gradientChange = deltas.get(l).getFirst();
            //get the gradient
            if(isUseAdaGrad())
                gradientChange.muli(getLayers()[l].getAdaGrad().getLearningRates(gradientChange));

            else
                gradientChange.muli(lr);

            //l2
            if(useRegularization)
                gradientChange.muli(getLayers()[l].getW().mul(l2));

            if(momentum != 0)
                gradientChange.muli(momentum);

            if(this.isNormalizeByInputRows())
                gradientChange.divi(input.rows);

            //update W
            getLayers()[l].getW().subi(gradientChange);
            getSigmoidLayers()[l].setW(layers[l].getW());


            //update hidden bias
            DoubleMatrix deltaColumnSums = deltas.get(l + 1).getSecond().columnSums();

            if(sparsity != 0)
                deltaColumnSums = MatrixUtil.scalarMinus(sparsity, deltaColumnSums);

            if(useAdaGrad)
                deltaColumnSums.muli(layers[l].gethBiasAdaGrad().getLearningRates(deltaColumnSums));
            else
                deltaColumnSums.muli(lr);

            if(momentum != 0)
                deltaColumnSums.muli(momentum);

            if(isNormalizeByInputRows())
                deltaColumnSums.divi(input.rows);


            getLayers()[l].gethBias().subi(deltaColumnSums);
            getSigmoidLayers()[l].setB(getLayers()[l].gethBias());
        }

        DoubleMatrix logLayerGradient = deltas.get(getnLayers()).getFirst();
        DoubleMatrix biasGradient = deltas.get(getnLayers()).getSecond().columnSums();

        if(momentum != 0)
            logLayerGradient.muli(momentum);


        if(useAdaGrad)
            logLayerGradient.muli(outputLayer.getAdaGrad().getLearningRates(logLayerGradient));


        else
            logLayerGradient.muli(lr);

        if(isNormalizeByInputRows())
            logLayerGradient.divi(input.rows);



        if(momentum != 0)
            biasGradient.muli(momentum);

        if(useAdaGrad)
            biasGradient.muli(outputLayer.getBiasAdaGrad().getLearningRates(biasGradient));
        else
            biasGradient.muli(lr);

        if(isNormalizeByInputRows())
            biasGradient.divi(input.rows);


        getOutputLayer().getW().subi(logLayerGradient);

        if(getOutputLayer().getB().length == biasGradient.length)
            getOutputLayer().getB().subi(biasGradient);
        else
            getOutputLayer().getB().subi(biasGradient.mean());

    }


    /**
     * Run SGD based on the given labels
     * @param labels the labels to use
     * @param lr the learning rate during training
     * @param epochs the number of times to iterate
     */
    public void finetune(DoubleMatrix labels,double lr, int epochs) {
        feedForward();
        if(labels != null)
            this.labels = labels;
        optimizer = new MultiLayerNetworkOptimizer(this,lr);
        optimizer.optimize(this.labels, lr,epochs);
    }

    /**
     * Run training algorithm based on the given labels
     * @param labels the labels to use
     * @param lr the learning rate during training
     * @param epochs the number of times to iterate
     */
    public void finetune(DoubleMatrix labels,double lr, int epochs,TrainingEvaluator eval) {
        feedForward();
        if(labels != null)
            this.labels = labels;
        optimizer = new MultiLayerNetworkOptimizer(this,lr);
        optimizer.optimize(this.labels, lr,epochs,eval);
    }




    /**
     * Label the probabilities of the input
     * @param x the input to label
     * @return a vector of probabilities
     * given each label.
     *
     * This is typically of the form:
     * [0.5, 0.5] or some other probability distribution summing to one
     */
    public DoubleMatrix output(DoubleMatrix x) {
        List<DoubleMatrix> activations = feedForward(x);
        if(columnSums != null) {
            for(int i = 0; i < x.columns; i++) {
                DoubleMatrix col = x.getColumn(i);
                col = col.div(columnSums.get(0,i));
                x.putColumn(i, col);
            }
        }

        if(columnMeans != null) {
            for(int i = 0; i < x.columns; i++) {
                DoubleMatrix col = x.getColumn(i);
                col = col.sub(columnMeans.get(0,i));
                x.putColumn(i, col);
            }
        }

        if(columnStds != null) {
            for(int i = 0; i < x.columns; i++) {
                DoubleMatrix col = x.getColumn(i);
                col = col.div(columnStds.get(0,i));
                x.putColumn(i, col);
            }
        }


        if(this.input == null) {
            this.initializeLayers(x);
        }

        //second to last activation is input
        DoubleMatrix predicted = activations.get(activations.size() - 1);
        return predicted;
    }


    /**
     * Reconstructs the input.
     * This is equivalent functionality to a
     * deep autoencoder.
     * @param x the input to reconstruct
     * @param layerNum the layer to output for encoding
     * @return a reconstructed matrix
     * relative to the size of the last hidden layer.
     * This is great for data compression and visualizing
     * high dimensional data (or just doing dimensionality reduction).
     *
     * This is typically of the form:
     * [0.5, 0.5] or some other probability distribution summing to one
     */
    public DoubleMatrix reconstruct(DoubleMatrix x,int layerNum) {
        DoubleMatrix currInput = x;

        for(int i = 0; i < layerNum -1; i++) {
            getLayers()[i].setInput(currInput);
            getSigmoidLayers()[i].setInput(input);
            if(useHiddenActivationsForwardProp)
                currInput = getSigmoidLayers()[i].activate(currInput);
            else
                currInput = getLayers()[i].sampleHiddenGivenVisible(currInput).getSecond();

        }
        return getLayers()[layerNum - 1].reconstruct(currInput);

    }

    /**
     * Reconstruct from the final layer
     * @param x the input to reconstruct
     * @return the reconstructed input
     */
    public DoubleMatrix reconstruct(DoubleMatrix x) {
        return reconstruct(x,sigmoidLayers.length);
    }



    @Override
    public void write(OutputStream os) {
        SerializationUtils.writeObject(this, os);
    }

    /**
     * Load (using {@link ObjectInputStream}
     * @param is the input stream to load from (usually a file)
     */
    @Override
    public void load(InputStream is) {
        BaseMultiLayerNetwork loaded = SerializationUtils.readObject(is);
        update(loaded);
    }



    /**
     * Assigns the parameters of this model to the ones specified by this
     * network. This is used in loading from input streams, factory methods, etc
     * @param network the network to get parameters from
     */
    protected  void update(BaseMultiLayerNetwork network) {
        if(network.layers != null && network.getnLayers() > 0) {
            this.setnLayers(network.getnLayers());
            this.layers = new NeuralNetwork[network.getnLayers()];
            for(int i = 0; i < layers.length; i++)
                if((this.getnLayers()) > i && (network.getnLayers() > i)) {
                    if(network.getLayers()[i] == null) {
                        throw new IllegalStateException("Will not clone uninitialized network, layer " + i + " of network was null");
                    }

                    this.getLayers()[i] = network.getLayers()[i].clone();

                }
        }

        this.layerLearningRates = network.layerLearningRates;
        this.normalizeByInputRows = network.normalizeByInputRows;
        this.useAdaGrad = network.useAdaGrad;
        this.hiddenLayerSizes = network.hiddenLayerSizes;
        if(network.outputLayer != null)
            this.outputLayer = network.outputLayer.clone();
        this.nIns = network.nIns;
        this.nOuts = network.nOuts;
        this.rng = network.rng;
        this.dist = network.dist;
        this.activation = network.activation;
        this.useRegularization = network.useRegularization;
        this.columnMeans = network.columnMeans;
        this.columnStds = network.columnStds;
        this.columnSums = network.columnSums;
        this.errorTolerance = network.errorTolerance;
        this.renderWeightsEveryNEpochs = network.renderWeightsEveryNEpochs;
        this.forceNumEpochs = network.forceNumEpochs;
        this.input = network.input;
        this.l2 = network.l2;
        this.fanIn = network.fanIn;
        this.labels =  network.labels;
        this.momentum = network.momentum;
        this.learningRateUpdate = network.learningRateUpdate;
        this.shouldBackProp = network.shouldBackProp;
        this.weightTransforms = network.weightTransforms;
        this.sparsity = network.sparsity;
        this.visibleBiasTransforms = network.visibleBiasTransforms;
        this.hiddenBiasTransforms = network.hiddenBiasTransforms;
        this.dropOut = network.dropOut;
        this.optimizationAlgorithm = network.optimizationAlgorithm;
        this.lossFunction = network.lossFunction;

        if(network.sigmoidLayers != null && network.sigmoidLayers.length > 0) {
            this.sigmoidLayers = new HiddenLayer[network.sigmoidLayers.length];
            for(int i = 0; i < sigmoidLayers.length; i++)
                this.getSigmoidLayers()[i] = network.getSigmoidLayers()[i].clone();

        }


    }

    /**
     * Score of the model (relative to the objective function)
     * @return the score of the model (relative to the objective function)
     */
    public  double score() {
        return outputLayer.score();
    }

    /**
     * Train the network running some unsupervised
     * pretraining followed by SGD/finetune
     * @param input the input to train on
     * @param labels the labels for the training examples(a matrix of the following format:
     * [0,1,0] where 0 represents the labels its not and 1 represents labels for the positive outcomes
     * @param otherParams the other parameters for child classes (algorithm specific parameters such as corruption level for SDA)
     */
    public abstract void trainNetwork(DoubleMatrix input,DoubleMatrix labels,Object[] otherParams);



    /**
     * Pretrain the network with the given parameters
     * @param input the input to train ons
     * @param otherParams the other parameters for child classes (algorithm specific parameters such as corruption level for SDA)
     */
    public abstract void pretrain(DoubleMatrix input,Object[] otherParams);


    protected void applyTransforms() {
        if(layers == null || layers.length < 1) {
            throw new IllegalStateException("Layers not initialized");
        }

        for(int i = 0; i < layers.length; i++) {
            if(weightTransforms.containsKey(i))
                layers[i].setW(weightTransforms.get(i).apply(layers[i].getW()));
            if(hiddenBiasTransforms.containsKey(i))
                layers[i].sethBias(getHiddenBiasTransforms().get(i).apply(layers[i].gethBias()));
            if(this.visibleBiasTransforms.containsKey(i))
                layers[i].setvBias(getVisibleBiasTransforms().get(i).apply(layers[i].getvBias()));
        }
    }




    /**
     * Creates a layer depending on the index.
     * The main reason this matters is for continuous variations such as the {@link org.deeplearning4j.dbn.DBN}
     * where the first layer needs to be an {@link org.deeplearning4j.rbm.RBM} for continuous inputs.
     *
     * @param input the input to the layer
     * @param nVisible the number of visible inputs
     * @param nHidden the number of hidden units
     * @param W the weight vector
     * @param hbias the hidden bias
     * @param vBias the visible bias
     * @param rng the rng to use (THiS IS IMPORTANT; YOU DO NOT WANT TO HAVE A MIS REFERENCED RNG OTHERWISE NUMBERS WILL BE MEANINGLESS)
     * @param index the index of the layer
     * @return a neural network layer such as {@link org.deeplearning4j.rbm.RBM}
     */
    public abstract NeuralNetwork createLayer(DoubleMatrix input,int nVisible,int nHidden, DoubleMatrix W,DoubleMatrix hbias,DoubleMatrix vBias,RandomGenerator rng,int index);


    public abstract NeuralNetwork[] createNetworkLayers(int numLayers);


    /**
     * Creates a hidden layer with the given parameters.
     * The default implementation is a binomial sampling
     * hidden layer, but this can be overridden
     * for other kinds of hidden units
     * @param nIn the number of inputs
     * @param nOut the number of outputs
     * @param activation the activation function for the layer
     * @param rng the rng to use for sampling
     * @param layerInput the layer starting input
     * @param dist the probability distribution to use
     * for generating weights
     * @return a hidden layer with the given parameters
     */
    public  HiddenLayer createHiddenLayer(int index,int nIn,int nOut,ActivationFunction activation,RandomGenerator rng,DoubleMatrix layerInput,RealDistribution dist) {
        return new HiddenLayer.Builder()
                .nIn(nIn).nOut(nOut)
                .withActivation(activation)
                .withRng(rng).withInput(layerInput).dist(dist)
                .build();

    }



    /**
     * Merges this network with the other one.
     * This is a weight averaging with the update of:
     * a += b - a / n
     * where a is a matrix on the network
     * b is the incoming matrix and n
     * is the batch size.
     * This update is performed across the network layers
     * as well as hidden layers and logistic layers
     *
     * @param network the network to merge with
     * @param batchSize the batch size (number of training examples)
     * to average by
     */
    public void merge(BaseMultiLayerNetwork network,int batchSize) {
        if(network.getnLayers() != getnLayers())
            throw new IllegalArgumentException("Unable to merge networks that are not of equal length");
        for(int i = 0; i < getnLayers(); i++) {
            NeuralNetwork n = layers[i];
            NeuralNetwork otherNetwork = network.layers[i];
            n.merge(otherNetwork, batchSize);
            //tied weights: must be updated at the same time
            getSigmoidLayers()[i].setB(n.gethBias());
            getSigmoidLayers()[i].setW(n.getW());

        }

        getOutputLayer().merge(network.outputLayer, batchSize);
    }

    /**
     * Transposes this network to turn it in to
     * ad encoder for the given auto encoder network
     * @param network the network to decode
     */
    public void encode(BaseMultiLayerNetwork network) {
        this.createNetworkLayers(network.getnLayers());
        this.layers = new NeuralNetwork[network.getnLayers()];
        hiddenLayerSizes = new int[getnLayers()];

        int count = 0;
        for(int i = getnLayers() - 1; i > 0; i--) {
            NeuralNetwork n = network.layers[i].clone();
            //tied weights: must be updated at the same time
            HiddenLayer l = network.sigmoidLayers[i].clone();
            layers[count] = n;
            sigmoidLayers[count] = l;
            hiddenLayerSizes[count] = network.hiddenLayerSizes[i];
            count++;
        }

        this.outputLayer = new OutputLayer(hiddenLayerSizes[getnLayers() - 1],network.input.columns);

    }



    public  DoubleMatrix getLabels() {
        return labels;
    }

    public OutputLayer getOutputLayer() {
        return outputLayer;
    }

    /**
     * Note that if input isn't null
     * and the layers are null, this is a way
     * of initializing the neural network
     * @param input
     */
    public  void setInput(DoubleMatrix input) {
        if(input != null && this.layers == null)
            this.initializeLayers(input);
        this.input = input;

    }

    public boolean isShouldBackProp() {
        return shouldBackProp;
    }


    public OptimizationAlgorithm getOptimizationAlgorithm() {
        return optimizationAlgorithm;
    }

    public void setOptimizationAlgorithm(OptimizationAlgorithm optimizationAlgorithm) {
        this.optimizationAlgorithm = optimizationAlgorithm;
    }

    public LossFunction getLossFunction() {
        return lossFunction;
    }

    public void setLossFunction(LossFunction lossFunction) {
        this.lossFunction = lossFunction;
    }


    public  DoubleMatrix getInput() {
        return input;
    }

    public  synchronized HiddenLayer[] getSigmoidLayers() {
        return sigmoidLayers;
    }

    public  synchronized NeuralNetwork[] getLayers() {
        return layers;
    }



    public boolean isForceNumEpochs() {
        return forceNumEpochs;
    }



    public DoubleMatrix getColumnSums() {
        return columnSums;
    }

    public void setColumnSums(DoubleMatrix columnSums) {
        this.columnSums = columnSums;
    }

    public  int[] getHiddenLayerSizes() {
        return hiddenLayerSizes;
    }

    public  void setHiddenLayerSizes(int[] hiddenLayerSizes) {
        this.hiddenLayerSizes = hiddenLayerSizes;
    }

    public  RandomGenerator getRng() {
        return rng;
    }

    public  void setRng(RandomGenerator rng) {
        this.rng = rng;
    }

    public  RealDistribution getDist() {
        return dist;
    }

    public  void setDist(RealDistribution dist) {
        this.dist = dist;
    }

    public  MultiLayerNetworkOptimizer getOptimizer() {
        return optimizer;
    }

    public  void setOptimizer(MultiLayerNetworkOptimizer optimizer) {
        this.optimizer = optimizer;
    }

    public  ActivationFunction getActivation() {
        return activation;
    }

    public  void setActivation(ActivationFunction activation) {
        this.activation = activation;
    }



    public  boolean isShouldInit() {
        return shouldInit;
    }

    public  void setShouldInit(boolean shouldInit) {
        this.shouldInit = shouldInit;
    }

    public  double getFanIn() {
        return fanIn;
    }

    public  void setFanIn(double fanIn) {
        this.fanIn = fanIn;
    }

    public  int getRenderWeightsEveryNEpochs() {
        return renderWeightsEveryNEpochs;
    }

    public  void setRenderWeightsEveryNEpochs(
            int renderWeightsEveryNEpochs) {
        this.renderWeightsEveryNEpochs = renderWeightsEveryNEpochs;
    }

    public  Map<Integer, MatrixTransform> getWeightTransforms() {
        return weightTransforms;
    }

    public  void setWeightTransforms(
            Map<Integer, MatrixTransform> weightTransforms) {
        this.weightTransforms = weightTransforms;
    }

    public  double getSparsity() {
        return sparsity;
    }

    public  void setSparsity(double sparsity) {
        this.sparsity = sparsity;
    }

    public  double getLearningRateUpdate() {
        return learningRateUpdate;
    }

    public  void setLearningRateUpdate(double learningRateUpdate) {
        this.learningRateUpdate = learningRateUpdate;
    }

    public  double getErrorTolerance() {
        return errorTolerance;
    }

    public  void setErrorTolerance(double errorTolerance) {
        this.errorTolerance = errorTolerance;
    }

    public  void setLabels(DoubleMatrix labels) {
        this.labels = labels;
    }

    public  void setForceNumEpochs(boolean forceNumEpochs) {
        this.forceNumEpochs = forceNumEpochs;
    }



    public DoubleMatrix getColumnMeans() {
        return columnMeans;
    }

    public void setColumnMeans(DoubleMatrix columnMeans) {
        this.columnMeans = columnMeans;
    }

    public DoubleMatrix getColumnStds() {
        return columnStds;
    }

    public void setColumnStds(DoubleMatrix columnStds) {
        this.columnStds = columnStds;
    }



    public  boolean isUseAdaGrad() {
        return useAdaGrad;
    }

    public  void setUseAdaGrad(boolean useAdaGrad) {
        this.useAdaGrad = useAdaGrad;
    }



    public  boolean isNormalizeByInputRows() {
        return normalizeByInputRows;
    }

    public  void setNormalizeByInputRows(boolean normalizeByInputRows) {
        this.normalizeByInputRows = normalizeByInputRows;
    }



    public boolean isUseHiddenActivationsForwardProp() {
        return useHiddenActivationsForwardProp;
    }

    public void setUseHiddenActivationsForwardProp(
            boolean useHiddenActivationsForwardProp) {
        this.useHiddenActivationsForwardProp = useHiddenActivationsForwardProp;
    }




    public double getDropOut() {
        return dropOut;
    }

    public void setDropOut(double dropOut) {
        this.dropOut = dropOut;
    }

    public  Map<Integer, MatrixTransform> getHiddenBiasTransforms() {
        return hiddenBiasTransforms;
    }

    public  Map<Integer, MatrixTransform> getVisibleBiasTransforms() {
        return visibleBiasTransforms;
    }

    public  int getnIns() {
        return nIns;
    }

    public  void setnIns(int nIns) {
        this.nIns = nIns;
    }

    public  int getnOuts() {
        return nOuts;
    }

    public  void setnOuts(int nOuts) {
        this.nOuts = nOuts;
    }

    public  int getnLayers() {
        return layers.length;
    }

    public  void setnLayers(int nLayers) {
        this.layers = createNetworkLayers(nLayers);
    }

    public  double getMomentum() {
        return momentum;
    }

    public  void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    public  double getL2() {
        return l2;
    }

    public  void setL2(double l2) {
        this.l2 = l2;
    }

    public  boolean isUseRegularization() {
        return useRegularization;
    }

    public  void setUseRegularization(boolean useRegularization) {
        this.useRegularization = useRegularization;
    }

    public  void setSigmoidLayers(HiddenLayer[] sigmoidLayers) {
        this.sigmoidLayers = sigmoidLayers;
    }

    public  void setOutputLayer(OutputLayer outputLayer) {
        this.outputLayer = outputLayer;
    }

    public  void setShouldBackProp(boolean shouldBackProp) {
        this.shouldBackProp = shouldBackProp;
    }

    public  void setLayers(NeuralNetwork[] layers) {
        this.layers = layers;
    }

    public void clearInput() {
        this.input = null;
        for(int i = 0; i < layers.length; i++)
            layers[i].clearInput();
    }

    public boolean isBuildAutoEncoder() {
        return buildAutoEncoder;
    }

    public void setBuildAutoEncoder(boolean buildAutoEncoder) {
        this.buildAutoEncoder = buildAutoEncoder;
    }

    public OutputLayer.LossFunction getOutputLossFunction() {
        return outputLossFunction;
    }

    public void setOutputLossFunction(OutputLayer.LossFunction outputLossFunction) {
        this.outputLossFunction = outputLossFunction;
    }

    public ActivationFunction getOutputActivationFunction() {
        return outputActivationFunction;
    }

    public void setOutputActivationFunction(ActivationFunction outputActivationFunction) {
        this.outputActivationFunction = outputActivationFunction;
    }

    public Map<Integer, Double> getLayerLearningRates() {
        return layerLearningRates;
    }

    public void setLayerLearningRates(Map<Integer, Double> layerLearningRates) {
        this.layerLearningRates = layerLearningRates;
    }

    public static class Builder<E extends BaseMultiLayerNetwork> {
        protected Class<? extends BaseMultiLayerNetwork> clazz;
        private E ret;
        private Map<Integer,Double> layerLearningRates = new HashMap<>();
        private int nIns;
        private int[] hiddenLayerSizes;
        private int nOuts;
        private int nLayers;
        private RandomGenerator rng = new MersenneTwister(1234);
        private DoubleMatrix input,labels;
        private ActivationFunction activation;
        private boolean decode = false;
        private double fanIn = -1;
        private int renderWeithsEveryNEpochs = -1;
        private double l2 = 0.01;
        private boolean useRegularization = false;
        private double momentum;
        private RealDistribution dist;
        protected Map<Integer,MatrixTransform> weightTransforms = new HashMap<>();
        protected boolean backProp = true;
        protected boolean shouldForceEpochs = false;
        private double sparsity = 0;
        private Map<Integer,MatrixTransform> hiddenBiasTransforms = new HashMap<>();
        private Map<Integer,MatrixTransform> visibleBiasTransforms = new HashMap<>();
        private boolean useAdaGrad = true;
        private boolean normalizeByInputRows = true;
        private boolean useHiddenActivationsForwardProp = true;
        private double dropOut = 0;
        private Map<Integer,ActivationFunction> activationForLayer = new HashMap<>();
        private boolean buildAutoEncoder = false;
        private LossFunction lossFunction = LossFunction.RECONSTRUCTION_CROSSENTROPY;
        private OptimizationAlgorithm optimizationAlgo = OptimizationAlgorithm.CONJUGATE_GRADIENT;
        private OutputLayer.LossFunction outputLossFunction = OutputLayer.LossFunction.MCXENT;
        private ActivationFunction outputActivationFunction = Activations.softmax();


        public Builder learningRateForLayer(Map<Integer,Double> learningRates) {
            this.layerLearningRates.putAll(learningRates);
            return this;
        }

        public Builder activateForLayer(Map<Integer,ActivationFunction> activationForLayer) {
            this.activationForLayer.putAll(activationForLayer);
            return this;
        }

        public Builder activateForLayer(int layer,ActivationFunction function) {
            activationForLayer.put(layer,function);
            return this;
        }

        /**
         * Activation function for output layer
         * @param outputActivationFunction the output activation function to use
         * @return builder pattern
         */
        public Builder withOutputActivationFunction(ActivationFunction outputActivationFunction) {
            this.outputActivationFunction = outputActivationFunction;
            return this;
        }


        /**
         * Output loss function
         * @param outputLossFunction the output loss function
         * @return
         */
        public Builder withOutputLossFunction(OutputLayer.LossFunction outputLossFunction) {
            this.outputLossFunction = outputLossFunction;
            return this;
        }

        /**
         * Which optimization algorithm to use with neural nets and Logistic regression
         * @param optimizationAlgo which optimization algorithm to use with
         * neural nets and logistic regression
         * @return builder pattern
         */
        public Builder<E> withOptimizationAlgorithm(OptimizationAlgorithm optimizationAlgo) {
            this.optimizationAlgo = optimizationAlgo;
            return this;
        }

        /**
         * Loss function to use with neural networks
         * @param lossFunction loss function to use with neural networks
         * @return builder pattern
         */
        public Builder<E> withLossFunction(LossFunction lossFunction) {
            this.lossFunction = lossFunction;
            return this;
        }

        /**
         * Whether to use drop out on the neural networks or not:
         * random zero out of examples
         * @param dropOut the dropout to use
         * @return builder pattern
         */
        public Builder<E> withDropOut(double dropOut) {
            this.dropOut = dropOut;
            return this;
        }

        /**
         * Whether to use hidden layer activations or neural network sampling
         * on feed forward pass
         * @param useHiddenActivationsForwardProp true if use hidden activations, false otherwise
         * @return builder pattern
         */
        public Builder<E> useHiddenActivationsForwardProp(boolean useHiddenActivationsForwardProp) {
            this.useHiddenActivationsForwardProp = useHiddenActivationsForwardProp;
            return this;
        }

        /**
         * Turn this off for full dataset training
         * @param normalizeByInputRows whether to normalize the changes
         * by the number of input rows
         * @return builder pattern
         */
        public Builder<E> normalizeByInputRows(boolean normalizeByInputRows) {
            this.normalizeByInputRows = normalizeByInputRows;
            return this;
        }




        public Builder<E> useAdaGrad(boolean useAdaGrad) {
            this.useAdaGrad = useAdaGrad;
            return this;
        }

        public Builder<E> withSparsity(double sparsity) {
            this.sparsity = sparsity;
            return this;
        }


        public Builder<E> withVisibleBiasTransforms(Map<Integer,MatrixTransform> visibleBiasTransforms) {
            this.visibleBiasTransforms = visibleBiasTransforms;
            return this;
        }

        public Builder<E> withHiddenBiasTransforms(Map<Integer,MatrixTransform> hiddenBiasTransforms) {
            this.hiddenBiasTransforms = hiddenBiasTransforms;
            return this;
        }

        /**
         * Forces use of number of epochs for training
         * SGD style rather than conjugate gradient
         * @return
         */
        public Builder<E> forceEpochs() {
            shouldForceEpochs = true;
            return this;
        }

        /**
         * Disables back propagation
         * @return
         */
        public Builder<E> disableBackProp() {
            backProp = false;
            return this;
        }

        /**
         * Transform the weights at the given layer
         * @param layer the layer to transform
         * @param transform the function used for transformation
         * @return
         */
        public Builder<E> transformWeightsAt(int layer,MatrixTransform transform) {
            weightTransforms.put(layer,transform);
            return this;
        }

        /**
         * A map of transformations for transforming
         * the given layers
         * @param transforms
         * @return
         */
        public Builder<E> transformWeightsAt(Map<Integer,MatrixTransform> transforms) {
            weightTransforms.putAll(transforms);
            return this;
        }

        /**
         * Probability distribution for generating weights
         * @param dist
         * @return
         */
        public Builder<E> withDist(RealDistribution dist) {
            this.dist = dist;
            return this;
        }

        /**
         * Specify momentum
         * @param momentum
         * @return
         */
        public Builder<E> withMomentum(double momentum) {
            this.momentum = momentum;
            return this;
        }

        /**
         * Use l2 reg
         * @param useRegularization
         * @return
         */
        public Builder<E> useRegularization(boolean useRegularization) {
            this.useRegularization = useRegularization;
            return this;
        }

        /**
         * L2 coefficient
         * @param l2
         * @return
         */
        public Builder<E> withL2(double l2) {
            this.l2 = l2;
            return this;
        }

        /**
         * Whether to plot weights or not
         * @param everyN
         * @return
         */
        public Builder<E> renderWeights(int everyN) {
            this.renderWeithsEveryNEpochs = everyN;
            return this;
        }

        public Builder<E> withFanIn(Double fanIn) {
            this.fanIn = fanIn;
            return this;
        }

        /**
         * Pick an activation function, default is sigmoid
         * @param activation
         * @return
         */
        public Builder<E> withActivation(ActivationFunction activation) {
            this.activation = activation;
            return this;
        }


        public Builder<E> numberOfInputs(int nIns) {
            this.nIns = nIns;
            return this;
        }

        /**
         * Whether the network is a decoder for an auto encoder
         * @param decode
         * @return
         */
        public Builder<E> decodeNetwork(boolean decode) {
            this.decode = decode;
            return this;
        }

        public Builder<E> hiddenLayerSizes(Integer[] hiddenLayerSizes) {
            this.hiddenLayerSizes = new int[hiddenLayerSizes.length];
            this.nLayers = hiddenLayerSizes.length;
            for(int i = 0; i < hiddenLayerSizes.length; i++)
                this.hiddenLayerSizes[i] = hiddenLayerSizes[i];
            return this;
        }

        public Builder<E> hiddenLayerSizes(int[] hiddenLayerSizes) {
            this.hiddenLayerSizes = hiddenLayerSizes;
            this.nLayers = hiddenLayerSizes.length;
            return this;
        }

        public Builder<E> numberOfOutPuts(int nOuts) {
            this.nOuts = nOuts;
            return this;
        }

        public Builder<E> withRng(RandomGenerator gen) {
            this.rng = gen;
            return this;
        }

        public Builder<E> withInput(DoubleMatrix input) {
            this.input = input;
            return this;
        }

        public Builder<E> withLabels(DoubleMatrix labels) {
            this.labels = labels;
            return this;
        }

        public Builder<E> withClazz(Class<? extends BaseMultiLayerNetwork> clazz) {
            this.clazz =  clazz;
            return this;
        }


        @SuppressWarnings("unchecked")
        public E buildEmpty() {
            try {
                E ret = null;
                Constructor<?> c = Dl4jReflection.getEmptyConstructor(clazz);
                c.setAccessible(true);

                ret = (E) c.newInstance();

                return ret;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public E build() {
            try {
                E ret = null;
                Constructor<?> c = Dl4jReflection.getEmptyConstructor(clazz);
                c.setAccessible(true);

                ret = (E) c.newInstance();
                ret.setNormalizeByInputRows(normalizeByInputRows);
                ret.setInput(this.input);
                ret.setnOuts(this.nOuts);
                ret.setnIns(this.nIns);
                ret.setLabels(this.labels);
                ret.setHiddenLayerSizes(this.hiddenLayerSizes);
                ret.setnLayers(this.nLayers);
                ret.setRng(this.rng);
                ret.setShouldBackProp(this.backProp);
                ret.setSigmoidLayers(new HiddenLayer[ret.getnLayers()]);
                ret.setUseHiddenActivationsForwardProp(useHiddenActivationsForwardProp);
                ret.setBuildAutoEncoder(buildAutoEncoder);
                ret.setInput(this.input);
                ret.setMomentum(momentum);
                ret.setLabels(labels);
                ret.setFanIn(fanIn);
                ret.activationFunctionForLayer.putAll(activationForLayer);
                ret.setSparsity(sparsity);
                ret.setRenderWeightsEveryNEpochs(renderWeithsEveryNEpochs);
                ret.setL2(l2);
                ret.setForceNumEpochs(shouldForceEpochs);
                ret.setUseRegularization(useRegularization);
                ret.setUseAdaGrad(useAdaGrad);
                ret.setDropOut(dropOut);
                ret.setOptimizationAlgorithm(optimizationAlgo);
                ret.setLossFunction(lossFunction);
                ret.setOutputActivationFunction(outputActivationFunction);
                ret.setOutputLossFunction(outputLossFunction);
                if(activation != null)
                    ret.setActivation(activation);
                if(dist != null)
                    ret.setDist(dist);
                ret.getWeightTransforms().putAll(weightTransforms);
                ret.getVisibleBiasTransforms().putAll(visibleBiasTransforms);
                ret.getHiddenBiasTransforms().putAll(hiddenBiasTransforms);
                ret.getLayerLearningRates().putAll(layerLearningRates);
                if(hiddenLayerSizes == null)
                    throw new IllegalStateException("Unable to build network, no hidden layer sizes defined");

                return ret;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

    }


}