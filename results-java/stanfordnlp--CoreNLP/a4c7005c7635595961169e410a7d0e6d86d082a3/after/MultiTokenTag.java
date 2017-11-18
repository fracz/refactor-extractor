package edu.stanford.nlp.ling;

import java.io.Serializable;

/**
 * Represents a tag for a multi token expression
 * Can be used to annotate individual tokens without
 *   having nested annotations
 *
 * @author Angel Chang
 */
public class MultiTokenTag implements Serializable {
  private static final long serialVersionUID = 1;

  public Tag tag;
  public int index;

  public static class Tag implements  Serializable {
    private static final long serialVersionUID = 1;

    public String name;
    public String tag;

    public int length;  // total length of expression

    public Tag(String name, String tag, int length) {
      this.name = name;
      this.tag = tag;
      this.length = length;
    }
  }

  public MultiTokenTag(Tag tag, int index) {
    this.tag = tag;
    this.index = index;
  }

  public boolean isStart() {
    return index == 0;
  }

  public boolean isEnd() {
    return index == tag.length - 1;
  }
}