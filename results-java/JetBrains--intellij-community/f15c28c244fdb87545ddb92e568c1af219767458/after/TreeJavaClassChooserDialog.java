/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
package com.intellij.ide.util;

import com.google.common.collect.Lists;
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * @author traff
 */
public class TreeJavaClassChooserDialog extends AbstractTreeClassChooserDialog<PsiClass> implements TreeClassChooser {
  public TreeJavaClassChooserDialog(String title, Project project) {
    super(title, project);
  }

  public TreeJavaClassChooserDialog(String title, Project project, @Nullable PsiClass initialClass) {
    super(title, project, initialClass);
  }

  public TreeJavaClassChooserDialog(String title,
                                    Project project,
                                    GlobalSearchScope scope,
                                    final ClassFilter classFilter, @Nullable PsiClass initialClass) {
    super(title, project, scope, createFilter(classFilter), initialClass);
  }


  @Override
  @Nullable
  protected PsiClass getSelectedFromTreeUserObject(DefaultMutableTreeNode node) {
    Object userObject = node.getUserObject();
    if (!(userObject instanceof ClassTreeNode)) return null;
    ClassTreeNode descriptor = (ClassTreeNode)userObject;
    return descriptor.getPsiClass();
  }

  public TreeJavaClassChooserDialog(String title,
                                    Project project,
                                    GlobalSearchScope scope,
                                    final ClassFilter classFilter,
                                    PsiClass baseClass,
                                    @Nullable PsiClass initialClass, boolean isShowMembers) {
    super(title, project, scope, createFilter(classFilter), baseClass, initialClass, isShowMembers);
  }

  public static TreeJavaClassChooserDialog withInnerClasses(String title,
                                                            Project project,
                                                            GlobalSearchScope scope,
                                                            final ClassFilter classFilter,
                                                            @Nullable PsiClass initialClass) {
    return new TreeJavaClassChooserDialog(title, project, scope, classFilter, null, initialClass, true);
  }

  @Nullable
  private static Filter<PsiClass> createFilter(@Nullable final ClassFilter classFilter) {
    if (classFilter == null) {
      return null;
    }
    else {
      return new Filter<PsiClass>() {
        @Override
        public boolean isAccepted(PsiClass element) {
          return classFilter.isAccepted(element);
        }
      };
    }
  }

  @NotNull
  protected List<PsiClass> getClassesByName(final String name,
                                            final boolean checkBoxState,
                                            final String pattern,
                                            final GlobalSearchScope searchScope) {
    final PsiShortNamesCache cache = JavaPsiFacade.getInstance(getProject()).getShortNamesCache();
    PsiClass[] classes =
      cache.getClassesByName(name, checkBoxState ? searchScope : GlobalSearchScope.projectScope(getProject()).intersectWith(searchScope));
    return Lists.newArrayList(classes);
  }

  @NotNull
  @Override
  protected BaseClassInheritorsProvider<PsiClass> getInheritorsProvider() {
    return new JavaInheritorsProvider(getProject(), getBaseClass(), getScope());
  }

  private static class JavaInheritorsProvider extends BaseClassInheritorsProvider<PsiClass> {
    private final Project myProject;

    public JavaInheritorsProvider(Project project, PsiClass baseClass, GlobalSearchScope scope) {
      super(baseClass, scope);
      myProject = project;
    }

    @NotNull
    @Override
    protected Query<PsiClass> searchForInheritors(PsiClass baseClass, GlobalSearchScope searchScope, boolean checkDeep) {
      return ClassInheritorsSearch.search(baseClass, searchScope, checkDeep);
    }

    @Override
    protected boolean isInheritor(PsiClass clazz, PsiClass baseClass, boolean checkDeep) {
      return clazz.isInheritor(baseClass, checkDeep);
    }

    @Override
    protected String[] getNames() {
      return JavaPsiFacade.getInstance(myProject).getShortNamesCache().getAllClassNames();
    }
  }

  public static class InheritanceJavaClassFilterImpl implements ClassFilter {
    private final PsiClass myBase;
    private final boolean myAcceptsSelf;
    private final boolean myAcceptsInner;
    private final Condition<? super PsiClass> myAdditionalCondition;

    public InheritanceJavaClassFilterImpl(PsiClass base,
                                          boolean acceptsSelf,
                                          boolean acceptInner,
                                          Condition<? super PsiClass> additionalCondition) {
      myAcceptsSelf = acceptsSelf;
      myAcceptsInner = acceptInner;
      if (additionalCondition == null) {
        additionalCondition = Conditions.alwaysTrue();
      }
      myAdditionalCondition = additionalCondition;
      myBase = base;
    }

    public boolean isAccepted(PsiClass aClass) {
      if (!myAcceptsInner && !(aClass.getParent() instanceof PsiJavaFile)) return false;
      if (!myAdditionalCondition.value(aClass)) return false;
      // we've already checked for inheritance
      return myAcceptsSelf || !aClass.getManager().areElementsEquivalent(aClass, myBase);
    }
  }
}