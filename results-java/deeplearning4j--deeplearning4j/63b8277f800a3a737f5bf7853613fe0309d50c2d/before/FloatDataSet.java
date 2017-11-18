package org.deeplearning4j.datasets;

import java.io.*;
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
import org.deeplearning4j.berkeley.Counter;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.nn.Persistable;
import org.deeplearning4j.util.MathUtils;
import org.deeplearning4j.util.MatrixUtil;
import org.jblas.FloatMatrix;
import org.jblas.SimpleBlas;
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
public class FloatDataSet  extends Pair<FloatMatrix,FloatMatrix> implements Persistable,Iterable<FloatDataSet> {

    private static final long serialVersionUID = 1935520764586513365L;
    private static Logger log = LoggerFactory.getLogger(DataSet.class);
    private List<String> columnNames = new ArrayList<>();
    private List<String> labelNames = new ArrayList<>();

    public FloatDataSet() {
        this(FloatMatrix.zeros(1),FloatMatrix.zeros(1));
    }

    public FloatDataSet(Pair<FloatMatrix,FloatMatrix> pair) {
        this(pair.getFirst(),pair.getSecond());
    }

    public FloatDataSet(FloatMatrix first, FloatMatrix second) {
        super(first, second);
        if(first.rows != second.rows)
            throw new IllegalStateException("Invalid data applyTransformToDestination; first and second do not have equal rows. First was " + first.rows + " second was " + second.rows);


    }

    public Counter<Integer> labelDistribution() {
        Counter<Integer> ret = new Counter<>();
        for(int i = 0;i < numExamples(); i++)
            ret.incrementCount(getLabel(get(i)),1.0);
        return ret;
    }

    public DataSetIterator iterator(int batches) {
      /*  List<FloatDataSet> list = this.dataSetBatches(batches);
        return new ListDataSetIterator(list);*/
        throw new UnsupportedOperationException();
    }


    public FloatDataSet copy() {
        return new FloatDataSet(getFirst(),getSecond());
    }




    public static FloatDataSet empty() {
        return new FloatDataSet(FloatMatrix.zeros(1),FloatMatrix.zeros(1));
    }

    public static FloatDataSet merge(List<FloatDataSet> data) {
        if(data.isEmpty())
            throw new IllegalArgumentException("Unable to merge empty dataset");
        FloatDataSet first = data.get(0);
        int numExamples = totalExamples(data);
        FloatMatrix in = new FloatMatrix(numExamples,first.getFirst().columns);
        FloatMatrix out = new FloatMatrix(numExamples,first.getSecond().columns);
        int count = 0;

        for(int i = 0; i < data.size(); i++) {
            FloatDataSet d1 = data.get(i);
            for(int j = 0; j < d1.numExamples(); j++) {
                FloatDataSet example = d1.get(j);
                in.putRow(count,example.getFirst());
                out.putRow(count,example.getSecond());
                count++;
            }


        }
        return new FloatDataSet(in,out);
    }

    /**
     * Reshapes the input in to the given rows and columns
     * @param rows the row size
     * @param cols the column size
     * @return a copy of this data applyTransformToDestination with the input resized
     */
    public FloatDataSet reshape(int rows,int cols) {
        FloatDataSet ret = new FloatDataSet(getFirst().reshape(rows,cols),getSecond());
        return ret;

    }


    public void multiplyBy(int num) {
        getFirst().muli(num);
    }

    public void divideBy(int num) {
        getFirst().divi(num);
    }

    public void shuffle() {
        List<FloatDataSet> list = asList();
        Collections.shuffle(list);
        FloatDataSet ret = FloatDataSet.merge(list);
        setFirst(ret.getFirst());
        setSecond(ret.getSecond());
    }


    public void roundInputToTheNearest(int numDecimalPlaces) {
        setFirst(MatrixUtil.roundToTheNearest(getFirst(), numDecimalPlaces));
    }


    /**
     * Squeezes input data to a max and a min
     * @param min the min value to occur in the dataset
     * @param max the max value to ccur in the dataset
     */
    public void squishToRange(float min,float max) {
        for(int i = 0;i  < getFirst().length; i++) {
            if(getFirst().get(i) < min)
                getFirst().put(i,min);
            else if(getFirst().get(i) > max)
                getFirst().put(i,max);
        }
    }

    /**
     * Divides the input data applyTransformToDestination by the max number in each row
     */
    public void scale() {
        MatrixUtil.scaleByMax(getFirst());
    }

    /**
     * Adds a feature for each example on to the current feature vector
     * @param toAdd the feature vector to add
     */
    public void addFeatureVector(FloatMatrix toAdd) {
        setFirst(FloatMatrix.concatHorizontally(getFirst(), toAdd));
    }


    /**
     * The feature to add, and the example/row number
     * @param feature the feature vector to add
     * @param example the number of the example to append to
     */
    public void addFeatureVector(FloatMatrix feature, int example) {
        getFirst().putRow(example,FloatMatrix.concatHorizontally(getFirst().getRow(example), feature));
    }

    public void normalize() {
        MatrixUtil.normalizeMatrix(getFirst());
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
        for(int i = 0; i < getFirst().length; i++)
            if(getFirst().get(i) > cutoff)
                getFirst().put(i,1);
            else
                getFirst().put(i,0);
    }





    public void normalizeZeroMeanZeroUnitVariance() {
        FloatMatrix columnMeans = getFirst().columnMeans();
        FloatMatrix columnStds = MatrixUtil.columnStdDeviation(getFirst());

        setFirst(getFirst().subiRowVector(columnMeans));
        columnStds.addi(1e-6f);
        setFirst(getFirst().diviRowVector(columnStds));
    }

    private static int totalExamples(Collection<FloatDataSet> coll) {
        int count = 0;
        for(FloatDataSet d : coll)
            count += d.numExamples();
        return count;
    }



    public int numInputs() {
        return getFirst().columns;
    }

    public void validate() {
        if(getFirst().rows != getSecond().rows)
            throw new IllegalStateException("Invalid dataset");
    }

    public int outcome() {
        if(this.numExamples() > 1)
            throw new IllegalStateException("Unable to derive outcome for dataset greater than one row");
        return SimpleBlas.iamax(getSecond());
    }


    /**
     * Clears the outcome matrix setting a new number of labels
     * @param labels the number of labels/columns in the outcome matrix
     * Note that this clears the labels for each example
     */
    public void setNewNumberOfLabels(int labels) {
        int examples = numExamples();
        FloatMatrix newOutcomes = new FloatMatrix(examples,labels);
        setSecond(newOutcomes);
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

        FloatMatrix outcome = MatrixUtil.toOutcomeVectorFloat(label, numOutcomes());
        getSecond().putRow(example,outcome);
    }

    /**
     * Gets a copy of example i
     * @param i the example to getFromOrigin
     * @return the example at i (one example)
     */
    public FloatDataSet get(int i) {
        if(i > numExamples() || i < 0)
            throw new IllegalArgumentException("invalid example number");

        return new FloatDataSet(getFirst().getRow(i),getSecond().getRow(i));
    }

    public List<List<FloatDataSet>> batchBy(int num) {
        return Lists.partition(asList(),num);
    }


    /**
     * Gets the label distribution (counts of each possible outcome)
     * @return the counts of each possible outcome
     */
    public Counter<Integer> outcomeCounts() {
        List<FloatDataSet> list = asList();
        Counter<Integer> ret = new Counter<>();
        for(int i = 0; i < list.size(); i++) {
            ret.incrementCount(list.get(i).outcome(),1.0);
        }
        return ret;
    }

    /**
     * Strips the data applyTransformToDestination of all but the passed in labels
     * @param labels strips the data applyTransformToDestination of all but the passed in labels
     * @return the dataset with only the specified labels
     */
    public FloatDataSet filterBy(int[] labels) {
        List<FloatDataSet> list = asList();
        List<FloatDataSet> newList = new ArrayList<>();
        List<Integer> labelList = new ArrayList<>();
        for(int i : labels)
            labelList.add(i);
        for(FloatDataSet d : list) {
            if(labelList.contains(d.getLabel(d))) {
                newList.add(d);
            }
        }

        return FloatDataSet.merge(newList);
    }


    /**
     * Strips the dataset down to the specified labels
     * and remaps them
     * @param labels the labels to strip down to
     */
    public void filterAndStrip(int[] labels) {
        FloatDataSet filtered = filterBy(labels);
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


        FloatMatrix newLabelMatrix = new FloatMatrix(filtered.numExamples(),labels.length);

        if(newLabelMatrix.rows != newLabels.size())
            throw new IllegalStateException("Inconsistent label sizes");

        for(int i = 0; i < newLabelMatrix.rows; i++) {
            Integer i2 = newLabels.get(i);
            if(i2 == null)
                throw new IllegalStateException("Label not found on row " + i);
            FloatMatrix newRow = MatrixUtil.toOutcomeVectorFloat(i2, labels.length);
            newLabelMatrix.putRow(i,newRow);

        }

        setFirst(filtered.getFirst());
        setSecond(newLabelMatrix);
    }




    /**
     * Partitions the data applyTransformToDestination by the specified number.
     * @param num the number to split by
     * @return the partitioned data applyTransformToDestination
     */
    public List<FloatDataSet> dataSetBatches(int num) {
        List<List<FloatDataSet>> list =  Lists.partition(asList(),num);
        List<FloatDataSet> ret = new ArrayList<>();
        for(List<FloatDataSet> l : list)
            ret.add(FloatDataSet.merge(l));
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
    public List<List<FloatDataSet>> sortAndBatchByNumLabels() {
        sortByLabel();
        return Lists.partition(asList(),numOutcomes());
    }

    public List<List<FloatDataSet>> batchByNumLabels() {
        return Lists.partition(asList(),numOutcomes());
    }


    public List<FloatDataSet> asList() {
        List<FloatDataSet> list = new ArrayList<>(numExamples());
        for(int i = 0; i < numExamples(); i++)  {
            list.add(new FloatDataSet(getFirst().getRow(i),getSecond().getRow(i)));
        }
        return list;
    }

    public Pair<FloatDataSet,FloatDataSet> splitTestAndTrain(int numHoldout) {

        if(numHoldout >= numExamples())
            throw new IllegalArgumentException("Unable to split on size larger than the number of rows");


        List<FloatDataSet> list = asList();

        Collections.rotate(list, 3);
        Collections.shuffle(list);
        List<List<FloatDataSet>> partition = new ArrayList<>();
        partition.add(list.subList(0, numHoldout));
        partition.add(list.subList(numHoldout, list.size()));
        FloatDataSet train = merge(partition.get(0));
        FloatDataSet test = merge(partition.get(1));
        return new Pair<>(train,test);
    }

    /**
     * Organizes the dataset to minimize sampling error
     * while still allowing efficient batching.
     */
    public void sortByLabel() {
        Map<Integer,Queue<FloatDataSet>> map = new HashMap<>();
        List<FloatDataSet> data = asList();
        int numLabels = numOutcomes();
        int examples = numExamples();
        for(FloatDataSet d : data) {
            int label = getLabel(d);
            Queue<FloatDataSet> q = map.get(label);
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
                    Queue<FloatDataSet> q = map.get(j);
                    FloatDataSet next = q.poll();
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
                FloatDataSet add = null;
                for(Queue<FloatDataSet> q : map.values()) {
                    if(!q.isEmpty()) {
                        add = q.poll();
                        break;
                    }
                }

                addRow(add,i);

            }


        }


    }


    public void addRow(FloatDataSet d, int i) {
        if(i > numExamples() || d == null)
            throw new IllegalArgumentException("Invalid index for adding a row");
        getFirst().putRow(i, d.getFirst());
        getSecond().putRow(i,d.getSecond());
    }


    private int getLabel(FloatDataSet data) {
        return SimpleBlas.iamax(data.getSecond());
    }


    public FloatMatrix exampleSums() {
        return getFirst().columnSums();
    }

    public FloatMatrix exampleMaxs() {
        return getFirst().columnMaxs();
    }

    public FloatMatrix exampleMeans() {
        return getFirst().columnMeans();
    }

    public void saveTo(File file,boolean binary) throws IOException {
        if(file.exists())
            file.delete();
        file.createNewFile();

        if(binary) {
            DataOutputStream bos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            getFirst().out(bos);
            getSecond().out(bos);
            bos.flush();
            bos.close();

        }
        else {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            for(int i = 0; i < numExamples(); i++) {
                bos.write(getFirst().getRow(i).toString("%.3f", "[", "]", ", ", ";").getBytes());
                bos.write("\t".getBytes());
                bos.write(getSecond().getRow(i).toString("%.3f", "[", "]", ", ", ";").getBytes());
                bos.write("\n".getBytes())	;


            }
            bos.flush();
            bos.close();

        }
    }


    public static FloatDataSet load(File path) throws IOException {
        DataInputStream bis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
        FloatMatrix x = new FloatMatrix(1,1);
        FloatMatrix y = new FloatMatrix(1,1);
        x.in(bis);
        y.in(bis);
        bis.close();
        return new FloatDataSet(x,y);
    }

    /**
     * Sample without replacement and a random rng
     * @param numSamples the number of samples to getFromOrigin
     * @return a sample data applyTransformToDestination without replacement
     */
    public FloatDataSet sample(int numSamples) {
        return sample(numSamples,new MersenneTwister(System.currentTimeMillis()));
    }

    /**
     * Sample without replacement
     * @param numSamples the number of samples to getFromOrigin
     * @param rng the rng to use
     * @return the sampled dataset without replacement
     */
    public FloatDataSet sample(int numSamples,RandomGenerator rng) {
        return sample(numSamples,rng,false);
    }

    /**
     * Sample a dataset numSamples times
     * @param numSamples the number of samples to getFromOrigin
     * @param withReplacement the rng to use
     * @return the sampled dataset without replacement
     */
    public FloatDataSet sample(int numSamples,boolean withReplacement) {
        return sample(numSamples,new MersenneTwister(System.currentTimeMillis()),withReplacement);
    }

    /**
     * Sample a dataset
     * @param numSamples the number of samples to getFromOrigin
     * @param rng the rng to use
     * @param withReplacement whether to allow duplicates (only tracked by example row number)
     * @return the sample dataset
     */
    public FloatDataSet sample(int numSamples,RandomGenerator rng,boolean withReplacement) {
        if(numSamples >= numExamples())
            return this;
        else {
            FloatMatrix examples = new FloatMatrix(numSamples,getFirst().columns);
            FloatMatrix outcomes = new FloatMatrix(numSamples,numOutcomes());
            Set<Integer> added = new HashSet<Integer>();
            for(int i = 0; i < numSamples; i++) {
                int picked = rng.nextInt(numExamples());
                if(!withReplacement)
                    while(added.contains(picked)) {
                        picked = rng.nextInt(numExamples());

                    }
                examples.putRow(i,get(picked).getFirst());
                outcomes.putRow(i,get(picked).getSecond());

            }
            return new FloatDataSet(examples,outcomes);
        }
    }

    public void roundToTheNearest(int roundTo) {
        for(int i = 0; i < getFirst().length; i++) {
            float curr = getFirst().get(i);
            getFirst().put(i,MathUtils.roundFloat(curr, roundTo));
        }
    }

    public int numOutcomes() {
        return getSecond().columns;
    }

    public int numExamples() {
        return getFirst().rows;
    }




    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("===========INPUT===================\n")
                .append(getFirst().toString().replaceAll(";","\n"))
                .append("\n=================OUTPUT==================\n")
                .append(getSecond().toString().replaceAll(";","\n"));
        return builder.toString();
    }

    @Override
    public void write(OutputStream os) {
        DataOutputStream dos = new DataOutputStream(os);

        try {
            getFirst().out(dos);
            getSecond().out(dos);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
    public void load(InputStream is) {
        DataInputStream dis = new DataInputStream(is);
        try {
            getFirst().in(dis);
            getSecond().in(dis);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<FloatDataSet> iterator() {
        return asList().iterator();
    }

}