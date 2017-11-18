package org.jetbrains.idea.maven;

import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;

public class ReimportingTest extends MavenImportingTestCase {
  private int questionsCount;

  @Override
  protected void setUpInWriteAction() throws Exception {
    super.setUpInWriteAction();
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>");

    createModulePom("m2", "<groupId>test</groupId>" +
                          "<artifactId>m2</artifactId>" +
                          "<version>1</version>");
    importProject();
  }

  @Override
  protected void tearDown() throws Exception {
    Messages.setTestDialog(TestDialog.DEFAULT);
    super.tearDown();
  }

  public void testKeepingModuleGroups() throws Exception {
    Module m = getModule("project");

    ModifiableModuleModel model = ModuleManager.getInstance(myProject).getModifiableModel();
    model.setModuleGroupPath(m, new String[]{"group"});
    model.commit();

    importProject();
    assertModuleGroupPath("project", "group");
  }

  public void testAddingNewModule() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "  <module>m3</module>" +
                     "</modules>");

    createModulePom("m3", "<groupId>test</groupId>" +
                          "<artifactId>m3</artifactId>" +
                          "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2", "m3");
  }

  public void testRemovingObsoleteModule() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>");

    configMessagesForYesAnswer();
    importProject();
    assertModules("project", "m1");
  }

  public void testDoesNotRemoveObsoleteModuleIfUserSaysNo() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>");

    configMessagesForNoAnswer();
    importProject();
    assertModules("project", "m1", "m2");
  }

  public void testDoesNotAskUserTwiceToRemoveTheSameModule() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>");

    assertEquals(0, questionsCount);

    configMessagesForNoAnswer();
    importProject();
    assertEquals(1, questionsCount);

    importProject();
    assertEquals(1, questionsCount);
  }

  public void testDoesNotAskToRemoveManuallyAdderModules() throws Exception {
    createModule("userModule");
    assertModules("project", "m1", "m2", "userModule");

    importProject();

    assertEquals(0, questionsCount);
    assertModules("project", "m1", "m2", "userModule");
  }

  public void testRemovingAndCreatingModulesForAggregativeProjects() throws Exception {
    createModulePom("m1", "<groupId>test</groupId>" +
                          "<artifactId>m1</artifactId>" +
                          "<version>1</version>" +
                          "<packaging>pom</packaging>");
    importProject();

    assertModules("project", "m1", "m2");

    configMessagesForYesAnswer();

    getMavenImporterSettings().setCreateModulesForAggregators(false);
    myMavenProjectsManager.importProjects(); // using raw call to ignore emulation of project file change
    assertModules("m2");

    getMavenImporterSettings().setCreateModulesForAggregators(true);
    myMavenProjectsManager.importProjects();
    assertModules("project", "m1", "m2");
  }

  public void testDoNotCreateModulesForNewlyCreatedAggregativeProjectsIfNotNecessary() throws Exception {
    getMavenImporterSettings().setCreateModulesForAggregators(false);
    configMessagesForYesAnswer();

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "  <module>m3</module>" +
                     "</modules>");

    createModulePom("m3", "<groupId>test</groupId>" +
                          "<artifactId>m3</artifactId>" +
                          "<version>1</version>" +
                          "<packaging>pom</packaging>");
    importProject();
    assertModules("m1", "m2");
  }

  public void testReimportingWithProfiles() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<profiles>" +
                     "  <profile>" +
                     "    <id>profile1</id>" +
                     "    <activation>" +
                     "      <activeByDefault>false</activeByDefault>" +
                     "    </activation>" +
                     "    <modules>" +
                     "      <module>m1</module>" +
                     "    </modules>" +
                     "  </profile>" +
                     "  <profile>" +
                     "    <id>profile2</id>" +
                     "    <activation>" +
                     "      <activeByDefault>false</activeByDefault>" +
                     "    </activation>" +
                     "    <modules>" +
                     "      <module>m2</module>" +
                     "    </modules>" +
                     "  </profile>" +
                     "</profiles>");

    configMessagesForYesAnswer(); // will ask about absent modules

    importProjectWithProfiles("profile1");
    assertModules("project", "m1");

    importProjectWithProfiles("profile2");
    assertModules("project", "m2");
  }

  public void testReimportingWhenModuleHaveRootOfTheParent() throws Exception {
    createProjectSubDir("m1/res");
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    createModulePom("m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>m2</artifactId>" +
                    "<version>1</version>" +

                    "<build>" +
                    "  <resources>" +
                    "    <resource><directory>../m1</directory></resource>" +
                    "  </resources>" +
                    "</build>");

    importProject(); // shouldn't throw Dialog.show exception
    resolveDependenciesAndImport();
    assertEquals(0, questionsCount);
  }

  private void configMessagesForYesAnswer() {
    Messages.setTestDialog(new TestDialog() {
      public int show(String message) {
        questionsCount++;
        return 0;
      }
    });
  }

  private void configMessagesForNoAnswer() {
    Messages.setTestDialog(new TestDialog() {
      public int show(String message) {
        questionsCount++;
        return 1;
      }
    });
  }
}