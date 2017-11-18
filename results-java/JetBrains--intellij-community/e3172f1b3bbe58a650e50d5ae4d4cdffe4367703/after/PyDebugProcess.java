package com.jetbrains.python.debugger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.containers.ConcurrentHashSet;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.python.console.pydev.PydevCompletionVariant;
import com.jetbrains.python.debugger.local.PyLocalPositionConverter;
import com.jetbrains.python.debugger.pydev.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.ServerSocket;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * @author yole
 */
// todo: bundle messages
// todo: pydevd supports module reloading - look for a way to use the feature
// todo: smart step into
public class PyDebugProcess extends XDebugProcess implements IPyDebugProcess {

  private final PyPositionConverter myPositionConverter = new PyLocalPositionConverter();
  private final RemoteDebugger myDebugger;
  private final XBreakpointHandler[] myBreakpointHandlers;
  private final PyDebuggerEditorsProvider myEditorsProvider;
  private final ProcessHandler myProcessHandler;
  private final ExecutionConsole myExecutionConsole;
  private final Map<PySourcePosition, XLineBreakpoint> myRegisteredBreakpoints = new ConcurrentHashMap<PySourcePosition, XLineBreakpoint>();
  private final Set<PyExceptionBreakpointProperties> myRegisteredExceptionBreakpoints = new ConcurrentHashSet<PyExceptionBreakpointProperties>();

  private final List<PyThreadInfo> mySuspendedThreads = Lists.newArrayList();
  private final Map<String, List<PyDebugValue>> myStackFrameCache = Maps.newHashMap();
  private final Map<String, PyDebugValue> myNewVariableValue = Maps.newHashMap();

  protected PyDebugProcess(@NotNull XDebugSession session,
                           final ServerSocket serverSocket,
                           final ExecutionConsole executionConsole,
                           final ProcessHandler processHandler) {
    super(session);
    session.setPauseActionSupported(true);
    myDebugger = new RemoteDebugger(this, serverSocket, 10);
    myBreakpointHandlers = new XBreakpointHandler[]{new PyLineBreakpointHandler(this), new PyExceptionBreakpointHandler(this)};
    myEditorsProvider = new PyDebuggerEditorsProvider();
    myProcessHandler = processHandler;
    myExecutionConsole = executionConsole;
  }

  @Override
  public PyPositionConverter getPositionConverter() {
    return myPositionConverter;
  }

  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    return myBreakpointHandlers;
  }

  @Override
  @NotNull
  public XDebuggerEditorsProvider getEditorsProvider() {
    return myEditorsProvider;
  }

  @Override
  @Nullable
  protected ProcessHandler doGetProcessHandler() {
    return myProcessHandler;
  }

  @Override
  @NotNull
  public ExecutionConsole createConsole() {
    return myExecutionConsole;
  }

  @Override
  public void sessionInitialized() {
    super.sessionInitialized();
    ProgressManager.getInstance().run(new Task.Backgroundable(null, "Connecting to debugger", false) {
      public void run(@NotNull final ProgressIndicator indicator) {
        indicator.setText("Connecting to debugger...");
        try {
          myDebugger.waitForConnect();
          handshake();
          registerBreakpoints();
          new RunCommand(myDebugger).execute();
        }
        catch (final Exception e) {
          myProcessHandler.destroyProcess();
          invokeLater(new Runnable() {
            public void run() {
              Messages.showErrorDialog("Unable to establish connection with debugger:\n" + e.getMessage(), "Connecting to debugger");
            }
          });
        }
      }
    });
  }

  private void handshake() throws PyDebuggerException {
    final String remoteVersion = myDebugger.handshake();
    ((ConsoleView)myExecutionConsole).print("Connected to pydevd (version " + remoteVersion + ")\n", ConsoleViewContentType.SYSTEM_OUTPUT);
  }

  private void registerBreakpoints() {
    for (Map.Entry<PySourcePosition, XLineBreakpoint> entry : myRegisteredBreakpoints.entrySet()) {
      addBreakpoint(entry.getKey(), entry.getValue());
    }
    for (PyExceptionBreakpointProperties bp: myRegisteredExceptionBreakpoints) {
      addExceptionBreakpoint(bp);
    }
  }

  @Override
  public void startStepOver() {
    resume(ResumeCommand.Mode.STEP_OVER);
  }

  @Override
  public void startStepInto() {
    resume(ResumeCommand.Mode.STEP_INTO);
  }

  @Override
  public void startStepOut() {
    resume(ResumeCommand.Mode.STEP_OUT);
  }

  @Override
  public void stop() {
    myDebugger.disconnect();
  }

  @Override
  public void resume() {
    resume(ResumeCommand.Mode.RESUME);
  }

  @Override
  public void startPausing() {
    if (myDebugger.isConnected()) {
      myDebugger.suspendAllThreads();
    }
  }

  private void resume(final ResumeCommand.Mode mode) {
    dropFrameCaches();
    if (myDebugger.isConnected()) {
      for (PyThreadInfo suspendedThread : mySuspendedThreads) {
        final ResumeCommand command = new ResumeCommand(myDebugger, suspendedThread.getId(), mode);
        myDebugger.execute(command);
      }
    }
  }

  @Override
  public void runToPosition(@NotNull final XSourcePosition position) {
    dropFrameCaches();
    if (myDebugger.isConnected() && !mySuspendedThreads.isEmpty()) {
      final PySourcePosition pyPosition = myPositionConverter.convert(position);
      final SetBreakpointCommand command = new SetBreakpointCommand(myDebugger, pyPosition.getFile(), pyPosition.getLine(), null);
      myDebugger.execute(command);  // set temp. breakpoint
      resume(ResumeCommand.Mode.RESUME);
    }
  }

  public PyDebugValue evaluate(final String expression, final boolean execute) throws PyDebuggerException {
    dropFrameCaches();
    final PyStackFrame frame = currentFrame();
    return myDebugger.evaluate(frame.getThreadId(), frame.getFrameId(), expression, execute);
  }

  public String consoleExec(String command) throws PyDebuggerException {
    dropFrameCaches();
    final PyStackFrame frame = currentFrame();
    return myDebugger.consoleExec(frame.getThreadId(), frame.getFrameId(), command);
  }

  public List<PyDebugValue> loadFrame() throws PyDebuggerException {
    final PyStackFrame frame = currentFrame();
    //do not reload frame every time it is needed, because due to bug in pdb, reloading frame clears all variable changes
    if (!myStackFrameCache.containsKey(frame.getThreadFrameId())) {
      List<PyDebugValue> values = myDebugger.loadFrame(frame.getThreadId(), frame.getFrameId());
      myStackFrameCache.put(frame.getThreadFrameId(), values);
    }
    return applyNewValue(myStackFrameCache.get(frame.getThreadFrameId()), frame.getThreadFrameId());
  }

  private List<PyDebugValue> applyNewValue(List<PyDebugValue> pyDebugValues, String threadFrameId) {
    if (myNewVariableValue.containsKey(threadFrameId)) {
      PyDebugValue newValue = myNewVariableValue.get(threadFrameId);
      List<PyDebugValue> res = Lists.newArrayList();
      for (PyDebugValue val : pyDebugValues) {
        if (val.getName().equals(newValue.getName())) {
          res.add(newValue);
        }
        else {
          res.add(val);
        }
      }
      return res;
    }
    else {
      return pyDebugValues;
    }
  }

  @Override
  public List<PyDebugValue> loadVariable(final PyDebugValue var) throws PyDebuggerException {
    final PyStackFrame frame = currentFrame();
    return myDebugger.loadVariable(frame.getThreadId(), frame.getFrameId(), var);
  }

  @Override
  public void changeVariable(final PyDebugValue var, final String value) throws PyDebuggerException {
    final PyStackFrame frame = currentFrame();
    PyDebugValue newValue = myDebugger.changeVariable(frame.getThreadId(), frame.getFrameId(), var, value);
    myNewVariableValue.put(frame.getThreadFrameId(), newValue);
  }

  @Override
  public boolean isVariable(String name) {
    final Project project = getSession().getProject();
    return PyDebugSupportUtils.isVariable(project, name);
  }

  private PyStackFrame currentFrame() throws PyDebuggerException {
    if (!myDebugger.isConnected()) {
      throw new PyDebuggerException("Disconnected");
    }

    final PyStackFrame frame = (PyStackFrame)getSession().getCurrentStackFrame();
    if (frame == null) {
      throw new PyDebuggerException("Process is running");
    }

    return frame;
  }

  public void addBreakpoint(final PySourcePosition position, final XLineBreakpoint breakpoint) {
    myRegisteredBreakpoints.put(position, breakpoint);
    if (myDebugger.isConnected()) {
      final SetBreakpointCommand command =
        new SetBreakpointCommand(myDebugger, position.getFile(), position.getLine(), breakpoint.getCondition());
      myDebugger.execute(command);
    }
  }

  public void removeBreakpoint(final PySourcePosition position) {
    myRegisteredBreakpoints.remove(position);
    if (myDebugger.isConnected()) {
      final RemoveBreakpointCommand command = new RemoveBreakpointCommand(myDebugger, position.getFile(), position.getLine());
      myDebugger.execute(command);
    }
  }

  public void addExceptionBreakpoint(PyExceptionBreakpointProperties properties) {
    myRegisteredExceptionBreakpoints.add(properties);
    if (myDebugger.isConnected()) {
      final ExceptionBreakpointCommand command =
        ExceptionBreakpointCommand.addExceptionBreakpointCommand(myDebugger, properties.getException());
      myDebugger.execute(command);
    }
  }

  public void removeExceptionBreakpoint(PyExceptionBreakpointProperties properties) {
    myRegisteredExceptionBreakpoints.remove(properties);
    if (myDebugger.isConnected()) {
      final ExceptionBreakpointCommand command =
        ExceptionBreakpointCommand.removeExceptionBreakpointCommand(myDebugger, properties.getException());
      myDebugger.execute(command);
    }
  }

  public Collection<PyThreadInfo> getThreads() {
    return myDebugger.getThreads();
  }

  @Override
  public void threadSuspended(final PyThreadInfo threadInfo) {
    if (!mySuspendedThreads.contains(threadInfo)) {
      mySuspendedThreads.add(threadInfo);

      final List<PyStackFrameInfo> frames = threadInfo.getFrames();
      if (frames != null) {
        final PySuspendContext suspendContext = new PySuspendContext(this, threadInfo);

        XLineBreakpoint breakpoint = null;
        if (threadInfo.isStopOnBreakpoint()) {
          final PySourcePosition position = frames.get(0).getPosition();
          breakpoint = myRegisteredBreakpoints.get(position);
          if (breakpoint == null) {
            final RemoveBreakpointCommand command = new RemoveBreakpointCommand(myDebugger, position.getFile(), position.getLine());
            myDebugger.execute(command);  // remove temp. breakpoint
          }
        }

        if (breakpoint != null) {
          if (!getSession().breakpointReached(breakpoint, suspendContext)) {
            resume();
          }
        }
        else {
          getSession().positionReached(suspendContext);
        }
      }
    }
  }

  @Override
  public void threadResumed(final PyThreadInfo threadInfo) {
    mySuspendedThreads.remove(threadInfo);
  }

  private void dropFrameCaches() {
    myStackFrameCache.clear();
    myNewVariableValue.clear();
  }

  @Nullable
  public List<PydevCompletionVariant> getCompletions(String prefix) throws Exception {
    if (myDebugger.isConnected()) {
      dropFrameCaches();
      final PyStackFrame frame = currentFrame();
      final GetCompletionsCommand command = new GetCompletionsCommand(myDebugger, frame.getThreadId(), frame.getFrameId(), prefix);
      myDebugger.execute(command);
      return command.getCompletions();
    }
    return null;
  }
}