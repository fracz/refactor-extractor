/**
 * created at Sep 24, 2001
 * @author Jeka
 */
package com.intellij.refactoring.move.moveInner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.refactoring.HelpID;
import com.intellij.refactoring.JavaRefactoringSettings;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.move.MoveInstanceMembersUtil;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.intellij.refactoring.ui.NameSuggestionsField;
import com.intellij.refactoring.ui.RefactoringDialog;
import com.intellij.refactoring.ui.PackageNameReferenceEditorCombo;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.refactoring.util.RefactoringMessageUtil;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class MoveInnerDialog extends RefactoringDialog {
  private final Project myProject;
  private final PsiClass myInnerClass;
  private final PsiElement myTargetContainer;
  private final MoveInnerProcessor myProcessor;

  private EditorTextField myClassNameField;
  private NameSuggestionsField myParameterField;
  private JCheckBox myCbPassOuterClass;
  private JPanel myPanel;
  private JCheckBox myCbSearchInComments;
  private JCheckBox myCbSearchForTextOccurences;
  private PackageNameReferenceEditorCombo myPackageNameField;
  private JLabel myPackageNameLabel;
  private SuggestedNameInfo mySuggestedNameInfo;
  private final PsiClass myOuterClass;

  @NonNls private static final String RECENTS_KEY = "MoveInnerDialog.RECENTS_KEY";

  public MoveInnerDialog(Project project, PsiClass innerClass, MoveInnerProcessor processor, final PsiElement targetContainer) {
    super(project, true);
    myProject = project;
    myInnerClass = innerClass;
    myTargetContainer = targetContainer;
    myOuterClass = myInnerClass.getContainingClass();
    myProcessor = processor;
    setTitle(MoveInnerImpl.REFACTORING_NAME);
    init();
  }

  public boolean isSearchInComments() {
    return myCbSearchInComments.isSelected();
  }

  public boolean isSearchInNonJavaFiles() {
    return myCbSearchForTextOccurences.isSelected();
  }

  public String getClassName() {
    return myClassNameField.getText().trim();
  }

  @Nullable
  public String getParameterName() {
    if (myParameterField != null) {
      return myParameterField.getEnteredName();
    }
    else {
      return null;
    }
  }

  public boolean isPassOuterClass() {
    return myCbPassOuterClass.isSelected();
  }

  @NotNull
  public PsiClass getInnerClass() {
    return myInnerClass;
  }

  protected void init() {
    myClassNameField.setText(myInnerClass.getName());
    myClassNameField.selectAll();

    if (!myInnerClass.hasModifierProperty(PsiModifier.STATIC)) {
      myCbPassOuterClass.setSelected(true);
      myCbPassOuterClass.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          myParameterField.setEnabled(myCbPassOuterClass.isSelected());
        }
      });
    }
    else {
      myCbPassOuterClass.setSelected(false);
      myCbPassOuterClass.setEnabled(false);
      myParameterField.setEnabled(false);
    }

    if (myCbPassOuterClass.isEnabled()) {
      final boolean thisNeeded = MoveInstanceMembersUtil.getThisClassesToMembers(myInnerClass).containsKey(myOuterClass);
      myCbPassOuterClass.setSelected(thisNeeded);
      myParameterField.setEnabled(thisNeeded);
    }

    myCbPassOuterClass.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean selected = myCbPassOuterClass.isSelected();
        myParameterField.getComponent().setEnabled(selected);
      }
    });

    if (!(myTargetContainer instanceof PsiDirectory)) {
      myPackageNameField.setVisible(false);
      myPackageNameLabel.setVisible(false);
    }

    super.init();
  }

  public JComponent getPreferredFocusedComponent() {
    return myClassNameField;
  }

  protected String getDimensionServiceKey() {
    return "#com.intellij.refactoring.move.moveInner.MoveInnerDialog";
  }

  protected JComponent createNorthPanel() {
    return myPanel;
  }

  protected JComponent createCenterPanel() {
    return null;
  }

  @Nullable
  private PsiElement getTargetContainer() {
    if (myTargetContainer instanceof PsiDirectory) {
      final PsiDirectory psiDirectory = (PsiDirectory)myTargetContainer;
      PsiPackage oldPackage = getTargetPackage();
      String name = oldPackage == null ? "" : oldPackage.getQualifiedName();
      final String targetName = myPackageNameField.getText();
      if (!Comparing.equal(name, targetName)) {
        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(myProject);
        final VirtualFile[] contentSourceRoots = projectRootManager.getContentSourceRoots();
        final PackageWrapper newPackage = new PackageWrapper(PsiManager.getInstance(myProject), targetName);
        final VirtualFile targetSourceRoot;
        if (contentSourceRoots.length > 1) {
          PsiDirectory initialDir = null;
          if (oldPackage != null) {
            final PsiDirectory[] directories = oldPackage.getDirectories();
            final VirtualFile root = projectRootManager.getFileIndex().getContentRootForFile(psiDirectory.getVirtualFile());
            for(PsiDirectory dir: directories) {
              if (projectRootManager.getFileIndex().getContentRootForFile(dir.getVirtualFile()) == root) {
                initialDir = dir;
              }
            }
          }
          final VirtualFile sourceRoot = MoveClassesOrPackagesUtil.chooseSourceRoot(newPackage, contentSourceRoots, initialDir);
          if (sourceRoot == null) return null;
          targetSourceRoot = sourceRoot;
        }
        else {
          targetSourceRoot = contentSourceRoots [0];
        }
        PsiDirectory dir = RefactoringUtil.findPackageDirectoryInSourceRoot(newPackage, targetSourceRoot);
        if (dir == null) {
          dir = ApplicationManager.getApplication().runWriteAction(new NullableComputable<PsiDirectory>() {
            public PsiDirectory compute() {
              try {
                return RefactoringUtil.createPackageDirectoryInSourceRoot(newPackage, targetSourceRoot);
              }
              catch (IncorrectOperationException e) {
                return null;
              }
            }
          });
        }
        return dir;
      }
    }
    return myTargetContainer;
  }

  protected void doAction() {
    String message = null;
    final String className = getClassName();
    final String parameterName = getParameterName();
    PsiManager manager = PsiManager.getInstance(myProject);
    if ("".equals(className)) {
      message = RefactoringBundle.message("no.class.name.specified");
    }
    else {
      if (!JavaPsiFacade.getInstance(manager.getProject()).getNameHelper().isIdentifier(className)) {
        message = RefactoringMessageUtil.getIncorrectIdentifierMessage(className);
      }
      else {
        if (myCbPassOuterClass.isSelected()) {
          if ("".equals(parameterName)) {
            message = RefactoringBundle.message("no.parameter.name.specified");
          }
          else {
            if (!JavaPsiFacade.getInstance(manager.getProject()).getNameHelper().isIdentifier(parameterName)) {
              message = RefactoringMessageUtil.getIncorrectIdentifierMessage(parameterName);
            }
          }
        }
        if (message == null) {
          if (myTargetContainer instanceof PsiClass) {
            PsiClass targetClass = (PsiClass)myTargetContainer;
            PsiClass[] classes = targetClass.getInnerClasses();
            for (PsiClass aClass : classes) {
              if (className.equals(aClass.getName())) {
                message = RefactoringBundle.message("inner.class.exists", className, targetClass.getName());
                break;
              }
            }
          }
          else if (myTargetContainer instanceof PsiDirectory) {
            message = RefactoringMessageUtil.checkCanCreateClass((PsiDirectory)myTargetContainer, className);
          }
        }
      }
    }

    if (message != null) {
      CommonRefactoringUtil.showErrorMessage(
        MoveInnerImpl.REFACTORING_NAME,
        message,
        HelpID.MOVE_INNER_UPPER,
        myProject);
      return;
    }

    JavaRefactoringSettings.getInstance().MOVE_INNER_PREVIEW_USAGES = isPreviewUsages();
    if (myCbPassOuterClass.isSelected() && mySuggestedNameInfo != null) {
      mySuggestedNameInfo.nameChoosen(getParameterName());
    }

    final PsiElement target = getTargetContainer();
    if (target == null) return;
    myProcessor.setup(getInnerClass(), className, isPassOuterClass(), parameterName,
                      isSearchInComments(), isSearchInNonJavaFiles(), target);
    invokeRefactoring(myProcessor);
  }

  protected void doHelpAction() {
    HelpManager.getInstance().invokeHelp(HelpID.MOVE_INNER_UPPER);
  }

  private void createUIComponents() {
    if (!myInnerClass.hasModifierProperty(PsiModifier.STATIC)) {
      final PsiManager manager = myInnerClass.getManager();
      PsiType outerType = JavaPsiFacade.getInstance(manager.getProject()).getElementFactory().createType(myInnerClass.getContainingClass());
      mySuggestedNameInfo =  JavaCodeStyleManager.getInstance(myProject).suggestVariableName(VariableKind.PARAMETER, null, null, outerType);
      String[] variants = mySuggestedNameInfo.names;
      myParameterField = new NameSuggestionsField(variants, myProject);
    }
    else {
      myParameterField = new NameSuggestionsField(new String[]{""}, myProject);
      myParameterField.getComponent().setEnabled(false);
    }

    myPackageNameField = new PackageNameReferenceEditorCombo("", myProject, RECENTS_KEY,
                                                             RefactoringBundle.message("choose.destination.package"));
    PsiPackage psiPackage = getTargetPackage();
    if (psiPackage != null) {
      myPackageNameField.prependItem(psiPackage.getQualifiedName());
    }
  }

  @Nullable
  private PsiPackage getTargetPackage() {
    if (myTargetContainer instanceof PsiDirectory) {
      final PsiDirectory directory = (PsiDirectory)myTargetContainer;
      return JavaDirectoryService.getInstance().getPackage(directory);
    }
    return null;
  }
}