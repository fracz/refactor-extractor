package org.deeplearning4j.rntn;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.nn.Tensor;
import org.deeplearning4j.nn.activation.ActivationFunction;
import org.deeplearning4j.nn.activation.Activations;
import org.deeplearning4j.optimize.OptimizableByGradientValueMatrix;
import org.deeplearning4j.util.MatrixUtil;
import org.deeplearning4j.util.MultiDimensionalMap;
import org.deeplearning4j.util.MultiDimensionalSet;
import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.SimpleBlas;
import org.jblas.ranges.RangeUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Recursive Neural Tensor Network by Socher et. al
 *
 */
public class RNTN implements Serializable,OptimizableByGradientValueMatrix {

    private int numOuts;
    private int numHidden = 25;
    private RandomGenerator rng;
    private long seed = 123;
    private boolean useTensors = true;
    private boolean combineClassification = true;
    private boolean simplifiedModel = true;
    private boolean randomFeatureVectors = true;
    private double scalingForInit = 1.0;
    public final static String UNKNOWN_FEATURE = "UNK";
    private boolean lowerCasefeatureNames;
    protected ActivationFunction activationFunction = Activations.hardTanh();
    /** Regularization cost for the transform matrix  */
    private double regTransformMatrix = 0.001;

    /** Regularization cost for the classification matrices */
    private double regClassification = 0.0001;

    /** Regularization cost for the word vectors */
    private double regWordVector = 0.0001;

    /**
     * The value to set the learning rate for each parameter when initializing adagrad.
     */
    private double initialAdagradWeight = 0.0;

    /**
     * How many epochs between resets of the adagrad learning rates.
     * Set to 0 to never reset.
     */
    private int adagradResetFrequency = 1;

    /** Regularization cost for the transform tensor  */
    private double regTransformTensor = 0.001;

    /**
     * Nx2N+1, where N is the size of the word vectors
     */
    private MultiDimensionalMap<String, String, DoubleMatrix> binaryTransform;

    /**
     * 2Nx2NxN, where N is the size of the word vectors
     */
    private MultiDimensionalMap<String, String, Tensor> binaryTensors;

    /**
     * CxN+1, where N = size of word vectors, C is the number of classes
     */
    private Map<String, DoubleMatrix> unaryClassification;

    private Map<String, DoubleMatrix> featureVectors;

    /**
     * CxN+1, where N = size of word vectors, C is the number of classes
     */
    private MultiDimensionalMap<String, String, DoubleMatrix> binaryClassification;


    /**
     * Cached here for easy calculation of the model size;
     * MultiDimensionalMap does not return that in O(1) time
     */
    private  int numBinaryMatrices;

    /** How many elements a transformation matrix has */
    private  int binaryTransformSize;
    /** How many elements the binary transformation tensors have */
    private  int binaryTensorSize;
    /** How many elements a classification matrix has */
    private  int binaryClassificationSize;

    /**
     * Cached here for easy calculation of the model size;
     * MultiDimensionalMap does not return that in O(1) time
     */
    private  int numUnaryMatrices;

    /** How many elements a classification matrix has */
    private  int unaryClassificationSize;

    private DoubleMatrix identity;

    private List<Tree> trainingTrees;


    private Map<Integer,Double> classWeights;


    private RNTN(){
        rng = new MersenneTwister(seed);

        if (randomFeatureVectors) {
            initRandomWordVectors(trainingTrees);
        }


        MultiDimensionalSet<String, String> binaryProductions = MultiDimensionalSet.hashSet();
        if (simplifiedModel) {
            binaryProductions.add("", "");
        } else {
            // TODO
            // figure out what binary productions we have in these trees
            // Note: the current sentiment training data does not actually
            // have any constituent labels
            throw new UnsupportedOperationException("Not yet implemented");
        }

        Set<String> unaryProductions = new HashSet<>();

        if (simplifiedModel) {
            unaryProductions.add("");
        } else {
            // TODO
            // figure out what unary productions we have in these trees (preterminals only, after the collapsing)
            throw new UnsupportedOperationException("Not yet implemented");
        }


        identity = DoubleMatrix.eye(numHidden);

        binaryTransform = MultiDimensionalMap.newTreeBackedMap();
        binaryTensors = MultiDimensionalMap.newTreeBackedMap();
        binaryClassification = MultiDimensionalMap.newTreeBackedMap();

        // When making a flat model (no symantic untying) the
        // basicCategory function will return the same basic category for
        // all labels, so all entries will map to the same matrix
        for (Pair<String, String> binary : binaryProductions) {
            String left = basicCategory(binary.getFirst());
            String right = basicCategory(binary.getSecond());
            if (binaryTransform.contains(left, right)) {
                continue;
            }
            binaryTransform.put(left, right, randomTransformMatrix());
            if (useTensors) {
                binaryTensors.put(left, right, randomBinaryTensor());
            }
            if (!combineClassification) {
                binaryClassification.put(left, right, randomClassificationMatrix());
            }
        }
        numBinaryMatrices = binaryTransform.size();
        binaryTransformSize = numHidden * (2 * numHidden + 1);
        if (useTensors) {
            binaryTensorSize = numHidden * numHidden * numHidden * 4;
        } else {
            binaryTensorSize = 0;
        }
        binaryClassificationSize = (combineClassification) ? 0 : numOuts * (numHidden + 1);

        unaryClassification = new TreeMap<>();

        // When making a flat model (no symantic untying) the
        // basicCategory function will return the same basic category for
        // all labels, so all entries will map to the same matrix
        for (String unary : unaryProductions) {
            unary = basicCategory(unary);
            if (unaryClassification.containsKey(unary)) {
                continue;
            }
            unaryClassification.put(unary, randomClassificationMatrix());
        }
        numUnaryMatrices = unaryClassification.size();
        unaryClassificationSize = numOuts * (numHidden + 1);

    }



    void initRandomWordVectors(List<Tree> trainingTrees) {
        if (numHidden == 0) {
            throw new RuntimeException("Cannot create random word vectors for an unknown numHid");
        }
        Set<String> words = new HashSet<>();
        words.add(UNKNOWN_FEATURE);
        for (Tree tree : trainingTrees) {
            List<Tree> leaves = tree.getLeaves();
            for (Tree leaf : leaves) {
                String word = leaf.value();
                if (lowerCasefeatureNames) {
                    word = word.toLowerCase();
                }
                words.add(word);
            }
        }
        this.featureVectors = new TreeMap<>();
        for (String word : words) {
            DoubleMatrix vector = randomWordVector();
            featureVectors.put(word, vector);
        }
    }




    Tensor randomBinaryTensor() {
        double range = 1.0 / (4.0 * numHidden);
        Tensor tensor = Tensor.rand(numHidden * 2, numHidden * 2, numHidden, -range, range, rng);
        return tensor.scale(scalingForInit);
    }

    DoubleMatrix randomTransformMatrix() {
        DoubleMatrix binary = new DoubleMatrix(numHidden, numHidden * 2 + 1);
        // bias column values are initialized zero
        DoubleMatrix block = randomTransformBlock();
        binary.put(RangeUtils.interval(0,block.rows),RangeUtils.interval(numHidden,block.columns),block);
        return SimpleBlas.scal(scalingForInit,binary);
    }

    DoubleMatrix randomTransformBlock() {
        double range = 1.0 / (Math.sqrt((double) numHidden) * 2.0);
        return MatrixUtil.rand(numHidden, numHidden, -range, range, rng).add(identity);
    }

    /**
     * Returns matrices of the right size for either binary or unary (terminal) classification
     */
    DoubleMatrix randomClassificationMatrix() {
        // Leave the bias column with 0 values
        double range = 1.0 / (Math.sqrt((double) numHidden));
        DoubleMatrix score = MatrixUtil.rand(numOuts, numHidden + 1, -range, range, rng);
        return SimpleBlas.scal(scalingForInit,score);
    }

    DoubleMatrix randomWordVector() {
        return randomWordVector(numHidden, rng);
    }

    static DoubleMatrix randomWordVector(int size, RandomGenerator rng) {
        return MatrixUtil.uniform(rng,1,size);
    }

    public void train(List<Tree> trainingBatch) {
        this.trainingTrees = trainingBatch;
         getValueGradient();
    }



    // TODO: combine this and getClassWForNode?
    public DoubleMatrix getWForNode(Tree node) {
        if (node.children().size() == 2) {
            String leftLabel = node.children().get(0).value();
            String leftBasic = basicCategory(leftLabel);
            String rightLabel = node.children().get(1).value();
            String rightBasic = basicCategory(rightLabel);
            return binaryTransform.get(leftBasic, rightBasic);
        } else if (node.children().size() == 1) {
            throw new AssertionError("No unary transform matrices, only unary classification");
        } else {
            throw new AssertionError("Unexpected tree children size of " + node.children().size());
        }
    }

    public Tensor getTensorForNode(Tree node) {
        if (!useTensors) {
            throw new AssertionError("Not using tensors");
        }
        if (node.children().size() == 2) {
            String leftLabel = node.children().get(0).value();
            String leftBasic = basicCategory(leftLabel);
            String rightLabel = node.children().get(1).value();
            String rightBasic = basicCategory(rightLabel);
            return binaryTensors.get(leftBasic, rightBasic);
        } else if (node.children().size() == 1) {
            throw new AssertionError("No unary transform matrices, only unary classification");
        } else {
            throw new AssertionError("Unexpected tree children size of " + node.children().size());
        }
    }

    public DoubleMatrix getClassWForNode(Tree node) {
        if (combineClassification) {
            return unaryClassification.get("");
        } else if (node.children().size() == 2) {
            String leftLabel = node.children().get(0).value();
            String leftBasic = basicCategory(leftLabel);
            String rightLabel = node.children().get(1).value();
            String rightBasic = basicCategory(rightLabel);
            return binaryClassification.get(leftBasic, rightBasic);
        } else if (node.children().size() == 1) {
            String unaryLabel = node.children().get(0).value();
            String unaryBasic = basicCategory(unaryLabel);
            return unaryClassification.get(unaryBasic);
        } else {
            throw new AssertionError("Unexpected tree children size of " + node.children().size());
        }
    }



    private Tensor getTensorGradient(DoubleMatrix deltaFull, DoubleMatrix leftVector, DoubleMatrix rightVector) {
        int size = deltaFull.length;
        Tensor Wt_df = new Tensor(size*2, size*2, size);
        // TODO: combine this concatenation with computeTensorDeltaDown?
        DoubleMatrix fullVector = DoubleMatrix.concatHorizontally(leftVector, rightVector);
        for (int slice = 0; slice < size; ++slice) {
            Wt_df.setSlice(slice, SimpleBlas.scal(deltaFull.get(slice),fullVector).mmul(fullVector.transpose()));
        }
        return Wt_df;
    }



    public DoubleMatrix getFeatureVector(String word) {
        return featureVectors.get(getVocabWord(word));
    }

    public String getVocabWord(String word) {
        if (lowerCasefeatureNames) {
            word = word.toLowerCase();
        }
        if (featureVectors.containsKey(word)) {
            return word;
        }
        // TODO: go through unknown words here
        return UNKNOWN_FEATURE;
    }

    public String basicCategory(String category) {
        if (simplifiedModel) {
            return "";
        }
        throw new IllegalStateException("Only simplified model enabled");
    }

    public DoubleMatrix getUnaryClassification(String category) {
        category = basicCategory(category);
        return unaryClassification.get(category);
    }

    public DoubleMatrix getBinaryClassification(String left, String right) {
        if (combineClassification) {
            return unaryClassification.get("");
        } else {
            left = basicCategory(left);
            right = basicCategory(right);
            return binaryClassification.get(left, right);
        }
    }

    public DoubleMatrix getBinaryTransform(String left, String right) {
        left = basicCategory(left);
        right = basicCategory(right);
        return binaryTransform.get(left, right);
    }

    public Tensor getBinaryTensor(String left, String right) {
        left = basicCategory(left);
        right = basicCategory(right);
        return binaryTensors.get(left, right);
    }



    @Override
    public int getNumParameters() {
        int totalSize = 0;
        // binaryTensorSize was set to 0 if useTensors=false
        totalSize = numBinaryMatrices * (binaryTransformSize + binaryClassificationSize + binaryTensorSize);
        totalSize += numUnaryMatrices * unaryClassificationSize;
        totalSize += featureVectors.size() * numHidden;
        return totalSize;
    }

    @Override
    public DoubleMatrix getParameters() {
       return MatrixUtil.toFlattened(getNumParameters(),
               binaryTransform.values().iterator(),
               binaryClassification.values().iterator(),
               binaryTensors.values().iterator(),
               unaryClassification.values().iterator(),
               featureVectors.values().iterator());
    }










    double scaleAndRegularize(MultiDimensionalMap<String, String, DoubleMatrix> derivatives,
                              MultiDimensionalMap<String, String, DoubleMatrix> currentMatrices,
                              double scale,
                              double regCost) {
        double cost = 0.0; // the regularization cost
        for (MultiDimensionalMap.Entry<String, String, DoubleMatrix> entry : currentMatrices.entrySet()) {
            DoubleMatrix D = derivatives.get(entry.getFirstKey(), entry.getSecondKey());
            D = SimpleBlas.scal(scale,D).add(SimpleBlas.scal(regCost,entry.getValue()));
            derivatives.put(entry.getFirstKey(), entry.getSecondKey(), D);
            cost += entry.getValue().mul(entry.getValue()).sum() * regCost / 2.0;
        }
        return cost;
    }

    double scaleAndRegularize(Map<String, DoubleMatrix> derivatives,
                              Map<String, DoubleMatrix> currentMatrices,
                              double scale,
                              double regCost) {
        double cost = 0.0; // the regularization cost
        for (Map.Entry<String, DoubleMatrix> entry : currentMatrices.entrySet()) {
            DoubleMatrix D = derivatives.get(entry.getKey());
            D = SimpleBlas.scal(scale,D).add(SimpleBlas.scal(regCost,entry.getValue()));
            derivatives.put(entry.getKey(), D);
            cost += entry.getValue().mul(entry.getValue()).sum() * regCost / 2.0;
        }
        return cost;
    }

    double scaleAndRegularizeTensor(MultiDimensionalMap<String, String, Tensor> derivatives,
                                    MultiDimensionalMap<String, String, Tensor> currentMatrices,
                                    double scale,
                                    double regCost) {
        double cost = 0.0; // the regularization cost
        for (MultiDimensionalMap.Entry<String, String, Tensor> entry : currentMatrices.entrySet()) {
            Tensor D = derivatives.get(entry.getFirstKey(), entry.getSecondKey());
            D = D.scale(scale).add(entry.getValue().scale(regCost));
            derivatives.put(entry.getFirstKey(), entry.getSecondKey(), D);
            cost += entry.getValue().mul(entry.getValue()).sum() * regCost / 2.0;
        }
        return cost;
    }

    private void backpropDerivativesAndError(Tree tree,
                                             MultiDimensionalMap<String, String, DoubleMatrix> binaryTD,
                                             MultiDimensionalMap<String, String, DoubleMatrix> binaryCD,
                                             MultiDimensionalMap<String, String, Tensor> binaryTensorTD,
                                             Map<String, DoubleMatrix> unaryCD,
                                             Map<String, DoubleMatrix> wordVectorD) {
        DoubleMatrix delta = new DoubleMatrix(numHidden, 1);
        backpropDerivativesAndError(tree, binaryTD, binaryCD, binaryTensorTD, unaryCD, wordVectorD, delta);
    }

    private void backpropDerivativesAndError(Tree tree,
                                             MultiDimensionalMap<String, String, DoubleMatrix> binaryTD,
                                             MultiDimensionalMap<String, String, DoubleMatrix> binaryCD,
                                             MultiDimensionalMap<String, String, Tensor> binaryTensorTD,
                                             Map<String, DoubleMatrix> unaryCD,
                                             Map<String, DoubleMatrix> wordVectorD,
                                             DoubleMatrix deltaUp) {
        if (tree.isLeaf()) {
            return;
        }

        DoubleMatrix currentVector = tree.vector();
        String category = tree.label();
        category = basicCategory(category);

        // Build a vector that looks like 0,0,1,0,0 with an indicator for the correct class
        DoubleMatrix goldLabel = new DoubleMatrix(numOuts, 1);
        int goldClass = tree.goldLabel();
        if (goldClass >= 0) {
            goldLabel.put(goldClass, 1.0);
        }

        double nodeWeight = classWeights.get(goldClass);

        DoubleMatrix predictions = tree.prediction();

        // If this is an unlabeled class, set deltaClass to 0.  We could
        // make this more efficient by eliminating various of the below
        // calculations, but this would be the easiest way to handle the
        // unlabeled class
        DoubleMatrix deltaClass = goldClass >= 0 ? SimpleBlas.scal(nodeWeight,predictions.sub(goldLabel)) : new DoubleMatrix(predictions.rows, predictions.columns);
        DoubleMatrix localCD = deltaClass.mmul(DoubleMatrix.concatHorizontally(currentVector,DoubleMatrix.ones(1,currentVector.columns)).transpose());

        double error = -(MatrixFunctions.log(predictions).mul(goldLabel).sum());
        error = error * nodeWeight;
        tree.setError(error);

        if (tree.isPreTerminal()) { // below us is a word vector
            unaryCD.put(category, unaryCD.get(category).add(localCD));

            String word = tree.children().get(0).label();
            word = getVocabWord(word);


            DoubleMatrix currentVectorDerivative = activationFunction.apply(currentVector);
            DoubleMatrix deltaFromClass = getUnaryClassification(category).transpose().mmul(deltaClass);
            deltaFromClass = deltaFromClass.get(RangeUtils.interval(0, 1), RangeUtils.interval(0, numHidden)).mul(currentVectorDerivative);
            DoubleMatrix deltaFull = deltaFromClass.add(deltaUp);
            wordVectorD.put(word, wordVectorD.get(word).add(deltaFull));


        } else {
            // Otherwise, this must be a binary node
            String leftCategory = basicCategory(tree.children().get(0).label());
            String rightCategory = basicCategory(tree.children().get(1).label());
            if (combineClassification) {
                unaryCD.put("", unaryCD.get("").add(localCD));
            } else {
                binaryCD.put(leftCategory, rightCategory, binaryCD.get(leftCategory, rightCategory).add(localCD));
            }

            DoubleMatrix currentVectorDerivative = activationFunction.apply(currentVector);
            DoubleMatrix deltaFromClass = getBinaryClassification(leftCategory, rightCategory).transpose().mmul(deltaClass);
            deltaFromClass = deltaFromClass.get(RangeUtils.interval( 0, 1),RangeUtils.interval(0, numHidden)).mul(currentVectorDerivative);
            DoubleMatrix deltaFull = deltaFromClass.add(deltaUp);

            DoubleMatrix leftVector =tree.children().get(0).vector();
            DoubleMatrix rightVector = tree.children().get(1).vector();

            DoubleMatrix childrenVector = DoubleMatrix.concatHorizontally(leftVector, rightVector);


            DoubleMatrix W_df = deltaFull.mmul(childrenVector.transpose());
            binaryTD.put(leftCategory, rightCategory, binaryTD.get(leftCategory, rightCategory).add(W_df));

            DoubleMatrix deltaDown;
            if (useTensors) {
                Tensor Wt_df = getTensorGradient(deltaFull, leftVector, rightVector);
                binaryTensorTD.put(leftCategory, rightCategory, binaryTensorTD.get(leftCategory, rightCategory).add(Wt_df));
                deltaDown = computeTensorDeltaDown(deltaFull, leftVector, rightVector, getBinaryTransform(leftCategory, rightCategory), getBinaryTensor(leftCategory, rightCategory));
            } else {
                deltaDown = getBinaryTransform(leftCategory, rightCategory).transpose().mmul(deltaFull);
            }

            DoubleMatrix leftDerivative = activationFunction.apply(leftVector);
            DoubleMatrix rightDerivative = activationFunction.apply(rightVector);
            DoubleMatrix leftDeltaDown = deltaDown.get(RangeUtils.interval( 0, 1),RangeUtils.interval(0, deltaFull.rows));
            DoubleMatrix rightDeltaDown = deltaDown.get(RangeUtils.interval( 0, 1),RangeUtils.interval(deltaFull.rows, deltaFull.rows * 2));
            backpropDerivativesAndError(tree.children().get(0), binaryTD, binaryCD, binaryTensorTD, unaryCD, wordVectorD, leftDerivative.mul(leftDeltaDown));
            backpropDerivativesAndError(tree.children().get(1), binaryTD, binaryCD, binaryTensorTD, unaryCD, wordVectorD, rightDerivative.mul(rightDeltaDown));
        }
    }

    private DoubleMatrix computeTensorDeltaDown(DoubleMatrix deltaFull, DoubleMatrix leftVector, DoubleMatrix rightVector,
                                                DoubleMatrix W, Tensor Wt) {
        DoubleMatrix WTDelta = W.transpose().mmul(deltaFull);
        DoubleMatrix WTDeltaNoBias = WTDelta.get(RangeUtils.interval( 0, 1),RangeUtils.interval(0, deltaFull.rows * 2));
        int size = deltaFull.length;
        DoubleMatrix deltaTensor = new DoubleMatrix(size*2, 1);
        DoubleMatrix fullVector = DoubleMatrix.concatHorizontally(leftVector, rightVector);
        for (int slice = 0; slice < size; ++slice) {
            DoubleMatrix scaledFullVector = SimpleBlas.scal(deltaFull.get(slice),fullVector);
            deltaTensor = deltaTensor.add(Wt.getSlice(slice).add(Wt.getSlice(slice).transpose()).mmul(scaledFullVector));
        }
        return deltaTensor.add(WTDeltaNoBias);
    }


    /**
     * This is the method to call for assigning labels and node vectors
     * to the Tree.  After calling this, each of the non-leaf nodes will
     * have the node vector and the predictions of their classes
     * assigned to that subtree's node.
     */
    public void forwardPropagateTree(Tree tree) {
        DoubleMatrix nodeVector = null;
        DoubleMatrix classification = null;

        if (tree.isLeaf()) {
            // We do nothing for the leaves.  The preterminals will
            // calculate the classification for this word/tag.  In fact, the
            // recursion should not have gotten here (unless there are
            // degenerate trees of just one leaf)
            throw new AssertionError("We should not have reached leaves in forwardPropagate");
        } else if (tree.isPreTerminal()) {
            classification = getUnaryClassification(tree.label());
            String word = tree.children().get(0).label();
            DoubleMatrix wordVector = getFeatureVector(word);
            nodeVector = MatrixFunctions.tanh(wordVector);
        } else if (tree.children().size() == 1) {
            throw new AssertionError("Non-preterminal nodes of size 1 should have already been collapsed");
        } else if (tree.children().size() == 2) {
            forwardPropagateTree(tree.children().get(0));
            forwardPropagateTree(tree.children().get(1));

            String leftCategory = tree.children().get(0).label();
            String rightCategory = tree.children().get(1).label();
            DoubleMatrix W = getBinaryTransform(leftCategory, rightCategory);
            classification = getBinaryClassification(leftCategory, rightCategory);

            DoubleMatrix leftVector = tree.children().get(0).vector();
            DoubleMatrix rightVector = tree.children().get(1).vector();
            DoubleMatrix childrenVector = DoubleMatrix.concatHorizontally(leftVector, rightVector);
            if (useTensors) {
                Tensor tensor = getBinaryTensor(leftCategory, rightCategory);
                DoubleMatrix tensorIn = DoubleMatrix.concatHorizontally(leftVector, rightVector);
                DoubleMatrix tensorOut = tensor.bilinearProducts(tensorIn);
                nodeVector = activationFunction.apply(W.mmul(childrenVector).add(tensorOut));
            } else {
                nodeVector = activationFunction.apply(W.mmul(childrenVector));
            }
        } else {
            throw new AssertionError("Tree not correctly binarized");
        }

        DoubleMatrix predictions = MatrixUtil.softmax(classification.mmul(DoubleMatrix.concatHorizontally(nodeVector, DoubleMatrix.ones(nodeVector.columns, 1))));

        int index = SimpleBlas.iamax(predictions);
        tree.setPrediction(predictions);
        tree.setVector(nodeVector);
        tree.setLabel(String.valueOf(index));
    }

    @Override
    public double getParameter(int index) {
        return 0;
    }

    @Override
    public void setParameters(DoubleMatrix params) {

    }

    @Override
    public void setParameter(int index, double value) {

    }

    @Override
    public DoubleMatrix getValueGradient() {
        // We use TreeMap for each of these so that they stay in a
        // canonical sorted order
        // TODO: factor out the initialization routines
        // binaryTD stands for Transform Derivatives (see the SentimentModel)
        MultiDimensionalMap<String, String, DoubleMatrix> binaryTD = MultiDimensionalMap.newTreeBackedMap();
        // the derivatives of the tensors for the binary nodes
        MultiDimensionalMap<String, String, Tensor> binaryTensorTD = MultiDimensionalMap.newTreeBackedMap();
        // binaryCD stands for Classification Derivatives
        MultiDimensionalMap<String, String, DoubleMatrix> binaryCD = MultiDimensionalMap.newTreeBackedMap();

        // unaryCD stands for Classification Derivatives
        Map<String, DoubleMatrix> unaryCD = new TreeMap<>();

        // word vector derivatives
        Map<String, DoubleMatrix> wordVectorD = new TreeMap<>();

        for (MultiDimensionalMap.Entry<String, String, DoubleMatrix> entry : binaryTransform.entrySet()) {
            int numRows = entry.getValue().rows;
            int numCols = entry.getValue().columns;

            binaryTD.put(entry.getFirstKey(), entry.getSecondKey(), new DoubleMatrix(numRows, numCols));
        }

        if (!combineClassification) {
            for (MultiDimensionalMap.Entry<String, String, DoubleMatrix> entry :  binaryClassification.entrySet()) {
                int numRows = entry.getValue().rows;
                int numCols = entry.getValue().columns;

                binaryCD.put(entry.getFirstKey(), entry.getSecondKey(), new DoubleMatrix(numRows, numCols));
            }
        }

        if (useTensors) {
            for (MultiDimensionalMap.Entry<String, String, Tensor> entry : binaryTensors.entrySet()) {
                int numRows = entry.getValue().rows();
                int numCols = entry.getValue().columns;
                int numSlices = entry.getValue().slices();

                binaryTensorTD.put(entry.getFirstKey(), entry.getSecondKey(), new Tensor(numRows, numCols, numSlices));
            }
        }

        for (Map.Entry<String, DoubleMatrix> entry : unaryClassification.entrySet()) {
            int numRows = entry.getValue().rows;
            int numCols = entry.getValue().columns;
            unaryCD.put(entry.getKey(), new DoubleMatrix(numRows, numCols));
        }
        for (Map.Entry<String, DoubleMatrix> entry : featureVectors.entrySet()) {
            int numRows = entry.getValue().rows;
            int numCols = entry.getValue().columns;
            wordVectorD.put(entry.getKey(), new DoubleMatrix(numRows, numCols));
        }

        // TODO: This part can easily be parallelized
        List<Tree> forwardPropTrees = new ArrayList<>();
        for (Tree tree : trainingTrees) {
            Tree trainingTree = tree.clone();
            // this will attach the error vectors and the node vectors
            // to each node in the tree
            forwardPropagateTree(trainingTree);
            forwardPropTrees.add(trainingTree);
        }

        // TODO: we may find a big speedup by separating the derivatives and then summing
        double error = 0.0;
        for (Tree tree : forwardPropTrees) {
            backpropDerivativesAndError(tree, binaryTD, binaryCD, binaryTensorTD, unaryCD, wordVectorD);
            error += tree.errorSum();
        }

        // scale the error by the number of sentences so that the
        // regularization isn't drowned out for large training batchs
        double scale = (1.0 / trainingTrees.size());
        double value = error * scale;

        value += scaleAndRegularize(binaryTD, binaryTransform, scale, regTransformMatrix);
        value += scaleAndRegularize(binaryCD, binaryClassification, scale, regClassification);
        value += scaleAndRegularizeTensor(binaryTensorTD, binaryTensors, scale, regTransformTensor);
        value += scaleAndRegularize(unaryCD, unaryClassification, scale, regClassification);
        value += scaleAndRegularize(wordVectorD, featureVectors, scale, regWordVector);

        DoubleMatrix derivative = MatrixUtil.toFlattened(getNumParameters(), binaryTD.values().iterator(), binaryCD.values().iterator(), binaryTensorTD.values().iterator()
                , unaryCD.values().iterator(), wordVectorD.values().iterator());

        return derivative;
    }

    @Override
    public double getValue() {
        return 0;
    }
}