package com.intellij.structuralsearch.plugin.replace.impl;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.idea.LoggerFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.structuralsearch.*;
import com.intellij.structuralsearch.impl.matcher.MatcherImplUtil;
import com.intellij.structuralsearch.impl.matcher.predicates.ScriptSupport;
import com.intellij.structuralsearch.plugin.replace.ReplaceOptions;
import com.intellij.structuralsearch.plugin.replace.ReplacementInfo;
import com.intellij.structuralsearch.plugin.util.CollectingMatchResultSink;
import com.intellij.util.IncorrectOperationException;

import java.util.*;

/**
 * @author Maxim.Mossienko
 * Date: Mar 4, 2004
 * Time: 9:19:34 PM
 */
public class ReplacerImpl {
  private final Project project;
  private ReplacementBuilder replacementBuilder;
  private ReplaceOptions options;
  private ReplacementContext context;

  protected ReplacerImpl(Project project, ReplaceOptions options) {
    this.project = project;
    this.options = options;
  }

  protected String testReplace(String in, String what, String by, ReplaceOptions options,boolean filePattern) {
    this.options = options;
    this.options.getMatchOptions().setSearchPattern(what);
    this.options.setReplacement(by);
    replacementBuilder=null;
    context = null;

    this.options.getMatchOptions().clearVariableConstraints();
    MatcherImplUtil.transform(this.options.getMatchOptions());

    checkSupportedReplacementPattern(project, options);

    Matcher matcher = new Matcher(project);
    try {
      PsiElement firstElement, lastElement, parent;

      if (options.getMatchOptions().getScope() == null) {
        PsiElement[] elements = MatcherImplUtil.createTreeFromText(
          in,
          filePattern ? MatcherImplUtil.TreeContext.File : MatcherImplUtil.TreeContext.Block,
          this.options.getMatchOptions().getFileType(),
          project
        );

        firstElement = elements[0];
        lastElement = elements[elements.length-1];
        parent = firstElement.getParent();

        this.options.getMatchOptions().setScope(
          new LocalSearchScope(parent)
        );
      } else {
        parent = ((LocalSearchScope)options.getMatchOptions().getScope()).getScope()[0];
        firstElement = parent.getFirstChild();
        lastElement = parent.getLastChild();
      }

      this.options.getMatchOptions().setResultIsContextMatch(true);
      CollectingMatchResultSink sink = new CollectingMatchResultSink();

      matcher.testFindMatches(sink,this.options.getMatchOptions());

      final List<ReplacementInfo> resultPtrList = new LinkedList<ReplacementInfo>();

      for (final MatchResult result : sink.getMatches()) {
        resultPtrList.add(buildReplacement(result));
      }

      sink.getMatches().clear();

      int startOffset = firstElement.getTextRange().getStartOffset();
      int endOffset = filePattern ?0: parent.getTextLength() - (lastElement.getTextRange().getEndOffset());

      // get nodes from text may contain
      PsiElement prevSibling = firstElement.getPrevSibling();
      if (prevSibling instanceof PsiWhiteSpace) {
        startOffset -= prevSibling.getTextLength() - 1;
      }

      PsiElement nextSibling = lastElement.getNextSibling();
      if (nextSibling instanceof PsiWhiteSpace) {
        endOffset -= nextSibling.getTextLength() - 1;
      }

      replaceAll(resultPtrList);

      String result = parent.getText();
      result = result.substring(startOffset);
      result = result.substring(0,result.length() - endOffset);

      return result;
    } catch(Exception ex) {
      throw new IncorrectOperationException("Unexpected failure:", ex);
    } finally {
      options.getMatchOptions().setScope(null);
    }
  }

  protected void replaceAll(final List<ReplacementInfo> resultPtrList) {
    PsiElement lastAffectedElement = null;
    PsiElement currentAffectedElement;

    for (final ReplacementInfo aResultPtrList : resultPtrList) {
      currentAffectedElement = doReplace(aResultPtrList);

      if (currentAffectedElement != lastAffectedElement) {
        if (lastAffectedElement != null) reformatAndShortenRefs(lastAffectedElement);
        lastAffectedElement = currentAffectedElement;
      }
    }

    reformatAndShortenRefs(lastAffectedElement);
  }

  protected void replace(ReplacementInfo info) {
    reformatAndShortenRefs(doReplace(info));
  }

  private PsiElement doReplace(final ReplacementInfo info) {
    final ReplacementInfoImpl replacementInfo = (ReplacementInfoImpl)info;
    final PsiElement element = replacementInfo.matchesPtrList.get(0).getElement();
    final String replacement = replacementInfo.result;

    if (element==null || !element.isWritable() || !element.isValid()) return null;

    final PsiElement elementParent = element.getParent();

    //noinspection HardCodedStringLiteral
    CommandProcessor.getInstance().executeCommand(
      project,
      new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(
            new Runnable() {
              public void run() {
                doReplace(element, replacement, replacementInfo, elementParent);
              }
            }
          );
          PsiDocumentManager.getInstance(project).commitAllDocuments();
        }
      },
      "ssreplace",
      "test"
    );

    return elementParent;
  }

  private void reformatAndShortenRefs(final PsiElement elementParent) {
    if (elementParent == null) return;
    final Runnable action = new Runnable() {
      public void run() {
        CodeStyleManager codeStyleManager = PsiManager.getInstance(project).getCodeStyleManager();
        final PsiFile containingFile = elementParent.getContainingFile();

        if (containingFile != null) {

          if (options.isToShortenFQN()) {
            if (containingFile.getVirtualFile() != null) {
              PsiDocumentManager.getInstance(project)
                .commitDocument(FileDocumentManager.getInstance().getDocument(containingFile.getVirtualFile()));
            }

            JavaCodeStyleManager.getInstance(project).shortenClassReferences(elementParent, 0, elementParent.getTextLength());
          }

          if (options.isToReformatAccordingToStyle()) {
            if (containingFile.getVirtualFile() != null) {
              PsiDocumentManager.getInstance(project)
                .commitDocument(FileDocumentManager.getInstance().getDocument(containingFile.getVirtualFile()));
            }

            final int paretOffset = elementParent.getTextRange().getStartOffset();

            codeStyleManager.reformatRange(containingFile, paretOffset, paretOffset + elementParent.getTextLength(), true);
          }
        }
      }
    };

    CommandProcessor.getInstance().executeCommand(
      project,
      new Runnable() {
        public void run() {
          ApplicationManager.getApplication().runWriteAction(action);
        }
      },
      "reformat and shorten refs after ssr",
      "test"
    );
  }

  private void doReplace(final PsiElement elementToReplace,
                         final String replacementToMake,
                         final ReplacementInfoImpl info,
                         final PsiElement elementParent) {
    PsiManager.getInstance(project).performActionWithFormatterDisabled(
      new Runnable() {
        public void run() {
          doReplacement(info, elementToReplace, replacementToMake, elementParent);
        }
      }
    );
  }

  private void doReplacement(final ReplacementInfoImpl info,
                             final PsiElement elementToReplace,
                             String replacementToMake,
                             final PsiElement elementParent) {
    boolean listContext = false;

    if (context == null) context = new ReplacementContext(options, project);
    context.replacementInfo = info;

    PsiElement el = findRealSubstitutionElement(elementToReplace);
    listContext = isListContext(el);

    if (el instanceof PsiAnnotation && !StringUtil.startsWithChar(replacementToMake, '@'))  {
      replacementToMake = "@" + replacementToMake;
    }

    PsiElement[] statements = MatcherImplUtil.createTreeFromText(
      replacementToMake,
      el instanceof PsiMember && !isSymbolReplacement(el, context)?
      MatcherImplUtil.TreeContext.Class :
      MatcherImplUtil.TreeContext.Block,
      options.getMatchOptions().getFileType(),
      project
    );


    if (listContext) {
      if (statements.length > 1) {
        elementParent.addRangeBefore(statements[0],statements[statements.length-1],elementToReplace);
      } else if (statements.length==1) {
        PsiElement replacement = getMatchExpr(statements[0], elementToReplace);

        handleModifierList(el, replacement, context);
        replacement = handleSymbolReplacemenent(replacement, el, context);

        if (replacement instanceof PsiTryStatement) {
          final List<PsiCatchSection> unmatchedCatchSections = el.getUserData(MatcherImplUtil.UNMATCHED_CATCH_SECTION_CONTENT_VAR_KEY);
          final PsiCatchSection[] catches = ((PsiTryStatement)replacement).getCatchSections();

          if (unmatchedCatchSections!=null) {
            for(int i = unmatchedCatchSections.size()-1; i >= 0; --i) {
              final PsiParameter parameter = unmatchedCatchSections.get(i).getParameter();
              final PsiCatchSection catchSection = JavaPsiFacade.getInstance(project).getElementFactory().createCatchSection(
                (PsiClassType)parameter.getType(),
                parameter.getName(),
                null
              );

              catchSection.getCatchBlock().replace(
                unmatchedCatchSections.get(i).getCatchBlock()
              );
              replacement.addAfter(
                catchSection, catches[catches.length-1]
              );
              replacement.addBefore(createWhiteSpace(replacement), replacement.getLastChild());
            }
          }
        }

        try {
          final PsiElement inserted = elementParent.addBefore(replacement,elementToReplace);

          if (replacement instanceof PsiComment &&
               ( elementParent instanceof PsiIfStatement ||
                 elementParent instanceof PsiLoopStatement
               )
              ) {
            elementParent.addAfter(createSemicolon(replacement),inserted);
          }
        }
        catch (IncorrectOperationException e) {
          elementToReplace.replace(replacement);
        }
      }
    } else if (statements.length > 0) {
      int i = 0;
      while( true ) {    // if it goes out of bounds then deep error happens
        if (!(statements[i] instanceof PsiComment ||
              statements[i] instanceof PsiWhiteSpace
        )
          ) {
          break;
        }
        ++i;
        if (statements.length == i) {
          Logger logger = LoggerFactory.getInstance().getLoggerInstance(getClass().getName());
          logger.error("Unexpected replacement structure:" + replacementToMake);
          break;
        }
      }

      if (i != 0) {
        elementParent.addRangeBefore(statements[0],statements[i-1],el);
      }
      PsiElement replacement = getMatchExpr(statements[i], elementToReplace);

      if (replacement instanceof PsiStatement &&
          !(replacement.getLastChild() instanceof PsiJavaToken) &&
          !(replacement.getLastChild() instanceof PsiComment)
         ) {
        // assert w/o ;
        final PsiElement prevLastChildInParent = replacement.getLastChild().getPrevSibling();

        if (prevLastChildInParent != null) {
          elementParent.addRangeBefore(replacement.getFirstChild(), prevLastChildInParent,el);
        } else {
          elementParent.addBefore(replacement.getFirstChild(), el);
        }

        el.getNode().getTreeParent().removeChild(el.getNode());
      } else {
        // preserve comments
        handleModifierList(el, replacement, context);

        if (replacement instanceof PsiClass) {
          // modifier list
          final PsiStatement[] searchStatements = context.getCodeBlock().getStatements();
          if (searchStatements.length > 0 &&
              searchStatements[0] instanceof PsiDeclarationStatement &&
              ((PsiDeclarationStatement)searchStatements[0]).getDeclaredElements()[0] instanceof PsiClass
             ) {
            final PsiClass replaceClazz = (PsiClass)replacement;
            final PsiClass queryClazz = (PsiClass)((PsiDeclarationStatement)searchStatements[0]).getDeclaredElements()[0];
            final PsiClass clazz = (PsiClass)el;

            if (replaceClazz.getExtendsList().getTextLength() == 0 &&
                queryClazz.getExtendsList().getTextLength() == 0 &&
                clazz.getExtendsList().getTextLength() != 0
                ) {
              replaceClazz.addBefore(clazz.getExtendsList().getPrevSibling(),replaceClazz.getExtendsList()); // whitespace
              replaceClazz.getExtendsList().addRange(
                clazz.getExtendsList().getFirstChild(),clazz.getExtendsList().getLastChild()
              );
            }

            if (replaceClazz.getImplementsList().getTextLength() == 0 &&
                queryClazz.getImplementsList().getTextLength() == 0 &&
                clazz.getImplementsList().getTextLength() != 0
                ) {
              replaceClazz.addBefore(clazz.getImplementsList().getPrevSibling(),replaceClazz.getImplementsList()); // whitespace
              replaceClazz.getImplementsList().addRange(
                clazz.getImplementsList().getFirstChild(),
                clazz.getImplementsList().getLastChild()
              );
            }

            if (replaceClazz.getTypeParameterList().getTextLength() == 0 &&
                queryClazz.getTypeParameterList().getTextLength() == 0 &&
                clazz.getTypeParameterList().getTextLength() != 0
                ) {
              // skip < and >
              replaceClazz.getTypeParameterList().replace(
                clazz.getTypeParameterList()
              );
            }
          }
        }

        replacement = handleSymbolReplacemenent(replacement, el, context);

        el.replace(replacement);
      }
    } else {
      final PsiElement nextSibling = el.getNextSibling();
      el.delete();
      if(nextSibling.isValid()){
        if (nextSibling instanceof PsiWhiteSpace) {
          nextSibling.delete();
        }
      }
    }

    if (listContext) {
      final int matchSize = info.matchesPtrList.size();

      for (int i = 0; i < matchSize; ++i) {
        SmartPsiElementPointer aMatchesPtrList = info.matchesPtrList.get(i);
        PsiElement element = findRealSubstitutionElement(
          aMatchesPtrList.getElement()
        );

        if (element == null) continue;
        PsiElement firstToDelete = element;
        PsiElement lastToDelete = element;
        PsiElement prevSibling = element.getPrevSibling();
        PsiElement nextSibling = element.getNextSibling();

        if (prevSibling instanceof PsiWhiteSpace) {
          firstToDelete = prevSibling;
          prevSibling = prevSibling != null ? prevSibling.getPrevSibling(): null;
        } else if (prevSibling == null && nextSibling instanceof PsiWhiteSpace) {
          lastToDelete = nextSibling;
        }

        if (nextSibling instanceof XmlText && i + 1 < matchSize) {
          final PsiElement next = info.matchesPtrList.get(i + 1).getElement();
          if (next != null && next == nextSibling.getNextSibling()) {
            lastToDelete = nextSibling;
          }
        }

        if (element instanceof PsiExpression) {
          final PsiElement parent = element.getParent().getParent();
          if ((parent instanceof PsiCall ||
               parent instanceof PsiAnonymousClass
              ) &&
              prevSibling instanceof PsiJavaToken &&
              ((PsiJavaToken)prevSibling).getTokenType() == JavaTokenType.COMMA
             ) {
            firstToDelete = prevSibling;
          }
        } else if (element instanceof PsiParameter &&
                   prevSibling instanceof PsiJavaToken &&
              ((PsiJavaToken)prevSibling).getTokenType() == JavaTokenType.COMMA
          ) {
          firstToDelete = prevSibling;
        }

        element.getParent().deleteChildRange(firstToDelete,lastToDelete);
      }
    }
  }

  private static PsiNamedElement getSymbolReplacementTarget(final PsiElement el,ReplacementContext context) throws IncorrectOperationException {
    if (context.options.getMatchOptions().getFileType() != StdFileTypes.JAVA) return null; //?
    final PsiStatement[] searchStatements = context.getCodeBlock().getStatements();
    if (searchStatements.length > 0 &&
        searchStatements[0] instanceof PsiExpressionStatement) {
      final PsiExpression expression = ((PsiExpressionStatement)searchStatements[0]).getExpression();

      if (expression instanceof PsiReferenceExpression &&
          ((PsiReferenceExpression)expression).getQualifierExpression() == null
         ) {
        // looks like symbol replacements, namely replace AAA by BBB, so lets do the best
        if (el instanceof PsiNamedElement) {
          return (PsiNamedElement)el;
        }
      }
    }

    return null;
  }
  private static boolean isSymbolReplacement(final PsiElement el,ReplacementContext context) throws IncorrectOperationException {
    return getSymbolReplacementTarget(el, context) != null;
  }

  private static PsiElement handleSymbolReplacemenent(PsiElement replacement,
                                                      final PsiElement el,
                                                      ReplacementContext context) throws IncorrectOperationException {
    PsiNamedElement nameElement = getSymbolReplacementTarget(el, context);
    if (nameElement != null) {
      PsiElement oldReplacement = replacement;
      replacement = el.copy();
      ((PsiNamedElement)replacement).setName(oldReplacement.getText());
    }

    return replacement;
  }

  private static void handleComments(final PsiElement el, final PsiElement replacement, ReplacementContext context) throws IncorrectOperationException {
    ReplacementInfoImpl replacementInfo = context.replacementInfo;
    if (replacementInfo.elementToVariableNameMap == null) {
      replacementInfo.elementToVariableNameMap = new HashMap<PsiElement, String>(1);
      Map<String, MatchResult> variableMap = replacementInfo.variableMap;
      if (variableMap != null) {
        for(String name:variableMap.keySet()) {
          fill(name,replacementInfo.variableMap.get(name),replacementInfo.elementToVariableNameMap);
        }
      }
    }

    PsiElement lastChild = el.getLastChild();
    if (lastChild instanceof PsiComment &&
        replacementInfo.elementToVariableNameMap.get(lastChild) == null &&
        !(replacement.getLastChild() instanceof PsiComment)
      ) {
      PsiElement firstElementAfterStatementEnd = lastChild;
      for(PsiElement curElement=firstElementAfterStatementEnd.getPrevSibling();curElement!=null;curElement = curElement.getPrevSibling()) {
        if (!(curElement instanceof PsiWhiteSpace) && !(curElement instanceof PsiComment)) break;
        firstElementAfterStatementEnd = curElement;
      }
      replacement.addRangeAfter(firstElementAfterStatementEnd,lastChild,replacement.getLastChild());
    }

    final PsiElement firstChild = el.getFirstChild();
    if (firstChild instanceof PsiComment &&
        !(firstChild instanceof PsiDocComment) &&
        replacementInfo.elementToVariableNameMap.get(firstChild) == null
        ) {
      PsiElement lastElementBeforeStatementStart = firstChild;

      for(PsiElement curElement=lastElementBeforeStatementStart.getNextSibling();curElement!=null;curElement = curElement.getNextSibling()) {
        if (!(curElement instanceof PsiWhiteSpace) && !(curElement instanceof PsiComment)) break;
        lastElementBeforeStatementStart = curElement;
      }
      replacement.addRangeBefore(firstChild,lastElementBeforeStatementStart,replacement.getFirstChild());
    }
  }

  private static void fill(final String name, final MatchResult matchResult, final Map<PsiElement, String> elementToVariableNameMap) {
    boolean b = matchResult.isMultipleMatch() || matchResult.isScopeMatch();
    if(matchResult.hasSons() && b) {
      for(MatchResult r:matchResult.getAllSons()) {
        fill(name, r, elementToVariableNameMap);
      }
    } else if (!b && matchResult.getMatchRef() != null)  {
      elementToVariableNameMap.put(matchResult.getMatch(),name);
    }
  }

  static class ModifierListOwnerCollector extends JavaRecursiveElementWalkingVisitor {
    HashMap<String,PsiNamedElement> namedElements = new HashMap<String,PsiNamedElement>(1);

    @Override public void visitClass(PsiClass aClass) {
      if (aClass instanceof PsiAnonymousClass) return;
      handleNamedElement(aClass);
    }

    private void handleNamedElement(final PsiNamedElement named) {
      String name = named.getName();

      if (ReplacementBuilder.isTypedVariable(name)) {
        name = name.substring(1,name.length()-1);
      }

      if (!namedElements.containsKey(name)) namedElements.put(name,named);
      named.acceptChildren(this);
    }

    @Override public void visitVariable(PsiVariable var) {
      handleNamedElement(var);
    }

    @Override public void visitMethod(PsiMethod method) {
      handleNamedElement(method);
    }
  }

  private static void handleModifierList(final PsiElement el,
                                         final PsiElement replacement,
                                         final ReplacementContext context) throws IncorrectOperationException {
    // We want to copy all comments, including doc comments and modifier lists
    // that are present in matched nodes but not present in search/replace
    Map<String,String> newNameToSearchPatternNameMap = new HashMap<String, String>(1);
    final Map<String, MatchResult> variableMap = context.replacementInfo.variableMap;

    if (variableMap != null) {
      for(String s:variableMap.keySet()) {
        final MatchResult matchResult = context.replacementInfo.variableMap.get(s);
        PsiElement match = matchResult.getMatchRef() != null? matchResult.getMatch():null;
        if (match instanceof PsiIdentifier) match = match.getParent();

        if (match instanceof PsiNamedElement) {
          final String name = ((PsiNamedElement)match).getName();

          newNameToSearchPatternNameMap.put(name,s);
        }
      }
    }

    ModifierListOwnerCollector collector = new ModifierListOwnerCollector();
    el.accept( collector );
    Map<String,PsiNamedElement> originalNamedElements = (Map<String, PsiNamedElement>)collector.namedElements.clone();
    collector.namedElements.clear();

    replacement.accept( collector );
    Map<String,PsiNamedElement> replacedNamedElements = (Map<String, PsiNamedElement>)collector.namedElements.clone();
    collector.namedElements.clear();

    if (originalNamedElements.size() == 0 && replacedNamedElements.size() == 0) {
      handleComments(el, replacement,context);
      return;
    }

    final PsiStatement[] statements = context.getCodeBlock().getStatements();
    if (statements.length > 0) {
      statements[0].getParent().accept(collector);
    }

    Map<String,PsiNamedElement> searchedNamedElements = (Map<String, PsiNamedElement>)collector.namedElements.clone();
    collector.namedElements.clear();

    for(String name:originalNamedElements.keySet()) {
      PsiNamedElement originalNamedElement = originalNamedElements.get(name);
      PsiNamedElement replacementNamedElement = replacedNamedElements.get(name);
      String key = newNameToSearchPatternNameMap.get(name);
      if (key == null) key = name;
      PsiNamedElement searchNamedElement = searchedNamedElements.get(key);

      if (replacementNamedElement == null && originalNamedElements.size() == 1 && replacedNamedElements.size() == 1) {
        replacementNamedElement = replacedNamedElements.entrySet().iterator().next().getValue();
      }

      PsiElement comment = null;

      if (originalNamedElement instanceof PsiDocCommentOwner) {
        comment = ((PsiDocCommentOwner)originalNamedElement).getDocComment();
        if (comment == null) {
          PsiElement prevElement = originalNamedElement.getPrevSibling();
          if (prevElement instanceof PsiWhiteSpace) {
            prevElement = prevElement.getPrevSibling();
          }
          if (prevElement instanceof PsiComment) {
            comment = prevElement;
          }
        }
      }

      if (replacementNamedElement != null && searchNamedElement != null) {
        handleComments(originalNamedElement, replacementNamedElement,context);
      }

      if (comment!=null && replacementNamedElement instanceof PsiDocCommentOwner &&
          !(replacementNamedElement.getFirstChild() instanceof PsiDocComment)
         ) {
        final PsiElement nextSibling = comment.getNextSibling();
        PsiElement prevSibling = comment.getPrevSibling();
        replacementNamedElement.addRangeBefore(
          prevSibling instanceof PsiWhiteSpace ? prevSibling:comment,
          nextSibling instanceof PsiWhiteSpace ? nextSibling:comment,
          replacementNamedElement.getFirstChild()
        );
      }

      if (originalNamedElement instanceof PsiModifierListOwner &&
          replacementNamedElement instanceof PsiModifierListOwner
         )  {
        PsiModifierList modifierList = ((PsiModifierListOwner)originalNamedElements.get(name)).getModifierList();

        if (searchNamedElement instanceof PsiModifierListOwner &&
            ((PsiModifierListOwner)searchNamedElement).getModifierList().getTextLength() == 0 &&
            ((PsiModifierListOwner)replacementNamedElement).getModifierList().getTextLength() == 0 &&
            modifierList.getTextLength() > 0
           ) {
          final PsiModifierListOwner modifierListOwner = ((PsiModifierListOwner)replacementNamedElement);
          PsiElement space = modifierList.getNextSibling();
          if (!(space instanceof PsiWhiteSpace)) {
            space = createWhiteSpace(space);
          }

          modifierListOwner.getModifierList().replace(
            modifierList
          );
          // copy space after modifier list
          if (space instanceof PsiWhiteSpace) {
            modifierListOwner.addRangeAfter(space,space,modifierListOwner.getModifierList());
          }
        }
      }
    }
  }

  private static PsiElement createWhiteSpace(final PsiElement space) throws IncorrectOperationException {
    return JavaPsiFacade.getInstance(space.getProject()).getElementFactory().createWhiteSpaceFromText(" ");
  }

  private static PsiElement createSemicolon(final PsiElement space) throws IncorrectOperationException {
    final PsiStatement text = JavaPsiFacade.getInstance(space.getProject()).getElementFactory().createStatementFromText(";", null);
    return text.getFirstChild();
  }

  public static void checkSupportedReplacementPattern(Project project, ReplaceOptions options) throws UnsupportedPatternException {
    try {
      String search = options.getMatchOptions().getSearchPattern();
      String replacement = options.getReplacement();
      FileType fileType = options.getMatchOptions().getFileType();
      Template template = TemplateManager.getInstance(project).createTemplate("","",search);
      Template template2 = TemplateManager.getInstance(project).createTemplate("","",replacement);

      int segmentCount = template2.getSegmentsCount();
      for(int i=0;i<segmentCount;++i) {
        final String replacementSegmentName = template2.getSegmentName(i);
        final int segmentCount2  = template.getSegmentsCount();
        int j;

        for(j=0;j<segmentCount2;++j) {
          final String searchSegmentName = template.getSegmentName(j);

          if (replacementSegmentName.equals(searchSegmentName)) break;

          // Reference to
          if (replacementSegmentName.startsWith(searchSegmentName) &&
              replacementSegmentName.charAt(searchSegmentName.length())=='_'
             ) {
            try {
              Integer.parseInt(replacementSegmentName.substring(searchSegmentName.length()+1));
              break;
            } catch(NumberFormatException ex) {}
          }
        }

        if (j==segmentCount2) {
          ReplacementVariableDefinition definition = options.getVariableDefinition(replacementSegmentName);

          if (definition == null || definition.getScriptCodeConstraint().length() <= 2 /*empty quotes*/) {
            throw new UnsupportedPatternException(
              SSRBundle.message("replacement.variable.is.not.defined.message", replacementSegmentName)
            );
          } else {
            String message = ScriptSupport.checkValidScript(StringUtil.stripQuotesAroundValue(definition.getScriptCodeConstraint()));
            if (message != null) {
              throw new UnsupportedPatternException(
                SSRBundle.message("replacement.variable.is.not.valid", replacementSegmentName, message)
              );
            }
          }
        }
      }

      if (fileType==StdFileTypes.JAVA) {
        PsiElement[] statements = MatcherImplUtil.createTreeFromText(
          search,
          MatcherImplUtil.TreeContext.Block,
          fileType,
          project
        );
        boolean searchIsExpression = false;

        for (PsiElement statement : statements) {
          if (statement.getLastChild() instanceof PsiErrorElement) {
            searchIsExpression = true;
            break;
          }
        }

        PsiElement[] statements2 = MatcherImplUtil.createTreeFromText(
          replacement,
          MatcherImplUtil.TreeContext.Block,
          fileType,
          project
        );
        boolean replaceIsExpression = false;

        for (PsiElement statement : statements2) {
          if (statement.getLastChild() instanceof PsiErrorElement) {
            replaceIsExpression = true;
            break;
          }
        }

        if (searchIsExpression!=replaceIsExpression) {
          throw new UnsupportedPatternException(
            searchIsExpression ? SSRBundle.message("replacement.template.is.not.expression.error.message") :
            SSRBundle.message("search.template.is.not.expression.error.message")
          );
        }
      }
    } catch(IncorrectOperationException ex) {
      throw new UnsupportedPatternException(SSRBundle.message("incorrect.pattern.message"));
    }
  }

  private static PsiElement getMatchExpr(PsiElement replacement, PsiElement elementToReplace) {
    if (replacement instanceof PsiExpressionStatement &&
        !(replacement.getLastChild() instanceof PsiJavaToken) &&
        !(replacement.getLastChild() instanceof PsiComment)
       ) {
      // replacement is expression (and pattern should be so)
      // assert ...
      replacement = ((PsiExpressionStatement)replacement).getExpression();
    } else if (replacement instanceof PsiDeclarationStatement &&
               ((PsiDeclarationStatement)replacement).getDeclaredElements().length==1
               ) {
      return ((PsiDeclarationStatement)replacement).getDeclaredElements()[0];
    } else if (replacement instanceof PsiBlockStatement &&
               elementToReplace instanceof PsiCodeBlock
              ) {
      return ((PsiBlockStatement)replacement).getCodeBlock();
    }

    return replacement;
  }

  private static boolean isListContext(PsiElement el) {
    boolean listContext = false;
    final PsiElement parent = el.getParent();

    if (parent instanceof PsiParameterList ||
        parent instanceof PsiExpressionList ||
        parent instanceof PsiCodeBlock ||
        parent instanceof XmlTag ||
        parent instanceof PsiClass ||
        ( parent instanceof PsiIfStatement &&
          ( ((PsiIfStatement)parent).getThenBranch() == el ||
            ((PsiIfStatement)parent).getElseBranch() == el
          )
        ) ||
        ( parent instanceof PsiLoopStatement &&
          ((PsiLoopStatement)parent).getBody() == el
        )
       ) {
      listContext = true;
    }

    return listContext;
  }

  private static PsiElement findRealSubstitutionElement(PsiElement el) {
    if (el instanceof PsiIdentifier) {
        // matches are tokens, identifiers, etc
      el = el.getParent();
    }

    if (el instanceof PsiReferenceExpression &&
        el.getParent() instanceof PsiMethodCallExpression
       ) {
      // method
      el = el.getParent();
    }

    if (el instanceof PsiDeclarationStatement && ((PsiDeclarationStatement)el).getDeclaredElements()[0] instanceof PsiClass) {
      el = ((PsiDeclarationStatement)el).getDeclaredElements()[0];
    }
    return el;
  }

  protected ReplacementInfo buildReplacement(MatchResult result) {
    List<SmartPsiElementPointer> l = new ArrayList<SmartPsiElementPointer>();
    SmartPointerManager manager = SmartPointerManager.getInstance(project);

    if (MatchResult.MULTI_LINE_MATCH.equals(result.getName())) {
      for(Iterator<MatchResult> i=result.getAllSons().iterator();i.hasNext();) {
        final MatchResult r = i.next();

        if (MatchResult.LINE_MATCH.equals(r.getName())) {
          PsiElement element = r.getMatchRef().getElement();

          if (element instanceof PsiDocComment) { // doc comment is not collapsed when created in block
            if (i.hasNext()) {
              MatchResult matchResult = i.next();

              if (MatchResult.LINE_MATCH.equals(matchResult.getName()) &&
                  matchResult.getMatch() instanceof PsiMember
                 ) {
                element = matchResult.getMatch();
              } else {
                l.add( manager.createSmartPsiElementPointer(element) );
                element = matchResult.getMatch();
              }
            }
          }
          l.add( manager.createSmartPsiElementPointer(element) );
        }
      }
    } else {
      l.add( manager.createSmartPsiElementPointer(result.getMatchRef().getElement()));
    }

    ReplacementInfoImpl replacementInfo = new ReplacementInfoImpl();

    replacementInfo.matchesPtrList = l;
    if (replacementBuilder==null) {
      replacementBuilder = new ReplacementBuilder(project,options);
    }
    replacementInfo.result = replacementBuilder.process(result,replacementInfo);
    replacementInfo.matchResult = result;

    return replacementInfo;
  }
}