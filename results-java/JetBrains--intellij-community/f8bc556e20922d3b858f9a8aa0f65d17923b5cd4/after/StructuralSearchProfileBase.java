package com.intellij.structuralsearch;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.structuralsearch.duplicates.PsiElementRole;
import com.intellij.structuralsearch.duplicates.SSRDuplicatesProfile;
import com.intellij.structuralsearch.equivalence.*;
import com.intellij.structuralsearch.impl.matcher.AbstractMatchingVisitor;
import com.intellij.structuralsearch.impl.matcher.CompiledPattern;
import com.intellij.structuralsearch.impl.matcher.GlobalMatchingVisitor;
import com.intellij.structuralsearch.impl.matcher.PatternTreeContext;
import com.intellij.structuralsearch.impl.matcher.compiler.GlobalCompilingVisitor;
import com.intellij.structuralsearch.impl.matcher.compiler.PatternCompiler;
import com.intellij.structuralsearch.impl.matcher.filters.LexicalNodesFilter;
import com.intellij.structuralsearch.impl.matcher.filters.NodeFilter;
import com.intellij.structuralsearch.impl.matcher.handlers.*;
import com.intellij.structuralsearch.impl.matcher.iterators.FilteringNodeIterator;
import com.intellij.structuralsearch.impl.matcher.iterators.NodeIterator;
import com.intellij.structuralsearch.impl.matcher.strategies.MatchingStrategy;
import com.intellij.structuralsearch.plugin.replace.ReplaceOptions;
import com.intellij.structuralsearch.plugin.replace.impl.ReplacementContext;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Eugene.Kudelevsky
 */
public abstract class StructuralSearchProfileBase extends StructuralSearchProfile {
  private static final String DELIMETER_CHARS = ",;.[]{}():";
  protected static final String PATTERN_PLACEHOLDER = "$$PATTERN_PLACEHOLDER$$";
  private PsiElementVisitor myLexicalNodesFilter;

  @Override
  public void compile(PsiElement[] elements, @NotNull final GlobalCompilingVisitor globalVisitor) {
    final PsiElement topElement = elements[0].getParent();
    final PsiElement element = elements.length > 1 ? topElement : elements[0];

    element.accept(new MyCompilingVisitor(globalVisitor, topElement));

    element.accept(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        super.visitElement(element);
        if (isIgnoredNode(element)) {
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
        return StructuralSearchProfileBase.shouldSkip(element, elementToMatchWith);
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
          if (isIgnoredNode(element)) {
            filter.setResult(true);
          }
        }
      };
    }
    return myLexicalNodesFilter;
  }

  public static boolean isIgnoredNode(PsiElement element) {
    // ex. "var i = 0" in AS: empty JSAttributeList should be skipped
    /*if (element.getText().length() == 0) {
      return true;
    }*/

    if (element instanceof PsiWhiteSpace || element instanceof PsiErrorElement || element instanceof PsiComment) {
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
  protected abstract String[] getVarPrefixes();

  @NotNull
  @Override
  public CompiledPattern createCompiledPattern() {
    return new CompiledPattern() {
      @Override
      public String[] getTypedVarPrefixes() {
        return getVarPrefixes();
      }

      @Override
      public boolean isTypedVar(String str) {
        for (String prefix : getVarPrefixes()) {
          if (str.startsWith(prefix)) {
            return true;
          }
        }
        return false;
      }

      @Override
      public String getTypedVarString(PsiElement element) {
        final PsiElement initialElement = element;
        PsiElement child = SkippingHandler.getOnlyNonWhitespaceChild(element);

        while (child != element && child != null && !(child instanceof LeafElement)) {
          element = child;
          child = SkippingHandler.getOnlyNonWhitespaceChild(element);
        }
        return child instanceof LeafElement ? element.getText() : initialElement.getText();
      }
    };
  }

  @Override
  public boolean canProcess(@NotNull FileType fileType) {
    return fileType instanceof LanguageFileType &&
           isMyLanguage(((LanguageFileType)fileType).getLanguage());
  }

  @Override
  public boolean isMyLanguage(@NotNull Language language) {
    return language.isKindOf(getFileType().getLanguage());
  }

  @NotNull
  protected abstract LanguageFileType getFileType();

  @NotNull
  @Override
  public PsiElement[] createPatternTree(@NotNull String text,
                                        @NotNull PatternTreeContext context,
                                        @NotNull FileType fileType,
                                        @Nullable Language language,
                                        @Nullable String extension,
                                        @NotNull Project project,
                                        boolean physical) {
    if (context == PatternTreeContext.Block) {
      final String strContext = getContext(text);
      return strContext != null ?
             parsePattern(project, strContext, text, fileType, language, extension, physical) :
             PsiElement.EMPTY_ARRAY;
    }
    return super.createPatternTree(text, context, fileType, language, extension, project, physical);
  }

  @Override
  public void checkReplacementPattern(Project project, ReplaceOptions options) {
    final CompiledPattern compiledPattern = PatternCompiler.compilePattern(project, options.getMatchOptions());
    if (compiledPattern == null) {
      return;
    }

    final NodeIterator it = compiledPattern.getNodes();
    if (!it.hasNext()) {
      return;
    }

    final PsiElement root = it.current().getParent();

    if (!checkOptionalChildren(root) ||
        !checkErrorElements(root)) {
      throw new UnsupportedPatternException(": Partial and expression patterns are not supported");
    }
  }

  private boolean checkErrorElements(PsiElement element) {
    final boolean[] result = {true};
    final int endOffset = element.getTextRange().getEndOffset();

    element.accept(new PsiRecursiveElementWalkingVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        super.visitElement(element);

        if (element instanceof PsiErrorElement && element.getTextRange().getEndOffset() == endOffset) {
          result[0] = false;
        }
      }
    });

    return result[0];
  }

  private boolean checkOptionalChildren(PsiElement root) {
    final boolean[] result = {true};

    root.accept(new PsiRecursiveElementWalkingVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        super.visitElement(element);

        if (element instanceof LeafElement) {
          return;
        }

        final EquivalenceDescriptorProvider provider = EquivalenceDescriptorProvider.getInstance(element);
        if (provider == null) {
          return;
        }

        final EquivalenceDescriptor descriptor = provider.buildDescriptor(element);
        if (descriptor == null) {
          return;
        }

        for (SingleChildDescriptor childDescriptor : descriptor.getSingleChildDescriptors()) {
          if (childDescriptor.getType() == SingleChildDescriptor.MyType.OPTIONALLY_IN_PATTERN &&
              childDescriptor.getElement() == null) {
            result[0] = false;
          }
        }

        for (MultiChildDescriptor childDescriptor : descriptor.getMultiChildDescriptors()) {
          if (childDescriptor.getType() == MultiChildDescriptor.MyType.OPTIONALLY_IN_PATTERN) {
            PsiElement[] elements = childDescriptor.getElements();
            if (elements == null || elements.length == 0) {
              result[0] = false;
            }
          }
        }
      }
    });
    return result[0];
  }

  @Override
  public StructuralReplaceHandler getReplaceHandler(@NotNull ReplacementContext context) {
    return new DocumentBasedReplaceHandler(context.getProject());
  }

  @Nullable
  protected String getContext(@NotNull String pattern) {
    return PATTERN_PLACEHOLDER;
  }

  private static boolean canBePatternVariable(PsiElement element) {
    // can be leaf element! (ex. var a = 1 <-> var $a$ = 1)
    if (element instanceof LeafElement) {
      return true;
    }

    while (!(element instanceof LeafElement) && element != null) {
      element = SkippingHandler.getOnlyNonWhitespaceChild(element);
    }
    return element != null;

    /*PsiElement child = SkippingHandler.getOnlyNonWhitespaceChild(element);
    return child != null && child instanceof LeafElement;*/
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
    final ASTNode node = element.getNode();
    return node != null && getVariableDelimiters().contains(node.getElementType());
  }

  protected TokenSet getVariableDelimiters() {
    return TokenSet.EMPTY;
  }

  public static boolean match(@NotNull EquivalenceDescriptor descriptor1,
                              @NotNull EquivalenceDescriptor descriptor2,
                              @NotNull AbstractMatchingVisitor g,
                              @NotNull Set<PsiElementRole> skippedRoles,
                              @Nullable SSRDuplicatesProfile profile) {

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

      if (!match(childDescriptor1, childDescriptor2, g, skippedRoles, profile)) {
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
                               @NotNull Set<PsiElementRole> skippedRoles,
                               @Nullable SSRDuplicatesProfile duplicatesProfile) {
    if (childDescriptor1.getType() != childDescriptor2.getType()) {
      return false;
    }

    final PsiElement element1 = childDescriptor1.getElement();
    final PsiElement element2 = childDescriptor2.getElement();

    if (duplicatesProfile != null) {
      final PsiElementRole role1 = element1 != null ? duplicatesProfile.getRole(element1) : null;
      final PsiElementRole role2 = element2 != null ? duplicatesProfile.getRole(element2) : null;

      if (role1 == role2 && skippedRoles.contains(role1)) {
        return true;
      }
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

  public static PsiElement[] parsePattern(Project project,
                                          String context,
                                          String pattern,
                                          FileType fileType,
                                          Language language,
                                          String extension,
                                          boolean physical) {
    int offset = context.indexOf(PATTERN_PLACEHOLDER);

    final int patternLength = pattern.length();
    final String patternInContext = context.replace(PATTERN_PLACEHOLDER, pattern);

    final String ext = extension != null ? extension : fileType.getDefaultExtension();
    final String name = "__dummy." + ext;
    final PsiFileFactory factory = PsiFileFactory.getInstance(project);

    final PsiFile file = language == null
                         ? factory.createFileFromText(name, fileType, patternInContext, LocalTimeCounter.currentTime(), physical, true)
                         : factory.createFileFromText(name, language, patternInContext, physical, true);
    if (file == null) {
      return PsiElement.EMPTY_ARRAY;
    }

    final List<PsiElement> result = new ArrayList<PsiElement>();

    PsiElement element = file.findElementAt(offset);
    if (element == null) {
      return PsiElement.EMPTY_ARRAY;
    }

    PsiElement topElement = element;
    element = element.getParent();

    while (element != null) {
      if (element.getTextRange().getStartOffset() == offset && element.getTextLength() <= patternLength) {
        topElement = element;
      }
      element = element.getParent();
    }

    if (topElement instanceof PsiFile) {
      return topElement.getChildren();
    }

    final int endOffset = offset + patternLength;
    result.add(topElement);
    topElement = topElement.getNextSibling();

    while (topElement != null && topElement.getTextRange().getEndOffset() <= endOffset) {
      result.add(topElement);
      topElement = topElement.getNextSibling();
    }

    return result.toArray(new PsiElement[result.size()]);
  }

  // todo: support expression patterns
  // todo: support {statement;} = statement; (node has only non-lexical child)

  private static class MyCompilingVisitor extends PsiRecursiveElementVisitor {
    private final GlobalCompilingVisitor myGlobalVisitor;
    private final PsiElement myTopElement;

    private Pattern[] mySubstitutionPatterns;

    private MyCompilingVisitor(GlobalCompilingVisitor globalVisitor, PsiElement topElement) {
      myGlobalVisitor = globalVisitor;
      myTopElement = topElement;
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

        if (mySubstitutionPatterns == null) {
          final String[] prefixes = myGlobalVisitor.getContext().getPattern().getTypedVarPrefixes();
          mySubstitutionPatterns = createPatterns(prefixes);
        }

        for (Pattern substitutionPattern : mySubstitutionPatterns) {
          @Nullable MatchingHandler handler =
            myGlobalVisitor.processPatternStringWithFragments(value, GlobalCompilingVisitor.OccurenceKind.LITERAL, substitutionPattern);

          if (handler != null) {
            literal.putUserData(CompiledPattern.HANDLER_KEY, handler);
            break;
          }
        }
      }
    }

    private static Pattern[] createPatterns(String[] prefixes) {
      final Pattern[] patterns = new Pattern[prefixes.length];

      for (int i = 0; i < prefixes.length; i++) {
        final String s = StructuralSearchUtil.shieldSpecialChars(prefixes[0]);
        patterns[i] = Pattern.compile("\\b(" + s + "\\w+)\\b");
      }
      return patterns;
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
            pattern.setHandler(el, element == myTopElement ? new TopLevelMatchingHandler(matchingHandler) :
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
          final boolean result = match(descriptor1, descriptor2, myGlobalVisitor, Collections.<PsiElementRole>emptySet(), null);
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