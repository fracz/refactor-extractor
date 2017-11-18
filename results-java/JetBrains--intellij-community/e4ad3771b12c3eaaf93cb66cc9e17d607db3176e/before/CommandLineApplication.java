package com.intellij.idea;

import com.intellij.ide.impl.DataManagerImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.vfs.local.win32.FileWatcher;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class CommandLineApplication {
  private static final Logger LOG = Logger.getInstance("#com.intellij.idea.CommandLineApplication");
  protected static CommandLineApplication ourInstance = null;

  static {
    System.setProperty(FileWatcher.PROPERTY_WATCHER_DISABLED, Boolean.TRUE.toString());

    /*
    final Category category = Category.getRoot();
    final Enumeration enumeration = category.getAllAppenders();
    while (enumeration.hasMoreElements()) {
      Object o = enumeration.nextElement();
      if (o instanceof DialogAppender) {
        category.removeAppender((Appender)o);
        break;
      }
    }
    */
  }

  protected CommandLineApplication() {}

  protected CommandLineApplication(boolean isInternal, boolean isUnitTestMode, boolean isHeadless, @NonNls String componentsDescriptor) {
    this(isInternal, isUnitTestMode, isHeadless, componentsDescriptor, "idea");
  }

  protected CommandLineApplication(boolean isInternal, boolean isUnitTestMode, boolean isHeadless, String componentsDescriptor, @NonNls String appName) {
    LOG.assertTrue(ourInstance == null, "Only one instance allowed.");
    ourInstance = this;
    ApplicationManagerEx.createApplication(componentsDescriptor, isInternal, isUnitTestMode, isHeadless, true, appName);
  }

  public Object getData(String dataId) {
    return null;
  }

  public static class MyDataManagerImpl extends DataManagerImpl {

    public DataContext getDataContext() {
      return new DataContext() {
        public Object getData(String dataId) {
          return ourInstance.getData(dataId);
        }
      };
    }

    public DataContext getDataContext(Component component) {
      return getDataContext();
    }

    public DataContext getDataContext(@NotNull Component component, int x, int y) {
      return getDataContext();
    }
  }
}