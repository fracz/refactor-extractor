package org.jetbrains.idea.maven;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import org.jetbrains.idea.maven.indices.MavenCustomRepositoryHelper;

import java.io.File;

public class MiscImportingTest extends MavenImportingTestCase {
  private int count;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myProject.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      public void rootsChanged(ModuleRootEvent event) {
        count++;
      }
    });
  }

  public void testImportingAllAvailableFilesIfNotInitialized() throws Exception {
    createModule("m1");
    createModule("m2");
    createProjectSubDirs("m1/src/main/java",
                         "m2/src/main/java");

    createModulePom("m1",
                    "<groupId>test</groupId>" +
                    "<artifactId>m1</artifactId>" +
                    "<version>1</version>");

    createModulePom("m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>m2</artifactId>" +
                    "<version>1</version>");

    assertSources("m1");
    assertSources("m2");

    assertFalse(myMavenProjectsManager.isMavenizedProject());
    myMavenProjectsManager.importProjectOrAllAvailablePomFiles();

    assertSources("m1", "src/main/java");
    assertSources("m2", "src/main/java");
  }

  public void testImportingFiresRootChangesOnlyOnce() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertEquals(1, count);
  }

  public void testResolvingFiresRootChangesOnlyOnce() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertEquals(1, count);

    resolveDependenciesAndImport();
    assertEquals(2, count);
  }

  public void testImportingWithLibrariesFiresRootChangesOnlyOnce() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>junit</groupId>" +
                  "    <artifactId>junit</artifactId>" +
                  "    <version>4.0</version>" +
                  "  </dependency>" +
                  "  <dependency>" +
                  "    <groupId>jmock</groupId>" +
                  "    <artifactId>jmock</artifactId>" +
                  "    <version>1.0.0</version>" +
                  "  </dependency>" +
                  "</dependencies>");

    assertEquals(1, count);
  }

  public void testFacetsDoNotFireRootsChanges() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +
                  "<packaging>war</packaging>");

    assertEquals(1, count);

    resolveDependenciesAndImport();
    assertEquals(2, count);
  }

  public void testDoNotRecreateModulesBeforeResolution() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    Module m = getModule("project");
    resolveDependenciesAndImport();

    assertSame(m, getModule("project"));
  }

  public void testTakingProxySettingsIntoAccount() throws Exception {
    MavenCustomRepositoryHelper helper = new MavenCustomRepositoryHelper(myDir, "local1");
    setRepositoryPath(helper.getTestDataPath("local1"));

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>junit</groupId>" +
                  "    <artifactId>junit</artifactId>" +
                  "    <version>4.0</version>" +
                  "  </dependency>" +
                  "</dependencies>");

    removeFromLocalRepository("junit");
    resolveDependenciesAndImport();

    File jarFile = new File(getRepositoryFile(), "junit/junit/4.0/junit-4.0.jar");
    assertTrue(jarFile.exists());

    myMavenProjectsManager.listenForExternalChanges();

    // valid password is 'fg3W9' (see http://www.jetbrains.net/confluence/display/JBINT/HTTP+Proxy+with+authorization)
    updateSettingsXml("<proxies>" +
                       " <proxy>" +
                       "    <id>my</id>" +
                       "    <active>true</active>" +
                       "    <protocol>http</protocol>" +
                       "    <host>proxy-auth-test.labs.intellij.net</host>" +
                       "    <port>3128</port>" +
                       "    <username>user1</username>" +
                       "    <password>invalid</password>" +
                       "  </proxy>" +
                       "</proxies>");

    removeFromLocalRepository("junit");
    assertFalse(jarFile.exists());

    try {
      resolveDependenciesAndImport();
    }
    finally {
      // LightweightHttpWagon does not clear settings if they were not set before a proxy was configured.
      System.clearProperty("http.proxyHost");
      System.clearProperty("http.proxyPort");
    }
    assertFalse(jarFile.exists());

    restoreSettingsFile();

    resolveDependenciesAndImport();
    assertTrue(jarFile.exists());
  }
}