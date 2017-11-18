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
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrExtendsClause;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrImplementsClause;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinitionBody;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMembersDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementFactory;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;

/**
 * @autor: Dmitry.Krasilschikov
 * @date: 16.03.2007
 */
public class GrClassDefinitionImpl extends GrTypeDefinitionImpl implements GrClassDefinition {
  public GrClassDefinitionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "Class definition";
  }

  public GrMethod addMethod(@NotNull GrMethod method) throws IncorrectOperationException {

    GrTypeDefinitionBody body = getBody();
    if (body == null) return null;
    ASTNode methodNode = method.getNode();
    assert methodNode != null;

    ASTNode bodyNode = body.getNode();
    PsiElement brace = body.getRBrace();
    if (brace != null) {
      ASTNode anchor = brace.getNode();
      bodyNode.addChild(methodNode, anchor);
      bodyNode.addLeaf(GroovyTokenTypes.mNLS, "\n", anchor);
    } else {
      bodyNode.addChild(methodNode);
    }
    ASTNode treePrev = methodNode.getTreePrev();
    if (treePrev != null) {
      if (treePrev.getText().matches("(\\s*\n\\s*)+")) {
        bodyNode.removeChild(treePrev);
      }
      bodyNode.addLeaf(GroovyTokenTypes.mNLS, "\n\n", methodNode);
    }

    return method;

  }
}