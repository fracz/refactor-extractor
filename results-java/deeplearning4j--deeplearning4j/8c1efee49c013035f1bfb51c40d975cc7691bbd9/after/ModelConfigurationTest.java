package org.deeplearning4j.nn.modelimport.keras;

import org.junit.Test;
import org.nd4j.linalg.io.ClassPathResource;

/**
 * Unit tests for Keras model configuration import.
 *
 * TODO: Replace deprecated stuff and rename to something like KerasModelTest
 * TODO: Move test resources to dl4j-test-resources
 * TODO: Reorganize test resources
 * TODO: Add more extensive tests including exotic Functional API architectures
 *
 * @author dave@skymind.io
 */
public class ModelConfigurationTest {

    @Test
    public void importKerasMlpSequentialConfigTest() throws Exception {
        ClassPathResource resource = new ClassPathResource("keras/simple/mlp_config.json",
                ModelConfigurationTest.class.getClassLoader());
        String configFilename = resource.getFile().getAbsolutePath();
        ModelConfiguration.importSequentialModelConfigFromFile(configFilename);
    }

    @Test
    public void importKerasMlpModelConfigTest() throws Exception {
        ClassPathResource resource = new ClassPathResource("keras/simple/mlp_fapi_config.json",
                ModelConfigurationTest.class.getClassLoader());
        String configFilename = resource.getFile().getAbsolutePath();
        ModelConfiguration.importFunctionalApiConfigFromFile(configFilename);
    }

    @Test
    public void importKerasMlpModelMultilossConfigTest() throws Exception {
        ClassPathResource resource = new ClassPathResource("keras/simple/mlp_fapi_multiloss_config.json",
                ModelConfigurationTest.class.getClassLoader());
        String configFilename = resource.getFile().getAbsolutePath();
        ModelConfiguration.importFunctionalApiConfigFromFile(configFilename);
    }

    @Test
    public void importKerasConvnetTensorflowConfigTest() throws Exception {
        ClassPathResource resource = new ClassPathResource("keras/simple/cnn_tf_config.json",
                ModelConfigurationTest.class.getClassLoader());
        String configFilename = resource.getFile().getAbsolutePath();
        ModelConfiguration.importSequentialModelConfigFromFile(configFilename);
    }

    @Test
    public void importKerasConvnetTheanoConfigTest() throws Exception {
        ClassPathResource resource = new ClassPathResource("keras/simple/cnn_th_config.json",
                ModelConfigurationTest.class.getClassLoader());
        String configFilename = resource.getFile().getAbsolutePath();
        ModelConfiguration.importSequentialModelConfigFromFile(configFilename);
    }

    @Test
    public void importKerasLstmFixedLenConfigTest() throws Exception {
        ClassPathResource resource = new ClassPathResource("keras/simple/lstm_fixed_config.json",
                ModelConfigurationTest.class.getClassLoader());
        String configFilename = resource.getFile().getAbsolutePath();
        ModelConfiguration.importSequentialModelConfigFromFile(configFilename);
    }
}