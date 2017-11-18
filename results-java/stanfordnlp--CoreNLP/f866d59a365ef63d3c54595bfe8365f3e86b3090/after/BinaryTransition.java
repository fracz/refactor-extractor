package edu.stanford.nlp.parser.shiftreduce;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.TreeShapedStack;

/**
 * Transition that makes a binary parse node in a partially finished tree.
 */
public class BinaryTransition implements Transition {
  public final String label;

  /** Which side the head is on */
  public final Side side;

  public enum Side {
    LEFT, RIGHT
  }

  public BinaryTransition(String label, Side side) {
    this.label = label;
    this.side = side;
  }

  /**
   * Legal as long as there are at least two items on the state's stack.
   *
   * TODO: implement other measures of legality
   */
  public boolean isLegal(State state) {
    if (state.finished) {
      return false;
    }
    if (state.stack.size() <= 1) {
      return false;
    }
    // don't allow binarized labels if it makes the state have a stack
    // of size 1 and a queue of size 0
    if (state.stack.size() == 2 && isBinarized() && state.endOfQueue()) {
      return false;
    }
    return true;
  }

  public boolean isBinarized() {
    return (label.charAt(0) == '@');
  }

  /**
   * Add a binary node to the existing node on top of the stack
   */
  public State apply(State state) {
    return apply(state, 0.0);
  }

  /**
   * Add a binary node to the existing node on top of the stack
   */
  public State apply(State state, double scoreDelta) {
    TreeShapedStack<Tree> stack = state.stack;
    Tree right = stack.peek();
    stack = stack.pop();
    Tree left = stack.peek();
    stack = stack.pop();

    Tree head;
    switch(side) {
    case LEFT:
      head = left;
      break;
    case RIGHT:
      head = right;
      break;
    default:
      throw new IllegalArgumentException("Unknown side " + side);
    }

    if (!(head.label() instanceof CoreLabel)) {
      throw new IllegalArgumentException("Stack should have CoreLabel nodes");
    }
    CoreLabel headLabel = (CoreLabel) head.label();

    CoreLabel production = new CoreLabel();
    production.setValue(label);
    production.set(TreeCoreAnnotations.HeadWordAnnotation.class, headLabel.get(TreeCoreAnnotations.HeadWordAnnotation.class));
    production.set(TreeCoreAnnotations.HeadTagAnnotation.class, headLabel.get(TreeCoreAnnotations.HeadTagAnnotation.class));
    Tree newTop = new LabeledScoredTreeNode(production);
    newTop.addChild(left);
    newTop.addChild(right);

    stack = stack.push(newTop);


    TreeShapedStack<State.HeadPosition> separators = state.separators;
    State.HeadPosition rightSeparator = separators.peek();
    separators = separators.pop();
    State.HeadPosition leftSeparator = separators.peek();
    separators = separators.pop();

    State.HeadPosition newSeparator;
    switch(side) {
    case LEFT:
      if (rightSeparator == State.HeadPosition.NONE) {
        newSeparator = leftSeparator;
      } else if (leftSeparator == State.HeadPosition.LEFT || leftSeparator == State.HeadPosition.BOTH) {
        newSeparator = State.HeadPosition.BOTH;
      } else if (leftSeparator == State.HeadPosition.HEAD) {
        newSeparator = State.HeadPosition.HEAD;
      } else {
        newSeparator = State.HeadPosition.RIGHT;
      }
      break;
    case RIGHT:
      if (leftSeparator == State.HeadPosition.NONE) {
        newSeparator = rightSeparator;
      } else if (rightSeparator == State.HeadPosition.RIGHT || rightSeparator == State.HeadPosition.BOTH) {
        newSeparator = State.HeadPosition.BOTH;
      } else if (rightSeparator == State.HeadPosition.HEAD) {
        newSeparator = State.HeadPosition.HEAD;
      } else {
        newSeparator = State.HeadPosition.LEFT;
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown side " + side);
    }
    separators = separators.push(newSeparator);

    return new State(stack, state.transitions.push(this), separators, state.sentence, state.tokenPosition, state.score + scoreDelta, false);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof BinaryTransition)) {
      return false;
    }
    String otherLabel = ((BinaryTransition) o).label;
    Side otherSide = ((BinaryTransition) o).side;
    return otherSide.equals(side) && label.equals(otherLabel);
  }

  @Override
  public int hashCode() {
    switch(side) {
    case LEFT:
      return 97197711 ^ label.hashCode();
    case RIGHT:
      return 97197711 ^ label.hashCode();
    default:
      throw new IllegalArgumentException("Unknown side " + side);
    }
  }

  @Override
  public String toString() {
    switch(side) {
    case LEFT:
      return "LeftBinary(" + label + ")";
    case RIGHT:
      return "RightBinary(" + label + ")";
    default:
      throw new IllegalArgumentException("Unknown side " + side);
    }
  }

  private static final long serialVersionUID = 1;
}