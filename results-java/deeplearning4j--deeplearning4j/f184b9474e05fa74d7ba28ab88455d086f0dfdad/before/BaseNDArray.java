package org.deeplearning4j.linalg.api.ndarray;


import org.deeplearning4j.linalg.factory.NDArrayFactory;
import org.deeplearning4j.linalg.factory.NDArrays;
import org.deeplearning4j.linalg.indexing.NDArrayIndex;

import org.deeplearning4j.linalg.ops.TwoArrayOps;
import org.deeplearning4j.linalg.ops.elementwise.AddOp;
import org.deeplearning4j.linalg.ops.elementwise.DivideOp;
import org.deeplearning4j.linalg.ops.elementwise.MultiplyOp;
import org.deeplearning4j.linalg.ops.elementwise.SubtractOp;
import org.deeplearning4j.linalg.ops.reduceops.Ops;
import org.deeplearning4j.linalg.ops.transforms.Transforms;
import org.deeplearning4j.linalg.util.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.deeplearning4j.linalg.util.ArrayUtil.*;


/**
 * NDArray: (think numpy)
 *
 * A few things of note.
 *
 * An NDArray can have any number of dimensions.
 *
 * An NDArray is accessed via strides.
 *
 * Strides are how to index over
 * a contiguous block of data.
 *
 * This block of data has 2 orders(as of right now):
 * fortran and c
 *
 *
 *
 * @author Adam Gibson
 */
public abstract class BaseNDArray  implements INDArray {



    protected int[] shape;
    protected int[] stride;
    protected int offset = 0;
    protected char ordering;
    protected double[] data;
    protected int rows,columns;
    protected int length;



    public BaseNDArray() {}



    public BaseNDArray(double[][] data) {
        this(data.length, data[0].length);

        for (int r = 0; r < rows; r++) {
            assert (data[r].length == columns);
        }

        this.data = new double[length];


        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                putScalar(new int[]{r, c}, data[r][c]);
            }
        }
    }


    /**
     * Create this ndarray with the given data and shape and 0 offset
     * @param data the data to use
     * @param shape the shape of the ndarray
     */
    public BaseNDArray(double[] data, int[] shape, char ordering) {
        this(data,shape,0,ordering);
    }

    /**
     *
     * @param data the data to use
     * @param shape the shape of the ndarray
     * @param offset the desired offset
     * @param ordering the ordering of the ndarray
     */
    public BaseNDArray(double[] data, int[] shape, int offset, char ordering) {
        this(data,shape,ordering == NDArrayFactory.C ? calcStrides(shape) : calcStridesFortran(shape),offset);

    }


    /**
     * Construct an ndarray of the specified shape
     * with an empty data array
     * @param shape the shape of the ndarray
     * @param stride the stride of the ndarray
     * @param offset the desired offset
     * @param ordering the ordering of the ndarray
     */
    public BaseNDArray(int[] shape, int[] stride, int offset, char ordering) {
        this(new double[ArrayUtil.prod(shape)],shape,stride,offset,ordering);
    }


    /**
     * Create the ndarray with
     * the specified shape and stride and an offset of 0
     * @param shape the shape of the ndarray
     * @param stride the stride of the ndarray
     * @param ordering the ordering of the ndarray
     */
    public BaseNDArray(int[] shape, int[] stride, char ordering){
        this(shape,stride,0,ordering);
    }

    public BaseNDArray(int[] shape, int offset, char ordering) {
        this(shape,ordering == NDArrayFactory.C ? calcStrides(shape) : calcStridesFortran(shape),offset,ordering);
    }


    public BaseNDArray(int[] shape) {
        this(shape,0,NDArrays.order());
    }




    /**
     * Creates a new <i>n</i> times <i>m</i> <tt>DoubleMatrix</tt>.
     *
     * @param newRows    the number of rows (<i>n</i>) of the new matrix.
     * @param newColumns the number of columns (<i>m</i>) of the new matrix.
     */
    public BaseNDArray(int newRows, int newColumns, char ordering) {
        this.ordering = ordering;
        initShape(new int[]{newRows,newColumns});
    }


    /**
     * Create an ndarray from the specified slices.
     * This will go through and merge all of the
     * data from each slice in to one ndarray
     * which will then take the specified shape
     * @param slices the slices to merge
     * @param shape the shape of the ndarray
     */
    public BaseNDArray(List<INDArray> slices, int[] shape, char ordering) {
        List<double[]> list = new ArrayList<>();
        for(int i = 0; i < slices.size(); i++)
            list.add(slices.get(i).data());
        this.ordering = ordering;
        this.data = ArrayUtil.combine(list);

        initShape(shape);



    }


    /**
     * Create an ndarray from the specified slices.
     * This will go through and merge all of the
     * data from each slice in to one ndarray
     * which will then take the specified shape
     * @param slices the slices to merge
     * @param shape the shape of the ndarray
     */
    public BaseNDArray(List<INDArray> slices, int[] shape, int[] stride, char ordering) {
        List<double[]> list = new ArrayList<>();
        for(int i = 0; i < slices.size(); i++)
            list.add(slices.get(i).data());
        this.ordering = ordering;
        this.data = ArrayUtil.combine(list);
        this.stride = stride;
        initShape(shape);



    }


    public BaseNDArray(double[] data, int[] shape, int[] stride, char ordering) {
        this(data,shape,stride,0,ordering);
    }


    public BaseNDArray(double[] data, int[] shape, int[] stride, int offset, char ordering) {


        this.offset = offset;
        this.stride = stride;
        this.ordering = ordering;
        initShape(shape);

        if(data != null  && data.length > 0) {
            this.data = data;
            if(offset >= data.length)
                throw new IllegalArgumentException("Invalid offset: must be < data.length");


        }



    }

    public BaseNDArray(float[] data, int[] shape, int[] stride, int offset, char ordering) {
        this(ArrayUtil.doubleCopyOf(data),shape,stride,offset);
    }





    /**
     * Create this ndarray with the given data and shape and 0 offset
     * @param data the data to use
     * @param shape the shape of the ndarray
     */
    public BaseNDArray(double[] data, int[] shape) {
        this(data,shape,0);
    }

    public BaseNDArray(double[] data, int[] shape, int offset) {
        this(data,shape,offset,NDArrays.order());

    }


    /**
     * Construct an ndarray of the specified shape
     * with an empty data array
     * @param shape the shape of the ndarray
     * @param stride the stride of the ndarray
     * @param offset the desired offset
     */
    public BaseNDArray(int[] shape, int[] stride, int offset) {
        this(new double[ArrayUtil.prod(shape)],shape,stride,offset,NDArrays.order());
    }


    /**
     * Create the ndarray with
     * the specified shape and stride and an offset of 0
     * @param shape the shape of the ndarray
     * @param stride the stride of the ndarray
     */
    public BaseNDArray(int[] shape, int[] stride){
        this(shape,stride,0);
    }

    public BaseNDArray(int[] shape, int offset) {
        this(shape,calcStrides(shape),offset);
    }


    public BaseNDArray(int[] shape, char ordering) {
        this(shape,0,ordering);
    }


    /**
     * Creates a new <i>n</i> times <i>m</i> <tt>DoubleMatrix</tt>.
     *
     * @param newRows    the number of rows (<i>n</i>) of the new matrix.
     * @param newColumns the number of columns (<i>m</i>) of the new matrix.
     */
    public BaseNDArray(int newRows, int newColumns) {
        this(newRows,newColumns,NDArrays.order());
    }


    /**
     * Create an ndarray from the specified slices.
     * This will go through and merge all of the
     * data from each slice in to one ndarray
     * which will then take the specified shape
     * @param slices the slices to merge
     * @param shape the shape of the ndarray
     */
    public BaseNDArray(List<INDArray> slices, int[] shape) {
        this(slices,shape,NDArrays.order());
    }


    /**
     * Create an ndarray from the specified slices.
     * This will go through and merge all of the
     * data from each slice in to one ndarray
     * which will then take the specified shape
     * @param slices the slices to merge
     * @param shape the shape of the ndarray
     */
    public BaseNDArray(List<INDArray> slices, int[] shape, int[] stride) {
        this(slices,shape,stride,NDArrays.order());

    }


    public BaseNDArray(double[] data, int[] shape, int[] stride) {
        this(data,shape,stride,NDArrays.order());
    }


    public BaseNDArray(double[] data, int[] shape, int[] stride, int offset) {
        this(data,shape,stride,offset,NDArrays.order());

    }

    public BaseNDArray(float[] data, int[] shape, int[] stride, int offset) {
        this(data,shape,stride,offset,NDArrays.order());
    }

    public BaseNDArray(double[] data) {
        this.data = data;
    }


    /**
     * Returns a linear float array representation of this ndarray
     *
     * @return the linear float array representation of this ndarray
     */
    @Override
    public float[] floatData() {
        return ArrayUtil.floatCopyOf(data());
    }

    @Override
    public void setData(float[] data) {
        this.data = ArrayUtil.doubleCopyOf(data);
    }




    /**
     * Returns the number of possible vectors for a given dimension
     *
     * @param dimension the dimension to calculate the number of vectors for
     * @return the number of possible vectors along a dimension
     */
    @Override
    public int vectorsAlongDimension(int dimension) {
        return length / size(dimension);
    }

    /**
     * Get the vector along a particular dimension
     *
     * @param index     the index of the vector to get
     * @param dimension the dimension to get the vector from
     * @return the vector along a particular dimension
     */
    @Override
    public INDArray vectorAlongDimension(int index, int dimension) {
        assert dimension <= shape.length : "Invalid dimension " + dimension;
        if(ordering == NDArrayFactory.C) {

            if(dimension == shape.length - 1 && dimension != 0)
                return NDArrays.create(data,
                        new int[]{1,shape[dimension]}
                        ,ArrayUtil.removeIndex(stride,0),
                        offset + index * stride[dimension - 1]);

            else if(dimension == 0)
                return NDArrays.create(data,
                        new int[]{shape[dimension],1}
                        ,new int[]{stride[dimension],1},
                        offset + index);



            return  NDArrays.create(data,
                    new int[]{shape[dimension],1}
                    ,new int[]{stride[dimension],1},
                    offset + index * stride[0]);
        }

        else if(ordering == NDArrayFactory.FORTRAN) {

            if(dimension == shape.length - 1 && dimension != 0)
                return NDArrays.create(data,
                        new int[]{1,shape[dimension]}
                        ,ArrayUtil.removeIndex(stride,0),
                        offset + index * stride[dimension - 1]);


            return  NDArrays.create(data,
                    new int[]{shape[dimension],1}
                    ,new int[]{stride[dimension],1},
                    offset + index * stride[stride.length - 1]);
        }

        throw new IllegalStateException("Illegal ordering..none declared");


    }

    /**
     * Cumulative sum along a dimension
     *
     * @param dimension the dimension to perform cumulative sum along
     * @return the cumulative sum along the specified dimension
     */
    @Override
    public INDArray cumsumi(int dimension) {
        if(isVector()) {
            double s = 0.0;
            for (int i = 0; i < length; i++) {
                s += (double) getScalar(i).element();
                putScalar(i, s);
            }
        }

        else if(dimension == Integer.MAX_VALUE || dimension == shape.length - 1) {
            INDArray flattened = ravel().dup();
            double prevVal = (double) flattened.getScalar(0).element();
            for(int i = 1; i < flattened.length(); i++) {
                double d = prevVal + (double) flattened.getScalar(i).element();
                flattened.putScalar(i,d);
                prevVal = d;
            }

            return flattened;
        }



        else {
            for(int i = 0; i < vectorsAlongDimension(dimension); i++) {
                INDArray vec = vectorAlongDimension(i,dimension);
                vec.cumsumi(0);

            }
        }


        return this;
    }

    /**
     * Cumulative sum along a dimension (in place)
     *
     * @param dimension the dimension to perform cumulative sum along
     * @return the cumulative sum along the specified dimension
     */
    @Override
    public INDArray cumsum(int dimension) {
        return dup().cumsumi(dimension);
    }

    /**
     * Assign all of the elements in the given
     * ndarray to this ndarray
     *
     * @param arr the elements to assign
     * @return this
     */
    @Override
    public INDArray assign(INDArray arr) {
        LinAlgExceptions.assertSameShape(this,arr);
        INDArray other = arr.ravel();
        INDArray thisArr = ravel();
        for(int i = 0; i < other.length(); i++)
            thisArr.put(i, other.getScalar(i));
        return this;
    }

    @Override
    public INDArray putScalar(int i, Number value) {
        int idx = linearIndex(i);
        data[idx] = value.doubleValue();
        return this;
    }

    @Override
    public INDArray putScalar(int[] indexes, Number value) {
        int ix = offset;
        for (int i = 0; i < shape.length; i++) {
            ix += indexes[i] * stride[i];
        }

        data[ix] = value.doubleValue();

        return this;
    }

    @Override
    public INDArray lt(Number other) {
        return dup().lti(other);
    }

    @Override
    public INDArray lti(Number other) {
        return lti(NDArrays.scalar(other));
    }

    @Override
    public INDArray eq(Number other) {
        return dup().eqi(other);
    }

    @Override
    public INDArray eqi(Number other) {
        return eqi(NDArrays.scalar(other));
    }

    @Override
    public INDArray gt(Number other) {
        return dup().gti(other);
    }

    @Override
    public INDArray gti(Number other) {
        return gti(NDArrays.scalar(other));
    }

    @Override
    public INDArray lt(INDArray other) {
        return dup().lti(other);
    }

    @Override
    public INDArray lti(INDArray other) {
        return Transforms.lt(other);
    }

    @Override
    public INDArray eq(INDArray other) {
        return dup().eqi(other);
    }

    @Override
    public INDArray eqi(INDArray other) {
        return Transforms.eq(other);
    }

    @Override
    public INDArray gt(INDArray other) {
        return dup().gti(other);
    }

    @Override
    public INDArray gti(INDArray other) {
        return Transforms.gt(other);
    }

    /**
     * Negate each element.
     */
    @Override
    public INDArray neg() {
        return dup().negi();
    }

    /**
     * Negate each element (in-place).
     */
    @Override
    public INDArray negi() {
        return  Transforms.neg(this);
    }






    /**
     * Returns the element at the specified row/column
     * This will throw an exception if the
     *
     * @param row    the row of the element to return
     * @param column the row of the element to return
     * @return a scalar indarray of the element at this index
     */
    @Override
    public INDArray getScalar(int row, int column) {
        return getScalar(new int[]{row,column});
    }


    @Override
    public   INDArray dup() {
        return NDArrays.create(Arrays.copyOf(data,data.length),shape,stride,offset,ordering);
    }





    /**
     * Test whether a matrix is scalar.
     */
    @Override
    public boolean isScalar() {
        if(shape.length == 0)
            return true;
        else if(shape.length == 1 && shape[0] == 1)
            return true;
        else if(shape.length >= 2) {
            for(int i = 0; i < shape.length; i++)
                if(shape[i] != 1)
                    return false;
        }

        return length == 1;
    }




    /**
     * Inserts the element at the specified index
     *
     * @param indices the indices to insert into
     * @param element a scalar ndarray
     * @return a scalar ndarray of the element at this index
     */
    @Override
    public INDArray put(int[] indices, INDArray element) {
        if(!element.isScalar())
            throw new IllegalArgumentException("Unable to insert anything but a scalar");
        int ix = offset;
        if (indices.length != shape.length)
            throw new IllegalArgumentException("Unable to applyTransformToDestination values: number of indices must be equal to the shape");

        for (int i = 0; i< shape.length; i++)
            ix += indices[i] * stride[i];


        data[ix] = (double) element.element();
        return this;

    }

    /**
     * Inserts the element at the specified index
     *
     * @param i       the row insert into
     * @param j       the column to insert into
     * @param element a scalar ndarray
     * @return a scalar ndarray of the element at this index
     */
    @Override
    public INDArray put(int i, int j, INDArray element) {
        return put(new int[]{i,j},element);
    }

    /**
     * Inserts the element at the specified index
     *
     * @param i       the row insert into
     * @param j       the column to insert into
     * @param element a scalar ndarray
     * @return a scalar ndarray of the element at this index
     */
    @Override
    public INDArray put(int i, int j, Number element) {
        return put(i,j,NDArrays.scalar(element));
    }


    /**
     * Assigns the given matrix (put) to the specified slice
     * @param slice the slice to assign
     * @param put the slice to applyTransformToDestination
     * @return this for chainability
     */
    @Override
    public INDArray putSlice(int slice,INDArray put) {
        if(isScalar()) {
            assert put.isScalar() : "Invalid dimension. Can only insert a scalar in to another scalar";
            put(0,put.getScalar(0));
            return this;
        }

        else if(isVector()) {
            assert put.isScalar() : "Invalid dimension on insertion. Can only insert scalars input vectors";
            put(slice,put.getScalar(0));
            return this;
        }


        assertSlice(put,slice);


        INDArray view = slice(slice);

        if(put.isScalar())
            put(slice,put.getScalar(0));
        else if(put.isVector())
            for(int i = 0; i < put.length(); i++)
                view.put(i,put.getScalar(i));
        else if(put.shape().length == 2)
            for(int i = 0; i < put.rows(); i++)
                for(int j = 0; j < put.columns(); j++)
                    view.put(i,j,(double) put.getScalar(i,j).element());

        else {

            assert put.slices() == view.slices() : "Slices must be equivalent.";
            for(int i = 0; i < put.slices(); i++)
                view.slice(i).putSlice(i,view.slice(i));

        }

        return this;

    }


    protected void assertSlice(INDArray put,int slice) {
        assert slice <= slices() : "Invalid slice specified " + slice;
        int[] sliceShape = put.shape();
        int[] requiredShape = ArrayUtil.removeIndex(shape(),0);

        //no need to compare for scalar; primarily due to shapes either being [1] or length 0
        if(put.isScalar())
            return;



        assert Shape.shapeEquals(sliceShape,requiredShape) : String.format("Invalid shape size of %s . Should have been %s ",Arrays.toString(sliceShape),Arrays.toString(requiredShape));

    }


    /**
     * Returns true if this ndarray is 2d
     * or 3d with a singleton element
     * @return true if the element is a matrix, false otherwise
     */
    public boolean isMatrix() {
        return (shape().length == 2
                && (shape[0] != 1 && shape[1] != 1)) ||
                shape.length == 3 &&
                        (shape[0] == 1 || shape[1] == 1 || shape[2] == 1);
    }

    /**
     *
     * http://docs.scipy.org/doc/numpy/reference/generated/numpy.ufunc.reduce.html
     * @param op the operation to do
     * @param dimension the dimension to return from
     * @return the results of the reduce (applying the operation along the specified
     * dimension)
     */
    @Override
    public INDArray reduce(Ops.DimensionOp op,int dimension) {
        if (isScalar())
            return this;


        if (isVector())
            return NDArrays.scalar(reduceVector(op, this));


        int[] shape = ArrayUtil.removeIndex(this.shape, dimension);

        if (dimension == 0) {
            double[] data2 = new double[ArrayUtil.prod(shape)];
            int dataIter = 0;

            //iterating along the dimension is relative to the number of slices
            //in the return dimension
            int numTimes = ArrayUtil.prod(shape);
            for (int offset = this.offset; offset < numTimes; offset++) {
                double reduce = op(dimension, offset, op);
                data2[dataIter++] = reduce;

            }

            return NDArrays.create(data2, shape);
        } else {
            double[] data2 = new double[ArrayUtil.prod(shape)];
            int dataIter = 0;
            //want the milestone to slice[1] and beyond
            int[] sliceIndices = endsForSlices();
            int currOffset = 0;

            //iterating along the dimension is relative to the number of slices
            //in the return dimension
            int numTimes = ArrayUtil.prod(shape);
            for (int offset = this.offset; offset < numTimes; offset++) {
                if (dataIter >= data2.length || currOffset >= sliceIndices.length)
                    break;

                //do the operation,, and look for whether it exceeded the current slice
                IterationResult pair = op(dimension, offset, op, sliceIndices[currOffset]);
                //append the result
                double reduce = pair.getResult();
                data2[dataIter++] = reduce;

                //go to next slice and iterate over that
                if (pair.isNextSlice()) {
                    //will update to next step
                    offset = sliceIndices[currOffset];
                    numTimes += sliceIndices[currOffset];
                    currOffset++;
                }

            }

            return NDArrays.create(data2, shape);

        }

    }




    //getFromOrigin one result along one dimension based on the given offset
    public DimensionSlice vectorForDimensionAndOffset(int dimension, int offset) {
        if(isScalar() && dimension == 0 && offset == 0)
            return new DimensionSlice(false,getScalar(offset),new int[]{offset});


            //need whole vector
        else   if (isVector()) {
            if(dimension == 0) {
                int[] indices = new int[length];
                for(int i = 0; i < indices.length; i++)
                    indices[i] = i;
                return new DimensionSlice(false,dup(),indices);
            }
            else if(dimension == 1)
                return new DimensionSlice(false,getScalar(offset),new int[]{offset});
            else
                throw new IllegalArgumentException("Illegal dimension for vector " + dimension);

        }

        else {

            int count = 0;
            List<Integer> indices = new ArrayList<>();
            INDArray ret = NDArrays.create(new int[]{shape[dimension]});

            for(int j = offset; count < this.shape[dimension]; j+= this.stride[dimension]) {
                double d = data[j];
                ret.putScalar(count++, d);
                indices.add(j);


            }

            return new DimensionSlice(false,ret,ArrayUtil.toArray(indices));

        }

    }


    //getFromOrigin one result along one dimension based on the given offset
    private IterationResult op(int dimension, int offset, Ops.DimensionOp op,int currOffsetForSlice) {
        double[] dim = new double[this.shape[dimension]];
        int count = 0;
        boolean newSlice = false;
        for(int j = offset; count < dim.length; j+= this.stride[dimension]) {
            double d = data[j];
            dim[count++] = d;
            if(j >= currOffsetForSlice)
                newSlice = true;
        }

        return new IterationResult(reduceVector(op,NDArrays.create(dim)),newSlice);
    }


    //getFromOrigin one result along one dimension based on the given offset
    private double op(int dimension, int offset, Ops.DimensionOp op) {
        double[] dim = new double[this.shape[dimension]];
        int count = 0;
        for(int j = offset; count < dim.length; j+= this.stride[dimension]) {
            double d = data[j];
            dim[count++] = d;
        }

        return reduceVector(op,NDArrays.create(dim));
    }

    @Override
    public int index(int row, int column) {
        if(!isMatrix()) {
            if(isColumnVector()) {
                int idx = linearIndex(row);
                return idx;
            }
            else if (isRowVector()) {
                int idx = linearIndex(column);
                return idx;
            }
            else
                throw new IllegalStateException("Unable to getFromOrigin row/column from a non matrix");
        }



        return offset + (row *  stride[0]  + column * stride[1]);
    }


    protected double reduceVector(Ops.DimensionOp op,INDArray vector) {

        switch(op) {
            case SUM:
                return (double) vector.sum(Integer.MAX_VALUE).element();
            case MEAN:
                return (double) vector.mean(Integer.MAX_VALUE).element();
            case MIN:
                return (double) vector.min(Integer.MAX_VALUE).element();
            case MAX:
                return (double) vector.max(Integer.MAX_VALUE).element();
            case NORM_1:
                return (double) vector.norm1(Integer.MAX_VALUE).element();
            case NORM_2:
                return (double) vector.norm2(Integer.MAX_VALUE).element();
            case NORM_MAX:
                return (double) vector.normmax(Integer.MAX_VALUE).element();
            default: throw new IllegalArgumentException("Illegal operation");
        }
    }



    /**
     * Returns the squared (Euclidean) distance.
     */
    public double squaredDistance(INDArray other) {
        double sd = 0.0;
        for (int i = 0; i < length; i++) {
            double d = (double) getScalar(i).element() - (double) other.getScalar(i).element();
            sd += d * d;
        }
        return sd;
    }

    /**
     * Returns the (euclidean) distance.
     */
    public double distance2(INDArray other) {
        return  Math.sqrt(squaredDistance(other));
    }

    /**
     * Returns the (1-norm) distance.
     */
    public double distance1(INDArray other) {
        double d = 0.0;
        for (int i = 0; i < length; i++) {
            d += Math.abs((double) getScalar(i).sub(other.getScalar(i)).element());
        }
        return d;
    }

    @Override
    public INDArray put(NDArrayIndex[] indices, INDArray element) {
        return null;
    }

    @Override
    public INDArray put(NDArrayIndex[] indices, Number element) {
        return null;
    }


    /**
     * Iterate over every column of every slice
     * @param op the operation to apply
     */
    @Override
    public void iterateOverAllColumns(SliceOp op) {
        if(isVector())
            op.operate(new DimensionSlice(false,this,null));
        else if(isMatrix()) {
            for(int i = 0; i < columns(); i++) {
                op.operate(new DimensionSlice(false,getColumn(i),null));
            }
        }

        else {
            for(int i = 0; i < slices(); i++) {
                slice(i).iterateOverAllRows(op);
            }
        }
    }


    /**
     * Iterate over every row of every slice
     * @param op the operation to apply
     */
    @Override
    public void iterateOverAllRows(SliceOp op) {
        if(isVector())
            op.operate(new DimensionSlice(false,this,null));
        else if(isMatrix()) {
            for(int i = 0; i < rows(); i++) {
                op.operate(new DimensionSlice(false,getRow(i),null));
            }
        }

        else {
            for(int i = 0; i < slices(); i++) {
                slice(i).iterateOverAllRows(op);
            }
        }
    }


    /**
     * Mainly here for people coming from numpy.
     * This is equivalent to a call to permute
     * @param dimension the dimension to swap
     * @param with the one to swap it with
     * @return the swapped axes view
     */
    @Override
    public INDArray swapAxes(int dimension,int with) {
        int[] shape = ArrayUtil.range(0,shape().length);
        shape[dimension] = with;
        shape[with] = dimension;
        return permute(shape);
    }



    /**
     * Gives the indices for the ending of each slice
     * @return the off sets for the beginning of each slice
     */
    @Override
    public int[] endsForSlices() {
        int[] ret = new int[slices()];
        int currOffset = offset + stride[0] - 1;
        for(int i = 0; i < slices(); i++) {
            ret[i] = currOffset;
            currOffset += stride[0];
        }
        return ret;
    }

    /**
     * Gives the indices for the beginning of each slice
     * @return the off sets for the beginning of each slice
     */
    public int[] offsetsForSlices() {
        int[] ret = new int[slices()];
        int currOffset = offset;
        for(int i = 0; i < slices(); i++) {
            ret[i] = currOffset;
            currOffset += stride[0] + 1;
        }
        return ret;
    }


    @Override
    public double[] data() {
        return data;
    }

    @Override
    public void setData(double[] data) {
        this.data = data;
    }



    /**
     * Number of slices: aka shape[0]
     * @return the number of slices
     * for this nd array
     */
    @Override
    public int slices() {
        if(shape.length < 1)
            return 0;
        return shape[0];
    }



    @Override
    public INDArray subArray(int[] offsets, int[] shape,int[] stride) {
        int n = shape.length;
        if (offsets.length != n)
            throw new IllegalArgumentException("Invalid offset " + Arrays.toString(offsets));
        if (shape.length != n)
            throw new IllegalArgumentException("Invalid shape " + Arrays.toString(shape));

        if (Arrays.equals(shape, this.shape)) {
            if (ArrayUtil.isZero(offsets)) {
                return this;
            } else {
                throw new IllegalArgumentException("Invalid subArray offsets");
            }
        }

        return NDArrays.create(
                data
                , Arrays.copyOf(shape,shape.length)
                , stride
                ,offset + ArrayUtil.dotProduct(offsets, stride),ordering
        );
    }



    /**
     * Iterate along a dimension.
     * This encapsulates the process of sum, mean, and other processes
     * take when iterating over a dimension.
     * @param dimension the dimension to iterate over
     * @param op the operation to apply
     * @param modify whether to modify this array while iterating
     */
    @Override
    public void iterateOverDimension(int dimension,SliceOp op,boolean modify) {
        if(dimension >= shape.length)
            throw new IllegalArgumentException("Unable to remove dimension  " + dimension + " was >= shape length");

        if(isScalar()) {
            if(dimension > 0)
                throw new IllegalArgumentException("Dimension must be 0 for a scalar");
            else {
                DimensionSlice slice = this.vectorForDimensionAndOffset(0,0);
                op.operate(slice);
                if(modify && slice.getIndices() != null) {
                    INDArray result = (INDArray) slice.getResult();
                    for(int i = 0; i < slice.getIndices().length; i++) {
                        data[slice.getIndices()[i]] = (double) result.getScalar(i).element();
                    }
                }
            }
        }

        else if(isVector()) {
            if(dimension == 0) {
                DimensionSlice slice = this.vectorForDimensionAndOffset(0,0);
                op.operate(slice);
                if(modify && slice.getIndices() != null) {
                    INDArray result = (INDArray) slice.getResult();
                    for(int i = 0; i < slice.getIndices().length; i++) {
                        data[slice.getIndices()[i]] = (double) result.getScalar(i).element();
                    }
                }
            }
            else if(dimension == 1) {
                for(int i = 0; i < length; i++) {
                    DimensionSlice slice = vectorForDimensionAndOffset(dimension,i);
                    op.operate(slice);
                    if(modify && slice.getIndices() != null) {
                        INDArray result = (INDArray) slice.getResult();
                        for(int j = 0; j < slice.getIndices().length; j++) {
                            data[slice.getIndices()[j]] = (double) result.getScalar(j).element();
                        }
                    }

                }
            }
            else
                throw new IllegalArgumentException("Illegal dimension for vector " + dimension);
        }


        else {
            for(int i = 0; i < vectorsAlongDimension(dimension); i++) {
                INDArray vector = vectorAlongDimension(i,dimension);
                op.operate(vector);
            }

        }



    }




    @Override
    public void setStride(int[] stride) {
        this.stride = stride;
    }








    protected void initShape(int[] shape) {
        this.shape = shape;

        if(this.shape.length == 1) {
            rows = 1;
            columns = this.shape[0];
        }
        else if(this.shape().length == 2) {
            if(shape[0] == 1) {
                this.shape = new int[1];
                this.shape[0] = shape[1];
                rows = 1;
                columns = shape[1];

            }
            else {
                rows = shape[0];
                columns = shape[1];
            }


        }

        //default row vector
        else if(this.shape.length == 1) {
            columns = this.shape[0];
            rows = 1;
        }

        //null character
        if(this.ordering == '\u0000')
            this.ordering = NDArrays.order();

        this.length = ArrayUtil.prod(this.shape);
        if(this.stride == null) {
            if(ordering == NDArrayFactory.FORTRAN)
                this.stride = ArrayUtil.calcStridesFortran(shape);
            else
                this.stride = ArrayUtil.calcStrides(this.shape);
        }

        //recalculate stride: this should only happen with row vectors
        if(this.stride.length != this.shape.length) {
            if(ordering == NDArrayFactory.FORTRAN)
                this.stride = ArrayUtil.calcStridesFortran(this.shape);
            else
                this.stride = ArrayUtil.calcStrides(this.shape);
        }


    }



    @Override
    public INDArray getScalar(int i) {
        if(!isVector() && !isScalar())
            throw new IllegalArgumentException("Unable to do linear indexing with dimensions greater than 1");
        int idx = linearIndex(i);
        return NDArrays.scalar(data[idx]);
    }



    /**
     * Inserts the element at the specified index
     *
     * @param i       the index insert into
     * @param element a scalar ndarray
     * @return a scalar ndarray of the element at this index
     */
    @Override
    public INDArray put(int i, INDArray element) {
        if(element == null)
            throw new IllegalArgumentException("Unable to insert null element");
        assert element.isScalar() : "Unable to insert non scalar element";
        int idx = linearIndex(i);
        data[idx] = (double) element.element();
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param columnVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray diviColumnVector(INDArray columnVector) {
        assert columnVector.isColumnVector() : "Must only add a column vector";
        assert columnVector.length() == rows() : "Illegal column vector must have the same length as the number of column in this ndarray";

        for(int i = 0; i < columns(); i++) {
            getColumn(i).divi(columnVector);
        }
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param columnVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray divColumnVector(INDArray columnVector) {
        return dup().diviColumnVector(columnVector);
    }

    /**
     * In place addition of a column vector
     *
     * @param rowVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray diviRowVector(INDArray rowVector) {
        assert rowVector.isRowVector() : "Must only add a row vector";
        assert rowVector.length() == columns() : "Illegal row vector must have the same length as the number of rows in this ndarray";
        for(int j = 0; j< rows(); j++) {
            getRow(j).divi(rowVector);
        }
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param rowVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray divRowVector(INDArray rowVector) {
        return dup().diviRowVector(rowVector);
    }

    /**
     * In place addition of a column vector
     *
     * @param columnVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray muliColumnVector(INDArray columnVector) {
        assert columnVector.isColumnVector() : "Must only add a column vector";
        assert columnVector.length() == rows() : "Illegal column vector must have the same length as the number of column in this ndarray";

        for(int i = 0; i < columns(); i++) {
            getColumn(i).muli(columnVector);
        }
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param columnVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray mulColumnVector(INDArray columnVector) {
        return dup().muliColumnVector(columnVector);
    }

    /**
     * In place addition of a column vector
     *
     * @param rowVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray muliRowVector(INDArray rowVector) {
        assert rowVector.isRowVector() : "Must only add a row vector";
        assert rowVector.length() == columns() : "Illegal row vector must have the same length as the number of rows in this ndarray";
        for(int j = 0; j< rows(); j++) {
            getRow(j).muli(rowVector);
        }
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param rowVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray mulRowVector(INDArray rowVector) {
        return dup().muliRowVector(rowVector);
    }

    /**
     * In place addition of a column vector
     *
     * @param columnVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray subiColumnVector(INDArray columnVector) {
        assert columnVector.isColumnVector() : "Must only add a column vector";
        assert columnVector.length() == rows() : "Illegal column vector must have the same length as the number of column in this ndarray";

        for(int i = 0; i < columns(); i++) {
            getColumn(i).subi(columnVector);
        }
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param columnVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray subColumnVector(INDArray columnVector) {
        return dup().subiColumnVector(columnVector);
    }

    /**
     * In place addition of a column vector
     *
     * @param rowVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray subiRowVector(INDArray rowVector) {
        assert rowVector.isRowVector() : "Must only add a row vector";
        assert rowVector.length() == columns() : "Illegal row vector must have the same length as the number of rows in this ndarray";
        for(int j = 0; j< rows(); j++) {
            getRow(j).subi(rowVector);
        }
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param rowVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray subRowVector(INDArray rowVector) {
        return dup().subiRowVector(rowVector);
    }

    /**
     * In place addition of a column vector
     *
     * @param columnVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray addiColumnVector(INDArray columnVector) {
        assert columnVector.isColumnVector() : "Must only add a column vector";
        assert columnVector.length() == rows() : "Illegal column vector must have the same length as the number of column in this ndarray";

        for(int i = 0; i < columns(); i++) {
            INDArray column = getColumn(i);
            double d = column.get(0);
            double d2 = column.get(1);
            column.addi(columnVector);
        }
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param columnVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray addColumnVector(INDArray columnVector) {
        return dup().addiColumnVector(columnVector);
    }

    /**
     * In place addition of a column vector
     *
     * @param rowVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray addiRowVector(INDArray rowVector) {
        assert rowVector.isRowVector() : "Must only add a row vector";
        assert rowVector.length() == columns() : "Illegal row vector must have the same length as the number of rows in this ndarray";
        for(int j = 0; j < rows(); j++) {
            INDArray row = getRow(j);
            row.addi(rowVector);
        }
        return this;
    }

    /**
     * In place addition of a column vector
     *
     * @param rowVector the column vector to add
     * @return the result of the addition
     */
    @Override
    public INDArray addRowVector(INDArray rowVector) {
        return dup().addiRowVector(rowVector);
    }

    /**
     * Perform a copy matrix multiplication
     *
     * @param other the other matrix to perform matrix multiply with
     * @return the result of the matrix multiplication
     */
    @Override
    public INDArray mmul(INDArray other) {
        int[] shape = {rows(),other.columns()};
        char order = NDArrays.factory().order();
        boolean switchedOrder = false;
        if(order != NDArrayFactory.FORTRAN) {
            NDArrays.factory().setOrder(NDArrayFactory.FORTRAN);
            switchedOrder = true;
        }

        INDArray result = NDArrays.create(shape);

        if(switchedOrder)
            NDArrays.factory().setOrder(NDArrayFactory.C);

        return mmuli(other,result);
    }

    /**
     * Perform an copy matrix multiplication
     *
     * @param other  the other matrix to perform matrix multiply with
     * @param result the result ndarray
     * @return the result of the matrix multiplication
     */
    @Override
    public INDArray mmul(INDArray other, INDArray result) {
        return dup().mmuli(other,result);
    }

    /**
     * in place (element wise) division of two matrices
     *
     * @param other the second ndarray to divide
     * @return the result of the divide
     */
    @Override
    public INDArray div(INDArray other) {
        return dup().divi(other);
    }

    /**
     * copy (element wise) division of two matrices
     *
     * @param other  the second ndarray to divide
     * @param result the result ndarray
     * @return the result of the divide
     */
    @Override
    public INDArray div(INDArray other, INDArray result) {
        return dup().divi(other,result);
    }

    /**
     * copy (element wise) multiplication of two matrices
     *
     * @param other the second ndarray to multiply
     * @return the result of the addition
     */
    @Override
    public INDArray mul(INDArray other) {
        return dup().muli(other);
    }

    /**
     * copy (element wise) multiplication of two matrices
     *
     * @param other  the second ndarray to multiply
     * @param result the result ndarray
     * @return the result of the multiplication
     */
    @Override
    public INDArray mul(INDArray other, INDArray result) {
        return dup().muli(other,result);
    }

    /**
     * copy subtraction of two matrices
     *
     * @param other the second ndarray to subtract
     * @return the result of the addition
     */
    @Override
    public INDArray sub(INDArray other) {
        return dup().subi(other);
    }

    /**
     * copy subtraction of two matrices
     *
     * @param other  the second ndarray to subtract
     * @param result the result ndarray
     * @return the result of the subtraction
     */
    @Override
    public INDArray sub(INDArray other, INDArray result) {
        return dup().subi(other,result);
    }

    /**
     * copy addition of two matrices
     *
     * @param other the second ndarray to add
     * @return the result of the addition
     */
    @Override
    public INDArray add(INDArray other) {
        return dup().addi(other);
    }

    /**
     * copy addition of two matrices
     *
     * @param other  the second ndarray to add
     * @param result the result ndarray
     * @return the result of the addition
     */
    @Override
    public INDArray add(INDArray other, INDArray result) {
        return dup().addi(other,result);
    }

    /**
     * Perform an copy matrix multiplication
     *
     * @param other the other matrix to perform matrix multiply with
     * @return the result of the matrix multiplication
     */
    @Override
    public INDArray mmuli(INDArray other) {
        return dup().mmuli(other,this);
    }

    /**
     * Perform an copy matrix multiplication
     *
     * @param other  the other matrix to perform matrix multiply with
     * @param result the result ndarray
     * @return the result of the matrix multiplication
     */
    @Override
    public INDArray mmuli(INDArray other, INDArray result) {
        INDArray otherArray =  other;
        INDArray resultArray =  result;

        LinAlgExceptions.assertMultiplies(this,other);

        if (other.isScalar()) {
            return muli((INDArray) otherArray.element(), resultArray);
        }
        if (isScalar()) {
            return otherArray.muli((INDArray)  element(), resultArray);
        }

        /* check sizes and resize if necessary */
        //assertMultipliesWith(other);


        if (result == this || result == other) {
            /* actually, blas cannot do multiplications in-place. Therefore, we will fake by
             * allocating a temporary object on the side and copy the result later.
             */
            INDArray temp = NDArrays.create(resultArray.shape(), ArrayUtil.calcStridesFortran(resultArray.shape()));

            if (otherArray.columns() == 1) {
                NDArrays.getBlasWrapper().gemv(1.0, this, otherArray, 0.0, temp);
            } else {
                NDArrays.getBlasWrapper().gemm(1.0, this, otherArray, 0.0, temp);
            }

            NDArrays.getBlasWrapper().copy(temp, resultArray);


        } else {
            if (otherArray.columns() == 1)
                NDArrays.getBlasWrapper().gemv(1.0, this, otherArray, 0.0, resultArray);
            else
                NDArrays.getBlasWrapper().gemm(1.0, this, otherArray, 0.0, resultArray);

        }
        return resultArray;
    }

    /**
     * in place (element wise) division of two matrices
     *
     * @param other the second ndarray to divide
     * @return the result of the divide
     */
    @Override
    public INDArray divi(INDArray other) {
        return divi(other,this);
    }

    /**
     * in place (element wise) division of two matrices
     *
     * @param other  the second ndarray to divide
     * @param result the result ndarray
     * @return the result of the divide
     */
    @Override
    public INDArray divi(INDArray other, INDArray result) {
        if(other.isScalar())
            new TwoArrayOps().from(this).scalar(other).op(DivideOp.class)
                    .to(result).build().exec();
        else
            new TwoArrayOps().from(this).other(other).op(DivideOp.class)
                    .to(result).build().exec();
        return  result;
    }

    /**
     * in place (element wise) multiplication of two matrices
     *
     * @param other the second ndarray to multiply
     * @return the result of the addition
     */
    @Override
    public INDArray muli(INDArray other) {
        return muli(other,this);
    }

    /**
     * in place (element wise) multiplication of two matrices
     *
     * @param other  the second ndarray to multiply
     * @param result the result ndarray
     * @return the result of the multiplication
     */
    @Override
    public INDArray muli(INDArray other, INDArray result) {
        if(other.isScalar())
            new TwoArrayOps().from(this).scalar(other).op(MultiplyOp.class)
                    .to(result).build().exec();
        else
            new TwoArrayOps().from(this).other(other).op(MultiplyOp.class)
                    .to(result).build().exec();
        return  result;
    }

    /**
     * in place subtraction of two matrices
     *
     * @param other the second ndarray to subtract
     * @return the result of the addition
     */
    @Override
    public INDArray subi(INDArray other) {
        return subi(other,this);
    }

    /**
     * in place subtraction of two matrices
     *
     * @param other  the second ndarray to subtract
     * @param result the result ndarray
     * @return the result of the subtraction
     */
    @Override
    public INDArray subi(INDArray other, INDArray result) {

        if(other.isScalar())
            new TwoArrayOps().from(this).scalar(other).op(SubtractOp.class)
                    .to(result).build().exec();
        else
            new TwoArrayOps().from(this).other(other).op(SubtractOp.class)
                    .to(result).build().exec();
        return  result;
    }

    /**
     * in place addition of two matrices
     *
     * @param other the second ndarray to add
     * @return the result of the addition
     */
    @Override
    public INDArray addi(INDArray other) {
        return addi(other,this);
    }

    /**
     * in place addition of two matrices
     *
     * @param other  the second ndarray to add
     * @param result the result ndarray
     * @return the result of the addition
     */
    @Override
    public INDArray addi(INDArray other, INDArray result) {
        if(other.isScalar())
            new TwoArrayOps().from(this).scalar(other).op(AddOp.class)
                    .to(result).build().exec();

        else
            new TwoArrayOps().from(this).other(other).op(AddOp.class)
                    .to(result).build().exec();
        return  result;
    }

    /**
     * Returns the normmax along the specified dimension
     *
     * @param dimension the dimension to getScalar the norm1 along
     * @return the norm1 along the specified dimension
     */
    @Override
    public INDArray normmax(int dimension) {
        if(isVector()) {
            return normmax(Integer.MAX_VALUE);
        }
        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.normmax(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    INDArray arr2 = nd;
                    arr.put(i.get(),arr2.normmax(0));
                    i.incrementAndGet();
                }
            }, false);

            return arr.reshape(shape);
        }
    }






    /**
     * Reverse division
     *
     * @param other the matrix to divide from
     * @return
     */
    @Override
    public INDArray rdiv(INDArray other) {
        return dup().rdivi(other);
    }

    /**
     * Reverse divsion (in place)
     *
     * @param other
     * @return
     */
    @Override
    public INDArray rdivi(INDArray other) {
        return rdivi(other,this);
    }

    /**
     * Reverse division
     *
     * @param other  the matrix to subtract from
     * @param result the result ndarray
     * @return
     */
    @Override
    public INDArray rdiv(INDArray other, INDArray result) {
        return dup().rdivi(other,result);
    }

    /**
     * Reverse division (in-place)
     *
     * @param other  the other ndarray to subtract
     * @param result the result ndarray
     * @return the ndarray with the operation applied
     */
    @Override
    public INDArray rdivi(INDArray other, INDArray result) {
        return other.divi(this, result);
    }

    /**
     * Reverse subtraction
     *
     * @param other  the matrix to subtract from
     * @param result the result ndarray
     * @return
     */
    @Override
    public INDArray rsub(INDArray other, INDArray result) {
        return dup().rsubi(other,result);
    }

    /**
     * @param other
     * @return
     */
    @Override
    public INDArray rsub(INDArray other) {
        return dup().rsubi(other);
    }

    /**
     * @param other
     * @return
     */
    @Override
    public INDArray rsubi(INDArray other) {
        return rsubi(other,this);
    }

    /**
     * Reverse subtraction (in-place)
     *
     * @param other  the other ndarray to subtract
     * @param result the result ndarray
     * @return the ndarray with the operation applied
     */
    @Override
    public INDArray rsubi(INDArray other, INDArray result) {
        return other.subi(this, result);
    }

    /**
     * Set the value of the ndarray to the specified value
     *
     * @param value the value to assign
     * @return the ndarray with the values
     */
    @Override
    public INDArray assign(Number value) {
        INDArray one = reshape(new int[]{1,length});
        for(int i = 0; i < one.length(); i++)
            one.put(i,NDArrays.scalar(value.doubleValue()));
        return one;
    }

    @Override
    public int linearIndex(int i) {
         int realStride = getRealStrideForLinearIndex();
        int idx = offset + i * realStride;
        if(idx >= data.length)
            throw new IllegalArgumentException("Illegal index " + idx + " derived from " + i + " with offset of " + offset + " and stride of " + realStride);
        return idx;
    }

    protected int getRealStrideForLinearIndex() {
        assert stride != null && shape != null && stride.length == shape.length : "Illegal state: stride and shape must be same length";
        if(stride == null || stride().length < 1)
            return 1;
        if(stride.length == 2 && shape[0] == 1)
            return stride[1];
        if(stride().length == 2 && shape[1] == 1)
            return stride[0];
        return stride[0];


    }







    /**
     * Returns the specified slice of this matrix.
     * In matlab, this would be equivalent to (given a 2 x 2 x 2):
     * A(:,:,x) where x is the slice you want to return.
     *
     * The slice is always relative to the final dimension of the matrix.
     *
     * @param slice the slice to return
     * @return the specified slice of this matrix
     */
    @Override
    public INDArray slice(int slice) {

        if (shape.length == 0)
            throw new IllegalArgumentException("Can't slice a 0-d NDArray");

            //slice of a vector is a scalar
        else if (shape.length == 1)
            return NDArrays.create(data,ArrayUtil.empty(),ArrayUtil.empty(),offset + slice * stride[0]);


            //slice of a matrix is a vector
        else if (shape.length == 2) {
            INDArray slice2 = NDArrays.create(
                    data,
                    ArrayUtil.of(shape[1]),
                    Arrays.copyOfRange(stride, 1, stride.length),
                    offset + slice * stride[0], ordering
            );
            return slice2;

        }

        else {
            INDArray slice2 = NDArrays.create(data,
                    Arrays.copyOfRange(shape, 1, shape.length),
                    Arrays.copyOfRange(stride, 1, stride.length),
                    offset + (slice * stride[0]),ordering);
            return slice2;
        }
    }


    /**
     * Returns the slice of this from the specified dimension
     * @param slice the dimension to return from
     * @param dimension the dimension of the slice to return
     * @return the slice of this matrix from the specified dimension
     * and dimension
     */
    @Override
    public INDArray slice(int slice, int dimension) {
        if(shape.length == 2) {
            //rows
            if(dimension == 1)
                return getRow(slice);


            else if(dimension == 0)
                return getColumn(slice);

            else throw new IllegalAccessError("Illegal dimension for matrix");

        }

        if (slice == shape.length - 1)
            return slice(dimension);

        INDArray slice2 = NDArrays.create(data,
                ArrayUtil.removeIndex(shape,dimension),
                ArrayUtil.removeIndex(stride,dimension),
                offset + slice * stride[dimension],ordering);
        return slice2;
    }



    /**
     * Fetch a particular number on a multi dimensional scale.
     * @param indexes the indexes to getFromOrigin a number from
     * @return the number at the specified indices
     */
    @Override
    public INDArray getScalar(int... indexes) {
        int ix = offset;
        for (int i = 0; i < shape.length; i++) {
            ix += indexes[i] * stride[i];
        }

        if(ix >= data.length)
            throw new IllegalArgumentException("Illegal index " + Arrays.toString(indexes));

        return NDArrays.scalar(data[ix]);
    }


    @Override
    public INDArray rdiv(Number n) {
        return dup().rdivi(n);
    }

    @Override
    public INDArray rdivi(Number n) {
        return rdivi(NDArrays.valueArrayOf(shape(), n.doubleValue()));
    }

    @Override
    public INDArray rsub(Number n) {
        return dup().rsubi(n);
    }

    @Override
    public INDArray rsubi(Number n) {
        return rsubi(NDArrays.valueArrayOf(shape(),n.doubleValue()));
    }

    @Override
    public INDArray div(Number n) {
        return dup().divi(n);
    }

    @Override
    public INDArray divi(Number n) {
        return divi(NDArrays.scalar(n));
    }

    @Override
    public INDArray mul(Number n) {
        return dup().muli(n);
    }

    @Override
    public INDArray muli(Number n) {
        return muli(NDArrays.scalar(n));
    }

    @Override
    public INDArray sub(Number n) {
        return dup().subi(n);
    }

    @Override
    public INDArray subi(Number n) {
        return subi(NDArrays.scalar(n));
    }

    @Override
    public INDArray add(Number n) {
        return dup().addi(n);
    }

    @Override
    public INDArray addi(Number n) {
        return addi(NDArrays.scalar(n));
    }


    /**
     * Replicate and tile array to fill out to the given shape
     *
     * @param shape the new shape of this ndarray
     * @return the shape to fill out to
     */
    @Override
    public INDArray repmat(int[] shape) {
        int[] newShape = ArrayUtil.copy(shape());
        assert shape.length <= newShape.length : "Illegal shape: The passed in shape must be <= the current shape length";
        for(int i = 0; i < shape.length; i++)
            newShape[i] *= shape[i];
        INDArray result = NDArrays.create(newShape);
        //nd copy
        if(isScalar()) {
            for(int i = 0; i < result.length(); i++) {
                result.put(i,getScalar(0));

            }
        }

        else if(isMatrix()) {

            for (int c = 0; c < shape()[1]; c++) {
                for (int r = 0; r < shape()[0]; r++) {
                    for (int i = 0; i < rows(); i++) {
                        for (int j = 0; j < columns(); j++) {
                            result.put(r * rows() + i, c * columns() + j, getScalar(i, j));
                        }
                    }
                }
            }

        }

        else {
            int[] sliceRepmat = ArrayUtil.removeIndex(shape,0);
            for(int i = 0; i < result.slices(); i++) {
                result.putSlice(i,repmat(sliceRepmat));
            }
        }

        INDArray ret =  result;
        return  ret;
    }

    /**
     * Insert a row in to this array
     * Will throw an exception if this
     * ndarray is not a matrix
     *
     * @param row   the row insert into
     * @param toPut the row to insert
     * @return this
     */
    @Override
    public INDArray putRow(int row, INDArray toPut) {
        assert toPut.isVector() && toPut.length() == columns : "Illegal length for row " + toPut.length() + " should have been " + columns;
        INDArray r = getRow(row);
        for(int i = 0; i < r.length(); i++)
            r.putScalar(i,toPut.get(i));
        return this;
    }

    /**
     * Insert a column in to this array
     * Will throw an exception if this
     * ndarray is not a matrix
     *
     * @param column the column to insert
     * @param toPut  the array to put
     * @return this
     */
    @Override
    public INDArray putColumn(int column, INDArray toPut) {
        assert toPut.isVector() && toPut.length() == rows : "Illegal length for row " + toPut.length() + " should have been " + columns;
        INDArray r = getColumn(column);
        for(int i = 0; i < r.length(); i++)
            r.putScalar(i,toPut.get(i));

        return this;
    }



    @Override
    public INDArray get(int[] indices) {
        return  getScalar(indices);
    }

    @Override
    public double get(int i) {
        int idx = linearIndex(i);
        return data[idx];
    }

    @Override
    public double get(int i, int j) {
        int idx = index(i,j);
        return data[idx];
    }

    /**
     * Mainly an internal method (public for testing)
     * for given an offset and stride, and index,
     * calculating the beginning index of a query given indices
     * @param offset the desired offset
     * @param stride the desired stride
     * @param indexes the desired indexes to test on
     * @return the index for a query given stride and offset
     */
    public static int getIndex(int offset,int[] stride,int...indexes) {
        if(stride.length > indexes.length)
            throw new IllegalArgumentException("Invalid number of items in stride array: should be <= number of indexes");

        int ix = offset;


        for (int i = 0; i < indexes.length; i++) {
            ix += indexes[i] * stride[i];
        }
        return ix;
    }

    /**
     * Returns the begin index of a query
     * given the stride, array offset
     * @param indexes the desired indexes to test on
     * @return the index of the begin of this query
     */
    public int getIndex(int... indexes) {
        return getIndex(offset,stride,indexes);
    }


    protected void ensureSameShape(INDArray arr1,INDArray arr2) {
        assert true == Shape.shapeEquals(arr1.shape(), arr2.shape());

    }









    /**
     * Return transposed copy of this matrix.
     */
    @Override
    public INDArray transpose() {
        if(isRowVector())
            return NDArrays.create(data,new int[]{shape[0],1},offset);
        else if(isColumnVector())
            return NDArrays.create(data,new int[]{shape[0]},offset);
        if(ordering == NDArrayFactory.C) {
            INDArray n = NDArrays.create(data, reverseCopy(shape), reverseCopy(stride), offset, ordering);
            return n;
        }
        else if(ordering == NDArrayFactory.FORTRAN) {
            int[] reverseShape = reverseCopy(shape);
            int[] newStrides = ArrayUtil.calcStridesFortran(reverseShape);
            INDArray n = NDArrays.create(data,reverseShape,newStrides,offset,ordering);
            return n;
        }


        throw new IllegalArgumentException("Illegal ordering " + ordering);

    }


    /**
     * Reshape the ndarray in to the specified dimensions,
     * possible errors being thrown for invalid shapes
     * @param shape
     * @return
     */
    @Override
    public INDArray reshape(int[] shape) {
        long ec = 1;
        for (int i = 0; i < shape.length; i++) {
            int si = shape[i];
            if (( ec * si ) != (((int) ec ) * si ))
                throw new IllegalArgumentException("Too many elements");
            ec *= shape[i];
        }

        int n = (int) ec;

        if (ec != n)
            throw new IllegalArgumentException("Too many elements");

        if(Shape.shapeEquals(shape(), shape))
            return this;




        //row to column vector
        if(isRowVector()) {
            if(Shape.isColumnVectorShape(shape)) {
                return NDArrays.create(data,shape,new int[]{stride[0],1},offset,ordering);
            }
        }
        //column to row vector
        if(isColumnVector()) {
            if(Shape.isRowVectorShape(shape)) {
                return NDArrays.create(data,shape,new int[]{stride[0]},offset,ordering);

            }
        }



        //returns strides for reshape or null if data needs to be copied
        int[] newStrides = newStridesReshape(shape);

        if(newStrides != null) {
            INDArray ndArray = NDArrays.create(data,shape,stride,offset,ordering);
            return ndArray;


        }

        //need to copy data
        else {
            INDArray create = NDArrays.create(shape,NDArrays.getStrides(shape,ordering));
            final INDArray flattened = ravel();
            //individual vector size
            int vectorSize = create.size(create.shape().length - 1);
            //current position in the vector
            final AtomicInteger vectorCounter = new AtomicInteger(0);
            //row order
            if(ordering == NDArrayFactory.C) {
                create.iterateOverAllRows(new SliceOp() {
                    @Override
                    public void operate(DimensionSlice nd) {
                        INDArray nd1 = (INDArray) nd.getResult();
                        for (int i = 0; i < nd1.length(); i++) {
                            int element = vectorCounter.getAndIncrement();
                            nd1.put(i, flattened.getScalar(element));
                        }
                    }

                    @Override
                    public void operate(INDArray nd) {
                        for (int i = 0; i < nd.length(); i++) {
                            int element = vectorCounter.getAndIncrement();
                            nd.put(i, flattened.getScalar(element));

                        }
                    }
                });
            }
            //column order
            else if(ordering == NDArrayFactory.FORTRAN) {
                create.iterateOverAllColumns(new SliceOp() {
                    @Override
                    public void operate(DimensionSlice nd) {
                        INDArray nd1 = (INDArray) nd.getResult();

                        for(int i = 0; i < nd1.length(); i++) {
                            int element = vectorCounter.getAndIncrement();
                            nd1.put(i, flattened.getScalar(element));

                        }
                    }

                    @Override
                    public void operate(INDArray nd) {
                        for(int i = 0; i < nd.length(); i++) {
                            int element = vectorCounter.getAndIncrement();
                            nd.put(i, flattened.getScalar(element));

                        }
                    }
                });
            }

            return create;

        }



    }





    protected int[] newStridesReshape(int[] shape) {

        int[][] oldShapeAndStride = getNonOneStridesAndShape();
        int[] oldShape = oldShapeAndStride[0];
        int[] oldStride = oldShapeAndStride[1];
         /* oi to oj and ni to nj give the axis ranges currently worked with */
        int newNd = shape.length;
        int oldNd = oldShapeAndStride[0].length;
        int np, op;
        int nk;


        //must be same length
        if (ArrayUtil.prod(shape) != ArrayUtil.prod(oldShape))
            return null;
        //no 0 length arr
        if (ArrayUtil.prod(shape) == 0)
            return null;

        int[] newStrides = new int[oldStride.length];


         /* oi to oj and ni to nj give the axis ranges currently worked with */
        int ni = 0,
                oi = 0,
                nj = 1,
                oj = 1;

        for (; ni < newNd && oi < oldNd; ni = nj++, oi = oj++) {
            np = shape[ni];
            op = oldShape[oi];

            while (np != op) {
                if (np < op)
                /* Misses trailing 1s, these are handled later */
                    np *= shape[nj++];

                else
                    op *= oldShape[oj++];

            }

             /* Check whether the original axes can be combined */
            for (int ok = oi; ok < oj - 1; ok++) {
                if (ordering == NDArrayFactory.FORTRAN) {
                    if (oldStride[ok + 1] != oldStride[ok] * oldStride[ok])
                     /* not contiguous enough */
                        return null;

                } else {
                /* C order */
                    if (oldStride[ok] != oldShape[ok + 1] * oldStride[ok + 1])
                    /* not contiguous enough */
                        return null;

                }
            }


        }
        return NDArrays.getStrides(shape,ordering);

    }


    //get a 2d array of the non one sizes of the array and their associated strides
    protected int[][] getNonOneStridesAndShape() {
        int nonOneDims = 0;
        for(int i = 0; i < shape.length; i++)
            if(size(i) != 1)
                nonOneDims++;
        int[][] ret = new int[2][nonOneDims];
        int count = 0;
        for(int i = 0; i < shape.length; i++) {
            if(size(i) != 1) {
                ret[0][count] = size(i);
                ret[1][count] = stride[i];
                count++;
            }
        }

        return ret;
    }


    @Override
    public void checkDimensions(INDArray other) {
        assert Arrays.equals(shape,other.shape()) : " Other array should have been shape: " + Arrays.toString(shape) + " but was " + Arrays.toString(other.shape());
        assert Arrays.equals(stride,other.stride()) : " Other array should have been stride: " + Arrays.toString(stride) + " but was " + Arrays.toString(other.stride());
        assert offset == other.offset() : "Offset of this array is " + offset + " but other was " + other.offset();

    }






    /**
     * Returns the product along a given dimension
     *
     * @param dimension the dimension to getScalar the product along
     * @return the product along the specified dimension
     */
    @Override
    public INDArray prod(int dimension) {

        if(dimension == Integer.MAX_VALUE) {
            return NDArrays.scalar(Ops.prod(this));
        }

        else if(isVector()) {
            return  prod(Integer.MAX_VALUE);
        }
        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.prod(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.prod(0));
                    i.incrementAndGet();

                }
            }, false);

            return arr.reshape(shape);
        }
    }

    /**
     * Returns the overall mean of this ndarray
     *
     * @param dimension the dimension to getScalar the mean along
     * @return the mean along the specified dimension of this ndarray
     */
    @Override
    public INDArray mean(int dimension) {
        if(dimension == Integer.MAX_VALUE || isVector()) {
            return NDArrays.scalar(Ops.mean(this));

        }
        else if(isVector()) {
            return NDArrays.scalar((double) sum(Integer.MAX_VALUE).element() / length());
        }
        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.mean(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.mean(0));
                    i.incrementAndGet();
                }
            }, false);

            return arr.reshape(shape);
        }
    }

    /**
     * Returns the overall variance of this ndarray
     *
     * @param dimension the dimension to getScalar the mean along
     * @return the mean along the specified dimension of this ndarray
     */
    @Override
    public INDArray var(int dimension) {
        if(dimension == Integer.MAX_VALUE || isVector()) {
            return NDArrays.scalar(Ops.var(this));
        }

        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.var(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.var(0));
                    i.incrementAndGet();
                }
            }, false);

            return arr.reshape(shape);
        }
    }

    /**
     * Returns the overall max of this ndarray
     *
     * @param dimension the dimension to getScalar the mean along
     * @return the mean along the specified dimension of this ndarray
     */
    @Override
    public INDArray max(int dimension) {
        if(dimension == Integer.MAX_VALUE || isVector()) {
            return NDArrays.scalar(Ops.max(this));
        }


        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.max(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.max(0));
                    i.incrementAndGet();
                }
            }, false);

            return arr.reshape(shape).transpose();
        }
    }

    /**
     * Returns the overall min of this ndarray
     *
     * @param dimension the dimension to getScalar the mean along
     * @return the mean along the specified dimension of this ndarray
     */
    @Override
    public INDArray min(int dimension) {
        if(dimension == Integer.MAX_VALUE || isVector()) {
            return NDArrays.scalar(Ops.min(this));
        }


        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr =  NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.min(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.min(0));
                    i.incrementAndGet();
                }
            }, false);

            return arr.reshape(shape).transpose();
        }
    }

    /**
     * Returns the sum along the last dimension of this ndarray
     *
     * @param dimension the dimension to getScalar the sum along
     * @return the sum along the specified dimension of this ndarray
     */
    @Override
    public INDArray sum(int dimension) {
        if(dimension == Integer.MAX_VALUE || isVector()) {
            return NDArrays.scalar(Ops.sum(this));
        }

        else if(isVector()) {
            return  sum(Integer.MAX_VALUE);
        }
        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.sum(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.sum(0));
                    i.incrementAndGet();
                }
            }, false);

            return arr.reshape(shape);
        }
    }



    /**
     * Returns the norm1 along the specified dimension
     *
     * @param dimension the dimension to getScalar the norm1 along
     * @return the norm1 along the specified dimension
     */
    @Override
    public INDArray norm1(int dimension) {
        if(isVector() || dimension == Integer.MAX_VALUE) {
            return NDArrays.scalar(Ops.norm1(this));
        }
        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.norm1(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.norm1(0));
                    i.incrementAndGet();

                }
            }, false);

            return arr.reshape(shape);
        }
    }




    /**
     * Standard deviation of an ndarray along a dimension
     *
     * @param dimension the dimension to get the std along
     * @return the standard deviation along a particular dimension
     */
    @Override
    public INDArray std(int dimension) {
        if(isVector() || dimension == Integer.MAX_VALUE) {
            return NDArrays.scalar(Ops.std(this));
        }
        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.std(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.std(0));
                    i.incrementAndGet();

                }
            }, false);

            return arr.reshape(shape);
        }
    }


    /**
     * Returns the norm2 along the specified dimension
     *
     * @param dimension the dimension to getScalar the norm2 along
     * @return the norm2 along the specified dimension
     */
    @Override
    public INDArray norm2(int dimension) {
        if(isVector() || dimension == Integer.MAX_VALUE) {
            return  NDArrays.scalar(Ops.norm2(this));
        }
        else {
            int[] shape = ArrayUtil.removeIndex(shape(),dimension);
            final INDArray arr = NDArrays.create(new int[]{ArrayUtil.prod(shape)});
            final AtomicInteger i = new AtomicInteger(0);
            iterateOverDimension(dimension, new SliceOp() {
                @Override
                public void operate(DimensionSlice nd) {
                    INDArray arr2 = (INDArray) nd.getResult();
                    arr.put(i.get(),arr2.norm2(0));
                    i.incrementAndGet();
                }

                /**
                 * Operates on an ndarray slice
                 *
                 * @param nd the result to operate on
                 */
                @Override
                public void operate(INDArray nd) {
                    arr.put(i.get(),nd.norm2(0));
                    i.incrementAndGet();

                }
            }, false);

            return arr.reshape(shape);
        }
    }


    /**
     * Number of columns (shape[1]), throws an exception when
     * called when not 2d
     * @return the number of columns in the array (only 2d)
     */
    @Override
    public int columns() {
        if(isMatrix()) {
            if (shape().length > 2)
                return Shape.squeeze(shape)[1];
            else if (shape().length == 2)
                return shape[1];
        }
        if(isVector()) {
            if(isColumnVector())
                return 1;
            else
                return shape[0];
        }
        throw new IllegalStateException("Unable to getFromOrigin number of of rows for a non 2d matrix");
    }

    /**
     * Returns the number of rows
     * in the array (only 2d) throws an exception when
     * called when not 2d
     * @return the number of rows in the matrix
     */
    @Override
    public int rows() {
        if(isMatrix()) {
            if (shape().length > 2)
                return Shape.squeeze(shape)[0];
            else if (shape().length == 2)
                return shape[0];
        }
        else if(isVector()) {
            if(isRowVector())
                return 1;
            else
                return shape[0];
        }
        throw new IllegalStateException("Unable to getFromOrigin number of of rows for a non 2d matrix");
    }





    /**
     * Flattens the array for linear indexing
     * @return the flattened version of this array
     */
    @Override
    public INDArray ravel() {
        final INDArray ret = NDArrays.create(length,ordering);
        final AtomicInteger counter = new AtomicInteger(0);

        SliceOp op = new SliceOp() {
            @Override
            public void operate(DimensionSlice nd) {
                INDArray nd1 = (INDArray) nd.getResult();
                for (int i = 0; i < nd1.length(); i++) {
                    int element = counter.getAndIncrement();
                    ret.putScalar(element,nd1.get(i));
                }
            }

            @Override
            public void operate(INDArray nd) {
                for (int i = 0; i < nd.length(); i++) {
                    int element = counter.getAndIncrement();
                    ret.putScalar(element,nd.get(i));


                }
            }
        };
        //row order
        if(ordering == NDArrayFactory.C) {
            iterateOverAllRows(op);
        }
        //column order
        else if(ordering == NDArrayFactory.FORTRAN) {
            iterateOverAllColumns(op);
        }

        return ret;

    }

    /**
     * Flattens the array for linear indexing
     * @return the flattened version of this array
     */
    @Override
    public void sliceVectors(List<INDArray> list) {
        if(isVector())
            list.add(this);
        else {
            for(int i = 0; i < slices(); i++) {
                slice(i).sliceVectors(list);
            }
        }
    }

    /**
     * Reshape the matrix. Number of elements must not change.
     *
     * @param newRows
     * @param newColumns
     */
    @Override
    public INDArray reshape(int newRows, int newColumns) {
        return reshape(new int[]{newRows,newColumns});
    }

    /**
     * Get the specified column
     *
     * @param c
     */
    @Override
    public INDArray getColumn(int c) {
        if(shape.length == 2) {
            if(ordering == NDArrayFactory.C) {
                INDArray ret = NDArrays.create(
                        data,
                        new int[]{shape[0]},
                        new int[]{stride[0]},
                        offset + c, ordering
                );

                return ret;
            }
            else {
                INDArray ret = NDArrays.create(
                        data,
                        new int[]{shape[0]},
                        new int[]{stride[0]},
                        offset + c * rows(),ordering
                );

                return ret;
            }

        }

        else
            throw new IllegalArgumentException("Unable to getFromOrigin column of non 2d matrix");
    }


    /**
     * Get whole rows from the passed indices.
     *
     * @param rindices
     */
    @Override
    public INDArray getRows(int[] rindices) {
        INDArray rows = NDArrays.create(rindices.length,columns());
        for(int i = 0; i < rindices.length; i++) {
            rows.putRow(i,getRow(rindices[i]));
        }
        return  rows;
    }

    /**
     * Returns a subset of this array based on the specified
     * indexes
     *
     * @param indexes the indexes in to the array
     * @return a view of the array with the specified indices
     */
    @Override
    public INDArray get(NDArrayIndex... indexes) {
        if(indexes.length < shape().length) {

        }
        return null;
    }

    /**
     * Get whole columns from the passed indices.
     *
     * @param cindices
     */
    @Override
    public INDArray getColumns(int[] cindices) {
        INDArray rows = NDArrays.create(rows(),cindices.length);
        for(int i = 0; i < cindices.length; i++) {
            rows.putColumn(i,getColumn(cindices[i]));
        }
        return rows;
    }

    /**
     * Get a copy of a row.
     *
     * @param r
     */
    @Override
    public INDArray getRow(int r) {
        if(shape.length == 2) {
            if(ordering == NDArrayFactory.C) {
                INDArray ret = NDArrays.create(
                        data,
                        new int[]{shape[1]},
                        new int[]{stride[1]},
                        offset + r * columns(),
                        ordering
                );
                return ret;
            }
            else {
                INDArray ret  = NDArrays.create(
                        data,
                        new int[]{shape[1]},
                        new int[]{stride[1]},
                        offset + r,
                        ordering
                );
                return ret;
            }


        }
        else
            throw new IllegalArgumentException("Unable to getFromOrigin row of non 2d matrix");
    }

    /**
     * Compare two matrices. Returns true if and only if other is also a
     * DoubleMatrix which has the same size and the maximal absolute
     * difference in matrix elements is smaller than 1e-6.
     *
     * @param o
     */
    @Override
    public boolean equals(Object o) {
        INDArray n = null;

        if(!(o instanceof  INDArray))
            return false;

        if(n == null)
            n =  (INDArray) o;

        //epsilon equals
        if(isScalar() && n.isScalar()) {
            double val = (double) element();
            double val2 = (double) n.element();
            return Math.abs(val - val2) < 1e-6;
        }
        else if(isVector() && n.isVector()) {
            for(int i = 0; i < length; i++) {
                double curr = get(i);
                double comp = n.get(i);
                if(Math.abs(curr - comp) > 1e-6)
                    return false;
            }

            return true;

        }


        if(!Shape.shapeEquals(shape(),n.shape()))
            return false;



        if(slices() != n.slices())
            return false;

        for (int i = 0; i < slices(); i++) {
            INDArray slice = slice(i);
            INDArray nSlice = n.slice(i);

            if (!slice.equals(nSlice))
                return false;
        }

        return true;


    }


    /**
     * Returns the shape(dimensions) of this array
     * @return the shape of this matrix
     */
    public int[] shape() {
        return shape;
    }

    /**
     * Returns the stride(indices along the linear index for which each slice is accessed) of this array
     * @return the stride of this array
     */
    @Override
    public int[] stride() {
        return stride;
    }


    @Override
    public int offset() {
        return offset;
    }

    @Override
    public char ordering() {
        return ordering;
    }

    /**
     * Returns the size of this array
     * along a particular dimension
     * @param dimension the dimension to return from
     * @return the shape of the specified dimension
     */
    @Override
    public int size(int dimension) {
        if(isScalar()) {
            if(dimension == 0)
                return length;
            else
                throw new IllegalArgumentException("Illegal dimension for scalar " + dimension);
        }

        else if(isVector()) {
            if(dimension == 0)
                return length;
            else if(dimension == 1)
                return 1;
        }

        return shape[dimension];
    }

    /**
     * Returns the total number of elements in the ndarray
     *
     * @return the number of elements in the ndarray
     */
    @Override
    public int length() {
        return length;
    }

    /**
     * Broadcasts this ndarray to be the specified shape
     *
     * @param shape the new shape of this ndarray
     * @return the broadcasted ndarray
     */
    @Override
    public INDArray broadcast(int[] shape) {
        return null;
    }

    /**
     * Broadcasts this ndarray to be the specified shape
     *
     * @param shape the new shape of this ndarray
     * @return the broadcasted ndarray
     */
    @Override
    public INDArray broadcasti(int[] shape) {
        return null;
    }


    /**
     * See: http://www.mathworks.com/help/matlab/ref/permute.html
     * @param rearrange the dimensions to swap to
     * @return the newly permuted array
     */
    @Override
    public INDArray permute(int[] rearrange) {
        checkArrangeArray(rearrange);

        int[] newShape = doPermuteSwap(shape,rearrange);
        int[] newStride = doPermuteSwap(stride,rearrange);
        return  NDArrays.create(data,newShape,newStride,offset,ordering);
    }




    protected int[] doPermuteSwap(int[] shape,int[] rearrange) {
        int[] ret = new int[shape.length];
        for(int i = 0; i < shape.length; i++) {
            ret[i] = shape[rearrange[i]];
        }
        return ret;
    }


    protected void checkArrangeArray(int[] arr) {
        assert arr.length == shape.length : "Invalid rearrangement: number of arrangement != shape";
        for(int i = 0; i < arr.length; i++) {
            if (arr[i] >= arr.length)
                throw new IllegalArgumentException("The specified dimensions can't be swapped. Given element " + i + " was >= number of dimensions");
            if (arr[i] < 0)
                throw new IllegalArgumentException("Invalid dimension: " + i + " : negative value");


        }

        for(int i = 0; i < arr.length; i++) {
            for(int j = 0; j < arr.length; j++) {
                if(i != j && arr[i] == arr[j])
                    throw new IllegalArgumentException("Permute array must have unique elements");
            }
        }

    }

    /**
     * Checks whether the matrix is a vector.
     */
    @Override
    public boolean isVector() {
        return shape.length == 1
                ||
                shape.length == 1  && shape[0] == 1
                ||
                shape.length == 2 && (shape[0] == 1 || shape[1] == 1) && !isScalar();
    }


    /**
     * Checks whether the matrix is a row vector.
     */
    @Override
    public boolean isRowVector() {
        if(shape().length == 1)
            return true;

        if(isVector())
            return shape()[0] == 1;

        return false;
    }

    /**
     * Checks whether the matrix is a column vector.
     */
    @Override
    public boolean isColumnVector() {
        if(shape().length == 1)
            return false;

        if(isVector())
            return shape()[1] == 1;

        return false;

    }

    /** Generate string representation of the matrix. */
    @Override
    public String toString() {
        if (isScalar()) {
            return element().toString();
        }
        else if(isMatrix()) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for(int i = 0; i < rows; i++) {
                sb.append('[');
                for(int j = 0; j < columns; j++) {
                    sb.append(getScalar(i,j));
                    if(j < columns - 1)
                        sb.append(',');
                }
                sb.append(']');

            }

            sb.append("]\n");
            return sb.toString();
        }


        else if(isVector()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for(int i = 0; i < length; i++) {
                sb.append(getScalar(i));
                if(i < length - 1)
                    sb.append(',');
            }

            sb.append("]\n");
            return sb.toString();
        }



        StringBuilder sb = new StringBuilder();
        int length= shape[0];
        sb.append("[");
        if (length > 0) {
            sb.append(slice(0).toString());
            for (int i = 1; i < slices(); i++) {
                sb.append(slice(i).toString());
                if(i < length - 1)
                    sb.append(',');

            }
        }
        sb.append("]\n");
        return sb.toString();
    }



    /**
     * Returns a scalar (individual element)
     * of a scalar ndarray
     *
     * @return the individual item in this ndarray
     */
    @Override
    public Object element() {
        if(!isScalar())
            throw new IllegalStateException("Unable to retrieve element from non scalar matrix");
        return data[0];
    }







}