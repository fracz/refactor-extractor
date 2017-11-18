/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.util.ui.update;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;
import java.util.TreeMap;

public class MergingUpdateQueue implements Runnable, Disposable, Activatable {

  public static final JComponent ANY_COMPONENT = new JComponent() {};

  private volatile boolean myActive;
  private volatile boolean mySuspended;

  private final Map<Update, Update> mySheduledUpdates = new TreeMap<Update, Update>();

  private final Alarm myWaiterForMerge = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

  private volatile boolean myFlushing;

  private String myName;
  private int myMergingTimeSpan;
  private final JComponent myModalityStateComponent;
  private boolean myPassThrough;
  private boolean myDisposed;

  private UiNotifyConnector myUiNotifyConnector;
  private boolean myRestartOnAdd;

  public MergingUpdateQueue(@NonNls String name, int mergingTimeSpan, boolean isActive, JComponent modalityStateComponent) {
    this(name, mergingTimeSpan, isActive, modalityStateComponent, null);
  }

  public MergingUpdateQueue(@NonNls String name, int mergingTimeSpan, boolean isActive, JComponent modalityStateComponent, @Nullable Disposable parent) {
    this(name, mergingTimeSpan, isActive, modalityStateComponent, parent, null);
  }

  public MergingUpdateQueue(@NonNls String name, int mergingTimeSpan, boolean isActive, JComponent modalityStateComponent, @Nullable Disposable parent, @Nullable JComponent activationComponent) {
    myMergingTimeSpan = mergingTimeSpan;
    myModalityStateComponent = modalityStateComponent;
    myName = name;
    myPassThrough = ApplicationManager.getApplication().isUnitTestMode();

    if (isActive) {
      showNotify();
    }

    if (parent != null) {
      Disposer.register(parent, this);
    }

    if (activationComponent != null) {
      setActivationComponent(activationComponent);
    }
  }

  public void setMergingTimeSpan(int timeSpan) {
    myMergingTimeSpan = timeSpan;
    if (myActive) {
      restartTimer();
    }
  }

  public void cancelAllUpdates() {
    synchronized (mySheduledUpdates) {
      mySheduledUpdates.clear();
    }
  }

  public final boolean isPassThrough() {
    return myPassThrough;
  }

  public final void setPassThrough(boolean passThrough) {
    myPassThrough = passThrough;
  }

  public void activate() {
    showNotify();
  }

  public void deactivate() {
    hideNotify();
  }

  public void suspend() {
    mySuspended = true;
  }

  public void resume() {
    mySuspended = false;
    restartTimer();
  }

  public void hideNotify() {
    if (!myActive) {
      return;
    }

    myActive = false;
    clearWaiter();
  }

  public void showNotify() {
    if (myActive) {
      return;
    }

    restartTimer();
    myActive = true;
    flush();
  }

  public void restartTimer() {
    if (!myActive) return;

    clearWaiter();
    myWaiterForMerge.addRequest(this, myMergingTimeSpan, getMergerModailityState());
  }


  public void run() {
    if (mySuspended) return;
    flush();
  }

  public void flush() {
    synchronized(mySheduledUpdates) {
      if (mySheduledUpdates.isEmpty()) return;
    }
    flush(true);
  }

  public void flush(boolean invokeLaterIfNotDispatch) {
    if (myFlushing) {
      return;
    }
    if (!isModalityStateCorrect()) {
      return;
    }

    myFlushing = true;
    final Runnable toRun = new Runnable() {
      public void run() {
        try {
          final Update[] all;

          synchronized (mySheduledUpdates) {
            all = mySheduledUpdates.keySet().toArray(new Update[mySheduledUpdates.size()]);
            mySheduledUpdates.clear();
          }

          for (Update each : all) {
            each.setProcessed();
          }

          execute(all);
        }
        finally {
          myFlushing = false;
        }
      }
    };

    if (invokeLaterIfNotDispatch && !ApplicationManager.getApplication().isDispatchThread()) {
      ApplicationManager.getApplication().invokeLater(toRun, ModalityState.NON_MODAL);
    }
    else {
      toRun.run();
    }
  }

  protected boolean isModalityStateCorrect() {
    if (myModalityStateComponent == ANY_COMPONENT) return true;

    ModalityState current = ApplicationManager.getApplication().getCurrentModalityState();
    final ModalityState modalityState = getModalityState();
    return !current.dominates(modalityState);
  }

  private static boolean isExpired(Update each) {
    return each.isDisposed() || each.isExpired();
  }

  protected void execute(final Update[] update) {
    for (final Update each : update) {
      if (isExpired(each)) {
        each.setRejected();
        continue;
      }

      if (each.executeInWriteAction()) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            execute(each);
          }
        });
      }
      else {
        execute(each);
      }
    }
  }

  private void execute(final Update each) {
    if (myDisposed) {
      each.setRejected();
    } else {
      each.run();
    }
  }

  public void queue(final Update update) {
    final Application app = ApplicationManager.getApplication();
    if (myPassThrough) {
      app.invokeLater(new Runnable() {
        public void run() {
          if (myDisposed) return;
          update.run();
        }
      });
      return;
    }

    final boolean active = myActive;
    synchronized (mySheduledUpdates) {
      if (eatThisOrOthers(update)) {
        return;
      }

      if (active && mySheduledUpdates.isEmpty()) {
        restartTimer();
      }
      put(update);

      if (myRestartOnAdd) {
        restartTimer();
      }
    }
  }

  private boolean eatThisOrOthers(Update update) {
    if (mySheduledUpdates.containsKey(update)) {
      return false;
    }

    final Update[] updates = mySheduledUpdates.keySet().toArray(new Update[mySheduledUpdates.size()]);
    for (Update eachInQueue : updates) {
      if (eachInQueue.canEat(update)) {
        return true;
      }
      if (update.canEat(eachInQueue)) {
        mySheduledUpdates.remove(eachInQueue);
      }
    }
    return false;
  }

  public final void run(Update update) {
    execute(new Update[]{update});
  }

  private void put(Update update) {
    final Update existing = mySheduledUpdates.remove(update);
    if (existing != null && existing != update) {
      existing.setProcessed();
      existing.setRejected();
    }
    mySheduledUpdates.put(update, update);
  }

  protected static boolean passThroughForUnitTesting() {
    return true;
  }

  public boolean isActive() {
    return myActive;
  }

  public void dispose() {
    myDisposed = true;
    myActive = false;
    clearWaiter();
  }

  private void clearWaiter() {
    myWaiterForMerge.cancelAllRequests();
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return "Merger: " + myName + " active=" + myActive + " sheduled=" + mySheduledUpdates;
  }

  private ModalityState getMergerModailityState() {
    return myModalityStateComponent == ANY_COMPONENT ? null : getModalityState();
  }

  public ModalityState getModalityState() {
    if (myModalityStateComponent == null) {
      return ModalityState.NON_MODAL;
    }
    return ModalityState.stateForComponent(myModalityStateComponent);
  }

  public void setActivationComponent(JComponent c) {
    if (myUiNotifyConnector != null) {
      Disposer.dispose(myUiNotifyConnector);
    }

    UiNotifyConnector connector = new UiNotifyConnector(c, this);
    Disposer.register(this, connector);
    myUiNotifyConnector = connector;
  }

  public MergingUpdateQueue setRestartTimerOnAdd(final boolean restart) {
    myRestartOnAdd = restart;
    return this;
  }
}