package org.deeplearning4j.util;

import java.lang.reflect.Array;
import java.util.List;

public class ArrayUtil {




    public static int prod(int[] mult) {
        int ret = 1;
        for(int i = 0; i < mult.length; i++)
            ret *= mult[i];
        return ret;
    }


    public static int[] consArray(int a, int[] as) {
        int len=as.length;
        int[] nas=new int[len+1];
        nas[0]=a;
        System.arraycopy(as, 0, nas, 1, len);
        return nas;
    }


    public static boolean isZero(int[] as) {
        for (int i=0; i<as.length; i++) {
            if (as[i]!=0) return false;
        }
        return true;
    }

    public static boolean anyMore(int[] target,int[] test) {
        assert target.length == test.length : "Unable to compare: different sizes";
        for(int i = 0; i < target.length; i++) {
            if(target[i] > test[i])
                return true;
        }
        return false;
    }


    public static boolean anyLess(int[] target,int[] test) {
        assert target.length == test.length : "Unable to compare: different sizes";
        for(int i = 0; i < target.length; i++) {
            if(target[i] < test[i])
                return true;
        }
        return false;
    }

    public static int dotProduct(int[] xs, int[] ys) {
        int result = 0;
        int n = xs.length;

        if (ys.length != n )
            throw new IllegalArgumentException("Different array sizes");

        for (int i=0; i < n; i++) {
            result += xs[i] * ys[i];
        }
        return result;
    }


    public static int[] empty() {
        return new int[0];
    }


    public static int[] of(int...arr) {
        return arr;
    }

    public static int[] copy(int[] copy) {
        int[] ret = new int[copy.length];
        System.arraycopy(copy,0,ret,0,ret.length);
        return ret;
    }


    /**
     * Returns a subset of an array from 0 to "to"
     * @param data the data to get a subset of
     * @param to the end point of the data
     * @return the subset of the data specified
     */
    public static double[] range(double[] data,int to) {
       return range(data,to,1);
    }



    /**
     * Returns a subset of an array from 0 to "to"
     * using the specified stride
     * @param data the data to get a subset of
     * @param to the end point of the data
     * @param stride the stride to go through the array
     * @return the subset of the data specified
     */
    public static double[] range(double[] data,int to,int stride) {
        return range(data,to,stride,1);
    }


    /**
     * Returns a subset of an array from 0 to "to"
     * using the specified stride
     * @param data the data to get a subset of
     * @param to the end point of the data
     * @param stride the stride to go through the array
     * @param numElementsEachStride the number of elements to collect at each stride
     * @return the subset of the data specified
     */
    public static double[] range(double[] data,int to,int stride,int numElementsEachStride) {
        double[] ret = new double[to];
        int count = 0;
        for(int i = 0; i < data.length; i+= stride) {
           for(int j = 0; j < numElementsEachStride; j++) {
               if(i + j >= data.length || count >= ret.length)
                   break;
               ret[count++] = data[i + j];
           }
        }
        return ret;
    }



    public static int[] toArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        for(int i = 0; i < list.size(); i++)
            ret[i] = list.get(i);
        return ret;
    }



    public static double[] toArrayDouble(List<Double> list) {
        double[] ret = new double[list.size()];
        for(int i = 0; i < list.size(); i++)
            ret[i] = list.get(i);
        return ret;

    }



    /**
     * Return a copy of this array with the
     * given index omitted
     * @param data the data to copy
     * @param index the index of the item to remove
     * @param newValue the newValue to replace
     * @return the new array with the omitted
     * item
     */
    public static int[] replace(int[] data, int index,int newValue) {
        int[] copy = copy(data);
        copy[index] = newValue;
        return copy;
    }

    /**
     * Return a copy of this array with the
     * given index omitted
     * @param data the data to copy
     * @param index the index of the item to remove
     * @return the new array with the omitted
     * item
     */
    public static int[] removeIndex(int[] data, int index) {
        if(index >= data.length)
            throw new IllegalArgumentException("Unable to remove index " + index + " was >= data.length");

        if(data == null)
            return null;
        if(data.length < 1)
            return data;
        if(index < 0)
            return data;

        int len = data.length;
        int[] result = new int[len - 1];
        System.arraycopy(data, 0, result, 0, index);
        System.arraycopy(data, index+1, result, index, len - index - 1);
        return result;
    }


    /**
     * Returns the array with the item in index
     * removed, if the array is empty it will return the array itself
     * @param data the data to remove data from
     * @param index the index of the item to remove
     * @return a copy of the array with the removed item,
     * or the array itself if empty
     */
    public static Integer[] removeIndex(Integer[] data, int index) {
        if(data == null)
            return null;
        if(data.length < 1)
            return data;
        int len = data.length;
        Integer[] result = new Integer[len - 1];
        System.arraycopy(data, 0, result, 0, index);
        System.arraycopy(data, index+1, result, index, len-index - 1);
        return result;
    }

    /**
     * Computes the standard packed array strides for a given shape.
     * @param shape the shape of a matrix:
     * @return the strides for a matrix of n dimensions
     */
    public static  int[] calcStrides(int[] shape) {
        int dimensions = shape.length;
        int[] stride = new int[dimensions];
        int st= 1;
        for (int j = dimensions - 1; j >= 0; j--) {
            stride[j] = st;
            st *= shape[j];
        }

        return stride;
    }



    public static  int[] reverseCopy(int[] e) {
        int[] copy = new int[e.length];
        for(int i = 0; i <= e.length / 2; i++)
        {
            int temp = e[i];
            copy[i] = e[e.length - i - 1];
            copy[e.length - i - 1] = temp;
        }
        return copy;
    }


    public static  void reverse(int[] e) {
        for(int i = 0; i <= e.length / 2; i++)
        {
            int temp = e[i];
            e[i] = e[e.length - i - 1];
            e[e.length - i - 1] = temp;
        }

    }


    public static <E> E[] reverseCopy(E[] e) {
        E[] copy = (E[]) new Object[e.length];
        for(int i = 0; i <= e.length / 2; i++)
        {
            E temp = e[i];
            copy[i] = e[e.length - i - 1];
            copy[e.length - i - 1] = temp;
        }
        return copy;

    }

    public static <E> void reverse(E[] e) {
        for(int i = 0; i <= e.length / 2; i++)
        {
            E temp = e[i];
            e[i] = e[e.length - i - 1];
            e[e.length - i - 1] = temp;
        }

    }


    public static int[] flatten(int[][] arr) {
        int[] ret = new int[arr.length * arr[0].length];
        int count = 0;
        for(int i = 0; i < arr.length; i++)
            for(int j = 0; j < arr[i].length; j++)
                ret[count++] = arr[i][j];
        return ret;
    }

    public static double[] flatten(double[][] arr) {
        double[] ret = new double[arr.length * arr[0].length];
        int count = 0;
        for(int i = 0; i < arr.length; i++)
            for(int j = 0; j < arr[i].length; j++)
                ret[count++] = arr[i][j];
        return ret;
    }

    public static double[][] toDouble(int[][] arr) {
        double[][] ret = new double[arr.length][arr[0].length];
        for(int i = 0; i < arr.length; i++) {
            for(int j = 0; j < arr[i].length; j++)
                ret[i][j] = arr[i][j];
        }
        return ret;
    }


    /**
     * Combines a set of int arrays in to one flat int array
     * @param nums the int arrays to combine
     * @return one combined int array
     */
    public static float[] combineFloat(List<float[]> nums) {
        int length = 0;
        for(int i = 0; i < nums.size(); i++)
            length += nums.get(i).length;
        float[] ret = new float[length];
        int count = 0;
        for(float[] i : nums) {
            for(int j = 0; j < i.length; j++) {
                ret[count++] = i[j];
            }
        }

        return ret;
    }


    /**
     * Combines a set of int arrays in to one flat int array
     * @param nums the int arrays to combine
     * @return one combined int array
     */
    public static double[] combine(List<double[]> nums) {
        int length = 0;
        for(int i = 0; i < nums.size(); i++)
            length += nums.get(i).length;
        double[] ret = new double[length];
        int count = 0;
        for(double[] i : nums) {
            for(int j = 0; j < i.length; j++) {
                ret[count++] = i[j];
            }
        }

        return ret;
    }

    /**
     * Combines a set of int arrays in to one flat int array
     * @param ints the int arrays to combine
     * @return one combined int array
     */
    public static double[] combine(double[]...ints) {
        int length = 0;
        for(int i = 0; i < ints.length; i++)
            length += ints[i].length;
        double[] ret = new double[length];
        int count = 0;
        for(double[] i : ints) {
            for(int j = 0; j < i.length; j++) {
                ret[count++] = i[j];
            }
        }

        return ret;
    }

    /**
     * Combines a set of int arrays in to one flat int array
     * @param ints the int arrays to combine
     * @return one combined int array
     */
    public static int[] combine(int[]...ints) {
        int length = 0;
        for(int i = 0; i < ints.length; i++)
            length += ints[i].length;
        int[] ret = new int[length];
        int count = 0;
        for(int[] i : ints) {
            for(int j = 0; j < i.length; j++) {
                ret[count++] = i[j];
            }
        }

        return ret;
    }

    public static <E> E[] combine(E[]...arrs) {
        int length = 0;
        for(int i = 0; i < arrs.length; i++)
            length += arrs[i].length;

        E[] ret =  (E[]) Array.newInstance(arrs[0][0].getClass(), length);
        int count = 0;
        for(E[] i : arrs) {
            for(int j = 0; j < i.length; j++) {
                ret[count++] = i[j];
            }
        }

        return ret;
    }


    public static int[] toOutcomeArray(int outcome,int numOutcomes) {
        int[] nums = new int[numOutcomes];
        nums[outcome] = 1;
        return nums;
    }

}