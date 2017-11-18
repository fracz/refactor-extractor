/*
 * Copyright (c) 2004 JetBrains s.r.o. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of JetBrains or IntelliJ IDEA
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. JETBRAINS AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL JETBRAINS OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF JETBRAINS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package com.intellij.ide.projectView.impl.nodes;

import com.intellij.coverage.CoverageDataManager;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PackageElementNode extends ProjectViewNode<PackageElement> {
  public PackageElementNode(final Project project,
                            final PackageElement value,
                            final ViewSettings viewSettings) {
    super(project, value, viewSettings);
  }

  public PackageElementNode(final Project project,
                            final Object value,
                            final ViewSettings viewSettings) {
    this(project, (PackageElement)value, viewSettings);
  }

  public boolean contains(@NotNull final VirtualFile file) {
    if (!isUnderContent(file) || getValue() == null) {
      return false;
    }

    final PsiDirectory[] directories = getValue().getPackage().getDirectories();
    for (PsiDirectory directory : directories) {
      if (VfsUtil.isAncestor(directory.getVirtualFile(), file, false)) return true;
    }
    return false;
  }

  private boolean isUnderContent(final VirtualFile file) {
    PackageElement element = getValue();
    final Module module = element == null ? null : element.getModule();
    if (module == null) {
      return PackageUtil.projectContainsFile(getProject(), file, isLibraryElement());
    }
    else {
      return ModuleUtil.moduleContainsFile(module, file, isLibraryElement());
    }
  }

  private boolean isLibraryElement() {
    return getValue() != null && getValue().isLibraryElement();
  }

  @NotNull
  public Collection<AbstractTreeNode> getChildren() {
    final PackageElement value = getValue();
    if (value == null) return Collections.emptyList();
    final List<AbstractTreeNode> children = new ArrayList<AbstractTreeNode>();
    final Module module = value.getModule();
    final PsiPackage aPackage = value.getPackage();

    if (!getSettings().isFlattenPackages()) {

      final PsiPackage[] subpackages = PackageUtil.getSubpackages(aPackage, module, myProject, isLibraryElement());
      for (PsiPackage subpackage : subpackages) {
        PackageUtil.addPackageAsChild(children, subpackage, module, getSettings(), isLibraryElement());
      }
    }
    // process only files in package's drectories
    final GlobalSearchScope scopeToShow = PackageUtil.getScopeToShow(myProject, module, isLibraryElement());
    final PsiDirectory[] dirs = aPackage.getDirectories(scopeToShow);
    for (final PsiDirectory dir : dirs) {
      children.addAll(ProjectViewDirectoryHelper.getInstance(myProject).getDirectoryChildren(dir, getSettings(), false));
    }
    return children;
  }


  protected void update(final PresentationData presentation) {
    if (getValue() != null && getValue().getPackage().isValid()) {
      updateValidData(presentation);
    }
    else {
      setValue(null);
    }
  }

  private void updateValidData(final PresentationData presentation) {
    final PackageElement value = getValue();
    final PsiPackage aPackage = value.getPackage();
    final String qName = aPackage.getQualifiedName();
    final CoverageDataManager coverageManager = CoverageDataManager.getInstance(aPackage.getProject());
    final String coverageString = coverageManager.getPackageCoverageInformationString(qName, value.getModule());
    presentation.setLocationString(coverageString);

    if (!getSettings().isFlattenPackages()
        && getSettings().isHideEmptyMiddlePackages()
        && PackageUtil.isPackageEmpty(aPackage, value.getModule(), true, isLibraryElement())) {
      setValue(null);
      return;
    }

    PsiPackage parentPackage;
    if (getParentValue() instanceof PackageElement) {
      parentPackage = ((PackageElement)getParentValue()).getPackage();
    }
    else {
      parentPackage = null;
    }
    String name = PackageUtil.getNodeName(getSettings(), aPackage,parentPackage, qName, showFQName(aPackage));
    presentation.setPresentableText(name);

    presentation.setOpenIcon(Icons.PACKAGE_OPEN_ICON);
    presentation.setClosedIcon(Icons.PACKAGE_ICON);
  }

  private boolean showFQName(final PsiPackage aPackage) {
    return getSettings().isFlattenPackages() && aPackage.getQualifiedName().length() > 0;
  }

  public String getTestPresentation() {
    final PresentationData presentation = new PresentationData();
    update(presentation);
    return "PsiPackage: " + presentation.getPresentableText();
  }

  public boolean valueIsCut() {
    return getValue() != null && CopyPasteManager.getInstance().isCutElement(getValue().getPackage());
  }

  public VirtualFile[] getVirtualFiles() {
    final PsiDirectory[] directories = getValue().getPackage().getDirectories(PackageUtil.getScopeToShow(getProject(), getValue().getModule(), isLibraryElement()));
    final VirtualFile[] result = new VirtualFile[directories.length];
    for (int i = 0; i < directories.length; i++) {
      PsiDirectory directory = directories[i];
      result[i] = directory.getVirtualFile();
    }
    return result;
  }

  public boolean canRepresent(final Object element) {
    if (super.canRepresent(element)) return true;
    final PackageElement value = getValue();
    if (element instanceof PackageElement) {
      final PackageElement packageElement = (PackageElement)element;
      final PsiPackage otherPackage = packageElement.getPackage();
      final PsiPackage aPackage = value.getPackage();
      if (otherPackage != null && aPackage != null && Comparing.equal(otherPackage.getQualifiedName(), aPackage.getQualifiedName())) {
        return true;
      }
    }
    if (getValue() == null) return true;
    if (element instanceof PsiDirectory) {
      final PsiDirectory directory = (PsiDirectory)element;
      return Arrays.asList(getValue().getPackage().getDirectories()).contains(directory);
    }
    return false;
  }

  public int getWeight() {
    return 0;
  }

  @Override
  public String getTitle() {
    final PackageElement packageElement = getValue();
    if (packageElement != null) {
      return packageElement.getPackage().getQualifiedName();
    }
    else {
      return super.getTitle();
    }
  }
}