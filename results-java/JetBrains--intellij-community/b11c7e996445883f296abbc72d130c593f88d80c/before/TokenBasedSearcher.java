package com.intellij.structuralsearch.impl.matcher;

import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.structuralsearch.MatchOptions;
import com.intellij.structuralsearch.StructuralSearchUtil;
import com.intellij.structuralsearch.impl.matcher.compiler.PatternCompiler;
import com.intellij.structuralsearch.impl.matcher.iterators.ArrayBackedNodeIterator;
import com.intellij.structuralsearch.impl.matcher.iterators.FilteringNodeIterator;
import com.intellij.structuralsearch.impl.matcher.iterators.NodeIterator;
import com.intellij.tokenindex.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.util.containers.IntArrayList;
import com.intellij.util.indexing.FileBasedIndex;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Eugene.Kudelevsky
 */
public class TokenBasedSearcher {
  private final boolean myTesting;

  private final MatcherImpl myMatcher;
  private List<Token> myTokens;
  private int[] myOccurences;

  public TokenBasedSearcher(MatcherImpl matcher) {
    myTesting = ApplicationManager.getApplication().isUnitTestMode();
    myMatcher = matcher;
  }

  public void search(CompiledPattern compiledPattern) {
    Set<Language> languages = getLanguages(compiledPattern);
    assert languages.size() == 1;
    final Language patternLanguage = languages.iterator().next();
    final Tokenizer patternTokenizer = StructuralSearchUtil.getTokenizerForLanguage(patternLanguage);
    final List<PsiElement> patternRoots = getNodes(compiledPattern);
    MatchOptions options = myMatcher.getMatchContext().getOptions();
    final SearchScope scope = options.getScope();
    final List<Token> patternTokens = patternTokenizer.tokenize(patternRoots);

    addTask(new Runnable() {
      public void run() {
        readTokens(patternLanguage, scope);
        ProgressIndicator progress = myMatcher.getProgress();
        if (progress != null) {
          progress.setFraction(0.15);
        }
      }
    });

    final MatchContext context = new MatchContext();
    context.setOptions(options);
    context.setSink(myMatcher.getMatchContext().getSink());
    context.setPattern(compiledPattern);
    GlobalMatchingVisitor visitor = new GlobalMatchingVisitor();
    visitor.setMatchContext(context);
    context.setMatcher(visitor);

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        search(patternTokens);
      }
    });
  }

  private void addTask(Runnable r) {
    if (myTesting) {
      r.run();
    }
    else {
      myMatcher.getScheduler().addOneTask(r);
    }
  }

  private void readTokens(Language patternLanguage, SearchScope scope) {
    assert scope instanceof GlobalSearchScope || scope instanceof LocalSearchScope;

    if (scope instanceof GlobalSearchScope) {
      myTokens = getTokensFromIndex(myMatcher.getProject(), patternLanguage, scope);
    }
    else {
      Set<PsiFile> files = new HashSet<PsiFile>();
      PsiElement[] elements = ((LocalSearchScope)scope).getScope();
      if (elements.length == 0) {
        return;
      }
      for (PsiElement element : elements) {
        files.add(element.getContainingFile());
      }
      Language scopeLanguage = elements[0].getLanguage();
      Tokenizer tokenizer = StructuralSearchUtil.getTokenizerForLanguage(scopeLanguage);
      assert tokenizer != null;
      List<PsiElement> elementList = new ArrayList<PsiElement>();
      Collections.addAll(elementList, elements);
      myTokens = new ArrayList<Token>();
      for (PsiFile file : files) {
        myTokens.addAll(tokenizer.tokenize(Arrays.asList(file)));
        myTokens.add(new PsiMarkerToken(file));
      }
    }
  }

  private class MySearcher implements Runnable {
    private final TIntObjectHashMap<PsiFile> myPsiFiles = new TIntObjectHashMap<PsiFile>();
    private final Set<PsiElement> startElements = new HashSet<PsiElement>();
    private final int myPatternLength;
    private double myStartFraction = -1;

    private int myIndex = 0;
    private int myBound = 0;

    private MySearcher(int patternLength) {
      myPatternLength = patternLength;
    }

    public void run() {
      ProgressIndicator progress = myMatcher.getProgress();
      if (progress != null && myStartFraction < 0) {
        myStartFraction = progress.getFraction();
      }
      if (myIndex >= myOccurences.length) {
        return;
      }
      if (myIndex > 0) {
        if (progress != null) {
          progress.setFraction(myStartFraction + (1.0 - myStartFraction) / myOccurences.length * myIndex);
        }
      }
      int occurence = myOccurences[myIndex];
      if (occurence < myBound) {
        doContinue();
        return;
      }
      PsiFile psiFile = getPsiFileForOccurence(myTokens, occurence, myPsiFiles);
      if (psiFile == null) {
        doContinue();
        return;
      }
      myPsiFiles.put(occurence, psiFile);
      int start = myTokens.get(occurence).getOffset();
      assert start >= 0;
      int afterOccurence = occurence + myPatternLength;
      int end = -1;
      if (afterOccurence < myTokens.size()) {
        Token afterOccurenceToken = myTokens.get(afterOccurence);
        end = afterOccurenceToken.getOffset();
      }
      if (end < 0) {
        end = psiFile.getTextLength();
      }

      final MatchContext context = myMatcher.getMatchContext();
      SearchScope scope = context.getOptions().getScope();

      final PsiElement[] elements;
      if (scope instanceof LocalSearchScope) {
        List<PsiElement> elementList = filterScope(psiFile, start, end, (LocalSearchScope)scope);
        elements = elementList.toArray(new PsiElement[elementList.size()]);
      }
      else {
        elements = new PsiElement[]{psiFile};
      }
      if (elements.length > 0) {
        context.setResult(null);
        if (startElements.add(elements[0])) {
          myBound = occurence + myPatternLength;
          ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
              context.getMatcher().matchContext(new FilteringNodeIterator(new ArrayBackedNodeIterator(elements)));
            }
          });
        }
      }
      doContinue();
    }

    private void doContinue() {
      myIndex++;
      addTask(this);
    }
  }

  private void search(final List<Token> patternTokens) {
    addTask(new Runnable() {
      public void run() {
        if (myTokens == null) {
          return;
        }
        assert myTokens.size() > 0;
        myOccurences = kmp(myTokens, patternTokens);
        ProgressIndicator progress = myMatcher.getProgress();
        if (progress != null) {
          progress.setFraction(0.3);
        }
      }
    });

    final int patternLength = patternTokens.size();
    assert patternLength > 0;

    addTask(new MySearcher(patternLength));
  }

  @Nullable
  private PsiFile getPsiFileForOccurence(List<Token> tokens, int occurence, TIntObjectHashMap<PsiFile> myPsiFiles) {
    PsiFile psiFile = null;
    for (int i = occurence; i < tokens.size(); i++) {
      if (myPsiFiles.contains(i)) {
        psiFile = myPsiFiles.get(i);
        break;
      }
      Token token = tokens.get(i);
      if (token instanceof PathMarkerToken) {
        String path = ((PathMarkerToken)token).getPath();
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file != null) {
          psiFile = ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
            public PsiFile compute() {
              return PsiManager.getInstance(myMatcher.getProject()).findFile(file);
            }
          });
        }
        break;
      }
      else if (token instanceof PsiMarkerToken) {
        psiFile = ((PsiMarkerToken)token).getFile();
        break;
      }
    }
    return psiFile;
  }

  @SuppressWarnings({"AssignmentToForLoopParameter"})
  private static int[] kmp(List<Token> tokens, List<Token> pattern) {
    int m = pattern.size();
    int n = tokens.size();

    if (n < m) {
      return ArrayUtil.EMPTY_INT_ARRAY;
    }

    int[] pf = new int[m];
    pf[0] = 0;
    for (int k = 0, i = 1; i < m; i++) {
      while (k > 0 && !pattern.get(i).equals(pattern.get(k))) {
        k = pf[k - 1];
      }

      if (pattern.get(i).equals(pattern.get(k))) {
        k++;
      }

      pf[i] = k;
    }

    IntArrayList result = new IntArrayList();
    for (int k = 0, i = 0; i < n; i++) {
      while ((k > 0) && !pattern.get(k).equals(tokens.get(i))) {
        k = pf[k - 1];
      }

      if (pattern.get(k).equals(tokens.get(i))) {
        k++;
      }

      if (k == m) {
        result.add(i - m + 1);
        k = pf[k - 1];
      }
    }
    return result.toArray();
  }

  private static List<PsiElement> filterScope(PsiFile file, int start, int end, LocalSearchScope scope) {
    List<PsiElement> elements = new ArrayList<PsiElement>();
    for (PsiElement element : scope.getScope()) {
      if (file == element) {
        elements.add(element);
        break;
      }
      else if (file == element.getContainingFile()) {
        TextRange range = element.getTextRange();
        if (end > range.getStartOffset() && start < range.getEndOffset()) {
          elements.add(element);
          continue;
        }
      }
      if (elements.size() > 0) {
        break;
      }
    }
    return elements;
  }

  private static List<Token> getTokensFromIndex(final Project project, final Language language, final SearchScope scope) {
    final List<Token> result = new ArrayList<Token>();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        Collection<TokenIndexKey> keys = fileBasedIndex.getAllKeys(TokenIndex.ID, project);
        for (TokenIndexKey key : keys) {
          if (key.getLanguageId().equals(language.getID())) {
            List<List<Token>> list = fileBasedIndex.getValues(TokenIndex.ID, key, (GlobalSearchScope)scope);
            for (List<Token> tokens : list) {
              result.addAll(tokens);
            }
          }
        }
      }
    });
    return result;
  }

  private static Set<Language> getLanguages(CompiledPattern compiledPattern) {
    Set<Language> languages = new HashSet<Language>();
    NodeIterator iterator = compiledPattern.getNodes();
    while (iterator.hasNext()) {
      PsiElement element = iterator.current();
      languages.add(element.getLanguage());
      iterator.advance();
    }
    iterator.reset();
    return languages;
  }

  private static Set<Language> getLanguages(PsiElement[] elements) {
    Set<Language> languages = new HashSet<Language>();
    for (PsiElement element : elements) {
      languages.add(element.getLanguage());
    }
    return languages;
  }

  private static List<PsiElement> getNodes(CompiledPattern compiledPattern) {
    List<PsiElement> result = new ArrayList<PsiElement>();
    NodeIterator iterator = compiledPattern.getNodes();
    while (iterator.hasNext()) {
      PsiElement element = iterator.current();
      result.add(element);
      iterator.advance();
    }
    iterator.reset();
    return result;
  }

  public static boolean canProcess(Project project, MatchOptions options) {
    CompiledPattern compiledPattern = PatternCompiler.compilePattern(project, options);
    if (!checkLanguages(getLanguages(compiledPattern))) {
      return false;
    }
    SearchScope scope = options.getScope();
    if (scope instanceof LocalSearchScope) {
      PsiElement[] elements = ((LocalSearchScope)scope).getScope();
      return checkLanguages(getLanguages(elements));
    }
    return scope instanceof GlobalSearchScope;
  }

  private static boolean checkLanguages(Set<Language> languages) {
    if (languages.size() != 1) {
      // multi languages is not yet support
      return false;
    }
    for (Language language : languages) {
      if (!TokenIndex.supports(language)) {
        return false;
      }
    }
    return true;
  }
}