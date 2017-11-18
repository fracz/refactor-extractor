/*
 * Copyright (C) 2011 The Android Open Source Project
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

import static android.view.WindowManagerInternal.AppTransitionListener;
import static com.android.internal.R.styleable.WindowAnimation_activityCloseEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_activityCloseExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_activityOpenEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_activityOpenExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_launchTaskBehindSourceAnimation;
import static com.android.internal.R.styleable.WindowAnimation_launchTaskBehindTargetAnimation;
import static com.android.internal.R.styleable.WindowAnimation_taskCloseEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_taskCloseExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_taskOpenEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_taskOpenExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_taskToBackEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_taskToBackExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_taskToFrontEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_taskToFrontExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_wallpaperCloseEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_wallpaperCloseExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_wallpaperIntraCloseEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_wallpaperIntraCloseExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_wallpaperIntraOpenEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_wallpaperIntraOpenExitAnimation;
import static com.android.internal.R.styleable.WindowAnimation_wallpaperOpenEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_wallpaperOpenExitAnimation;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WITH_CLASS_NAME;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;

import android.annotation.Nullable;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Debug;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.util.Slog;
import android.util.SparseArray;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ClipRectAnimation;
import android.view.animation.ClipRectLRAnimation;
import android.view.animation.ClipRectTBAnimation;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.android.internal.util.DumpUtils.Dump;
import com.android.server.AttributeCache;
import com.android.server.wm.WindowManagerService.H;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// State management of app transitions.  When we are preparing for a
// transition, mNextAppTransition will be the kind of transition to
// perform or TRANSIT_NONE if we are not waiting.  If we are waiting,
// mOpeningApps and mClosingApps are the lists of tokens that will be
// made visible or hidden at the next transition.
public class AppTransition implements Dump {
    private static final String TAG = TAG_WITH_CLASS_NAME ? "AppTransition" : TAG_WM;
    private static final boolean DEBUG_APP_TRANSITIONS =
            WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS;
    private static final boolean DEBUG_ANIM = WindowManagerDebugConfig.DEBUG_ANIM;
    private static final int CLIP_REVEAL_TRANSLATION_Y_DP = 8;

    /** Not set up for a transition. */
    public static final int TRANSIT_UNSET = -1;
    /** No animation for transition. */
    public static final int TRANSIT_NONE = 0;
    /** A window in a new activity is being opened on top of an existing one in the same task. */
    public static final int TRANSIT_ACTIVITY_OPEN = 6;
    /** The window in the top-most activity is being closed to reveal the
     * previous activity in the same task. */
    public static final int TRANSIT_ACTIVITY_CLOSE = 7;
    /** A window in a new task is being opened on top of an existing one
     * in another activity's task. */
    public static final int TRANSIT_TASK_OPEN = 8;
    /** A window in the top-most activity is being closed to reveal the
     * previous activity in a different task. */
    public static final int TRANSIT_TASK_CLOSE = 9;
    /** A window in an existing task is being displayed on top of an existing one
     * in another activity's task. */
    public static final int TRANSIT_TASK_TO_FRONT = 10;
    /** A window in an existing task is being put below all other tasks. */
    public static final int TRANSIT_TASK_TO_BACK = 11;
    /** A window in a new activity that doesn't have a wallpaper is being opened on top of one that
     * does, effectively closing the wallpaper. */
    public static final int TRANSIT_WALLPAPER_CLOSE = 12;
    /** A window in a new activity that does have a wallpaper is being opened on one that didn't,
     * effectively opening the wallpaper. */
    public static final int TRANSIT_WALLPAPER_OPEN = 13;
    /** A window in a new activity is being opened on top of an existing one, and both are on top
     * of the wallpaper. */
    public static final int TRANSIT_WALLPAPER_INTRA_OPEN = 14;
    /** The window in the top-most activity is being closed to reveal the previous activity, and
     * both are on top of the wallpaper. */
    public static final int TRANSIT_WALLPAPER_INTRA_CLOSE = 15;
    /** A window in a new task is being opened behind an existing one in another activity's task.
     * The new window will show briefly and then be gone. */
    public static final int TRANSIT_TASK_OPEN_BEHIND = 16;
    /** A window in a task is being animated in-place. */
    public static final int TRANSIT_TASK_IN_PLACE = 17;
    /** An activity is being relaunched (e.g. due to configuration change). */
    public static final int TRANSIT_ACTIVITY_RELAUNCH = 18;

    /** Fraction of animation at which the recents thumbnail stays completely transparent */
    private static final float RECENTS_THUMBNAIL_FADEIN_FRACTION = 0.5f;
    /** Fraction of animation at which the recents thumbnail becomes completely transparent */
    private static final float RECENTS_THUMBNAIL_FADEOUT_FRACTION = 0.5f;

    private static final int DEFAULT_APP_TRANSITION_DURATION = 336;
    private static final int THUMBNAIL_APP_TRANSITION_DURATION = 336;
    private static final int THUMBNAIL_APP_TRANSITION_ALPHA_DURATION = 336;
    private static final long APP_TRANSITION_TIMEOUT_MS = 5000;

    private final Context mContext;
    private final WindowManagerService mService;

    private int mNextAppTransition = TRANSIT_UNSET;

    private static final int NEXT_TRANSIT_TYPE_NONE = 0;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM = 1;
    private static final int NEXT_TRANSIT_TYPE_SCALE_UP = 2;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP = 3;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN = 4;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP = 5;
    private static final int NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN = 6;
    private static final int NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE = 7;
    private static final int NEXT_TRANSIT_TYPE_CLIP_REVEAL = 8;
    private int mNextAppTransitionType = NEXT_TRANSIT_TYPE_NONE;

    // These are the possible states for the enter/exit activities during a thumbnail transition
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_UP = 0;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_UP = 1;
    private static final int THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN = 2;
    private static final int THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN = 3;

    private String mNextAppTransitionPackage;
    // Used for thumbnail transitions. True if we're scaling up, false if scaling down
    private boolean mNextAppTransitionScaleUp;
    private IRemoteCallback mNextAppTransitionCallback;
    private IRemoteCallback mNextAppTransitionFutureCallback;
    private IRemoteCallback mAnimationFinishedCallback;
    private int mNextAppTransitionEnter;
    private int mNextAppTransitionExit;
    private int mNextAppTransitionInPlace;

    // Keyed by task id.
    private final SparseArray<AppTransitionAnimationSpec> mNextAppTransitionAnimationsSpecs
            = new SparseArray<>();
    private IAppTransitionAnimationSpecsFuture mNextAppTransitionAnimationsSpecsFuture;
    private boolean mNextAppTransitionAnimationsSpecsPending;
    private AppTransitionAnimationSpec mDefaultNextAppTransitionAnimationSpec;

    private Rect mNextAppTransitionInsets = new Rect();

    private Rect mTmpFromClipRect = new Rect();
    private Rect mTmpToClipRect = new Rect();

    private final Rect mTmpRect = new Rect();

    private final static int APP_STATE_IDLE = 0;
    private final static int APP_STATE_READY = 1;
    private final static int APP_STATE_RUNNING = 2;
    private final static int APP_STATE_TIMEOUT = 3;
    private int mAppTransitionState = APP_STATE_IDLE;

    private final int mConfigShortAnimTime;
    private final Interpolator mDecelerateInterpolator;
    private final Interpolator mThumbnailFadeInInterpolator;
    private final Interpolator mThumbnailFadeOutInterpolator;
    private final Interpolator mLinearOutSlowInInterpolator;
    private final Interpolator mFastOutLinearInInterpolator;
    private final Interpolator mClipHorizontalInterpolator = new PathInterpolator(0, 0, 0.4f, 1f);

    /** Interpolator to be used for animations that respond directly to a touch */
    private final Interpolator mTouchResponseInterpolator =
            new PathInterpolator(0.3f, 0f, 0.1f, 1f);

    private final int mClipRevealTranslationY;

    private int mCurrentUserId = 0;

    private final ArrayList<AppTransitionListener> mListeners = new ArrayList<>();
    private final ExecutorService mDefaultExecutor = Executors.newSingleThreadExecutor();

    AppTransition(Context context, WindowManagerService service) {
        mContext = context;
        mService = service;
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.linear_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.fast_out_linear_in);
        mConfigShortAnimTime = context.getResources().getInteger(
                com.android.internal.R.integer.config_shortAnimTime);
        mDecelerateInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.decelerate_cubic);
        mThumbnailFadeInInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                // Linear response for first fraction, then complete after that.
                if (input < RECENTS_THUMBNAIL_FADEIN_FRACTION) {
                    return 0f;
                }
                float t = (input - RECENTS_THUMBNAIL_FADEIN_FRACTION) /
                        (1f - RECENTS_THUMBNAIL_FADEIN_FRACTION);
                return mFastOutLinearInInterpolator.getInterpolation(t);
            }
        };
        mThumbnailFadeOutInterpolator = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                // Linear response for first fraction, then complete after that.
                if (input < RECENTS_THUMBNAIL_FADEOUT_FRACTION) {
                    float t = input / RECENTS_THUMBNAIL_FADEOUT_FRACTION;
                    return mLinearOutSlowInInterpolator.getInterpolation(t);
                }
                return 1f;
            }
        };
        mClipRevealTranslationY = (int) (CLIP_REVEAL_TRANSLATION_Y_DP
                * mContext.getResources().getDisplayMetrics().density);
    }

    boolean isTransitionSet() {
        return mNextAppTransition != TRANSIT_UNSET;
    }

    boolean isTransitionEqual(int transit) {
        return mNextAppTransition == transit;
    }

    int getAppTransition() {
        return mNextAppTransition;
     }

    private void setAppTransition(int transit) {
        mNextAppTransition = transit;
    }

    boolean isReady() {
        return mAppTransitionState == APP_STATE_READY
                || mAppTransitionState == APP_STATE_TIMEOUT;
    }

    void setReady() {
        mAppTransitionState = APP_STATE_READY;
        fetchAppTransitionSpecsFromFuture();
    }

    boolean isRunning() {
        return mAppTransitionState == APP_STATE_RUNNING;
    }

    void setIdle() {
        mAppTransitionState = APP_STATE_IDLE;
    }

    boolean isTimeout() {
        return mAppTransitionState == APP_STATE_TIMEOUT;
    }

    void setTimeout() {
        mAppTransitionState = APP_STATE_TIMEOUT;
    }

    Bitmap getAppTransitionThumbnailHeader(int taskId) {
        AppTransitionAnimationSpec spec = mNextAppTransitionAnimationsSpecs.get(taskId);
        if (spec == null) {
            spec = mDefaultNextAppTransitionAnimationSpec;
        }
        return spec != null ? spec.bitmap : null;
    }

    /** Returns whether the next thumbnail transition is aspect scaled up. */
    boolean isNextThumbnailTransitionAspectScaled() {
        return mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP ||
                mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
    }

    /** Returns whether the next thumbnail transition is scaling up. */
    boolean isNextThumbnailTransitionScaleUp() {
        return mNextAppTransitionScaleUp;
    }

    /**
     * @return true if and only if we are currently fetching app transition specs from the future
     *         passed into {@link #overridePendingAppTransitionMultiThumbFuture}
     */
    boolean isFetchingAppTransitionsSpecs() {
        return mNextAppTransitionAnimationsSpecsPending;
    }

    private boolean prepare() {
        if (!isRunning()) {
            mAppTransitionState = APP_STATE_IDLE;
            notifyAppTransitionPendingLocked();
            return true;
        }
        return false;
    }

    void goodToGo(AppWindowAnimator openingAppAnimator, AppWindowAnimator closingAppAnimator) {
        mNextAppTransition = TRANSIT_UNSET;
        mAppTransitionState = APP_STATE_RUNNING;
        notifyAppTransitionStartingLocked(
                openingAppAnimator != null ? openingAppAnimator.mAppToken.token : null,
                closingAppAnimator != null ? closingAppAnimator.mAppToken.token : null,
                openingAppAnimator != null ? openingAppAnimator.animation : null,
                closingAppAnimator != null ? closingAppAnimator.animation : null);
    }

    void clear() {
        mNextAppTransitionType = NEXT_TRANSIT_TYPE_NONE;
        mNextAppTransitionPackage = null;
        mNextAppTransitionAnimationsSpecs.clear();
        mNextAppTransitionAnimationsSpecsFuture = null;
        mDefaultNextAppTransitionAnimationSpec = null;
        mAnimationFinishedCallback = null;
    }

    void freeze() {
        setAppTransition(AppTransition.TRANSIT_UNSET);
        clear();
        setReady();
        notifyAppTransitionCancelledLocked();
    }

    void registerListenerLocked(AppTransitionListener listener) {
        mListeners.add(listener);
    }

    public void notifyAppTransitionFinishedLocked(IBinder token) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onAppTransitionFinishedLocked(token);
        }
    }

    private void notifyAppTransitionPendingLocked() {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onAppTransitionPendingLocked();
        }
    }

    private void notifyAppTransitionCancelledLocked() {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onAppTransitionCancelledLocked();
        }
    }

    private void notifyAppTransitionStartingLocked(IBinder openToken,
            IBinder closeToken, Animation openAnimation, Animation closeAnimation) {
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onAppTransitionStartingLocked(openToken, closeToken, openAnimation,
                    closeAnimation);
        }
    }

    private AttributeCache.Entry getCachedAnimations(WindowManager.LayoutParams lp) {
        if (DEBUG_ANIM) Slog.v(TAG, "Loading animations: layout params pkg="
                + (lp != null ? lp.packageName : null)
                + " resId=0x" + (lp != null ? Integer.toHexString(lp.windowAnimations) : null));
        if (lp != null && lp.windowAnimations != 0) {
            // If this is a system resource, don't try to load it from the
            // application resources.  It is nice to avoid loading application
            // resources if we can.
            String packageName = lp.packageName != null ? lp.packageName : "android";
            int resId = lp.windowAnimations;
            if ((resId&0xFF000000) == 0x01000000) {
                packageName = "android";
            }
            if (DEBUG_ANIM) Slog.v(TAG, "Loading animations: picked package="
                    + packageName);
            return AttributeCache.instance().get(packageName, resId,
                    com.android.internal.R.styleable.WindowAnimation, mCurrentUserId);
        }
        return null;
    }

    private AttributeCache.Entry getCachedAnimations(String packageName, int resId) {
        if (DEBUG_ANIM) Slog.v(TAG, "Loading animations: package="
                + packageName + " resId=0x" + Integer.toHexString(resId));
        if (packageName != null) {
            if ((resId&0xFF000000) == 0x01000000) {
                packageName = "android";
            }
            if (DEBUG_ANIM) Slog.v(TAG, "Loading animations: picked package="
                    + packageName);
            return AttributeCache.instance().get(packageName, resId,
                    com.android.internal.R.styleable.WindowAnimation, mCurrentUserId);
        }
        return null;
    }

    Animation loadAnimationAttr(WindowManager.LayoutParams lp, int animAttr) {
        int anim = 0;
        Context context = mContext;
        if (animAttr >= 0) {
            AttributeCache.Entry ent = getCachedAnimations(lp);
            if (ent != null) {
                context = ent.context;
                anim = ent.array.getResourceId(animAttr, 0);
            }
        }
        if (anim != 0) {
            return AnimationUtils.loadAnimation(context, anim);
        }
        return null;
    }

    Animation loadAnimationRes(WindowManager.LayoutParams lp, int resId) {
        Context context = mContext;
        if (resId >= 0) {
            AttributeCache.Entry ent = getCachedAnimations(lp);
            if (ent != null) {
                context = ent.context;
            }
            return AnimationUtils.loadAnimation(context, resId);
        }
        return null;
    }

    private Animation loadAnimationRes(String packageName, int resId) {
        int anim = 0;
        Context context = mContext;
        if (resId >= 0) {
            AttributeCache.Entry ent = getCachedAnimations(packageName, resId);
            if (ent != null) {
                context = ent.context;
                anim = resId;
            }
        }
        if (anim != 0) {
            return AnimationUtils.loadAnimation(context, anim);
        }
        return null;
    }

    /**
     * Compute the pivot point for an animation that is scaling from a small
     * rect on screen to a larger rect.  The pivot point varies depending on
     * the distance between the inner and outer edges on both sides.  This
     * function computes the pivot point for one dimension.
     * @param startPos  Offset from left/top edge of outer rectangle to
     * left/top edge of inner rectangle.
     * @param finalScale The scaling factor between the size of the outer
     * and inner rectangles.
     */
    private static float computePivot(int startPos, float finalScale) {
        final float denom = finalScale-1;
        if (Math.abs(denom) < .0001f) {
            return startPos;
        }
        return -startPos / denom;
    }

    private Animation createScaleUpAnimationLocked(int transit, boolean enter,
            Rect containingFrame) {
        Animation a;
        getDefaultNextAppTransitionStartRect(mTmpRect);
        final int appWidth = containingFrame.width();
        final int appHeight = containingFrame.height();
        if (enter) {
            // Entering app zooms out from the center of the initial rect.
            float scaleW = mTmpRect.width() / (float) appWidth;
            float scaleH = mTmpRect.height() / (float) appHeight;
            Animation scale = new ScaleAnimation(scaleW, 1, scaleH, 1,
                    computePivot(mTmpRect.left, scaleW),
                    computePivot(mTmpRect.right, scaleH));
            scale.setInterpolator(mDecelerateInterpolator);

            Animation alpha = new AlphaAnimation(0, 1);
            alpha.setInterpolator(mThumbnailFadeOutInterpolator);

            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            set.setDetachWallpaper(true);
            a = set;
        } else  if (transit == TRANSIT_WALLPAPER_INTRA_OPEN ||
                    transit == TRANSIT_WALLPAPER_INTRA_CLOSE) {
            // If we are on top of the wallpaper, we need an animation that
            // correctly handles the wallpaper staying static behind all of
            // the animated elements.  To do this, will just have the existing
            // element fade out.
            a = new AlphaAnimation(1, 0);
            a.setDetachWallpaper(true);
        } else {
            // For normal animations, the exiting element just holds in place.
            a = new AlphaAnimation(1, 1);
        }

        // Pick the desired duration.  If this is an inter-activity transition,
        // it  is the standard duration for that.  Otherwise we use the longer
        // task transition duration.
        final long duration;
        switch (transit) {
            case TRANSIT_ACTIVITY_OPEN:
            case TRANSIT_ACTIVITY_CLOSE:
                duration = mConfigShortAnimTime;
                break;
            default:
                duration = DEFAULT_APP_TRANSITION_DURATION;
                break;
        }
        a.setDuration(duration);
        a.setFillAfter(true);
        a.setInterpolator(mDecelerateInterpolator);
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    private void getDefaultNextAppTransitionStartRect(Rect rect) {
        if (mDefaultNextAppTransitionAnimationSpec == null ||
                mDefaultNextAppTransitionAnimationSpec.rect == null) {
            Slog.wtf(TAG, "Starting rect for app requested, but none available", new Throwable());
            rect.setEmpty();
        } else {
            rect.set(mDefaultNextAppTransitionAnimationSpec.rect);
        }
    }

    void getNextAppTransitionStartRect(int taskId, Rect rect) {
        AppTransitionAnimationSpec spec = mNextAppTransitionAnimationsSpecs.get(taskId);
        if (spec == null) {
            spec = mDefaultNextAppTransitionAnimationSpec;
        }
        if (spec == null || spec.rect == null) {
            Slog.wtf(TAG, "Starting rect for task: " + taskId + " requested, but not available",
                    new Throwable());
            rect.setEmpty();
        } else {
            rect.set(spec.rect);
        }
    }

    private void putDefaultNextAppTransitionCoordinates(int left, int top, int width, int height,
            Bitmap bitmap) {
        mDefaultNextAppTransitionAnimationSpec = new AppTransitionAnimationSpec(-1 /* taskId */,
                bitmap, new Rect(left, top, left + width, top + height));
    }

    private Animation createClipRevealAnimationLocked(int transit, boolean enter, Rect appFrame) {
        final Animation anim;
        if (enter) {
            // Reveal will expand and move faster in horizontal direction

            final int appWidth = appFrame.width();
            final int appHeight = appFrame.height();
            // mTmpRect will contain an area around the launcher icon that was pressed. We will
            // clip reveal from that area in the final area of the app.
            getDefaultNextAppTransitionStartRect(mTmpRect);

            float t = 0f;
            if (appHeight > 0) {
                t = (float) mTmpRect.left / appHeight;
            }
            int translationY = mClipRevealTranslationY + (int)(appHeight / 7f * t);

            int centerX = mTmpRect.centerX();
            int centerY = mTmpRect.centerY();
            int halfWidth = mTmpRect.width() / 2;
            int halfHeight = mTmpRect.height() / 2;

            // Clip third of the from size of launch icon, expand to full width/height
            Animation clipAnimLR = new ClipRectLRAnimation(
                    centerX - halfWidth, centerX + halfWidth, 0, appWidth);
            clipAnimLR.setInterpolator(mClipHorizontalInterpolator);
            clipAnimLR.setDuration((long) (DEFAULT_APP_TRANSITION_DURATION / 2.5f));

            Animation clipAnimTB = new ClipRectTBAnimation(centerY - halfHeight - translationY,
                    centerY + halfHeight/ 2 - translationY, 0, appHeight);
            clipAnimTB.setInterpolator(mTouchResponseInterpolator);
            clipAnimTB.setDuration(DEFAULT_APP_TRANSITION_DURATION);

            // We might be animating entrance of a docked task, so we need the translate to account
            // for the app frame in which the window will reside. Every other calculation here
            // is performed as if the window started at 0,0.
            translationY -= appFrame.top;
            TranslateAnimation translate = new TranslateAnimation(-appFrame.left, 0, translationY,
                    0);
            translate.setInterpolator(mLinearOutSlowInInterpolator);
            translate.setDuration(DEFAULT_APP_TRANSITION_DURATION);

            // Quick fade-in from icon to app window
            final int alphaDuration = DEFAULT_APP_TRANSITION_DURATION / 4;
            AlphaAnimation alpha = new AlphaAnimation(0.5f, 1);
            alpha.setDuration(alphaDuration);
            alpha.setInterpolator(mLinearOutSlowInInterpolator);

            AnimationSet set = new AnimationSet(false);
            set.addAnimation(clipAnimLR);
            set.addAnimation(clipAnimTB);
            set.addAnimation(translate);
            set.addAnimation(alpha);
            set.setZAdjustment(Animation.ZORDER_TOP);
            set.initialize(appWidth, appHeight, appWidth, appHeight);
            anim = set;
        } else {
            final long duration;
            switch (transit) {
                case TRANSIT_ACTIVITY_OPEN:
                case TRANSIT_ACTIVITY_CLOSE:
                    duration = mConfigShortAnimTime;
                    break;
                default:
                    duration = DEFAULT_APP_TRANSITION_DURATION;
                    break;
            }
            if (transit == TRANSIT_WALLPAPER_INTRA_OPEN ||
                    transit == TRANSIT_WALLPAPER_INTRA_CLOSE) {
                // If we are on top of the wallpaper, we need an animation that
                // correctly handles the wallpaper staying static behind all of
                // the animated elements.  To do this, will just have the existing
                // element fade out.
                anim = new AlphaAnimation(1, 0);
                anim.setDetachWallpaper(true);
            } else {
                // For normal animations, the exiting element just holds in place.
                anim = new AlphaAnimation(1, 1);
            }
            anim.setInterpolator(mDecelerateInterpolator);
            anim.setDuration(duration);
            anim.setFillAfter(true);
        }
        return anim;
    }

    /**
     * Prepares the specified animation with a standard duration, interpolator, etc.
     */
    Animation prepareThumbnailAnimationWithDuration(Animation a, int appWidth, int appHeight,
            int duration, Interpolator interpolator) {
        if (duration > 0) {
            a.setDuration(duration);
        }
        a.setFillAfter(true);
        a.setInterpolator(interpolator);
        a.initialize(appWidth, appHeight, appWidth, appHeight);
        return a;
    }

    /**
     * Prepares the specified animation with a standard duration, interpolator, etc.
     */
    Animation prepareThumbnailAnimation(Animation a, int appWidth, int appHeight, int transit) {
        // Pick the desired duration.  If this is an inter-activity transition,
        // it  is the standard duration for that.  Otherwise we use the longer
        // task transition duration.
        final int duration;
        switch (transit) {
            case TRANSIT_ACTIVITY_OPEN:
            case TRANSIT_ACTIVITY_CLOSE:
                duration = mConfigShortAnimTime;
                break;
            default:
                duration = DEFAULT_APP_TRANSITION_DURATION;
                break;
        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, duration,
                mDecelerateInterpolator);
    }

    /**
     * Return the current thumbnail transition state.
     */
    int getThumbnailTransitionState(boolean enter) {
        if (enter) {
            if (mNextAppTransitionScaleUp) {
                return THUMBNAIL_TRANSITION_ENTER_SCALE_UP;
            } else {
                return THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN;
            }
        } else {
            if (mNextAppTransitionScaleUp) {
                return THUMBNAIL_TRANSITION_EXIT_SCALE_UP;
            } else {
                return THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN;
            }
        }
    }

    /**
     * This animation runs for the thumbnail that gets cross faded with the enter/exit activity
     * when a thumbnail is specified with the pending animation override.
     */
    Animation createThumbnailAspectScaleAnimationLocked(Rect appRect, Bitmap thumbnailHeader,
            final int taskId) {
        Animation a;
        final int thumbWidthI = thumbnailHeader.getWidth();
        final float thumbWidth = thumbWidthI > 0 ? thumbWidthI : 1;
        final int thumbHeightI = thumbnailHeader.getHeight();
        final float thumbHeight = thumbHeightI > 0 ? thumbHeightI : 1;
        final int appWidth = appRect.width();

        float scaleW = appWidth / thumbWidth;
        float unscaledHeight = thumbHeight * scaleW;
        getNextAppTransitionStartRect(taskId, mTmpRect);
        float unscaledStartY = mTmpRect.top - (unscaledHeight - thumbHeight) / 2f;
        if (mNextAppTransitionScaleUp) {
            // Animation up from the thumbnail to the full screen
            Animation scale = new ScaleAnimation(1f, scaleW, 1f, scaleW,
                    mTmpRect.left + (thumbWidth / 2f), mTmpRect.top + (thumbHeight / 2f));
            scale.setInterpolator(mTouchResponseInterpolator);
            scale.setDuration(THUMBNAIL_APP_TRANSITION_DURATION);
            Animation alpha = new AlphaAnimation(1, 0);
            alpha.setInterpolator(mThumbnailFadeOutInterpolator);
            alpha.setDuration(THUMBNAIL_APP_TRANSITION_ALPHA_DURATION);
            final float toX = appRect.left + appRect.width() / 2 -
                    (mTmpRect.left + thumbWidth / 2);
            final float toY = appRect.top + mNextAppTransitionInsets.top + -unscaledStartY;
            Animation translate = new TranslateAnimation(0, toX, 0, toY);
            translate.setInterpolator(mTouchResponseInterpolator);
            translate.setDuration(THUMBNAIL_APP_TRANSITION_DURATION);

            // This AnimationSet uses the Interpolators assigned above.
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            set.addAnimation(translate);
            a = set;
        } else {
            // Animation down from the full screen to the thumbnail
            Animation scale = new ScaleAnimation(scaleW, 1f, scaleW, 1f,
                    mTmpRect.left + (thumbWidth / 2f), mTmpRect.top + (thumbHeight / 2f));
            scale.setInterpolator(mTouchResponseInterpolator);
            scale.setDuration(THUMBNAIL_APP_TRANSITION_DURATION);
            Animation alpha = new AlphaAnimation(0f, 1f);
            alpha.setInterpolator(mThumbnailFadeInInterpolator);
            alpha.setDuration(THUMBNAIL_APP_TRANSITION_ALPHA_DURATION);
            Animation translate = new TranslateAnimation(0, 0, -unscaledStartY +
                    mNextAppTransitionInsets.top, 0);
            translate.setInterpolator(mTouchResponseInterpolator);
            translate.setDuration(THUMBNAIL_APP_TRANSITION_DURATION);

            // This AnimationSet uses the Interpolators assigned above.
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            set.addAnimation(translate);
            a = set;

        }
        return prepareThumbnailAnimationWithDuration(a, appWidth, appRect.height(), 0,
                mTouchResponseInterpolator);
    }

    /**
     * This alternate animation is created when we are doing a thumbnail transition, for the
     * activity that is leaving, and the activity that is entering.
     */
    Animation createAspectScaledThumbnailEnterExitAnimationLocked(int thumbTransitState,
            int orientation, int transit, Rect containingFrame, Rect contentInsets,
            @Nullable Rect surfaceInsets, boolean freeform, int taskId) {
        Animation a;
        final int appWidth = containingFrame.width();
        final int appHeight = containingFrame.height();
        getDefaultNextAppTransitionStartRect(mTmpRect);
        final int thumbWidthI = mTmpRect.width();
        final float thumbWidth = thumbWidthI > 0 ? thumbWidthI : 1;
        final int thumbHeightI = mTmpRect.height();
        final float thumbHeight = thumbHeightI > 0 ? thumbHeightI : 1;

        // Used for the ENTER_SCALE_UP and EXIT_SCALE_DOWN transitions
        float scale = 1f;
        int scaledTopDecor = 0;

        switch (thumbTransitState) {
            case THUMBNAIL_TRANSITION_ENTER_SCALE_UP: {
                if (freeform) {
                    a = createAspectScaledThumbnailEnterFreeformAnimationLocked(
                            containingFrame, surfaceInsets, taskId);
                } else {
                    mTmpFromClipRect.set(containingFrame);
                    // exclude top screen decor (status bar) region from the source clip.
                    mTmpFromClipRect.top = contentInsets.top;
                    // App window scaling up to become full screen
                    mTmpToClipRect.set(containingFrame);
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        // In portrait, we scale the width and clip to the top/left square
                        scale = thumbWidth / appWidth;
                        scaledTopDecor = (int) (scale * contentInsets.top);
                        int unscaledThumbHeight = (int) (thumbHeight / scale);
                        mTmpFromClipRect.bottom = mTmpFromClipRect.top + unscaledThumbHeight;
                    } else {
                        // In landscape, we scale the height and clip to the top/left square. We
                        // only scale the part that is not covered by status bar and the nav bar.
                        scale = thumbHeight / (appHeight - contentInsets.top
                                - contentInsets.bottom);
                        scaledTopDecor = (int) (scale * contentInsets.top);
                        int unscaledThumbWidth = (int) (thumbWidth / scale);
                        mTmpFromClipRect.right = mTmpFromClipRect.left + unscaledThumbWidth;
                        // This removes the navigation bar from the first frame, so it better
                        // matches the thumbnail. We need to do this explicitly in landscape,
                        // because in portrait we already crop vertically.
                        mTmpFromClipRect.bottom = mTmpFromClipRect.bottom - contentInsets.bottom;
                    }

                    mNextAppTransitionInsets.set(contentInsets);

                    Animation scaleAnim = new ScaleAnimation(scale, 1, scale, 1,
                            computePivot(mTmpRect.left, scale),
                            computePivot(mTmpRect.top, scale));
                    Animation clipAnim = new ClipRectAnimation(mTmpFromClipRect, mTmpToClipRect);
                    Animation translateAnim = new TranslateAnimation(0, 0, -scaledTopDecor, 0);

                    AnimationSet set = new AnimationSet(true);
                    set.addAnimation(clipAnim);
                    set.addAnimation(scaleAnim);
                    set.addAnimation(translateAnim);
                    a = set;
                }
                break;
            }
            case THUMBNAIL_TRANSITION_EXIT_SCALE_UP: {
                // Previous app window during the scale up
                if (transit == TRANSIT_WALLPAPER_INTRA_OPEN) {
                    // Fade out the source activity if we are animating to a wallpaper
                    // activity.
                    a = new AlphaAnimation(1, 0);
                } else {
                    a = new AlphaAnimation(1, 1);
                }
                break;
            }
            case THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN: {
                // Target app window during the scale down
                if (transit == TRANSIT_WALLPAPER_INTRA_OPEN) {
                    // Fade in the destination activity if we are animating from a wallpaper
                    // activity.
                    a = new AlphaAnimation(0, 1);
                } else {
                    a = new AlphaAnimation(1, 1);
                }
                break;
            }
            case THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN: {
                // App window scaling down from full screen
                if (freeform) {
                    a = createAspectScaledThumbnailExitFreeformAnimationLocked(
                            containingFrame, surfaceInsets, taskId);
                } else {
                    mTmpFromClipRect.set(containingFrame);
                    mTmpToClipRect.set(containingFrame);
                    // exclude top screen decor (status bar) region from the destination clip.
                    mTmpToClipRect.top = contentInsets.top;
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        // In portrait, we scale the width and clip to the top/left square
                        scale = thumbWidth / appWidth;
                        scaledTopDecor = (int) (scale * contentInsets.top);
                        int unscaledThumbHeight = (int) (thumbHeight / scale);
                        mTmpToClipRect.bottom = mTmpToClipRect.top + unscaledThumbHeight;
                    } else {
                        // In landscape, we scale the height and clip to the top/left square. We only
                        // scale the part that is not covered by status bar and the nav bar.
                        scale = thumbHeight / (appHeight - contentInsets.top - contentInsets.bottom);
                        scaledTopDecor = (int) (scale * contentInsets.top);
                        int unscaledThumbWidth = (int) (thumbWidth / scale);
                        mTmpToClipRect.right = mTmpToClipRect.left + unscaledThumbWidth;
                        // This removes the navigation bar from the last frame, so it better matches the
                        // thumbnail. We need to do this explicitly in landscape, because in portrait we
                        // already crop vertically.
                        mTmpToClipRect.bottom = mTmpToClipRect.bottom - contentInsets.bottom;
                    }

                    mNextAppTransitionInsets.set(contentInsets);

                    Animation scaleAnim = new ScaleAnimation(1, scale, 1, scale,
                            computePivot(mTmpRect.left, scale),
                            computePivot(mTmpRect.top, scale));
                    Animation clipAnim = new ClipRectAnimation(mTmpFromClipRect, mTmpToClipRect);
                    Animation translateAnim = new TranslateAnimation(0, 0, 0, -scaledTopDecor);

                    AnimationSet set = new AnimationSet(true);
                    set.addAnimation(clipAnim);
                    set.addAnimation(scaleAnim);
                    set.addAnimation(translateAnim);

                    a = set;
                    a.setZAdjustment(Animation.ZORDER_TOP);
                }
                break;
            }
            default:
                throw new RuntimeException("Invalid thumbnail transition state");
        }

        int duration = Math.max(THUMBNAIL_APP_TRANSITION_ALPHA_DURATION,
                THUMBNAIL_APP_TRANSITION_DURATION);
        return prepareThumbnailAnimationWithDuration(a, appWidth, appHeight, duration,
                mTouchResponseInterpolator);
    }

    private Animation createAspectScaledThumbnailEnterFreeformAnimationLocked(Rect frame,
            @Nullable Rect surfaceInsets, int taskId) {
        getNextAppTransitionStartRect(taskId, mTmpRect);
        return createAspectScaledThumbnailFreeformAnimationLocked(mTmpRect, frame, surfaceInsets,
                true);
    }

    private Animation createAspectScaledThumbnailExitFreeformAnimationLocked(Rect frame,
            @Nullable Rect surfaceInsets, int taskId) {
        getNextAppTransitionStartRect(taskId, mTmpRect);
        return createAspectScaledThumbnailFreeformAnimationLocked(frame, mTmpRect, surfaceInsets,
                false);
    }

    private AnimationSet createAspectScaledThumbnailFreeformAnimationLocked(Rect sourceFrame,
            Rect destFrame, @Nullable Rect surfaceInsets, boolean enter) {
        final float sourceWidth = sourceFrame.width();
        final float sourceHeight = sourceFrame.height();
        final float destWidth = destFrame.width();
        final float destHeight = destFrame.height();
        final float scaleH = enter ? sourceWidth / destWidth : destWidth / sourceWidth;
        final float scaleV = enter ? sourceHeight / destHeight : destHeight / sourceHeight;
        AnimationSet set = new AnimationSet(true);
        final int surfaceInsetsH = surfaceInsets == null
                ? 0 : surfaceInsets.left + surfaceInsets.right;
        final int surfaceInsetsV = surfaceInsets == null
                ? 0 : surfaceInsets.top + surfaceInsets.bottom;
        // We want the scaling to happen from the center of the surface. In order to achieve that,
        // we need to account for surface insets that will be used to enlarge the surface.
        final float scaleHCenter = ((enter ? destWidth : sourceWidth) + surfaceInsetsH) / 2;
        final float scaleVCenter = ((enter ? destHeight : sourceHeight) + surfaceInsetsV) / 2;
        final ScaleAnimation scale = enter ?
                new ScaleAnimation(scaleH, 1, scaleV, 1, scaleHCenter, scaleVCenter)
                : new ScaleAnimation(1, scaleH, 1, scaleV, scaleHCenter, scaleVCenter);
        final int sourceHCenter = sourceFrame.left + sourceFrame.width() / 2;
        final int sourceVCenter = sourceFrame.top + sourceFrame.height() / 2;
        final int destHCenter = destFrame.left + destFrame.width() / 2;
        final int destVCenter = destFrame.top + destFrame.height() / 2;
        final int fromX = enter ? sourceHCenter - destHCenter : destHCenter - sourceHCenter;
        final int fromY = enter ? sourceVCenter - destVCenter : destVCenter - sourceVCenter;
        final TranslateAnimation translation = enter ? new TranslateAnimation(fromX, 0, fromY, 0)
                : new TranslateAnimation(0, fromX, 0, fromY);
        set.addAnimation(scale);
        set.addAnimation(translation);

        final IRemoteCallback callback = mAnimationFinishedCallback;
        if (callback != null) {
            set.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mService.mH.obtainMessage(H.DO_ANIMATION_CALLBACK, callback).sendToTarget();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        }
        return set;
    }

    /**
     * This animation runs for the thumbnail that gets cross faded with the enter/exit activity
     * when a thumbnail is specified with the pending animation override.
     */
    Animation createThumbnailScaleAnimationLocked(int appWidth, int appHeight, int transit,
            Bitmap thumbnailHeader) {
        Animation a;
        getDefaultNextAppTransitionStartRect(mTmpRect);
        final int thumbWidthI = thumbnailHeader.getWidth();
        final float thumbWidth = thumbWidthI > 0 ? thumbWidthI : 1;
        final int thumbHeightI = thumbnailHeader.getHeight();
        final float thumbHeight = thumbHeightI > 0 ? thumbHeightI : 1;

        if (mNextAppTransitionScaleUp) {
            // Animation for the thumbnail zooming from its initial size to the full screen
            float scaleW = appWidth / thumbWidth;
            float scaleH = appHeight / thumbHeight;
            Animation scale = new ScaleAnimation(1, scaleW, 1, scaleH,
                    computePivot(mTmpRect.left, 1 / scaleW),
                    computePivot(mTmpRect.top, 1 / scaleH));
            scale.setInterpolator(mDecelerateInterpolator);

            Animation alpha = new AlphaAnimation(1, 0);
            alpha.setInterpolator(mThumbnailFadeOutInterpolator);

            // This AnimationSet uses the Interpolators assigned above.
            AnimationSet set = new AnimationSet(false);
            set.addAnimation(scale);
            set.addAnimation(alpha);
            a = set;
        } else {
            // Animation for the thumbnail zooming down from the full screen to its final size
            float scaleW = appWidth / thumbWidth;
            float scaleH = appHeight / thumbHeight;
            a = new ScaleAnimation(scaleW, 1, scaleH, 1,
                    computePivot(mTmpRect.left, 1 / scaleW),
                    computePivot(mTmpRect.top, 1 / scaleH));
        }

        return prepareThumbnailAnimation(a, appWidth, appHeight, transit);
    }

    /**
     * This animation is created when we are doing a thumbnail transition, for the activity that is
     * leaving, and the activity that is entering.
     */
    Animation createThumbnailEnterExitAnimationLocked(int thumbTransitState, Rect containingFrame,
            int transit, int taskId) {
        final int appWidth = containingFrame.width();
        final int appHeight = containingFrame.height();
        Bitmap thumbnailHeader = getAppTransitionThumbnailHeader(taskId);
        Animation a;
        getDefaultNextAppTransitionStartRect(mTmpRect);
        final int thumbWidthI = thumbnailHeader != null ? thumbnailHeader.getWidth() : appWidth;
        final float thumbWidth = thumbWidthI > 0 ? thumbWidthI : 1;
        final int thumbHeightI = thumbnailHeader != null ? thumbnailHeader.getHeight() : appHeight;
        final float thumbHeight = thumbHeightI > 0 ? thumbHeightI : 1;

        switch (thumbTransitState) {
            case THUMBNAIL_TRANSITION_ENTER_SCALE_UP: {
                // Entering app scales up with the thumbnail
                float scaleW = thumbWidth / appWidth;
                float scaleH = thumbHeight / appHeight;
                a = new ScaleAnimation(scaleW, 1, scaleH, 1,
                        computePivot(mTmpRect.left, scaleW),
                        computePivot(mTmpRect.top, scaleH));
                break;
            }
            case THUMBNAIL_TRANSITION_EXIT_SCALE_UP: {
                // Exiting app while the thumbnail is scaling up should fade or stay in place
                if (transit == TRANSIT_WALLPAPER_INTRA_OPEN) {
                    // Fade out while bringing up selected activity. This keeps the
                    // current activity from showing through a launching wallpaper
                    // activity.
                    a = new AlphaAnimation(1, 0);
                } else {
                    // noop animation
                    a = new AlphaAnimation(1, 1);
                }
                break;
            }
            case THUMBNAIL_TRANSITION_ENTER_SCALE_DOWN: {
                // Entering the other app, it should just be visible while we scale the thumbnail
                // down above it
                a = new AlphaAnimation(1, 1);
                break;
            }
            case THUMBNAIL_TRANSITION_EXIT_SCALE_DOWN: {
                // Exiting the current app, the app should scale down with the thumbnail
                float scaleW = thumbWidth / appWidth;
                float scaleH = thumbHeight / appHeight;
                Animation scale = new ScaleAnimation(1, scaleW, 1, scaleH,
                        computePivot(mTmpRect.left, scaleW),
                        computePivot(mTmpRect.top, scaleH));

                Animation alpha = new AlphaAnimation(1, 0);

                AnimationSet set = new AnimationSet(true);
                set.addAnimation(scale);
                set.addAnimation(alpha);
                set.setZAdjustment(Animation.ZORDER_TOP);
                a = set;
                break;
            }
            default:
                throw new RuntimeException("Invalid thumbnail transition state");
        }

        return prepareThumbnailAnimation(a, appWidth, appHeight, transit);
    }

    private Animation createRelaunchAnimation(Rect containingFrame, Rect contentInsets) {
        getDefaultNextAppTransitionStartRect(mTmpFromClipRect);
        final int left = mTmpFromClipRect.left;
        final int top = mTmpFromClipRect.top;
        mTmpFromClipRect.offset(-left, -top);
        // TODO: Isn't that strange that we ignore exact position of the containingFrame?
        mTmpToClipRect.set(0, 0, containingFrame.width(), containingFrame.height());
        AnimationSet set = new AnimationSet(true);
        float fromWidth = mTmpFromClipRect.width();
        float toWidth = mTmpToClipRect.width();
        float fromHeight = mTmpFromClipRect.height();
        // While the window might span the whole display, the actual content will be cropped to the
        // system decoration frame, for example when the window is docked. We need to take into
        // account the visible height when constructing the animation.
        float toHeight = mTmpToClipRect.height() - contentInsets.top - contentInsets.bottom;
        int translateAdjustment = 0;
        if (fromWidth <= toWidth && fromHeight <= toHeight) {
            // The final window is larger in both dimensions than current window (e.g. we are
            // maximizing), so we can simply unclip the new window and there will be no disappearing
            // frame.
            set.addAnimation(new ClipRectAnimation(mTmpFromClipRect, mTmpToClipRect));
        } else {
            // The disappearing window has one larger dimension. We need to apply scaling, so the
            // first frame of the entry animation matches the old window.
            set.addAnimation(new ScaleAnimation(fromWidth / toWidth, 1, fromHeight / toHeight, 1));
            // We might not be going exactly full screen, but instead be aligned under the status
            // bar using cropping. We still need to account for the cropped part, which will also
            // be scaled.
            translateAdjustment = (int) (contentInsets.top * fromHeight / toHeight);
        }

        // We animate the translation from the old position of the removed window, to the new
        // position of the added window. The latter might not be full screen, for example docked for
        // docked windows.
        TranslateAnimation translate = new TranslateAnimation(left - containingFrame.left,
                0, top - containingFrame.top - translateAdjustment, 0);
        set.addAnimation(translate);
        set.setDuration(DEFAULT_APP_TRANSITION_DURATION);
        set.setZAdjustment(Animation.ZORDER_TOP);
        return set;
    }

    /**
     * @return true if and only if the first frame of the transition can be skipped, i.e. the first
     *         frame of the transition doesn't change the visuals on screen, so we can start
     *         directly with the second one
     */
    boolean canSkipFirstFrame() {
        return mNextAppTransitionType != NEXT_TRANSIT_TYPE_CUSTOM
                && mNextAppTransitionType != NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE
                && mNextAppTransitionType != NEXT_TRANSIT_TYPE_CLIP_REVEAL;
    }

    /**
     *
     * @param frame These are the bounds of the window when it finishes the animation. This is where
     *              the animation must usually finish in entrance animation, as the next frame will
     *              display the window at these coordinates. In case of exit animation, this is
     *              where the animation must start, as the frame before the animation is displaying
     *              the window at these bounds.
     * @param insets Knowing where the window will be positioned is not enough. Some parts of the
     *               window might be obscured, usually by the system windows (status bar and
     *               navigation bar) and we use content insets to convey that information. This
     *               usually affects the animation aspects vertically, as the system decoration is
     *               at the top and the bottom. For example when we animate from full screen to
     *               recents, we want to exclude the covered parts, because they won't match the
     *               thumbnail after the last frame is executed.
     * @param surfaceInsets In rare situation the surface is larger than the content and we need to
     *                      know about this to make the animation frames match. We currently use
     *                      this for freeform windows, which have larger surfaces to display
     *                      shadows. When we animate them from recents, we want to match the content
     *                      to the recents thumbnail and hence need to account for the surface being
     *                      bigger.
     */
    Animation loadAnimation(WindowManager.LayoutParams lp, int transit, boolean enter,
            int orientation, Rect frame, Rect insets, @Nullable Rect surfaceInsets,
            boolean isVoiceInteraction, boolean freeform, int taskId) {
        Animation a;
        if (isVoiceInteraction && (transit == TRANSIT_ACTIVITY_OPEN
                || transit == TRANSIT_TASK_OPEN
                || transit == TRANSIT_TASK_TO_FRONT)) {
            a = loadAnimationRes(lp, enter
                    ? com.android.internal.R.anim.voice_activity_open_enter
                    : com.android.internal.R.anim.voice_activity_open_exit);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) Slog.v(TAG,
                    "applyAnimation voice:"
                    + " anim=" + a + " transit=" + appTransitionToString(transit)
                    + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
        } else if (isVoiceInteraction && (transit == TRANSIT_ACTIVITY_CLOSE
                || transit == TRANSIT_TASK_CLOSE
                || transit == TRANSIT_TASK_TO_BACK)) {
            a = loadAnimationRes(lp, enter
                    ? com.android.internal.R.anim.voice_activity_close_enter
                    : com.android.internal.R.anim.voice_activity_close_exit);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) Slog.v(TAG,
                    "applyAnimation voice:"
                    + " anim=" + a + " transit=" + appTransitionToString(transit)
                    + " isEntrance=" + enter + " Callers=" + Debug.getCallers(3));
        } else if (transit == TRANSIT_ACTIVITY_RELAUNCH) {
            a = createRelaunchAnimation(frame, insets);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) Slog.v(TAG,
                    "applyAnimation:"
                    + " anim=" + a + " nextAppTransition=" + mNextAppTransition
                    + " transit=" + appTransitionToString(transit)
                    + " Callers=" + Debug.getCallers(3));
        } else if (mNextAppTransitionType == NEXT_TRANSIT_TYPE_CUSTOM) {
            a = loadAnimationRes(mNextAppTransitionPackage, enter ?
                    mNextAppTransitionEnter : mNextAppTransitionExit);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) Slog.v(TAG,
                    "applyAnimation:"
                    + " anim=" + a + " nextAppTransition=ANIM_CUSTOM"
                    + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter
                    + " Callers=" + Debug.getCallers(3));
        } else if (mNextAppTransitionType == NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE) {
            a = loadAnimationRes(mNextAppTransitionPackage, mNextAppTransitionInPlace);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) Slog.v(TAG,
                    "applyAnimation:"
                    + " anim=" + a + " nextAppTransition=ANIM_CUSTOM_IN_PLACE"
                    + " transit=" + appTransitionToString(transit)
                    + " Callers=" + Debug.getCallers(3));
        } else if (mNextAppTransitionType == NEXT_TRANSIT_TYPE_CLIP_REVEAL) {
            a = createClipRevealAnimationLocked(transit, enter, frame);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) Slog.v(TAG,
                    "applyAnimation:"
                            + " anim=" + a + " nextAppTransition=ANIM_CLIP_REVEAL"
                            + " transit=" + appTransitionToString(transit)
                            + " Callers=" + Debug.getCallers(3));
        } else if (mNextAppTransitionType == NEXT_TRANSIT_TYPE_SCALE_UP) {
            a = createScaleUpAnimationLocked(transit, enter, frame);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) Slog.v(TAG,
                    "applyAnimation:"
                    + " anim=" + a + " nextAppTransition=ANIM_SCALE_UP"
                    + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter
                    + " Callers=" + Debug.getCallers(3));
        } else if (mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP ||
                mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN) {
            mNextAppTransitionScaleUp =
                    (mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP);
            a = createThumbnailEnterExitAnimationLocked(getThumbnailTransitionState(enter),
                    frame, transit, taskId);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) {
                String animName = mNextAppTransitionScaleUp ?
                        "ANIM_THUMBNAIL_SCALE_UP" : "ANIM_THUMBNAIL_SCALE_DOWN";
                Slog.v(TAG, "applyAnimation:"
                        + " anim=" + a + " nextAppTransition=" + animName
                        + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter
                        + " Callers=" + Debug.getCallers(3));
            }
        } else if (mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP ||
                mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN) {
            mNextAppTransitionScaleUp =
                    (mNextAppTransitionType == NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP);
            a = createAspectScaledThumbnailEnterExitAnimationLocked(
                    getThumbnailTransitionState(enter), orientation, transit, frame,
                    insets, surfaceInsets, freeform, taskId);
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) {
                String animName = mNextAppTransitionScaleUp ?
                        "ANIM_THUMBNAIL_ASPECT_SCALE_UP" : "ANIM_THUMBNAIL_ASPECT_SCALE_DOWN";
                Slog.v(TAG, "applyAnimation:"
                        + " anim=" + a + " nextAppTransition=" + animName
                        + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter
                        + " Callers=" + Debug.getCallers(3));
            }
        } else {
            int animAttr = 0;
            switch (transit) {
                case TRANSIT_ACTIVITY_OPEN:
                    animAttr = enter
                            ? WindowAnimation_activityOpenEnterAnimation
                            : WindowAnimation_activityOpenExitAnimation;
                    break;
                case TRANSIT_ACTIVITY_CLOSE:
                    animAttr = enter
                            ? WindowAnimation_activityCloseEnterAnimation
                            : WindowAnimation_activityCloseExitAnimation;
                    break;
                case TRANSIT_TASK_OPEN:
                    animAttr = enter
                            ? WindowAnimation_taskOpenEnterAnimation
                            : WindowAnimation_taskOpenExitAnimation;
                    break;
                case TRANSIT_TASK_CLOSE:
                    animAttr = enter
                            ? WindowAnimation_taskCloseEnterAnimation
                            : WindowAnimation_taskCloseExitAnimation;
                    break;
                case TRANSIT_TASK_TO_FRONT:
                    animAttr = enter
                            ? WindowAnimation_taskToFrontEnterAnimation
                            : WindowAnimation_taskToFrontExitAnimation;
                    break;
                case TRANSIT_TASK_TO_BACK:
                    animAttr = enter
                            ? WindowAnimation_taskToBackEnterAnimation
                            : WindowAnimation_taskToBackExitAnimation;
                    break;
                case TRANSIT_WALLPAPER_OPEN:
                    animAttr = enter
                            ? WindowAnimation_wallpaperOpenEnterAnimation
                            : WindowAnimation_wallpaperOpenExitAnimation;
                    break;
                case TRANSIT_WALLPAPER_CLOSE:
                    animAttr = enter
                            ? WindowAnimation_wallpaperCloseEnterAnimation
                            : WindowAnimation_wallpaperCloseExitAnimation;
                    break;
                case TRANSIT_WALLPAPER_INTRA_OPEN:
                    animAttr = enter
                            ? WindowAnimation_wallpaperIntraOpenEnterAnimation
                            : WindowAnimation_wallpaperIntraOpenExitAnimation;
                    break;
                case TRANSIT_WALLPAPER_INTRA_CLOSE:
                    animAttr = enter
                            ? WindowAnimation_wallpaperIntraCloseEnterAnimation
                            : WindowAnimation_wallpaperIntraCloseExitAnimation;
                    break;
                case TRANSIT_TASK_OPEN_BEHIND:
                    animAttr = enter
                            ? WindowAnimation_launchTaskBehindSourceAnimation
                            : WindowAnimation_launchTaskBehindTargetAnimation;
            }
            a = animAttr != 0 ? loadAnimationAttr(lp, animAttr) : null;
            if (DEBUG_APP_TRANSITIONS || DEBUG_ANIM) Slog.v(TAG,
                    "applyAnimation:"
                    + " anim=" + a
                    + " animAttr=0x" + Integer.toHexString(animAttr)
                    + " transit=" + appTransitionToString(transit) + " isEntrance=" + enter
                    + " Callers=" + Debug.getCallers(3));
        }
        return a;
    }

    void postAnimationCallback() {
        if (mNextAppTransitionCallback != null) {
            mService.mH.sendMessage(mService.mH.obtainMessage(H.DO_ANIMATION_CALLBACK,
                    mNextAppTransitionCallback));
            mNextAppTransitionCallback = null;
        }
    }

    void overridePendingAppTransition(String packageName, int enterAnim, int exitAnim,
            IRemoteCallback startedCallback) {
        if (isTransitionSet()) {
            clear();
            mNextAppTransitionType = NEXT_TRANSIT_TYPE_CUSTOM;
            mNextAppTransitionPackage = packageName;
            mNextAppTransitionEnter = enterAnim;
            mNextAppTransitionExit = exitAnim;
            postAnimationCallback();
            mNextAppTransitionCallback = startedCallback;
        } else {
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionScaleUp(int startX, int startY, int startWidth,
            int startHeight) {
        if (isTransitionSet()) {
            clear();
            mNextAppTransitionType = NEXT_TRANSIT_TYPE_SCALE_UP;
            putDefaultNextAppTransitionCoordinates(startX, startY, startX + startWidth,
                    startY + startHeight, null);
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionClipReveal(int startX, int startY,
                                                int startWidth, int startHeight) {
        if (isTransitionSet()) {
            clear();
            mNextAppTransitionType = NEXT_TRANSIT_TYPE_CLIP_REVEAL;
            putDefaultNextAppTransitionCoordinates(startX, startY, startWidth, startHeight, null);
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionThumb(Bitmap srcThumb, int startX, int startY,
                                           IRemoteCallback startedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            clear();
            mNextAppTransitionType = scaleUp ? NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP
                    : NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN;
            mNextAppTransitionScaleUp = scaleUp;
            putDefaultNextAppTransitionCoordinates(startX, startY, 0, 0, srcThumb);
            postAnimationCallback();
            mNextAppTransitionCallback = startedCallback;
        } else {
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionAspectScaledThumb(Bitmap srcThumb, int startX, int startY,
            int targetWidth, int targetHeight, IRemoteCallback startedCallback, boolean scaleUp) {
        if (isTransitionSet()) {
            clear();
            mNextAppTransitionType = scaleUp ? NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP
                    : NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
            mNextAppTransitionScaleUp = scaleUp;
            putDefaultNextAppTransitionCoordinates(startX, startY, targetWidth, targetHeight,
                    srcThumb);
            postAnimationCallback();
            mNextAppTransitionCallback = startedCallback;
        } else {
            postAnimationCallback();
        }
    }

    public void overridePendingAppTransitionMultiThumb(AppTransitionAnimationSpec[] specs,
            IRemoteCallback onAnimationStartedCallback, IRemoteCallback onAnimationFinishedCallback,
            boolean scaleUp) {
        if (isTransitionSet()) {
            clear();
            mNextAppTransitionType = scaleUp ? NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP
                    : NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
            mNextAppTransitionScaleUp = scaleUp;
            if (specs != null) {
                for (int i = 0; i < specs.length; i++) {
                    AppTransitionAnimationSpec spec = specs[i];
                    if (spec != null) {
                        mNextAppTransitionAnimationsSpecs.put(spec.taskId, spec);
                        if (i == 0) {
                            // In full screen mode, the transition code depends on the default spec
                            // to be set.
                            Rect rect = spec.rect;
                            putDefaultNextAppTransitionCoordinates(rect.left, rect.top,
                                    rect.width(), rect.height(), spec.bitmap);
                        }
                    }
                }
            }
            postAnimationCallback();
            mNextAppTransitionCallback = onAnimationStartedCallback;
            mAnimationFinishedCallback = onAnimationFinishedCallback;
        } else {
            postAnimationCallback();
        }
    }

    void overridePendingAppTransitionMultiThumbFuture(
            IAppTransitionAnimationSpecsFuture specsFuture, IRemoteCallback callback,
            boolean scaleUp) {
        if (isTransitionSet()) {
            clear();
            mNextAppTransitionType = scaleUp ? NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP
                    : NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN;
            mNextAppTransitionAnimationsSpecsFuture = specsFuture;
            mNextAppTransitionScaleUp = scaleUp;
            mNextAppTransitionFutureCallback = callback;
        }
    }

    void overrideInPlaceAppTransition(String packageName, int anim) {
        if (isTransitionSet()) {
            clear();
            mNextAppTransitionType = NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE;
            mNextAppTransitionPackage = packageName;
            mNextAppTransitionInPlace = anim;
        } else {
            postAnimationCallback();
        }
    }

    /**
     * If a future is set for the app transition specs, fetch it in another thread.
     */
    private void fetchAppTransitionSpecsFromFuture() {
        if (mNextAppTransitionAnimationsSpecsFuture != null) {
            mNextAppTransitionAnimationsSpecsPending = true;
            final IAppTransitionAnimationSpecsFuture future
                    = mNextAppTransitionAnimationsSpecsFuture;
            mNextAppTransitionAnimationsSpecsFuture = null;
            mDefaultExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    AppTransitionAnimationSpec[] specs = null;
                    try {
                        specs = future.get();
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Failed to fetch app transition specs: " + e);
                    }
                    synchronized (mService.mWindowMap) {
                        mNextAppTransitionAnimationsSpecsPending = false;
                        overridePendingAppTransitionMultiThumb(specs,
                                mNextAppTransitionFutureCallback, null /* finishedCallback */,
                                mNextAppTransitionScaleUp);
                        mNextAppTransitionFutureCallback = null;
                        if (specs != null) {
                            mService.prolongAnimationsFromSpecs(specs, mNextAppTransitionScaleUp);
                        }
                    }
                    mService.requestTraversal();
                }
            });
        }
    }

    @Override
    public String toString() {
        return "mNextAppTransition=" + appTransitionToString(mNextAppTransition);
    }

    /**
     * Returns the human readable name of a window transition.
     *
     * @param transition The window transition.
     * @return The transition symbolic name.
     */
    public static String appTransitionToString(int transition) {
        switch (transition) {
            case TRANSIT_UNSET: {
                return "TRANSIT_UNSET";
            }
            case TRANSIT_NONE: {
                return "TRANSIT_NONE";
            }
            case TRANSIT_ACTIVITY_OPEN: {
                return "TRANSIT_ACTIVITY_OPEN";
            }
            case TRANSIT_ACTIVITY_CLOSE: {
                return "TRANSIT_ACTIVITY_CLOSE";
            }
            case TRANSIT_TASK_OPEN: {
                return "TRANSIT_TASK_OPEN";
            }
            case TRANSIT_TASK_CLOSE: {
                return "TRANSIT_TASK_CLOSE";
            }
            case TRANSIT_TASK_TO_FRONT: {
                return "TRANSIT_TASK_TO_FRONT";
            }
            case TRANSIT_TASK_TO_BACK: {
                return "TRANSIT_TASK_TO_BACK";
            }
            case TRANSIT_WALLPAPER_CLOSE: {
                return "TRANSIT_WALLPAPER_CLOSE";
            }
            case TRANSIT_WALLPAPER_OPEN: {
                return "TRANSIT_WALLPAPER_OPEN";
            }
            case TRANSIT_WALLPAPER_INTRA_OPEN: {
                return "TRANSIT_WALLPAPER_INTRA_OPEN";
            }
            case TRANSIT_WALLPAPER_INTRA_CLOSE: {
                return "TRANSIT_WALLPAPER_INTRA_CLOSE";
            }
            case TRANSIT_TASK_OPEN_BEHIND: {
                return "TRANSIT_TASK_OPEN_BEHIND";
            }
            case TRANSIT_ACTIVITY_RELAUNCH: {
                return "TRANSIT_ACTIVITY_RELAUNCH";
            }
            default: {
                return "<UNKNOWN>";
            }
        }
    }

    private String appStateToString() {
        switch (mAppTransitionState) {
            case APP_STATE_IDLE:
                return "APP_STATE_IDLE";
            case APP_STATE_READY:
                return "APP_STATE_READY";
            case APP_STATE_RUNNING:
                return "APP_STATE_RUNNING";
            case APP_STATE_TIMEOUT:
                return "APP_STATE_TIMEOUT";
            default:
                return "unknown state=" + mAppTransitionState;
        }
    }

    private String transitTypeToString() {
        switch (mNextAppTransitionType) {
            case NEXT_TRANSIT_TYPE_NONE:
                return "NEXT_TRANSIT_TYPE_NONE";
            case NEXT_TRANSIT_TYPE_CUSTOM:
                return "NEXT_TRANSIT_TYPE_CUSTOM";
            case NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE:
                return "NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE";
            case NEXT_TRANSIT_TYPE_SCALE_UP:
                return "NEXT_TRANSIT_TYPE_SCALE_UP";
            case NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP";
            case NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN";
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP";
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN:
                return "NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN";
            default:
                return "unknown type=" + mNextAppTransitionType;
        }
    }

    @Override
    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.println(this);
        pw.print(prefix); pw.print("mAppTransitionState="); pw.println(appStateToString());
        if (mNextAppTransitionType != NEXT_TRANSIT_TYPE_NONE) {
            pw.print(prefix); pw.print("mNextAppTransitionType=");
                    pw.println(transitTypeToString());
        }
        switch (mNextAppTransitionType) {
            case NEXT_TRANSIT_TYPE_CUSTOM:
                pw.print(prefix); pw.print("mNextAppTransitionPackage=");
                        pw.println(mNextAppTransitionPackage);
                pw.print(prefix); pw.print("mNextAppTransitionEnter=0x");
                        pw.print(Integer.toHexString(mNextAppTransitionEnter));
                        pw.print(" mNextAppTransitionExit=0x");
                        pw.println(Integer.toHexString(mNextAppTransitionExit));
                break;
            case NEXT_TRANSIT_TYPE_CUSTOM_IN_PLACE:
                pw.print(prefix); pw.print("mNextAppTransitionPackage=");
                        pw.println(mNextAppTransitionPackage);
                pw.print(prefix); pw.print("mNextAppTransitionInPlace=0x");
                        pw.print(Integer.toHexString(mNextAppTransitionInPlace));
                break;
            case NEXT_TRANSIT_TYPE_SCALE_UP: {
                getDefaultNextAppTransitionStartRect(mTmpRect);
                pw.print(prefix); pw.print("mNextAppTransitionStartX=");
                        pw.print(mTmpRect.left);
                        pw.print(" mNextAppTransitionStartY=");
                        pw.println(mTmpRect.top);
                pw.print(prefix); pw.print("mNextAppTransitionStartWidth=");
                        pw.print(mTmpRect.width());
                        pw.print(" mNextAppTransitionStartHeight=");
                        pw.println(mTmpRect.height());
                break;
            }
            case NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_UP:
            case NEXT_TRANSIT_TYPE_THUMBNAIL_SCALE_DOWN:
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_UP:
            case NEXT_TRANSIT_TYPE_THUMBNAIL_ASPECT_SCALE_DOWN: {
                pw.print(prefix); pw.print("mDefaultNextAppTransitionAnimationSpec=");
                        pw.println(mDefaultNextAppTransitionAnimationSpec);
                pw.print(prefix); pw.print("mNextAppTransitionAnimationsSpecs=");
                        pw.println(mNextAppTransitionAnimationsSpecs);
                pw.print(prefix); pw.print("mNextAppTransitionScaleUp=");
                        pw.println(mNextAppTransitionScaleUp);
                break;
            }
        }
        if (mNextAppTransitionCallback != null) {
            pw.print(prefix); pw.print("mNextAppTransitionCallback=");
                    pw.println(mNextAppTransitionCallback);
        }
    }

    public void setCurrentUser(int newUserId) {
        mCurrentUserId = newUserId;
    }

    /**
     * @return true if transition is not running and should not be skipped, false if transition is
     *         already running
     */
    boolean prepareAppTransitionLocked(int transit, boolean alwaysKeepCurrent) {
        if (DEBUG_APP_TRANSITIONS) Slog.v(TAG, "Prepare app transition:"
                + " transit=" + appTransitionToString(transit)
                + " " + this
                + " alwaysKeepCurrent=" + alwaysKeepCurrent
                + " Callers=" + Debug.getCallers(3));
        if (!isTransitionSet() || mNextAppTransition == TRANSIT_NONE) {
            setAppTransition(transit);
        } else if (!alwaysKeepCurrent) {
            if (transit == TRANSIT_TASK_OPEN && isTransitionEqual(TRANSIT_TASK_CLOSE)) {
                // Opening a new task always supersedes a close for the anim.
                setAppTransition(transit);
            } else if (transit == TRANSIT_ACTIVITY_OPEN
                    && isTransitionEqual(TRANSIT_ACTIVITY_CLOSE)) {
                // Opening a new activity always supersedes a close for the anim.
                setAppTransition(transit);
            }
        }
        boolean prepared = prepare();
        if (isTransitionSet()) {
            mService.mH.removeMessages(H.APP_TRANSITION_TIMEOUT);
            mService.mH.sendEmptyMessageDelayed(H.APP_TRANSITION_TIMEOUT, APP_TRANSITION_TIMEOUT_MS);
        }
        return prepared;
    }
}