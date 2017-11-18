package edu.stanford.nlp.parser.shiftreduce;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.common.ParserGrammar;
import edu.stanford.nlp.parser.common.ParserQuery;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.parser.metrics.Eval;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.HashIndex;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.ScoredComparator;
import edu.stanford.nlp.util.ScoredObject;

public class ShiftReduceParser implements Serializable, ParserGrammar {
  final Index<Transition> transitionIndex;
  Index<String> featureIndex;
  double[][] featureWeights;

  final ShiftReduceOptions op;

  // TODO: fold the featureFactory into our options
  final FeatureFactory featureFactory;

  public ShiftReduceParser(Index<Transition> transitionIndex, Index<String> featureIndex,
                           double[][] featureWeights, ShiftReduceOptions op, FeatureFactory featureFactory) {
    this.transitionIndex = transitionIndex;
    this.featureIndex = featureIndex;
    this.featureWeights = featureWeights;
    this.op = op;
    this.featureFactory = featureFactory;
  }

  public ParserQuery parserQuery() {
    return new ShiftReduceParserQuery(this);
  }

  public void condenseFeatures() {
    Index<String> newFeatureIndex = new HashIndex<String>();
    for (int j = 0; j < featureWeights[0].length; ++j) {
      boolean useless = true;
      for (int i = 0; i < featureWeights.length; ++i) {
        if (featureWeights[i][j] != 0.0) {
          useless = false;
          break;
        }
      }
      if (useless) {
        continue;
      }
      newFeatureIndex.add(featureIndex.get(j));
    }

    double[][] newFeatureWeights = new double[transitionIndex.size()][newFeatureIndex.size()];
    for (int j = 0; j < featureWeights[0].length; ++j) {
      int newIndex = newFeatureIndex.indexOf(featureIndex.get(j));
      if (newIndex < 0) {
        continue;
      }
      for (int i = 0; i < featureWeights.length; ++i) {
        newFeatureWeights[i][newIndex] = featureWeights[i][j];
      }
    }

    featureIndex = newFeatureIndex;
    featureWeights = newFeatureWeights;
  }


  public void outputStats() {
    int countZeros = 0;
    for (int i = 0; i < featureWeights.length; ++i) {
      for (int j = 0; j < featureWeights[i].length; ++j) {
        if (featureWeights[i][j] == 0) {
          countZeros++;
        }
      }
    }
    System.err.println("Number of zeros: " + countZeros + " out of weights: " + featureWeights.length * featureWeights[0].length);

    System.err.println("Feature index size: " + featureIndex.size());
    int wordLength = 0;
    for (String feature : featureIndex) {
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
    double[] scores = new double[featureWeights.length];
    for (String feature : features) {
      int featureNum = featureIndex.indexOf(feature);
      if (featureNum >= 0) {
        // Features not in our index are represented by < 0 and are ignored
        for (int i = 0; i < scores.length; ++i) {
          scores[i] += featureWeights[i][featureNum];
        }
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

  private static final long serialVersionUID = 1;
}
