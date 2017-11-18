package org.jetbrains.idea.maven;

import com.intellij.util.io.ReadOnlyAttributeUtil;
import org.apache.maven.artifact.Artifact;
import org.jetbrains.idea.maven.indices.MavenCustomRepositoryHelper;
import org.jetbrains.idea.maven.utils.MavenId;

import java.io.File;
import java.util.List;

public class AddingDependencyTest extends MavenImportingTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MavenCustomRepositoryHelper helper = new MavenCustomRepositoryHelper(myDir, "local1");
    setRepositoryPath(helper.getTestDataPath("local1"));
  }

  public void testBasics() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    myMavenProjectsManager.addDependency(myMavenTree.findProject(myProjectPom),
                                         new MavenId("junit", "junit", "4.0"));

    List<Artifact> deps = myMavenTree.getProjects().get(0).getDependencies();
    assertEquals(1, deps.size());
    assertEquals(new File(getRepositoryPath(), "junit/junit/4.0/junit-4.0.jar"),
                 deps.get(0).getFile());
    assertModuleLibDeps("project", "Maven: junit:junit:4.0");

    importProject();
    assertModuleLibDeps("project", "Maven: junit:junit:4.0");
  }

  public void testDownloadingBeforeAdding() throws Exception {
    File jarFile = new File(getRepositoryPath(), "junit/junit/4.0/junit-4.0.jar");

    removeFromLocalRepository("junit");

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertFalse(jarFile.exists());

    myMavenProjectsManager.addDependency(myMavenTree.findProject(myProjectPom),
                                         new MavenId("junit", "junit", "4.0"));
    assertTrue(jarFile.exists());
  }

  public void testClearingROStatus() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");
    ReadOnlyAttributeUtil.setReadOnlyAttribute(myProjectPom, true);

    // shouldn't throw 'File is read-only' exception.
    myMavenProjectsManager.addDependency(myMavenTree.findProject(myProjectPom),
                                         new MavenId("junit", "junit", "4.0"));
  }
}