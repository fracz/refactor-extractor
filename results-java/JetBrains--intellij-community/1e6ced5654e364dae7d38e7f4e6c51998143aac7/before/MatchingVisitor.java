package com.intellij.structuralsearch.impl.matcher;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.structuralsearch.MatchOptions;
import com.intellij.structuralsearch.MatchResult;
import com.intellij.structuralsearch.impl.matcher.filters.LexicalNodesFilter;
import com.intellij.structuralsearch.impl.matcher.handlers.MatchPredicate;
import com.intellij.structuralsearch.impl.matcher.handlers.MatchingHandler;
import com.intellij.structuralsearch.impl.matcher.handlers.SubstitutionHandler;
import com.intellij.structuralsearch.impl.matcher.iterators.*;
import com.intellij.structuralsearch.impl.matcher.predicates.ExprTypePredicate;
import com.intellij.structuralsearch.impl.matcher.predicates.NotPredicate;
import com.intellij.structuralsearch.impl.matcher.predicates.RegExpPredicate;
import com.intellij.structuralsearch.plugin.util.SmartPsiPointer;
import com.intellij.structuralsearch.plugin.ui.Configuration;

import java.util.*;

/**
 * Visitor class to manage pattern matching
 */
@SuppressWarnings({"RefusedBequest"})
public class MatchingVisitor {

  // the pattern element for visitor check
  protected PsiElement element;
  // the result of matching in visitor
  protected boolean result;

  // context of matching
  private MatchContext matchContext;

  private final PsiElementVisitor myXmlVisitor = new MyXmlVisitor();
  private final PsiElementVisitor myJavaVisitor = new MyJavaVisitor();

  public static final String[] MODIFIERS = {
    PsiModifier.PUBLIC, PsiModifier.PROTECTED, PsiModifier.PRIVATE, PsiModifier.STATIC, PsiModifier.ABSTRACT, PsiModifier.FINAL,
    PsiModifier.NATIVE, PsiModifier.SYNCHRONIZED, PsiModifier.STRICTFP, PsiModifier.TRANSIENT, PsiModifier.VOLATILE
  };
  static { Arrays.sort(MODIFIERS); }

  private static boolean isNotInstanceModifier(final PsiModifierList list2) {
    return list2.hasModifierProperty("static") ||
        list2.hasModifierProperty("abstract");
  }

  private final boolean matchSonsOptionally(final PsiElement element,final PsiElement element2) {

    return (element == null && matchContext.getOptions().isLooseMatching()) ||
           matchSons(element,element2);
  }

  protected boolean matchInAnyOrder(final PsiReferenceList elements,final PsiReferenceList elements2) {
    if ( ( elements == null && matchContext.getOptions().isLooseMatching() ) ||
         elements == elements2 // null
       ) {
      return true;
    }

    return matchInAnyOrder(
      elements.getReferenceElements(),
      (elements2!=null)?elements2.getReferenceElements():PsiElement.EMPTY_ARRAY
    );
  }

  protected final boolean matchInAnyOrder(final PsiElement[] elements,final PsiElement[] elements2) {
    if (elements==elements2) return true;

    return matchInAnyOrder(
      new ArrayBackedNodeIterator(elements),
      new ArrayBackedNodeIterator(elements2)
    );
  }

  protected final boolean matchInAnyOrder(final NodeIterator elements,final NodeIterator elements2) {
    if ( ( !elements.hasNext() && matchContext.getOptions().isLooseMatching() ) ||
         (!elements.hasNext() && !elements2.hasNext())
       ) {
      return true;
    }

    return matchContext.getPattern().getHandler(elements.current()).matchInAnyOrder(
      elements,
      elements2,
      matchContext
    );
  }

  private final boolean compareClasses(final PsiClass clazz,final PsiClass clazz2) {
    PsiClass saveClazz = this.clazz;
    MatchContext.UnmatchedElementsListener listener = matchContext.getUnmatchedElementsListener();

    this.clazz = clazz2;
    final PsiElement allRemainingClassContentElement = clazz.getUserData(CompiledPattern.ALL_CLASS_CONTENT_VAR_KEY);
    MatchContext.UnmatchedElementsListener mylistener = null;
    boolean result = false;

    if (allRemainingClassContentElement!=null) {
      matchContext.setUnmatchedElementsListener(
        mylistener = new MatchContext.UnmatchedElementsListener() {
          private List<PsiElement> myUnmatchedList;

          public void matchedElements(List<PsiElement> elementList) {
            if (elementList!=null) {
              if (myUnmatchedList ==null) myUnmatchedList = new LinkedList<PsiElement>(elementList);
              else myUnmatchedList.addAll(elementList);
            }
          }

          public void commitUnmatched() {
            final SubstitutionHandler handler = (SubstitutionHandler) matchContext.getPattern().getHandler(allRemainingClassContentElement);

            for(PsiElement el = clazz2.getFirstChild();el!=null;el = el.getNextSibling()) {
              if (el instanceof PsiMember && (myUnmatchedList ==null || myUnmatchedList.indexOf(el)==-1)) {
                handler.handle(el,matchContext);
              }
            }
          }
        }
      );
    }

    try {
      final boolean templateIsInterface = clazz.isInterface();
      if (templateIsInterface !=clazz2.isInterface()) return false;
      if (templateIsInterface && clazz.isAnnotationType() && !clazz2.isAnnotationType()) return false;
      final boolean templateIsEnum = clazz.isEnum();
      if (templateIsEnum && !clazz2.isEnum()) return false;

      if (!matchInAnyOrder(clazz.getExtendsList(),clazz2.getExtendsList())) {
        return false;
      }

      // check if implements is in extended classes implements
      final PsiReferenceList implementsList = clazz.getImplementsList();
      if (implementsList != null) {
        if (!matchInAnyOrder(implementsList,clazz2.getImplementsList())) {
          final PsiReferenceList anotherExtendsList = clazz2.getExtendsList();
          final PsiJavaCodeReferenceElement[] referenceElements = implementsList.getReferenceElements();

          boolean accepted = false;

          if (referenceElements.length > 0 && anotherExtendsList != null) {
            final HierarchyNodeIterator iterator = new HierarchyNodeIterator(clazz2, true, true, false);

            accepted = matchInAnyOrder(new ArrayBackedNodeIterator(referenceElements),iterator);
          }

          if (!accepted) return false;
        }
      }

      final PsiField[] fields  = clazz.getFields();

      if (fields.length > 0) {
        final PsiField[] fields2;
        fields2 = (matchContext.getPattern()).isRequestsSuperFields()?
                  clazz2.getAllFields():
                  clazz2.getFields();

        if (!matchInAnyOrder(fields,fields2)) {
          return false;
        }
      }

      final PsiMethod[] methods  = clazz.getMethods();

      if (methods.length > 0) {
        final PsiMethod[] methods2;
        methods2 = (matchContext.getPattern()).isRequestsSuperMethods()?
                   clazz2.getAllMethods():
                   clazz2.getMethods();

        if (!matchInAnyOrder(methods,methods2)) {
          return false;
        }
      }

      final PsiClass[] nestedClasses = clazz.getInnerClasses();

      if (nestedClasses.length > 0) {
        final PsiClass[] nestedClasses2 = (matchContext.getPattern()).isRequestsSuperInners()?
                                          clazz2.getAllInnerClasses():
                                          clazz2.getInnerClasses();

        if (!matchInAnyOrder(nestedClasses,nestedClasses2)) {
          return false;
        }
      }

      final PsiClassInitializer[] initializers = clazz.getInitializers();
      if (initializers.length > 0) {
        final PsiClassInitializer[] initializers2 = clazz2.getInitializers();

        if (!matchInAnyOrder(initializers,initializers2)) {
          return false;
        }
      }

      result = true;
      return result;
    } finally {
      if (result && mylistener!=null) mylistener.commitUnmatched();
      this.clazz = saveClazz;
      matchContext.setUnmatchedElementsListener(listener);
    }
  }

  private boolean checkHierarchy(PsiElement element,PsiElement patternElement) {
    final MatchingHandler handler = matchContext.getPattern().getHandler(patternElement);
    if (handler instanceof SubstitutionHandler) {
      final SubstitutionHandler handler2 = (SubstitutionHandler)handler;

      if (!handler2.isSubtype()) {
        if (handler2.isStrictSubtype()) {
          // check if element is declared not in current class  (in ancestors)
          return (element.getParent()!=clazz);
        }
      } else {
        return true;
      }
    }

    // check if element is declared in current class (not in ancestors)
    return (element.getParent()==clazz);
  }


  private PsiClass clazz;

  private final boolean compareBody(final PsiElement el1,final PsiElement el2) {
    PsiElement compareElemement1 = el1;
    PsiElement compareElemement2 = el2;

    if (matchContext.getOptions().isLooseMatching()) {
      if (el1 instanceof PsiBlockStatement ) {
        compareElemement1 = ((PsiBlockStatement)el1).getCodeBlock().getFirstChild();
      }

      if (el2 instanceof PsiBlockStatement ) {
        compareElemement2 = ((PsiBlockStatement)el2).getCodeBlock().getFirstChild();
      }
    }

    return matchSequentially( compareElemement1, compareElemement2 );
  }

  protected final boolean handleTypedElement(final PsiElement typedElement, final PsiElement match) {
    final SubstitutionHandler handler = (SubstitutionHandler) matchContext.getPattern().getHandler(typedElement);
    return handler.handle(match,matchContext);
  }

  /**
   * Identifies the match between given element of program tree and pattern element
   * @param el1 the pattern for matching
   * @param el2 the tree element for matching
   * @return true if equal and false otherwise
   */
  public boolean match(final PsiElement el1,final PsiElement el2) {
    // null
    if (el1==el2) return true;
    if (el2==null || el1==null) {
      // this a bug!
      return false;
    }

    // copy changed data to local stack
    PsiElement prevElement = element;
    element = el2;

    try {
      if (el1 instanceof XmlElement) {
        el1.accept(myXmlVisitor);
      }
      else {
        el1.accept(myJavaVisitor);
      }
    } catch(ClassCastException ex) {
      result = false;
    } finally {
      element = prevElement;
    }

    return result;
  }

  // Matches the sons of given elements to find equality
  // @param el1 the pattern element for matching
  // @param el2 the tree element for matching
  // @return if they are equal and false otherwise
  protected boolean matchSons(final PsiElement el1,final PsiElement el2) {
    if (el1==null || el2==null) return el1 == el2;
    return matchSequentially(el1.getFirstChild(),el2.getFirstChild());
  }

  public boolean shouldAdvanceThePattern(final PsiElement element, PsiElement match) {
    MatchingHandler handler = matchContext.getPattern().getHandler(element);

    return handler.shouldAdvanceThePatternFor(element, match);
  }

  private boolean allowsAbsenceOfMatch(final PsiElement element) {
    MatchingHandler handler = matchContext.getPattern().getHandler(element);

    if (handler instanceof SubstitutionHandler &&
        ((SubstitutionHandler)handler).getMinOccurs() == 0) {
      return true;
    }
    return false;
  }

  // Matches tree segments starting with given elements to find equality
  // @param el1 the pattern element for matching
  // @param el2 the tree element for matching
  // @return if they are equal and false otherwise
  private boolean matchSequentially(NodeIterator nodes,NodeIterator nodes2) {
    return continueMatchingSequentially(nodes, nodes2,matchContext);
  }

  public static boolean continueMatchingSequentially(final NodeIterator nodes, final NodeIterator nodes2, MatchContext matchContext) {
    if (!nodes.hasNext()) {
      return nodes.hasNext() == nodes2.hasNext();
    }

    return matchContext.getPattern().getHandler(nodes.current()).matchSequentially(
      nodes,
      nodes2,
      matchContext
    );
  }

  // Matches tree segments starting with given elements to find equality
  // @param el1 the pattern element for matching
  // @param el2 the tree element for matching
  // @return if they are equal and false otherwise
  protected boolean matchSequentially(PsiElement el1,PsiElement el2) {
    //if (el1==null || el2==null) return el1 == el2;
    return matchSequentially( new FilteringNodeIterator(el1), new FilteringNodeIterator(el2) );
  }

  /**
   * Descents the tree in depth finding matches
   * @param elements the element for which the sons are looked for match
   */
  public void matchContext(final NodeIterator elements) {
    final CompiledPattern pattern = matchContext.getPattern();
    final NodeIterator patternNodes = pattern.getNodes().clone();
    final MatchResultImpl saveResult = matchContext.hasResult() ? matchContext.getResult():null;
    final LinkedList<PsiElement> saveMatchedNodes = matchContext.getMatchedNodes();

    try {
      matchContext.setResult(null);
      matchContext.setMatchedNodes(null);

      if (!patternNodes.hasNext()) return;
      final MatchingHandler firstMatchingHandler = pattern.getHandler(patternNodes.current());

      for(;elements.hasNext();elements.advance()) {
        final PsiElement elementNode = elements.current();

        boolean matched =
          firstMatchingHandler.matchSequentially(patternNodes, elements, matchContext);

        if (matched) {
          MatchingHandler matchingHandler = matchContext.getPattern().getHandler(Configuration.CONTEXT_VAR_NAME);
          if (matchingHandler != null) {
            matched = ((SubstitutionHandler)matchingHandler).handle(elementNode, matchContext);
          }
        }

        final LinkedList<PsiElement> matchedNodes = matchContext.getMatchedNodes();

        if (matched) {
          dispatchMatched(matchedNodes, matchContext.getResult());
        }

        matchContext.setMatchedNodes(null);
        matchContext.setResult(null);

        patternNodes.reset();
        if (matchedNodes != null && matchedNodes.size() > 0 && matched) {
          elements.rewind();
        }
      }
    } finally {
      matchContext.setResult(saveResult);
      matchContext.setMatchedNodes(saveMatchedNodes);
    }
  }

  private void dispatchMatched(final List<PsiElement> matchedNodes, MatchResultImpl result) {
    if(!matchContext.getOptions().isResultIsContextMatch() && doDispatch(result, result)) return;

    // There is no substitutions so show the context

    processNoSubstitutionMatch(matchedNodes, result);
    matchContext.getSink().newMatch(result);
  }

  private boolean doDispatch(final MatchResultImpl result, MatchResultImpl context) {
    boolean ret = false;

    for(MatchResult _r:result.getAllSons()) {
      final MatchResultImpl r = (MatchResultImpl)_r;

      if ((r.isScopeMatch() && !r.isTarget()) || r.isMultipleMatch()) {
        ret |= doDispatch(r, context);
      } else if (r.isTarget()) {
        r.setContext(context);
        matchContext.getSink().newMatch(r);
        ret = true;
      }
    }
    return ret;
  }

  private static void processNoSubstitutionMatch(List<PsiElement> matchedNodes, MatchResultImpl result) {
    boolean complexMatch = matchedNodes.size() > 1;
    final PsiElement match = matchedNodes.get(0);

    if (!complexMatch) {
      result.setMatchRef(new SmartPsiPointer(match));
      result.setMatchImage(match.getText());
    } else {
      MatchResultImpl sonresult;

      for (final PsiElement matchStatement : matchedNodes) {
        result.getMatches().add(
          sonresult = new MatchResultImpl(
            MatchResult.LINE_MATCH,
            matchStatement.getText(),
            new SmartPsiPointer(matchStatement),
            true
          )
        );

        sonresult.setParent(result);
      }

      result.setMatchRef(
        new SmartPsiPointer(match)
      );
      result.setMatchImage(
        match.getText()
      );
      result.setName(MatchResult.MULTI_LINE_MATCH);
    }
  }

  void setMatchContext(MatchContext matchContext) {
    this.matchContext = matchContext;
  }

  protected boolean matchType(final PsiElement _type, final PsiElement _type2) {
    boolean result;
    PsiElement el = _type;
    PsiElement el2 = _type2;
    PsiType type1 = null;
    PsiType type2 = null;

    PsiElement[] typeparams = null;
    PsiReferenceParameterList list = null;

    // check for generics
    if (_type instanceof PsiTypeElement &&
        ((PsiTypeElement)_type).getInnermostComponentReferenceElement()!=null
       ) {
      el = ((PsiTypeElement)_type).getInnermostComponentReferenceElement();
      type1 = ((PsiTypeElement)_type).getType();
    }

    if (_type2 instanceof PsiTypeElement &&
          ((PsiTypeElement)_type2).getInnermostComponentReferenceElement()!=null
         ) {
      el2 = ((PsiTypeElement)_type2).getInnermostComponentReferenceElement();
      type2 = ((PsiTypeElement)_type2).getType();
    }

    if (el2 instanceof PsiJavaCodeReferenceElement) {
      typeparams = ((PsiJavaCodeReferenceElement)el2).getParameterList().getTypeParameterElements();
      if (typeparams.length > 0) {
        el2 = ((PsiJavaCodeReferenceElement)el2).getReferenceNameElement();
      }
    }
    else if (el2 instanceof PsiTypeParameter) {
      el2 = ((PsiTypeParameter)el2).getNameIdentifier();
    } else if (el2 instanceof PsiClass && ((PsiClass)el2).hasTypeParameters()
            ) {
      typeparams = ((PsiClass)el2).getTypeParameters();
      el2 = ((PsiClass)el2).getNameIdentifier();
    } else if (el2 instanceof PsiMethod && ((PsiMethod)el2).hasTypeParameters()
            ) {
      typeparams = ((PsiMethod)_type2).getTypeParameters();
      el2 = ((PsiMethod)_type2).getNameIdentifier();
    }

    if (el instanceof PsiJavaCodeReferenceElement) {
      list = ((PsiJavaCodeReferenceElement)el).getParameterList();
    }

    if (list!=null && list.getTypeParameterElements().length>0) {
        result = typeparams!=null &&
                 matchInAnyOrder(
                   list.getTypeParameterElements(),
                   typeparams
                 );

        if (!result) return false;
        el = ((PsiJavaCodeReferenceElement)el).getReferenceNameElement();
      } else {
        if (_type2 instanceof PsiTypeElement) {
          type2 = ((PsiTypeElement)_type2).getType();

          if (typeparams == null || typeparams.length == 0) {
            final PsiJavaCodeReferenceElement innermostComponentReferenceElement = ((PsiTypeElement)_type2).getInnermostComponentReferenceElement();
            if (innermostComponentReferenceElement != null) el2 = innermostComponentReferenceElement;
          } else {
            el2 = _type2;
          }
        }
      }

    final int array2Dims = (type2 != null ? type2.getArrayDimensions():0) + countCStyleArrayDeclarationDims(_type2);
    final int arrayDims = ( type1 != null ? type1.getArrayDimensions():0) + countCStyleArrayDeclarationDims(_type);

    if (matchContext.getPattern().isTypedVar(el)) {
      final SubstitutionHandler handler = (SubstitutionHandler) matchContext.getPattern().getHandler(el);

      RegExpPredicate regExpPredicate = null;

      if (arrayDims != 0) {
        if (arrayDims != array2Dims) {
          return false;
        }
      } else if (array2Dims != 0) {
        regExpPredicate = MatchingHandler.getSimpleRegExpPredicate(handler);

        if (regExpPredicate != null) {
          regExpPredicate.setNodeTextGenerator(new RegExpPredicate.NodeTextGenerator() {
            public String getText(PsiElement element) {
              StringBuilder builder = new StringBuilder(RegExpPredicate.getMeaningfulText(element));
              for(int i = 0; i < array2Dims; ++i) builder.append("[]");
              return builder.toString();
            }
          });
        }
      }

      try {
        if (handler.isSubtype() || handler.isStrictSubtype()) {
          return checkMatchWithingHierarchy(el2, handler, el);
        } else {
          return handler.handle(el2, matchContext);
        }
      }
      finally {
        if (regExpPredicate != null) regExpPredicate.setNodeTextGenerator( null );
      }
    }

    if (array2Dims != arrayDims) {
      return false;
    }

    final String text = el.getText();
    if (text.indexOf('.')==-1 || !(el2 instanceof PsiJavaReference)) {
      return MatchUtils.compareWithNoDifferenceToPackage(text,el2.getText());
    } else {
      PsiElement element2 = ((PsiJavaReference)el2).resolve();

      if (element2!=null) {
        return text.equals(((PsiClass)element2).getQualifiedName());
      } else {
        return MatchUtils.compareWithNoDifferenceToPackage(text,el2.getText());
      }
    }
  }

  private boolean checkMatchWithingHierarchy(PsiElement el2, SubstitutionHandler handler, PsiElement context) {
    boolean includeInterfaces = true;
    boolean includeClasses = true;
    final PsiElement contextParent = context.getParent();

    if (contextParent instanceof PsiReferenceList) {
      final PsiElement grandParentContext = contextParent.getParent();

      if (grandParentContext instanceof PsiClass) {
        final PsiClass psiClass = ((PsiClass)grandParentContext);

        if (contextParent == psiClass.getExtendsList()) {
          includeInterfaces = psiClass.isInterface();
        } else if (contextParent == psiClass.getImplementsList()) {
          includeClasses = false;
        }
      }
    }

    // is type2 is (strict) subtype of type
    final NodeIterator node = new HierarchyNodeIterator(el2, includeClasses, includeInterfaces);

    if (handler.isStrictSubtype()) {
      node.advance();
    }

    final boolean notPredicate = handler.getPredicate() instanceof NotPredicate;
    while (node.hasNext() && !handler.validate(node.current(), 0, -1, matchContext)) {
      if (notPredicate) return false;
      node.advance();
    }

    if (node.hasNext()) {
      handler.addResult(el2, 0, -1, matchContext);
      return true;
    } else {
      return false;
    }
  }

  private static int countCStyleArrayDeclarationDims(final PsiElement type2) {
    if (type2 != null) {
      final PsiElement parentElement = type2.getParent();

      if (parentElement instanceof PsiVariable) {
        final PsiIdentifier psiIdentifier = ((PsiVariable)parentElement).getNameIdentifier();
        if (psiIdentifier == null) return 0;

        int count = 0;
        for(PsiElement sibling = psiIdentifier.getNextSibling();sibling != null; sibling = sibling.getNextSibling()) {
          if (sibling instanceof PsiJavaToken) {
            final IElementType tokenType = ((PsiJavaToken)sibling).getTokenType();
            if (tokenType == JavaTokenType.LBRACKET) ++count;
            if (tokenType != JavaTokenType.RBRACKET) break;
          }
        }

        return count;
      }
    }
    return 0;
  }

  private void matchArrayDims(final PsiNewExpression new1, final PsiNewExpression new2) {
    final PsiExpression[] arrayDims = new1.getArrayDimensions();
    final PsiExpression[] arrayDims2 = new2.getArrayDimensions();

    if (arrayDims!=null && arrayDims2!=null && arrayDims.length == arrayDims2.length && arrayDims.length != 0) {
      for(int i = 0; i < arrayDims.length; ++i) {
        result = match(arrayDims[i],arrayDims2[i]);
        if (!result) return;
      }
    } else {
      result = (arrayDims == arrayDims2) && matchSons(new1.getArgumentList(),new2.getArgumentList());
    }
  }

  private void saveOrDropResult(final PsiIdentifier methodNameNode, final boolean typedVar, final PsiIdentifier methodNameNode2) {
    MatchResultImpl ourResult = matchContext.hasResult() ? matchContext.getResult():null;
    matchContext.popResult();

    if (result) {
      if (typedVar) {
        final SubstitutionHandler handler = (SubstitutionHandler) matchContext.getPattern().getHandler(methodNameNode);
        if (ourResult != null) ourResult.setScopeMatch(true);
        handler.setNestedResult( ourResult );
        result = handler.handle(methodNameNode2,matchContext);

        if (handler.getNestedResult() != null) { // some constraint prevent from adding
          handler.setNestedResult(null);
          copyResults(ourResult);
        }
      } else if (ourResult != null) {
        copyResults(ourResult);
      }
    }
  }

  private void copyResults(final MatchResultImpl ourResult) {
    if (ourResult.hasSons()) {
      for(MatchResult son:ourResult.getAllSons()) {
        matchContext.getResult().addSon((MatchResultImpl)son);
      }
    }
  }

  public static final String getText(final PsiElement match, int start,int end) {
    final String matchText = match.getText();
    if (start==0 && end==-1) return matchText;
    return matchText.substring(start,end == -1? matchText.length():end);
  }

  private class MyJavaVisitor extends JavaElementVisitor {
    @Override public void visitComment(PsiComment comment) {
      PsiElement comment2 = null;

      if (!(element instanceof PsiComment)) {
        if (element instanceof PsiMember) {
          final PsiElement[] children = element.getChildren();
          if (children[0] instanceof PsiComment) {
            comment2 = children[0];
          }
        }
      } else {
        comment2 = element;
      }

      if (comment2 == null) {
        result = false;
        return;
      }

      final Object userData = comment.getUserData(CompiledPattern.HANDLER_KEY);

      if (userData instanceof String) {
        String str = (String) userData;
        int end = comment2.getTextLength();

        if (((PsiComment)comment2).getTokenType() == JavaTokenType.C_STYLE_COMMENT) {
          end -= 2;
        }
        result = ((SubstitutionHandler)matchContext.getPattern().getHandler(str)).handle(
          comment2,
          2,
          end,
          matchContext
        );
      } else if (userData instanceof MatchingHandler) {
        result = ((MatchingHandler)userData).match(comment,comment2,matchContext);
      } else {
        result = comment.getText().equals(comment2.getText());
      }
    }

    @Override public void visitDocTagValue(final PsiDocTagValue value) {
      final PsiDocTagValue value2 = (PsiDocTagValue) element;
      final boolean isTypedVar = matchContext.getPattern().isTypedVar(value);

      if (isTypedVar) {
        result = handleTypedElement(value,value2);
      } else {
        result = value.textMatches(value2);
      }
    }

    @Override public final void visitModifierList(final PsiModifierList list) {
      final PsiModifierList list2 = (PsiModifierList) element;

      for (@Modifier String aMODIFIERS : MODIFIERS) {
        if (list.hasModifierProperty(aMODIFIERS) && !list2.hasModifierProperty(aMODIFIERS)) {
          result = false;
          return;
        }
      }

      final PsiAnnotation[] annotations = list.getAnnotations();
      if (annotations.length > 0) {
        HashSet<PsiAnnotation> set = new HashSet<PsiAnnotation>( Arrays.asList(annotations));

        for(PsiAnnotation annotation:annotations) {
          final PsiJavaCodeReferenceElement nameReferenceElement = annotation.getNameReferenceElement();

          if (nameReferenceElement != null && MatchOptions.MODIFIER_ANNOTATION_NAME.equals(nameReferenceElement.getText())) {
            final PsiAnnotationParameterList parameterList = annotation.getParameterList();
            final PsiNameValuePair[] attributes = parameterList.getAttributes();

            for(PsiNameValuePair pair:attributes) {
              final PsiAnnotationMemberValue value = pair.getValue();
              if (value == null) continue;

              if (value instanceof PsiArrayInitializerMemberValue) {
                boolean matchedOne = false;

                for(PsiAnnotationMemberValue v:((PsiArrayInitializerMemberValue)value).getInitializers()) {
                  final String name = StringUtil.stripQuotesAroundValue(v.getText());
                  if (MatchOptions.INSTANCE_MODIFIER_NAME.equals(name)) {
                    if (isNotInstanceModifier(list2)) {
                      result = false;
                      return;
                    } else {
                      matchedOne = true;
                    }
                  } else if (list2.hasModifierProperty(name)) {
                    matchedOne = true;
                    break;
                  }
                }

                if (!matchedOne) {
                  result = false;
                  return;
                }
              } else {
                final String name = StringUtil.stripQuotesAroundValue(value.getText());
                if (MatchOptions.INSTANCE_MODIFIER_NAME.equals(name)) {
                  if (isNotInstanceModifier(list2)) {
                    result = false;
                    return;
                  }
                } else if (!list2.hasModifierProperty(name)) {
                  result = false;
                  return;
                }
              }
            }

            set.remove(annotation);
          }
        }

        result = set.size() == 0 || matchInAnyOrder(set.toArray(new PsiAnnotation[set.size()]),list2.getAnnotations());
      } else {
        result = true;
      }
    }

    @Override public void visitDocTag(final PsiDocTag tag) {
      final PsiDocTag tag2 = (PsiDocTag) element;
      final boolean isTypedVar = matchContext.getPattern().isTypedVar(tag.getNameElement());

      result = (isTypedVar || tag.getName().equals(tag2.getName()) );

      final PsiDocTagValue psiDocTagValue = tag.getValueElement();
      boolean isTypedValue = false;

      if (result && psiDocTagValue !=null) {
        isTypedValue = matchContext.getPattern().isTypedVar(psiDocTagValue);

        if (isTypedValue) {
          if (tag2.getValueElement()!=null) {
            result = handleTypedElement(psiDocTagValue,tag2.getValueElement());
          }
          else {
            result = allowsAbsenceOfMatch(psiDocTagValue);
          }
        }
      }

      if (result && !isTypedValue) {
        result = matchInAnyOrder(
          new DocValuesIterator(tag.getFirstChild()),
          new DocValuesIterator(tag2.getFirstChild())
        );
      }

      if (result && isTypedVar) {
        result = handleTypedElement(tag.getNameElement(), tag2.getNameElement());
      }
    }

    @Override public void visitDocComment(final PsiDocComment comment) {
      PsiDocComment comment2;

      if (element instanceof PsiDocCommentOwner) {
        comment2 = ((PsiDocCommentOwner)element).getDocComment();

        if (comment2==null && comment.getTags()!=null) {
          // doc comment are not collapsed for inner classes!
          result = false;
          return;
        }
      } else {
        comment2 = (PsiDocComment) element;

        if (element.getParent() instanceof PsiDocCommentOwner) {
          result = false;
          return; // we should matched the doc before
        }
      }

      if (comment.getTags().length > 0) {
        result = matchInAnyOrder(comment.getTags(),comment2.getTags());
      } else {
        visitComment(comment);
      }
    }

    @Override public void visitElement(PsiElement el) {
      result = el.textMatches(element);
    }

    @Override public void visitArrayInitializerExpression(PsiArrayInitializerExpression expression) {
      final PsiArrayInitializerExpression expr2 = (PsiArrayInitializerExpression) element;

      result = matchSequentially(
        new ArrayBackedNodeIterator(expression.getInitializers()),
        new ArrayBackedNodeIterator(expr2.getInitializers())
      );
    }

    @Override public void visitClassInitializer(PsiClassInitializer initializer) {
      PsiClassInitializer initializer2 = (PsiClassInitializer)element;
      result = match(initializer.getModifierList(),initializer2.getModifierList()) &&
               match(initializer.getBody(),initializer2.getBody());
    }

    @Override public void visitCodeBlock(PsiCodeBlock block) {
      result = matchSons(block,element);
    }

    @Override public void visitJavaToken(final PsiJavaToken token) {
      final PsiJavaToken anotherToken = (PsiJavaToken) element;

      result = token.getTokenType() == anotherToken.getTokenType() &&
               token.textMatches(anotherToken);
    }

    @Override public void visitAnnotation(PsiAnnotation annotation) {
      final PsiAnnotation psiAnnotation = (PsiAnnotation)element;

      result = match(annotation.getNameReferenceElement(),psiAnnotation.getNameReferenceElement()) &&
               matchInAnyOrder(annotation.getParameterList().getAttributes(),psiAnnotation.getParameterList().getAttributes());
    }

    @Override public void visitNameValuePair(PsiNameValuePair pair) {
      final PsiIdentifier nameIdentifier = pair.getNameIdentifier();

      if (nameIdentifier!=null) {
        final MatchingHandler handler = matchContext.getPattern().getHandler(nameIdentifier);

        if (handler instanceof SubstitutionHandler) {
          result = ((SubstitutionHandler)handler).handle(((PsiNameValuePair)element).getNameIdentifier(),matchContext);
        } else {
          result = match(nameIdentifier, ((PsiNameValuePair)element).getNameIdentifier());
        }
      } else {
        result = true;
      }
    }

    @Override public void visitField(PsiField psiField) {
      if (!checkHierarchy(element,psiField)) {
        result = false;
        return;
      }
      super.visitField(psiField);
    }

    @Override public void visitAnonymousClass(final PsiAnonymousClass clazz) {
      final PsiAnonymousClass clazz2 = (PsiAnonymousClass) element;
      final boolean isTypedVar = matchContext.getPattern().isTypedVar(clazz.getFirstChild());

      result = (match(clazz.getBaseClassReference(),clazz2.getBaseClassReference()) || isTypedVar) &&
               matchSons(clazz.getArgumentList(),clazz2.getArgumentList()) &&
               compareClasses(clazz,clazz2);

      if (result && isTypedVar) {
        result = handleTypedElement(clazz.getFirstChild(),clazz2.getFirstChild());
      }
    }

    @Override public void visitArrayAccessExpression(final PsiArrayAccessExpression slice) {
      final PsiArrayAccessExpression slice2 = (PsiArrayAccessExpression) element;

      result = match(slice.getArrayExpression(),slice2.getArrayExpression()) &&
               match(slice.getIndexExpression(),slice2.getIndexExpression());
    }

    @Override public void visitReferenceExpression(final PsiReferenceExpression reference) {
      final PsiExpression qualifier = reference.getQualifierExpression();

      final PsiElement nameElement = reference.getReferenceNameElement();
      MatchingHandler _handler = nameElement != null ? matchContext.getPattern().getHandlerSimple(nameElement):null;
      if (!(_handler instanceof SubstitutionHandler)) _handler = matchContext.getPattern().getHandlerSimple(reference);

      if(_handler instanceof SubstitutionHandler &&
         !(matchContext.getPattern().getHandlerSimple(qualifier) instanceof SubstitutionHandler) &&
         !(qualifier instanceof PsiThisExpression)
        ) {
        if (element instanceof PsiReferenceExpression) {
          final PsiReferenceExpression psiReferenceExpression = ((PsiReferenceExpression)element);

          if (psiReferenceExpression.getQualifierExpression()==null) {
            element = psiReferenceExpression.getReferenceNameElement();
          }
        }

        final SubstitutionHandler handler = (SubstitutionHandler) _handler;
        if (handler.isSubtype() || handler.isStrictSubtype()) {
          result = checkMatchWithingHierarchy(element,handler, reference);
        } else {
          result = handler.handle(element,matchContext);
        }

        return;
      }

      if (!(element instanceof PsiReferenceExpression)) {
        result = false;
        return;
      }

      final PsiReferenceExpression reference2 = (PsiReferenceExpression) element;

      // just variable
      final PsiExpression reference2Qualifier = reference2.getQualifierExpression();
      if (qualifier ==null && reference2Qualifier == null) {
        result = reference.getReferenceNameElement().textMatches(reference2.getReferenceNameElement());
        return;
      }

      // handle field selection
      if ( !(element.getParent() instanceof PsiMethodCallExpression) && // element is not a method) &&
           qualifier !=null &&
           ( reference2Qualifier !=null ||
             ( qualifier instanceof PsiThisExpression &&
               MatchUtils.getReferencedElement(element) instanceof PsiField
             )
           )
         ) {
        final PsiElement referenceElement = reference.getReferenceNameElement();
        final PsiElement referenceElement2 = reference2.getReferenceNameElement();

        if (matchContext.getPattern().isTypedVar(referenceElement)) {
          result = handleTypedElement(referenceElement, referenceElement2);
        } else {
          result = (referenceElement2 != null && referenceElement != null && referenceElement.textMatches(referenceElement2)) ||
            referenceElement == referenceElement2;
        }

        if (result &&
            reference2Qualifier !=null
           ) {
          result = match(qualifier, reference2Qualifier);
        }

        return;
      }

      result = false;
    }

    @Override public void visitConditionalExpression(final PsiConditionalExpression cond) {
      final PsiConditionalExpression cond2 = (PsiConditionalExpression) element;

      result = match(cond.getCondition(),cond2.getCondition()) &&
               matchSons(cond,cond2);
    }

    @Override public void visitBinaryExpression(final PsiBinaryExpression binExpr) {
      final PsiBinaryExpression binExpr2 = (PsiBinaryExpression) element;

      result = binExpr.getOperationSign().textMatches(binExpr2.getOperationSign()) &&
               match(binExpr.getLOperand(), binExpr2.getLOperand()) &&
               match(binExpr.getROperand(),binExpr2.getROperand());
    }

    @Override public void visitVariable(final PsiVariable var) {
      matchContext.pushResult();
      final PsiIdentifier nameIdentifier = var.getNameIdentifier();

      boolean isTypedVar = matchContext.getPattern().isTypedVar(nameIdentifier);
      boolean isTypedInitializer = var.getInitializer() != null &&
                                   matchContext.getPattern().isTypedVar(var.getInitializer()) &&
                                   var.getInitializer() instanceof PsiReferenceExpression;
      final PsiVariable var2 = (PsiVariable) element;

      try {
        result = (var.getName().equals(var2.getName()) || isTypedVar) &&
                 ( ( var.getParent() instanceof PsiClass && ((PsiClass)var.getParent()).isInterface()) ||
                   match(var.getModifierList(),var2.getModifierList())
                 ) &&
                 match(var.getTypeElement(),var2.getTypeElement());

        if (result) {
          // Check initializer
          final PsiExpression var2Initializer = var2.getInitializer();

          result = match(var.getInitializer(), var2Initializer) ||
                   ( isTypedInitializer &&
                     var2Initializer == null &&
                     allowsAbsenceOfMatch(var.getInitializer())
                   );
        }

        if (result && var instanceof PsiParameter && var.getParent() instanceof PsiCatchSection) {
          result = match(
            ((PsiCatchSection)var.getParent()).getCatchBlock(),
            ((PsiCatchSection)var2.getParent()).getCatchBlock()
          );
        }

        if (result && isTypedVar) {
          result = handleTypedElement(nameIdentifier,var2.getNameIdentifier());
        }
      }
      finally {
        saveOrDropResult(nameIdentifier, isTypedVar, var2.getNameIdentifier());
      }
    }

    @Override public void visitMethodCallExpression(final PsiMethodCallExpression mcall) {
      if (!(element instanceof PsiMethodCallExpression)) {
        result = false;
        return;
      }
      final PsiMethodCallExpression mcall2 = (PsiMethodCallExpression) element;
      final PsiReferenceExpression mcallRef1 = mcall.getMethodExpression();
      final PsiReferenceExpression mcallRef2 = mcall2.getMethodExpression();

      final String mcallname1 = mcallRef1.getReferenceName();
      final String mcallname2 = mcallRef2.getReferenceName();
      boolean isTypedVar = matchContext.getPattern().isTypedVar(mcallRef1.getReferenceNameElement());

      if (mcallname1 != null && !mcallname1.equals(mcallname2) && !isTypedVar) {
        result = false;
        return;
      }

      final PsiExpression qualifier = mcallRef1.getQualifierExpression();
      final PsiExpression elementQualfier = mcallRef2.getQualifierExpression();
      if (qualifier !=null) {

        if (elementQualfier !=null) {
          result = match(qualifier, elementQualfier);
          if (!result) return;
        } else {
          MatchingHandler handler = matchContext.getPattern().getHandler(qualifier);
          if (!(handler instanceof SubstitutionHandler) ||
              ((SubstitutionHandler)handler).getMinOccurs()!=0) {
            result = false;
            return;
          } else {
            // we may have not ? expr_type constraint set on qualifier expression so validate it
            SubstitutionHandler substitutionHandler = (SubstitutionHandler)handler;

            if (substitutionHandler.getPredicate()!=null) {
              boolean isnot = false;
              MatchPredicate _predicate = substitutionHandler.getPredicate();
              ExprTypePredicate predicate = null;

              if (_predicate instanceof NotPredicate) {
                isnot = true;
                _predicate = ((NotPredicate)_predicate).getHandler();
              }

              if (_predicate instanceof ExprTypePredicate) {
                predicate = (ExprTypePredicate)_predicate;
              }

              if (predicate != null) {
                PsiMethod method = (PsiMethod)mcallRef2.resolve();
                if (method != null) {
                  result = predicate.checkClass(method.getContainingClass(),matchContext);
                  if (isnot) result = !result;
                } else {
                  result = false;
                }

                if (!result) return;
              }
            }
          }
        }
      } else if (elementQualfier !=null) {
        result = false;
        return;
      }

      result = matchSons(mcall.getArgumentList(),mcall2.getArgumentList());

      if (result && isTypedVar) {
        result &= handleTypedElement(mcallRef1.getReferenceNameElement(),mcallRef2.getReferenceNameElement());
      }
    }

    @Override public void visitExpressionStatement(final PsiExpressionStatement expr) {
      final PsiExpressionStatement expr2 = (PsiExpressionStatement) element;

      result = match(expr.getExpression(),expr2.getExpression());
    }

    @Override public void visitLiteralExpression(final PsiLiteralExpression const1) {
      final PsiLiteralExpression const2 = (PsiLiteralExpression) element;

      MatchingHandler handler = (MatchingHandler)const1.getUserData(CompiledPattern.HANDLER_KEY);

      if (handler instanceof SubstitutionHandler) {
        int offset = 0;
        int length = const2.getTextLength();
        final String text = const2.getText();

        if (length > 2 && text.charAt(0) == '"' && text.charAt(length-1)=='"') {
          length--;
          offset++;
        }
        result = ((SubstitutionHandler)handler).handle(const2,offset,length,matchContext);
      } else if (handler!=null) {
        result = handler.match(const1,const2,matchContext);
      } else {
        result = const1.textMatches(const2);
      }
    }

    @Override public void visitAssignmentExpression(final PsiAssignmentExpression assign) {
      final PsiAssignmentExpression assign2 = (PsiAssignmentExpression)element;

      result =
        assign.getOperationSign().textMatches(assign2.getOperationSign()) &&
        match(assign.getLExpression(),assign2.getLExpression()) &&
        match(assign.getRExpression(),assign2.getRExpression());
    }

    @Override public void visitIfStatement(final PsiIfStatement if1) {
      final PsiIfStatement if2 = (PsiIfStatement) element;

      result = match(if1.getCondition(),if2.getCondition()) &&
               compareBody(if1.getThenBranch(),if2.getThenBranch()) &&
               compareBody(if1.getElseBranch(),if2.getElseBranch());
    }

    @Override public void visitSwitchStatement(final PsiSwitchStatement switch1) {
      final PsiSwitchStatement switch2 = (PsiSwitchStatement) element;

      result = match(switch1.getExpression(),switch2.getExpression()) &&
               matchSons(switch1.getBody(),switch2.getBody());
    }

    @Override public void visitForStatement(final PsiForStatement for1) {
      final PsiForStatement for2 = (PsiForStatement) element;

      final PsiStatement initialization = for1.getInitialization();
      MatchingHandler handler = matchContext.getPattern().getHandler(initialization);

      result = handler.match(initialization, for2.getInitialization(), matchContext) &&
               match(for1.getCondition(),for2.getCondition()) &&
               match(for1.getUpdate(),for2.getUpdate()) &&
               compareBody(for1.getBody(),for2.getBody());
    }

    @Override public void visitForeachStatement(PsiForeachStatement for1) {
      final PsiForeachStatement for2 = (PsiForeachStatement) element;

      result = match(for1.getIterationParameter(),for2.getIterationParameter()) &&
               match(for1.getIteratedValue(),for2.getIteratedValue()) &&
               compareBody(for1.getBody(),for2.getBody());
    }

    @Override public void visitWhileStatement(final PsiWhileStatement while1) {
      final PsiWhileStatement while2 = (PsiWhileStatement)element;

      result = match(while1.getCondition(),while2.getCondition()) &&
               compareBody(while1.getBody(),while2.getBody());
    }

    @Override public void visitBlockStatement(final PsiBlockStatement block) {
      if (element instanceof PsiCodeBlock &&
          !(element.getParent() instanceof PsiBlockStatement)
         ) {
        result = matchSons(block.getCodeBlock(),element);
      } else {
        final PsiBlockStatement block2 = (PsiBlockStatement) element;
        result = matchSons(block,block2);
      }
    }

    @Override public void visitDeclarationStatement(final PsiDeclarationStatement dcl) {
      final PsiDeclarationStatement declaration = (PsiDeclarationStatement)element;
      result = matchInAnyOrder(dcl.getDeclaredElements(), declaration.getDeclaredElements());
    }

    @Override public void visitDoWhileStatement(final PsiDoWhileStatement while1) {
      final PsiDoWhileStatement while2 = (PsiDoWhileStatement) element;

      result = match(while1.getCondition(),while2.getCondition()) &&
               compareBody(while1.getBody(),while2.getBody());
    }

    @Override public void visitReturnStatement(final PsiReturnStatement return1) {
      final PsiReturnStatement return2 = (PsiReturnStatement) element;

      result = match(return1.getReturnValue(),return2.getReturnValue());
    }

    @Override public void visitPostfixExpression(final PsiPostfixExpression postfix) {
      final PsiPostfixExpression postfix2 = (PsiPostfixExpression)element;

      result = postfix.getOperationSign().textMatches(postfix2.getOperationSign())
               && match(postfix.getOperand(), postfix2.getOperand());
    }

    @Override public void visitPrefixExpression(final PsiPrefixExpression prefix) {
      final PsiPrefixExpression prefix2 = (PsiPrefixExpression)element;

      result = prefix.getOperationSign().textMatches(prefix2.getOperationSign())
               && match(prefix.getOperand(), prefix2.getOperand());
    }

    @Override public void visitAssertStatement(final PsiAssertStatement assert1) {
      final PsiAssertStatement assert2 = (PsiAssertStatement) element;

      result = match(assert1.getAssertCondition(),assert2.getAssertCondition()) &&
               match(assert1.getAssertDescription(),assert2.getAssertDescription());
    }

    @Override public void visitBreakStatement(final PsiBreakStatement break1) {
      final PsiBreakStatement break2 = (PsiBreakStatement) element;

      result = match(break1.getLabelIdentifier(),break2.getLabelIdentifier());
    }

    @Override public void visitContinueStatement(final PsiContinueStatement continue1) {
      final PsiContinueStatement continue2 = (PsiContinueStatement) element;

      result = match(continue1.getLabelIdentifier(),continue2.getLabelIdentifier());
    }

    @Override public void visitSuperExpression(final PsiSuperExpression super1) {
      result = true;
    }

    @Override public void visitThisExpression(final PsiThisExpression this1) {
      result = element instanceof PsiThisExpression;
    }

    @Override public void visitSynchronizedStatement(final PsiSynchronizedStatement synchronized1) {
      final PsiSynchronizedStatement synchronized2 = (PsiSynchronizedStatement) element;

      result = match(synchronized1.getLockExpression(),synchronized2.getLockExpression()) &&
               matchSons(synchronized1.getBody(),synchronized2.getBody());
    }

    @Override public void visitThrowStatement(final PsiThrowStatement throw1) {
      final PsiThrowStatement throw2 = (PsiThrowStatement) element;

      result = match(throw1.getException(),throw2.getException());
    }

    @Override public void visitParenthesizedExpression(PsiParenthesizedExpression expr) {
      if (element instanceof PsiParenthesizedExpression) {
        result = matchSons(expr,element);
      } else {
        result = false;
      }
    }

    @Override public void visitCatchSection(final PsiCatchSection section) {
      final PsiCatchSection section2 = (PsiCatchSection) element;
      final PsiParameter parameter = section.getParameter();
      if (parameter != null) result = match(parameter, section2.getParameter());
      else result = match(section.getCatchBlock(), section2.getCatchBlock());
    }

    @Override public void visitTryStatement(final PsiTryStatement try1) {
      final PsiTryStatement try2 = (PsiTryStatement) element;

      result = matchSons(try1.getTryBlock(),try2.getTryBlock());

      if (!result) return;

      final PsiCatchSection[] catches1 = try1.getCatchSections();
      final PsiCodeBlock finally1 = try1.getFinallyBlock();

      final PsiCatchSection[] catches2 = try2.getCatchSections();
      final PsiCodeBlock finally2 = try2.getFinallyBlock();

      if (!matchContext.getOptions().isLooseMatching() &&
          ( ( catches1.length == 0 &&
              catches2.length!=0
            ) ||
              ( finally1 == null &&
                finally2 != null
              )
          )
         ) {
        result = false;
      } else {
        List<PsiCatchSection> unmatchedCatchSections = new ArrayList<PsiCatchSection>();

        for(int j = 0;j < catches2.length;++j) {
          unmatchedCatchSections.add(catches2[j]);
        }

        for(int i = 0, j; i < catches1.length; ++i) {
          MatchingHandler handler = matchContext.getPattern().getHandler(catches1[i]);
          final PsiElement pinnedNode = handler.getPinnedNode(null);

          if (pinnedNode != null) {
            result = handler.match(catches1[i], pinnedNode, matchContext);
            if (!result) return;
          } else {
            for(j = 0; j < unmatchedCatchSections.size(); ++j) {
              if (handler.match(catches1[i],unmatchedCatchSections.get(j), matchContext)) {
                unmatchedCatchSections.remove(j);
                break;
              }
            }

            if (j==catches2.length) {
              result = false;
              return;
            }
          }
        }

        if (finally1!=null) {
          result = matchSons(finally1,finally2);
        }

        if (result && unmatchedCatchSections.size() > 0 && !matchContext.getOptions().isLooseMatching()) {
          try2.putUserData(MatcherImplUtil.UNMATCHED_CATCH_SECTION_CONTENT_VAR_KEY,unmatchedCatchSections);
        }
      }
    }

    @Override public void visitSwitchLabelStatement(final PsiSwitchLabelStatement case1) {
      final PsiSwitchLabelStatement case2 = (PsiSwitchLabelStatement) element;

      result = case1.isDefaultCase() == case2.isDefaultCase() &&
               match(case1.getCaseValue(),case2.getCaseValue());
    }

    @Override public void visitInstanceOfExpression(final PsiInstanceOfExpression instanceOf) {
      final PsiInstanceOfExpression instanceOf2 = (PsiInstanceOfExpression) element;
      result = match(instanceOf.getOperand(),instanceOf2.getOperand()) &&
               matchType(instanceOf.getCheckType(),instanceOf2.getCheckType());
    }

    @Override public void visitNewExpression(final PsiNewExpression new1) {
      if (element instanceof PsiArrayInitializerExpression &&
          element.getParent() instanceof PsiVariable &&
          new1.getArrayDimensions().length == 0 &&
          new1.getArrayInitializer() != null
         ) {
        result = match(new1.getClassReference(),((PsiVariable)element.getParent()).getTypeElement());
          matchSons(new1.getArrayInitializer(),element);
        return;
      }

      if (!(element instanceof PsiNewExpression)) {
        result = false;
        return;
      }
      final PsiNewExpression new2 = (PsiNewExpression) element;

      if (new1.getClassReference() != null) {
        if (new2.getClassReference() != null) {
          result = match(new1.getClassReference(),new2.getClassReference()) &&
                   matchSons(new1.getArrayInitializer(),new2.getArrayInitializer());

          if (result) {
            // matching dims
            matchArrayDims(new1, new2);
          }
          return;
        } else {
          // match array of primitive by new 'T();
          final PsiKeyword newKeyword = PsiTreeUtil.getChildOfType(new2, PsiKeyword.class);
          final PsiElement element = PsiTreeUtil.getNextSiblingOfType(newKeyword, PsiWhiteSpace.class);

          if (element != null && element.getNextSibling() instanceof PsiKeyword) {
            ((LexicalNodesFilter)LexicalNodesFilter.getInstance()).setCareKeyWords(true);

            result = match(new1.getClassReference(),element.getNextSibling()) &&
                     matchSons(new1.getArrayInitializer(),new2.getArrayInitializer());

            ((LexicalNodesFilter)LexicalNodesFilter.getInstance()).setCareKeyWords(false);
            if (result) {
              // matching dims
              matchArrayDims(new1, new2);
            }

            return;
          }
        }
      }

      if (new1.getClassReference() == new2.getClassReference()) {
        // probably anonymous class or array of primitive type
        ((LexicalNodesFilter)LexicalNodesFilter.getInstance()).setCareKeyWords(true);
        result = matchSons(new1,new2);
        ((LexicalNodesFilter)LexicalNodesFilter.getInstance()).setCareKeyWords(false);
      } else if (new1.getAnonymousClass()==null &&
                 new1.getClassReference()!=null &&
                 new2.getAnonymousClass()!=null) {
        // allow matching anonymous class without pattern
        result = match(new1.getClassReference(),new2.getAnonymousClass().getBaseClassReference()) &&
                 matchSons(new1.getArgumentList(),new2.getArgumentList());
      } else {
        result = false;
      }
    }

    @Override public void visitKeyword(PsiKeyword keyword) {
      result = keyword.textMatches(element);
    }

    @Override public void visitTypeCastExpression(final PsiTypeCastExpression cast) {
      final PsiTypeCastExpression cast2 = (PsiTypeCastExpression) element;

      result = ( match(cast.getCastType(),cast2.getCastType()) ) &&
               match(cast.getOperand(),cast2.getOperand());
    }

    @Override public void visitClassObjectAccessExpression(final PsiClassObjectAccessExpression expr) {
      final PsiClassObjectAccessExpression expr2 = (PsiClassObjectAccessExpression) element;

      result = match(expr.getOperand(),expr2.getOperand());
    }

    @Override public void visitReferenceElement(final PsiJavaCodeReferenceElement ref) {
      result = matchType(ref,element);
    }

    @Override public void visitTypeElement(final PsiTypeElement typeElement) {
      result = matchType(typeElement,element);
    }

    @Override public void visitTypeParameter(PsiTypeParameter psiTypeParameter) {
      final PsiTypeParameter parameter = (PsiTypeParameter) element;
      final PsiElement[] children = psiTypeParameter.getChildren();
      final PsiElement[] children2 = parameter.getChildren();

      final MatchingHandler handler = matchContext.getPattern().getHandler(children[0]);

      if (handler instanceof SubstitutionHandler) {
        result = ((SubstitutionHandler)handler).handle(children2[0],matchContext);
      } else {
        result = children[0].textMatches(children2[0]);
      }

      if (result && children.length > 2) {
        // constraint present
        if (children2.length == 2) {
          result = false;
          return;
        }

        if (!children[2].getFirstChild().textMatches(children2[2].getFirstChild())) {
          // constraint type (extends)
          result = false;
          return;
        }
        result = matchInAnyOrder(children[2].getChildren(), children2[2].getChildren());
      }
    }

    @Override public void visitClass(PsiClass clazz) {
      if (clazz.hasTypeParameters()) {
        result = match(clazz.getTypeParameterList(), ((PsiClass)element).getTypeParameterList());

        if (!result) return;
      }

      PsiClass clazz2;

      if (element instanceof PsiDeclarationStatement &&
          element.getFirstChild() instanceof PsiClass
         ) {
        clazz2 = (PsiClass) element.getFirstChild();
      } else {
        clazz2 = (PsiClass) element;
      }

      final boolean isTypedVar = matchContext.getPattern().isTypedVar(clazz.getNameIdentifier());

      if (clazz.getModifierList().getTextLength() > 0) {
        if (!match(clazz.getModifierList(),clazz2.getModifierList())) {
          result = false;
          return;
        }
      }

      result = (clazz.getName().equals(clazz2.getName()) || isTypedVar) &&
               compareClasses(clazz,clazz2);

      if (result && isTypedVar) {
        PsiElement id = clazz2.getNameIdentifier();
        if (id==null) id = clazz2;
        result = handleTypedElement(clazz.getNameIdentifier(),id);
      }
    }

    @Override public void visitTypeParameterList(PsiTypeParameterList psiTypeParameterList) {
      result = matchSequentially(
        psiTypeParameterList.getFirstChild(),
        element.getFirstChild()
      );
    }

    @Override public void visitMethod(PsiMethod method) {
      final PsiIdentifier methodNameNode = method.getNameIdentifier();
      final boolean isTypedVar = matchContext.getPattern().isTypedVar(methodNameNode);
      final PsiMethod method2 = (PsiMethod) element;

      matchContext.pushResult();

      try {
        if (method.hasTypeParameters()) {
          result = match(method.getTypeParameterList(), ((PsiMethod)element).getTypeParameterList());

          if (!result) return;
        }

        if (!checkHierarchy(method2,method)) {
          result = false;
          return;
        }

        result = (method.getName().equals(method2.getName()) || isTypedVar) &&
                 match(method.getModifierList(),method2.getModifierList()) &&
                 matchSons(method.getParameterList(),method2.getParameterList()) &&
                 match(method.getReturnTypeElement(),method2.getReturnTypeElement()) &&
                 matchInAnyOrder(method.getThrowsList(),method2.getThrowsList()) &&
                 matchSonsOptionally( method.getBody(), method2.getBody() );
      } finally {
        final PsiIdentifier methodNameNode2 = method2.getNameIdentifier();

        saveOrDropResult(methodNameNode, isTypedVar, methodNameNode2);
      }
    }
  }

  private class MyXmlVisitor extends XmlElementVisitor {
    @Override
    public void visitElement(final PsiElement element) {
      result = element.textMatches(element);
    }

    @Override public void visitXmlAttribute(XmlAttribute attribute) {
      final XmlAttribute another = (XmlAttribute)element;
      final boolean isTypedVar = matchContext.getPattern().isTypedVar(attribute.getName());

      result = (attribute.getName().equals(another.getName()) || isTypedVar);
      if (result) {
        result = match(attribute.getValueElement(), another.getValueElement());
      }

      if (result && isTypedVar) {
        MatchingHandler handler = matchContext.getPattern().getHandler(attribute.getName());
        result = ((SubstitutionHandler)handler).handle(another, matchContext);
      }
    }

    @Override public void visitXmlAttributeValue(XmlAttributeValue value) {
      final XmlAttributeValue another = (XmlAttributeValue) element;
      final String text = StringUtil.stripQuotesAroundValue( value.getText() );

      final boolean isTypedVar = matchContext.getPattern().isTypedVar(text);
      MatchingHandler handler;

      if (isTypedVar && (handler = matchContext.getPattern().getHandler( text )) instanceof SubstitutionHandler) {
        String text2 = another.getText();
        int offset = (text2.length() > 0 && ( text2.charAt(0) == '"' || text2.charAt(0) == '\''))? 1:0;
        result = ((SubstitutionHandler)handler).handle(another,offset,text2.length()-offset,matchContext);
      } else {
        result = text.equals(StringUtil.stripQuotesAroundValue(another.getText()));
      }
    }

    @Override public void visitXmlTag(XmlTag tag) {
      final XmlTag another = (XmlTag)element;
      final boolean isTypedVar = matchContext.getPattern().isTypedVar(tag.getName());

      result = (tag.getName().equals(another.getName()) || isTypedVar) &&
               matchInAnyOrder(tag.getAttributes(),another.getAttributes());

      if(result && tag.getValue()!=null) {
        final XmlTagChild[] contentChildren = tag.getValue().getChildren();

        if (contentChildren != null && contentChildren.length > 0) {
          result = matchSequentially(
            new ArrayBackedNodeIterator(contentChildren),
            new ArrayBackedNodeIterator(another.getValue()!=null ? another.getValue().getChildren():XmlTagChild.EMPTY_ARRAY)
          );
        }
      }

      if (result && isTypedVar) {
        MatchingHandler handler = matchContext.getPattern().getHandler( tag.getName() );
        result = ((SubstitutionHandler)handler).handle(another,matchContext);
      }
    }

    @Override public void visitXmlText(XmlText text) {
      result = matchSequentially(text.getFirstChild(),element.getFirstChild());
    }

    @Override public void visitXmlToken(XmlToken token) {
      if (token.getTokenType() == XmlTokenType.XML_DATA_CHARACTERS) {
        String text = token.getText();
        final boolean isTypedVar = matchContext.getPattern().isTypedVar(text);

        if (isTypedVar) {
          result = handleTypedElement(token, element);
        } else {
          result = text.equals(element.getText());
        }
      }
    }
  }
}