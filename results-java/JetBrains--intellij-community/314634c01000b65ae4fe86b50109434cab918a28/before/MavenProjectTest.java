/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package org.jetbrains.idea.maven.project;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.MavenImportingTestCase;

import java.util.Collection;
import java.util.List;

public class MavenProjectTest extends MavenImportingTestCase {
  public void testCollectingPlugins() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>group1</groupId>" +
                  "      <artifactId>id1</artifactId>" +
                  "      <version>1</version>" +
                  "    </plugin>" +
                  "    <plugin>" +
                  "      <groupId>group1</groupId>" +
                  "      <artifactId>id2</artifactId>" +
                  "    </plugin>" +
                  "    <plugin>" +
                  "      <groupId>group2</groupId>" +
                  "      <artifactId>id1</artifactId>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertModules("project");

    assertPlugins(collectPlugins(), p("group1", "id1"), p("group1", "id2"), p("group2", "id1"));
  }

  public void testCollectingPluginsFromProfilesAlso() throws Exception {
    if (ignore()) return;
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>group</groupId>" +
                  "      <artifactId>id</artifactId>" +
                  "      <version>1</version>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +

                  "<profiles>" +
                  "  <profile>" +
                  "    <id>profile1</id>" +
                  "    <build>" +
                  "      <plugins>" +
                  "        <plugin>" +
                  "          <groupId>group1</groupId>" +
                  "          <artifactId>id1</artifactId>" +
                  "        </plugin>" +
                  "      </plugins>" +
                  "    </build>" +
                  "  </profile>" +
                  "  <profile>" +
                  "    <id>profile2</id>" +
                  "    <build>" +
                  "      <plugins>" +
                  "        <plugin>" +
                  "          <groupId>group2</groupId>" +
                  "          <artifactId>id2</artifactId>" +
                  "        </plugin>" +
                  "      </plugins>" +
                  "    </build>" +
                  "  </profile>" +
                  "</profiles>");

    assertModules("project");

    assertPlugins(collectPlugins(), p("group", "id"));
    assertPlugins(collectPlugins("profile1"), p("group", "id"), p("group1", "id1"));
    assertPlugins(collectPlugins("profile2"), p("group", "id"), p("group2", "id2"));
    assertPlugins(collectPlugins("profile1", "profile2"), p("group", "id"), p("group1", "id1"), p("group2", "id2"));
  }

  public void testFindingPlugin() throws Exception {
    if (ignore()) return;
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>group</groupId>" +
                  "      <artifactId>id</artifactId>" +
                  "      <version>1</version>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>" +

                  "<profiles>" +
                  "  <profile>" +
                  "    <id>profile1</id>" +
                  "    <build>" +
                  "      <plugins>" +
                  "        <plugin>" +
                  "          <groupId>group1</groupId>" +
                  "          <artifactId>id1</artifactId>" +
                  "        </plugin>" +
                  "      </plugins>" +
                  "    </build>" +
                  "  </profile>" +
                  "  <profile>" +
                  "    <id>profile2</id>" +
                  "    <build>" +
                  "      <plugins>" +
                  "        <plugin>" +
                  "          <groupId>group2</groupId>" +
                  "          <artifactId>id2</artifactId>" +
                  "        </plugin>" +
                  "      </plugins>" +
                  "    </build>" +
                  "  </profile>" +
                  "</profiles>");

    assertModules("project");

    assertEquals(p("group", "id"), findPlugin("group", "id"));
    assertNull(findPlugin("group1", "id1"));

    assertEquals(p("group1", "id1"), findPlugin("group1", "id1", "profile1"));
    assertNull(findPlugin("group2", "id2", "profile1"));
  }

  public void testFindingMavenGroupPluginEvenIfGroupIsNotSpecified() throws Exception {
    if (ignore()) return;

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <artifactId>some.plugin.id</artifactId>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");
    assertModules("project");

    assertEquals(p("org.apache.maven.plugins", "some.plugin.id"),
                 findPlugin("org.apache.maven.plugins", "some.plugin.id"));
    assertNull(findPlugin("some.other.group.id", "some.plugin.id"));
  }

  public void testPluginConfiguration() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>group</groupId>" +
                  "      <artifactId>id1</artifactId>" +
                  "      <version>1</version>" +
                  "    </plugin>" +
                  "    <plugin>" +
                  "      <groupId>group</groupId>" +
                  "      <artifactId>id2</artifactId>" +
                  "      <version>1</version>" +
                  "      <configuration>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "    <plugin>" +
                  "      <groupId>group</groupId>" +
                  "      <artifactId>id3</artifactId>" +
                  "      <version>1</version>" +
                  "      <configuration>" +
                  "        <one>" +
                  "          <two>foo</two>" +
                  "        </one>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertNull(findPluginConfig("group", "id1", "one.two"));
    assertNull(findPluginConfig("group", "id2", "one.two"));
    assertEquals("foo", findPluginConfig("group", "id3", "one.two"));
    assertNull(findPluginConfig("group", "id3", "one.two.three"));
  }

  public void testPluginGoalConfiguration() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>group</groupId>" +
                  "      <artifactId>id</artifactId>" +
                  "      <version>1</version>" +
                  "      <executions>" +
                  "        <execution>" +
                  "          <id>a</id>" +
                  "          <goals>" +
                  "            <goal>compile</goal>" +
                  "          </goals>" +
                  "          <configuration>" +
                  "            <one>" +
                  "              <two>a</two>" +
                  "            </one>" +
                  "          </configuration>" +
                  "        </execution>" +
                  "        <execution>" +
                  "          <id>b</id>" +
                  "          <goals>" +
                  "            <goal>testCompile</goal>" +
                  "          </goals>" +
                  "          <configuration>" +
                  "            <one>" +
                  "              <two>b</two>" +
                  "            </one>" +
                  "          </configuration>" +
                  "        </execution>" +
                  "      </executions>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertNull(findPluginGoalConfig("group", "id", "package", "one.two"));
    assertEquals("a", findPluginGoalConfig("group", "id", "compile", "one.two"));
    assertEquals("b", findPluginGoalConfig("group", "id", "testCompile", "one.two"));
  }

  public void testPluginConfigurationHasResolvedVariables() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<properties>" +
                  "  <some.path>somePath</some.path>" +
                  "</properties>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>group</groupId>" +
                  "      <artifactId>id</artifactId>" +
                  "      <version>1</version>" +
                  "      <configuration>" +
                  "        <one>${some.path}</one>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertEquals("somePath", findPluginConfig("group", "id", "one"));
  }

  public void testPluginConfigurationWithStandardVariable() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>group</groupId>" +
                  "      <artifactId>id</artifactId>" +
                  "      <version>1</version>" +
                  "      <configuration>" +
                  "        <one>${project.build.directory}</one>" +
                  "      </configuration>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertEquals(getProjectPath() + "/target",
                 FileUtil.toSystemIndependentName(findPluginConfig("group", "id", "one")));
  }

  public void testCollectingRepositories() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<repositories>" +
                  "  <repository>" +
                  "    <id>one</id>" +
                  "    <url>http://repository.one.com</url>" +
                  "  </repository>" +
                  "  <repository>" +
                  "    <id>two</id>" +
                  "    <url>http://repository.two.com</url>" +
                  "  </repository>" +
                  "</repositories>");

    List<MavenRemoteRepository> result = getMavenProject().getRemoteRepositories();
    assertEquals(3, result.size());
    assertEquals("one", result.get(0).getId());
    assertEquals("two", result.get(1).getId());
    assertEquals("central", result.get(2).getId());
  }

  public void testOverridingCentralRepository() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<repositories>" +
                  "  <repository>" +
                  "    <id>central</id>" +
                  "    <url>http://my.repository.com</url>" +
                  "  </repository>" +
                  "</repositories>");

    List<MavenRemoteRepository> result = getMavenProject().getRemoteRepositories();
    assertEquals(1, result.size());
    assertEquals("central", result.get(0).getId());
    assertEquals("http://my.repository.com", result.get(0).getUrl());
  }

  public void testCollectingRepositoriesFromParent() throws Exception {
    VirtualFile m1 = createModulePom("p1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>p1</artifactId>" +
                                     "<version>1</version>" +

                                     "<repositories>" +
                                     "  <repository>" +
                                     "    <id>one</id>" +
                                     "    <url>http://repository.one.com</url>" +
                                     "  </repository>" +
                                     "  <repository>" +
                                     "    <id>two</id>" +
                                     "    <url>http://repository.two.com</url>" +
                                     "  </repository>" +
                                     "</repositories>");

    VirtualFile m2 = createModulePom("p2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>p2</artifactId>" +
                                     "<version>1</version>" +

                                     "<parent>" +
                                     "  <groupId>test</groupId>" +
                                     "  <artifactId>p1</artifactId>" +
                                     "  <version>1</version>" +
                                     "</parent>");

    importProjects(m1, m2);

    List<MavenRemoteRepository> result = myProjectsTree.getRootProjects().get(0).getRemoteRepositories();
    assertEquals(3, result.size());
    assertEquals("one", result.get(0).getId());
    assertEquals("two", result.get(1).getId());
    assertEquals("central", result.get(2).getId());

    result = myProjectsTree.getRootProjects().get(1).getRemoteRepositories();
    assertEquals(3, result.size());
    assertEquals("one", result.get(0).getId());
    assertEquals("two", result.get(1).getId());
    assertEquals("central", result.get(2).getId());
  }

  public void testStrippingDownMavenModel() throws Exception {
    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +
                  "<build>" +
                  "  <plugins>" +
                  "    <plugin>" +
                  "      <groupId>group1</groupId>" +
                  "      <artifactId>id1</artifactId>" +
                  "      <version>1</version>" +
                  "    </plugin>" +
                  "  </plugins>" +
                  "</build>");

    assertTrue(getMavenProject().getMavenModel().getBuild().getPlugins().isEmpty());
    assertTrue(getMavenProject().getMavenModel().getBuild().getPluginsAsMap().isEmpty());
  }

  private String findPluginConfig(String groupId, String artifactId, String path) {
    return getMavenProject().findPluginConfigurationValue(groupId, artifactId, path);
  }

  private String findPluginGoalConfig(String groupId, String artifactId, String goal, String path) {
    return getMavenProject().findPluginGoalConfigurationValue(groupId, artifactId, goal, path);
  }

  private List<MavenPlugin> collectPlugins(String... profiles) {
    return getMavenProject().getPlugins();
  }

  private void assertPlugins(Collection<MavenPlugin> actual, PluginInfo... expected) {
    assertUnorderedElementsAreEqual(actual, expected);
  }

  private MavenPlugin findPlugin(String groupId, String artifactId, String... profiles) {
    return getMavenProject().findPlugin(groupId, artifactId);
  }

  private MavenProject getMavenProject() {
    return myProjectsTree.getRootProjects().get(0);
  }

  private PluginInfo p(String groupId, String artifactId) {
    return new PluginInfo(groupId, artifactId);
  }

  private class PluginInfo {
    String groupId;
    String artifactId;

    private PluginInfo(String groupId, String artifactId) {
      this.groupId = groupId;
      this.artifactId = artifactId;
    }

    @Override
    public String toString() {
      return "";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      MavenPlugin p = (MavenPlugin)obj;
      return groupId.equals(p.getGroupId()) && artifactId.equals(p.getArtifactId());
    }
  }
}