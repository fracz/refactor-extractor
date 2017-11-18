package com.intellij.structuralsearch.impl.matcher.compiler;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.structuralsearch.*;
import com.intellij.structuralsearch.impl.matcher.CompiledPattern;
import com.intellij.structuralsearch.impl.matcher.MatcherImplUtil;
import com.intellij.structuralsearch.impl.matcher.filters.LexicalNodesFilter;
import com.intellij.structuralsearch.impl.matcher.filters.NodeFilter;
import com.intellij.structuralsearch.impl.matcher.handlers.MatchPredicate;
import com.intellij.structuralsearch.impl.matcher.handlers.SubstitutionHandler;
import com.intellij.structuralsearch.impl.matcher.iterators.ArrayBackedNodeIterator;
import com.intellij.structuralsearch.impl.matcher.predicates.*;
import com.intellij.structuralsearch.plugin.ui.Configuration;
import com.intellij.util.IncorrectOperationException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Compiles the handlers for usability
 */
public class PatternCompiler {
  private static CompileContext lastTestingContext;

  public static void transformOldPattern(MatchOptions options) {
    StringToConstraintsTransformer.transformOldPattern(options);
  }

  public static CompiledPattern compilePattern(final Project project, final MatchOptions options) throws MalformedPatternException,
                                                                                                         UnsupportedOperationException {
    return new WriteAction<CompiledPattern>() {
      protected void run(Result<CompiledPattern> result) throws Throwable {
        result.setResult(compilePatternImpl(project, options));
      }
    }.execute().getResultObject();
  }

  public static String getLastFindPlan() {
    return ((TestModeOptimizingSearchHelper)lastTestingContext.getSearchHelper()).getSearchPlan();
  }

  private static CompiledPattern compilePatternImpl(Project project,MatchOptions options) {
    final StringBuilder buf = new StringBuilder();
    final CompileContext context = new CompileContext();
    if (ApplicationManager.getApplication().isUnitTestMode()) lastTestingContext = context;

    /*CompiledPattern result = options.getFileType() == StdFileTypes.JAVA ?
                             new JavaCompiledPattern() :
                             new XmlCompiledPattern();*/
    FileType fileType = options.getFileType();
    assert fileType instanceof LanguageFileType;
    Language language = ((LanguageFileType)fileType).getLanguage();
    StructuralSearchProfile profile = StructuralSearchUtil.getProfileByLanguage(language);
    assert profile != null;
    CompiledPattern result = profile.createCompiledPattern();

    try {
      context.init(result, options,project, options.getScope() instanceof GlobalSearchScope);
      Template template = TemplateManager.getInstance(project).createTemplate("","",options.getSearchPattern());

      int segmentsCount = template.getSegmentsCount();
      String text = template.getTemplateText();
      buf.setLength(0);
      int prevOffset = 0;

      for(int i=0;i<segmentsCount;++i) {
        final int offset = template.getSegmentOffset(i);
        final String name = template.getSegmentName(i);

        buf.append(text.substring(prevOffset,offset));
        buf.append(result.getTypedVarPrefix());
        buf.append(name);

        MatchVariableConstraint constraint = options.getVariableConstraint(name);
        if (constraint==null) {
          // we do not edited the constraints
          constraint = new MatchVariableConstraint();
          constraint.setName( name );
          options.addVariableConstraint(constraint);
        }

        SubstitutionHandler handler = result.createSubstitutionHandler(
          name,
          result.getTypedVarPrefix() + name,
          constraint.isPartOfSearchResults(),
          constraint.getMinCount(),
          constraint.getMaxCount(),
          constraint.isGreedy()
        );

        if(constraint.isWithinHierarchy()) {
          handler.setSubtype(true);
        }

        if(constraint.isStrictlyWithinHierarchy()) {
          handler.setStrictSubtype(true);
        }

        MatchPredicate predicate;

        if (constraint.getRegExp()!=null && constraint.getRegExp().length() > 0) {
          predicate = new RegExpPredicate(
            constraint.getRegExp(),
            options.isCaseSensitiveMatch(),
            name,
            constraint.isWholeWordsOnly(),
            constraint.isPartOfSearchResults()
          );
          if (constraint.isInvertRegExp()) {
            predicate = new NotPredicate(predicate);
          }
          addPredicate(handler,predicate);
        }

        if (constraint.isReadAccess()) {
          predicate = new ReadPredicate();

          if (constraint.isInvertReadAccess()) {
            predicate = new NotPredicate(predicate);
          }
          addPredicate(handler,predicate);
        }

        if (constraint.isWriteAccess()) {
          predicate = new WritePredicate();

          if (constraint.isInvertWriteAccess()) {
            predicate = new NotPredicate(predicate);
          }
          addPredicate(handler,predicate);
        }

        if (constraint.isReference()) {
          predicate = new ReferencePredicate( constraint.getNameOfReferenceVar() );

          if (constraint.isInvertReference()) {
            predicate = new NotPredicate(predicate);
          }
          addPredicate(handler,predicate);
        }

        if (constraint.getNameOfExprType()!=null &&
            constraint.getNameOfExprType().length() > 0
            ) {
          predicate = new ExprTypePredicate(
            constraint.getNameOfExprType(),
            name,
            constraint.isExprTypeWithinHierarchy(),
            options.isCaseSensitiveMatch(),
            constraint.isPartOfSearchResults()
          );

          if (constraint.isInvertExprType()) {
            predicate = new NotPredicate(predicate);
          }
          addPredicate(handler,predicate);
        }

        if (constraint.getNameOfFormalArgType()!=null && constraint.getNameOfFormalArgType().length() > 0) {
          predicate = new FormalArgTypePredicate(
            constraint.getNameOfFormalArgType(),
            name,
            constraint.isFormalArgTypeWithinHierarchy(),
            options.isCaseSensitiveMatch(),
            constraint.isPartOfSearchResults()
          );
          if (constraint.isInvertFormalType()) {
            predicate = new NotPredicate(predicate);
          }
          addPredicate(handler,predicate);
        }

        addScriptConstraint(name, constraint, handler);

        if (constraint.getContainsConstraint() != null && constraint.getContainsConstraint().length() > 0) {
          predicate = new ContainsPredicate(name, constraint.getContainsConstraint());
          if (constraint.isInvertContainsConstraint()) {
            predicate = new NotPredicate(predicate);
          }
          addPredicate(handler,predicate);
        }

        if (constraint.getWithinConstraint() != null && constraint.getWithinConstraint().length() > 0) {
          assert false;
        }

        prevOffset = offset;
      }

      MatchVariableConstraint constraint = options.getVariableConstraint(Configuration.CONTEXT_VAR_NAME);
      if (constraint != null) {
        SubstitutionHandler handler = result.createSubstitutionHandler(
          Configuration.CONTEXT_VAR_NAME,
          Configuration.CONTEXT_VAR_NAME,
          constraint.isPartOfSearchResults(),
          constraint.getMinCount(),
          constraint.getMaxCount(),
          constraint.isGreedy()
        );

        if (constraint.getWithinConstraint() != null && constraint.getWithinConstraint().length() > 0) {
          MatchPredicate predicate = new WithinPredicate(Configuration.CONTEXT_VAR_NAME, constraint.getWithinConstraint(), project);
          if (constraint.isInvertWithinConstraint()) {
            predicate = new NotPredicate(predicate);
          }
          addPredicate(handler,predicate);
        }

        addScriptConstraint(Configuration.CONTEXT_VAR_NAME, constraint, handler);
      }

      buf.append(text.substring(prevOffset,text.length()));

      PsiElement patternNode;
      PsiElement[] matchStatements;

      try {
        matchStatements = MatcherImplUtil.createTreeFromText(buf.toString(), MatcherImplUtil.TreeContext.Block, options.getFileType(), project);
        if (matchStatements.length==0) throw new MalformedPatternException();
        patternNode = matchStatements[0].getParent();
      } catch (IncorrectOperationException e) {
        throw new MalformedPatternException(e.getMessage());
      }

      NodeFilter filter = LexicalNodesFilter.getInstance();

      GlobalCompilingVisitor compilingVisitor = new GlobalCompilingVisitor();
      compilingVisitor.compile(patternNode,context);
      List<PsiElement> elements = new LinkedList<PsiElement>();

      for (PsiElement matchStatement : matchStatements) {
        if (!filter.accepts(matchStatement)) {
          elements.add(matchStatement);
        }
      }
      context.getPattern().setNodes(
        new ArrayBackedNodeIterator(elements.toArray(new PsiElement[elements.size()]))
      );

      // delete last brace
      ApplicationManager.getApplication().runWriteAction(
        new DeleteNodesAction(compilingVisitor.getLexicalNodes())
      );

      if (context.getSearchHelper().doOptimizing() && context.getSearchHelper().isScannedSomething()) {
        final Set<PsiFile> set = context.getSearchHelper().getFilesSetToScan();
        final List<PsiFile> filesToScan = new ArrayList<PsiFile>(set.size());
        final GlobalSearchScope scope = (GlobalSearchScope)options.getScope();

        for (final PsiFile file : set) {
          if (!scope.contains(file.getVirtualFile())) {
            continue;
          }

          if (file instanceof PsiFileImpl) {
            ((PsiFileImpl)file).clearCaches();
          }
          filesToScan.add(file);
        }

        if (filesToScan.size() == 0) {
          throw new MalformedPatternException(SSRBundle.message("ssr.will.not.find.anything"));
        }
        result.setScope(
          new LocalSearchScope( filesToScan.toArray(new PsiElement[filesToScan.size()]) )
        );
      }
    } finally {
      context.clear();
    }

    return result;
  }

  private static void addScriptConstraint(String name, MatchVariableConstraint constraint, SubstitutionHandler handler) {
    MatchPredicate predicate;
    if (constraint.getScriptCodeConstraint()!= null && constraint.getScriptCodeConstraint().length() > 2) {
      final String script = StringUtil.stripQuotesAroundValue(constraint.getScriptCodeConstraint());
      final String s = ScriptSupport.checkValidScript(script);
      if (s != null) throw new MalformedPatternException("Script constraint for " + constraint.getName() + " has problem "+s);
      predicate = new ScriptPredicate(name, script);
      addPredicate(handler,predicate);
    }
  }

  static void addPredicate(SubstitutionHandler handler, MatchPredicate predicate) {
    if (handler.getPredicate()==null) {
      handler.setPredicate(predicate);
    } else {
      handler.setPredicate(
        new BinaryPredicate(
          handler.getPredicate(),
          predicate,
          false
        )
      );
    }
  }

}