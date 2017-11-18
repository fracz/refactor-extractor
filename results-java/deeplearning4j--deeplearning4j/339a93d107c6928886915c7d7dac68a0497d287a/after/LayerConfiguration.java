package org.deeplearning4j.nn.modelimport.keras;

import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.weights.WeightInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Routines for importing saved Keras layer configurations.
 *
 * @author davekale
 */
public class LayerConfiguration {
    /* Keras layer types. */
    public static final String KERAS_LAYER_TYPE_INPUT = "Input";
    public static final String KERAS_LAYER_TYPE_ACTIVATION = "Activation";
    public static final String KERAS_LAYER_TYPE_DROPOUT = "Dropout";
    public static final String KERAS_LAYER_TYPE_DENSE = "Dense";
    public static final String KERAS_LAYER_TYPE_TIME_DISTRIBUTED_DENSE = "TimeDistributedDense";
    public static final String KERAS_LAYER_TYPE_LSTM = "LSTM";
    public static final String KERAS_LAYER_TYPE_CONVOLUTION_2D = "Convolution2D";
    public static final String KERAS_LAYER_TYPE_MAX_POOLING_2D = "MaxPooling2D";
    public static final String KERAS_LAYER_TYPE_AVERAGE_POOLING_2D = "AveragePooling2D";
    public static final String KERAS_LAYER_TYPE_FLATTEN = "Flatten";

    /* Keras layer properties. */
    public static final String KERAS_LAYER_PROPERTY_CLASS_NAME = "class_name";
    public static final String KERAS_LAYER_PROPERTY_CONFIG = "config";
    public static final String KERAS_LAYER_PROPERTY_NAME = "name";
    public static final String KERAS_LAYER_PROPERTY_DROPOUT = "dropout";
    public static final String KERAS_LAYER_PROPERTY_ACTIVATION = "activation";
    public static final String KERAS_LAYER_PROPERTY_INIT = "init";
    public static final String KERAS_LAYER_PROPERTY_W_REGULARIZER = "W_regularizer";
    public static final String KERAS_LAYER_PROPERTY_B_REGULARIZER = "b_regularizer";
    public static final String KERAS_LAYER_PROPERTY_OUTPUT_DIM = "output_dim";
    public static final String KERAS_LAYER_PROPERTY_SUBSAMPLE = "subsample";
    public static final String KERAS_LAYER_PROPERTY_NB_ROW = "nb_row";
    public static final String KERAS_LAYER_PROPERTY_NB_COL = "nb_col";
    public static final String KERAS_LAYER_PROPERTY_NB_FILTER = "nb_filter";
    public static final String KERAS_LAYER_PROPERTY_STRIDES = "strides";
    public static final String KERAS_LAYER_PROPERTY_POOL_SIZE = "pool_size";
    public static final String KERAS_LAYER_PROPERTY_INNER_ACTIVATION = "inner_activation";
    public static final String KERAS_LAYER_PROPERTY_INNER_INIT = "inner_init";
    public static final String KERAS_LAYER_PROPERTY_DROPOUT_U = "dropout_U";
    public static final String KERAS_LAYER_PROPERTY_FORGET_BIAS_INIT = "forget_bias_init";
    public static final String KERAS_LAYER_PROPERTY_DROPOUT_W = "dropout_W";
    public static final String KERAS_LAYER_PROPERTY_BATCH_INPUT_SHAPE = "batch_input_shape";
    public static final String KERAS_LAYER_PROPERTY_DIM_ORDERING = "dim_ordering";

    /* Keras weight regularizers. */
    public static final String KERAS_REGULARIZATION_TYPE_L1 = "l1";
    public static final String KERAS_REGULARIZATION_TYPE_L2 = "l2";

    /* Keras weight initializers. */
    public static final String KERAS_INIT_UNIFORM = "uniform";
    public static final String KERAS_INIT_ZERO = "zero";
    public static final String KERAS_INIT_GLOROT_NORMAL = "glorot_normal";
    public static final String KERAS_INIT_GLOROT_UNIFORM = "glorot_uniform";
    public static final String KERAS_INIT_HE_NORMAL = "he_normal";
    public static final String KERAS_INIT_HE_UNIFORM = "he_uniform";
    public static final String KERAS_INIT_LECUN_UNIFORM = "lecun_uniform";
    public static final String KERAS_INIT_NORMAL = "normal";
    public static final String KERAS_INIT_ORTHOGONAL = "orthogonal";
    public static final String KERAS_INIT_IDENTITY = "identity";

    /* Keras and DL4J activation types. */
    public static final String KERAS_ACTIVATION_LINEAR = "linear";
    public static final String DL4J_ACTIVATION_IDENTITY = "identity";

    /* Keras LSTM forget gate bias initializations. */
    public static final String LSTM_FORGET_BIAS_KERAS_INIT_ZERO = "zero";
    public static final String LSTM_FORGET_BIAS_KERAS_INIT_ONE = "one";

    /* Keras dimension ordering for, e.g., convolutional layers. */
    public static final String KERAS_DIM_ORDERING_THEANO = "th";
    public static final String KERAS_DIM_ORDERING_TENSORFLOW = "tf";
    public static final String KERAS_LAYER_TYPE_RESHAPE = "Reshape";
    public static final String KERAS_LAYER_TYPE_REPEATVECTOR = "RepeatVector";

    /* Logging. */
    private static Logger log = LoggerFactory.getLogger(LayerConfiguration.class);

    private LayerConfiguration() {}

    public static Map<String,Object> processLayerConfigObject(Object layerConfigObject) {
        Map<String,Object> outerConfig = (Map<String,Object>)layerConfigObject;
        Map<String,Object> layerConfig = (Map<String,Object>)outerConfig.get(KERAS_LAYER_PROPERTY_CONFIG);
        for (String property : outerConfig.keySet())
            if (!property.equals(KERAS_LAYER_PROPERTY_CONFIG))
                layerConfig.put(property, outerConfig.get(property));
        return layerConfig;
    }

    /**
     * Configure DL4J Layer from a Keras layer configuration.
     *
     * @param layerConfig      Map containing Keras layer properties
     * @return                 DL4J Layer configuration
     * @see Layer
     */
    public static Layer buildLayer(Map<String,Object> layerConfig) {
        String kerasLayerClassName = (String)layerConfig.get(KERAS_LAYER_PROPERTY_CLASS_NAME);
        Layer layer = null;
        switch (kerasLayerClassName) {
            case KERAS_LAYER_TYPE_ACTIVATION:
                layer = buildActivationLayer(layerConfig);
                break;
            case KERAS_LAYER_TYPE_DROPOUT:
                layer = buildDropoutLayer(layerConfig);
                break;
            case KERAS_LAYER_TYPE_DENSE:
            case KERAS_LAYER_TYPE_TIME_DISTRIBUTED_DENSE:
            /* TODO: test to make sure that mapping TimeDistributedDense to DenseLayer works.
             * Also, Keras recently added support for TimeDistributed layer wrapper so may
             * need to look into how that changes things.
             * */
                layer = buildDenseLayer(layerConfig);
                break;
            case KERAS_LAYER_TYPE_LSTM:
                layer = buildGravesLstmLayer(layerConfig);
                break;
            case KERAS_LAYER_TYPE_CONVOLUTION_2D:
            /* TODO: Add support for 1D, 3D convolutional layers? */
                layer = buildConvolutionLayer(layerConfig);
                break;
            case KERAS_LAYER_TYPE_MAX_POOLING_2D:
            case KERAS_LAYER_TYPE_AVERAGE_POOLING_2D:
            /* TODO: Add support for 1D, 3D pooling layers? */
                layer = buildSubsamplingLayer(layerConfig);
                break;
            case KERAS_LAYER_TYPE_FLATTEN:
            case KERAS_LAYER_TYPE_RESHAPE:
            case KERAS_LAYER_TYPE_REPEATVECTOR:
                log.warn("Found Keras " + kerasLayerClassName + ". DL4J adds reshaping/repeating layers during model compilation: https://github.com/deeplearning4j/deeplearning4j/blob/master/deeplearning4j-nn/src/main/java/org/deeplearning4j/nn/conf/MultiLayerConfiguration.java#L429");
                break;
            default:
                throw new IncompatibleKerasConfigurationException("Unsupported keras layer type " + kerasLayerClassName);
        }
        return layer;
    }

    /**
     *
     * @param layerConfig
     * @return
     */
    public static int[] getLayerInputShape(Map<String,Object> layerConfig) {
        if (!layerConfig.containsKey(KERAS_LAYER_PROPERTY_BATCH_INPUT_SHAPE))
            return null;
        List<Integer> batchInputShape = (List<Integer>)layerConfig.get(KERAS_LAYER_PROPERTY_BATCH_INPUT_SHAPE);
        int[] inputShape = new int[batchInputShape.size()-1];
        for (int i = 1; i < batchInputShape.size(); i++) {
            inputShape[i - 1] = batchInputShape.get(i) != null ? batchInputShape.get(i) : 0;
        }
        if (layerConfig.containsKey(LayerConfiguration.KERAS_LAYER_PROPERTY_DIM_ORDERING)) {
            int[] oldInputShape = inputShape.clone();
            String dimOrdering = (String)layerConfig.get(LayerConfiguration.KERAS_LAYER_PROPERTY_DIM_ORDERING);
            if (inputShape.length == 3) {
                if (dimOrdering.equals(KERAS_DIM_ORDERING_TENSORFLOW)) {
                    /* TensorFlow convolutional input: # rows, # cols, # channels */
                    inputShape[0] = oldInputShape[0];
                    inputShape[1] = oldInputShape[1];
                    inputShape[2] = oldInputShape[2];
                } else if (dimOrdering.equals(KERAS_DIM_ORDERING_THEANO)) {
                    /* Theano convolutional input: # channels, # rows, # cols */
                    inputShape[0] = oldInputShape[1];
                    inputShape[1] = oldInputShape[2];
                    inputShape[2] = oldInputShape[0];
                } else
                    throw new IncompatibleKerasConfigurationException("Unknown keras backend " + dimOrdering);
            } else
                throw new IncompatibleKerasConfigurationException("Invalid input rank " + inputShape.length + " for dim ordering " + dimOrdering);
        }
        return inputShape;
    }

    /**
     * Map Keras to DL4J activation functions.
     *
     * @param kerasActivation   String containing Keras activation function name
     * @return                  String containing DL4J activation function name
     */
    public static String mapActivation(String kerasActivation) {
        if (kerasActivation.equals(KERAS_ACTIVATION_LINEAR))
            return DL4J_ACTIVATION_IDENTITY;
        return kerasActivation;
    }

    /**
     * Map Keras to DL4J weight initialization functions.
     *
     * @param kerasInit     String containing Keras initialization function name
     * @return              DL4J weight initialization enum
     * @see WeightInit
     */
    public static WeightInit mapWeightInitialization(String kerasInit) {
        /* WEIGHT INITIALIZATION
         * TODO: finish mapping keras-to-dl4j weight distributions.
         * Low priority since our focus is on loading trained models.
         *
         * Remaining dl4j distributions: DISTRIBUTION, SIZE, NORMALIZED,
         * VI, RELU, XAVIER
         */
        WeightInit init = WeightInit.XAVIER;
        if (kerasInit != null) {
            switch (kerasInit) {
                case KERAS_INIT_GLOROT_NORMAL:
                    init = WeightInit.XAVIER;
                    break;
                case KERAS_INIT_GLOROT_UNIFORM:
                    init = WeightInit.XAVIER_UNIFORM;
                    break;
                case KERAS_INIT_HE_NORMAL:
                    init = WeightInit.RELU;
                    break;
                case KERAS_INIT_HE_UNIFORM:
                    init = WeightInit.RELU_UNIFORM;
                    break;
                case KERAS_INIT_ZERO:
                    init = WeightInit.ZERO;
                    break;
                case KERAS_INIT_UNIFORM:
                    /* TODO: map to DL4J dist with scale taken from config. */
                case KERAS_INIT_NORMAL:
                    /* TODO: map to DL4J normal with params taken from config. */
                case KERAS_INIT_IDENTITY: // does not map to existing Dl4J initializer
                case KERAS_INIT_ORTHOGONAL: // does not map to existing Dl4J initializer
                case KERAS_INIT_LECUN_UNIFORM: // does not map to existing Dl4J initializer
                default:
                    log.warn("Unknown keras weight initializer " + init);
                    break;
            }
        }
        return init;
    }

    /**
     * Get L1 regularization (if any) from Keras weight regularization configuration.
     *
     * @param regularizerConfig     Map containing Keras weight reguarlization configuration
     * @return                      L1 regularization strength (0.0 if none)
     */
    public static double getL1Regularization(Map<String,Object> regularizerConfig) {
        if (regularizerConfig != null && regularizerConfig.containsKey(KERAS_REGULARIZATION_TYPE_L1))
            return (double)regularizerConfig.get(KERAS_REGULARIZATION_TYPE_L1);
        return 0.0;
    }

    /**
     * Get L2 regularization (if any) from Keras weight regularization configuration.
     *
     * @param regularizerConfig     Map containing Keras weight reguarlization configuration
     * @return                      L2 regularization strength (0.0 if none)
     */
    public static double getL2Regularization(Map<String,Object> regularizerConfig) {
        if (regularizerConfig != null && regularizerConfig.containsKey(KERAS_REGULARIZATION_TYPE_L2))
            return (double)regularizerConfig.get(KERAS_REGULARIZATION_TYPE_L2);
        return 0.0;
    }

    /**
     * Check whether Keras weight regularization is of unknown type. Currently prints a warning
     * since main use case for model import is inference, not further training. Unlikely since
     * standard Keras weight regularizers are L1 and L2.
     *
     * @param regularizerConfig     Map containing Keras weight reguarlization configuration
     * @return                      L1 regularization strength (0.0 if none)
     *
     * TODO: should this throw an error instead?
     */
    public static void checkForUnknownRegularizer(Map<String, Object> regularizerConfig) {
        if (regularizerConfig != null) {
            Set<String> regularizerFields = regularizerConfig.keySet();
            regularizerFields.remove(KERAS_REGULARIZATION_TYPE_L1);
            regularizerFields.remove(KERAS_REGULARIZATION_TYPE_L2);
            regularizerFields.remove(KERAS_LAYER_PROPERTY_NAME);
            if (regularizerFields.size() > 0) {
                String unknownField = (String) regularizerFields.toArray()[0];
                log.warn("Unknown regularization field: " + unknownField);
            }
        }
    }

    /**
     * Configure DL4J ActivationLayer from a Keras Activation configuration.
     *
     * @param layerConfig      Map containing Keras Activation layer properties
     * @return                 DL4J ActivationLayer configuration
     * @throws UnsupportedOperationException
     * @see ActivationLayer
     */
    public static ActivationLayer buildActivationLayer(Map<String, Object> layerConfig) {
        ActivationLayer.Builder builder = new ActivationLayer.Builder();
        finishLayerConfig(builder, layerConfig);
        return builder.build();
    }

    /**
     * Configure DL4J DropoutLayer from a Keras Dropout configuration.
     *
     * @param layerConfig      Map containing Keras Dropout layer properties
     * @return                 DL4J DropoutLayer configuration
     * @throws UnsupportedOperationException
     * @see DropoutLayer
     */
    public static DropoutLayer buildDropoutLayer(Map<String, Object> layerConfig) {
        DropoutLayer.Builder builder = new DropoutLayer.Builder();
        finishLayerConfig(builder, layerConfig);
        return builder.build();
    }

    /**
     * Configure DL4J DenseLayer from a Keras Dense configuration.
     *
     * @param layerConfig      Map containing Keras Dense layer properties
     * @return                 DL4J DenseLayer configuration
     * @throws UnsupportedOperationException
     * @see DenseLayer
     */
    public static DenseLayer buildDenseLayer(Map<String,Object> layerConfig)
        throws UnsupportedOperationException {
        DenseLayer.Builder builder = new DenseLayer.Builder()
                .nOut((int)layerConfig.get(KERAS_LAYER_PROPERTY_OUTPUT_DIM));
        finishLayerConfig(builder, layerConfig);
        return builder.build();
    }

    /**
     * Configure DL4J ConvolutionLayer from a Keras *Convolution configuration.
     *
     * @param layerConfig      Map containing Keras *Convolution layer properties
     * @return                 DL4J ConvolutionLayer configuration
     * @throws UnsupportedOperationException
     * @see ConvolutionLayer
     *
     * TODO: verify whether works for 1D convolutions. What about 3D convolutions?
     */
    public static ConvolutionLayer buildConvolutionLayer(Map<String,Object> layerConfig)
        throws UnsupportedOperationException {
        List<Integer> stride = (List<Integer>)layerConfig.get(KERAS_LAYER_PROPERTY_SUBSAMPLE);
        int nb_row = (Integer)layerConfig.get(KERAS_LAYER_PROPERTY_NB_ROW);
        int nb_col = (Integer)layerConfig.get(KERAS_LAYER_PROPERTY_NB_COL);
        ConvolutionLayer.Builder builder = new ConvolutionLayer.Builder()
                .stride(stride.get(0), stride.get(1))
                .kernelSize(nb_row, nb_col)
                .nOut((int)layerConfig.get(KERAS_LAYER_PROPERTY_NB_FILTER));
        finishLayerConfig(builder, layerConfig);
        return builder.build();
    }

    /**
     * Configure DL4J SubsamplingLayer from a Keras *Pooling* configuration.
     *
     * @param layerConfig      Map containing Keras *Pooling* layer properties
     * @return                 DL4J SubsamplingLayer configuration
     * @throws UnsupportedOperationException
     * @see SubsamplingLayer
     *
     * TODO: add other pooling layer types and shapes.
     */
    public static SubsamplingLayer buildSubsamplingLayer(Map<String,Object> layerConfig)
        throws UnsupportedOperationException {
        List<Integer> stride = (List<Integer>)layerConfig.get(KERAS_LAYER_PROPERTY_STRIDES);
        List<Integer> pool = (List<Integer>)layerConfig.get(KERAS_LAYER_PROPERTY_POOL_SIZE);
        SubsamplingLayer.Builder builder = new SubsamplingLayer.Builder()
                                                .stride(stride.get(0), stride.get(1))
                                                .kernelSize(pool.get(0), pool.get(1));
        String layerClassName = (String)layerConfig.get(KERAS_LAYER_PROPERTY_CLASS_NAME);
        switch (layerClassName) {
            case KERAS_LAYER_TYPE_MAX_POOLING_2D:
                builder.poolingType(SubsamplingLayer.PoolingType.MAX);
                break;
            case KERAS_LAYER_TYPE_AVERAGE_POOLING_2D:
                builder.poolingType(SubsamplingLayer.PoolingType.AVG);
                break;
            /* TODO: 1D (and 3D?) shaped pooling layers. */
            default:
                throw new UnsupportedOperationException("Unsupported Keras pooling layer " + layerClassName);
        }
        finishLayerConfig(builder, layerConfig);
        return builder.build();
    }

    /**
     * Configure DL4J GravesLSTM layer from a Keras LSTM configuration.
     *
     * @param layerConfig      Map containing Keras LSTM layer properties
     * @return                 DL4J GravesLSTM configuration
     * @throws IncompatibleKerasConfigurationException
     * @throws UnsupportedOperationException
     * @see GravesLSTM
     */
    public static GravesLSTM buildGravesLstmLayer(Map<String,Object> layerConfig)
        throws IncompatibleKerasConfigurationException, UnsupportedOperationException {
        if (!layerConfig.get(KERAS_LAYER_PROPERTY_ACTIVATION).equals(layerConfig.get(KERAS_LAYER_PROPERTY_INNER_ACTIVATION)))
            throw new IncompatibleKerasConfigurationException("Specifying different activation for LSTM inner cells not supported.");
        if (!layerConfig.get(KERAS_LAYER_PROPERTY_INIT).equals(layerConfig.get(KERAS_LAYER_PROPERTY_INNER_INIT)))
            log.warn("Specifying different initialization for inner cells not supported.");
        if ((double)layerConfig.get(KERAS_LAYER_PROPERTY_DROPOUT_U) > 0.0)
            throw new IncompatibleKerasConfigurationException("Dropout > 0 on LSTM recurrent connections not supported.");

        GravesLSTM.Builder builder = new GravesLSTM.Builder();
        builder.nOut((int)layerConfig.get(KERAS_LAYER_PROPERTY_OUTPUT_DIM));
        String forgetBiasInit = (String)layerConfig.get(KERAS_LAYER_PROPERTY_FORGET_BIAS_INIT);
        switch (forgetBiasInit) {
            case LSTM_FORGET_BIAS_KERAS_INIT_ZERO:
                builder.forgetGateBiasInit(0.0);
                break;
            case LSTM_FORGET_BIAS_KERAS_INIT_ONE:
                builder.forgetGateBiasInit(1.0);
                break;
            default:
                log.warn("Unsupported bias initialization: " + forgetBiasInit + ".");
                break;
        }
        layerConfig.put(KERAS_LAYER_PROPERTY_DROPOUT, (double)layerConfig.get(KERAS_LAYER_PROPERTY_DROPOUT_W));
        finishLayerConfig(builder, layerConfig);
        return builder.build();
    }

    /**
     * Perform layer configuration steps that are common across all Keras and DL4J layer types.
     *
     * @param builder       DL4J Layer builder object
     * @param layerConfig   Map containing Keras layer properties
     * @return              DL4J Layer builder object
     * @throws UnsupportedOperationException
     * @see Layer.Builder
     */
    public static Layer.Builder finishLayerConfig(Layer.Builder builder, Map<String,Object> layerConfig)
            throws UnsupportedOperationException {
        if (layerConfig.containsKey(KERAS_LAYER_PROPERTY_DROPOUT)) {
            /* NOTE: Keras "dropout" parameter determines dropout probability,
             * while DL4J "dropout" parameter determines retention probability.
             */
            builder.dropOut(1.0-(double)layerConfig.get(KERAS_LAYER_PROPERTY_DROPOUT));
        }
        if (layerConfig.containsKey(KERAS_LAYER_PROPERTY_ACTIVATION))
            builder.activation(mapActivation((String)layerConfig.get(KERAS_LAYER_PROPERTY_ACTIVATION)));
        builder.name((String)layerConfig.get(KERAS_LAYER_PROPERTY_NAME));
        if (layerConfig.containsKey(KERAS_LAYER_PROPERTY_INIT)) {
            WeightInit init = mapWeightInitialization((String) layerConfig.get(KERAS_LAYER_PROPERTY_INIT));
            builder.weightInit(init);
            if (init == WeightInit.ZERO)
                builder.biasInit(0.0);
        }
        if (layerConfig.containsKey(KERAS_LAYER_PROPERTY_W_REGULARIZER)) {
            Map<String,Object> regularizerConfig = (Map<String,Object>)layerConfig.get(KERAS_LAYER_PROPERTY_W_REGULARIZER);
            double l1 = getL1Regularization(regularizerConfig);
            if (l1 > 0)
                builder.l1(l1);
            double l2 = getL2Regularization(regularizerConfig);
            if (l2 > 0)
                builder.l2(l2);
            checkForUnknownRegularizer(regularizerConfig);
        }
        if (layerConfig.containsKey(KERAS_LAYER_PROPERTY_B_REGULARIZER)) {
            Map<String,Object> regularizerConfig = (Map<String,Object>)layerConfig.get(KERAS_LAYER_PROPERTY_B_REGULARIZER);
            double l1 = getL1Regularization(regularizerConfig);
            double l2 = getL2Regularization(regularizerConfig);
            if (l1 > 0 || l2 > 0)
                throw new UnsupportedOperationException("Bias regularization not implemented");
        }
        return builder;
    }
}