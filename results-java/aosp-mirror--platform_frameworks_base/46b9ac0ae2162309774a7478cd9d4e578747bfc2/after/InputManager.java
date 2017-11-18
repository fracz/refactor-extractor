/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server;

import com.android.internal.util.XmlUtils;
import com.android.server.KeyInputQueue.VirtualKey;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.LocalPowerManager;
import android.os.PowerManager;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import android.view.InputChannel;
import android.view.InputTarget;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.RawInputEvent;
import android.view.Surface;
import android.view.WindowManagerPolicy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/*
 * Wraps the C++ InputManager and provides its callbacks.
 *
 * XXX Tempted to promote this to a first-class service, ie. InputManagerService, to
 *     improve separation of concerns with respect to the window manager.
 */
public class InputManager {
    static final String TAG = "InputManager";

    private final Callbacks mCallbacks;
    private final Context mContext;
    private final WindowManagerService mWindowManagerService;
    private final WindowManagerPolicy mWindowManagerPolicy;
    private final PowerManager mPowerManager;
    private final PowerManagerService mPowerManagerService;

    private int mTouchScreenConfig;
    private int mKeyboardConfig;
    private int mNavigationConfig;

    private static native void nativeInit(Callbacks callbacks);
    private static native void nativeStart();
    private static native void nativeSetDisplaySize(int displayId, int width, int height);
    private static native void nativeSetDisplayOrientation(int displayId, int rotation);

    private static native int nativeGetScanCodeState(int deviceId, int deviceClasses,
            int scanCode);
    private static native int nativeGetKeyCodeState(int deviceId, int deviceClasses,
            int keyCode);
    private static native int nativeGetSwitchState(int deviceId, int deviceClasses,
            int sw);
    private static native boolean nativeHasKeys(int[] keyCodes, boolean[] keyExists);
    private static native void nativeRegisterInputChannel(InputChannel inputChannel);
    private static native void nativeUnregisterInputChannel(InputChannel inputChannel);

    // Device class as defined by EventHub.
    private static final int CLASS_KEYBOARD = 0x00000001;
    private static final int CLASS_ALPHAKEY = 0x00000002;
    private static final int CLASS_TOUCHSCREEN = 0x00000004;
    private static final int CLASS_TRACKBALL = 0x00000008;
    private static final int CLASS_TOUCHSCREEN_MT = 0x00000010;
    private static final int CLASS_DPAD = 0x00000020;

    public InputManager(Context context,
            WindowManagerService windowManagerService,
            WindowManagerPolicy windowManagerPolicy,
            PowerManager powerManager,
            PowerManagerService powerManagerService) {
        this.mContext = context;
        this.mWindowManagerService = windowManagerService;
        this.mWindowManagerPolicy = windowManagerPolicy;
        this.mPowerManager = powerManager;
        this.mPowerManagerService = powerManagerService;

        this.mCallbacks = new Callbacks();

        mTouchScreenConfig = Configuration.TOUCHSCREEN_NOTOUCH;
        mKeyboardConfig = Configuration.KEYBOARD_NOKEYS;
        mNavigationConfig = Configuration.NAVIGATION_NONAV;

        init();
    }

    private void init() {
        Slog.i(TAG, "Initializing input manager");
        nativeInit(mCallbacks);
    }

    public void start() {
        Slog.i(TAG, "Starting input manager");
        nativeStart();
    }

    public void setDisplaySize(int displayId, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid display id or dimensions.");
        }

        Slog.i(TAG, "Setting display #" + displayId + " size to " + width + "x" + height);
        nativeSetDisplaySize(displayId, width, height);
    }

    public void setDisplayOrientation(int displayId, int rotation) {
        if (rotation < Surface.ROTATION_0 || rotation > Surface.ROTATION_270) {
            throw new IllegalArgumentException("Invalid rotation.");
        }

        Slog.i(TAG, "Setting display #" + displayId + " orientation to " + rotation);
        nativeSetDisplayOrientation(displayId, rotation);
    }

    public void getInputConfiguration(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null.");
        }

        config.touchscreen = mTouchScreenConfig;
        config.keyboard = mKeyboardConfig;
        config.navigation = mNavigationConfig;
    }

    public int getScancodeState(int code) {
        return nativeGetScanCodeState(0, -1, code);
    }

    public int getScancodeState(int deviceId, int code) {
        return nativeGetScanCodeState(deviceId, -1, code);
    }

    public int getTrackballScancodeState(int code) {
        return nativeGetScanCodeState(-1, CLASS_TRACKBALL, code);
    }

    public int getDPadScancodeState(int code) {
        return nativeGetScanCodeState(-1, CLASS_DPAD, code);
    }

    public int getKeycodeState(int code) {
        return nativeGetKeyCodeState(0, -1, code);
    }

    public int getKeycodeState(int deviceId, int code) {
        return nativeGetKeyCodeState(deviceId, -1, code);
    }

    public int getTrackballKeycodeState(int code) {
        return nativeGetKeyCodeState(-1, CLASS_TRACKBALL, code);
    }

    public int getDPadKeycodeState(int code) {
        return nativeGetKeyCodeState(-1, CLASS_DPAD, code);
    }

    public int getSwitchState(int sw) {
        return nativeGetSwitchState(-1, -1, sw);
    }

    public int getSwitchState(int deviceId, int sw) {
        return nativeGetSwitchState(deviceId, -1, sw);
    }

    public boolean hasKeys(int[] keyCodes, boolean[] keyExists) {
        if (keyCodes == null) {
            throw new IllegalArgumentException("keyCodes must not be null.");
        }
        if (keyExists == null) {
            throw new IllegalArgumentException("keyExists must not be null.");
        }

        return nativeHasKeys(keyCodes, keyExists);
    }

    public void registerInputChannel(InputChannel inputChannel) {
        if (inputChannel == null) {
            throw new IllegalArgumentException("inputChannel must not be null.");
        }

        nativeRegisterInputChannel(inputChannel);
    }

    public void unregisterInputChannel(InputChannel inputChannel) {
        if (inputChannel == null) {
            throw new IllegalArgumentException("inputChannel must not be null.");
        }

        nativeUnregisterInputChannel(inputChannel);
    }

    // TBD where this really belongs, duplicate copy in WindowManagerService
    static final int INJECT_FAILED = 0;
    static final int INJECT_SUCCEEDED = 1;
    static final int INJECT_NO_PERMISSION = -1;

    /**
     * Injects a key event into the event system on behalf of an application.
     * @param event The event to inject.
     * @param nature The nature of the event.
     * @param sync If true, waits for the event to be completed before returning.
     * @param pid The pid of the injecting application.
     * @param uid The uid of the injecting application.
     * @return INJECT_SUCCEEDED, INJECT_FAILED or INJECT_NO_PERMISSION
     */
    public int injectKeyEvent(KeyEvent event, int nature, boolean sync, int pid, int uid) {
        // TODO
        return INJECT_FAILED;
    }

    /**
     * Injects a motion event into the event system on behalf of an application.
     * @param event The event to inject.
     * @param nature The nature of the event.
     * @param sync If true, waits for the event to be completed before returning.
     * @param pid The pid of the injecting application.
     * @param uid The uid of the injecting application.
     * @return INJECT_SUCCEEDED, INJECT_FAILED or INJECT_NO_PERMISSION
     */
    public int injectMotionEvent(MotionEvent event, int nature, boolean sync, int pid, int uid) {
        // TODO
        return INJECT_FAILED;
    }

    public void dump(PrintWriter pw) {
        // TODO
    }

    private static final class VirtualKeyDefinition {
        public int scanCode;

        // configured position data, specified in display coords
        public int centerX;
        public int centerY;
        public int width;
        public int height;
    }

    /*
     * Callbacks from native.
     */
    private class Callbacks {
        static final String TAG = "InputManager-Callbacks";

        private static final boolean DEBUG_VIRTUAL_KEYS = false;
        private static final String EXCLUDED_DEVICES_PATH = "etc/excluded-input-devices.xml";

        private final InputTargetList mReusableInputTargetList = new InputTargetList();

        @SuppressWarnings("unused")
        public boolean isScreenOn() {
            return mPowerManagerService.isScreenOn();
        }

        @SuppressWarnings("unused")
        public boolean isScreenBright() {
            return mPowerManagerService.isScreenBright();
        }

        @SuppressWarnings("unused")
        public void virtualKeyFeedback(long whenNanos, int deviceId, int action, int flags,
                int keyCode, int scanCode, int metaState, long downTimeNanos) {
            KeyEvent keyEvent = new KeyEvent(downTimeNanos / 1000000,
                    whenNanos / 1000000, action, keyCode, 0, metaState, scanCode, deviceId,
                    flags);

            mWindowManagerService.virtualKeyFeedback(keyEvent);
        }

        @SuppressWarnings("unused")
        public void notifyConfigurationChanged(long whenNanos,
                int touchScreenConfig, int keyboardConfig, int navigationConfig) {
            mTouchScreenConfig = touchScreenConfig;
            mKeyboardConfig = keyboardConfig;
            mNavigationConfig = navigationConfig;

            mWindowManagerService.sendNewConfiguration();
        }

        @SuppressWarnings("unused")
        public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
            mWindowManagerPolicy.notifyLidSwitchChanged(whenNanos, lidOpen);
        }

        @SuppressWarnings("unused")
        public int hackInterceptKey(int deviceId, int type, int scanCode,
                int keyCode, int policyFlags, int value, long whenNanos, boolean isScreenOn) {
            RawInputEvent event = new RawInputEvent();
            event.deviceId = deviceId;
            event.type = type;
            event.scancode = scanCode;
            event.keycode = keyCode;
            event.flags = policyFlags;
            event.value = value;
            event.when = whenNanos / 1000000;

            return mWindowManagerPolicy.interceptKeyTq(event, isScreenOn);
        }

        @SuppressWarnings("unused")
        public void goToSleep(long whenNanos) {
            long when = whenNanos / 1000000;
            mPowerManager.goToSleep(when);
        }

        @SuppressWarnings("unused")
        public void pokeUserActivityForKey(long whenNanos) {
            long when = whenNanos / 1000000;
            mPowerManagerService.userActivity(when, false,
                    LocalPowerManager.BUTTON_EVENT, false);
        }

        @SuppressWarnings("unused")
        public void notifyAppSwitchComing() {
            mWindowManagerService.mKeyWaiter.appSwitchComing();
        }

        @SuppressWarnings("unused")
        public boolean filterTouchEvents() {
            return mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_filterTouchEvents);
        }

        @SuppressWarnings("unused")
        public boolean filterJumpyTouchEvents() {
            return mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_filterJumpyTouchEvents);
        }

        @SuppressWarnings("unused")
        public VirtualKeyDefinition[] getVirtualKeyDefinitions(String deviceName) {
            ArrayList<VirtualKeyDefinition> keys = new ArrayList<VirtualKeyDefinition>();

            try {
                FileInputStream fis = new FileInputStream(
                        "/sys/board_properties/virtualkeys." + deviceName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr, 2048);
                String str = br.readLine();
                if (str != null) {
                    String[] it = str.split(":");
                    if (DEBUG_VIRTUAL_KEYS) Slog.v(TAG, "***** VIRTUAL KEYS: " + it);
                    final int N = it.length-6;
                    for (int i=0; i<=N; i+=6) {
                        if (!"0x01".equals(it[i])) {
                            Slog.w(TAG, "Unknown virtual key type at elem #" + i
                                    + ": " + it[i]);
                            continue;
                        }
                        try {
                            VirtualKeyDefinition key = new VirtualKeyDefinition();
                            key.scanCode = Integer.parseInt(it[i+1]);
                            key.centerX = Integer.parseInt(it[i+2]);
                            key.centerY = Integer.parseInt(it[i+3]);
                            key.width = Integer.parseInt(it[i+4]);
                            key.height = Integer.parseInt(it[i+5]);
                            if (DEBUG_VIRTUAL_KEYS) Slog.v(TAG, "Virtual key "
                                    + key.scanCode + ": center=" + key.centerX + ","
                                    + key.centerY + " size=" + key.width + "x"
                                    + key.height);
                            keys.add(key);
                        } catch (NumberFormatException e) {
                            Slog.w(TAG, "Bad number at region " + i + " in: "
                                    + str, e);
                        }
                    }
                }
                br.close();
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "No virtual keys found");
            } catch (IOException e) {
                Slog.w(TAG, "Error reading virtual keys", e);
            }

            return keys.toArray(new VirtualKeyDefinition[keys.size()]);
        }

        @SuppressWarnings("unused")
        public String[] getExcludedDeviceNames() {
            ArrayList<String> names = new ArrayList<String>();

            // Read partner-provided list of excluded input devices
            XmlPullParser parser = null;
            // Environment.getRootDirectory() is a fancy way of saying ANDROID_ROOT or "/system".
            File confFile = new File(Environment.getRootDirectory(), EXCLUDED_DEVICES_PATH);
            FileReader confreader = null;
            try {
                confreader = new FileReader(confFile);
                parser = Xml.newPullParser();
                parser.setInput(confreader);
                XmlUtils.beginDocument(parser, "devices");

                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"device".equals(parser.getName())) {
                        break;
                    }
                    String name = parser.getAttributeValue(null, "name");
                    if (name != null) {
                        names.add(name);
                    }
                }
            } catch (FileNotFoundException e) {
                // It's ok if the file does not exist.
            } catch (Exception e) {
                Slog.e(TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
            } finally {
                try { if (confreader != null) confreader.close(); } catch (IOException e) { }
            }

            return names.toArray(new String[names.size()]);
        }

        @SuppressWarnings("unused")
        public InputTarget[] getKeyEventTargets(KeyEvent event, int nature, int policyFlags) {
            mReusableInputTargetList.clear();

            mWindowManagerService.getKeyEventTargets(mReusableInputTargetList,
                    event, nature, policyFlags);

            return mReusableInputTargetList.toNullTerminatedArray();
        }

        @SuppressWarnings("unused")
        public InputTarget[] getMotionEventTargets(MotionEvent event, int nature, int policyFlags) {
            mReusableInputTargetList.clear();

            mWindowManagerService.getMotionEventTargets(mReusableInputTargetList,
                    event, nature, policyFlags);

            return mReusableInputTargetList.toNullTerminatedArray();
        }
    }
}