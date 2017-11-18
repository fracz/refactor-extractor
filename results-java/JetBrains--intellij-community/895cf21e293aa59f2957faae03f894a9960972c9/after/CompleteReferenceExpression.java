package org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiManager;
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;
import org.jetbrains.plugins.groovy.lang.resolve.processors.ResolverProcessor;
import org.jetbrains.plugins.groovy.lang.resolve.processors.ClassHint;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.completion.GroovyCompletionUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * @author ven
 */
public class CompleteReferenceExpression {
  public static Object[] getVariants(GrReferenceExpressionImpl refExpr) {
    Object[] propertyVariants = getVariantsImpl(refExpr, GrReferenceExpressionImpl.getMethodOrPropertyResolveProcessor(refExpr, null, true, true));
    PsiElement parent = refExpr.getParent();
    if (parent instanceof GrArgumentList) {
      GrExpression call = (GrExpression) parent.getParent(); //add named argument label variants
      PsiType type = call.getType();
      if (type instanceof PsiClassType) {
        PsiClass clazz = ((PsiClassType) type).resolve();
        if (clazz != null) {
          List<String> props = new ArrayList<String>();
          for (PsiMethod method : clazz.getAllMethods()) {
            if (PropertyUtil.isSimplePropertySetter(method)) {
              String prop = PropertyUtil.getPropertyName(method);
              if (prop != null) {
                props.add(prop);
              }
            }
          }

          if (props.size() > 0) {
            propertyVariants = ArrayUtil.mergeArrays(propertyVariants, props.toArray(new Object[props.size()]), Object.class);
          }

          propertyVariants = ArrayUtil.mergeArrays(propertyVariants, clazz.getFields(), Object.class);
        }
      }
    }


    if (refExpr.getKind() == GrReferenceExpressionImpl.Kind.TYPE_OR_PROPERTY) {
      ResolverProcessor classVariantsCollector = new ResolverProcessor(null, EnumSet.of(ClassHint.ResolveKind.CLASS_OR_PACKAGE), refExpr, true, PsiType.EMPTY_ARRAY);
      getVariantsImpl(refExpr, classVariantsCollector);
      GroovyResolveResult[] classVariants = classVariantsCollector.getCandidates();
      return ArrayUtil.mergeArrays(propertyVariants, ResolveUtil.mapToElements(classVariants), Object.class);
    }


    return propertyVariants;
  }

  static Object[] getVariantsImpl(GrReferenceExpression refExpr, ResolverProcessor processor) {
    GrExpression qualifier = refExpr.getQualifierExpression();
    if (qualifier == null) {
      ResolveUtil.treeWalkUp(refExpr, processor);
      qualifier = PsiImplUtil.getRuntimeQualifier(refExpr);
      if (qualifier != null) getVariantsFromQualifier(refExpr, processor, qualifier);
    } else {
      if (refExpr.getDotTokenType() != GroovyTokenTypes.mSPREAD_DOT) {
        getVariantsFromQualifier(refExpr, processor, qualifier);
      } else {
        getVariantsFromQualifierForSpreadOperator(refExpr, processor, qualifier);
      }
    }

    GroovyResolveResult[] candidates = processor.getCandidates();
    if (candidates.length == 0) return PsiNamedElement.EMPTY_ARRAY;
    PsiElement[] elements = ResolveUtil.mapToElements(candidates);
    String[] properties = addPretendedProperties(elements);
    final Object[] variants = GroovyCompletionUtil.getCompletionVariants(candidates);
    return ArrayUtil.mergeArrays(variants, properties, Object.class);
  }

  static void getVariantsFromQualifierForSpreadOperator(GrReferenceExpression refExpr, ResolverProcessor processor, GrExpression qualifier) {
    PsiType qualifierType = qualifier.getType();
    if (qualifierType instanceof PsiClassType) {
      PsiClassType.ClassResolveResult result = ((PsiClassType) qualifierType).resolveGenerics();
      PsiClass clazz = result.getElement();
      if (clazz != null) {
        PsiClass listClass = refExpr.getManager().findClass("java.util.List", refExpr.getResolveScope());
        if (listClass != null && listClass.getTypeParameters().length == 1) {
          PsiSubstitutor substitutor = TypeConversionUtil.getClassSubstitutor(clazz, listClass, result.getSubstitutor());
          if (substitutor != null) {
            PsiType componentType = substitutor.substitute(listClass.getTypeParameters()[0]);
            if (componentType != null) {
              getVariantsFromQualifierType(refExpr, processor, componentType, refExpr.getProject());
            }
          }
        }
      }
    } else if (qualifierType instanceof PsiArrayType) {
      getVariantsFromQualifierType(refExpr, processor, ((PsiArrayType) qualifierType).getComponentType(), refExpr.getProject());
    }
  }

  static String[] addPretendedProperties(PsiElement[] elements) {
    List<String> result = new ArrayList<String>();
    for (PsiElement element : elements) {
      if (element instanceof PsiMethod) {
        PsiMethod method = (PsiMethod) element;
        String propName = PropertyUtil.getPropertyName(method);
        if (propName != null) {
          result.add(propName);
        }
      }
    }

    return result.toArray(new String[result.size()]);
  }

  static void getVariantsFromQualifier(GrReferenceExpression refExpr, ResolverProcessor processor, GrExpression qualifier) {
    PsiType qualifierType = qualifier.getType();
    if (qualifierType == null) {
      if (qualifier instanceof GrReferenceExpression) {
        PsiElement resolved = ((GrReferenceExpression) qualifier).resolve();
        if (resolved instanceof PsiPackage) {
          resolved.processDeclarations(processor, PsiSubstitutor.EMPTY, null, refExpr);
        }
      }
    } else {
      Project project = qualifier.getProject();
      if (qualifierType instanceof PsiIntersectionType) {
        for (PsiType conjunct : ((PsiIntersectionType) qualifierType).getConjuncts()) {
          getVariantsFromQualifierType(refExpr, processor, conjunct, project);
        }
      } else {
        getVariantsFromQualifierType(refExpr, processor, qualifierType, project);
        if (qualifier instanceof GrReferenceExpression) {
          PsiElement resolved = ((GrReferenceExpression) qualifier).resolve();
          if (resolved instanceof PsiClass) { ////omitted .class
            GlobalSearchScope scope = refExpr.getResolveScope();
            PsiClass javaLangClass = PsiUtil.getJavaLangObject(resolved, scope);
            if (javaLangClass != null) {
              javaLangClass.processDeclarations(processor, PsiSubstitutor.EMPTY, null, refExpr);
            }
          }
        }
      }
    }
  }

  static void getVariantsFromQualifierType(GrReferenceExpression refExpr, ResolverProcessor processor, PsiType qualifierType, Project project) {
    if (qualifierType instanceof PsiClassType) {
      PsiClass qualifierClass = ((PsiClassType) qualifierType).resolve();
      if (qualifierClass != null) {
        qualifierClass.processDeclarations(processor, PsiSubstitutor.EMPTY, null, refExpr);
      }
    } else if (qualifierType instanceof PsiArrayType) {
      final GrTypeDefinition arrayClass = GroovyPsiManager.getInstance(project).getArrayClass();
      if (!arrayClass.processDeclarations(processor, PsiSubstitutor.EMPTY, null, refExpr)) return;
    }

    ResolveUtil.processDefaultMethods(qualifierType, processor, project);
  }
}