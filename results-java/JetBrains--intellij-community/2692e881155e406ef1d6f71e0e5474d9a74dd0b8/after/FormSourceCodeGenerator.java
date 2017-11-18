package com.intellij.uiDesigner.make;

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.impl.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.uiDesigner.*;
import com.intellij.uiDesigner.compiler.*;
import com.intellij.uiDesigner.core.SupportCode;
import com.intellij.uiDesigner.lw.*;
import com.intellij.uiDesigner.shared.BorderType;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public final class FormSourceCodeGenerator {
  @NonNls private StringBuffer myBuffer;
  private Stack<Boolean> myIsFirstParameterStack;
  private final Project myProject;
  private final ArrayList<String> myErrors;
  private LayoutSourceGenerator myLayoutSourceGenerator = new GridLayoutSourceGenerator();

  private static Map<Class, LayoutSourceGenerator> myComponentLayoutCodeGenerators = new HashMap<Class, LayoutSourceGenerator>();

  static {
    myComponentLayoutCodeGenerators.put(LwSplitPane.class, new SplitPaneLayoutSourceGenerator());
    myComponentLayoutCodeGenerators.put(LwTabbedPane.class, new TabbedPaneLayoutSourceGenerator());
    myComponentLayoutCodeGenerators.put(LwScrollPane.class, new ScrollPaneLayoutSourceGenerator());
  }

  public FormSourceCodeGenerator(@NotNull final Project project){
    myProject = project;
    myErrors = new ArrayList<String>();
  }

  public void generate(final VirtualFile formFile) {
    final Module module = ModuleUtil.getModuleForFile(myProject, formFile);
    if (module == null) {
      return;
    }

    final PsiPropertiesProvider propertiesProvider = new PsiPropertiesProvider(module);

    final Document doc = FileDocumentManager.getInstance().getDocument(formFile);
    final LwRootContainer rootContainer;
    try {
      rootContainer = Utils.getRootContainer(doc.getText(), propertiesProvider);
    }
    catch (AlienFormFileException ignored) {
      // ignoring this file
      return;
    }
    catch (Exception e) {
      myErrors.add(UIDesignerBundle.message("error.cannot.process.form.file", e));
      return;
    }

    if (rootContainer.getClassToBind() == null) {
      // form skipped - no class to bind
      return;
    }

    ErrorAnalyzer.analyzeErrors(module, formFile, null, rootContainer);
    FormEditingUtil.iterate(
      rootContainer,
      new FormEditingUtil.ComponentVisitor<LwComponent>() {
        public boolean visit(final LwComponent iComponent) {
          final ErrorInfo errorInfo = ErrorAnalyzer.getErrorForComponent(iComponent);
          if (errorInfo != null){
            myErrors.add(errorInfo.myDescription);
          }
          return true;
        }
      }
    );

    if (myErrors.size() != 0) {
      return;
    }

    try {
      _generate(rootContainer, module);
    }
    catch (ClassToBindNotFoundException e) {
      // ignored
      return;
    }
    catch (CodeGenerationException e) {
      myErrors.add(e.getMessage());
    }
    catch (IncorrectOperationException e) {
      myErrors.add(e.getMessage());
    }
  }

  public ArrayList<String> getErrors() {
    return myErrors;
  }

  private void _generate(final LwRootContainer rootContainer, final Module module) throws CodeGenerationException, IncorrectOperationException{
    myBuffer = new StringBuffer();
    myIsFirstParameterStack = new Stack<Boolean>();

    final HashMap<LwComponent,String> component2variable = new HashMap<LwComponent,String>();
    final TObjectIntHashMap<String> class2variableIndex = new TObjectIntHashMap<String>();

    if (rootContainer.getComponentCount() != 1) {
      throw new CodeGenerationException(UIDesignerBundle.message("error.one.toplevel.component.required"));
    }
    if (containsNotEmptyPanelsWithXYLayout((LwComponent)rootContainer.getComponent(0))) {
      throw new CodeGenerationException(UIDesignerBundle.message("error.nonempty.xy.panels.found"));
    }

    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
    generateSetupCodeForComponent((LwComponent)rootContainer.getComponent(0), component2variable, class2variableIndex, scope);

    final String methodText = myBuffer.toString();

    final PsiManager psiManager = PsiManager.getInstance(module.getProject());
    final PsiElementFactory elementFactory = psiManager.getElementFactory();

    final PsiClass aClass = FormEditingUtil.findClassToBind(module, rootContainer.getClassToBind());
    if (aClass == null) {
      throw new ClassToBindNotFoundException(UIDesignerBundle.message("error.class.to.bind.not.found", rootContainer.getClassToBind()));
    }

    cleanup(aClass);

    // [anton] the comments are written according to the SCR 26896
    final PsiClass fakeClass = elementFactory.createClassFromText(
      "{\n" +
      "// GUI initializer generated by " + ApplicationNamesInfo.getInstance().getFullProductName() + " GUI Designer \n" +
      "// >>> IMPORTANT!! <<<\n" +
      "// DO NOT EDIT OR ADD ANY CODE HERE!\n" +
      "" + METHOD_NAME + "();\n" +
      "}\n" +
      "\n" +
      "/** Method generated by " + ApplicationNamesInfo.getInstance().getFullProductName() + " GUI Designer\n" +
      " * >>> IMPORTANT!! <<<\n" +
      " * DO NOT edit this method OR call it in your code!\t" +
      " */\n" +
      "private void " + METHOD_NAME + "()\n" +
      "{\n" +
      methodText +
      "}\n",
      null
    );

    final PsiElement method = aClass.add(fakeClass.getMethods()[0]);
    final PsiElement initializer = aClass.addBefore(fakeClass.getInitializers()[0], method);

    final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(module.getProject());
    codeStyleManager.shortenClassReferences(method);
    codeStyleManager.reformat(method);
    codeStyleManager.shortenClassReferences(initializer);
    codeStyleManager.reformat(initializer);
  }

  @NonNls
  public final static String METHOD_NAME = "$$$setupUI$$$";

  public static void cleanup(final PsiClass aClass) throws IncorrectOperationException{
    final PsiMethod[] methods = aClass.findMethodsByName(METHOD_NAME, false);
    for (int i = 0; i < methods.length; i++) {
      final PsiMethod method = methods[i];

      final PsiClassInitializer[] initializers = aClass.getInitializers();
      for (int j = 0; j < initializers.length; j++) {
        final PsiClassInitializer initializer = initializers[j];

        if (containsMethodIdentifier(initializer, method)) {
          initializer.delete();
        }
      }

      method.delete();
    }
  }

  private static boolean containsMethodIdentifier(final PsiElement element, final PsiMethod setupMethod) {
    if (element instanceof PsiMethodCallExpression) {
      final PsiMethod psiMethod = ((PsiMethodCallExpression)element).resolveMethod();
      if (setupMethod.equals(psiMethod)){
        return true;
      }
    }
    final PsiElement[] children = element.getChildren();
    for (int i = children.length - 1; i >= 0; i--) {
      if (containsMethodIdentifier(children[i], setupMethod)) {
        return true;
      }
    }
    return false;
  }


  private static boolean containsNotEmptyPanelsWithXYLayout(final LwComponent component) {
    if (!(component instanceof LwContainer)) {
      return false;
    }
    final LwContainer container = (LwContainer)component;
    if (container.getComponentCount() == 0){
      return false;
    }
    if (container.isXY()){
      return true;
    }
    for (int i=0; i < container.getComponentCount(); i++){
      if (containsNotEmptyPanelsWithXYLayout((LwComponent)container.getComponent(i))) {
        return true;
      }
    }
    return false;
  }

  private void generateSetupCodeForComponent(
    final LwComponent component,
    final HashMap<LwComponent, String> component2TempVariable,
    final TObjectIntHashMap<String> class2variableIndex,
    final GlobalSearchScope globalSearchScope
  ) throws CodeGenerationException{
    final LwContainer parent = component.getParent();

    final String variable = getVariable(component, component2TempVariable, class2variableIndex);

    final String binding = component.getBinding();
    if (binding != null) {
      myBuffer.append(binding);
    }
    else {
      myBuffer.append("final ");
      myBuffer.append(component.getComponentClassName());
      myBuffer.append(" ");
      myBuffer.append(variable);
    }
    myBuffer.append('=');
    startConstructor(component.getComponentClassName());
    endConstructor(); // will finish the line

    if (component instanceof LwContainer) {
      getComponentLayoutGenerator(component).generateContainerLayout(component, this, variable);
    }

    // introspected properties
    final LwIntrospectedProperty[] introspectedProperties = component.getAssignedIntrospectedProperties();

    // see SCR #35990
    Arrays.sort(introspectedProperties, new Comparator<LwIntrospectedProperty>() {
      public int compare(LwIntrospectedProperty p1, LwIntrospectedProperty p2) {
        return p1.getName().compareTo(p2.getName());
      }
    });

    for (int i = 0; i < introspectedProperties.length; i++) {
      final LwIntrospectedProperty property = introspectedProperties[i];

      Object value = component.getPropertyValue(property);

      final String componentClass = component.getComponentClassName();
      //noinspection HardCodedStringLiteral
      final boolean isTextWithMnemonicProperty =
        "text".equals(property.getName()) &&
        (isAssignableFrom(AbstractButton.class.getName(), componentClass, globalSearchScope) ||
         isAssignableFrom(JLabel.class.getName(), componentClass, globalSearchScope));

      // handle resource bundles
      if (property instanceof LwRbIntroStringProperty) {
        final StringDescriptor descriptor = (StringDescriptor)value;
        if (descriptor.getValue() == null) {
          if (isTextWithMnemonicProperty) {
            startStaticMethodCall(SupportCode.class, "setTextFromBundle");
            pushVar(variable);
            push(descriptor.getBundleName());
            push(descriptor.getKey());
            endMethod();
          }
          else {
            startMethodCall(variable, property.getWriteMethodName());
            push(descriptor);
            endMethod();
          }

          continue;
        }
        else {
          value = descriptor.getValue();
        }
      }

      SupportCode.TextWithMnemonic textWithMnemonic = null;
      if (isTextWithMnemonicProperty) {
        textWithMnemonic = SupportCode.parseText((String)value);
        value = textWithMnemonic.myText;
      }

      startMethodCall(variable, property.getWriteMethodName());


      final String propertyClass = property.getPropertyClassName();
      if (propertyClass.equals(Dimension.class.getName())) {
        newDimension((Dimension)value);
      }
      else if (propertyClass.equals(Integer.class.getName())){
        push(((Integer)value).intValue());
      }
      else if (propertyClass.equals(Double.class.getName())){
        push(((Double)value).doubleValue());
      }
      else if (propertyClass.equals(Boolean.class.getName())){
        push(((Boolean)value).booleanValue());
      }
      else if (propertyClass.equals(Rectangle.class.getName())) {
        newRectangle((Rectangle)value);
      }
      else if (propertyClass.equals(Insets.class.getName())) {
        newInsets((Insets)value);
      }
      else if (propertyClass.equals(String.class.getName())) {
        push((String)value);
      }
      else {
        //noinspection HardCodedStringLiteral
        throw new RuntimeException("unexpected property class: " + propertyClass);
      }

      endMethod();

      // special handling of mnemonics

      if (!isTextWithMnemonicProperty) {
        continue;
      }

      if (textWithMnemonic.myMnemonicIndex == -1) {
        continue;
      }

      if (isAssignableFrom(AbstractButton.class.getName(), componentClass, globalSearchScope)) {
        startMethodCall(variable, "setMnemonic");
        push(textWithMnemonic.getMnemonicChar());
        endMethod();

        startStaticMethodCall(SupportCode.class, "setDisplayedMnemonicIndex");
        pushVar(variable);
        push(textWithMnemonic.myMnemonicIndex);
        endMethod();
      }
      else if (isAssignableFrom(JLabel.class.getName(), componentClass, globalSearchScope)) {
        startMethodCall(variable, "setDisplayedMnemonic");
        push(textWithMnemonic.getMnemonicChar());
        endMethod();

        startStaticMethodCall(SupportCode.class, "setDisplayedMnemonicIndex");
        pushVar(variable);
        push(textWithMnemonic.myMnemonicIndex);
        endMethod();
      }
    }

    // add component to parent
    if (!(component.getParent() instanceof LwRootContainer)) {

      final String parentVariable = getVariable(parent, component2TempVariable, class2variableIndex);
      getComponentLayoutGenerator(component.getParent()).generateComponentLayout(component, this, variable, parentVariable);
    }

    if (component instanceof LwContainer) {
      final LwContainer container = (LwContainer)component;

      // border

      final BorderType borderType = container.getBorderType();
      final StringDescriptor borderTitle = container.getBorderTitle();
      final String borderFactoryMethodName = borderType.getBorderFactoryMethodName();

      final boolean borderNone = borderType.equals(BorderType.NONE);
      if (!borderNone || borderTitle != null) {
        startMethodCall(variable, "setBorder");


        startStaticMethodCall(BorderFactory.class, "createTitledBorder");

        if (!borderNone) {
          startStaticMethodCall(BorderFactory.class, borderFactoryMethodName);
          endMethod();
        }

        push(borderTitle);

        endMethod(); // createTitledBorder

        endMethod(); // setBorder
      }

      for (int i = 0; i < container.getComponentCount(); i++) {
        generateSetupCodeForComponent((LwComponent)container.getComponent(i), component2TempVariable, class2variableIndex, globalSearchScope);
      }
    }
  }

  private LayoutSourceGenerator getComponentLayoutGenerator(final LwComponent lwComponent) {
    LayoutSourceGenerator generator = myComponentLayoutCodeGenerators.get(lwComponent.getClass());
    if (generator != null) {
      return generator;
    }
    return myLayoutSourceGenerator;
  }

  void push(final StringDescriptor descriptor) {
    if (descriptor == null) {
      push((String)null);
    }
    else if (descriptor.getValue() != null) {
      push(descriptor.getValue());
    }
    else {
      startStaticMethodCall(SupportCode.class, "getResourceString");
      push(descriptor.getBundleName());
      push(descriptor.getKey());
      endMethod();
    }
  }

  private boolean isAssignableFrom(final String className, final String fromName, final GlobalSearchScope scope) {
    final PsiClass aClass = PsiManager.getInstance(myProject).findClass(className, scope);
    final PsiClass fromClass = PsiManager.getInstance(myProject).findClass(fromName, scope);
    if (aClass == null || fromClass == null) {
      return false;
    }
    return InheritanceUtil.isInheritorOrSelf(fromClass, aClass, true);
  }

  /**
   * @return variable idx
   */
  private static String getVariable(
    final LwComponent component,
    final HashMap<LwComponent,String> component2variable,
    final TObjectIntHashMap<String> class2variableIndex
  ){
    if (component2variable.containsKey(component)){
      return component2variable.get(component);
    }

    if (component.getBinding() != null) {
      return component.getBinding();
    }

    @NonNls final String className = component.getComponentClassName();

    final String shortName;
    if (className.startsWith("javax.swing.J")) {
      shortName =  className.substring("javax.swing.J".length());
    }
    else {
      final int idx = className.lastIndexOf('.');
      if (idx != -1) {
        shortName = className.substring(idx + 1);
      }
      else {
        shortName = className;
      }
    }

    // todo[anton] check that no generated name is equal to class' field
    if (!class2variableIndex.containsKey(className)) class2variableIndex.put(className, 0);
    class2variableIndex.increment(className);
    int newIndex = class2variableIndex.get(className);

    final String result = Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1) + newIndex;
    component2variable.put(component, result);

    return result;
  }

  void newDimensionOrNull(final Dimension dimension) {
    if (dimension.width == -1 && dimension.height == -1) {
      checkParameter();
      myBuffer.append("null");
    }
    else {
      newDimension(dimension);
    }
  }

  private void newDimension(final Dimension dimension) {
    startConstructor(Dimension.class.getName());
    push(dimension.width);
    push(dimension.height);
    endConstructor();
  }

  void newInsets(final Insets insets){
    startConstructor(Insets.class.getName());
    push(insets.top);
    push(insets.left);
    push(insets.bottom);
    push(insets.right);
    endConstructor();
  }

  private void newRectangle(final Rectangle rectangle){
    startConstructor(Insets.class.getName());
    push(rectangle.x);
    push(rectangle.y);
    push(rectangle.width);
    push(rectangle.height);
    endConstructor();
  }


  void startMethodCall(final String variable, @NonNls final String methodName) {
    checkParameter();

    appendVarName(variable);
    myBuffer.append('.');
    myBuffer.append(methodName);
    myBuffer.append('(');

    myIsFirstParameterStack.push(Boolean.TRUE);
  }

  private void startStaticMethodCall(final Class aClass, @NonNls final String methodName) {
    checkParameter();

    myBuffer.append(aClass.getName());
    myBuffer.append('.');
    myBuffer.append(methodName);
    myBuffer.append('(');

    myIsFirstParameterStack.push(Boolean.TRUE);
  }

  void endMethod() {
    myBuffer.append(')');

    myIsFirstParameterStack.pop();

    if (myIsFirstParameterStack.empty()) {
      myBuffer.append(";\n");
    }
  }

  void startConstructor(final String className) {
    checkParameter();

    myBuffer.append("new ");
    myBuffer.append(className);
    myBuffer.append('(');

    myIsFirstParameterStack.push(Boolean.TRUE);
  }

  void endConstructor() {
    endMethod();
  }

  void push(final int value) {
    checkParameter();
    myBuffer.append(value);
  }

  void push(final int value, final TIntObjectHashMap map){
    final String stringRepresentation = (String)map.get(value);
    if (stringRepresentation != null) {
      checkParameter();
      myBuffer.append(stringRepresentation);
    }
    else {
      push(value);
    }
  }

  private void push(final double value) {
    checkParameter();
    myBuffer.append(value);
  }

  void push(final boolean value) {
    checkParameter();
    myBuffer.append(value);
  }

  void push(final String value) {
    checkParameter();
    if (value == null) {
      myBuffer.append("null");
    }
    else {
      myBuffer.append('"');
      myBuffer.append(StringUtil.escapeStringCharacters(value));
      myBuffer.append('"');
    }
  }

  void pushVar(final String variable) {
    checkParameter();
    appendVarName(variable);
  }

  private void appendVarName(final String variable) {
    myBuffer.append(variable);
  }

  void checkParameter() {
    if (!myIsFirstParameterStack.empty()) {
      final Boolean b = myIsFirstParameterStack.pop();
      if (b.equals(Boolean.FALSE)) {
        myBuffer.append(',');
      }
      myIsFirstParameterStack.push(Boolean.FALSE);
    }
  }
}