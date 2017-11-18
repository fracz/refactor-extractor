package org.deeplearning4j.nn.conf.preprocessor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Arrays;

/**A preprocessor to allow CNN and RNN layers to be used together.<br>
 * For example, ConvolutionLayer -> GravesLSTM
 * Functionally equivalent to combining CnnToFeedForwardPreProcessor + FeedForwardToRnnPreProcessor<br>
 * Specifically, this does two things:<br>
 * (a) Reshape 4d activations out of CNN layer, with shape [timeSeriesLength*miniBatchSize, numChannels, inputHeight, inputWidth])
 * into 3d (time series) activations (with shape [numExamples, inputHeight*inputWidth*numChannels, timeSeriesLength])
 * for use in RNN layers<br>
 * (b) Reshapes 3d epsilons (weights.*deltas) out of RNN layer (with shape
 * [miniBatchSize,inputHeight*inputWidth*numChannels,timeSeriesLength]) into 4d epsilons with shape
 * [miniBatchSize*timeSeriesLength, numChannels, inputHeight, inputWidth] suitable to feed into CNN layers.
 * Note: numChannels is equivalent to depth or featureMaps referenced in different literature
 * @author Alex Black
 */
@Data
public class CnnToRnnPreProcessor implements InputPreProcessor {
    private int inputHeight;
    private int inputWidth;
    private int numChannels;
    private int product;

    @JsonCreator
    public CnnToRnnPreProcessor(@JsonProperty("inputHeight") int inputHeight,
                                @JsonProperty("inputWidth") int inputWidth,
                                @JsonProperty("numChannels") int numChannels) {
        this.inputHeight = inputHeight;
        this.inputWidth = inputWidth;
        this.numChannels = numChannels;
        this.product = inputHeight*inputWidth*numChannels;
    }

    @Override
    public INDArray preProcess(INDArray input, int miniBatchSize) {
        if(input.rank() != 4) throw new IllegalArgumentException("Invalid input: expect CNN activations with rank 4 (received input with shape "
            + Arrays.toString(input.shape())+")");
        //Input: 4d activations (CNN)
        //Output: 3d activations (RNN)

        int[] shape = input.shape();    //[timeSeriesLength*miniBatchSize, numChannels, inputHeight, inputWidth]
        INDArray reshaped = input.reshape(miniBatchSize,shape[0]/miniBatchSize,product);
        return reshaped.permute(0,2,1);
    }

    @Override
    public INDArray backprop(INDArray output, int miniBatchSize) {
        int[] shape = output.shape();
        INDArray output2d;
        if(shape[0]==1){
            //Edge case: miniBatchSize = 1
            output2d = output.tensorAlongDimension(0,1,2);
        } else if(shape[2]==1){
            //Edge case: timeSeriesLength = 1
            output2d = output.tensorAlongDimension(0,1,0);
        } else {
            INDArray permuted3d = output.permute(0, 2, 1);	//Permute, so we get correct order after reshaping
            output2d = permuted3d.reshape(shape[0]*shape[2],shape[1]);    //normally what would be returned for FF layers
        }

        if(shape[1] != product)
            throw new IllegalArgumentException("Invalid input: expected output size(1)="+shape[1]+" must be equal to "
                + inputHeight + " x columns " + inputWidth + " x depth " + numChannels +" = " + product + ", received: " + shape[1]);
        //INDArray output2d = permuted3d.reshape(shape[0]*shape[2],shape[1]);    //normally what would be returned for FF layers
        return output2d.reshape(output2d.size(0), numChannels, inputHeight, inputWidth);
    }

    @Override
    public CnnToRnnPreProcessor clone() {
        return new CnnToRnnPreProcessor(inputHeight,inputWidth,numChannels);
    }
}