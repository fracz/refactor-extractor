package org.deeplearning4j.nn.linalg;

import org.deeplearning4j.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates all shape related logic (vector of 0 dimension is a scalar is equivalent to
 *                                       a vector of length 1...)
 */
public class Shape {


    /**
     * Gets rid of any singleton dimensions of the given array
     * @param shape the shape to squeeze
     * @return the array with all of the singleton dimensions removed
     */
    public static int[] squeeze(int[] shape) {
        List<Integer> ret = new ArrayList<>();

        for(int i = 0; i < shape.length; i++)
            if(shape[i] != 1)
                ret.add(shape[i]);
        return ArrayUtil.toArray(ret);
    }


    /**
     * Returns whether 2 shapes are equals by checking for dimension semantics
     * as well as array equality
     * @param shape1 the first shape for comparison
     * @param shape2 the second shape for comparison
     * @return whether the shapes are equivalent
     */
    public static boolean shapeEquals(int[] shape1,int[] shape2) {
        return scalarEquals(shape1,shape2) || Arrays.equals(shape1,shape2);
    }

    /**
     * Returns true if the given shapes are both scalars (0 dimension or shape[0] == 1)
     * @param shape1 the first shape for comparison
     * @param shape2 the second shape for comparison
     * @return whether the 2 shapes are equal based on scalar rules
     */
    public static boolean scalarEquals(int[] shape1,int[] shape2) {
        if(shape1.length == 0) {
            if(shape2.length == 1 && shape2[0] == 1)
                return true;
        }

        else if(shape2.length == 0) {
            if(shape1.length == 1 && shape1[0] == 1)
                return true;
        }

        return false;
    }


}