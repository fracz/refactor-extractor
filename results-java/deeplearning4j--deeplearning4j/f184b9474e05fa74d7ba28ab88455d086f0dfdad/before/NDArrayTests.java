package org.deeplearning4j.linalg.jblas;


import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.factory.NDArrays;
import org.jblas.DoubleMatrix;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * NDArrayTests
 * @author Adam Gibson
 */
public class NDArrayTests extends org.deeplearning4j.linalg.api.test.NDArrayTests {
    private static Logger log = LoggerFactory.getLogger(NDArrayTests.class);


    @Test
    public void testMatrixVector() {
        double[][] data = new double[][]{
                {1,2,3,4},
                {5,6,7,8}
        };


        NDArrays.factory().setOrder('f');
        double[] mmul = {1,2,3,4};

        DoubleMatrix d = new DoubleMatrix(data);
        INDArray d2 = NDArrays.create(data);
        assertEquals(d.rows,d2.rows());
        assertEquals(d.columns,d2.columns());
        verifyElements(d,d2);

        INDArray toMmulD2 = NDArrays.create(mmul).transpose();
        DoubleMatrix toMmulD = new DoubleMatrix(mmul);


        assertEquals(d.rows,d2.rows());
        assertEquals(d.columns,d2.columns());

        assertEquals(toMmulD.rows,toMmulD2.rows());
        assertEquals(toMmulD.columns,toMmulD2.columns());

        DoubleMatrix mmulResultD = d.mmul(toMmulD);
        INDArray mmulResultD2 = d2.mmul(toMmulD2);

        verifyElements(mmulResultD,mmulResultD2);





        NDArrays.factory().setOrder('c');


    }



    @Test
    public void testFortranRavel() {
        double[][] data = new double[][]{
                {1,2,3,4},
                {5,6,7,8}
        };

        INDArray toRavel = NDArrays.create(data);
        NDArrays.factory().setOrder('f');
        INDArray toRavelF = NDArrays.create(data);
        INDArray ravel = toRavel.ravel();
        INDArray ravelF = toRavelF.ravel();
        assertNotEquals(ravel,ravelF);
        NDArrays.factory().setOrder('c');

    }





    @Test
    public void testFortranReshapeMatrix() {
        double[][] data = new double[][]{
                {1,2,3,4},
                {5,6,7,8}
        };

        NDArrays.factory().setOrder('f');

        DoubleMatrix d = new DoubleMatrix(data);
        INDArray d2 = NDArrays.create(data);
        assertEquals(d.rows, d2.rows());
        assertEquals(d.columns, d2.columns());
        verifyElements(d, d2);


        DoubleMatrix reshapedD = d.reshape(4,2);
        INDArray reshapedD2 = d2.reshape(4,2);
        verifyElements(reshapedD,reshapedD2);
        NDArrays.factory().setOrder('c');


    }






    @Test
    public void testFortranCreation() {
        double[][] data = new double[][]{
                {1,2,3,4},
                {5,6,7,8}
        };


        NDArrays.factory().setOrder('f');
        double[][] mmul = {{1,2,3,4},{5,6,7,8}};

        INDArray d2 = NDArrays.create(data);
        verifyElements(mmul,d2);
    }


    @Test
    public void testMatrixMatrix() {
        double[][] data = new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8}
        };


        NDArrays.factory().setOrder('f');
        double[][] mmul = {{1, 2, 3, 4}, {5, 6, 7, 8}};

        DoubleMatrix d = new DoubleMatrix(data).reshape(4, 2);
        INDArray d2 = NDArrays.create(data).reshape(4, 2);
        assertEquals(d.rows, d2.rows());
        assertEquals(d.columns, d2.columns());
        verifyElements(d, d2);

        INDArray toMmulD2 = NDArrays.create(mmul);
        DoubleMatrix toMmulD = new DoubleMatrix(mmul);

        DoubleMatrix mmulResultD = d.mmul(toMmulD);
        INDArray mmulResultD2 = d2.mmul(toMmulD2);
        verifyElements(mmulResultD, mmulResultD2);


        NDArrays.factory().setOrder('c');
    }

    @Test
    public void testVectorVector() {
        DoubleMatrix d = new DoubleMatrix(2,1);
        d.data = new double[]{1,2};
        DoubleMatrix d2 = new DoubleMatrix(1,2);
        d2.data = new double[]{3,4};

        INDArray d3 = NDArrays.create(new double[]{1,2}).reshape(2,1);
        INDArray d4 = NDArrays.create(new double[]{3,4});

        assertEquals(d.rows,d3.rows());
        assertEquals(d.columns,d3.columns());

        assertEquals(d2.rows,d4.rows());
        assertEquals(d2.columns,d4.columns());

        DoubleMatrix resultMatrix = d.mmul(d2);



        INDArray resultNDArray = d3.mmul(d4);
        verifyElements(resultMatrix,resultNDArray);

    }


    @Test
    public void testVector() {
        NDArrays.factory().setOrder('f');

        DoubleMatrix dJblas = DoubleMatrix.linspace(1,4,4);
        INDArray d = NDArrays.linspace(1,4,4);
        verifyElements(dJblas,d);
        NDArrays.factory().setOrder('c');


    }


    @Test
    public void testReshapeCompatibility() {
        NDArrays.factory().setOrder('f');
        DoubleMatrix oneThroughFourJblas = DoubleMatrix.linspace(1,4,4).reshape(2,2);
        DoubleMatrix fiveThroughEightJblas = DoubleMatrix.linspace(5,8,4).reshape(2,2);
        INDArray oneThroughFour = NDArrays.linspace(1,4,4).reshape(2,2);
        INDArray fiveThroughEight = NDArrays.linspace(5,8,4).reshape(2,2);
        verifyElements(oneThroughFourJblas,oneThroughFour);
        verifyElements(fiveThroughEightJblas,fiveThroughEight);
        NDArrays.factory().setOrder('c');

    }





    protected void verifyElements(double[][] d,INDArray d2) {
        for(int i = 0; i < d2.rows(); i++) {
            for(int j = 0; j < d2.columns(); j++) {
                double test1 =  d[i][j];
                double test2 = (double) d2.getScalar(i,j).element();
                assertEquals(test1,test2,1e-6);
            }
        }
    }


    protected void verifyElements(DoubleMatrix d,INDArray d2) {
        for(int i = 0; i < d.rows; i++) {
            for(int j = 0; j < d.columns; j++) {
                double test1 = d.get(i,j);
                double test2 = (double) d2.getScalar(i,j).element();
                assertEquals(test1,test2,1e-6);
            }
        }
    }

}