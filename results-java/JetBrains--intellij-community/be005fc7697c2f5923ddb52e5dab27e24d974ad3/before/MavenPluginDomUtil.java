package org.jetbrains.idea.maven.dom;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.idea.maven.dom.model.MavenDomPlugin;
import org.jetbrains.idea.maven.dom.plugin.MavenDomPluginModel;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;

public class MavenPluginDomUtil {
  public static MavenDomPluginModel getMavenPlugin(DomElement element) {
    Project p = element.getXmlElement().getProject();

    MavenDomPlugin pluginElement = element.getParentOfType(MavenDomPlugin.class, false);
    if (pluginElement == null) return null;

    VirtualFile pluginXmlFile = getPluginXmlFile(p, pluginElement);
    if (pluginXmlFile == null) return null;

    return MavenDomUtil.getMavenPluginModel(p, pluginXmlFile);
  }

  private static VirtualFile getPluginXmlFile(Project p, MavenDomPlugin pluginElement) {
    String groupId = resolveProperties(pluginElement.getGroupId());
    String artifactId = resolveProperties(pluginElement.getArtifactId());
    String version = resolveProperties(pluginElement.getVersion());

    File file = MavenArtifactUtil.getArtifactFile(MavenProjectsManager.getInstance(p).getLocalRepository(),
                                                    groupId, artifactId, version, "jar");
    VirtualFile pluginFile = LocalFileSystem.getInstance().findFileByIoFile(file);
    if (pluginFile == null) return null;

    VirtualFile pluginJarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(pluginFile);
    if (pluginJarRoot == null) return null;
    return pluginJarRoot.findFileByRelativePath(MavenArtifactUtil.MAVEN_PLUGIN_DESCRIPTOR);
  }

  private static String resolveProperties(GenericDomValue<String> value) {
    return MavenPropertyResolver.resolve(value);
  }
}