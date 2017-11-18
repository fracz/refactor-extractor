package org.deeplearning4j.eval;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.Op;
import org.nd4j.linalg.api.ops.impl.transforms.Not;
import org.nd4j.linalg.api.ops.impl.transforms.arithmetic.MulOp;
import org.nd4j.linalg.api.ops.impl.transforms.comparison.CompareAndSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Condition;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.nd4j.shade.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;

/**
 * ROC (Receiver Operating Characteristic) for multi-task binary classifiers, using the specified number of threshold steps.
 * <p>
 * Some ROC implementations will automatically calculate the threshold points based on the data set to give a 'smoother'
 * ROC curve (or optimal cut points for diagnostic purposes). This implementation currently uses fixed steps of size
 * 1.0 / thresholdSteps, as this allows easy implementation for batched and distributed evaluation scenarios (where the
 * full data set is not available in memory on any one machine at once).
 * <p>
 * Unlike {@link ROC} (which supports a single binary label (as a single column probability, or 2 column 'softmax' probability
 * distribution), ROCBinary assumes that all outputs are independent binary variables. This also differs from
 * {@link ROCMultiClass}, which should be used for multi-class (single non-binary) cases.
 * <p>
 * ROCBinary supports per-example and per-output masking: for per-output masking, any particular output may be absent
 * (mask value 0) and hence won't be included in the calculated ROC.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ROCBinary extends BaseEvaluation<ROCBinary> {
    public static final int DEFAULT_PRECISION = 4;

    private ROC[] underlying;

    private int thresholdSteps;
    private boolean rocRemoveRedundantPts;
    private List<String> labels;

    public ROCBinary(int thresholdSteps) {
        this( thresholdSteps, true);
    }

    public ROCBinary(int thresholdSteps, boolean rocRemoveRedundantPts) {
        this.thresholdSteps = thresholdSteps;
        this.rocRemoveRedundantPts = rocRemoveRedundantPts;
    }


    @Override
    public void reset() {
        underlying = null;
    }

    @Override
    public void eval(INDArray labels, INDArray networkPredictions) {
        eval(labels, networkPredictions, (INDArray) null);
    }

    @Override
    public void eval(INDArray labels, INDArray networkPredictions, INDArray maskArray) {
        if (underlying != null && underlying.length != labels.size(1)) {
            throw new IllegalStateException("Labels array does not match stored state size. Expected labels array with "
                            + "size " + underlying.length + ", got labels array with size " + labels.size(1));
        }

        if (labels.rank() == 3) {
            evalTimeSeries(labels, networkPredictions, maskArray);
            return;
        }

        int n = labels.size(1);
        if(underlying == null){
            underlying = new ROC[n];
            for( int i=0; i<n; i++ ){
                underlying[i] = new ROC(thresholdSteps, rocRemoveRedundantPts);
            }
        }

        int[] perExampleNonMaskedIdxs = null;
        for( int i=0; i<n; i++ ){
            INDArray prob = networkPredictions.getColumn(i);
            INDArray label = labels.getColumn(i);
            if(maskArray != null){
                //If mask array is present, pull out the non-masked rows only
                INDArray m;
                boolean perExampleMasking = false;
                if(maskArray.isColumnVector()){
                    //Per-example masking
                    m = maskArray;
                    perExampleMasking = true;
                } else {
                    //Per-output masking
                    m = maskArray.getColumn(i);
                }
                int[] rowsToPull;

                if(perExampleNonMaskedIdxs != null){
                    //Reuse, per-example masking
                    rowsToPull = perExampleNonMaskedIdxs;
                } else {
                    int nonMaskedCount = m.sumNumber().intValue();
                    rowsToPull = new int[nonMaskedCount];
                    int maskSize = m.size(0);
                    int used = 0;
                    for( int j=0; j<maskSize; j++ ){
                        if(m.getDouble(j) != 0.0){
                            rowsToPull[used++] = j;
                        }
                    }
                    if(perExampleMasking){
                        perExampleNonMaskedIdxs = rowsToPull;
                    }
                }

                prob = Nd4j.pullRows(prob, 1, rowsToPull);  //1: tensor along dim 1
                label = Nd4j.pullRows(label, 1, rowsToPull);
            }

            underlying[i].eval(label, prob);
        }
    }

    @Override
    public void merge(ROCBinary other) {
        if (this.underlying == null) {
            this.underlying = other.underlying;
            return;
        } else if (other.underlying == null) {
            return;
        }

        //Both have data
        if(underlying.length != other.underlying.length){
            throw new UnsupportedOperationException("Cannot merge ROCBinary: this expects " + underlying.length +
                    "outputs, other expects " + other.underlying.length + " outputs");
        }
        for( int i=0; i<underlying.length; i++ ){
            this.underlying[i].merge(other.underlying[i]);
        }
    }

    private void assertIndex(int outputNum) {
        if (underlying == null) {
            throw new UnsupportedOperationException("ROCBinary does not have any stats: eval must be called first");
        }
        if (outputNum < 0 || outputNum >= underlying.length) {
            throw new IllegalArgumentException("Invalid input: output number must be between 0 and " + (outputNum - 1));
        }
    }

    /**
     * Returns the number of labels - (i.e., size of the prediction/labels arrays) - if known. Returns -1 otherwise
     */
    public int numLabels() {
        if (underlying == null) {
            return -1;
        }

        return underlying.length;
    }

    /**
     * Get the actual positive count (accounting for any masking) for  the specified output/column
     *
     * @param outputNum Index of the output (0 to {@link #numLabels()}-1)
     */
    public long getCountActualPositive(int outputNum) {
        assertIndex(outputNum);
        return underlying[outputNum].getCountActualPositive();
    }

    /**
     * Get the actual negative count (accounting for any masking) for  the specified output/column
     *
     * @param outputNum Index of the output (0 to {@link #numLabels()}-1)
     */
    public long getCountActualNegative(int outputNum) {
        assertIndex(outputNum);
        return underlying[outputNum].getCountActualNegative();
    }

    /**
     * Get the ROC curve, as a set of points
     *
     * @param outputNum Index of the output (0 to {@link #numLabels()}-1)
     * @return ROC curve, as a list of points
     */
    @Deprecated
    public List<ROC.ROCValue> getResults(int outputNum) {
        assertIndex(outputNum);

        return underlying[outputNum].getResults();
    }

    /**
     * Get the precision/recall curve, for the specified output
     *
     * @param outputNum Index of the output (0 to {@link #numLabels()}-1)
     * @return the precision/recall curve
     */
    @Deprecated
    public List<ROC.PrecisionRecallPoint> getPrecisionRecallCurve(int outputNum) {
        assertIndex(outputNum);
        return underlying[outputNum].getPrecisionRecallCurve();
    }

    /**
     * Get the ROC curve, as a set of (falsePositive, truePositive) points
     * <p>
     * Returns a 2d array of {falsePositive, truePositive values}.<br>
     * Size is [2][thresholdSteps], with out[0][.] being false positives, and out[1][.] being true positives
     *
     * @return ROC curve as double[][]
     */
    @JsonIgnore
    @Deprecated
    public double[][] getResultsAsArray(int outputNum) {
        return getRocCurveAsArray(outputNum);
    }

    @JsonIgnore
    public double[][] getRocCurveAsArray(int outputNum){
        assertIndex(outputNum);

        return underlying[outputNum].getRocCurveAsArray();
    }


    /**
     * Macro-average AUC for all outcomes
     * @return the (macro-)average AUC for all outcomes.
     */
    public double calculateAverageAuc() {
        double ret = 0.0;
        for(int i = 0; i < numLabels(); i++) {
            ret += calculateAUC(i);
        }

        return ret / (double) numLabels();
    }

    /**
     * Calculate the AUC - Area Under Curve<br>
     * Utilizes trapezoidal integration internally
     *
     * @param outputNum
     * @return AUC
     */
    public double calculateAUC(int outputNum) {
        assertIndex(outputNum);

        return underlying[outputNum].calculateAUC();
    }

    /**
     * Set the label names, for printing via {@link #stats()}
     */
    public void setLabelNames(List<String> labels) {
        if (labels == null) {
            this.labels = null;
            return;
        }
        this.labels = new ArrayList<>(labels);
    }

    @Override
    public String stats() {
        return stats(DEFAULT_PRECISION);
    }

    public String stats(int printPrecision) {
        //Calculate AUC and also print counts, for each output

        StringBuilder sb = new StringBuilder();

        int maxLabelsLength = 15;
        if (labels != null) {
            for (String s : labels) {
                maxLabelsLength = Math.max(s.length(), maxLabelsLength);
            }
        }

        String patternHeader = "%-" + (maxLabelsLength + 5) + "s%-12s%-10s%-10s";
        String header = String.format(patternHeader, "Label", "AUC", "# Pos", "# Neg");

        String pattern = "%-" + (maxLabelsLength + 5) + "s" //Label
                        + "%-12." + printPrecision + "f" //AUC
                        + "%-10d%-10d"; //Count pos, count neg

        sb.append(header);

        for (int i = 0; i < underlying.length; i++) {
            double auc = calculateAUC(i);

            String label = (labels == null ? String.valueOf(i) : labels.get(i));

            sb.append("\n").append(String.format(pattern, label, auc, getCountActualPositive(i), getCountActualNegative(i)));
        }

        return sb.toString();
    }
}