/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
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
 */

package org.jetbrains.plugins.groovy.refactoring.move;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.refactoring.MoveDestination;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassHandler;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.actions.GroovyTemplatesFactory;
import org.jetbrains.plugins.groovy.actions.NewGroovyActionBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyRecursiveElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.refactoring.GroovyChangeContextUtil;

/**
 * @author Maxim.Medvedev
 */
public class MoveGroovyClassHandler implements MoveClassHandler {
  public PsiClass doMoveClass(@NotNull PsiClass aClass, @NotNull MoveDestination moveDestination) throws IncorrectOperationException {
    if (!aClass.getLanguage().equals(GroovyFileType.GROOVY_LANGUAGE)) return null;
    PsiFile file = aClass.getContainingFile();
    PsiDirectory newDirectory = moveDestination.getTargetDirectory(file);
    final PsiPackage newPackage = JavaDirectoryService.getInstance().getPackage(newDirectory);

    GroovyChangeContextUtil.encodeContextInfo(aClass);

    PsiClass newClass = null;
    if (file instanceof GroovyFile) {
      if (((GroovyFile)file).isScript() || ((GroovyFile)file).getClasses().length > 1) {
        correctSelfReferences(aClass, newPackage);
        final PsiClass created = ((GroovyFile)GroovyTemplatesFactory
          .createFromTemplate(newDirectory, aClass.getName(), aClass.getName() + NewGroovyActionBase.GROOVY_EXTENSION,
                              "GroovyClass.groovy")).getClasses()[0];
        if (aClass.getDocComment() == null) {
          final PsiDocComment createdDocComment = created.getDocComment();
          if (createdDocComment != null) {
            aClass.addBefore(createdDocComment, null);
          }
        }
        newClass = (PsiClass)created.replace(aClass);
        correctOldClassReferences(newClass, aClass);
        aClass.delete();
      }
      else if (!newDirectory.equals(file.getContainingDirectory()) && newDirectory.findFile(file.getName()) != null) {
        // moving second of two classes which were in the same file to a different directory (IDEADEV-3089)
        correctSelfReferences(aClass, newPackage);
        PsiFile newFile = newDirectory.findFile(file.getName());
        TreeElement enter = Factory.createSingleLeafElement(GroovyTokenTypes.mNLS, "\n", 0, 1, null, aClass.getManager());
        newFile.getNode().addChild(enter);
        newClass = (PsiClass)newFile.add(aClass);
        aClass.delete();
      }
      else if (!newDirectory.equals(file.getContainingDirectory()) && newDirectory.findFile(file.getName()) == null) {
        if (!newDirectory.equals(file.getContainingDirectory())) {
          aClass.getManager().moveFile(file, newDirectory);
          newClass=((GroovyFile)file).getClasses()[0];
          if (newPackage != null) {
            ((PsiClassOwner)file).setPackageName(newPackage.getQualifiedName());
          }
        }
      }
    }
    if (newClass != null) GroovyChangeContextUtil.decodeContextInfo(newClass, null, null);
    return newClass;
  }

  private static void correctOldClassReferences(final PsiClass newClass, final PsiClass oldClass) {
    ((GroovyPsiElement)newClass).accept(new GroovyRecursiveElementVisitor() {
      @Override
      public void visitReferenceExpression(GrReferenceExpression reference) {
        if (reference.isReferenceTo(oldClass)) {
          try {
            reference.bindToElement(newClass);
          }
          catch (IncorrectOperationException e) {
            //here is an exception
          }
        }
        super.visitReferenceExpression(reference);
      }
    });
  }

  private static void correctSelfReferences(final PsiClass aClass, final PsiPackage newContainingPackage) {
    final PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(aClass.getContainingFile().getContainingDirectory());
    if (aPackage != null) {
      ((GroovyPsiElement)aClass).accept(new GroovyRecursiveElementVisitor() {
        @Override
        public void visitReferenceExpression(GrReferenceExpression reference) {
          if (reference.isQualified() && reference.isReferenceTo(aClass)) {
            final PsiElement qualifier = reference.getQualifier();
            if (qualifier instanceof PsiJavaCodeReferenceElement && ((PsiJavaCodeReferenceElement)qualifier).isReferenceTo(aPackage)) {
              try {
                ((PsiJavaCodeReferenceElement)qualifier).bindToElement(newContainingPackage);
              }
              catch (IncorrectOperationException e) {
                //here is an exception
              }
            }
          }
          super.visitReferenceExpression(reference);
        }
      });
    }
  }

  public String getName(PsiClass clazz) {
    final PsiFile file = clazz.getContainingFile();
    if (!(file instanceof GroovyFile)) return null;
    return ((GroovyFile)file).getClasses().length > 1 ? clazz.getName() + "." + GroovyFileType.DEFAULT_EXTENSION : file.getName();
  }

}