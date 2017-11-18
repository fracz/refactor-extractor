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

package com.android.server.wm;

import java.io.PrintWriter;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Slog;
import android.view.Surface;
import android.view.SurfaceSession;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

class ScreenRotationAnimation {
    static final String TAG = "ScreenRotationAnimation";
    static final boolean DEBUG_STATE = false;
    static final boolean DEBUG_TRANSFORMS = false;

    static final int FREEZE_LAYER = WindowManagerService.TYPE_LAYER_MULTIPLIER * 200;

    final Context mContext;
    Surface mSurface;
    BlackFrame mBlackFrame;
    int mWidth, mHeight;

    int mSnapshotRotation;
    int mSnapshotDeltaRotation;
    int mOriginalRotation;
    int mOriginalWidth, mOriginalHeight;
    int mCurRotation;

    // For all animations, "exit" is for the UI elements that are going
    // away (that is the snapshot of the old screen), and "enter" is for
    // the new UI elements that are appearing (that is the active windows
    // in their final orientation).

    // The starting animation for the exiting and entering elements.  This
    // animation applies a transformation while the rotation is in progress.
    // It is started immediately, before the new entering UI is ready.
    Animation mStartExitAnimation;
    final Transformation mStartExitTransformation = new Transformation();
    Animation mStartEnterAnimation;
    final Transformation mStartEnterTransformation = new Transformation();
    Animation mStartFrameAnimation;
    final Transformation mStartFrameTransformation = new Transformation();

    // The finishing animation for the exiting and entering elements.  This
    // animation needs to undo the transformation of the starting animation.
    // It starts running once the new rotation UI elements are ready to be
    // displayed.
    Animation mFinishExitAnimation;
    final Transformation mFinishExitTransformation = new Transformation();
    Animation mFinishEnterAnimation;
    final Transformation mFinishEnterTransformation = new Transformation();
    Animation mFinishFrameAnimation;
    final Transformation mFinishFrameTransformation = new Transformation();

    // The current active animation to move from the old to the new rotated
    // state.  Which animation is run here will depend on the old and new
    // rotations.
    Animation mRotateExitAnimation;
    final Transformation mRotateExitTransformation = new Transformation();
    Animation mRotateEnterAnimation;
    final Transformation mRotateEnterTransformation = new Transformation();
    Animation mRotateFrameAnimation;
    final Transformation mRotateFrameTransformation = new Transformation();

    // A previously running rotate animation.  This will be used if we need
    // to switch to a new rotation before finishing the previous one.
    Animation mLastRotateExitAnimation;
    final Transformation mLastRotateExitTransformation = new Transformation();
    Animation mLastRotateEnterAnimation;
    final Transformation mLastRotateEnterTransformation = new Transformation();
    Animation mLastRotateFrameAnimation;
    final Transformation mLastRotateFrameTransformation = new Transformation();

    // Complete transformations being applied.
    final Transformation mExitTransformation = new Transformation();
    final Transformation mEnterTransformation = new Transformation();
    final Transformation mFrameTransformation = new Transformation();

    boolean mStarted;
    boolean mAnimRunning;
    boolean mFinishAnimReady;
    long mFinishAnimStartTime;

    final Matrix mFrameInitialMatrix = new Matrix();
    final Matrix mSnapshotInitialMatrix = new Matrix();
    final Matrix mSnapshotFinalMatrix = new Matrix();
    final Matrix mTmpMatrix = new Matrix();
    final float[] mTmpFloats = new float[9];
    private boolean mMoreRotateEnter;
    private boolean mMoreRotateExit;
    private boolean mMoreRotateFrame;
    private boolean mMoreFinishEnter;
    private boolean mMoreFinishExit;
    private boolean mMoreFinishFrame;
    private boolean mMoreStartEnter;
    private boolean mMoreStartExit;
    private boolean mMoreStartFrame;

    public void printTo(String prefix, PrintWriter pw) {
        pw.print(prefix); pw.print("mSurface="); pw.print(mSurface);
                pw.print(" mWidth="); pw.print(mWidth);
                pw.print(" mHeight="); pw.println(mHeight);
        pw.print(prefix); pw.print("mBlackFrame="); pw.println(mBlackFrame);
        if (mBlackFrame != null) {
            mBlackFrame.printTo(prefix + "  ", pw);
        }
        pw.print(prefix); pw.print("mSnapshotRotation="); pw.print(mSnapshotRotation);
                pw.print(" mSnapshotDeltaRotation="); pw.print(mSnapshotDeltaRotation);
                pw.print(" mCurRotation="); pw.println(mCurRotation);
        pw.print(prefix); pw.print("mOriginalRotation="); pw.print(mOriginalRotation);
                pw.print(" mOriginalWidth="); pw.print(mOriginalWidth);
                pw.print(" mOriginalHeight="); pw.println(mOriginalHeight);
        pw.print(prefix); pw.print("mStarted="); pw.print(mStarted);
                pw.print(" mAnimRunning="); pw.print(mAnimRunning);
                pw.print(" mFinishAnimReady="); pw.print(mFinishAnimReady);
                pw.print(" mFinishAnimStartTime="); pw.println(mFinishAnimStartTime);
        pw.print(prefix); pw.print("mStartExitAnimation="); pw.print(mStartExitAnimation);
                pw.print(" "); mStartExitTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mStartEnterAnimation="); pw.print(mStartEnterAnimation);
                pw.print(" "); mStartEnterTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mStartFrameAnimation="); pw.print(mStartFrameAnimation);
                pw.print(" "); mStartFrameTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mFinishExitAnimation="); pw.print(mFinishExitAnimation);
                pw.print(" "); mFinishExitTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mFinishEnterAnimation="); pw.print(mFinishEnterAnimation);
                pw.print(" "); mFinishEnterTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mFinishFrameAnimation="); pw.print(mFinishFrameAnimation);
                pw.print(" "); mFinishFrameTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mRotateExitAnimation="); pw.print(mRotateExitAnimation);
                pw.print(" "); mRotateExitTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mRotateEnterAnimation="); pw.print(mRotateEnterAnimation);
                pw.print(" "); mRotateEnterTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mRotateFrameAnimation="); pw.print(mRotateFrameAnimation);
                pw.print(" "); mRotateFrameTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mExitTransformation=");
                mExitTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mEnterTransformation=");
                mEnterTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mFrameTransformation=");
                mEnterTransformation.printShortString(pw); pw.println();
        pw.print(prefix); pw.print("mFrameInitialMatrix=");
                mFrameInitialMatrix.printShortString(pw);
                pw.println();
        pw.print(prefix); pw.print("mSnapshotInitialMatrix=");
                mSnapshotInitialMatrix.printShortString(pw);
                pw.print(" mSnapshotFinalMatrix="); mSnapshotFinalMatrix.printShortString(pw);
                pw.println();
    }

    public ScreenRotationAnimation(Context context, SurfaceSession session,
            boolean inTransaction, int originalWidth, int originalHeight, int originalRotation) {
        mContext = context;

        // Screenshot does NOT include rotation!
        mSnapshotRotation = 0;
        if (originalRotation == Surface.ROTATION_90
                || originalRotation == Surface.ROTATION_270) {
            mWidth = originalHeight;
            mHeight = originalWidth;
        } else {
            mWidth = originalWidth;
            mHeight = originalHeight;
        }

        mOriginalRotation = originalRotation;
        mOriginalWidth = originalWidth;
        mOriginalHeight = originalHeight;

        if (!inTransaction) {
            if (WindowManagerService.SHOW_LIGHT_TRANSACTIONS) Slog.i(WindowManagerService.TAG,
                    ">>> OPEN TRANSACTION ScreenRotationAnimation");
            Surface.openTransaction();
        }

        try {
            try {
                mSurface = new Surface(session, 0, "FreezeSurface",
                        -1, mWidth, mHeight, PixelFormat.OPAQUE, Surface.FX_SURFACE_SCREENSHOT | Surface.HIDDEN);
                if (mSurface == null || !mSurface.isValid()) {
                    // Screenshot failed, punt.
                    mSurface = null;
                    return;
                }
                mSurface.setLayer(FREEZE_LAYER);
                mSurface.show();
            } catch (Surface.OutOfResourcesException e) {
                Slog.w(TAG, "Unable to allocate freeze surface", e);
            }

            if (WindowManagerService.SHOW_TRANSACTIONS ||
                    WindowManagerService.SHOW_SURFACE_ALLOC) Slog.i(WindowManagerService.TAG,
                            "  FREEZE " + mSurface + ": CREATE");

            setRotation(originalRotation);
        } finally {
            if (!inTransaction) {
                Surface.closeTransaction();
                if (WindowManagerService.SHOW_LIGHT_TRANSACTIONS) Slog.i(WindowManagerService.TAG,
                        "<<< CLOSE TRANSACTION ScreenRotationAnimation");
            }
        }
    }

    boolean hasScreenshot() {
        return mSurface != null;
    }

    static int deltaRotation(int oldRotation, int newRotation) {
        int delta = newRotation - oldRotation;
        if (delta < 0) delta += 4;
        return delta;
    }

    void setSnapshotTransform(Matrix matrix, float alpha) {
        if (mSurface != null) {
            matrix.getValues(mTmpFloats);
            mSurface.setPosition(mTmpFloats[Matrix.MTRANS_X],
                    mTmpFloats[Matrix.MTRANS_Y]);
            mSurface.setMatrix(
                    mTmpFloats[Matrix.MSCALE_X], mTmpFloats[Matrix.MSKEW_Y],
                    mTmpFloats[Matrix.MSKEW_X], mTmpFloats[Matrix.MSCALE_Y]);
            mSurface.setAlpha(alpha);
            if (DEBUG_TRANSFORMS) {
                float[] srcPnts = new float[] { 0, 0, mWidth, mHeight };
                float[] dstPnts = new float[4];
                matrix.mapPoints(dstPnts, srcPnts);
                Slog.i(TAG, "Original  : (" + srcPnts[0] + "," + srcPnts[1]
                        + ")-(" + srcPnts[2] + "," + srcPnts[3] + ")");
                Slog.i(TAG, "Transformed: (" + dstPnts[0] + "," + dstPnts[1]
                        + ")-(" + dstPnts[2] + "," + dstPnts[3] + ")");
            }
        }
    }

    public static void createRotationMatrix(int rotation, int width, int height,
            Matrix outMatrix) {
        switch (rotation) {
            case Surface.ROTATION_0:
                outMatrix.reset();
                break;
            case Surface.ROTATION_90:
                outMatrix.setRotate(90, 0, 0);
                outMatrix.postTranslate(height, 0);
                break;
            case Surface.ROTATION_180:
                outMatrix.setRotate(180, 0, 0);
                outMatrix.postTranslate(width, height);
                break;
            case Surface.ROTATION_270:
                outMatrix.setRotate(270, 0, 0);
                outMatrix.postTranslate(0, width);
                break;
        }
    }

    // Must be called while in a transaction.
    private void setRotation(int rotation) {
        mCurRotation = rotation;

        // Compute the transformation matrix that must be applied
        // to the snapshot to make it stay in the same original position
        // with the current screen rotation.
        int delta = deltaRotation(rotation, mSnapshotRotation);
        createRotationMatrix(delta, mWidth, mHeight, mSnapshotInitialMatrix);

        if (DEBUG_STATE) Slog.v(TAG, "**** ROTATION: " + delta);
        setSnapshotTransform(mSnapshotInitialMatrix, 1.0f);
    }

    // Must be called while in a transaction.
    public boolean setRotation(int rotation, SurfaceSession session,
            long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight) {
        setRotation(rotation);
        return startAnimation(session, maxAnimationDuration, animationScale,
                finalWidth, finalHeight, false);
    }

    /**
     * Returns true if animating.
     */
    private boolean startAnimation(SurfaceSession session, long maxAnimationDuration,
            float animationScale, int finalWidth, int finalHeight, boolean dismissing) {
        if (mSurface == null) {
            // Can't do animation.
            return false;
        }
        if (mStarted) {
            return true;
        }

        mStarted = true;

        boolean firstStart = false;

        // Figure out how the screen has moved from the original rotation.
        int delta = deltaRotation(mCurRotation, mOriginalRotation);

        if (mFinishExitAnimation == null && (!dismissing || delta != Surface.ROTATION_0)) {
            if (DEBUG_STATE) Slog.v(TAG, "Creating start and finish animations");
            firstStart = true;
            mStartExitAnimation = AnimationUtils.loadAnimation(mContext,
                    com.android.internal.R.anim.screen_rotate_start_exit);
            mStartEnterAnimation = AnimationUtils.loadAnimation(mContext,
                    com.android.internal.R.anim.screen_rotate_start_enter);
            mStartFrameAnimation = AnimationUtils.loadAnimation(mContext,
                    com.android.internal.R.anim.screen_rotate_start_frame);
            mFinishExitAnimation = AnimationUtils.loadAnimation(mContext,
                    com.android.internal.R.anim.screen_rotate_finish_exit);
            mFinishEnterAnimation = AnimationUtils.loadAnimation(mContext,
                    com.android.internal.R.anim.screen_rotate_finish_enter);
            mFinishFrameAnimation = AnimationUtils.loadAnimation(mContext,
                    com.android.internal.R.anim.screen_rotate_finish_frame);
        }

        if (DEBUG_STATE) Slog.v(TAG, "Rotation delta: " + delta + " finalWidth="
                + finalWidth + " finalHeight=" + finalHeight
                + " origWidth=" + mOriginalWidth + " origHeight=" + mOriginalHeight);

        switch (delta) {
            case Surface.ROTATION_0:
                mRotateExitAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_0_exit);
                mRotateEnterAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_0_enter);
                mRotateFrameAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_0_frame);
                break;
            case Surface.ROTATION_90:
                mRotateExitAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_plus_90_exit);
                mRotateEnterAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_plus_90_enter);
                mRotateFrameAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_plus_90_frame);
                break;
            case Surface.ROTATION_180:
                mRotateExitAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_180_exit);
                mRotateEnterAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_180_enter);
                mRotateFrameAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_180_frame);
                break;
            case Surface.ROTATION_270:
                mRotateExitAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_minus_90_exit);
                mRotateEnterAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_minus_90_enter);
                mRotateFrameAnimation = AnimationUtils.loadAnimation(mContext,
                        com.android.internal.R.anim.screen_rotate_minus_90_frame);
                break;
        }

        // Compute partial steps between original and final sizes.  These
        // are used for the dimensions of the exiting and entering elements,
        // so they are never stretched too significantly.
        final int halfWidth = (finalWidth + mOriginalWidth) / 2;
        final int halfHeight = (finalHeight + mOriginalHeight) / 2;

        // Initialize the animations.  This is a hack, redefining what "parent"
        // means to allow supplying the last and next size.  In this definition
        // "%p" is the original (let's call it "previous") size, and "%" is the
        // screen's current/new size.
        if (firstStart) {
            if (DEBUG_STATE) Slog.v(TAG, "Initializing start and finish animations");
            mStartEnterAnimation.initialize(finalWidth, finalHeight,
                    halfWidth, halfHeight);
            mStartExitAnimation.initialize(halfWidth, halfHeight,
                    mOriginalWidth, mOriginalHeight);
            mStartFrameAnimation.initialize(finalWidth, finalHeight,
                    mOriginalWidth, mOriginalHeight);
            mFinishEnterAnimation.initialize(finalWidth, finalHeight,
                    halfWidth, halfHeight);
            mFinishExitAnimation.initialize(halfWidth, halfHeight,
                    mOriginalWidth, mOriginalHeight);
            mFinishFrameAnimation.initialize(finalWidth, finalHeight,
                    mOriginalWidth, mOriginalHeight);
        }
        mRotateEnterAnimation.initialize(finalWidth, finalHeight, mOriginalWidth, mOriginalHeight);
        mRotateExitAnimation.initialize(finalWidth, finalHeight, mOriginalWidth, mOriginalHeight);
        mRotateFrameAnimation.initialize(finalWidth, finalHeight, mOriginalWidth, mOriginalHeight);
        mAnimRunning = false;
        mFinishAnimReady = false;
        mFinishAnimStartTime = -1;

        if (firstStart) {
            mStartExitAnimation.restrictDuration(maxAnimationDuration);
            mStartExitAnimation.scaleCurrentDuration(animationScale);
            mStartEnterAnimation.restrictDuration(maxAnimationDuration);
            mStartEnterAnimation.scaleCurrentDuration(animationScale);
            mStartFrameAnimation.restrictDuration(maxAnimationDuration);
            mStartFrameAnimation.scaleCurrentDuration(animationScale);
            mFinishExitAnimation.restrictDuration(maxAnimationDuration);
            mFinishExitAnimation.scaleCurrentDuration(animationScale);
            mFinishEnterAnimation.restrictDuration(maxAnimationDuration);
            mFinishEnterAnimation.scaleCurrentDuration(animationScale);
            mFinishFrameAnimation.restrictDuration(maxAnimationDuration);
            mFinishFrameAnimation.scaleCurrentDuration(animationScale);
        }
        mRotateExitAnimation.restrictDuration(maxAnimationDuration);
        mRotateExitAnimation.scaleCurrentDuration(animationScale);
        mRotateEnterAnimation.restrictDuration(maxAnimationDuration);
        mRotateEnterAnimation.scaleCurrentDuration(animationScale);
        mRotateFrameAnimation.restrictDuration(maxAnimationDuration);
        mRotateFrameAnimation.scaleCurrentDuration(animationScale);

        if (mBlackFrame == null) {
            if (WindowManagerService.SHOW_LIGHT_TRANSACTIONS || DEBUG_STATE) Slog.i(
                    WindowManagerService.TAG,
                    ">>> OPEN TRANSACTION ScreenRotationAnimation.startAnimation");
            Surface.openTransaction();

            // Compute the transformation matrix that must be applied
            // the the black frame to make it stay in the initial position
            // before the new screen rotation.  This is different than the
            // snapshot transformation because the snapshot is always based
            // of the native orientation of the screen, not the orientation
            // we were last in.
            createRotationMatrix(delta, mOriginalWidth, mOriginalHeight, mFrameInitialMatrix);

            try {
                Rect outer = new Rect(-mOriginalWidth*1, -mOriginalHeight*1,
                        mOriginalWidth*2, mOriginalHeight*2);
                Rect inner = new Rect(0, 0, mOriginalWidth, mOriginalHeight);
                mBlackFrame = new BlackFrame(session, outer, inner, FREEZE_LAYER + 1);
                mBlackFrame.setMatrix(mFrameInitialMatrix);
            } catch (Surface.OutOfResourcesException e) {
                Slog.w(TAG, "Unable to allocate black surface", e);
            } finally {
                Surface.closeTransaction();
                if (WindowManagerService.SHOW_LIGHT_TRANSACTIONS || DEBUG_STATE) Slog.i(
                        WindowManagerService.TAG,
                        "<<< CLOSE TRANSACTION ScreenRotationAnimation.startAnimation");
            }
        }

        return true;
    }

    /**
     * Returns true if animating.
     */
    public boolean dismiss(SurfaceSession session, long maxAnimationDuration,
            float animationScale, int finalWidth, int finalHeight) {
        if (DEBUG_STATE) Slog.v(TAG, "Dismiss!");
        if (mSurface == null) {
            // Can't do animation.
            return false;
        }
        if (!mStarted) {
            startAnimation(session, maxAnimationDuration, animationScale, finalWidth, finalHeight,
                    true);
        }
        if (!mStarted) {
            return false;
        }
        if (DEBUG_STATE) Slog.v(TAG, "Setting mFinishAnimReady = true");
        mFinishAnimReady = true;
        return true;
    }

    public void kill() {
        if (DEBUG_STATE) Slog.v(TAG, "Kill!");
        if (mSurface != null) {
            if (WindowManagerService.SHOW_TRANSACTIONS ||
                    WindowManagerService.SHOW_SURFACE_ALLOC) Slog.i(WindowManagerService.TAG,
                            "  FREEZE " + mSurface + ": DESTROY");
            mSurface.destroy();
            mSurface = null;
        }
        if (mBlackFrame != null) {
            mBlackFrame.kill();
            mBlackFrame = null;
        }
        if (mStartExitAnimation != null) {
            mStartExitAnimation.cancel();
            mStartExitAnimation = null;
        }
        if (mStartEnterAnimation != null) {
            mStartEnterAnimation.cancel();
            mStartEnterAnimation = null;
        }
        if (mStartFrameAnimation != null) {
            mStartFrameAnimation.cancel();
            mStartFrameAnimation = null;
        }
        if (mFinishExitAnimation != null) {
            mFinishExitAnimation.cancel();
            mFinishExitAnimation = null;
        }
        if (mFinishEnterAnimation != null) {
            mFinishEnterAnimation.cancel();
            mFinishEnterAnimation = null;
        }
        if (mFinishFrameAnimation != null) {
            mFinishFrameAnimation.cancel();
            mFinishFrameAnimation = null;
        }
        if (mRotateExitAnimation != null) {
            mRotateExitAnimation.cancel();
            mRotateExitAnimation = null;
        }
        if (mRotateEnterAnimation != null) {
            mRotateEnterAnimation.cancel();
            mRotateEnterAnimation = null;
        }
        if (mRotateFrameAnimation != null) {
            mRotateFrameAnimation.cancel();
            mRotateFrameAnimation = null;
        }
    }

    public boolean isAnimating() {
        return mStartEnterAnimation != null || mStartExitAnimation != null
                || mStartFrameAnimation != null
                || mFinishEnterAnimation != null || mFinishExitAnimation != null
                || mFinishFrameAnimation != null
                || mRotateEnterAnimation != null || mRotateExitAnimation != null
                || mRotateFrameAnimation != null;
    }

    private boolean stepAnimation(long now) {

        if (mFinishAnimReady && mFinishAnimStartTime < 0) {
            if (DEBUG_STATE) Slog.v(TAG, "Step: finish anim now ready");
            mFinishAnimStartTime = now;
        }

        // If the start animation is no longer running, we want to keep its
        // transformation intact until the finish animation also completes.

        mMoreStartExit = false;
        if (mStartExitAnimation != null) {
            mStartExitTransformation.clear();
            mMoreStartExit = mStartExitAnimation.getTransformation(now, mStartExitTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped start exit: " + mStartExitTransformation);
            if (!mMoreStartExit) {
                if (DEBUG_STATE) Slog.v(TAG, "Start exit animation done!");
                mStartExitAnimation.cancel();
                mStartExitAnimation = null;
            }
        }

        mMoreStartEnter = false;
        if (mStartEnterAnimation != null) {
            mStartEnterTransformation.clear();
            mMoreStartEnter = mStartEnterAnimation.getTransformation(now, mStartEnterTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped start enter: " + mStartEnterTransformation);
            if (!mMoreStartEnter) {
                if (DEBUG_STATE) Slog.v(TAG, "Start enter animation done!");
                mStartEnterAnimation.cancel();
                mStartEnterAnimation = null;
            }
        }

        mMoreStartFrame = false;
        if (mStartFrameAnimation != null) {
            mStartFrameTransformation.clear();
            mMoreStartFrame = mStartFrameAnimation.getTransformation(now, mStartFrameTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped start frame: " + mStartFrameTransformation);
            if (!mMoreStartFrame) {
                if (DEBUG_STATE) Slog.v(TAG, "Start frame animation done!");
                mStartFrameAnimation.cancel();
                mStartFrameAnimation = null;
            }
        }

        long finishNow = mFinishAnimReady ? (now - mFinishAnimStartTime) : 0;
        if (DEBUG_STATE) Slog.v(TAG, "Step: finishNow=" + finishNow);

        mFinishExitTransformation.clear();
        mMoreFinishExit = false;
        if (mFinishExitAnimation != null) {
            mMoreFinishExit = mFinishExitAnimation.getTransformation(finishNow, mFinishExitTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped finish exit: " + mFinishExitTransformation);
            if (!mMoreStartExit && !mMoreFinishExit) {
                if (DEBUG_STATE) Slog.v(TAG, "Finish exit animation done, clearing start/finish anims!");
                mStartExitTransformation.clear();
                mFinishExitAnimation.cancel();
                mFinishExitAnimation = null;
                mFinishExitTransformation.clear();
            }
        }

        mFinishEnterTransformation.clear();
        mMoreFinishEnter = false;
        if (mFinishEnterAnimation != null) {
            mMoreFinishEnter = mFinishEnterAnimation.getTransformation(finishNow, mFinishEnterTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped finish enter: " + mFinishEnterTransformation);
            if (!mMoreStartEnter && !mMoreFinishEnter) {
                if (DEBUG_STATE) Slog.v(TAG, "Finish enter animation done, clearing start/finish anims!");
                mStartEnterTransformation.clear();
                mFinishEnterAnimation.cancel();
                mFinishEnterAnimation = null;
                mFinishEnterTransformation.clear();
            }
        }

        mFinishFrameTransformation.clear();
        mMoreFinishFrame = false;
        if (mFinishFrameAnimation != null) {
            mMoreFinishFrame = mFinishFrameAnimation.getTransformation(finishNow, mFinishFrameTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped finish frame: " + mFinishFrameTransformation);
            if (!mMoreStartFrame && !mMoreFinishFrame) {
                if (DEBUG_STATE) Slog.v(TAG, "Finish frame animation done, clearing start/finish anims!");
                mStartFrameTransformation.clear();
                mFinishFrameAnimation.cancel();
                mFinishFrameAnimation = null;
                mFinishFrameTransformation.clear();
            }
        }

        mRotateExitTransformation.clear();
        mMoreRotateExit = false;
        if (mRotateExitAnimation != null) {
            mMoreRotateExit = mRotateExitAnimation.getTransformation(now, mRotateExitTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped rotate exit: " + mRotateExitTransformation);
            if (!mMoreFinishExit && !mMoreRotateExit) {
                if (DEBUG_STATE) Slog.v(TAG, "Rotate exit animation done!");
                mRotateExitAnimation.cancel();
                mRotateExitAnimation = null;
                mRotateExitTransformation.clear();
            }
        }

        mRotateEnterTransformation.clear();
        mMoreRotateEnter = false;
        if (mRotateEnterAnimation != null) {
            mMoreRotateEnter = mRotateEnterAnimation.getTransformation(now, mRotateEnterTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped rotate enter: " + mRotateEnterTransformation);
            if (!mMoreFinishEnter && !mMoreRotateEnter) {
                if (DEBUG_STATE) Slog.v(TAG, "Rotate enter animation done!");
                mRotateEnterAnimation.cancel();
                mRotateEnterAnimation = null;
                mRotateEnterTransformation.clear();
            }
        }

        mRotateFrameTransformation.clear();
        mMoreRotateFrame = false;
        if (mRotateFrameAnimation != null) {
            mMoreRotateFrame = mRotateFrameAnimation.getTransformation(now, mRotateFrameTransformation);
            if (DEBUG_TRANSFORMS) Slog.v(TAG, "Stepped rotate frame: " + mRotateFrameTransformation);
            if (!mMoreFinishFrame && !mMoreRotateFrame) {
                if (DEBUG_STATE) Slog.v(TAG, "Rotate frame animation done!");
                mRotateFrameAnimation.cancel();
                mRotateFrameAnimation = null;
                mRotateFrameTransformation.clear();
            }
        }

        mExitTransformation.set(mRotateExitTransformation);
        mExitTransformation.compose(mStartExitTransformation);
        mExitTransformation.compose(mFinishExitTransformation);

        mEnterTransformation.set(mRotateEnterTransformation);
        mEnterTransformation.compose(mStartEnterTransformation);
        mEnterTransformation.compose(mFinishEnterTransformation);

        //mFrameTransformation.set(mRotateExitTransformation);
        //mFrameTransformation.compose(mStartExitTransformation);
        //mFrameTransformation.compose(mFinishExitTransformation);
        mFrameTransformation.set(mRotateFrameTransformation);
        mFrameTransformation.compose(mStartFrameTransformation);
        mFrameTransformation.compose(mFinishFrameTransformation);
        mFrameTransformation.getMatrix().preConcat(mFrameInitialMatrix);

        if (DEBUG_TRANSFORMS) Slog.v(TAG, "Final exit: " + mExitTransformation);
        if (DEBUG_TRANSFORMS) Slog.v(TAG, "Final enter: " + mEnterTransformation);
        if (DEBUG_TRANSFORMS) Slog.v(TAG, "Final frame: " + mFrameTransformation);

        final boolean more = mMoreStartEnter || mMoreStartExit || mMoreStartFrame
                || mMoreFinishEnter || mMoreFinishExit || mMoreFinishFrame
                || mMoreRotateEnter || mMoreRotateExit || mMoreRotateFrame
                || !mFinishAnimReady;

        mSnapshotFinalMatrix.setConcat(mExitTransformation.getMatrix(), mSnapshotInitialMatrix);

        if (DEBUG_STATE) Slog.v(TAG, "Step: more=" + more);

        return more;
    }

    void updateSurfaces() {
        if (!mMoreStartExit && !mMoreFinishExit && !mMoreRotateExit) {
            if (mSurface != null) {
                if (DEBUG_STATE) Slog.v(TAG, "Exit animations done, hiding screenshot surface");
                mSurface.hide();
            }
        }

        if (!mMoreStartFrame && !mMoreFinishFrame && !mMoreRotateFrame) {
            if (mBlackFrame != null) {
                if (DEBUG_STATE) Slog.v(TAG, "Frame animations done, hiding black frame");
                mBlackFrame.hide();
            }
        } else {
            if (mBlackFrame != null) {
                mBlackFrame.setMatrix(mFrameTransformation.getMatrix());
            }
        }

        setSnapshotTransform(mSnapshotFinalMatrix, mExitTransformation.getAlpha());
    }

    public boolean stepAnimationLocked(long now) {
        if (!isAnimating()) {
            if (DEBUG_STATE) Slog.v(TAG, "Step: no animations running");
            mFinishAnimReady = false;
            return false;
        }

        if (!mAnimRunning) {
            if (DEBUG_STATE) Slog.v(TAG, "Step: starting start, finish, rotate");
            if (mStartEnterAnimation != null) {
                mStartEnterAnimation.setStartTime(now);
            }
            if (mStartExitAnimation != null) {
                mStartExitAnimation.setStartTime(now);
            }
            if (mStartFrameAnimation != null) {
                mStartFrameAnimation.setStartTime(now);
            }
            if (mFinishEnterAnimation != null) {
                mFinishEnterAnimation.setStartTime(0);
            }
            if (mFinishExitAnimation != null) {
                mFinishExitAnimation.setStartTime(0);
            }
            if (mFinishFrameAnimation != null) {
                mFinishFrameAnimation.setStartTime(0);
            }
            if (mRotateEnterAnimation != null) {
                mRotateEnterAnimation.setStartTime(now);
            }
            if (mRotateExitAnimation != null) {
                mRotateExitAnimation.setStartTime(now);
            }
            if (mRotateFrameAnimation != null) {
                mRotateFrameAnimation.setStartTime(now);
            }
            mAnimRunning = true;
        }

        return stepAnimation(now);
    }

    public Transformation getEnterTransformation() {
        return mEnterTransformation;
    }
}