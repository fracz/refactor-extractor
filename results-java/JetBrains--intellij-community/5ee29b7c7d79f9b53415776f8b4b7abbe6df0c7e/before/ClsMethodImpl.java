package com.intellij.psi.impl.compiled;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.pom.java.PomMethod;
import com.intellij.psi.*;
import com.intellij.psi.impl.*;
import com.intellij.psi.impl.cache.MethodView;
import com.intellij.psi.impl.cache.RepositoryManager;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.scope.util.PsiScopesUtil;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.RowIcon;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.cls.BytePointer;
import com.intellij.util.cls.ClsFormatException;
import com.intellij.util.cls.ClsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

public class ClsMethodImpl extends ClsRepositoryPsiElement implements PsiAnnotationMethod, ClsModifierListOwner {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.compiled.ClsMethodImpl");

  private final ClsClassImpl myParent;
  private volatile int myStartOffset;

  private volatile String myName = null;
  private PsiIdentifier myNameIdentifier = null; //protected by PsiLock
  private volatile boolean myConstructorFlag;
  private volatile Boolean myIsVarArgs = null;
  private PsiTypeElement myReturnType = null; //protected by PsiLock
  private volatile ClsModifierListImpl myModifierList = null;
  private volatile PsiParameterList myParameterList = null;
  private volatile PsiReferenceList myThrowsList = null;
  private PsiDocComment myDocComment = null; //protected by PsiLock
  private Boolean myIsDeprecated = null; //protected by PsiLock
  private PsiTypeParameterList myTypeParameters = null; //protected by PsiLock
  private PsiAnnotationMemberValue[] myDefaultValue = null; //protected by PsiLock
  private volatile ClsAnnotationImpl[] myAnnotations = null;
  private volatile ClsAnnotationImpl[][] myParameterAnnotations = null;
  @NonNls private static final String SYNTHETIC_INIT_METHOD = "<init>";


  public ClsMethodImpl(ClsClassImpl parent, int startOffset) {
    super(parent.myManager, -1);
    myParent = parent;
    myStartOffset = startOffset;
  }

  public ClsMethodImpl(PsiManagerImpl manager, long repositoryId) {
    super(manager, repositoryId);
    myParent = null;
    myStartOffset = -1;
  }

  public void setRepositoryId(long repositoryId) {
    super.setRepositoryId(repositoryId);
    if (repositoryId >= 0) {
      myStartOffset = -1;
    }
  }

  @NotNull
  public PsiElement[] getChildren() {
    PsiDocComment docComment = getDocComment();
    PsiModifierList modifierList = getModifierList();
    PsiTypeElement returnType = getReturnTypeElement();
    PsiIdentifier name = getNameIdentifier();
    PsiParameterList parameterList = getParameterList();
    PsiReferenceList throwsList = getThrowsList();
    PsiAnnotationMemberValue defaultValue = getDefaultValue();

    int count =
      (docComment != null ? 1 : 0)
      + (modifierList != null ? 1 : 0)
      + (returnType != null ? 1 : 0)
      + (name != null ? 1 : 0)
      + (parameterList != null ? 1 : 0)
      + (throwsList != null ? 1 : 0)
      + (defaultValue != null ? 1 : 0);

    PsiElement[] children = new PsiElement[count];

    int offset = 0;
    if (docComment != null) {
      children[offset++] = docComment;
    }
    if (modifierList != null) {
      children[offset++] = modifierList;
    }
    if (returnType != null) {
      children[offset++] = returnType;
    }
    if (name != null) {
      children[offset++] = name;
    }
    if (parameterList != null) {
      children[offset++] = parameterList;
    }
    if (throwsList != null) {
      children[offset++] = throwsList;
    }
    if (defaultValue != null) {
      children[offset++] = defaultValue;
    }

    return children;
  }

  public PsiElement getParent() {
      final long parentId = getParentId();
      if (parentId < 0) return myParent;
      return getRepositoryElementsManager().findOrCreatePsiElementById(parentId);
    }

  public PsiClass getContainingClass() {
    return (PsiClass)getParent();
  }

  public PsiIdentifier getNameIdentifier() {
    synchronized (PsiLock.LOCK) {
      if (myNameIdentifier == null) {
        myNameIdentifier = new ClsIdentifierImpl(this, getName());
      }
    }
    return myNameIdentifier;
  }

  @NotNull
  public PsiMethod[] findSuperMethods() {
    return PsiSuperMethodImplUtil.findSuperMethods(this);
  }

  @NotNull
  public PsiMethod[] findSuperMethods(boolean checkAccess) {
    return PsiSuperMethodImplUtil.findSuperMethods(this, checkAccess);
  }

  @NotNull
  public PsiMethod[] findSuperMethods(PsiClass parentClass) {
    return PsiSuperMethodImplUtil.findSuperMethods(this, parentClass);
  }

  @NotNull
  public List<MethodSignatureBackedByPsiMethod> findSuperMethodSignaturesIncludingStatic(boolean checkAccess) {
    return PsiSuperMethodImplUtil.findSuperMethodSignaturesIncludingStatic(this, checkAccess);
  }

  public PsiMethod findDeepestSuperMethod() {
    return PsiSuperMethodImplUtil.findDeepestSuperMethod(this);
  }

  @NotNull
  public PsiMethod[] findDeepestSuperMethods() {
    return PsiSuperMethodImplUtil.findDeepestSuperMethods(this);
  }

  public PomMethod getPom() {
    //TODO:
    return null;
  }

  @NotNull
  public String getName() {
    if (myName == null) {
      synchronized (PsiLock.LOCK) {
        long repositoryId = getRepositoryId();
        if (repositoryId < 0) {
          try {
            ClassFileData classFileData = myParent.getClassFileData();
            byte[] data = classFileData.getData();
            int offset = myStartOffset + 2;
            int b1 = data[offset++] & 0xFF;
            int b2 = data[offset++] & 0xFF;
            int index = (b1 << 8) + b2;
            offset = classFileData.getOffsetInConstantPool(index);
            String name = ClsUtil.readUtf8Info(data, offset);
            if (name.equals(SYNTHETIC_INIT_METHOD)) {
              name = myParent.getName();
              myConstructorFlag = true;
            }
            else {
              myConstructorFlag = false;
            }
            myName = name;
          }
          catch (ClsFormatException e) {
            myName = "";
          }
        }
        else {
          MethodView methodView = getRepositoryManager().getMethodView();
          String name = methodView.getName(repositoryId);
          myConstructorFlag = methodView.getReturnTypeText(repositoryId) == null;
          myName = name;
        }
      }
    }
    return myName;
  }

  @NotNull
  public HierarchicalMethodSignature getHierarchicalMethodSignature() {
    return PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this);
  }

  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    SharedPsiElementImplUtil.setName(getNameIdentifier(), name);
    return this;
  }

  public PsiTypeElement getReturnTypeElement() {
    if (isConstructor()) return null;

    synchronized (PsiLock.LOCK) {
      if (myReturnType == null) {
        long repositoryId = getRepositoryId();
        if (repositoryId < 0) {
          if (!parseViaGenericSignature()) {
            try {
              ClassFileData classFileData = myParent.getClassFileData();
              byte[] data = classFileData.getData();
              int offset = myStartOffset + 4;
              int b1 = data[offset++] & 0xFF;
              int b2 = data[offset++] & 0xFF;
              int index = (b1 << 8) + b2;
              offset = classFileData.getOffsetInConstantPool(index);
              offset += 3; // skip tag and length
              while (true) {
                if (offset >= data.length) {
                  throw new ClsFormatException();
                }
                if (data[offset++] == ')') break;
              }
              String typeText = ClsUtil.getTypeText(data, offset);
              myReturnType = new ClsTypeElementImpl(this, typeText, ClsTypeElementImpl.VARIANCE_NONE);
            }
            catch (ClsFormatException e) {
              myReturnType = null;
            }
          }
        }
        else {
          String typeText = getRepositoryManager().getMethodView().getReturnTypeText(repositoryId);
          myReturnType = new ClsTypeElementImpl(this, typeText, ClsTypeElementImpl.VARIANCE_NONE);
        }
      }
    }
    return myReturnType;
  }

  public PsiType getReturnType() {
    PsiTypeElement typeElement = getReturnTypeElement();
    return typeElement == null ? null : typeElement.getType();
  }

  @NotNull
  public PsiModifierList getModifierList() {
    synchronized (PsiLock.LOCK) {
      if (myModifierList == null) {
        int flags = getAccessFlags();
        myModifierList = new ClsModifierListImpl(this, flags & ClsUtil.ACC_METHOD_MASK);
      }
    }
    return myModifierList;
  }

  public boolean hasModifierProperty(@NotNull String name) {
    return getModifierList().hasModifierProperty(name);
  }

  private int getAccessFlags() {
  synchronized (PsiLock.LOCK) {
    long repositoryId = getRepositoryId();
    if (repositoryId < 0) {
      try {
        int offset = myStartOffset;
        byte[] data = myParent.getClassFileData().getData();
        if (offset + 2 > data.length) {
          throw new ClsFormatException();
        }
        int b1 = data[offset++] & 0xFF;
        int b2 = data[offset++] & 0xFF;
        return (b1 << 8) + b2;
      }
      catch (ClsFormatException e) {
        return 0;
      }
    }
    else {
      MethodView methodView = getRepositoryManager().getMethodView();
      return methodView.getModifiers(repositoryId);
    }
  }
  }

  boolean isBridge() throws ClsFormatException {
    return ClsUtil.isBridge(getAccessFlags()) ||
           //This is 2.2 spec rudiment; TODO remove it
           myParent.getClassFileData().findAttribute(myStartOffset + 6, "Bridge") != null;
  }

  boolean isSynthetic() throws ClsFormatException {
    return ClsUtil.isSynthetic(getAccessFlags()) ||
           myParent.getClassFileData().findAttribute(myStartOffset + 6, "Synthetic") != null;
  }

  @NotNull
  public PsiParameterList getParameterList() {
    synchronized (PsiLock.LOCK) {
      if (myParameterList == null && !parseViaGenericSignature()) {
        myParameterList = createParameters(calcParameterTypes());
      }
    }

    return myParameterList;
  }

  private PsiParameterList createParameters(ArrayList<String> types) {
    ClsParameterImpl[] parameters = types != null ? new ClsParameterImpl[types.size()] : ClsParameterImpl.EMPTY_ARRAY;
    ClsParameterListImpl parameterList = new ClsParameterListImpl(this, parameters);
    for (int i = 0; i < parameters.length; i++) {
      String typeText = types.get(i);
      ClsTypeElementImpl type = new ClsTypeElementImpl(null, typeText, ClsTypeElementImpl.VARIANCE_NONE);
      parameters[i] = new ClsParameterImpl(parameterList, type, i);
      type.setParent(parameters[i]);
    }

    return parameterList;
  }

  @NotNull private ClsAnnotationImpl[][] calcParameterAnnotations() {
    long repositoryId = getRepositoryId();
    if (repositoryId < 0) {
      ClassFileData classFileData = myParent.getClassFileData();
      try {
        BytePointer pointer1 = classFileData.findAttribute(myStartOffset + 6, "RuntimeVisibleParameterAnnotations");
        ClsParameterImpl[] parameters = (ClsParameterImpl[])myParameterList.getParameters();
        if (pointer1 != null) {
          ClsAnnotationImpl[][] ann1 = readParameterAnnotations(pointer1, parameters);
          BytePointer pointer2 = classFileData.findAttribute(myStartOffset + 6, "RuntimeInvisibleParameterAnnotations");
          if (pointer2 != null) {
            ClsAnnotationImpl[][] ann2 = readParameterAnnotations(pointer2, parameters);
            ClsAnnotationImpl[][] result = ArrayUtil.mergeArrays(ann1, ann2, ClsAnnotationImpl[].class);
            return result;
          }
          else {
            return ann1;
          }
        }
        else {
          BytePointer pointer2 = classFileData.findAttribute(myStartOffset + 6, "RuntimeInvisibleParameterAnnotations");
          if (pointer2 != null) {
            return readParameterAnnotations(pointer2, parameters);
          }
          else {
            return ClsAnnotationImpl.EMPTY_2D_ARRAY;
          }
        }
      }
      catch (ClsFormatException e) {
        return ClsAnnotationImpl.EMPTY_2D_ARRAY;
      }
    }
    else {
      MethodView methodView = getRepositoryManager().getMethodView();

      String[][] parameterAnnotations = methodView.getParameterAnnotations(repositoryId);
      ClsAnnotationImpl[][] result = new ClsAnnotationImpl[parameterAnnotations.length][];
      for (int i = 0; i < parameterAnnotations.length; i++) {
        String[] annotations = parameterAnnotations[i];
        ClsParameterImpl parameter = (ClsParameterImpl)myParameterList.getParameters()[i];
        result[i] = annotations.length == 0 ? ClsAnnotationImpl.EMPTY_ARRAY : new ClsAnnotationImpl[annotations.length];
        for (int j = 0; j < annotations.length; j++) {
          result[i][j] = (ClsAnnotationImpl)ClsAnnotationsUtil.createMemberValueFromText(annotations[j], myManager,
                                                                                         (ClsElementImpl)parameter.getModifierList());
        }
      }

      return result;
    }
  }

  @NotNull private ClsAnnotationImpl[][] readParameterAnnotations(BytePointer pointer, ClsParameterImpl[] parameters) throws ClsFormatException {
    pointer.offset += 4;
    int numParameters = ClsUtil.readU1(pointer);
    ClsAnnotationImpl[][] result = parameters.length == 0 ? ClsAnnotationImpl.EMPTY_2D_ARRAY : new ClsAnnotationImpl[parameters.length][];
    int limit = Math.min(numParameters, parameters.length);
    for (int i = 0; i < limit; i++) {
      result[i] = myParent.getClassFileData().readAnnotations(parameters[i], pointer);
    }

    for (int i = limit; i < parameters.length; i++) {
      result[i] = ClsAnnotationImpl.EMPTY_ARRAY;
    }

    return result;
  }

  private ArrayList<String> calcParameterTypes() {
    getName(); //initialize flags
    long repositoryId = getRepositoryId();
    if (repositoryId < 0) {
      try {
        ClassFileData classFileData = myParent.getClassFileData();
        byte[] data = classFileData.getData();
        int offset = myStartOffset + 4;
        int b1 = data[offset++] & 0xFF;
        int b2 = data[offset++] & 0xFF;
        int index = (b1 << 8) + b2;
        offset = classFileData.getOffsetInConstantPool(index);
        offset += 3; // skip tag and length
        if (offset + 1 >= data.length) {
          throw new ClsFormatException();
        }
        if (data[offset++] != '(') {
          throw new ClsFormatException();
        }

        if (myConstructorFlag &&
            myParent.getParent() instanceof PsiClass &&
            !myParent.getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
          //Then there is presumably a synthetic field in the class, that is instantiated in the constructor
          //Skip the first parameter
          // there is Sun generic compiler bug here, happens to throw...
          if (data[offset] == ')') {
            throw new ClsFormatException();
          }
          offset = ClsUtil.getTypeEndOffset(data, offset);
          if (offset >= data.length) {
            throw new ClsFormatException();
          }
        }

        ArrayList<String> types = null;
        while (data[offset] != ')') {
          String typeText = ClsUtil.getTypeText(data, offset);
          offset = ClsUtil.getTypeEndOffset(data, offset);
          if (offset >= data.length) {
            throw new ClsFormatException();
          }
          if (types == null) {
            types = new ArrayList<String>();
          }
          types.add(typeText);
        }

        if (types != null) {
          patchVarargs(types);
        }
        return types;
      }
      catch (ClsFormatException e) {
        return new ArrayList<String>();
      }
    }
    else {
      MethodView methodView = getRepositoryManager().getMethodView();
      int count = methodView.getParameterCount(repositoryId);
      if (count == 0) return null;
      ArrayList<String> types = new ArrayList<String>(count);
      for (int i = 0; i < count; i++) {
        String text = methodView.getParameterTypeText(repositoryId, i);
        types.add(text);
      }
      return types;
    }
  }

  private void patchVarargs(ArrayList<String> types) {
    if (isVarArgs()) {
      String lastParamTypeText = types.get(types.size() - 1);
      LOG.assertTrue(lastParamTypeText.endsWith("[]"));
      lastParamTypeText = lastParamTypeText.substring(0, lastParamTypeText.length() - 2) + "...";
      types.set(types.size() - 1, lastParamTypeText);
    }
  }

  @NotNull
  public PsiReferenceList getThrowsList() {
    synchronized (PsiLock.LOCK) {
      if (myThrowsList == null) {
        long repositoryId = getRepositoryId();
        if (repositoryId < 0) {
          parseViaGenericSignature();
          if (myThrowsList == null) {
            try {
              ClassFileData classFileData = myParent.getClassFileData();
              BytePointer ptr = classFileData.findAttribute(myStartOffset + 6, "Exceptions");
              if (ptr != null) {
                ptr.offset += 4; // skip length
                int exceptionCount = ClsUtil.readU2(ptr);
                ClsJavaCodeReferenceElementImpl[] refs = exceptionCount == 0 ? ClsJavaCodeReferenceElementImpl.EMPTY_ARRAY : new ClsJavaCodeReferenceElementImpl[exceptionCount];
                int jj = 0;
                for (int j = 0; j < exceptionCount; j++) {
                  int index = ClsUtil.readU2(ptr);
                  if (index != 0) {
                    refs[jj++] = classFileData.buildReference(index);
                  }
                }
                if (jj < exceptionCount) {
                  ClsJavaCodeReferenceElementImpl[] refs1 = new ClsJavaCodeReferenceElementImpl[jj];
                  System.arraycopy(refs, 0, refs1, 0, jj);
                  refs = refs1;
                }

                myThrowsList = new ClsReferenceListImpl(this, refs, PsiKeyword.THROWS);
                for (ClsJavaCodeReferenceElementImpl ref : refs) {
                  ref.setParent(myThrowsList);
                }
              }
            }
            catch (ClsFormatException e) {
            }
            if (myThrowsList == null) {
              myThrowsList = new ClsReferenceListImpl(this, PsiJavaCodeReferenceElement.EMPTY_ARRAY, PsiKeyword.THROWS);
            }
          }
        }
        else {
          MethodView methodView = getRepositoryManager().getMethodView();
          String[] refTexts = methodView.getThrowsList(repositoryId);
          ClsJavaCodeReferenceElementImpl[] refs = refTexts.length == 0 ? ClsJavaCodeReferenceElementImpl.EMPTY_ARRAY : new ClsJavaCodeReferenceElementImpl[refTexts.length];
          for (int i = 0; i < refTexts.length; i++) {
            refs[i] = new ClsJavaCodeReferenceElementImpl(null, refTexts[i]);
          }
          myThrowsList = new ClsReferenceListImpl(this, refs, PsiKeyword.THROWS);
          for (ClsJavaCodeReferenceElementImpl ref : refs) {
            ref.setParent(myThrowsList);
          }
        }
      }
    }
    return myThrowsList;
  }

  public PsiCodeBlock getBody() {
    return null;
  }

  public boolean isDeprecated() {
    synchronized (PsiLock.LOCK) {
      if (myIsDeprecated == null) {
        long repositoryId = getRepositoryId();
        if (repositoryId < 0) {
          try {
            boolean isDeprecated = myParent.getClassFileData().findAttribute(myStartOffset + 6, "Deprecated") != null;
            myIsDeprecated = isDeprecated ? Boolean.TRUE : Boolean.FALSE;
          }
          catch (ClsFormatException e) {
            myIsDeprecated = Boolean.FALSE;
          }
        }
        else {
          boolean isDeprecated = getRepositoryManager().getMethodView().isDeprecated(repositoryId);
          myIsDeprecated = isDeprecated ? Boolean.TRUE : Boolean.FALSE;
        }
      }
    }
    return myIsDeprecated.booleanValue();
  }

  public PsiAnnotationMemberValue getDefaultValue() {
    synchronized (PsiLock.LOCK) {
      if (myDefaultValue == null) {
        myDefaultValue = new PsiAnnotationMemberValue[1];

        if (getRepositoryId() < 0) {
          if (myParent != null && myParent.isAnnotationType()) {
            try {
              BytePointer ptr = myParent.getClassFileData().findAttribute(myStartOffset + 6, "AnnotationDefault");
              if (ptr != null) {
                ptr.offset += 4;
                myDefaultValue[0] = myParent.getClassFileData().readMemberValue(ptr, this);
              }
            }
            catch (ClsFormatException e) {
            }
          }
        }
        else {
          RepositoryManager repositoryManager = getRepositoryManager();
          if (repositoryManager.getClassView().isAnnotationType(getParentId())) {
            String defaultValueText = repositoryManager.getMethodView().getDefaultValueText(getRepositoryId());
            if (defaultValueText != null && defaultValueText.length() > 0) {
              myDefaultValue[0] = ClsAnnotationsUtil.createMemberValueFromText(defaultValueText, myManager, this);
            }
          }
        }
      }
    }

    return myDefaultValue[0];
  }

  private boolean parseViaGenericSignature() {
    if (getRepositoryId() >= 0) return false;
    try {
      String signature = getSignatureAttribute();
      if (signature == null) return false;

      CharacterIterator iterator = new StringCharacterIterator(signature, 0);
      myTypeParameters = GenericSignatureParsing.parseTypeParametersDeclaration(iterator, this, signature);

      if (iterator.current() != '(') return false;
      iterator.next();
      ArrayList<String> types = new ArrayList<String>();
      while (iterator.current() != ')') {
        types.add(GenericSignatureParsing.parseTypeString(iterator));
      }
      patchVarargs(types);
      myParameterList = createParameters(types);

      iterator.next();
      myReturnType = new ClsTypeElementImpl(this, GenericSignatureParsing.parseTypeString(iterator), ClsTypeElementImpl.VARIANCE_NONE);

      if (iterator.current() == '^') {
        iterator.next();
        myThrowsList = new ClsReferenceListImpl(this, PsiKeyword.THROWS);
        PsiJavaCodeReferenceElement[] refs = GenericSignatureParsing.parseToplevelClassRefSignatures(iterator, myThrowsList);
          ((ClsReferenceListImpl)myThrowsList).setReferences(refs);
      }

      return true;
    }
    catch (ClsFormatException e) {
      return false;
    }
  }

  private String getSignatureAttribute() {
    try {
      ClassFileData data = myParent.getClassFileData();
      return data.readUtf8Attribute(data.findAttribute(myStartOffset + 6, "Signature"));
    }
    catch (ClsFormatException e) {
      return null;
    }
  }

  public PsiDocComment getDocComment() {
    if (!isDeprecated()) return null;

    synchronized (PsiLock.LOCK) {
      if (myDocComment == null) {
        myDocComment = new ClsDocCommentImpl(this);
      }
    }
    return myDocComment;
  }


  public boolean isConstructor() {
    getName(); // to initialize myConstructorFlag
    return myConstructorFlag;
  }

  public boolean isVarArgs() {
    if (myIsVarArgs == null) {
      boolean isVarArgs;
      if (PsiUtil.getLanguageLevel(this).compareTo(LanguageLevel.JDK_1_5) >= 0) {
        if (getRepositoryId() < 0) {
          isVarArgs = (getAccessFlags() & ClsUtil.ACC_VARARGS) != 0;
        }
        else {
          RepositoryManager repositoryManager = getRepositoryManager();
          isVarArgs = repositoryManager.getMethodView().isVarArgs(getRepositoryId());
        }
      }
      else {
        isVarArgs = false;
      }

      myIsVarArgs = isVarArgs ? Boolean.TRUE : Boolean.FALSE;
    }

    return myIsVarArgs.booleanValue();
    }

  @NotNull
  public MethodSignature getSignature(@NotNull PsiSubstitutor substitutor) {
    return MethodSignatureBackedByPsiMethod.create(this, substitutor);
  }

  public void appendMirrorText(final int indentLevel, final StringBuffer buffer) {
    appendMethodHeader(buffer, indentLevel);

    if (hasModifierProperty(PsiModifier.ABSTRACT) || hasModifierProperty(PsiModifier.NATIVE)) {
      buffer.append(";");
    }
    else {
      buffer.append(" { /* ");
      buffer.append(PsiBundle.message("psi.decompiled.method.body"));
      buffer.append(" */ }");
    }
  }

  private void appendMethodHeader(@NonNls StringBuffer buffer, final int indentLevel) {
    ClsDocCommentImpl docComment = (ClsDocCommentImpl)getDocComment();
    if (docComment != null) {
      docComment.appendMirrorText(indentLevel, buffer);
      goNextLine(indentLevel, buffer);
    }
    ((ClsElementImpl)getModifierList()).appendMirrorText(indentLevel, buffer);
    ((ClsElementImpl)getTypeParameterList()).appendMirrorText(indentLevel, buffer);
    if (!isConstructor()) {
      ((ClsElementImpl)getReturnTypeElement()).appendMirrorText(indentLevel, buffer);
      buffer.append(' ');
    }
    ((ClsElementImpl)getNameIdentifier()).appendMirrorText(indentLevel, buffer);
    ((ClsElementImpl)getParameterList()).appendMirrorText(indentLevel, buffer);
    final PsiReferenceList throwsList = getThrowsList();
    if (throwsList.getReferencedTypes().length > 0) {
      buffer.append(' ');
      ((ClsElementImpl)throwsList).appendMirrorText(indentLevel, buffer);
    }

    PsiAnnotationMemberValue defaultValue = getDefaultValue();
    if (defaultValue != null) {
      buffer.append(" default ");
      ((ClsElementImpl)defaultValue).appendMirrorText(indentLevel, buffer);
    }
  }

  public void setMirror(@NotNull TreeElement element) {
    LOG.assertTrue(isValid());
    LOG.assertTrue(!CHECK_MIRROR_ENABLED || myMirror == null);
    myMirror = element;

    PsiMethod mirror = (PsiMethod)SourceTreeToPsiMap.treeElementToPsi(element);
    if (getDocComment() != null) {
        ((ClsElementImpl)getDocComment()).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(mirror.getDocComment()));
    }
      ((ClsElementImpl)getModifierList()).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(mirror.getModifierList()));
    if (!isConstructor() && mirror.getReturnTypeElement() != null) {
        ((ClsElementImpl)getReturnTypeElement()).setMirror(
        (TreeElement)SourceTreeToPsiMap.psiElementToTree(mirror.getReturnTypeElement()));
    }
      ((ClsElementImpl)getNameIdentifier()).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(mirror.getNameIdentifier()));
      ((ClsElementImpl)getParameterList()).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(mirror.getParameterList()));
      ((ClsElementImpl)getThrowsList()).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(mirror.getThrowsList()));
      ((ClsElementImpl)getTypeParameterList()).setMirror(
      (TreeElement)SourceTreeToPsiMap.psiElementToTree(mirror.getTypeParameterList()));
    if (getDefaultValue() != null) {
      LOG.assertTrue(mirror instanceof PsiAnnotationMethod);
        ((ClsElementImpl)getDefaultValue()).setMirror((TreeElement)SourceTreeToPsiMap.psiElementToTree(((PsiAnnotationMethod)mirror).getDefaultValue()));
    }
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    visitor.visitMethod(this);
  }

  public String toString() {
    return "PsiMethod:" + getName();
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull PsiSubstitutor substitutor,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, this);
    if (lastParent == null) return true;

    if (!PsiScopesUtil.walkChildrenScopes(this, processor, substitutor, lastParent, place)) return false;

    final PsiParameter[] parameters = getParameterList().getParameters();
    for (PsiParameter parameter : parameters) {
      if (!processor.execute(parameter, substitutor)) return false;
    }

    return true;
  }

  public PsiElement getNavigationElement() {
    PsiClass sourceClassMirror = ((ClsClassImpl)getParent()).getSourceMirrorClass();
    if (sourceClassMirror == null) return this;
    final PsiMethod[] methodsByName = sourceClassMirror.findMethodsByName(getName(), false);
    for (PsiMethod sourceMethod : methodsByName) {
      if (MethodSignatureUtil.areParametersErasureEqual(this, sourceMethod)) return sourceMethod;
    }
    return this;
  }

  public PsiTypeParameterList getTypeParameterList() {
    synchronized (PsiLock.LOCK) {
      if (myTypeParameters == null) {
        long repositoryId = getRepositoryId();
        if (repositoryId < 0) {
          if (!parseViaGenericSignature()) {
            myTypeParameters = new ClsTypeParametersListImpl(this, ClsTypeParameterImpl.EMPTY_ARRAY);
          }
        }
        else {
          MethodView methodView = getRepositoryManager().getMethodView();
          int count = methodView.getTypeParametersCount(repositoryId);
          if (count == 0) {
            myTypeParameters = new ClsTypeParametersListImpl(this, ClsTypeParameterImpl.EMPTY_ARRAY);
          }
          else {
            StringBuilder compiledParams = new StringBuilder();
            compiledParams.append('<');
            for (int i = 0; i < count; i++) {
              compiledParams.append(methodView.getTypeParameterText(repositoryId, i));
            }
            compiledParams.append('>');
            try {
              final String signature = compiledParams.toString();
              myTypeParameters =
                GenericSignatureParsing.parseTypeParametersDeclaration(new StringCharacterIterator(signature, 0), this, signature);
            }
            catch (ClsFormatException e) {
              LOG.error(e); // dsl: should not happen when stuff is parsed from repository
            }
          }
        }
      }
      return myTypeParameters;
    }
  }

  public boolean hasTypeParameters() {
    return PsiImplUtil.hasTypeParameters(this);
  }

  @NotNull public PsiTypeParameter[] getTypeParameters() {
    return PsiImplUtil.getTypeParameters(this);
  }

  @NotNull
  public ClsAnnotationImpl[] getAnnotations() {
    ClsAnnotationImpl[] annotations = myAnnotations;
    if (annotations == null) {
      ClsAnnotationsUtil.AttributeReader reader = new ClsAnnotationsUtil.AttributeReader() {
        public BytePointer readAttribute(String attributeName) {
          try {
            return myParent.getClassFileData().findAttribute(myStartOffset + 6, attributeName);
          }
          catch (ClsFormatException e) {
            return null;
          }
        }

        public ClassFileData getClassFileData() {
          return myParent.getClassFileData();
        }
      };
      myAnnotations = annotations = ClsAnnotationsUtil.getAnnotationsImpl(this, reader, myModifierList);
    }

    return annotations;
  }

  @NotNull public ClsAnnotationImpl[] getParameterAnnotations(int paramIdx) {
    ClsAnnotationImpl[][] parameterAnnotations = myParameterAnnotations;
    if (parameterAnnotations == null) {
      myParameterAnnotations = parameterAnnotations = calcParameterAnnotations();
    }
    return paramIdx >= parameterAnnotations.length ? ClsAnnotationImpl.EMPTY_ARRAY : parameterAnnotations[paramIdx];
  }

  public ItemPresentation getPresentation() {
    return SymbolPresentationUtil.getMethodPresentation(this);
  }

  public Icon getElementIcon(final int flags) {
    Icon methodIcon = hasModifierProperty(PsiModifier.ABSTRACT) ? Icons.ABSTRACT_METHOD_ICON : Icons.METHOD_ICON;
    RowIcon baseIcon = createLayeredIcon(methodIcon, ElementPresentationUtil.getFlags(this, false));
    return addVisibilityIcon(this, flags, baseIcon);
  }
}