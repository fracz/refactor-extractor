/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.server.display;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.util.SparseArray;
import android.view.Surface;
import android.view.Surface.PhysicalDisplayInfo;

import java.io.PrintWriter;

/**
 * A display adapter for the local displays managed by Surface Flinger.
 * <p>
 * Display adapters are guarded by the {@link DisplayManagerService.SyncRoot} lock.
 * </p>
 */
final class LocalDisplayAdapter extends DisplayAdapter {
    private static final String TAG = "LocalDisplayAdapter";

    private static final int[] BUILT_IN_DISPLAY_IDS_TO_SCAN = new int[] {
            Surface.BUILT_IN_DISPLAY_ID_MAIN,
            Surface.BUILT_IN_DISPLAY_ID_HDMI,
    };

    private final SparseArray<LocalDisplayDevice> mDevices =
            new SparseArray<LocalDisplayDevice>();

    private final PhysicalDisplayInfo mTempPhys = new PhysicalDisplayInfo();

    public LocalDisplayAdapter(DisplayManagerService.SyncRoot syncRoot,
            Context context, Handler handler, Listener listener) {
        super(syncRoot, context, handler, listener, TAG);
    }

    @Override
    public void registerLocked() {
        // TODO: listen for notifications from Surface Flinger about
        // built-in displays being added or removed and rescan as needed.
        super.registerLocked();
        scanDisplaysLocked();
    }

    private void scanDisplaysLocked() {
        for (int builtInDisplayId : BUILT_IN_DISPLAY_IDS_TO_SCAN) {
            IBinder displayToken = Surface.getBuiltInDisplay(builtInDisplayId);
            if (displayToken != null && Surface.getDisplayInfo(displayToken, mTempPhys)) {
                LocalDisplayDevice device = mDevices.get(builtInDisplayId);
                if (device == null) {
                    // Display was added.
                    device = new LocalDisplayDevice(displayToken, builtInDisplayId, mTempPhys);
                    mDevices.put(builtInDisplayId, device);
                    sendDisplayDeviceEventLocked(device, DISPLAY_DEVICE_EVENT_ADDED);
                } else if (device.updatePhysicalDisplayInfoLocked(mTempPhys)) {
                    // Display properties changed.
                    sendDisplayDeviceEventLocked(device, DISPLAY_DEVICE_EVENT_CHANGED);
                }
            } else {
                LocalDisplayDevice device = mDevices.get(builtInDisplayId);
                if (device != null) {
                    // Display was removed.
                    mDevices.remove(builtInDisplayId);
                    sendDisplayDeviceEventLocked(device, DISPLAY_DEVICE_EVENT_REMOVED);
                }
            }
        }
    }

    private final class LocalDisplayDevice extends DisplayDevice {
        private final int mBuiltInDisplayId;
        private final PhysicalDisplayInfo mPhys;

        private DisplayDeviceInfo mInfo;
        private boolean mHavePendingChanges;

        public LocalDisplayDevice(IBinder displayToken, int builtInDisplayId,
                PhysicalDisplayInfo phys) {
            super(LocalDisplayAdapter.this, displayToken);
            mBuiltInDisplayId = builtInDisplayId;
            mPhys = new PhysicalDisplayInfo(phys);
        }

        public boolean updatePhysicalDisplayInfoLocked(PhysicalDisplayInfo phys) {
            if (!mPhys.equals(phys)) {
                mPhys.copyFrom(phys);
                mHavePendingChanges = true;
                return true;
            }
            return false;
        }

        @Override
        public void applyPendingDisplayDeviceInfoChangesLocked() {
            if (mHavePendingChanges) {
                mInfo = null;
                mHavePendingChanges = false;
            }
        }

        @Override
        public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
            if (mInfo == null) {
                mInfo = new DisplayDeviceInfo();
                mInfo.width = mPhys.width;
                mInfo.height = mPhys.height;
                mInfo.refreshRate = mPhys.refreshRate;
                mInfo.densityDpi = (int)(mPhys.density * 160 + 0.5f);
                mInfo.xDpi = mPhys.xDpi;
                mInfo.yDpi = mPhys.yDpi;
                if (mBuiltInDisplayId == Surface.BUILT_IN_DISPLAY_ID_MAIN) {
                    mInfo.name = getContext().getResources().getString(
                            com.android.internal.R.string.display_manager_built_in_display_name);
                    mInfo.flags = DisplayDeviceInfo.FLAG_DEFAULT_DISPLAY
                            | DisplayDeviceInfo.FLAG_SECURE
                            | DisplayDeviceInfo.FLAG_SUPPORTS_ROTATION;
                } else {
                    mInfo.name = getContext().getResources().getString(
                            com.android.internal.R.string.display_manager_hdmi_display_name);
                    mInfo.flags = DisplayDeviceInfo.FLAG_SECURE;
                }
            }
            return mInfo;
        }

        @Override
        public void dumpLocked(PrintWriter pw) {
            super.dumpLocked(pw);
            pw.println("mBuiltInDisplayId=" + mBuiltInDisplayId);
            pw.println("mPhys=" + mPhys);
        }
    }
}