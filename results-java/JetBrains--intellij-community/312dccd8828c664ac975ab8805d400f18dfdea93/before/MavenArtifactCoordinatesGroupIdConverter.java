package org.jetbrains.idea.maven.dom;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.idea.maven.indices.MavenProjectIndicesManager;
import org.jetbrains.idea.maven.utils.MavenId;

import java.util.Set;

public class MavenArtifactCoordinatesGroupIdConverter extends MavenArtifactCoordinatesConverter {
  @Override
  protected boolean doIsValid(MavenId id, MavenProjectIndicesManager manager, ConvertContext context) {
    if (StringUtil.isEmpty(id.groupId)) return false;
    return manager.hasGroupId(id.groupId);
  }

  @Override
  protected Set<String> doGetVariants(MavenId id, MavenProjectIndicesManager manager) {
    return manager.getGroupIds();
  }
}