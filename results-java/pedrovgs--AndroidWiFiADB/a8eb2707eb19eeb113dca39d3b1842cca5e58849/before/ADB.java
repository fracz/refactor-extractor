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

package com.github.pedrovgs.androidwifiadb.adb;

import org.jetbrains.android.sdk.AndroidSdkUtils;

import com.intellij.openapi.project.Project;
import org.jetbrains.android.sdk.AndroidSdkUtils;

import com.github.pedrovgs.androidwifiadb.model.Device;
import com.intellij.util.EnvironmentUtil;
import java.io.File;
import java.util.List;

public class ADB {

  private final CommandLine commandLine;
  private final ADBParser adbParser;
  private Project project;

  public ADB(CommandLine commandLine, ADBParser adbParser) {
    this.commandLine = commandLine;
    this.adbParser = adbParser;
  }

  public boolean isInstalled() {
    return AndroidSdkUtils.isAndroidSdkAvailable();
  }

  public List<Device> getDevicesConnectedByUSB() {
    String getDevicesCommand = getCommand("devices -l");
    String adbDevicesOutput = commandLine.executeCommand(getDevicesCommand);
    return adbParser.parseGetDevicesOutput(adbDevicesOutput);
  }

  public List<Device> connectDevices(List<Device> devices) {
    for (Device device : devices) {
      boolean connected = connectDeviceByIp(device);
      device.setConnected(connected);
    }
    return devices;
  }

  private boolean connectDeviceByIp(Device device) {
    String deviceIp = getDeviceIp(device);
    if (deviceIp.isEmpty()) {
      return false;
    } else {
      return connectDevice(deviceIp);
    }
  }

  private String getDeviceIp(Device device) {
    String getDeviceIpCommand =
        getCommand("-s " + device.getId() + " shell ip -f inet addr show wlan0");
    String ipInfoOutput = commandLine.executeCommand(getDeviceIpCommand);
    return adbParser.parseGetDeviceIp(ipInfoOutput);
  }

  private boolean connectDevice(String deviceIp) {
    String enableTCPCommand = getCommand("tcpip 5555");
    commandLine.executeCommand(enableTCPCommand);
    String connectDeviceCommand = getCommand("connect " + deviceIp);
    String connectOutput = commandLine.executeCommand(connectDeviceCommand);
    return connectOutput.contains("connected");
  }


  private String getAdbPath() {
    String adbPath = "";
    File adbFile = AndroidSdkUtils.getAdb(project);
    if (adbFile != null) {
      adbPath = adbFile.getAbsolutePath();
    }
    return adbPath;
  }

  private String getCommand(String command) {
    return getAdbPath() + " " + command;
  }

  public void updateProject(Project project) {
    this.project = project;
  }
}