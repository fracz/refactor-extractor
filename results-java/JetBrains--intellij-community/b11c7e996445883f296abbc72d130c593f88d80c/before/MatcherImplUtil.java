package com.intellij.structuralsearch.impl.matcher;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiCatchSection;
import com.intellij.psi.PsiElement;
import com.intellij.structuralsearch.MatchOptions;
import com.intellij.structuralsearch.StructuralSearchProfile;
import com.intellij.structuralsearch.StructuralSearchUtil;
import com.intellij.structuralsearch.impl.matcher.compiler.PatternCompiler;
import com.intellij.util.IncorrectOperationException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Mar 19, 2004
 * Time: 6:56:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatcherImplUtil {
  public static final Key<List<PsiCatchSection>> UNMATCHED_CATCH_SECTION_CONTENT_VAR_KEY = Key.create("UnmatchedCatchSection");

  public static void transform(MatchOptions options) {
    if (options.hasVariableConstraints()) return;
    PatternCompiler.transformOldPattern(options);
  }

  public static PsiElement[] createTreeFromText(String text, PatternTreeContext context, FileType fileType, Project project)
    throws IncorrectOperationException {
    if (fileType instanceof LanguageFileType) {
      Language language = ((LanguageFileType)fileType).getLanguage();
      StructuralSearchProfile profile = StructuralSearchUtil.getProfileByLanguage(language);
      if (profile != null) {
        return profile.createPatternTree(text, context, fileType, project);
      }
    }
    return PsiElement.EMPTY_ARRAY;
  }
}