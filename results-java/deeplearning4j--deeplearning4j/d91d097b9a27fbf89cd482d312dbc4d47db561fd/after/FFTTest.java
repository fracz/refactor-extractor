package org.deeplearning4j.fft;

import static org.junit.Assert.*;

import org.deeplearning4j.nn.linalg.ComplexNDArray;
import org.deeplearning4j.nn.linalg.NDArray;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testing FFTs
 */
public class FFTTest {

    private static Logger log = LoggerFactory.getLogger(FFTTest.class);

    @Test
    public void testBasicFFT() {
        DoubleMatrix d = DoubleMatrix.linspace(1,6,6);
        ComplexDoubleMatrix d2 = new ComplexDoubleMatrix(d);
        ComplexDoubleMatrix fft = FFT.fft(d2);
        assertEquals(6,fft.length);
        log.info("FFT " + fft);



    }

    @Test
    public void testMultiDimFFT() {
        //1d case: these should be equal
        DoubleMatrix d = DoubleMatrix.linspace(1,8,8);
        NDArray arr = new NDArray(d.data,new int[]{8});
        ComplexNDArray arr2 = FFT.fftn(arr, 0, 1);
        assertEquals(arr,arr2.getReal());

        ComplexNDArray other = FFT.fftn(arr,1,1);
        assertEquals(1,other.length);
        NDArray single = NDArray.scalar(1.0);
        NDArray real = other.getReal();
        assertEquals(single,real);
    }



    @Test
    public void testFFTOp() {
        NDArray arr = new NDArray(DoubleMatrix.linspace(1,24,24).data,new int[]{4,3,2});
        log.info("Before " + arr);
        arr.iterateOverDimension(0,new FFTSliceOp(arr,arr.shape()[0]));
        log.info("After " + arr);
        arr.iterateOverDimension(1,new FFTSliceOp(arr,arr.shape()[1]));

    }

    @Test
    public void testBasicIFFT() {
        DoubleMatrix d = DoubleMatrix.linspace(1,6,6);
        ComplexDoubleMatrix d2 = new ComplexDoubleMatrix(d);
        ComplexDoubleMatrix fft = FFT.ifft(FFT.fft(d2));
        assertEquals(6,fft.length);
        log.info("IFFT " + fft);

        assertEquals(d2,fft);



    }

    @Test
    public void testIFFT() {
        NDArray arr = new NDArray(DoubleMatrix.linspace(1,24,24).data,new int[]{4,3,2});
        log.info("Before " + arr);
        arr.iterateOverDimension(0,new IFFTSliceOp(arr,arr.shape()[0]));
        log.info("After " + arr);
    }



}