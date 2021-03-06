package edu.stanford.nlp.coref.sieve;

public class StrictHeadMatch3 extends DeterministicCorefSieve {
  public StrictHeadMatch3() {
    super();
    flags.USE_iwithini = true;
    flags.USE_INCLUSION_HEADMATCH = true;
    flags.USE_INCOMPATIBLE_MODIFIER = true;
  }
}