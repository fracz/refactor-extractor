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

import android.graphics.Rect;
import android.view.DisplayInfo;
import android.view.Surface;

import java.io.PrintWriter;
import java.util.List;

import libcore.util.Objects;

/**
 * Describes how a logical display is configured.
 * <p>
 * At this time, we only support logical displays that are coupled to a particular
 * primary display device from which the logical display derives its basic properties
 * such as its size, density and refresh rate.
 * </p><p>
 * A logical display may be mirrored onto multiple display devices in addition to its
 * primary display device.  Note that the contents of a logical display may not
 * always be visible, even on its primary display device, such as in the case where
 * the primary display device is currently mirroring content from a different
 * logical display.
 * </p><p>
 * This object is designed to encapsulate as much of the policy of logical
 * displays as possible.  The idea is to make it easy to implement new kinds of
 * logical displays mostly by making local changes to this class.
 * </p><p>
 * Note: The display manager architecture does not actually require logical displays
 * to be associated with any individual display device.  Logical displays and
 * display devices are orthogonal concepts.  Some mapping will exist between
 * logical displays and display devices but it can be many-to-many and
 * and some might have no relation at all.
 * </p><p>
 * Logical displays are guarded by the {@link DisplayManagerService.SyncRoot} lock.
 * </p>
 */
final class LogicalDisplay {
    private final DisplayInfo mBaseDisplayInfo = new DisplayInfo();

    private final int mLayerStack;
    private DisplayInfo mOverrideDisplayInfo; // set by the window manager
    private DisplayInfo mInfo;

    // The display device that this logical display is based on and which
    // determines the base metrics that it uses.
    private DisplayDevice mPrimaryDisplayDevice;
    private DisplayDeviceInfo mPrimaryDisplayDeviceInfo;

    // Temporary rectangle used when needed.
    private final Rect mTempRect = new Rect();

    public LogicalDisplay(int layerStack, DisplayDevice primaryDisplayDevice) {
        mLayerStack = layerStack;
        mPrimaryDisplayDevice = primaryDisplayDevice;
    }

    /**
     * Gets the primary display device associated with this logical display.
     *
     * @return The primary display device.
     */
    public DisplayDevice getPrimaryDisplayDeviceLocked() {
        return mPrimaryDisplayDevice;
    }

    /**
     * Gets information about the logical display.
     *
     * @return The device info, which should be treated as immutable by the caller.
     * The logical display should allocate a new display info object whenever
     * the data changes.
     */
    public DisplayInfo getDisplayInfoLocked() {
        if (mInfo == null) {
            mInfo = new DisplayInfo();
            if (mOverrideDisplayInfo != null) {
                mInfo.copyFrom(mOverrideDisplayInfo);
                mInfo.layerStack = mBaseDisplayInfo.layerStack;
                mInfo.name = mBaseDisplayInfo.name;
            } else {
                mInfo.copyFrom(mBaseDisplayInfo);
            }
        }
        return mInfo;
    }

    /**
     * Sets overridden logical display information from the window manager.
     * This method can be used to adjust application insets, rotation, and other
     * properties that the window manager takes care of.
     *
     * @param info The logical display information, may be null.
     */
    public void setDisplayInfoOverrideFromWindowManagerLocked(DisplayInfo info) {
        if (info != null) {
            if (mOverrideDisplayInfo == null) {
                mOverrideDisplayInfo = new DisplayInfo(info);
                mInfo = null;
            } else if (!mOverrideDisplayInfo.equals(info)) {
                mOverrideDisplayInfo.copyFrom(info);
                mInfo = null;
            }
        } else if (mOverrideDisplayInfo != null) {
            mOverrideDisplayInfo = null;
            mInfo = null;
        }
    }

    /**
     * Returns true if the logical display is in a valid state.
     * This method should be checked after calling {@link #update} to handle the
     * case where a logical display should be removed because all of its associated
     * display devices are gone or if it is otherwise no longer needed.
     *
     * @return True if the logical display is still valid.
     */
    public boolean isValidLocked() {
        return mPrimaryDisplayDevice != null;
    }

    /**
     * Updates the state of the logical display based on the available display devices.
     * The logical display might become invalid if it is attached to a display device
     * that no longer exists.
     *
     * @param devices The list of all connected display devices.
     */
    public void updateLocked(List<DisplayDevice> devices) {
        // Nothing to update if already invalid.
        if (mPrimaryDisplayDevice == null) {
            return;
        }

        // Check whether logical display has become invalid.
        if (!devices.contains(mPrimaryDisplayDevice)) {
            mPrimaryDisplayDevice = null;
            return;
        }

        // Bootstrap the logical display using its associated primary physical display.
        // We might use more elaborate configurations later.  It's possible that the
        // configuration of several physical displays might be used to determine the
        // logical display that they are sharing.  (eg. Adjust size for pixel-perfect
        // mirroring over HDMI.)
        DisplayDeviceInfo deviceInfo = mPrimaryDisplayDevice.getDisplayDeviceInfoLocked();
        if (!Objects.equal(mPrimaryDisplayDeviceInfo, deviceInfo)) {
            mBaseDisplayInfo.layerStack = mLayerStack;
            mBaseDisplayInfo.name = deviceInfo.name;
            mBaseDisplayInfo.appWidth = deviceInfo.width;
            mBaseDisplayInfo.appHeight = deviceInfo.height;
            mBaseDisplayInfo.logicalWidth = deviceInfo.width;
            mBaseDisplayInfo.logicalHeight = deviceInfo.height;
            mBaseDisplayInfo.rotation = Surface.ROTATION_0;
            mBaseDisplayInfo.refreshRate = deviceInfo.refreshRate;
            mBaseDisplayInfo.logicalDensityDpi = deviceInfo.densityDpi;
            mBaseDisplayInfo.physicalXDpi = deviceInfo.xDpi;
            mBaseDisplayInfo.physicalYDpi = deviceInfo.yDpi;
            mBaseDisplayInfo.smallestNominalAppWidth = deviceInfo.width;
            mBaseDisplayInfo.smallestNominalAppHeight = deviceInfo.height;
            mBaseDisplayInfo.largestNominalAppWidth = deviceInfo.width;
            mBaseDisplayInfo.largestNominalAppHeight = deviceInfo.height;

            mPrimaryDisplayDeviceInfo = deviceInfo;
            mInfo = null;
        }
    }

    /**
     * Applies the layer stack and transformation to the given display device
     * so that it shows the contents of this logical display.
     *
     * We know that the given display device is only ever showing the contents of
     * a single logical display, so this method is expected to blow away all of its
     * transformation properties to make it happen regardless of what the
     * display device was previously showing.
     *
     * The caller must have an open Surface transaction.
     *
     * The display device may not be the primary display device, in the case
     * where the display is being mirrored.
     *
     * @param device The display device to modify.
     */
    public void configureDisplayInTransactionLocked(DisplayDevice device) {
        final DisplayInfo displayInfo = getDisplayInfoLocked();
        final DisplayDeviceInfo displayDeviceInfo = device.getDisplayDeviceInfoLocked();

        // Set the layer stack.
        device.setLayerStackInTransactionLocked(mLayerStack);

        // Set the viewport.
        // This is the area of the logical display that we intend to show on the
        // display device.  For now, it is always the full size of the logical display.
        mTempRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        device.setViewportInTransactionLocked(mTempRect);

        // Set the orientation.
        // The orientation specifies how the physical coordinate system of the display
        // is rotated when the contents of the logical display are rendered.
        int orientation = Surface.ROTATION_0;
        if (device == mPrimaryDisplayDevice
                && (displayDeviceInfo.flags & DisplayDeviceInfo.FLAG_SUPPORTS_ROTATION) != 0) {
            orientation = displayInfo.rotation;
        }
        device.setOrientationInTransactionLocked(orientation);

        // Set the frame.
        // The frame specifies the rotated physical coordinates into which the viewport
        // is mapped.  We need to take care to preserve the aspect ratio of the viewport.
        // Currently we maximize the area to fill the display, but we could try to be
        // more clever and match resolutions.
        boolean rotated = (orientation == Surface.ROTATION_90
                || orientation == Surface.ROTATION_270);
        int physWidth = rotated ? displayDeviceInfo.height : displayDeviceInfo.width;
        int physHeight = rotated ? displayDeviceInfo.width : displayDeviceInfo.height;

        // Determine whether the width or height is more constrained to be scaled.
        //    physWidth / displayInfo.logicalWidth    => letter box
        // or physHeight / displayInfo.logicalHeight  => pillar box
        //
        // We avoid a division (and possible floating point imprecision) here by
        // multiplying the fractions by the product of their denominators before
        // comparing them.
        int frameWidth, frameHeight;
        if (physWidth * displayInfo.logicalHeight
                < physHeight * displayInfo.logicalWidth) {
            // Letter box.
            frameWidth = physWidth;
            frameHeight = displayInfo.logicalHeight * physWidth / displayInfo.logicalWidth;
        } else {
            // Pillar box.
            frameWidth = displayInfo.logicalWidth * physHeight / displayInfo.logicalHeight;
            frameHeight = physHeight;
        }
        int frameTop = (physHeight - frameHeight) / 2;
        int frameLeft = (physWidth - frameWidth) / 2;
        mTempRect.set(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight);
        device.setFrameInTransactionLocked(mTempRect);
    }

    public void dumpLocked(PrintWriter pw) {
        pw.println("mLayerStack=" + mLayerStack);
        pw.println("mPrimaryDisplayDevice=" + (mPrimaryDisplayDevice != null ?
                mPrimaryDisplayDevice.getNameLocked() : "null"));
        pw.println("mBaseDisplayInfo=" + mBaseDisplayInfo);
        pw.println("mOverrideDisplayInfo=" + mOverrideDisplayInfo);
    }
}