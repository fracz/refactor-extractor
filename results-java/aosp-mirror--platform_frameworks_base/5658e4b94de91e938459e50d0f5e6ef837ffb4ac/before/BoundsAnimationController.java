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
 * limitations under the License.
 */

package com.android.server.wm;

import static com.android.server.wm.AppTransition.DEFAULT_APP_TRANSITION_DURATION;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_ANIM;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WITH_CLASS_NAME;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.animation.LinearInterpolator;

/**
 * Enables animating bounds of objects.
 *
 * In multi-window world bounds of both stack and tasks can change. When we need these bounds to
 * change smoothly and not require the app to relaunch (e.g. because it handles resizes and
 * relaunching it would cause poorer experience), these class provides a way to directly animate
 * the bounds of the resized object.
 *
 * The object that is resized needs to implement {@link AnimateBoundsUser} interface.
 */
public class BoundsAnimationController {
    private static final String TAG = TAG_WITH_CLASS_NAME ? "BoundsAnimationController" : TAG_WM;
    private static final int DEBUG_ANIMATION_SLOW_DOWN_FACTOR = 1;

    // Only accessed on UI thread.
    private ArrayMap<AnimateBoundsUser, BoundsAnimator> mRunningAnimations = new ArrayMap<>();

    private final class BoundsAnimator extends ValueAnimator
            implements ValueAnimator.AnimatorUpdateListener, ValueAnimator.AnimatorListener {
        private final AnimateBoundsUser mTarget;
        private final Rect mFrom;
        private final Rect mTo;
        private final Rect mTmpRect;
        private final boolean mMoveToFullScreen;
        // True if this this animation was cancelled and will be replaced the another animation from
        // the same {@link #AnimateBoundsUser} target.
        private boolean mWillReplace;
        // True to true if this animation replaced a previous animation of the same
        // {@link #AnimateBoundsUser} target.
        private final boolean mReplacement;

        BoundsAnimator(AnimateBoundsUser target, Rect from, Rect to,
                boolean moveToFullScreen, boolean replacement) {
            super();
            mTarget = target;
            mFrom = from;
            mTo = to;
            mTmpRect = new Rect();
            mMoveToFullScreen = moveToFullScreen;
            mReplacement = replacement;
            addUpdateListener(this);
            addListener(this);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final float value = (Float) animation.getAnimatedValue();
            final float remains = 1 - value;
            mTmpRect.left = (int) (mFrom.left * remains + mTo.left * value + 0.5f);
            mTmpRect.top = (int) (mFrom.top * remains + mTo.top * value + 0.5f);
            mTmpRect.right = (int) (mFrom.right * remains + mTo.right * value + 0.5f);
            mTmpRect.bottom = (int) (mFrom.bottom * remains + mTo.bottom * value + 0.5f);
            if (DEBUG_ANIM) Slog.d(TAG, "animateUpdate: mTarget=" + mTarget + ", mBounds="
                    + mTmpRect + ", from=" + mFrom + ", mTo=" + mTo + ", value=" + value
                    + ", remains=" + remains);
            if (!mTarget.setSize(mTmpRect)) {
                // Whoops, the target doesn't feel like animating anymore. Let's immediately finish
                // any further animation.
                animation.cancel();
            }
        }


        @Override
        public void onAnimationStart(Animator animation) {
            if (!mReplacement) {
                mTarget.onAnimationStart();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            finishAnimation();
            if (mMoveToFullScreen && !mWillReplace) {
                mTarget.moveToFullscreen();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            finishAnimation();
        }

        @Override
        public void cancel() {
            mWillReplace = true;
            super.cancel();
        }

        private void finishAnimation() {
            if (!mWillReplace) {
                mTarget.onAnimationEnd();
            }
            removeListener(this);
            removeUpdateListener(this);
            mRunningAnimations.remove(mTarget);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public interface AnimateBoundsUser {
        /**
         * Asks the target to directly (without any intermediate steps, like scheduling animation)
         * resize its bounds.
         *
         * @return Whether the target still wants to be animated and successfully finished the
         * operation. If it returns false, the animation will immediately be cancelled. The target
         * should return false when something abnormal happened, e.g. it was completely removed
         * from the hierarchy and is not valid anymore.
         */
        boolean setSize(Rect bounds);

        void onAnimationStart();

        /**
         * Callback for the target to inform it that the animation has ended, so it can do some
         * necessary cleanup.
         */
        void onAnimationEnd();

        void moveToFullscreen();

        void getFullScreenBounds(Rect bounds);
    }

    void animateBounds(final AnimateBoundsUser target, Rect from, Rect to) {
        boolean moveToFullscreen = false;
        if (to == null) {
            to = new Rect();
            target.getFullScreenBounds(to);
            moveToFullscreen = true;
        }

        final BoundsAnimator existing = mRunningAnimations.get(target);
        final boolean replacing = existing != null;
        if (replacing) {
            existing.cancel();
        }
        final BoundsAnimator animator =
                new BoundsAnimator(target, from, to, moveToFullscreen, replacing);
        mRunningAnimations.put(target, animator);
        animator.setFloatValues(0f, 1f);
        animator.setDuration(DEFAULT_APP_TRANSITION_DURATION * DEBUG_ANIMATION_SLOW_DOWN_FACTOR);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }
}