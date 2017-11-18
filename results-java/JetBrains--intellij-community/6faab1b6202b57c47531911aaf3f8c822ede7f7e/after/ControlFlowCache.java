package com.jetbrains.python.codeInsight.controlflow;

import com.intellij.codeInsight.controlflow.ControlFlow;
import com.intellij.openapi.util.Key;
import com.intellij.reference.SoftReference;
import com.jetbrains.python.codeInsight.dataflow.scope.Scope;
import com.jetbrains.python.codeInsight.dataflow.scope.impl.ScopeImpl;

/**
 * @author yole
 */
public class ControlFlowCache {
  private static Key<SoftReference<ControlFlow>> CONTROL_FLOW_KEY = Key.create("com.jetbrains.python.codeInsight.controlflow.ControlFlow");
  private static Key<SoftReference<Scope>> SCOPE_KEY = Key.create("com.jetbrains.python.codeInsight.controlflow.Scope");

  private ControlFlowCache() {
  }

  public static void clear(ScopeOwner scopeOwner) {
    scopeOwner.putUserData(CONTROL_FLOW_KEY, null);
    scopeOwner.putUserData(SCOPE_KEY, null);
  }

  public static ControlFlow getControlFlow(ScopeOwner element) {
    SoftReference<ControlFlow> ref = element.getUserData(CONTROL_FLOW_KEY);
    ControlFlow flow = ref != null ? ref.get() : null;
    if (flow == null) {
      flow = new PyControlFlowBuilder().buildControlFlow(element);
      element.putUserData(CONTROL_FLOW_KEY, new SoftReference<ControlFlow>(flow));
    }
    return flow;
  }

  public static Scope getScope(ScopeOwner element) {
    SoftReference<Scope> ref = element.getUserData(SCOPE_KEY);
    Scope scope = ref != null ? ref.get() : null;
    if (scope == null) {
      scope = new ScopeImpl(element);
      element.putUserData(SCOPE_KEY, new SoftReference<Scope>(scope));
    }
    return scope;
  }
}