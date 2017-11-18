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

package org.deeplearning4j.optimize.solvers;

import org.apache.commons.math3.util.FastMath;
import static org.nd4j.linalg.ops.transforms.Transforms.*;

import org.deeplearning4j.exception.InvalidStepException;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.optimize.api.ConvexOptimizer;
import org.deeplearning4j.optimize.api.StepFunction;
import org.deeplearning4j.optimize.stepfunctions.DefaultStepFunction;
import org.deeplearning4j.optimize.stepfunctions.NegativeDefaultStepFunction;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.scalar.comparison.ScalarSetValue;
import org.nd4j.linalg.api.ops.impl.transforms.comparison.Eps;
import org.nd4j.linalg.factory.Nd4j;
import org.deeplearning4j.optimize.api.LineOptimizer;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.nd4j.linalg.indexing.functions.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//"Line Searches and Backtracking", p385, "Numeric Recipes in C"
/**
 @author Aron Culotta <a href="mailto:culotta@cs.umass.edu">culotta@cs.umass.edu</a>

 Adapted from mallet with original authors above.
 Modified to be a vectorized version that uses jblas matrices
 for computation rather than the mallet ops.


 Numerical Recipes in C: p.385. lnsrch. A simple backtracking line
 search. No attempt at accurately finding the true minimum is
 made. The goal is only to ensure that BackTrackLineSearch will
 return a position of higher value.

 @author Adam Gibson


 */

public class BackTrackLineSearch implements LineOptimizer  {
    private static final Logger logger = LoggerFactory.getLogger(BackTrackLineSearch.class.getName());

    private Model layer;
    private StepFunction stepFunction = new DefaultStepFunction();
    private ConvexOptimizer optimizer;
    private int maxIterations = 5;
    double stepMax = 100;

    // termination conditions: either
    //   a) abs(delta x/x) < REL_TOLX for all coordinates
    //   b) abs(delta x) < ABS_TOLX for all coordinates
    //   c) sufficient function increase (uses ALF)
    private double relTolx = 1e-7f;
    private double absTolx = 1e-4f; // tolerance on absolute value difference
    protected final double ALF = 1e-4f;

    /**
     *
     * @param layer
     * @param stepFunction
     * @param optimizer
     */
    public BackTrackLineSearch(Model layer, StepFunction stepFunction, ConvexOptimizer optimizer) {
        this.layer = layer;
        this.stepFunction = stepFunction;
        this.optimizer = optimizer;
    }

    /**
     *
     * @param optimizable
     * @param optimizer
     */
    public BackTrackLineSearch(Model optimizable, ConvexOptimizer optimizer) {
        this(optimizable, new NegativeDefaultStepFunction(), optimizer);
    }


    public void setStepMax(double stepMax) {
        this.stepMax = stepMax;
    }


    public double getStepMax() {
        return stepMax;
    }

    /**
     * Sets the tolerance of relative diff in function value.
     *  Line search converges if abs(delta x / x) < tolx
     *  for all coordinates. */
    public void setRelTolx (double tolx) { relTolx = tolx; }

    /**
     * Sets the tolerance of absolute diff in function value.
     *  Line search converges if abs(delta x) < tolx
     *  for all coordinates. */
    public void setAbsTolx (double tolx) { absTolx = tolx; }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public double setScoreFor(INDArray parameters) {
        if(Nd4j.ENFORCE_NUMERICAL_STABILITY) {
            BooleanIndexing.applyWhere(parameters, Conditions.isNan(),new Value(Nd4j.EPS_THRESHOLD));
        }

        layer.setParams(parameters);
        layer.computeGradientAndScore();
        return layer.score();
    }

    // returns fraction of step size if found a good step
    // returns 0.0 if could not step in direction
    // step == alam and score == f in book
    /**
     *
     * @param parameters the parameters to optimize
     * @param gradients the line/rate of change
     * @param searchDirection  the point for the line search to go in
     * @return the next step size
     * @throws InvalidStepException
     */
    @Override
    public double optimize(INDArray parameters, INDArray gradients, INDArray searchDirection) throws InvalidStepException {
        double test, stepMin, step, step2, oldStep, tmpStep;
        double rhs1, rhs2, a, b, disc, score, oldScore, score2;

        double sum = searchDirection.norm2(Integer.MAX_VALUE).getDouble(0);
        double slope = Nd4j.getBlasWrapper().dot(searchDirection, gradients);
        logger.debug("slope = {}", slope);

        INDArray maxOldParams = abs(parameters);
        Nd4j.getExecutioner().exec(new ScalarSetValue(maxOldParams, 1));
        INDArray testMatrix = abs(gradients).divi(maxOldParams);
        test = testMatrix.max(Integer.MAX_VALUE).getDouble(0);

        step  = 1.0; // initially, step = 1.0, i.e. take full Newton step
        stepMin = relTolx / test; // relative convergence tolerance
        oldStep = 0.0;
        step2 = 0.0;

        score2 = oldScore = layer.score();

        if(logger.isTraceEnabled()) {
            logger.trace ("ENTERING BACKTRACK\n");
            logger.trace("Entering BackTrackLinnSearch, value = " + oldScore + ",\ndirection.oneNorm:"
                    +	searchDirection.dup().norm1(Integer.MAX_VALUE) + "  direction.infNorm:"+
                    FastMath.max(Float.NEGATIVE_INFINITY, abs(searchDirection.dup()).max(Integer.MAX_VALUE).getDouble(0)));
        }

        if(sum > stepMax) {
            logger.warn("Attempted step too big. scaling: sum= {}, stepMax= {}", sum, stepMax);
            searchDirection.muli(stepMax / sum);
        }

        if( slope <= 0.0 )
            throw new InvalidStepException("Slope " + slope + " is <= 0.0. Expect slope > 0.0");

        // find maximum lambda
        // converge when (delta x) / x < REL_TOLX for all coordinates.
        // the largest step size that triggers this threshold is precomputed and saved in stepMin
        // look for step size in direction given by "line"
        INDArray candidateParameters = null;
        for(int iteration = 0; iteration < maxIterations; iteration++) {
            if( logger.isTraceEnabled() ){
                logger.trace("BackTrack loop iteration {} : step={}, oldStep={}", iteration, step, oldStep);
                logger.trace("before step, x.1norm: {} \nstep: {} \noldStep: {}", parameters.norm1(Integer.MAX_VALUE), step, oldStep);
            }

            if(step == oldStep)
                throw new IllegalArgumentException("Current step == oldStep");

            // step
            if(candidateParameters == null)
                candidateParameters = parameters.dup();
            else
                candidateParameters.assign(parameters);

            stepFunction.step(candidateParameters, searchDirection, step);
            oldStep = step;

            if(logger.isDebugEnabled())  {
                double norm1 = candidateParameters.norm1(Integer.MAX_VALUE).getDouble(0);
                logger.debug("after step, x.1norm: " + norm1);
            }

            // check for convergence on delta x
            if ((step < stepMin) || Nd4j.getExecutioner().execAndReturn(new Eps(parameters, candidateParameters,
                    candidateParameters.dup(), candidateParameters.length())).sum(Integer.MAX_VALUE).getDouble(0) == candidateParameters.length()) {
                score = setScoreFor(parameters);
                logger.trace("EXITING BACKTRACK: Jump too small (stepMin = {}). Exiting and using xold. Value = {}", stepMin, score);
                return 0.0;
            }

            score = setScoreFor(candidateParameters);
            logger.debug("Model score after step = {}", score);

            //Sufficient decrease in cost/loss function (Wolfe condition / Armijo condition)
            if(score <= oldScore - ALF * step * slope) {
                logger.debug("Sufficient decrease (Wolfe cond.), exiting backtrack on iter {}: score={}, oldScore={}",iteration,score,oldScore);
                if (score > oldScore)
                    throw new IllegalStateException
                            ("Function did not decrease: score = " + score + " > " + oldScore + " = oldScore");
                return step;
            }

            // if value is infinite, i.e. we've jumped to unstable territory, then scale down jump
            else if(Double.isInfinite(score) || Double.isInfinite(score2) || Double.isNaN(score) || Double.isNaN(score2)) {
                logger.warn("Value is infinite after jump. oldStep={}. score={}, score2={}. Scaling back step size...",oldStep,score,score2);
                tmpStep = .2 * step;
                if(step < stepMin || Double.isNaN(score2) || Double.isInfinite(score2)) { //convergence on delta x
                    score = setScoreFor(parameters);
                    logger.warn("EXITING BACKTRACK: Jump too small. Exiting and using previous parameters. Value={}", score);
                    return 0.0;
                }
            }

            // backtrack
            else {
                if(step == 1.0) // first time through
                    tmpStep = slope / (2.0 * ( score - oldScore + slope ));
                else {
                    rhs1 = score - oldScore + step * slope;
                    rhs2 = score2 - oldScore + step2 * slope;
                    if(step == step2)
                        throw new IllegalStateException("FAILURE: dividing by step-step2 which equals 0. step=" + step);
                    double stepSquared = step*step;
                    double step2Squared = step2*step2;
                    a = ( rhs1/stepSquared - rhs2/step2Squared ) / (step - step2);
                    b = ( -step2*rhs1/stepSquared + step*rhs2/step2Squared ) / (step - step2);
                    if(a == 0.0)
                        tmpStep = slope / (2.0 * b);
                    else {
                        disc = b * b + 3.0 * a * slope;
                        if(disc < 0.0) {
                            tmpStep = 0.5 * step;
                        }
                        else if (b <= 0.0)
                            tmpStep = (-b + FastMath.sqrt(disc))/(3.0 * a );
                        else
                            tmpStep = slope / (b +FastMath.sqrt(disc));
                    }
                    if (tmpStep > 0.5 * step)
                        tmpStep = 0.5 * step;    // lambda <= 0.5 lambda_1
                }
            }

            step2 = step;
            score2 = score;
            logger.debug("tmpStep: {}", tmpStep);
            step = Math.max(tmpStep, .1f * step);  // lambda >= .1*Lambda_1
        }

        logger.debug("Exited line search after maxIterations termination condition");
        return 0.0;
    }




}
