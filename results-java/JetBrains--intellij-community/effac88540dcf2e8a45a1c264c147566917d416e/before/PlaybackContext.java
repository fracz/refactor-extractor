/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
package com.intellij.openapi.ui.playback;

import java.awt.*;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: kirillk
 * Date: 8/4/11
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackContext {

  private PlaybackRunner.StatusCallback myCallback;
  private int myCurrentLine;
  private Robot myRobot;
  private boolean myUseDirectActionCall;
  private PlaybackCommand myCurrentCmd;

  public PlaybackContext(PlaybackRunner.StatusCallback callback, int currentLine, Robot robot, boolean useDriectActionCall, PlaybackCommand currentCmd) {
    myCallback = callback;
    myCurrentLine = currentLine;
    myRobot = robot;
    myUseDirectActionCall = useDriectActionCall;
    myCurrentCmd = currentCmd;
  }

  public PlaybackRunner.StatusCallback getCallback() {
    return myCallback;
  }

  public int getCurrentLine() {
    return myCurrentLine;
  }

  public Robot getRobot() {
    return myRobot;
  }

  public boolean isUseDirectActionCall() {
    return myUseDirectActionCall;
  }

  public PlaybackCommand getCurrentCmd() {
    return myCurrentCmd;
  }
}