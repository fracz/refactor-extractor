package edu.stanford.nlp.ling.tokensregex;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Timing;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TokenSequenceMatcherITest extends TestCase {

  private static AnnotationPipeline pipeline = null;

  @Override
  public void setUp() throws Exception {
    synchronized(TokenSequenceMatcherITest.class) {
      if (pipeline == null) {
        pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(new PTBTokenizerAnnotator(false));
        pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
        pipeline.addAnnotator(new POSTaggerAnnotator(false));
        pipeline.addAnnotator(new NumberAnnotator(false, false));
//        pipeline.addAnnotator(new QuantifiableEntityNormalizingAnnotator(false));
      }
    }
  }

  private static CoreMap createDocument(String text) {
    Annotation annotation = new Annotation(text);
    pipeline.annotate(annotation);
    return annotation;
  }

  private static SequencePattern.PatternExpr getSequencePatternExpr(String... textRegex) {
    List<SequencePattern.PatternExpr> patterns = new ArrayList<SequencePattern.PatternExpr>(textRegex.length);
    for (String s:textRegex) {
      patterns.add(new SequencePattern.NodePatternExpr(CoreMapNodePattern.valueOf(s)));
    }
    return new SequencePattern.SequencePatternExpr(patterns);
  }

  private static SequencePattern.PatternExpr getOrPatternExpr(Pair<String,Object>... textRegex) {
    List<SequencePattern.PatternExpr> patterns = new ArrayList<SequencePattern.PatternExpr>(textRegex.length);
    for (Pair<String,Object> p:textRegex) {
      SequencePattern.PatternExpr pe = new SequencePattern.NodePatternExpr(CoreMapNodePattern.valueOf(p.first()));
      if (p.second() != null) {
        pe = new SequencePattern.ValuePatternExpr(pe, p.second());
      }
      patterns.add(pe);
    }
    return new SequencePattern.OrPatternExpr(patterns);
  }

  private static SequencePattern.PatternExpr getNodePatternExpr(String textRegex) {
    return new SequencePattern.NodePatternExpr(CoreMapNodePattern.valueOf(textRegex));
  }

  private static String testText = "the number were one, two and fifty.";
  public void testTokenSequenceMatcherValue() throws IOException {
    CoreMap doc = createDocument(testText);

    // Test simple sequence with value
    TokenSequencePattern p = TokenSequencePattern.compile(getOrPatternExpr(
            new Pair<String,Object>("one", 1), new Pair<String,Object>("two", null), new Pair<String,Object>("fifty", 50)));
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));

    boolean match = m.find();
    assertTrue(match);
    assertEquals("one", m.group());
    assertEquals(1, m.groupValue());

    match = m.find();
    assertTrue(match);
    assertEquals("two", m.group());
    assertNull(m.groupValue());

    match = m.find();
    assertTrue(match);
    assertEquals("fifty", m.group());
    assertEquals(50, m.groupValue());

    match = m.find();
    assertFalse(match);
  }

  private static String testText1 = "Mellitus was the first Bishop of London, the third Archbishop of Canterbury, and a member of the Gregorian mission  sent to England to convert the Anglo-Saxons. He arrived in 601 AD, and was consecrated as Bishop of London in 604.";
  public void testTokenSequenceMatcher1() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test simple sequence
    TokenSequencePattern p = TokenSequencePattern.compile(getSequencePatternExpr("Archbishop", "of", "Canterbury"));
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals("Archbishop of Canterbury", m.group());
    match = m.find();
    assertFalse(match);

    m.reset();
    match = m.find();
    assertTrue(match);
    assertEquals("Archbishop of Canterbury", m.group());

    m.reset();
    match = m.matches();
    assertFalse(match);

    // Test sequence with or
    p = TokenSequencePattern.compile(
            new SequencePattern.OrPatternExpr(
                    getSequencePatternExpr("Archbishop", "of", "Canterbury"),
                    getSequencePatternExpr("Bishop", "of", "London")
            ));
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Bishop of London", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Archbishop of Canterbury", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Bishop of London", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile(
              new SequencePattern.SequencePatternExpr(
                    SequencePattern.SEQ_BEGIN_PATTERN_EXPR,
                    getSequencePatternExpr("Archbishop", "of", "Canterbury")
            ));
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile(
            new SequencePattern.SequencePatternExpr(
                    SequencePattern.SEQ_BEGIN_PATTERN_EXPR,
                    getSequencePatternExpr("Mellitus", "was", "the")
            ));
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Mellitus was the", m.group());
    match = m.find();
    assertFalse(match);


    p = TokenSequencePattern.compile(
            new SequencePattern.SequencePatternExpr(
                    getSequencePatternExpr("Archbishop", "of", "Canterbury"),
                    SequencePattern.SEQ_END_PATTERN_EXPR
                    ));
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile(
            new SequencePattern.SequencePatternExpr(
                    getSequencePatternExpr("London", "in", "604", "."),
                    SequencePattern.SEQ_END_PATTERN_EXPR
                    ));
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("London in 604 .", m.group());
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcher2() throws IOException {
    CoreMap doc = createDocument(testText1);
    TokenSequencePattern p = TokenSequencePattern.compile(
                    getSequencePatternExpr(".*", ".*", "of", ".*"));

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("a member of the", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    match = m.find();
    assertFalse(match);

    // Test sequence with groups
    p = TokenSequencePattern.compile(
                    new SequencePattern.SequencePatternExpr(
                      new SequencePattern.GroupPatternExpr(
                            getSequencePatternExpr(".*", ".*")),
                      getNodePatternExpr("of"),
                      new SequencePattern.GroupPatternExpr(
                            getSequencePatternExpr(".*"))));

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    assertEquals("third Archbishop", m.group(1));
    assertEquals("Canterbury", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertFalse(match);

  }

  public void testTokenSequenceMatcher3() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test sequence with groups
    TokenSequencePattern p = TokenSequencePattern.compile(
            new SequencePattern.SequencePatternExpr(
                    new SequencePattern.GroupPatternExpr(
                            new SequencePattern.RepeatPatternExpr(
                                    getSequencePatternExpr("[A-Za-z]+"), 1, 2)),
                    getNodePatternExpr("of"),
                    new SequencePattern.GroupPatternExpr(
                            new SequencePattern.RepeatPatternExpr(
                                    getSequencePatternExpr("[A-Za-z]+"), 1, 3))));

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    assertEquals("third Archbishop", m.group(1));
    assertEquals("Canterbury", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the Gregorian mission", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the Gregorian mission", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("as Bishop of London in", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London in", m.group(2));
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile(
            new SequencePattern.SequencePatternExpr(
                    new SequencePattern.GroupPatternExpr(
                            new SequencePattern.RepeatPatternExpr(
                                    getNodePatternExpr("[A-Za-z]+"), 2, 2)),
                    getNodePatternExpr("of"),
                    new SequencePattern.GroupPatternExpr(
                            new SequencePattern.RepeatPatternExpr(
                                    getNodePatternExpr("[A-Za-z]+"), 1, 3, false))));

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    assertEquals("third Archbishop", m.group(1));
    assertEquals("Canterbury", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcherConj() throws IOException {
    CoreMap doc = createDocument(testText1);
    TokenSequencePattern p = TokenSequencePattern.compile(
                  new SequencePattern.AndPatternExpr(
                    new SequencePattern.SequencePatternExpr(
                      new SequencePattern.GroupPatternExpr(
                            new SequencePattern.RepeatPatternExpr(
                                    getNodePatternExpr("[A-Za-z]+"), 2, 2)),
                      getNodePatternExpr("of"),
                      new SequencePattern.GroupPatternExpr(
                            new SequencePattern.RepeatPatternExpr(
                                    getNodePatternExpr("[A-Za-z]+"), 1, 3, false))),
                    new SequencePattern.SequencePatternExpr(
                      new SequencePattern.GroupPatternExpr(
                        new SequencePattern.RepeatPatternExpr(
                            getNodePatternExpr(".*"), 0, -1)),
                      getNodePatternExpr("Bishop"),
                      new SequencePattern.RepeatPatternExpr(
                            getNodePatternExpr(".*"), 0, -1)
                    )));

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    assertEquals("first", m.group(3));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    // TODO: This conjunction has both a greedy and nongreedy pattern
    //  - the greedy will try to match as much as possible
    //  - while the non greedy will try to match less
    //  - currently the greedy overrides the nongreedy so we get an additional in...
    assertEquals("as Bishop of London in", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London in", m.group(2));
    assertEquals("as", m.group(3));
    match = m.find();
    assertFalse(match);


    // Same as before, but both non-greedy now...
    p = TokenSequencePattern.compile(
                  new SequencePattern.AndPatternExpr(
                    new SequencePattern.SequencePatternExpr(
                      new SequencePattern.GroupPatternExpr(
                            new SequencePattern.RepeatPatternExpr(
                                    getNodePatternExpr("[A-Za-z]+"), 2, 2)),
                      getNodePatternExpr("of"),
                      new SequencePattern.GroupPatternExpr(
                            new SequencePattern.RepeatPatternExpr(
                                    getNodePatternExpr("[A-Za-z]+"), 1, 3, false))),
                    new SequencePattern.SequencePatternExpr(
                      new SequencePattern.GroupPatternExpr(
                        new SequencePattern.RepeatPatternExpr(
                            getNodePatternExpr(".*"), 0, -1)),
                      getNodePatternExpr("Bishop"),
                      new SequencePattern.RepeatPatternExpr(
                            getNodePatternExpr(".*"), 0, -1, false)
                    )));

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    assertEquals("first", m.group(3));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London", m.group(2));
    assertEquals("as", m.group(3));
    match = m.find();
    assertFalse(match);


    // Same as before, but compiled from string
    p = TokenSequencePattern.compile(
            "(?: (/[A-Za-z]+/{2,2}) /of/ (/[A-Za-z]+/{1,3}?) ) & (?: (/.*/*) /Bishop/ /.*/*? )");

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    assertEquals("first", m.group(3));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London", m.group(2));
    assertEquals("as", m.group(3));
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcherConjAll() throws IOException {
    CoreMap doc = createDocument(testText1);
    TokenSequencePattern p = TokenSequencePattern.compile(
            "(?: (/[A-Za-z]+/{1,2}) /of/ (/[A-Za-z]+/{1,3}?) ) & (?: (/.*/*) /Bishop/ /.*/*? )");

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    m.setFindType(SequenceMatcher.FindType.FIND_ALL);
    // Test finding of ALL matching sequences with conjunctions
    // NOTE: Not all sequences are found for some reason - missing sequences starting with just Bishop....
    boolean match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    assertEquals("first", m.group(3));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London", m.group(2));
    assertEquals("as", m.group(3));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("as Bishop of London in", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London in", m.group(2));
    assertEquals("as", m.group(3));
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcherAll() throws IOException {
    CoreMap doc = createDocument(testText1);
    TokenSequencePattern p = TokenSequencePattern.compile(
            "(/[A-Za-z]+/{1,2}) /of/ (/[A-Za-z]+/{1,3}?) ");

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    m.setFindType(SequenceMatcher.FindType.FIND_ALL);
    // Test finding of ALL matching sequences
    // NOTE: when using FIND_ALL greedy/recluctant modifiers are not enforced
    //       perhaps should add syntax where some of them are enforced...
    boolean match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("Bishop of London", m.group());
    assertEquals("Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    assertEquals("third Archbishop", m.group(1));
    assertEquals("Canterbury", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("Archbishop of Canterbury", m.group());
    assertEquals("Archbishop", m.group(1));
    assertEquals("Canterbury", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the Gregorian", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the Gregorian", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the Gregorian mission", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the Gregorian mission", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("member of the", m.group());
    assertEquals("member", m.group(1));
    assertEquals("the", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("member of the Gregorian", m.group());
    assertEquals("member", m.group(1));
    assertEquals("the Gregorian", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("member of the Gregorian mission", m.group());
    assertEquals("member", m.group(1));
    assertEquals("the Gregorian mission", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("as Bishop of London in", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London in", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("Bishop of London", m.group());
    assertEquals("Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("Bishop of London in", m.group());
    assertEquals("Bishop", m.group(1));
    assertEquals("London in", m.group(2));
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcher4() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test sequence with groups
    TokenSequencePattern p = TokenSequencePattern.compile(
                      new SequencePattern.RepeatPatternExpr(
                                    getSequencePatternExpr("[A-Za-z]+"), 0, -1));

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Mellitus was the first Bishop of London", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("the third Archbishop of Canterbury", m.group());

    p = TokenSequencePattern.compile(
            new SequencePattern.SequencePatternExpr(
                      new SequencePattern.RepeatPatternExpr(
                              getSequencePatternExpr("[A-Za-z]+"), 0, -1),
                      getSequencePatternExpr("Mellitus", "was")));

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Mellitus was", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile(
            new SequencePattern.SequencePatternExpr(
                      new SequencePattern.RepeatPatternExpr(
                              getSequencePatternExpr("[A-Za-z]+"), 1, -1),
                      getSequencePatternExpr("Mellitus", "was")));

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertFalse(match);

  }

  public void testTokenSequenceMatcher5() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test simple sequence
    TokenSequencePattern p = TokenSequencePattern.compile(" [ { word:\"Archbishop\" } ]  [ { word:\"of\" } ]  [ { word:\"Canterbury\" } ]");
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals("Archbishop of Canterbury", m.group());
    match = m.find();
    assertFalse(match);

    m.reset();
    match = m.find();
    assertTrue(match);
    assertEquals("Archbishop of Canterbury", m.group());

    m.reset();
    match = m.matches();
    assertFalse(match);


    p = TokenSequencePattern.compile(" [ \"Archbishop\" ]  [ \"of\"  ]  [ \"Canterbury\"  ]");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals("Archbishop of Canterbury", m.group());
    match = m.find();
    assertFalse(match);

    m.reset();
    match = m.find();
    assertTrue(match);
    assertEquals("Archbishop of Canterbury", m.group());

    m.reset();
    match = m.matches();
    assertFalse(match);

    // Test sequence with or
    p = TokenSequencePattern.compile(" [ \"Archbishop\"] [\"of\"] [\"Canterbury\"] |  [ \"Bishop\" ] [ \"of\" ]  [ \"London\" ] ");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Bishop of London", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Archbishop of Canterbury", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Bishop of London", m.group());
    match = m.find();
    assertFalse(match);

  }

  public void testTokenSequenceMatcher6() throws IOException {
    CoreMap doc = createDocument(testText1);
    TokenSequencePattern p = TokenSequencePattern.compile("[ /.*/ ] [ /.*/ ] [/of/] [/.*/]");
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("a member of the", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile("([ /.*/ ] [ /.*/ ]) [/of/] ([/.*/])");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    assertEquals("third Archbishop", m.group(1));
    assertEquals("Canterbury", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertFalse(match);

  }

  public void testTokenSequenceMatcher7() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test sequence with groups
    TokenSequencePattern p = TokenSequencePattern.compile(" ( [ /[A-Za-z]+/ ]{1,2} )  [ /of/ ] ( [ /[A-Za-z]+/ ]{1,3} )");
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    assertEquals("third Archbishop", m.group(1));
    assertEquals("Canterbury", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the Gregorian mission", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the Gregorian mission", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("as Bishop of London in", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London in", m.group(2));
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( " ( [ /[A-Za-z]+/ ]{2,2} )  [ /of/ ] ( [ /[A-Za-z]+/ ]{1,3}? )");

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("first Bishop of London", m.group());
    assertEquals("first Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("third Archbishop of Canterbury", m.group());
    assertEquals("third Archbishop", m.group(1));
    assertEquals("Canterbury", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("a member of the", m.group());
    assertEquals("a member", m.group(1));
    assertEquals("the", m.group(2));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("as Bishop of London", m.group());
    assertEquals("as Bishop", m.group(1));
    assertEquals("London", m.group(2));
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcher8() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test sequence with groups
    TokenSequencePattern p = TokenSequencePattern.compile( "[ /[A-Za-z]+/ ]*");

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Mellitus was the first Bishop of London", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("the third Archbishop of Canterbury", m.group());

    p = TokenSequencePattern.compile( "[ /[A-Za-z]+/ ]*  [\"Mellitus\"] [ \"was\"]");

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Mellitus was", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ /[A-Za-z]+/ ]+  [\"Mellitus\"] [ \"was\"]");

    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertFalse(match);

  }

  public void testTokenSequenceMatcher9() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test sequence with groups
//    TokenSequencePattern p = TokenSequencePattern.compile( "(?$contextprev /.*/) (?$treat [{{treat}} & /.*/]) (?$contextnext [/.*/])");
    TokenSequencePattern p = TokenSequencePattern.compile( "(?$contextprev /.*/) (?$test [{tag:NNP} & /.*/]) (?$contextnext [/.*/])");

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("first Bishop of", m.group());

    assertEquals("first", m.group(1));
    assertEquals("Bishop", m.group(2));
    assertEquals("of", m.group(3));
    assertEquals("first", m.group("$contextprev"));
    assertEquals("Bishop", m.group("$test"));
    assertEquals("of", m.group("$contextnext"));
    assertEquals("first", m.group(" $contextprev"));
    assertEquals("Bishop", m.group("$test "));
    assertEquals(null, m.group("$contex tnext"));

    assertEquals(3, m.start("$contextprev"));
    assertEquals(4, m.end("$contextprev"));
    assertEquals(4, m.start("$test"));
    assertEquals(5, m.end("$test"));
    assertEquals(5, m.start("$contextnext"));
    assertEquals(6, m.end("$contextnext"));
  }

  public void testTokenSequenceMatcher10() throws IOException {
    CoreMap doc = createDocument("the number is five or 5 or 5.0 or but not 5x or -5 or 5L.");

    // Test simplified pattern with number
    TokenSequencePattern p = TokenSequencePattern.compile( "(five|5|5x|5.0|-5|5L)");

    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("five", m.group(1));

    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("5", m.group(1));

    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("5.0", m.group(1));

    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("5x", m.group(1));

    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("-5", m.group(1));

    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("5L", m.group(1));

    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceOptimizeOrString() throws IOException {
    CoreMap doc = createDocument("atropine we need to have many many words here but we don't sweating");

    // Test simplified pattern with number
    TokenSequencePattern p = TokenSequencePattern.compile( "(?$dt \"atropine\") []{0,15} " +
            "(?$se  \"social\" \"avoidant\" \"behaviour\"|\"dysuria\"|\"hyperglycaemia\"| \"mental\" \"disorder\"|\"vertigo\"|\"flutter\"| \"chest\" \"pain\"| \"elevated\" \"blood\" \"pressure\"|\"mania\"| \"rash\" \"erythematous\"|\"manic\"| \"papular\" \"rash\"|\"death\"| \"atrial\" \"arrhythmia\"| \"dry\" \"eyes\"| \"loss\" \"of\" \"libido\"| \"rash\" \"papular\"|\"hypersensitivity\"| \"blood\" \"pressure\" \"increased\"|\"dyspepsia\"| \"accommodation\" \"disorder\"| \"reflexes\" \"increased\"|\"lesions\"|\"asthenia\"| \"gastrointestinal\" \"pain\"|\"excitement\"| \"breast\" \"feeding\"|\"hypokalaemia\"| \"cerebellar\" \"syndrome\"|\"nervousness\"| \"pulmonary\" \"oedema\"| \"inspiratory\" \"stridor\"| \"taste\" \"altered\"|\"paranoia\"| \"psychotic\" \"disorder\"| \"open\" \"angle\" \"glaucoma\"|\"photophobia\"| \"dry\" \"eye\"|\"osteoarthritis\"| \"keratoconjunctivitis\" \"sicca\"| \"haemoglobin\" \"increased\"| \"ventricular\" \"extrasystoles\"|\"hallucinations\"|\"conjunctivitis\"|\"paralysis\"| \"qrs\" \"complex\"|\"anxiety\"| \"conjunctival\" \"disorder\"|\"coma\"|\"strabismus\"|\"thirst\"|\"para\"| \"sicca\" \"syndrome\"| \"atrioventricular\" \"dissociation\"|\"desquamation\"|\"crusting\"| \"abdominal\" \"distension\"|\"blindness\"|\"hypotension\"|\"dermatitis\"| \"sinus\" \"tachycardia\"| \"abdominal\" \"distention\"| \"lacrimation\" \"decreased\"|\"sicca\"| \"paralytic\" \"ileus\"| \"urinary\" \"hesitation\"|\"withdrawn\"| \"erectile\" \"dysfunction\"|\"keratoconjunctivitis\"|\"anaphylaxis\"| \"psychiatric\" \"disorders\"| \"altered\" \"taste\"|\"somnolence\"|\"extrasystoles\"|\"ageusia\"| \"intraocular\" \"pressure\" \"increased\"| \"left\" \"ventricular\" \"failure\"|\"impotence\"|\"drowsiness\"|\"conjunctiva\"| \"delayed\" \"gastric\" \"emptying\"| \"gastrointestinal\" \"sounds\" \"abnormal\"| \"qt\" \"prolonged\"| \"supraventricular\" \"tachycardia\"|\"weakness\"|\"hypertonia\"| \"confusional\" \"state\"|\"anhidrosis\"|\"myopia\"|\"dyspnoea\"| \"speech\" \"impairment\" \"nos\"| \"rash\" \"maculo\" \"papular\"|\"petechiae\"|\"tachypnea\"| \"acute\" \"angle\" \"closure\" \"glaucoma\"| \"gastrooesophageal\" \"reflux\" \"disease\"|\"hypokalemia\"| \"left\" \"heart\" \"failure\"| \"myocardial\" \"infarction\"| \"site\" \"reaction\"| \"ventricular\" \"fibrillation\"|\"fibrillation\"| \"maculopapular\" \"rash\"| \"impaired\" \"gastric\" \"emptying\"|\"amnesia\"| \"labored\" \"respirations\"| \"decreased\" \"lacrimation\"|\"mydriasis\"|\"headache\"| \"dry\" \"mouth\"|\"scab\"| \"cardiac\" \"syncope\"| \"visual\" \"acuity\" \"reduced\"|\"tension\"| \"blurred\" \"vision\"| \"bloated\" \"feeling\"| \"labored\" \"breathing\"| \"stridor\" \"inspiratory\"| \"skin\" \"exfoliation\"| \"memory\" \"loss\"|\"syncope\"| \"rash\" \"scarlatiniform\"|\"hyperpyrexia\"| \"cardiac\" \"flutter\"|\"heartburn\"| \"bowel\" \"sounds\" \"decreased\"|\"blepharitis\"|\"tachycardia\"| \"excessive\" \"thirst\"|\"confusion\"| \"rash\" \"macular\"| \"taste\" \"loss\"| \"respiratory\" \"failure\"|\"hesitancy\"|\"dysmetria\"|\"disorientation\"| \"decreased\" \"hemoglobin\"| \"atrial\" \"fibrillation\"| \"urinary\" \"retention\"| \"dry\" \"skin\"|\"dehydration\"|\"hyponatraemia\"|\"dysgeusia\"|\"disorder\"| \"increased\" \"intraocular\" \"pressure\"| \"speech\" \"disorder\"| \"feeling\" \"abnormal\"|\"pain\"| \"anaphylactic\" \"shock\"|\"hallucination\"| \"abdominal\" \"pain\"| \"junctional\" \"tachycardia\"| \"bun\" \"increased\"| \"ventricular\" \"flutter\"| \"scarlatiniform\" \"rash\"|\"agitation\"| \"feeling\" \"hot\"|\"hyponatremia\"| \"decreased\" \"bowel\" \"sounds\"|\"cyanosis\"|\"dysarthria\"| \"heat\" \"intolerance\"|\"hyperglycemia\"|\"reflux\"| \"angle\" \"closure\" \"glaucoma\"| \"electrocardiogram\" \"qt\" \"prolonged\"| \"vision\" \"blurred\"| \"blood\" \"urea\" \"increased\"|\"dizziness\"|\"arrhythmia\"|\"erythema\"|\"vomiting\"| \"difficulty\" \"in\" \"micturition\"|\"infarction\"|\"laryngospasm\"|\"hypoglycaemia\"|\"hypoglycemia\"| \"elevated\" \"hemoglobin\"| \"skin\" \"warm\"| \"ventricular\" \"arrhythmia\"|\"dissociation\"| \"warm\" \"skin\"| \"follicular\" \"conjunctivitis\"|\"urticaria\"|\"fatigue\"| \"cardiac\" \"fibrillation\"| \"decreased\" \"sweating\"| \"decreased\" \"visual\" \"acuity\"|\"lethargy\"| \"acute\" \"angle\" \"closure\" \"glaucoma\"| \"nodal\" \"rhythm\"|\"borborygmi\"|\"hyperreflexia\"| \"respiratory\" \"depression\"|\"diarrhea\"|\"leukocytosis\"| \"speech\" \"disturbance\"|\"ataxia\"|\"cycloplegia\"|\"tachypnoea\"|\"eczema\"| \"supraventricular\" \"extrasystoles\"|\"ileus\"| \"cardiac\" \"arrest\"| \"ventricular\" \"tachycardia\"|\"laryngitis\"|\"delirium\"|\"lactation\"|\"glaucoma\"|\"obstruction\"|\"hypohidrosis\"|\"parity\"|\"palpitations\"| \"temperature\" \"intolerance\"|\"constipation\"|\"cyclophoria\"| \"acute\" \"coronary\" \"syndrome\"| \"arrhythmia\" \"supraventricular\"|\"arrest\"|\"lesion\"|\"nausea\"| \"sweating\" \"decreased\"|\"keratitis\"|\"dyskinesia\"| \"pulmonary\" \"function\" \"test\" \"decreased\"|\"stridor\"|\"swelling\"|\"dysphagia\"| \"haemoglobin\" \"decreased\"|\"diarrhoea\"| \"ileus\" \"paralytic\"|\"clonus\"|\"insomnia\"| \"electrocardiogram\" \"qrs\" \"complex\"| \"nasal\" \"congestion\"| \"nasal\" \"dryness\"|\"sweating\"|\"rash\"| \"nodal\" \"arrhythmia\"|\"irritability\"|\"hyperhidrosis\"| \"ventricular\" \"failure\")");

    Timing timing = new Timing();
    timing.start();
    for (int i = 0; i < 100; i++) {
      TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
      boolean match = m.find();
      assertTrue(match);
      assertEquals("atropine we need to have many many words here but we do n't sweating", m.group(0));

      match = m.find();
      assertFalse(match);
    }
    timing.stop("testTokenSequenceOptimizeOrString matched");


    CoreMap docNoMatch = createDocument("atropine we need to have many many words here but we don't, many many many words but still no match");
    timing.start();
    for (int i = 0; i < 100; i++) {
      TokenSequenceMatcher m = p.getMatcher(docNoMatch.get(CoreAnnotations.TokensAnnotation.class));
      boolean match = m.find();
      assertFalse(match);
    }
    timing.stop("testTokenSequenceOptimizeOrString no match");
  }

  public void testTokenSequenceMatcherPosNNP() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test sequence with groups
    TokenSequencePattern p = TokenSequencePattern.compile( "[ { tag:\"NNP\" } ]+");
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Mellitus", m.group());

    p = TokenSequencePattern.compile( "[ { tag:\"NNP\" } ] [ /is|was/ ] []*? [ { tag:\"NNP\" } ]+ ");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Mellitus was the first Bishop", m.group());

    TokenSequencePattern nnpPattern = TokenSequencePattern.compile( "[ { tag:\"NNP\" } ]" );
    Env env = TokenSequencePattern.getNewEnv();
    env.bind("$NNP", nnpPattern);
    p = TokenSequencePattern.compile(env, " $NNP [ /is|was/ ] []*? $NNP+ [ \"of\" ] $NNP+ ");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("Mellitus was the first Bishop of London", m.group());

    p = TokenSequencePattern.compile(env, " ($NNP) /is|was/ []*? ($NNP)+ \"of\" ($NNP)+ ");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("Mellitus was the first Bishop of London", m.group());
    assertEquals("Mellitus", m.group(1));
    assertEquals("Bishop", m.group(2));
    assertEquals("London", m.group(3));


    nnpPattern = TokenSequencePattern.compile( " ( [ { tag:\"NNP\" } ] )" );
    env.bind("$NNP", nnpPattern);
    p = TokenSequencePattern.compile(env, " $NNP /is|was/ []*? $NNP+ \"of\" $NNP+ ");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(3, m.groupCount());
    assertEquals("Mellitus was the first Bishop of London", m.group());
    assertEquals("Mellitus", m.group(1));
    assertEquals("Bishop", m.group(2));
    assertEquals("London", m.group(3));
  }

  public void testTokenSequenceMatcherNumber() throws IOException {
    CoreMap doc = createDocument("It happened on January 3, 2002");

    // Test sequence with groups
    TokenSequencePattern p = TokenSequencePattern.compile( "[ { word::IS_NUM } ]+");
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("3", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("2002", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { word>=2000 } ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("2002", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { word>2000 } ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("2002", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { word<=2000 } ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("3", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { word<2000 } ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("3", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { word==2002 } ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("2002", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { ner:DATE } ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("January 3 , 2002", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { ner::NOT_NIL } ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("January 3 , 2002", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { ner::IS_NIL } ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("It happened on", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ {{ word=~/2002/ }} ]+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("2002", m.group());
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcherABs() throws IOException {
    CoreMap doc = createDocument("A A A A A A A B A A B A C A E A A A A A A A A A A A B A A A");

    // Test sequence with groups
    TokenSequencePattern p = TokenSequencePattern.compile( "/A/+ B");
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("A A A A A A A B", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("A A B", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("A A A A A A A A A A A B", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "(/A/+ B)+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A A A A A A B A A B", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A A A A A A A A A A B", m.group());
    match = m.find();
    assertFalse(match);

  /*  p = TokenSequencePattern.compile( "( A+ ( /B/+ )? )*");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("A A A A A A A B A A B A", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("A", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(2, m.groupCount());
    assertEquals("A A A A A A A A A A A B A A A", m.group());
    match = m.find();
    assertFalse(match);              */

    p = TokenSequencePattern.compile( "(/A/+ /B/+ )+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A A A A A A B A A B", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A A A A A A A A A A B", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "(/A/+ /C/? /A/* )+");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A A A A A A", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A C A", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A A A A A A A A A A", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A A", m.group());
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcherMultiNodePattern() throws IOException {
    CoreMap doc = createDocument("blah four-years blah blah four - years");

    // Test sequence with groups
    CoreMapNodePattern nodePattern  = CoreMapNodePattern.valueOf("four\\s*-?\\s*years");
    SequencePattern.MultiNodePatternExpr expr = new SequencePattern.MultiNodePatternExpr(
            new MultiCoreMapNodePattern(nodePattern));
    TokenSequencePattern p = TokenSequencePattern.compile(expr);
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four-years", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four - years", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "(?m) /four\\s*-?\\s*years/");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four-years", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four - years", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "(?m){2,3} /four\\s*-?\\s*years/");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four - years", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "(?m){1,2} /four\\s*-?\\s*years/");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four-years", m.group());
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "(?m){1,3} /four\\s*-?\\s*years/ ==> &annotate( { ner=YEAR } )");
    m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four-years", m.group());
    p.getAction().apply(m, 0);
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four - years", m.group());
    SequenceMatchResult<CoreMap> res = p.getAction().apply(m, 0);
    match = m.find();
    assertFalse(match);

    p = TokenSequencePattern.compile( "[ { ner:YEAR } ]+");
    m = p.getMatcher(res.elements());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four-years", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(0, m.groupCount());
    assertEquals("four - years", m.group());
    match = m.find();
    assertFalse(match);
  }

  public void testTokenSequenceMatcherBackRef() throws IOException {
    CoreMap doc = createDocument("A A A A A A A B A A B A C A E A A A A A A A A A A A B A A A");

    // Test sequence with groups
    TokenSequencePattern p = TokenSequencePattern.compile( "(/A/+) B \\1");
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A B A A", m.group());
    match = m.find();
    assertTrue(match);
    assertEquals(1, m.groupCount());
    assertEquals("A A A B A A A", m.group());
    match = m.find();
    assertFalse(match);

  }

  public void testMultiPatternMatcher() throws IOException {
    CoreMap doc = createDocument(testText1);

    // Test simple sequence
    TokenSequencePattern p1 = TokenSequencePattern.compile("/Archbishop/ /of/ /Canterbury/");
    p1.setPriority(1);
    TokenSequencePattern p2 = TokenSequencePattern.compile("/[a-zA-Z]+/{1,2}  /of/ /[a-zA-Z]+/+");
    MultiPatternMatcher<CoreMap> m = new MultiPatternMatcher<CoreMap>(p2,p1);
    List<SequenceMatchResult<CoreMap>> matched = m.findNonOverlapping(doc.get(CoreAnnotations.TokensAnnotation.class));
    assertEquals(4, matched.size());
    assertEquals("first Bishop of London", matched.get(0).group());
    assertEquals("Archbishop of Canterbury", matched.get(1).group());
    assertEquals("a member of the Gregorian mission sent to England to convert the", matched.get(2).group());
    assertEquals("as Bishop of London in", matched.get(3).group());
  }

  //just to test if a pattern is compiling or not
  public void testcompile() {
    String s = "(?$se \"matching\" \"this\"|\"don't\")";
    CoreMap doc = createDocument("does this do matching this");
    TokenSequencePattern p =TokenSequencePattern.compile(s);
    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
    boolean match = m.find();
    assertTrue(match);
    //assertEquals(m.group(), "matching this");
  }

}