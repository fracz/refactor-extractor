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
 * limitations under the License
 */

package com.android.server.wm;

import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
import static android.view.WindowManager.LayoutParams.TYPE_WALLPAPER;
import static android.view.WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_WALLPAPER;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WITH_CLASS_NAME;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;
import static com.android.server.wm.WindowManagerService.H.WALLPAPER_DRAW_PENDING_TIMEOUT;
import static com.android.server.wm.WindowManagerService.TYPE_LAYER_MULTIPLIER;
import static com.android.server.wm.WindowManagerService.TYPE_LAYER_OFFSET;

import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.WindowManager;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Controls wallpaper windows visibility, ordering, and so on.
 * NOTE: All methods in this class must be called with the window manager service lock held.
 */
class WallpaperController {
    private static final String TAG = TAG_WITH_CLASS_NAME ? "WallpaperController" : TAG_WM;
    final private WindowManagerService mService;

    private final ArrayList<WindowToken> mWallpaperTokens = new ArrayList<>();

    // If non-null, this is the currently visible window that is associated
    // with the wallpaper.
    private WindowState mWallpaperTarget = null;
    // If non-null, we are in the middle of animating from one wallpaper target
    // to another, and this is the lower one in Z-order.
    private WindowState mLowerWallpaperTarget = null;
    // If non-null, we are in the middle of animating from one wallpaper target
    // to another, and this is the higher one in Z-order.
    private WindowState mUpperWallpaperTarget = null;

    private int mWallpaperAnimLayerAdjustment;

    private float mLastWallpaperX = -1;
    private float mLastWallpaperY = -1;
    private float mLastWallpaperXStep = -1;
    private float mLastWallpaperYStep = -1;
    private int mLastWallpaperDisplayOffsetX = Integer.MIN_VALUE;
    private int mLastWallpaperDisplayOffsetY = Integer.MIN_VALUE;

    // This is set when we are waiting for a wallpaper to tell us it is done
    // changing its scroll position.
    WindowState mWaitingOnWallpaper;

    // The last time we had a timeout when waiting for a wallpaper.
    private long mLastWallpaperTimeoutTime;
    // We give a wallpaper up to 150ms to finish scrolling.
    private static final long WALLPAPER_TIMEOUT = 150;
    // Time we wait after a timeout before trying to wait again.
    private static final long WALLPAPER_TIMEOUT_RECOVERY = 10000;

    // Set to the wallpaper window we would like to hide once the transition animations are done.
    // This is useful in cases where we don't want the wallpaper to be hidden when the close app
    // is a wallpaper target and is done animating out, but the opening app isn't a wallpaper
    // target and isn't done animating in.
    WindowState mDeferredHideWallpaper = null;

    // We give a wallpaper up to 500ms to finish drawing before playing app transitions.
    private static final long WALLPAPER_DRAW_PENDING_TIMEOUT_DURATION = 500;
    private static final int WALLPAPER_DRAW_NORMAL = 0;
    private static final int WALLPAPER_DRAW_PENDING = 1;
    private static final int WALLPAPER_DRAW_TIMEOUT = 2;
    private int mWallpaperDrawState = WALLPAPER_DRAW_NORMAL;

    private final FindWallpaperTargetResult mFindResults = new FindWallpaperTargetResult();

    public WallpaperController(WindowManagerService service) {
        mService = service;
    }

    WindowState getWallpaperTarget() {
        return mWallpaperTarget;
    }

    WindowState getLowerWallpaperTarget() {
        return mLowerWallpaperTarget;
    }

    WindowState getUpperWallpaperTarget() {
        return mUpperWallpaperTarget;
    }

    boolean isWallpaperTarget(WindowState win) {
        return win == mWallpaperTarget;
    }

    boolean isBelowWallpaperTarget(WindowState win) {
        return mWallpaperTarget != null && mWallpaperTarget.mLayer >= win.mBaseLayer;
    }

    boolean isWallpaperVisible() {
        return isWallpaperVisible(mWallpaperTarget);
    }

    private boolean isWallpaperVisible(WindowState wallpaperTarget) {
        if (DEBUG_WALLPAPER) Slog.v(TAG, "Wallpaper vis: target " + wallpaperTarget + ", obscured="
                + (wallpaperTarget != null ? Boolean.toString(wallpaperTarget.mObscured) : "??")
                + " anim=" + ((wallpaperTarget != null && wallpaperTarget.mAppToken != null)
                ? wallpaperTarget.mAppToken.mAppAnimator.animation : null)
                + " upper=" + mUpperWallpaperTarget
                + " lower=" + mLowerWallpaperTarget);
        return (wallpaperTarget != null
                && (!wallpaperTarget.mObscured || (wallpaperTarget.mAppToken != null
                && wallpaperTarget.mAppToken.mAppAnimator.animation != null)))
                || mUpperWallpaperTarget != null
                || mLowerWallpaperTarget != null;
    }

    boolean isWallpaperTargetAnimating() {
        return mWallpaperTarget != null && mWallpaperTarget.mWinAnimator.isAnimationSet()
                && !mWallpaperTarget.mWinAnimator.isDummyAnimation();
    }

    void updateWallpaperVisibility() {
        final boolean visible = isWallpaperVisible(mWallpaperTarget);

        for (int curTokenNdx = mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            final WindowToken token = mWallpaperTokens.get(curTokenNdx);
            token.updateWallpaperVisibility(visible);
        }
    }

    void hideDeferredWallpapersIfNeeded() {
        if (mDeferredHideWallpaper != null) {
            hideWallpapers(mDeferredHideWallpaper);
            mDeferredHideWallpaper = null;
        }
    }

    void hideWallpapers(final WindowState winGoingAway) {
        if (mWallpaperTarget != null
                && (mWallpaperTarget != winGoingAway || mLowerWallpaperTarget != null)) {
            return;
        }
        if (mService.mAppTransition.isRunning()) {
            // Defer hiding the wallpaper when app transition is running until the animations
            // are done.
            mDeferredHideWallpaper = winGoingAway;
            return;
        }

        final boolean wasDeferred = (mDeferredHideWallpaper == winGoingAway);
        for (int i = mWallpaperTokens.size() - 1; i >= 0; i--) {
            final WindowToken token = mWallpaperTokens.get(i);
            token.hideWallpaperToken(wasDeferred, "hideWallpapers");
            if (DEBUG_WALLPAPER_LIGHT && !token.hidden) Slog.d(TAG, "Hiding wallpaper " + token
                    + " from " + winGoingAway + " target=" + mWallpaperTarget + " lower="
                    + mLowerWallpaperTarget + "\n" + Debug.getCallers(5, "  "));
        }
    }

    boolean updateWallpaperOffset(WindowState wallpaperWin, int dw, int dh, boolean sync) {
        boolean rawChanged = false;
        // Set the default wallpaper x-offset to either edge of the screen (depending on RTL), to
        // match the behavior of most Launchers
        float defaultWallpaperX = wallpaperWin.isRtl() ? 1f : 0f;
        float wpx = mLastWallpaperX >= 0 ? mLastWallpaperX : defaultWallpaperX;
        float wpxs = mLastWallpaperXStep >= 0 ? mLastWallpaperXStep : -1.0f;
        int availw = wallpaperWin.mFrame.right - wallpaperWin.mFrame.left - dw;
        int offset = availw > 0 ? -(int)(availw * wpx + .5f) : 0;
        if (mLastWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
            offset += mLastWallpaperDisplayOffsetX;
        }
        boolean changed = wallpaperWin.mXOffset != offset;
        if (changed) {
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Update wallpaper " + wallpaperWin + " x: " + offset);
            wallpaperWin.mXOffset = offset;
        }
        if (wallpaperWin.mWallpaperX != wpx || wallpaperWin.mWallpaperXStep != wpxs) {
            wallpaperWin.mWallpaperX = wpx;
            wallpaperWin.mWallpaperXStep = wpxs;
            rawChanged = true;
        }

        float wpy = mLastWallpaperY >= 0 ? mLastWallpaperY : 0.5f;
        float wpys = mLastWallpaperYStep >= 0 ? mLastWallpaperYStep : -1.0f;
        int availh = wallpaperWin.mFrame.bottom - wallpaperWin.mFrame.top - dh;
        offset = availh > 0 ? -(int)(availh * wpy + .5f) : 0;
        if (mLastWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
            offset += mLastWallpaperDisplayOffsetY;
        }
        if (wallpaperWin.mYOffset != offset) {
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Update wallpaper " + wallpaperWin + " y: " + offset);
            changed = true;
            wallpaperWin.mYOffset = offset;
        }
        if (wallpaperWin.mWallpaperY != wpy || wallpaperWin.mWallpaperYStep != wpys) {
            wallpaperWin.mWallpaperY = wpy;
            wallpaperWin.mWallpaperYStep = wpys;
            rawChanged = true;
        }

        if (rawChanged && (wallpaperWin.mAttrs.privateFlags &
                WindowManager.LayoutParams.PRIVATE_FLAG_WANTS_OFFSET_NOTIFICATIONS) != 0) {
            try {
                if (DEBUG_WALLPAPER) Slog.v(TAG, "Report new wp offset "
                        + wallpaperWin + " x=" + wallpaperWin.mWallpaperX
                        + " y=" + wallpaperWin.mWallpaperY);
                if (sync) {
                    mWaitingOnWallpaper = wallpaperWin;
                }
                wallpaperWin.mClient.dispatchWallpaperOffsets(
                        wallpaperWin.mWallpaperX, wallpaperWin.mWallpaperY,
                        wallpaperWin.mWallpaperXStep, wallpaperWin.mWallpaperYStep, sync);
                if (sync) {
                    if (mWaitingOnWallpaper != null) {
                        long start = SystemClock.uptimeMillis();
                        if ((mLastWallpaperTimeoutTime + WALLPAPER_TIMEOUT_RECOVERY)
                                < start) {
                            try {
                                if (DEBUG_WALLPAPER) Slog.v(TAG,
                                        "Waiting for offset complete...");
                                mService.mWindowMap.wait(WALLPAPER_TIMEOUT);
                            } catch (InterruptedException e) {
                            }
                            if (DEBUG_WALLPAPER) Slog.v(TAG, "Offset complete!");
                            if ((start + WALLPAPER_TIMEOUT) < SystemClock.uptimeMillis()) {
                                Slog.i(TAG, "Timeout waiting for wallpaper to offset: "
                                        + wallpaperWin);
                                mLastWallpaperTimeoutTime = start;
                            }
                        }
                        mWaitingOnWallpaper = null;
                    }
                }
            } catch (RemoteException e) {
            }
        }

        return changed;
    }

    void setWindowWallpaperPosition(
            WindowState window, float x, float y, float xStep, float yStep) {
        if (window.mWallpaperX != x || window.mWallpaperY != y)  {
            window.mWallpaperX = x;
            window.mWallpaperY = y;
            window.mWallpaperXStep = xStep;
            window.mWallpaperYStep = yStep;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    void setWindowWallpaperDisplayOffset(WindowState window, int x, int y) {
        if (window.mWallpaperDisplayOffsetX != x || window.mWallpaperDisplayOffsetY != y)  {
            window.mWallpaperDisplayOffsetX = x;
            window.mWallpaperDisplayOffsetY = y;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    Bundle sendWindowWallpaperCommand(
            WindowState window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (window == mWallpaperTarget
                || window == mLowerWallpaperTarget
                || window == mUpperWallpaperTarget) {
            boolean doWait = sync;
            for (int curTokenNdx = mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                final WindowToken token = mWallpaperTokens.get(curTokenNdx);
                token.sendWindowWallpaperCommand(action, x, y, z, extras, sync);
            }

            if (doWait) {
                // TODO: Need to wait for result.
            }
        }

        return null;
    }

    private void updateWallpaperOffsetLocked(WindowState changingTarget, boolean sync) {
        final DisplayContent displayContent = changingTarget.getDisplayContent();
        if (displayContent == null) {
            return;
        }
        final DisplayInfo displayInfo = displayContent.getDisplayInfo();
        final int dw = displayInfo.logicalWidth;
        final int dh = displayInfo.logicalHeight;

        WindowState target = mWallpaperTarget;
        if (target != null) {
            if (target.mWallpaperX >= 0) {
                mLastWallpaperX = target.mWallpaperX;
            } else if (changingTarget.mWallpaperX >= 0) {
                mLastWallpaperX = changingTarget.mWallpaperX;
            }
            if (target.mWallpaperY >= 0) {
                mLastWallpaperY = target.mWallpaperY;
            } else if (changingTarget.mWallpaperY >= 0) {
                mLastWallpaperY = changingTarget.mWallpaperY;
            }
            if (target.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                mLastWallpaperDisplayOffsetX = target.mWallpaperDisplayOffsetX;
            } else if (changingTarget.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                mLastWallpaperDisplayOffsetX = changingTarget.mWallpaperDisplayOffsetX;
            }
            if (target.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                mLastWallpaperDisplayOffsetY = target.mWallpaperDisplayOffsetY;
            } else if (changingTarget.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                mLastWallpaperDisplayOffsetY = changingTarget.mWallpaperDisplayOffsetY;
            }
            if (target.mWallpaperXStep >= 0) {
                mLastWallpaperXStep = target.mWallpaperXStep;
            } else if (changingTarget.mWallpaperXStep >= 0) {
                mLastWallpaperXStep = changingTarget.mWallpaperXStep;
            }
            if (target.mWallpaperYStep >= 0) {
                mLastWallpaperYStep = target.mWallpaperYStep;
            } else if (changingTarget.mWallpaperYStep >= 0) {
                mLastWallpaperYStep = changingTarget.mWallpaperYStep;
            }
        }

        for (int curTokenNdx = mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            mWallpaperTokens.get(curTokenNdx).updateWallpaperOffset(dw, dh, sync);
        }
    }

    void clearLastWallpaperTimeoutTime() {
        mLastWallpaperTimeoutTime = 0;
    }

    void wallpaperCommandComplete(IBinder window) {
        if (mWaitingOnWallpaper != null &&
                mWaitingOnWallpaper.mClient.asBinder() == window) {
            mWaitingOnWallpaper = null;
            mService.mWindowMap.notifyAll();
        }
    }

    void wallpaperOffsetsComplete(IBinder window) {
        if (mWaitingOnWallpaper != null &&
                mWaitingOnWallpaper.mClient.asBinder() == window) {
            mWaitingOnWallpaper = null;
            mService.mWindowMap.notifyAll();
        }
    }

    int getAnimLayerAdjustment() {
        return mWallpaperAnimLayerAdjustment;
    }

    private void findWallpaperTarget(ReadOnlyWindowList windows, FindWallpaperTargetResult result) {
        final WindowAnimator winAnimator = mService.mAnimator;
        result.reset();
        WindowState w = null;
        int windowDetachedI = -1;
        boolean resetTopWallpaper = false;
        boolean inFreeformSpace = false;
        boolean replacing = false;
        boolean keyguardGoingAwayWithWallpaper = false;

        for (int i = windows.size() - 1; i >= 0; i--) {
            w = windows.get(i);
            if ((w.mAttrs.type == TYPE_WALLPAPER)) {
                if (result.topWallpaper == null || resetTopWallpaper) {
                    result.setTopWallpaper(w, i);
                    resetTopWallpaper = false;
                }
                continue;
            }
            resetTopWallpaper = true;
            if (w != winAnimator.mWindowDetachedWallpaper && w.mAppToken != null) {
                // If this window's app token is hidden and not animating,
                // it is of no interest to us.
                if (w.mAppToken.hidden && w.mAppToken.mAppAnimator.animation == null) {
                    if (DEBUG_WALLPAPER) Slog.v(TAG,
                            "Skipping hidden and not animating token: " + w);
                    continue;
                }
            }
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Win #" + i + " " + w + ": isOnScreen="
                    + w.isOnScreen() + " mDrawState=" + w.mWinAnimator.mDrawState);

            if (!inFreeformSpace) {
                TaskStack stack = w.getStack();
                inFreeformSpace = stack != null && stack.mStackId == FREEFORM_WORKSPACE_STACK_ID;
            }

            replacing |= w.mWillReplaceWindow;
            keyguardGoingAwayWithWallpaper |= (w.mAppToken != null
                    && w.mWinAnimator.mKeyguardGoingAwayWithWallpaper);

            final boolean hasWallpaper = (w.mAttrs.flags & FLAG_SHOW_WALLPAPER) != 0;
            if (hasWallpaper && w.isOnScreen() && (mWallpaperTarget == w || w.isDrawFinishedLw())) {
                if (DEBUG_WALLPAPER) Slog.v(TAG, "Found wallpaper target: #" + i + "=" + w);
                result.setWallpaperTarget(w, i);
                if (w == mWallpaperTarget && w.mWinAnimator.isAnimationSet()) {
                    // The current wallpaper target is animating, so we'll look behind it for
                    // another possible target and figure out what is going on later.
                    if (DEBUG_WALLPAPER) Slog.v(TAG,
                            "Win " + w + ": token animating, looking behind.");
                    continue;
                }
                break;
            } else if (w == winAnimator.mWindowDetachedWallpaper) {
                windowDetachedI = i;
            }
        }

        if (result.wallpaperTarget != null) {
            return;
        }

        if (windowDetachedI >= 0) {
            if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG,
                    "Found animating detached wallpaper activity: #" + windowDetachedI + "=" + w);
            result.setWallpaperTarget(w, windowDetachedI);
        } else if (inFreeformSpace || (replacing && mWallpaperTarget != null)) {
            // In freeform mode we set the wallpaper as its own target, so we don't need an
            // additional window to make it visible. When we are replacing a window and there was
            // wallpaper before replacement, we want to keep the window until the new windows fully
            // appear and can determine the visibility, to avoid flickering.
            result.setWallpaperTarget(result.topWallpaper, result.topWallpaperIndex);

        } else if (keyguardGoingAwayWithWallpaper) {
            // If the app is executing an animation because the keyguard is going away (and the
            // keyguard was showing the wallpaper) keep the wallpaper during the animation so it
            // doesn't flicker out by having it be its own target.
            result.setWallpaperTarget(result.topWallpaper, result.topWallpaperIndex);
        }
    }

    /** Updates the target wallpaper if needed and returns true if an update happened. */
    private boolean updateWallpaperWindowsTarget(
            ReadOnlyWindowList windows, FindWallpaperTargetResult result) {

        WindowState wallpaperTarget = result.wallpaperTarget;
        int wallpaperTargetIndex = result.wallpaperTargetIndex;

        if (mWallpaperTarget == wallpaperTarget
                || (mLowerWallpaperTarget != null && mLowerWallpaperTarget == wallpaperTarget)) {

            if (mLowerWallpaperTarget != null) {
                // Is it time to stop animating?
                if (!mLowerWallpaperTarget.isAnimatingLw()
                        || !mUpperWallpaperTarget.isAnimatingLw()) {
                    if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG,
                            "No longer animating wallpaper targets!");
                    mLowerWallpaperTarget = null;
                    mUpperWallpaperTarget = null;
                    mWallpaperTarget = wallpaperTarget;
                    return true;
                }
            }

            return false;
        }

        if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG,
                "New wallpaper target: " + wallpaperTarget + " oldTarget: " + mWallpaperTarget);

        mLowerWallpaperTarget = null;
        mUpperWallpaperTarget = null;

        WindowState oldW = mWallpaperTarget;
        mWallpaperTarget = wallpaperTarget;

        if (wallpaperTarget == null || oldW == null) {
            return true;
        }

        // Now what is happening...  if the current and new targets are animating,
        // then we are in our super special mode!
        boolean oldAnim = oldW.isAnimatingLw();
        boolean foundAnim = wallpaperTarget.isAnimatingLw();
        if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG,
                "New animation: " + foundAnim + " old animation: " + oldAnim);

        if (!foundAnim || !oldAnim) {
            return true;
        }

        int oldI = windows.indexOf(oldW);
        if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG,
                "New i: " + wallpaperTargetIndex + " old i: " + oldI);

        if (oldI < 0) {
            return true;
        }

        final boolean newTargetHidden = wallpaperTarget.mAppToken != null
                && wallpaperTarget.mAppToken.hiddenRequested;
        final boolean oldTargetHidden = oldW.mAppToken != null
                && oldW.mAppToken.hiddenRequested;

        if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG, "Animating wallpapers:" + " old#" + oldI + "="
                + oldW + " hidden=" + oldTargetHidden + " new#" + wallpaperTargetIndex + "="
                + wallpaperTarget + " hidden=" + newTargetHidden);

        // Set the upper and lower wallpaper targets correctly,
        // and make sure that we are positioning the wallpaper below the lower.
        if (wallpaperTargetIndex > oldI) {
            // The new target is on top of the old one.
            if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG, "Found target above old target.");
            mUpperWallpaperTarget = wallpaperTarget;
            mLowerWallpaperTarget = oldW;

            wallpaperTarget = oldW;
            wallpaperTargetIndex = oldI;
        } else {
            // The new target is below the old one.
            if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG, "Found target below old target.");
            mUpperWallpaperTarget = oldW;
            mLowerWallpaperTarget = wallpaperTarget;
        }

        if (newTargetHidden && !oldTargetHidden) {
            if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG, "Old wallpaper still the target.");
            // Use the old target if new target is hidden but old target
            // is not. If they're both hidden, still use the new target.
            mWallpaperTarget = oldW;
        } else if (newTargetHidden == oldTargetHidden
                && !mService.mOpeningApps.contains(wallpaperTarget.mAppToken)
                && (mService.mOpeningApps.contains(oldW.mAppToken)
                || mService.mClosingApps.contains(oldW.mAppToken))) {
            // If they're both hidden (or both not hidden), prefer the one that's currently in
            // opening or closing app list, this allows transition selection logic to better
            // determine the wallpaper status of opening/closing apps.
            mWallpaperTarget = oldW;
        }

        result.setWallpaperTarget(wallpaperTarget, wallpaperTargetIndex);
        return true;
    }

    private boolean updateWallpaperWindowsTargetByLayer(ReadOnlyWindowList windows,
            FindWallpaperTargetResult result) {

        WindowState wallpaperTarget = result.wallpaperTarget;
        int wallpaperTargetIndex = result.wallpaperTargetIndex;
        boolean visible = wallpaperTarget != null;

        if (visible) {
            // The window is visible to the compositor...but is it visible to the user?
            // That is what the wallpaper cares about.
            visible = isWallpaperVisible(wallpaperTarget);
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Wallpaper visibility: " + visible);

            // If the wallpaper target is animating, we may need to copy its layer adjustment.
            // Only do this if we are not transferring between two wallpaper targets.
            mWallpaperAnimLayerAdjustment =
                    (mLowerWallpaperTarget == null && wallpaperTarget.mAppToken != null)
                            ? wallpaperTarget.mAppToken.mAppAnimator.animLayerAdjustment : 0;

            final int maxLayer = (mService.mPolicy.getMaxWallpaperLayer() * TYPE_LAYER_MULTIPLIER)
                    + TYPE_LAYER_OFFSET;

            // Now w is the window we are supposed to be behind...  but we
            // need to be sure to also be behind any of its attached windows,
            // AND any starting window associated with it, AND below the
            // maximum layer the policy allows for wallpapers.
            while (wallpaperTargetIndex > 0) {
                final WindowState wb = windows.get(wallpaperTargetIndex - 1);
                final WindowState wbParentWindow = wb.getParentWindow();
                final WindowState wallpaperParentWindow = wallpaperTarget.getParentWindow();
                if (wb.mBaseLayer < maxLayer
                        && wbParentWindow != wallpaperTarget
                        && (wallpaperParentWindow == null || wbParentWindow != wallpaperParentWindow)
                        && (wb.mAttrs.type != TYPE_APPLICATION_STARTING
                                || wallpaperTarget.mToken == null
                                || wb.mToken != wallpaperTarget.mToken)) {
                    // This window is not related to the previous one in any
                    // interesting way, so stop here.
                    break;
                }
                wallpaperTarget = wb;
                wallpaperTargetIndex--;
            }
        } else {
            if (DEBUG_WALLPAPER) Slog.v(TAG, "No wallpaper target");
        }

        result.setWallpaperTarget(wallpaperTarget, wallpaperTargetIndex);
        return visible;
    }

    private boolean updateWallpaperWindowsPlacement(ReadOnlyWindowList windows,
            WindowState wallpaperTarget, int wallpaperTargetIndex, boolean visible) {

        // TODO(multidisplay): Wallpapers on main screen only.
        final DisplayInfo displayInfo = mService.getDefaultDisplayContentLocked().getDisplayInfo();
        final int dw = displayInfo.logicalWidth;
        final int dh = displayInfo.logicalHeight;

        // Start stepping backwards from here, ensuring that our wallpaper windows are correctly placed.
        boolean changed = false;
        for (int curTokenNdx = mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            final WindowToken token = mWallpaperTokens.get(curTokenNdx);
            changed |= token.updateWallpaperWindowsPlacement(windows, wallpaperTarget,
                    wallpaperTargetIndex, visible, dw, dh, mWallpaperAnimLayerAdjustment);
        }

        return changed;
    }

    boolean adjustWallpaperWindows(ReadOnlyWindowList windows) {
        mService.mRoot.mWallpaperMayChange = false;

        // First find top-most window that has asked to be on top of the wallpaper;
        // all wallpapers go behind it.
        findWallpaperTarget(windows, mFindResults);
        final boolean targetChanged = updateWallpaperWindowsTarget(windows, mFindResults);
        final boolean visible = updateWallpaperWindowsTargetByLayer(windows, mFindResults);
        WindowState wallpaperTarget = mFindResults.wallpaperTarget;
        int wallpaperTargetIndex = mFindResults.wallpaperTargetIndex;

        if (wallpaperTarget == null && mFindResults.topWallpaper != null) {
            // There is no wallpaper target, so it goes at the bottom.
            // We will assume it is the same place as last time, if known.
            wallpaperTarget = mFindResults.topWallpaper;
            wallpaperTargetIndex = mFindResults.topWallpaperIndex + 1;
        } else {
            // Okay i is the position immediately above the wallpaper.
            // Look at what is below it for later.
            wallpaperTarget = wallpaperTargetIndex > 0
                    ? windows.get(wallpaperTargetIndex - 1) : null;
        }

        if (visible) {
            if (mWallpaperTarget.mWallpaperX >= 0) {
                mLastWallpaperX = mWallpaperTarget.mWallpaperX;
                mLastWallpaperXStep = mWallpaperTarget.mWallpaperXStep;
            }
            if (mWallpaperTarget.mWallpaperY >= 0) {
                mLastWallpaperY = mWallpaperTarget.mWallpaperY;
                mLastWallpaperYStep = mWallpaperTarget.mWallpaperYStep;
            }
            if (mWallpaperTarget.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                mLastWallpaperDisplayOffsetX = mWallpaperTarget.mWallpaperDisplayOffsetX;
            }
            if (mWallpaperTarget.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                mLastWallpaperDisplayOffsetY = mWallpaperTarget.mWallpaperDisplayOffsetY;
            }
        }

        final boolean changed = updateWallpaperWindowsPlacement(
                windows, wallpaperTarget, wallpaperTargetIndex, visible);

        if (targetChanged && DEBUG_WALLPAPER_LIGHT)  Slog.d(TAG, "New wallpaper: target="
                + mWallpaperTarget + " lower=" + mLowerWallpaperTarget + " upper="
                + mUpperWallpaperTarget);

        return changed;
    }

    boolean processWallpaperDrawPendingTimeout() {
        if (mWallpaperDrawState == WALLPAPER_DRAW_PENDING) {
            mWallpaperDrawState = WALLPAPER_DRAW_TIMEOUT;
            if (DEBUG_APP_TRANSITIONS || DEBUG_WALLPAPER) Slog.v(TAG,
                    "*** WALLPAPER DRAW TIMEOUT");
            return true;
        }
        return false;
    }

    boolean wallpaperTransitionReady() {
        boolean transitionReady = true;
        boolean wallpaperReady = true;
        for (int curTokenIndex = mWallpaperTokens.size() - 1;
                curTokenIndex >= 0 && wallpaperReady; curTokenIndex--) {
            final WindowToken token = mWallpaperTokens.get(curTokenIndex);
            if (token.hasVisibleNotDrawnWallpaper()) {
                // We've told this wallpaper to be visible, but it is not drawn yet
                wallpaperReady = false;
                if (mWallpaperDrawState != WALLPAPER_DRAW_TIMEOUT) {
                    // wait for this wallpaper until it is drawn or timeout
                    transitionReady = false;
                }
                if (mWallpaperDrawState == WALLPAPER_DRAW_NORMAL) {
                    mWallpaperDrawState = WALLPAPER_DRAW_PENDING;
                    mService.mH.removeMessages(WALLPAPER_DRAW_PENDING_TIMEOUT);
                    mService.mH.sendEmptyMessageDelayed(WALLPAPER_DRAW_PENDING_TIMEOUT,
                            WALLPAPER_DRAW_PENDING_TIMEOUT_DURATION);
                }
                if (DEBUG_APP_TRANSITIONS || DEBUG_WALLPAPER) Slog.v(TAG,
                        "Wallpaper should be visible but has not been drawn yet. " +
                                "mWallpaperDrawState=" + mWallpaperDrawState);
                break;
            }
        }
        if (wallpaperReady) {
            mWallpaperDrawState = WALLPAPER_DRAW_NORMAL;
            mService.mH.removeMessages(WALLPAPER_DRAW_PENDING_TIMEOUT);
        }

        return transitionReady;
    }

    /**
     * Adjusts the wallpaper windows if the input display has a pending wallpaper layout or one of
     * the opening apps should be a wallpaper target.
     */
    void adjustWallpaperWindowsForAppTransitionIfNeeded(DisplayContent dc,
            ArraySet<AppWindowToken> openingApps) {
        boolean adjust = false;
        if ((dc.pendingLayoutChanges & FINISH_LAYOUT_REDO_WALLPAPER) != 0) {
            adjust = true;
        } else {
            for (int i = openingApps.size() - 1; i >= 0; --i) {
                final AppWindowToken token = openingApps.valueAt(i);
                if (token.windowsCanBeWallpaperTarget()) {
                    adjust = true;
                    break;
                }
            }
        }

        if (adjust) {
            dc.adjustWallpaperWindows();
        }
    }

    void addWallpaperToken(WindowToken token) {
        mWallpaperTokens.add(token);
    }

    void removeWallpaperToken(WindowToken token) {
        mWallpaperTokens.remove(token);
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.print("mWallpaperTarget="); pw.println(mWallpaperTarget);
        if (mLowerWallpaperTarget != null || mUpperWallpaperTarget != null) {
            pw.print(prefix); pw.print("mLowerWallpaperTarget="); pw.println(mLowerWallpaperTarget);
            pw.print(prefix); pw.print("mUpperWallpaperTarget="); pw.println(mUpperWallpaperTarget);
        }
        pw.print(prefix); pw.print("mLastWallpaperX="); pw.print(mLastWallpaperX);
        pw.print(" mLastWallpaperY="); pw.println(mLastWallpaperY);
        if (mLastWallpaperDisplayOffsetX != Integer.MIN_VALUE
                || mLastWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
            pw.print(prefix);
            pw.print("mLastWallpaperDisplayOffsetX="); pw.print(mLastWallpaperDisplayOffsetX);
            pw.print(" mLastWallpaperDisplayOffsetY="); pw.println(mLastWallpaperDisplayOffsetY);
        }

        if (mWallpaperAnimLayerAdjustment != 0) {
            pw.println(prefix + "mWallpaperAnimLayerAdjustment=" + mWallpaperAnimLayerAdjustment);
        }
    }

    void dumpTokens(PrintWriter pw, String prefix, boolean dumpAll) {
        if (!mWallpaperTokens.isEmpty()) {
            pw.println();
            pw.print(prefix); pw.println("Wallpaper tokens:");
            for (int i = mWallpaperTokens.size() - 1; i >= 0; i--) {
                WindowToken token = mWallpaperTokens.get(i);
                pw.print(prefix); pw.print("Wallpaper #"); pw.print(i);
                pw.print(' '); pw.print(token);
                if (dumpAll) {
                    pw.println(':');
                    token.dump(pw, "    ");
                } else {
                    pw.println();
                }
            }
        }
    }

    /** Helper class for storing the results of a wallpaper target find operation. */
    final private static class FindWallpaperTargetResult {
        int topWallpaperIndex = 0;
        WindowState topWallpaper = null;
        int wallpaperTargetIndex = 0;
        WindowState wallpaperTarget = null;

        void setTopWallpaper(WindowState win, int index) {
            topWallpaper = win;
            topWallpaperIndex = index;
        }

        void setWallpaperTarget(WindowState win, int index) {
            wallpaperTarget = win;
            wallpaperTargetIndex = index;
        }

        void reset() {
            topWallpaperIndex = 0;
            topWallpaper = null;
            wallpaperTargetIndex = 0;
            wallpaperTarget = null;
        }
    }
}