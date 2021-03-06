package com.intellij.lang.ant.psi.impl;

import com.intellij.lang.ant.psi.AntElement;
import com.intellij.lang.ant.psi.AntProject;
import com.intellij.lang.ant.psi.AntProperty;
import com.intellij.lang.ant.psi.AntTarget;
import com.intellij.lang.ant.psi.introspection.AntTypeDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceType;
import com.intellij.psi.impl.source.resolve.reference.impl.GenericReference;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.StringBuilderSpinAllocator;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AntProjectImpl extends AntStructuredElementImpl implements AntProject {
  final static AntTarget[] EMPTY_TARGETS = new AntTarget[0];
  private AntTarget[] myTargets;
  private List<AntProperty> myPredefinedProps = new ArrayList<AntProperty>();

  public AntProjectImpl(final AntFileImpl parent, final XmlTag tag, final AntTypeDefinition projectDefinition) {
    super(parent, tag);
    myDefinition = projectDefinition;
  }

  @NonNls
  public String toString() {
    @NonNls StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      builder.append("AntProject[");
      final String name = getName();
      builder.append((name == null) ? "unnamed" : name);
      builder.append("]");
      if (getDescription() != null) {
        builder.append(" :");
        builder.append(getDescription());
      }
      return builder.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  public void clearCaches() {
    super.clearCaches();
    myTargets = null;
  }

  @Nullable
  public String getBaseDir() {
    return getSourceElement().getAttributeValue("basedir");
  }

  @Nullable
  public String getDescription() {
    final XmlTag tag = getSourceElement().findFirstSubTag("description");
    return tag != null ? tag.getValue().getTrimmedText() : null;
  }

  @NotNull
  public AntTarget[] getTargets() {
    if (myTargets != null) return myTargets;
    final List<AntTarget> targets = new ArrayList<AntTarget>();
    for (final AntElement child : getChildren()) {
      if (child instanceof AntTarget) targets.add((AntTarget) child);
    }
    return myTargets = targets.toArray(new AntTarget[targets.size()]);
  }

  @Nullable
  public AntTarget getDefaultTarget() {
    final PsiReference[] references = getReferences();
    for (PsiReference ref : references) {
      final GenericReference reference = (GenericReference) ref;
      if (reference.getType().isAssignableTo(ReferenceType.ANT_TARGET)) {
        return (AntTarget) reference.resolve();
      }
    }
    return null;
  }

  @Nullable
  public AntTarget getTarget(final String name) {
    AntTarget[] targets = getTargets();
    for (AntTarget target : targets) {
      if (name.equals(target.getName())) {
        return target;
      }
    }
    return null;
  }

  @Nullable
  public PsiElement getProperty(final String name) {
    checkPropertiesMap();
    return super.getProperty(name);
  }

  @NotNull
  public PsiElement[] getProperties() {
    checkPropertiesMap();
    return super.getProperties();
  }


  public void setProperty(final String name, final PsiElement element) {
    checkPropertiesMap();
    super.setProperty(name, element);
  }

  @SuppressWarnings({"UseOfObsoleteCollectionType"})
  void loadPredefinedProperties(Project project) {
    Hashtable ht = project.getProperties();
    final Enumeration props = ht.keys();
    @NonNls final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    builder.append("<project name=\"fake\">");
    try {
      while (props.hasMoreElements()) {
        final String name = (String) props.nextElement();
        final String value = (String) ht.get(name);
        builder.append("<property name=\"");
        builder.append(name);
        builder.append("\" value=\"");
        builder.append(value);
        builder.append("\"/>");
      }
      final String basedir = getBaseDir();
      if (basedir != null) {
        builder.append("<property name=\"basedir\" value=\"");
        builder.append(basedir);
        builder.append("\"/>");
      }
      builder.append("</project>");
      final PsiElementFactory elementFactory = getManager().getElementFactory();
      final XmlFile fakeFile = (XmlFile)
          elementFactory.createFileFromText("dummy.xml", builder.toString());
      final XmlDocument document = fakeFile.getDocument();
      if (document == null) return;
      final XmlTag rootTag = document.getRootTag();
      if (rootTag == null) return;
      getAntFile().getBaseTypeDefinition(Property.class.getName());
      AntTypeDefinition propertyDef = getAntFile().getBaseTypeDefinition(Property.class.getName());
      AntProject fakeProject = new AntProjectImpl(null, rootTag, myDefinition);
      for (XmlTag tag : rootTag.getSubTags()) {
        final AntPropertyImpl property = new AntPropertyImpl(fakeProject, tag, propertyDef) {
          public PsiFile getContainingFile() {
            return getSourceElement().getContainingFile();
          }
        };
        myPredefinedProps.add(property);
      }
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
    setPredefinedProperties();
  }

  private void setPredefinedProperties() {
    for (AntProperty property : myPredefinedProps) {
      setProperty(property.getName(), property);
    }
  }

  private void checkPropertiesMap() {
    if (myProperties == null) {
      myProperties = new HashMap<String, PsiElement>(myPredefinedProps.size());
      setPredefinedProperties();
    }
  }
}