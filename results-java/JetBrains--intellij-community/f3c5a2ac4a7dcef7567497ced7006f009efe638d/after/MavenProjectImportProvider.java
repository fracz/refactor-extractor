/*
 * User: anna
 * Date: 12-Jul-2007
 */
package org.jetbrains.idea.maven.project.action;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.projectImport.ProjectImportProvider;
import com.intellij.projectImport.SelectImportedProjectsStep;
import org.jetbrains.idea.maven.project.MavenProjectModel;

import java.io.File;

public class MavenProjectImportProvider extends ProjectImportProvider {
  public MavenProjectImportProvider(final MavenImportBuilder builder) {
    super(builder);
  }

  public ModuleWizardStep[] createSteps(WizardContext wizardContext) {
    final ProjectWizardStepFactory stepFactory = ProjectWizardStepFactory.getInstance();
    return new ModuleWizardStep[]{new MavenImportRootStep(wizardContext), new SelectProfilesStep(wizardContext),
      new SelectImportedProjectsStep<MavenProjectModel>(wizardContext) {
        protected String getElementText(final MavenProjectModel node) {
          final StringBuilder stringBuilder = new StringBuilder();
          stringBuilder.append(node.getMavenId());
          final String relPath =
            VfsUtil.getRelativePath(node.getFile().getParent(), ((MavenImportBuilder)getBuilder()).getRootDirectory(), File.separatorChar);
          if (relPath.length() != 0) {
            stringBuilder.append(" [").append(relPath).append("]");
          }
          return stringBuilder.toString();
        }

        public void updateDataModel() {
          super.updateDataModel();
          getWizardContext().setProjectName(((MavenImportBuilder)getBuilder()).getSuggestedProjectName());
        }

        public String getHelpId() {
          return "reference.dialogs.new.project.import.maven.page3";
        }
      }, stepFactory.createProjectJdkStep(wizardContext), stepFactory.createNameAndLocationStep(wizardContext)};
  }
}