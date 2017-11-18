package org.deeplearning4j.exceptions;

import org.deeplearning4j.exception.DL4JException;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * A set of tests to ensure that useful exceptions are thrown on invalid network configurations
 */
public class TestInvalidConfigurations {

    public static MultiLayerNetwork getDensePlusOutput(int nIn, int nOut) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .list()
                .layer(0, new DenseLayer.Builder().nIn(nIn).nOut(10).build())
                .layer(1, new OutputLayer.Builder().nIn(10).nOut(nOut).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }

    public static MultiLayerNetwork getLSTMPlusRnnOutput(int nIn, int nOut) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .list()
                .layer(0, new GravesLSTM.Builder().nIn(nIn).nOut(10).build())
                .layer(1, new RnnOutputLayer.Builder().nIn(10).nOut(nOut).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }

    public static MultiLayerNetwork getCnnPlusOutputLayer(int depthIn, int inH, int inW, int nOut) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .list()
                .layer(0, new ConvolutionLayer.Builder().nIn(depthIn).nOut(5).build())
                .layer(1, new OutputLayer.Builder().nOut(nOut).build())
                .setInputType(InputType.convolutional(inH, inW, depthIn))
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }

    @Test
    public void testDenseNin0() {
        try {
            getDensePlusOutput(0, 10);
        } catch (DL4JException e) {
            System.out.println("testDenseNin0(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDenseNout0() {
        try {
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .list()
                    .layer(0, new DenseLayer.Builder().nIn(10).nOut(0).build())
                    .layer(1, new OutputLayer.Builder().nIn(10).nOut(10).build())
                    .build();

            MultiLayerNetwork net = new MultiLayerNetwork(conf);
            net.init();
        } catch (DL4JException e) {
            System.out.println("testDenseNout0(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testOutputLayerNin0() {
        try {
            getDensePlusOutput(10, 0);
        } catch (DL4JException e) {
            System.out.println("testOutputLayerNin0(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRnnOutputLayerNin0() {
        try {
            getLSTMPlusRnnOutput(10, 0);
        } catch (DL4JException e) {
            System.out.println("testRnnOutputLayerNin0(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testLSTMNIn0() {
        try {
            getLSTMPlusRnnOutput(0, 10);
        } catch (DL4JException e) {
            System.out.println("testLSTMNIn0(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testLSTMNOut0() {
        try {
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .list()
                    .layer(0, new GravesLSTM.Builder().nIn(10).nOut(0).build())
                    .layer(1, new RnnOutputLayer.Builder().nIn(10).nOut(10).build())
                    .build();

            MultiLayerNetwork net = new MultiLayerNetwork(conf);
            net.init();
        } catch (DL4JException e) {
            System.out.println("testLSTMNOut0(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testConvolutionalNin0() {
        try {
            getCnnPlusOutputLayer(0, 10, 10, 10);
        } catch (DL4JException e) {
            System.out.println("testConvolutionalNin0(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testConvolutionalNOut0() {
        try {
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .list()
                    .layer(0, new ConvolutionLayer.Builder().nIn(5).nOut(0).build())
                    .layer(1, new OutputLayer.Builder().nOut(10).build())
                    .setInputType(InputType.convolutional(10, 10, 5))
                    .build();

            MultiLayerNetwork net = new MultiLayerNetwork(conf);
            net.init();
        } catch (DL4JException e) {
            System.out.println("testConvolutionalNOut0(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


    @Test
    public void testCnnInvalidConfigPaddingStrides() {
        //Idea: some combination of padding/strides are invalid.

        int depthIn = 3;
        int hIn = 10;
        int wIn = 10;

        //Using kernel size of 3, stride of 2:
        //(10-3+2*0)/2+1 = 7/2 + 1

        try {
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .list()
                    .layer(0, new ConvolutionLayer.Builder().kernelSize(3,3).stride(2,2).padding(0,0).nOut(5).build())
                    .layer(1, new OutputLayer.Builder().nOut(10).build())
                    .setInputType(InputType.convolutional(hIn, wIn, depthIn))
                    .build();

            MultiLayerNetwork net = new MultiLayerNetwork(conf);
            net.init();
        } catch (DL4JException e) {
//            e.printStackTrace();
            System.out.println("testCnnInvalidConfigPaddingStrides(): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}