package com.chrisrm.idea.actions;

import com.chrisrm.idea.MTTheme;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class MTContrastAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent e) {
    MTTheme.getCurrentPreference().toggleContrast();
  }
}