package com.intellij.structuralsearch;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.structuralsearch.impl.matcher.MatcherImpl;
import com.intellij.structuralsearch.impl.matcher.MatcherImplUtil;
import com.intellij.testFramework.IdeaTestCase;

import java.util.List;

abstract class StructuralSearchTestCase extends IdeaTestCase {
  protected MatchOptions options;
  protected Matcher testMatcher;

  protected void setUp() throws Exception {
    super.setUp();

    testMatcher = new Matcher(myProject);
    options = new MatchOptions();
    options.setLooseMatching(true);
    options.setRecursiveSearch(true);
    LanguageLevelProjectExtension.getInstance(myProject).setLanguageLevel(LanguageLevel.JDK_1_5);
  }

  @Override
  protected void tearDown() throws Exception {
    testMatcher = null;
    options = null;
    super.tearDown();
  }

  protected int findMatchesCount(String in, String pattern, boolean filePattern, FileType fileType) {
    return findMatches(in,pattern,filePattern, fileType).size();
  }

  protected List<MatchResult> findMatches(String in,
                                          String pattern,
                                          boolean filePattern,
                                          FileType patternFileType,
                                          String patternFileExtension,
                                          FileType sourceFileType,
                                          String sourceFileExtension,
                                          boolean physicalSourceFile) {
    options.clearVariableConstraints();
    options.setSearchPattern(pattern);
    MatcherImplUtil.transform(options);
    pattern = options.getSearchPattern();
    options.setFileType(patternFileType);
    options.setFileExtension(patternFileExtension);

    MatcherImpl.validate(myProject, options);
    return testMatcher.testFindMatches(in, pattern, options, filePattern, sourceFileType, sourceFileExtension, physicalSourceFile);
  }

  protected List<MatchResult> findMatches(String in, String pattern, boolean filePattern, FileType patternFileType) {
    String ext = patternFileType.getDefaultExtension();
    return findMatches(in, pattern, filePattern, patternFileType, ext, patternFileType, ext, false);
  }

  protected int findMatchesCount(String in, String pattern, boolean filePattern) {
    return findMatchesCount(in, pattern,filePattern, StdFileTypes.JAVA);
  }

  protected int findMatchesCount(String in, String pattern) {
    return findMatchesCount(in,pattern,false);
  }

  protected List<MatchResult> findMatches(String in, String pattern) {
    return findMatches(in,pattern,false, StdFileTypes.JAVA);
  }
}