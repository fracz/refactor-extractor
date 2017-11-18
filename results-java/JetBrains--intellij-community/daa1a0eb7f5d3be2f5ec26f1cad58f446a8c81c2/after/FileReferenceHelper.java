/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.psi.impl.source.resolve.reference.impl.providers;

import com.intellij.codeInsight.daemon.QuickFixProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiReference;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public interface FileReferenceHelper<T extends PsiFileSystemItem> extends QuickFixProvider<FileReference> {
  @NotNull
  Class<T> getDirectoryClass();

  @NotNull String getDirectoryTypeName();

  boolean isDoNothingOnBind(PsiFile currentFile, final FileReference reference);

  @Nullable
  @NonNls
  String getRelativePath(Project project, VirtualFile currentFile, VirtualFile dstVFile);

  @Nullable
  PsiDirectory getPsiDirectory(T element);

  @Nullable
  T getAbsoluteTopLevelDirLocation(@NotNull final PsiFile file);

  @Nullable
  T getContainingDirectory(PsiFile file);

  @NotNull String trimUrl(@NotNull String url);

  @Nullable
  PsiReference createDynamicReference(PsiElement element, String str);

  @Nullable
  PsiFileSystemItem innerResolve(T element, String text, final Condition<String> equalsTo);

  boolean processVariants(T element, PsiScopeProcessor processor);

}