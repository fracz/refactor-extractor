package com.intellij.structuralsearch;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.structuralsearch.equivalence.*;
import com.intellij.structuralsearch.impl.matcher.AbstractMatchingVisitor;
import com.intellij.structuralsearch.impl.matcher.CompiledPattern;
import com.intellij.structuralsearch.impl.matcher.GlobalMatchingVisitor;
import com.intellij.structuralsearch.impl.matcher.compiler.GlobalCompilingVisitor;
import com.intellij.structuralsearch.impl.matcher.filters.LexicalNodesFilter;
import com.intellij.structuralsearch.impl.matcher.filters.NodeFilter;
import com.intellij.structuralsearch.impl.matcher.handlers.*;
import com.intellij.structuralsearch.impl.matcher.iterators.FilteringNodeIterator;
import com.intellij.structuralsearch.impl.matcher.strategies.MatchingStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * @author Eugene.Kudelevsky
 */
public class StructuralSearchProfileImpl extends StructuralSearchProfile {
  private static final String TYPED_VAR_PREFIX = "__$_";

  private static final String DELIMETER_CHARS = ",;.[]{}():";
  private PsiElementVisitor myLexicalNodesFilter;

  @Override
  public void compile(PsiElement element, @NotNull final GlobalCompilingVisitor globalVisitor) {
    element.accept(new MyCompilingVisitor(globalVisitor));

    element.accept(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        super.visitElement(element);
        if (isLexicalNode(element)) {
          return;
        }
        CompiledPattern pattern = globalVisitor.getContext().getPattern();
        MatchingHandler handler = pattern.getHandler(element);

        if (!(handler instanceof SubstitutionHandler) &&
            !(handler instanceof TopLevelMatchingHandler) &&
            !(handler instanceof LightTopLevelMatchingHandler)) {
          pattern.setHandler(element, new SkippingHandler(handler));
        }

        // todo: simplify logic

        /*
        place skipping handler under top-level handler, because when we skip top-level node we can get non top-level handler, so
        depth matching won't be done!;
         */
        if (handler instanceof LightTopLevelMatchingHandler) {
          MatchingHandler delegate = ((LightTopLevelMatchingHandler)handler).getDelegate();
          if (!(delegate instanceof SubstitutionHandler)) {
            pattern.setHandler(element, new LightTopLevelMatchingHandler(new SkippingHandler(delegate)));
          }
        }
      }
    });


    final Language baseLanguage = element.getContainingFile().getLanguage();

    // todo: try to optimize it: too heavy strategy!
    globalVisitor.getContext().getPattern().setStrategy(new MatchingStrategy() {
      @Override
      public boolean continueMatching(PsiElement start) {
        Language language = start.getLanguage();

        PsiFile file = start.getContainingFile();
        if (file != null) {
          Language fileLanguage = file.getLanguage();
          if (fileLanguage.isKindOf(language)) {
            // dialect
            language = fileLanguage;
          }
        }

        return language == baseLanguage;
      }

      @Override
      public boolean shouldSkip(PsiElement element, PsiElement elementToMatchWith) {
        return StructuralSearchProfileImpl.shouldSkip(element, elementToMatchWith);
      }
    });
  }

  public static boolean shouldSkip(PsiElement element, PsiElement elementToMatchWith) {
    if (element == null || elementToMatchWith == null) {
      return false;
    }

    if (element.getClass() == elementToMatchWith.getClass()) {
      return false;
    }

    if (element.getFirstChild() == null && element.getTextLength() == 0 && !(element instanceof LeafElement)) {
      return true;
    }

    return false;
  }

  @NotNull
  @Override
  public PsiElementVisitor createMatchingVisitor(@NotNull GlobalMatchingVisitor globalVisitor) {
    return new MyMatchingVisitor(globalVisitor);
  }

  @NotNull
  @Override
  public PsiElementVisitor getLexicalNodesFilter(@NotNull final LexicalNodesFilter filter) {
    if (myLexicalNodesFilter == null) {
      myLexicalNodesFilter = new PsiElementVisitor() {
        @Override
        public void visitElement(PsiElement element) {
          super.visitElement(element);
          if (isLexicalNode(element)) {
            filter.setResult(true);
          }
        }
      };
    }
    return myLexicalNodesFilter;
  }

  public static boolean isLexicalNode(PsiElement element) {
    // ex. "var i = 0" in AS: empty JSAttributeList should be skipped
    /*if (element.getText().length() == 0) {
      return true;
    }*/

    if (element instanceof PsiWhiteSpace || element instanceof PsiErrorElement) {
      return true;
    }

    if (!(element instanceof LeafElement)) {
      return false;
    }

    EquivalenceDescriptorProvider descriptorProvider = EquivalenceDescriptorProvider.getInstance(element);
    if (descriptorProvider == null) {
      return false;
    }

    final IElementType elementType = ((LeafElement)element).getElementType();
    return descriptorProvider.getIgnoredTokens().contains(elementType);
  }

  public static boolean containsOnlyDelimeters(String s) {
    for (int i = 0, n = s.length(); i < n; i++) {
      if (DELIMETER_CHARS.indexOf(s.charAt(i)) < 0) {
        return false;
      }
    }
    return true;
  }

  @NotNull
  @Override
  public CompiledPattern createCompiledPattern() {
    return new CompiledPattern() {
      @Override
      public String getTypedVarPrefix() {
        return TYPED_VAR_PREFIX;
      }

      @Override
      public boolean isTypedVar(String str) {
        return str.startsWith(TYPED_VAR_PREFIX);
      }

      /*@Override
      public String getTypedVarString(PsiElement element) {
        String text = element.getText();

        int start = 0;
        while (start < text.length() && DELIMETER_CHARS.indexOf(text.charAt(start)) >= 0) {
          start++;
        }

        if (start == text.length()) {
          return "";
        }

        int end = text.length() - 1;
        while (end >= 0 && DELIMETER_CHARS.indexOf(text.charAt(end)) >= 0) {
          end--;
        }

        if (end < 0) {
          return "";
        }

        return text.substring(start, end + 1);
      }*/
    };
  }

  @Override
  public boolean canProcess(@NotNull FileType fileType) {
    return fileType instanceof LanguageFileType;
  }

  @Override
  public boolean isMyLanguage(@NotNull Language language) {
    return true;
  }

  private static boolean canBePatternVariable(PsiElement element) {
    // can be leaf element! (ex. var a = 1 <-> var $a$ = 1)
    if (element instanceof LeafElement) {
      return true;
    }

    PsiElement child = SkippingHandler.getOnlyNonWhitespaceChild(element);
    return child != null && child instanceof LeafElement;
  }

  /*@Nullable
  private static PsiElement getOnlyNonLexicalChild(PsiElement element) {
    PsiElement onlyChild = null;
    for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (LexicalNodesFilter.getInstance().accepts(child)) {
        continue;
      }
      if (onlyChild != null) {
        return null;
      }
      onlyChild = child;
    }
    return onlyChild;
  }*/


  private static boolean isLiteral(PsiElement element) {
    if (element == null) return false;
    final ASTNode astNode = element.getNode();
    if (astNode == null) {
      return false;
    }
    final IElementType elementType = astNode.getElementType();
    final ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(element.getLanguage());
    if (parserDefinition != null) {
      final TokenSet literals = parserDefinition.getStringLiteralElements();
      return literals.contains(elementType);
    }
    return false;
  }

  private static boolean canBePatternVariableValue(PsiElement element) {
    // can be leaf element! (ex. var a = 1 <-> var $a$ = 1)
    return !containsOnlyDelimeters(element.getText());
  }

  @Override
  public boolean canBeVarDelimeter(@NotNull PsiElement element) {
    final EquivalenceDescriptorProvider provider = EquivalenceDescriptorProvider.getInstance(element);
    if (provider != null) {
      return provider.canBeVariableDelimeter(element);
    }
    return super.canBeVarDelimeter(element);
  }

  public static boolean match(@NotNull EquivalenceDescriptor descriptor1,
                              @NotNull EquivalenceDescriptor descriptor2,
                              @NotNull AbstractMatchingVisitor g,
                              @NotNull Set<ChildRole> skippedRoles) {

    if (descriptor1.getSingleChildDescriptors().size() != descriptor2.getSingleChildDescriptors().size()) {
      return false;
    }

    if (descriptor1.getMultiChildDescriptors().size() != descriptor2.getMultiChildDescriptors().size()) {
      return false;
    }

    if (descriptor1.getCodeBlocks().size() != descriptor2.getCodeBlocks().size()) {
      return false;
    }

    if (descriptor1.getConstants().size() != descriptor2.getConstants().size()) {
      return false;
    }

    for (int i = 0, n = descriptor1.getConstants().size(); i < n; i++) {
      Object childDescriptor1 = descriptor1.getConstants().get(i);
      Object childDescriptor2 = descriptor2.getConstants().get(i);

      if (!Comparing.equal(childDescriptor1, childDescriptor2)) {
        return false;
      }
    }

    for (int i = 0, n = descriptor1.getSingleChildDescriptors().size(); i < n; i++) {
      SingleChildDescriptor childDescriptor1 = descriptor1.getSingleChildDescriptors().get(i);
      SingleChildDescriptor childDescriptor2 = descriptor2.getSingleChildDescriptors().get(i);

      if (!match(childDescriptor1, childDescriptor2, g, skippedRoles)) {
        return false;
      }
    }

    for (int i = 0, n = descriptor1.getMultiChildDescriptors().size(); i < n; i++) {
      MultiChildDescriptor childDescriptor1 = descriptor1.getMultiChildDescriptors().get(i);
      MultiChildDescriptor childDescriptor2 = descriptor2.getMultiChildDescriptors().get(i);

      if (!match(childDescriptor1, childDescriptor2, g)) {
        return false;
      }
    }

    for (int i = 0, n = descriptor1.getCodeBlocks().size(); i < n; i++) {
      final PsiElement[] codeBlock1 = descriptor1.getCodeBlocks().get(i);
      final PsiElement[] codeBlock2 = descriptor2.getCodeBlocks().get(i);

      if (!g.matchSequentially(codeBlock1, codeBlock2)) {
        return false;
      }
    }

    return true;
  }

  private static boolean match(@NotNull SingleChildDescriptor childDescriptor1,
                               @NotNull SingleChildDescriptor childDescriptor2,
                               @NotNull AbstractMatchingVisitor g,
                               @NotNull Set<ChildRole> skippedRoles) {
    if (childDescriptor1.getType() != childDescriptor2.getType()) {
      return false;
    }

    final PsiElement element1 = childDescriptor1.getElement();
    final PsiElement element2 = childDescriptor2.getElement();

    final ChildRole role1 = childDescriptor1.getRole();
    final ChildRole role2 = childDescriptor2.getRole();

    if (role1 == role2 && skippedRoles.contains(role1)) {
      return true;
    }

    switch (childDescriptor1.getType()) {

      case DEFAULT:
        return g.match(element1, element2);

      case OPTIONALLY_IN_PATTERN:
      case OPTIONALLY:
        return g.matchOptionally(element1, element2);

      case CHILDREN:
        return g.matchSons(element1, element2);

      case CHILDREN_OPTIONALLY_IN_PATTERN:
      case CHILDREN_OPTIONALLY:
        return g.matchSonsOptionally(element1, element2);

      case CHILDREN_IN_ANY_ORDER:
        return g.matchSonsInAnyOrder(element1, element2);

      default:
        return false;
    }
  }

  private static boolean match(@NotNull MultiChildDescriptor childDescriptor1,
                               @NotNull MultiChildDescriptor childDescriptor2,
                               @NotNull AbstractMatchingVisitor g) {

    if (childDescriptor1.getType() != childDescriptor2.getType()) {
      return false;
    }

    final PsiElement[] elements1 = childDescriptor1.getElements();
    final PsiElement[] elements2 = childDescriptor2.getElements();

    switch (childDescriptor1.getType()) {

      case DEFAULT:
        return g.matchSequentially(elements1, elements2);

      case OPTIONALLY_IN_PATTERN:
      case OPTIONALLY:
        return g.matchOptionally(elements1, elements2);

      case IN_ANY_ORDER:
        return g.matchInAnyOrder(elements1, elements2);

      default:
        return false;
    }
  }

  // todo: support expression patterns
  // todo: support {statement;} = statement; (node has only non-lexical child)

  private static class MyCompilingVisitor extends PsiRecursiveElementVisitor {
    private final GlobalCompilingVisitor myGlobalVisitor;

    private MyCompilingVisitor(GlobalCompilingVisitor globalVisitor) {
      myGlobalVisitor = globalVisitor;
    }

    @Override
    public void visitElement(PsiElement element) {
      doVisitElement(element);

      if (isLiteral(element)) {
        visitLiteral(element);
      }
    }

    private void doVisitElement(PsiElement element) {
      CompiledPattern pattern = myGlobalVisitor.getContext().getPattern();

      if (myGlobalVisitor.getCodeBlockLevel() == 0) {
        initTopLevelElement(element);
        return;
      }

      if (canBePatternVariable(element) && pattern.isRealTypedVar(element)) {
        myGlobalVisitor.handle(element);
        final MatchingHandler handler = pattern.getHandler(element);
        handler.setFilter(new NodeFilter() {
          public boolean accepts(PsiElement other) {
            return canBePatternVariableValue(other);
          }
        });

        super.visitElement(element);

        return;
      }

      super.visitElement(element);

      if (myGlobalVisitor.getContext().getSearchHelper().doOptimizing() && element instanceof LeafElement) {
        ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(element.getLanguage());
        if (parserDefinition != null) {
          String text = element.getText();
          IElementType elementType = ((LeafElement)element).getElementType();

          GlobalCompilingVisitor.OccurenceKind kind = null;

          if (parserDefinition.getCommentTokens().contains(elementType)) {
            kind = GlobalCompilingVisitor.OccurenceKind.COMMENT;
          }
          else if (parserDefinition.getStringLiteralElements().contains(elementType)) {
            kind = GlobalCompilingVisitor.OccurenceKind.LITERAL;
          }
          else if (StringUtil.isJavaIdentifier(text)) {
            kind = GlobalCompilingVisitor.OccurenceKind.CODE;
          }

          if (kind != null) {
            // todo: support variables inside comments and literals (use handler returned by method)
            myGlobalVisitor.processPatternStringWithFragments(text, kind);
          }
        }
      }
    }

    private void visitLiteral(PsiElement literal) {
      String value = literal.getText();

      if (value.length() > 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
        @Nullable MatchingHandler handler =
          myGlobalVisitor.processPatternStringWithFragments(value, GlobalCompilingVisitor.OccurenceKind.LITERAL);

        if (handler != null) {
          literal.putUserData(CompiledPattern.HANDLER_KEY, handler);
        }
      }
    }

    private void initTopLevelElement(PsiElement element) {
      CompiledPattern pattern = myGlobalVisitor.getContext().getPattern();

      PsiElement newElement = SkippingHandler.skipNodeIfNeccessary(element);

      if (element != newElement && newElement != null) {
        // way to support partial matching (ex. if ($condition$) )
        newElement.accept(this);
        pattern.setHandler(element, new LightTopLevelMatchingHandler(pattern.getHandler(element)));
      }
      else {
        myGlobalVisitor.setCodeBlockLevel(myGlobalVisitor.getCodeBlockLevel() + 1);

        for (PsiElement el = element.getFirstChild(); el != null; el = el.getNextSibling()) {
          if (GlobalCompilingVisitor.getFilter().accepts(el)) {
            if (el instanceof PsiWhiteSpace) {
              myGlobalVisitor.addLexicalNode(el);
            }
          }
          else {
            el.accept(this);

            MatchingHandler matchingHandler = pattern.getHandler(el);
            pattern.setHandler(el, element instanceof PsiFile ? new TopLevelMatchingHandler(matchingHandler) :
                                   new LightTopLevelMatchingHandler(matchingHandler));

            /*
              do not assign light-top-level handlers through skipping, because it is incorrect;
              src: if (...) { st1; st2; }
              pattern: if (...) {$a$;}

              $a$ will have top-level handler, so matching will be considered as correct, although "st2;" is left!
             */
          }
        }

        myGlobalVisitor.setCodeBlockLevel(myGlobalVisitor.getCodeBlockLevel() - 1);
        pattern.setHandler(element, new TopLevelMatchingHandler(pattern.getHandler(element)));
      }
    }
  }

  private static class MyMatchingVisitor extends PsiElementVisitor {
    private final GlobalMatchingVisitor myGlobalVisitor;

    private MyMatchingVisitor(GlobalMatchingVisitor globalVisitor) {
      myGlobalVisitor = globalVisitor;
    }

    @Override
    public void visitElement(PsiElement element) {
      super.visitElement(element);

      final EquivalenceDescriptorProvider descriptorProvider = EquivalenceDescriptorProvider.getInstance(element);

      if (descriptorProvider != null) {
        final EquivalenceDescriptor descriptor1 = descriptorProvider.buildDescriptor(element);
        final EquivalenceDescriptor descriptor2 = descriptorProvider.buildDescriptor(myGlobalVisitor.getElement());

        if (descriptor1 != null && descriptor2 != null) {
          final boolean result = match(descriptor1, descriptor2, myGlobalVisitor, Collections.<ChildRole>emptySet());
          myGlobalVisitor.setResult(result);
          return;
        }
      }

      if (isLiteral(element)) {
        visitLiteral(element);
        return;
      }

      if (canBePatternVariable(element) && myGlobalVisitor.getMatchContext().getPattern().isRealTypedVar(element)) {
        PsiElement matchedElement = myGlobalVisitor.getElement();
        PsiElement newElement = SkippingHandler.skipNodeIfNeccessary(matchedElement);
        while (newElement != matchedElement) {
          matchedElement = newElement;
          newElement = SkippingHandler.skipNodeIfNeccessary(matchedElement);
        }

        myGlobalVisitor.setResult(myGlobalVisitor.handleTypedElement(element, matchedElement));
      }
      else if (element instanceof LeafElement) {
        myGlobalVisitor.setResult(element.getText().equals(myGlobalVisitor.getElement().getText()));
      }
      else if (element.getFirstChild() == null && element.getTextLength() == 0) {
        myGlobalVisitor.setResult(true);
      }
      else {
        PsiElement patternChild = element.getFirstChild();
        PsiElement matchedChild = myGlobalVisitor.getElement().getFirstChild();

        FilteringNodeIterator patternIterator = new FilteringNodeIterator(patternChild);
        FilteringNodeIterator matchedIterator = new FilteringNodeIterator(matchedChild);

        boolean matched = myGlobalVisitor.matchSequentially(patternIterator, matchedIterator);
        myGlobalVisitor.setResult(matched);
      }
    }

    public void visitLiteral(PsiElement literal) {
      final PsiElement l2 = myGlobalVisitor.getElement();

      MatchingHandler handler = (MatchingHandler)literal.getUserData(CompiledPattern.HANDLER_KEY);

      if (handler instanceof SubstitutionHandler) {
        int offset = 0;
        int length = l2.getTextLength();
        final String text = l2.getText();

        if (length > 2 && text.charAt(0) == '"' && text.charAt(length - 1) == '"') {
          length--;
          offset++;
        }
        myGlobalVisitor.setResult(((SubstitutionHandler)handler).handle(l2, offset, length, myGlobalVisitor.getMatchContext()));
      }
      else if (handler != null) {
        myGlobalVisitor.setResult(handler.match(literal, l2, myGlobalVisitor.getMatchContext()));
      }
      else {
        myGlobalVisitor.setResult(literal.textMatches(l2));
      }
    }
  }
}