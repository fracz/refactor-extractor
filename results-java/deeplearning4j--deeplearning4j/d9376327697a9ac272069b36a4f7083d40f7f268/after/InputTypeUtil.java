package org.deeplearning4j.nn.conf.layers;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.exception.DL4JInvalidConfigException;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.preprocessor.CnnToRnnPreProcessor;
import org.deeplearning4j.nn.conf.preprocessor.FeedForwardToCnnPreProcessor;
import org.deeplearning4j.nn.conf.preprocessor.FeedForwardToRnnPreProcessor;

import java.util.Arrays;

/**
 * Utilities for calculating input types
 *
 * @author Alex Black
 */
@Slf4j
public class InputTypeUtil {


    public static InputType getOutputTypeCnnLayers(InputType inputType, int[] kernelSize, int[] stride, int[] padding,
                                                   int outputDepth, int layerIdx, String layerName, Class<?> layerClass) {

        InputType.InputTypeConvolutional i = (InputType.InputTypeConvolutional) inputType;
        int inHeight = i.getHeight();
        int inWidth = i.getWidth();
        int padH = padding[0];
        int padW = padding[1];
        int kH = kernelSize[0];
        int kW = kernelSize[1];
        int sH = stride[0];
        int sW = stride[1];

        if(sH <= 0 || sW <= 0){
            throw new DL4JInvalidConfigException(
                    getConfigErrorCommonLine1(layerIdx, layerName, layerClass, sH <= 0 ) +
                    " Invalid strides: strides must be > 0 (strideH = " + sH + ", strideW = " + sW + ")"
                    + "\n" + getConfigErrorCommonLastLine(inputType, kernelSize, stride, padding, outputDepth));
        }

        if( kH <= 0 || kH > inHeight + 2*padH){
            throw new DL4JInvalidConfigException(
                    getConfigErrorCommonLine1(layerIdx, layerName, layerClass, true )
                    + " Invalid input configuration for kernel height. Require 0 < kH <= inHeight + 2*padH; got (kH="
                    + kH + ", inHeight=" + inHeight + ", padH=" + padH + ")\n"
                    + getConfigErrorCommonLastLine(inputType, kernelSize, stride, padding, outputDepth));
        }

        if( kW <= 0 || kW > inWidth + 2*padW){
            throw new DL4JInvalidConfigException(
                    getConfigErrorCommonLine1(layerIdx, layerName, layerClass, false )
                            + " Invalid input configuration for kernel width. Require 0 < kW <= inWidth + 2*padW; got (kW="
                            + kW + ", inWidth=" + inWidth + ", padW=" + padW + ")\n"
                            + getConfigErrorCommonLastLine(inputType, kernelSize, stride, padding, outputDepth));
        }

        if ((inHeight - kH + 2 * padH) % sH != 0) {
            double d = (inHeight - kH + 2 * padH) / ((double)sH) + 1.0;
            String str = String.format("%.2f",d);
            throw new DL4JInvalidConfigException(
                    getConfigErrorCommonLine1(layerIdx, layerName, layerClass, true )
                    + "\nCombination of kernel size, stride and padding are not valid for given input height.\n"
                    + "Require: (input - kernelSize + 2*padding)/stride + 1 in height dimension to be an integer. Got: ("
                            + inHeight + " - " + kH + " + 2*" + padH + ")/" + sH + " + 1 = " + str + "\n"
                    + "See \"Constraints on strides\" at http://cs231n.github.io/convolutional-networks/\n"
                    + getConfigErrorCommonLastLine(inputType, kernelSize, stride, padding, outputDepth));
        }


        if ((inWidth - kW + 2 * padW) % sW != 0) {
            double d = (inWidth - kW + 2 * padW) / ((double)sW) + 1.0;
            String str = String.format("%.2f",d);
            throw new DL4JInvalidConfigException(
                    getConfigErrorCommonLine1(layerIdx, layerName, layerClass, false )
                            + "\nCombination of kernel size, stride and padding are not valid for given input width.\n"
                            + "Require: (input - kernelSize + 2*padding)/stride + 1 in width dimension to be an integer. Got: ("
                            + inWidth + " - " + kW + " + 2*" + padW + ")/" + sW + " + 1 = " + str + "\n"
                            + "See \"Constraints on strides\" at http://cs231n.github.io/convolutional-networks/\n"
                            + getConfigErrorCommonLastLine(inputType, kernelSize, stride, padding, outputDepth));
        }

        int hOut = (inHeight - kH + 2 * padH) / sH + 1;
        int wOut = (inWidth - kW + 2 * padW) / sW + 1;
        return InputType.convolutional(hOut, wOut, outputDepth);
    }

    private static String getConfigErrorCommonLine1(int layerIdx, String layerName, Class<?> layerClass, boolean isHeight){
        String name = layerName == null ? "(not named)" : layerName;
        String layerType = layerClass.getSimpleName();

        return "Invalid configuration for layer (idx=" + layerIdx + ", name=" + name + ", type=" + layerType
                + ") for " + (isHeight ? "height" : "width") + " dimension: ";
    }

    private static String getConfigErrorCommonLastLine(InputType inputType, int[] kernelSize, int[] stride, int[] padding, int outputDepth){
        return "Input type = " + inputType + ", kernel = " + Arrays.toString(kernelSize) + ", strides = " + Arrays.toString(stride)
                + ", padding = " + Arrays.toString(padding) + ", layer size (output depth) = " + outputDepth;
    }

    /**
     * Utility method for determining the appropriate preprocessor for CNN layers, such as {@link ConvolutionLayer} and
     * {@link SubsamplingLayer}
     *
     * @param inputType     Input type to get the preprocessor for
     * @return              Null if no preprocessor is required; otherwise the appropriate preprocessor for the given input type
     */
    public static InputPreProcessor getPreProcessorForInputTypeCnnLayers(InputType inputType, String layerName){

        //To add x-to-CNN preprocessor: need to know image depth/width/height after reshaping
        //But this can't be inferred from the FF/RNN activations directly (could be anything)

        switch (inputType.getType()){
            case FF:
                //FF -> CNN
//                return new FeedForwardToCnnPreProcessor(inputSize[0], inputSize[1], inputDepth);
                log.info("Automatic addition of FF -> CNN preprocessors: not yet implemented (layer name: \"" + layerName + "\")");
                return null;
            case RNN:
                //RNN -> CNN
//                return new RnnToCnnPreProcessor(inputSize[0], inputSize[1], inputDepth);
                log.warn("Automatic addition of RNN -> CNN preprocessors: not yet implemented (layer name: \"" + layerName + "\")");
                return null;
            case CNN:
                //CNN -> CNN: no preprocessor required
                return null;
            case CNNFlat:
                //CNN (flat) -> CNN
                InputType.InputTypeConvolutionalFlat f = (InputType.InputTypeConvolutionalFlat)inputType;
                return new FeedForwardToCnnPreProcessor(f.getHeight(), f.getWidth(), f.getDepth());
            default:
                throw new RuntimeException("Unknown input type: " + inputType);
        }
    }

    public static InputPreProcessor getPreprocessorForInputTypeRnnLayers(InputType inputType, String layerName){
        if (inputType == null) {
            throw new IllegalStateException("Invalid input for RNN layer (layer name = \"" + layerName + "\"): input type is null");
        }

        switch (inputType.getType()) {
            case FF:
            case CNNFlat:
                //FF -> RNN or CNNFlat -> RNN
                //In either case, input data format is a row vector per example
                return new FeedForwardToRnnPreProcessor();
            case RNN:
                //RNN -> RNN: No preprocessor necessary
                return null;
            case CNN:
                //CNN -> RNN
                InputType.InputTypeConvolutional c = (InputType.InputTypeConvolutional)inputType;
                return new CnnToRnnPreProcessor(c.getHeight(),c.getWidth(),c.getDepth());
            default:
                throw new RuntimeException("Unknown input type: " + inputType);
        }
    }

}