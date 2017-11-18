/*
 *
 *  * Copyright 2015 Skymind,Inc.
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

package org.deeplearning4j.cli.flags;


import org.deeplearning4j.cli.api.flags.Model;
import org.deeplearning4j.cli.api.flags.test.BaseFlagTest;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author sonali
 */
public class ModelFlagTest extends BaseFlagTest {
    @Test
    public void test() throws Exception {
        Model testModelFlag = new Model();

        NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
                .dist(new NormalDistribution(1,1e-1))
                .layer(new RBM())
                .build();
        String json = conf.toJson();
        NeuralNetConfiguration testConfig = testModelFlag.value(json);
        assertEquals(conf, testConfig);
   }

}