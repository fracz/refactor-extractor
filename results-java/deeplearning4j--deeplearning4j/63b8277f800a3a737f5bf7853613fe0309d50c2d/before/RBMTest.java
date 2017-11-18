package org.deeplearning4j.rbm;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.datasets.DataSet;
import org.deeplearning4j.datasets.fetchers.MnistDataFetcher;
import org.deeplearning4j.nn.NeuralNetwork;
import org.jblas.DoubleMatrix;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RBMTest  {

	private static Logger log = LoggerFactory.getLogger(RBMTest.class);


	@Test
	public void testBasic() {
		double[][] data = new double[][]
				{
				{1,1,1,0,0,0},
				{1,0,1,0,0,0},
				{1,1,1,0,0,0},
				{0,0,1,1,1,0},
				{0,0,1,1,0,0},
				{0,0,1,1,1,0},
				{0,0,1,1,1,0}
				};

		DoubleMatrix d = new DoubleMatrix(data);
		RandomGenerator g = new MersenneTwister(123);

		RBM r = new RBM.Builder().useAdaGrad(true)
				.numberOfVisible(d.columns).numHidden(4).withRandom(g).build();

		r.trainTillConvergence(d,  0.01,new Object[]{1,0.01,1000});

		DoubleMatrix v = new DoubleMatrix(new double[][]
				{{1, 1, 0, 0, 0, 0},
				{0, 0, 0, 1, 1, 0}});

		log.info("Reconstruction " + r.reconstruct(v).toString());

		NeuralNetwork r2 = r.clone();
		assertEquals(r2.getnVisible(),r.getnVisible());
		assertEquals(r2.getnHidden(),r.getnHidden());
		assertEquals(r2.getW(),r.getW());
		assertEquals(r2.gethBias(),r.gethBias());
		assertEquals(r2.getvBias(),r.getvBias());
		r2.trainTillConvergence(d, 0.01,new Object[]{1,0.01,1000});
		log.info("Cross entropy " + r.getReConstructionCrossEntropy());


	}


	@Test
	public void mnistTest() throws Exception {

		MnistDataFetcher fetcher = new MnistDataFetcher(true);
		fetcher.fetch(10);
		DataSet d = fetcher.next();

		RandomGenerator g = new MersenneTwister(123);

		RBM r = new RBM.Builder()
		.numberOfVisible(d.numInputs())
		.numHidden(600)
		.build();



		r.trainTillConvergence(d.getFirst() ,1e-2,new Object[]{1,1e-1,100});




	}



}