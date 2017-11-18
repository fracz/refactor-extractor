package org.jetbrains.idea.maven.project;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import com.intellij.util.ProfilingUtil;
import org.jetbrains.idea.maven.MavenImportingTestCase;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.List;

public class MavenProjectsTreeTest extends MavenImportingTestCase {
  private MavenProjectsTree myTree = new MavenProjectsTree();

  public void testTwoRootProjects() throws Exception {
    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");

    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");

    updateAll(m1, m2);
    List<MavenProject> roots = myTree.getRootProjects();

    assertEquals(2, roots.size());
    assertEquals(m1, roots.get(0).getFile());
    assertEquals(m2, roots.get(1).getFile());
  }

  public void testDoNotImportChildAsRootProject() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(myProjectPom, m);
    List<MavenProject> roots = myTree.getRootProjects();

    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());

    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m, myTree.getModules(roots.get(0)).get(0).getFile());
  }

  public void testDoNotImportSameRootProjectTwice() throws Exception {
    MyLoggingListener listener = new MyLoggingListener();
    myTree.addListener(listener);

    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");

    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");

    updateAll(m1, m2, m1);
    List<MavenProject> roots = myTree.getRootProjects();

    assertEquals(2, roots.size());
    assertEquals(m2, roots.get(0).getFile());
    assertEquals(m1, roots.get(1).getFile());

    assertEquals("read m1 read m2 projectsRead m1, m2 ", listener.log);
  }

  public void testRereadingChildIfParentWasReadAfterIt() throws Exception {
    MyLoggingListener listener = new MyLoggingListener();
    myTree.addListener(listener);

    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>" +

                                     "<properties>" +
                                     " <childId>m2</childId>" +
                                     "</properties>");

    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>${childId}</artifactId>" +
                                     "<version>1</version>" +

                                     "<parent>" +
                                     "  <groupId>test</groupId>" +
                                     "  <artifactId>m1</artifactId>" +
                                     "  <version>1</version>" +
                                     "</parent>");

    updateAll(m2, m1);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(2, roots.size());
    assertEquals(m1, roots.get(0).getFile());
    assertEquals(m2, roots.get(1).getFile());
    assertEquals("m1", roots.get(0).getMavenId().artifactId);
    assertEquals("m2", roots.get(1).getMavenId().artifactId);

    assertEquals("read ${childId} read m1 read m2 projectsRead m2, m1 ", listener.log);
  }

  public void testSameProjectAsModuleOfSeveralProjects() throws Exception {
    VirtualFile p1 = createModulePom("project1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project1</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>../module</module>" +
                                     "</modules>");

    VirtualFile p2 = createModulePom("project2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>project2</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>../module</module>" +
                                     "</modules>");

    VirtualFile m = createModulePom("module",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>module</artifactId>" +
                                    "<version>1</version>");

    updateAll(p1, p2);
    List<MavenProject> roots = myTree.getRootProjects();

    assertEquals(2, roots.size());
    assertEquals(p1, roots.get(0).getFile());
    assertEquals(p2, roots.get(1).getFile());

    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m, myTree.getModules(roots.get(0)).get(0).getFile());

    assertEquals(0, myTree.getModules(roots.get(1)).size());
  }

  public void testSameProjectAsModuleOfSeveralProjectsInHierarchy() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>module1</module>" +
                     "  <module>module1/module2</module>" +
                     "</modules>");

    VirtualFile m1 = createModulePom("module1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>module1</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>module2</module>" +
                                     "</modules>");

    VirtualFile m2 = createModulePom("module1/module2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>module2</artifactId>" +
                                     "<version>1</version>");

    updateAll(myProjectPom);
    List<MavenProject> roots = myTree.getRootProjects();

    assertEquals(1, roots.size());
    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m1, myTree.getModules(roots.get(0)).get(0).getFile());

    assertEquals(1, myTree.getModules(myTree.getModules(roots.get(0)).get(0)).size());
    assertEquals(m2, myTree.getModules(myTree.getModules(roots.get(0)).get(0)).get(0).getFile());
  }

  public void testRemovingChildProjectFromRootProjects() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    // all projects are processed in the specified order
    // if we have imported a child project as a root one,
    // we have to correct ourselves and to remove it from roots.
    updateAll(m, myProjectPom);
    List<MavenProject> roots = myTree.getRootProjects();

    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());

    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m, myTree.getModules(roots.get(0)).get(0).getFile());
  }

  public void testSendingNotificationsWhenAggregationChanged() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");

    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");

    updateAll(myProjectPom, m1, m2);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(2, myTree.getModules(roots.get(0)).size());

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>");

    MyLoggingListener listener = new MyLoggingListener();
    myTree.addListener(listener);
    update(myProjectPom);

    roots = myTree.getRootProjects();
    assertEquals(2, roots.size());
    assertEquals(1, myTree.getModules(roots.get(0)).size());

    assertEquals("read project reconnected m2 projectsRead project ", listener.log);
  }

  public void testUpdatingWholeModel() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();

    MavenProject parentNode = roots.get(0);
    MavenProject childNode = myTree.getModules(roots.get(0)).get(0);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project1</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    createModulePom("m", "<groupId>test</groupId>" +
                         "<artifactId>m1</artifactId>" +
                         "<version>1</version>");

    updateAll(myProjectPom);

    roots = myTree.getRootProjects();

    assertEquals(1, roots.size());
    assertEquals(1, myTree.getModules(roots.get(0)).size());

    MavenProject parentNode1 = roots.get(0);
    MavenProject childNode1 = myTree.getModules(roots.get(0)).get(0);

    assertSame(parentNode, parentNode1);
    assertSame(childNode, childNode1);

    assertEquals("project1", parentNode1.getMavenId().artifactId);
    assertEquals("m1", childNode1.getMavenId().artifactId);
  }

  public void testUpdatingModelWithNewProfiles() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<profiles>" +
                     "  <profile>" +
                     "    <id>one</id>" +
                     "    <modules>" +
                     "      <module>m1</module>" +
                     "    </modules>" +
                     "  </profile>" +
                     "  <profile>" +
                     "    <id>two</id>" +
                     "    <modules>" +
                     "      <module>m2</module>" +
                     "    </modules>" +
                     "  </profile>" +
                     "</profiles>");

    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");

    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");

    updateAll(Collections.singletonList("one"), myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());

    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m1, myTree.getModules(roots.get(0)).get(0).getFile());

    updateAll(Collections.singletonList("two"), myProjectPom);

    roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());

    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m2, myTree.getModules(roots.get(0)).get(0).getFile());
  }

  public void testUpdatingParticularProject() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(myProjectPom);

    createModulePom("m", "<groupId>test</groupId>" +
                         "<artifactId>m1</artifactId>" +
                         "<version>1</version>");

    update(m);

    MavenProject n = myTree.findProject(m);
    assertEquals("m1", n.getMavenId().artifactId);
  }

  public void testUpdatingInheritance() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <childName>child</childName>" +
                     "</properties>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>${childName}</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    updateAll(myProjectPom, child);
    assertEquals("child", myTree.findProject(child).getMavenId().artifactId);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <childName>child2</childName>" +
                     "</properties>");

    update(myProjectPom);

    assertEquals("child2", myTree.findProject(child).getMavenId().artifactId);
  }

  public void testUpdatingInheritanceHierarhically() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <subChildName>subChild</subChildName>" +
                     "</properties>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>child</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    VirtualFile subChild = createModulePom("subChild",
                                           "<groupId>test</groupId>" +
                                           "<artifactId>${subChildName}</artifactId>" +
                                           "<version>1</version>" +

                                           "<parent>" +
                                           "  <groupId>test</groupId>" +
                                           "  <artifactId>child</artifactId>" +
                                           "  <version>1</version>" +
                                           "</parent>");

    updateAll(myProjectPom, child, subChild);

    assertEquals("subChild", myTree.findProject(subChild).getMavenId().artifactId);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <subChildName>subChild2</subChildName>" +
                     "</properties>");

    update(myProjectPom);

    assertEquals("subChild2", myTree.findProject(subChild).getMavenId().artifactId);
  }

  public void testSendingNotificationAfterProjectIsAddedInToHierarchy() throws Exception {
    MyLoggingListener listener = new MyLoggingListener();
    myTree.addListener(listener);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>m1</artifactId>" +
                     "<version>1</version>");
    updateAll(myProjectPom);

    assertEquals("read m1 projectsRead m1 ", listener.log);
  }

  public void testAddingInheritanceParent() throws Exception {
    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>${childName}</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    updateAll(child);
    assertEquals("${childName}", myTree.findProject(child).getMavenId().artifactId);

    VirtualFile parent = createModulePom("parent",
                                         "<groupId>test</groupId>" +
                                         "<artifactId>parent</artifactId>" +
                                         "<version>1</version>" +

                                         "<properties>" +
                                         "  <childName>child</childName>" +
                                         "</properties>");

    update(parent);

    assertEquals("child", myTree.findProject(child).getMavenId().artifactId);
  }

  public void testAddingInheritanceChild() throws Exception {
    VirtualFile parent = createModulePom("parent",
                                         "<groupId>test</groupId>" +
                                         "<artifactId>parent</artifactId>" +
                                         "<version>1</version>" +

                                         "<properties>" +
                                         "  <childName>child</childName>" +
                                         "</properties>");

    updateAll(parent);

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>${childName}</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    update(child);

    assertEquals("child", myTree.findProject(child).getMavenId().artifactId);
  }

  public void testAddingInheritanceChildOnParentUpdate() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <childName>child</childName>" +
                     "</properties>" +

                     "<modules>" +
                     " <module>child</module>" +
                     "</modules>");

    updateAll(myProjectPom);

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>${childName}</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    update(myProjectPom);

    assertEquals("child", myTree.findProject(child).getMavenId().artifactId);
  }

  public void testDoNotReAddInheritanceChildOnParentModulesRemoval() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<modules>" +
                     " <module>child</module>" +
                     "</modules>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>child</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");
    updateAll(myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(child, myTree.getModules(roots.get(0)).get(0).getFile());

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>");

    update(myProjectPom);

    roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(0, myTree.getModules(roots.get(0)).size());
  }

  public void testChangingInheritance() throws Exception {
    VirtualFile parent1 = createModulePom("parent1",
                                          "<groupId>test</groupId>" +
                                          "<artifactId>parent1</artifactId>" +
                                          "<version>1</version>" +

                                          "<properties>" +
                                          "  <childName>child1</childName>" +
                                          "</properties>");

    VirtualFile parent2 = createModulePom("parent2",
                                          "<groupId>test</groupId>" +
                                          "<artifactId>parent2</artifactId>" +
                                          "<version>1</version>" +

                                          "<properties>" +
                                          "  <childName>child2</childName>" +
                                          "</properties>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>${childName}</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent1</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    updateAll(parent1, parent2, child);
    assertEquals("child1", myTree.findProject(child).getMavenId().artifactId);

    createModulePom("child", "<groupId>test</groupId>" +
                             "<artifactId>${childName}</artifactId>" +
                             "<version>1</version>" +

                             "<parent>" +
                             "  <groupId>test</groupId>" +
                             "  <artifactId>parent2</artifactId>" +
                             "  <version>1</version>" +
                             "</parent>");

    update(child);

    assertEquals("child2", myTree.findProject(child).getMavenId().artifactId);
  }

  public void testChangingInheritanceParentId() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <childName>child</childName>" +
                     "</properties>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>${childName}</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    updateAll(myProjectPom, child);
    assertEquals("child", myTree.findProject(child).getMavenId().artifactId);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent2</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <childName>child</childName>" +
                     "</properties>");

    update(myProjectPom);

    assertEquals("${childName}", myTree.findProject(child).getMavenId().artifactId);
  }

  public void testHandlingSelfInheritance() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>parent</artifactId>" +
                     "  <version>1</version>" +
                     "</parent>");

    updateAll(myProjectPom); // shouldn't hang

    updateTimestamps(myProjectPom);
    update(myProjectPom); // shouldn't hang

    updateTimestamps(myProjectPom);
    updateAll(myProjectPom); // shouldn't hang
  }

  public void testHandlingRecursiveInheritance() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>child</artifactId>" +
                     "  <version>1</version>" +
                     "</parent>" +

                     "<modules>" +
                     "  <module>child</module>" +
                     "</properties>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>child</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    updateAll(myProjectPom, child); // shouldn't hang

    updateTimestamps(myProjectPom, child);
    update(myProjectPom); // shouldn't hang

    updateTimestamps(myProjectPom, child);
    update(child); // shouldn't hang

    updateTimestamps(myProjectPom, child);
    updateAll(myProjectPom, child); // shouldn't hang
  }

  public void testDeletingInheritanceParent() throws Exception {
    VirtualFile parent = createModulePom("parent",
                                         "<groupId>test</groupId>" +
                                         "<artifactId>parent</artifactId>" +
                                         "<version>1</version>" +

                                         "<properties>" +
                                         "  <childName>child</childName>" +
                                         "</properties>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>${childName}</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    updateAll(parent, child);

    assertEquals("child", myTree.findProject(child).getMavenId().artifactId);

    deleteProject(parent);

    assertEquals("${childName}", myTree.findProject(child).getMavenId().artifactId);
  }

  public void testDeletingInheritanceChild() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +

                     "<properties>" +
                     "  <subChildName>subChild</subChildName>" +
                     "</properties>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>child</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>");

    VirtualFile subChild = createModulePom("subChild",
                                           "<groupId>test</groupId>" +
                                           "<artifactId>${subChildName}</artifactId>" +
                                           "<version>1</version>" +

                                           "<parent>" +
                                           "  <groupId>test</groupId>" +
                                           "  <artifactId>child</artifactId>" +
                                           "  <version>1</version>" +
                                           "</parent>");

    updateAll(myProjectPom, child, subChild);
    assertEquals("subChild", myTree.findProject(subChild).getMavenId().artifactId);

    deleteProject(child);
    assertEquals("${subChildName}", myTree.findProject(subChild).getMavenId().artifactId);
  }

  public void testRecursiveInheritanceAndAggregation() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>parent</artifactId>" +
                     "<version>1</version>" +
                     "" +
                     "<parent>" +
                     "  <groupId>test</groupId>" +
                     "  <artifactId>child</artifactId>" +
                     "  <version>1</version>" +
                     "</parent>" +

                     "<modules>" +
                     " <module>child</module>" +
                     "</modules>");

    VirtualFile child = createModulePom("child",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>child</artifactId>" +
                                        "<version>1</version>");
    updateAll(myProjectPom); // should not recurse

    updateTimestamps(myProjectPom, child);
    updateAll(child); // should not recurse
  }

  public void testUpdatingAddsModules() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(0, myTree.getModules(roots.get(0)).size());

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    update(myProjectPom);

    roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m, myTree.getModules(roots.get(0)).get(0).getFile());
  }

  public void testUpdatingUpdatesModulesIfProjectIsChanged() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(myProjectPom);

    assertEquals("m", myTree.findProject(m).getMavenId().artifactId);

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<name>foo</name>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    createModulePom("m", "<groupId>test</groupId>" +
                         "<artifactId>m2</artifactId>" +
                         "<version>1</version>");
    update(myProjectPom);

    assertEquals("m2", myTree.findProject(m).getMavenId().artifactId);
  }

  public void testUpdatingDoesNotUpdateModulesIfProjectIsNotChanged() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(myProjectPom);

    assertEquals("m", myTree.findProject(m).getMavenId().artifactId);

    createModulePom("m", "<groupId>test</groupId>" +
                         "<artifactId>m2</artifactId>" +
                         "<version>1</version>");

    update(myProjectPom);

    // did not change
    assertEquals("m", myTree.findProject(m).getMavenId().artifactId);
  }

  public void testAddingProjectAsModuleToExistingOne() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    updateAll(myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(0, myTree.getModules(roots.get(0)).size());

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    update(m);

    roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m, myTree.getModules(roots.get(0)).get(0).getFile());
  }

  public void testAddingProjectAsAggregatorForExistingOne() throws Exception {
    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(m);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(m, roots.get(0).getFile());
    assertEquals(0, myTree.getModules(roots.get(0)).size());

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    update(myProjectPom);

    roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m, myTree.getModules(roots.get(0)).get(0).getFile());
  }

  public void testAddingProjectWithModules() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");

    updateAll(myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(0, myTree.getModules(roots.get(0)).size());

    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>m2</module>" +
                                     "</modules>");

    VirtualFile m2 = createModulePom("m1/m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");

    update(m1);

    roots = myTree.getRootProjects();
    assertEquals(2, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(m1, roots.get(1).getFile());
    assertEquals(1, myTree.getModules(roots.get(1)).size());
    assertEquals(m2, myTree.getModules(roots.get(1)).get(0).getFile());
  }

  public void testUpdatingAddsModulesFromRootProjects() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(myProjectPom, m);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(2, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(m, roots.get(1).getFile());
    assertEquals("m", roots.get(1).getMavenId().artifactId);
    assertEquals(0, myTree.getModules(roots.get(0)).size());

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    update(myProjectPom);

    roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(m, myTree.getModules(roots.get(0)).get(0).getFile());
  }

  public void testMovingModuleToRootsWhenAggregationChanged() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");
    updateAll(myProjectPom, m);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(1, myTree.getModules(roots.get(0)).size());

    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>");

    update(myProjectPom);

    roots = myTree.getRootProjects();
    assertEquals(2, roots.size());
    assertTrue(myTree.getModules(roots.get(0)).isEmpty());
    assertTrue(myTree.getModules(roots.get(1)).isEmpty());
  }

  public void testDeletingProject() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    VirtualFile m = createModulePom("m",
                                    "<groupId>test</groupId>" +
                                    "<artifactId>m</artifactId>" +
                                    "<version>1</version>");

    updateAll(myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(1, myTree.getModules(roots.get(0)).size());

    deleteProject(m);

    roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(0, myTree.getModules(roots.get(0)).size());
  }

  public void testDeletingProjectWithModules() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "</modules>");

    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>" +
                                     "<packaging>pom</packaging>" +

                                     "<modules>" +
                                     "  <module>m2</module>" +
                                     "</modules>");

    createModulePom("m1/m2",
                    "<groupId>test</groupId>" +
                    "<artifactId>m2</artifactId>" +
                    "<version>1</version>");

    updateAll(myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(1, myTree.getModules(roots.get(0)).size());
    assertEquals(1, myTree.getModules(myTree.getModules(roots.get(0)).get(0)).size());

    deleteProject(m1);

    roots = myTree.getRootProjects();
    assertEquals(1, roots.size());
    assertEquals(myProjectPom, roots.get(0).getFile());
    assertEquals(0, myTree.getModules(roots.get(0)).size());
  }

  public void testAddingProjectsOnUpdateAllWhenManagedFilesChanged() throws Exception {
    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");
    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");
    VirtualFile m3 = createModulePom("m3",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m3</artifactId>" +
                                     "<version>1</version>");
    updateAll(m1, m2);
    assertEquals(2, myTree.getRootProjects().size());

    updateAll(m1, m2, m3);
    assertEquals(3, myTree.getRootProjects().size());
  }

  public void testDeletingProjectsOnUpdateAllWhenManagedFilesChanged() throws Exception {
    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");
    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");
    VirtualFile m3 = createModulePom("m3",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m3</artifactId>" +
                                     "<version>1</version>");
    updateAll(m1, m2, m3);
    assertEquals(3, myTree.getRootProjects().size());

    updateAll(m1, m2);
    assertEquals(2, myTree.getRootProjects().size());
  }

  public void testUpdatingModelWhenActiveProfilesChange() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<profiles>" +
                     "  <profile>" +
                     "    <id>one</id>" +
                     "    <properties>" +
                     "      <prop>value1</prop>" +
                     "    </properties>" +
                     "  </profile>" +
                     "  <profile>" +
                     "    <id>two</id>" +
                     "    <properties>" +
                     "      <prop>value2</prop>" +
                     "    </properties>" +
                     "  </profile>" +
                     "</profiles>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>" +

                     "<build>" +
                     "  <sourceDirectory>${prop}</sourceDirectory>" +
                     "</build>");

    createModulePom("m",
                    "<groupId>test</groupId>" +
                    "<artifactId>m</artifactId>" +
                    "<version>1</version>" +

                    "<parent>" +
                    "  <groupId>test</groupId>" +
                    "  <artifactId>project</artifactId>" +
                    "  <version>1</version>" +
                    "</parent>" +

                    "<build>" +
                    "  <sourceDirectory>${prop}</sourceDirectory>" +
                    "</build>");

    updateAll(Arrays.asList("one"), myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();

    MavenProject parentNode = roots.get(0);
    MavenProject childNode = myTree.getModules(roots.get(0)).get(0);

    assertUnorderedElementsAreEqual(parentNode.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/value1"));
    assertUnorderedElementsAreEqual(childNode.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/m/value1"));

    updateAll(Arrays.asList("two"), myProjectPom);

    assertUnorderedElementsAreEqual(parentNode.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/value2"));
    assertUnorderedElementsAreEqual(childNode.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/m/value2"));
  }

  public void testUpdatingModelWhenProfilesXmlChange() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<build>" +
                     "  <sourceDirectory>${prop}</sourceDirectory>" +
                     "</build>");

    createProfilesXml("<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <prop>value1</prop>" +
                      "  </properties>" +
                      "</profile>");

    updateAll(myProjectPom);

    List<MavenProject> roots = myTree.getRootProjects();

    MavenProject project = roots.get(0);
    assertUnorderedElementsAreEqual(project.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/value1"));

    createProfilesXml("<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <prop>value2</prop>" +
                      "  </properties>" +
                      "</profile>");

    updateAll(myProjectPom);

    assertUnorderedElementsAreEqual(project.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/value2"));
  }

  public void testUpdatingModelWhenParentProfilesXmlChange() throws Exception {
    VirtualFile parent = createModulePom("parent",
                                         "<groupId>test</groupId>" +
                                         "<artifactId>parent</artifactId>" +
                                         "<version>1</version>" +
                                         "<packaging>pom</packaging>");

    createProfilesXml("parent",
                      "<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <prop>value1</prop>" +
                      "  </properties>" +
                      "</profile>");

    VirtualFile child = createModulePom("m",
                                        "<groupId>test</groupId>" +
                                        "<artifactId>m</artifactId>" +
                                        "<version>1</version>" +

                                        "<parent>" +
                                        "  <groupId>test</groupId>" +
                                        "  <artifactId>parent</artifactId>" +
                                        "  <version>1</version>" +
                                        "</parent>" +

                                        "<build>" +
                                        "  <sourceDirectory>${prop}</sourceDirectory>" +
                                        "</build>");

    updateAll(parent, child);

    List<MavenProject> roots = myTree.getRootProjects();

    MavenProject childProject = roots.get(1);
    assertUnorderedElementsAreEqual(childProject.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/m/value1"));

    createProfilesXml("parent",
                      "<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <prop>value2</prop>" +
                      "  </properties>" +
                      "</profile>");

    update(parent);
    assertUnorderedElementsAreEqual(childProject.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/m/value2"));
  }

  public void testUpdatingModelWhenParentProfilesXmlChangeAndItIsAModuleAlso() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    createProfilesXml("<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <prop>value1</prop>" +
                      "  </properties>" +
                      "</profile>");

    createModulePom("m",
                    "<groupId>test</groupId>" +
                    "<artifactId>m</artifactId>" +
                    "<version>1</version>" +

                    "<parent>" +
                    "  <groupId>test</groupId>" +
                    "  <artifactId>project</artifactId>" +
                    "  <version>1</version>" +
                    "</parent>" +

                    "<build>" +
                    "  <sourceDirectory>${prop}</sourceDirectory>" +
                    "</build>");

    updateAll(myProjectPom);

    MavenProject childNode = myTree.getModules(myTree.getRootProjects().get(0)).get(0);
    assertUnorderedElementsAreEqual(childNode.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/m/value1"));

    createProfilesXml("<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <prop>value2</prop>" +
                      "  </properties>" +
                      "</profile>");

    updateAll(myProjectPom);
    assertUnorderedElementsAreEqual(childNode.getSources(), FileUtil.toSystemDependentName(getProjectPath() + "/m/value2"));
  }

  public void testDoNotUpdateModelWhenAggregatorProfilesXmlChange() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m</module>" +
                     "</modules>");

    createModulePom("m",
                    "<groupId>test</groupId>" +
                    "<artifactId>m</artifactId>" +
                    "<version>1</version>" +

                    "<build>" +
                    "  <sourceDirectory>${prop}</sourceDirectory>" +
                    "</build>");

    createProfilesXml("<profile>" +
                      " <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      "    <prop>value1</prop>" +
                      "  </properties>" +
                      "</profile>");

    updateAll(myProjectPom);

    MyLoggingListener l = new MyLoggingListener();
    myTree.addListener(l);

    createProfilesXml("<profile>" +
                      "  <id>one</id>" +
                      "  <activation>" +
                      "    <activeByDefault>true</activeByDefault>" +
                      "  </activation>" +
                      "  <properties>" +
                      " <prop>value2</prop>" +
                      "  </properties>" +
                      "</profile>");

    updateAll(myProjectPom);
    assertEquals("read project projectsRead project ", l.log);
  }

  public void testSaveLoad() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +
                     "<packaging>pom</packaging>" +

                     "<modules>" +
                     "  <module>m1</module>" +
                     "  <module>m2</module>" +
                     "</modules>");

    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");

    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");

    updateAll(myProjectPom);

    File f = new File(myDir, "tree.dat");
    myTree.save(f);
    MavenProjectsTree read = MavenProjectsTree.read(f);

    List<MavenProject> roots = read.getRootProjects();
    assertEquals(1, roots.size());

    MavenProject rootProject = roots.get(0);
    assertEquals(myProjectPom, rootProject.getFile());

    assertEquals(2, read.getModules(rootProject).size());
    assertEquals(m1, read.getModules(rootProject).get(0).getFile());
    assertEquals(m2, read.getModules(rootProject).get(1).getFile());

    assertNull(read.findAggregator(rootProject));
    assertEquals(rootProject, read.findAggregator(read.findProject(m1)));
    assertEquals(rootProject, read.findAggregator(read.findProject(m2)));
  }

  public void testPerformanceTest() throws Exception {
    VirtualFile pom = LocalFileSystem.getInstance().findFileByPath("C:\\projects\\mvn\\_projects\\geronimo\\pom.xml");

    long before = System.currentTimeMillis();
    ProfilingUtil.startCPUProfiling();
    updateAll(pom);
    System.out.println(ProfilingUtil.captureCPUSnapshot());
    ProfilingUtil.stopCPUProfiling();

    long after = System.currentTimeMillis();

    System.out.println("delta:" + (after - before));
  }

  private void updateAll(VirtualFile... files) throws MavenProcessCanceledException, MavenException {
    updateAll(Collections.<String>emptyList(), files);
  }

  private void updateAll(List<String> profiles, VirtualFile... files) throws MavenProcessCanceledException, MavenException {
    myTree.resetManagedFilesAndProfiles(asList(files), profiles);
    myTree.updateAll(getMavenGeneralSettings(), EMPTY_MAVEN_PROCESS);
  }

  private void update(VirtualFile file) throws MavenProcessCanceledException {
    myTree.update(asList(file), getMavenGeneralSettings(), EMPTY_MAVEN_PROCESS);
  }

  private void deleteProject(VirtualFile file) throws MavenProcessCanceledException {
    myTree.delete(asList(file), getMavenGeneralSettings(), EMPTY_MAVEN_PROCESS);
  }

  private void updateTimestamps(VirtualFile... files) throws IOException {
    for (VirtualFile each : files) {
      each.setBinaryContent(each.contentsToByteArray());
    }
  }

  private static class MyLoggingListener implements MavenProjectsTree.Listener {
    String log = "";

    public void profilesChanged(List<String> profiles) {
    }

    public void projectsRead(List<MavenProject> projects) {
      log += "projectsRead ";
      if (!projects.isEmpty()) {
        log += StringUtil.join(projects, new Function<MavenProject, String>() {
          public String fun(MavenProject each) {
            return each.getMavenId().artifactId;
          }
        }, ", ") + " ";
      }
    }

    public void projectRead(MavenProject project) {
      log += "read " + project.getMavenId().artifactId + " ";
    }

    public void projectAggregatorChanged(MavenProject project) {
      log += "reconnected " + project.getMavenId().artifactId + " ";
    }

    public void projectRemoved(MavenProject project) {
      log += "removed " + project.getMavenId().artifactId + " ";
    }

    public void projectResolved(boolean quickResolve, MavenProject project, org.apache.maven.project.MavenProject nativeMavenProject) {
      log += "resolved " + project.getMavenId().artifactId + " ";
    }
  }
}