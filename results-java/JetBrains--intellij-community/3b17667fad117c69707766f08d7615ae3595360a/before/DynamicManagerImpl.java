package org.jetbrains.plugins.groovy.annotator.intentions.dynamic;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.impl.DaemonProgressIndicator;
import com.intellij.codeInsight.daemon.impl.ExternalToolPass;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ui.treetable.ListTreeTableModelOnColumns;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.groovy.annotator.intentions.QuickfixUtil;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements.DItemElement;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements.DMethodElement;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements.DPropertyElement;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiManager;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiElementFactoryImpl;
import org.jetbrains.plugins.groovy.lang.psi.util.GrDynamicImplicitMethodImpl;
import org.jetbrains.plugins.groovy.lang.psi.util.GrDynamicImplicitPropertyImpl;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 23.11.2007
 */
@State(
    name = "DynamicManagerImpl",
    storages = {
    @Storage(
        id = "myDir",
        file = "$WORKSPACE_FILE$"
    )}
)

public class DynamicManagerImpl extends DynamicManager {
  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.annotator.intentions.dynamic.DynamicManagerImpl");

  private final Project myProject;
  private List<DynamicChangeListener> myListeners = new ArrayList<DynamicChangeListener>();
  private DRootElement myRootElement = new DRootElement();

  private final Map<Pair<String, Pair<String, Pair<String, String[]>>>, GrDynamicImplicitMethodImpl> myNamesToMethodsMap = new HashMap<Pair<String, Pair<String, Pair<String, String[]>>>, GrDynamicImplicitMethodImpl>();

  private final Map<Pair<String, Pair<String, String>>, GrDynamicImplicitPropertyImpl> myNamesToPropertiesMap = new HashMap<Pair<String, Pair<String, String>>, GrDynamicImplicitPropertyImpl>();

  public DynamicManagerImpl(Project project) {
    myProject = project;
  }

  public Project getProject() {
    return myProject;
  }


  public void initComponent() {
  }

  public void addProperty(DClassElement classElement, DPropertyElement propertyElement) {
    if (classElement == null) return;

    classElement.addProperty(propertyElement);
    addItemInTree(classElement, propertyElement);
  }

  private void addItemInTree(DClassElement classElement, DItemElement itemElement) {
    final ToolWindow window = ToolWindowManager.getInstance(myProject).getToolWindow(DynamicToolWindowWrapper.DYNAMIC_TOOLWINDOW_ID);
    final ListTreeTableModelOnColumns myTreeTableModel = DynamicToolWindowWrapper.getTreeTableModel(window, myProject);

    final Object rootObject = myTreeTableModel.getRoot();
    if (!(rootObject instanceof DefaultMutableTreeNode)) return;
    final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootObject;

    DefaultMutableTreeNode node = new DefaultMutableTreeNode(itemElement);
    if (rootNode.getChildCount() > 0) {
      for (DefaultMutableTreeNode classNode = (DefaultMutableTreeNode) rootNode.getFirstChild();
           classNode != null;
           classNode = (DefaultMutableTreeNode) rootNode.getChildAfter(classNode)) {

        final Object classRow = classNode.getUserObject();
        if (!(classRow instanceof DClassElement)) return;

        DClassElement otherClassName = (DClassElement) classRow;
        if (otherClassName.equals(classElement)) {
          int index = getIndexToInsert(classNode, itemElement);
          classNode.insert(node, index);
          myTreeTableModel.nodesWereInserted(classNode, new int[]{index});
          DynamicToolWindowWrapper.setSelectedNode(node);
          return;
        }
      }
    }

    // if there is no such class in tree
    int index = getIndexToInsert(rootNode, classElement);
    DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(classElement);
    rootNode.insert(classNode, index);
    myTreeTableModel.nodesWereInserted(rootNode, new int[]{index});

    classNode.add(node);
    myTreeTableModel.nodesWereInserted(classNode, new int[]{0});

    DynamicToolWindowWrapper.setSelectedNode(node);
  }

  private int getIndexToInsert(DefaultMutableTreeNode parent, DNamedElement namedElement) {
    if (parent.getChildCount() == 0) return 0;

    int res = 0;
    for (DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getFirstChild();
         child != null;
         child = (DefaultMutableTreeNode) parent.getChildAfter(child)) {
      Object childObject = child.getUserObject();

      if (!(childObject instanceof DNamedElement)) return 0;

      String otherName = ((DNamedElement) childObject).getName();
      if (otherName.compareTo(namedElement.getName()) > 0) return res;
      res++;
    }
    return res;
  }

  public void addMethod(DClassElement classElement, DMethodElement methodElement) {
    if (classElement == null) return;

    classElement.addMethod(methodElement);
    addItemInTree(classElement, methodElement);
  }

  public void removeClassElement(String containingClassName) {

    final DRootElement rootElement = getRootElement();
    rootElement.removeClassElement(containingClassName);
  }

  public void removePropertyElement(DPropertyElement propertyElement) {
    final DClassElement classElement = getClassElementByItem(propertyElement);
    assert classElement != null;

    classElement.removeProperty(propertyElement.getName());
  }

  @NotNull
  public Collection<DPropertyElement> findDynamicPropertiesOfClass(String className) {
    final DClassElement classElement = findClassElement(getRootElement(), className);

    if (classElement != null) {
      return classElement.getProperties();
    }
    return new ArrayList<DPropertyElement>();
  }

  @NotNull
  public String[] getPropertiesNamesOfClass(String conatainingClassName) {
    final DClassElement classElement = findClassElement(getRootElement(), conatainingClassName);

    Set<String> result = new HashSet<String>();
    if (classElement != null) {
      for (DPropertyElement propertyElement : classElement.getProperties()) {
        result.add(propertyElement.getName());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  @Nullable
  public String getPropertyType(String className, String propertyName) {
    final DPropertyElement dynamicProperty = findConcreteDynamicProperty(getRootElement(), className, propertyName);

    if (dynamicProperty == null) return null;
    return dynamicProperty.getType();
  }

  public void replaceDynamicMethod(DMethodElement oldMethod, DMethodElement newElement) {
//    final DClassElement className = oldMethod.getClassElement();
//    assert className.getName().equals(newElement.getClassElement().getName());

//    className.removeMethod(oldMethod);
  }

  @NotNull
  public Collection<DClassElement> getAllContainingClasses() {
    //TODO: use iterator
    final DRootElement root = getRootElement();

    return root.getContainingClasses();
  }

  public DRootElement getRootElement() {
    return myRootElement;
  }

  /*
  * Adds dynamicPropertyChange listener
  */
  public void addDynamicChangeListener(DynamicChangeListener listener) {
    myListeners.add(listener);
  }

  /*
  * Removes dynamicPropertyChange listener
  */
  public void removeDynamicChangeListener(DynamicChangeListener listener) {
    myListeners.remove(listener);
  }

  public void replaceDynamicProperty(DPropertyElement oldElement, DPropertyElement newElement) {
    removePropertyElement(oldElement);
  }

  /*
  * Changes dynamic property
  */

  public String replaceDynamicPropertyName(String className, String oldPropertyName, String newPropertyName) {
    final DClassElement classElement = findClassElement(getRootElement(), className);
    if (classElement == null) return null;

    final DPropertyElement oldPropertyElement = classElement.getPropertyByName(oldPropertyName);
    if (oldPropertyElement == null) return null;
    classElement.removeProperty(oldPropertyName);
    classElement.addProperty(new DPropertyElement(newPropertyName, oldPropertyElement.getType()));
    fireChange();

    return newPropertyName;
  }

  public String replaceDynamicPropertyType(String className, String propertyName, String oldPropertyType, String newPropertyType) {
    final DPropertyElement property = findConcreteDynamicProperty(className, propertyName);

    if (property == null) return null;

    property.setType(newPropertyType);
    return newPropertyType;
  }

  /*
  * Find dynamic property in class with name
  */

  @Nullable
  public DMethodElement findConcreteDynamicMethod(DRootElement rootElement, String conatainingClassName, String methodName, String[] parametersTypes) {
    DClassElement classElement = findClassElement(rootElement, conatainingClassName);
    if (classElement == null) return null;

    return classElement.getMethod(methodName, parametersTypes);
  }

  //  @Nullable

  public DMethodElement findConcreteDynamicMethod(String conatainingClassName, String name, String[] parameterTypes) {
    return findConcreteDynamicMethod(getRootElement(), conatainingClassName, name, parameterTypes);
  }

  @Nullable
  public String getMethodReturnType(String className, String methodName, String[] paramTypes) {
    final DMethodElement methodElement = findConcreteDynamicMethod(getRootElement(), className, methodName, paramTypes);

    if (methodElement == null) return null;

    return methodElement.getType();
  }

  public void removeMethodElement(DMethodElement methodElement) {
    final DClassElement classElement = getClassElementByItem(methodElement);
    assert classElement != null;

    classElement.removeMethod(methodElement);
  }

  public void removeItemElement(DItemElement element) {
    if (element instanceof DPropertyElement) {
      removePropertyElement(((DPropertyElement) element));
    } else if (element instanceof DMethodElement) {
      removeMethodElement(((DMethodElement) element));
    }
  }

  public void replaceDynamicMethodType(String className, String name, List<MyPair> myPairList, String oldType, String newType) {
    final DMethodElement method = findConcreteDynamicMethod(className, name, QuickfixUtil.getArgumentsTypes(myPairList));

    if (method == null) return;
    method.setType(newType);
  }

  @NotNull
  public DClassElement getOrCreateClassElement(Project project, String className, boolean binded) {
    DClassElement classElement = DynamicManager.getInstance(myProject).getRootElement().getClassElement(className);
    if (classElement == null) {
      return new DClassElement(project, className, binded);
    }

    return classElement;
  }

  @Nullable
  public DClassElement getClassElementByItem(DItemElement itemElement) {
    final Collection<DClassElement> classes = getAllContainingClasses();
    for (DClassElement aClass : classes) {
      if (aClass.containsElement(itemElement)) return aClass;
    }
    return null;
  }

  public void replaceDynamicMethodName(String className, String oldName, String newName, String[] types) {
    final DMethodElement oldMethodElement = findConcreteDynamicMethod(className, oldName, types);
    if (oldMethodElement != null) {
      oldMethodElement.setName(newName);
    }
  }

  public GrDynamicImplicitMethodImpl getCashedOrCreateMethod(PsiManager manager, @NonNls String name, @NonNls String type, String containingClassName, PsiFile containingFile, final String[] paramTypes) {
    final Pair<String, String[]> typeAndPairs = new Pair<String, String[]>(type, paramTypes);

    final Pair<String, Pair<String, String[]>> methodPair = new Pair<String, Pair<String, String[]>>(name, typeAndPairs);
    final Pair<String, Pair<String, Pair<String, String[]>>> pair = new Pair<String, Pair<String, Pair<String, String[]>>>(containingClassName, methodPair);

    final GrDynamicImplicitMethodImpl implicitMethod = myNamesToMethodsMap.get(pair);

    if (implicitMethod != null) return implicitMethod;

    final GrMethod method = GroovyPsiElementFactory.getInstance(myProject).createMethodFromText(name, type, paramTypes);

    final GrDynamicImplicitMethodImpl newMethod = new GrDynamicImplicitMethodImpl(manager, method, containingClassName, containingFile);
    myNamesToMethodsMap.put(pair, newMethod);

    return newMethod;
  }

  public GrDynamicImplicitPropertyImpl getCashedOrCreateProperty(PsiManager manager, String name, String type, String containingClassName, PsiFile containingFile) {
    final Pair<String, String> property = new Pair<String, String>(name, type);
    final Pair<String, Pair<String, String>> pair = new Pair<String, Pair<String, String>>(containingClassName, property);
    final GrDynamicImplicitPropertyImpl implicitProperty = myNamesToPropertiesMap.get(pair);

    if (implicitProperty != null) return implicitProperty;

    final GrDynamicImplicitPropertyImpl newPropery = new GrDynamicImplicitPropertyImpl(manager, name, type, containingClassName, containingFile);
    myNamesToPropertiesMap.put(pair, newPropery);

    return newPropery;
  }

  public String[] getMethodsNamesOfClass(String qualifiedName) {
    final DClassElement classElement = getOrCreateClassElement(myProject, qualifiedName, false);
    final Set<DMethodElement> methods = classElement.getMethods();

    List<String> result = new ArrayList<String>();
    for (DMethodElement method : methods) {
      result.add(method.getName());
    }

    return result.toArray(new String[result.size()]);
  }

  public Iterable<PsiMethod> getMethods(String classQname) {
    return Collections.emptyList();
  }

  public String replaceClassName(String oldClassName, String newClassName) {
    final DRootElement rootElement = getRootElement();
    final DClassElement oldClassElement = findClassElement(rootElement, oldClassName);
    if (oldClassElement == null) return oldClassName;

    oldClassElement.setName(newClassName);

    fireChange();

    return newClassName;
  }

  public void fireChange() {
    for (DynamicChangeListener listener : myListeners) {
      listener.dynamicPropertyChange();
    }

    fireChangeCodeAnalyze();
//    fireChangeToolWindow();
  }

  private void fireChangeToolWindow() {
    final ToolWindow window = ToolWindowManager.getInstance(myProject).getToolWindow(DynamicToolWindowWrapper.DYNAMIC_TOOLWINDOW_ID);

    window.getComponent().revalidate();
    window.getComponent().repaint();
  }

  private void fireChangeCodeAnalyze() {
    final Editor textEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
    if (textEditor == null) return;
    final PsiFile file = PsiDocumentManager.getInstance(myProject).getPsiFile(textEditor.getDocument());
    if (file == null) return;

    ExternalToolPass externalToolPass = new ExternalToolPass(file, textEditor, file.getTextRange().getStartOffset(), file.getTextRange().getStartOffset());
    externalToolPass.doCollectInformation(new DaemonProgressIndicator());
    GroovyPsiManager.getInstance(myProject).dropTypesCache();
    DaemonCodeAnalyzer.getInstance(myProject).restart();
  }

  @Nullable
  public DPropertyElement findConcreteDynamicProperty(final String conatainingClassName, final String propertyName) {
    return findConcreteDynamicProperty(getRootElement(), conatainingClassName, propertyName);
  }

  @Nullable
  public DPropertyElement findConcreteDynamicProperty(DRootElement rootElement, final String conatainingClassName, final String propertyName) {
    final DClassElement classElement = rootElement.getClassElement(conatainingClassName);

    if (classElement == null) return null;

    return classElement.getPropertyByName(propertyName);
  }

  @Nullable
  private DClassElement findClassElement(DRootElement rootElement, final String conatainingClassName) {
    return rootElement.getClassElement(conatainingClassName);
  }

  public void disposeComponent() {
  }

  @NotNull
  public String getComponentName() {
    return "DynamicManagerImpl";
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  /**
   * On exit
   */
  public Element getState() {
    return XmlSerializer.serialize(myRootElement);
  }

  /*
   * On loading
   */
  public void loadState(Element element) {
    myRootElement = XmlSerializer.deserialize(element, myRootElement.getClass());
  }
}