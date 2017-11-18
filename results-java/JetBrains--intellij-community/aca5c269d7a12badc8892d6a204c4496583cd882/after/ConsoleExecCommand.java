package com.jetbrains.python.debugger.pydev;

import com.jetbrains.python.debugger.PyDebugValue;
import com.jetbrains.python.debugger.PyDebuggerException;


public class ConsoleExecCommand extends AbstractFrameCommand {
  private final String myExpression;
  private String myValue = null;

  public ConsoleExecCommand(final RemoteDebugger debugger, final String threadId, final String frameId, final String expression) {
    super(debugger, CONSOLE_EXEC, threadId, frameId);
    myExpression = expression;
  }

  @Override
  protected void buildPayload(Payload payload) {
    super.buildPayload(payload);
    payload.add("FRAME").add(myExpression);
  }

  @Override
  public boolean isResponseExpected() {
    return true;
  }

  @Override
  protected void processResponse(final ProtocolFrame response) throws PyDebuggerException {
    super.processResponse(response);
    final PyDebugValue value = ProtocolParser.parseValue(response.getPayload());
    myValue = value.getValue();
  }

  public String getValue() {
    return myValue;
  }

}