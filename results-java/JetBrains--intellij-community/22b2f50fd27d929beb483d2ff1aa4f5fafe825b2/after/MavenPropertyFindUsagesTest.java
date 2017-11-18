package org.jetbrains.idea.maven.dom;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import java.util.List;

public class MavenPropertyFindUsagesTest extends MavenDomTestCase {
  @Override
  protected void setUpInWriteAction() throws Exception {
    super.setUpInWriteAction();

    importProject("<groupId>test</groupId>" +
                  "<artifactId>module1</artifactId>" +
                  "<version>1</version>");
  }

  public void testFindModelPropertyFromReference() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>module1</artifactId>" +
                     "<version>1</version>" +

                     "<name>${<caret>project.version}</name>" +
                     "<description>${project.version}</description>");

    assertSearchResults(myProjectPom,
                        findTag("project.name"),
                        findTag("project.description"));
  }

  public void testFindModelPropertyFromReferenceWithDifferentQualifiers() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>module1</artifactId>" +
                     "<version>1</version>" +

                     "<name>${<caret>version}</name>" +
                     "<description>${pom.version}</description>");

    assertSearchResults(myProjectPom,
                        findTag("project.name"),
                        findTag("project.description"));
  }

  public void testFindUsagesFromTag() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>module1</artifactId>" +
                     "<<caret>version>1</version>" +

                     "<name>${project.version}</name>" +
                     "<description>${version}</description>");

    assertSearchResults(myProjectPom,
                        findTag("project.name"),
                        findTag("project.description"));
  }

  public void testFindUsagesFromTagValue() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>module1</artifactId>" +
                     "<version>1<caret>1</version>" +

                     "<name>${project.version}</name>");

    assertSearchResults(myProjectPom, findTag("project.name"));
  }

  public void testFindUsagesFromProperty() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>module1</artifactId>" +
                     "<version>11</version>" +
                     "<name>${foo}</name>" +
                     "<properties>" +
                     "  <f<caret>oo>value</foo>" +
                     "</properties>");

    assertSearchResultsContain(myProjectPom, findTag("project.name"));
  }

  public void testFindUsagesForEnvProperty() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>module1</artifactId>" +
                     "<version>11</version>" +
                     "<name>${env.<caret>TEMP}</name>" +
                     "<description>${env.TEMP}</description>");

    assertSearchResultsContain(myProjectPom, findTag("project.name"), findTag("project.description"));
  }

  public void testFindUsagesForSystemProperty() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>module1</artifactId>" +
                     "<version>11</version>" +
                     "<name>${use<caret>r.home}</name>" +
                     "<description>${user.home}</description>");

    assertSearchResultsContain(myProjectPom, findTag("project.name"), findTag("project.description"));
  }

  public void testFindUsagesForSystemPropertyInFilteredResources() throws Exception {
    createProjectSubDir("res");

    importProject("<groupId>test</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +
                  "<name>${user.home}</name>" +

                  "<build>" +
                  "  <resources>" +
                  "    <resource>" +
                  "      <directory>res</directory>" +
                  "      <filtering>true</filtering>" +
                  "    </resource>" +
                  "  </resources>" +
                  "</build>");

    VirtualFile f = createProjectSubFile("res/foo.properties",
                                         "foo=abc${user<caret>.home}abc");

    List<PsiElement> result = search(f);
    assertInclude(result, findTag("project.name"), MavenDomUtil.findPropertyValue(myProject, f, "foo"));
  }

  public void testHighlightingFromTag() throws Exception {
    createProjectPom("<groupId>test</groupId>" +
                     "<artifactId>module1</artifactId>" +
                     "<<caret>version>1</version>" +

                     "<name>${project.version}</name>" +
                     "<description>${version}</description>");

    assertHighlighted(myProjectPom,
                      new HighlightInfo(findTag("project.name"), "project.version"),
                      new HighlightInfo(findTag("project.description"), "version"));
  }
}