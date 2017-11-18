package org.deeplearning4j.optimize;

import org.deeplearning4j.exception.InvalidStepException;
import org.deeplearning4j.linalg.api.ndarray.INDArray;

/**
 * Line optimizer interface adapted from mallet
 * @author Adam Gibson
 *
 */
public interface LineOptimizerMatrix {
	/** Returns the last step size used. */
	public float optimize (INDArray line, int iteration,float initialStep) throws InvalidStepException;

	public interface ByGradient	{
		/** Returns the last step size used. */
		public float optimize (INDArray line, int iteration,float initialStep) throws InvalidStepException;
	}
}