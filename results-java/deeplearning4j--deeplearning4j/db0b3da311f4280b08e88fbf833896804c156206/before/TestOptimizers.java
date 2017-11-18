package org.deeplearning4j.optimize.solver;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.IrisDataSetIterator;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.conf.override.ConfOverride;
import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.ConvexOptimizer;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.solvers.ConjugateGradient;
import org.deeplearning4j.optimize.solvers.LineGradientDescent;
import org.deeplearning4j.optimize.solvers.StochasticGradientDescent;
import org.deeplearning4j.optimize.solvers.LBFGS;
import org.deeplearning4j.optimize.stepfunctions.DefaultStepFunction;
import org.junit.Test;
import org.nd4j.linalg.api.complex.IComplexNumber;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.DefaultRandom;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.conditions.Condition;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

public class TestOptimizers {

	@Test
	public void testOptimizersBasicMLPBackprop(){
		//Basic tests of the 'does it throw an exception' variety.

		DataSetIterator iter = new IrisDataSetIterator(5,50);

		for( OptimizationAlgorithm oa : OptimizationAlgorithm.values() ){
			MultiLayerNetwork network = new MultiLayerNetwork(getMLPConfigIris(oa));
			network.init();

			iter.reset();
			network.fit(iter);
		}
	}

	private static MultiLayerConfiguration getMLPConfigIris( OptimizationAlgorithm oa ){
		MultiLayerConfiguration c = new NeuralNetConfiguration.Builder()
		.nIn(4).nOut(3)
		.weightInit(WeightInit.DISTRIBUTION)
		.dist(new NormalDistribution(0, 0.1))

		.activationFunction("sigmoid")
		.lossFunction(LossFunction.MCXENT)
		.optimizationAlgo(oa)
		.iterations(1)
		.batchSize(5)
		.constrainGradientToUnitNorm(false)
		.corruptionLevel(0.0)
		.layer(new RBM())
		.learningRate(0.1).useAdaGrad(false)
		.regularization(true)
		.l2(0.01)
		.applySparsity(false).sparsity(0.0)
		.seed(12345L)
		.list(4).hiddenLayerSizes(8,10,5)
		.backward(true).pretrain(false)
		.useDropConnect(false)

		.override(3, new ConfOverride() {
			@Override
			public void overrideLayer(int i, NeuralNetConfiguration.Builder builder) {
				builder.activationFunction("softmax");
				builder.layer(new OutputLayer());
				builder.weightInit(WeightInit.DISTRIBUTION);
				builder.dist(new NormalDistribution(0, 0.1));
			}
		}).build();

		return c;
	}

	//==================================================
	// Sphere Function Optimizer Tests

	@Test
	public void testSphereFnOptStochGradDescent(){
		testSphereFnOptHelper(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT,-1,2);
		testSphereFnOptHelper(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT,-1,10);
		testSphereFnOptHelper(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT,-1,100);
	}

	@Test
	public void testSphereFnOptLineGradDescent(){
		//Test a single line search with calculated search direction (with multiple line search iterations)
		int[] numLineSearchIter = {1,2,5,10};
		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.LINE_GRADIENT_DESCENT,n,2);

		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.LINE_GRADIENT_DESCENT,n,10);

		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.LINE_GRADIENT_DESCENT,n,100);
	}

	@Test
	public void testSphereFnOptCG(){
		//Test a single line search with calculated search direction (with multiple line search iterations)
		int[] numLineSearchIter = {1,2,5,10};
		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.CONJUGATE_GRADIENT,n,2);

		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.CONJUGATE_GRADIENT,n,10);

		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.CONJUGATE_GRADIENT,n,100);
	}

	@Test
	public void testSphereFnOptLBFGS(){
		//Test a single line search with calculated search direction (with multiple line search iterations)
		int[] numLineSearchIter = {1,2,5,10};
		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.LBFGS,n,2);

		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.LBFGS,n,10);

		for( int n : numLineSearchIter )
			testSphereFnOptHelper(OptimizationAlgorithm.LBFGS,n,100);
	}

	//For debugging.
	private static final boolean PRINT_OPT_RESULTS = true;
	public void testSphereFnOptHelper( OptimizationAlgorithm oa, int numLineSearchIter, int nDimensions ){

		if( PRINT_OPT_RESULTS ) System.out.println("---------\n Alg=" + oa
				+ ", nIter=" + numLineSearchIter + ", nDimensions=" + nDimensions );

		NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
		.maxNumLineSearchIterations(numLineSearchIter)
		.iterations(1)
		.learningRate(0.01)
		.layer(new RBM()).batchSize(1).build();
		conf.addVariable("x");	//Normally done by ParamInitializers, but obviously that isn't done here

		Random rng = new DefaultRandom(12345L);
		org.nd4j.linalg.api.rng.distribution.Distribution dist
			= new org.nd4j.linalg.api.rng.distribution.impl.UniformDistribution(rng,-10, 10);
		Model m = new SphereFunctionModel(nDimensions,dist,conf);

		double scoreBefore = m.score();
		assertTrue(!Double.isNaN(scoreBefore) && !Double.isInfinite(scoreBefore));
		if( PRINT_OPT_RESULTS ){
			System.out.println("Before:");
			System.out.println(scoreBefore);
			System.out.println(m.params());
		}

		ConvexOptimizer opt = getOptimizer(oa,conf,m);

		opt.setupSearchState(m.gradientAndScore());
		opt.optimize();

		double scoreAfter = m.score();
		assertTrue(!Double.isNaN(scoreAfter) && !Double.isInfinite(scoreAfter));
		if( PRINT_OPT_RESULTS ){
			System.out.println("After:");
			System.out.println(scoreAfter);
			System.out.println(m.params());
		}

		//Expected behaviour after optimization:
		//(a) score is better (lower) after optimization.
		//(b) Parameters are closer to minimum after optimization (TODO)
		assertTrue("Score did not improve after optimization (b="+scoreBefore+",a="+scoreAfter+")",scoreAfter < scoreBefore);
	}

	private static ConvexOptimizer getOptimizer( OptimizationAlgorithm oa, NeuralNetConfiguration conf, Model m ){
		switch(oa){
		case STOCHASTIC_GRADIENT_DESCENT:
			return new StochasticGradientDescent(conf,new DefaultStepFunction(),null,m);
		case LINE_GRADIENT_DESCENT:
			return new LineGradientDescent(conf,new DefaultStepFunction(),null,m);
		case CONJUGATE_GRADIENT:
			return new ConjugateGradient(conf,new DefaultStepFunction(),null,m);
		case LBFGS:
			return new LBFGS(conf,new DefaultStepFunction(),null,m);
		default:
			throw new UnsupportedOperationException();
		}
	}


	@Test
	public void testSphereFnOptStochGradDescentMultipleSteps(){
		//Earlier tests: only do a single line search, though each line search will do multiple iterations
		// of line search algorithm.
		//Here, do multiple optimization runs + multiple line search iterations within each run
		//i.e., gradient is re-calculated at each step/run
		//Single step tests earlier won't test storing of state between iterations

		testSphereFnMultipleStepsHelper(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT,5,5);
	}

	@Test
	public void testSphereFnOptLineGradDescentMultipleSteps(){
		testSphereFnMultipleStepsHelper(OptimizationAlgorithm.LINE_GRADIENT_DESCENT,5,5);
	}

	@Test
	public void testSphereFnOptCGMultipleSteps(){
		testSphereFnMultipleStepsHelper(OptimizationAlgorithm.CONJUGATE_GRADIENT,5,5);
	}

	@Test
	public void testSphereFnOptLBFGSMultipleSteps(){
		testSphereFnMultipleStepsHelper(OptimizationAlgorithm.LBFGS,5,5);
	}


	private static void testSphereFnMultipleStepsHelper( OptimizationAlgorithm oa, int nOptIter, int maxNumLineSearchIter ){
		double[] scores = new double[nOptIter+1];

		for( int i=0; i<=nOptIter; i++ ){
			Random rng = new DefaultRandom(12345L);
			org.nd4j.linalg.api.rng.distribution.Distribution dist
			= new org.nd4j.linalg.api.rng.distribution.impl.UniformDistribution(rng,-10, 10);
			NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
			.maxNumLineSearchIterations(maxNumLineSearchIter)
			.iterations(i)
			.learningRate(0.01)
			.layer(new RBM()).batchSize(1).build();
			conf.addVariable("x");	//Normally done by ParamInitializers, but obviously that isn't done here

			Model m = new SphereFunctionModel(100,dist,conf);
			if( i == 0 ){
				scores[0] = m.score();	//Before optimization
			} else {
				ConvexOptimizer opt = getOptimizer(oa,conf,m);
				opt.optimize();
				scores[i] = m.score();
				assertTrue(!Double.isNaN(scores[i]) && !Double.isInfinite(scores[i]));
			}
		}

		if( PRINT_OPT_RESULTS ){
			System.out.println("Multiple optimization iterations ("+nOptIter+" opt. iter.) score vs iteration, maxNumLineSearchIter=" + maxNumLineSearchIter +": " + oa );
			System.out.println(Arrays.toString(scores));
		}
		for( int i=1; i<scores.length; i++ ){
			assertTrue( scores[i] < scores[i-1] || (scores[i] == 0.0 && scores[i-1] == 0.0) );
		}
		assertTrue(scores[scores.length-1]<1.0);	//Very easy function, expect score ~= 0 with any reasonable number of steps/numLineSearchIter
	}


	/** A non-NN optimization problem. Optimization function (cost function) is
	 * \sum_i x_i^2. Has minimum of 0.0 at x_i=0 for all x_i
	 * See: https://en.wikipedia.org/wiki/Test_functions_for_optimization
	 */
	private static class SphereFunctionModel extends SimpleOptimizableModel {
		private static final long serialVersionUID = -6963606137417355405L;

		private SphereFunctionModel( int nParams, org.nd4j.linalg.api.rng.distribution.Distribution distribution,
				NeuralNetConfiguration conf ){
			super(distribution.sample(new int[]{1,nParams}), conf);
		}

		@Override
		public double score() {
			return Nd4j.getBlasWrapper().dot(parameters, parameters);	//sum_i x_i^2
		}

		@Override
		public Gradient gradient() {
			// Gradients: d(x^2)/dx = 2x
			INDArray gradient = parameters.mul(2);
			Gradient g = new DefaultGradient();
			g.gradientForVariable().put("x", gradient);
			return g;
		}
	}


	//==================================================
	// Rastrigin Function Optimizer Tests


	@Test
	public void testRastriginFnOptStochGradDescentMultipleSteps(){
		testRastriginFnMultipleStepsHelper(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT,10,20);
	}

	@Test
	public void testRastriginFnOptLineGradDescentMultipleSteps(){
		testRastriginFnMultipleStepsHelper(OptimizationAlgorithm.LINE_GRADIENT_DESCENT,10,20);
	}

	@Test
	public void testRastriginFnOptCGMultipleSteps(){
		testRastriginFnMultipleStepsHelper(OptimizationAlgorithm.CONJUGATE_GRADIENT,10,20);
	}

	@Test
	public void testRastriginFnOptLBFGSMultipleSteps(){
		testRastriginFnMultipleStepsHelper(OptimizationAlgorithm.LBFGS,10,20);
	}


	private static void testRastriginFnMultipleStepsHelper( OptimizationAlgorithm oa, int nOptIter, int maxNumLineSearchIter ){
		double[] scores = new double[nOptIter+1];

		for( int i=0; i<=nOptIter; i++ ){
			NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
			.maxNumLineSearchIterations(maxNumLineSearchIter)
			.iterations(i)
			.learningRate(0.01)
			.layer(new RBM()).batchSize(1).build();
			conf.addVariable("x");	//Normally done by ParamInitializers, but obviously that isn't done here

			Model m = new RastriginFunctionModel(100,conf);
			if( i == 0 ){
				scores[0] = m.score();	//Before optimization
			} else {
				ConvexOptimizer opt = getOptimizer(oa,conf,m);
				opt.optimize();
				scores[i] = m.score();
				assertTrue(!Double.isNaN(scores[i]) && !Double.isInfinite(scores[i]));
			}
		}

		if( PRINT_OPT_RESULTS ){
			System.out.println("Rastrigin: Multiple optimization iterations ("+nOptIter+" opt. iter.) score vs iteration, maxNumLineSearchIter=" + maxNumLineSearchIter +": " + oa );
			System.out.println(Arrays.toString(scores));
		}
		for( int i=1; i<scores.length; i++ ){
			assertTrue( scores[i] < scores[i-1] || (scores[i] == 0.0 && scores[i-1] == 0.0) );
		}
	}

	/** Rastrigin function: A much more complex non-NN multi-dimensional optimization problem.
	 * Global minimum of 0 at x_i = 0 for all x_i.
	 * Very large number of local minima. Can't expect to achieve global minimum with gradient-based (line search)
	 * optimizers, but can expect significant improvement in score/cost relative to initial parameters.
	 * This implementation has cost function = infinity if any parameters x_i are
	 * outside of range [-5.12,5.12]
	 * https://en.wikipedia.org/wiki/Rastrigin_function
	 */
	private static class RastriginFunctionModel extends SimpleOptimizableModel {
		private static final long serialVersionUID = -1772954508787487941L;

		private RastriginFunctionModel(int nDimensions,NeuralNetConfiguration conf){
			super(initParams(nDimensions),conf);
		}

		private static INDArray initParams( int nDimensions ){
			Random rng = new DefaultRandom(12345L);
			org.nd4j.linalg.api.rng.distribution.Distribution dist
			= new org.nd4j.linalg.api.rng.distribution.impl.UniformDistribution(rng,-5.12, 5.12);
			return dist.sample(new int[]{1,nDimensions});
		}

		@Override
		public double score() {
			//If any parameters are outside range [-5.12,5.12]: score = infinity
			INDArray paramExceeds512 = parameters.cond(new Condition(){
				@Override
				public Boolean apply(Number input) {
					return Math.abs(input.doubleValue()) > 5.12;
				}

				@Override
				public Boolean apply(IComplexNumber input){ throw new UnsupportedOperationException(); };
			});

			int nExceeds512 = paramExceeds512.sum(Integer.MAX_VALUE).getInt(0);
			if( nExceeds512 > 0 ) return Double.POSITIVE_INFINITY;

			//Otherwise:
			int nDim = parameters.length();
			double costFn = 10*parameters.length();

			for( int i=0; i<nDim; i++ ){
				double xi = parameters.getDouble(i);
				costFn += xi*xi - 10.0*Math.cos(2.0*Math.PI*xi);
			}
			return costFn;
		}

		@Override
		public Gradient gradient() {
			//Gradient decomposes due to sum, so:
			//d(x^2 - 10*cos(2*Pi*x))/dx
			// = 2x + 20*pi*sin(2*Pi*x)
			int nDim = parameters.length();
			INDArray gradient = Nd4j.zeros(nDim);
			for( int i=0; i<nDim; i++ ){
				double xi = parameters.getDouble(i);
				double g = 2*xi + 20*Math.PI*Math.sin(2*Math.PI*xi);
				gradient.put(0, i, g);
			}
			Gradient g = new DefaultGradient();
			g.gradientForVariable().put("x", gradient);
			return g;
		}
	}


	//==================================================
	// Rosenbrock Function Optimizer Tests

	@Test
	public void testRosenbrockFnOptStochGradDescentMultipleSteps(){
		testRosenbrockFnMultipleStepsHelper(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT,20,20);
	}

	@Test
	public void testRosenbrockFnOptLineGradDescentMultipleSteps(){
		testRosenbrockFnMultipleStepsHelper(OptimizationAlgorithm.LINE_GRADIENT_DESCENT,20,20);
	}

	@Test
	public void testRosenbrockFnOptCGMultipleSteps(){
		testRosenbrockFnMultipleStepsHelper(OptimizationAlgorithm.CONJUGATE_GRADIENT,20,20);
	}

	@Test
	public void testRosenbrockFnOptLBFGSMultipleSteps(){
		testRosenbrockFnMultipleStepsHelper(OptimizationAlgorithm.LBFGS,20,20);
	}


	private static void testRosenbrockFnMultipleStepsHelper( OptimizationAlgorithm oa, int nOptIter, int maxNumLineSearchIter ){
		double[] scores = new double[nOptIter+1];

		for( int i=0; i<=nOptIter; i++ ){
			NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
			.maxNumLineSearchIterations(maxNumLineSearchIter)
			.iterations(i)
			.learningRate(0.01)
			.layer(new RBM()).batchSize(1).build();
			conf.addVariable("x");	//Normally done by ParamInitializers, but obviously that isn't done here

			Model m = new RosenbrockFunctionModel(100,conf);
			if( i == 0 ){
				scores[0] = m.score();	//Before optimization
			} else {
				ConvexOptimizer opt = getOptimizer(oa,conf,m);
				opt.optimize();
				scores[i] = m.score();
				assertTrue("NaN or infinite score: " + scores[i], !Double.isNaN(scores[i]) && !Double.isInfinite(scores[i]));
			}
		}

		if( PRINT_OPT_RESULTS ){
			System.out.println("Rosenbrock: Multiple optimization iterations ("+nOptIter+" opt. iter.) score vs iteration, maxNumLineSearchIter=" + maxNumLineSearchIter +": " + oa );
			System.out.println(Arrays.toString(scores));
		}
		for( int i=1; i<scores.length; i++ ){
			assertTrue( scores[i] < scores[i-1] || (scores[i] == 0.0 && scores[i-1] == 0.0) );
		}
	}



	/**Rosenbrock function: a multi-dimensional 'valley' type function.
	 * Has a single local/global minimum of f(x)=0 at x_i=1 for all x_i.
	 * Expect gradient-based optimization functions to find global minimum eventually,
	 * but optimization may be slow due to nearly flat gradient along valley.
	 * Restricted here to the range [-5,5]. This implementation gives infinite cost/score
	 * if any parameter is outside of this range.
	 * Parameters initialized in range [-4,4]
	 * See: http://www.sfu.ca/~ssurjano/rosen.html
	 */
	private static class RosenbrockFunctionModel extends SimpleOptimizableModel {
		private static final long serialVersionUID = -5129494342531033706L;

		private RosenbrockFunctionModel(int nDimensions, NeuralNetConfiguration conf){
			super(initParams(nDimensions),conf);
		}

		private static INDArray initParams( int nDimensions ){
			Random rng = new DefaultRandom(12345L);
			org.nd4j.linalg.api.rng.distribution.Distribution dist
			= new org.nd4j.linalg.api.rng.distribution.impl.UniformDistribution(rng,-4.0, 4.0);
			return dist.sample(new int[]{1,nDimensions});
		}

		@Override
		public double score() {
			INDArray paramExceeds5 = parameters.cond(new Condition(){
				@Override
				public Boolean apply(Number input) {
					return Math.abs(input.doubleValue()) > 5.0;
				}

				@Override
				public Boolean apply(IComplexNumber input){ throw new UnsupportedOperationException(); };
			});

			int nExceeds5 = paramExceeds5.sum(Integer.MAX_VALUE).getInt(0);
			if( nExceeds5 > 0 ) return Double.POSITIVE_INFINITY;

			int nDims = parameters.length();
			double score = 0.0;
			for( int i=0; i<nDims-1; i++ ){
				double xi = parameters.getDouble(i);
				double xi1 = parameters.getDouble(i+1);
				score += 100.0*Math.pow((xi1-xi*xi), 2.0) + (xi-1)*(xi-1);
			}
			return score;
		}

		@Override
		public Gradient gradient() {
			int nDims = parameters.length();
			INDArray gradient = Nd4j.zeros(nDims);
			double x0 = parameters.getDouble(0);
			double x1 = parameters.getDouble(1);
			double g0 = -400*x0*(x1-x0*x0) + 2*(x0-1);
			gradient.put(0, 0, g0);
			for( int i=1; i<nDims-1; i++ ){
				double xim1 = parameters.getDouble(i-1);
				double xi = parameters.getDouble(i);
				double xip1 = parameters.getDouble(i+1);
				double g = 200*(xi-xim1*xim1) - 400*xi*(xip1-xi*xi) + 2*(xi-1);
				gradient.put(0, i,g);
			}
			double xl = parameters.getDouble(nDims-1);
			double xlm1 = parameters.getDouble(nDims-2);
			double gl = 200*(xl-xlm1*xlm1);
			gradient.put(0, nDims-1,gl);
			Gradient g = new DefaultGradient();
			g.gradientForVariable().put("x", gradient);
			return g;
		}
	}


	/** Simple abstract class to deal with the fact that we don't care about the majority of the Model/Layer
	 * methods here. Classes extending this model for optimizer tests need only implement the score() and
	 * gradient() methods.
	 */
	private static abstract class SimpleOptimizableModel implements Model, Layer {
		private static final long serialVersionUID = 4409380971404019303L;
		protected INDArray parameters;
		protected final NeuralNetConfiguration conf;

		/**@param parameterInit Initial parameters. Also determines dimensionality of problem. Should be row vector.
		 */
		private SimpleOptimizableModel( INDArray parameterInit, NeuralNetConfiguration conf ){
			this.parameters = parameterInit.dup();
			this.conf = conf;
		}

		@Override
		public void fit() { throw new UnsupportedOperationException(); }

		@Override
		public void update(INDArray gradient, String paramType) {
			if(!"x".equals(paramType)) throw new UnsupportedOperationException();
			parameters.subi(gradient);
		}

		@Override
		public double l2Magnitude() {
			return 0;
		}

		@Override
		public double l1Magnitude() {
			return 0;
		}

		@Override
		public void setScore() { }

		@Override
		public void accumulateScore(double accum) { throw new UnsupportedOperationException(); }

		@Override
		public INDArray transform(INDArray data) { throw new UnsupportedOperationException(); }

		@Override
		public INDArray params() {return parameters; }

		@Override
		public int numParams() { return parameters.length(); }

		@Override
		public void setParams(INDArray params) { this.parameters = params; }

		@Override
		public void fit(INDArray data) { throw new UnsupportedOperationException(); }

		@Override
		public void iterate(INDArray input) { throw new UnsupportedOperationException(); }

		@Override
		public Pair<Gradient, Double> gradientAndScore() {
			return new Pair<Gradient,Double>(gradient(),score());
		}

		@Override
		public int batchSize() { return 1; }

		@Override
		public NeuralNetConfiguration conf() { return conf; }

		@Override
		public void setConf(NeuralNetConfiguration conf) { throw new UnsupportedOperationException(); }

		@Override
		public INDArray input() {
			//Work-around for BaseUpdater.postApply(): Uses Layer.input().size(0)
			//in order to get mini-batch size. i.e., divide by 1 here.
			return Nd4j.zeros(1);
		}

		@Override
		public void validateInput() { }

		@Override
		public ConvexOptimizer getOptimizer() { throw new UnsupportedOperationException(); }

		@Override
		public INDArray getParam(String param) { return parameters; }

		@Override
		public void initParams() { throw new UnsupportedOperationException(); }

		@Override
		public Map<String, INDArray> paramTable() { throw new UnsupportedOperationException(); }

		@Override
		public void setParamTable(Map<String, INDArray> paramTable) { throw new UnsupportedOperationException(); }

		@Override
		public void setParam(String key, INDArray val) { throw new UnsupportedOperationException(); }

		@Override
		public void clear() { throw new UnsupportedOperationException(); }

		@Override
		public Type type() { throw new UnsupportedOperationException(); }

		@Override
		public Gradient error(INDArray input) { throw new UnsupportedOperationException(); }

		@Override
		public INDArray derivativeActivation(INDArray input) { throw new UnsupportedOperationException(); }

		@Override
		public Gradient calcGradient(Gradient layerError, INDArray indArray) { throw new UnsupportedOperationException(); }

		@Override
		public Gradient errorSignal(Gradient error, INDArray input){ throw new UnsupportedOperationException(); }

		@Override
		public Gradient backwardGradient(INDArray z, Layer nextLayer,
				Gradient nextGradient, INDArray activation) { throw new UnsupportedOperationException(); }

		@Override
		public void merge(Layer layer, int batchSize) { throw new UnsupportedOperationException(); }

		@Override
		public INDArray activationMean() { throw new UnsupportedOperationException(); }

		@Override
		public INDArray preOutput(INDArray x) { throw new UnsupportedOperationException(); }

		@Override
		public INDArray activate() { throw new UnsupportedOperationException(); }

		@Override
		public INDArray activate(INDArray input) { throw new UnsupportedOperationException(); }

		@Override
		public Layer transpose() { throw new UnsupportedOperationException(); }

		@Override
		public Layer clone() { throw new UnsupportedOperationException(); }

		@Override
		public Pair<Gradient, Gradient> backWard(Gradient errors,
				Gradient deltas, INDArray activation, String previousActivation) { throw new UnsupportedOperationException(); }

		@Override
		public Collection<IterationListener> getIterationListeners() { return null; }

		@Override
		public void setListeners(Collection<IterationListener> listeners) { throw new UnsupportedOperationException(); }

		@Override
		public void setIndex(int index) { throw new UnsupportedOperationException(); }

		@Override
		public int getIndex() { throw new UnsupportedOperationException(); }
	}
}