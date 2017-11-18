package org.deeplearning4j.spark.api.stats;

import lombok.Data;
import org.nd4j.linalg.util.ArrayUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Alex on 16/06/2016.
 */
@Data
public class CommonSparkTrainingStats implements SparkTrainingStats {

    private static Set<String> columnNames = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList(
                    "WorkerFlatMapTotalTimeMs",
                    "WorkerFlatMapTotalExampleCount",
                    "WorkerFlatMapGetInitialModelTimeMs",
                    "WorkerFlatMapDataSetGetTimesMs",
                    "WorkerFlatMapProcessMiniBatchTimesMs",
                    "WorkerFlatMapCountNoDataInstances"

            )));

    private SparkTrainingStats trainingMasterSpecificStats;
    private int[] workerFlatMapTotalTimeMs;
    private int[] workerFlatMapTotalExampleCount;
    private int[] workerFlatMapGetInitialModelTimeMs;
    private int[] workerFlatMapDataSetGetTimesMs;
    private int[] workerFlatMapProcessMiniBatchTimesMs;
    private int workerFlatMapCountNoDataInstances;




    public CommonSparkTrainingStats(){

    }

    private CommonSparkTrainingStats(Builder builder){
        this.trainingMasterSpecificStats = builder.trainingMasterSpecificStats;
        this.workerFlatMapTotalTimeMs = builder.workerFlatMapTotalTimeMs;
        this.workerFlatMapTotalExampleCount = builder.workerFlatMapTotalExampleCount;
        this.workerFlatMapGetInitialModelTimeMs = builder.workerFlatMapGetInitialModelTimeMs;
        this.workerFlatMapDataSetGetTimesMs = builder.workerFlatMapDataSetGetTimesMs;
        this.workerFlatMapProcessMiniBatchTimesMs = builder.workerFlatMapProcessMiniBatchTimesMs;
        this.workerFlatMapCountNoDataInstances = builder.workerFlatMapCountNoDataInstances;
    }


    @Override
    public Set<String> getKeySet() {
        Set<String> set = new LinkedHashSet<>(columnNames);
        if(trainingMasterSpecificStats != null) set.addAll(trainingMasterSpecificStats.getKeySet());

        return set;
    }

    @Override
    public Object getValue(String key) {
        switch (key){
            case "WorkerFlatMapTotalTimeMs":
                return workerFlatMapTotalTimeMs;
            case "WorkerFlatMapTotalExampleCount":
                return workerFlatMapTotalExampleCount;
            case "WorkerFlatMapGetInitialModelTimeMs":
                return workerFlatMapGetInitialModelTimeMs;
            case "WorkerFlatMapDataSetGetTimesMs":
                return workerFlatMapDataSetGetTimesMs;
            case "WorkerFlatMapProcessMiniBatchTimesMs":
                return workerFlatMapProcessMiniBatchTimesMs;
            case "WorkerFlatMapCountNoDataInstances":
                return workerFlatMapCountNoDataInstances;

            default:
                if(trainingMasterSpecificStats != null) return trainingMasterSpecificStats.getValue(key);
                throw new IllegalArgumentException("Unknown key: \"" + key + "\"");
        }
    }

    @Override
    public void addOtherTrainingStats(SparkTrainingStats other) {
        if(!(other instanceof CommonSparkTrainingStats)) throw new IllegalArgumentException("Cannot add other training stats: not an instance of CommonSparkTrainingStats");

        CommonSparkTrainingStats o = (CommonSparkTrainingStats)other;

        workerFlatMapTotalTimeMs = ArrayUtil.combine(workerFlatMapTotalTimeMs, o.workerFlatMapTotalTimeMs);
        workerFlatMapTotalExampleCount = ArrayUtil.combine(workerFlatMapTotalExampleCount, o.workerFlatMapTotalExampleCount);
        workerFlatMapGetInitialModelTimeMs = ArrayUtil.combine(workerFlatMapGetInitialModelTimeMs, o.workerFlatMapGetInitialModelTimeMs);
        workerFlatMapProcessMiniBatchTimesMs = ArrayUtil.combine(workerFlatMapProcessMiniBatchTimesMs, o.workerFlatMapProcessMiniBatchTimesMs);

        if(trainingMasterSpecificStats != null) trainingMasterSpecificStats.addOtherTrainingStats(o.trainingMasterSpecificStats);
        else if(o.trainingMasterSpecificStats != null) throw new IllegalStateException("Cannot merge: training master specific stats is null in one, but not the other");


    }

    @Override
    public String statsAsString() {
        StringBuilder sb = new StringBuilder();
        String f = SparkTrainingStats.DEFAULT_PRINT_FORMAT;

        sb.append(String.format(f,"WorkerFlatMapTotalTimeMs"));
        if(workerFlatMapTotalTimeMs == null ) sb.append("-\n");
        else sb.append(Arrays.toString(workerFlatMapTotalTimeMs)).append("\n");

        sb.append(String.format(f,"WorkerFlatMapTotalExampleCount"));
        if(workerFlatMapTotalExampleCount == null ) sb.append("-\n");
        else sb.append(Arrays.toString(workerFlatMapTotalExampleCount)).append("\n");

        sb.append(String.format(f,"WorkerFlatMapGetInitialModelTimeMs"));
        if(workerFlatMapGetInitialModelTimeMs == null ) sb.append("-\n");
        else sb.append(Arrays.toString(workerFlatMapGetInitialModelTimeMs)).append("\n");

        sb.append(String.format(f,"WorkerFlatMapDataSetGetTimesMs"));
        if(workerFlatMapDataSetGetTimesMs == null ) sb.append("-\n");
        else sb.append(Arrays.toString(workerFlatMapDataSetGetTimesMs)).append("\n");

        sb.append(String.format(f,"WorkerFlatMapProcessMiniBatchTimesMs"));
        if(workerFlatMapProcessMiniBatchTimesMs == null ) sb.append("-\n");
        else sb.append(Arrays.toString(workerFlatMapProcessMiniBatchTimesMs)).append("\n");

        sb.append(String.format(f,"WorkerFlatMapCountNoDataInstances")).append(workerFlatMapCountNoDataInstances).append("\n");

        if(trainingMasterSpecificStats != null) sb.append(trainingMasterSpecificStats.statsAsString()).append("\n");

        return sb.toString();
    }

    public static class Builder {
        private SparkTrainingStats trainingMasterSpecificStats;
        private int[] workerFlatMapTotalTimeMs;
        private int[] workerFlatMapTotalExampleCount;
        private int[] workerFlatMapGetInitialModelTimeMs;
        private int[] workerFlatMapDataSetGetTimesMs;
        private int[] workerFlatMapProcessMiniBatchTimesMs;
        private int workerFlatMapCountNoDataInstances;

        public Builder trainingMasterSpecificStats(SparkTrainingStats trainingMasterSpecificStats){
            this.trainingMasterSpecificStats = trainingMasterSpecificStats;
            return this;
        }

        public Builder workerFlatMapTotalTimeMs(int... workerFlatMapTotalTimeMs){
            this.workerFlatMapTotalTimeMs = workerFlatMapTotalTimeMs;
            return this;
        }

        public Builder workerFlatMapTotalExampleCount(int... workerFlatMapTotalExampleCount){
            this.workerFlatMapTotalExampleCount = workerFlatMapTotalExampleCount;
            return this;
        }

        public Builder workerFlatMapGetInitialModelTimeMs(int... workerFlatMapGetInitialModelTimeMs){
            this.workerFlatMapGetInitialModelTimeMs = workerFlatMapGetInitialModelTimeMs;
            return this;
        }

        public Builder workerFlatMapDataSetGetTimesMs(int... workerFlatMapDataSetGetTimesMs){
            this.workerFlatMapDataSetGetTimesMs = workerFlatMapDataSetGetTimesMs;
            return this;
        }

        public Builder workerFlatMapProcessMiniBatchTimesMs(int... workerFlatMapProcessMiniBatchTimesMs){
            this.workerFlatMapProcessMiniBatchTimesMs = workerFlatMapProcessMiniBatchTimesMs;
            return this;
        }

        public Builder workerFlatMapCountNoDataInstances(int workerFlatMapCountNoDataInstances){
            this.workerFlatMapCountNoDataInstances = workerFlatMapCountNoDataInstances;
            return this;
        }

        public CommonSparkTrainingStats build(){
            return new CommonSparkTrainingStats(this);
        }
    }
}