// Copyright 2012 Google Inc. All Rights Reserved.

package com.android.server.wm;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

import static com.android.server.wm.WindowManagerService.LayoutFields.SET_UPDATE_ROTATION;
import static com.android.server.wm.WindowManagerService.LayoutFields.SET_WALLPAPER_MAY_CHANGE;
import static com.android.server.wm.WindowManagerService.LayoutFields.SET_FORCE_HIDING_CHANGED;
import static com.android.server.wm.WindowManagerService.LayoutFields.SET_ORIENTATION_CHANGE_COMPLETE;

import static com.android.server.wm.WindowManagerService.H.UPDATE_ANIM_PARAMETERS;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManagerPolicy;
import android.view.animation.Animation;

import com.android.internal.policy.impl.PhoneWindowManager;
import com.android.server.wm.WindowManagerService.AppWindowAnimParams;
import com.android.server.wm.WindowManagerService.LayoutToAnimatorParams;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Singleton class that carries out the animations and Surface operations in a separate task
 * on behalf of WindowManagerService.
 */
public class WindowAnimator {
    private static final String TAG = "WindowAnimator";

    final WindowManagerService mService;
    final Context mContext;
    final WindowManagerPolicy mPolicy;

    ArrayList<WinAnimatorList> mWinAnimatorLists = new ArrayList<WinAnimatorList>();

    boolean mAnimating;

    final Runnable mAnimationRunnable;

    int mAdjResult;

    int mPendingLayoutChanges;

    /** Overall window dimensions */
    int mDw, mDh;

    /** Interior window dimensions */
    int mInnerDw, mInnerDh;

    /** Time of current animation step. Reset on each iteration */
    long mCurrentTime;

    /** Skip repeated AppWindowTokens initialization. Note that AppWindowsToken's version of this
     * is a long initialized to Long.MIN_VALUE so that it doesn't match this value on startup. */
    private int mAnimTransactionSequence;

    /** The one and only screen rotation if one is happening */
    ScreenRotationAnimation mScreenRotationAnimation = null;

    // Window currently running an animation that has requested it be detached
    // from the wallpaper.  This means we need to ensure the wallpaper is
    // visible behind it in case it animates in a way that would allow it to be
    // seen. If multiple windows satisfy this, use the lowest window.
    WindowState mWindowDetachedWallpaper = null;

    DimSurface mWindowAnimationBackgroundSurface = null;

    WindowStateAnimator mUniverseBackground = null;
    int mAboveUniverseLayer = 0;

    int mBulkUpdateParams = 0;

    DimAnimator mDimAnimator = null;
    DimAnimator.Parameters mDimParams = null;

    static final int WALLPAPER_ACTION_PENDING = 1;
    int mPendingActions;

    WindowState mWallpaperTarget = null;
    AppWindowAnimator mWpAppAnimator = null;
    WindowState mLowerWallpaperTarget = null;
    WindowState mUpperWallpaperTarget = null;

    ArrayList<AppWindowAnimator> mAppAnimators = new ArrayList<AppWindowAnimator>();

    ArrayList<WindowToken> mWallpaperTokens = new ArrayList<WindowToken>();

    /** Parameters being passed from this into mService. */
    static class AnimatorToLayoutParams {
        boolean mUpdateQueued;
        int mBulkUpdateParams;
        int mPendingLayoutChanges;
        WindowState mWindowDetachedWallpaper;
    }
    /** Do not modify unless holding mService.mWindowMap or this and mAnimToLayout in that order */
    final AnimatorToLayoutParams mAnimToLayout = new AnimatorToLayoutParams();

    boolean mInitialized = false;

    WindowAnimator(final WindowManagerService service) {
        mService = service;
        mContext = service.mContext;
        mPolicy = service.mPolicy;

        mAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                // TODO(cmautner): When full isolation is achieved for animation, the first lock
                // goes away and only the WindowAnimator.this remains.
                synchronized(mService.mWindowMap) {
                    synchronized(WindowAnimator.this) {
                        copyLayoutToAnimParamsLocked();
                        animateLocked();
                    }
                }
            }
        };
    }

    void initializeLocked(final int layerStack) {
        mWindowAnimationBackgroundSurface =
                new DimSurface(mService.mFxSession, layerStack);
        mDimAnimator = new DimAnimator(mService.mFxSession, layerStack);
        mInitialized = true;
    }

    /** Locked on mAnimToLayout */
    void updateAnimToLayoutLocked() {
        final AnimatorToLayoutParams animToLayout = mAnimToLayout;
        synchronized (animToLayout) {
            animToLayout.mBulkUpdateParams = mBulkUpdateParams;
            animToLayout.mPendingLayoutChanges = mPendingLayoutChanges;
            animToLayout.mWindowDetachedWallpaper = mWindowDetachedWallpaper;

            if (!animToLayout.mUpdateQueued) {
                animToLayout.mUpdateQueued = true;
                mService.mH.sendMessage(mService.mH.obtainMessage(UPDATE_ANIM_PARAMETERS));
            }
        }
    }

    /** Copy all WindowManagerService params into local params here. Locked on 'this'. */
    private void copyLayoutToAnimParamsLocked() {
        final LayoutToAnimatorParams layoutToAnim = mService.mLayoutToAnim;
        synchronized(layoutToAnim) {
            layoutToAnim.mAnimationScheduled = false;

            if (!layoutToAnim.mParamsModified) {
                return;
            }
            layoutToAnim.mParamsModified = false;

            if ((layoutToAnim.mChanges & LayoutToAnimatorParams.WALLPAPER_TOKENS_CHANGED) != 0) {
                layoutToAnim.mChanges &= ~LayoutToAnimatorParams.WALLPAPER_TOKENS_CHANGED;
                mWallpaperTokens = new ArrayList<WindowToken>(layoutToAnim.mWallpaperTokens);
            }

            mWinAnimatorLists =
                    new ArrayList<WinAnimatorList>(layoutToAnim.mWinAnimatorLists);
            mWallpaperTarget = layoutToAnim.mWallpaperTarget;
            mWpAppAnimator = mWallpaperTarget == null
                    ? null : mWallpaperTarget.mAppToken == null
                            ? null : mWallpaperTarget.mAppToken.mAppAnimator;
            mLowerWallpaperTarget = layoutToAnim.mLowerWallpaperTarget;
            mUpperWallpaperTarget = layoutToAnim.mUpperWallpaperTarget;

            // Set the new DimAnimator params.
            DimAnimator.Parameters dimParams = layoutToAnim.mDimParams;
            if (dimParams == null) {
                mDimParams = dimParams;
            } else {
                final WindowStateAnimator newWinAnimator = dimParams.mDimWinAnimator;

                // Only set dim params on the highest dimmed layer.
                final WindowStateAnimator existingDimWinAnimator = mDimParams == null
                        ? null : mDimParams.mDimWinAnimator;
                // Don't turn on for an unshown surface, or for any layer but the highest dimmed one.
                if (newWinAnimator.mSurfaceShown &&
                        (existingDimWinAnimator == null || !existingDimWinAnimator.mSurfaceShown
                        || existingDimWinAnimator.mAnimLayer < newWinAnimator.mAnimLayer)) {
                    mDimParams = dimParams;
                }
            }

            mAppAnimators.clear();
            final int N = layoutToAnim.mAppWindowAnimParams.size();
            for (int i = 0; i < N; i++) {
                final AppWindowAnimParams params = layoutToAnim.mAppWindowAnimParams.get(i);
                AppWindowAnimator appAnimator = params.mAppAnimator;
                appAnimator.mAllAppWinAnimators.clear();
                appAnimator.mAllAppWinAnimators.addAll(params.mWinAnimators);
                mAppAnimators.add(appAnimator);
            }
        }
    }

    void hideWallpapersLocked(final WindowState w) {
        if ((mWallpaperTarget == w && mLowerWallpaperTarget == null) || mWallpaperTarget == null) {
            final int numTokens = mWallpaperTokens.size();
            for (int i = numTokens - 1; i >= 0; i--) {
                final WindowToken token = mWallpaperTokens.get(i);
                final int numWindows = token.windows.size();
                for (int j = numWindows - 1; j >= 0; j--) {
                    final WindowState wallpaper = token.windows.get(j);
                    final WindowStateAnimator winAnimator = wallpaper.mWinAnimator;
                    if (!winAnimator.mLastHidden) {
                        winAnimator.hide();
                        mService.dispatchWallpaperVisibility(wallpaper, false);
                        mPendingLayoutChanges |= WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
                    }
                }
                token.hidden = true;
            }
        }
    }

    private void updateWindowsAppsAndRotationAnimationsLocked() {
        int i;
        final int NAT = mAppAnimators.size();
        for (i=0; i<NAT; i++) {
            final AppWindowAnimator appAnimator = mAppAnimators.get(i);
            final boolean wasAnimating = appAnimator.animation != null
                    && appAnimator.animation != AppWindowAnimator.sDummyAnimation;
            if (appAnimator.stepAnimationLocked(mCurrentTime, mInnerDw, mInnerDh)) {
                mAnimating = true;
            } else if (wasAnimating) {
                // stopped animating, do one more pass through the layout
                mPendingLayoutChanges |= WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
                if (WindowManagerService.DEBUG_LAYOUT_REPEATS) {
                    mService.debugLayoutRepeats("appToken " + appAnimator.mAppToken + " done",
                        mPendingLayoutChanges);
                }
                if (WindowManagerService.DEBUG_ANIM) Slog.v(TAG,
                        "updateWindowsApps...: done animating " + appAnimator.mAppToken);
            }
        }

        final int NEAT = mService.mExitingAppTokens.size();
        for (i=0; i<NEAT; i++) {
            final AppWindowAnimator appAnimator = mService.mExitingAppTokens.get(i).mAppAnimator;
            final boolean wasAnimating = appAnimator.animation != null
                    && appAnimator.animation != AppWindowAnimator.sDummyAnimation;
            if (appAnimator.stepAnimationLocked(mCurrentTime, mInnerDw, mInnerDh)) {
                mAnimating = true;
            } else if (wasAnimating) {
                // stopped animating, do one more pass through the layout
                mPendingLayoutChanges |= WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
                if (WindowManagerService.DEBUG_LAYOUT_REPEATS) {
                    mService.debugLayoutRepeats("exiting appToken " + appAnimator.mAppToken
                        + " done", mPendingLayoutChanges);
                }
                if (WindowManagerService.DEBUG_ANIM) Slog.v(TAG,
                        "updateWindowsApps...: done animating exiting " + appAnimator.mAppToken);
            }
        }

        if (mScreenRotationAnimation != null && mScreenRotationAnimation.isAnimating()) {
            if (mScreenRotationAnimation.stepAnimationLocked(mCurrentTime)) {
                mAnimating = true;
            } else {
                mBulkUpdateParams |= SET_UPDATE_ROTATION;
                mScreenRotationAnimation.kill();
                mScreenRotationAnimation = null;
            }
        }
    }

    private void updateWindowsLocked(final WinAnimatorList winAnimatorList) {
        ++mAnimTransactionSequence;

        ArrayList<WindowStateAnimator> unForceHiding = null;
        boolean wallpaperInUnForceHiding = false;

        // forceHiding states.
        final int KEYGUARD_NOT_SHOWN     = 0;
        final int KEYGUARD_ANIMATING_IN  = 1;
        final int KEYGUARD_SHOWN         = 2;
        final int KEYGUARD_ANIMATING_OUT = 3;
        int forceHiding = KEYGUARD_NOT_SHOWN;

        for (int i = winAnimatorList.size() - 1; i >= 0; i--) {
            WindowStateAnimator winAnimator = winAnimatorList.get(i);
            WindowState win = winAnimator.mWin;
            final int flags = winAnimator.mAttrFlags;

            if (winAnimator.mSurface != null) {
                final boolean wasAnimating = winAnimator.mWasAnimating;
                final boolean nowAnimating = winAnimator.stepAnimationLocked(mCurrentTime);

                if (WindowManagerService.DEBUG_WALLPAPER) {
                    Slog.v(TAG, win + ": wasAnimating=" + wasAnimating +
                            ", nowAnimating=" + nowAnimating);
                }

                if (wasAnimating && !winAnimator.mAnimating && mWallpaperTarget == win) {
                    mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
                    mPendingLayoutChanges |= WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
                    if (WindowManagerService.DEBUG_LAYOUT_REPEATS) {
                        mService.debugLayoutRepeats("updateWindowsAndWallpaperLocked 2",
                            mPendingLayoutChanges);
                    }
                }

                if (mPolicy.doesForceHide(win, win.mAttrs)) {
                    if (!wasAnimating && nowAnimating) {
                        if (WindowManagerService.DEBUG_ANIM ||
                                WindowManagerService.DEBUG_VISIBILITY) Slog.v(TAG,
                                "Animation started that could impact force hide: " + win);
                        mBulkUpdateParams |= SET_FORCE_HIDING_CHANGED;
                        mPendingLayoutChanges |= WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
                        if (WindowManagerService.DEBUG_LAYOUT_REPEATS) {
                            mService.debugLayoutRepeats("updateWindowsAndWallpaperLocked 3",
                                mPendingLayoutChanges);
                        }
                        mService.mFocusMayChange = true;
                    }
                    if (win.isReadyForDisplay()) {
                        if (nowAnimating) {
                            if (winAnimator.mAnimationIsEntrance) {
                                forceHiding = KEYGUARD_ANIMATING_IN;
                            } else {
                                forceHiding = KEYGUARD_ANIMATING_OUT;
                            }
                        } else {
                            forceHiding = KEYGUARD_SHOWN;
                        }
                    }
                    if (WindowManagerService.DEBUG_VISIBILITY) Slog.v(TAG,
                            "Force hide " + forceHiding
                            + " hasSurface=" + win.mHasSurface
                            + " policyVis=" + win.mPolicyVisibility
                            + " destroying=" + win.mDestroying
                            + " attHidden=" + win.mAttachedHidden
                            + " vis=" + win.mViewVisibility
                            + " hidden=" + win.mRootToken.hidden
                            + " anim=" + win.mWinAnimator.mAnimation);
                } else if (mPolicy.canBeForceHidden(win, win.mAttrs)) {
                    final boolean hideWhenLocked =
                            (winAnimator.mAttrFlags & FLAG_SHOW_WHEN_LOCKED) == 0;
                    final boolean changed;
                    if (((forceHiding == KEYGUARD_ANIMATING_IN)
                                && (!winAnimator.isAnimating() || hideWhenLocked))
                            || ((forceHiding == KEYGUARD_SHOWN) && hideWhenLocked)) {
                        changed = win.hideLw(false, false);
                        if (WindowManagerService.DEBUG_VISIBILITY && changed) Slog.v(TAG,
                                "Now policy hidden: " + win);
                    } else {
                        changed = win.showLw(false, false);
                        if (WindowManagerService.DEBUG_VISIBILITY && changed) Slog.v(TAG,
                                "Now policy shown: " + win);
                        if (changed) {
                            if ((mBulkUpdateParams & SET_FORCE_HIDING_CHANGED) != 0
                                    && win.isVisibleNow() /*w.isReadyForDisplay()*/) {
                                if (unForceHiding == null) {
                                    unForceHiding = new ArrayList<WindowStateAnimator>();
                                }
                                unForceHiding.add(winAnimator);
                                if ((flags & FLAG_SHOW_WALLPAPER) != 0) {
                                    wallpaperInUnForceHiding = true;
                                }
                            }
                            if (mCurrentFocus == null || mCurrentFocus.mLayer < win.mLayer) {
                                // We are showing on to of the current
                                // focus, so re-evaluate focus to make
                                // sure it is correct.
                                mService.mFocusMayChange = true;
                            }
                        }
                    }
                    if (changed && (flags & FLAG_SHOW_WALLPAPER) != 0) {
                        mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
                        mPendingLayoutChanges |= WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
                        if (WindowManagerService.DEBUG_LAYOUT_REPEATS) {
                            mService.debugLayoutRepeats("updateWindowsAndWallpaperLocked 4",
                                mPendingLayoutChanges);
                        }
                    }
                }
            }

            final AppWindowToken atoken = win.mAppToken;
            if (winAnimator.mDrawState == WindowStateAnimator.READY_TO_SHOW) {
                if (atoken == null || atoken.allDrawn) {
                    if (winAnimator.performShowLocked()) {
                        mPendingLayoutChanges |= WindowManagerPolicy.FINISH_LAYOUT_REDO_ANIM;
                        if (WindowManagerService.DEBUG_LAYOUT_REPEATS) {
                            mService.debugLayoutRepeats("updateWindowsAndWallpaperLocked 5",
                                mPendingLayoutChanges);
                        }
                    }
                }
            }
            final AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
            if (appAnimator != null && appAnimator.thumbnail != null) {
                if (appAnimator.thumbnailTransactionSeq != mAnimTransactionSequence) {
                    appAnimator.thumbnailTransactionSeq = mAnimTransactionSequence;
                    appAnimator.thumbnailLayer = 0;
                }
                if (appAnimator.thumbnailLayer < winAnimator.mAnimLayer) {
                    appAnimator.thumbnailLayer = winAnimator.mAnimLayer;
                }
            }
        } // end forall windows

        // If we have windows that are being show due to them no longer
        // being force-hidden, apply the appropriate animation to them.
        if (unForceHiding != null) {
            for (int i=unForceHiding.size()-1; i>=0; i--) {
                Animation a = mPolicy.createForceHideEnterAnimation(wallpaperInUnForceHiding);
                if (a != null) {
                    final WindowStateAnimator winAnimator = unForceHiding.get(i);
                    winAnimator.setAnimation(a);
                    winAnimator.mAnimationIsEntrance = true;
                }
            }
        }
    }

    private void updateWallpaperLocked(final WinAnimatorList winAnimatorList) {
        WindowStateAnimator windowAnimationBackground = null;
        int windowAnimationBackgroundColor = 0;
        WindowState detachedWallpaper = null;

        for (int i = winAnimatorList.size() - 1; i >= 0; i--) {
            WindowStateAnimator winAnimator = winAnimatorList.get(i);
            if (winAnimator.mSurface == null) {
                continue;
            }

            final int flags = winAnimator.mAttrFlags;
            final WindowState win = winAnimator.mWin;

            // If this window is animating, make a note that we have
            // an animating window and take care of a request to run
            // a detached wallpaper animation.
            if (winAnimator.mAnimating) {
                if (winAnimator.mAnimation != null) {
                    if ((flags & FLAG_SHOW_WALLPAPER) != 0
                            && winAnimator.mAnimation.getDetachWallpaper()) {
                        detachedWallpaper = win;
                    }
                    final int backgroundColor = winAnimator.mAnimation.getBackgroundColor();
                    if (backgroundColor != 0) {
                        if (windowAnimationBackground == null || (winAnimator.mAnimLayer <
                                windowAnimationBackground.mAnimLayer)) {
                            windowAnimationBackground = winAnimator;
                            windowAnimationBackgroundColor = backgroundColor;
                        }
                    }
                }
                mAnimating = true;
            }

            // If this window's app token is running a detached wallpaper
            // animation, make a note so we can ensure the wallpaper is
            // displayed behind it.
            final AppWindowAnimator appAnimator = winAnimator.mAppAnimator;
            if (appAnimator != null && appAnimator.animation != null
                    && appAnimator.animating) {
                if ((flags & FLAG_SHOW_WALLPAPER) != 0
                        && appAnimator.animation.getDetachWallpaper()) {
                    detachedWallpaper = win;
                }

                final int backgroundColor = appAnimator.animation.getBackgroundColor();
                if (backgroundColor != 0) {
                    if (windowAnimationBackground == null || (winAnimator.mAnimLayer <
                            windowAnimationBackground.mAnimLayer)) {
                        windowAnimationBackground = winAnimator;
                        windowAnimationBackgroundColor = backgroundColor;
                    }
                }
            }
        } // end forall windows

        if (mWindowDetachedWallpaper != detachedWallpaper) {
            if (WindowManagerService.DEBUG_WALLPAPER) Slog.v(TAG,
                    "Detached wallpaper changed from " + mWindowDetachedWallpaper
                    + " to " + detachedWallpaper);
            mWindowDetachedWallpaper = detachedWallpaper;
            mBulkUpdateParams |= SET_WALLPAPER_MAY_CHANGE;
        }

        if (windowAnimationBackgroundColor != 0) {
            // If the window that wants black is the current wallpaper
            // target, then the black goes *below* the wallpaper so we
            // don't cause the wallpaper to suddenly disappear.
            int animLayer = windowAnimationBackground.mAnimLayer;
            WindowState win = windowAnimationBackground.mWin;
            if (mWallpaperTarget == win
                    || mLowerWallpaperTarget == win || mUpperWallpaperTarget == win) {
                final int N = winAnimatorList.size();
                for (int i = 0; i < N; i++) {
                    WindowStateAnimator winAnimator = winAnimatorList.get(i);
                    if (winAnimator.mIsWallpaper) {
                        animLayer = winAnimator.mAnimLayer;
                        break;
                    }
                }
            }

            mWindowAnimationBackgroundSurface.show(mDw, mDh,
                    animLayer - WindowManagerService.LAYER_OFFSET_DIM,
                    windowAnimationBackgroundColor);
        } else {
            mWindowAnimationBackgroundSurface.hide();
        }
    }

    private void testTokenMayBeDrawnLocked() {
        // See if any windows have been drawn, so they (and others
        // associated with them) can now be shown.
        final int NT = mAppAnimators.size();
        for (int i=0; i<NT; i++) {
            AppWindowAnimator appAnimator = mAppAnimators.get(i);
            AppWindowToken wtoken = appAnimator.mAppToken;
            final boolean allDrawn = wtoken.allDrawn;
            if (allDrawn != appAnimator.allDrawn) {
                appAnimator.allDrawn = allDrawn;
                if (allDrawn) {
                    // The token has now changed state to having all
                    // windows shown...  what to do, what to do?
                    if (appAnimator.freezingScreen) {
                        appAnimator.showAllWindowsLocked();
                        mService.unsetAppFreezingScreenLocked(wtoken, false, true);
                        if (WindowManagerService.DEBUG_ORIENTATION) Slog.i(TAG,
                                "Setting mOrientationChangeComplete=true because wtoken "
                                + wtoken + " numInteresting=" + wtoken.numInterestingWindows
                                + " numDrawn=" + wtoken.numDrawnWindows);
                        // This will set mOrientationChangeComplete and cause a pass through layout.
                        mPendingLayoutChanges |= WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER;
                    } else {
                        mPendingLayoutChanges |= PhoneWindowManager.FINISH_LAYOUT_REDO_ANIM;
                        if (WindowManagerService.DEBUG_LAYOUT_REPEATS) {
                            mService.debugLayoutRepeats("testTokenMayBeDrawnLocked",
                                mPendingLayoutChanges);
                        }

                        // We can now show all of the drawn windows!
                        if (!mService.mOpeningApps.contains(wtoken)) {
                            mAnimating |= appAnimator.showAllWindowsLocked();
                        }
                    }
                }
            }
        }
    }

    private void performAnimationsLocked(final WinAnimatorList winAnimatorList) {
        updateWindowsLocked(winAnimatorList);
        updateWallpaperLocked(winAnimatorList);

        if ((mPendingLayoutChanges & WindowManagerPolicy.FINISH_LAYOUT_REDO_WALLPAPER) != 0) {
            mPendingActions |= WALLPAPER_ACTION_PENDING;
        }

        testTokenMayBeDrawnLocked();
    }

    // TODO(cmautner): Change the following comment when no longer locked on mWindowMap */
    /** Locked on mService.mWindowMap and this. */
    private void animateLocked() {
        if (!mInitialized) {
            return;
        }
        for (int i = mWinAnimatorLists.size() - 1; i >= 0; i--) {
            animateLocked(mWinAnimatorLists.get(i));
        }
    }

    private void animateLocked(final WinAnimatorList winAnimatorList) {
        mPendingLayoutChanges = 0;
        mCurrentTime = SystemClock.uptimeMillis();
        mBulkUpdateParams = SET_ORIENTATION_CHANGE_COMPLETE;
        boolean wasAnimating = mAnimating;
        mAnimating = false;
        if (WindowManagerService.DEBUG_WINDOW_TRACE) {
            Slog.i(TAG, "!!! animate: entry time=" + mCurrentTime);
        }

        // Update animations of all applications, including those
        // associated with exiting/removed apps
        Surface.openTransaction();

        try {
            updateWindowsAppsAndRotationAnimationsLocked();
            performAnimationsLocked(winAnimatorList);

            // THIRD LOOP: Update the surfaces of all windows.

            if (mScreenRotationAnimation != null) {
                mScreenRotationAnimation.updateSurfaces();
            }

            final int N = winAnimatorList.size();
            for (int i = 0; i < N; i++) {
                winAnimatorList.get(i).prepareSurfaceLocked(true);
            }

            if (mDimParams != null) {
                mDimAnimator.updateParameters(mContext.getResources(), mDimParams, mCurrentTime);
            }
            if (mDimAnimator != null && mDimAnimator.mDimShown) {
                mAnimating |= mDimAnimator.updateSurface(isDimming(), mCurrentTime,
                        !mService.okToDisplay());
            }

            if (mService.mBlackFrame != null) {
                if (mScreenRotationAnimation != null) {
                    mService.mBlackFrame.setMatrix(
                            mScreenRotationAnimation.getEnterTransformation().getMatrix());
                } else {
                    mService.mBlackFrame.clearMatrix();
                }
            }

            if (mService.mWatermark != null) {
                mService.mWatermark.drawIfNeeded();
            }
        } catch (RuntimeException e) {
            Log.wtf(TAG, "Unhandled exception in Window Manager", e);
        } finally {
            Surface.closeTransaction();
        }

        if (mBulkUpdateParams != 0 || mPendingLayoutChanges != 0) {
            updateAnimToLayoutLocked();
        }

        if (mAnimating) {
            synchronized (mService.mLayoutToAnim) {
                mService.scheduleAnimationLocked();
            }
        } else if (wasAnimating) {
            mService.requestTraversalLocked();
        }
        if (WindowManagerService.DEBUG_WINDOW_TRACE) {
            Slog.i(TAG, "!!! animate: exit mAnimating=" + mAnimating
                + " mBulkUpdateParams=" + Integer.toHexString(mBulkUpdateParams)
                + " mPendingLayoutChanges=" + Integer.toHexString(mPendingLayoutChanges));
        }
    }

    WindowState mCurrentFocus;
    void setCurrentFocus(final WindowState currentFocus) {
        mCurrentFocus = currentFocus;
    }

    void setDisplayDimensions(final int curWidth, final int curHeight,
                        final int appWidth, final int appHeight) {
        mDw = curWidth;
        mDh = curHeight;
        mInnerDw = appWidth;
        mInnerDh = appHeight;
    }

    boolean isDimming() {
        return mDimParams != null;
    }

    boolean isDimming(final WindowStateAnimator winAnimator) {
        return mDimParams != null && mDimParams.mDimWinAnimator == winAnimator;
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (dumpAll) {
            if (mWindowDetachedWallpaper != null) {
                pw.print(prefix); pw.print("mWindowDetachedWallpaper=");
                        pw.println(mWindowDetachedWallpaper);
            }
            pw.print(prefix); pw.print("mAnimTransactionSequence=");
                    pw.println(mAnimTransactionSequence);
            if (mWindowAnimationBackgroundSurface != null) {
                pw.print(prefix); pw.print("mWindowAnimationBackgroundSurface:");
                        mWindowAnimationBackgroundSurface.printTo(prefix + "  ", pw);
            }
            if (mDimAnimator != null) {
                pw.print(prefix); pw.print("mDimAnimator:");
                mDimAnimator.printTo(prefix + "  ", pw);
            } else {
                pw.print(prefix); pw.print("no DimAnimator ");
            }
        }
    }

    static class SetAnimationParams {
        final WindowStateAnimator mWinAnimator;
        final Animation mAnimation;
        final int mAnimDw;
        final int mAnimDh;
        public SetAnimationParams(final WindowStateAnimator winAnimator,
                                  final Animation animation, final int animDw, final int animDh) {
            mWinAnimator = winAnimator;
            mAnimation = animation;
            mAnimDw = animDw;
            mAnimDh = animDh;
        }
    }

    synchronized void clearPendingActions() {
        mPendingActions = 0;
    }
}