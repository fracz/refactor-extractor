package org.deeplearning4j.fft;

import static org.junit.Assert.*;

import org.deeplearning4j.nn.linalg.ComplexNDArray;
import org.deeplearning4j.nn.linalg.NDArray;
import org.jblas.ComplexFloat;
import org.jblas.ComplexFloatMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.FloatMatrix;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1d fft tests
 *
 * @author Adam Gibson
 */
public class VectorFFTTest {
    private static Logger log = LoggerFactory.getLogger(VectorFFTTest.class);
    private double[] testVector = new double[]{
            55.00000000
            ,0.00000000e+00
            ,-26.37586651
            ,-2.13098631e+01
            ,12.07106781
            ,2.58578644e+00
            ,-9.44674873
            ,1.75576651e+00
            ,5.00000000
            ,-6.00000000e+00
            ,-0.89639702
            ,5.89790214e+00
            ,-2.07106781
            ,-5.41421356e+00
            ,4.71901226
            ,2.83227249e+00
            ,-5.00000000
            ,-6.12323400e-15
            , 4.71901226
            ,-2.83227249e+00
            ,-2.07106781
            ,5.41421356e+00
            ,-0.89639702
            ,-5.89790214e+00
            , 5.00000000
            ,6.00000000e+00
            ,-9.44674873
            ,-1.75576651e+00
            ,  12.07106781
            ,-2.58578644e+00
            , -26.37586651
            ,2.13098631e+01
    };

    private float[] testFloatVector = new float[]{ 55f , 0f  , -5,  1.53884177e01f ,  -5f , 6.88190960e00f,   -5f , 3.63271264e00f ,  -5f , 1.62459848e00f  , -5f, 4.44089210e-16f, -5.f , -1.62459848e00f  , -5.f , -3.63271264e00f ,  -5.f , -6.88190960e00f,  -5.f , -1.53884177e01f};



    @Test
    public void testColumnVector() {
        ComplexNDArray n = new VectorFFT(8).apply(ComplexNDArray.linspace(1, 8,8));
        ComplexNDArray assertion = new ComplexNDArray(new double[]
                { 36.,0.,-4.,9.65685425 ,-4.,4,-4.,1.65685425,-4.,0.,-4.,-1.65685425,-4.,-4.,-4.,-9.65685425
                },new int[]{8});
        assertEquals(n,assertion);

    }





    @Test
    public void testWithOffset() {
        NDArray n = new NDArray(DoubleMatrix.linspace(1, 30, 30).data,new int[]{3,5,2});
        NDArray swapped   = n.swapAxes(n.shape().length - 1,1);
        VectorFFT op = new VectorFFT(5);
        NDArray firstSlice = swapped.slice(0).slice(0);
        ComplexNDArray test = new ComplexNDArray(firstSlice);
        ComplexNDArray testNoOffset = new ComplexNDArray(new double[]{1,0,3,0,5,0,7,0,9,0},new int[]{5});
        assertEquals(op.apply(testNoOffset),op.apply(test));

    }


}