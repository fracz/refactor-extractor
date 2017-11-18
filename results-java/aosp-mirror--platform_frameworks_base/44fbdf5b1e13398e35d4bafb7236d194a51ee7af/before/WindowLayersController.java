/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.server.wm;

import android.util.Slog;
import android.view.Display;

import java.util.ArrayDeque;

import static android.app.ActivityManager.StackId.DOCKED_STACK_ID;
import static android.app.ActivityManager.StackId.PINNED_STACK_ID;
import static android.view.WindowManager.LayoutParams.TYPE_DOCK_DIVIDER;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYERS;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;
import static com.android.server.wm.WindowManagerService.WINDOW_LAYER_MULTIPLIER;

/**
 * Controller for assigning layers to windows on the display.
 *
 * This class encapsulates general algorithm for assigning layers and special rules that we need to
 * apply on top. The general algorithm goes through windows from bottom to the top and the higher
 * the window is, the higher layer is assigned. The final layer is equal to base layer +
 * adjustment from the order. This means that the window list is assumed to be ordered roughly by
 * the base layer (there are exceptions, e.g. due to keyguard and wallpaper and they need to be
 * handled with care, because they break the algorithm).
 *
 * On top of the general algorithm we add special rules, that govern such amazing things as:
 * <li>IME (which has higher base layer, but will be positioned above application windows)</li>
 * <li>docked/pinned windows (that need to be lifted above other application windows, including
 * animations)
 * <li>dock divider (which needs to live above applications, but below IME)</li>
 * <li>replaced windows, which need to live above their normal level, because they anticipate
 * an animation</li>.
 */
class WindowLayersController {
    private final WindowManagerService mService;

    WindowLayersController(WindowManagerService service) {
        mService = service;
    }

    private int mHighestApplicationLayer = 0;
    private ArrayDeque<WindowState> mPinnedWindows = new ArrayDeque<>();
    private ArrayDeque<WindowState> mDockedWindows = new ArrayDeque<>();
    private ArrayDeque<WindowState> mInputMethodWindows = new ArrayDeque<>();
    private ArrayDeque<WindowState> mOnTopLauncherWindows = new ArrayDeque<>();
    private WindowState mDockDivider = null;
    private ArrayDeque<WindowState> mReplacingWindows = new ArrayDeque<>();
    private int mCurBaseLayer;
    private int mCurLayer;
    private boolean mAnyLayerChanged;

    final void assignWindowLayers(DisplayContent dc) {
        if (DEBUG_LAYERS) Slog.v(TAG_WM, "Assigning layers based",
                new RuntimeException("here").fillInStackTrace());

        clear();
        dc.forAllWindows((w) -> {
            boolean layerChanged = false;

            int oldLayer = w.mLayer;
            if (w.mBaseLayer == mCurBaseLayer || w.mIsImWindow) {
                mCurLayer += WINDOW_LAYER_MULTIPLIER;
            } else {
                mCurBaseLayer = mCurLayer = w.mBaseLayer;
            }
            assignAnimLayer(w, mCurLayer);

            // TODO: Preserved old behavior of code here but not sure comparing oldLayer to
            // mAnimLayer and mLayer makes sense...though the worst case would be unintentional
            // layer reassignment.
            if (w.mLayer != oldLayer || w.mWinAnimator.mAnimLayer != oldLayer) {
                layerChanged = true;
                mAnyLayerChanged = true;
            }

            if (w.mAppToken != null) {
                mHighestApplicationLayer = Math.max(mHighestApplicationLayer,
                        w.mWinAnimator.mAnimLayer);
            }
            collectSpecialWindows(w);

            if (layerChanged) {
                w.scheduleAnimationIfDimming();
            }
        }, false /* traverseTopToBottom */);

        adjustSpecialWindows();

        //TODO (multidisplay): Magnification is supported only for the default display.
        if (mService.mAccessibilityController != null && mAnyLayerChanged
                && dc.getDisplayId() == Display.DEFAULT_DISPLAY) {
            mService.mAccessibilityController.onWindowLayersChangedLocked();
        }

        if (DEBUG_LAYERS) logDebugLayers(dc);
    }

    private void logDebugLayers(DisplayContent dc) {
        dc.forAllWindows((w) -> {
            final WindowStateAnimator winAnimator = w.mWinAnimator;
            Slog.v(TAG_WM, "Assign layer " + w + ": " + "mBase=" + w.mBaseLayer
                    + " mLayer=" + w.mLayer + (w.mAppToken == null
                    ? "" : " mAppLayer=" + w.mAppToken.mAppAnimator.animLayerAdjustment)
                    + " =mAnimLayer=" + winAnimator.mAnimLayer);
        }, false /* traverseTopToBottom */);
    }

    private void clear() {
        mHighestApplicationLayer = 0;
        mPinnedWindows.clear();
        mInputMethodWindows.clear();
        mDockedWindows.clear();
        mOnTopLauncherWindows.clear();
        mReplacingWindows.clear();
        mDockDivider = null;

        mCurBaseLayer = 0;
        mCurLayer = 0;
        mAnyLayerChanged = false;
    }

    private void collectSpecialWindows(WindowState w) {
        if (w.mAttrs.type == TYPE_DOCK_DIVIDER) {
            mDockDivider = w;
            return;
        }
        if (w.mWillReplaceWindow) {
            mReplacingWindows.add(w);
        }
        if (w.mIsImWindow) {
            mInputMethodWindows.add(w);
            return;
        }
        final Task task = w.getTask();
        if (task == null) {
            return;
        }
        if (task.isOnTopLauncher()) {
            mOnTopLauncherWindows.add(w);
        }
        final TaskStack stack = task.mStack;
        if (stack == null) {
            return;
        }
        if (stack.mStackId == PINNED_STACK_ID) {
            mPinnedWindows.add(w);
        } else if (stack.mStackId == DOCKED_STACK_ID) {
            mDockedWindows.add(w);
        }
    }

    private void adjustSpecialWindows() {
        int layer = mHighestApplicationLayer + WINDOW_LAYER_MULTIPLIER;
        // For pinned and docked stack window, we want to make them above other windows also when
        // these windows are animating.
        while (!mDockedWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded(mDockedWindows.remove(), layer);
        }

        layer = assignAndIncreaseLayerIfNeeded(mDockDivider, layer);

        boolean onTopLauncherVisible = !mOnTopLauncherWindows.isEmpty();
        while (!mOnTopLauncherWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded(mOnTopLauncherWindows.remove(), layer);
        }

        // Make sure IME windows are showing above the dock divider and on-top launcher windows.
        if ((mDockDivider != null && mDockDivider.isVisibleLw()) || onTopLauncherVisible) {
            while (!mInputMethodWindows.isEmpty()) {
                final WindowState w = mInputMethodWindows.remove();
                // Only ever move IME windows up, else we brake IME for windows above the divider.
                if (layer > w.mLayer) {
                    layer = assignAndIncreaseLayerIfNeeded(w, layer);
                }
            }
        }

        // We know that we will be animating a relaunching window in the near future, which will
        // receive a z-order increase. We want the replaced window to immediately receive the same
        // treatment, e.g. to be above the dock divider.
        while (!mReplacingWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded(mReplacingWindows.remove(), layer);
        }

        while (!mPinnedWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded(mPinnedWindows.remove(), layer);
        }
    }

    private int assignAndIncreaseLayerIfNeeded(WindowState win, int layer) {
        if (win != null) {
            assignAnimLayer(win, layer);
            // Make sure we leave space inbetween normal windows for dims and such.
            layer += WINDOW_LAYER_MULTIPLIER;
        }
        return layer;
    }

    private void assignAnimLayer(WindowState w, int layer) {
        w.mLayer = layer;
        w.mWinAnimator.mAnimLayer = w.getAnimLayerAdjustment()
                + w.getSpecialWindowAnimLayerAdjustment();
        if (w.mAppToken != null && w.mAppToken.mAppAnimator.thumbnailForceAboveLayer > 0
                && w.mWinAnimator.mAnimLayer > w.mAppToken.mAppAnimator.thumbnailForceAboveLayer) {
            w.mAppToken.mAppAnimator.thumbnailForceAboveLayer = w.mWinAnimator.mAnimLayer;
        }
    }
}