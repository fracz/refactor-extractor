package com.intellij.vcs.log.ui.filter;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.vcs.log.VcsLogFilterCollection;
import org.jetbrains.annotations.NotNull;

/**
 * Graphical UI for filtering commits in the log.
 *
 * @author Kirill Likhodedov
 */
public interface VcsLogFilterUi {

  /**
   * Returns filter components which will be added to the Log toolbar.
   */
  ActionGroup getFilterActionComponents();

  /**
   * Returns the filters currently active, i.e. switched on by user.
   */
  @NotNull
  VcsLogFilterCollection getFilters();

}