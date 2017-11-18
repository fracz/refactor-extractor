/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.lang.psi.util;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.TIntStack;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocComment;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocMemberReference;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyLexer;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.lexer.TokenSets;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrConstructorInvocation;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrTopLevelDefintion;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrIndexProperty;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrPropertySelection;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrMemberOwner;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrAccessorMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrEnumConstant;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.impl.GrClosureType;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GroovyScriptClass;

import java.util.*;

/**
 * @author ven
 */
public class PsiUtil {
  public static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil");

  @Nullable
  public static String getQualifiedReferenceText(GrCodeReferenceElement referenceElement) {
    StringBuilder builder = new StringBuilder();
    if (!appendName(referenceElement, builder)) return null;

    return builder.toString();
  }

  private static boolean appendName(GrCodeReferenceElement referenceElement, StringBuilder builder) {
    String refName = referenceElement.getReferenceName();
    if (refName == null) return false;
    GrCodeReferenceElement qualifier = referenceElement.getQualifier();
    if (qualifier != null) {
      appendName(qualifier, builder);
      builder.append(".");
    }

    builder.append(refName);
    return true;
  }

  public static boolean isLValue(GroovyPsiElement element) {
    if (element instanceof GrExpression) {
      PsiElement parent = element.getParent();
      if (parent instanceof GrListOrMap && !((GrListOrMap)parent).isMap()) {
        return isLValue((GroovyPsiElement)parent);
      }
      return parent instanceof GrAssignmentExpression && element.equals(((GrAssignmentExpression)parent).getLValue());
    }
    return false;
  }

  public static boolean isGetterName(String name) {
    if (name == null) return false;
    if (name.startsWith("get") && !name.equals("get")) {
      String tail = StringUtil.trimStart("get", name);
      return tail.equals(StringUtil.capitalize(tail));

    }
    return false;
  }

  public static boolean isSetterName(String name) {
    if (name == null) return false;
    if (name.startsWith("set") && !name.equals("set")) {
      String tail = StringUtil.trimStart("set", name);
      return tail.equals(StringUtil.capitalize(tail));

    }
    return false;
  }

  public static boolean isApplicable(@Nullable PsiType[] argumentTypes,
                                     PsiMethod method,
                                     PsiSubstitutor substitutor,
                                     boolean isInUseCategory) {
    if (argumentTypes == null) return true;

    PsiParameter[] parameters = method.getParameterList().getParameters();
    if (isInUseCategory && method.hasModifierProperty(PsiModifier.STATIC) && parameters.length > 0) {
      //do not check first parameter, it is 'this' inside categorized block
      parameters = ArrayUtil.remove(parameters, 0);
    }

    PsiType[] parameterTypes = skipOptionalParametersAndSubstitute(argumentTypes.length, parameters, substitutor);
    if (parameterTypes.length - 1 > argumentTypes.length) return false; //one Map type might represent named arguments
    if (parameterTypes.length == 0 && argumentTypes.length > 0) return false;

    final GlobalSearchScope scope = method.getResolveScope();

    if (parameterTypes.length - 1 == argumentTypes.length) {
      final PsiType firstType = parameterTypes[0];
      final PsiClassType mapType = getMapType(method.getManager(), scope);
      if (mapType.isAssignableFrom(firstType)) {
        final PsiType[] trimmed = new PsiType[parameterTypes.length - 1];
        System.arraycopy(parameterTypes, 1, trimmed, 0, trimmed.length);
        parameterTypes = trimmed;
      } else if (!method.isVarArgs()) return false;
    }

    for (int i = 0; i < argumentTypes.length; i++) {
      PsiType argType = argumentTypes[i];
      PsiType parameterTypeToCheck;
      if (i < parameterTypes.length - 1) {
        parameterTypeToCheck = parameterTypes[i];
      } else {
        PsiType lastParameterType = parameterTypes[parameterTypes.length - 1];
        if (lastParameterType instanceof PsiArrayType && !(argType instanceof PsiArrayType)) {
          parameterTypeToCheck = ((PsiArrayType)lastParameterType).getComponentType();
        } else if (parameterTypes.length == argumentTypes.length) {
          parameterTypeToCheck = lastParameterType;
        } else {
          return false;
        }
      }

      if (!TypesUtil.isAssignableByMethodCallConversion(parameterTypeToCheck, argType, method.getManager(), scope)) return false;
    }

    return true;
  }

  public static PsiType[] skipOptionalParametersAndSubstitute(int argNum, PsiParameter[] parameters, PsiSubstitutor substitutor) {
    int diff = parameters.length - argNum;
    List<PsiType> result = new ArrayList<PsiType>(argNum);
    for (PsiParameter parameter : parameters) {
      if (diff > 0 && parameter instanceof GrParameter && ((GrParameter)parameter).isOptional()) {
        diff--;
        continue;
      }

      result.add(substitutor.substitute(parameter.getType()));
    }

    return result.toArray(new PsiType[result.size()]);
  }

  public static PsiClassType getMapType(PsiManager manager, GlobalSearchScope scope) {
    return JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createTypeByFQClassName("java.util.Map", scope);
  }

  @Nullable
  public static GroovyPsiElement getArgumentsElement(PsiElement methodRef) {
    PsiElement parent = methodRef.getParent();
    if (parent instanceof GrMethodCallExpression) {
      return ((GrMethodCallExpression)parent).getArgumentList();
    } else if (parent instanceof GrApplicationStatement) {
      return ((GrApplicationStatement)parent).getArgumentList();
    } else if (parent instanceof GrNewExpression) {
      return ((GrNewExpression)parent).getArgumentList();
    }
    return null;
  }

  // Returns arguments types not including Map for named arguments
  @Nullable
  public static PsiType[] getArgumentTypes(PsiElement place, boolean forConstructor, boolean nullAsBottom) {
    PsiElementFactory factory = JavaPsiFacade.getInstance(place.getProject()).getElementFactory();
    PsiElement parent = place.getParent();
    if (parent instanceof GrCallExpression) {
      List<PsiType> result = new ArrayList<PsiType>();
      GrCallExpression call = (GrCallExpression)parent;

      if (!forConstructor) {
        GrNamedArgument[] namedArgs = call.getNamedArguments();
        if (namedArgs.length > 0) {
          result.add(factory.createTypeByFQClassName("java.util.HashMap", place.getResolveScope()));
        }
      }

      GrExpression[] expressions = call.getExpressionArguments();
      for (GrExpression expression : expressions) {
        PsiType type = getArgumentType(expression);
        if (type == null) {
          result.add(nullAsBottom ? PsiType.NULL : TypesUtil.getJavaLangObject(call));
        } else {
          result.add(type);
        }
      }

      GrClosableBlock[] closures = call.getClosureArguments();
      for (GrClosableBlock closure : closures) {
        PsiType closureType = closure.getType();
        if (closureType != null) {
          result.add(closureType);
        }
      }

      return result.toArray(new PsiType[result.size()]);

    } else if (parent instanceof GrApplicationStatement) {
      final GrApplicationStatement call = (GrApplicationStatement)parent;
      GrExpression[] args = call.getArguments();
      final GrArgumentList argList = call.getArgumentList();
      GrNamedArgument[] namedArgs = argList != null ? argList.getNamedArguments() : GrNamedArgument.EMPTY_ARRAY;
      final ArrayList<PsiType> result = new ArrayList<PsiType>();
      if (namedArgs.length > 0) {
        result.add(factory.createTypeByFQClassName("java.util.HashMap", place.getResolveScope()));
      }
      for (int i = 0; i < args.length; i++) {
        PsiType argType = getArgumentType(args[i]);
        if (argType == null) {
          result.add(nullAsBottom ? PsiType.NULL : TypesUtil.getJavaLangObject((GroovyPsiElement)parent));
        } else {
          result.add(argType);
        }
      }
      return result.toArray(PsiType.EMPTY_ARRAY);
    } else if (parent instanceof GrConstructorInvocation || parent instanceof GrEnumConstant) {
      final GrArgumentList argList = (GrArgumentList)((GrCall)parent).getArgumentList();
      if (argList == null) return PsiType.EMPTY_ARRAY;

      List<PsiType> result = new ArrayList<PsiType>();
      if (argList.getNamedArguments().length > 0) {
        result.add(factory.createTypeByFQClassName("java.util.HashMap", place.getResolveScope()));
      }

      GrExpression[] expressions = argList.getExpressionArguments();
      for (GrExpression expression : expressions) {
        PsiType type = getArgumentType(expression);
        if (type == null) {
          result.add(nullAsBottom ? PsiType.NULL : TypesUtil.getJavaLangObject(argList));
        } else {
          result.add(type);
        }
      }

      return result.toArray(new PsiType[result.size()]);
    }

    return null;
  }

  private static PsiType getArgumentType(GrExpression expression) {
    if (expression instanceof GrReferenceExpression) {
      final PsiElement resolved = ((GrReferenceExpression)expression).resolve();
      if (resolved instanceof PsiClass) {
        //this argument is passed as java.lang.Class
        return JavaPsiFacade.getInstance(resolved.getProject()).getElementFactory()
          .createTypeByFQClassName("java.lang.Class", expression.getResolveScope());
      }
    }

    return expression.getType();
  }

  public static SearchScope restrictScopeToGroovyFiles(final Computable<SearchScope> originalScopeComputation) { //important to compute originalSearchScope in read action!
    return ApplicationManager.getApplication().runReadAction(new Computable<SearchScope>() {
      public SearchScope compute() {
        final SearchScope originalScope = originalScopeComputation.compute();
        if (originalScope instanceof GlobalSearchScope) {
          return GlobalSearchScope
            .getScopeRestrictedByFileTypes((GlobalSearchScope)originalScope, GroovyFileType.GROOVY_FILE_TYPE, GspFileType.GSP_FILE_TYPE);
        }
        return originalScope;
      }
    });
  }

  public static PsiClass getJavaLangClass(PsiElement resolved, GlobalSearchScope scope) {
    return JavaPsiFacade.getInstance(resolved.getProject()).findClass("java.lang.Class", scope);
  }

  public static boolean isValidReferenceName(String text) {
    final GroovyLexer lexer = new GroovyLexer();
    lexer.start(text);
    return TokenSets.REFERENCE_NAMES.contains(lexer.getTokenType()) && lexer.getTokenEnd() == text.length();
  }

  public static boolean isSimplePropertyAccessor(PsiMethod method) {
    return isSimplePropertyGetter(method) || isSimplePropertySetter(method);
  }

  //do not check return type
  public static boolean isSimplePropertyGetter(PsiMethod method) {
    return isSimplePropertyGetter(method, null);
  }

  //do not check return type
  public static boolean isSimplePropertyGetter(PsiMethod method, String propertyName) {
    if (method == null) return false;

    if (method.isConstructor()) return false;

    String methodName = method.getName();
    if (methodName.length() <= "is".length()) return false;

    if (!methodName.startsWith("get") ||
        methodName.length() <= "get".length() ||
        !Character.isUpperCase(methodName.charAt("get".length()))) {
      if (!methodName.startsWith("is") || !Character.isUpperCase(methodName.charAt("is".length()))) return false;
    }

    if (propertyName != null && !checkPropertyName(method, propertyName)) return false;

    return method.getParameterList().getParameters().length == 0;
  }

  private static boolean checkPropertyName(PsiMethod method, @NotNull String propertyName) {
    String methodName = method.getName();
    String accessorNamePart;
    if (method instanceof GrAccessorMethod) {
      accessorNamePart = ((GrAccessorMethod)method).getProperty().getName();
    } else {
      accessorNamePart = methodName.startsWith("is") ? methodName.substring(2) : methodName.substring(3); //"set" or "get" or "is"
      if (Character.isLowerCase(accessorNamePart.charAt(0))) return false;
      accessorNamePart = StringUtil.decapitalize(accessorNamePart);
    }

    if (!propertyName.equals(accessorNamePart)) return false;
    return true;
  }

  public static boolean isSimplePropertySetter(PsiMethod method) {
    return isSimplePropertySetter(method, null);
  }

  public static boolean isSimplePropertySetter(PsiMethod method, String propertyName) {
    if (method == null) return false;

    if (method.isConstructor()) return false;

    String methodName = method.getName();

    if (!methodName.startsWith("set") ||
        methodName.length() <= "set".length() ||
        !Character.isUpperCase(methodName.charAt("set".length()))) {
      return false;
    }

    if (propertyName != null && !checkPropertyName(method, propertyName)) return false;

    return method.getParameterList().getParametersCount() == 1;
  }

  public static void shortenReferences(GroovyPsiElement element) {
    doShorten(element);
  }

  private static void doShorten(PsiElement element) {
    PsiElement child = element.getFirstChild();
    while (child != null) {
      if (child instanceof GrCodeReferenceElement) {
        shortenReference((GrCodeReferenceElement)child);
      }

      doShorten(child);
      child = child.getNextSibling();
    }
  }

  public static void shortenReference(GrCodeReferenceElement ref) {
    if (ref.getQualifier() != null && mayShorten(ref)) {
      final PsiElement resolved = ref.resolve();
      if (resolved instanceof PsiClass) {
        ref.setQualifier(null);
        try {
          ref.bindToElement(resolved);
        }
        catch (IncorrectOperationException e) {
          LOG.error(e);
        }
      }
    }
  }

  private static boolean mayShorten(GrCodeReferenceElement ref) {
    if (PsiTreeUtil.getParentOfType(ref, GrDocMemberReference.class) != null) return true;
    return PsiTreeUtil.getParentOfType(ref, GrDocComment.class) == null;
  }

  @Nullable
  public static GrTopLevelDefintion findPreviousTopLevelElementByThisElement(PsiElement element) {
    PsiElement parent = element.getParent();

    while (parent != null && !(parent instanceof GrTopLevelDefintion)) {
      parent = parent.getParent();
    }

    if (parent == null) return null;
    return ((GrTopLevelDefintion)parent);
  }

  public static String getPropertyNameByGetter(PsiMethod getterMethod) {
    @NonNls String methodName = getterMethod.getName();
    return methodName.startsWith("get") && methodName.length() > 3 ? decapitalize(methodName.substring(3)) : methodName;
  }

  public static String getPropertyNameBySetter(PsiMethod setterMethod) {
    @NonNls String methodName = setterMethod.getName();
    return methodName.startsWith("set") && methodName.length() > 3 ? decapitalize(methodName.substring(3)) : methodName;
  }

  private static String decapitalize(String s) {
    final char[] chars = s.toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return new String(chars);
  }

  public static boolean isStaticsOK(PsiModifierListOwner owner, PsiElement place) {
    if (!owner.hasModifierProperty(PsiModifier.STATIC)) {
      if (place instanceof GrReferenceExpression) {
        GrExpression qualifier = ((GrReferenceExpression)place).getQualifierExpression();
        if (qualifier instanceof GrReferenceExpression) {
          PsiElement qualifierResolved = ((GrReferenceExpression)qualifier).resolve();
          if (qualifierResolved instanceof PsiClass) {

            if (owner instanceof PsiMember) {
              //members from java.lang.Class can be invoked without ".class"
              PsiClass javaLangClass = JavaPsiFacade.getInstance(place.getProject()).findClass("java.lang.Class", place.getResolveScope());
              if (javaLangClass != null) {
                PsiClass containingClass = ((PsiMember)owner).getContainingClass();
                if (containingClass == null || //default groovy method
                    InheritanceUtil.isInheritorOrSelf(javaLangClass, containingClass, true)) {
                  return true;
                }
              }
            }
            return false;
          }
        }
      }
    }

    return true;
  }

  public static boolean isAccessible(PsiElement place, PsiMember member) {

    if (PsiTreeUtil.getParentOfType(place, GrDocComment.class) != null) return true;

    if (place instanceof GrReferenceExpression && ((GrReferenceExpression)place).getQualifierExpression() == null) {
      if (member.getContainingClass() instanceof GroovyScriptClass) { //calling toplevel script members from the same script file
        return true;
      }
    }
    return com.intellij.psi.util.PsiUtil.isAccessible(member, place, null);
  }

  public static void reformatCode(final PsiElement element) {
    final TextRange textRange = element.getTextRange();
    try {
      CodeStyleManager.getInstance(element.getProject())
        .reformatText(element.getContainingFile(), textRange.getStartOffset(), textRange.getEndOffset());
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  public static boolean isInStaticContext(GrReferenceExpression refExpression, GrMemberOwner targetClass) {
    if (refExpression.isQualified()) {
      GrExpression qualifer = refExpression.getQualifierExpression();
      if (qualifer instanceof GrReferenceExpression) return ((GrReferenceExpression)qualifer).resolve() instanceof PsiClass;
    } else {
      PsiElement run = refExpression;
      while (run != null && run != targetClass) {
        if (run instanceof PsiModifierListOwner && ((PsiModifierListOwner)run).hasModifierProperty(PsiModifier.STATIC)) return true;
        run = run.getParent();
      }
    }
    return false;

  }

  public static Iterable<PsiClass> iterateSupers(final @NotNull PsiClass psiClass, final boolean includeSelf) {
    return new Iterable<PsiClass>() {
      public Iterator<PsiClass> iterator() {
        return new Iterator<PsiClass>() {
          TIntStack indices = new TIntStack();
          Stack<PsiClassType[]> superTypesStack = new Stack<PsiClassType[]>();
          PsiClass current;
          boolean nextObtained;
          Set<PsiClass> visited = new com.intellij.util.containers.HashSet<PsiClass>();

          {
            if (includeSelf) {
              current = psiClass;
              nextObtained = true;
            } else {
              current = null;
              nextObtained = false;
            }

            pushSuper(psiClass);
          }

          public boolean hasNext() {
            nextElement();
            return current != null;
          }

          private void nextElement() {
            if (nextObtained) return;

            nextObtained = true;
            while (!superTypesStack.empty()) {
              assert indices.size() > 0;

              int i = indices.pop();
              PsiClassType[] superTypes = superTypesStack.peek();
              while (i < superTypes.length) {
                PsiClass clazz = superTypes[i].resolve();
                if (clazz != null && !visited.contains(clazz)) {
                  current = clazz;
                  visited.add(clazz);
                  indices.push(i + 1);
                  pushSuper(clazz);
                  return;
                }
                i++;
              }

              superTypesStack.pop();
            }

            current = null;
          }

          private void pushSuper(PsiClass clazz) {
            superTypesStack.push(clazz.getSuperTypes());
            indices.push(0);
          }

          @NotNull
          public PsiClass next() {
            nextElement();
            nextObtained = false;
            if (current == null) throw new NoSuchElementException();
            return current;
          }

          public void remove() {
            throw new IllegalStateException("should not be called");
          }
        };
      }
    };
  }

  public static PsiClass getContextClass(PsiElement context) {
    GroovyPsiElement parent = PsiTreeUtil.getParentOfType(context, GrTypeDefinition.class, GroovyFileBase.class);
    if (parent instanceof GrTypeDefinition) {
      return (PsiClass)parent;
    } else if (parent instanceof GroovyFileBase) return ((GroovyFileBase)parent).getScriptClass();
    return null;
  }

  public static boolean mightBeLVlaue(GrExpression expr) {
    if (expr instanceof GrParenthesizedExpression) return mightBeLVlaue(((GrParenthesizedExpression)expr).getOperand());

    if (expr instanceof GrListOrMap) {
      GrListOrMap listOrMap = (GrListOrMap)expr;
      if (listOrMap.isMap()) return false;
      GrExpression[] initializers = listOrMap.getInitializers();
      for (GrExpression initializer : initializers) {
        if (!mightBeLVlaue(initializer)) return false;
      }
      return true;
    }
    if (expr instanceof GrTupleExpression) return true;
    return expr instanceof GrReferenceExpression || expr instanceof GrIndexProperty || expr instanceof GrPropertySelection;
  }

  public static PsiType[] skipOptionalClosureParameters(int argsNum, GrClosureType closureType) {
    PsiType[] parameterTypes = closureType.getClosureParameterTypes();
    int diff = parameterTypes.length - argsNum;
    List<PsiType> result = new ArrayList<PsiType>(argsNum);
    for (int i = 0; i < parameterTypes.length; i++) {
      PsiType type = parameterTypes[i];
      if (diff > 0 && closureType.isOptionalParameter(i)) {
        diff--;
        continue;
      }

      result.add(type);
    }

    return result.toArray(new PsiType[result.size()]);
  }

  public static boolean isNewLine(PsiElement element) {
    if (element == null) return false;
    ASTNode node = element.getNode();
    if (node == null) return false;
    return node.getElementType() == GroovyTokenTypes.mNLS;
  }

  public static PsiElement getPrevNonSpace(final PsiElement elem) {
    PsiElement prevSibling = elem.getPrevSibling();
    while (prevSibling instanceof PsiWhiteSpace) {
      prevSibling = prevSibling.getPrevSibling();
    }
    return prevSibling;
  }

  public static PsiElement getNextNonSpace(final PsiElement elem) {
    PsiElement nextSibling = elem.getNextSibling();
    while (nextSibling instanceof PsiWhiteSpace) {
      nextSibling = nextSibling.getNextSibling();
    }
    return nextSibling;
  }
}