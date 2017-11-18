/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.OverScroller;
import com.android.systemui.R;
import com.android.systemui.recents.misc.Utilities;

/* The scrolling logic for a TaskStackView */
public class TaskStackViewScroller {

    private static final String TAG = "TaskStackViewScroller";
    private static final boolean DEBUG = false;

    public interface TaskStackViewScrollerCallbacks {
        void onScrollChanged(float prevScroll, float curScroll);
    }

    Context mContext;
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    TaskStackViewScrollerCallbacks mCb;

    float mStackScrollP;
    float mFlingDownScrollP;
    int mFlingDownY;

    OverScroller mScroller;
    ObjectAnimator mScrollAnimator;
    float mFinalAnimatedScroll;

    private Interpolator mLinearOutSlowInInterpolator;

    public TaskStackViewScroller(Context context, TaskStackLayoutAlgorithm layoutAlgorithm) {
        mContext = context;
        mScroller = new OverScroller(context);
        mLayoutAlgorithm = layoutAlgorithm;
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.linear_out_slow_in);
        setStackScroll(getStackScroll());
    }

    /** Resets the task scroller. */
    void reset() {
        mStackScrollP = 0f;
    }

    /** Sets the callbacks */
    void setCallbacks(TaskStackViewScrollerCallbacks cb) {
        mCb = cb;
    }

    /** Gets the current stack scroll */
    public float getStackScroll() {
        return mStackScrollP;
    }

    /** Sets the current stack scroll */
    public void setStackScroll(float s) {
        float prevStackScroll = mStackScrollP;
        mStackScrollP = s;
        if (mCb != null) {
            mCb.onScrollChanged(prevStackScroll, mStackScrollP);
        }
    }

    /**
     * Sets the current stack scroll to the initial state when you first enter recents.
     * @return whether the stack progress changed.
     */
    public boolean setStackScrollToInitialState() {
        float prevStackScrollP = mStackScrollP;
        setStackScroll(getBoundedStackScroll(mLayoutAlgorithm.mInitialScrollP));
        return Float.compare(prevStackScrollP, mStackScrollP) != 0;
    }

    /**
     * Starts a fling that is coordinated with the {@link TaskStackViewTouchHandler}.
     */
    public void fling(float downScrollP, int downY, int y, int velY, int minY, int maxY,
            int overscroll) {
        if (DEBUG) {
            Log.d(TAG, "fling: " + downScrollP + ", downY: " + downY + ", y: " + y +
                    ", velY: " + velY + ", minY: " + minY + ", maxY: " + maxY);
        }
        mFlingDownScrollP = downScrollP;
        mFlingDownY = downY;
        mScroller.fling(0, y, 0, velY, 0, 0, minY, maxY, 0, overscroll);
    }

    /** Bounds the current scroll if necessary */
    public boolean boundScroll() {
        float curScroll = getStackScroll();
        float newScroll = getBoundedStackScroll(curScroll);
        if (Float.compare(newScroll, curScroll) != 0) {
            setStackScroll(newScroll);
            return true;
        }
        return false;
    }

    /** Returns the bounded stack scroll */
    float getBoundedStackScroll(float scroll) {
        return Math.max(mLayoutAlgorithm.mMinScrollP,
                Math.min(mLayoutAlgorithm.mMaxScrollP, scroll));
    }

    /** Returns the amount that the absolute value of how much the scroll is out of bounds. */
    float getScrollAmountOutOfBounds(float scroll) {
        if (scroll < mLayoutAlgorithm.mMinScrollP) {
            return Math.abs(scroll - mLayoutAlgorithm.mMinScrollP);
        } else if (scroll > mLayoutAlgorithm.mMaxScrollP) {
            return Math.abs(scroll - mLayoutAlgorithm.mMaxScrollP);
        }
        return 0f;
    }

    /** Returns whether the specified scroll is out of bounds */
    boolean isScrollOutOfBounds() {
        return Float.compare(getScrollAmountOutOfBounds(mStackScrollP), 0f) != 0;
    }

    /** Animates the stack scroll into bounds */
    ObjectAnimator animateBoundScroll() {
        float curScroll = getStackScroll();
        float newScroll = getBoundedStackScroll(curScroll);
        if (Float.compare(newScroll, curScroll) != 0) {
            // Start a new scroll animation
            animateScroll(curScroll, newScroll, null);
        }
        return mScrollAnimator;
    }

    /** Animates the stack scroll */
    void animateScroll(float curScroll, float newScroll, final Runnable postRunnable) {
        // Finish any current scrolling animations
        if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
            setStackScroll(mFinalAnimatedScroll);
            mScroller.startScroll(0, progressToScrollRange(mFinalAnimatedScroll), 0, 0, 0);
        }
        stopScroller();
        stopBoundScrollAnimation();

        mFinalAnimatedScroll = newScroll;
        mScrollAnimator = ObjectAnimator.ofFloat(this, "stackScroll", curScroll, newScroll);
        mScrollAnimator.setDuration(mContext.getResources().getInteger(
                R.integer.recents_animate_task_stack_scroll_duration));
        mScrollAnimator.setInterpolator(mLinearOutSlowInInterpolator);
        mScrollAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (postRunnable != null) {
                    postRunnable.run();
                }
                mScrollAnimator.removeAllListeners();
            }
        });
        mScrollAnimator.start();
    }

    /** Aborts any current stack scrolls */
    void stopBoundScrollAnimation() {
        Utilities.cancelAnimationWithoutCallbacks(mScrollAnimator);
    }

    /**** OverScroller ****/

    // TODO: Remove
    @Deprecated
    int progressToScrollRange(float p) {
        return (int) (p * mLayoutAlgorithm.mStackRect.height());
    }

    /** Called from the view draw, computes the next scroll. */
    boolean computeScroll() {
        if (mScroller.computeScrollOffset()) {
            float deltaP = mLayoutAlgorithm.getDeltaPForY(mFlingDownY, mScroller.getCurrY());
            float scroll = mFlingDownScrollP + deltaP;
            setStackScroll(scroll);
            if (DEBUG) {
                Log.d(TAG, "computeScroll: " + scroll);
            }
            return true;
        }
        return false;
    }

    /** Returns whether the overscroller is scrolling. */
    boolean isScrolling() {
        return !mScroller.isFinished();
    }

    /** Stops the scroller and any current fling. */
    void stopScroller() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }
}