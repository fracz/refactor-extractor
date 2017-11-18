package com.intellij.xdebugger.impl;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.breakpoints.*;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.impl.breakpoints.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * @author nik
 */
public class XDebugSessionImpl implements XDebugSession {
  private static final Logger LOG = Logger.getInstance("#com.intellij.xdebugger.impl.XDebugSessionImpl");
  private XDebugProcess myDebugProcess;
  private final Map<XBreakpoint<?>, CustomizedBreakpointPresentation> myRegisteredBreakpoints = new HashMap<XBreakpoint<?>, CustomizedBreakpointPresentation>();
  private final Set<XBreakpoint<?>> myDisabledSlaveBreakpoints = new HashSet<XBreakpoint<?>>();
  private boolean myBreakpointsMuted;
  private boolean myBreakpointsDisabled;
  private final XDebuggerManagerImpl myDebuggerManager;
  private MyBreakpointListener myBreakpointListener;
  private XSuspendContext mySuspendContext;
  private XSourcePosition myCurrentPosition;
  private boolean myPaused;
  private MyDependentBreakpointListener myDependentBreakpointListener;

  public XDebugSessionImpl(XDebuggerManagerImpl debuggerManager) {
    myDebuggerManager = debuggerManager;
  }

  @NotNull
  public Project getProject() {
    return myDebuggerManager.getProject();
  }

  @NotNull
  public XDebugProcess getDebugProcess() {
    return myDebugProcess;
  }

  public boolean isSuspended() {
    return myPaused && mySuspendContext != null;
  }

  public boolean isPaused() {
    return myPaused;
  }

  public XSuspendContext getSuspendContext() {
    return mySuspendContext;
  }

  @Nullable
  public XSourcePosition getCurrentPosition() {
    return myCurrentPosition;
  }

  public void init(final XDebugProcess process) {
    LOG.assertTrue(myDebugProcess == null);
    myDebugProcess = process;

    XBreakpointManagerImpl breakpointManager = myDebuggerManager.getBreakpointManager();
    XDependentBreakpointManager dependentBreakpointManager = breakpointManager.getDependentBreakpointManager();
    disableSlaveBreakpoints(dependentBreakpointManager);
    processAllBreakpoints(true, false);

    myBreakpointListener = new MyBreakpointListener();
    breakpointManager.addBreakpointListener(myBreakpointListener);
    myDependentBreakpointListener = new MyDependentBreakpointListener();
    dependentBreakpointManager.addListener(myDependentBreakpointListener);
    process.sessionInitialized();
  }

  private void disableSlaveBreakpoints(final XDependentBreakpointManager dependentBreakpointManager) {
    Set<XBreakpointBase> slaveBreakpoints = dependentBreakpointManager.getAllSlaveBreakpoints();
    Set<XBreakpointType<?,?>> breakpointTypes = new HashSet<XBreakpointType<?,?>>();
    for (XBreakpointHandler<?> handler : myDebugProcess.getBreakpointHandlers()) {
      breakpointTypes.add(getBreakpointTypeClass(handler));
    }
    for (XBreakpointBase slaveBreakpoint : slaveBreakpoints) {
      if (breakpointTypes.contains(slaveBreakpoint.getType())) {
        myDisabledSlaveBreakpoints.add(slaveBreakpoint);
      }
    }
  }

  private static <B extends XBreakpoint<?>> XBreakpointType<?, ?> getBreakpointTypeClass(final XBreakpointHandler<B> handler) {
    return XDebuggerUtil.getInstance().findBreakpointType(handler.getBreakpointTypeClass());
  }

  private <B extends XBreakpoint<?>> void processBreakpoints(final XBreakpointHandler<B> handler, boolean register, final boolean temporary) {
    XBreakpointType<B,?> type = XDebuggerUtil.getInstance().findBreakpointType(handler.getBreakpointTypeClass());
    Collection<? extends B> breakpoints = myDebuggerManager.getBreakpointManager().getBreakpoints(type);
    for (B b : breakpoints) {
      handleBreakpoint(handler, b, register, temporary);
    }
  }

  private <B extends XBreakpoint<?>> void handleBreakpoint(final XBreakpointHandler<B> handler, final B b, final boolean register,
                                                           final boolean temporary) {
    if (register && isBreakpointActive(b)) {
      synchronized (myRegisteredBreakpoints) {
        myRegisteredBreakpoints.put(b, new CustomizedBreakpointPresentation());
      }
      handler.registerBreakpoint(b);
    }
    if (!register) {
      boolean removed = false;
      synchronized (myRegisteredBreakpoints) {
        if (myRegisteredBreakpoints.containsKey(b)) {
          myRegisteredBreakpoints.remove(b);
          removed = true;
        }
      }
      if (removed) {
        handler.unregisterBreakpoint(b, temporary);
      }
    }
  }

  @Nullable
  public CustomizedBreakpointPresentation getBreakpointPresentation(@NotNull XLineBreakpoint<?> breakpoint) {
    synchronized (myRegisteredBreakpoints) {
      return myRegisteredBreakpoints.get(breakpoint);
    }
  }

  private void processAllHandlers(final XBreakpoint<?> breakpoint, final boolean register) {
    for (XBreakpointHandler<?> handler : myDebugProcess.getBreakpointHandlers()) {
      processBreakpoint(breakpoint, handler, register);
    }
  }

  private <B extends XBreakpoint<?>> void processBreakpoint(final XBreakpoint<?> breakpoint, final XBreakpointHandler<B> handler, boolean register) {
    XBreakpointType<?, ?> type = breakpoint.getType();
    if (handler.getBreakpointTypeClass().equals(type.getClass())) {
      B b = (B)breakpoint;
      handleBreakpoint(handler, b, register, false);
    }
  }

  private boolean isBreakpointActive(final XBreakpoint<?> b) {
    return !myBreakpointsMuted && b.isEnabled() && !myDisabledSlaveBreakpoints.contains(b);
  }

  public boolean areBreakpointsMuted() {
    return myBreakpointsMuted;
  }

  public void setBreakpointMuted(boolean muted) {
    if (myBreakpointsMuted == muted) return;
    myBreakpointsMuted = muted;
    processAllBreakpoints(!muted, muted);
  }

  public void stepOver(final boolean ignoreBreakpoints) {
    if (ignoreBreakpoints) {
      disableBreakpoints();
    }
    doResume();
    myDebugProcess.startStepOver();
  }

  public void stepInto() {
    doResume();
    myDebugProcess.startStepInto();
  }

  public void stepOut() {
    doResume();
    myDebugProcess.startStepOut();
  }

  public void forceStepInto() {
    stepInto();
  }

  public void runToPosition(@NotNull final XSourcePosition position, final boolean ignoreBreakpoints) {
    if (ignoreBreakpoints) {
      disableBreakpoints();
    }
    doResume();
    myDebugProcess.runToPosition(position);
  }

  private void processAllBreakpoints(final boolean register, final boolean temporary) {
    for (XBreakpointHandler<?> handler : myDebugProcess.getBreakpointHandlers()) {
      processBreakpoints(handler, register, temporary);
    }
  }

  private void disableBreakpoints() {
    myBreakpointsDisabled = true;
    processAllBreakpoints(false, true);
  }

  public void resume() {
    doResume();
    myDebugProcess.resume();
  }

  private void doResume() {
    myDebuggerManager.updateExecutionPosition(this, null);
    mySuspendContext = null;
    myCurrentPosition = null;
    myPaused = false;
  }

  public void showExecutionPoint() {
    myDebuggerManager.showExecutionPosition();
  }

  public void updateBreakpointPresentation(@NotNull final XLineBreakpoint<?> breakpoint, @Nullable final Icon icon, @Nullable final String errorMessage) {
    CustomizedBreakpointPresentation presentation;
    synchronized (myRegisteredBreakpoints) {
      presentation = myRegisteredBreakpoints.get(breakpoint);
      if (presentation != null) {
        presentation.setErrorMessage(errorMessage);
        presentation.setIcon(icon);
      }
    }
    if (presentation != null) {
      myDebuggerManager.getBreakpointManager().getLineBreakpointManager().queueBreakpointUpdate((XLineBreakpointImpl<?>)breakpoint);
    }
  }

  public boolean breakpointReached(@NotNull final XBreakpoint<?> breakpoint, @NotNull final XSuspendContext suspendContext) {
    XDebuggerEvaluator evaluator = suspendContext.getEvaluator();
    String condition = breakpoint.getCondition();
    if (condition != null && evaluator != null) {
      LOG.debug("evaluating condition: " + condition);
      boolean result = evaluator.evaluateCondition(condition);
      LOG.debug("condition evaluates to " + result);
      if (!result) {
        return false;
      }
    }

    if (breakpoint.isLogMessage()) {
      String message = XDebuggerBundle.message("xbreakpoint.reached.at.0", XBreakpointUtil.getDisplayText(breakpoint));
      printMessage(message);
    }
    String expression = breakpoint.getLogExpression();
    if (expression != null && evaluator != null) {
      LOG.debug("evaluating log expression: " + expression);
      printMessage(evaluator.evaluateMessage(expression));
    }

    processDependencies(breakpoint);

    if (breakpoint.getSuspendPolicy() == SuspendPolicy.NONE) {
      return false;
    }

    myPaused = true;
    XSourcePosition position = breakpoint.getSourcePosition();
    if (position != null) {
      positionReached(position, suspendContext);
    }
    return true;
  }

  private void processDependencies(final XBreakpoint<?> breakpoint) {
    XDependentBreakpointManager dependentBreakpointManager = myDebuggerManager.getBreakpointManager().getDependentBreakpointManager();
    if (!dependentBreakpointManager.isMasterOrSlave(breakpoint)) return;

    List<XBreakpoint<?>> breakpoints = dependentBreakpointManager.getSlaveBreakpoints(breakpoint);
    myDisabledSlaveBreakpoints.removeAll(breakpoints);
    for (XBreakpoint<?> slaveBreakpoint : breakpoints) {
      processAllHandlers(slaveBreakpoint, true);
    }

    if (dependentBreakpointManager.getMasterBreakpoint(breakpoint) != null && !dependentBreakpointManager.isLeaveEnabled(breakpoint)) {
      boolean added = myDisabledSlaveBreakpoints.add(breakpoint);
      if (added) {
        processAllHandlers(breakpoint, false);
        myDebuggerManager.getBreakpointManager().getLineBreakpointManager().queueBreakpointUpdate(breakpoint);
      }
    }
  }

  private static void printMessage(final String message) {
    //todo[nik]
    LOG.info(message);
  }

  public void positionReached(@NotNull final XSourcePosition position, @NotNull final XSuspendContext suspendContext) {
    enableBreakpoints();
    mySuspendContext = suspendContext;
    myCurrentPosition = position;
    myPaused = true;
    myDebuggerManager.updateExecutionPosition(this, position);
  }

  private void enableBreakpoints() {
    if (myBreakpointsDisabled) {
      myBreakpointsDisabled = false;
      new ReadAction() {
        protected void run(final Result result) {
          processAllBreakpoints(true, false);
        }
      }.execute();
    }
  }

  public void stop() {
    myDebugProcess.stop();
    myDebuggerManager.updateExecutionPosition(this, null);
    XBreakpointManagerImpl breakpointManager = myDebuggerManager.getBreakpointManager();
    breakpointManager.removeBreakpointListener(myBreakpointListener);
    breakpointManager.getDependentBreakpointManager().removeListener(myDependentBreakpointListener);
    myDebuggerManager.removeSession(this);
  }

  public boolean isDisabledSlaveBreakpoint(final XBreakpoint<?> breakpoint) {
    return myDisabledSlaveBreakpoints.contains(breakpoint);
  }

  private class MyBreakpointListener implements XBreakpointListener<XBreakpoint<?>> {
    public void breakpointAdded(@NotNull final XBreakpoint<?> breakpoint) {
      if (!myBreakpointsDisabled) {
        processAllHandlers(breakpoint, true);
      }
    }

    public void breakpointRemoved(@NotNull final XBreakpoint<?> breakpoint) {
      processAllHandlers(breakpoint, false);
    }

    public void breakpointChanged(@NotNull final XBreakpoint<?> breakpoint) {
      breakpointRemoved(breakpoint);
      breakpointAdded(breakpoint);
    }
  }

  private class MyDependentBreakpointListener implements XDependentBreakpointListener {
    public void dependencySet(final XBreakpoint<?> slave, final XBreakpoint<?> master) {
      boolean added = myDisabledSlaveBreakpoints.add(slave);
      if (added) {
        processAllHandlers(slave, false);
      }
    }

    public void dependencyCleared(final XBreakpoint<?> breakpoint) {
      boolean removed = myDisabledSlaveBreakpoints.remove(breakpoint);
      if (removed) {
        processAllHandlers(breakpoint, true);
      }
    }
  }
}