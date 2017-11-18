package org.deeplearning4j.nn.modelimport.keras.layers.convolutional;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.Convolution1DLayer;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.KerasLayer;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.params.ConvolutionParamInitializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Imports a 1D Convolution layer from Keras.
 *
 * @author Max Pumperla
 */
@Slf4j
@Data
public class KerasConvolution1D extends KerasConvolution {

    /**
     * Pass-through constructor from KerasLayer
     * @param kerasVersion major keras version
     * @throws UnsupportedKerasConfigurationException
     */
    public KerasConvolution1D(Integer kerasVersion) throws UnsupportedKerasConfigurationException {
        super(kerasVersion);
    }

    /**
     * Constructor from parsed Keras layer configuration dictionary.
     *
     * @param layerConfig       dictionary containing Keras layer configuration
     * @throws InvalidKerasConfigurationException
     * @throws UnsupportedKerasConfigurationException
     */
    public KerasConvolution1D(Map<String, Object> layerConfig)
            throws InvalidKerasConfigurationException, UnsupportedKerasConfigurationException {
        this(layerConfig, true);
    }

    /**
     * Constructor from parsed Keras layer configuration dictionary.
     *
     * @param layerConfig               dictionary containing Keras layer configuration
     * @param enforceTrainingConfig     whether to enforce training-related configuration options
     * @throws InvalidKerasConfigurationException
     * @throws UnsupportedKerasConfigurationException
     */
    public KerasConvolution1D(Map<String, Object> layerConfig, boolean enforceTrainingConfig)
            throws InvalidKerasConfigurationException, UnsupportedKerasConfigurationException {
        super(layerConfig, enforceTrainingConfig);
        hasBias = getHasBiasFromConfig(layerConfig);
        numTrainableParams = hasBias ? 2 : 1;

        Convolution1DLayer.Builder builder = new Convolution1DLayer.Builder().name(this.layerName)
                .nOut(getNOutFromConfig(layerConfig)).dropOut(this.dropout)
                .activation(getActivationFromConfig(layerConfig))
                .weightInit(getWeightInitFromConfig(
                        layerConfig, conf.getLAYER_FIELD_INIT(), enforceTrainingConfig))
                .biasInit(0.0)
                .l1(this.weightL1Regularization).l2(this.weightL2Regularization)
                .convolutionMode(getConvolutionModeFromConfig(layerConfig))
                .kernelSize(getKernelSizeFromConfig(layerConfig, 1)[0])
                .hasBias(hasBias).stride(getStrideFromConfig(layerConfig, 1)[0]);
        int[] padding = getPaddingFromBorderModeConfig(layerConfig, 1);
        if (padding != null)
            builder.padding(padding[0]);
        this.layer = builder.build();
    }

    /**
     * Get DL4J ConvolutionLayer.
     *
     * @return  ConvolutionLayer
     */
    public Convolution1DLayer getConvolution1DLayer() {
        return (Convolution1DLayer) this.layer;
    }
}