/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.power.V1_0.PowerHint;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.EventLog;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.WindowManager;
import com.android.internal.util.ArrayUtils;
import com.android.server.EventLogTags;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import static android.app.ActivityManager.StackId.DOCKED_STACK_ID;
import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.MODE_DEFAULT;
import static android.app.AppOpsManager.OP_NONE;
import static android.view.Display.DEFAULT_DISPLAY;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_KEYGUARD;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_SUSTAINED_PERFORMANCE_MODE;
import static android.view.WindowManager.LayoutParams.TYPE_DREAM;
import static android.view.WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
import static android.view.WindowManagerPolicy.FINISH_LAYOUT_REDO_ANIM;
import static android.view.WindowManagerPolicy.FINISH_LAYOUT_REDO_LAYOUT;
import static android.view.WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
import static android.view.WindowManager.LayoutParams.TYPE_WALLPAPER;
import static com.android.server.wm.AppTransition.TRANSIT_UNSET;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_ADD_REMOVE;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_DISPLAY;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_KEEP_SCREEN_ON;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_ORIENTATION;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_POWER;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_STACK;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_VISIBILITY;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_WINDOW_TRACE;
import static com.android.server.wm.WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS;
import static com.android.server.wm.WindowManagerDebugConfig.SHOW_SURFACE_ALLOC;
import static com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_KEEP_SCREEN_ON;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WITH_CLASS_NAME;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;
import static com.android.server.wm.WindowManagerService.H.REPORT_LOSING_FOCUS;
import static com.android.server.wm.WindowManagerService.H.SEND_NEW_CONFIGURATION;
import static com.android.server.wm.WindowManagerService.UPDATE_FOCUS_NORMAL;
import static com.android.server.wm.WindowManagerService.UPDATE_FOCUS_PLACING_SURFACES;
import static com.android.server.wm.WindowManagerService.UPDATE_FOCUS_WILL_PLACE_SURFACES;
import static com.android.server.wm.WindowManagerService.WINDOWS_FREEZING_SCREENS_NONE;
import static com.android.server.wm.WindowManagerService.H.WINDOW_FREEZE_TIMEOUT;
import static com.android.server.wm.WindowManagerService.logSurface;
import static com.android.server.wm.WindowSurfacePlacer.SET_FORCE_HIDING_CHANGED;
import static com.android.server.wm.WindowSurfacePlacer.SET_ORIENTATION_CHANGE_COMPLETE;
import static com.android.server.wm.WindowSurfacePlacer.SET_TURN_ON_SCREEN;
import static com.android.server.wm.WindowSurfacePlacer.SET_UPDATE_ROTATION;
import static com.android.server.wm.WindowSurfacePlacer.SET_WALLPAPER_ACTION_PENDING;
import static com.android.server.wm.WindowSurfacePlacer.SET_WALLPAPER_MAY_CHANGE;

/** Root {@link WindowContainer} for the device. */
class RootWindowContainer extends WindowContainer<DisplayContent> {
    private static final String TAG = TAG_WITH_CLASS_NAME ? "RootWindowContainer" : TAG_WM;

    WindowManagerService mService;

    private boolean mWallpaperForceHidingChanged = false;
    private Object mLastWindowFreezeSource = null;
    private Session mHoldScreen = null;
    private float mScreenBrightness = -1;
    private float mButtonBrightness = -1;
    private long mUserActivityTimeout = -1;
    private boolean mUpdateRotation = false;
    // Following variables are for debugging screen wakelock only.
    // Last window that requires screen wakelock
    WindowState mHoldScreenWindow = null;
    // Last window that obscures all windows below
    WindowState mObscuringWindow = null;
    // Only set while traversing the default display based on its content.
    // Affects the behavior of mirroring on secondary displays.
    private boolean mObscureApplicationContentOnSecondaryDisplays = false;

    private boolean mSustainedPerformanceModeEnabled = false;
    private boolean mSustainedPerformanceModeCurrent = false;

    boolean mWallpaperMayChange = false;
    // During an orientation change, we track whether all windows have rendered
    // at the new orientation, and this will be false from changing orientation until that occurs.
    // For seamless rotation cases this always stays true, as the windows complete their orientation
    // changes 1 by 1 without disturbing global state.
    boolean mOrientationChangeComplete = true;
    boolean mWallpaperActionPending = false;

    private final ArrayList<Integer> mChangedStackList = new ArrayList();

    private final ArrayList<WindowToken> mTmpTokensList = new ArrayList();

    // Collection of binder tokens mapped to their window type we are allowed to create window
    // tokens for but that are not current attached to any display. We need to track this here
    // because a binder token can be added through {@link WindowManagerService#addWindowToken},
    // but we don't know what display windows for the token will be added to until
    // {@link WindowManagerService#addWindow} is called.
    private final HashMap<IBinder, Integer> mUnattachedBinderTokens = new HashMap();

    // State for the RemoteSurfaceTrace system used in testing. If this is enabled SurfaceControl
    // instances will be replaced with an instance that writes a binary representation of all
    // commands to mSurfaceTraceFd.
    boolean mSurfaceTraceEnabled;
    ParcelFileDescriptor mSurfaceTraceFd;
    RemoteEventTrace mRemoteEventTrace;

    private final WindowLayersController mLayersController;
    final WallpaperController mWallpaperController;

    RootWindowContainer(WindowManagerService service) {
        mService = service;
        mLayersController = new WindowLayersController(mService);
        mWallpaperController = new WallpaperController(mService);
    }

    WindowState computeFocusedWindow() {
        final int count = mChildren.size();
        for (int i = 0; i < count; i++) {
            final DisplayContent dc = mChildren.get(i);
            final WindowState win = dc.findFocusedWindow();
            if (win != null) {
                return win;
            }
        }
        return null;
    }

    /**
     * Retrieve the DisplayContent for the specified displayId. Will create a new DisplayContent if
     * there is a Display for the displayId.
     *
     * @param displayId The display the caller is interested in.
     * @return The DisplayContent associated with displayId or null if there is no Display for it.
     */
    DisplayContent getDisplayContentOrCreate(int displayId) {
        DisplayContent dc = getDisplayContent(displayId);

        if (dc == null) {
            final Display display = mService.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                dc = createDisplayContent(display);
            }
        }
        return dc;
    }

    DisplayContent getDisplayContent(int displayId) {
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            final DisplayContent current = mChildren.get(i);
            if (current.getDisplayId() == displayId) {
                return current;
            }
        }
        return null;
    }

    private DisplayContent createDisplayContent(final Display display) {
        final DisplayContent dc = new DisplayContent(display, mService, mLayersController,
                mWallpaperController);
        final int displayId = display.getDisplayId();

        if (DEBUG_DISPLAY) Slog.v(TAG_WM, "Adding display=" + display);
        addChild(dc, null);

        final DisplayInfo displayInfo = dc.getDisplayInfo();
        final Rect rect = new Rect();
        mService.mDisplaySettings.getOverscanLocked(displayInfo.name, displayInfo.uniqueId, rect);
        displayInfo.overscanLeft = rect.left;
        displayInfo.overscanTop = rect.top;
        displayInfo.overscanRight = rect.right;
        displayInfo.overscanBottom = rect.bottom;
        if (mService.mDisplayManagerInternal != null) {
            mService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(
                    displayId, displayInfo);
            mService.configureDisplayPolicyLocked(dc);

            // TODO(multi-display): Create an input channel for each display with touch capability.
            if (displayId == DEFAULT_DISPLAY) {
                dc.mTapDetector = new TaskTapPointerEventListener(
                        mService, dc);
                mService.registerPointerEventListener(dc.mTapDetector);
                mService.registerPointerEventListener(mService.mMousePositionTracker);
            }
        }

        return dc;
    }

    /** Adds the input stack id to the input display id and returns the bounds of the added stack.*/
    Rect addStackToDisplay(int stackId, int displayId, boolean onTop) {
        final DisplayContent dc = getDisplayContent(displayId);
        if (dc == null) {
            Slog.w(TAG_WM, "addStackToDisplay: Trying to add stackId=" + stackId
                    + " to unknown displayId=" + displayId + " callers=" + Debug.getCallers(6));
            return null;
        }

        boolean attachedToDisplay = false;
        TaskStack stack = mService.mStackIdToStack.get(stackId);
        if (stack == null) {
            if (DEBUG_STACK) Slog.d(TAG_WM, "attachStack: stackId=" + stackId);

            stack = dc.getStackById(stackId);
            if (stack != null) {
                // It's already attached to the display...clear mDeferRemoval and move stack to
                // appropriate z-order on display as needed.
                stack.mDeferRemoval = false;
                dc.moveStack(stack, onTop);
                attachedToDisplay = true;
            } else {
                stack = new TaskStack(mService, stackId);
            }

            mService.mStackIdToStack.put(stackId, stack);
            if (stackId == DOCKED_STACK_ID) {
                dc.mDividerControllerLocked.notifyDockedStackExistsChanged(true);
            }
        }

        if (!attachedToDisplay) {
            dc.attachStack(stack, onTop);
        }

        if (stack.getRawFullscreen()) {
            return null;
        }
        final Rect bounds = new Rect();
        stack.getRawBounds(bounds);
        return bounds;
    }

    boolean isLayoutNeeded() {
        final int numDisplays = mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
            final DisplayContent displayContent = mChildren.get(displayNdx);
            if (displayContent.isLayoutNeeded()) {
                return true;
            }
        }
        return false;
    }

    void getWindows(WindowList output) {
        final int count = mChildren.size();
        for (int i = 0; i < count; ++i) {
            final DisplayContent dc = mChildren.get(i);
            dc.getWindows(output);
        }
    }

    void getWindows(WindowList output, boolean visibleOnly, boolean appsOnly) {
        final int numDisplays = mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
            final ReadOnlyWindowList windowList = mChildren.get(displayNdx).getReadOnlyWindowList();
            for (int winNdx = windowList.size() - 1; winNdx >= 0; --winNdx) {
                final WindowState w = windowList.get(winNdx);
                if ((!visibleOnly || w.mWinAnimator.getShown())
                        && (!appsOnly || w.mAppToken != null)) {
                    output.add(w);
                }
            }
        }
    }

    void getWindowsByName(WindowList output, String name) {
        int objectId = 0;
        // See if this is an object ID.
        try {
            objectId = Integer.parseInt(name, 16);
            name = null;
        } catch (RuntimeException e) {
        }
        final int numDisplays = mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
            final ReadOnlyWindowList windowList = mChildren.get(displayNdx).getReadOnlyWindowList();
            for (int winNdx = windowList.size() - 1; winNdx >= 0; --winNdx) {
                final WindowState w = windowList.get(winNdx);
                if (name != null) {
                    if (w.mAttrs.getTitle().toString().contains(name)) {
                        output.add(w);
                    }
                } else if (System.identityHashCode(w) == objectId) {
                    output.add(w);
                }
            }
        }
    }

    WindowState findWindow(int hashCode) {
        final int numDisplays = mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
            final ReadOnlyWindowList windows = mChildren.get(displayNdx).getReadOnlyWindowList();
            final int numWindows = windows.size();
            for (int winNdx = 0; winNdx < numWindows; ++winNdx) {
                final WindowState w = windows.get(winNdx);
                if (System.identityHashCode(w) == hashCode) {
                    return w;
                }
            }
        }

        return null;
    }

    /** Return the window token associated with the input binder token on the input display */
    WindowToken getWindowToken(IBinder binder, DisplayContent dc) {
        final WindowToken token = dc.getWindowToken(binder);
        if (token != null) {
            return token;
        }

        // There is no window token mapped to the binder on the display. Create and map a window
        // token if it is currently allowed.
        if (!mUnattachedBinderTokens.containsKey(binder)) {
            return null;
        }

        final int type = mUnattachedBinderTokens.get(binder);
        return new WindowToken(mService, binder, type, true, dc);
    }

    /** Returns all window tokens mapped to the input binder. */
    ArrayList<WindowToken> getWindowTokens(IBinder binder) {
        mTmpTokensList.clear();
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            final DisplayContent dc = mChildren.get(i);
            final WindowToken token = dc.getWindowToken(binder);
            if (token != null) {
                mTmpTokensList.add(token);
            }
        }
        return mTmpTokensList;
    }

    /**
     * Returns the app window token for the input binder if it exist in the system.
     * NOTE: Only one AppWindowToken is allowed to exist in the system for a binder token, since
     * AppWindowToken represents an activity which can only exist on one display.
     */
    AppWindowToken getAppWindowToken(IBinder binder) {
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            final DisplayContent dc = mChildren.get(i);
            final AppWindowToken atoken = dc.getAppWindowToken(binder);
            if (atoken != null) {
                return atoken;
            }
        }
        return null;
    }

    /** Returns the display object the input window token is currently mapped on. */
    DisplayContent getWindowTokenDisplay(WindowToken token) {
        if (token == null) {
            return null;
        }

        for (int i = mChildren.size() - 1; i >= 0; --i) {
            final DisplayContent dc = mChildren.get(i);
            final WindowToken current = dc.getWindowToken(token.token);
            if (current == token) {
                return dc;
            }
        }

        return null;
    }

    void addWindowToken(IBinder binder, int type) {
        if (mUnattachedBinderTokens.containsKey(binder)) {
            Slog.w(TAG_WM, "addWindowToken: Attempted to add existing binder token: " + binder);
            return;
        }

        final ArrayList<WindowToken> tokens = getWindowTokens(binder);

        if (!tokens.isEmpty()) {
            Slog.w(TAG_WM, "addWindowToken: Attempted to add binder token: " + binder
                    + " for already created window tokens: " + tokens);
            return;
        }

        mUnattachedBinderTokens.put(binder, type);

        // TODO(multi-display): By default we add this to the default display, but maybe we
        // should provide an API for a token to be added to any display?
        final DisplayContent dc = getDisplayContent(DEFAULT_DISPLAY);
        final WindowToken token = new WindowToken(mService, binder, type, true, dc);
        if (type == TYPE_WALLPAPER) {
            dc.mWallpaperController.addWallpaperToken(token);
        }
    }

    ArrayList<WindowToken> removeWindowToken(IBinder binder) {
        mUnattachedBinderTokens.remove(binder);

        mTmpTokensList.clear();
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            final DisplayContent dc = mChildren.get(i);
            final WindowToken token = dc.removeWindowToken(binder);
            if (token != null) {
                mTmpTokensList.add(token);
            }
        }
        return mTmpTokensList;
    }

    /**
     * Removed the mapping to the input binder for the system if it no longer as a window token
     * associated with it on any display.
     */
    void removeWindowTokenIfPossible(IBinder binder) {
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            final DisplayContent dc = mChildren.get(i);
            final WindowToken token = dc.getWindowToken(binder);
            if (token != null) {
                return;
            }
        }

        mUnattachedBinderTokens.remove(binder);
    }

    void removeAppToken(IBinder binder) {
        final ArrayList<WindowToken> removedTokens = removeWindowToken(binder);
        if (removedTokens == null || removedTokens.isEmpty()) {
            Slog.w(TAG_WM, "removeAppToken: Attempted to remove non-existing token: " + binder);
            return;
        }

        for (int i = removedTokens.size() - 1; i >= 0; --i) {
            WindowToken wtoken = removedTokens.get(i);
            AppWindowToken appToken = wtoken.asAppWindowToken();

            if (appToken == null) {
                Slog.w(TAG_WM,
                        "Attempted to remove non-App token: " + binder + " wtoken=" + wtoken);
                continue;
            }

            AppWindowToken startingToken = null;

            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG_WM, "Removing app token: " + appToken);

            boolean delayed = appToken.setVisibility(null, false, TRANSIT_UNSET, true,
                    appToken.voiceInteraction);

            mService.mOpeningApps.remove(appToken);
            appToken.waitingToShow = false;
            if (mService.mClosingApps.contains(appToken)) {
                delayed = true;
            } else if (mService.mAppTransition.isTransitionSet()) {
                mService.mClosingApps.add(appToken);
                delayed = true;
            }

            if (DEBUG_APP_TRANSITIONS) Slog.v(TAG_WM, "Removing app " + appToken
                    + " delayed=" + delayed
                    + " animation=" + appToken.mAppAnimator.animation
                    + " animating=" + appToken.mAppAnimator.animating);

            if (DEBUG_ADD_REMOVE || DEBUG_TOKEN_MOVEMENT) Slog.v(TAG_WM, "removeAppToken: "
                    + appToken + " delayed=" + delayed + " Callers=" + Debug.getCallers(4));

            final TaskStack stack = appToken.mTask.mStack;
            if (delayed && !appToken.isEmpty()) {
                // set the token aside because it has an active animation to be finished
                if (DEBUG_ADD_REMOVE || DEBUG_TOKEN_MOVEMENT) Slog.v(TAG_WM,
                        "removeAppToken make exiting: " + appToken);
                stack.mExitingAppTokens.add(appToken);
                appToken.mIsExiting = true;
            } else {
                // Make sure there is no animation running on this token, so any windows associated
                // with it will be removed as soon as their animations are complete
                appToken.mAppAnimator.clearAnimation();
                appToken.mAppAnimator.animating = false;
                appToken.removeIfPossible();
            }

            appToken.removed = true;
            if (appToken.startingData != null) {
                startingToken = appToken;
            }
            appToken.stopFreezingScreen(true, true);
            if (mService.mFocusedApp == appToken) {
                if (DEBUG_FOCUS_LIGHT) Slog.v(TAG_WM, "Removing focused app token:" + appToken);
                mService.mFocusedApp = null;
                mService.updateFocusedWindowLocked(
                        UPDATE_FOCUS_NORMAL, true /*updateInputWindows*/);
                mService.mInputMonitor.setFocusedAppLw(null);
            }

            if (!delayed) {
                appToken.updateReportedVisibilityLocked();
            }

            // Will only remove if startingToken non null.
            mService.scheduleRemoveStartingWindowLocked(startingToken);
        }
    }

    // TODO: Users would have their own window containers under the display container?
    void switchUser() {
        final int count = mChildren.size();
        for (int i = 0; i < count; ++i) {
            final DisplayContent dc = mChildren.get(i);
            dc.switchUser();
        }
    }

    /**
     * Set new display override config and return array of ids of stacks that were changed during
     * update. If called for the default display, global configuration will also be updated.
     */
    int[] setDisplayOverrideConfigurationIfNeeded(Configuration newConfiguration, int displayId) {
        final DisplayContent displayContent = getDisplayContent(displayId);
        if (displayContent == null) {
            throw new IllegalArgumentException("Display not found for id: " + displayId);
        }

        final Configuration currentConfig = displayContent.getOverrideConfiguration();
        final boolean configChanged = currentConfig.diff(newConfiguration) != 0;
        if (!configChanged) {
            return null;
        }
        displayContent.onOverrideConfigurationChanged(currentConfig);

        if (displayId == DEFAULT_DISPLAY) {
            // Override configuration of the default display duplicates global config. In this case
            // we also want to update the global config.
            return setGlobalConfigurationIfNeeded(newConfiguration);
        } else {
            return updateStackBoundsAfterConfigChange(displayId);
        }
    }

    private int[] setGlobalConfigurationIfNeeded(Configuration newConfiguration) {
        final boolean configChanged = getConfiguration().diff(newConfiguration) != 0;
        if (!configChanged) {
            return null;
        }
        onConfigurationChanged(newConfiguration);
        return updateStackBoundsAfterConfigChange();
    }

    @Override
    void onConfigurationChanged(Configuration newParentConfig) {
        prepareFreezingTaskBounds();
        super.onConfigurationChanged(newParentConfig);

        mService.mPolicy.onConfigurationChanged();
    }

    /**
     * Callback used to trigger bounds update after configuration change and get ids of stacks whose
     * bounds were updated.
     */
    private int[] updateStackBoundsAfterConfigChange() {
        mChangedStackList.clear();

        final int numDisplays = mChildren.size();
        for (int i = 0; i < numDisplays; ++i) {
            final DisplayContent dc = mChildren.get(i);
            dc.updateStackBoundsAfterConfigChange(mChangedStackList);
        }

        return mChangedStackList.isEmpty() ? null : ArrayUtils.convertToIntArray(mChangedStackList);
    }

    /** Same as {@link #updateStackBoundsAfterConfigChange()} but only for a specific display. */
    private int[] updateStackBoundsAfterConfigChange(int displayId) {
        mChangedStackList.clear();

        final DisplayContent dc = getDisplayContent(displayId);
        dc.updateStackBoundsAfterConfigChange(mChangedStackList);

        return mChangedStackList.isEmpty() ? null : ArrayUtils.convertToIntArray(mChangedStackList);
    }

    private void prepareFreezingTaskBounds() {
        for (int i = mChildren.size() - 1; i >= 0; i--) {
            mChildren.get(i).prepareFreezingTaskBounds();
        }
    }

    void setSecureSurfaceState(int userId, boolean disabled) {
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            final ReadOnlyWindowList windows = mChildren.get(i).getReadOnlyWindowList();
            for (int winNdx = windows.size() - 1; winNdx >= 0; --winNdx) {
                final WindowState win = windows.get(winNdx);
                if (win.mHasSurface && userId == UserHandle.getUserId(win.mOwnerUid)) {
                    win.mWinAnimator.setSecureLocked(disabled);
                }
            }
        }
    }

    void updateAppOpsState() {
        final int count = mChildren.size();
        for (int i = 0; i < count; ++i) {
            final ReadOnlyWindowList windows = mChildren.get(i).getReadOnlyWindowList();
            final int numWindows = windows.size();
            for (int winNdx = 0; winNdx < numWindows; ++winNdx) {
                final WindowState win = windows.get(winNdx);
                if (win.mAppOp == OP_NONE) {
                    continue;
                }
                final int mode = mService.mAppOps.checkOpNoThrow(win.mAppOp, win.getOwningUid(),
                        win.getOwningPackage());
                win.setAppOpVisibilityLw(mode == MODE_ALLOWED || mode == MODE_DEFAULT);
            }
        }
    }

    boolean canShowStrictModeViolation(int pid) {
        final int count = mChildren.size();
        for (int i = 0; i < count; ++i) {
            final ReadOnlyWindowList windows = mChildren.get(i).getReadOnlyWindowList();
            final int numWindows = windows.size();
            for (int winNdx = 0; winNdx < numWindows; ++winNdx) {
                final WindowState ws = windows.get(winNdx);
                if (ws.mSession.mPid == pid && ws.isVisibleLw()) {
                    return true;
                }
            }
        }
        return false;
    }

    void closeSystemDialogs(String reason) {
        final int count = mChildren.size();
        for (int i = 0; i < count; ++i) {
            final ReadOnlyWindowList windows = mChildren.get(i).getReadOnlyWindowList();
            final int numWindows = windows.size();
            for (int j = 0; j < numWindows; ++j) {
                final WindowState w = windows.get(j);
                if (w.mHasSurface) {
                    try {
                        w.mClient.closeSystemDialogs(reason);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    void removeReplacedWindows() {
        if (SHOW_TRANSACTIONS) Slog.i(TAG, ">>> OPEN TRANSACTION removeReplacedWindows");
        mService.openSurfaceTransaction();
        try {
            for (int i = mChildren.size() - 1; i >= 0; i--) {
                DisplayContent dc = mChildren.get(i);
                final ReadOnlyWindowList windows = mChildren.get(i).getReadOnlyWindowList();
                for (int j = windows.size() - 1; j >= 0; j--) {
                    final WindowState win = windows.get(j);
                    final AppWindowToken aToken = win.mAppToken;
                    if (aToken != null) {
                        aToken.removeReplacedWindowIfNeeded(win);
                    }
                }
            }
        } finally {
            mService.closeSurfaceTransaction();
            if (SHOW_TRANSACTIONS) Slog.i(TAG, "<<< CLOSE TRANSACTION removeReplacedWindows");
        }
    }

    boolean hasPendingLayoutChanges(WindowAnimator animator) {
        boolean hasChanges = false;

        final int count = mChildren.size();
        for (int i = 0; i < count; ++i) {
            final DisplayContent dc = mChildren.get(i);
            final int pendingChanges = animator.getPendingLayoutChanges(dc.getDisplayId());
            if ((pendingChanges & FINISH_LAYOUT_REDO_WALLPAPER) != 0) {
                animator.mBulkUpdateParams |= SET_WALLPAPER_ACTION_PENDING;
            }
            if (pendingChanges != 0) {
                hasChanges = true;
            }
        }

        return hasChanges;
    }

    void updateInputWindows(InputMonitor inputMonitor, WindowState inputFocus, boolean inDrag) {
        final int count = mChildren.size();
        for (int i = 0; i < count; ++i) {
            final DisplayContent dc = mChildren.get(i);
            dc.updateInputWindows(inputMonitor, inputFocus, inDrag);
        }
    }

    boolean reclaimSomeSurfaceMemory(WindowStateAnimator winAnimator, String operation,
            boolean secure) {
        final WindowSurfaceController surfaceController = winAnimator.mSurfaceController;
        boolean leakedSurface = false;
        boolean killedApps = false;

        EventLog.writeEvent(EventLogTags.WM_NO_SURFACE_MEMORY, winAnimator.mWin.toString(),
                winAnimator.mSession.mPid, operation);

        final long callingIdentity = Binder.clearCallingIdentity();
        try {
            // There was some problem...first, do a sanity check of the window list to make sure
            // we haven't left any dangling surfaces around.

            Slog.i(TAG_WM, "Out of memory for surface!  Looking for leaks...");
            final int numDisplays = mChildren.size();
            for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
                leakedSurface |= mChildren.get(displayNdx).destroyLeakedSurfaces();
            }

            if (!leakedSurface) {
                Slog.w(TAG_WM, "No leaked surfaces; killing applications!");
                final SparseIntArray pidCandidates = new SparseIntArray();
                for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
                    final ReadOnlyWindowList windows =
                            mChildren.get(displayNdx).getReadOnlyWindowList();
                    final int numWindows = windows.size();
                    for (int winNdx = 0; winNdx < numWindows; ++winNdx) {
                        final WindowState ws = windows.get(winNdx);
                        if (mService.mForceRemoves.contains(ws)) {
                            continue;
                        }
                        final WindowStateAnimator wsa = ws.mWinAnimator;
                        if (wsa.mSurfaceController != null) {
                            pidCandidates.append(wsa.mSession.mPid, wsa.mSession.mPid);
                        }
                    }

                    if (pidCandidates.size() > 0) {
                        int[] pids = new int[pidCandidates.size()];
                        for (int i = 0; i < pids.length; i++) {
                            pids[i] = pidCandidates.keyAt(i);
                        }
                        try {
                            if (mService.mActivityManager.killPids(pids, "Free memory", secure)) {
                                killedApps = true;
                            }
                        } catch (RemoteException e) {
                        }
                    }
                }
            }

            if (leakedSurface || killedApps) {
                // We managed to reclaim some memory, so get rid of the trouble surface and ask the
                // app to request another one.
                Slog.w(TAG_WM,
                        "Looks like we have reclaimed some memory, clearing surface for retry.");
                if (surfaceController != null) {
                    if (SHOW_TRANSACTIONS || SHOW_SURFACE_ALLOC) logSurface(winAnimator.mWin,
                            "RECOVER DESTROY", false);
                    winAnimator.destroySurface();
                    mService.scheduleRemoveStartingWindowLocked(winAnimator.mWin.mAppToken);
                }

                try {
                    winAnimator.mWin.mClient.dispatchGetNewSurface();
                } catch (RemoteException e) {
                }
            }
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }

        return leakedSurface || killedApps;
    }

    // "Something has changed!  Let's make it correct now."
    // TODO: Super crazy long method that should be broken down...
    void performSurfacePlacement(boolean recoveringMemory) {
        if (DEBUG_WINDOW_TRACE) Slog.v(TAG, "performSurfacePlacementInner: entry. Called by "
                + Debug.getCallers(3));

        int i;
        boolean updateInputWindowsNeeded = false;

        if (mService.mFocusMayChange) {
            mService.mFocusMayChange = false;
            updateInputWindowsNeeded = mService.updateFocusedWindowLocked(
                    UPDATE_FOCUS_WILL_PLACE_SURFACES, false /*updateInputWindows*/);
        }

        // Initialize state of exiting tokens.
        final int numDisplays = mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
            final DisplayContent displayContent = mChildren.get(displayNdx);
            for (i = displayContent.mExitingTokens.size() - 1; i >= 0; i--) {
                displayContent.mExitingTokens.get(i).hasVisible = false;
            }
        }

        for (int stackNdx = mService.mStackIdToStack.size() - 1; stackNdx >= 0; --stackNdx) {
            // Initialize state of exiting applications.
            final AppTokenList exitingAppTokens =
                    mService.mStackIdToStack.valueAt(stackNdx).mExitingAppTokens;
            for (int tokenNdx = exitingAppTokens.size() - 1; tokenNdx >= 0; --tokenNdx) {
                exitingAppTokens.get(tokenNdx).hasVisible = false;
            }
        }

        mHoldScreen = null;
        mScreenBrightness = -1;
        mButtonBrightness = -1;
        mUserActivityTimeout = -1;
        mObscureApplicationContentOnSecondaryDisplays = false;
        mSustainedPerformanceModeCurrent = false;
        mService.mTransactionSequence++;

        // TODO(multi-display):
        final DisplayContent defaultDisplay = mService.getDefaultDisplayContentLocked();
        final DisplayInfo defaultInfo = defaultDisplay.getDisplayInfo();
        final int defaultDw = defaultInfo.logicalWidth;
        final int defaultDh = defaultInfo.logicalHeight;

        if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG,
                ">>> OPEN TRANSACTION performLayoutAndPlaceSurfaces");
        mService.openSurfaceTransaction();
        try {
            applySurfaceChangesTransaction(recoveringMemory, defaultDw, defaultDh);
        } catch (RuntimeException e) {
            Slog.wtf(TAG, "Unhandled exception in Window Manager", e);
        } finally {
            mService.closeSurfaceTransaction();
            if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG,
                    "<<< CLOSE TRANSACTION performLayoutAndPlaceSurfaces");
        }

        final WindowSurfacePlacer surfacePlacer = mService.mWindowPlacerLocked;

        // If we are ready to perform an app transition, check through all of the app tokens to be
        // shown and see if they are ready to go.
        if (mService.mAppTransition.isReady()) {
            defaultDisplay.pendingLayoutChanges |=
                    surfacePlacer.handleAppTransitionReadyLocked();
            if (DEBUG_LAYOUT_REPEATS)
                surfacePlacer.debugLayoutRepeats("after handleAppTransitionReadyLocked",
                        defaultDisplay.pendingLayoutChanges);
        }

        if (!mService.mAnimator.mAppWindowAnimating && mService.mAppTransition.isRunning()) {
            // We have finished the animation of an app transition. To do this, we have delayed a
            // lot of operations like showing and hiding apps, moving apps in Z-order, etc. The app
            // token list reflects the correct Z-order, but the window list may now be out of sync
            // with it. So here we will just rebuild the entire app window list. Fun!
            defaultDisplay.pendingLayoutChanges |=
                    mService.handleAnimatingStoppedAndTransitionLocked();
            if (DEBUG_LAYOUT_REPEATS)
                surfacePlacer.debugLayoutRepeats("after handleAnimStopAndXitionLock",
                        defaultDisplay.pendingLayoutChanges);
        }

        if (mWallpaperForceHidingChanged && defaultDisplay.pendingLayoutChanges == 0
                && !mService.mAppTransition.isReady()) {
            // At this point, there was a window with a wallpaper that was force hiding other
            // windows behind it, but now it is going away. This may be simple -- just animate away
            // the wallpaper and its window -- or it may be hard -- the wallpaper now needs to be
            // shown behind something that was hidden.
            defaultDisplay.pendingLayoutChanges |= FINISH_LAYOUT_REDO_LAYOUT;
            if (DEBUG_LAYOUT_REPEATS) surfacePlacer.debugLayoutRepeats(
                    "after animateAwayWallpaperLocked", defaultDisplay.pendingLayoutChanges);
        }
        mWallpaperForceHidingChanged = false;

        if (mWallpaperMayChange) {
            if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG, "Wallpaper may change!  Adjusting");
            defaultDisplay.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
            if (DEBUG_LAYOUT_REPEATS) surfacePlacer.debugLayoutRepeats("WallpaperMayChange",
                    defaultDisplay.pendingLayoutChanges);
        }

        if (mService.mFocusMayChange) {
            mService.mFocusMayChange = false;
            if (mService.updateFocusedWindowLocked(UPDATE_FOCUS_PLACING_SURFACES,
                    false /*updateInputWindows*/)) {
                updateInputWindowsNeeded = true;
                defaultDisplay.pendingLayoutChanges |= FINISH_LAYOUT_REDO_ANIM;
            }
        }

        if (isLayoutNeeded()) {
            defaultDisplay.pendingLayoutChanges |= FINISH_LAYOUT_REDO_LAYOUT;
            if (DEBUG_LAYOUT_REPEATS) surfacePlacer.debugLayoutRepeats("mLayoutNeeded",
                    defaultDisplay.pendingLayoutChanges);
        }

        for (i = mService.mResizingWindows.size() - 1; i >= 0; i--) {
            WindowState win = mService.mResizingWindows.get(i);
            if (win.mAppFreezing) {
                // Don't remove this window until rotation has completed.
                continue;
            }
            // Discard the saved surface if window size is changed, it can't be reused.
            if (win.mAppToken != null) {
                win.mAppToken.destroySavedSurfaces();
            }
            win.reportResized();
            mService.mResizingWindows.remove(i);
        }

        if (DEBUG_ORIENTATION && mService.mDisplayFrozen) Slog.v(TAG,
                "With display frozen, orientationChangeComplete=" + mOrientationChangeComplete);
        if (mOrientationChangeComplete) {
            if (mService.mWindowsFreezingScreen != WINDOWS_FREEZING_SCREENS_NONE) {
                mService.mWindowsFreezingScreen = WINDOWS_FREEZING_SCREENS_NONE;
                mService.mLastFinishedFreezeSource = mLastWindowFreezeSource;
                mService.mH.removeMessages(WINDOW_FREEZE_TIMEOUT);
            }
            mService.stopFreezingDisplayLocked();
        }

        // Destroy the surface of any windows that are no longer visible.
        boolean wallpaperDestroyed = false;
        i = mService.mDestroySurface.size();
        if (i > 0) {
            do {
                i--;
                WindowState win = mService.mDestroySurface.get(i);
                win.mDestroying = false;
                if (mService.mInputMethodWindow == win) {
                    mService.mInputMethodWindow = null;
                }
                if (win.getDisplayContent().mWallpaperController.isWallpaperTarget(win)) {
                    wallpaperDestroyed = true;
                }
                win.destroyOrSaveSurface();
            } while (i > 0);
            mService.mDestroySurface.clear();
        }

        // Time to remove any exiting tokens?
        for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
            final DisplayContent displayContent = mChildren.get(displayNdx);
            ArrayList<WindowToken> exitingTokens = displayContent.mExitingTokens;
            for (i = exitingTokens.size() - 1; i >= 0; i--) {
                WindowToken token = exitingTokens.get(i);
                if (!token.hasVisible) {
                    exitingTokens.remove(i);
                    if (token.windowType == TYPE_WALLPAPER) {
                        displayContent.mWallpaperController.removeWallpaperToken(token);
                    }
                }
            }
        }

        // Time to remove any exiting applications?
        for (int stackNdx = mService.mStackIdToStack.size() - 1; stackNdx >= 0; --stackNdx) {
            // Initialize state of exiting applications.
            final AppTokenList exitingAppTokens =
                    mService.mStackIdToStack.valueAt(stackNdx).mExitingAppTokens;
            for (i = exitingAppTokens.size() - 1; i >= 0; i--) {
                final AppWindowToken token = exitingAppTokens.get(i);
                if (!token.hasVisible && !mService.mClosingApps.contains(token) &&
                        (!token.mIsExiting || token.isEmpty())) {
                    // Make sure there is no animation running on this token, so any windows
                    // associated with it will be removed as soon as their animations are complete
                    token.mAppAnimator.clearAnimation();
                    token.mAppAnimator.animating = false;
                    if (DEBUG_ADD_REMOVE || DEBUG_TOKEN_MOVEMENT) Slog.v(TAG,
                            "performLayout: App token exiting now removed" + token);
                    token.removeIfPossible();
                }
            }
        }

        if (wallpaperDestroyed) {
            defaultDisplay.pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
            defaultDisplay.setLayoutNeeded();
        }

        for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
            final DisplayContent displayContent = mChildren.get(displayNdx);
            if (displayContent.pendingLayoutChanges != 0) {
                displayContent.setLayoutNeeded();
            }
        }

        // Finally update all input windows now that the window changes have stabilized.
        mService.mInputMonitor.updateInputWindowsLw(true /*force*/);

        mService.setHoldScreenLocked(mHoldScreen);
        if (!mService.mDisplayFrozen) {
            if (mScreenBrightness < 0 || mScreenBrightness > 1.0f) {
                mService.mPowerManagerInternal.setScreenBrightnessOverrideFromWindowManager(-1);
            } else {
                mService.mPowerManagerInternal.setScreenBrightnessOverrideFromWindowManager(
                        toBrightnessOverride(mScreenBrightness));
            }
            if (mButtonBrightness < 0 || mButtonBrightness > 1.0f) {
                mService.mPowerManagerInternal.setButtonBrightnessOverrideFromWindowManager(-1);
            } else {
                mService.mPowerManagerInternal.setButtonBrightnessOverrideFromWindowManager(
                        toBrightnessOverride(mButtonBrightness));
            }
            mService.mPowerManagerInternal.setUserActivityTimeoutOverrideFromWindowManager(
                    mUserActivityTimeout);
        }

        if (mSustainedPerformanceModeCurrent != mSustainedPerformanceModeEnabled) {
            mSustainedPerformanceModeEnabled = mSustainedPerformanceModeCurrent;
            mService.mPowerManagerInternal.powerHint(
                    PowerHint.SUSTAINED_PERFORMANCE,
                    (mSustainedPerformanceModeEnabled ? 1 : 0));
        }

        if (mService.mTurnOnScreen) {
            if (mService.mAllowTheaterModeWakeFromLayout
                    || Settings.Global.getInt(mService.mContext.getContentResolver(),
                    Settings.Global.THEATER_MODE_ON, 0) == 0) {
                if (DEBUG_VISIBILITY || DEBUG_POWER) {
                    Slog.v(TAG, "Turning screen on after layout!");
                }
                mService.mPowerManager.wakeUp(SystemClock.uptimeMillis(),
                        "android.server.wm:TURN_ON");
            }
            mService.mTurnOnScreen = false;
        }

        if (mUpdateRotation) {
            if (DEBUG_ORIENTATION) Slog.d(TAG, "Performing post-rotate rotation");
            // TODO(multi-display): Update rotation for different displays separately.
            final int displayId = defaultDisplay.getDisplayId();
            if (mService.updateRotationUncheckedLocked(false, displayId)) {
                mService.mH.obtainMessage(SEND_NEW_CONFIGURATION, displayId).sendToTarget();
            } else {
                mUpdateRotation = false;
            }
        }

        if (mService.mWaitingForDrawnCallback != null ||
                (mOrientationChangeComplete && !defaultDisplay.isLayoutNeeded()
                        && !mUpdateRotation)) {
            mService.checkDrawnWindowsLocked();
        }

        final int N = mService.mPendingRemove.size();
        if (N > 0) {
            if (mService.mPendingRemoveTmp.length < N) {
                mService.mPendingRemoveTmp = new WindowState[N+10];
            }
            mService.mPendingRemove.toArray(mService.mPendingRemoveTmp);
            mService.mPendingRemove.clear();
            ArrayList<DisplayContent> displayList = new ArrayList();
            for (i = 0; i < N; i++) {
                final WindowState w = mService.mPendingRemoveTmp[i];
                w.removeImmediately();
                final DisplayContent displayContent = w.getDisplayContent();
                if (displayContent != null && !displayList.contains(displayContent)) {
                    displayList.add(displayContent);
                }
            }

            for (int j = displayList.size() - 1; j >= 0; --j) {
                final DisplayContent dc = displayList.get(j);
                dc.assignWindowLayers(true /*setLayoutNeeded*/);
            }
        }

        // Remove all deferred displays stacks, tasks, and activities.
        for (int displayNdx = mChildren.size() - 1; displayNdx >= 0; --displayNdx) {
            mChildren.get(displayNdx).checkCompleteDeferredRemoval();
        }

        if (updateInputWindowsNeeded) {
            mService.mInputMonitor.updateInputWindowsLw(false /*force*/);
        }
        mService.setFocusTaskRegionLocked();

        // Check to see if we are now in a state where the screen should
        // be enabled, because the window obscured flags have changed.
        mService.enableScreenIfNeededLocked();

        mService.scheduleAnimationLocked();
        mService.mWindowPlacerLocked.destroyPendingSurfaces();

        if (DEBUG_WINDOW_TRACE) Slog.e(TAG,
                "performSurfacePlacementInner exit: animating=" + mService.mAnimator.isAnimating());
    }

    private void applySurfaceChangesTransaction(boolean recoveringMemory, int defaultDw,
            int defaultDh) {
        mHoldScreenWindow = null;
        mObscuringWindow = null;

        if (mService.mWatermark != null) {
            mService.mWatermark.positionSurface(defaultDw, defaultDh);
        }
        if (mService.mStrictModeFlash != null) {
            mService.mStrictModeFlash.positionSurface(defaultDw, defaultDh);
        }
        if (mService.mCircularDisplayMask != null) {
            mService.mCircularDisplayMask.positionSurface(defaultDw, defaultDh, mService.mRotation);
        }
        if (mService.mEmulatorDisplayOverlay != null) {
            mService.mEmulatorDisplayOverlay.positionSurface(defaultDw, defaultDh,
                    mService.mRotation);
        }

        boolean focusDisplayed = false;

        final int count = mChildren.size();
        for (int j = 0; j < count; ++j) {
            final DisplayContent dc = mChildren.get(j);
            focusDisplayed |= dc.applySurfaceChangesTransaction(recoveringMemory);
        }

        if (focusDisplayed) {
            mService.mH.sendEmptyMessage(REPORT_LOSING_FOCUS);
        }

        // Give the display manager a chance to adjust properties like display rotation if it needs
        // to.
        mService.mDisplayManagerInternal.performTraversalInTransactionFromWindowManager();
    }

    /**
     * @param w WindowState this method is applied to.
     * @param obscured True if there is a window on top of this obscuring the display.
     * @param syswin System window?
     * @return True when the display contains content to show the user. When false, the display
     *          manager may choose to mirror or blank the display.
     */
    boolean handleNotObscuredLocked(WindowState w, boolean obscured, boolean syswin) {
        final WindowManager.LayoutParams attrs = w.mAttrs;
        final int attrFlags = attrs.flags;
        final boolean canBeSeen = w.isDisplayedLw();
        final int privateflags = attrs.privateFlags;
        boolean displayHasContent = false;

        if (w.mHasSurface && canBeSeen) {
            if ((attrFlags & FLAG_KEEP_SCREEN_ON) != 0) {
                mHoldScreen = w.mSession;
                mHoldScreenWindow = w;
            } else if (DEBUG_KEEP_SCREEN_ON && w == mService.mLastWakeLockHoldingWindow) {
                Slog.d(TAG_KEEP_SCREEN_ON, "handleNotObscuredLocked: " + w + " was holding "
                        + "screen wakelock but no longer has FLAG_KEEP_SCREEN_ON!!! called by"
                        + Debug.getCallers(10));
            }
            if (!syswin && w.mAttrs.screenBrightness >= 0 && mScreenBrightness < 0) {
                mScreenBrightness = w.mAttrs.screenBrightness;
            }
            if (!syswin && w.mAttrs.buttonBrightness >= 0 && mButtonBrightness < 0) {
                mButtonBrightness = w.mAttrs.buttonBrightness;
            }
            if (!syswin && w.mAttrs.userActivityTimeout >= 0 && mUserActivityTimeout < 0) {
                mUserActivityTimeout = w.mAttrs.userActivityTimeout;
            }

            final int type = attrs.type;
            // This function assumes that the contents of the default display are processed first
            // before secondary displays.
            final DisplayContent displayContent = w.getDisplayContent();
            if (displayContent != null && displayContent.isDefaultDisplay) {
                // While a dream or keyguard is showing, obscure ordinary application content on
                // secondary displays (by forcibly enabling mirroring unless there is other content
                // we want to show) but still allow opaque keyguard dialogs to be shown.
                if (type == TYPE_DREAM || (attrs.privateFlags & PRIVATE_FLAG_KEYGUARD) != 0) {
                    mObscureApplicationContentOnSecondaryDisplays = true;
                }
                displayHasContent = true;
            } else if (displayContent != null &&
                    (!mObscureApplicationContentOnSecondaryDisplays
                            || (obscured && type == TYPE_KEYGUARD_DIALOG))) {
                // Allow full screen keyguard presentation dialogs to be seen.
                displayHasContent = true;
            }
            if ((privateflags & PRIVATE_FLAG_SUSTAINED_PERFORMANCE_MODE) != 0) {
                mSustainedPerformanceModeCurrent = true;
            }
        }

        return displayHasContent;
    }

    boolean copyAnimToLayoutParams() {
        boolean doRequest = false;

        final int bulkUpdateParams = mService.mAnimator.mBulkUpdateParams;
        if ((bulkUpdateParams & SET_UPDATE_ROTATION) != 0) {
            mUpdateRotation = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & SET_WALLPAPER_MAY_CHANGE) != 0) {
            mWallpaperMayChange = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & SET_FORCE_HIDING_CHANGED) != 0) {
            mWallpaperForceHidingChanged = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & SET_ORIENTATION_CHANGE_COMPLETE) == 0) {
            mOrientationChangeComplete = false;
        } else {
            mOrientationChangeComplete = true;
            mLastWindowFreezeSource = mService.mAnimator.mLastWindowFreezeSource;
            if (mService.mWindowsFreezingScreen != WINDOWS_FREEZING_SCREENS_NONE) {
                doRequest = true;
            }
        }
        if ((bulkUpdateParams & SET_TURN_ON_SCREEN) != 0) {
            mService.mTurnOnScreen = true;
        }
        if ((bulkUpdateParams & SET_WALLPAPER_ACTION_PENDING) != 0) {
            mWallpaperActionPending = true;
        }

        return doRequest;
    }

    private static int toBrightnessOverride(float value) {
        return (int)(value * PowerManager.BRIGHTNESS_ON);
    }

    void enableSurfaceTrace(ParcelFileDescriptor pfd) {
        final FileDescriptor fd = pfd.getFileDescriptor();
        if (mSurfaceTraceEnabled) {
            disableSurfaceTrace();
        }
        mSurfaceTraceEnabled = true;
        mRemoteEventTrace = new RemoteEventTrace(mService, fd);
        mSurfaceTraceFd = pfd;
        for (int displayNdx = mChildren.size() - 1; displayNdx >= 0; --displayNdx) {
            final DisplayContent dc = mChildren.get(displayNdx);
            dc.enableSurfaceTrace(fd);
        }
    }

    void disableSurfaceTrace() {
        mSurfaceTraceEnabled = false;
        mRemoteEventTrace = null;
        mSurfaceTraceFd = null;
        for (int displayNdx = mChildren.size() - 1; displayNdx >= 0; --displayNdx) {
            final DisplayContent dc = mChildren.get(displayNdx);
            dc.disableSurfaceTrace();
        }
    }

    void dumpDisplayContents(PrintWriter pw) {
        pw.println("WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)");
        if (mService.mDisplayReady) {
            final int count = mChildren.size();
            for (int i = 0; i < count; ++i) {
                final DisplayContent displayContent = mChildren.get(i);
                displayContent.dump("  ", pw);
            }
        } else {
            pw.println("  NO DISPLAY");
        }
    }

    void dumpLayoutNeededDisplayIds(PrintWriter pw) {
        if (!isLayoutNeeded()) {
            return;
        }
        pw.print("  mLayoutNeeded on displays=");
        final int count = mChildren.size();
        for (int displayNdx = 0; displayNdx < count; ++displayNdx) {
            final DisplayContent displayContent = mChildren.get(displayNdx);
            if (displayContent.isLayoutNeeded()) {
                pw.print(displayContent.getDisplayId());
            }
        }
        pw.println();
    }

    void dumpWindowsNoHeader(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        final int numDisplays = mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; ++displayNdx) {
            final ReadOnlyWindowList windowList = mChildren.get(displayNdx).getReadOnlyWindowList();
            for (int winNdx = windowList.size() - 1; winNdx >= 0; --winNdx) {
                final WindowState w = windowList.get(winNdx);
                if (windows == null || windows.contains(w)) {
                    pw.println("  Window #" + winNdx + " " + w + ":");
                    w.dump(pw, "    ", dumpAll || windows != null);
                }
            }
        }
    }

    void dumpTokens(PrintWriter pw, boolean dumpAll) {
        pw.println("  All tokens:");
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            mChildren.get(i).dumpTokens(pw, dumpAll);
        }
    }

    @Override
    String getName() {
        return "ROOT";
    }
}