package edu.stanford.nlp.parser.lexparser;

import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexParseException;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.TregexPatternCompiler;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * An extension of
 * {@link edu.stanford.nlp.parser.lexparser.AbstractTreebankParserParams}
 * which provides support for Tregex-powered annotations.
 *
 * Subclasses of this class provide collections of <em>features</em>
 * which are associated with annotation behaviors that seek out
 * and label matching trees in some way. For example, a <em>coord</em>
 * feature might have an annotation behavior which searches for
 * coordinating noun phrases and labels the associated constituent
 * with a suffix <tt>-coordinating</tt>.
 *
 * The "search" in this process is conducted via Tregex, and the
 * actual annotation is done through execution of an arbitrary
 * {@link edu.stanford.nlp.util.Function} provided by the user.
 * This class carries as inner several classes several useful common
 * annotation functions.
 *
 * @see #annotations
 *
 * @author Jon Gauthier
 * @author Spence Green
 */
public abstract class TregexPoweredTreebankParserParams extends AbstractTreebankParserParams {

  /**
   * This data structure dictates how an arbitrary tree should be
   * annotated. Subclasses should fill out the related member
   * {@link #annotations}.
   *
   * It is a collection of <em>features:</em> a map from feature name
   * to behavior, where each behavior is a tuple <tt>(t, f)</tt>.
   * <tt>t</tt> is a Tregex pattern which matches subtrees
   * corresponding to the feature, and <tt>f</tt> is a function which
   * accepts such matches and generates an annotation which the matched
   * subtree should be given.
   *
   * @see #annotations
   */
  private final Map<String, Pair<TregexPattern, Function<TregexMatcher, String>>> annotationPatterns
    = Generics.newHashMap();

  /**
   * This data structure dictates how an arbitrary tree should be
   * annotated.
   *
   * It is a collection of <em>features:</em> a map from feature name
   * to behavior, where each behavior is a tuple <tt>(t, f)</tt>.
   * <tt>t</tt> is a string form of a TregexPattern which matches
   * subtrees corresponding to the feature, and <tt>f</tt> is a
   * function which accepts such matches and generates an annotation
   * which the matched subtree should be given.
   *
   * @see #annotationPatterns
   */
  protected final Map<String, Pair<String, Function<TregexMatcher, String>>> annotations
    = Generics.newHashMap();

  /**
   * Features which should be enabled by default.
   */
  protected final String[] baselineFeatures = {};

  /**
   * Extra features which have been requested. Use
   * {@link #addFeature(String)} to add features.
   */
  private final List<String> features;

  public TregexPoweredTreebankParserParams(TreebankLanguagePack tlp) {
    super(tlp);

    features = Arrays.asList(baselineFeatures);
  }

  /**
   * Compile the {@link #annotations} collection given a
   * particular head finder. Subclasses should call this method at
   * least once before the class is used, and whenever the head finder
   * is changed.
   */
  protected void compileAnnotations(HeadFinder hf) {
    TregexPatternCompiler compiler = new TregexPatternCompiler(hf);

    annotationPatterns.clear();
    for (Map.Entry<String, Pair<String, Function<TregexMatcher, String>>> annotation : annotations.entrySet()) {
      TregexPattern compiled;
      try {
        compiled = compiler.compile(annotation.getValue().first());
      } catch (TregexParseException e) {
        int nth = annotationPatterns.size() + 1;
        System.err.println("Parse exception on annotation pattern #" + nth + " initialization: " + e);
        continue;
      }

      Pair<TregexPattern, Function<TregexMatcher, String>> behavior =
        new Pair<TregexPattern, Function<TregexMatcher, String>>(compiled, annotation.getValue().second());

      annotationPatterns.put(annotation.getKey(), behavior);
    }
  }

  /**
   * Enable an annotation feature.
   *
   * @param featureName
   * @throws java.lang.IllegalArgumentException If the provided feature
   *           name is unknown (i.e., if there is no entry in the
   *           {@link #annotations} collection with the same name)
   */
  protected void addFeature(String featureName) {
    if (!annotations.containsKey(featureName))
      throw new IllegalArgumentException("Invalid feature name '" + featureName + "'");
    if (!annotationPatterns.containsKey(featureName))
      throw new RuntimeException("Compiled patterns out of sync with annotations data structure;" +
        "did you call compileAnnotations?");

    features.add(featureName);
  }

  /**
   * This method does language-specific tree transformations such as annotating particular nodes with language-relevant
   * features. Such parameterizations should be inside the specific TreebankLangParserParams class.  This method is
   * recursively applied to each node in the tree (depth first, left-to-right), so you shouldn't write this method to
   * apply recursively to tree members.  This method is allowed to (and in some cases does) destructively change the
   * input tree <code>t</code>. It changes both labels and the tree shape.
   *
   * @param t    The input tree (with non-language specific annotation already done, so you need to strip back to basic
   *             categories)
   * @param root The root of the current tree (can be null for words)
   * @return The fully annotated tree node (with daughters still as you want them in the final result)
   */
  @Override
  public Tree transformTree(Tree t, Tree root) {
    String newCat = t.value() + getAnnotationString(t, root);
    t.setValue(newCat);
    if (t.isPreTerminal() && t.label() instanceof HasTag)
      ((HasTag) t.label()).setTag(newCat);

    return t;
  }

  /**
   * Build a string of annotations for the given tree.
   *
   * @param t The input tree (with non-language specific annotation
   *          already done, so you need to strip back to basic categories)
   * @param root The root of the current tree (can be null for words)
   * @return A (possibly empty) string of annotations to add to the
   *         given tree
   */
  protected String getAnnotationString(Tree t, Tree root) {
    // Accumulate all annotations in this string
    StringBuilder annotationStr = new StringBuilder();

    for (String featureName : features) {
      Pair<TregexPattern, Function<TregexMatcher, String>> behavior = annotationPatterns.get(featureName);
      TregexMatcher m = behavior.first().matcher(root);
      if (m.matchesAt(t))
        annotationStr.append(behavior.second().apply(m));
    }

    return annotationStr.toString();
  }
}