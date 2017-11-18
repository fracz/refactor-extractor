package com.jetbrains.python.actions;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.Function;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Available on self.my_something when my_something is unresolved.
 * User: dcheryasov
 * Date: Apr 4, 2009 1:53:46 PM
 */
public class AddFieldQuickFix implements LocalQuickFix {

  private PyClass myQualifierClass;
  private String myIdentifier;

  public AddFieldQuickFix(String identifier, PyClass qualifierClass) {
    myIdentifier = identifier;
    myQualifierClass = qualifierClass;
  }

  @NotNull
  public String getName() {
    return PyBundle.message("QFIX.NAME.add.field.$0.to.class.$1", myIdentifier, myQualifierClass.getName());
  }

  @NotNull
  public String getFamilyName() {
    return PyBundle.message("INSP.GROUP.python");
  }

  private static PsiElement appendToInit(PyFunction init, Function<String, PyStatement> callback) {
    // add this field as the last stmt of the constructor
    final PyStatementList stmt_list = init.getStatementList();
    PyStatement[] stmts = stmt_list.getStatements(); // NOTE: rather wasteful, consider iterable stmt list
    PyStatement last_stmt = null;
    if (stmts.length > 0) last_stmt = stmts[stmts.length-1];
    // name of 'self' may be different for fancier styles
    PyParameter[] params = init.getParameterList().getParameters();
    String self_name = PyNames.CANONICAL_SELF;
    if (params.length > 0) {
      self_name = params[0].getName();
    }
    PyStatement new_stmt = callback.fun(self_name);
    PyUtil.ensureWritable(stmt_list);
    final PsiElement result = stmt_list.addAfter(new_stmt, last_stmt);
    PyPsiUtils.removeRedundantPass(stmt_list);
    return result;
  }

  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    // expect the descriptor to point to the unresolved identifier.
    PyClass cls = myQualifierClass;
    String item_name = myIdentifier;
    if (cls != null) {
      PsiElement initStatement = addFieldToInit(project, cls, item_name, new CreateFieldCallback(project, item_name));
      if (initStatement != null) {
        showTemplateBuilder(initStatement);
        return;
      }
    }
    // somehow we failed. tell about this
    PyUtil.showBalloon(project, PyBundle.message("QFIX.failed.to.add.field"), MessageType.ERROR);
  }

  private static void showTemplateBuilder(PsiElement initStatement) {
    initStatement = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(initStatement);
    if (initStatement instanceof PyAssignmentStatement) {
      final TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(initStatement);
      builder.replaceElement(((PyAssignmentStatement) initStatement).getAssignedValue(), "None");
      builder.run();
    }
  }

  public static PsiElement addFieldToInit(Project project, PyClass cls, String item_name, FieldCallback callback) {
    if (cls != null && item_name != null) {
      PyFunction init = cls.findMethodByName(PyNames.INIT, false);
      Language language = cls.getLanguage();
      if (language instanceof PythonLanguage) {
        PythonLanguage pythonLanguage = (PythonLanguage)language;
        PyElementGenerator generator = pythonLanguage.getElementGenerator();
        callback.setGenerator(generator);
        if (init != null) {
          return appendToInit(init, callback);
        }
        else { // no init! boldly copy ancestor's.
          for (PyClass ancestor : cls.iterateAncestors()) {
            init = ancestor.findMethodByName(PyNames.INIT, false);
            if (init != null) break;
          }
          PyFunction new_init = createInitMethod(project, cls, init, generator);

          appendToInit(new_init, callback);
          new_init.add(generator.createFromText(project, PsiWhiteSpace.class, "\n\n")); // after the last line

          PsiElement add_anchor = null;
          PyFunction[] meths = cls.getMethods();
          if (meths.length > 0) add_anchor = meths[0].getPrevSibling();
          PyStatementList cls_content = cls.getStatementList();
          new_init = (PyFunction) cls_content.addAfter(new_init, add_anchor);

          PyUtil.showBalloon(project, PyBundle.message("QFIX.added.constructor.$0.for.field.$1", cls.getName(), item_name), MessageType.INFO);
          return new_init.getStatementList().getStatements()[0];
          //else  // well, that can't be
        }
      }
    }
    return null;
  }

  private static PyFunction createInitMethod(Project project, PyClass cls, @Nullable PyFunction ancestorInit, PyElementGenerator generator) {
    // found it; copy its param list and make a call to it.
    PyUtil.ensureWritable(cls);
    String paramList = ancestorInit != null ? ancestorInit.getParameterList().getText() : "(self)";

    String functionText = "def " + PyNames.INIT + paramList + ":\n";
    if (cls.isNewStyleClass() && ancestorInit != null) {
      // form the super() call
      StringBuffer sb = new StringBuffer("super(");
      sb.append(cls.getName());
      PyParameter[] params = ancestorInit.getParameterList().getParameters();
      // NOTE: assume that we have at least the first param
      String self_name = params[0].getName();
      sb.append(", ").append(self_name).append(").").append(PyNames.INIT).append("(");
      boolean seen = false;
      for (int i = 1; i < params.length; i += 1) {
        if (seen) sb.append(", ");
        else seen = true;
        sb.append(params[i].getText());
      }
      sb.append(")");
      functionText += "    " + sb.toString();
    }
    else {
      functionText += "    pass";
    }

    return generator.createFromText(
      project, PyFunction.class, functionText,
      new int[]{0}
    );
  }

  public abstract static class FieldCallback implements Function<String, PyStatement> {
    protected PyElementGenerator myGenerator;

    public void setGenerator(PyElementGenerator generator) {
      myGenerator = generator;
    }
  }

  private static class CreateFieldCallback extends FieldCallback {
    private Project myProject;
    private String myItemName;


    private CreateFieldCallback(Project project, String itemName) {
      myProject = project;
      myItemName = itemName;
    }

    public PyStatement fun(String self_name) {
      return myGenerator.createFromText(myProject, PyStatement.class, self_name + "." + myItemName + " = None");
    }
  }
}