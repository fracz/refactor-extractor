package org.deeplearning4j.linalg.dataset;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import org.deeplearning4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.linalg.factory.NDArrays;
import org.deeplearning4j.linalg.util.FeatureUtil;
import org.deeplearning4j.linalg.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * A data applyTransformToDestination (example/outcome pairs)
 * The outcomes are specifically for neural network encoding such that
 * any labels that are considered true are 1s. The rest are zeros.
 * @author Adam Gibson
 *
 */
public class DataSet  implements Iterable<DataSet>,Serializable {

    private static final long serialVersionUID = 1935520764586513365L;
    private static Logger log = LoggerFactory.getLogger(DataSet.class);
    private List<String> columnNames = new ArrayList<>();
    private List<String> labelNames = new ArrayList<>();
    private INDArray features,labels;

    public DataSet() {
        this(NDArrays.zeros(new int[]{1}),NDArrays.zeros(new int[]{1}));
    }

    /**
     * Creates a dataset with the specified input matrix and labels
     * @param first the feature matrix
     * @param second the labels (these should be binarized label matrices such that the specified label
     *               has a value of 1 in the desired column with the label)
     */
    public DataSet(INDArray first, INDArray second) {
        if(first.rows() != second.rows())
            throw new IllegalStateException("Invalid data applyTransformToDestination; first and second do not have equal rows. First was " + first.rows() + " second was " + second.rows());
    }



    public INDArray getFeatures() {
        return features;
    }

    public void setFeatures(INDArray features) {
        this.features = features;
    }

    public void setLabels(INDArray labels) {
        this.labels = labels;
    }

    /**
     * Clone the dataset
     * @return a clone of the dataset
     */
    public DataSet copy() {
        return new DataSet(getFeatures().dup(),getLabels().dup());
    }


    /**
     * Returns a single dataset
     * @return an empty dataset with 2 1x1 zero matrices
     */
    public static DataSet empty() {
        return new DataSet(NDArrays.zeros(new int[]{1}),NDArrays.zeros(new int[]{1}));
    }

    /**
     * Merge the list of datasets in to one list.
     * All the rows are merged in to one dataset
     * @param data the data to merge
     * @return a single dataset
     */
    public static DataSet merge(List<DataSet> data) {
        if(data.isEmpty())
            throw new IllegalArgumentException("Unable to merge empty dataset");
        DataSet first = data.get(0);
        int numExamples = totalExamples(data);
        INDArray in = NDArrays.create(numExamples,first.getFeatures().columns());
        INDArray out = NDArrays.create(numExamples,first.getLabels().columns());
        int count = 0;

        for(int i = 0; i < data.size(); i++) {
            DataSet d1 = data.get(i);
            for(int j = 0; j < d1.numExamples(); j++) {
                DataSet example = d1.get(j);
                in.putRow(count,example.getFeatures());
                out.putRow(count,example.getLabels());
                count++;
            }


        }
        return new DataSet(in,out);
    }

    /**
     * Reshapes the input in to the given rows and columns
     * @param rows the row size
     * @param cols the column size
     * @return a copy of this data applyTransformToDestination with the input resized
     */
    public DataSet reshape(int rows,int cols) {
        DataSet ret = new DataSet(getFeatures().reshape(new int[]{rows,cols}),getLabels());
        return ret;
    }


    public void multiplyBy(double num) {
        getFeatures().muli(NDArrays.scalar(num));
    }

    public void divideBy(int num) {
        getFeatures().divi(NDArrays.scalar(num));
    }

    public void shuffle() {
        List<DataSet> list = asList();
        Collections.shuffle(list);
        DataSet ret = DataSet.merge(list);
        setFeatures(ret.getFeatures());
        setLabels(ret.getLabels());
    }





    /**
     * Squeezes input data to a max and a min
     * @param min the min value to occur in the dataset
     * @param max the max value to ccur in the dataset
     */
    public void squishToRange(double min,double max) {
        for(int i = 0;i  < getFeatures().length(); i++) {
            double curr = (double) getFeatures().getScalar(i).element();
            if(curr < min)
                getFeatures().put(i,NDArrays.scalar(min));
            else if(curr> max)
                getFeatures().put(i,NDArrays.scalar(max));
        }
    }

    /**
     * Divides the input data applyTransformToDestination by the max number in each row
     */
    public void scale() {
        FeatureUtil.scaleByMax(getFeatures());
    }

    /**
     * Adds a feature for each example on to the current feature vector
     * @param toAdd the feature vector to add
     */
    public void addFeatureVector(INDArray toAdd) {
        setFeatures(NDArrays.concatHorizontally(getFeatures(), toAdd));
    }


    /**
     * The feature to add, and the example/row number
     * @param feature the feature vector to add
     * @param example the number of the example to append to
     */
    public void addFeatureVector(INDArray feature, int example) {
        getFeatures().putRow(example,NDArrays.concatHorizontally(getFeatures().getRow(example), feature));
    }

    public void normalize() {
        FeatureUtil.normalizeMatrix(getFeatures());
    }


    /**
     * Same as calling binarize(0)
     */
    public void binarize() {
        binarize(0);
    }

    /**
     * Binarizes the dataset such that any number greater than cutoff is 1 otherwise zero
     * @param cutoff the cutoff point
     */
    public void binarize(double cutoff) {
        for(int i = 0; i < getFeatures().length(); i++) {
            double curr = (double) getFeatures().getScalar(i).element();
            if (curr > cutoff)
                getFeatures().put(i, NDArrays.scalar(1));
            else
                getFeatures().put(i, NDArrays.scalar(0));
        }
    }


    /**
     * Subtract by the column means and divide by the standard deviation
     */
    public void normalizeZeroMeanZeroUnitVariance() {
        INDArray columnMeans = getFeatures().mean(1);
        INDArray columnStds = getFeatureMatrix().std(1);

        setFeatures(getFeatures().subiRowVector(columnMeans));
        columnStds.addi(NDArrays.scalar(1e-6));
        setFeatures(getFeatures().diviRowVector(columnStds));
    }

    private static int totalExamples(Collection<DataSet> coll) {
        int count = 0;
        for(DataSet d : coll)
            count += d.numExamples();
        return count;
    }


    /**
     * The number of inputs in the feature matrix
     * @return
     */
    public int numInputs() {
        return getFeatures().columns();
    }

    public void validate() {
        if(getFeatures().rows() != getLabels().rows())
            throw new IllegalStateException("Invalid dataset");
    }

    public int outcome() {
        if(this.numExamples() > 1)
            throw new IllegalStateException("Unable to derive outcome for dataset greater than one row");
        return (int) getLabels().max(Integer.MAX_VALUE).element();
    }


    /**
     * Clears the outcome matrix setting a new number of labels
     * @param labels the number of labels/columns in the outcome matrix
     * Note that this clears the labels for each example
     */
    public void setNewNumberOfLabels(int labels) {
        int examples = numExamples();
        INDArray newOutcomes = NDArrays.create(examples,labels);
        setLabels(newOutcomes);
    }

    /**
     * Sets the outcome of a particular example
     * @param example the example to applyTransformToDestination
     * @param label the label of the outcome
     */
    public void setOutcome(int example,int label) {
        if(example > numExamples())
            throw new IllegalArgumentException("No example at " + example);
        if(label > numOutcomes() || label < 0)
            throw new IllegalArgumentException("Illegal label");

        INDArray outcome = FeatureUtil.toOutcomeVector(label, numOutcomes());
        getLabels().putRow(example,outcome);
    }

    /**
     * Gets a copy of example i
     * @param i the example to getFromOrigin
     * @return the example at i (one example)
     */
    public DataSet get(int i) {
        if(i > numExamples() || i < 0)
            throw new IllegalArgumentException("invalid example number");

        return new DataSet(getFeatures().getRow(i),getLabels().getRow(i));
    }

    /**
     * Gets a copy of example i
     * @param i the example to getFromOrigin
     * @return the example at i (one example)
     */
    public DataSet get(int[] i) {
        return new DataSet(getFeatures().getRows(i),getLabels().getRows(i));
    }


    /**
     * Partitions a dataset in to mini batches where
     * each dataset in each list is of the specified number of examples
     * @param num the number to split by
     * @return the partitioned datasets
     */
    public List<List<DataSet>> batchBy(int num) {
        return Lists.partition(asList(),num);
    }




    /**
     * Strips the data applyTransformToDestination of all but the passed in labels
     * @param labels strips the data applyTransformToDestination of all but the passed in labels
     * @return the dataset with only the specified labels
     */
    public DataSet filterBy(int[] labels) {
        List<DataSet> list = asList();
        List<DataSet> newList = new ArrayList<>();
        List<Integer> labelList = new ArrayList<>();
        for(int i : labels)
            labelList.add(i);
        for(DataSet d : list) {
            if(labelList.contains(d.getLabel(d))) {
                newList.add(d);
            }
        }

        return DataSet.merge(newList);
    }


    /**
     * Strips the dataset down to the specified labels
     * and remaps them
     * @param labels the labels to strip down to
     */
    public void filterAndStrip(int[] labels) {
        DataSet filtered = filterBy(labels);
        List<Integer> newLabels = new ArrayList<>();

        //map new labels to index according to passed in labels
        Map<Integer,Integer> labelMap = new HashMap<>();

        for(int i = 0; i < labels.length; i++)
            labelMap.put(labels[i],i);

        //map examples
        for(int i = 0; i < filtered.numExamples(); i++)  {
            int o2 = filtered.get(i).outcome();
            int outcome = labelMap.get(o2);
            newLabels.add(outcome);

        }


        INDArray newLabelMatrix = NDArrays.create(filtered.numExamples(),labels.length);

        if(newLabelMatrix.rows() != newLabels.size())
            throw new IllegalStateException("Inconsistent label sizes");

        for(int i = 0; i < newLabelMatrix.rows(); i++) {
            Integer i2 = newLabels.get(i);
            if(i2 == null)
                throw new IllegalStateException("Label not found on row " + i);
            INDArray newRow = FeatureUtil.toOutcomeVector(i2, labels.length);
            newLabelMatrix.putRow(i,newRow);

        }

        setFeatures(filtered.getFeatures());
        setLabels(newLabelMatrix);
    }




    /**
     * Partitions the data applyTransformToDestination by the specified number.
     * @param num the number to split by
     * @return the partitioned data applyTransformToDestination
     */
    public List<DataSet> dataSetBatches(int num) {
        List<List<DataSet>> list =  Lists.partition(asList(),num);
        List<DataSet> ret = new ArrayList<>();
        for(List<DataSet> l : list)
            ret.add(DataSet.merge(l));
        return ret;

    }


    /**
     * Sorts the dataset by label:
     * Splits the data applyTransformToDestination such that examples are sorted by their labels.
     * A ten label dataset would produce lists with batches like the following:
     * x1   y = 1
     * x2   y = 2
     * ...
     * x10  y = 10
     * @return a list of data sets partitioned by outcomes
     */
    public List<List<DataSet>> sortAndBatchByNumLabels() {
        sortByLabel();
        return Lists.partition(asList(),numOutcomes());
    }

    public List<List<DataSet>> batchByNumLabels() {
        return Lists.partition(asList(),numOutcomes());
    }


    public List<DataSet> asList() {
        List<DataSet> list = new ArrayList<>(numExamples());
        for(int i = 0; i < numExamples(); i++)  {
            list.add(new DataSet(getFeatures().getRow(i),getLabels().getRow(i)));
        }
        return list;
    }


    /**
     * Splits a dataset in to test and train
     * @param numHoldout the number to hold out for training
     * @return the pair of datasets for the train test split
     */
    public SplitTestAndTrain splitTestAndTrain(int numHoldout) {

        if(numHoldout >= numExamples())
            throw new IllegalArgumentException("Unable to split on size larger than the number of rows");


        List<DataSet> list = asList();

        Collections.rotate(list, 3);
        Collections.shuffle(list);
        List<List<DataSet>> partition = new ArrayList<>();
        partition.add(list.subList(0, numHoldout));
        partition.add(list.subList(numHoldout, list.size()));
        DataSet train = merge(partition.get(0));
        DataSet test = merge(partition.get(1));
        return new SplitTestAndTrain(train,test);
    }


    /**
     * Returns the labels for the dataset
     * @return the labels for the dataset
     */
    public INDArray getLabels() {
        return getLabels();
    }

    /**
     * Get the feature matrix (inputs for the data)
     * @return the feature matrix for the dataset
     */
    public INDArray getFeatureMatrix() {
        return getFeatures();
    }


    /**
     * Organizes the dataset to minimize sampling error
     * while still allowing efficient batching.
     */
    public void sortByLabel() {
        Map<Integer,Queue<DataSet>> map = new HashMap<>();
        List<DataSet> data = asList();
        int numLabels = numOutcomes();
        int examples = numExamples();
        for(DataSet d : data) {
            int label = getLabel(d);
            Queue<DataSet> q = map.get(label);
            if(q == null) {
                q = new ArrayDeque<>();
                map.put(label, q);
            }
            q.add(d);
        }

        for(Integer label : map.keySet()) {
            log.info("Label " + label + " has " + map.get(label).size() + " elements");
        }

        //ideal input splits: 1 of each label in each batch
        //after we run out of ideal batches: fall back to a new strategy
        boolean optimal = true;
        for(int i = 0; i < examples; i++) {
            if(optimal) {
                for(int j = 0; j < numLabels; j++) {
                    Queue<DataSet> q = map.get(j);
                    if(q == null) {
                        optimal = false;
                        break;
                    }
                    DataSet next = q.poll();
                    //add a row; go to next
                    if(next != null) {
                        addRow(next,i);
                        i++;
                    }
                    else {
                        optimal = false;
                        break;
                    }
                }
            }
            else {
                DataSet add = null;
                for(Queue<DataSet> q : map.values()) {
                    if(!q.isEmpty()) {
                        add = q.poll();
                        break;
                    }
                }

                addRow(add,i);

            }


        }


    }


    public void addRow(DataSet d, int i) {
        if(i > numExamples() || d == null)
            throw new IllegalArgumentException("Invalid index for adding a row");
        getFeatures().putRow(i, d.getFeatures());
        getLabels().putRow(i,d.getLabels());
    }


    private int getLabel(DataSet data) {
        return (int) data.getLabels().max(Integer.MAX_VALUE).element();
    }


    public INDArray exampleSums() {
        return getFeatures().sum(1);
    }

    public INDArray exampleMaxs() {
        return getFeatures().max(1);
    }

    public INDArray exampleMeans() {
        return getFeatures().mean(1);
    }


    /**
     * Sample without replacement and a random rng
     * @param numSamples the number of samples to getFromOrigin
     * @return a sample data applyTransformToDestination without replacement
     */
    public DataSet sample(int numSamples) {
        return sample(numSamples,new MersenneTwister(System.currentTimeMillis()));
    }

    /**
     * Sample without replacement
     * @param numSamples the number of samples to getFromOrigin
     * @param rng the rng to use
     * @return the sampled dataset without replacement
     */
    public DataSet sample(int numSamples,RandomGenerator rng) {
        return sample(numSamples,rng,false);
    }

    /**
     * Sample a dataset numSamples times
     * @param numSamples the number of samples to getFromOrigin
     * @param withReplacement the rng to use
     * @return the sampled dataset without replacement
     */
    public DataSet sample(int numSamples,boolean withReplacement) {
        return sample(numSamples,new MersenneTwister(System.currentTimeMillis()),withReplacement);
    }

    /**
     * Sample a dataset
     * @param numSamples the number of samples to getFromOrigin
     * @param rng the rng to use
     * @param withReplacement whether to allow duplicates (only tracked by example row number)
     * @return the sample dataset
     */
    public DataSet sample(int numSamples,RandomGenerator rng,boolean withReplacement) {
        if(numSamples >= numExamples())
            return this;
        else {
            INDArray examples = NDArrays.create(numSamples,getFeatures().columns());
            INDArray outcomes = NDArrays.create(numSamples,numOutcomes());
            Set<Integer> added = new HashSet<Integer>();
            for(int i = 0; i < numSamples; i++) {
                int picked = rng.nextInt(numExamples());
                if(!withReplacement)
                    while(added.contains(picked)) {
                        picked = rng.nextInt(numExamples());

                    }
                examples.putRow(i,get(picked).getFeatures());
                outcomes.putRow(i,get(picked).getLabels());

            }
            return new DataSet(examples,outcomes);
        }
    }

    public void roundToTheNearest(int roundTo) {
        for(int i = 0; i < getFeatures().length(); i++) {
            double curr = (double) getFeatures().getScalar(i).element();
            getFeatures().put(i, NDArrays.scalar(MathUtils.roundDouble(curr, roundTo)));
        }
    }

    public int numOutcomes() {
        return getLabels().columns();
    }

    public int numExamples() {
        return getFeatures().rows();
    }




    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("===========INPUT===================\n")
                .append(getFeatures().toString().replaceAll(";","\n"))
                .append("\n=================OUTPUT==================\n")
                .append(getLabels().toString().replaceAll(";","\n"));
        return builder.toString();
    }




    /**
     * Gets the optional label names
     * @return
     */
    public List<String> getLabelNames() {
        return labelNames;
    }

    /**
     * Sets the label names, will throw an exception if the passed
     * in label names doesn't equal the number of outcomes
     * @param labelNames the label names to use
     */
    public void setLabelNames(List<String> labelNames) {
        if(labelNames == null || labelNames.size() != numOutcomes())
            throw new IllegalArgumentException("Unable to applyTransformToDestination label names, does not match number of possible outcomes");
        this.labelNames = labelNames;
    }

    /**
     * Optional column names of the data applyTransformToDestination, this is mainly used
     * for interpeting what columns are in the dataset
     * @return
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Sets the column names, will throw an exception if the column names
     * don't match the number of columns
     * @param columnNames
     */
    public void setColumnNames(List<String> columnNames) {
        if(columnNames.size() != numInputs())
            throw new IllegalArgumentException("Column names don't match input");
        this.columnNames = columnNames;
    }



    @Override
    public Iterator<DataSet> iterator() {
        return asList().iterator();
    }

}