package edu.stanford.nlp.parser.shiftreduce;

import edu.stanford.nlp.trees.Tree;
import java.util.regex.Pattern;

/**
 * Transition that moves a single item from the front of the queue to
 * the top of the stack without making any other changes.
 */
public class ShiftTransition implements Transition {
  /**
   * Shifting is legal as long as the state is not finished and there
   * are more items on the queue to be shifted.
   * TODO: go through the papers and make sure they don't mention any
   * other conditions where one shouldn't shift
   */
  public boolean isLegal(State state) {
    if (state.finished) {
      return false;
    }
    if (state.tokenPosition >= state.sentence.size()) {
      return false;
    }
    // We disallow shifting when the previous transition was a right
    // head transition to a partial (binarized) state
    // TODO: I don't have an explanation for this, it was just stated
    // in Zhang & Clark 2009
    if (state.stack.size() > 0) {
      Tree top = state.stack.peek();
      // Temporary node, eg part of a binarized sequence
      if (top.label().value().startsWith("@")) {
        if (top.children().length == 2) {
          Tree rightChild = top.children()[1];
          if (rightChild.label().value().equals(top.label().value())) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Add the new preterminal to the stack, increment the queue position.
   */
  public State apply(State state) {
    return apply(state, 0.0);
  }

  static final Pattern separatorRegex = Pattern.compile("^[,;:-]+$");

  /**
   * Add the new preterminal to the stack, increment the queue position.
   */
  public State apply(State state, double scoreDelta) {
    Tree tagNode = state.sentence.get(state.tokenPosition);
    if (!tagNode.isPreTerminal()) {
      throw new AssertionError("Only expected preterminal nodes");
    }
    Tree wordNode = tagNode.children()[0];
    String word = wordNode.label().value();
    State.HeadPosition separator = (separatorRegex.matcher(word).matches()) ? State.HeadPosition.HEAD : State.HeadPosition.NONE;
    return new State(state.stack.push(tagNode), state.transitions.push(this), state.separators.push(separator), state.sentence, state.tokenPosition + 1, state.score + scoreDelta, false);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ShiftTransition) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 900967388; // a random int
  }

  @Override
  public String toString() {
    return "Shift";
  }

  private static final long serialVersionUID = 1;
}