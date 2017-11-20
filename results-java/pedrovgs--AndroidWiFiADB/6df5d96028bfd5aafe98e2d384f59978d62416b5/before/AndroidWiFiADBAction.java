/*
 * Copyright (C) 2015 Pedro Vicente Gómez Sánchez.
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

package com.github.pedrovgs.androidwifiadb.view;

import com.github.pedrovgs.androidwifiadb.adb.ADB;
import com.github.pedrovgs.androidwifiadb.adb.ADBParser;
import com.github.pedrovgs.androidwifiadb.AndroidWiFiADB;
import com.github.pedrovgs.androidwifiadb.adb.CommandLine;
import com.github.pedrovgs.androidwifiadb.Device;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

public class AndroidWiFiADBAction implements ToolWindowFactory, View, DeviceAction {

  private static final int INTERVAL_REFRESH_DEVICES = 1000;
  private static final String ANDROID_WIFI_ADB_TITLE = "Android WiFi ADB";
  private static final NotificationGroup NOTIFICATION_GROUP =
      NotificationGroup.balloonGroup(ANDROID_WIFI_ADB_TITLE);

  private final AndroidWiFiADB androidWifiADB;
  private JPanel toolWindowContent;
  private final CardLayoutDevices cardLayoutDevices;

  public AndroidWiFiADBAction() {
    CommandLine commandLine = new CommandLine();
    ADBParser adbParser = new ADBParser();
    ADB adb = new ADB(commandLine, adbParser);
    this.androidWifiADB = new AndroidWiFiADB(adb, this);
    cardLayoutDevices = new CardLayoutDevices(toolWindowContent, this);
  }

  @Override public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    this.androidWifiADB.updateProject(project);

    createToolWindowContent(toolWindow);
    setupUI();
    monitorDevices();
  }

  private void createToolWindowContent(ToolWindow toolWindow) {
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(toolWindowContent, "", false);
    toolWindow.getContentManager().addContent(content);
  }

  @Override public void showNoConnectedDevicesNotification() {
    showNotification(ANDROID_WIFI_ADB_TITLE,
        "There are no devices connected. Review your USB connection and try again.",
        NotificationType.INFORMATION);
  }

  @Override public void showConnectedDeviceNotification(Device device) {
    showNotification(ANDROID_WIFI_ADB_TITLE, "Device '" + device.getName() + "' connected.",
        NotificationType.INFORMATION);
  }

  @Override public void showDisconnectedDeviceNotification(Device device) {
    showNotification(ANDROID_WIFI_ADB_TITLE, "Device '" + device.getName() + "' disconnected.",
        NotificationType.INFORMATION);
  }

  @Override public void showErrorConnectingDeviceNotification(Device device) {
    showNotification(ANDROID_WIFI_ADB_TITLE,
        "Unable to connect device '" + device.getName() + "'. Review your WiFi connection.",
        NotificationType.INFORMATION);
  }

  @Override public void showErrorDisconnectingDeviceNotification(Device device) {
    showNotification(ANDROID_WIFI_ADB_TITLE,
        "Unable to disconnect device '" + device.getName() + "'. Review your WiFi connection.",
        NotificationType.INFORMATION);
  }

  @Override public void showADBNotInstalledNotification() {
    showNotification(ANDROID_WIFI_ADB_TITLE,
        "'adb' command not found. Review your Android SDK installation.", NotificationType.ERROR);
  }

  private void setupUI() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        cardLayoutDevices.createAndShowGUI();
      }
    });
  }

  @Override public void connectDevice(final Device device) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        androidWifiADB.connectDevice(device);
        updateUi();
      }
    });
  }

  @Override public void disconnectDevice(final Device device) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        androidWifiADB.disconnectDevice(device);
        updateUi();
      }
    });
  }

  private void monitorDevices() {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        new Timer().schedule(new TimerTask() {
          @Override public void run() {
            boolean refreshRequired = androidWifiADB.refreshDevicesList();
            if (refreshRequired) {
              updateUi();
            }
          }
        }, 0, INTERVAL_REFRESH_DEVICES);
      }
    });
  }

  private void showNotification(final String title, final String message,
      final NotificationType type) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        Notification notification =
            NOTIFICATION_GROUP.createNotification(title, message, type, null);
        Notifications.Bus.notify(notification);
      }
    });
  }

  private void updateUi() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        cardLayoutDevices.setDevices(androidWifiADB.getDevices());
        cardLayoutDevices.updateUi();
      }
    });
  }
}