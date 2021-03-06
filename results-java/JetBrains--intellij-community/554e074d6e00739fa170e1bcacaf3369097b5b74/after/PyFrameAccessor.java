package com.jetbrains.python.debugger;

import com.intellij.xdebugger.frame.XValueChildrenList;
import org.jetbrains.annotations.Nullable;

/**
 * Facade to access python variables frame
 *
 * @author traff
 */
public interface PyFrameAccessor {
  PyDebugValue evaluate(final String expression, final boolean execute, boolean doTrunc) throws PyDebuggerException;

  @Nullable
  XValueChildrenList loadFrame() throws PyDebuggerException;

  XValueChildrenList loadVariable(PyDebugValue var) throws PyDebuggerException;

  void changeVariable(PyDebugValue variable, String expression) throws PyDebuggerException;

  @Nullable
  PyReferrersLoader getReferrersLoader();

  Object[][] getArrayItems(PyDebugValue var, int colOffset, int rowOffset, int cols, int rows, String format);
}