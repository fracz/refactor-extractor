package org.deeplearning4j.linalg.factory;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.linalg.api.complex.IComplexDouble;
import org.deeplearning4j.linalg.api.complex.IComplexFloat;
import org.deeplearning4j.linalg.api.complex.IComplexNDArray;
import org.deeplearning4j.linalg.api.complex.IComplexNumber;
import org.deeplearning4j.linalg.api.ndarray.INDArray;

import java.util.*;

/**
 *
 * Creation of ndarrays via classpath discovery.
 *
 *
 * @author Adam Gibson
 */
public interface NDArrayFactory  {



    public final static char FORTRAN = 'f';
    public final static char C = 'c';





    /**
     * Sets the order. Primarily for testing purposes
     * @param order
     */
    void setOrder(char order);

    /**
     * Sets the data type
     * @param dtype
     */
    void setDType(String dtype);

    /**
     * Returns the order for this ndarray for internal data storage
     * @return the order (c or f)
     */
    public char order();

    /**
     * Returns the data type for this ndarray
     * @return the data type for this ndarray
     */
    public String dtype();

    /**
     * Creates a complex ndarray with the specified shape
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
    public IComplexNDArray createComplex(int rows,int columns,int[] stride,int offset);

    /**
     * Generate a linearly spaced vector
     * @param lower upper bound
     * @param upper lower bound
     * @param num the step size
     * @return the linearly spaced vector
     */
     INDArray linspace(int lower,int upper,int num);


     INDArray toFlattened(Collection<INDArray> matrices);


     INDArray toFlattened(int length,Iterator<? extends INDArray>...matrices);




    /**
     * Returns a column vector where each entry is the nth bilinear
     * product of the nth slices of the two tensors.
     */
     INDArray bilinearProducts(INDArray curr,INDArray in);

     INDArray toFlattened(INDArray...matrices);

    /**
     * Create the identity ndarray
     * @param n the number for the identity
     * @return
     */
     INDArray eye(int n);

    /**
     * Rotate a matrix 90 degrees
     * @param toRotate the matrix to rotate
     * @return the rotated matrix
     */
     void rot90(INDArray toRotate);
    /**
     * Reverses the passed in matrix such that m[0] becomes m[m.length - 1] etc
     * @param reverse the matrix to reverse
     * @return the reversed matrix
     */
     INDArray rot(INDArray reverse);

    /**
     * Reverses the passed in matrix such that m[0] becomes m[m.length - 1] etc
     * @param reverse the matrix to reverse
     * @return the reversed matrix
     */
     INDArray reverse(INDArray reverse);


    /**
     *  Array of evenly spaced values.
     * @param begin the begin of the range
     * @param end the end of the range
     * @return the range vector
     */
     INDArray arange(double begin, double end);

    /**
     * Create float
     * @param real real component
     * @param imag imag component
     * @return
     */
     IComplexFloat createFloat(float real,float imag);


    /**
     * Create an instance of a complex double
     * @param real the real component
     * @param imag the imaginary component
     * @return a new imaginary double with the specified real and imaginary components
     */
     IComplexDouble createDouble(double real,double imag);


    /**
     * Copy a to b
     * @param a the origin matrix
     * @param b the destination matrix
     */
     void copy(INDArray a,INDArray b);

    /**
     * Generates a random matrix between min and max
     * @param shape the number of rows of the matrix
     * @param min the minimum number
     * @param max the maximum number
     * @param rng the rng to use
     * @return a drandom matrix of the specified shape and range
     */
     INDArray rand(int[] shape,float min,float max,RandomGenerator rng);
    /**
     * Generates a random matrix between min and max
     * @param rows the number of rows of the matrix
     * @param columns the number of columns in the matrix
     * @param min the minimum number
     * @param max the maximum number
     * @param rng the rng to use
     * @return a drandom matrix of the specified shape and range
     */
     INDArray rand(int rows, int columns,float min,float max,RandomGenerator rng);

     INDArray appendBias(INDArray...vectors);

    /**
     * Create an ndarray with the given data layout
     * @param data the data to create the ndarray with
     * @return the ndarray with the given data layout
     *
     */
    INDArray create(double[][] data);

    /**
     * Create a complex ndarray from the passed in indarray
     * @param arr the arr to wrap
     * @return the complex ndarray with the specified ndarray as the
     * real components
     */
     IComplexNDArray createComplex(INDArray arr);

    /**
     * Create a complex ndarray from the passed in indarray
     * @param data the data to wrap
     * @return the complex ndarray with the specified ndarray as the
     * real components
     */
     IComplexNDArray createComplex(IComplexNumber[] data,int[] shape);


    /**
     * Create a complex ndarray from the passed in indarray
     * @param arrs the arr to wrap
     * @return the complex ndarray with the specified ndarray as the
     * real components
     */
     IComplexNDArray createComplex(List<IComplexNDArray> arrs,int[] shape);


    /**
     * Create a random ndarray with the given shape using the given rng
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @param r the random generator to use
     * @return the random ndarray with the specified shape
     */
     INDArray rand(int rows,int columns,RandomGenerator r);
     /**
     * Create a random ndarray with the given shape using the given rng
     * @param rows the number of rows in the matrix
     * @param columns the columns of the ndarray
     * @param seed the  seed to use
     * @return the random ndarray with the specified shape
     */
     INDArray rand(int rows,int columns,long seed);
    /**
     * Create a random ndarray with the given shape using
     * the current time as the seed
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return the random ndarray with the specified shape
     */
     INDArray rand(int rows,int columns);
    /**
     * Random normal using the given rng
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @param r the random generator to use
     * @return
     */
     INDArray randn(int rows,int columns,RandomGenerator r);

    /**
     * Random normal using the current time stamp
     * as the seed
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return
     */
     INDArray randn(int rows,int columns);
    /**
     * Random normal using the specified seed
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return
     */
     INDArray randn(int rows,int columns,long seed);





    /**
     * Create a random ndarray with the given shape using the given rng
     * @param shape the shape of the ndarray
     * @param r the random generator to use
     * @return the random ndarray with the specified shape
     */
     INDArray rand(int[] shape,RealDistribution r);

    /**
     * Create a random ndarray with the given shape using the given rng
     * @param shape the shape of the ndarray
     * @param r the random generator to use
     * @return the random ndarray with the specified shape
     */
     INDArray rand(int[] shape,RandomGenerator r);

    /**
     * Create a random ndarray with the given shape using the given rng
     * @param shape the shape of the ndarray
     * @param seed the  seed to use
     * @return the random ndarray with the specified shape
     */
     INDArray rand(int[] shape,long seed);

    /**
     * Create a random ndarray with the given shape using
     * the current time as the seed
     * @param shape the shape of the ndarray
     * @return the random ndarray with the specified shape
     */
     INDArray rand(int[] shape);

    /**
     * Random normal using the given rng
     * @param shape the shape of the ndarray
     * @param r the random generator to use
     * @return
     */
     INDArray randn(int[] shape,RandomGenerator r);

    /**
     * Random normal using the current time stamp
     * as the seed
     * @param shape the shape of the ndarray
     * @return
     */
     INDArray randn(int[] shape);
    /**
     * Random normal using the specified seed
     * @param shape the shape of the ndarray
     * @return
     */
     INDArray randn(int[] shape,long seed);


    /**
     * Creates a row vector with the data
     * @param data the columns of the ndarray
     * @return  the created ndarray
     */
     INDArray create(double[] data);

    /**
     * Creates a row vector with the data
     * @param data the columns of the ndarray
     * @return  the created ndarray
     */
     INDArray create(float[] data);
    /**
     * Creates an ndarray with the specified data
     * @param data the number of columns in the row vector
     * @return ndarray
     */
     IComplexNDArray createComplex(double[] data);


    /**
     * Creates a row vector with the specified number of columns
     * @param columns the columns of the ndarray
     * @return  the created ndarray
     */
     INDArray create(int columns);
    /**
     * Creates an ndarray
     * @param columns the number of columns in the row vector
     * @return ndarray
     */
     IComplexNDArray createComplex(int columns);

    /**
     * Creates a row vector with the specified number of columns
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @return  the created ndarray
     */
     INDArray zeros(int rows,int columns);

    /**
     * Creates a matrix of zeros
     * @param rows te number of rows in the matrix
     * @param columns the number of columns in the row vector
     * @return ndarray
     */
     IComplexNDArray complexZeros(int rows,int columns);


    /**
     * Creates a row vector with the specified number of columns
     * @param columns the columns of the ndarray
     * @return  the created ndarray
     */
     INDArray zeros(int columns);
    /**
     * Creates an ndarray
     * @param columns the number of columns in the row vector
     * @return ndarray
     */
     IComplexNDArray complexZeros(int columns);


    /**
     * Creates an ndarray with the specified value
     * as the  only value in the ndarray
     * @param shape the shape of the ndarray
     * @param value the value to assign
     * @return  the created ndarray
     */
     INDArray valueArrayOf(int[] shape,double value);


    /**
     * Creates a row vector with the specified number of columns
     * @param rows the number of rows in the matrix
     * @param columns the columns of the ndarray
     * @param value the value to assign
     * @return  the created ndarray
     */
     INDArray valueArrayOf(int rows,int columns,double value);



    /**
     * Creates a row vector with the specified number of columns
     * @param rows the number of rows in the matrix
     * @param columns the columns of the ndarray
     * @return  the created ndarray
     */
     INDArray ones(int rows,int columns);

    /**
     * Creates an ndarray
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the row vector
     * @return ndarray
     */
     INDArray complexOnes(int rows,int columns);
    /**
     * Creates a row vector with the specified number of columns
     * @param columns the columns of the ndarray
     * @return  the created ndarray
     */
     INDArray ones(int columns);
    /**
     * Creates an ndarray
     * @param columns the number of columns in the row vector
     * @return ndarray
     */
     IComplexNDArray complexOnes(int columns);


    /**
     * Concatenates two matrices horizontally. Matrices must have identical
     * numbers of rows.
     */
     INDArray concatHorizontally(INDArray A, INDArray B);

    /**
     * Concatenates two matrices vertically. Matrices must have identical
     * numbers of columns.
     */
     INDArray concatVertically(INDArray A, INDArray B);





    /**
     * Create an ndarray of zeros
     * @param shape the shape of the ndarray
     * @return an ndarray with ones filled in
     */
     INDArray zeros(int[] shape);
    /**
     * Create an ndarray of ones
     * @param shape the shape of the ndarray
     * @return an ndarray with ones filled in
     */
     IComplexNDArray complexZeros(int[] shape);

    /**
     * Create an ndarray of ones
     * @param shape the shape of the ndarray
     * @return an ndarray with ones filled in
     */
     INDArray ones(int[] shape);

    /**
     * Create an ndarray of ones
     * @param shape the shape of the ndarray
     * @return an ndarray with ones filled in
     */
     IComplexNDArray complexOnes(int[] shape);

    /**
     * Creates a complex ndarray with the specified shape
     * @param data the data to use with the ndarray
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(float[] data,int rows,int columns,int[] stride,int offset);

    /**
     * Creates an ndarray with the specified shape
     * @param data  the data to use with the ndarray
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
     INDArray create(float[] data,int rows,int columns,int[] stride,int offset);

    /**
     * Creates a complex ndarray with the specified shape
     * @param data the data to use with the ndarray
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset  the offset of the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(float[] data,int[] shape,int[] stride,int offset);


    /**
     * Creates a complex ndarray with the specified shape
     * @param data the data to use with the ndarray
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset  the offset of the ndarray
     * @return the instance
     */
    IComplexNDArray createComplex(IComplexNumber[] data,int[] shape,int[] stride,int offset);

    /**
     * Creates a complex ndarray with the specified shape
     * @param data the data to use with the ndarray
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset  the offset of the ndarray
     * @return the instance
     */
    IComplexNDArray createComplex(IComplexNumber[] data,int[] shape,int[] stride,int offset,char ordering);


    /**
     * Creates a complex ndarray with the specified shape
     * @param data the data to use with the ndarray
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @return the instance
     */
    IComplexNDArray createComplex(IComplexNumber[] data,int[] shape,int[] stride,char ordering);

    /**
     * Creates a complex ndarray with the specified shape
     * @param data the data to use with the ndarray
     * @param shape the shape of the ndarray
     * @param offset the stride for the ndarray
     * @return the instance
     */
    IComplexNDArray createComplex(IComplexNumber[] data,int[] shape,int offset,char ordering);




    /**
     * Creates a complex ndarray with the specified shape
     * @param data the data to use with the ndarray
     * @param shape the shape of the ndarray
     * @return the instance
     */
    IComplexNDArray createComplex(IComplexNumber[] data,int[] shape,char ordering);






    /**
     * Creates an ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
     INDArray create(float[] data,int[] shape,int[] stride,int offset);
    /**
     * Create an ndrray with the specified shape
     * @param data the data to use with tne ndarray
     * @param shape the shape of the ndarray
     * @return the created ndarray
     */
     INDArray create(double[] data,int[] shape);

    /**
     * Create an ndrray with the specified shape
     * @param data the data to use with tne ndarray
     * @param shape the shape of the ndarray
     * @return the created ndarray
     */
     INDArray create(float[] data,int[] shape);
    /**
     * Create an ndrray with the specified shape
     * @param data the data to use with tne ndarray
     * @param shape the shape of the ndarray
     * @return the created ndarray
     */
     IComplexNDArray createComplex(double[] data,int[] shape);

    /**
     * Create an ndrray with the specified shape
     * @param data the data to use with tne ndarray
     * @param shape the shape of the ndarray
     * @return the created ndarray
     */
     IComplexNDArray createComplex(float[] data,int[] shape);

    /**
     * Create an ndrray with the specified shape
     * @param data the data to use with tne ndarray
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @return the created ndarray
     */
     IComplexNDArray createComplex(double[] data,int[] shape,int[] stride);

    /**
     * Create an ndrray with the specified shape
     * @param data the data to use with tne ndarray
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @return the created ndarray
     */
     IComplexNDArray createComplex(float[] data,int[] shape,int[] stride);
    /**
     * Creates a complex ndarray with the specified shape
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(double[] data,int rows,int columns,int[] stride,int offset);

    /**
     * Creates an ndarray with the specified shape
     * @param data the data to use with tne ndarray
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
     INDArray create(double[] data,int rows,int columns,int[] stride,int offset);

    /**
     * Creates a complex ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset  the offset of the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(double[] data,int[] shape,int[] stride,int offset);


    /**
     * Creates an ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
     INDArray create(double[] data,int[] shape,int[] stride,int offset);

    /**
     * Creates an ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @return the instance
     */
     INDArray create(List<INDArray> list,int[] shape);



    /**
     * Creates a complex ndarray with the specified shape
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */;

    /**
     * Creates an ndarray with the specified shape
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
     INDArray create(int rows,int columns,int[] stride,int offset);


    /**
     * Creates a complex ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset  the offset of the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(int[] shape,int[] stride,int offset);
    /**
     * Creates an ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @return the instance
     */
     INDArray create(int[] shape,int[] stride,int offset);






    /**
     * Creates a complex ndarray with the specified shape
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(int rows,int columns,int[] stride);

    /**
     * Creates an ndarray with the specified shape
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @param stride the stride for the ndarray
     * @return the instance
     */
     INDArray create(int rows,int columns,int[] stride);



    /**
     * Creates a complex ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(int[] shape,int[] stride);
    /**
     * Creates an ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @return the instance
     */
     INDArray create(int[] shape,int[] stride);




    /**
     * Creates a complex ndarray with the specified shape
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(int rows,int columns);
    /**
     * Creates an ndarray with the specified shape
     * @param rows the rows of the ndarray
     * @param columns the columns of the ndarray
     * @return the instance
     */
     INDArray create(int rows,int columns);



    /**
     * Creates a complex ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @return the instance
     */
     IComplexNDArray createComplex(int[] shape);
    /**
     * Creates an ndarray with the specified shape
     * @param shape the shape of the ndarray
     * @return the instance
     */
     INDArray create(int[] shape);
    /**
     * Create a scalar ndarray with the specified offset
     * @param value the value to initialize the scalar with
     * @param offset the offset of the ndarray
     * @return the created ndarray
     */
     INDArray scalar(Number value,int offset);
    /**
     * Create a scalar ndarray with the specified offset
     * @param value the value to initialize the scalar with
     * @param offset the offset of the ndarray
     * @return the created ndarray
     */
     IComplexNDArray complexScalar(Number value,int offset);


    /**
     * Create a scalar ndarray with the specified offset
     * @param value the value to initialize the scalar with
     * @return the created ndarray
     */
     IComplexNDArray complexScalar(Number value);




    /**
     * Create a scalar nd array with the specified value and offset
     * @param value the value of the scalar
     * @param offset the offset of the ndarray
     * @return the scalar nd array
     */
     INDArray scalar(float value,int offset);

    /**
     * Create a scalar nd array with the specified value and offset
     * @param value the value of the scalar
     * @param offset the offset of the ndarray
     * @return the scalar nd array
     */
     INDArray scalar(double value,int offset);



    /**
     * Create a scalar ndarray with the specified offset
     * @param value the value to initialize the scalar with
     * @return the created ndarray
     */
     INDArray scalar(Number value);
    /**
     * Create a scalar nd array with the specified value and offset
     * @param value the value of the scalar
    =     * @return the scalar nd array
     */
     INDArray scalar(float value);

    /**
     * Create a scalar nd array with the specified value and offset
     * @param value the value of the scalar
    =     * @return the scalar nd array
     */
     INDArray scalar(double value);

    /**
     * Create a scalar ndarray with the specified offset
     * @param value the value to initialize the scalar with
     * @param offset the offset of the ndarray
     * @return the created ndarray
     */
     IComplexNDArray scalar(IComplexNumber value,int offset);
    /**
     * Create a scalar nd array with the specified value and offset
     * @param value the value of the scalar
     * @return the scalar nd array
     */
     IComplexNDArray scalar(IComplexFloat value);
    /**
     * Create a scalar nd array with the specified value and offset
     * @param value the value of the scalar
    =     * @return the scalar nd array
     */
     IComplexNDArray scalar(IComplexDouble value);

    /**
     * Create a scalar ndarray with the specified offset
     * @param value the value to initialize the scalar with
     * @return the created ndarray
     */
     IComplexNDArray scalar(IComplexNumber value);

    /**
     * Create a scalar nd array with the specified value and offset
     * @param value the value of the scalar
     * @param offset the offset of the ndarray
     * @return the scalar nd array
     */
     IComplexNDArray scalar(IComplexFloat value,int offset);

    /**
     * Create a scalar nd array with the specified value and offset
     * @param value the value of the scalar
     * @param offset the offset of the ndarray
     * @return the scalar nd array
     */
     IComplexNDArray scalar(IComplexDouble value,int offset);


    /**
     * Create a complex ndarray with the given data
     * @param data the data to use with tne ndarray
     * @param shape the shape of the ndarray
     * @param stride the stride for the ndarray
     * @param offset the offset of the ndarray
     * @param ordering the ordering for the ndarray
     * @return the created complex ndarray
     */
    IComplexNDArray createComplex(double[] data, int[] shape, int[] stride, int offset, char ordering);


    /**
     *
     * @param data
     * @param shape
     * @param offset
     * @param ordering
     * @return
     */
    IComplexNDArray createComplex(double[] data, int[] shape, int offset, char ordering);


    /**
     *
     * @param data
     * @param shape
     * @param offset
     * @return
     */
    IComplexNDArray createComplex(double[] data, int[] shape, int offset);


    INDArray create(float[] data, int[] shape, int offset);

    IComplexNDArray createComplex(float[] data, int[] shape, int offset, char ordering);

    IComplexNDArray createComplex(float[] data, int[] shape, int offset);

    IComplexNDArray createComplex(float[] data, int[] shape, int[] stride, int offset, char ordering);

    INDArray create(float[][] floats);

    IComplexNDArray createComplex(float[] dim);
}