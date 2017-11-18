package edu.stanford.nlp.parser.shiftreduce;

import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.common.ArgUtils;
import edu.stanford.nlp.parser.common.ParserGrammar;
import edu.stanford.nlp.parser.common.ParserQuery;
import edu.stanford.nlp.parser.lexparser.BinaryHeadFinder;
import edu.stanford.nlp.parser.lexparser.EvaluateTreebank;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.parser.lexparser.TreebankLangParserParams;
import edu.stanford.nlp.parser.lexparser.TreeBinarizer;
import edu.stanford.nlp.parser.metrics.Eval;
import edu.stanford.nlp.trees.BasicCategoryTreeTransformer;
import edu.stanford.nlp.trees.CompositeTreeTransformer;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.Trees;
import edu.stanford.nlp.util.CollectionUtils;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.HashIndex;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.ReflectionLoading;
import edu.stanford.nlp.util.ScoredComparator;
import edu.stanford.nlp.util.ScoredObject;

public class ShiftReduceParser implements Serializable, ParserGrammar {
  final Index<Transition> transitionIndex;
  final Map<String, List<ScoredObject<Integer>>> featureWeights;

  final ShiftReduceOptions op;

  final FeatureFactory featureFactory;

  public ShiftReduceParser(ShiftReduceOptions op) {
    this.transitionIndex = new HashIndex<Transition>();
    this.featureWeights = Generics.newHashMap();
    this.op = op;
    this.featureFactory = ReflectionLoading.loadByReflection(op.featureFactoryClass);
  }

  @Override
  public Options getOp() {
    return op;
  }

  @Override
  public TreebankLangParserParams getTLPParams() {
    return op.tlpParams;
  }

  @Override
  public TreebankLanguagePack treebankLanguagePack() {
    return getTLPParams().treebankLanguagePack();
  }

  public ShiftReduceParser deepCopy() {
    // TODO: should we deep copy the options?
    ShiftReduceParser copy = new ShiftReduceParser(op);
    for (Transition transition : transitionIndex) {
      copy.transitionIndex.add(transition);
    }
    for (String feature : featureWeights.keySet()) {
      List<ScoredObject<Integer>> newWeights = Generics.newArrayList();
      for (ScoredObject<Integer> weight : featureWeights.get(feature)) {
        newWeights.add(new ScoredObject<Integer>(weight.object(), weight.score()));
      }
      if (newWeights.size() > 0) {
        copy.featureWeights.put(feature, newWeights);
      }
    }
    return copy;
  }

  public static ShiftReduceParser averageModels(Collection<ScoredObject<ShiftReduceParser>> scoredModels) {
    if (scoredModels.size() == 0) {
      throw new IllegalArgumentException("Cannot average empty models");
    }

    System.err.print("Averaging models with scores");
    for (ScoredObject<ShiftReduceParser> model : scoredModels) {
      System.err.print(" " + NF.format(model.score()));
    }
    System.err.println();

    List<ShiftReduceParser> models = CollectionUtils.transformAsList(scoredModels, new Function<ScoredObject<ShiftReduceParser>, ShiftReduceParser>() { public ShiftReduceParser apply(ScoredObject<ShiftReduceParser> object) { return object.object(); }});

    ShiftReduceParser firstModel = models.iterator().next();
    ShiftReduceOptions op = firstModel.op;
    // TODO: should we deep copy the options?
    ShiftReduceParser copy = new ShiftReduceParser(op);

    for (Transition transition : firstModel.transitionIndex) {
      copy.transitionIndex.add(transition);
    }

    for (ShiftReduceParser model : models) {
      if (!model.transitionIndex.equals(copy.transitionIndex)) {
        throw new IllegalArgumentException("Can only average models with the same transition index");
      }
    }

    Set<String> features = Generics.newHashSet();
    for (ShiftReduceParser model : models) {
      for (String feature : model.featureWeights.keySet()) {
        features.add(feature);
      }
    }

    for (String feature : features) {
      List<ScoredObject<Integer>> weights = Generics.newArrayList();
      copy.featureWeights.put(feature, weights);
    }

    int numModels = models.size();
    for (String feature : features) {
      for (ShiftReduceParser model : models) {
        if (!model.featureWeights.containsKey(feature)) {
          continue;
        }
        for (ScoredObject<Integer> weight : model.featureWeights.get(feature)) {
          updateWeight(copy.featureWeights.get(feature), weight.object(), weight.score() / numModels);
        }
      }
    }

    return copy;
  }

  public static void updateWeight(List<ScoredObject<Integer>> weights, int transition, double delta) {
    for (int i = 0; i < weights.size(); ++i) {
      ScoredObject<Integer> weight = weights.get(i);
      if (weight.object() == transition) {
        weight.setScore(weight.score() + delta);
        return;
      } else if (weight.object() > transition) {
        weights.add(i, new ScoredObject<Integer>(transition, delta));
        return;
      }
    }
    weights.add(new ScoredObject<Integer>(transition, delta));
  }

  public ParserQuery parserQuery() {
    return new ShiftReduceParserQuery(this);
  }

  public void condenseFeatures() {
    // iterate over feature weight map
    // for each feature, remove all transitions with score of 0
    // any feature with no transitions left is then removed
    Iterator<String> featureIt = featureWeights.keySet().iterator();
    while (featureIt.hasNext()) {
      String feature = featureIt.next();
      List<ScoredObject<Integer>> weights = featureWeights.get(feature);
      Iterator<ScoredObject<Integer>> weightIt = weights.iterator();
      while (weightIt.hasNext()) {
        ScoredObject<Integer> score = weightIt.next();
        if (score.score() == 0.0) {
          weightIt.remove();
        }
      }
      if (weights.size() == 0) {
        featureIt.remove();
      }
    }
  }


  public void outputStats() {
    int numWeights = 0;
    for (String feature : featureWeights.keySet()) {
      numWeights += featureWeights.get(feature).size();
    }
    System.err.println("Number of non-zero weights: " + numWeights);

    System.err.println("Number of known features: " + featureWeights.size());
    int wordLength = 0;
    for (String feature : featureWeights.keySet()) {
      wordLength += feature.length();
    }
    System.err.println("Total word length: " + wordLength);

    System.err.println("Number of transitions: " + transitionIndex.size());
  }

  /** TODO: add an eval which measures transition accuracy? */
  public List<Eval> getExtraEvals() {
    return Collections.emptyList();
  }

  public ScoredObject<Integer> findHighestScoringTransition(State state, Set<String> features, boolean requireLegal) {
    Collection<ScoredObject<Integer>> transitions = findHighestScoringTransitions(state, features, requireLegal, 1);
    if (transitions.size() == 0) {
      return null;
    }
    return transitions.iterator().next();
  }

  public Collection<ScoredObject<Integer>> findHighestScoringTransitions(State state, Set<String> features, boolean requireLegal, int numTransitions) {
    double[] scores = new double[transitionIndex.size()];
    for (String feature : features) {
      List<ScoredObject<Integer>> weights = featureWeights.get(feature);
      if (weights == null) {
        // Features not in our index are ignored
        continue;
      }
      for (ScoredObject<Integer> weight : weights) {
        scores[weight.object()] += weight.score();
      }
    }

    PriorityQueue<ScoredObject<Integer>> queue = new PriorityQueue<ScoredObject<Integer>>(numTransitions + 1, ScoredComparator.ASCENDING_COMPARATOR);
    for (int i = 0; i < scores.length; ++i) {
      if (!requireLegal || transitionIndex.get(i).isLegal(state)) {
        queue.add(new ScoredObject<Integer>(i, scores[i]));
        if (queue.size() > numTransitions) {
          queue.poll();
        }
      }
    }

    return queue;
  }

  public static State initialStateFromGoldTagTree(Tree tree) {
    return initialStateFromTaggedSentence(tree.taggedYield());
  }

  public static State initialStateFromTaggedSentence(List<? extends HasWord> words) {
    List<Tree> preterminals = Generics.newArrayList();
    for (HasWord hw : words) {
      CoreLabel wordLabel = new CoreLabel();
      wordLabel.setValue(hw.word());
      if (!(hw instanceof HasTag)) {
        throw new RuntimeException("Expected tagged words");
      }
      String tag = ((HasTag) hw).tag();
      if (tag == null) {
        throw new RuntimeException("Word is not tagged");
      }
      CoreLabel tagLabel = new CoreLabel();
      tagLabel.setValue(((HasTag) hw).tag());

      LabeledScoredTreeNode wordNode = new LabeledScoredTreeNode(wordLabel);
      LabeledScoredTreeNode tagNode = new LabeledScoredTreeNode(tagLabel);
      tagNode.addChild(wordNode);

      wordLabel.set(TreeCoreAnnotations.HeadWordAnnotation.class, wordNode);
      wordLabel.set(TreeCoreAnnotations.HeadTagAnnotation.class, tagNode);
      tagLabel.set(TreeCoreAnnotations.HeadWordAnnotation.class, wordNode);
      tagLabel.set(TreeCoreAnnotations.HeadTagAnnotation.class, tagNode);

      preterminals.add(tagNode);
    }
    return new State(preterminals);
  }


  private static final NumberFormat NF = new DecimalFormat("0.00");
  private static final NumberFormat FILENAME = new DecimalFormat("0000");

  // java -mx5g edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser -testTreebank ../data/parsetrees/wsj.dev.mrg -serializedPath foo.ser.gz
  // java -mx10g edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser -trainTreebank ../data/parsetrees/wsj.train.mrg -devTreebank ../data/parsetrees/wsj.dev.mrg -serializedPath foo.ser.gz
  public static void main(String[] args) {
    List<String> remainingArgs = Generics.newArrayList();

    String trainTreebankPath = null;
    FileFilter trainTreebankFilter = null;
    String testTreebankPath = null;
    FileFilter testTreebankFilter = null;
    String devTreebankPath = null;
    FileFilter devTreebankFilter = null;

    String serializedPath = null;

    String tlppClass = null;

    for (int argIndex = 0; argIndex < args.length; ) {
      if (args[argIndex].equalsIgnoreCase("-trainTreebank")) {
        Pair<String, FileFilter> treebankDescription = ArgUtils.getTreebankDescription(args, argIndex, "-trainTreebank");
        argIndex = argIndex + ArgUtils.numSubArgs(args, argIndex) + 1;
        trainTreebankPath = treebankDescription.first();
        trainTreebankFilter = treebankDescription.second();
      } else if (args[argIndex].equalsIgnoreCase("-testTreebank")) {
        Pair<String, FileFilter> treebankDescription = ArgUtils.getTreebankDescription(args, argIndex, "-testTreebank");
        argIndex = argIndex + ArgUtils.numSubArgs(args, argIndex) + 1;
        testTreebankPath = treebankDescription.first();
        testTreebankFilter = treebankDescription.second();
      } else if (args[argIndex].equalsIgnoreCase("-devTreebank")) {
        Pair<String, FileFilter> treebankDescription = ArgUtils.getTreebankDescription(args, argIndex, "-devTreebank");
        argIndex = argIndex + ArgUtils.numSubArgs(args, argIndex) + 1;
        devTreebankPath = treebankDescription.first();
        devTreebankFilter = treebankDescription.second();
      } else if (args[argIndex].equalsIgnoreCase("-serializedPath")) {
        serializedPath = args[argIndex + 1];
        argIndex += 2;
      } else if (args[argIndex].equalsIgnoreCase("-tlpp")) {
        tlppClass = args[argIndex] + 1;
        argIndex += 2;
      } else {
        remainingArgs.add(args[argIndex]);
        ++argIndex;
      }
    }

    String[] newArgs = new String[remainingArgs.size()];
    newArgs = remainingArgs.toArray(newArgs);

    if (trainTreebankPath == null && serializedPath == null) {
      throw new IllegalArgumentException("Must specify a treebank to train from with -trainTreebank");
    }

    ShiftReduceParser parser = null;

    if (trainTreebankPath != null) {
      ShiftReduceOptions op = new ShiftReduceOptions();
      op.setOptions("-forceTags", "-debugOutputFrequency", "1");
      if (tlppClass != null) {
        op.tlpParams = ReflectionLoading.loadByReflection(tlppClass);
      }
      op.setOptions(newArgs);

      if (op.trainOptions.randomSeed == 0) {
        op.trainOptions.randomSeed = (new Random()).nextLong();
        System.err.println("Random seed not set by options, using " + op.trainOptions.randomSeed);
      }

      TreeBinarizer binarizer = new TreeBinarizer(op.tlpParams.headFinder(), op.tlpParams.treebankLanguagePack(), false, false, 0, false, false, 0.0, false, true, true);
      BasicCategoryTreeTransformer basicTransformer = new BasicCategoryTreeTransformer(op.langpack());
      CompositeTreeTransformer transformer = new CompositeTreeTransformer();
      transformer.addTransformer(binarizer);
      transformer.addTransformer(basicTransformer);

      System.err.println("Loading training trees from " + trainTreebankPath);
      Treebank trainTreebank = op.tlpParams.memoryTreebank();
      trainTreebank.loadPath(trainTreebankPath, trainTreebankFilter);
      trainTreebank = trainTreebank.transform(transformer);
      System.err.println("Read in " + trainTreebank.size() + " trees from " + trainTreebankPath);

      HeadFinder binaryHeadFinder = new BinaryHeadFinder(op.tlpParams.headFinder());
      List<Tree> binarizedTrees = Generics.newArrayList();
      for (Tree tree : trainTreebank) {
        Trees.convertToCoreLabels(tree);
        tree.percolateHeadAnnotations(binaryHeadFinder);
        binarizedTrees.add(tree);
      }

      parser = new ShiftReduceParser(op);

      Index<Transition> transitionIndex = parser.transitionIndex;
      FeatureFactory featureFactory = parser.featureFactory;
      Index<String> featureIndex = new HashIndex<String>();
      for (Tree tree : binarizedTrees) {
        List<Transition> transitions = CreateTransitionSequence.createTransitionSequence(tree, op.compoundUnaries);
        transitionIndex.addAll(transitions);

        State state = ShiftReduceParser.initialStateFromGoldTagTree(tree);
        for (Transition transition : transitions) {
          Set<String> features = featureFactory.featurize(state);
          featureIndex.addAll(features);
          state = transition.apply(state);
        }
      }

      System.err.println("Number of unique features: " + featureIndex.size());
      System.err.println("Number of transitions: " + transitionIndex.size());
      System.err.println("Feature space will be " + (featureIndex.size() * transitionIndex.size()));

      Map<String, List<ScoredObject<Integer>>> featureWeights = parser.featureWeights;
      for (String feature : featureIndex) {
        List<ScoredObject<Integer>> weights = Generics.newArrayList();
        featureWeights.put(feature, weights);
      }

      Random random = new Random(parser.op.trainOptions.randomSeed);

      Treebank devTreebank = null;
      if (devTreebankPath != null) {
        System.err.println("Loading dev trees from " + devTreebankPath);
        devTreebank = parser.op.tlpParams.memoryTreebank();
        devTreebank.loadPath(devTreebankPath, devTreebankFilter);
        System.err.println("Loaded " + devTreebank.size() + " trees");
      }

      double bestScore = 0.0;
      int bestIteration = 0;
      PriorityQueue<ScoredObject<ShiftReduceParser>> bestModels = null;
      if (parser.op.averagedModels > 0) {
        bestModels = new PriorityQueue<ScoredObject<ShiftReduceParser>>(parser.op.averagedModels + 1, ScoredComparator.ASCENDING_COMPARATOR);
      }

      for (int iteration = 1; iteration <= parser.op.trainOptions.trainingIterations; ++iteration) {
        int numCorrect = 0;
        int numWrong = 0;
        Collections.shuffle(binarizedTrees, random);
        for (Tree tree : binarizedTrees) {
          List<Transition> transitions = CreateTransitionSequence.createTransitionSequence(tree, op.compoundUnaries);
          State state = ShiftReduceParser.initialStateFromGoldTagTree(tree);
          for (Transition transition : transitions) {
            int transitionNum = transitionIndex.indexOf(transition);
            Set<String> features = featureFactory.featurize(state);
            int predictedNum = parser.findHighestScoringTransition(state, features, false).object();
            Transition predicted = transitionIndex.get(predictedNum);
            if (transitionNum == predictedNum) {
              numCorrect++;
            } else {
              numWrong++;
              for (String feature : features) {
                List<ScoredObject<Integer>> weights = featureWeights.get(feature);
                // TODO: allow weighted features, weighted training, etc
                ShiftReduceParser.updateWeight(weights, transitionNum, 1.0);
                ShiftReduceParser.updateWeight(weights, predictedNum, -1.0);
              }
            }
            state = transition.apply(state);
          }
        }
        System.err.println("Iteration " + iteration + " complete");
        System.err.println("While training, got " + numCorrect + " transitions correct and " + numWrong + " transitions wrong");


        double labelF1 = 0.0;
        if (devTreebank != null) {
          EvaluateTreebank evaluator = new EvaluateTreebank(parser.op, null, parser);
          evaluator.testOnTreebank(devTreebank);
          labelF1 = evaluator.getLBScore();
          System.err.println("Label F1 after " + iteration + " iterations: " + labelF1);

          if (labelF1 > bestScore) {
            System.err.println("New best dev score (previous best " + bestScore + ")");
            bestScore = labelF1;
            bestIteration = iteration;
          } else {
            System.err.println("Failed to improve for " + (iteration - bestIteration) + " iteration(s) on previous best score of " + bestScore);
            if (op.trainOptions.stalledIterationLimit > 0 && (iteration - bestIteration >= op.trainOptions.stalledIterationLimit)) {
              System.err.println("Failed to improve for too long, stopping training");
              break;
            }
          }

          if (bestModels != null) {
            bestModels.add(new ScoredObject<ShiftReduceParser>(parser.deepCopy(), labelF1));
            if (bestModels.size() > parser.op.averagedModels) {
              bestModels.poll();
            }
          }
        }
        if (serializedPath != null && parser.op.trainOptions.debugOutputFrequency > 0) {
          String tempName = serializedPath.substring(0, serializedPath.length() - 7) + "-" + FILENAME.format(iteration) + "-" + NF.format(labelF1) + ".ser.gz";
          try {
            IOUtils.writeObjectToFile(parser, tempName);
          } catch (IOException e) {
            throw new RuntimeIOException(e);
          }
        }
      }

      if (bestModels != null) {
        if (op.cvAveragedModels && devTreebank != null) {
          List<ScoredObject<ShiftReduceParser>> models = Generics.newArrayList();
          while (bestModels.size() > 0) {
            models.add(bestModels.poll());
          }
          Collections.reverse(models);
          double bestF1 = 0.0;
          int bestSize = 0;
          for (int i = 1; i < models.size(); ++i) {
            System.err.println("Testing with " + i + " models averaged together");
            parser = averageModels(models.subList(0, i));
            EvaluateTreebank evaluator = new EvaluateTreebank(parser.op, null, parser);
            evaluator.testOnTreebank(devTreebank);
            double labelF1 = evaluator.getLBScore();
            System.err.println("Label F1 for " + i + " models: " + labelF1);
            if (labelF1 > bestF1) {
              bestF1 = labelF1;
              bestSize = i;
            }
          }
          parser = averageModels(models.subList(0, bestSize));
        } else {
          parser = ShiftReduceParser.averageModels(bestModels);
        }
      }

      parser.condenseFeatures();

      if (serializedPath != null) {
        try {
          IOUtils.writeObjectToFile(parser, serializedPath);
        } catch (IOException e) {
          throw new RuntimeIOException(e);
        }
      }
    }

    if (serializedPath != null && parser == null) {
      try {
        parser = IOUtils.readObjectFromFile(serializedPath);
        parser.op.setOptions("-forceTags");
      } catch (IOException e) {
        throw new RuntimeIOException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeIOException(e);
      }
      parser.op.setOptions(newArgs);
    }

    //parser.outputStats();

    if (testTreebankPath != null) {
      System.err.println("Loading test trees from " + testTreebankPath);
      Treebank testTreebank = parser.op.tlpParams.memoryTreebank();
      testTreebank.loadPath(testTreebankPath, testTreebankFilter);
      System.err.println("Loaded " + testTreebank.size() + " trees");

      EvaluateTreebank evaluator = new EvaluateTreebank(parser.op, null, parser);
      evaluator.testOnTreebank(testTreebank);

      // System.err.println("Input tree: " + tree);
      // System.err.println("Debinarized tree: " + query.getBestParse());
      // System.err.println("Parsed binarized tree: " + query.getBestBinarizedParse());
      // System.err.println("Predicted transition sequence: " + query.getBestTransitionSequence());
    }
  }


  private static final long serialVersionUID = 1;
}
