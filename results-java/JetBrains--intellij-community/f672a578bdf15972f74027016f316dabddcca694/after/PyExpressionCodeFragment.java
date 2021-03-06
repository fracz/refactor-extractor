package com.jetbrains.python.refactoring.changeSignature;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.impl.PyFileImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User : ktisha
 */

public class PyExpressionCodeFragment extends PyFileImpl implements PsiCodeFragment {

  private GlobalSearchScope myResolveScope;

  public PyExpressionCodeFragment(@NotNull final Project project,
                                  @NonNls final String name,
                                  @NotNull final CharSequence text) {
    super(((PsiManagerEx)PsiManager.getInstance(project)).getFileManager().createFileViewProvider(
      new LightVirtualFile(name, PythonFileType.INSTANCE, text), true));
    ((SingleRootFileViewProvider)getViewProvider()).forceCachedPsi(this);
  }

  @Override
  public void forceResolveScope(GlobalSearchScope scope) {
    myResolveScope = scope;
  }

  @Override
  public GlobalSearchScope getForcedResolveScope() {
    return myResolveScope;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
  }

  public boolean isAcceptedFor(@NotNull Class visitorClass) {
    return false;
  }
}