/*
 * Class ExceptionBreakpoint
 * @author Jeka
 */
package com.intellij.debugger.ui.breakpoints;

import com.intellij.debugger.DebuggerBundle;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.ExceptionRequest;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

public class ExceptionBreakpoint extends Breakpoint {
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.ui.breakpoints.ExceptionBreakpoint");

  public boolean NOTIFY_CAUGHT   = true;
  public boolean NOTIFY_UNCAUGHT = true;
  private String myQualifiedName;
  private String myPackageName;

  public static Icon ICON = IconLoader.getIcon("/debugger/db_exception_breakpoint.png");
  public static Icon DISABLED_ICON = IconLoader.getIcon("/debugger/db_disabled_exception_breakpoint.png");
  public static Icon DISABLED_DEP_ICON = IconLoader.getIcon("/debugger/db_dep_exception_breakpoint.png");
  protected final static String READ_NO_CLASS_NAME = DebuggerBundle.message("error.absent.exception.breakpoint.class.name");
  public static final @NonNls String CATEGORY = "exception_breakpoints";

  public ExceptionBreakpoint(Project project) {
    super(project);
  }

  public String getCategory() {
    return CATEGORY;
  }

  protected ExceptionBreakpoint(Project project, String qualifiedName, String packageName) {
    super(project);
    myQualifiedName = qualifiedName;
    if (packageName == null) {
      myPackageName = calcPackageName(qualifiedName);
    }
    else {
      myPackageName = packageName;
    }
  }

  private String calcPackageName(String qualifiedName) {
    if (qualifiedName == null) {
      return null;
    }
    int dotIndex = qualifiedName.lastIndexOf('.');
    return dotIndex >= 0? qualifiedName.substring(0, dotIndex) : "";
  }

  public String getClassName() {
    return myQualifiedName;
  }

  public String getPackageName() {
    return myPackageName;
  }

  public PsiClass getPsiClass() {
    return PsiDocumentManager.getInstance(myProject).commitAndRunReadAction(new Computable<PsiClass>() {
      public PsiClass compute() {
        return myQualifiedName != null ? DebuggerUtilsEx.findClass(myQualifiedName, myProject, GlobalSearchScope.allScope(myProject)) : null;
      }
    });
  }

  public String getDisplayName() {
    return DebuggerBundle.message("breakpoint.exception.breakpoint.display.name", myQualifiedName);
  }

  public Icon getIcon() {
    if (!ENABLED) {
      final Breakpoint master = DebuggerManagerEx.getInstanceEx(myProject).getBreakpointManager().findMasterBreakpoint(this);
      return master == null? DISABLED_ICON : DISABLED_DEP_ICON;
    }
    return ICON;
  }

  public void reload() {
  }

  public void createRequest(final DebugProcessImpl debugProcess) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    if (!ENABLED || !debugProcess.isAttached() || debugProcess.areBreakpointsMuted() || !debugProcess.getRequestsManager().findRequests(this).isEmpty()) {
      return;
    }

    SourcePosition classPosition = PsiDocumentManager.getInstance(myProject).commitAndRunReadAction(new Computable<SourcePosition>() {
      public SourcePosition compute() {
        PsiClass psiClass = DebuggerUtilsEx.findClass(myQualifiedName, myProject, debugProcess.getSession().getSearchScope());

        return psiClass != null ? SourcePosition.createFromElement(psiClass) : null;
      }
    });

    if(classPosition == null) {
      createOrWaitPrepare(debugProcess, myQualifiedName);
    }
    else {
      createOrWaitPrepare(debugProcess, classPosition);
    }
  }

  public void processClassPrepare(DebugProcess process, ReferenceType refType) {
    DebugProcessImpl debugProcess = (DebugProcessImpl)process;
    if (!ENABLED) {
      return;
    }
    // trying to create a request
    ExceptionRequest request = debugProcess.getRequestsManager().createExceptionRequest(this, refType, NOTIFY_CAUGHT, NOTIFY_UNCAUGHT);
    debugProcess.getRequestsManager().enableRequest(request);
    if (LOG.isDebugEnabled()) {
      if (refType != null) {
        LOG.debug("Created exception request for reference type " + refType.name());
      }
      else {
        LOG.debug("Created exception request for reference type null");
      }
    }
  }

  protected ObjectReference getThisObject(SuspendContextImpl context, LocatableEvent event) throws EvaluateException {
    if(event instanceof ExceptionEvent) {
      return ((ExceptionEvent) event).exception();
    }
    return super.getThisObject(context, event);    //To change body of overriden methods use Options | File Templates.
  }

  public String getEventMessage(LocatableEvent event) {
    String exceptionName = (myQualifiedName != null)? myQualifiedName : "java.lang.Throwable";
    String threadName    = null;
    if (event instanceof ExceptionEvent) {
      ExceptionEvent exceptionEvent = (ExceptionEvent)event;
      try {
        exceptionName = exceptionEvent.exception().type().name();
        threadName = exceptionEvent.thread().name();
      }
      catch (Exception e) {
      }
    }

    if (threadName != null) {
      return DebuggerBundle.message("exception.breakpoint.console.message.with.thread.info", exceptionName, threadName);
    }
    else {
      return DebuggerBundle.message("exception.breakpoint.console.message", exceptionName);
    }
  }

  public boolean isValid() {
    return true;
  }

  @SuppressWarnings({"HardCodedStringLiteral"}) public void writeExternal(Element parentNode) throws WriteExternalException {
    super.writeExternal(parentNode);
    if(myQualifiedName != null) {
      parentNode.setAttribute("class_name", myQualifiedName);
    }
    if(myPackageName != null) {
      parentNode.setAttribute("package_name", myPackageName);
    }
  }

  public PsiElement getEvaluationElement() {
    if (getClassName() == null) {
      return null;
    }
    return PsiManager.getInstance(myProject).findClass(getClassName(), GlobalSearchScope.allScope(myProject));
  }

  public void readExternal(Element parentNode) throws InvalidDataException {
    super.readExternal(parentNode);
    //noinspection HardCodedStringLiteral
    String className = parentNode.getAttributeValue("class_name");
    myQualifiedName = className;
    if(className == null) {
      throw new InvalidDataException(READ_NO_CLASS_NAME);
    }

    //noinspection HardCodedStringLiteral
    String packageName = parentNode.getAttributeValue("package_name");
    myPackageName = packageName != null? packageName : calcPackageName(packageName);
  }

}