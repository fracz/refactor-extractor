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

package com.android.server.wm;

import static android.app.ActivityManager.StackId.DOCKED_STACK_ID;
import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.HOME_STACK_ID;
import static android.app.ActivityManager.StackId.PINNED_STACK_ID;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_BEHIND;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSET;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.view.Display.DEFAULT_DISPLAY;
import static android.view.Display.FLAG_PRIVATE;
import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;
import static android.view.View.GONE;
import static android.view.WindowManager.DOCKED_BOTTOM;
import static android.view.WindowManager.DOCKED_INVALID;
import static android.view.WindowManager.DOCKED_TOP;
import static android.view.WindowManager.INPUT_CONSUMER_NAVIGATION;
import static android.view.WindowManager.INPUT_CONSUMER_PIP;
import static android.view.WindowManager.INPUT_CONSUMER_WALLPAPER;
import static android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SECURE;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
import static android.view.WindowManager.LayoutParams.NEEDS_MENU_SET_TRUE;
import static android.view.WindowManager.LayoutParams.NEEDS_MENU_UNSET;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_DISABLE_WALLPAPER_TOUCH_EVENTS;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_KEYGUARD;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
import static android.view.WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
import static android.view.WindowManager.LayoutParams.TYPE_BOOT_PROGRESS;
import static android.view.WindowManager.LayoutParams.TYPE_DOCK_DIVIDER;
import static android.view.WindowManager.LayoutParams.TYPE_DRAWN_APPLICATION;
import static android.view.WindowManager.LayoutParams.TYPE_DREAM;
import static android.view.WindowManager.LayoutParams.TYPE_STATUS_BAR;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
import static android.view.WindowManager.LayoutParams.TYPE_TOAST;
import static android.view.WindowManager.LayoutParams.TYPE_WALLPAPER;
import static android.view.WindowManagerPolicy.FINISH_LAYOUT_REDO_ANIM;
import static android.view.WindowManagerPolicy.FINISH_LAYOUT_REDO_CONFIG;
import static android.view.WindowManagerPolicy.FINISH_LAYOUT_REDO_LAYOUT;
import static android.view.WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
import static android.view.WindowManagerPolicy.KEYGUARD_GOING_AWAY_FLAG_NO_WINDOW_ANIMATIONS;
import static android.view.WindowManagerPolicy.KEYGUARD_GOING_AWAY_FLAG_TO_SHADE;
import static android.view.WindowManagerPolicy.KEYGUARD_GOING_AWAY_FLAG_WITH_WALLPAPER;
import static com.android.server.wm.WindowAnimator.KEYGUARD_ANIMATING_OUT;
import static com.android.server.wm.WindowAnimator.KEYGUARD_ANIM_TIMEOUT_MS;
import static com.android.server.wm.WindowAnimator.KEYGUARD_NOT_SHOWN;
import static com.android.server.wm.WindowAnimator.KEYGUARD_SHOWN;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_ADD_REMOVE;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_ANIM;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_BOOT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_DISPLAY;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_FOCUS;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_INPUT_METHOD;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_KEYGUARD;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYERS;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREENSHOT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_SCREEN_ON;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_WALLPAPER;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_VISIBILITY;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_ORIENTATION;
import static com.android.server.wm.WindowManagerDebugConfig.SHOW_STACK_CRAWLS;
import static com.android.server.wm.WindowManagerDebugConfig.SHOW_TRANSACTIONS;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WITH_CLASS_NAME;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;
import static com.android.server.wm.WindowManagerService.H.SEND_NEW_CONFIGURATION;
import static com.android.server.wm.WindowManagerService.H.UPDATE_DOCKED_STACK_DIVIDER;
import static com.android.server.wm.WindowManagerService.H.WINDOW_HIDE_TIMEOUT;
import static com.android.server.wm.WindowManagerService.LAYOUT_REPEAT_THRESHOLD;
import static com.android.server.wm.WindowManagerService.TYPE_LAYER_MULTIPLIER;
import static com.android.server.wm.WindowManagerService.TYPE_LAYER_OFFSET;
import static com.android.server.wm.WindowManagerService.UPDATE_FOCUS_WILL_PLACE_SURFACES;
import static com.android.server.wm.WindowManagerService.WINDOWS_FREEZING_SCREENS_TIMEOUT;
import static com.android.server.wm.WindowManagerService.dipToPixel;
import static com.android.server.wm.WindowManagerService.localLOGV;
import static com.android.server.wm.WindowManagerService.logSurface;
import static com.android.server.wm.WindowState.RESIZE_HANDLE_WIDTH_IN_DP;
import static com.android.server.wm.WindowStateAnimator.DRAW_PENDING;
import static com.android.server.wm.WindowStateAnimator.READY_TO_SHOW;
import static com.android.server.wm.WindowStateAnimator.STACK_CLIP_BEFORE_ANIM;
import static com.android.server.wm.WindowSurfacePlacer.SET_FORCE_HIDING_CHANGED;
import static com.android.server.wm.WindowSurfacePlacer.SET_WALLPAPER_MAY_CHANGE;

import android.annotation.NonNull;
import android.app.ActivityManager.StackId;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.hardware.display.DisplayManagerInternal;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManagerPolicy;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.android.internal.util.FastPrintWriter;
import com.android.internal.view.IInputMethodClient;
import com.android.server.input.InputWindowHandle;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility class for keeping track of the WindowStates and other pertinent contents of a
 * particular Display.
 *
 * IMPORTANT: No method from this class should ever be used without holding
 * WindowManagerService.mWindowMap.
 */
class DisplayContent extends WindowContainer<DisplayContent.DisplayChildWindowContainer> {
    private static final String TAG = TAG_WITH_CLASS_NAME ? "DisplayContent" : TAG_WM;

    /** Unique identifier of this stack. */
    private final int mDisplayId;

    // The display only has 2 child window containers. mTaskStackContainers which contains all
    // window containers that are related to apps (Activities) and mNonAppWindowContainers which
    // contains all window containers not related to apps (e.g. Status bar).
    private final TaskStackContainers mTaskStackContainers = new TaskStackContainers();
    private final NonAppWindowContainers mNonAppWindowContainers = new NonAppWindowContainers();

    /** Z-ordered (bottom-most first) list of all Window objects. Assigned to an element
     * from mDisplayWindows; */
    private final WindowList mWindows = new WindowList();

    // Mapping from a token IBinder to a WindowToken object on this display.
    private final HashMap<IBinder, WindowToken> mTokenMap = new HashMap();

    int mInitialDisplayWidth = 0;
    int mInitialDisplayHeight = 0;
    int mInitialDisplayDensity = 0;
    int mBaseDisplayWidth = 0;
    int mBaseDisplayHeight = 0;
    int mBaseDisplayDensity = 0;
    boolean mDisplayScalingDisabled;
    private final DisplayInfo mDisplayInfo = new DisplayInfo();
    private final Display mDisplay;
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    Rect mBaseDisplayRect = new Rect();
    private Rect mContentRect = new Rect();

    // Accessed directly by all users.
    private boolean mLayoutNeeded;
    int pendingLayoutChanges;
    final boolean isDefaultDisplay;

    /** Window tokens that are in the process of exiting, but still on screen for animations. */
    final ArrayList<WindowToken> mExitingTokens = new ArrayList<>();

    /** A special TaskStack with id==HOME_STACK_ID that moves to the bottom whenever any TaskStack
     * (except a future lockscreen TaskStack) moves to the top. */
    private TaskStack mHomeStack = null;

    /** Detect user tapping outside of current focused task bounds .*/
    TaskTapPointerEventListener mTapDetector;

    /** Detect user tapping outside of current focused stack bounds .*/
    private Region mTouchExcludeRegion = new Region();

    /** Save allocating when calculating rects */
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final RectF mTmpRectF = new RectF();
    private final Matrix mTmpMatrix = new Matrix();
    private final Region mTmpRegion = new Region();

    final WindowManagerService mService;

    /** Remove this display when animation on it has completed. */
    private boolean mDeferredRemoval;

    final DockedStackDividerController mDividerControllerLocked;
    final PinnedStackController mPinnedStackControllerLocked;

    final DimLayerController mDimLayerController;

    final ArrayList<WindowState> mTapExcludedWindows = new ArrayList<>();

    /** Used when rebuilding window list to keep track of windows that have been removed. */
    private WindowState[] mRebuildTmp = new WindowState[20];

    /**
     * Temporary list for comparison. Always clear this after use so we don't end up with
     * orphaned windows references
     */
    private final ArrayList<WindowState> mTmpWindows = new ArrayList<>();

    private final LinkedList<AppWindowToken> mTmpUpdateAllDrawn = new LinkedList();

    private final TaskForResizePointSearchResult mTmpTaskForResizePointSearchResult =
            new TaskForResizePointSearchResult();
    private final GetWindowOnDisplaySearchResult mTmpGetWindowOnDisplaySearchResult =
            new GetWindowOnDisplaySearchResult();

    // True if this display is in the process of being removed. Used to determine if the removal of
    // the display's direct children should be allowed.
    private boolean mRemovingDisplay = false;

    private final WindowLayersController mLayersController;
    final WallpaperController mWallpaperController;
    int mInputMethodAnimLayerAdjustment;

    /**
     * @param display May not be null.
     * @param service You know.
     * @param layersController window layer controller used to assign layer to the windows on this
     *                         display.
     * @param wallpaperController wallpaper windows controller used to adjust the positioning of the
     *                            wallpaper windows in the window list.
     */
    DisplayContent(Display display, WindowManagerService service,
            WindowLayersController layersController, WallpaperController wallpaperController) {
        mDisplay = display;
        mDisplayId = display.getDisplayId();
        mLayersController = layersController;
        mWallpaperController = wallpaperController;
        display.getDisplayInfo(mDisplayInfo);
        display.getMetrics(mDisplayMetrics);
        isDefaultDisplay = mDisplayId == DEFAULT_DISPLAY;
        mService = service;
        initializeDisplayBaseInfo();
        mDividerControllerLocked = new DockedStackDividerController(service, this);
        mPinnedStackControllerLocked = new PinnedStackController(service, this);
        mDimLayerController = new DimLayerController(this);

        // These are the only direct children we should ever have and they are permanent.
        super.addChild(mTaskStackContainers, null);
        super.addChild(mNonAppWindowContainers, null);
    }

    int getDisplayId() {
        return mDisplayId;
    }

    WindowToken getWindowToken(IBinder binder) {
        return mTokenMap.get(binder);
    }

    AppWindowToken getAppWindowToken(IBinder binder) {
        final WindowToken token = getWindowToken(binder);
        if (token == null) {
            return null;
        }
        return token.asAppWindowToken();
    }

    void setWindowToken(IBinder binder, WindowToken token) {
        final DisplayContent dc = mService.mRoot.getWindowTokenDisplay(token);
        if (dc != null) {
            // We currently don't support adding a window token to the display if the display
            // already has the binder mapped to another token. If there is a use case for supporting
            // this moving forward we will either need to merge the WindowTokens some how or have
            // the binder map to a list of window tokens.
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + this
                    + " already mapped to display=" + dc + " tokens=" + dc.mTokenMap);
        }
        mTokenMap.put(binder, token);

        if (token.asAppWindowToken() == null) {
            // Add non-app token to container hierarchy on the display. App tokens are added through
            // the parent container managing them (e.g. Tasks).
            mNonAppWindowContainers.addChild(token, null);
        }
    }

    WindowToken removeWindowToken(IBinder binder) {
        final WindowToken token = mTokenMap.remove(binder);
        if (token != null && token.asAppWindowToken() == null) {
            mNonAppWindowContainers.removeChild(token);
        }
        return token;
    }

    Display getDisplay() {
        return mDisplay;
    }

    DisplayInfo getDisplayInfo() {
        return mDisplayInfo;
    }

    DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }

    DockedStackDividerController getDockedDividerController() {
        return mDividerControllerLocked;
    }

    PinnedStackController getPinnedStackController() {
        return mPinnedStackControllerLocked;
    }

    /**
     * Returns true if the specified UID has access to this display.
     */
    boolean hasAccess(int uid) {
        return mDisplay.hasAccess(uid);
    }

    boolean isPrivate() {
        return (mDisplay.getFlags() & FLAG_PRIVATE) != 0;
    }

    TaskStack getHomeStack() {
        if (mHomeStack == null && mDisplayId == DEFAULT_DISPLAY) {
            Slog.e(TAG_WM, "getHomeStack: Returning null from this=" + this);
        }
        return mHomeStack;
    }

    TaskStack getStackById(int stackId) {
        for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
            final TaskStack stack = mTaskStackContainers.get(i);
            if (stack.mStackId == stackId) {
                return stack;
            }
        }
        return null;
    }

    @Override
    void onConfigurationChanged(Configuration newParentConfig) {
        super.onConfigurationChanged(newParentConfig);

        // The display size information is heavily dependent on the resources in the current
        // configuration, so we need to reconfigure it every time the configuration changes.
        // See {@link PhoneWindowManager#setInitialDisplaySize}...sigh...
        mService.reconfigureDisplayLocked(this);

        getDockedDividerController().onConfigurationChanged();
        getPinnedStackController().onConfigurationChanged();
    }

    /**
     * Callback used to trigger bounds update after configuration change and get ids of stacks whose
     * bounds were updated.
     */
    void updateStackBoundsAfterConfigChange(@NonNull List<Integer> changedStackList) {
        for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
            final TaskStack stack = mTaskStackContainers.get(i);
            if (stack.updateBoundsAfterConfigChange()) {
                changedStackList.add(stack.mStackId);
            }
        }
    }

    @Override
    boolean fillsParent() {
        return true;
    }

    @Override
    boolean isVisible() {
        return true;
    }

    @Override
    void onAppTransitionDone() {
        super.onAppTransitionDone();
        rebuildAppWindowList();
    }

    @Override
    int getOrientation() {
        final WindowManagerPolicy policy = mService.mPolicy;

        // TODO: All the logic before the last return statement in this method should really go in
        // #NonAppWindowContainer.getOrientation() since it is trying to decide orientation based
        // on non-app windows. But, we can not do that until the window list is always correct in
        // terms of z-ordering based on layers.
        if (mService.mDisplayFrozen) {
            if (mService.mLastWindowForcedOrientation != SCREEN_ORIENTATION_UNSPECIFIED) {
                if (DEBUG_ORIENTATION) Slog.v(TAG_WM,
                        "Display is frozen, return " + mService.mLastWindowForcedOrientation);
                // If the display is frozen, some activities may be in the middle of restarting, and
                // thus have removed their old window. If the window has the flag to hide the lock
                // screen, then the lock screen can re-appear and inflict its own orientation on us.
                // Keep the orientation stable until this all settles down.
                return mService.mLastWindowForcedOrientation;
            } else if (policy.isKeyguardLocked()) {
                // Use the last orientation the while the display is frozen with the keyguard
                // locked. This could be the keyguard forced orientation or from a SHOW_WHEN_LOCKED
                // window. We don't want to check the show when locked window directly though as
                // things aren't stable while the display is frozen, for example the window could be
                // momentarily unavailable due to activity relaunch.
                if (DEBUG_ORIENTATION) Slog.v(TAG_WM, "Display is frozen while keyguard locked, "
                        + "return " + mService.mLastOrientation);
                return mService.mLastOrientation;
            }
        } else {
            for (int pos = mWindows.size() - 1; pos >= 0; --pos) {
                final WindowState win = mWindows.get(pos);
                if (win.mAppToken != null) {
                    // We hit an application window. so the orientation will be determined by the
                    // app window. No point in continuing further.
                    break;
                }
                if (!win.isVisibleLw() || !win.mPolicyVisibilityAfterAnim) {
                    continue;
                }
                int req = win.mAttrs.screenOrientation;
                if(req == SCREEN_ORIENTATION_UNSPECIFIED || req == SCREEN_ORIENTATION_BEHIND) {
                    continue;
                }

                if (DEBUG_ORIENTATION) Slog.v(TAG_WM, win + " forcing orientation to " + req);
                if (policy.isKeyguardHostWindow(win.mAttrs)) {
                    mService.mLastKeyguardForcedOrientation = req;
                }
                return (mService.mLastWindowForcedOrientation = req);
            }
            mService.mLastWindowForcedOrientation = SCREEN_ORIENTATION_UNSPECIFIED;

            if (policy.isKeyguardLocked()) {
                // The screen is locked and no top system window is requesting an orientation.
                // Return either the orientation of the show-when-locked app (if there is any) or
                // the orientation of the keyguard. No point in searching from the rest of apps.
                WindowState winShowWhenLocked = (WindowState) policy.getWinShowWhenLockedLw();
                AppWindowToken appShowWhenLocked = winShowWhenLocked == null
                        ? null : winShowWhenLocked.mAppToken;
                if (appShowWhenLocked != null) {
                    int req = appShowWhenLocked.getOrientation();
                    if (req == SCREEN_ORIENTATION_BEHIND) {
                        req = mService.mLastKeyguardForcedOrientation;
                    }
                    if (DEBUG_ORIENTATION) Slog.v(TAG_WM, "Done at " + appShowWhenLocked
                            + " -- show when locked, return " + req);
                    return req;
                }
                if (DEBUG_ORIENTATION) Slog.v(TAG_WM,
                        "No one is requesting an orientation when the screen is locked");
                return mService.mLastKeyguardForcedOrientation;
            }
        }

        // Top system windows are not requesting an orientation. Start searching from apps.
        return mTaskStackContainers.getOrientation();
    }

    void updateDisplayInfo() {
        mDisplay.getDisplayInfo(mDisplayInfo);
        mDisplay.getMetrics(mDisplayMetrics);
        for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
            mTaskStackContainers.get(i).updateDisplayInfo(null);
        }
    }

    void initializeDisplayBaseInfo() {
        final DisplayManagerInternal displayManagerInternal = mService.mDisplayManagerInternal;
        if (displayManagerInternal != null) {
            // Bootstrap the default logical display from the display manager.
            final DisplayInfo newDisplayInfo = displayManagerInternal.getDisplayInfo(mDisplayId);
            if (newDisplayInfo != null) {
                mDisplayInfo.copyFrom(newDisplayInfo);
            }
        }

        mBaseDisplayWidth = mInitialDisplayWidth = mDisplayInfo.logicalWidth;
        mBaseDisplayHeight = mInitialDisplayHeight = mDisplayInfo.logicalHeight;
        mBaseDisplayDensity = mInitialDisplayDensity = mDisplayInfo.logicalDensityDpi;
        mBaseDisplayRect.set(0, 0, mBaseDisplayWidth, mBaseDisplayHeight);
    }

    void getLogicalDisplayRect(Rect out) {
        // Uses same calculation as in LogicalDisplay#configureDisplayInTransactionLocked.
        final int orientation = mDisplayInfo.rotation;
        boolean rotated = (orientation == ROTATION_90 || orientation == ROTATION_270);
        final int physWidth = rotated ? mBaseDisplayHeight : mBaseDisplayWidth;
        final int physHeight = rotated ? mBaseDisplayWidth : mBaseDisplayHeight;
        int width = mDisplayInfo.logicalWidth;
        int left = (physWidth - width) / 2;
        int height = mDisplayInfo.logicalHeight;
        int top = (physHeight - height) / 2;
        out.set(left, top, left + width, top + height);
    }

    private void getLogicalDisplayRect(Rect out, int orientation) {
        getLogicalDisplayRect(out);

        // Rotate the Rect if needed.
        final int currentRotation = mDisplayInfo.rotation;
        final int rotationDelta = deltaRotation(currentRotation, orientation);
        if (rotationDelta == ROTATION_90 || rotationDelta == ROTATION_270) {
            createRotationMatrix(rotationDelta, mBaseDisplayWidth, mBaseDisplayHeight, mTmpMatrix);
            mTmpRectF.set(out);
            mTmpMatrix.mapRect(mTmpRectF);
            mTmpRectF.round(out);
        }
    }

    void getContentRect(Rect out) {
        out.set(mContentRect);
    }

    /** Refer to {@link WindowManagerService#attachStack(int, int, boolean)} */
    void attachStack(TaskStack stack, boolean onTop) {
        mTaskStackContainers.attachStack(stack, onTop);
    }

    void moveStack(TaskStack stack, boolean toTop) {
        mTaskStackContainers.moveStack(stack, toTop);
    }

    @Override
    protected void addChild(DisplayChildWindowContainer child,
            Comparator<DisplayChildWindowContainer> comparator) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    @Override
    protected void addChild(DisplayChildWindowContainer child, int index) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    @Override
    protected void removeChild(DisplayChildWindowContainer child) {
        // Only allow removal of direct children from this display if the display is in the process
        // of been removed.
        if (mRemovingDisplay) {
            super.removeChild(child);
            return;
        }
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    int taskIdFromPoint(int x, int y) {
        for (int stackNdx = mTaskStackContainers.size() - 1; stackNdx >= 0; --stackNdx) {
            final TaskStack stack = mTaskStackContainers.get(stackNdx);
            final int taskId = stack.taskIdFromPoint(x, y);
            if (taskId != -1) {
                return taskId;
            }
        }
        return -1;
    }

    /**
     * Find the task whose outside touch area (for resizing) (x, y) falls within.
     * Returns null if the touch doesn't fall into a resizing area.
     */
    Task findTaskForResizePoint(int x, int y) {
        final int delta = dipToPixel(RESIZE_HANDLE_WIDTH_IN_DP, mDisplayMetrics);
        mTmpTaskForResizePointSearchResult.reset();
        for (int stackNdx = mTaskStackContainers.size() - 1; stackNdx >= 0; --stackNdx) {
            final TaskStack stack = mTaskStackContainers.get(stackNdx);
            if (!StackId.isTaskResizeAllowed(stack.mStackId)) {
                return null;
            }

            stack.findTaskForResizePoint(x, y, delta, mTmpTaskForResizePointSearchResult);
            if (mTmpTaskForResizePointSearchResult.searchDone) {
                return mTmpTaskForResizePointSearchResult.taskForResize;
            }
        }
        return null;
    }

    void setTouchExcludeRegion(Task focusedTask) {
        mTouchExcludeRegion.set(mBaseDisplayRect);
        final int delta = dipToPixel(RESIZE_HANDLE_WIDTH_IN_DP, mDisplayMetrics);
        mTmpRect2.setEmpty();
        for (int stackNdx = mTaskStackContainers.size() - 1; stackNdx >= 0; --stackNdx) {
            final TaskStack stack = mTaskStackContainers.get(stackNdx);
            stack.setTouchExcludeRegion(
                    focusedTask, delta, mTouchExcludeRegion, mContentRect, mTmpRect2);
        }
        // If we removed the focused task above, add it back and only leave its
        // outside touch area in the exclusion. TapDectector is not interested in
        // any touch inside the focused task itself.
        if (!mTmpRect2.isEmpty()) {
            mTouchExcludeRegion.op(mTmpRect2, Region.Op.UNION);
        }
        final WindowState inputMethod = mService.mInputMethodWindow;
        if (inputMethod != null && inputMethod.isVisibleLw()) {
            // If the input method is visible and the user is typing, we don't want these touch
            // events to be intercepted and used to change focus. This would likely cause a
            // disappearance of the input method.
            inputMethod.getTouchableRegion(mTmpRegion);
            mTouchExcludeRegion.op(mTmpRegion, Region.Op.UNION);
        }
        for (int i = mTapExcludedWindows.size() - 1; i >= 0; i--) {
            WindowState win = mTapExcludedWindows.get(i);
            win.getTouchableRegion(mTmpRegion);
            mTouchExcludeRegion.op(mTmpRegion, Region.Op.UNION);
        }
        if (getDockedStackVisibleForUserLocked() != null) {
            mDividerControllerLocked.getTouchRegion(mTmpRect);
            mTmpRegion.set(mTmpRect);
            mTouchExcludeRegion.op(mTmpRegion, Op.UNION);
        }
        if (mTapDetector != null) {
            mTapDetector.setTouchExcludeRegion(mTouchExcludeRegion);
        }
    }

    void switchUser() {
        final int count = mWindows.size();
        for (int i = 0; i < count; i++) {
            final WindowState win = mWindows.get(i);
            if (win.isHiddenFromUserLocked()) {
                if (DEBUG_VISIBILITY) Slog.w(TAG_WM, "user changing, hiding " + win
                        + ", attrs=" + win.mAttrs.type + ", belonging to " + win.mOwnerUid);
                win.hideLw(false);
            }
        }

        for (int stackNdx = mTaskStackContainers.size() - 1; stackNdx >= 0; --stackNdx) {
            mTaskStackContainers.get(stackNdx).switchUser();
        }

        rebuildAppWindowList();
    }

    void resetAnimationBackgroundAnimator() {
        for (int stackNdx = mTaskStackContainers.size() - 1; stackNdx >= 0; --stackNdx) {
            mTaskStackContainers.get(stackNdx).resetAnimationBackgroundAnimator();
        }
    }

    boolean animateDimLayers() {
        return mDimLayerController.animateDimLayers();
    }

    private void resetDimming() {
        mDimLayerController.resetDimming();
    }

    boolean isDimming() {
        return mDimLayerController.isDimming();
    }

    private void stopDimmingIfNeeded() {
        mDimLayerController.stopDimmingIfNeeded();
    }

    @Override
    void removeIfPossible() {
        if (isAnimating()) {
            mDeferredRemoval = true;
            return;
        }
        removeImmediately();
    }

    @Override
    void removeImmediately() {
        mRemovingDisplay = true;
        try {
            super.removeImmediately();
            if (DEBUG_DISPLAY) Slog.v(TAG_WM, "Removing display=" + this);
            mDimLayerController.close();
            if (mDisplayId == DEFAULT_DISPLAY) {
                mService.unregisterPointerEventListener(mTapDetector);
                mService.unregisterPointerEventListener(mService.mMousePositionTracker);
            }
        } finally {
            mRemovingDisplay = false;
        }
    }

    /** Returns true if a removal action is still being deferred. */
    @Override
    boolean checkCompleteDeferredRemoval() {
        final boolean stillDeferringRemoval = super.checkCompleteDeferredRemoval();

        if (!stillDeferringRemoval && mDeferredRemoval) {
            removeImmediately();
            mService.onDisplayRemoved(mDisplayId);
            return false;
        }
        return true;
    }

    boolean animateForIme(float interpolatedValue, float animationTarget,
            float dividerAnimationTarget) {
        boolean updated = false;

        for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
            final TaskStack stack = mTaskStackContainers.get(i);
            if (stack == null || !stack.isAdjustedForIme()) {
                continue;
            }

            if (interpolatedValue >= 1f && animationTarget == 0f && dividerAnimationTarget == 0f) {
                stack.resetAdjustedForIme(true /* adjustBoundsNow */);
                updated = true;
            } else {
                mDividerControllerLocked.mLastAnimationProgress =
                        mDividerControllerLocked.getInterpolatedAnimationValue(interpolatedValue);
                mDividerControllerLocked.mLastDividerProgress =
                        mDividerControllerLocked.getInterpolatedDividerValue(interpolatedValue);
                updated |= stack.updateAdjustForIme(
                        mDividerControllerLocked.mLastAnimationProgress,
                        mDividerControllerLocked.mLastDividerProgress,
                        false /* force */);
            }
            if (interpolatedValue >= 1f) {
                stack.endImeAdjustAnimation();
            }
        }

        return updated;
    }

    boolean clearImeAdjustAnimation() {
        boolean changed = false;
        for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
            final TaskStack stack = mTaskStackContainers.get(i);
            if (stack != null && stack.isAdjustedForIme()) {
                stack.resetAdjustedForIme(true /* adjustBoundsNow */);
                changed  = true;
            }
        }
        return changed;
    }

    void beginImeAdjustAnimation() {
        for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
            final TaskStack stack = mTaskStackContainers.get(i);
            if (stack.isVisible() && stack.isAdjustedForIme()) {
                stack.beginImeAdjustAnimation();
            }
        }
    }

    void adjustForImeIfNeeded() {
        final WindowState imeWin = mService.mInputMethodWindow;
        final boolean imeVisible = imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw()
                && !mDividerControllerLocked.isImeHideRequested();
        final boolean dockVisible = mService.isStackVisibleLocked(DOCKED_STACK_ID);
        final TaskStack imeTargetStack = mService.getImeFocusStackLocked();
        final int imeDockSide = (dockVisible && imeTargetStack != null) ?
                imeTargetStack.getDockSide() : DOCKED_INVALID;
        final boolean imeOnTop = (imeDockSide == DOCKED_TOP);
        final boolean imeOnBottom = (imeDockSide == DOCKED_BOTTOM);
        final boolean dockMinimized = mDividerControllerLocked.isMinimizedDock();
        final int imeHeight = mService.mPolicy.getInputMethodWindowVisibleHeightLw();
        final boolean imeHeightChanged = imeVisible &&
                imeHeight != mDividerControllerLocked.getImeHeightAdjustedFor();

        // The divider could be adjusted for IME position, or be thinner than usual,
        // or both. There are three possible cases:
        // - If IME is visible, and focus is on top, divider is not moved for IME but thinner.
        // - If IME is visible, and focus is on bottom, divider is moved for IME and thinner.
        // - If IME is not visible, divider is not moved and is normal width.

        if (imeVisible && dockVisible && (imeOnTop || imeOnBottom) && !dockMinimized) {
            for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
                final TaskStack stack = mTaskStackContainers.get(i);
                final boolean isDockedOnBottom = stack.getDockSide() == DOCKED_BOTTOM;
                if (stack.isVisible() && (imeOnBottom || isDockedOnBottom)) {
                    stack.setAdjustedForIme(imeWin, imeOnBottom && imeHeightChanged);
                } else {
                    stack.resetAdjustedForIme(false);
                }
            }
            mDividerControllerLocked.setAdjustedForIme(
                    imeOnBottom /*ime*/, true /*divider*/, true /*animate*/, imeWin, imeHeight);
        } else {
            for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
                final TaskStack stack = mTaskStackContainers.get(i);
                stack.resetAdjustedForIme(!dockVisible);
            }
            mDividerControllerLocked.setAdjustedForIme(
                    false /*ime*/, false /*divider*/, dockVisible /*animate*/, imeWin, imeHeight);
        }
        mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
    }

    void setInputMethodAnimLayerAdjustment(int adj) {
        if (DEBUG_LAYERS) Slog.v(TAG_WM, "Setting im layer adj to " + adj);
        mInputMethodAnimLayerAdjustment = adj;
        final WindowState imw = mService.mInputMethodWindow;
        if (imw != null) {
            imw.adjustAnimLayer(adj);
        }
        for (int i = mService.mInputMethodDialogs.size() - 1; i >= 0; i--) {
            final WindowState dialog = mService.mInputMethodDialogs.get(i);
            // TODO: This and other places setting mAnimLayer can probably use WS.adjustAnimLayer,
            // but need to make sure we are not setting things twice for child windows that are
            // already in the list.
            dialog.mWinAnimator.mAnimLayer = dialog.mLayer + adj;
            if (DEBUG_LAYERS) Slog.v(TAG_WM, "IM win " + imw
                    + " anim layer: " + dialog.mWinAnimator.mAnimLayer);
        }
    }

    /**
     * If a window that has an animation specifying a colored background and the current wallpaper
     * is visible, then the color goes *below* the wallpaper so we don't cause the wallpaper to
     * suddenly disappear.
     */
    int getLayerForAnimationBackground(WindowStateAnimator winAnimator) {
        for (int i = mWindows.size() - 1; i >= 0; --i) {
            final WindowState win = mWindows.get(i);
            if (win.mIsWallpaper && win.isVisibleNow()) {
                return win.mWinAnimator.mAnimLayer;
            }
        }
        return winAnimator.mAnimLayer;
    }

    void prepareFreezingTaskBounds() {
        for (int stackNdx = mTaskStackContainers.size() - 1; stackNdx >= 0; --stackNdx) {
            final TaskStack stack = mTaskStackContainers.get(stackNdx);
            stack.prepareFreezingTaskBounds();
        }
    }

    void rotateBounds(int oldRotation, int newRotation, Rect bounds) {
        getLogicalDisplayRect(mTmpRect, newRotation);

        // Compute a transform matrix to undo the coordinate space transformation,
        // and present the window at the same physical position it previously occupied.
        final int deltaRotation = deltaRotation(newRotation, oldRotation);
        createRotationMatrix(deltaRotation, mTmpRect.width(), mTmpRect.height(), mTmpMatrix);

        mTmpRectF.set(bounds);
        mTmpMatrix.mapRect(mTmpRectF);
        mTmpRectF.round(bounds);
    }

    static int deltaRotation(int oldRotation, int newRotation) {
        int delta = newRotation - oldRotation;
        if (delta < 0) delta += 4;
        return delta;
    }

    private static void createRotationMatrix(int rotation, float displayWidth, float displayHeight,
            Matrix outMatrix) {
        // For rotations without Z-ordering we don't need the target rectangle's position.
        createRotationMatrix(rotation, 0 /* rectLeft */, 0 /* rectTop */, displayWidth,
                displayHeight, outMatrix);
    }

    static void createRotationMatrix(int rotation, float rectLeft, float rectTop,
            float displayWidth, float displayHeight, Matrix outMatrix) {
        switch (rotation) {
            case ROTATION_0:
                outMatrix.reset();
                break;
            case ROTATION_270:
                outMatrix.setRotate(270, 0, 0);
                outMatrix.postTranslate(0, displayHeight);
                outMatrix.postTranslate(rectTop, 0);
                break;
            case ROTATION_180:
                outMatrix.reset();
                break;
            case ROTATION_90:
                outMatrix.setRotate(90, 0, 0);
                outMatrix.postTranslate(displayWidth, 0);
                outMatrix.postTranslate(-rectTop, rectLeft);
                break;
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix); pw.print("Display: mDisplayId="); pw.println(mDisplayId);
        final String subPrefix = "  " + prefix;
        pw.print(subPrefix); pw.print("init="); pw.print(mInitialDisplayWidth); pw.print("x");
            pw.print(mInitialDisplayHeight); pw.print(" "); pw.print(mInitialDisplayDensity);
            pw.print("dpi");
            if (mInitialDisplayWidth != mBaseDisplayWidth
                    || mInitialDisplayHeight != mBaseDisplayHeight
                    || mInitialDisplayDensity != mBaseDisplayDensity) {
                pw.print(" base=");
                pw.print(mBaseDisplayWidth); pw.print("x"); pw.print(mBaseDisplayHeight);
                pw.print(" "); pw.print(mBaseDisplayDensity); pw.print("dpi");
            }
            if (mDisplayScalingDisabled) {
                pw.println(" noscale");
            }
            pw.print(" cur=");
            pw.print(mDisplayInfo.logicalWidth);
            pw.print("x"); pw.print(mDisplayInfo.logicalHeight);
            pw.print(" app=");
            pw.print(mDisplayInfo.appWidth);
            pw.print("x"); pw.print(mDisplayInfo.appHeight);
            pw.print(" rng="); pw.print(mDisplayInfo.smallestNominalAppWidth);
            pw.print("x"); pw.print(mDisplayInfo.smallestNominalAppHeight);
            pw.print("-"); pw.print(mDisplayInfo.largestNominalAppWidth);
            pw.print("x"); pw.println(mDisplayInfo.largestNominalAppHeight);
            pw.println(subPrefix + "deferred=" + mDeferredRemoval
                    + " mLayoutNeeded=" + mLayoutNeeded);

        pw.println();
        pw.println("  Application tokens in top down Z order:");
        for (int stackNdx = mTaskStackContainers.size() - 1; stackNdx >= 0; --stackNdx) {
            final TaskStack stack = mTaskStackContainers.get(stackNdx);
            stack.dump(prefix + "  ", pw);
        }

        pw.println();
        if (!mExitingTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting tokens:");
            for (int i = mExitingTokens.size() - 1; i >= 0; i--) {
                final WindowToken token = mExitingTokens.get(i);
                pw.print("  Exiting #"); pw.print(i);
                pw.print(' '); pw.print(token);
                pw.println(':');
                token.dump(pw, "    ");
            }
        }
        pw.println();
        mDimLayerController.dump(prefix + "  ", pw);
        pw.println();
        mDividerControllerLocked.dump(prefix + "  ", pw);
        pw.println();
        mPinnedStackControllerLocked.dump(prefix + "  ", pw);

        if (mInputMethodAnimLayerAdjustment != 0) {
            pw.println(subPrefix
                    + "mInputMethodAnimLayerAdjustment=" + mInputMethodAnimLayerAdjustment);
        }
    }

    @Override
    public String toString() {
        return "Display " + mDisplayId + " info=" + mDisplayInfo + " stacks=" + mChildren;
    }

    String getName() {
        return "Display " + mDisplayId + " name=\"" + mDisplayInfo.name + "\"";
    }

    /**
     * @return The docked stack, but only if it is visible, and {@code null} otherwise.
     */
    TaskStack getDockedStackLocked() {
        final TaskStack stack = mService.mStackIdToStack.get(DOCKED_STACK_ID);
        return (stack != null && stack.isVisible()) ? stack : null;
    }

    /**
     * Like {@link #getDockedStackLocked}, but also returns the docked stack if it's currently not
     * visible, as long as it's not hidden because the current user doesn't have any tasks there.
     */
    TaskStack getDockedStackVisibleForUserLocked() {
        final TaskStack stack = mService.mStackIdToStack.get(DOCKED_STACK_ID);
        return (stack != null && stack.isVisible(true /* ignoreKeyguard */)) ? stack : null;
    }

    /** Find the visible, touch-deliverable window under the given point */
    WindowState getTouchableWinAtPointLocked(float xf, float yf) {
        WindowState touchedWin = null;
        final int x = (int) xf;
        final int y = (int) yf;

        for (int i = mWindows.size() - 1; i >= 0; i--) {
            WindowState window = mWindows.get(i);
            final int flags = window.mAttrs.flags;
            if (!window.isVisibleLw()) {
                continue;
            }
            if ((flags & FLAG_NOT_TOUCHABLE) != 0) {
                continue;
            }

            window.getVisibleBounds(mTmpRect);
            if (!mTmpRect.contains(x, y)) {
                continue;
            }

            window.getTouchableRegion(mTmpRegion);

            final int touchFlags = flags & (FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL);
            if (mTmpRegion.contains(x, y) || touchFlags == 0) {
                touchedWin = window;
                break;
            }
        }

        return touchedWin;
    }

    boolean canAddToastWindowForUid(int uid) {
        // We allow one toast window per UID being shown at a time.
        final int windowCount = mWindows.size();
        for (int i = 0; i < windowCount; i++) {
            final WindowState window = mWindows.get(i);
            if (window.mAttrs.type == TYPE_TOAST && window.mOwnerUid == uid
                    && !window.mPermanentlyHidden && !window.mAnimatingExit
                    && !window.mRemoveOnExit) {
                return false;
            }
        }
        return true;
    }

    void scheduleToastWindowsTimeoutIfNeededLocked(WindowState oldFocus, WindowState newFocus) {
        if (oldFocus == null || (newFocus != null && newFocus.mOwnerUid == oldFocus.mOwnerUid)) {
            return;
        }
        final int lostFocusUid = oldFocus.mOwnerUid;
        final int windowCount = mWindows.size();
        final Handler handler = mService.mH;
        for (int i = 0; i < windowCount; i++) {
            final WindowState window = mWindows.get(i);
            if (window.mAttrs.type == TYPE_TOAST && window.mOwnerUid == lostFocusUid) {
                if (!handler.hasMessages(WINDOW_HIDE_TIMEOUT, window)) {
                    handler.sendMessageDelayed(handler.obtainMessage(WINDOW_HIDE_TIMEOUT, window),
                            window.mAttrs.hideTimeoutMilliseconds);
                }
            }
        }
    }

    WindowState findFocusedWindow() {
        final AppWindowToken focusedApp = mService.mFocusedApp;

        for (int i = mWindows.size() - 1; i >= 0; i--) {
            final WindowState win = mWindows.get(i);

            if (DEBUG_FOCUS) Slog.v(TAG_WM, "Looking for focus: " + i + " = " + win
                    + ", flags=" + win.mAttrs.flags + ", canReceive=" + win.canReceiveKeys());

            if (!win.canReceiveKeys()) {
                continue;
            }

            final AppWindowToken wtoken = win.mAppToken;

            // If this window's application has been removed, just skip it.
            if (wtoken != null && (wtoken.removed || wtoken.sendingToBottom)) {
                if (DEBUG_FOCUS) Slog.v(TAG_WM, "Skipping " + wtoken + " because "
                        + (wtoken.removed ? "removed" : "sendingToBottom"));
                continue;
            }

            if (focusedApp == null) {
                if (DEBUG_FOCUS_LIGHT) Slog.v(TAG_WM, "findFocusedWindow: focusedApp=null"
                        + " using new focus @ " + i + " = " + win);
                return win;
            }

            if (!focusedApp.windowsAreFocusable()) {
                // Current focused app windows aren't focusable...
                if (DEBUG_FOCUS_LIGHT) Slog.v(TAG_WM, "findFocusedWindow: focusedApp windows not"
                        + " focusable using new focus @ " + i + " = " + win);
                return win;
            }

            // Descend through all of the app tokens and find the first that either matches
            // win.mAppToken (return win) or mFocusedApp (return null).
            if (wtoken != null && win.mAttrs.type != TYPE_APPLICATION_STARTING) {
                if (focusedApp.compareTo(wtoken) > 0) {
                    // App stack below focused app stack. No focus for you!!!
                    if (DEBUG_FOCUS_LIGHT) Slog.v(TAG_WM,
                            "findFocusedWindow: Reached focused app=" + focusedApp);
                    return null;
                }
            }

            if (DEBUG_FOCUS_LIGHT) Slog.v(TAG_WM, "findFocusedWindow: Found new focus @ "
                    + i + " = " + win);
            return win;
        }

        if (DEBUG_FOCUS_LIGHT) Slog.v(TAG_WM, "findFocusedWindow: No focusable windows.");
        return null;
    }

    void addAppWindowToWindowList(final WindowState win) {
        final IWindow client = win.mClient;

        WindowList tokenWindowList = getTokenWindowsOnDisplay(win.mToken);
        if (!tokenWindowList.isEmpty()) {
            addAppWindowExisting(win, tokenWindowList);
            return;
        }

        // No windows from this token on this display
        if (localLOGV) Slog.v(TAG_WM, "Figuring out where to add app window "
                + client.asBinder() + " (token=" + this + ")");

        final WindowToken wToken = win.mToken;

        // Figure out where the window should go, based on the order of applications.
        mTmpGetWindowOnDisplaySearchResult.reset();
        for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
            final TaskStack stack = mTaskStackContainers.get(i);
            stack.getWindowOnDisplayBeforeToken(this, wToken, mTmpGetWindowOnDisplaySearchResult);
            if (mTmpGetWindowOnDisplaySearchResult.reachedToken) {
                // We have reach the token we are interested in. End search.
                break;
            }
        }

        WindowState pos = mTmpGetWindowOnDisplaySearchResult.foundWindow;

        // We now know the index into the apps. If we found an app window above, that gives us the
        // position; else we need to look some more.
        if (pos != null) {
            // Move behind any windows attached to this one.
            final WindowToken atoken = getWindowToken(pos.mClient.asBinder());
            if (atoken != null) {
                tokenWindowList = getTokenWindowsOnDisplay(atoken);
                final int NC = tokenWindowList.size();
                if (NC > 0) {
                    WindowState bottom = tokenWindowList.get(0);
                    if (bottom.mSubLayer < 0) {
                        pos = bottom;
                    }
                }
            }
            addWindowToListBefore(win, pos);
            return;
        }

        // Continue looking down until we find the first token that has windows on this display.
        mTmpGetWindowOnDisplaySearchResult.reset();
        for (int i = mTaskStackContainers.size() - 1; i >= 0; --i) {
            final TaskStack stack = mTaskStackContainers.get(i);
            stack.getWindowOnDisplayAfterToken(this, wToken, mTmpGetWindowOnDisplaySearchResult);
            if (mTmpGetWindowOnDisplaySearchResult.foundWindow != null) {
                // We have found a window after the token. End search.
                break;
            }
        }

        pos = mTmpGetWindowOnDisplaySearchResult.foundWindow;

        if (pos != null) {
            // Move in front of any windows attached to this one.
            final WindowToken atoken = getWindowToken(pos.mClient.asBinder());
            if (atoken != null) {
                final WindowState top = atoken.getTopWindow();
                if (top != null && top.mSubLayer >= 0) {
                    pos = top;
                }
            }
            addWindowToListAfter(win, pos);
            return;
        }

        // Just search for the start of this layer.
        final int myLayer = win.mBaseLayer;
        int i;
        for (i = mWindows.size() - 1; i >= 0; --i) {
            final WindowState w = mWindows.get(i);
            // Dock divider shares the base layer with application windows, but we want to always
            // keep it above the application windows. The sharing of the base layer is intended
            // for window animations, which need to be above the dock divider for the duration
            // of the animation.
            if (w.mBaseLayer <= myLayer && w.mAttrs.type != TYPE_DOCK_DIVIDER) {
                break;
            }
        }
        if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT || DEBUG_ADD_REMOVE) Slog.v(TAG_WM,
                "Based on layer: Adding window " + win + " at " + (i + 1) + " of "
                + mWindows.size());
        mWindows.add(i + 1, win);
        mService.mWindowsChanged = true;
    }

    /** Adds this non-app window to the window list. */
    void addNonAppWindowToWindowList(WindowState win) {
        // Figure out where window should go, based on layer.
        int i;
        for (i = mWindows.size() - 1; i >= 0; i--) {
            final WindowState otherWin = mWindows.get(i);
            if (otherWin.getBaseType() != TYPE_WALLPAPER && otherWin.mBaseLayer <= win.mBaseLayer) {
                // Wallpaper wanders through the window list, for example to position itself
                // directly behind keyguard. Because of this it will break the ordering based on
                // WindowState.mBaseLayer. There might windows with higher mBaseLayer behind it and
                // we don't want the new window to appear above them. An example of this is adding
                // of the docked stack divider. Consider a scenario with the following ordering (top
                // to bottom): keyguard, wallpaper, assist preview, apps. We want the dock divider
                // to land below the assist preview, so the dock divider must ignore the wallpaper,
                // with which it shares the base layer.
                break;
            }
        }

        i++;
        if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT || DEBUG_ADD_REMOVE) Slog.v(TAG_WM,
                "Free window: Adding window " + this + " at " + i + " of " + mWindows.size());
        mWindows.add(i, win);
        mService.mWindowsChanged = true;
    }

    void addToWindowList(WindowState win, int index) {
        mService.mWindowsChanged = true;
        mWindows.add(index, win);
    }

    boolean removeFromWindowList(WindowState win) {
        mService.mWindowsChanged = true;
        return mWindows.remove(win);
    }

    private int removeWindowAndChildrenFromWindowList(WindowState win, int interestingPos) {
        int wpos = mWindows.indexOf(win);
        if (wpos < 0) {
            return interestingPos;
        }

        if (wpos < interestingPos) interestingPos--;
        if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG_WM, "Temp removing at " + wpos + ": " + this);
        mWindows.remove(wpos);
        mService.mWindowsChanged = true;
        int childWinCount = win.mChildren.size();
        while (childWinCount > 0) {
            childWinCount--;
            final WindowState cw = win.mChildren.get(childWinCount);
            int cpos = mWindows.indexOf(cw);
            if (cpos >= 0) {
                if (cpos < interestingPos) interestingPos--;
                if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG_WM,
                        "Temp removing child at " + cpos + ": " + cw);
                mWindows.remove(cpos);
            }
        }
        return interestingPos;
    }

    void addChildWindowToWindowList(WindowState win) {
        final WindowState parentWindow = win.getParentWindow();

        WindowList windowsOnSameDisplay = getTokenWindowsOnDisplay(win.mToken);

        // Figure out this window's ordering relative to the parent window.
        final int wCount = windowsOnSameDisplay.size();
        final int sublayer = win.mSubLayer;
        int largestSublayer = Integer.MIN_VALUE;
        WindowState windowWithLargestSublayer = null;
        int i;
        for (i = 0; i < wCount; i++) {
            WindowState w = windowsOnSameDisplay.get(i);
            final int wSublayer = w.mSubLayer;
            if (wSublayer >= largestSublayer) {
                largestSublayer = wSublayer;
                windowWithLargestSublayer = w;
            }
            if (sublayer < 0) {
                // For negative sublayers, we go below all windows in the same sublayer.
                if (wSublayer >= sublayer) {
                    addWindowToListBefore(win, wSublayer >= 0 ? parentWindow : w);
                    break;
                }
            } else {
                // For positive sublayers, we go above all windows in the same sublayer.
                if (wSublayer > sublayer) {
                    addWindowToListBefore(win, w);
                    break;
                }
            }
        }
        if (i >= wCount) {
            if (sublayer < 0) {
                addWindowToListBefore(win, parentWindow);
            } else {
                addWindowToListAfter(win,
                        largestSublayer >= 0 ? windowWithLargestSublayer : parentWindow);
            }
        }
    }

    /** Updates the layer assignment of windows on this display. */
    void assignWindowLayers(boolean setLayoutNeeded) {
        mLayersController.assignWindowLayers(mWindows.getReadOnly());
        if (setLayoutNeeded) {
            setLayoutNeeded();
        }
    }

    void adjustWallpaperWindows() {
        if (mWallpaperController.adjustWallpaperWindows(mWindows.getReadOnly())) {
            assignWindowLayers(true /*setLayoutNeeded*/);
        }
    }

    /**
     * Z-orders the display window list so that:
     * <ul>
     * <li>Any windows that are currently below the wallpaper window stay below the wallpaper
     *      window.
     * <li>Exiting application windows are at the bottom, but above the wallpaper window.
     * <li>All other application windows are above the exiting application windows and ordered based
     *      on the ordering of their stacks and tasks on the display.
     * <li>Non-application windows are at the very top.
     * </ul>
     * <p>
     * NOTE: This isn't a complete picture of what the user see. Further manipulation of the window
     *       surface layering is done in {@link WindowLayersController}.
     */
    void rebuildAppWindowList() {
        int count = mWindows.size();
        int i;
        int lastBelow = -1;
        int numRemoved = 0;

        if (mRebuildTmp.length < count) {
            mRebuildTmp = new WindowState[count + 10];
        }

        // First remove all existing app windows.
        i = 0;
        while (i < count) {
            final WindowState w = mWindows.get(i);
            if (w.mAppToken != null) {
                final WindowState win = mWindows.remove(i);
                win.mRebuilding = true;
                mRebuildTmp[numRemoved] = win;
                mService.mWindowsChanged = true;
                if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG_WM, "Rebuild removing window: " + win);
                count--;
                numRemoved++;
                continue;
            } else if (lastBelow == i-1) {
                if (w.mAttrs.type == TYPE_WALLPAPER) {
                    lastBelow = i;
                }
            }
            i++;
        }

        // Keep whatever windows were below the app windows still below, by skipping them.
        lastBelow++;
        i = lastBelow;

        // First add all of the exiting app tokens...  these are no longer in the main app list,
        // but still have windows shown. We put them in the back because now that the animation is
        // over we no longer will care about them.
        final int numStacks = mTaskStackContainers.size();
        for (int stackNdx = 0; stackNdx < numStacks; ++stackNdx) {
            AppTokenList exitingAppTokens = mTaskStackContainers.get(stackNdx).mExitingAppTokens;
            int NT = exitingAppTokens.size();
            for (int j = 0; j < NT; j++) {
                i = exitingAppTokens.get(j).rebuildWindowListUnchecked(i);
            }
        }

        // And add in the still active app tokens in Z order.
        for (int stackNdx = 0; stackNdx < numStacks; ++stackNdx) {
            i = mTaskStackContainers.get(stackNdx).rebuildWindowList(i);
        }

        i -= lastBelow;
        if (i != numRemoved) {
            setLayoutNeeded();
            Slog.w(TAG_WM, "On display=" + mDisplayId + " Rebuild removed " + numRemoved
                    + " windows but added " + i + " rebuildAppWindowListLocked() "
                    + " callers=" + Debug.getCallers(10));
            for (i = 0; i < numRemoved; i++) {
                WindowState ws = mRebuildTmp[i];
                if (ws.mRebuilding) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                    ws.dump(pw, "", true);
                    pw.flush();
                    Slog.w(TAG_WM, "This window was lost: " + ws);
                    Slog.w(TAG_WM, sw.toString());
                    ws.mWinAnimator.destroySurfaceLocked();
                }
            }
            Slog.w(TAG_WM, "Current window hierarchy:");
            dumpChildrenNames();
            Slog.w(TAG_WM, "Final window list:");
            dumpWindows();
        }
        Arrays.fill(mRebuildTmp, null);
    }

    /** Rebuilds the display's window list and does a relayout if something changed. */
    void rebuildAppWindowsAndLayoutIfNeeded() {
        mTmpWindows.clear();
        mTmpWindows.addAll(mWindows);

        rebuildAppWindowList();

        // Set displayContent.mLayoutNeeded if window order changed.
        final int tmpSize = mTmpWindows.size();
        final int winSize = mWindows.size();
        int tmpNdx = 0, winNdx = 0;
        while (tmpNdx < tmpSize && winNdx < winSize) {
            // Skip over all exiting windows, they've been moved out of order.
            WindowState tmp;
            do {
                tmp = mTmpWindows.get(tmpNdx++);
            } while (tmpNdx < tmpSize && tmp.mAppToken != null && tmp.mAppToken.mIsExiting);

            WindowState win;
            do {
                win = mWindows.get(winNdx++);
            } while (winNdx < winSize && win.mAppToken != null && win.mAppToken.mIsExiting);

            if (tmp != win) {
                // Window order changed.
                setLayoutNeeded();
                break;
            }
        }
        if (tmpNdx != winNdx) {
            // One list was different from the other.
            setLayoutNeeded();
        }
        mTmpWindows.clear();

        if (!mService.updateFocusedWindowLocked(UPDATE_FOCUS_WILL_PLACE_SURFACES,
                false /*updateInputWindows*/)) {
            assignWindowLayers(false /* setLayoutNeeded */);
        }

        mService.mInputMonitor.setUpdateInputWindowsNeededLw();
        mService.mWindowPlacerLocked.performSurfacePlacement();
        mService.mInputMonitor.updateInputWindowsLw(false /*force*/);
    }

    void updateInputWindows(InputMonitor inputMonitor, WindowState inputFocus, boolean inDrag) {
        final InputConsumerImpl navInputConsumer =
                mService.mInputMonitor.getInputConsumer(INPUT_CONSUMER_NAVIGATION, mDisplayId);
        final InputConsumerImpl pipInputConsumer =
                mService.mInputMonitor.getInputConsumer(INPUT_CONSUMER_PIP, mDisplayId);
        final InputConsumerImpl wallpaperInputConsumer =
                mService.mInputMonitor.getInputConsumer(INPUT_CONSUMER_WALLPAPER, mDisplayId);
        boolean addInputConsumerHandle = navInputConsumer != null;
        boolean addPipInputConsumerHandle = pipInputConsumer != null;
        boolean addWallpaperInputConsumerHandle = wallpaperInputConsumer != null;
        final Rect pipTouchableBounds = addPipInputConsumerHandle ? new Rect() : null;
        boolean disableWallpaperTouchEvents = false;

        for (int winNdx = mWindows.size() - 1; winNdx >= 0; --winNdx) {
            final WindowState child = mWindows.get(winNdx);
            final InputChannel inputChannel = child.mInputChannel;
            final InputWindowHandle inputWindowHandle = child.mInputWindowHandle;
            if (inputChannel == null || inputWindowHandle == null || child.mRemoved
                    || child.isAdjustedForMinimizedDock()) {
                // Skip this window because it cannot possibly receive input.
                continue;
            }

            if (addPipInputConsumerHandle
                    && child.getStackId() == PINNED_STACK_ID
                    && inputWindowHandle.layer <= pipInputConsumer.mWindowHandle.layer) {
                // Update the bounds of the Pip input consumer to match the Pinned stack
                child.getStack().getBounds(pipTouchableBounds);
                pipInputConsumer.mWindowHandle.touchableRegion.set(pipTouchableBounds);
                inputMonitor.addInputWindowHandle(pipInputConsumer.mWindowHandle);
                addPipInputConsumerHandle = false;
            }

            if (addInputConsumerHandle
                    && inputWindowHandle.layer <= navInputConsumer.mWindowHandle.layer) {
                inputMonitor.addInputWindowHandle(navInputConsumer.mWindowHandle);
                addInputConsumerHandle = false;
            }

            if (addWallpaperInputConsumerHandle) {
                if (child.mAttrs.type == TYPE_WALLPAPER && child.isVisibleLw()) {
                    // Add the wallpaper input consumer above the first visible wallpaper.
                    inputMonitor.addInputWindowHandle(wallpaperInputConsumer.mWindowHandle);
                    addWallpaperInputConsumerHandle = false;
                }
            }

            final int flags = child.mAttrs.flags;
            final int privateFlags = child.mAttrs.privateFlags;
            final int type = child.mAttrs.type;

            final boolean hasFocus = child == inputFocus;
            final boolean isVisible = child.isVisibleLw();
            if ((privateFlags & PRIVATE_FLAG_DISABLE_WALLPAPER_TOUCH_EVENTS) != 0) {
                disableWallpaperTouchEvents = true;
            }
            final boolean hasWallpaper = mWallpaperController.isWallpaperTarget(child)
                    && (privateFlags & PRIVATE_FLAG_KEYGUARD) == 0
                    && !disableWallpaperTouchEvents;

            // If there's a drag in progress and 'child' is a potential drop target,
            // make sure it's been told about the drag
            if (inDrag && isVisible && isDefaultDisplay) {
                mService.mDragState.sendDragStartedIfNeededLw(child);
            }

            inputMonitor.addInputWindowHandle(
                    inputWindowHandle, child, flags, type, isVisible, hasFocus, hasWallpaper);
        }

        if (addWallpaperInputConsumerHandle) {
            // No visible wallpaper found, add the wallpaper input consumer at the end.
            inputMonitor.addInputWindowHandle(wallpaperInputConsumer.mWindowHandle);
        }
    }

    /** Returns true if a leaked surface was destroyed */
    boolean destroyLeakedSurfaces() {
        boolean leakedSurface = false;
        final int numWindows = mWindows.size();
        for (int winNdx = 0; winNdx < numWindows; ++winNdx) {
            final WindowState ws = mWindows.get(winNdx);
            final WindowStateAnimator wsa = ws.mWinAnimator;
            if (wsa.mSurfaceController == null) {
                continue;
            }
            if (!mService.mSessions.contains(wsa.mSession)) {
                Slog.w(TAG_WM, "LEAKED SURFACE (session doesn't exist): "
                        + ws + " surface=" + wsa.mSurfaceController
                        + " token=" + ws.mToken
                        + " pid=" + ws.mSession.mPid
                        + " uid=" + ws.mSession.mUid);
                wsa.destroySurface();
                mService.mForceRemoves.add(ws);
                leakedSurface = true;
            } else if (ws.mAppToken != null && ws.mAppToken.clientHidden) {
                Slog.w(TAG_WM, "LEAKED SURFACE (app token hidden): "
                        + ws + " surface=" + wsa.mSurfaceController
                        + " token=" + ws.mAppToken
                        + " saved=" + ws.hasSavedSurface());
                if (SHOW_TRANSACTIONS) logSurface(ws, "LEAK DESTROY", false);
                wsa.destroySurface();
                leakedSurface = true;
            }
        }

        return leakedSurface;
    }

    /** Return the list of Windows on this display associated with the input token. */
    WindowList getTokenWindowsOnDisplay(WindowToken token) {
        final WindowList windowList = new WindowList();
        final int count = mWindows.size();
        for (int i = 0; i < count; i++) {
            final WindowState win = mWindows.get(i);
            if (win.mToken == token) {
                windowList.add(win);
            }
        }
        return windowList;
    }

    private void reAddToWindowList(WindowState win) {
        win.mToken.addWindow(win);
        // This is a hack to get all of the child windows added as well at the right position. Child
        // windows should be rare and this case should be rare, so it shouldn't be that big a deal.
        int wpos = mWindows.indexOf(win);
        if (wpos >= 0) {
            if (DEBUG_WINDOW_MOVEMENT) Slog.v(TAG_WM, "ReAdd removing from " + wpos + ": " + win);
            mWindows.remove(wpos);
            mService.mWindowsChanged = true;
            win.reAddWindow(wpos);
        }
    }

    void moveInputMethodDialogs(int pos) {
        ArrayList<WindowState> dialogs = mService.mInputMethodDialogs;

        final int N = dialogs.size();
        if (DEBUG_INPUT_METHOD) Slog.v(TAG_WM, "Removing " + N + " dialogs w/pos=" + pos);
        for (int i = 0; i < N; i++) {
            pos = removeWindowAndChildrenFromWindowList(dialogs.get(i), pos);
        }
        if (DEBUG_INPUT_METHOD) {
            Slog.v(TAG_WM, "Window list w/pos=" + pos);
            logWindowList(mWindows, "  ");
        }

        WindowState ime = mService.mInputMethodWindow;
        if (pos >= 0) {
            // Skip windows owned by the input method.
            if (ime != null) {
                while (pos < mWindows.size()) {
                    WindowState wp = mWindows.get(pos);
                    if (wp == ime || wp.getParentWindow() == ime) {
                        pos++;
                        continue;
                    }
                    break;
                }
            }
            if (DEBUG_INPUT_METHOD) Slog.v(TAG_WM, "Adding " + N + " dialogs at pos=" + pos);
            for (int i=0; i<N; i++) {
                WindowState win = dialogs.get(i);
                pos = win.reAddWindow(pos);
            }
            if (DEBUG_INPUT_METHOD) {
                Slog.v(TAG_WM, "Final window list:");
                logWindowList(mWindows, "  ");
            }
            return;
        }
        for (int i=0; i<N; i++) {
            WindowState win = dialogs.get(i);
            reAddToWindowList(win);
            if (DEBUG_INPUT_METHOD) {
                Slog.v(TAG_WM, "No IM target, final list:");
                logWindowList(mWindows, "  ");
            }
        }
    }

    boolean moveInputMethodWindowsIfNeeded(boolean needAssignLayers) {
        final WindowState imWin = mService.mInputMethodWindow;
        final int DN = mService.mInputMethodDialogs.size();
        if (imWin == null && DN == 0) {
            return false;
        }

        // TODO(multidisplay): IMEs are only supported on the default display.
        int imPos = findDesiredInputMethodWindowIndex(true);
        if (imPos >= 0) {
            // In this case, the input method windows are to be placed
            // immediately above the window they are targeting.

            // First check to see if the input method windows are already
            // located here, and contiguous.
            final int N = mWindows.size();
            final WindowState firstImWin = imPos < N ? mWindows.get(imPos) : null;

            // Figure out the actual input method window that should be
            // at the bottom of their stack.
            WindowState baseImWin = imWin != null ? imWin : mService.mInputMethodDialogs.get(0);
            final WindowState cw = baseImWin.getBottomChild();
            if (cw != null && cw.mSubLayer < 0) {
                baseImWin = cw;
            }

            if (firstImWin == baseImWin) {
                // The windows haven't moved...  but are they still contiguous?
                // First find the top IM window.
                int pos = imPos+1;
                while (pos < N) {
                    if (!(mWindows.get(pos)).mIsImWindow) {
                        break;
                    }
                    pos++;
                }
                pos++;
                // Now there should be no more input method windows above.
                while (pos < N) {
                    if ((mWindows.get(pos)).mIsImWindow) {
                        break;
                    }
                    pos++;
                }
                if (pos >= N) {
                    return false;
                }
            }

            if (imWin != null) {
                if (DEBUG_INPUT_METHOD) {
                    Slog.v(TAG_WM, "Moving IM from " + imPos);
                    logWindowList(mWindows, "  ");
                }
                imPos = removeWindowAndChildrenFromWindowList(imWin, imPos);
                if (DEBUG_INPUT_METHOD) {
                    Slog.v(TAG_WM, "List after removing with new pos " + imPos + ":");
                    logWindowList(mWindows, "  ");
                }
                imWin.reAddWindow(imPos);
                if (DEBUG_INPUT_METHOD) {
                    Slog.v(TAG_WM, "List after moving IM to " + imPos + ":");
                    logWindowList(mWindows, "  ");
                }
                if (DN > 0) moveInputMethodDialogs(imPos+1);
            } else {
                moveInputMethodDialogs(imPos);
            }

        } else {
            // In this case, the input method windows go in a fixed layer,
            // because they aren't currently associated with a focus window.

            if (imWin != null) {
                if (DEBUG_INPUT_METHOD) Slog.v(TAG_WM, "Moving IM from " + imPos);
                removeWindowAndChildrenFromWindowList(imWin, 0);
                reAddToWindowList(imWin);
                if (DEBUG_INPUT_METHOD) {
                    Slog.v(TAG_WM, "List with no IM target:");
                    logWindowList(mWindows, "  ");
                }
                if (DN > 0) moveInputMethodDialogs(-1);
            } else {
                moveInputMethodDialogs(-1);
            }

        }

        if (needAssignLayers) {
            assignWindowLayers(false /* setLayoutNeeded */);
        }

        return true;
    }

    /**
     * Dig through the WindowStates and find the one that the Input Method will target.
     * @param willMove
     * @return The index+1 in mWindows of the discovered target.
     */
    int findDesiredInputMethodWindowIndex(boolean willMove) {
        // TODO(multidisplay): Needs some serious rethought when the target and IME are not on the
        // same display. Or even when the current IME/target are not on the same screen as the next
        // IME/target. For now only look for input windows on the main screen.
        WindowState w = null;
        int i;
        for (i = mWindows.size() - 1; i >= 0; --i) {
            final WindowState win = mWindows.get(i);

            if (DEBUG_INPUT_METHOD && willMove) Slog.i(TAG_WM, "Checking window @" + i
                    + " " + win + " fl=0x" + Integer.toHexString(win.mAttrs.flags));
            if (canBeImeTarget(win)) {
                w = win;
                //Slog.i(TAG_WM, "Putting input method here!");

                // Yet more tricksyness!  If this window is a "starting" window, we do actually want
                // to be on top of it, but it is not -really- where input will go.  So if the caller
                // is not actually looking to move the IME, look down below for a real window to
                // target...
                if (!willMove && w.mAttrs.type == TYPE_APPLICATION_STARTING && i > 0) {
                    final WindowState wb = mWindows.get(i-1);
                    if (wb.mAppToken == w.mAppToken && canBeImeTarget(wb)) {
                        i--;
                        w = wb;
                    }
                }
                break;
            }
        }

        // Now w is either mWindows[0] or an IME (or null if mWindows is empty).

        if (DEBUG_INPUT_METHOD && willMove) Slog.v(TAG_WM, "Proposed new IME target: " + w);

        // Now, a special case -- if the last target's window is in the process of exiting, and is
        // above the new target, keep on the last target to avoid flicker. Consider for example a
        // Dialog with the IME shown: when the Dialog is dismissed, we want to keep the IME above it
        // until it is completely gone so it doesn't drop behind the dialog or its full-screen
        // scrim.
        final WindowState curTarget = mService.mInputMethodTarget;
        if (curTarget != null
                && curTarget.isDisplayedLw()
                && curTarget.isClosing()
                && (w == null || curTarget.mWinAnimator.mAnimLayer > w.mWinAnimator.mAnimLayer)) {
            if (DEBUG_INPUT_METHOD) Slog.v(TAG_WM, "Current target higher, not changing");
            return mWindows.indexOf(curTarget) + 1;
        }

        if (DEBUG_INPUT_METHOD) Slog.v(TAG_WM, "Desired input method target="
                + w + " willMove=" + willMove);

        if (willMove && w != null) {
            AppWindowToken token = curTarget == null ? null : curTarget.mAppToken;
            if (token != null) {

                // Now some fun for dealing with window animations that modify the Z order. We need
                // to look at all windows below the current target that are in this app, finding the
                // highest visible one in layering.
                WindowState highestTarget = null;
                int highestPos = 0;
                if (token.mAppAnimator.animating || token.mAppAnimator.animation != null) {
                    WindowList curWindows = token.getDisplayContent().mWindows;
                    int pos = curWindows.indexOf(curTarget);
                    while (pos >= 0) {
                        WindowState win = curWindows.get(pos);
                        if (win.mAppToken != token) {
                            break;
                        }
                        if (!win.mRemoved) {
                            if (highestTarget == null || win.mWinAnimator.mAnimLayer >
                                    highestTarget.mWinAnimator.mAnimLayer) {
                                highestTarget = win;
                                highestPos = pos;
                            }
                        }
                        pos--;
                    }
                }

                if (highestTarget != null) {
                    final AppTransition appTransition = mService.mAppTransition;
                    if (DEBUG_INPUT_METHOD) Slog.v(TAG_WM, appTransition + " " + highestTarget
                            + " animating=" + highestTarget.mWinAnimator.isAnimationSet()
                            + " layer=" + highestTarget.mWinAnimator.mAnimLayer
                            + " new layer=" + w.mWinAnimator.mAnimLayer);

                    if (appTransition.isTransitionSet()) {
                        // If we are currently setting up for an animation, hold everything until we
                        // can find out what will happen.
                        mService.mInputMethodTargetWaitingAnim = true;
                        mService.mInputMethodTarget = highestTarget;
                        return highestPos + 1;
                    } else if (highestTarget.mWinAnimator.isAnimationSet() &&
                            highestTarget.mWinAnimator.mAnimLayer > w.mWinAnimator.mAnimLayer) {
                        // If the window we are currently targeting is involved with an animation,
                        // and it is on top of the next target we will be over, then hold off on
                        // moving until that is done.
                        mService.mInputMethodTargetWaitingAnim = true;
                        mService.mInputMethodTarget = highestTarget;
                        return highestPos + 1;
                    }
                }
            }
        }

        //Slog.i(TAG_WM, "Placing input method @" + (i+1));
        if (w != null) {
            if (willMove) {
                if (DEBUG_INPUT_METHOD) Slog.w(TAG_WM, "Moving IM target from " + curTarget + " to "
                        + w + (SHOW_STACK_CRAWLS ? " Callers=" + Debug.getCallers(4) : ""));
                mService.mInputMethodTarget = w;
                mService.mInputMethodTargetWaitingAnim = false;
                if (w.mAppToken != null) {
                    setInputMethodAnimLayerAdjustment(
                            w.mAppToken.mAppAnimator.animLayerAdjustment);
                } else {
                    setInputMethodAnimLayerAdjustment(0);
                }
            }

            // If the docked divider is visible, we still need to go through this whole excercise to
            // find the appropriate input method target (used for animations and dialog
            // adjustments), but for purposes of Z ordering we simply wish to place it above the
            // docked divider. Unless it is already above the divider.
            final WindowState dockedDivider = mDividerControllerLocked.getWindow();
            if (dockedDivider != null && dockedDivider.isVisibleLw()) {
                int dividerIndex = mWindows.indexOf(dockedDivider);
                if (dividerIndex > 0 && dividerIndex > i) {
                    return dividerIndex + 1;
                }
            }
            return i+1;
        }
        if (willMove) {
            if (DEBUG_INPUT_METHOD) Slog.w(TAG_WM, "Moving IM target from " + curTarget
                    + " to null." + (SHOW_STACK_CRAWLS ? " Callers=" + Debug.getCallers(4) : ""));
            mService.mInputMethodTarget = null;
            setInputMethodAnimLayerAdjustment(0);
        }
        return -1;
    }

    private static boolean canBeImeTarget(WindowState w) {
        final int fl = w.mAttrs.flags & (FLAG_NOT_FOCUSABLE|FLAG_ALT_FOCUSABLE_IM);
        final int type = w.mAttrs.type;

        if (fl != 0 && fl != (FLAG_NOT_FOCUSABLE | FLAG_ALT_FOCUSABLE_IM)
                && type != TYPE_APPLICATION_STARTING) {
            return false;
        }

        if (DEBUG_INPUT_METHOD) {
            Slog.i(TAG_WM, "isVisibleOrAdding " + w + ": " + w.isVisibleOrAdding());
            if (!w.isVisibleOrAdding()) {
                Slog.i(TAG_WM, "  mSurfaceController=" + w.mWinAnimator.mSurfaceController
                        + " relayoutCalled=" + w.mRelayoutCalled
                        + " viewVis=" + w.mViewVisibility
                        + " policyVis=" + w.mPolicyVisibility
                        + " policyVisAfterAnim=" + w.mPolicyVisibilityAfterAnim
                        + " parentHidden=" + w.isParentWindowHidden()
                        + " exiting=" + w.mAnimatingExit + " destroying=" + w.mDestroying);
                if (w.mAppToken != null) {
                    Slog.i(TAG_WM, "  mAppToken.hiddenRequested=" + w.mAppToken.hiddenRequested);
                }
            }
        }
        return w.isVisibleOrAdding();
    }

    private void logWindowList(final WindowList windows, String prefix) {
        int N = windows.size();
        while (N > 0) {
            N--;
            Slog.v(TAG_WM, prefix + "#" + N + ": " + windows.get(N));
        }
    }

    boolean getNeedsMenu(WindowState win, WindowManagerPolicy.WindowState bottom) {
        int index = -1;
        while (true) {
            if (win.mAttrs.needsMenuKey != NEEDS_MENU_UNSET) {
                return win.mAttrs.needsMenuKey == NEEDS_MENU_SET_TRUE;
            }
            // If we reached the bottom of the range of windows we are considering,
            // assume no menu is needed.
            if (win == bottom) {
                return false;
            }
            // The current window hasn't specified whether menu key is needed; look behind it.
            // First, we may need to determine the starting position.
            if (index < 0) {
                index = mWindows.indexOf(win);
            }
            index--;
            if (index < 0) {
                return false;
            }
            win = mWindows.get(index);
        }
    }

    void setLayoutNeeded() {
        if (DEBUG_LAYOUT) Slog.w(TAG_WM, "setLayoutNeeded: callers=" + Debug.getCallers(3));
        mLayoutNeeded = true;
    }

    private void clearLayoutNeeded() {
        if (DEBUG_LAYOUT) Slog.w(TAG_WM, "clearLayoutNeeded: callers=" + Debug.getCallers(3));
        mLayoutNeeded = false;
    }

    boolean isLayoutNeeded() {
        return mLayoutNeeded;
    }

    private void addAppWindowExisting(WindowState win, WindowList tokenWindowList) {

        // If this application has existing windows, we simply place the new window on top of
        // them... but keep the starting window on top.
        if (win.mAttrs.type == TYPE_BASE_APPLICATION) {
            // Base windows go behind everything else.
            final WindowState lowestWindow = tokenWindowList.get(0);
            addWindowToListBefore(win, lowestWindow);
        } else {
            final AppWindowToken atoken = win.mAppToken;
            final int windowListPos = tokenWindowList.size();
            final WindowState lastWindow = tokenWindowList.get(windowListPos - 1);
            if (atoken != null && lastWindow == atoken.startingWindow) {
                addWindowToListBefore(win, lastWindow);
            } else {
                int newIdx = findIdxBasedOnAppTokens(win);
                // There is a window above this one associated with the same apptoken note that the
                // window could be a floating window that was created later or a window at the top
                // of the list of windows associated with this token.
                if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT || DEBUG_ADD_REMOVE) Slog.v(TAG_WM,
                        "not Base app: Adding window " + win + " at " + (newIdx + 1) + " of "
                                + mWindows.size());
                mWindows.add(newIdx + 1, win);
                mService.mWindowsChanged = true;
            }
        }
    }

    /** Places the first input window after the second input window in the window list. */
    private void addWindowToListAfter(WindowState first, WindowState second) {
        final int i = mWindows.indexOf(second);
        if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT || DEBUG_ADD_REMOVE) Slog.v(TAG_WM,
                "Adding window " + this + " at " + (i + 1) + " of " + mWindows.size()
                + " (after " + second + ")");
        mWindows.add(i + 1, first);
        mService.mWindowsChanged = true;
    }

    /** Places the first input window before the second input window in the window list. */
    private void addWindowToListBefore(WindowState first, WindowState second) {
        int i = mWindows.indexOf(second);
        if (DEBUG_FOCUS || DEBUG_WINDOW_MOVEMENT || DEBUG_ADD_REMOVE) Slog.v(TAG_WM,
                "Adding window " + this + " at " + i + " of " + mWindows.size()
                + " (before " + second + ")");
        if (i < 0) {
            Slog.w(TAG_WM, "addWindowToListBefore: Unable to find " + second + " in " + mWindows);
            i = 0;
        }
        mWindows.add(i, first);
        mService.mWindowsChanged = true;
    }

    /**
     * This method finds out the index of a window that has the same app token as win. used for z
     * ordering the windows in mWindows
     */
    private int findIdxBasedOnAppTokens(WindowState win) {
        for(int j = mWindows.size() - 1; j >= 0; j--) {
            final WindowState wentry = mWindows.get(j);
            if(wentry.mAppToken == win.mAppToken) {
                return j;
            }
        }
        return -1;
    }

    private void dumpChildrenNames() {
        StringBuilder output = new StringBuilder();
        dumpChildrenNames(output, " ");
        Slog.v(TAG_WM, output.toString());
    }

    private void dumpWindows() {
        Slog.v(TAG_WM, " Display #" + mDisplayId);
        for (int winNdx = mWindows.size() - 1; winNdx >= 0; --winNdx) {
            Slog.v(TAG_WM, "  #" + winNdx + ": " + mWindows.get(winNdx));
        }
    }

    void dumpTokens(PrintWriter pw, boolean dumpAll) {
        if (mTokenMap.isEmpty()) {
            return;
        }
        pw.println("  Display #" + mDisplayId);
        final Iterator<WindowToken> it = mTokenMap.values().iterator();
        while (it.hasNext()) {
            final WindowToken token = it.next();
            pw.print("  ");
            pw.print(token);
            if (dumpAll) {
                pw.println(':');
                token.dump(pw, "    ");
            } else {
                pw.println();
            }
        }
    }

    void dumpWindowAnimators(PrintWriter pw, String subPrefix) {
        final int count = mWindows.size();
        for (int j = 0; j < count; j++) {
            final WindowStateAnimator wAnim = mWindows.get(j).mWinAnimator;
            pw.println(subPrefix + "Window #" + j + ": " + wAnim);
        }
    }

    void enableSurfaceTrace(FileDescriptor fd) {
        for (int i = mWindows.size() - 1; i >= 0; i--) {
            final WindowState win = mWindows.get(i);
            win.mWinAnimator.enableSurfaceTrace(fd);
        }
    }

    void disableSurfaceTrace() {
        for (int i = mWindows.size() - 1; i >= 0; i--) {
            final WindowState win = mWindows.get(i);
            win.mWinAnimator.disableSurfaceTrace();
        }
    }

    boolean checkWaitingForWindows() {

        boolean haveBootMsg = false;
        boolean haveApp = false;
        // if the wallpaper service is disabled on the device, we're never going to have
        // wallpaper, don't bother waiting for it
        boolean haveWallpaper = false;
        boolean wallpaperEnabled = mService.mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_enableWallpaperService)
                && !mService.mOnlyCore;
        boolean haveKeyguard = true;
        final int count = mWindows.size();
        for (int i = 0; i < count; i++) {
            final WindowState w = mWindows.get(i);
            if (w.isVisibleLw() && !w.mObscured && !w.isDrawnLw()) {
                return true;
            }
            if (w.isDrawnLw()) {
                if (w.mAttrs.type == TYPE_BOOT_PROGRESS) {
                    haveBootMsg = true;
                } else if (w.mAttrs.type == TYPE_APPLICATION
                        || w.mAttrs.type == TYPE_DRAWN_APPLICATION) {
                    haveApp = true;
                } else if (w.mAttrs.type == TYPE_WALLPAPER) {
                    haveWallpaper = true;
                } else if (w.mAttrs.type == TYPE_STATUS_BAR) {
                    haveKeyguard = mService.mPolicy.isKeyguardDrawnLw();
                }
            }
        }

        if (DEBUG_SCREEN_ON || DEBUG_BOOT) Slog.i(TAG_WM,
                "******** booted=" + mService.mSystemBooted
                + " msg=" + mService.mShowingBootMessages
                + " haveBoot=" + haveBootMsg + " haveApp=" + haveApp
                + " haveWall=" + haveWallpaper + " wallEnabled=" + wallpaperEnabled
                + " haveKeyguard=" + haveKeyguard);

        // If we are turning on the screen to show the boot message, don't do it until the boot
        // message is actually displayed.
        if (!mService.mSystemBooted && !haveBootMsg) {
            return true;
        }

        // If we are turning on the screen after the boot is completed normally, don't do so until
        // we have the application and wallpaper.
        if (mService.mSystemBooted && ((!haveApp && !haveKeyguard) ||
                (wallpaperEnabled && !haveWallpaper))) {
            return true;
        }

        return false;
    }

    void updateWindowsForAnimator(WindowAnimator animator) {
        final WindowManagerPolicy policy = animator.mPolicy;
        final int keyguardGoingAwayFlags = animator.mKeyguardGoingAwayFlags;
        final boolean keyguardGoingAwayToShade =
                (keyguardGoingAwayFlags & KEYGUARD_GOING_AWAY_FLAG_TO_SHADE) != 0;
        final boolean keyguardGoingAwayNoAnimation =
                (keyguardGoingAwayFlags & KEYGUARD_GOING_AWAY_FLAG_NO_WINDOW_ANIMATIONS) != 0;
        final boolean keyguardGoingAwayWithWallpaper =
                (keyguardGoingAwayFlags & KEYGUARD_GOING_AWAY_FLAG_WITH_WALLPAPER) != 0;

        if (animator.mKeyguardGoingAway) {
            for (int i = mWindows.size() - 1; i >= 0; i--) {
                WindowState win = mWindows.get(i);
                if (!policy.isKeyguardHostWindow(win.mAttrs)) {
                    continue;
                }
                final WindowStateAnimator winAnimator = win.mWinAnimator;
                if (policy.isKeyguardShowingAndNotOccluded()) {
                    if (!winAnimator.mAnimating) {
                        if (DEBUG_KEYGUARD) Slog.d(TAG,
                                "updateWindowsForAnimator: creating delay animation");

                        // Create a new animation to delay until keyguard is gone on its own.
                        winAnimator.mAnimation = new AlphaAnimation(1.0f, 1.0f);
                        winAnimator.mAnimation.setDuration(KEYGUARD_ANIM_TIMEOUT_MS);
                        winAnimator.mAnimationIsEntrance = false;
                        winAnimator.mAnimationStartTime = -1;
                        winAnimator.mKeyguardGoingAwayAnimation = true;
                        winAnimator.mKeyguardGoingAwayWithWallpaper
                                = keyguardGoingAwayWithWallpaper;
                    }
                } else {
                    if (DEBUG_KEYGUARD) Slog.d(TAG,
                            "updateWindowsForAnimator: StatusBar is no longer keyguard");
                    animator.mKeyguardGoingAway = false;
                    winAnimator.clearAnimation();
                }
                break;
            }
        }

        animator.mForceHiding = KEYGUARD_NOT_SHOWN;

        boolean wallpaperInUnForceHiding = false;
        boolean startingInUnForceHiding = false;
        ArrayList<WindowStateAnimator> unForceHiding = null;
        WindowState wallpaper = null;
        final WallpaperController wallpaperController = mWallpaperController;
        for (int i = mWindows.size() - 1; i >= 0; i--) {
            WindowState win = mWindows.get(i);
            WindowStateAnimator winAnimator = win.mWinAnimator;
            final int flags = win.mAttrs.flags;
            boolean canBeForceHidden = policy.canBeForceHidden(win, win.mAttrs);
            boolean shouldBeForceHidden = animator.shouldForceHide(win);
            if (winAnimator.hasSurface()) {
                final boolean wasAnimating = winAnimator.mWasAnimating;
                final boolean nowAnimating = winAnimator.stepAnimationLocked(animator.mCurrentTime);
                winAnimator.mWasAnimating = nowAnimating;
                animator.orAnimating(nowAnimating);

                if (DEBUG_WALLPAPER) Slog.v(TAG,
                        win + ": wasAnimating=" + wasAnimating + ", nowAnimating=" + nowAnimating);

                if (wasAnimating && !winAnimator.mAnimating
                        && wallpaperController.isWallpaperTarget(win)) {
                    animator.mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
                    pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                    if (DEBUG_LAYOUT_REPEATS) {
                        mService.mWindowPlacerLocked.debugLayoutRepeats(
                                "updateWindowsAndWallpaperLocked 2", pendingLayoutChanges);
                    }
                }

                if (policy.isForceHiding(win.mAttrs)) {
                    if (!wasAnimating && nowAnimating) {
                        if (DEBUG_KEYGUARD || DEBUG_ANIM || DEBUG_VISIBILITY) Slog.v(TAG,
                                "Animation started that could impact force hide: " + win);
                        animator.mBulkUpdateParams |= SET_FORCE_HIDING_CHANGED;
                        pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                        if (DEBUG_LAYOUT_REPEATS) {
                            mService.mWindowPlacerLocked.debugLayoutRepeats(
                                    "updateWindowsAndWallpaperLocked 3", pendingLayoutChanges);
                        }
                        mService.mFocusMayChange = true;
                    } else if (animator.mKeyguardGoingAway && !nowAnimating) {
                        // Timeout!!
                        Slog.e(TAG, "Timeout waiting for animation to startup");
                        policy.startKeyguardExitAnimation(0, 0);
                        animator.mKeyguardGoingAway = false;
                    }
                    if (win.isReadyForDisplay()) {
                        if (nowAnimating && win.mWinAnimator.mKeyguardGoingAwayAnimation) {
                            animator.mForceHiding = KEYGUARD_ANIMATING_OUT;
                        } else {
                            animator.mForceHiding = win.isDrawnLw()
                                    ? KEYGUARD_SHOWN : KEYGUARD_NOT_SHOWN;
                        }
                    }
                    if (DEBUG_KEYGUARD || DEBUG_VISIBILITY) Slog.v(TAG,
                            "Force hide " + animator.forceHidingToString()
                                    + " hasSurface=" + win.mHasSurface
                                    + " policyVis=" + win.mPolicyVisibility
                                    + " destroying=" + win.mDestroying
                                    + " parentHidden=" + win.isParentWindowHidden()
                                    + " vis=" + win.mViewVisibility
                                    + " hidden=" + win.mToken.hidden
                                    + " anim=" + win.mWinAnimator.mAnimation);
                } else if (canBeForceHidden) {
                    if (shouldBeForceHidden) {
                        if (!win.hideLw(false, false)) {
                            // Was already hidden
                            continue;
                        }
                        if (DEBUG_KEYGUARD || DEBUG_VISIBILITY) Slog.v(TAG,
                                "Now policy hidden: " + win);
                    } else {
                        final Animation postKeyguardExitAnimation =
                                animator.mPostKeyguardExitAnimation;
                        boolean applyExistingExitAnimation = postKeyguardExitAnimation != null
                                && !postKeyguardExitAnimation.hasEnded()
                                && !winAnimator.mKeyguardGoingAwayAnimation
                                && win.hasDrawnLw()
                                && !win.isChildWindow()
                                && !win.mIsImWindow
                                && isDefaultDisplay;

                        // If the window is already showing and we don't need to apply an existing
                        // Keyguard exit animation, skip.
                        if (!win.showLw(false, false) && !applyExistingExitAnimation) {
                            continue;
                        }
                        final boolean visibleNow = win.isVisibleNow();
                        if (!visibleNow) {
                            // Couldn't really show, must showLw() again when win becomes visible.
                            win.hideLw(false, false);
                            continue;
                        }
                        if (DEBUG_KEYGUARD || DEBUG_VISIBILITY) Slog.v(TAG,
                                "Now policy shown: " + win);
                        if ((animator.mBulkUpdateParams & SET_FORCE_HIDING_CHANGED) != 0
                                && !win.isChildWindow()) {
                            if (unForceHiding == null) {
                                unForceHiding = new ArrayList<>();
                            }
                            unForceHiding.add(winAnimator);
                            if ((flags & FLAG_SHOW_WALLPAPER) != 0) {
                                wallpaperInUnForceHiding = true;
                            }
                            if (win.mAttrs.type == TYPE_APPLICATION_STARTING) {
                                startingInUnForceHiding = true;
                            }
                        } else if (applyExistingExitAnimation) {
                            // We're already in the middle of an animation. Use the existing
                            // animation to bring in this window.
                            if (DEBUG_KEYGUARD) Slog.v(TAG,
                                    "Applying existing Keyguard exit animation to new window: win="
                                            + win);

                            final Animation a = policy.createForceHideEnterAnimation(false,
                                    keyguardGoingAwayToShade);
                            winAnimator.setAnimation(a, postKeyguardExitAnimation.getStartTime(),
                                    STACK_CLIP_BEFORE_ANIM);
                            winAnimator.mKeyguardGoingAwayAnimation = true;
                            winAnimator.mKeyguardGoingAwayWithWallpaper
                                    = keyguardGoingAwayWithWallpaper;
                        }
                        final WindowState currentFocus = mService.mCurrentFocus;
                        if (currentFocus == null || currentFocus.mLayer < win.mLayer) {
                            // We are showing on top of the current
                            // focus, so re-evaluate focus to make
                            // sure it is correct.
                            if (DEBUG_FOCUS_LIGHT) Slog.v(TAG,
                                    "updateWindowsForAnimator: setting mFocusMayChange true");
                            mService.mFocusMayChange = true;
                        }
                    }
                    if ((flags & FLAG_SHOW_WALLPAPER) != 0) {
                        animator.mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
                        pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                        if (DEBUG_LAYOUT_REPEATS) {
                            mService.mWindowPlacerLocked.debugLayoutRepeats(
                                    "updateWindowsAndWallpaperLocked 4", pendingLayoutChanges);
                        }
                    }
                }
            }

            // If the window doesn't have a surface, the only thing we care about is the correct
            // policy visibility.
            else if (canBeForceHidden) {
                if (shouldBeForceHidden) {
                    win.hideLw(false, false);
                } else {
                    win.showLw(false, false);
                }
            }

            final AppWindowToken atoken = win.mAppToken;
            if (winAnimator.mDrawState == READY_TO_SHOW) {
                if (atoken == null || atoken.allDrawn) {
                    if (win.performShowLocked()) {
                        pendingLayoutChanges |= FINISH_LAYOUT_REDO_ANIM;
                        if (DEBUG_LAYOUT_REPEATS) {
                            mService.mWindowPlacerLocked.debugLayoutRepeats(
                                    "updateWindowsAndWallpaperLocked 5", pendingLayoutChanges);
                        }
                    }
                }
            }
            final AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
            if (appAnimator != null && appAnimator.thumbnail != null) {
                if (appAnimator.thumbnailTransactionSeq != animator.mAnimTransactionSequence) {
                    appAnimator.thumbnailTransactionSeq = animator.mAnimTransactionSequence;
                    appAnimator.thumbnailLayer = 0;
                }
                if (appAnimator.thumbnailLayer < winAnimator.mAnimLayer) {
                    appAnimator.thumbnailLayer = winAnimator.mAnimLayer;
                }
            }
            if (win.mIsWallpaper) {
                wallpaper = win;
            }
        } // end forall windows

        // If we have windows that are being shown due to them no longer being force-hidden, apply
        // the appropriate animation to them if animations are not disabled.
        if (unForceHiding != null) {
            if (!keyguardGoingAwayNoAnimation) {
                boolean first = true;
                for (int i=unForceHiding.size()-1; i>=0; i--) {
                    final WindowStateAnimator winAnimator = unForceHiding.get(i);
                    final Animation a = policy.createForceHideEnterAnimation(
                            wallpaperInUnForceHiding && !startingInUnForceHiding,
                            keyguardGoingAwayToShade);
                    if (a != null) {
                        if (DEBUG_KEYGUARD) Slog.v(TAG,
                                "Starting keyguard exit animation on window " + winAnimator.mWin);
                        winAnimator.setAnimation(a, STACK_CLIP_BEFORE_ANIM);
                        winAnimator.mKeyguardGoingAwayAnimation = true;
                        winAnimator.mKeyguardGoingAwayWithWallpaper
                                = keyguardGoingAwayWithWallpaper;
                        if (first) {
                            animator.mPostKeyguardExitAnimation = a;
                            animator.mPostKeyguardExitAnimation.setStartTime(animator.mCurrentTime);
                            first = false;
                        }
                    }
                }
            } else if (animator.mKeyguardGoingAway) {
                policy.startKeyguardExitAnimation(animator.mCurrentTime, 0 /* duration */);
                animator.mKeyguardGoingAway = false;
            }


            // Wallpaper is going away in un-force-hide motion, animate it as well.
            if (!wallpaperInUnForceHiding && wallpaper != null && !keyguardGoingAwayNoAnimation) {
                if (DEBUG_KEYGUARD) Slog.d(TAG,
                        "updateWindowsForAnimator: wallpaper animating away");
                final Animation a = policy.createForceHideWallpaperExitAnimation(
                        keyguardGoingAwayToShade);
                if (a != null) {
                    wallpaper.mWinAnimator.setAnimation(a);
                }
            }
        }

        if (animator.mPostKeyguardExitAnimation != null) {
            // We're in the midst of a keyguard exit animation.
            if (animator.mKeyguardGoingAway) {
                policy.startKeyguardExitAnimation(animator.mCurrentTime +
                                animator.mPostKeyguardExitAnimation.getStartOffset(),
                        animator.mPostKeyguardExitAnimation.getDuration());
                animator.mKeyguardGoingAway = false;
            }
            // mPostKeyguardExitAnimation might either be ended normally, cancelled, or "orphaned",
            // meaning that the window it was running on was removed. We check for hasEnded() for
            // ended normally and cancelled case, and check the time for the "orphaned" case.
            else if (animator.mPostKeyguardExitAnimation.hasEnded()
                    || animator.mCurrentTime - animator.mPostKeyguardExitAnimation.getStartTime()
                    > animator.mPostKeyguardExitAnimation.getDuration()) {
                // Done with the animation, reset.
                if (DEBUG_KEYGUARD) Slog.v(TAG, "Done with Keyguard exit animations.");
                animator.mPostKeyguardExitAnimation = null;
            }
        }

        final WindowState winShowWhenLocked = (WindowState) policy.getWinShowWhenLockedLw();
        if (winShowWhenLocked != null) {
            animator.mLastShowWinWhenLocked = winShowWhenLocked;
        }
    }

    void updateWallpaperForAnimator(WindowAnimator animator) {
        resetAnimationBackgroundAnimator();

        final WindowList windows = mWindows;
        WindowState detachedWallpaper = null;

        for (int i = windows.size() - 1; i >= 0; i--) {
            final WindowState win = windows.get(i);
            final WindowStateAnimator winAnimator = win.mWinAnimator;
            if (winAnimator.mSurfaceController == null || !winAnimator.hasSurface()) {
                continue;
            }

            final int flags = win.mAttrs.flags;

            // If this window is animating, make a note that we have an animating window and take
            // care of a request to run a detached wallpaper animation.
            if (winAnimator.mAnimating) {
                if (winAnimator.mAnimation != null) {
                    if ((flags & FLAG_SHOW_WALLPAPER) != 0
                            && winAnimator.mAnimation.getDetachWallpaper()) {
                        detachedWallpaper = win;
                    }
                    final int color = winAnimator.mAnimation.getBackgroundColor();
                    if (color != 0) {
                        final TaskStack stack = win.getStack();
                        if (stack != null) {
                            stack.setAnimationBackground(winAnimator, color);
                        }
                    }
                }
                animator.setAnimating(true);
            }

            // If this window's app token is running a detached wallpaper animation, make a note so
            // we can ensure the wallpaper is displayed behind it.
            final AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
            if (appAnimator != null && appAnimator.animation != null
                    && appAnimator.animating) {
                if ((flags & FLAG_SHOW_WALLPAPER) != 0
                        && appAnimator.animation.getDetachWallpaper()) {
                    detachedWallpaper = win;
                }

                final int color = appAnimator.animation.getBackgroundColor();
                if (color != 0) {
                    final TaskStack stack = win.getStack();
                    if (stack != null) {
                        stack.setAnimationBackground(winAnimator, color);
                    }
                }
            }
        } // end forall windows

        if (animator.mWindowDetachedWallpaper != detachedWallpaper) {
            if (DEBUG_WALLPAPER) Slog.v(TAG, "Detached wallpaper changed from "
                    + animator.mWindowDetachedWallpaper + " to " + detachedWallpaper);
            animator.mWindowDetachedWallpaper = detachedWallpaper;
            animator.mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
        }
    }

    void prepareWindowSurfaces() {
        final int count = mWindows.size();
        for (int j = 0; j < count; j++) {
            mWindows.get(j).mWinAnimator.prepareSurfaceLocked(true);
        }
    }

    boolean inputMethodClientHasFocus(IInputMethodClient client) {
        // The focus for the client is the window immediately below where we would place the input
        // method window.
        int idx = findDesiredInputMethodWindowIndex(false);
        if (idx <= 0) {
            return false;
        }

        WindowState imFocus = mWindows.get(idx - 1);
        if (DEBUG_INPUT_METHOD) {
            Slog.i(TAG_WM, "Desired input method target: " + imFocus);
            Slog.i(TAG_WM, "Current focus: " + mService.mCurrentFocus);
            Slog.i(TAG_WM, "Last focus: " + mService.mLastFocus);
        }

        if (imFocus == null) {
            return false;
        }

        // This may be a starting window, in which case we still want to count it as okay.
        if (imFocus.mAttrs.type == TYPE_APPLICATION_STARTING && imFocus.mAppToken != null) {
            // The client has definitely started, so it really should have a window in this app
            // token. Let's look for it.
            final WindowState w = imFocus.mAppToken.getFirstNonStartingWindow();
            if (w != null) {
                if (DEBUG_INPUT_METHOD) Slog.i(TAG_WM, "Switching to real app window: " + w);
                imFocus = w;
            }
        }

        final IInputMethodClient imeClient = imFocus.mSession.mClient;

        if (DEBUG_INPUT_METHOD) {
            Slog.i(TAG_WM, "IM target client: " + imeClient);
            if (imeClient != null) {
                Slog.i(TAG_WM, "IM target client binder: " + imeClient.asBinder());
                Slog.i(TAG_WM, "Requesting client binder: " + client.asBinder());
            }
        }

        return imeClient != null && imeClient.asBinder() == client.asBinder();
    }

    boolean hasSecureWindowOnScreen() {
        for (int i = mWindows.size() - 1; i >= 0; --i) {
            final WindowState ws = mWindows.get(i);
            if (ws.isOnScreen() && (ws.mAttrs.flags & FLAG_SECURE) != 0) {
                return true;
            }
        }
        return false;
    }

    void updateSystemUiVisibility(int visibility, int globalDiff) {
        for (int i = mWindows.size() - 1; i >= 0; --i) {
            final WindowState ws = mWindows.get(i);
            try {
                int curValue = ws.mSystemUiVisibility;
                int diff = (curValue ^ visibility) & globalDiff;
                int newValue = (curValue & ~diff) | (visibility & diff);
                if (newValue != curValue) {
                    ws.mSeq++;
                    ws.mSystemUiVisibility = newValue;
                }
                if (newValue != curValue || ws.mAttrs.hasSystemUiListeners) {
                    ws.mClient.dispatchSystemUiVisibilityChanged(ws.mSeq,
                            visibility, newValue, diff);
                }
            } catch (RemoteException e) {
                // so sorry
            }
        }
    }

    void onWindowFreezeTimeout() {
        Slog.w(TAG_WM, "Window freeze timeout expired.");
        mService.mWindowsFreezingScreen = WINDOWS_FREEZING_SCREENS_TIMEOUT;
        for (int i = mWindows.size() - 1; i >= 0; --i) {
            final WindowState w = mWindows.get(i);
            if (!w.mOrientationChanging) {
                continue;
            }
            w.mOrientationChanging = false;
            w.mLastFreezeDuration = (int)(SystemClock.elapsedRealtime()
                    - mService.mDisplayFreezeTime);
            Slog.w(TAG_WM, "Force clearing orientation change: " + w);
        }
        mService.mWindowPlacerLocked.performSurfacePlacement();
    }

    void waitForAllWindowsDrawn() {
        final WindowManagerPolicy policy = mService.mPolicy;
        for (int winNdx = mWindows.size() - 1; winNdx >= 0; --winNdx) {
            final WindowState win = mWindows.get(winNdx);
            final boolean isForceHiding = policy.isForceHiding(win.mAttrs);
            final boolean keyguard = policy.isKeyguardHostWindow(win.mAttrs);
            if (win.isVisibleLw() && (win.mAppToken != null || isForceHiding || keyguard)) {
                win.mWinAnimator.mDrawState = DRAW_PENDING;
                // Force add to mResizingWindows.
                win.mLastContentInsets.set(-1, -1, -1, -1);
                mService.mWaitingForDrawn.add(win);

                // No need to wait for the windows below Keyguard.
                if (isForceHiding) {
                    return;
                }
            }
        }
    }

    ReadOnlyWindowList getReadOnlyWindowList() {
        return mWindows.getReadOnly();
    }

    void getWindows(WindowList output) {
        output.addAll(mWindows);
    }

    // TODO: Super crazy long method that should be broken down...
    boolean applySurfaceChangesTransaction(boolean recoveringMemory) {

        boolean focusDisplayed = false;
        boolean displayHasContent = false;
        float preferredRefreshRate = 0;
        int preferredModeId = 0;


        final int dw = mDisplayInfo.logicalWidth;
        final int dh = mDisplayInfo.logicalHeight;
        final WindowSurfacePlacer surfacePlacer = mService.mWindowPlacerLocked;

        mTmpUpdateAllDrawn.clear();

        int repeats = 0;
        do {
            repeats++;
            if (repeats > 6) {
                Slog.w(TAG, "Animation repeat aborted after too many iterations");
                clearLayoutNeeded();
                break;
            }

            if (DEBUG_LAYOUT_REPEATS) surfacePlacer.debugLayoutRepeats("On entry to LockedInner",
                    pendingLayoutChanges);

            if ((pendingLayoutChanges & FINISH_LAYOUT_REDO_WALLPAPER) != 0) {
                adjustWallpaperWindows();
            }

            if (isDefaultDisplay && (pendingLayoutChanges & FINISH_LAYOUT_REDO_CONFIG) != 0) {
                if (DEBUG_LAYOUT) Slog.v(TAG, "Computing new config from layout");
                if (mService.updateOrientationFromAppTokensLocked(true, mDisplayId)) {
                    setLayoutNeeded();
                    mService.mH.obtainMessage(SEND_NEW_CONFIGURATION, mDisplayId).sendToTarget();
                }
            }

            if ((pendingLayoutChanges & FINISH_LAYOUT_REDO_LAYOUT) != 0) {
                setLayoutNeeded();
            }

            // FIRST LOOP: Perform a layout, if needed.
            if (repeats < LAYOUT_REPEAT_THRESHOLD) {
                performLayout(repeats == 1, false /* updateInputWindows */);
            } else {
                Slog.w(TAG, "Layout repeat skipped after too many iterations");
            }

            // FIRST AND ONE HALF LOOP: Make WindowManagerPolicy think it is animating.
            pendingLayoutChanges = 0;

            if (isDefaultDisplay) {
                mService.mPolicy.beginPostLayoutPolicyLw(dw, dh);
                for (int i = mWindows.size() - 1; i >= 0; i--) {
                    final WindowState w = mWindows.get(i);
                    if (w.mHasSurface) {
                        mService.mPolicy.applyPostLayoutPolicyLw(w, w.mAttrs, w.getParentWindow());
                    }
                }
                pendingLayoutChanges |= mService.mPolicy.finishPostLayoutPolicyLw();
                if (DEBUG_LAYOUT_REPEATS) surfacePlacer.debugLayoutRepeats(
                        "after finishPostLayoutPolicyLw", pendingLayoutChanges);
            }
        } while (pendingLayoutChanges != 0);

        RootWindowContainer root = mService.mRoot;
        boolean obscured = false;
        boolean syswin = false;
        resetDimming();

        // Only used if default window
        final boolean someoneLosingFocus = !mService.mLosingFocus.isEmpty();

        for (int i = mWindows.size() - 1; i >= 0; i--) {
            final WindowState w = mWindows.get(i);
            final Task task = w.getTask();
            final boolean obscuredChanged = w.mObscured != obscured;

            // Update effect.
            w.mObscured = obscured;
            if (!obscured) {
                final boolean isDisplayed = w.isDisplayedLw();

                if (isDisplayed && w.isObscuringFullscreen(mDisplayInfo)) {
                    // This window completely covers everything behind it, so we want to leave all
                    // of them as undimmed (for performance reasons).
                    root.mObscuringWindow = w;
                    obscured = true;
                }

                displayHasContent |= root.handleNotObscuredLocked(w, obscured, syswin);

                if (w.mHasSurface && isDisplayed) {
                    final int type = w.mAttrs.type;
                    if (type == TYPE_SYSTEM_DIALOG || type == TYPE_SYSTEM_ERROR
                            || (w.mAttrs.privateFlags & PRIVATE_FLAG_KEYGUARD) != 0) {
                        syswin = true;
                    }
                    if (preferredRefreshRate == 0 && w.mAttrs.preferredRefreshRate != 0) {
                        preferredRefreshRate = w.mAttrs.preferredRefreshRate;
                    }
                    if (preferredModeId == 0 && w.mAttrs.preferredDisplayModeId != 0) {
                        preferredModeId = w.mAttrs.preferredDisplayModeId;
                    }
                }
            }

            w.applyDimLayerIfNeeded();

            if (isDefaultDisplay && obscuredChanged && w.isVisibleLw()
                    && mWallpaperController.isWallpaperTarget(w)) {
                // This is the wallpaper target and its obscured state changed... make sure the
                // current wallpaper's visibility has been updated accordingly.
                mWallpaperController.updateWallpaperVisibility();
            }

            w.handleWindowMovedIfNeeded();

            final WindowStateAnimator winAnimator = w.mWinAnimator;

            //Slog.i(TAG, "Window " + this + " clearing mContentChanged - done placing");
            w.mContentChanged = false;

            // Moved from updateWindowsAndWallpaperLocked().
            if (w.mHasSurface) {
                // Take care of the window being ready to display.
                final boolean committed = winAnimator.commitFinishDrawingLocked();
                if (isDefaultDisplay && committed) {
                    if (w.mAttrs.type == TYPE_DREAM) {
                        // HACK: When a dream is shown, it may at that point hide the lock screen.
                        // So we need to redo the layout to let the phone window manager make this
                        // happen.
                        pendingLayoutChanges |= FINISH_LAYOUT_REDO_LAYOUT;
                        if (DEBUG_LAYOUT_REPEATS) {
                            surfacePlacer.debugLayoutRepeats(
                                    "dream and commitFinishDrawingLocked true",
                                    pendingLayoutChanges);
                        }
                    }
                    if ((w.mAttrs.flags & FLAG_SHOW_WALLPAPER) != 0) {
                        if (DEBUG_WALLPAPER_LIGHT) Slog.v(TAG,
                                "First draw done in potential wallpaper target " + w);
                        root.mWallpaperMayChange = true;
                        pendingLayoutChanges |= FINISH_LAYOUT_REDO_WALLPAPER;
                        if (DEBUG_LAYOUT_REPEATS) {
                            surfacePlacer.debugLayoutRepeats(
                                    "wallpaper and commitFinishDrawingLocked true",
                                    pendingLayoutChanges);
                        }
                    }
                }
                if (!winAnimator.isAnimationStarting() && !winAnimator.isWaitingForOpening()) {
                    // Updates the shown frame before we set up the surface. This is needed
                    // because the resizing could change the top-left position (in addition to
                    // size) of the window. setSurfaceBoundariesLocked uses mShownPosition to
                    // position the surface.
                    //
                    // If an animation is being started, we can't call this method because the
                    // animation hasn't processed its initial transformation yet, but in general
                    // we do want to update the position if the window is animating.
                    winAnimator.computeShownFrameLocked();
                }
                winAnimator.setSurfaceBoundariesLocked(recoveringMemory);
            }

            final AppWindowToken atoken = w.mAppToken;
            if (atoken != null) {
                final boolean updateAllDrawn = atoken.updateDrawnWindowStates(w);
                if (updateAllDrawn && !mTmpUpdateAllDrawn.contains(atoken)) {
                    mTmpUpdateAllDrawn.add(atoken);
                }
            }

            if (isDefaultDisplay && someoneLosingFocus && w == mService.mCurrentFocus
                    && w.isDisplayedLw()) {
                focusDisplayed = true;
            }

            w.updateResizingWindowIfNeeded();
        }

        mService.mDisplayManagerInternal.setDisplayProperties(mDisplayId,
                displayHasContent,
                preferredRefreshRate,
                preferredModeId,
                true /* inTraversal, must call performTraversalInTrans... below */);

        stopDimmingIfNeeded();

        while (!mTmpUpdateAllDrawn.isEmpty()) {
            final AppWindowToken atoken = mTmpUpdateAllDrawn.removeLast();
            // See if any windows have been drawn, so they (and others associated with them)
            // can now be shown.
            atoken.updateAllDrawn(this);
        }

        return focusDisplayed;
    }

    void performLayout(boolean initial, boolean updateInputWindows) {
        if (!isLayoutNeeded()) {
            return;
        }
        clearLayoutNeeded();

        final int dw = mDisplayInfo.logicalWidth;
        final int dh = mDisplayInfo.logicalHeight;

        int i;

        if (DEBUG_LAYOUT) {
            Slog.v(TAG, "-------------------------------------");
            Slog.v(TAG, "performLayout: needed=" + isLayoutNeeded() + " dw=" + dw + " dh=" + dh);
        }

        mService.mPolicy.beginLayoutLw(isDefaultDisplay, dw, dh, mService.mRotation,
                getConfiguration().uiMode);
        if (isDefaultDisplay) {
            // Not needed on non-default displays.
            mService.mSystemDecorLayer = mService.mPolicy.getSystemDecorLayerLw();
            mService.mScreenRect.set(0, 0, dw, dh);
        }

        mService.mPolicy.getContentRectLw(mContentRect);

        int seq = mService.mLayoutSeq + 1;
        if (seq < 0) seq = 0;
        mService.mLayoutSeq = seq;

        boolean behindDream = false;

        // First perform layout of any root windows (not attached to another window).
        int topAttached = -1;
        for (i = mWindows.size() - 1; i >= 0; i--) {
            final WindowState win = mWindows.get(i);

            // Don't do layout of a window if it is not visible, or soon won't be visible, to avoid
            // wasting time and funky changes while a window is animating away.
            final boolean gone = (behindDream && mService.mPolicy.canBeForceHidden(win, win.mAttrs))
                    || win.isGoneForLayoutLw();

            if (DEBUG_LAYOUT && !win.mLayoutAttached) {
                Slog.v(TAG, "1ST PASS " + win + ": gone=" + gone + " mHaveFrame=" + win.mHaveFrame
                        + " mLayoutAttached=" + win.mLayoutAttached
                        + " screen changed=" + win.isConfigChanged());
                final AppWindowToken atoken = win.mAppToken;
                if (gone) Slog.v(TAG, "  GONE: mViewVisibility=" + win.mViewVisibility
                        + " mRelayoutCalled=" + win.mRelayoutCalled + " hidden=" + win.mToken.hidden
                        + " hiddenRequested=" + (atoken != null && atoken.hiddenRequested)
                        + " parentHidden=" + win.isParentWindowHidden());
                else Slog.v(TAG, "  VIS: mViewVisibility=" + win.mViewVisibility
                        + " mRelayoutCalled=" + win.mRelayoutCalled + " hidden=" + win.mToken.hidden
                        + " hiddenRequested=" + (atoken != null && atoken.hiddenRequested)
                        + " parentHidden=" + win.isParentWindowHidden());
            }

            // If this view is GONE, then skip it -- keep the current frame, and let the caller know
            // so they can ignore it if they want.  (We do the normal layout for INVISIBLE windows,
            // since that means "perform layout as normal, just don't display").
            if (!gone || !win.mHaveFrame || win.mLayoutNeeded
                    || ((win.isConfigChanged() || win.setReportResizeHints())
                    && !win.isGoneForLayoutLw() &&
                    ((win.mAttrs.privateFlags & PRIVATE_FLAG_KEYGUARD) != 0 ||
                            (win.mHasSurface && win.mAppToken != null &&
                                    win.mAppToken.layoutConfigChanges)))) {
                if (!win.mLayoutAttached) {
                    if (initial) {
                        //Slog.i(TAG, "Window " + this + " clearing mContentChanged - initial");
                        win.mContentChanged = false;
                    }
                    if (win.mAttrs.type == TYPE_DREAM) {
                        // Don't layout windows behind a dream, so that if it does stuff like hide
                        // the status bar we won't get a bad transition when it goes away.
                        behindDream = true;
                    }
                    win.mLayoutNeeded = false;
                    win.prelayout();
                    mService.mPolicy.layoutWindowLw(win, null);
                    win.mLayoutSeq = seq;

                    // Window frames may have changed. Update dim layer with the new bounds.
                    final Task task = win.getTask();
                    if (task != null) {
                        mDimLayerController.updateDimLayer(task);
                    }

                    if (DEBUG_LAYOUT) Slog.v(TAG, "  LAYOUT: mFrame=" + win.mFrame
                            + " mContainingFrame=" + win.mContainingFrame
                            + " mDisplayFrame=" + win.mDisplayFrame);
                } else {
                    if (topAttached < 0) topAttached = i;
                }
            }
        }

        boolean attachedBehindDream = false;

        // Now perform layout of attached windows, which usually depend on the position of the
        // window they are attached to. XXX does not deal with windows that are attached to windows
        // that are themselves attached.
        for (i = topAttached; i >= 0; i--) {
            final WindowState win = mWindows.get(i);

            if (win.mLayoutAttached) {
                if (DEBUG_LAYOUT) Slog.v(TAG, "2ND PASS " + win + " mHaveFrame=" + win.mHaveFrame
                        + " mViewVisibility=" + win.mViewVisibility
                        + " mRelayoutCalled=" + win.mRelayoutCalled);
                // If this view is GONE, then skip it -- keep the current frame, and let the caller
                // know so they can ignore it if they want.  (We do the normal layout for INVISIBLE
                // windows, since that means "perform layout as normal, just don't display").
                if (attachedBehindDream && mService.mPolicy.canBeForceHidden(win, win.mAttrs)) {
                    continue;
                }
                if ((win.mViewVisibility != GONE && win.mRelayoutCalled) || !win.mHaveFrame
                        || win.mLayoutNeeded) {
                    if (initial) {
                        //Slog.i(TAG, "Window " + this + " clearing mContentChanged - initial");
                        win.mContentChanged = false;
                    }
                    win.mLayoutNeeded = false;
                    win.prelayout();
                    mService.mPolicy.layoutWindowLw(win, win.getParentWindow());
                    win.mLayoutSeq = seq;
                    if (DEBUG_LAYOUT) Slog.v(TAG, "  LAYOUT: mFrame=" + win.mFrame
                            + " mContainingFrame=" + win.mContainingFrame
                            + " mDisplayFrame=" + win.mDisplayFrame);
                }
            } else if (win.mAttrs.type == TYPE_DREAM) {
                // Don't layout windows behind a dream, so that if it does stuff like hide the
                // status bar we won't get a bad transition when it goes away.
                attachedBehindDream = behindDream;
            }
        }

        // Window frames may have changed. Tell the input dispatcher about it.
        mService.mInputMonitor.layoutInputConsumers(dw, dh);
        mService.mInputMonitor.setUpdateInputWindowsNeededLw();
        if (updateInputWindows) {
            mService.mInputMonitor.updateInputWindowsLw(false /*force*/);
        }

        mService.mPolicy.finishLayoutLw();
        mService.mH.sendEmptyMessage(UPDATE_DOCKED_STACK_DIVIDER);
    }

    /**
     * Takes a snapshot of the display.  In landscape mode this grabs the whole screen.
     * In portrait mode, it grabs the full screenshot.
     *
     * @param width the width of the target bitmap
     * @param height the height of the target bitmap
     * @param includeFullDisplay true if the screen should not be cropped before capture
     * @param frameScale the scale to apply to the frame, only used when width = -1 and height = -1
     * @param config of the output bitmap
     * @param wallpaperOnly true if only the wallpaper layer should be included in the screenshot
     */
    Bitmap screenshotApplications(IBinder appToken, int displayId, int width, int height,
            boolean includeFullDisplay, float frameScale, Bitmap.Config config,
            boolean wallpaperOnly) {
        int dw = mDisplayInfo.logicalWidth;
        int dh = mDisplayInfo.logicalHeight;
        if (dw == 0 || dh == 0) {
            if (DEBUG_SCREENSHOT) Slog.i(TAG_WM, "Screenshot of " + appToken
                    + ": returning null. logical widthxheight=" + dw + "x" + dh);
            return null;
        }

        Bitmap bm = null;

        int maxLayer = 0;
        final Rect frame = new Rect();
        final Rect stackBounds = new Rect();

        boolean screenshotReady;
        int minLayer;
        if (appToken == null && !wallpaperOnly) {
            screenshotReady = true;
            minLayer = 0;
        } else {
            screenshotReady = false;
            minLayer = Integer.MAX_VALUE;
        }

        WindowState appWin = null;

        boolean includeImeInScreenshot;
        synchronized(mService.mWindowMap) {
            final AppWindowToken imeTargetAppToken = mService.mInputMethodTarget != null
                    ? mService.mInputMethodTarget.mAppToken : null;
            // We only include the Ime in the screenshot if the app we are screenshoting is the IME
            // target and isn't in multi-window mode. We don't screenshot the IME in multi-window
            // mode because the frame of the IME might not overlap with that of the app.
            // E.g. IME target app at the top in split-screen mode and the IME at the bottom
            // overlapping with the bottom app.
            includeImeInScreenshot = imeTargetAppToken != null
                    && imeTargetAppToken.appToken != null
                    && imeTargetAppToken.appToken.asBinder() == appToken
                    && !mService.mInputMethodTarget.isInMultiWindowMode();
        }

        final int aboveAppLayer = (mService.mPolicy.windowTypeToLayerLw(TYPE_APPLICATION) + 1)
                * TYPE_LAYER_MULTIPLIER + TYPE_LAYER_OFFSET;

        synchronized(mService.mWindowMap) {
            // Figure out the part of the screen that is actually the app.
            appWin = null;
            for (int i = mWindows.size() - 1; i >= 0; i--) {
                final WindowState ws = mWindows.get(i);
                if (!ws.mHasSurface) {
                    continue;
                }
                if (ws.mLayer >= aboveAppLayer) {
                    continue;
                }
                if (wallpaperOnly && !ws.mIsWallpaper) {
                    continue;
                }
                if (ws.mIsImWindow) {
                    if (!includeImeInScreenshot) {
                        continue;
                    }
                } else if (ws.mIsWallpaper) {
                    // If this is the wallpaper layer and we're only looking for the wallpaper layer
                    // then the target window state is this one.
                    if (wallpaperOnly) {
                        appWin = ws;
                    }

                    if (appWin == null) {
                        // We have not ran across the target window yet, so it is probably behind
                        // the wallpaper. This can happen when the keyguard is up and all windows
                        // are moved behind the wallpaper. We don't want to include the wallpaper
                        // layer in the screenshot as it will cover-up the layer of the target
                        // window.
                        continue;
                    }
                    // Fall through. The target window is in front of the wallpaper. For this
                    // case we want to include the wallpaper layer in the screenshot because
                    // the target window might have some transparent areas.
                } else if (appToken != null) {
                    if (ws.mAppToken == null || ws.mAppToken.token != appToken) {
                        // This app window is of no interest if it is not associated with the
                        // screenshot app.
                        continue;
                    }
                    appWin = ws;
                }

                // Include this window.

                final WindowStateAnimator winAnim = ws.mWinAnimator;
                int layer = winAnim.mSurfaceController.getLayer();
                if (maxLayer < layer) {
                    maxLayer = layer;
                }
                if (minLayer > layer) {
                    minLayer = layer;
                }

                // Don't include wallpaper in bounds calculation
                if (!includeFullDisplay && !ws.mIsWallpaper) {
                    final Rect wf = ws.mFrame;
                    final Rect cr = ws.mContentInsets;
                    int left = wf.left + cr.left;
                    int top = wf.top + cr.top;
                    int right = wf.right - cr.right;
                    int bottom = wf.bottom - cr.bottom;
                    frame.union(left, top, right, bottom);
                    ws.getVisibleBounds(stackBounds);
                    if (!Rect.intersects(frame, stackBounds)) {
                        // Set frame empty if there's no intersection.
                        frame.setEmpty();
                    }
                }

                final boolean foundTargetWs =
                        (ws.mAppToken != null && ws.mAppToken.token == appToken)
                                || (appWin != null && wallpaperOnly);
                if (foundTargetWs && ws.isDisplayedLw() && winAnim.getShown()) {
                    screenshotReady = true;
                }

                if (ws.isObscuringFullscreen(mDisplayInfo)){
                    break;
                }
            }

            if (appToken != null && appWin == null) {
                // Can't find a window to snapshot.
                if (DEBUG_SCREENSHOT) Slog.i(TAG_WM,
                        "Screenshot: Couldn't find a surface matching " + appToken);
                return null;
            }

            if (!screenshotReady) {
                Slog.i(TAG_WM, "Failed to capture screenshot of " + appToken +
                        " appWin=" + (appWin == null ? "null" : (appWin + " drawState=" +
                        appWin.mWinAnimator.mDrawState)));
                return null;
            }

            // Screenshot is ready to be taken. Everything from here below will continue
            // through the bottom of the loop and return a value. We only stay in the loop
            // because we don't want to release the mWindowMap lock until the screenshot is
            // taken.

            if (maxLayer == 0) {
                if (DEBUG_SCREENSHOT) Slog.i(TAG_WM, "Screenshot of " + appToken
                        + ": returning null maxLayer=" + maxLayer);
                return null;
            }

            if (!includeFullDisplay) {
                // Constrain frame to the screen size.
                if (!frame.intersect(0, 0, dw, dh)) {
                    frame.setEmpty();
                }
            } else {
                // Caller just wants entire display.
                frame.set(0, 0, dw, dh);
            }
            if (frame.isEmpty()) {
                return null;
            }

            if (width < 0) {
                width = (int) (frame.width() * frameScale);
            }
            if (height < 0) {
                height = (int) (frame.height() * frameScale);
            }

            // Tell surface flinger what part of the image to crop. Take the top
            // right part of the application, and crop the larger dimension to fit.
            Rect crop = new Rect(frame);
            if (width / (float) frame.width() < height / (float) frame.height()) {
                int cropWidth = (int)((float)width / (float)height * frame.height());
                crop.right = crop.left + cropWidth;
            } else {
                int cropHeight = (int)((float)height / (float)width * frame.width());
                crop.bottom = crop.top + cropHeight;
            }

            // The screenshot API does not apply the current screen rotation.
            int rot = mDisplay.getRotation();

            if (rot == ROTATION_90 || rot == ROTATION_270) {
                rot = (rot == ROTATION_90) ? ROTATION_270 : ROTATION_90;
            }

            // Surfaceflinger is not aware of orientation, so convert our logical
            // crop to surfaceflinger's portrait orientation.
            convertCropForSurfaceFlinger(crop, rot, dw, dh);

            if (DEBUG_SCREENSHOT) {
                Slog.i(TAG_WM, "Screenshot: " + dw + "x" + dh + " from " + minLayer + " to "
                        + maxLayer + " appToken=" + appToken);
                for (int i = 0; i < mWindows.size(); i++) {
                    final WindowState win = mWindows.get(i);
                    final WindowSurfaceController controller = win.mWinAnimator.mSurfaceController;
                    Slog.i(TAG_WM, win + ": " + win.mLayer
                            + " animLayer=" + win.mWinAnimator.mAnimLayer
                            + " surfaceLayer=" + ((controller == null)
                            ? "null" : controller.getLayer()));
                }
            }

            final ScreenRotationAnimation screenRotationAnimation =
                    mService.mAnimator.getScreenRotationAnimationLocked(DEFAULT_DISPLAY);
            final boolean inRotation = screenRotationAnimation != null &&
                    screenRotationAnimation.isAnimating();
            if (DEBUG_SCREENSHOT && inRotation) Slog.v(TAG_WM,
                    "Taking screenshot while rotating");

            // We force pending transactions to flush before taking
            // the screenshot by pushing an empty synchronous transaction.
            SurfaceControl.openTransaction();
            SurfaceControl.closeTransactionSync();

            bm = SurfaceControl.screenshot(crop, width, height, minLayer, maxLayer,
                    inRotation, rot);
            if (bm == null) {
                Slog.w(TAG_WM, "Screenshot failure taking screenshot for (" + dw + "x" + dh
                        + ") to layer " + maxLayer);
                return null;
            }
        }

        if (DEBUG_SCREENSHOT) {
            // TEST IF IT's ALL BLACK
            int[] buffer = new int[bm.getWidth() * bm.getHeight()];
            bm.getPixels(buffer, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
            boolean allBlack = true;
            final int firstColor = buffer[0];
            for (int i = 0; i < buffer.length; i++) {
                if (buffer[i] != firstColor) {
                    allBlack = false;
                    break;
                }
            }
            if (allBlack) {
                Slog.i(TAG_WM, "Screenshot " + appWin + " was monochrome(" +
                        Integer.toHexString(firstColor) + ")! mSurfaceLayer=" +
                        (appWin != null ?
                                appWin.mWinAnimator.mSurfaceController.getLayer() : "null") +
                        " minLayer=" + minLayer + " maxLayer=" + maxLayer);
            }
        }

        // Create a copy of the screenshot that is immutable and backed in ashmem.
        // This greatly reduces the overhead of passing the bitmap between processes.
        Bitmap ret = bm.createAshmemBitmap(config);
        bm.recycle();
        return ret;
    }

    // TODO: Can this use createRotationMatrix()?
    private static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
        if (rot == Surface.ROTATION_90) {
            final int tmp = crop.top;
            crop.top = dw - crop.right;
            crop.right = crop.bottom;
            crop.bottom = dw - crop.left;
            crop.left = tmp;
        } else if (rot == Surface.ROTATION_180) {
            int tmp = crop.top;
            crop.top = dh - crop.bottom;
            crop.bottom = dh - tmp;
            tmp = crop.right;
            crop.right = dw - crop.left;
            crop.left = dw - tmp;
        } else if (rot == Surface.ROTATION_270) {
            final int tmp = crop.top;
            crop.top = crop.left;
            crop.left = dh - crop.bottom;
            crop.bottom = crop.right;
            crop.right = dh - tmp;
        }
    }

    void onSeamlessRotationTimeout() {
        boolean layoutNeeded = false;
        for (int i = mWindows.size() - 1; i >= 0; i--) {
            final WindowState w = mWindows.get(i);
            if (!w.mSeamlesslyRotated) {
                continue;
            }
            layoutNeeded = true;
            w.setDisplayLayoutNeeded();
            mService.markForSeamlessRotation(w, false);
        }

        if (layoutNeeded) {
            mService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    static final class GetWindowOnDisplaySearchResult {
        boolean reachedToken;
        WindowState foundWindow;

        void reset() {
            reachedToken = false;
            foundWindow = null;
        }
    }

    static final class TaskForResizePointSearchResult {
        boolean searchDone;
        Task taskForResize;

        void reset() {
            searchDone = false;
            taskForResize = null;
        }
    }

    /**
     * Base class for any direct child window container of {@link #DisplayContent} need to inherit
     * from. This is mainly a pass through class that allows {@link #DisplayContent} to have
     * homogeneous children type which is currently required by sub-classes of
     * {@link WindowContainer} class.
     */
    static class DisplayChildWindowContainer<E extends WindowContainer> extends WindowContainer<E> {

        int size() {
            return mChildren.size();
        }

        E get(int index) {
            return mChildren.get(index);
        }

        @Override
        boolean fillsParent() {
            return true;
        }

        @Override
        boolean isVisible() {
            return true;
        }
    }

    /**
     * Window container class that contains all containers on this display relating to Apps.
     * I.e Activities.
     */
    private class TaskStackContainers extends DisplayChildWindowContainer<TaskStack> {

        void attachStack(TaskStack stack, boolean onTop) {
            if (stack.mStackId == HOME_STACK_ID) {
                if (mHomeStack != null) {
                    throw new IllegalArgumentException("attachStack: HOME_STACK_ID (0) not first.");
                }
                mHomeStack = stack;
            }
            addChild(stack, onTop);
            stack.onDisplayChanged(DisplayContent.this);
        }

        void moveStack(TaskStack stack, boolean toTop) {
            if (StackId.isAlwaysOnTop(stack.mStackId) && !toTop) {
                // This stack is always-on-top silly...
                Slog.w(TAG_WM, "Ignoring move of always-on-top stack=" + stack + " to bottom");
                return;
            }

            if (!mChildren.contains(stack)) {
                Slog.wtf(TAG_WM, "moving stack that was not added: " + stack, new Throwable());
            }
            removeChild(stack);
            addChild(stack, toTop);
        }

        private void addChild(TaskStack stack, boolean toTop) {
            int addIndex = toTop ? mChildren.size() : 0;

            if (toTop
                    && mService.isStackVisibleLocked(PINNED_STACK_ID)
                    && stack.mStackId != PINNED_STACK_ID) {
                // The pinned stack is always the top most stack (always-on-top) when it is visible.
                // So, stack is moved just below the pinned stack.
                addIndex--;
                TaskStack topStack = mChildren.get(addIndex);
                if (topStack.mStackId != PINNED_STACK_ID) {
                    throw new IllegalStateException("Pinned stack isn't top stack??? " + mChildren);
                }
            }
            addChild(stack, addIndex);
            setLayoutNeeded();
        }

        @Override
        int getOrientation() {
            if (mService.isStackVisibleLocked(DOCKED_STACK_ID)
                    || mService.isStackVisibleLocked(FREEFORM_WORKSPACE_STACK_ID)) {
                // Apps and their containers are not allowed to specify an orientation while the
                // docked or freeform stack is visible...except for the home stack/task if the
                // docked stack is minimized and it actually set something.
                if (mHomeStack != null && mHomeStack.isVisible()
                        && mDividerControllerLocked.isMinimizedDock()) {
                    final int orientation = mHomeStack.getOrientation();
                    if (orientation != SCREEN_ORIENTATION_UNSET) {
                        return orientation;
                    }
                }
                return SCREEN_ORIENTATION_UNSPECIFIED;
            }

            final int orientation = super.getOrientation();
            if (orientation != SCREEN_ORIENTATION_UNSET
                    && orientation != SCREEN_ORIENTATION_BEHIND) {
                if (DEBUG_ORIENTATION) Slog.v(TAG_WM,
                        "App is requesting an orientation, return " + orientation);
                return orientation;
            }

            if (DEBUG_ORIENTATION) Slog.v(TAG_WM,
                    "No app is requesting an orientation, return " + mService.mLastOrientation);
            // The next app has not been requested to be visible, so we keep the current orientation
            // to prevent freezing/unfreezing the display too early.
            return mService.mLastOrientation;
        }
    }

    /**
     * Window container class that contains all containers on this display that are not related to
     * Apps. E.g. status bar.
     */
    private static class NonAppWindowContainers extends DisplayChildWindowContainer<WindowToken> {

    }
}