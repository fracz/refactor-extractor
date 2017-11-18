
package com.intellij.refactoring.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiFormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RefactoringMessageUtil {
  private static final Logger LOG = Logger.getInstance("#com.intellij.refactoring.util.RefactoringMessageUtil");

  public static void showErrorMessage(String title, String message, String helpId, Project project) {
    RefactoringMessageDialog dialog=new RefactoringMessageDialog(title,message,helpId,"OptionPane.errorIcon",false, project);
    dialog.show();
  }

  public static boolean checkReadOnlyStatus(Project project, PsiElement element) {
    return checkReadOnlyStatus(element, project, "Refactoring cannot be performed");
  }

  public static boolean checkReadOnlyStatus(PsiElement element, Project project, String messagePrefix) {
    return checkReadOnlyStatus(element, project, messagePrefix, false);
  }

  public static boolean checkReadOnlyStatusRecursively (Project project, PsiElement element) {
    return checkReadOnlyStatus(element, project, "Refactoring cannot be performed", true);
  }

  private static boolean checkReadOnlyStatus(PsiElement element,
                                            Project project,
                                            final String messagePrefix,
                                            boolean recursively
    ) {
    if (element instanceof PsiDirectory) {
      PsiDirectory dir = (PsiDirectory)element;
      final VirtualFile vFile = dir.getVirtualFile();
      if (vFile.getFileSystem() instanceof JarFileSystem) {
        String message1 = messagePrefix + ".\n Directory " + vFile.getPresentableUrl() + " is located in a jar file.";
        showErrorMessage("Cannot Modify Jar", message1, null, project);
        return false;
      }
      else {
        VirtualFile[] vFiles;
        if (recursively) {
          List<VirtualFile> list = new ArrayList<VirtualFile>();
          addVirtualFiles(vFile, list);
          vFiles = list.toArray(new VirtualFile[list.size()]);
        } else vFiles = new VirtualFile[]{vFile};
        final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(vFiles);
        final VirtualFile[] readonlyFiles = status.getReadonlyFiles();
        if (readonlyFiles.length > 0) {
          String subj = readonlyFiles[0].isDirectory() ? "Directory" : "File";
          String message1 = messagePrefix + ".\n " + subj + " " + readonlyFiles[0].getPresentableUrl() + " is read-only.";
          showErrorMessage("Read-only " + subj, message1, null, project);
          return false;
        }
        return true;
      }
    }
    else if (element instanceof PsiPackage) {
      final PsiDirectory[] directories = ((PsiPackage) element).getDirectories();
      final List<VirtualFile> readonlyList = new ArrayList<VirtualFile>();
      final List<VirtualFile> failedDirs = new ArrayList<VirtualFile>();
      for (PsiDirectory directory : directories) {
        VirtualFile virtualFile = directory.getVirtualFile();
        if (recursively) {
          if (virtualFile.getFileSystem() instanceof JarFileSystem) {
            failedDirs.add(virtualFile);
          }
          else {
            addVirtualFiles(virtualFile, readonlyList);
          }
        }
        else {
          if (!directory.isWritable()) {
            if (virtualFile.getFileSystem() instanceof JarFileSystem) {
              failedDirs.add(virtualFile);
            }
            else {
              readonlyList.add(virtualFile);
            }
          }
        }
      }
      final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(readonlyList.toArray(new VirtualFile[readonlyList.size()]));
      failedDirs.addAll(Arrays.asList(status.getReadonlyFiles()));
      if (failedDirs.size() > 0) {
        StringBuffer message = new StringBuffer(messagePrefix);
        message.append('\n');
        for (VirtualFile virtualFile : failedDirs) {
          final String presentableUrl = virtualFile.getPresentableUrl();
          final String subj = virtualFile.isDirectory() ? "Directory " : "File ";
          if (virtualFile.getFileSystem() instanceof JarFileSystem) {
            message.append(subj + presentableUrl + " is located in a jar file.\n");
          }
          else {
            message.append(subj + presentableUrl + " is read-only.\n");
          }
        }
        return false;
      }
      return true;
    }
    else if (element instanceof PsiCompiledElement) {
      PsiFile file = element.getContainingFile();
      String qName = ((PsiJavaFile)file).getClasses()[0].getQualifiedName();
      showErrorMessage("Compiled Class", messagePrefix + " on compiled class " + qName + ".", null, project);
      return false;
    }
    else{
      PsiFile file = element.getContainingFile();
      if (file == null) {

        if (element instanceof PsiVariable) {
          String message1 = "Variable " + element.getText() + " cannot be renamed.";
          showErrorMessage("Read-only entity", message1, null, project);
        }
        else {
          String message1 = "Cannot rename " + element.getText() + ".";
          showErrorMessage("Read-only entity", message1, null, project);
        }

        return false;
      }
      VirtualFile vFile = file.getVirtualFile();
      if (vFile.getFileSystem() instanceof JarFileSystem) {
        String message1 = messagePrefix + ".\n File " + vFile.getPresentableUrl() + " is located in a jar file.";
        showErrorMessage("Cannot Modify Jar", message1, null, project);
        return false;
      }
      else {
        final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(new VirtualFile[]{vFile});
        if (status.hasReadonlyFiles()) {
          String message1 = messagePrefix + ".\n File " + vFile.getPresentableUrl() + " is read-only.";
          showErrorMessage("Read-only File", message1, null, project);
          return false;
        }
        return true;
      }
    }
  }

  private static void addVirtualFiles(final VirtualFile vFile, final List<VirtualFile> list) {
    if (!vFile.isWritable()) {
      list.add(vFile);
    }
    final VirtualFile[] children = vFile.getChildren();
    if (children != null) {
      for (VirtualFile virtualFile : children) {
        addVirtualFiles(virtualFile, list);
      }
    }
  }

  public static String getIncorrectIdentifierMessage(String identifierName) {
    return "'" + identifierName + "' is not a legal java identifier";
  }

  /**
   * Must be invoked in AtomicAction
   * @return true - can continue, false return to editing
   */
  public static boolean checkMethodConflicts(PsiElement scope, PsiMethod refactoredMethod, PsiMethod prototype) {
    if (prototype == null) return true;

    PsiMethod method = null;
    if (scope instanceof PsiClass) {
      method = ((PsiClass)scope).findMethodBySignature(prototype, true);
    }
    else {
      LOG.assertTrue(false);
    }
    if (method != null && method != refactoredMethod) {
      String methodInfo = PsiFormatUtil.formatMethod(
        method,
          PsiSubstitutor.EMPTY, PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_PARAMETERS,
        PsiFormatUtil.SHOW_TYPE
      );
      if (scope instanceof PsiClass) {
        PsiClass aClass = (PsiClass)scope;
        if (method.getContainingClass().equals(aClass)) {
          String className = !(aClass instanceof PsiAnonymousClass) ? "class " + aClass.getName() : "current class";
          int ret = Messages.showYesNoDialog(
            "Method " + methodInfo + " is already defined in the " + className + ".\nContinue anyway?",
            "Warning",
            Messages.getWarningIcon()
          );
          if (ret != 0) {
            return false;
          }
        }
        else { // method somewhere in base class
          if (!method.hasModifierProperty(PsiModifier.PRIVATE)) {
            String protoMethodInfo = PsiFormatUtil.formatMethod(
              prototype,
                PsiSubstitutor.EMPTY, PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_PARAMETERS,
              PsiFormatUtil.SHOW_TYPE
            );
            String className = method.getContainingClass().getName();
            if (!prototype.hasModifierProperty(PsiModifier.PRIVATE)) {
              boolean isMethodAbstract = method.hasModifierProperty(PsiModifier.ABSTRACT);
              boolean isMyMethodAbstract = refactoredMethod != null? refactoredMethod.hasModifierProperty(PsiModifier.ABSTRACT) : false;
              int ret = Messages.showYesNoDialog(
                "Method " + protoMethodInfo + " will " + ((isMethodAbstract && !isMyMethodAbstract) ? "implement" : "override") +
                "\nmethod of the base class " + className + ".\n" +
                "Continue anyway?",
                "Warning",
                Messages.getWarningIcon()
              );
              if (ret != 0) {
                return false;
              }
            }
            else { // prototype is private, will be compile-error
              int ret = Messages.showYesNoDialog(
                "Method " + protoMethodInfo+ " will hide\nmethod of the base class " + className + ".\n" +
                "Continue anyway?",
                "Warning",
                Messages.getWarningIcon()
              );
              if (ret != 0) {
                return false;
              }
            }
          }
        }
      }
      else { // scope instanceof JspFile
        PsiFile file = method.getContainingFile();
        int toContinue = Messages.showYesNoDialog(
          "Method " + methodInfo + " is already defined in the file\n" + file.getVirtualFile().getPresentableUrl() + ".\nContinue anyway?",
          "Warning",
          Messages.getWarningIcon()
        );
        if (toContinue != 0) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Must be invoked in AtomicAction
   * @return true - can continue, false return to editing
   */
  public static boolean checkFieldConflicts(PsiElement scope, String newName) {
    PsiField existingField = null;
    String name = newName;
    if (scope instanceof PsiClass) {
      existingField = ((PsiClass)scope).findFieldByName(name, true);
    }
    else {
      LOG.assertTrue(false);
    }
    if (existingField != null) {
      if (scope instanceof PsiClass) {
        PsiClass aClass = (PsiClass)scope;
        if (existingField.getContainingClass().equals(aClass)) {
          String className = !(aClass instanceof PsiAnonymousClass) ? "class " + aClass.getName() : "current class";
          int ret = Messages.showYesNoDialog(
            "Field " + existingField.getName() + " is already defined in the " + className + ".\nContinue anyway?",
            "Warning",
            Messages.getWarningIcon()
          );
          if (ret != 0) {
            return false;
          }
        }
        else { // method somewhere in base class
          if (!existingField.hasModifierProperty(PsiModifier.PRIVATE)) {
            String fieldInfo = PsiFormatUtil.formatVariable(existingField, PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_TYPE | PsiFormatUtil.TYPE_AFTER, PsiSubstitutor.EMPTY);
            String protoFieldInfo = newName;
            String className = existingField.getContainingClass().getName();
            int ret = Messages.showYesNoDialog(
              "Field " + protoFieldInfo + " will hide\n field " + fieldInfo + " of the base class " + className + ".\n" +
              "Continue anyway?",
              "Warning",
              Messages.getWarningIcon()
            );
            if (ret != 0) {
              return false;
            }
          }
        }
      }
      else { // scope instanceof JspFile
        PsiFile psiFile = existingField.getContainingFile();
        int ret = Messages.showYesNoDialog(
          "Field " + existingField.getName() + " is already defined in the file\n" + psiFile.getVirtualFile().getPresentableUrl() + ".\nContinue anyway?",
          "Warning",
          Messages.getWarningIcon()
        );
        if (ret != 0) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * @return null, if can create a class
   *         an error message, if cannot create a class
   *
   */
  public static String checkCanCreateClass(PsiDirectory destinationDirectory, String className) {
    PsiClass[] classes = destinationDirectory.getClasses();
    VirtualFile file = destinationDirectory.getVirtualFile();
    for (PsiClass aClass : classes) {
      if (className.equals(aClass.getName())) {
        return "Directory " + file.getPresentableUrl() + "\nalready contains " + (aClass.isInterface() ? "an interface" : "a class") +
               " named '" + className + "'";
      }
    }
    String fileName = className+".java";
    return checkCanCreateFile(destinationDirectory, fileName);
  }
  public static String checkCanCreateFile(PsiDirectory destinationDirectory, String fileName) {
    VirtualFile file = destinationDirectory.getVirtualFile();
    VirtualFile child = file.findChild(fileName);
    if (child != null) {
      return "Directory " + file.getPresentableUrl() + "\nalready contains a file named '" + fileName + "'";
    }
    return null;
  }

  public static String getGetterSetterMessage(String newName, String action, PsiMethod getter, PsiMethod setter) {
    String text;
    if (getter != null && setter != null) {
      text = "Getter and setter methods found for the field " + newName + ". \n" + action + " them as well?";
    } else if (getter != null) {
      text = "Getter method found for the field " + newName + ". \n" + action + " the getter as well?";
    } else {
      text = "Setter method found for the field " + newName + ". \n" + action + " the setter as well?";
    }
    return text;
  }

}