package com.intellij.ide.plugins;

import com.intellij.ide.startup.StartupActionScriptManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.io.ZipUtil;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: stathik
 * Date: Nov 29, 2003
 * Time: 9:15:30 PM
 * To change this template use Options | File Templates.
 */
public class PluginInstaller {
  private static final Object lock = new Object();

  public static boolean prepareToInstall (List <PluginNode> plugins) {
    ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();

    long total = 0;
    for (int i = 0; i < plugins.size(); i++) {
      PluginNode pluginNode = plugins.get(i);
      total += Long.valueOf(pluginNode.getSize()).longValue();
    }

    long count = 0;
    boolean result = false;

    for (int i = 0; i < plugins.size(); i++) {
      PluginNode pluginNode = plugins.get(i);
      pi.setText(pluginNode.getName());

      try {
        result |= prepareToInstall(pluginNode, true, count, total);
      } catch (IOException e) {
        throw new RuntimeException (e);
      }
      count += Integer.valueOf(pluginNode.getSize()).intValue();
    }
    return result;
  }

  public static boolean prepareToInstall (PluginNode pluginNode, boolean packet, long count, long available) throws IOException {
    // check for dependent plugins at first.
    if (pluginNode.getDepends() != null && pluginNode.getDepends().size() > 0) {
      // prepare plugins list for install

      List <PluginNode> depends = new ArrayList <PluginNode> ();
      for (int i = 0; i < pluginNode.getDepends().size(); i++) {
        String depPluginName = pluginNode.getDepends().get(i);

        if (PluginManager.isPluginInstalled(depPluginName)) {
        //  ignore installed plugins
          continue;
        }

        PluginNode depPlugin = new PluginNode(depPluginName);
        depPlugin.setSize("-1");

        depends.add(depPlugin);

      }

      if (depends.size() > 0) { // has something to install prior installing the plugin
        final boolean success = prepareToInstall(depends);
        if (!success) {
          return false;
        }
      }
    }

    synchronized (lock) {
      if (PluginManager.isPluginInstalled(pluginNode.getName())) {
        // add command to delete the 'action script' file
        PluginDescriptor pluginDescriptor = PluginManager.getPlugin(pluginNode.getName());

        StartupActionScriptManager.ActionCommand deleteOld = new StartupActionScriptManager.DeleteCommand(pluginDescriptor.getPath());
        StartupActionScriptManager.addActionCommand(deleteOld);
      }
      // download plugin
      File file = RepositoryHelper.downloadPlugin(pluginNode, packet, count, available);
      if (file == null) {
        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "Plugin " + pluginNode.getName() + " was not installed.");
        return false;
      }

      if (file.getName().endsWith(".jar")) {
        // add command to copy file to the IDEA/plugins path
        StartupActionScriptManager.ActionCommand copyPlugin = new StartupActionScriptManager.CopyCommand(file,
                                                     new File (PathManager.getPluginsPath() +
                                                     File.separator + file.getName()));
        StartupActionScriptManager.addActionCommand(copyPlugin);
      } else {
        // add command to unzip file to the IDEA/plugins path
        String unzipPath;
        if (ZipUtil.isZipContainsFolder(file))
          unzipPath = PathManager.getPluginsPath();
        else
          unzipPath = PathManager.getPluginsPath() + File.separator + pluginNode.getName();

        StartupActionScriptManager.ActionCommand unzip = new StartupActionScriptManager.UnzipCommand(file, new File (unzipPath));
        StartupActionScriptManager.addActionCommand(unzip);
      }

      // add command to remove temp plugin file
      StartupActionScriptManager.ActionCommand deleteTemp = new StartupActionScriptManager.DeleteCommand(file);
      StartupActionScriptManager.addActionCommand(deleteTemp);

      pluginNode.setStatus(PluginNode.STATUS_DOWNLOADED);
    }

    return true;
  }
  /**
   * Install plugin into a temp direcotry
   * Append 'action script' file with installing actions
   *
   * @param pluginNode Plugin to install
   */
  public static boolean prepareToInstall (PluginNode pluginNode) throws IOException {
    return prepareToInstall(pluginNode, false, 0, 0);
  }

  public static void prepareToUninstall (String pluginName) throws IOException {
    synchronized (lock) {
      if (PluginManager.isPluginInstalled(pluginName)) {
        // add command to delete the 'action script' file
        PluginDescriptor pluginDescriptor = PluginManager.getPlugin(pluginName);

        StartupActionScriptManager.ActionCommand deleteOld = new StartupActionScriptManager.DeleteCommand(pluginDescriptor.getPath());
        StartupActionScriptManager.addActionCommand(deleteOld);
      }
    }
  }

  public static void initPluginClasses () {
    synchronized(lock) {
      File file = new File (getPluginClassesPath());
      if (file.exists())
        file.delete();
    }
  }

  private static String getPluginClassesPath() {
    return PathManagerEx.getPluginTempPath() + File.separator + "plugin.classes";
  }

  public static Map<String, String> loadPluginClasses () throws IOException, ClassNotFoundException {
    synchronized(lock) {
      File file = new File (getPluginClassesPath());
      if (file.exists()) {
        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (file));
        return (Map<String, String>)ois.readObject();
      } else {
        return new HashMap<String, String> ();
      }
    }
  }

  public static void savePluginClasses (Map<String, String> classes) throws IOException {
    synchronized(lock) {
      File file = new File (getPluginClassesPath());

      ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream(file, false));
      oos.writeObject(classes);
      oos.close();
    }
  }
}