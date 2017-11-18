package edu.stanford.nlp.ling.tokensregex.matcher;

import java.util.List;

/**
* Exact match cost function
*
* @author Angel Chang
*/
public final class ExactMatchCost<K,V> implements MatchCostFunction<K,V> {
  final double mismatchCost;
  final double insCost;
  final double delCost;

  public ExactMatchCost() {
    this(1);
  }

  public ExactMatchCost(double mismatchCost) {
    this(mismatchCost,1,1);
  }

  public ExactMatchCost(double mismatchCost, double insCost, double delCost) {
    this.mismatchCost = mismatchCost;
    this.insCost = insCost;
    this.delCost = delCost;
  }

  @Override
  public double cost(K k1, K k2) {
    if (k1 != null) {
      if (k2 == null) return delCost;
      return (k1.equals(k2))? 0:mismatchCost;
    } else {
      return (k2 == null)? 0:insCost;
    }
  }

  @Override
  public double multiMatchDeltaCost(List<K> ks, V v, int n) {
    return 0;
  }
}