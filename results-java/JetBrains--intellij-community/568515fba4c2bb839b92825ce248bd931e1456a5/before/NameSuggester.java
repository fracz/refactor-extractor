package com.intellij.refactoring.rename.naming;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiNameHelper;
import gnu.trove.TIntIntHashMap;

import java.util.*;

/**
 * @author dsl
 */
public class NameSuggester {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.rename.naming.NameSuggester");
  private final String[] myOldClassName;
  private final String[] myNewClassName;
  private final List<OriginalToNewChange> myChanges; // sorted from right to left
  private final String myOldClassNameAsGiven;
  private final String myNewClassNameAsGiven;


  public NameSuggester(String oldClassName, String newClassName) {
    myOldClassNameAsGiven = oldClassName;
    myNewClassNameAsGiven = newClassName;
    myOldClassName = PsiNameHelper.splitNameIntoWords(oldClassName);
    myNewClassName = PsiNameHelper.splitNameIntoWords(newClassName);

    myChanges = new ArrayList<OriginalToNewChange>();
    int oldIndex = myOldClassName.length - 1;
    int oldLastMatch = myOldClassName.length;
    int newLastMatch = myNewClassName.length;

    while(oldIndex >= 0) {
      final String patternWord = myOldClassName[oldIndex];
      final int matchingWordIndex = findInNewBackwardsFromIndex(patternWord, newLastMatch - 1);
      if (matchingWordIndex < 0) { // no matching word
        oldIndex--;
      }
      else { // matching word found
        if (oldIndex + 1 <= oldLastMatch - 1 || matchingWordIndex + 1 <= newLastMatch - 1) {
          final OriginalToNewChange change = new OriginalToNewChange(
            oldIndex + 1, oldLastMatch - 1, matchingWordIndex + 1, newLastMatch - 1);
          myChanges.add(change);
        }
        oldLastMatch = oldIndex;
        newLastMatch = matchingWordIndex;
        oldIndex--;
      }
    }
    if (0 <= oldLastMatch - 1 || 0 <= newLastMatch - 1) {
      myChanges.add(new OriginalToNewChange(0, oldLastMatch - 1, 0, newLastMatch - 1));
    }
  }

  private int findInNewBackwardsFromIndex(String patternWord, int newIndex) {
    for (int i = newIndex; i >= 0; i--) {
      final String s = myNewClassName[i];
      if (s.equals(patternWord)) return i;
    }
    return -1;
  }

  List<Pair<String,String>> getChanges() {
    final ArrayList<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
    for (int i = myChanges.size() - 1; i >=0; i--) {
      final OriginalToNewChange change = myChanges.get(i);
      result.add(Pair.create(change.getOldString(), change.getNewString()));
    }
    return result;
  }

  public String suggestName(final String propertyName) {
    final String[] propertyWords = PsiNameHelper.splitNameIntoWords(propertyName);
    TIntIntHashMap matches = calculateMatches(propertyWords);
    if (matches.isEmpty()) return propertyName;
    TreeMap<Pair<Integer,Integer>, String> replacements = calculateReplacements(propertyWords, matches);
    if (replacements.isEmpty()) return propertyName;
    final String newName = calculateNewName(replacements, propertyWords, propertyName);
    return newName;
  }


  private static Pair<int[],int[]> calculateWordPositions(String s, String[] words) {
    int[] starts = new int[words.length + 1];
    int[] prevEnds = new int[words.length + 1];
    int pos = 0;
    prevEnds[0] = -1;
    for (int i = 0; i < words.length; i++) {
      final String word = words[i];
      final int index = s.indexOf(word, pos);
      LOG.assertTrue(index >= 0);
      starts[i] = index;
      pos = index + word.length();
      prevEnds[i + 1] = pos - 1;
    }
    starts[words.length] = s.length();
    return Pair.create(starts, prevEnds);
  }

  private String calculateNewName(TreeMap<Pair<Integer, Integer>, String> replacements,
                                  final String[] propertyWords,
                                  String propertyName) {
    StringBuffer resultingWords = new StringBuffer();
    int currentWord = 0;
    final Pair<int[],int[]> wordIndicies = calculateWordPositions(propertyName, propertyWords);
    for (Iterator<Map.Entry<Pair<Integer,Integer>,String>> iterator = replacements.entrySet().iterator(); iterator.hasNext();) {
      final Map.Entry<Pair<Integer,Integer>,String> entry = iterator.next();
      final int first = entry.getKey().getFirst().intValue();
      final int last = entry.getKey().getSecond().intValue();
      for (int i = currentWord; i < first; i++) {
        resultingWords.append(calculateBetween(wordIndicies, i, propertyName));
        final String propertyWord = propertyWords[i];
        appendWord(resultingWords, propertyWord);
      }
      resultingWords.append(calculateBetween(wordIndicies, first, propertyName));
      appendWord(resultingWords, entry.getValue());
      currentWord = last + 1;
    }
    for(; currentWord < propertyWords.length; currentWord++) {
      resultingWords.append(calculateBetween(wordIndicies, currentWord, propertyName));
      appendWord(resultingWords, propertyWords[currentWord]);
    }
    resultingWords.append(calculateBetween(wordIndicies, propertyWords.length, propertyName));
    if (resultingWords.length() == 0) return propertyName;
    return decapitalizeProbably(resultingWords.toString(), propertyName);
  }

  private void appendWord(StringBuffer resultingWords, String propertyWord) {
    if (resultingWords.length() > 0) {
      final char lastChar = resultingWords.charAt(resultingWords.length() - 1);
      if (Character.isLetterOrDigit(lastChar)) {
        propertyWord = StringUtil.capitalize(propertyWord);
      }
    }
    resultingWords.append(propertyWord);
  }

  private String calculateBetween(final Pair<int[], int[]> wordIndicies, int i, String propertyName) {
    final int thisWordStart = wordIndicies.getFirst()[i];
    final int prevWordEnd = wordIndicies.getSecond()[i];
    final String between = propertyName.substring(prevWordEnd + 1, thisWordStart);
    return between;
  }

  /**
   * Calculates a map of replacements. Result has a form:<br>
   * {&lt;first,last&gt; -&gt; replacement} <br>
   * where start and end are indices of property words range (inclusive), and replacement is a
   * string that this range must be replaced with.<br>
   * It is valid situation that <code>last == first - 1</code>: in this case replace means insertion
   * before first word. Furthermore, first may be equal to <code>propertyWords.length</code>  - in
   * that case replacements transormates to appending.
   * @param propertyWords
   * @param matches
   * @return
   */
  private TreeMap<Pair<Integer, Integer>, String> calculateReplacements(String[] propertyWords, TIntIntHashMap matches) {
    TreeMap<Pair<Integer,Integer>, String> replacements = new TreeMap<Pair<Integer,Integer>, String>(new Comparator<Pair<Integer, Integer>>() {
      public int compare(Pair<Integer, Integer> pair, Pair<Integer, Integer> pair1) {
        return pair.getFirst().compareTo(pair1.getFirst());
      }
    });
    for (Iterator<OriginalToNewChange> iterator = myChanges.iterator(); iterator.hasNext();) {
      final OriginalToNewChange change = iterator.next();
      final int first = change.oldFirst;
      final int last = change.oldLast;
      if (change.getOldLength() > 0) {
        if (containsAllBetween(matches, first, last)) {
          final String newString = change.getNewString();
          final int propertyWordFirst = matches.get(first);

          if (first >= myOldClassName.length || last >= myOldClassName.length) {
            LOG.assertTrue(false, "old class name = " + myOldClassNameAsGiven +
                                  ", new class name = " + myNewClassNameAsGiven +
                                  ", propertyWords = " + Arrays.asList(propertyWords).toString());
          }

          final String replacement = suggestReplacement(myOldClassName[first], propertyWords[propertyWordFirst], newString);
          replacements.put(Pair.create(new Integer(propertyWordFirst),
                                       new Integer(matches.get(last))), replacement);
        }
      }
      else {
        final String newString = change.getNewString();
        final int propertyWordToInsertBefore;
        if (matches.containsKey(first)) {
          propertyWordToInsertBefore = matches.get(first);
        }
        else {
          propertyWordToInsertBefore = propertyWords.length;
        }
        replacements.put(Pair.create(new Integer(propertyWordToInsertBefore), new Integer(propertyWordToInsertBefore - 1)), newString);
      }
    }
    return replacements;
  }

  private String suggestReplacement(String classNameWord, String propertyWord, String newClassNameWords) {
    return decapitalizeProbably(newClassNameWords, propertyWord);
  }

  private static String decapitalizeProbably(String word, String originalWord) {
    if (originalWord.length() == 0) return word;
    if (Character.isLowerCase(originalWord.charAt(0))) {
      return StringUtil.decapitalize(word);
    }
    return word;
  }

  private boolean containsAllBetween(TIntIntHashMap matches, int first, int last) {
    for (int i = first; i <= last; i++) {
      if (!matches.containsKey(i)) return false;
    }
    return true;
  }

  private TIntIntHashMap calculateMatches(final String[] propertyWords) {
    int classNameIndex = myOldClassName.length - 1;
    TIntIntHashMap matches = new TIntIntHashMap();
    for (int i = propertyWords.length - 1; i >= 0; i--) {
      final String propertyWord = propertyWords[i];
      Match match = null;
      for (int j = classNameIndex; j >= 0 && match == null; j--) {
        match = checkMatch(j, i, propertyWord);
      }
      if (match != null) {
        matches.put(match.oldClassNameIndex, i);
        classNameIndex = match.oldClassNameIndex - 1;
      }
    }
    return matches;
  }

  private class OriginalToNewChange {
    final int oldFirst;
    final int oldLast;
    final int newFirst;
    final int newLast;

    public OriginalToNewChange(int firstInOld, int lastInOld, int firstInNew, int lastInNew) {
      this.oldFirst = firstInOld;
      this.oldLast = lastInOld;
      this.newFirst = firstInNew;
      this.newLast = lastInNew;
    }

    int getOldLength() {
      return oldLast - oldFirst + 1;
    }

    String getOldString() {
      final StringBuffer buffer = new StringBuffer();
      for (int i = oldFirst; i <= oldLast; i++) {
        buffer.append(myOldClassName[i]);
      }
      return buffer.toString();
    }

    String getNewString() {
      final StringBuffer buffer = new StringBuffer();
      for (int i = newFirst; i <= newLast; i++) {
        buffer.append(myNewClassName[i]);
      }
      return buffer.toString();
    }
  }

  private class Match {
    final int oldClassNameIndex;
    final int propertyNameIndex;
    final String propertyWord;

    public Match(int oldClassNameIndex, int propertyNameIndex, String propertyWord) {
      this.oldClassNameIndex = oldClassNameIndex;
      this.propertyNameIndex = propertyNameIndex;
      this.propertyWord = propertyWord;
    }
  }

  private Match checkMatch(final int oldClassNameIndex, final int propertyNameIndex, final String propertyWord) {
    if (propertyWord.equalsIgnoreCase(myOldClassName[oldClassNameIndex])) {
      return new Match(oldClassNameIndex, propertyNameIndex, propertyWord);
    }
    else return null;
  }
}