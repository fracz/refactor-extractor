package org.deeplearning4j.nn.layers.recurrent;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.layers.BaseLayer;
import org.deeplearning4j.nn.params.GRUParamInitializer;
import org.deeplearning4j.util.Dropout;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

import static org.nd4j.linalg.indexing.NDArrayIndex.interval;

/** Gated Recurrent Unit RNN Layer.
 * @author Alex Black
 */
public class GRU extends BaseLayer {

	public GRU(NeuralNetConfiguration conf) {
		super(conf);
	}

	public GRU(NeuralNetConfiguration conf, INDArray input) {
		super(conf, input);
	}
	@Override
	public Gradient gradient() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Gradient calcGradient(Gradient layerError, INDArray activation){
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Pair<Gradient, INDArray> backpropGradient(INDArray epsilon) {
		//First: Do forward pass to get gate activations etc.
		INDArray[] activations = activateHelper(true);	//Order: {outputActivations,rucZs,rucAs}
		INDArray outputActivations = activations[0];
		INDArray rucZs = activations[1];
		INDArray rucAs = activations[2];

		INDArray inputWeights = getParam(GRUParamInitializer.INPUT_WEIGHT_KEY); //Shape: [n^(L-1),3*n^L], order: [wr,wu,wc]
		INDArray recurrentWeights = getParam(GRUParamInitializer.RECURRENT_WEIGHT_KEY);	//Shape: [n^L,3*n^L]; order: [wR,wU,wC]

		int layerSize = recurrentWeights.size(0);	//i.e., n^L
		int prevLayerSize = inputWeights.size(0);	//n^(L-1)
		int miniBatchSize = epsilon.size(0);
		boolean is2dInput = epsilon.rank() < 3; //Edge case: T=1 may have shape [miniBatchSize,n^(L+1)], equiv. to [miniBatchSize,n^(L+1),1]
		int timeSeriesLength = (is2dInput? 1: epsilon.size(2));

		INDArray wr = inputWeights.get(NDArrayIndex.all(),interval(0,layerSize));
		INDArray wu = inputWeights.get(NDArrayIndex.all(),interval(layerSize,2*layerSize));
		INDArray wc = inputWeights.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize));
		INDArray wR = recurrentWeights.get(NDArrayIndex.all(),interval(0,layerSize));
		INDArray wU = recurrentWeights.get(NDArrayIndex.all(),interval(layerSize,2*layerSize));
		INDArray wC = recurrentWeights.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize));

		INDArray biasGradients = Nd4j.zeros(new int[]{miniBatchSize,3*layerSize}); //Gradients before summing over mini-batch (summed over time)
		INDArray inputWeightGradients = Nd4j.zeros(new int[]{prevLayerSize,3*layerSize});	//Stores sum over each time step
		INDArray recurrentWeightGradients = Nd4j.zeros(new int[]{layerSize,3*layerSize}); //Stores sum over for each time step

		INDArray epsilonNext = Nd4j.zeros(miniBatchSize,prevLayerSize,timeSeriesLength);	//i.e., what would be W^L*(delta^L)^T. Shape: [m,n^(L-1),T]

		INDArray deltaOutNext = Nd4j.zeros(miniBatchSize,layerSize);
		INDArray deltaRNext = deltaOutNext;
		INDArray deltaUNext = deltaOutNext;
		INDArray deltaCNext = deltaOutNext;
		for( int t=timeSeriesLength-1; t>=0; t-- ){
			INDArray prevOut = (t==0 ? Nd4j.zeros(miniBatchSize,layerSize) : outputActivations.tensorAlongDimension(t-1,1,0));	//Shape: [m,n^L]

			INDArray aSlice = (is2dInput ? rucAs : rucAs.tensorAlongDimension(t,1,0));
			INDArray zSlice = (is2dInput ? rucZs : rucZs.tensorAlongDimension(t,1,0));
			INDArray aSliceNext;
			INDArray zSliceNext;
			if(t == timeSeriesLength-1){
				aSliceNext = Nd4j.zeros(miniBatchSize,3*layerSize);
				zSliceNext = Nd4j.zeros(miniBatchSize,3*layerSize);
			} else {
				aSliceNext = rucAs.tensorAlongDimension(t,1,0);
				zSliceNext = rucZs.tensorAlongDimension(t,1,0);
			}

			INDArray zr = zSlice.get(NDArrayIndex.all(),interval(0,layerSize));
			INDArray sigmaPrimeR = Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform("sigmoid", zr).derivative());

			INDArray dOutNextdOut;
			if( t == timeSeriesLength-1 ){
				//No need to calculate, as next delta is null
				dOutNextdOut = Nd4j.zeros(miniBatchSize,layerSize);
			} else {
				INDArray aOut = (is2dInput ? outputActivations : outputActivations.tensorAlongDimension(t,1,0));
				INDArray arNext = aSliceNext.get(NDArrayIndex.all(),interval(0,layerSize));
				INDArray auNext = aSliceNext.get(NDArrayIndex.all(),interval(layerSize,2*layerSize));
				INDArray acNext = aSliceNext.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize));
				INDArray zuNext = zSliceNext.get(NDArrayIndex.all(),interval(layerSize,2*layerSize));
				INDArray zcNext = zSliceNext.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize));

				INDArray sigmaPrimeUNext = Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform("sigmoid", zuNext).derivative());
				INDArray sigmaPrimeCNext = Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(conf.getActivationFunction(), zcNext).derivative());
				INDArray second = aOut.sub(acNext).muli(sigmaPrimeUNext);
				INDArray third = auNext.rsub(1.0).muli(sigmaPrimeCNext);
				INDArray temp = Nd4j.diag(Nd4j.diag(wC)).mmul(arNext.transpose()).transpose()
						.addi(
								Nd4j.diag(Nd4j.diag(wR))
								.mmul( wC.mmul(aOut.transpose()) ).transpose()
								.muli(sigmaPrimeR)
							);
				third.muli(temp);
				dOutNextdOut = auNext.add(second).addi(third);
			}

			//First: Calculate hidden unit deltas (d^{Lt}_h)
			INDArray epsilonSlice = (is2dInput ? epsilon : epsilon.tensorAlongDimension(t,1,0));		//(w^{L+1}*(delta^{(L+1)t})^T)^T or equiv.
			INDArray deltaOut = epsilonSlice
					.add(deltaOutNext.mul(dOutNextdOut.transpose()))
					.addi(deltaRNext.mmul(wR.transpose()))
					.addi(deltaUNext.mmul(wU.transpose()))
					.addi(deltaCNext.mmul(wC.transpose()));

			//Delta at update gate
			INDArray zu = zSlice.get(NDArrayIndex.all(),interval(layerSize,2*layerSize));
			INDArray sigmaPrimeU = Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform("sigmoid", zu).derivative());
			INDArray ac = aSlice.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize));
			INDArray deltaU = deltaOut.mul(sigmaPrimeU).muli(prevOut.sub(ac));

			//Delta for candidate activation
			INDArray zc = zSlice.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize));
			INDArray sigmaPrimeC = Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(conf.getActivationFunction(), zc).derivative());
			INDArray au = aSlice.get(NDArrayIndex.all(),interval(layerSize,2*layerSize));
			INDArray deltaC = deltaOut.mul(sigmaPrimeC).muli(au.rsub(1.0));

			//Delta at reset gate
			INDArray deltaR = deltaC.mul(Nd4j.diag(Nd4j.diag(wC)).mmul(prevOut.transpose()).transpose()).muli(sigmaPrimeR);

			//Add input gradients for this time step:
			INDArray prevLayerActivationSlice = (is2dInput ? input : input.tensorAlongDimension(t,1,0));
			inputWeightGradients.get(NDArrayIndex.all(),interval(0,layerSize))
				.addi(deltaR.transpose().mmul(prevLayerActivationSlice).transpose());
			inputWeightGradients.get(NDArrayIndex.all(),interval(layerSize,2*layerSize))
				.addi(deltaU.transpose().mmul(prevLayerActivationSlice).transpose());
			inputWeightGradients.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize))
				.addi(deltaC.transpose().mmul(prevLayerActivationSlice).transpose());

			//Add recurrent weight gradients for this time step:
			if(t>0){
				recurrentWeightGradients.get(NDArrayIndex.all(),interval(0,layerSize))
					.addi(deltaR.transpose().mmul(prevOut).transpose());
				recurrentWeightGradients.get(NDArrayIndex.all(),interval(layerSize,2*layerSize))
					.addi(deltaU.transpose().mmul(prevOut).transpose());
				recurrentWeightGradients.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize))
					.addi(deltaC.transpose().mmul(prevOut).transpose());
			}

			//Add bias gradients for this time step:
			biasGradients.get(NDArrayIndex.all(),interval(0,layerSize)).addi(deltaR);
			biasGradients.get(NDArrayIndex.all(),interval(layerSize,2*layerSize)).addi(deltaU);
			biasGradients.get(NDArrayIndex.all(),interval(2*layerSize,3*layerSize)).addi(deltaC);

			INDArray epsilonNextSlice = wr.mmul(deltaR.transpose()).transpose()
					.addi(wu.mmul(deltaU.transpose()).transpose())
					.addi(wc.mmul(deltaC.transpose()).transpose());
			epsilonNext.tensorAlongDimension(t,1,0).assign(epsilonNextSlice);

			deltaOutNext = deltaOut;
			deltaRNext = deltaR;
			deltaUNext = deltaU;
			deltaCNext = deltaC;
		}

		Gradient g = new DefaultGradient();
		g.setGradientFor(GRUParamInitializer.INPUT_WEIGHT_KEY, inputWeightGradients);
		g.setGradientFor(GRUParamInitializer.RECURRENT_WEIGHT_KEY,recurrentWeightGradients);
		g.setGradientFor(GRUParamInitializer.BIAS_KEY, biasGradients.sum(0)); //Sum over mini-batch

		return new Pair<>(g,epsilonNext);
	}

	@Override
	public INDArray preOutput(INDArray x) {
		return activate(x,true);
	}

	@Override
	public INDArray preOutput(INDArray x, boolean training) {
		return activate(x, training);
	}

	@Override
	public INDArray activate(INDArray input, boolean training){
		setInput(input, training);
		return activateHelper(training)[0];
	}

	@Override
	public INDArray activate(INDArray input){
		setInput(input);
		return activateHelper(true)[0];
	}

	@Override
	public INDArray activate(boolean training){
		return activateHelper(training)[0];
	}

	@Override
	public INDArray activate(){
		return activateHelper(false)[0];
	}

	/** Returns activations array: {output,rucZs,rucAs} in that order. */
	private INDArray[] activateHelper(boolean training){

		INDArray inputWeights = getParam(GRUParamInitializer.INPUT_WEIGHT_KEY); //Shape: [n^(L-1),3*n^L], order: [wr,wu,wc]
		INDArray recurrentWeights = getParam(GRUParamInitializer.RECURRENT_WEIGHT_KEY);	//Shape: [n^L,3*n^L]; order: [wR,wU,wC]
		INDArray biases = getParam(GRUParamInitializer.BIAS_KEY); //Shape: [1,3*n^L]; order: [br,bu,bc]

		boolean is2dInput = input.rank() < 3;		//Edge case of T=1, may have shape [m,nIn], equiv. to [m,nIn,1]
		int timeSeriesLength = (is2dInput ? 1 : input.size(2));
		int hiddenLayerSize = recurrentWeights.size(0);
		int miniBatchSize = input.size(0);

		//Apply dropconnect to input (not recurrent) weights only:
		if(conf.isUseDropConnect() && training) {
			if (conf.getDropOut() > 0) {
				inputWeights = Dropout.applyDropConnect(this,GRUParamInitializer.INPUT_WEIGHT_KEY);
			}
		}

		//Allocate arrays for activations:
		INDArray outputActivations = Nd4j.zeros(miniBatchSize,hiddenLayerSize,timeSeriesLength);
		INDArray rucZs = Nd4j.zeros(miniBatchSize,3*hiddenLayerSize,timeSeriesLength);	//zs for reset gate, update gate, candidate activation
		INDArray rucAs = Nd4j.zeros(miniBatchSize,3*hiddenLayerSize,timeSeriesLength);	//activations for above

		for( int t=0; t<timeSeriesLength; t++ ){
			INDArray prevLayerInputSlice = (is2dInput ? input : input.tensorAlongDimension(t,1,0));	//[Expected shape: [m,nIn]. Also deals with edge case of T=1, with 'time series' data of shape [m,nIn], equiv. to [m,nIn,1]
			INDArray prevOutputActivations = (t==0 ? Nd4j.zeros(miniBatchSize,hiddenLayerSize) : outputActivations.tensorAlongDimension(t-1,1,0));	//Shape: [m,nL]

			//Calculate reset gate, update gate and candidate zs
			INDArray zs = prevLayerInputSlice.mmul(inputWeights)
					.addi(prevOutputActivations.mmul(recurrentWeights))
					.addiRowVector(biases);	//Shape: [m,3n^L]

			INDArray as = zs.dup();		//Want to apply sigmoid to both reset and update gates; user-settable activation on candidate activation
			Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform("sigmoid",
					as.get(NDArrayIndex.all(),NDArrayIndex.interval(0, 2*hiddenLayerSize))));
			Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(conf.getActivationFunction(),
					as.get(NDArrayIndex.all(),NDArrayIndex.interval(2*hiddenLayerSize,3*hiddenLayerSize))));

			//Finally, calculate output activation:
			INDArray candidateAs = as.get(NDArrayIndex.all(),NDArrayIndex.interval(2*hiddenLayerSize,3*hiddenLayerSize));
			INDArray updateAs = as.get(NDArrayIndex.all(),NDArrayIndex.interval(hiddenLayerSize, 2*hiddenLayerSize));	//from {a_r, a_u, a_c} with shape [m,3*n^L]
			INDArray oneMinUpdateAs = updateAs.rsub(1);
			INDArray outputASlice = updateAs.mul(prevOutputActivations).addi(oneMinUpdateAs.muli(candidateAs));

			rucZs.tensorAlongDimension(t,1,0).assign(zs);
			rucAs.tensorAlongDimension(t,1,0).assign(as);
			outputActivations.tensorAlongDimension(t,1,0).assign(outputASlice);
		}

		return new INDArray[]{outputActivations,rucZs,rucAs};
	}

	@Override
	public INDArray activationMean(){
		return activate();
	}

	@Override
	public Type type(){
		return Type.RECURRENT;
	}

	@Override
	public Layer transpose(){
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
    public double calcL2() {
    	if(!conf.isUseRegularization() || conf.getL2() <= 0.0 ) return 0.0;
    	double l2 = Transforms.pow(getParam(GRUParamInitializer.RECURRENT_WEIGHT_KEY), 2).sum(Integer.MAX_VALUE).getDouble(0)
    			+ Transforms.pow(getParam(GRUParamInitializer.INPUT_WEIGHT_KEY), 2).sum(Integer.MAX_VALUE).getDouble(0);
    	return 0.5 * conf.getL2() * l2;
    }

    @Override
    public double calcL1() {
    	if(!conf.isUseRegularization() || conf.getL1() <= 0.0 ) return 0.0;
        double l1 = Transforms.abs(getParam(GRUParamInitializer.RECURRENT_WEIGHT_KEY)).sum(Integer.MAX_VALUE).getDouble(0)
        		+ Transforms.abs(getParam(GRUParamInitializer.INPUT_WEIGHT_KEY)).sum(Integer.MAX_VALUE).getDouble(0);
        return conf.getL1() * l1;
    }

}