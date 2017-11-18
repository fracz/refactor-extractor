package com.jetbrains.python.actions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.psi.PyElementGenerator;
import com.jetbrains.python.psi.PyNamedParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Parameter renaming.
* User: dcheryasov
* Date: Nov 30, 2008 6:10:13 AM
*/
public class RenameParameterQuickFix implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance("#" + RenameParameterQuickFix.class.getName());
  private String myNewName;

  public RenameParameterQuickFix(String newName) {
    myNewName = newName;
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    final PsiElement elt = descriptor.getPsiElement();
    if (elt != null && elt instanceof PyNamedParameter && elt.isWritable()) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          final PyNamedParameter the_self = PyElementGenerator.getInstance(project).createParameter(myNewName);
          try {
            elt.replace(the_self);
          }
          catch (IncorrectOperationException e) {
            LOG.error(e);
          }
        }
      });
    }
  }

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("QFIX.NAME.parameters");
  }

  @NotNull
  public String getName() {
    return PyBundle.message("QFIX.rename.parameter.to.$0", myNewName);
  }
}