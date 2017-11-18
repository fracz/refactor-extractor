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

package org.jetbrains.plugins.groovy.lang.psi.impl.statements;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrCondition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrWhileStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiElementImpl;

/**
 * @autor: ilyas
 */
public class GrWhileStatementImpl extends GroovyPsiElementImpl implements GrWhileStatement {
  public GrWhileStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "WHILE statement";
  }

  public GrCondition getCondition() {
    GroovyPsiElement condition = findChildByClass(GrCondition.class);
    if (condition != null) {
      return (GrCondition) condition;
    }
    return null;
  }

  public GrStatement getBody() {
    GroovyPsiElement[] statements = findChildrenByClass(GrCondition.class);
    if (statements.length == 2 && (statements[1] instanceof GrStatement)) {
      return (GrStatement) statements[1];
    }
    return null;
//    return findChildByClass(GrOpenBlock.class);
  }
}