package com.intellij.debugger.engine.requests;

import com.intellij.debugger.DebuggerBundle;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.*;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.debugger.requests.ClassPrepareRequestor;
import com.intellij.debugger.requests.RequestManager;
import com.intellij.debugger.requests.Requestor;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.debugger.ui.breakpoints.Breakpoint;
import com.intellij.debugger.ui.breakpoints.BreakpointManager;
import com.intellij.debugger.ui.breakpoints.FilteredRequestor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.ui.classFilter.ClassFilter;
import com.intellij.util.containers.HashMap;
import com.sun.jdi.*;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author lex
 * Date: May 6, 2003
 * Time: 5:32:38 PM
 */
public class RequestManagerImpl extends DebugProcessAdapterImpl implements RequestManager {
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.RequestManagerImpl");

  private static final Key CLASS_NAME = Key.create("ClassName");
  private static final Key<Requestor> REQUESTOR = Key.create("Requestor");

  private DebugProcessImpl myDebugProcess;
  private HashMap<Requestor, String> myInvalidRequestors = new HashMap<Requestor, String>();

  private Map<Requestor, Set<EventRequest>> myRequestorToBelongedRequests = new HashMap<Requestor, Set<EventRequest>>();
  private EventRequestManager myEventRequestManager;
  private @Nullable ThreadReference myFilterThread;

  public RequestManagerImpl(DebugProcessImpl debugProcess) {
    myDebugProcess = debugProcess;
    myDebugProcess.addDebugProcessListener(this);
  }


  public EventRequestManager getVMRequestManager() {
    return myEventRequestManager;
  }

  @Nullable
  public ThreadReference getFilterThread() {
    return myFilterThread;
  }

  public void setFilterThread(@Nullable final ThreadReference filterThread) {
    myFilterThread = filterThread;
  }

  public Set findRequests(Requestor requestor) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    if (!myRequestorToBelongedRequests.containsKey(requestor)) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(myRequestorToBelongedRequests.get(requestor));
  }

  public Requestor findRequestor(EventRequest request) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    return request != null? (Requestor)request.getProperty(REQUESTOR) : null;
  }

  private static void addClassFilter(EventRequest request, String pattern){
    if(request instanceof AccessWatchpointRequest){
      ((AccessWatchpointRequest) request).addClassFilter(pattern);
    }
    else if(request instanceof ExceptionRequest){
      ((ExceptionRequest) request).addClassFilter(pattern);
    }
    else if(request instanceof MethodEntryRequest) {
      ((MethodEntryRequest)request).addClassFilter(pattern);
    }
    else if(request instanceof MethodExitRequest) {
      ((MethodExitRequest)request).addClassFilter(pattern);
    }
    else if(request instanceof ModificationWatchpointRequest) {
      ((ModificationWatchpointRequest)request).addClassFilter(pattern);
    }
    else if(request instanceof WatchpointRequest) {
      ((WatchpointRequest)request).addClassFilter(pattern);
    }
  }

  private static void addClassExclusionFilter(EventRequest request, String pattern){
    if(request instanceof AccessWatchpointRequest){
      ((AccessWatchpointRequest) request).addClassExclusionFilter(pattern);
    }
    else if(request instanceof ExceptionRequest){
      ((ExceptionRequest) request).addClassExclusionFilter(pattern);
    }
    else if(request instanceof MethodEntryRequest) {
      ((MethodEntryRequest)request).addClassExclusionFilter(pattern);
    }
    else if(request instanceof MethodExitRequest) {
      ((MethodExitRequest)request).addClassExclusionFilter(pattern);
    }
    else if(request instanceof ModificationWatchpointRequest) {
      ((ModificationWatchpointRequest)request).addClassExclusionFilter(pattern);
    }
    else if(request instanceof WatchpointRequest) {
      ((WatchpointRequest)request).addClassExclusionFilter(pattern);
    }
  }

  private void addLocatableRequest(FilteredRequestor requestor, EventRequest request) {
    if(DebuggerSettings.SUSPEND_ALL.equals(requestor.SUSPEND_POLICY)) {
      request.setSuspendPolicy(EventRequest.SUSPEND_ALL);
    }
    else {
      //when requestor.SUSPEND_POLICY == SUSPEND_NONE
      //we should pause thread in order to evaluate conditions
      request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
    }

    if (requestor.COUNT_FILTER_ENABLED) {
      request.addCountFilter(requestor.COUNT_FILTER);
    }

    if (requestor.CLASS_FILTERS_ENABLED) {
      ClassFilter[] classFilters = requestor.getClassFilters();
      for (final ClassFilter filter : classFilters) {
        if (!filter.isEnabled()) {
          continue;
        }
        final JVMName jvmClassName = ApplicationManager.getApplication().runReadAction(new Computable<JVMName>() {
          public JVMName compute() {
            PsiClass psiClass =
              DebuggerUtilsEx.findClass(filter.getPattern(), myDebugProcess.getProject(), myDebugProcess.getSession().getSearchScope());
            if (psiClass == null) {
              return null;
            }
            return JVMNameUtil.getJVMQualifiedName(psiClass);
          }
        });
        String pattern = filter.getPattern();
        try {
          if (jvmClassName != null) {
            pattern = jvmClassName.getName(myDebugProcess);
          }
        }
        catch (EvaluateException e) {
        }

        addClassFilter(request, pattern);
      }

      final ClassFilter[] iclassFilters = requestor.getClassExclusionFilters();
      for (ClassFilter filter : iclassFilters) {
        if (filter.isEnabled()) {
          addClassExclusionFilter(request, filter.getPattern());
        }
      }
    }

    registerRequestInternal(requestor, request);
  }

  public void registerRequestInternal(final Requestor requestor, final EventRequest request) {
    registerRequest(requestor, request);
    request.putProperty(REQUESTOR, requestor);
  }

  private void registerRequest(Requestor requestor, EventRequest request) {
    Set<EventRequest> reqSet = myRequestorToBelongedRequests.get(requestor);
    if(reqSet == null) {
      reqSet = new HashSet<EventRequest>();
      myRequestorToBelongedRequests.put(requestor, reqSet);
    }
    reqSet.add(request);

  }

  // requests creation
  public ClassPrepareRequest createClassPrepareRequest(ClassPrepareRequestor requestor, String pattern) {
    ClassPrepareRequest classPrepareRequest = myEventRequestManager.createClassPrepareRequest();
    classPrepareRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
    classPrepareRequest.addClassFilter(pattern);
    classPrepareRequest.putProperty(CLASS_NAME, pattern);

    registerRequestInternal(requestor, classPrepareRequest);
    return classPrepareRequest;
  }

  public ExceptionRequest createExceptionRequest(FilteredRequestor requestor, ReferenceType referenceType, boolean notifyCaught, boolean notifyUnCaught) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    ExceptionRequest req = myEventRequestManager.createExceptionRequest(referenceType, notifyCaught, notifyUnCaught);
    addLocatableRequest(requestor, req);
    return req;
  }

  public MethodEntryRequest createMethodEntryRequest(FilteredRequestor requestor) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    MethodEntryRequest req = myEventRequestManager.createMethodEntryRequest();
    addLocatableRequest(requestor, req);
    return req;
  }

  public MethodExitRequest createMethodExitRequest(FilteredRequestor requestor) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    MethodExitRequest req = myEventRequestManager.createMethodExitRequest();
    addLocatableRequest(requestor, req);
    return req;
  }

  public BreakpointRequest createBreakpointRequest(FilteredRequestor requestor, Location location) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    BreakpointRequest req = myEventRequestManager.createBreakpointRequest(location);
    addLocatableRequest(requestor, req);
    return req;
  }

  public AccessWatchpointRequest createAccessWatchpointRequest(FilteredRequestor requestor, Field field) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    AccessWatchpointRequest req = myEventRequestManager.createAccessWatchpointRequest(field);
    addLocatableRequest(requestor, req);
    return req;
  }

  public ModificationWatchpointRequest createModificationWatchpointRequest(FilteredRequestor requestor, Field field) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    ModificationWatchpointRequest req = myEventRequestManager.createModificationWatchpointRequest(field);
    addLocatableRequest(requestor, req);
    return req;
  }

  public void deleteRequest(Requestor requestor) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    if(!myDebugProcess.isAttached()) {
      return;
    }
    Set<EventRequest> requests = myRequestorToBelongedRequests.get(requestor);
    if(requests == null) {
      return;
    }
    myRequestorToBelongedRequests.remove(requestor);
    for (final EventRequest request : requests) {
      try {
        myEventRequestManager.deleteEventRequest(request);
      }
      catch (InternalException e) {
        if (e.errorCode() == 41) {
          //event request not found
          //there could be no requests after hotswap
        }
        else {
          LOG.error(e);
        }
      }
    }
  }

  public void callbackOnPrepareClasses(final ClassPrepareRequestor requestor, final SourcePosition classPosition) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    ClassPrepareRequest prepareRequest = myDebugProcess.getPositionManager().createPrepareRequest(requestor, classPosition);

    if(prepareRequest == null) {
      setInvalid(requestor, DebuggerBundle.message("status.invalid.breakpoint.out.of.class"));
      return;
    }

    registerRequest(requestor, prepareRequest);
    prepareRequest.enable();
  }

  public void callbackOnPrepareClasses(ClassPrepareRequestor requestor, String classOrPatternToBeLoaded) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    ClassPrepareRequest classPrepareRequest = createClassPrepareRequest(requestor, classOrPatternToBeLoaded);

    registerRequest(requestor, classPrepareRequest);
    classPrepareRequest.enable();
    if (LOG.isDebugEnabled()) {
      LOG.debug("classOrPatternToBeLoaded = " + classOrPatternToBeLoaded);
    }
  }

  public void enableRequest(EventRequest request) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    LOG.assertTrue(findRequestor(request) != null);
    try {
      final ThreadReference filterThread = myFilterThread;
      if (filterThread != null) {
        if (request instanceof BreakpointRequest) {
          ((BreakpointRequest)request).addThreadFilter(filterThread);
        }
        else if (request instanceof MethodEntryRequest) {
          ((MethodEntryRequest)request).addThreadFilter(filterThread);
        }
        else if (request instanceof MethodExitRequest) {
          ((MethodExitRequest)request).addThreadFilter(filterThread);
        }
      }
      request.enable();
    } catch (InternalException e) {
      if(e.errorCode() == 41) {
        //event request not found
        //there could be no requests after hotswap
      } else {
        LOG.error(e);
      }
    }
  }

  public void setInvalid(Requestor requestor, String message) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    myInvalidRequestors.put(requestor, message);
  }

  public boolean isInvalid(Requestor requestor) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    return myInvalidRequestors.containsKey(requestor);
  }

  public String getInvalidMessage(Requestor requestor) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    return myInvalidRequestors.get(requestor);
  }

  public boolean isVerified(Requestor requestor) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    //ClassPrepareRequest is added in any case
    return findRequests(requestor).size() > 1;
  }

  public void processDetached(DebugProcessImpl process, boolean closedByUser) {
    DebuggerManagerThreadImpl.assertIsManagerThread();
    myEventRequestManager = null;
    myInvalidRequestors.clear();
    myRequestorToBelongedRequests.clear();
  }

  public void processAttached(DebugProcessImpl process) {
    myEventRequestManager = myDebugProcess.getVirtualMachineProxy().eventRequestManager();
    // invoke later, so that requests are for sure created only _after_ 'processAttached()' methods of other listeneres are executed
    process.getManagerThread().invokeLater(new DebuggerCommandImpl() {
      protected void action() throws Exception {
        final BreakpointManager breakpointManager = DebuggerManagerEx.getInstanceEx(myDebugProcess.getProject()).getBreakpointManager();
        for (final Breakpoint breakpoint : breakpointManager.getBreakpoints()) {
          breakpoint.createRequest(myDebugProcess);
        }
      }
    });
  }

  public void processClassPrepared(final ClassPrepareEvent event) {
    if (!myDebugProcess.isAttached()) {
      return;
    }

    final ReferenceType refType = event.referenceType();

    if (refType instanceof ClassType) {
      ClassType classType = (ClassType)refType;
      if (LOG.isDebugEnabled()) {
        LOG.debug("signature = " + classType.signature());
      }
      ClassPrepareRequestor requestor = (ClassPrepareRequestor)event.request().getProperty(REQUESTOR);
      if (requestor != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("requestor found " + classType.signature());
        }
        requestor .processClassPrepare(myDebugProcess, classType);
      }
    }
  }

  private static interface AllProcessesCommand {
    void action(DebugProcessImpl process);
  }

  private static void invoke(Project project, final AllProcessesCommand command) {
    Collection<DebuggerSession> sessions = (DebuggerManagerEx.getInstanceEx(project)).getSessions();
    for (Iterator<DebuggerSession> iterator = sessions.iterator(); iterator.hasNext();) {
      DebuggerSession debuggerSession = iterator.next();
      final DebugProcessImpl process = debuggerSession.getProcess();
      if(process != null) {
        process.getManagerThread().invoke(new DebuggerCommandImpl() {
          protected void action() throws Exception {
            command.action(process);
          }
        });
      }
    }
  }

  public static void createRequests(final Breakpoint breakpoint) {
    invoke(breakpoint.getProject(), new AllProcessesCommand (){
      public void action(DebugProcessImpl process)  {
        breakpoint.createRequest(process);
      }
    });
  }

  public static void updateRequests(final Breakpoint breakpoint) {
    invoke(breakpoint.getProject(), new AllProcessesCommand (){
      public void action(DebugProcessImpl process)  {
        process.getRequestsManager().myInvalidRequestors.remove(breakpoint);
        process.getRequestsManager().deleteRequest(breakpoint);
        breakpoint.createRequest(process);
      }
    });
  }

  public static void deleteRequests(final Breakpoint breakpoint) {
    invoke(breakpoint.getProject(), new AllProcessesCommand (){
      public void action(DebugProcessImpl process)  {
        process.getRequestsManager().deleteRequest(breakpoint);
      }
    });
  }
}