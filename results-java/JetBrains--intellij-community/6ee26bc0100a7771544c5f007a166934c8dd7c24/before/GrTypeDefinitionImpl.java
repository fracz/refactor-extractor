/*
 *  Copyright 2000-2007 JetBrains s.r.o.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.java.PomMemberOwner;
import com.intellij.psi.*;
import com.intellij.psi.impl.ElementBase;
import com.intellij.psi.impl.InheritanceImplUtil;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.Icons;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.GrModifierList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrExtendsClause;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrImplementsClause;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinitionBody;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMembersDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrWildcardTypeArgument;
import org.jetbrains.plugins.groovy.lang.psi.impl.GrClassReferenceType;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyFileImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiElementImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.arithmetic.TypesUtil;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.DefaultGroovyMethod;
import org.jetbrains.plugins.groovy.lang.resolve.CollectClassMembersUtil;
import org.jetbrains.plugins.groovy.lang.resolve.processors.ClassHint;

import javax.swing.*;
import java.util.*;

/**
 * @author ilyas
 */
public abstract class GrTypeDefinitionImpl extends GroovyPsiElementImpl implements GrTypeDefinition {
  private GrMethod[] myMethods;
  private GrMethod[] myConstructors;

  public GrTypeDefinitionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitTypeDefinition(this);
  }

  public int getTextOffset() {
    return getNameIdentifierGroovy().getTextRange().getStartOffset();
  }

  @Nullable
  public String getQualifiedName() {
    PsiElement parent = getParent();
    if (parent instanceof GrTypeDefinition) {
      return ((GrTypeDefinition) parent).getQualifiedName() + "." + getName();
    } else if (parent instanceof GroovyFile) {
      String packageName = ((GroovyFile) parent).getPackageName();
      return packageName.length() > 0 ? packageName + "." + getName() : getName();
    }

    return null;
  }

  public GrWildcardTypeArgument[] getTypeParametersGroovy() {
    return findChildrenByClass(GrWildcardTypeArgument.class);
  }

  public GrTypeDefinitionBody getBody() {
    return this.findChildByClass(GrTypeDefinitionBody.class);
  }

  @Nullable
  public GrMembersDeclaration[] getMemberDeclarations() {
    GrTypeDefinitionBody body = getBody();
    if (body == null) return null;
    return body.getMemberDeclarations();
  }

  public ItemPresentation getPresentation() {
    return new ItemPresentation() {

      public String getPresentableText() {
        return getName();
      }

      @Nullable
      public String getLocationString() {
        PsiFile file = getContainingFile();
        if (file instanceof GroovyFile) {
          GroovyFile groovyFile = (GroovyFile) file;

          return groovyFile.getPackageName().length() >0 ?
              "(" + groovyFile.getPackageName() +")" :
              "";
        }
        return "";
      }

      @Nullable
      public Icon getIcon(boolean open) {
        return Icons.SMALLEST;
      }

      @Nullable
      public TextAttributesKey getTextAttributesKey() {
        return null;
      }
    };
  }

  @Nullable
  public GrExtendsClause getExtendsClause() {
    return findChildByClass(GrExtendsClause.class);
  }

  @Nullable
  public GrImplementsClause getImplementsClause() {
    return findChildByClass(GrImplementsClause.class);
  }

  @NotNull
  public PsiElement getNameIdentifierGroovy() {
    PsiElement result = findChildByType(GroovyElementTypes.mIDENT);
    assert result != null;
    return result;
  }

  public void delete() throws IncorrectOperationException {
    PsiElement parent = getParent();
    if (parent instanceof GroovyFileImpl) {
      GroovyFileImpl file = (GroovyFileImpl) parent;
      if (file.getTypeDefinitions().length == 1 && !file.isScript()) {
        file.delete();
      }else {
        file.getNode().removeChild(getNode());
      }
      return;
    }

    throw new IncorrectOperationException("Invalid type definition");
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull PsiSubstitutor substitutor, PsiElement lastParent, @NotNull PsiElement place) {
/*
    for (final GrTypeParameter typeParameter : getTypeParameters()) {
      if (!ResolveUtil.processElement(processor, typeParameter)) return false;
    }
*/
    NameHint nameHint = processor.getHint(NameHint.class);
    String name = nameHint == null ? null : nameHint.getName();
    ClassHint classHint = processor.getHint(ClassHint.class);
    if (classHint == null || classHint.shouldProcess(ClassHint.ResolveKind.PROPERTY)) {
      Map<String, PsiField> fieldsMap = CollectClassMembersUtil.getAllFields(this);
      if (name != null) {
        PsiField field = fieldsMap.get(name);
        if (field != null && !processor.execute(field, PsiSubstitutor.EMPTY)) return false;
      } else {
        for (PsiField field : fieldsMap.values()) {
          if (!processor.execute(field, PsiSubstitutor.EMPTY)) return false;
        }
      }
    }

    if (classHint == null || classHint.shouldProcess(ClassHint.ResolveKind.METHOD)) {
      Map<String, List<PsiMethod>> methodsMap = CollectClassMembersUtil.getAllMethods(this);
      boolean isPlaceGroovy = place.getLanguage() == GroovyFileType.GROOVY_FILE_TYPE.getLanguage();
      if (name == null) {
        for (List<PsiMethod> list : methodsMap.values()) {
          for (PsiMethod method : list) {
            if (isMethodVisible(isPlaceGroovy, method) && !processor.execute(method, PsiSubstitutor.EMPTY)) return false;
          }
        }
      } else {
        List<PsiMethod> byName = methodsMap.get(name);
        if (byName != null) {
          for (PsiMethod method : byName) {
            if (isMethodVisible(isPlaceGroovy, method) && !processor.execute(method, PsiSubstitutor.EMPTY)) return false;
          }
        }

        final boolean isGetter = name.startsWith("get");
        final boolean isSetter = name.startsWith("set");
        if (isGetter || isSetter) {
          final String propName = StringUtil.decapitalize(name.substring(3));
          if (propName.length() > 0) {
            Map<String, PsiField> fieldsMap = CollectClassMembersUtil.getAllFields(this); //cached
            final PsiField aField = fieldsMap.get(propName);
            if (aField instanceof GrField && ((GrField) aField).isProperty() && isPropertyReference(place, aField, isGetter)) {
              if (!processor.execute(aField, PsiSubstitutor.EMPTY)) return false;
            }
          }
        }
      }
    }

    return true;
  }

  private boolean isPropertyReference(PsiElement place, PsiField aField, boolean isGetter) {
    //filter only in groovy, todo: analyze java place
    if (place.getLanguage() != GroovyFileType.GROOVY_FILE_TYPE.getLanguage()) return true;

    if (place instanceof GrReferenceExpression) {
      final PsiElement parent = place.getParent();
      if (parent instanceof GrMethodCallExpression) {
        final GrMethodCallExpression call = (GrMethodCallExpression) parent;
        if (call.getNamedArguments().length > 0 || call.getClosureArguments().length > 0) return false;
        final GrExpression[] args = call.getExpressionArguments();
        if (isGetter) {
          return args.length == 0;
        } else {
          return args.length == 1 &&
              TypesUtil.isAssignableByMethodCallConversion(aField.getType(), args[0].getType(),
                  place.getManager(), place.getResolveScope());
        }
      }
    }

    return false;
  }

  private boolean isMethodVisible(boolean isPlaceGroovy, PsiMethod method) {
    return isPlaceGroovy || !(method instanceof DefaultGroovyMethod);
  }

  @NotNull
  public String getName() {
    return getNameIdentifierGroovy().getText();
  }

  //Fake java class implementation
  public boolean isInterface() {
    return false;
  }

  public boolean isAnnotationType() {
    return false;
  }

  public boolean isEnum() {
    return false;
  }

  @Nullable
  public PsiReferenceList getExtendsList() {
    return null;
  }

  @Nullable
  public PsiReferenceList getImplementsList() {
    return null;
  }

  @NotNull
  public PsiClassType[] getExtendsListTypes() {
    GrExtendsClause extendsClause = getExtendsClause();

    PsiClassType[] result = PsiClassType.EMPTY_ARRAY;

    if (extendsClause != null) {
      GrCodeReferenceElement[] extendsRefElements = extendsClause.getReferenceElements();

      result = new PsiClassType[extendsRefElements.length];

      if (extendsRefElements.length > 0) {
        for (int j = 0; j < extendsRefElements.length; j++) {
          result[j] = new GrClassReferenceType(extendsRefElements[j]);
        }
      } else if (!isInterface()) {
        result[0] = getManager().getElementFactory().createTypeByFQClassName("groovy.lang.GroovyObject", getResolveScope());
      }
    }

    return result;
  }

  @NotNull
  public PsiClassType[] getImplementsListTypes() {
    GrImplementsClause implementsClause = getImplementsClause();

    PsiClassType[] result = PsiClassType.EMPTY_ARRAY;
    if (implementsClause != null) {
      GrCodeReferenceElement[] implementsRefElements = implementsClause.getReferenceElements();

      result = new PsiClassType[implementsRefElements.length];

      for (int j = 0; j < implementsRefElements.length; j++) {
        result[j] = new GrClassReferenceType(implementsRefElements[j]);
      }
    }

    return result;
  }

  @Nullable
  public PsiClass getSuperClass() {
    final PsiClassType[] superTypes = getSuperTypes();
    if (superTypes.length > 0) {
      return superTypes[0].resolve();
    }
    return null;
  }

  public PsiClass[] getInterfaces() {
    GrImplementsClause implementsClause = findChildByClass(GrImplementsClause.class);
    if (implementsClause != null) {
      final GrCodeReferenceElement[] refs = implementsClause.getReferenceElements();
      List<PsiClass> result = new ArrayList<PsiClass>(refs.length);
      for (GrCodeReferenceElement ref : refs) {
        final PsiElement resolved = ref.resolve();
        if (resolved instanceof PsiClass) result.add((PsiClass) resolved);
      }

      return result.toArray(new PsiClass[result.size()]);
    }
    return PsiClass.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClass[] getSupers() {
    PsiClassType[] superTypes = getSuperTypes();
    List<PsiClass> result = new ArrayList<PsiClass>();
    for (PsiClassType superType : superTypes) {
      PsiClass superClass = superType.resolve();
      if (superClass != null) {
        result.add(superClass);
      }
    }

    return result.toArray(new PsiClass[result.size()]);
  }

  @NotNull
  public PsiClassType[] getSuperTypes() {
    GrExtendsClause extendsClause = findChildByClass(GrExtendsClause.class);
    GrCodeReferenceElement[] extendsRefs = GrCodeReferenceElement.EMPTY_ARRAY;
    GrCodeReferenceElement[] implementsRefs = GrCodeReferenceElement.EMPTY_ARRAY;
    if (extendsClause != null) {
      extendsRefs = extendsClause.getReferenceElements();
    }

    GrImplementsClause implementsClause = findChildByClass(GrImplementsClause.class);
    if (implementsClause != null) {
      implementsRefs = implementsClause.getReferenceElements();
    }

    int len = implementsRefs.length + extendsRefs.length;
    if (!isInterface() && extendsRefs.length == 0) {
      len++;
    }
    PsiClassType[] result = new PsiClassType[len];

    int i = 0;
    if (extendsRefs.length > 0) {
      for (int j = 0; j < extendsRefs.length; j++) {
        result[j] = new GrClassReferenceType(extendsRefs[j]);
      }
      i = extendsRefs.length;
    } else if (!isInterface()) {
      result[0] = getManager().getElementFactory().createTypeByFQClassName(DEFAULT_BASE_CLASS_NAME, getResolveScope());
      i = 1;
    }

    for (int j = 0; j < implementsRefs.length; i++, j++) {
      result[i] = new GrClassReferenceType(implementsRefs[j]);
    }

    return result;
  }

  @NotNull
  public GrField[] getFields() {
    GrTypeDefinitionBody body = getBody();
    if (body != null) {
      return body.getFields();
    }

    return GrField.EMPTY_ARRAY;
  }

  @NotNull
  public GrMethod[] getMethods() {
    if (myMethods == null) {
      GrTypeDefinitionBody body = getBody();
      if (body != null) {
        myMethods = body.getMethods();
      } else {
        myMethods = GrMethod.EMPTY_ARRAY;
      }
    }
    return myMethods;
  }

  public void subtreeChanged() {
    myMethods = null;
    myConstructors = null;
    super.subtreeChanged();
  }

  @NotNull
  public PsiMethod[] getConstructors() {
    if (myConstructors == null) {
      List<GrMethod> result = new ArrayList<GrMethod>();
      for (final GrMethod method : getMethods()) {
        if (method.isConstructor()) {
          result.add(method);
        }
      }

      myConstructors = result.toArray(new GrMethod[result.size()]);
      return myConstructors;
    }
    return myConstructors;
  }

  @NotNull
  public PsiClass[] getInnerClasses() {
    return PsiClass.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClassInitializer[] getInitializers() {
    return PsiClassInitializer.EMPTY_ARRAY;
  }

  @NotNull
  public PsiField[] getAllFields() {
    return PsiField.EMPTY_ARRAY;
  }

  @NotNull
  public PsiMethod[] getAllMethods() {
    return PsiMethod.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClass[] getAllInnerClasses() {
    return PsiClass.EMPTY_ARRAY;
  }

  @Nullable
  public PsiField findFieldByName(String name, boolean checkBases) {
    if (!checkBases) {
      for (GrField field : getFields()) {
        if (name.equals(field.getName())) return field;
      }

      return null;
    }

    Map<String, PsiField> fieldsMap = CollectClassMembersUtil.getAllFields(this);
    return fieldsMap.get(name);
  }

  @Nullable
  public PsiMethod findMethodBySignature(PsiMethod patternMethod, boolean checkBases) {
    return null;
  }

  @NotNull
  public PsiMethod[] findMethodsBySignature(PsiMethod patternMethod, boolean checkBases) {
    return PsiMethod.EMPTY_ARRAY;
  }

  @NotNull
  public PsiMethod[] findMethodsByName(@NonNls String name, boolean checkBases) {
    if (!checkBases) {
      List<PsiMethod> result = new ArrayList<PsiMethod>();
      for (GrMethod method : getMethods()) {
        if (name.equals(method.getName())) result.add(method);
      }

      return result.toArray(new PsiMethod[result.size()]);
    }

    Map<String, List<PsiMethod>> methodsMap = CollectClassMembersUtil.getAllMethods(this);
    List<PsiMethod> list = methodsMap.get(name);
    return list == null ? PsiMethod.EMPTY_ARRAY : list.toArray(new PsiMethod[list.size()]);
  }

  @NotNull
  public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(String name, boolean checkBases) {
    return Collections.emptyList();
  }

  @NotNull
  public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors() {
    return Collections.emptyList();
  }

  @Nullable
  public PsiClass findInnerClassByName(String name, boolean checkBases) {
    return null;
  }

  @Nullable
  public PsiJavaToken getLBrace() {
    return null;
  }

  @Nullable
  public PsiJavaToken getRBrace() {
    return null;
  }

  @Nullable
  public PsiIdentifier getNameIdentifier() {
    return null;
  }

  public PsiElement getScope() {
    return null;
  }

  public boolean isInheritor(@NotNull PsiClass baseClass, boolean checkDeep) {
    return InheritanceImplUtil.isInheritor(this, baseClass, checkDeep);
  }

  public boolean isInheritorDeep(PsiClass baseClass, @Nullable PsiClass classToByPass) {
    return InheritanceImplUtil.isInheritorDeep(this, baseClass, classToByPass);
  }

  @Nullable
  public PomMemberOwner getPom() {
    return null;
  }

  @Nullable
  public PsiClass getContainingClass() {
    return null;
  }

  @NotNull
  public Collection<HierarchicalMethodSignature> getVisibleSignatures() {
    return Collections.emptyList();
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    boolean renameFile = isRenameFileOnClassRenaming();

    PsiImplUtil.setName(name, getNameIdentifierGroovy());

    if (renameFile) {
      final GroovyFile file = (GroovyFile) getContainingFile();
      file.setName(name + "." + GroovyFileType.GROOVY_FILE_TYPE.getDefaultExtension());
    }

    return this;
  }

  @Nullable
  public PsiModifierList getModifierList() {
    return findChildByClass(GrModifierList.class);
  }

  public boolean hasModifierProperty(@NonNls @NotNull String name) {
    PsiModifierList modifierList = getModifierList();
    return modifierList != null && modifierList.hasModifierProperty(name);
  }

  @Nullable
  public PsiDocComment getDocComment() {
    return null;
  }

  public boolean isDeprecated() {
    return false;
  }

  @Nullable
  public PsiMetaData getMetaData() {
    return null;
  }

  public boolean isMetaEnough() {
    return false;
  }

  public boolean hasTypeParameters() {
    return false;
  }

  @Nullable
  public PsiTypeParameterList getTypeParameterList() {
    return null;
  }

  @NotNull
  public PsiTypeParameter[] getTypeParameters() {
    return PsiTypeParameter.EMPTY_ARRAY;
  }

  @Nullable
  public Icon getIcon(int flags)
  {
    Icon icon = getIconInner();
    final boolean isLocked = (flags & ICON_FLAG_READ_STATUS) != 0 && !isWritable();
    return ElementBase.createLayeredIcon(icon, ElementBase.getFlags(this, isLocked));
  }

  private Icon getIconInner() {
    if (isInterface())
      return Icons.INTERFACE;

    if (isAnnotationType())
      return Icons.ANNOTAION;

    if (isEnum())
      return Icons.ENUM;

    if (hasModifierProperty(PsiModifier.ABSTRACT))
      return Icons.ABSTRACT;

    return Icons.CLAZZ;
  }

  private boolean isRenameFileOnClassRenaming() {
    final PsiFile file = getContainingFile();
    if (!(file instanceof GroovyFile)) return false;
    final GroovyFile groovyFile = (GroovyFile) file;
    if (groovyFile.isScript()) return false;
    final GrTypeDefinition[] typeDefinitions = groovyFile.getTypeDefinitions();
    if (typeDefinitions.length > 1) return false;
    final String name = getName();
    final VirtualFile vFile = groovyFile.getVirtualFile();
    return vFile != null && name.equals(vFile.getNameWithoutExtension());
  }

}