/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.lang.ant.config.impl;

import com.intellij.lang.ant.config.AntConfiguration;
import com.intellij.lang.ant.config.AntConfigurationBase;
import com.intellij.lang.ant.config.actions.TargetActionStub;
import com.intellij.lang.ant.config.explorer.AntExplorer;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Eugene Zhuravlev
 *         Date: Apr 24, 2007
 */
public class AntToolwindowRegistrar implements ProjectComponent {
  private AntExplorer myAntExplorer;
  private final Project myProject;
  private final ToolWindowManager myToolWindowManager;

  public AntToolwindowRegistrar(Project project, ToolWindowManager toolWindowManager) {
    myProject = project;
    myToolWindowManager = toolWindowManager;
  }

  public void projectOpened() {

    final KeymapManagerEx keymapManager = KeymapManagerEx.getInstanceEx();
    final String prefix = AntConfiguration.getActionIdPrefix(myProject);
    final ActionManager actionManager = ActionManager.getInstance();

    for (Keymap keymap : keymapManager.getAllKeymaps()) {
      for (String id : keymap.getActionIds()) {
        if (id.startsWith(prefix) && actionManager.getAction(id) == null) {
          actionManager.registerAction(id, new TargetActionStub(id, myProject));
        }
      }
    }

    final CompilerManager compilerManager = CompilerManager.getInstance(myProject);
    final DataContext dataContext = SimpleDataContext.getProjectContext(myProject);
    compilerManager.addBeforeTask(new CompileTask() {
      public boolean execute(CompileContext context) {
        final AntConfiguration config = AntConfiguration.getInstance(myProject);
        ((AntConfigurationBase)config).ensureInitialized();
        return config.executeTargetBeforeCompile(dataContext);
      }
    });
    compilerManager.addAfterTask(new CompileTask() {
      public boolean execute(CompileContext context) {
        final AntConfiguration config = AntConfiguration.getInstance(myProject);
        ((AntConfigurationBase)config).ensureInitialized();
        return config.executeTargetAfterCompile(dataContext);
      }
    });

    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        final JPanel explorerPanel = new JPanel(new BorderLayout()) {
          boolean explorerInitialized = false;
          public void addNotify() {
            super.addNotify();
            if (!explorerInitialized) {
              explorerInitialized = true;
              add(myAntExplorer = new AntExplorer(myProject), BorderLayout.CENTER);
            }
          }
        };
        ToolWindow toolWindow = myToolWindowManager.registerToolWindow(ToolWindowId.ANT_BUILD, explorerPanel, ToolWindowAnchor.RIGHT);
        toolWindow.setIcon(IconLoader.getIcon("/general/toolWindowAnt.png"));
      }
    });
  }

  public void projectClosed() {
    final ActionManagerEx actionManager = ActionManagerEx.getInstanceEx();
    final String[] oldIds = actionManager.getActionIds(AntConfiguration.getActionIdPrefix(myProject));
    for (String oldId : oldIds) {
      actionManager.unregisterAction(oldId);
    }
    if (myAntExplorer != null) {
      myToolWindowManager.unregisterToolWindow(ToolWindowId.ANT_BUILD);
      myAntExplorer.dispose();
      myAntExplorer = null;
    }
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "AntToolwindowRegistrar";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }
}