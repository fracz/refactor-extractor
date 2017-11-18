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

package org.jetbrains.plugins.groovy.refactoring;

import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrSuperReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrThisReferenceExpression;
import org.jetbrains.plugins.groovy.refactoring.introduceVariable.GroovyIntroduceVariableBase;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author ilyas
 */
public class GroovyNameSuggestionUtil {
  public static String[] suggestVariableNames(GrExpression expr,
                                              GroovyIntroduceVariableBase.Validator validator) {
    ArrayList<String> possibleNames = new ArrayList<String>();
    PsiType type = expr.getType();
    if (type != null && !PsiType.VOID.equals(type)) {
      String unboxed = PsiTypesUtil.unboxIfPossible(type.getCanonicalText());
      if (!unboxed.equals(type.getCanonicalText())) {
        String name = generateNameForBuiltInType(unboxed);
        name = validator.validateName(name, true);
        if (GroovyNamesUtil.isIdentifier(name)) {
          possibleNames.add(name);
        }
      } else {
        generateByType(expr.getType(), possibleNames, validator);
      }
    }
    generateNameByExpr(expr, possibleNames, validator);
    while (possibleNames.contains("")) {
      possibleNames.remove("");
    }
    if (possibleNames.size() == 0) {
      possibleNames.add(validator.validateName("var", true));
    }
    return possibleNames.toArray(new String[0]);
  }


  private static void generateNameByExpr(GrExpression expr, ArrayList<String> possibleNames, GroovyIntroduceVariableBase.Validator validator) {
    if (expr instanceof GrThisReferenceExpression) {
      possibleNames.add(validator.validateName("thisInstance", true));
    }
    if (expr instanceof GrSuperReferenceExpression) {
      possibleNames.add(validator.validateName("superInstance", true));
    }
    if (expr instanceof GrReferenceExpression && ((GrReferenceExpression) expr).getName() != null) {
      GrReferenceExpression refExpr = (GrReferenceExpression) expr;
      generateCamelNames(possibleNames, validator, refExpr.getName());
      possibleNames.remove(refExpr.getName());
    }
  }

  private static void generateByType(PsiType type, ArrayList<String> possibleNames, GroovyIntroduceVariableBase.Validator validator) {
    String typeName = type.getPresentableText();
    if (typeName.contains(".")) {
      typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
    }
    if (typeName.contains("<")) {
      typeName = typeName.substring(0, typeName.indexOf("<"));
    }
    if (typeName.equals("String")) {
      possibleNames.add(validator.validateName("s", true));
    }
    if (typeName.equals("Closure")) {
      possibleNames.add(validator.validateName("cl", true));
    }
    if (!typeName.substring(0, 1).equals(typeName.substring(0, 1).toLowerCase())) {
      generateCamelNames(possibleNames, validator, typeName);
    }
  }

  private static void generateCamelNames(ArrayList<String> possibleNames, GroovyIntroduceVariableBase.Validator validator, String typeName) {
    ArrayList<String> camelTokens = camelizeString(typeName);
    Collections.reverse(camelTokens);
    if (camelTokens.size() > 0) {
      String possibleName = "";
      for (String camelToken : camelTokens) {
        possibleName = camelToken + fromUpperLetter(possibleName);
        String candidate = validator.validateName(possibleName, true);
        // todo generify
        if (candidate.equals("class")) {
          candidate = validator.validateName("clazz", true);
        }
        if (!possibleNames.contains(candidate) && GroovyNamesUtil.isIdentifier(candidate)) {
          possibleNames.add(candidate);
        }
      }
    }
  }

  private static String generateNameForBuiltInType(String unboxed) {
    return unboxed.toLowerCase().substring(0, 1);
  }

  private static ArrayList<String> camelizeString(String str) {
    ArrayList<String> camelizedTokens = new ArrayList<String>();
    if (!GroovyNamesUtil.isIdentifier(str)) {
      return camelizedTokens;
    }
    String result = fromLowerLetter(str);
    while (result != "") {
      result = fromLowerLetter(result);
      String temp = "";
      while (!(result.length() == 0) && !result.substring(0, 1).toUpperCase().equals(result.substring(0, 1))) {
        temp += result.substring(0, 1);
        result = result.substring(1);
      }
      camelizedTokens.add(temp);
      temp = "";
    }
    return camelizedTokens;
  }

  private static String fromLowerLetter(String str) {
    if (str.length() == 0) return "";
    if (str.length() == 1) return str.toLowerCase();
    return str.substring(0, 1).toLowerCase() + str.substring(1);
  }

  private static String fromUpperLetter(String str) {
    if (str.length() == 0) return "";
    if (str.length() == 1) return str.toUpperCase();
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }


}