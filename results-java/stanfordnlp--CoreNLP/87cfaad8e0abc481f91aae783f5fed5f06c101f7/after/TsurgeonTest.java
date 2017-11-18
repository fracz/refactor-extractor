package edu.stanford.nlp.trees.tregex.tsurgeon;

import junit.framework.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.tregex.*;
import edu.stanford.nlp.util.Pair;

/**
 * Tests a few random tsurgeon operations.
 * TODO: needs more coverage.
 *
 * @author John Bauer
 */
public class TsurgeonTest extends TestCase {

  // We don't use valueOf because we sometimes use trees such as
  // (bar (foo (foo 1))), and the default valueOf uses a
  // TreeNormalizer that removes nodes from such a tree
  public static Tree treeFromString(String s) {
    try {
      TreeReader tr = new PennTreeReader(new StringReader(s),
                                         new LabeledScoredTreeFactory());
      return tr.readTree();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void testDelete() {
    TsurgeonPattern tsurgeon = Tsurgeon.parseOperation("delete bob");

    TregexPattern tregex = TregexPattern.compile("B=bob");
    runTest(tregex, tsurgeon, "(A (B (C 1)))", "A");
    runTest(tregex, tsurgeon, "(A (foo 1) (B (C 1)))", "(A (foo 1))");
    runTest(tregex, tsurgeon, "(A (B 1) (B (C 1)))", "A");
    runTest(tregex, tsurgeon, "(A (foo 1) (bar (C 1)))",
            "(A (foo 1) (bar (C 1)))");

    tregex = TregexPattern.compile("C=bob");
    runTest(tregex, tsurgeon, "(A (B (C 1)))", "(A B)");
    runTest(tregex, tsurgeon, "(A (foo 1) (B (C 1)))", "(A (foo 1) B)");
    runTest(tregex, tsurgeon, "(A (B 1) (B (C 1)))", "(A (B 1) B)");
    runTest(tregex, tsurgeon, "(A (foo 1) (bar (C 1)))", "(A (foo 1) bar)");
  }

  public void testPrune() {
    TsurgeonPattern tsurgeon = Tsurgeon.parseOperation("prune bob");

    TregexPattern tregex = TregexPattern.compile("B=bob");
    runTest(tregex, tsurgeon, "(A (B (C 1)))", null);
    runTest(tregex, tsurgeon, "(A (foo 1) (B (C 1)))", "(A (foo 1))");
    runTest(tregex, tsurgeon, "(A (B 1) (B (C 1)))", null);
    runTest(tregex, tsurgeon, "(A (foo 1) (bar (C 1)))",
            "(A (foo 1) (bar (C 1)))");

    tregex = TregexPattern.compile("C=bob");
    runTest(tregex, tsurgeon, "(A (B (C 1)))", null);
    runTest(tregex, tsurgeon, "(A (foo 1) (B (C 1)))", "(A (foo 1))");
    runTest(tregex, tsurgeon, "(A (B 1) (B (C 1)))", "(A (B 1))");
    runTest(tregex, tsurgeon, "(A (foo 1) (bar (C 1)))", "(A (foo 1))");
  }

  public void testInsert() {
    TsurgeonPattern tsurgeon =
      Tsurgeon.parseOperation("insert (D (E 6)) $+ bar");
    TregexPattern tregex = TregexPattern.compile("B=bar !$ D");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (D (E 6)) (B 0) (C 1))");

    tsurgeon = Tsurgeon.parseOperation("insert (D (E 6)) $- bar");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (B 0) (D (E 6)) (C 1))");

    tsurgeon = Tsurgeon.parseOperation("insert (D (E 6)) >0 bar");
    tregex = TregexPattern.compile("B=bar !<D");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (B (D (E 6)) 0) (C 1))");

    tsurgeon = Tsurgeon.parseOperation("insert foo >0 bar");
    tregex = TregexPattern.compile("B=bar !<C $C=foo");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (B (C 1) 0) (C 1))");

    // the name will be cut off
    tsurgeon = Tsurgeon.parseOperation("insert (D (E=blah 6)) >0 bar");
    tregex = TregexPattern.compile("B=bar !<D");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (B (D (E 6)) 0) (C 1))");

    // the name should not be cut off, with the escaped = unescaped now
    tsurgeon = Tsurgeon.parseOperation("insert (D (E\\=blah 6)) >0 bar");
    tregex = TregexPattern.compile("B=bar !<D");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (B (D (E=blah 6)) 0) (C 1))");

    // the name should be cut off again, with a \ at the end of the new node
    tsurgeon = Tsurgeon.parseOperation("insert (D (E\\\\=blah 6)) >0 bar");
    tregex = TregexPattern.compile("B=bar !<D");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (B (D (E\\ 6)) 0) (C 1))");
  }

  public void testRelabel() {
    TsurgeonPattern tsurgeon;
    TregexPattern tregex;

    tregex = TregexPattern.compile("/^((?!_head).)*$/=preTerminal < (__=terminal !< __)");
    tsurgeon = Tsurgeon.parseOperation("relabel preTerminal /^(.*)$/$1_head=={terminal}/");
    runTest(tregex, tsurgeon, "($ $)", "($_head=$ $)");

    tsurgeon = Tsurgeon.parseOperation("relabel foo blah");
    tregex = TregexPattern.compile("B=foo");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (blah 0) (C 1))");
    runTest(tregex, tsurgeon, "(A (B 0) (B 1))", "(A (blah 0) (blah 1))");

    tsurgeon = Tsurgeon.parseOperation("relabel foo /\\//");
    tregex = TregexPattern.compile("B=foo");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (/ 0) (C 1))");
    runTest(tregex, tsurgeon, "(A (B 0) (B 1))", "(A (/ 0) (/ 1))");

    tsurgeon = Tsurgeon.parseOperation("relabel foo /.*(voc.*)/$1/");
    tregex = TregexPattern.compile("/^a.*t/=foo");
    runTest(tregex, tsurgeon, "(A (avocet 0) (C 1))", "(A (vocet 0) (C 1))");
    runTest(tregex, tsurgeon, "(A (avocet 0) (advocate 1))",
            "(A (vocet 0) (vocate 1))");

    tregex = TregexPattern.compile("curlew=baz < /^a(.*)t/#1%bar=foo");

    tsurgeon = Tsurgeon.parseOperation("relabel baz /cu(rle)w/={foo}/");
    runTest(tregex, tsurgeon, "(curlew (avocet 0))", "(avocet (avocet 0))");
    tsurgeon = Tsurgeon.parseOperation("relabel baz /cu(rle)w/%{bar}/");
    runTest(tregex, tsurgeon, "(curlew (avocet 0))", "(voce (avocet 0))");
    tsurgeon = Tsurgeon.parseOperation("relabel baz /cu(rle)w/$1/");
    runTest(tregex, tsurgeon, "(curlew (avocet 0))", "(rle (avocet 0))");
    tsurgeon = Tsurgeon.parseOperation("relabel baz /cu(rle)w/$1={foo}/");
    runTest(tregex, tsurgeon, "(curlew (avocet 0))", "(rleavocet (avocet 0))");
    tsurgeon = Tsurgeon.parseOperation("relabel baz /cu(rle)w/%{bar}$1={foo}/");
    runTest(tregex, tsurgeon,
            "(curlew (avocet 0))", "(vocerleavocet (avocet 0))");

    tregex = TregexPattern.compile("A=baz < /curlew.*/=foo < /avocet.*/=bar");
    tsurgeon = Tsurgeon.parseOperation("relabel baz /^.*$/={foo}={bar}/");
    runTest(tregex, tsurgeon, "(A (curlewfoo 0) (avocetzzz 1))",
            "(curlewfooavocetzzz (curlewfoo 0) (avocetzzz 1))");

    tregex = TregexPattern.compile("A=baz < /curle.*/=foo < /avo(.*)/#1%bar");
    tsurgeon = Tsurgeon.parseOperation("relabel baz /^(.*)$/={foo}$1%{bar}/");
    runTest(tregex, tsurgeon, "(A (curlew 0) (avocet 1))",
            "(curlewAcet (curlew 0) (avocet 1))");

    tsurgeon = Tsurgeon.parseOperation("relabel baz /^(.*)$/=foo$1%bar/");
    runTest(tregex, tsurgeon, "(A (curlew 0) (avocet 1))",
            "(=fooA%bar (curlew 0) (avocet 1))");

    tregex = TregexPattern.compile("/foo/=foo");
    tsurgeon = Tsurgeon.parseOperation("relabel foo /foo/bar/");
    runTest(tregex, tsurgeon, "(foofoo (curlew 0) (avocet 1))",
            "(barbar (curlew 0) (avocet 1))");

    tregex = TregexPattern.compile("/foo/=foo < /cur.*/=bar");
    tsurgeon = Tsurgeon.parseOperation("relabel foo /foo/={bar}/");
    runTest(tregex, tsurgeon, "(foofoo (curlew 0) (avocet 1))",
            "(curlewcurlew (curlew 0) (avocet 1))");

    tregex = TregexPattern.compile("/^foo(.*)$/=foo");
    tsurgeon = Tsurgeon.parseOperation("relabel foo /foo(.*)$/bar$1/");
    runTest(tregex, tsurgeon, "(foofoo (curlew 0) (avocet 1))",
            "(barfoo (curlew 0) (avocet 1))");
  }

  public void testReplace() {
    TsurgeonPattern tsurgeon = Tsurgeon.parseOperation("replace foo blah");
    TregexPattern tregex = TregexPattern.compile("B=foo : C=blah");
    runTest(tregex, tsurgeon, "(A (B 0) (C 1))", "(A (C 1) (C 1))");

    // This test was a bug reported by a user; only one of the -NONE-
    // nodes was being replaced.  This was because the replace was
    // reusing existing tree nodes instead of creating new ones, which
    // caused tregex to fail to find the second replacement
    tsurgeon = Tsurgeon.parseOperation("replace dest src");
    tregex = TregexPattern.compile("(/-([0-9]+)$/#1%i=src > /^FILLER$/) : (/^-NONE-/=dest <: /-([0-9]+)$/#1%i)");
    runTest(tregex, tsurgeon,
            "( (S (FILLER (NP-SBJ-1 (NNP Koito))) (VP (VBZ has) (VP (VBN refused) (S (NP-SBJ (-NONE- *-1)) (VP (TO to) (VP (VB grant) (NP (NNP Mr.) (NNP Pickens)) (NP (NP (NNS seats)) (PP-LOC (IN on) (NP (PRP$ its) (NN board))))))) (, ,) (S-ADV (NP-SBJ (-NONE- *-1)) (VP (VBG asserting) (SBAR (-NONE- 0) (S (NP-SBJ (PRP he)) (VP (VBZ is) (NP-PRD (NP (DT a) (NN greenmailer)) (VP (VBG trying) (S (NP-SBJ (-NONE- *)) (VP (TO to) (VP (VB pressure) (NP (NP (NNP Koito) (POS 's)) (JJ other) (NNS shareholders)) (PP-CLR (IN into) (S-NOM (NP-SBJ (-NONE- *)) (VP (VBG buying) (NP (PRP him)) (PRT (RP out)) (PP-MNR (IN at) (NP (DT a) (NN profit)))))))))))))))))) (. .)))",
            "( (S (FILLER (NP-SBJ-1 (NNP Koito))) (VP (VBZ has) (VP (VBN refused) (S (NP-SBJ (NP-SBJ-1 (NNP Koito))) (VP (TO to) (VP (VB grant) (NP (NNP Mr.) (NNP Pickens)) (NP (NP (NNS seats)) (PP-LOC (IN on) (NP (PRP$ its) (NN board))))))) (, ,) (S-ADV (NP-SBJ (NP-SBJ-1 (NNP Koito))) (VP (VBG asserting) (SBAR (-NONE- 0) (S (NP-SBJ (PRP he)) (VP (VBZ is) (NP-PRD (NP (DT a) (NN greenmailer)) (VP (VBG trying) (S (NP-SBJ (-NONE- *)) (VP (TO to) (VP (VB pressure) (NP (NP (NNP Koito) (POS 's)) (JJ other) (NNS shareholders)) (PP-CLR (IN into) (S-NOM (NP-SBJ (-NONE- *)) (VP (VBG buying) (NP (PRP him)) (PRT (RP out)) (PP-MNR (IN at) (NP (DT a) (NN profit)))))))))))))))))) (. .)))");
  }

  public void testInsertDelete() {
    // The same bug as the Replace bug, but for a sequence of
    // insert/delete operations
    List<Pair<TregexPattern, TsurgeonPattern>> surgery =
      new ArrayList<Pair<TregexPattern, TsurgeonPattern>>();

    TregexPattern tregex = TregexPattern.compile("(/-([0-9]+)$/#1%i=src > /^FILLER$/) : (/^-NONE-/=dest <: /-([0-9]+)$/#1%i !$ ~src)");
    TsurgeonPattern tsurgeon = Tsurgeon.parseOperation("insert src $+ dest");
    surgery.add(new Pair<TregexPattern, TsurgeonPattern>(tregex, tsurgeon));
    tregex = TregexPattern.compile("(/-([0-9]+)$/#1%i=src > /^FILLER$/) : (/^-NONE-/=dest <: /-([0-9]+)$/#1%i)");
    tsurgeon = Tsurgeon.parseOperation("delete dest");
    surgery.add(new Pair<TregexPattern, TsurgeonPattern>(tregex, tsurgeon));

    runTest(surgery,
            "( (S (FILLER (NP-SBJ-1 (NNP Koito))) (VP (VBZ has) (VP (VBN refused) (S (NP-SBJ (-NONE- *-1)) (VP (TO to) (VP (VB grant) (NP (NNP Mr.) (NNP Pickens)) (NP (NP (NNS seats)) (PP-LOC (IN on) (NP (PRP$ its) (NN board))))))) (, ,) (S-ADV (NP-SBJ (-NONE- *-1)) (VP (VBG asserting) (SBAR (-NONE- 0) (S (NP-SBJ (PRP he)) (VP (VBZ is) (NP-PRD (NP (DT a) (NN greenmailer)) (VP (VBG trying) (S (NP-SBJ (-NONE- *)) (VP (TO to) (VP (VB pressure) (NP (NP (NNP Koito) (POS 's)) (JJ other) (NNS shareholders)) (PP-CLR (IN into) (S-NOM (NP-SBJ (-NONE- *)) (VP (VBG buying) (NP (PRP him)) (PRT (RP out)) (PP-MNR (IN at) (NP (DT a) (NN profit)))))))))))))))))) (. .)))",
            "( (S (FILLER (NP-SBJ-1 (NNP Koito))) (VP (VBZ has) (VP (VBN refused) (S (NP-SBJ (NP-SBJ-1 (NNP Koito))) (VP (TO to) (VP (VB grant) (NP (NNP Mr.) (NNP Pickens)) (NP (NP (NNS seats)) (PP-LOC (IN on) (NP (PRP$ its) (NN board))))))) (, ,) (S-ADV (NP-SBJ (NP-SBJ-1 (NNP Koito))) (VP (VBG asserting) (SBAR (-NONE- 0) (S (NP-SBJ (PRP he)) (VP (VBZ is) (NP-PRD (NP (DT a) (NN greenmailer)) (VP (VBG trying) (S (NP-SBJ (-NONE- *)) (VP (TO to) (VP (VB pressure) (NP (NP (NNP Koito) (POS 's)) (JJ other) (NNS shareholders)) (PP-CLR (IN into) (S-NOM (NP-SBJ (-NONE- *)) (VP (VBG buying) (NP (PRP him)) (PRT (RP out)) (PP-MNR (IN at) (NP (DT a) (NN profit)))))))))))))))))) (. .)))");
  }

  /**
   * There was a bug where repeated children with the same exact
   * structure meant that each of the children would be repeated, even
   * if some of them wouldn't match the tree structure.  For example,
   * if you had the tree <code>(NP NP , NP , NP , CC NP)</code> and
   * tried to replace with <code>@NP &lt; (/^,/=comma $+ CC)</code>,
   * all of the commas would be replaced, not just the one next to CC.
   */
  public void testReplaceWithRepeats() {
    TsurgeonPattern tsurgeon;
    TregexPattern tregex;

    tregex = TregexPattern.compile("@NP < (/^,/=comma $+ CC)");
    tsurgeon = Tsurgeon.parseOperation("replace comma (COMMA)");
    runTest(tregex, tsurgeon, "(NP NP , NP , NP , CC NP)", "(NP NP , NP , NP COMMA CC NP)");
  }

  public void runTest(TregexPattern tregex, TsurgeonPattern tsurgeon,
                      String input, String expected) {
    Tree result = Tsurgeon.processPattern(tregex, tsurgeon,
                                          treeFromString(input));
    if (expected == null) {
      assertEquals(null, result);
    } else {
      assertEquals(expected, result.toString());
    }

    // run the test on both a list and as a single pattern just to
    // make sure the underlying code works for both
    Pair<TregexPattern, TsurgeonPattern> surgery =
      new Pair<TregexPattern, TsurgeonPattern>(tregex, tsurgeon);
    runTest(Collections.singletonList(surgery), input, expected);
  }

  public void runTest(List<Pair<TregexPattern, TsurgeonPattern>> surgery,
                      String input, String expected) {
    Tree result = Tsurgeon.processPatternsOnTree(surgery, treeFromString(input));
    if (expected == null) {
      assertEquals(null, result);
    } else {
      assertEquals(expected, result.toString());
    }
  }

  public void outputResults(TregexPattern tregex, TsurgeonPattern tsurgeon,
                            String input, String expected) {
    outputResults(tregex, tsurgeon, input);
  }

  public void outputResults(TregexPattern tregex, TsurgeonPattern tsurgeon,
                            String input) {
    Tree result = Tsurgeon.processPattern(tregex, tsurgeon, treeFromString(input));
    System.out.println(result);
  }
}