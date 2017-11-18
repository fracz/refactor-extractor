package org.deeplearning4j.dbn;


import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.linalg.api.activation.ActivationFunction;
import org.deeplearning4j.linalg.api.activation.Activations;
import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.convolution.Convolution;
import org.deeplearning4j.linalg.factory.NDArrays;
import org.deeplearning4j.linalg.indexing.NDArrayIndex;
import org.deeplearning4j.linalg.ops.transforms.Transforms;
import org.deeplearning4j.linalg.util.ArrayUtil;
import org.deeplearning4j.nn.*;


import org.deeplearning4j.nn.learning.AdaGrad;
import org.deeplearning4j.rbm.ConvolutionalRBM;
import org.deeplearning4j.rng.SynchronizedRandomGenerator;


import java.util.ArrayList;
import java.util.List;

/**
 * Convolutional Deep Belief Network
 * @author Adam Gibson
 */
public class ConvolutionalDBN extends BaseMultiLayerNetwork {


    //filter size per layer
    protected int[][] filterSizes;
    //the per datum input size (2d image dimensions)
    protected int[] inputSize;

    //rbm wise nfm
    protected int[] nFm;
    //layer wise stride (arr[k] is an array)
    protected int[][] stride;
    //layer wise filter number of filters
    protected int[][] numFilters;
    //sparse gain for each rbm
    protected double sparseGain;
    //layer type: convolution or subsampling
    protected LayerType[] layerTypes;
    public  static enum LayerType {
        SUBSAMPLE,CONVOLUTION
    }






    protected ConvolutionalDBN() {
    }

    protected ConvolutionalDBN(int nIns, int[] hiddenLayerSizes, int nOuts, int nLayers, RandomGenerator rng) {
        super(nIns, hiddenLayerSizes, nOuts, nLayers, rng);
    }

    protected ConvolutionalDBN(int nIn, int[] hiddenLayerSizes, int nOuts, int nLayers, RandomGenerator rng, INDArray input, INDArray labels) {
        super(nIn, hiddenLayerSizes, nOuts, nLayers, rng, input, labels);
    }



    /**
     * Pretrain the network with the given parameters
     *
     * @param input       the input to train ons
     * @param otherParams the other parameters for child classes (algorithm specific parameters such as corruption level for SDA)
     */
    @Override
    public void pretrain(INDArray input, Object[] otherParams) {

    }

    /**
     * Pretrain with a data applyTransformToDestination iterator.
     * This will run through each neural net at a time and train on the input.
     *
     * @param iter        the iterator to use
     * @param otherParams
     */
    @Override
    public void pretrain(DataSetIterator iter, Object[] otherParams) {

    }

    /**
     * Compute activations from input to output of the output layer
     *
     * @param input the input data to feed forward
     * @return the list of activations for each layer
     */
    @Override
    public List<INDArray> feedForward(INDArray input) {
        /* Number of tensors is equivalent to the number of mini batches */
        INDArray tensor = NDArrays.create(new int[]{inputSize[0],inputSize[1],  input.rows() / inputSize[0],input.rows() / input.length()});
        INDArray curr = tensor;
        List<INDArray> activations = new ArrayList<>();
        for(int i = 0; i < getnLayers(); i++) {
            ConvolutionalRBM r = (ConvolutionalRBM) getLayers()[i];
            DownSamplingLayer d = (DownSamplingLayer) getSigmoidLayers()[i];
            for(int j = 0; j < r.getNumFilters()[0]; j++) {

              int nObs = curr.slices();
                //equivalent to a 3d tensor: only one tensor
                INDArray featureMap = NDArrays.zeros(new int[]{r.getFmSize()[0],r.getFmSize()[1],1,nObs});
                for(int k = 0; j < r.getNumFilters()[0]; j++) {
                    featureMap.addi(Convolution.convn(featureMap.slice(i), r.getW().slice(j, i), Convolution.Type.VALID));
                }
                featureMap.addi(r.gethBias().getScalar(i));
                r.getFeatureMap().putSlice(j, d.activate(featureMap));

                //put the down sampled
                d.getFeatureMap().putSlice(j, Transforms.downSample(r.getFeatureMap().slice(j),r.getStride()));


            }

            activations.add(d.getFeatureMap());

        }

        activations.add(output(activations.get(activations.size() - 1)));


        return activations;
    }

    /**
     * Compute activations from input to output of the output layer
     *
     * @return the list of activations for each layer
     */
    @Override
    public List<INDArray> feedForward() {
        return feedForward(input);
    }

    @Override
    protected void computeDeltas(List<INDArray> deltaRet) {
        ActivationFunction a = getSigmoidLayers()[0].getActivationFunction();
        ActivationFunction softMaxDerivative = Activations.softMaxRows();
        List<INDArray> activations = feedForward();

        /**
         * Prediction will actually be a tensor, need to map this out
         */
        INDArray error = labels.sub(activations.get(activations.size() - 1)).neg().mul(softMaxDerivative.applyDerivative(activations.get(activations.size() - 1)));
        //should this be a 4d tensor?
        INDArray es = outputLayer.getW().transpose().mmul(error);
        DownSamplingLayer d = (DownSamplingLayer) getSigmoidLayers()[getSigmoidLayers().length - 1];
        ConvolutionalRBM rbm = (ConvolutionalRBM) getLayers()[getnLayers() - 1];
        INDArray[] errorSignals = new INDArray[getnLayers()];
        INDArray[] biasGradients = new INDArray[getnLayers()];

        INDArray layerErrorSignal = NDArrays.zeros(d.getFeatureMap().shape());
        errorSignals[errorSignals.length - 1] = es;
        //initial hidden layer error signal

        int nMap = layerErrorSignal.size(0)*  layerErrorSignal.size(1);

        //translate in to a 2d feature map
        for(int i = 0; i < rbm.getNumFilters()[0]; i++) {
           /*
             These will be different slices of the tensor
            */
            INDArray subSlice = es.get(NDArrayIndex.interval(i * nMap, (i + 1) * nMap),NDArrayIndex.interval(0,es.columns()));
            INDArray reshaped = subSlice.reshape(d.getFeatureMap().shape());
            layerErrorSignal.putSlice(i, reshaped);

        }

        errorSignals[errorSignals.length - 2] = layerErrorSignal;

        for(int i = getnLayers() -2; i >= 0; i--) {
            DownSamplingLayer layer = (DownSamplingLayer) getSigmoidLayers()[i];
            ConvolutionalRBM r2 = (ConvolutionalRBM) getLayers()[i];
            DownSamplingLayer forwardDownSamplingLayer = (DownSamplingLayer) getSigmoidLayers()[i + 1];
            ConvolutionalRBM forwardRBM = (ConvolutionalRBM) getLayers()[i + 1];
            int[] stride = forwardRBM.getStride();

            INDArray propErrorSignal = NDArrays.zeros(d.getFeatureMap().shape());

            //handle subsampling layer first
            for(int k = 0; k < layer.getNumFeatureMaps(); k++) {
                INDArray rotFilter = NDArrays.rot(forwardRBM.getW().slice(i).slice(k));
                INDArray tensor =   errorSignals[i + 1];
                INDArray currEs = tensor.slice(k);
                propErrorSignal.addi(Convolution.convn(currEs, rotFilter, Convolution.Type.FULL));

            }


            errorSignals[i] = propErrorSignal;

            INDArray mapSize = forwardRBM.getFeatureMap();
            INDArray rbmEs = NDArrays.zeros(mapSize.shape());
            for(int k = 0; k < rbm.getNumFilters()[0]; k++) {
                INDArray propEs = Transforms.upSample(forwardDownSamplingLayer.getFeatureMap().slice(k),
                        ArrayUtil.toNDArray(new int[]{stride[0],stride[1],1,1})).divi(ArrayUtil.prod(stride));
                rbmEs.putSlice(k, propEs);
            }

            errorSignals[i - 1] = rbmEs;



        }



        //now calculate the gradients

        for(int i = getnLayers() -2; i >= 0; i--) {
            ConvolutionalRBM r2 = (ConvolutionalRBM) getLayers()[i];
            ConvolutionalRBM prevRBM = (ConvolutionalRBM) getLayers()[i - 1];

            INDArray errorSignal = errorSignals[i - 1];
            INDArray biasGradient = NDArrays.create(1,r2.getNumFilters()[0]);
            for(int j = 0; j < r2.getNumFilters()[0]; j++) {
                INDArray es2 = errorSignal.slice(j);
                for(int k = 0; k < prevRBM.getNumFilters()[0]; k++)  {

                    //figure out what to do wrt error signal for each neural net here.
                    INDArray flipped = NDArrays.reverse(prevRBM.getFeatureMap().slice(k));

                    INDArray dedFilter = Convolution.convn(flipped, es2, Convolution.Type.VALID);
                    r2.getdWeights().put(j,k,dedFilter);
                }

                biasGradient.put(j, es.sum(1).div(errorSignal.slices()).sum(Integer.MAX_VALUE));

            }
            biasGradients[i] = biasGradient;

        }


        for(int i = 0; i < biasGradients.length; i++) {
            deltaRet.add(errorSignals[i]);
        }

        //output layer gradients
        deltaRet.add(errorSignals[errorSignals.length - 1].mmul(outputLayer.getInput()));

    }

    /**
     * Do a back prop iteration.
     * This involves computing the activations, tracking the last layers weights
     * to revert to in case of convergence, the learning rate being used to train
     * and the current epoch
     *
     * @param revert the best network so far
     * @param lr     the learning rate to use for training
     * @param epoch  the epoch to use
     * @return whether the training should converge or not
     */
    //@Override
    protected void backPropStep(BaseMultiLayerNetwork revert, double lr, int epoch) {
        //feedforward to compute activations
        //initial error


        //precompute deltas
        List<INDArray> deltas = new ArrayList<>();
        //compute derivatives and gradients given activations
        computeDeltas(deltas);


        for(int l = 1; l < getnLayers(); l++) {
            ConvolutionalRBM r = (ConvolutionalRBM) getLayers()[l];
            ConvolutionalRBM prevR = (ConvolutionalRBM) getLayers()[l - 1];

            INDArray wGradient =   deltas.get(l);
            INDArray biasGradient =  deltas.get(l).mean(1);

            INDArray biasLearningRates = null;
            AdaGrad wAdaGrad= r.getAdaGrad();
            if(useAdaGrad)
                biasLearningRates = layers[l].gethBiasAdaGrad().getLearningRates(biasGradient);


            for(int m = 0; m < r.getNumFilters()[0]; m++) {
                if(useAdaGrad)
                    biasGradient.put(m,biasLearningRates.getScalar(m).muli(r.gethBias().getScalar(m)));

                else
                    biasGradient.put(m,r.gethBias().getScalar(m).muli(lr));
                for(int n = 0; n < prevR.getNumFilters()[0]; n++) {
                    if(useRegularization)  {
                        INDArray penalty = r.getFeatureMap().slice(m, n).mul(l2) ;
                        if(useAdaGrad) {
                            INDArray learningRates = wAdaGrad.getLearningRates(wGradient.slice(m).slice(n));
                            penalty.muli(learningRates);

                        }
                        else
                            penalty.muli(lr);
                        wGradient.put(m, n, wGradient.slice(m).slice(n).mul(penalty));

                    }

                }


            }


            INDArray gradientChange = deltas.get(l);
            //getFromOrigin the gradient
            if(isUseAdaGrad())
                gradientChange.muli(getLayers()[l].getAdaGrad().getLearningRates(gradientChange));

            else
                gradientChange.muli(lr);

            //l2
            if(useRegularization)
                gradientChange.muli(getLayers()[l].getW().mul(l2));

            if(momentum != 0)
                gradientChange.muli(momentum);

            if(isNormalizeByInputRows())
                gradientChange.divi(input.rows());

            //update W
            getLayers()[l].getW().subi(gradientChange);
            getSigmoidLayers()[l].setW(layers[l].getW());


            //update hidden bias
            INDArray deltaColumnSums = deltas.get(l).mean(1);

            if(sparsity != 0)
                deltaColumnSums = deltaColumnSums.rsubi(sparsity);

            if(useAdaGrad)
                deltaColumnSums.muli(layers[l].gethBiasAdaGrad().getLearningRates(deltaColumnSums));
            else
                deltaColumnSums.muli(lr);

            if(momentum != 0)
                deltaColumnSums.muli(momentum);

            if(isNormalizeByInputRows())
                deltaColumnSums.divi(input.rows());


            getLayers()[l].gethBias().subi(deltaColumnSums);
            getSigmoidLayers()[l].setB(getLayers()[l].gethBias());
        }

        INDArray logLayerGradient = deltas.get(getnLayers());
        INDArray biasGradient = deltas.get(getnLayers()).mean(1);

        if(momentum != 0)
            logLayerGradient.muli(momentum);


        if(useAdaGrad)
            logLayerGradient.muli(outputLayer.getAdaGrad().getLearningRates(logLayerGradient));


        else
            logLayerGradient.muli(lr);

        if(isNormalizeByInputRows())
            logLayerGradient.divi(input.rows());



        if(momentum != 0)
            biasGradient.muli(momentum);

        if(useAdaGrad)
            biasGradient.muli(outputLayer.getBiasAdaGrad().getLearningRates(biasGradient));
        else
            biasGradient.muli(lr);

        if(isNormalizeByInputRows())
            biasGradient.divi(input.rows());


        getOutputLayer().getW().subi(logLayerGradient);

        if(getOutputLayer().getB().length() == biasGradient.length())
            getOutputLayer().getB().subi(biasGradient);
        else
            getOutputLayer().getB().subi(biasGradient.mean(Integer.MAX_VALUE));
    }


    /**
     * Label the probabilities of the input
     *
     * @param x the input to label
     * @return a vector of probabilities
     * given each label.
     * <p/>
     * This is typically of the form:
     * [0.5, 0.5] or some other probability distribution summing to one
     */
    @Override
    public INDArray output(INDArray x) {
        DownSamplingLayer d = (DownSamplingLayer)  getSigmoidLayers()[getSigmoidLayers().length - 1];
        INDArray lastLayer = d.getFeatureMap();
        int nY = lastLayer.shape()[3];
        int nX = lastLayer.shape()[2];
        int nM = lastLayer.shape()[1];
        int noBs = lastLayer.slices();

        int nMap = nY * nX;

        INDArray features = NDArrays.create(nMap * nM, noBs);


        for(int j = 0; j < d.getNumFeatureMaps(); j++) {
            INDArray map = d.getFeatureMap().slice(j);
            features.put(new NDArrayIndex[] { NDArrayIndex.interval(j * nMap + 1, j * nMap),NDArrayIndex.interval(0, features.columns())},map.reshape(nMap,noBs));
        }




        return outputLayer.output(features);
    }

    /**
     * Backpropagation of errors for weights
     *
     * @param lr     the learning rate to use
     * @param epochs the number of epochs to iterate (this is already called in finetune)
     */
    @Override
    public void backProp(double lr, int epochs) {
        super.backProp(lr, epochs);
    }

    @Override
    public void init() {
        if(!(rng instanceof SynchronizedRandomGenerator))
            rng = new SynchronizedRandomGenerator(rng);
        if(getnLayers() < 1)
            throw new IllegalStateException("Unable to createComplex network layers; number specified is less than 1");

        if(this.dist == null)
            dist = new NormalDistribution(rng,0,.01,NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

        this.layers = new NeuralNetwork[getnLayers()];

        // construct multi-layer
        int nInY,nInX,nInFM,nFm = -1;
        for(int i = 0; i < getnLayers(); i++) {
            ConvolutionalRBM prevLayer = (ConvolutionalRBM) getLayers()[i];

            if(i == 0) {
                nInY = input.rows();
                nInX = input.columns();
                nInFM = input.slices();
            }

            else {
                nInX = prevLayer.getFmSize()[1];
                nInY = prevLayer.getFmSize()[0];
                nInFM = prevLayer.getFmSize()[0];
            }

            nFm = this.nFm[i];
            INDArray filterSize = ArrayUtil.toNDArray(this.filterSizes[i]);
            INDArray fmSize = ArrayUtil.toNDArray(new int[]{nInY,nInX}).sub(filterSize).add(1);
            double prodFilterSize = ArrayUtil.prod(this.filterSizes[i]);
            INDArray stride = ArrayUtil.toNDArray(this.stride[i]);
            double fanIn = nInFM * prodFilterSize;
            double fanOut = nFm * prodFilterSize;

            double range = 2 * FastMath.sqrt(6 / fanIn + fanOut);
            INDArray W = NDArrays.rand(new int[]{(int) filterSize.getScalar(0).element(),(int) filterSize.getScalar(1).element(),nInFM,nFm},dist).mul(range);

            ConvolutionalRBM r = new ConvolutionalRBM.Builder().withDistribution(getDist())
                    .withDropOut(getDropOut()).withOptmizationAlgo(getOptimizationAlgorithm())
                    .withFilterSize(ArrayUtil.toInts(filterSize)).withInput(input).numberOfVisible(1)
                    .useAdaGrad(isUseAdaGrad()).normalizeByInputRows(normalizeByInputRows)
                    .numHidden(1).withHBias(NDArrays.zeros(nFm,1)).withMomentum(getMomentum())
                    .withL2(getL2()).useRegularization(isUseRegularization())
                    .withRandom(rng).withLossFunction(getLossFunction()).withFmSize(ArrayUtil.toInts(fmSize))
                    .withStride(this.stride[i]).withNumFilters(new int[]{nFm,nFm})
                    .withSparseGain(sparseGain).withSparsity(getSparsity())
                    .withWeights(W).build();
            this.layers[i] = r;


            DownSamplingLayer d = new DownSamplingLayer.Builder()
                    .dist(dist).withStride(this.stride[i])
                    .withFmSize(Transforms.floor(ArrayUtil.toNDArray(r.getFmSize()).div(stride)))
                    .numFeatureMaps(nFm).withBias(r.gethBias())
                    .withRng(rng).build();

            this.sigmoidLayers[i] = d;


        }

        ConvolutionalRBM r= (ConvolutionalRBM) getLayers()[getLayers().length - 1];

        int nFmIn =  r.getNumFilters()[0];
        int nOuts = r.getFmSize()[0] * r.getFmSize()[1] * nFmIn;


        // layer for output using OutputLayer
        this.outputLayer = new OutputLayer.Builder()
                .useAdaGrad(useAdaGrad).optimizeBy(getOptimizationAlgorithm())
                .normalizeByInputRows(normalizeByInputRows)
                .useRegularization(useRegularization)
                .numberOfInputs(nFmIn)
                .numberOfOutputs(nOuts).withL2(l2).build();

        synchonrizeRng();

        applyTransforms();
        initCalled = true;

    }

    /**
     * Creates a hidden layer with the given parameters.
     * The default implementation is a binomial sampling
     * hidden layer, but this can be overridden
     * for other kinds of hidden units
     *
     * @param index
     * @param nIn        the number of inputs
     * @param nOut       the number of outputs
     * @param activation the activation function for the layer
     * @param rng        the rng to use for sampling
     * @param layerInput the layer starting input
     * @param dist       the probability distribution to use
     *                   for generating weights
     * @return a hidden layer with the given parameters
     */
    @Override
    public DownSamplingLayer createHiddenLayer(int index, int nIn, int nOut, ActivationFunction activation, RandomGenerator rng, INDArray layerInput, RealDistribution dist) {
        ConvolutionalRBM r = (ConvolutionalRBM) getLayers()[index - 1];
        DownSamplingLayer layer = new DownSamplingLayer.Builder().dist(dist).withInput(layerInput)
                .withFmSize(Transforms.floor(ArrayUtil.toNDArray(r.getFmSize())).div(ArrayUtil.toNDArray(stride[index])))
                .numFeatureMaps(nFm[index])
                .nIn(nIn).nOut(nOut).withActivation(activation).build();
        return layer;
    }

    /**
     * Creates a layer depending on the index.
     * The main reason this matters is for continuous variations such as the {@link org.deeplearning4j.dbn.DBN}
     * where the first layer needs to be an {@link org.deeplearning4j.rbm.RBM} for continuous inputs.
     * <p/>
     * Please be sure to call super.initializeNetwork
     * <p/>
     * to handle the passing of baseline parameters such as fanin
     * and rendering.
     *
     * @param input    the input to the layer
     * @param nVisible the number of visible inputs
     * @param nHidden  the number of hidden units
     * @param W        the weight vector
     * @param hbias    the hidden bias
     * @param vBias    the visible bias
     * @param rng      the rng to use (THiS IS IMPORTANT; YOU DO NOT WANT TO HAVE A MIS REFERENCED RNG OTHERWISE NUMBERS WILL BE MEANINGLESS)
     * @param index    the index of the layer
     * @return a neural network layer such as {@link org.deeplearning4j.rbm.RBM}
     */
    @Override
    public NeuralNetwork createLayer(INDArray input, int nVisible, int nHidden, INDArray W, INDArray hbias, INDArray vBias, RandomGenerator rng, int index) {
        ConvolutionalRBM r = new ConvolutionalRBM.Builder().withDistribution(getDist())
                .withDropOut(getDropOut()).withOptmizationAlgo(getOptimizationAlgorithm())
                .withFilterSize(filterSizes[index]).withInput(input).numberOfVisible(nVisible)
                .useAdaGrad(isUseAdaGrad()).normalizeByInputRows(normalizeByInputRows)
                .numHidden(nHidden).withHBias(hbias).withMomentum(getMomentum())
                .withL2(getL2()).useRegularization(isUseRegularization())
                .withRandom(rng).withLossFunction(getLossFunction())
                .withStride(stride[index]).withNumFilters(numFilters[index])
                .withSparseGain(sparseGain).withSparsity(getSparsity())
                .withWeights(W).build();

        return r;
    }

    @Override
    public NeuralNetwork[] createNetworkLayers(int numLayers) {
        return new NeuralNetwork[numLayers];
    }
}