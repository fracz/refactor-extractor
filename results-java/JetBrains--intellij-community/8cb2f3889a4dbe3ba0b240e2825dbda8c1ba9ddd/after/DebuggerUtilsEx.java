/*
 * Class DebuggerUtilsEx
 * @author Jeka
 */
package com.intellij.debugger.impl;

import com.intellij.debugger.ClassFilter;
import com.intellij.debugger.requests.Requestor;
import com.intellij.debugger.ui.breakpoints.Breakpoint;
import com.intellij.debugger.ui.tree.DebuggerTreeNode;
import com.intellij.debugger.ui.CompletionEditor;
import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.engine.evaluation.*;
import com.intellij.debugger.engine.evaluation.expression.EvaluatorBuilder;
import com.intellij.debugger.jdi.VirtualMachineProxyImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.execution.ExecutionException;
import com.intellij.util.ArrayUtil;
import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.tools.jdi.TransportService;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.*;
import java.util.regex.PatternSyntaxException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

public abstract class DebuggerUtilsEx extends DebuggerUtils {
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.impl.DebuggerUtilsEx");

  public static final int MAX_LABEL_SIZE = 255;
  public static Runnable DO_NOTHING = EmptyRunnable.getInstance();

  public static PsiMethod findPsiMethod(PsiFile file, int offset) {
    PsiElement element = null;

    while(offset >= 0) {
      element = file.findElementAt(offset);
      if(element != null) break;
      offset --;
    }

    for (; element != null; element = element.getParent()) {
      if (element instanceof PsiClass) return null;
      if (element instanceof PsiMethod) return (PsiMethod)element;
    }
    return null;
  }


  public static boolean isAssignableFrom(final String baseQualifiedName, ReferenceType checkedType) {
    return getSuperClass(baseQualifiedName, checkedType) != null;

  }

  public static ReferenceType getSuperClass(final String baseQualifiedName, ReferenceType checkedType) {
    if (baseQualifiedName.equals(checkedType.name())) {
      return checkedType;
    }

    if (checkedType instanceof ClassType) {
      ClassType classType = (ClassType)checkedType;
      ClassType superClassType = classType.superclass();
      if (superClassType != null) {
        ReferenceType superClass = getSuperClass(baseQualifiedName, superClassType);
        if (superClass != null) {
          return superClass;
        }
      }
      List ifaces = classType.allInterfaces();
      for (Iterator it = ifaces.iterator(); it.hasNext();) {
        InterfaceType iface = (InterfaceType)it.next();
        ReferenceType superClass = getSuperClass(baseQualifiedName, iface);
        if (superClass != null) {
          return superClass;
        }
      }
    }

    if (checkedType instanceof InterfaceType) {
      List list = ((InterfaceType)checkedType).superinterfaces();
      for (Iterator it = list.iterator(); it.hasNext();) {
        InterfaceType superInterface = (InterfaceType)it.next();
        ReferenceType superClass = getSuperClass(baseQualifiedName, superInterface);
        if (superClass != null) {
          return superClass;
        }
      }
    }
    return null;
  }

  public static boolean valuesEqual(Value val1, Value val2) {
    if (val1 == null) {
      return val2 == null;
    }
    if (val2 == null) {
      return false;
    }
    if (val1 instanceof StringReference && val2 instanceof StringReference) {
      return ((StringReference)val1).value().equals(((StringReference)val2).value());
    }
    return val1.equals(val2);
  }

  public static String getValueOrErrorAsString(final EvaluationContext evaluationContext, Value value) {
    try {
      return getValueAsString(evaluationContext, value);
    }
    catch (EvaluateException e) {
      return e.getMessage();
    }
  }

  public static boolean isCharOrInteger(Value value) {
    return value != null &&
           value instanceof CharValue ||
           isInteger(value);
  }

  private static Set myCharOrIntegers;

  public static boolean isCharOrIntegerArray(Value value) {
    if (value == null) return false;
    if (myCharOrIntegers == null) {
      myCharOrIntegers = new HashSet();
      myCharOrIntegers.add("C");
      myCharOrIntegers.add("B");
      myCharOrIntegers.add("S");
      myCharOrIntegers.add("I");
      myCharOrIntegers.add("J");
    }

    String signature = value.type().signature();
    int i;
    for (i = 0; signature.charAt(i) == '['; i++) ;
    if (i == 0) return false;
    signature = signature.substring(i, signature.length());
    return myCharOrIntegers.contains(signature);
  }

  public static ClassFilter create(Element element) throws InvalidDataException {
    ClassFilter filter = new ClassFilter();
    filter.readExternal(element);
    return filter;
  }

  private static boolean isFiltered(ClassFilter classFilter, String qName) {
    if (!classFilter.isEnabled()) {
      return false;
    }
    try {
      if (classFilter.matches(qName)) {
        return true;
      }
    }
    catch (PatternSyntaxException e) {
      LOG.debug(e);
    }
    return false;
  }

  public static boolean isFiltered(String qName, ClassFilter[] classFilters) {
    if(qName.indexOf('[') != -1) {
      return false; //is array
    }

    for(int i = 0; i < classFilters.length; i++) {
      ClassFilter filter = classFilters[i];
      if(isFiltered(filter, qName)) {
        return true;
      }
    }
    return false;
  }

  public static ClassFilter[] readFilters(List children) throws InvalidDataException {
    if (children == null || children.size() == 0) {
      return ClassFilter.EMPTY_ARRAY;
    }
    List<ClassFilter> classFiltersList = new ArrayList<ClassFilter>(children.size());
    for (Iterator i = children.iterator(); i.hasNext();) {
      final ClassFilter classFilter = new ClassFilter();
      classFilter.readExternal((Element)i.next());
      classFiltersList.add(classFilter);
    }
    return classFiltersList.toArray(new ClassFilter[classFiltersList.size()]);
  }

  public static void writeFilters(Element parentNode, String tagName, ClassFilter[] filters) throws WriteExternalException {
    for (int idx = 0; idx < filters.length; idx++) {
      Element element = new Element(tagName);
      parentNode.addContent(element);
      filters[idx].writeExternal(element);
    }
  }

  public static boolean filterEquals(ClassFilter[] filters1, ClassFilter[] filters2) {
    if (filters1.length != filters2.length) {
      return false;
    }
    Set f1 = new HashSet();
    Set f2 = new HashSet();
    for (int idx = 0; idx < filters1.length; idx++) {
      f1.add(filters1[idx]);
    }
    for (int idx = 0; idx < filters2.length; idx++) {
      f2.add(filters2[idx]);
    }
    return f2.equals(f1);
  }

  private static boolean elementListsEqual(List<Element> l1, List<Element> l2) {
    if(l1 == null) return l2 == null;
    if(l2 == null) return false;

    if(l1.size() != l2.size()) return false;

    Iterator<Element> i1 = l1.iterator();
    Iterator<Element> i2 = l2.iterator();

    while (i2.hasNext()) {
      Element elem1 = i1.next();
      Element elem2 = i2.next();

      if(!elementsEqual(elem1, elem2)) return false;
    }
    return true;
  }

  private static boolean attributeListsEqual(List<Attribute> l1, List<Attribute> l2) {
    if(l1 == null) return l2 == null;
    if(l2 == null) return false;

    if(l1.size() != l2.size()) return false;

    Iterator<Attribute> i1 = l1.iterator();
    Iterator<Attribute> i2 = l2.iterator();

    while (i2.hasNext()) {
      Attribute attr1 = i1.next();
      Attribute attr2 = i2.next();

      if(!Comparing.equal(attr1.getName() , attr2.getName()) ||
         !Comparing.equal(attr1.getValue(), attr2.getValue())) return false;
    }
    return true;
  }

  private static boolean elementsEqual(Element e1, Element e2) {
    if(e1 == null) return e2 == null;
    return Comparing.equal(e1.getName(),        e2.getName()) &&
           elementListsEqual  ((List<Element>  )e1.getChildren  (), (List<Element>  )e2.getChildren  ()) &&
           attributeListsEqual((List<Attribute>)e1.getAttributes(), (List<Attribute>)e2.getAttributes());
  }

  public static boolean externalizableEqual(JDOMExternalizable  e1, JDOMExternalizable e2) {
    Element root1 = new Element("root");
    Element root2 = new Element("root");
    try {
      e1.writeExternal(root1);
    }
    catch (WriteExternalException e) {
      LOG.debug(e);
    }
    try {
      e2.writeExternal(root2);
    }
    catch (WriteExternalException e) {
      LOG.debug(e);
    }

    return elementsEqual(root1, root2);
  }

  public static List<Pair<Breakpoint, Event>> getEventDescriptors(SuspendContextImpl suspendContext) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    if(suspendContext == null || suspendContext.getEventSet() == null) return Collections.EMPTY_LIST;
    final List<Pair<Breakpoint, Event>> eventDescriptors = new ArrayList<Pair<Breakpoint, Event>>();

    for (Iterator iterator = suspendContext.getEventSet().iterator(); iterator.hasNext();) {
      Event event = (Event)iterator.next();
      Requestor requestor = suspendContext.getDebugProcess().getRequestsManager().findRequestor(event.request());
      if(requestor instanceof Breakpoint) {
        eventDescriptors.add(new Pair<Breakpoint, Event>((Breakpoint)requestor, event));
      }
    }
    return eventDescriptors;
  }

  private static PsiElement findExpression(PsiElement element) {
    if (!(element instanceof PsiIdentifier || element instanceof PsiKeyword)) {
      return null;
    }
    PsiElement parent = element.getParent();
    if (parent instanceof PsiVariable) {
      return element;
    }
    if (parent instanceof PsiReferenceExpression) {
      if (parent.getParent() instanceof PsiCallExpression) return parent.getParent();
      return parent;
    }
    if (parent instanceof PsiThisExpression) {
      return parent;
    }
    return null;
  }

  public static TextWithImports getEditorText(final Editor editor) {
    if(editor == null) return null;
    final Project         project = editor.getProject();

    String defaultExpression = editor.getSelectionModel().getSelectedText();
    if (defaultExpression == null) {
      int offset = editor.getCaretModel().getOffset();
      PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      if (psiFile != null) {
        PsiElement elementAtCursor = psiFile.findElementAt(offset);
        if (elementAtCursor != null) {
          PsiElement element = findExpression(elementAtCursor);
          if (element != null) {
            defaultExpression = element.getText();
          }
        }
      }
    }

    if(defaultExpression != null) {
      return EvaluationManager.getInstance().createExpressionFragment(defaultExpression);
    } else {
      return null;
    }
  }

  public abstract DebuggerTreeNode  getSelectedNode    (DataContext context);

  public abstract EvaluatorBuilder  getEvaluatorBuilder();

  public abstract CompletionEditor createEditor(Project project, PsiElement context, String recentsId);

  public static TransportService getTransportService(boolean forceSocketTransport) throws ExecutionException {
    TransportService transport = null;
    try {
      try {
        if (forceSocketTransport) {
          transport = createTransport(Class.forName("com.sun.tools.jdi.SocketTransport"));
        }
        else {
          transport = createTransport(Class.forName("com.sun.tools.jdi.SharedMemoryTransport"));
        }
      }
      catch (UnsatisfiedLinkError e) {
        transport = createTransport(Class.forName("com.sun.tools.jdi.SocketTransport"));
      }
    }
    catch (Exception e) {
      throw new ExecutionException(e.getClass().getName() + " : " + e.getMessage());
    }
    return transport;
  }

  private static TransportService createTransport(final Class aClass) throws NoSuchMethodException,
                                                                             InstantiationException,
                                                                             IllegalAccessException,
                                                                             InvocationTargetException {
    final Constructor constructor = aClass.getDeclaredConstructor(new Class[0]);
    constructor.setAccessible(true);
    return (TransportService)constructor.newInstance(ArrayUtil.EMPTY_OBJECT_ARRAY);
  }

  private static class SigReader {
    final String buffer;
    int pos = 0;

    SigReader(String s) {
      buffer = s;
    }

    int get() {
      return buffer.charAt(pos++);
    }

    int peek() {
      return buffer.charAt(pos);
    }

    boolean eof() {
      return buffer.length() <= pos;
    }

    String getSignature() {
      if (eof()) return "";

      switch (get()) {
        case 'Z':
          return "boolean";
        case 'B':
          return "byte";
        case 'C':
          return "char";
        case 'S':
          return "short";
        case 'I':
          return "int";
        case 'J':
          return "long";
        case 'F':
          return "float";
        case 'D':
          return "double";
        case 'V':
          return "void";
        case 'L':
          int start = pos;
          pos = buffer.indexOf(';', start) + 1;
          LOG.assertTrue(pos > 0);
          return buffer.substring(start, pos - 1).replace('/', '.');
        case '[':
          return getSignature() + "[]";
        case '(':
          StringBuffer result = new StringBuffer("(");
          String separator = "";
          while (peek() != ')') {
            result.append(separator);
            result.append(getSignature());
            separator = ", ";
          }
          get();
          result.append(")");
          return getSignature() + " " + getClassName() + "." + getMethodName() + " " + result;
        default:
//          LOG.assertTrue(false, "unknown signature " + buffer);
          return null;
      }
    }

    String getMethodName() {
      return "";
    }

    String getClassName() {
      return "";
    }
  }

  public static String methodName(final Method m) {
    return methodName(signatureToName(m.declaringType().signature()), m.name(), m.signature());
  }

  public static String methodName(final String className, final String methodName, final String signature) {
    try {
      return new SigReader(signature) {
        String getMethodName() {
          return methodName;
        }

        String getClassName() {
          return className;
        }
      }.getSignature();
    }
    catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Internal error : unknown signature" + signature);
      }
      return className + "." + methodName;
    }
  }

  public static String signatureToName(String s) {
    return new SigReader(s).getSignature();
  }

  public static Value createValue(VirtualMachineProxyImpl vm, String expectedType, double value) {
    if (PsiType.DOUBLE.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf(value);
    }
    if (PsiType.FLOAT.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((float)value);
    }
    return createValue(vm, expectedType, (long)value);
  }

  public static Value createValue(VirtualMachineProxyImpl vm, String expectedType, long value) {
    if (PsiType.LONG.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf(value);
    }
    if (PsiType.INT.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((int)value);
    }
    if (PsiType.SHORT.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((short)value);
    }
    if (PsiType.BYTE.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((byte)value);
    }
    if (PsiType.CHAR.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((char)value);
    }
    return null;
  }

  public static Value createValue(VirtualMachineProxyImpl vm, String expectedType, boolean value) {
    if (PsiType.BOOLEAN.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf(value);
    }
    return null;
  }

  public static Value createValue(VirtualMachineProxyImpl vm, String expectedType, char value) {
    if (PsiType.CHAR.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf(value);
    }
    if (PsiType.LONG.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((long)value);
    }
    if (PsiType.INT.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((int)value);
    }
    if (PsiType.SHORT.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((short)value);
    }
    if (PsiType.BYTE.getPresentableText().equals(expectedType)) {
      return vm.mirrorOf((byte)value);
    }
    return null;
  }

  public static Value createValue(VirtualMachineProxyImpl vm, String value) {
    return vm.mirrorOf(value);
  }

  public static String truncateString(final String str) {
    if (str.length() > MAX_LABEL_SIZE) {
      return str.substring(0, MAX_LABEL_SIZE) + "...";
    }
    return str;
  }

  public static String getThreadStatusText(int statusId) {
    switch (statusId) {
      case ThreadReference.THREAD_STATUS_MONITOR:
        return "MONITOR";
      case ThreadReference.THREAD_STATUS_NOT_STARTED:
        return "NOT_STARTED";
      case ThreadReference.THREAD_STATUS_RUNNING:
        return "RUNNING";
      case ThreadReference.THREAD_STATUS_SLEEPING:
        return "SLEEPING";
      case ThreadReference.THREAD_STATUS_UNKNOWN:
        return "UNKNOWN";
      case ThreadReference.THREAD_STATUS_WAIT:
        return "WAIT";
      case ThreadReference.THREAD_STATUS_ZOMBIE:
        return "ZOMBIE";
      default:
        return "UNDEFINED";
    }
  }

  //ToDo:[lex] find common implementation
  public static void findAllSupertypes(final Type type, final Collection typeNames) {
    if (type instanceof ClassType) {
      ClassType classType = (ClassType)type;
      ClassType superclassType = classType.superclass();
      if (superclassType != null) {
        typeNames.add(superclassType);
        findAllSupertypes(superclassType, typeNames);
      }
      List ifaces = classType.allInterfaces();
      for (Iterator it = ifaces.iterator(); it.hasNext();) {
        InterfaceType iface = (InterfaceType)it.next();
        typeNames.add(iface);
        findAllSupertypes(iface, typeNames);
      }
    }
    if (type instanceof InterfaceType) {
      List ifaces = ((InterfaceType)type).superinterfaces();
      for (Iterator it = ifaces.iterator(); it.hasNext();) {
        InterfaceType iface = (InterfaceType)it.next();
        typeNames.add(iface);
        findAllSupertypes(iface, typeNames);
      }
    }
  }

  public static interface ElementVisitor {
    boolean acceptElement(PsiElement element);
  }

  public static void iterateLine(Project project, Document document, int line, ElementVisitor visitor) {
    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
    int lineStart;
    int lineEnd;

    try {
      lineStart = document.getLineStartOffset(line);
      lineEnd = document.getLineEndOffset(line);
    }
    catch (IndexOutOfBoundsException e) {
      return;
    }

    PsiElement element;

    for (int off = lineStart; off < lineEnd;) {
      element = file.findElementAt(off);
      if (element != null) {
        if (visitor.acceptElement(element)) {
          return;
        }
        else {
          off = element.getTextRange().getEndOffset() + 1;
        }
      }
      else {
        off++;
      }
    }
  }

  public static String getQualifiedClassName(final String jdiName, final Project project) {
    final String name= ApplicationManager.getApplication().runReadAction(new Computable<String>() {
      public String compute() {
        String name = jdiName;
        int startFrom = 0;
        final PsiManager psiManager = PsiManager.getInstance(project);
        while (true) {
          final int separator = name.indexOf('$', startFrom);
          if(separator < 0) {
            break;
          }
          final String qualifiedName = name.substring(0, separator);
          final PsiClass psiClass = psiManager.findClass(qualifiedName, GlobalSearchScope.allScope(project));
          if(psiClass != null) {
            int tail = separator + 1;
            while(tail < name.length() && Character.isDigit(name.charAt(tail))) tail ++;
            name = qualifiedName + "." + name.substring(tail);
          }
          startFrom = separator + 1;
        }
        return name;
      }
    });

    if(jdiName.equals(name)) {
      return jdiName.replace('$', '.');
    }

    return name;
  }
}