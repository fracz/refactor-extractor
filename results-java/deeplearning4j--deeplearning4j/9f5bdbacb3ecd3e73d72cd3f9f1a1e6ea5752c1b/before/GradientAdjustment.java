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

package org.deeplearning4j.optimize;

import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.gradient.Gradient;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.AdaGrad;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Gradient adjustment
 * @author Adam Gibson
 */
public class GradientAdjustment {

    private GradientAdjustment(){}


    private static final Logger log = LoggerFactory.getLogger(GradientAdjustment.class);


    /**
     * Updates each variable wrt its gradient
     * @param conf the configuration
     * @param iteration the iteration
     * @param gradient the gradients for the variables
     * @param batchSize the batch size of the input
     * @param adaGrad the adagrad map (per variable adagrad entries(
     * @param model the model to use
     */
    public static void updateGradientAccordingToParams(NeuralNetConfiguration conf,int iteration,Gradient gradient,int batchSize,Map<String,AdaGrad> adaGrad,Map<String,INDArray> stepCache,Model model) {
        for(String variable : gradient.gradientForVariable().keySet()) {
            AdaGrad adaGradForVariable = adaGrad.get(variable);
            INDArray lastStepForVariable = stepCache.get(variable);
            if(adaGradForVariable == null) {
                adaGradForVariable = new AdaGrad(model.getParam(variable).shape());
                adaGrad.put(variable, adaGradForVariable);
            }

            if(lastStepForVariable == null) {
                lastStepForVariable = Nd4j.ones((model.getParam(variable).shape()));
                stepCache.put(variable,lastStepForVariable);
            }

            updateGradientAccordingToParams(
                    conf
                    ,iteration
                    ,adaGradForVariable
                    ,gradient.getGradientFor(variable)
                    ,model.getParam(variable)
                    ,stepCache.get(variable)
                    ,batchSize);
        }
    }

    /**
     * Update the gradient according to
     * the configuration such as
     * adagrad, momentum, and sparsity
     * @param gradient the gradient to modify
     */
    public static void updateGradientAccordingToParams(NeuralNetConfiguration conf,int iteration,AdaGrad adaGrad,INDArray gradient,INDArray params,INDArray lastStep,int batchSize) {
        if(adaGrad == null)
            adaGrad = new AdaGrad(gradient.shape());


        //reset adagrad history
        if(iteration != 0 && conf.getResetAdaGradIterations() > 0 &&  iteration % conf.getResetAdaGradIterations() == 0) {
            adaGrad.historicalGradient = null;

            log.info("Resetting adagrad");
        }

        //change up momentum after so many iterations if specified
        double momentum = conf.getMomentum();
        if(conf.getMomentumAfter() != null && !conf.getMomentumAfter().isEmpty()) {
            int key = conf.getMomentumAfter().keySet().iterator().next();
            if(iteration >= key) {
                momentum = conf.getMomentumAfter().get(key);
            }
        }


        //RMSPROP
        if(conf.getRmsDecay() > 0) {
            lastStep.assign(lastStep.mul(conf.getRmsDecay()).addi(Transforms.pow(gradient,2).muli((1 - conf.getRmsDecay()))));
            gradient = gradient.muli(conf.getLr()).negi().divi(Transforms.sqrt(lastStep.add(Nd4j.EPS_THRESHOLD)));
        }

        if (conf.isUseAdaGrad())
            gradient = adaGrad.getGradient(gradient);

        else
            gradient.muli(conf.getLr());




        //apply nesterov's AFTER learning rate update
        if (momentum > 0) {
            gradient = lastStep.mul(momentum).subi(gradient);
            //in place update on the step cache
            lastStep.assign(gradient);
        }

        //simulate post gradient application  and apply the difference to the gradient to decrease the change the gradient has
        if(conf.isUseRegularization() && conf.getL2() > 0)
            gradient.subi(params.mul(conf.getL2() * conf.getLr()));
        else if(conf.isUseRegularization() && conf.getL1() < 0)
            gradient.subi(gradient.mul(Transforms.sign(params)).muli(conf.getL1() * conf.getLr()));



        if(conf.isConstrainGradientToUnitNorm())
            gradient.divi(gradient.norm2(Integer.MAX_VALUE));


        gradient.divi(batchSize);


    }


}