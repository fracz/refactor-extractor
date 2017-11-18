package com.intellij.structuralsearch;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.structuralsearch.impl.matcher.CompiledPattern;
import com.intellij.structuralsearch.impl.matcher.GlobalMatchingVisitor;
import com.intellij.structuralsearch.impl.matcher.PatternTreeContext;
import com.intellij.structuralsearch.impl.matcher.compiler.GlobalCompilingVisitor;
import com.intellij.structuralsearch.impl.matcher.filters.DefaultFilter;
import com.intellij.structuralsearch.impl.matcher.filters.NodeFilter;
import com.intellij.structuralsearch.impl.matcher.handlers.MatchingHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene.Kudelevsky
 */
public class JSStructuralSearchProfile extends TokenBasedProfile {
  private static final String TYPED_VAR_PREFIX = "__$_";

  @Override
  public void compile(PsiElement element, @NotNull GlobalCompilingVisitor globalVisitor) {
    element.accept(new MyCompilingVisitor(globalVisitor) {
      @Override
      public void visitElement(final PsiElement element) {
        super.visitElement(element);
        if (element instanceof JSStatement) {
          CompiledPattern pattern = myGlobalVisitor.getContext().getPattern();
          MatchingHandler handler = pattern.getHandler(element);
          if (handler.getFilter() == null) {
            handler.setFilter(new NodeFilter() {
              public boolean accepts(PsiElement e) {
                if (e instanceof JSBlockStatement) {
                  e = extractOnlyStatement((JSBlockStatement)e);
                }
                return DefaultFilter.accepts(
                  element instanceof JSBlockStatement ? extractOnlyStatement((JSBlockStatement)element) : element, e);
              }
            });
          }
        }
      }
    });
  }

  private static PsiElement extractOnlyStatement(JSBlockStatement e) {
    JSStatement[] statements = e.getStatements();
    if (statements.length == 1) {
      return statements[0];
    }
    return e;
  }

  @NotNull
  @Override
  public PsiElementVisitor createMatchingVisitor(@NotNull GlobalMatchingVisitor globalVisitor) {
    return new MyJsMatchingVisitor(globalVisitor);
  }

  @Override
  protected boolean isLexicalNode(@NotNull PsiElement element) {
    if (super.isLexicalNode(element)) {
      return true;
    }
    if (!(element instanceof LeafElement)) {
      return false;
    }
    IElementType type = ((LeafElement)element).getElementType();
    return type == JSTokenTypes.COMMA || type == JSTokenTypes.SEMICOLON;
  }

  @NotNull
  @Override
  protected String getTypedVarPrefix() {
    return TYPED_VAR_PREFIX;
  }

  @Override
  protected boolean isBlockElement(@NotNull PsiElement element) {
    return element instanceof JSBlockStatement || element instanceof JSFile;
  }

  @Override
  protected boolean canBeVariable(PsiElement element) {
    if (element instanceof JSExpression ||
        element instanceof JSParameter ||
        element instanceof JSVariable) {
      return true;
    }
    return false;
  }

  @NotNull
  @Override
  public LanguageFileType[] getFileTypes() {
    LanguageFileType jsFileType = JavaScriptSupportLoader.JAVASCRIPT;
    if (jsFileType != null) {
      return new LanguageFileType[]{jsFileType};
    }
    return new LanguageFileType[0];
  }

  @Override
  public boolean isMyLanguage(@NotNull Language language) {
    return language instanceof JavascriptLanguage;
  }

  @NotNull
  @Override
  public PsiElement[] createPatternTree(@NotNull String text,
                                        @NotNull PatternTreeContext context,
                                        @NotNull FileType fileType,
                                        @NotNull Project project) {
    return PsiFileFactory.getInstance(project).createFileFromText("__dummy.js", text).getChildren();
  }

  private static class MyJsMatchingVisitor extends JSElementVisitor {
    private final GlobalMatchingVisitor myGlobalVisitor;
    private final MyMatchingVisitor myBaseVisitor;

    private MyJsMatchingVisitor(GlobalMatchingVisitor globalVisitor) {
      myGlobalVisitor = globalVisitor;
      myBaseVisitor = new MyMatchingVisitor(globalVisitor);
    }

    @Override
    public void visitElement(PsiElement element) {
      PsiElement e = myGlobalVisitor.getElement();
      if (e instanceof JSBlockStatement) {
        JSStatement[] statements = ((JSBlockStatement)e).getStatements();
        if (statements.length == 1) {
          myGlobalVisitor.setResult(myGlobalVisitor.match(element, statements[0]));
          return;
        }
      }
      myBaseVisitor.visitElement(element);
    }

    @Override
    public void visitJSFunctionDeclaration(JSFunction f1) {
      final JSFunction f2 = (JSFunction)myGlobalVisitor.getElement();

      myGlobalVisitor.setResult(f1.getKind() == f2.getKind() &&
                                myGlobalVisitor.match(f1.getNameIdentifier(), f2.getNameIdentifier()) &&
                                myGlobalVisitor.matchSons(f1.getParameterList(), f2.getParameterList()) &&
                                myGlobalVisitor.match(f1.getReturnTypeElement(), f2.getReturnTypeElement()) &&
                                myGlobalVisitor.matchOptionally(f1.getBody(), f2.getBody()));
    }

    @Override
    public void visitJSIfStatement(JSIfStatement if1) {
      JSIfStatement if2 = (JSIfStatement)myGlobalVisitor.getElement();

      myGlobalVisitor.setResult(myGlobalVisitor.match(if1.getCondition(), if2.getCondition()) &&
                                myGlobalVisitor.matchOptionally(if1.getThen(), if2.getThen()) &&
                                myGlobalVisitor.matchOptionally(if1.getElse(), if2.getElse()));
    }

    @Override
    public void visitJSForStatement(JSForStatement for1) {
      JSForStatement for2 = (JSForStatement)myGlobalVisitor.getElement();

      myGlobalVisitor.setResult(myGlobalVisitor.match(for1.getVarDeclaration(), for2.getVarDeclaration()) &&
                                myGlobalVisitor.match(for1.getInitialization(), for2.getInitialization()) &&
                                myGlobalVisitor.match(for1.getCondition(), for2.getCondition()) &&
                                myGlobalVisitor.match(for1.getUpdate(), for2.getUpdate()) &&
                                myGlobalVisitor.matchOptionally(for1.getBody(), for2.getBody()));
    }

    @Override
    public void visitJSForInStatement(JSForInStatement for1) {
      JSForInStatement for2 = (JSForInStatement)myGlobalVisitor.getElement();

      myGlobalVisitor.setResult(myGlobalVisitor.match(for1.getDeclarationStatement(), for2.getDeclarationStatement()) &&
                                myGlobalVisitor.match(for1.getVariableExpression(), for2.getVariableExpression()) &&
                                myGlobalVisitor.match(for1.getCollectionExpression(), for1.getCollectionExpression()) &&
                                myGlobalVisitor.matchOptionally(for1.getBody(), for2.getBody()));
    }

    @Override
    public void visitJSDoWhileStatement(JSDoWhileStatement while1) {
      JSDoWhileStatement while2 = (JSDoWhileStatement)myGlobalVisitor.getElement();

      myGlobalVisitor.setResult(myGlobalVisitor.match(while1.getCondition(), while2.getCondition()) &&
                                myGlobalVisitor.matchOptionally(while1.getBody(), while2.getBody()));
    }

    @Override
    public void visitJSWhileStatement(JSWhileStatement while1) {
      JSWhileStatement while2 = (JSWhileStatement)myGlobalVisitor.getElement();

      myGlobalVisitor.setResult(myGlobalVisitor.match(while1.getCondition(), while2.getCondition()) &&
                                myGlobalVisitor.matchOptionally(while1.getBody(), while2.getBody()));
    }

    @Override
    public void visitJSBlock(JSBlockStatement block1) {
      PsiElement element = myGlobalVisitor.getElement();
      PsiElement[] statements2 =
        element instanceof JSBlockStatement ? ((JSBlockStatement)element).getStatements() : new PsiElement[]{element};

      myGlobalVisitor.setResult(myGlobalVisitor.matchSequentially(block1.getStatements(), statements2));
    }
  }
}