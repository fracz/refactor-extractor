package com.intellij.debugger.ui.impl;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.Disposable;
import com.intellij.debugger.ui.DebuggerView;
import com.intellij.debugger.ui.impl.watch.DebuggerTree;
import com.intellij.debugger.impl.DebuggerStateManager;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerContextListener;
import com.intellij.util.Alarm;
import com.sun.jdi.VMDisconnectedException;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public abstract class UpdatableDebuggerView extends JPanel implements DebuggerView {
  private final Project myProject;
  private final DebuggerStateManager myStateManager;
  protected final Alarm myRebuildAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
  private volatile boolean myRefreshNeeded = true;
  protected final java.util.List<Disposable> myDisposables = new ArrayList<Disposable>();
  private boolean myUpdateEnabled;

  protected UpdatableDebuggerView(final Project project, final DebuggerStateManager stateManager) {
    setLayout(new BorderLayout());
    myProject = project;
    myStateManager = stateManager;

    final DebuggerContextListener contextListener = new DebuggerContextListener() {
      public void changeEvent(DebuggerContextImpl newContext, int event) {
        UpdatableDebuggerView.this.changeEvent(newContext, event);
      }
    };
    myStateManager.addListener(contextListener);

    registerDisposable(new Disposable() {
      public void dispose() {
        myStateManager.removeListener(contextListener);
      }
    });

  }

  protected void changeEvent(final DebuggerContextImpl newContext, final int event) {
    if (newContext.getDebuggerSession() != null) {
      rebuildIfVisible(event);
    }
  }

  protected final boolean isUpdateEnabled() {
    return myUpdateEnabled;
  }

  public final void setUpdateEnabled(final boolean enabled) {
    myUpdateEnabled = enabled;
  }

  public final boolean isRefreshNeeded() {
    return myRefreshNeeded;
  }

  public final void rebuildIfVisible(final int event) {
    if(isUpdateEnabled()) {
      myRefreshNeeded = false;
      myRebuildAlarm.cancelAllRequests();
      myRebuildAlarm.addRequest(new Runnable() {
        public void run() {
          try {
            rebuild(event == DebuggerSession.EVENT_REFRESH || event == DebuggerSession.EVENT_REFRESH_VIEWS_ONLY);
          }
          catch (VMDisconnectedException e) {
            // ignored
          }
        }
      }, 100);
    }
    else {
      myRefreshNeeded = true;
    }
  }

  protected abstract void rebuild(boolean updateOnly);

  protected final void registerDisposable(Disposable disposable) {
    myDisposables.add(disposable);
  }

  public DebuggerContextImpl getContext() {
    return myStateManager.getContext();
  }

  protected final Project getProject() {
    return myProject;
  }

  public DebuggerStateManager getContextManager() {
    return myStateManager;
  }

  public void dispose() {
    myRebuildAlarm.dispose();
    for (Disposable disposable : myDisposables) {
      disposable.dispose();
    }
    myDisposables.clear();
  }
}