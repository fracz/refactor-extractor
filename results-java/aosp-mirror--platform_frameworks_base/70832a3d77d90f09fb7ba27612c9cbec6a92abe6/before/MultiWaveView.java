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

package com.android.internal.widget.multiwaveview;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.android.internal.R;

/**
 * A special widget containing a center and outer ring. Moving the center ring to the outer ring
 * causes an event that can be caught by implementing OnTriggerListener.
 */
public class MultiWaveView extends View implements AnimatorUpdateListener {
    private static final String TAG = "MultiWaveView";
    private static final boolean DEBUG = false;

    // Wave state machine
    private static final int STATE_IDLE = 0;
    private static final int STATE_FIRST_TOUCH = 1;
    private static final int STATE_TRACKING = 2;
    private static final int STATE_SNAP = 3;
    private static final int STATE_FINISH = 4;

    // Animation properties.
    private static final float SNAP_MARGIN_DEFAULT = 20.0f; // distance to ring before we snap to it

    public interface OnTriggerListener {
        int NO_HANDLE = 0;
        int CENTER_HANDLE = 1;
        public void onGrabbed(View v, int handle);
        public void onReleased(View v, int handle);
        public void onTrigger(View v, int target);
        public void onGrabbedStateChange(View v, int handle);
    }

    // Tune-able parameters
    private static final int CHEVRON_INCREMENTAL_DELAY = 50;
    private static final int CHEVRON_ANIMATION_DURATION = 1000;
    private static final int RETURN_TO_HOME_DURATION = 150;
    private static final int HIDE_ANIMATION_DELAY = 500;
    private static final int HIDE_ANIMATION_DURACTION = 2000;
    private static final int SHOW_ANIMATION_DURATION = 0;
    private static final int SHOW_ANIMATION_DELAY = 0;
    private TimeInterpolator mChevronAnimationInterpolator = Ease.Quint.easeOut;

    private ArrayList<TargetDrawable> mTargetDrawables = new ArrayList<TargetDrawable>();
    private ArrayList<TargetDrawable> mChevronDrawables = new ArrayList<TargetDrawable>();
    private ArrayList<Tweener> mChevronAnimations = new ArrayList<Tweener>();
    private ArrayList<Tweener> mTargetAnimations = new ArrayList<Tweener>();
    private Tweener mHandleAnimation;
    private OnTriggerListener mOnTriggerListener;
    private TargetDrawable mHandleDrawable;
    private TargetDrawable mOuterRing;
    private Vibrator mVibrator;

    private int mFeedbackCount = 3;
    private int mVibrationDuration = 0;
    private int mGrabbedState;
    private int mActiveTarget = -1;
    private float mTapRadius;
    private float mWaveCenterX;
    private float mWaveCenterY;
    private float mVerticalOffset;
    private float mHorizontalOffset;
    private float mOuterRadius = 0.0f;
    private float mHitRadius = 0.0f;
    private float mSnapMargin = 0.0f;
    private boolean mDragging;

    private AnimatorListener mResetListener = new Animator.AnimatorListener() {
        public void onAnimationStart(Animator animation) { }
        public void onAnimationRepeat(Animator animation) { }
        public void onAnimationEnd(Animator animation) {
            switchToState(STATE_IDLE, mWaveCenterX, mWaveCenterY);
        }
        public void onAnimationCancel(Animator animation) { }
    };

    public MultiWaveView(Context context) {
        this(context, null);
    }

    public MultiWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiWaveView);
        mOuterRadius = a.getDimension(R.styleable.MultiWaveView_outerRadius, mOuterRadius);
        mHorizontalOffset = a.getDimension(R.styleable.MultiWaveView_horizontalOffset,
                mHorizontalOffset);
        mVerticalOffset = a.getDimension(R.styleable.MultiWaveView_verticalOffset,
                mVerticalOffset);
        mHitRadius = a.getDimension(R.styleable.MultiWaveView_hitRadius, mHitRadius);
        mSnapMargin = a.getDimension(R.styleable.MultiWaveView_snapMargin, mSnapMargin);
        mVibrationDuration = a.getInt(R.styleable.MultiWaveView_vibrationDuration,
                mVibrationDuration);
        mFeedbackCount = a.getInt(R.styleable.MultiWaveView_feedbackCount,
                mFeedbackCount);
        mHandleDrawable = new TargetDrawable(res,
                a.getDrawable(R.styleable.MultiWaveView_handleDrawable));
        mTapRadius = mHandleDrawable.getWidth()/2;
        mOuterRing = new TargetDrawable(res, a.getDrawable(R.styleable.MultiWaveView_waveDrawable));

        // Read animation drawables
        Drawable leftChevron = a.getDrawable(R.styleable.MultiWaveView_leftChevronDrawable);
        if (leftChevron != null) {
            for (int i = 0; i < mFeedbackCount; i++) {
                mChevronDrawables.add(new TargetDrawable(res, leftChevron));
            }
        }
        Drawable rightChevron = a.getDrawable(R.styleable.MultiWaveView_rightChevronDrawable);
        if (rightChevron != null) {
            for (int i = 0; i < mFeedbackCount; i++) {
                mChevronDrawables.add(new TargetDrawable(res, rightChevron));
            }
        }

        // Read array of target drawables
        TypedValue outValue = new TypedValue();
        if (a.getValue(R.styleable.MultiWaveView_targetDrawables, outValue)) {
            setTargetResources(outValue.resourceId);
        }
        if (mTargetDrawables == null || mTargetDrawables.size() == 0) {
            throw new IllegalStateException("Must specify at least one target drawable");
        }

        setVibrateEnabled(mVibrationDuration > 0);
    }

    private void dump() {
        Log.v(TAG, "Outer Radius = " + mOuterRadius);
        Log.v(TAG, "HitRadius = " + mHitRadius);
        Log.v(TAG, "SnapMargin = " + mSnapMargin);
        Log.v(TAG, "FeedbackCount = " + mFeedbackCount);
        Log.v(TAG, "VibrationDuration = " + mVibrationDuration);
        Log.v(TAG, "TapRadius = " + mTapRadius);
        Log.v(TAG, "WaveCenterX = " + mWaveCenterX);
        Log.v(TAG, "WaveCenterY = " + mWaveCenterY);
        Log.v(TAG, "HorizontalOffset = " + mHorizontalOffset);
        Log.v(TAG, "VerticalOffset = " + mVerticalOffset);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // View should be large enough to contain the background + target drawable on either edge
        return mOuterRing.getWidth()
                + (mTargetDrawables.size() > 0 ? (mTargetDrawables.get(0).getWidth()) : 0);
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        // View should be large enough to contain the unlock ring + target drawable on either edge
        return mOuterRing.getHeight()
                + (mTargetDrawables.size() > 0 ? (mTargetDrawables.get(0).getHeight()) : 0);
    }

    private void switchToState(int state, float x, float y) {
        switch (state) {
            case STATE_IDLE:
                stopChevronAnimation();
                deactivateTargets();
                mHandleDrawable.setState(TargetDrawable.STATE_INACTIVE);
                break;

            case STATE_FIRST_TOUCH:
                stopHandleAnimation();
                deactivateTargets();
                showTargets();
                mHandleDrawable.setState(TargetDrawable.STATE_ACTIVE);
                setGrabbedState(OnTriggerListener.CENTER_HANDLE);
                break;

            case STATE_TRACKING:
                break;

            case STATE_SNAP:
                break;

            case STATE_FINISH:
                doFinish();
                break;
        }
    }

    /**
     * Animation used to attract user's attention to the target button.
     * Assumes mChevronDrawables is an a list with an even number of chevrons filled with left
     * followed by right chevrons.
     */
    private void startChevronAnimation() {
        final int icons = mChevronDrawables.size();
        for (Tweener tween : mChevronAnimations) {
            tween.animator.cancel();
        }
        for (int i = 0; i < icons; i++) {
            TargetDrawable icon = mChevronDrawables.get(i);
            icon.setY(mWaveCenterY);
            icon.setAlpha(1.0f);
            mChevronAnimations.clear();
            int delay = (int) (Math.abs(0.5f + i - icons / 2) * CHEVRON_INCREMENTAL_DELAY);
            if (i < icons/2) {
                // Left chevrons
                icon.setX(mWaveCenterX - mHandleDrawable.getWidth() / 2);
                mChevronAnimations.add(Tweener.to(icon, CHEVRON_ANIMATION_DURATION,
                        "ease", mChevronAnimationInterpolator,
                        "delay", delay,
                        "x", mWaveCenterX - mOuterRadius,
                        "alpha", 0.0f,
                        "onUpdate", this));
            } else {
                // Right chevrons
                icon.setX(mWaveCenterX + mHandleDrawable.getWidth() / 2);
                mChevronAnimations.add(Tweener.to(icon, CHEVRON_ANIMATION_DURATION,
                        "ease", mChevronAnimationInterpolator,
                        "delay", delay,
                        "x", mWaveCenterX + mOuterRadius,
                        "alpha", 0.0f,
                        "onUpdate", this));
            }
        }
    }

    private void stopChevronAnimation() {
        for (Tweener anim : mChevronAnimations) {
            anim.animator.end();
        }
        mChevronAnimations.clear();
    }

    private void stopHandleAnimation() {
        if (mHandleAnimation != null) {
            mHandleAnimation.animator.end();
            mHandleAnimation = null;
        }
    }

    private void deactivateTargets() {
        for (TargetDrawable target : mTargetDrawables) {
            target.setState(TargetDrawable.STATE_INACTIVE);
        }
        mActiveTarget = -1;
    }

    void invalidateGlobalRegion(TargetDrawable drawable) {
        int width = drawable.getWidth();
        int height = drawable.getHeight();
        RectF childBounds = new RectF(0, 0, width, height);
        childBounds.offset(drawable.getX() - width/2, drawable.getY() - height/2);
        View view = this;
        while (view.getParent() != null && view.getParent() instanceof View) {
            view = (View) view.getParent();
            view.getMatrix().mapRect(childBounds);
            view.invalidate((int) Math.floor(childBounds.left),
                    (int) Math.floor(childBounds.top),
                    (int) Math.ceil(childBounds.right),
                    (int) Math.ceil(childBounds.bottom));
        }
    }

    /**
     * Dispatches a trigger event to listener. Ignored if a listener is not set.
     * @param whichHandle the handle that triggered the event.
     */
    private void dispatchTriggerEvent(int whichHandle) {
        vibrate();
        if (mOnTriggerListener != null) {
            mOnTriggerListener.onTrigger(this, whichHandle);
        }
    }

    private void doFinish() {
        // Inform listener of any active targets.  Typically only one will be active.
        final int activeTarget = mActiveTarget;
        boolean targetHit =  activeTarget != -1;
        if (targetHit) {
            Log.v(TAG, "Finish with target hit = " + targetHit);
            dispatchTriggerEvent(mActiveTarget);
        }

        setGrabbedState(OnTriggerListener.NO_HANDLE);

        // Animate finger outline back to home position
        mHandleDrawable.setAlpha(targetHit ? 0.0f : 1.0f);
        mHandleAnimation = Tweener.to(mHandleDrawable, RETURN_TO_HOME_DURATION,
                "ease", Ease.Quart.easeOut,
                "delay", targetHit ? HIDE_ANIMATION_DELAY : 0,
                "alpha", 1.0f,
                "x", mWaveCenterX,
                "y", mWaveCenterY,
                "onUpdate", this,
                "onComplete", mResetListener);

        // Hide unselected targets
        hideTargets(true);

        // Highlight the selected one
        if (targetHit) {
            mTargetDrawables.get(activeTarget).setState(TargetDrawable.STATE_ACTIVE);
        }

        stopChevronAnimation();
    }

    private void hideTargets(boolean animate) {
        if (mTargetAnimations.size() > 0) {
            stopTargetAnimation();
        }
        for (TargetDrawable target : mTargetDrawables) {
            target.setState(TargetDrawable.STATE_INACTIVE);
            mTargetAnimations.add(Tweener.to(target,
                    animate ? HIDE_ANIMATION_DURACTION : 0,
                    "alpha", 0.0f,
                    "delay", HIDE_ANIMATION_DELAY,
                    "onUpdate", this));
        }
        mTargetAnimations.add(Tweener.to(mOuterRing,
                animate ? HIDE_ANIMATION_DURACTION : 0,
                "alpha", 0.0f,
                "delay", HIDE_ANIMATION_DELAY,
                "onUpdate", this));
    }

    private void showTargets() {
        if (mTargetAnimations.size() > 0) {
            stopTargetAnimation();
        }
        for (TargetDrawable target : mTargetDrawables) {
            target.setState(TargetDrawable.STATE_INACTIVE);
            mTargetAnimations.add(Tweener.to(target, SHOW_ANIMATION_DURATION,
                    "alpha", 1.0f,
                    "delay", SHOW_ANIMATION_DELAY,
                    "onUpdate", this));
        }
        mTargetAnimations.add(Tweener.to(mOuterRing, SHOW_ANIMATION_DURATION,
                "alpha", 1.0f,
                "delay", SHOW_ANIMATION_DELAY,
                "onUpdate", this));
    }

    private void stopTargetAnimation() {
        for (Tweener anim : mTargetAnimations) {
            anim.animator.end();
        }
        mTargetAnimations.clear();
    }

    private void vibrate() {
        if (mVibrator != null) {
            mVibrator.vibrate(mVibrationDuration);
        }
    }

    /**
     * Loads an array of drawables from the given resourceId.
     *
     * @param resourceId
     */
    public void setTargetResources(int resourceId) {
        Resources res = getContext().getResources();
        TypedArray array = res.obtainTypedArray(resourceId);
        int count = array.length();
        mTargetDrawables = new ArrayList<TargetDrawable>(count);
        for (int i = 0; i < count; i++) {
            Drawable drawable = array.getDrawable(i);
            mTargetDrawables.add(new TargetDrawable(res, drawable));
        }
    }

    /**
     * Enable or disable vibrate on touch.
     *
     * @param enabled
     */
    public void setVibrateEnabled(boolean enabled) {
        if (enabled && mVibrator == null) {
            mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        } else {
            mVibrator = null;
        }
    }

    /**
     * Starts chevron animation. Example use case: show chevron animation whenever the phone rings
     * or the user touches the screen.
     *
     */
    public void ping() {
        stopChevronAnimation();
        startChevronAnimation();
    }

    /**
     * Resets the widget to default state and cancels all animation. If animate is 'true', will
     * animate objects into place. Otherwise, objects will snap back to place.
     *
     * @param animate
     */
    public void reset(boolean animate) {
        stopChevronAnimation();
        stopHandleAnimation();
        stopTargetAnimation();
        hideChevrons();
        hideTargets(animate);
        mHandleDrawable.setX(mWaveCenterX);
        mHandleDrawable.setY(mWaveCenterY);
        mHandleDrawable.setState(TargetDrawable.STATE_INACTIVE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        boolean handled = false;
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                handleDown(x, y);
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                handleMove(x, y);
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                handleUp(x, y);
                handleMove(x, y);
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handleMove(x, y);
                handled = true;
                break;
        }
        invalidate();
        return handled ? true : super.onTouchEvent(event);
    }

    private void handleDown(float x, float y) {
        final float dx = x - mWaveCenterX;
        final float dy = y - mWaveCenterY;
        if (dist2(dx,dy) <= square(mTapRadius)) {
            if (DEBUG) Log.v(TAG, "** Handle HIT");
            switchToState(STATE_FIRST_TOUCH, x, y);
            mDragging = true;
        } else {
            mDragging = false;
            stopTargetAnimation();
            ping();
        }
    }

    private void handleUp(float x, float y) {
        if (DEBUG && mDragging) Log.v(TAG, "** Handle RELEASE");
        switchToState(STATE_FINISH, x, y);
    }

    private void handleMove(float x, float y) {
        if (!mDragging) {
            return;
        }

        float tx = x - mWaveCenterX;
        float ty = y - mWaveCenterY;
        float touchRadius = (float) Math.sqrt(dist2(tx, ty));
        final float scale = touchRadius > mOuterRadius ? mOuterRadius / touchRadius : 1.0f;
        float limitX = mWaveCenterX + tx * scale;
        float limitY = mWaveCenterY + ty * scale;

        int activeTarget = -1;
        boolean singleTarget = mTargetDrawables.size() == 1;
        if (singleTarget) {
            // Snap to outer ring if there's only one target
            float snapRadius = mOuterRadius - mSnapMargin;
            if (touchRadius > snapRadius) {
                activeTarget = 0;
                x = limitX;
                y = limitY;
            }
        } else {
            // If there's more than one target, snap to the closest one less than hitRadius away.
            float best = Float.MAX_VALUE;
            final float hitRadius2 = mHitRadius * mHitRadius;
            for (int i = 0; i < mTargetDrawables.size(); i++) {
                // Snap to the first target in range
                TargetDrawable target = mTargetDrawables.get(i);
                float dx = limitX - target.getX();
                float dy = limitY - target.getY();
                float dist2 = dx*dx + dy*dy;
                if (target.isValid() && dist2 < hitRadius2 && dist2 < best) {
                    activeTarget = i;
                    best = dist2;
                }
            }
        }
        if (activeTarget != -1) {
            switchToState(STATE_SNAP, x,y);
            mHandleDrawable.setX(singleTarget ? limitX : mTargetDrawables.get(activeTarget).getX());
            mHandleDrawable.setY(singleTarget ? limitY : mTargetDrawables.get(activeTarget).getY());
            TargetDrawable currentTarget = mTargetDrawables.get(activeTarget);
            if (currentTarget.hasState(TargetDrawable.STATE_FOCUSED)) {
                currentTarget.setState(TargetDrawable.STATE_FOCUSED);
                mHandleDrawable.setAlpha(0.0f);
            }
        } else {
            switchToState(STATE_TRACKING, x, y);
            mHandleDrawable.setX(x);
            mHandleDrawable.setY(y);
            mHandleDrawable.setAlpha(1.0f);
        }
        // Draw handle outside parent's bounds
        invalidateGlobalRegion(mHandleDrawable);

        if (mActiveTarget != activeTarget && activeTarget != -1) {
            vibrate();
        }
        mActiveTarget = activeTarget;
    }

    /**
     * Sets the current grabbed state, and dispatches a grabbed state change
     * event to our listener.
     */
    private void setGrabbedState(int newState) {
        if (newState != mGrabbedState) {
            if (newState != OnTriggerListener.NO_HANDLE) {
                vibrate();
            }
            mGrabbedState = newState;
            if (mOnTriggerListener != null) {
                mOnTriggerListener.onGrabbedStateChange(this, mGrabbedState);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int width = right - left;
        final int height = bottom - top;

        mWaveCenterX = mHorizontalOffset + Math.max(width, mOuterRing.getWidth() ) / 2;
        mWaveCenterY = mVerticalOffset + Math.max(height, mOuterRing.getHeight()) / 2;
        mHandleDrawable.setX(mWaveCenterX);
        mHandleDrawable.setY(mWaveCenterY);
        mOuterRing.setX(mWaveCenterX);
        mOuterRing.setY(Math.max(mWaveCenterY, mWaveCenterY));
        mOuterRing.setAlpha(0.0f);
        if (mOuterRadius == 0.0f) {
            mOuterRadius = 0.5f*(float) Math.sqrt(dist2(mWaveCenterX, mWaveCenterY));
        }
        if (mHitRadius == 0.0f) {
            // Use the radius of inscribed circle of the first target.
            mHitRadius = mTargetDrawables.get(0).getWidth() / 2.0f;
        }
        if (mSnapMargin == 0.0f) {
            mSnapMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    SNAP_MARGIN_DEFAULT, getContext().getResources().getDisplayMetrics());
        }
        for (int i = 0; i < mTargetDrawables.size(); i++) {
            final TargetDrawable targetIcon = mTargetDrawables.get(i);
            double angle = -2.0f * Math.PI * i / mTargetDrawables.size();
            float xPosition = mWaveCenterX + mOuterRadius * (float) Math.cos(angle);
            float yPosition = mWaveCenterY + mOuterRadius * (float) Math.sin(angle);
            targetIcon.setX(xPosition);
            targetIcon.setY(yPosition);
            targetIcon.setAlpha(0.0f);
        }
        hideChevrons();
        hideTargets(false);
        if (DEBUG) dump();
    }

    private void hideChevrons() {
        for (TargetDrawable chevron : mChevronDrawables) {
            chevron.setAlpha(0.0f);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mOuterRing.draw(canvas);
        for (TargetDrawable target : mTargetDrawables) {
            target.draw(canvas);
        }
        for (TargetDrawable target : mChevronDrawables) {
            target.draw(canvas);
        }
        mHandleDrawable.draw(canvas);
    }

    public void setOnTriggerListener(OnTriggerListener listener) {
        mOnTriggerListener = listener;
    }

    public void onAnimationUpdate(ValueAnimator animation) {
        invalidateGlobalRegion(mHandleDrawable);
        invalidate();
    }

    private float square(float d) {
        return d * d;
    }

    private float dist2(float dx, float dy) {
        return dx*dx + dy*dy;
    }

}