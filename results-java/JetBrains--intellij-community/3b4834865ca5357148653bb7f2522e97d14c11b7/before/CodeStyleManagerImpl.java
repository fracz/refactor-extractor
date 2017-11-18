package com.intellij.psi.impl.source.codeStyle;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.newCodeFormatting.Formatter;
import com.intellij.newCodeFormatting.FormattingModel;
import com.intellij.newCodeFormatting.FormattingModelBuilder;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.*;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.parsing.ChameleonTransforming;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.text.BlockSupport;
import com.intellij.psi.tree.jsp.IJspElementType;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CharTable;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.text.CharArrayUtil;

import java.beans.Introspector;
import java.util.*;

import org.jetbrains.annotations.Nullable;

public class CodeStyleManagerImpl extends CodeStyleManagerEx implements ProjectComponent {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl");

  private Project myProject;

  private StatisticsManagerEx myStatisticsManager;

  public CodeStyleManagerImpl(Project project, StatisticsManagerEx statisticsManagerEx) {
    myProject = project;

    myStatisticsManager =
    statisticsManagerEx;
  }

  public String getComponentName() {
    return "CodeStyleManager";
  }

  public void initComponent() { }

  public void disposeComponent() { }

  public void projectOpened() { }

  public void projectClosed() { }

  public Project getProject() {
    return myProject;
  }

  public PsiElement reformat(PsiElement element) throws IncorrectOperationException {
    CheckUtil.checkWritable(element);
    if (!SourceTreeToPsiMap.hasTreeElement(element)) return element;

    ASTNode treeElement = SourceTreeToPsiMap.psiElementToTree(element);
    if (treeElement instanceof CompositeElement) {
      ChameleonTransforming.transformChildren(treeElement, true); // optimization : parse all first
    }
    PsiFileImpl file = (PsiFileImpl)element.getContainingFile();
    FileType fileType = StdFileTypes.JAVA;
    if (file != null) {
      fileType = file.getFileType();
    }
    Helper helper = new Helper(fileType, myProject);
    final PsiElement formatted = SourceTreeToPsiMap.treeElementToPsi(
      new CodeFormatterFacade(getSettings(), helper).process(treeElement, -1));
    return new BraceEnforcer(getSettings()).process(formatted);
  }

  public PsiElement reformatRange(PsiElement element,
                                  int startOffset,
                                  int endOffset,
                                  boolean canChangeWhiteSpacesOnly) throws IncorrectOperationException {
     return reformatRangeImpl(element, startOffset, endOffset, canChangeWhiteSpacesOnly);
  }

  public PsiElement reformatRange(PsiElement element, int startOffset, int endOffset)
    throws IncorrectOperationException {
    return reformatRangeImpl(element, startOffset, endOffset, false);

  }

  private PsiElement reformatRangeImpl(final PsiElement element,
                                       final int startOffset,
                                       final int endOffset,
                                       boolean canChangeWhiteSpacesOnly) throws IncorrectOperationException {
    CheckUtil.checkWritable(element);
    if (!SourceTreeToPsiMap.hasTreeElement(element)) return element;

    ASTNode treeElement = SourceTreeToPsiMap.psiElementToTree(element);
    if (treeElement instanceof CompositeElement) {
      ChameleonTransforming.transformChildren(treeElement, true); // optimization : parse all first
    }
    FileType fileType = StdFileTypes.JAVA;
    PsiFile file = element.getContainingFile();
    if (file != null) {
      fileType = file.getFileType();
    }
    Helper helper = new Helper(fileType, myProject);
    final CodeFormatterFacade codeFormatter = new CodeFormatterFacade(getSettings(), helper);
    final PsiElement start = element.getContainingFile().findElementAt(startOffset);
    final PsiElement end = element.getContainingFile().findElementAt(endOffset);

    final SmartPsiElementPointer startPointer = start == null ? null : SmartPointerManager.getInstance(getProject())
      .createSmartPsiElementPointer(start);

    final SmartPsiElementPointer endPointer = end == null ? null : SmartPointerManager.getInstance(getProject())
      .createSmartPsiElementPointer(end);

    final PsiElement formatted = SourceTreeToPsiMap.treeElementToPsi(codeFormatter.processRange(treeElement, startOffset, endOffset));
    final PsiElement startElement = startPointer == null ? null : startPointer.getElement();
    final PsiElement endElement = endPointer == null ? null : endPointer.getElement();

    if (!canChangeWhiteSpacesOnly && startElement != null && endElement != null) {
      return new BraceEnforcer(getSettings()).process(formatted,
                                                      startElement.getTextRange().getStartOffset(),
                                                      endElement.getTextRange().getEndOffset());
    } else {
      return formatted;
    }
  }

  public PsiElement shortenClassReferences(PsiElement element) throws IncorrectOperationException {
    return shortenClassReferences(element, 0);
  }

  public PsiElement shortenClassReferences(PsiElement element, int flags) throws IncorrectOperationException {
    CheckUtil.checkWritable(element);
    if (!SourceTreeToPsiMap.hasTreeElement(element)) return element;

    boolean addImports = (flags & DO_NOT_ADD_IMPORTS) == 0;
    boolean uncompleteCode = (flags & UNCOMPLETE_CODE) != 0;
    return SourceTreeToPsiMap.treeElementToPsi(
      new ReferenceAdjuster(getSettings()).process((TreeElement)SourceTreeToPsiMap.psiElementToTree(element), addImports,
                                                   uncompleteCode));
  }

  public void shortenClassReferences(PsiElement element, int startOffset, int endOffset)
    throws IncorrectOperationException {
    CheckUtil.checkWritable(element);
    if (!SourceTreeToPsiMap.hasTreeElement(element)) return;
    new ReferenceAdjuster(getSettings()).processRange((TreeElement)SourceTreeToPsiMap.psiElementToTree(element), startOffset,
                                                      endOffset);
  }

  public PsiElement qualifyClassReferences(PsiElement element) {
    return SourceTreeToPsiMap.treeElementToPsi(
      new ReferenceAdjuster(getSettings(), true, true).process((TreeElement)SourceTreeToPsiMap.psiElementToTree(element), false,
                                                               false));
  }

  public void optimizeImports(PsiFile file) throws IncorrectOperationException {
    CheckUtil.checkWritable(file);
    if (file instanceof PsiJavaFile) {
      PsiImportList newList = prepareOptimizeImportsResult(file);
      if (newList != null) {
          ((PsiJavaFile)file).getImportList().replace(newList);
      }
    }
  }

  public PsiImportList prepareOptimizeImportsResult(PsiFile file) {
    if (!(file instanceof PsiJavaFile)) return null;
    return new ImportHelper(getSettings()).prepareOptimizeImportsResult(this, (PsiJavaFile)file);
  }

  public boolean addImport(PsiFile file, PsiClass refClass) {
    return new ImportHelper(getSettings()).addImport(file, refClass);
  }

  public void removeRedundantImports(PsiJavaFile file) throws IncorrectOperationException {
    final PsiImportStatementBase[] imports = file.getImportList().getAllImportStatements();
    if (imports.length == 0) return;

    final Set<PsiImportStatementBase> redundants = new HashSet<PsiImportStatementBase>(Arrays.asList(imports));
    final PsiFile[] roots = file.getPsiRoots();
    for (PsiFile root : roots) {
      root.accept(new PsiRecursiveElementVisitor() {
        public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
          if (!reference.isQualified()) {
            final ResolveResult resolveResult = reference.advancedResolve(false);
            final PsiElement resolveScope = resolveResult.getCurrentFileResolveScope();
            if (resolveScope instanceof PsiImportStatementBase) {
              //noinspection SuspiciousMethodCalls
              redundants.remove(resolveScope);
            }
          }
          super.visitReferenceElement(reference);
        }
      });
    }

    for (final PsiImportStatementBase importStatement : redundants) {
      final PsiJavaCodeReferenceElement ref = importStatement.getImportReference();
      //Do not remove non-resolving refs
      if (ref == null || ref.resolve() == null) continue;

      importStatement.delete();
    }
  }

  public int findEntryIndex(PsiImportStatementBase statement) {
    return new ImportHelper(getSettings()).findEntryIndex(statement);
  }

  public int adjustLineIndent(PsiFile file, int offset) throws IncorrectOperationException {
    final PsiElement element = file.findElementAt(offset);
    if (element == null) return offset;
    if (!(element instanceof PsiWhiteSpace) && insideElement(element, offset)) {
      return CharArrayUtil.shiftForward(file.textToCharArray(), offset, " \t");
    }
    final Language fileLanguage = file.getLanguage();
    LOG.assertTrue(fileLanguage != null, file.getClass().getName());
    final FormattingModelBuilder builder = fileLanguage.getFormattingModelBuilder();
    final Language elementLanguage = element.getLanguage();
    LOG.assertTrue(elementLanguage != null, element.getClass().getName());
    final FormattingModelBuilder elementBuilder = elementLanguage.getFormattingModelBuilder();
    if (builder != null && elementBuilder != null) {
      final CodeStyleSettings settings = getSettings();
      final CodeStyleSettings.IndentOptions indentOptions = settings.getIndentOptions(file.getFileType());
      final TextRange significantRange = getSignificantRange(file, offset);
      final FormattingModel model = builder.createModel(file, settings);

      int result = Formatter.getInstance().adjustLineIndent(model,
                                                            settings,
                                                            indentOptions,
                                                            offset,
                                                            significantRange);
      return result;

    } else {
      return offset;
    }
  }

  private boolean insideElement(final PsiElement element, final int offset) {
    final TextRange textRange = element.getTextRange();
    return textRange.getStartOffset() < offset && textRange.getEndOffset() >= offset;
  }

  private TextRange getSignificantRange(final PsiFile file, final int offset) {
    final ASTNode elementAtOffset = SourceTreeToPsiMap.psiElementToTree(file.findElementAt(offset));
    if (file instanceof PsiJavaFile) {
      ASTNode current = elementAtOffset;
      while (current != null && !isSignificantJavaElement(current)) {
        current = current.getTreeParent();
      }
      if (current == null) {
        return file.getTextRange();
      }
      else {
        return current.getTextRange();
      }
    } else {
      return elementAtOffset.getTextRange();
    }
  }

  private boolean isSignificantJavaElement(final ASTNode current) {
    if (current.getTreeParent() != null && current.getTreeParent().getElementType() == ElementType.CLASS) return true;
    if (current.getElementType() == ElementType.METHOD) return true;
    if (current.getElementType() == ElementType.CLASS) return true;
    if (ElementType.STATEMENT_BIT_SET.isInSet(current.getElementType())) return true;
    return false;
  }

  public boolean isLineToBeIndented(PsiFile file, int offset) {
    if (!SourceTreeToPsiMap.hasTreeElement(file)) return false;
    Helper helper = new Helper(file.getFileType(), myProject);
    char[] chars = file.textToCharArray();
    int start = CharArrayUtil.shiftBackward(chars, offset - 1, " \t");
    if (start > 0 && chars[start] != '\n' && chars[start] != '\r') return false;
    int end = CharArrayUtil.shiftForward(chars, offset, " \t");
    if (end >= chars.length) return false;
    ASTNode element = SourceTreeToPsiMap.psiElementToTree(file.findElementAt(end));
    if (element == null) return false;
    if (element.getElementType() == ElementType.WHITE_SPACE) return false;
    if (element.getElementType() == ElementType.PLAIN_TEXT) return false;
    if (element.getElementType() instanceof IJspElementType) return false;
    if (getSettings().KEEP_FIRST_COLUMN_COMMENT
        && (element.getElementType() == ElementType.END_OF_LINE_COMMENT || element.getElementType() == ElementType.C_STYLE_COMMENT)
    ) {
      if (helper.getIndent(element, true) == 0) return false;
    }
    return true;
  }

  public PsiElement insertNewLineIndentMarker(PsiFile file, int offset) throws IncorrectOperationException {
    CheckUtil.checkWritable(file);
    final CharTable charTable = ((FileElement)SourceTreeToPsiMap.psiElementToTree(file)).getCharTable();
    PsiElement elementAt = file.findElementAt(offset);
    if (elementAt == null) return null;
    ASTNode element = SourceTreeToPsiMap.psiElementToTree(elementAt);
    ASTNode parent = element.getTreeParent();
    int elementStart = element.getTextRange().getStartOffset();
    if (element.getElementType() != ElementType.WHITE_SPACE) {
      /*
      if (elementStart < offset) return null;
      Element marker = Factory.createLeafElement(ElementType.NEW_LINE_INDENT, "###".toCharArray(), 0, "###".length());
      ChangeUtil.addChild(parent, marker, element);
      return marker;
      */
      return null;
    }

    ASTNode space1 = Helper.splitSpaceElement((TreeElement)element, offset - elementStart, charTable);
    ASTNode marker = Factory.createSingleLeafElement(TokenTypeEx.NEW_LINE_INDENT, "xxx".toCharArray(), 0, "xxx".length(), charTable, file.getManager());
    parent.addChild(marker, space1.getTreeNext());
    return SourceTreeToPsiMap.treeElementToPsi(marker);
  }

  public Indent getIndent(String text, FileType fileType) {
    int indent = new Helper(fileType, myProject).getIndent(text, true);
    int indenLevel = indent / Helper.INDENT_FACTOR;
    int spaceCount = indent - indenLevel * Helper.INDENT_FACTOR;
    return new IndentImpl(getSettings(), indenLevel, spaceCount, fileType);
  }

  public String fillIndent(Indent indent, FileType fileType) {
    IndentImpl indent1 = (IndentImpl)indent;
    int indentLevel = indent1.getIndentLevel();
    int spaceCount = indent1.getSpaceCount();
    if (indentLevel < 0) {
      spaceCount += indentLevel * getSettings().getIndentSize(fileType);
      indentLevel = 0;
      if (spaceCount < 0) {
        spaceCount = 0;
      }
    }
    else {
      if (spaceCount < 0) {
        int v = (-spaceCount + getSettings().getIndentSize(fileType) - 1) / getSettings().getIndentSize(fileType);
        indentLevel -= v;
        spaceCount += v * getSettings().getIndentSize(fileType);
        if (indentLevel < 0) {
          indentLevel = 0;
        }
      }
    }
    return new Helper(fileType, myProject).fillIndent(indentLevel * Helper.INDENT_FACTOR + spaceCount);
  }

  public Indent zeroIndent() {
    return new IndentImpl(getSettings(), 0, 0, null);
  }

  public VariableKind getVariableKind(PsiVariable variable) {
    if (variable instanceof PsiField) {
      if (variable.hasModifierProperty(PsiModifier.STATIC)) {
        if (variable.hasModifierProperty(PsiModifier.FINAL)) {
          return VariableKind.STATIC_FINAL_FIELD;
        }
        else {
          return VariableKind.STATIC_FIELD;
        }
      }
      else {
        return VariableKind.FIELD;
      }
    }
    else {
      if (variable instanceof PsiParameter) {
        if (((PsiParameter)variable).getDeclarationScope() instanceof PsiForeachStatement) {
          return VariableKind.LOCAL_VARIABLE;
        }
        else {
          return VariableKind.PARAMETER;
        }
      }
      else {
        if (variable instanceof PsiLocalVariable) {
          return VariableKind.LOCAL_VARIABLE;
        }
        else {
          return VariableKind.LOCAL_VARIABLE;
          // TODO[ik]: open api for this
          //LOG.assertTrue(false);
          //return null;
        }
      }
    }
  }

  public SuggestedNameInfo suggestVariableName(final VariableKind kind,
                                               final String propertyName,
                                               final PsiExpression expr,
                                               PsiType type) {
    LinkedHashSet<String> names = new LinkedHashSet<String>();

    if (expr != null && type == null) {
      type = expr.getType();
    }

    if (propertyName != null) {
      String[] namesByName = getSuggestionsByName(propertyName, kind, false);
      sortVariableNameSuggestions(namesByName, kind, propertyName, null);
      names.addAll(Arrays.asList(namesByName));
    }

    final NamesByExprInfo namesByExpr;
    if (expr != null) {
      namesByExpr = suggestVariableNameByExpression(expr, kind);
      if (namesByExpr.propertyName != null) {
        sortVariableNameSuggestions(namesByExpr.names, kind, namesByExpr.propertyName, null);
      }
      names.addAll(Arrays.asList(namesByExpr.names));
    }
    else {
      namesByExpr = null;
    }

    if (type != null) {
      String[] namesByType = suggestVariableNameByType(type, kind);
      sortVariableNameSuggestions(namesByType, kind, null, type);
      names.addAll(Arrays.asList(namesByType));
    }

    final String _propertyName;
    if (propertyName != null) {
      _propertyName = propertyName;
    }
    else {
      _propertyName = namesByExpr != null ? namesByExpr.propertyName : null;
    }

    addNamesFromStatistics(names, kind, _propertyName, type);

    String[] namesArray = names.toArray(new String[names.size()]);
    sortVariableNameSuggestions(namesArray, kind, _propertyName, type);

    final PsiType _type = type;
    return new SuggestedNameInfo(namesArray) {
      public void nameChoosen(String name) {
        if (_propertyName != null || _type != null) {
          if (_type != null && !_type.isValid()) return;
          myStatisticsManager.incVariableNameUseCount(name, kind, _propertyName, _type);
        }
      }
    };
  }

  private void addNamesFromStatistics(Set<String> names, VariableKind variableKind, String propertyName, PsiType type) {
    String[] allNames = myStatisticsManager.getAllVariableNamesUsed(variableKind, propertyName, type);

    int maxFrequency = 0;
    for (String name : allNames) {
      int count = myStatisticsManager.getVariableNameUseCount(name, variableKind, propertyName, type);
      maxFrequency = Math.max(maxFrequency, count);
    }

    int frequencyLimit = Math.max(5, maxFrequency / 2);

    for (String name : allNames) {
      if (names.contains(name)) continue;
      int count = myStatisticsManager.getVariableNameUseCount(name, variableKind, propertyName, type);
      if (LOG.isDebugEnabled()) {
        LOG.debug("new name:" + name + " count:" + count);
        LOG.debug("frequencyLimit:" + frequencyLimit);
      }
      if (count >= frequencyLimit) {
        names.add(name);
      }
    }

    if (propertyName != null && type != null) {
      addNamesFromStatistics(names, variableKind, propertyName, null);
      addNamesFromStatistics(names, variableKind, null, type);
    }
  }

  private String[] suggestVariableNameByType(PsiType type, final VariableKind variableKind) {
    String longTypeName = getLongTypeName(type);
    CodeStyleSettings.TypeToNameMap map = getMapByVariableKind(variableKind);
    if (map != null && longTypeName != null) {
      if (type.equals(PsiType.NULL)) {
        longTypeName = "java.lang.Object";
      }
      String name = map.nameByType(longTypeName);
      if (name != null) {
        return new String[]{name};
      }
    }

    List<String> suggestions = new ArrayList<String>();

    suggestNamesForCollectionInheritors(type, variableKind, suggestions);

    String typeName = normalizeTypeName(getTypeName(type));
    if (typeName != null) {
      suggestions.addAll(Arrays.asList(getSuggestionsByName(typeName, variableKind, type instanceof PsiArrayType)));
    }

    return suggestions.toArray(new String[suggestions.size()]);
  }

  private void suggestNamesForCollectionInheritors(final PsiType type,
                                                   final VariableKind variableKind,
                                                   List<String> suggestions) {
    if (!(type instanceof PsiClassType)) return;
    PsiClassType classType = (PsiClassType)type;
    PsiClassType.ClassResolveResult resolved = classType.resolveGenerics();
    if (resolved.getElement() == null) return;
    final PsiManager manager = PsiManager.getInstance(myProject);
    final PsiClass collectionClass = manager.findClass("java.util.Collection", resolved.getElement().getResolveScope());
    if (collectionClass == null) return;

    if (InheritanceUtil.isInheritorOrSelf(resolved.getElement(), collectionClass, true)) {
      final PsiSubstitutor substitutor;
      if (!manager.areElementsEquivalent(resolved.getElement(), collectionClass)) {
        substitutor = TypeConversionUtil.getClassSubstitutor(collectionClass, resolved.getElement(),
                                                             PsiSubstitutor.EMPTY);
      }
      else {
        substitutor = PsiSubstitutor.EMPTY;
      }

      PsiTypeParameterList typeParameterList = collectionClass.getTypeParameterList();
      if (typeParameterList == null) return;
      PsiTypeParameter[] typeParameters = typeParameterList.getTypeParameters();
      if (typeParameters.length == 0) return;

      PsiType componentTypeParameter = substitutor.substitute(typeParameters[0]);
      if (componentTypeParameter instanceof PsiClassType) {
        PsiClass componentClass = ((PsiClassType)componentTypeParameter).resolve();
        if (componentClass instanceof PsiTypeParameter) {
          if (collectionClass.getManager().areElementsEquivalent(((PsiTypeParameter)componentClass).getOwner(),
                                                                                                   resolved.getElement())) {
            PsiType componentType = resolved.getSubstitutor().substitute((PsiTypeParameter)componentClass);
            if (componentType == null) return;
            String typeName = normalizeTypeName(getTypeName(componentType));
            if (typeName != null) {
              suggestions.addAll(Arrays.asList(getSuggestionsByName(typeName, variableKind, true)));
            }
          }
        }
      }
    }
  }

  private static String normalizeTypeName(String typeName) {
    if (typeName == null) return null;
    if (typeName.endsWith("Impl") && typeName.length() > "Impl".length()) {
      return typeName.substring(0, typeName.length() - "Impl".length());
    }
    return typeName;
  }

  private static String getTypeName(PsiType type) {
    type = type.getDeepComponentType();
    if (type instanceof PsiClassType) {
      final PsiClassType classType = (PsiClassType)type;
      final String className = classType.getClassName();
      if (className != null) {
        return className;
      }
      else {
        final PsiClass aClass = classType.resolve();
        if (aClass instanceof PsiAnonymousClass) {
          return ((PsiAnonymousClass)aClass).getBaseClassType().getClassName();
        }
        else {
          return null;
        }
      }
    }
    else {
      if (type instanceof PsiPrimitiveType) {
        return type.getPresentableText();
      }
      else {
        if (type instanceof PsiWildcardType) {
          return getTypeName(((PsiWildcardType)type).getExtendsBound());
        }
        else {
          if (type instanceof PsiIntersectionType) {
            return getTypeName(((PsiIntersectionType)type).getRepresentative());
          }
          else {
            if (type instanceof PsiCapturedWildcardType) {
              return getTypeName(((PsiCapturedWildcardType)type).getWildcard());
            }
            else {
              LOG.error("Unknown type:" + type);
              return null;
            }
          }
        }
      }
    }
  }

  private @Nullable String getLongTypeName(PsiType type) {
    if (type instanceof PsiClassType) {
      PsiClass aClass = ((PsiClassType)type).resolve();
      if (aClass == null) return null;
      if (aClass instanceof PsiAnonymousClass) {
        PsiClass baseClass = ((PsiAnonymousClass)aClass).getBaseClassType().resolve();
        if (baseClass == null) return null;
        return baseClass.getQualifiedName();
      }
      return aClass.getQualifiedName();
    }
    else {
      if (type instanceof PsiArrayType) {
        return getLongTypeName(((PsiArrayType)type).getComponentType()) + "[]";
      }
      else {
        if (type instanceof PsiPrimitiveType) {
          return type.getPresentableText();
        }
        else {
          if (type instanceof PsiWildcardType) {
            final PsiType bound = ((PsiWildcardType)type).getBound();
            if (bound != null) {
              return getLongTypeName(bound);
            }
            else {
              return "java.lang.Object";
            }
          }
          else {
            if (type instanceof PsiCapturedWildcardType) {
              final PsiType bound = ((PsiCapturedWildcardType)type).getWildcard().getBound();
              if (bound != null) {
                return getLongTypeName(bound);
              }
              else {
                return "java.lang.Object";
              }
            }
            else {
              if (type instanceof PsiIntersectionType) {
                return getLongTypeName(((PsiIntersectionType)type).getRepresentative());
              }
              else {
                LOG.error("Unknown type:" + type);
                return null;
              }
            }
          }
        }
      }
    }
  }

  private static class NamesByExprInfo {
    final String[] names;
    final String propertyName;

    public NamesByExprInfo(String[] names, String propertyName) {
      this.names = names;
      this.propertyName = propertyName;
    }
  }

  private NamesByExprInfo suggestVariableNameByExpression(PsiExpression expr, VariableKind variableKind) {
    final NamesByExprInfo names1 = suggestVariableNameByExpressionOnly(expr, variableKind);
    final NamesByExprInfo names2 = suggestVariableNameByExpressionPlace(expr, variableKind);

    PsiType type = expr.getType();
    final String[] names3;
    if (type != null) {
      names3 = suggestVariableNameByType(type, variableKind);
    }
    else {
      names3 = null;
    }

    LinkedHashSet<String> names = new LinkedHashSet<String>();
    names.addAll(Arrays.asList(names1.names));
    names.addAll(Arrays.asList(names2.names));
    if (names3 != null) {
      names.addAll(Arrays.asList(names3));
    }
    String[] namesArray = names.toArray(new String[names.size()]);
    String propertyName = names1.propertyName != null ? names1.propertyName : names2.propertyName;
    return new NamesByExprInfo(namesArray, propertyName);
  }

  private NamesByExprInfo suggestVariableNameByExpressionOnly(PsiExpression expr, final VariableKind variableKind) {
    if (expr instanceof PsiMethodCallExpression) {
      PsiReferenceExpression methodExpr = ((PsiMethodCallExpression)expr).getMethodExpression();
      PsiElement[] children = methodExpr.getChildren();
      String methodName = children[children.length - 1].getText();
      String[] words = NameUtil.nameToWords(methodName);
      if (words.length > 1) {
        String firstWord = words[0];
        if ("get".equals(firstWord)
            || "is".equals(firstWord)
            || "find".equals(firstWord)
            || "create".equals(firstWord)) {
          final String propertyName = methodName.substring(firstWord.length());
          String[] names = getSuggestionsByName(propertyName, variableKind, false);
          return new NamesByExprInfo(names, propertyName);
        }
      }
    }
    else {
      if (expr instanceof PsiReferenceExpression) {
        String propertyName = ((PsiReferenceExpression)expr).getReferenceName();
        PsiElement refElement = ((PsiReferenceExpression)expr).resolve();
        if (refElement instanceof PsiVariable) {
          VariableKind refVariableKind = getVariableKind((PsiVariable)refElement);
          propertyName = variableNameToPropertyName(propertyName, refVariableKind);
        }
        if (refElement != null && propertyName != null) {
          String[] names = getSuggestionsByName(propertyName, variableKind, false);
          return new NamesByExprInfo(names, propertyName);
        }
      }
      else {
        if (expr instanceof PsiArrayAccessExpression) {
          PsiExpression array = ((PsiArrayAccessExpression)expr).getArrayExpression();
          if (array instanceof PsiReferenceExpression) {
            PsiElement[] children = array.getChildren();
            String arrayName = children[children.length - 1].getText();
            PsiElement refElement = ((PsiReferenceExpression)array).resolve();
            if (refElement instanceof PsiVariable) {
              VariableKind refVariableKind = getVariableKind((PsiVariable)refElement);
              arrayName = variableNameToPropertyName(arrayName, refVariableKind);
            }
            String name = null;
            if (arrayName.endsWith("ses") || arrayName.endsWith("xes")) { //?
              name = arrayName.substring(0, arrayName.length() - 2);
            }
            else {
              if (arrayName.endsWith("ies")) {
                name = arrayName.substring(0, arrayName.length() - 3) + "y";
              }
              else {
                if (StringUtil.endsWithChar(arrayName, 's')) {
                  name = arrayName.substring(0, arrayName.length() - 1);
                }
                else {
                  if ("children".equals(arrayName)) {
                    name = "child";
                  }
                }
              }
            }

            if (name != null) {
              String[] names = getSuggestionsByName(name, variableKind, false);
              return new NamesByExprInfo(names, name);
            }
          }
        }
      }
    }

    return new NamesByExprInfo(ArrayUtil.EMPTY_STRING_ARRAY, null);
  }

  private NamesByExprInfo suggestVariableNameByExpressionPlace(PsiExpression expr, final VariableKind variableKind) {
    if (expr.getParent() instanceof PsiExpressionList) {
      PsiExpressionList list = (PsiExpressionList)expr.getParent();
      PsiElement listParent = list.getParent();
      PsiMethod method = null;
      if (listParent instanceof PsiMethodCallExpression) {
        method = (PsiMethod)((PsiMethodCallExpression)listParent).getMethodExpression().resolve();
      }
      else {
        if (listParent instanceof PsiAnonymousClass) {
          listParent = listParent.getParent();
        }
        if (listParent instanceof PsiNewExpression) {
          method = ((PsiNewExpression)listParent).resolveConstructor();
        }
      }

      if (method != null) {
        method = (PsiMethod)method.getNavigationElement();
        PsiExpression[] expressions = list.getExpressions();
        int index = -1;
        for (int i = 0; i < expressions.length; i++) {
          if (expressions[i] == expr) {
            index = i;
            break;
          }
        }
        PsiParameter[] parms = method.getParameterList().getParameters();
        if (index < parms.length) {
          PsiIdentifier identifier = parms[index].getNameIdentifier();
          if (identifier != null) {
            String name = identifier.getText();
            name = variableNameToPropertyName(name, VariableKind.PARAMETER);
            String[] names = getSuggestionsByName(name, variableKind, false);
            return new NamesByExprInfo(names, name);
          }
        }
      }
    }

    return new NamesByExprInfo(ArrayUtil.EMPTY_STRING_ARRAY, null);
  }

  public String variableNameToPropertyName(String name, VariableKind variableKind) {
    if (variableKind == VariableKind.STATIC_FINAL_FIELD) {
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < name.length(); i++) {
        char c = name.charAt(i);
        if (c != '_') {
          if (Character.isLowerCase(c)) return variableNameToPropertyNameInner(name, variableKind);

          buffer.append(Character.toLowerCase(c));
        continue;
        }
        i++;
        if (i < name.length()) {
          c = name.charAt(i);
          buffer.append(c);
        }
      }
      return buffer.toString();
    }

    return variableNameToPropertyNameInner(name, variableKind);
  }

  private String variableNameToPropertyNameInner(String name, VariableKind variableKind) {
    String prefix = getPrefixByVariableKind(variableKind);
    String suffix = getSuffixByVariableKind(variableKind);
    boolean doDecapitalize = false;

    if (name.startsWith(prefix) && name.length() > prefix.length()) {
      name = name.substring(prefix.length());
      doDecapitalize = true;
    }

    if (name.endsWith(suffix) && name.length() > suffix.length()) {
      name = name.substring(0, name.length() - suffix.length());
      doDecapitalize = true;
    }

    if (name.startsWith("is") && name.length() > "is".length() && Character.isUpperCase(name.charAt("is".length()))) {
      name = name.substring("is".length());
      doDecapitalize = true;
    }

    if (doDecapitalize) {
      name = Introspector.decapitalize(name);
    }

    return name;
  }

  public String propertyNameToVariableName(String propertyName, VariableKind variableKind) {
    if (variableKind == VariableKind.STATIC_FINAL_FIELD) {
      String[] words = NameUtil.nameToWords(propertyName);
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < words.length; i++) {
        String word = words[i];
        if (i > 0) {
          buffer.append("_");
        }
        buffer.append(word.toUpperCase());
      }
      return buffer.toString();
    }

    String prefix = getPrefixByVariableKind(variableKind);
    String name = propertyName;
    if (name.length() > 0 && prefix.length() > 0 && !StringUtil.endsWithChar(prefix, '_')) {
      name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    name = prefix + name + getSuffixByVariableKind(variableKind);
    name = changeIfNotIdentifier(name);
    return name;
  }

  private String[] getSuggestionsByName(String name, VariableKind variableKind, boolean isArray) {
    String prefix = getPrefixByVariableKind(variableKind);
    ArrayList<String> list = new ArrayList<String>();
    String[] words = NameUtil.nameToWords(name);

    for (int step = 0; step < words.length; step++) {
      int wordCount = getSettings().PREFER_LONGER_NAMES ? words.length - step : step + 1;

      String startWord = words[words.length - wordCount];
      char c = startWord.charAt(0);
      if (c == '_' || !Character.isJavaIdentifierStart(c)) continue;

      StringBuffer buffer = new StringBuffer();
      buffer.append(prefix);

      if (variableKind == VariableKind.STATIC_FINAL_FIELD) {
        startWord = startWord.toUpperCase();
      }
      else {
        if (prefix.length() == 0 || StringUtil.endsWithChar(prefix, '_')) {
          startWord = startWord.toLowerCase();
        }
        else {
          startWord = Character.toUpperCase(c) + startWord.substring(1);
        }
      }
      buffer.append(startWord);

      for (int i = words.length - wordCount + 1; i < words.length; i++) {
        String word = words[i];
        String prevWord = words[i - 1];
        if (variableKind == VariableKind.STATIC_FINAL_FIELD) {
          word = word.toUpperCase();
          if (prevWord.charAt(prevWord.length() - 1) != '_') {
            word = "_" + word;
          }
        }
        else {
          if (prevWord.charAt(prevWord.length() - 1) == '_') {
            word = word.toLowerCase();
          }
        }
        buffer.append(word);
      }

      String suggestion = buffer.toString();

      if (isArray && variableKind != VariableKind.STATIC_FINAL_FIELD) {
        suggestion = StringUtil.pluralize(suggestion);
      }

      suggestion = changeIfNotIdentifier(suggestion + getSuffixByVariableKind(variableKind));

      list.add(suggestion);
    }

    return list.toArray(new String[list.size()]);
  }

  public String suggestUniqueVariableName(String baseName, PsiElement place, boolean lookForward) {
    PsiElement scope;
    if (lookForward) {
      scope = place.getParent();
      while (true) {
        if (scope instanceof PsiCodeBlock) break;
        if (scope instanceof PsiClass) break;
        if (scope instanceof PsiFile) break;
        scope = scope.getParent();
      }
      place = scope.getLastChild();
    }
    else {
      scope = null;
    }

    int index = 0;
    while (true) {
      String name = baseName;
      if (index > 0) {
        name += index;
      }
      index++;
      if (PsiUtil.isVariableNameUnique(name, place)) {
        if (scope instanceof PsiCodeBlock) {
          final String name1 = name;
          class Cancel extends RuntimeException {
          }
          try {
            scope.accept(new PsiRecursiveElementVisitor() {
                           public void visitClass(PsiClass aClass) {

                           }

                           public void visitVariable(PsiVariable variable) {
                             if (name1.equals(variable.getName())) {
                               throw new Cancel();
                             }
                           }
                         });
          }
          catch (Cancel e) {
          continue;
          }
        }
        return name;
      }
    }
  }

  public boolean checkIdentifierRole(String identifier, IdentifierRole role) {
    if (role == IdentifierRole.CLASS_NAME) {
      return identifier.length() > 0 && Character.isUpperCase(identifier.charAt(0));
    }
    else {
      if (role == IdentifierRole.FIELD_NAME) {
        return identifier.startsWith(getSettings().FIELD_NAME_PREFIX) &&
               identifier.endsWith(getSettings().FIELD_NAME_SUFFIX);
      }
      else {
        if (role == IdentifierRole.LOCAL_VAR_NAME) {
          return identifier.startsWith(getSettings().LOCAL_VARIABLE_NAME_PREFIX) &&
                 identifier.endsWith(getSettings().LOCAL_VARIABLE_NAME_SUFFIX);
        }
        else {
          if (role == IdentifierRole.PARAMETER_NAME) {
            return identifier.startsWith(getSettings().PARAMETER_NAME_PREFIX) &&
                   identifier.endsWith(getSettings().PARAMETER_NAME_SUFFIX);
          }
        }
      }
    }

    return false;
  }

  private void sortVariableNameSuggestions(String[] names,
                                           final VariableKind variableKind,
                                           final String propertyName,
                                           final PsiType type) {
    if (names.length <= 1) return;

    if (LOG.isDebugEnabled()) {
      LOG.debug("sorting names:" + variableKind);
      if (propertyName != null) {
        LOG.debug("propertyName:" + propertyName);
      }
      if (type != null) {
        LOG.debug("type:" + type);
      }
      for (String name : names) {
        int count = myStatisticsManager.getVariableNameUseCount(name, variableKind, propertyName, type);
        LOG.debug(name + " : " + count);
      }
    }

    Comparator<String> comparator = new Comparator<String>() {
      public int compare(String s1, String s2) {
        int count1 = myStatisticsManager.getVariableNameUseCount(s1, variableKind, propertyName, type);
        int count2 = myStatisticsManager.getVariableNameUseCount(s2, variableKind, propertyName, type);
        return count2 - count1;
      }
    };
    Arrays.sort(names, comparator);
  }

  public String getPrefixByVariableKind(VariableKind variableKind) {
    String prefix = "";
    if (variableKind == VariableKind.FIELD) {
      prefix = getSettings().FIELD_NAME_PREFIX;
    }
    else {
      if (variableKind == VariableKind.STATIC_FIELD) {
        prefix = getSettings().STATIC_FIELD_NAME_PREFIX;
      }
      else {
        if (variableKind == VariableKind.PARAMETER) {
          prefix = getSettings().PARAMETER_NAME_PREFIX;
        }
        else {
          if (variableKind == VariableKind.LOCAL_VARIABLE) {
            prefix = getSettings().LOCAL_VARIABLE_NAME_PREFIX;
          }
          else {
            if (variableKind == VariableKind.STATIC_FINAL_FIELD) {
              prefix = "";
            }
            else {
              LOG.assertTrue(false);
            }
          }
        }
      }
    }
    if (prefix == null) {
      prefix = "";
    }
    return prefix;
  }

  public String getSuffixByVariableKind(VariableKind variableKind) {
    String suffix = "";
    if (variableKind == VariableKind.FIELD) {
      suffix = getSettings().FIELD_NAME_SUFFIX;
    }
    else {
      if (variableKind == VariableKind.STATIC_FIELD) {
        suffix = getSettings().STATIC_FIELD_NAME_SUFFIX;
      }
      else {
        if (variableKind == VariableKind.PARAMETER) {
          suffix = getSettings().PARAMETER_NAME_SUFFIX;
        }
        else {
          if (variableKind == VariableKind.LOCAL_VARIABLE) {
            suffix = getSettings().LOCAL_VARIABLE_NAME_SUFFIX;
          }
          else {
            if (variableKind == VariableKind.STATIC_FINAL_FIELD) {
              suffix = "";
            }
            else {
              LOG.assertTrue(false);
            }
          }
        }
      }
    }
    if (suffix == null) {
      suffix = "";
    }
    return suffix;
  }

  private CodeStyleSettings.TypeToNameMap getMapByVariableKind(VariableKind variableKind) {
    if (variableKind == VariableKind.FIELD) {
      return getSettings().FIELD_TYPE_TO_NAME;
    }
    else {
      if (variableKind == VariableKind.STATIC_FIELD) {
        return getSettings().STATIC_FIELD_TYPE_TO_NAME;
      }
      else {
        if (variableKind == VariableKind.PARAMETER) {
          return getSettings().PARAMETER_TYPE_TO_NAME;
        }
        else {
          if (variableKind == VariableKind.LOCAL_VARIABLE) {
            return getSettings().LOCAL_VARIABLE_TYPE_TO_NAME;
          }
          else {
            return null;
          }
        }
      }
    }
  }

  private String changeIfNotIdentifier(String name) {
    PsiManager manager = PsiManager.getInstance(myProject);

    if (!manager.getNameHelper().isIdentifier(name)) {
      char c = name.charAt(0);
      if (StringUtil.isVowel(c)) {
        return "an" + Character.toUpperCase(c) + name.substring(1);
      }
      return "a" + Character.toUpperCase(c) + name.substring(1);
    }
    return name;
  }

  private CodeStyleSettings getSettings() {
    return CodeStyleSettingsManager.getSettings(myProject);
  }
}