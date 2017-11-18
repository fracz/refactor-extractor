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

package org.jetbrains.plugins.groovy.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.ChangeUtil;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;

import java.util.Iterator;

/**
 * @author ilyas
 */
public class GroovyPsiElementImpl extends ASTWrapperPsiElement implements GroovyPsiElement {

  public GroovyPsiElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void replaceAsNode(PsiElement newExpr) throws IncorrectOperationException {
    if (getParent() == null ||
        getParent().getNode() == null ||
        newExpr.getNode() == null) {
      throw new IncorrectOperationException();
    }
    getParent().getNode().replaceChild(this.getNode(), newExpr.getNode());
  }

  public <T extends GroovyPsiElement> Iterable<T> childrenOfType(final TokenSet tokSet) {
    return new Iterable<T>() {

      public Iterator<T> iterator() {
        return new Iterator<T>() {
          private ASTNode findChild(ASTNode child) {
            if (child == null) return null;

            if (tokSet.contains(child.getElementType())) return child;

            return findChild(child.getTreeNext());
          }

          PsiElement first = getFirstChild();

          ASTNode n = first == null ? null : findChild(first.getNode());

          public boolean hasNext() {
            return n != null;
          }

          public T next() {
            if (n == null) return null;
            else {
              final ASTNode res = n;
              n = findChild(n.getTreeNext());
              return (T) res.getPsi();
            }
          }

          public void remove() {
          }
        };
      }
    };
  }
}