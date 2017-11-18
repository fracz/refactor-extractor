/*
 * User: anna
 * Date: 13-May-2010
 */
package com.jetbrains.python.testing.pytest;

import com.intellij.execution.Location;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.webcore.packaging.PackageVersionComparator;
import com.jetbrains.python.packaging.PyExternalProcessException;
import com.jetbrains.python.packaging.PyPackage;
import com.jetbrains.python.packaging.PyPackageManager;
import com.jetbrains.python.packaging.PyPackageManagerImpl;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStatement;
import com.jetbrains.python.sdk.PythonSdkType;
import com.jetbrains.python.testing.PythonTestConfigurationType;
import com.jetbrains.python.testing.PythonTestConfigurationsModel;
import com.jetbrains.python.testing.TestRunnerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PyTestConfigurationProducer extends RuntimeConfigurationProducer {
  private PsiElement myPsiElement;

  public PyTestConfigurationProducer() {
    super(PythonTestConfigurationType.getInstance().PY_PYTEST_FACTORY);
  }

  @Override
  public PsiElement getSourceElement() {
    return myPsiElement;
  }

  @Override
  protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
    PsiElement element = location.getPsiElement();
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module == null) return null;
    if (! (TestRunnerService.getInstance(module).getProjectConfiguration().equals(
           PythonTestConfigurationsModel.PY_TEST_NAME))) return null;

    PsiFileSystemItem file = element instanceof PsiDirectory ? (PsiDirectory)element : element.getContainingFile();
    if (file == null) return null;
    myPsiElement = file;
    final VirtualFile virtualFile = file.getVirtualFile();
    if (virtualFile == null) return null;
    String path = virtualFile.getPath();

    if (file instanceof PyFile || file instanceof PsiDirectory) {
      final List<PyStatement> testCases = PyTestUtil.getPyTestCasesFromFile(file);
      if (testCases.isEmpty()) return null;
    } else return null;

    final RunnerAndConfigurationSettings result =
      RunManager.getInstance(location.getProject()).createRunConfiguration(file.getName(), getConfigurationFactory());
    PyTestRunConfiguration configuration = (PyTestRunConfiguration)result.getConfiguration();
    configuration.setUseModuleSdk(true);
    configuration.setModule(ModuleUtilCore.findModuleForPsiElement(myPsiElement));

    final Sdk sdk = PythonSdkType.findPythonSdk(location.getModule());
    if (sdk == null) return null;
    configuration.setTestToRun(path);

    PyFunction pyFunction = findTestFunction(location);
    PyClass pyClass = PsiTreeUtil.getParentOfType(location.getPsiElement(), PyClass.class, false);
    if (pyFunction != null) {
      String name = pyFunction.getName();
      if (pyClass != null) {
        final PyPackageManagerImpl packageManager = (PyPackageManagerImpl)PyPackageManager.getInstance(sdk);
        try {
          final PyPackage pytestPackage = packageManager.findPackage("pytest");
          if (pytestPackage != null && PackageVersionComparator.VERSION_COMPARATOR.compare(pytestPackage.getVersion(), "2.3.3") >= 0) {
            name = pyClass.getName() + " and " + name;
          }
          else {
            name = pyClass.getName() + "." + name;
          }
        }
        catch (PyExternalProcessException e) {
          name = pyClass.getName() + "." + name;
        }
      }
      configuration.useKeyword(true);
      configuration.setKeywords(name);
      configuration.setName(name);
      myPsiElement = pyFunction;
    }
    else if (pyClass != null) {
      String name = pyClass.getName();
      configuration.useKeyword(true);
      configuration.setKeywords(name);
      configuration.setName(name);
      myPsiElement = pyClass;
    }
    configuration.setName(configuration.suggestedName());
    return result;
  }

  @Nullable
  private static PyFunction findTestFunction(Location location) {
    PyFunction function = PsiTreeUtil.getParentOfType(location.getPsiElement(), PyFunction.class);
    if (function != null) {
      String name = function.getName();
      if (name != null && name.startsWith("test")) {
        return function;
      }
    }
    return null;
  }

  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(Location location,
                                                                 @NotNull List<RunnerAndConfigurationSettings> existingConfigurations,
                                                                 ConfigurationContext context) {
    for (RunnerAndConfigurationSettings existingConfiguration : existingConfigurations) {
      final RunConfiguration configuration = existingConfiguration.getConfiguration();
      if (configuration instanceof PyTestRunConfiguration) {
        final PsiElement element = location.getPsiElement();
        PsiFileSystemItem file = element instanceof PsiDirectory ? (PsiDirectory)element : element.getContainingFile();
        final VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null || !((PyTestRunConfiguration)configuration).getTestToRun().equals(virtualFile.getPath())) {
          continue;
        }
        PyFunction testFunction = findTestFunction(location);
        String keyword = testFunction != null ? testFunction.getName() : null;
        if (Comparing.equal(((PyTestRunConfiguration)configuration).getKeywords(), keyword)) {
          return existingConfiguration;
        }
      }
    }
    return null;
  }

  public int compareTo(@NotNull Object o) {
    return PREFERED;
  }
}