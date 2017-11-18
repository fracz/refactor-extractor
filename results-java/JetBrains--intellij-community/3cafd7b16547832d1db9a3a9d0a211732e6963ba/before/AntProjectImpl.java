package com.intellij.lang.ant.psi.impl;

import com.intellij.lang.ant.psi.AntElement;
import com.intellij.lang.ant.psi.AntProject;
import com.intellij.lang.ant.psi.AntProperty;
import com.intellij.lang.ant.psi.AntTarget;
import com.intellij.lang.ant.psi.introspection.AntAttributeType;
import com.intellij.lang.ant.psi.introspection.AntTypeDefinition;
import com.intellij.lang.ant.psi.introspection.AntTypeId;
import com.intellij.lang.ant.psi.introspection.impl.AntTypeDefinitionImpl;
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
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Property;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"UseOfObsoleteCollectionType"})
public class AntProjectImpl extends AntStructuredElementImpl implements AntProject {
  final static AntTarget[] EMPTY_TARGETS = new AntTarget[0];
  private AntTarget[] myTargets;
  /**
   * Map of class names to task definitions.
   */
  private Map<String, AntTypeDefinition> myTypeDefinitions;
  private AntTypeDefinition[] myTypeDefinitionArrays;
  private AntTypeDefinition myTargetDefinition;
  private List<AntProperty> myPredefinedProps = new ArrayList<AntProperty>();

  public AntProjectImpl(final AntFileImpl parent, final XmlTag tag) {
    super(parent, tag);
    myDefinition = createProjectDefinition();
  }

  private AntProjectImpl(final AntFileImpl parent, final XmlTag tag, final AntTypeDefinition projectDef) {
    super(parent, tag);
    myDefinition = projectDef;
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

  @NotNull
  public AntTypeDefinition[] getBaseTypeDefinitions() {
    if (myTypeDefinitionArrays != null) return myTypeDefinitionArrays;
    getBaseTypeDefinition(null);
    return myTypeDefinitionArrays = myTypeDefinitions.values().toArray(new AntTypeDefinition[myTypeDefinitions.size()]);
  }

  @Nullable
  public AntTypeDefinition getBaseTypeDefinition(final String className) {
    if (myTypeDefinitions != null) return myTypeDefinitions.get(className);
    myTypeDefinitions = new HashMap<String, AntTypeDefinition>();
    Project project = new Project();
    project.init();
    // first, create task definitons without nested elements
    updateTypeDefinitions(project.getTaskDefinitions(), true);
    // second, create definitions for data types
    updateTypeDefinitions(project.getDataTypeDefinitions(), false);
    // third, load and set predefined properties
    loadPredefinedProperties(project);
    return myTypeDefinitions.get(className);
  }

  @NotNull
  public AntTypeDefinition getTargetDefinition() {
    getBaseTypeDefinition(null);
    if (myTargetDefinition == null) {
      @NonNls final HashMap<String, AntAttributeType> targetAttrs = new HashMap<String, AntAttributeType>();
      targetAttrs.put("name", AntAttributeType.STRING);
      targetAttrs.put("depends", AntAttributeType.STRING);
      targetAttrs.put("if", AntAttributeType.STRING);
      targetAttrs.put("unless", AntAttributeType.STRING);
      targetAttrs.put("description", AntAttributeType.STRING);
      final HashMap<AntTypeId, String> targetElements = new HashMap<AntTypeId, String>();
      for (AntTypeDefinition def : getBaseTypeDefinitions()) {
        targetElements.put(def.getTypeId(), def.getClassName());
      }
      myTargetDefinition = new AntTypeDefinitionImpl(new AntTypeId("target"), Target.class.getName(), false, targetAttrs, targetElements);
      registerCustomType(myTargetDefinition);
    }
    return myTargetDefinition;
  }

  public void registerCustomType(final AntTypeDefinition definition) {
    myTypeDefinitionArrays = null;
    myTypeDefinitions.put(definition.getClassName(), definition);
  }

  private void updateTypeDefinitions(final Hashtable ht, final boolean isTask) {
    if (ht == null) return;
    final Enumeration types = ht.keys();
    while (types.hasMoreElements()) {
      final String typeName = (String) types.nextElement();
      final Class typeClass = (Class) ht.get(typeName);
      AntTypeDefinition def = createTypeDefinition(new AntTypeId(typeName, getSourceElement().getNamespace()), typeClass, isTask);
      if (def != null) {
        myTypeDefinitions.put(def.getClassName(), def);
      }
    }
  }

  private void loadPredefinedProperties(Project project) {
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
      AntTypeDefinition propertyDef = myTypeDefinitions.get(Property.class.getName());
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

  private AntTypeDefinitionImpl createProjectDefinition() {
    getBaseTypeDefinition(null);
    @NonNls final HashMap<String, AntAttributeType> projectAttrs = new HashMap<String, AntAttributeType>();
    projectAttrs.put("name", AntAttributeType.STRING);
    projectAttrs.put("default", AntAttributeType.STRING);
    projectAttrs.put("basedir", AntAttributeType.STRING);
    final HashMap<AntTypeId, String> projectElements = new HashMap<AntTypeId, String>();
    for (AntTypeDefinition def : getBaseTypeDefinitions()) {
      if (def.isTask()) {
        projectElements.put(def.getTypeId(), def.getClassName());
      }
    }
    final AntTypeDefinition def = getTargetDefinition();
    projectElements.put(def.getTypeId(), def.getClassName());
    return new AntTypeDefinitionImpl(new AntTypeId("project"), Project.class.getName(), false, projectAttrs, projectElements);
  }

  static AntTypeDefinition createTypeDefinition(final AntTypeId id, final Class taskClass, final boolean isTask) {
    final IntrospectionHelper helper = getHelperExceptionSafe(taskClass);
    if (helper == null) return null;
    final HashMap<String, AntAttributeType> attributes = new HashMap<String, AntAttributeType>();
    final Enumeration attrEnum = helper.getAttributes();
    while (attrEnum.hasMoreElements()) {
      String attr = (String) attrEnum.nextElement();
      final Class attrClass = helper.getAttributeType(attr);
      if (int.class.equals(attrClass)) {
        attributes.put(attr, AntAttributeType.INTEGER);
      } else if (boolean.class.equals(attrClass)) {
        attributes.put(attr, AntAttributeType.BOOLEAN);
      } else {
        attributes.put(attr, AntAttributeType.STRING);
      }
    }
    final HashMap<AntTypeId, String> nestedDefinitions = new HashMap<AntTypeId, String>();
    final Enumeration nestedEnum = helper.getNestedElements();
    while (nestedEnum.hasMoreElements()) {
      final String nestedElement = (String) nestedEnum.nextElement();
      final String className = ((Class) helper.getNestedElementMap().get(nestedElement)).getName();
      nestedDefinitions.put(new AntTypeId(nestedElement), className);
    }
    return new AntTypeDefinitionImpl(id, taskClass.getName(), isTask, attributes, nestedDefinitions);
  }

  private static IntrospectionHelper getHelperExceptionSafe(Class c) {
    try {
      return IntrospectionHelper.getHelper(c);
    }
    catch (Throwable e) {
      // TODO[lvo]: please review.
      // Problem creating introspector like classes this task depends on cannot be loaded or missing.
    }
    return null;
  }
}