package org.deeplearning4j.util;

import org.deeplearning4j.nn.linalg.*;
import org.jblas.ComplexDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ComplexNDArray operations
 *
 * @author Adam Gibson
 */
public class ComplexNDArrayUtil {

    private static Logger log = LoggerFactory.getLogger(ComplexNDArrayUtil.class);

    public static enum ScalarOp {
        SUM,
        MEAN,
        PROD,
        MAX,
        MIN,
        ARG_MAX,
        ARG_MIN,
        NORM_2,
        NORM_1,
        NORM_MAX
    }


    public static enum DimensionOp {
        SUM,
        MEAN,
        PROD,
        MAX,
        MIN,
        ARG_MIN,
        NORM_2,
        NORM_1,
        NORM_MAX,
        FFT
    }


    public static enum MatrixOp {
        COLUMN_MIN,
        COLUMN_MAX,
        COLUMN_SUM,
        COLUMN_MEAN,
        ROW_MIN,
        ROW_MAX,
        ROW_SUM,
        ROW_MEAN
    }





    /**
     * Truncates an ndarray to the specified shape.
     * If the shape is the same or greater, it just returns
     * the original array
     * @param nd the ndarray to truncate
     * @param n the number of elements to truncate to
     * @return the truncated ndarray
     */
    public static ComplexNDArray truncate(ComplexNDArray nd,final int n,int dimension) {

        if(nd.size(dimension) > n) {
            int[] targetShape = ArrayUtil.copy(nd.shape());
            targetShape[dimension] = n;
            int numRequired = ArrayUtil.prod(targetShape);
            if(nd.isVector()) {
                ComplexNDArray ret = new ComplexNDArray(targetShape);
                int count = 0;
                for(int i = 0; i < nd.length; i+= nd.stride()[dimension]) {
                    ret.put(count++,nd.get(i));

                }
                return ret;
            }

            else if(nd.isMatrix()) {
                List<ComplexDouble> list = new ArrayList<>();
                //row
                if(dimension == 0) {
                    for(int i = 0;i < nd.rows(); i++) {
                        ComplexNDArray row = nd.getRow(i);
                        for(int j = 0; j < row.length; j++) {
                            if(list.size() == numRequired)
                                return new ComplexNDArray(list.toArray(new ComplexDouble[0]),targetShape);

                            list.add(row.get(j));
                        }
                    }
                }
                else if(dimension == 1) {
                    for(int i = 0;i < nd.columns(); i++) {
                        ComplexNDArray row = nd.getColumn(i);
                        for(int j = 0; j < row.length; j++) {
                            if(list.size() == numRequired)
                                return new ComplexNDArray(list.toArray(new ComplexDouble[0]),targetShape);

                            list.add(row.get(j));
                        }
                    }
                }

                else
                    throw new IllegalArgumentException("Illegal dimension for matrix " + dimension);


                return new ComplexNDArray(list.toArray(new ComplexDouble[0]),targetShape);

            }


            List<ComplexNDArray> slices = new ArrayList<>();
            for(int i = 0; i < n; i++) {
                ComplexNDArray slice = nd.slice(i);
                slices.add(slice);
            }

            return new ComplexNDArray(slices,targetShape);



        }

        return nd;

    }


    /**
     * Pads an ndarray with zeros
     * @param nd the ndarray to pad
     * @param targetShape the the new shape
     * @return the padded ndarray
     */
    public static ComplexNDArray padWithZeros(ComplexNDArray nd,int[] targetShape) {
        if(Arrays.equals(nd.shape(),targetShape))
            return nd;
        //no padding required
        if(ArrayUtil.prod(nd.shape()) >= ArrayUtil.prod(targetShape))
            return nd;

        ComplexNDArray ret = new ComplexNDArray(targetShape);
        System.arraycopy(nd.data,0,ret.data,0,nd.data.length);
        return ret;

    }



    private static boolean isRowOp(MatrixOp op) {
        return
                op == MatrixOp.ROW_MIN ||
                        op == MatrixOp.ROW_MAX ||
                        op == MatrixOp.ROW_SUM ||
                        op == MatrixOp.ROW_MEAN;
    }


    private static boolean isColumnOp(MatrixOp op) {
        return
                op == MatrixOp.COLUMN_MIN ||
                        op == MatrixOp.COLUMN_MAX ||
                        op == MatrixOp.COLUMN_SUM ||
                        op == MatrixOp.COLUMN_MEAN;
    }


    /**
     * Dimension wise operation along an ndarray
     * @param op the op to do
     * @param arr the array  to do operations on
     * @param dimension the dimension to do operations on
     * @return the
     */
    public static ComplexNDArray dimensionOp(DimensionOp op,ComplexNDArray arr,int dimension) {
        if(dimension >= arr.slices())
            return arr;

        int[] shape = ArrayUtil.removeIndex(arr.shape(),dimension);


        List<ComplexNDArray> list = new ArrayList<>();

        for(int i = 0; i < arr.slices(); i++) {
            switch(op) {
                case SUM:
                    //list.add(arr.slice(i,dimension).sum());
                    break;
                case MEAN:
                    list.add(arr.slice(i,dimension).columnSums());
                    break;
                case PROD:
                    list.add(arr.slice(i,dimension).columnMeans());
                    break;
            }
        }


        return arr;
    }


    public static double[] collectForOp(ComplexNDArray from,int dimension) {
        //number of operations per op
        int num = from.shape()[dimension];

        //how to isolate blocks from the matrix
        double[] d = new double[num];
        int idx = 0;
        for(int k = 0; k < d.length; k++) {
            d[k] = from.data[idx];
            idx += num;
        }


        return d;
    }


    /**
     * Does slice wise ops on matrices and
     * returns the aggregate results in one matrix
     *
     * @param op the operation to perform
     * @param arr the array  to do operations on
     * @return the slice wise operations
     */
    public static ComplexNDArray doSliceWise(MatrixOp op,ComplexNDArray arr) {
        int columns = isColumnOp(op) ? arr.columns() : arr.rows();
        int[] shape = {arr.slices(),columns};

        ComplexNDArray ret = new ComplexNDArray(shape);

        for(int i = 0; i < arr.slices(); i++) {
            switch(op) {
                case COLUMN_SUM:
                    ret.putSlice(i,arr.slice(i).columnSums());
                    break;
                case COLUMN_MEAN:
                    ret.putSlice(i,arr.slice(i).columnMeans());
                    break;
                case ROW_SUM:
                    ret.putSlice(i,arr.slice(i).rowSums());
                    break;
                case ROW_MEAN:
                    ret.putSlice(i,arr.slice(i).rowMeans());
                    break;
            }
        }


        return ret;
    }






    public static ComplexDouble doSliceWise(ScalarOp op,ComplexNDArray arr) {
        if(arr.isScalar())
            return arr.get(0);

        else {
            ComplexDouble ret = new ComplexDouble(0);

            for(int i = 0; i < arr.slices(); i++) {
                switch(op) {
                    case MEAN:
                        ret.addi(arr.slice(i).mean());
                        break;
                    case SUM :
                        ret.addi(arr.slice(i).sum());
                        break;
                    case NORM_1:
                        ret.addi(arr.slice(i).norm1());
                        break;
                    case NORM_2:
                        ret.addi(arr.slice(i).norm2());
                        break;
                    case NORM_MAX:
                        ret.addi(arr.slice(i).normmax());
                        break;


                }
            }

            return ret;
        }

    }

}