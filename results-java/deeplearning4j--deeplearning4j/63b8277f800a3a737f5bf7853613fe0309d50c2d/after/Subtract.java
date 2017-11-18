package org.deeplearning4j.linalg.transformation;


import org.deeplearning4j.linalg.api.ndarray.INDArray;

public class Subtract implements MatrixTransform {

	/**
	 *
	 */
	private static final long serialVersionUID = -604699802899787537L;

	@Override
	public INDArray apply(INDArray input) {
		return input.sub(input);
	}


}