/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
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
package com.intellij.ide.macro;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.JavaDataAccessors;

public final class FileClassMacro extends Macro {
  public String getName() {
    return "FileClass";
  }

  public String getDescription() {
    return IdeBundle.message("macro.class.name");
  }

  public String expand(DataContext dataContext) {
    //Project project = (Project)dataContext.getData(DataConstants.PROJECT);
    //if (project == null) {
    //  return null;
    //}
    //VirtualFile file = (VirtualFile)dataContext.getData(DataConstantsEx.VIRTUAL_FILE);
    //if (file == null) {
    //  return null;
    //}
    //PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    //if (!(psiFile instanceof PsiJavaFile)) {
    //  return null;
    //}
    final PsiJavaFile javaFile = JavaDataAccessors.PSI_JAVA_FILE.from(dataContext);
    if (javaFile == null) return null;
    PsiClass[] classes = javaFile.getClasses();
    if (classes.length == 1) {
      return classes[0].getQualifiedName();
    }
    String fileName = javaFile.getVirtualFile().getNameWithoutExtension();
    for (int i = 0; i < classes.length; i++) {
      PsiClass aClass = classes[i];
      String name = aClass.getName();
      if (fileName.equals(name)) {
        return aClass.getQualifiedName();
      }
    }
    return null;
  }
}