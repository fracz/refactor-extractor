/*
 *
 *  * Copyright 2016 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.nn.modelimport.keras;

import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Routines for importing saved Keras model configurations.
 *
 * @author dave@skymind.io
 *
 * @deprecated Use {@link org.deeplearning4j.nn.modelimport.keras.KerasModel} and
 *             {@link org.deeplearning4j.nn.modelimport.keras.KerasSequentialModel} instead.
 */
@Deprecated
public class ModelConfiguration {

    private static Logger log = LoggerFactory.getLogger(ModelConfiguration.class);

    private ModelConfiguration() {}

    /**
     * Imports a Keras Sequential model configuration saved using call to model.to_json().
     *
     * @param modelJsonFilename    Path to text file storing Keras Sequential configuration as valid JSON.
     * @return                     DL4J MultiLayerConfiguration
     * @throws IOException
     * @deprecated Use {@link KerasModelImport#importKerasSequentialConfiguration} instead
     */
    @Deprecated
    public static MultiLayerConfiguration importSequentialModelConfigFromFile(String modelJsonFilename)
            throws IOException {
        return KerasModelImport.importKerasSequentialConfiguration(modelJsonFilename);
    }

    /**
     * Imports a Keras Functional API model configuration saved using call to model.to_json().
     *
     * @param modelJsonFilename    Path to text file storing Keras Model configuration as valid JSON.
     * @return                     DL4J ComputationGraphConfiguration
     * @throws IOException
     * @deprecated Use {@link KerasModelImport#importKerasModelConfiguration} instead
     */
    @Deprecated
    public static ComputationGraphConfiguration importFunctionalApiConfigFromFile(String modelJsonFilename)
            throws IOException {
        return KerasModelImport.importKerasModelConfiguration(modelJsonFilename);
    }

    /**
     * Imports a Keras Sequential model configuration saved using call to model.to_json().
     *
     * @param modelJson    String storing Keras Sequential configuration as valid JSON.
     * @return             DL4J MultiLayerConfiguration
     * @throws IOException
     * @deprecated Use {@link org.deeplearning4j.nn.modelimport.keras.KerasSequentialModel} instead
     */
    @Deprecated
    public static MultiLayerConfiguration importSequentialModelConfig(String modelJson)
            throws IOException {
        KerasSequentialModel kerasModel = new KerasSequentialModel(modelJson);
        return kerasModel.getMultiLayerConfiguration();
    }

    /**
     * Imports a Keras Functional API model configuration saved using call to model.to_json().
     *
     * @param modelJson    String storing Keras Model configuration as valid JSON.
     * @return             DL4J ComputationGraphConfiguration
     * @throws IOException
     * @deprecated Use {@link org.deeplearning4j.nn.modelimport.keras.KerasModel} instead
     */
    @Deprecated
    public static ComputationGraphConfiguration importFunctionalApiConfig(String modelJson)
            throws IOException {
        KerasModel kerasModel = new KerasModel(modelJson);
        return kerasModel.getComputationGraphConfiguration();
    }
}