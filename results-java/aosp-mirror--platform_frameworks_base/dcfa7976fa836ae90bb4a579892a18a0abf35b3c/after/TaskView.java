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
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.recents.AlternateRecentsComponent;
import com.android.systemui.recents.Constants;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.model.Task;

// XXX: In debug mode, we should override invalidate() and check the layout type (do this in TaskStackView as well)

/* A task view */
public class TaskView extends FrameLayout implements Task.TaskCallbacks,
        TaskFooterView.TaskFooterViewCallbacks, View.OnClickListener, View.OnLongClickListener {
    /** The TaskView callbacks */
    interface TaskViewCallbacks {
        public void onTaskViewAppIconClicked(TaskView tv);
        public void onTaskViewAppInfoClicked(TaskView tv);
        public void onTaskViewClicked(TaskView tv, Task task, boolean lockToTask);
        public void onTaskViewDismissed(TaskView tv);
        public void onTaskViewClipStateChanged(TaskView tv);
    }

    RecentsConfiguration mConfig;

    int mDim;
    int mMaxDim;
    AccelerateInterpolator mDimInterpolator = new AccelerateInterpolator();

    Task mTask;
    boolean mTaskDataLoaded;
    boolean mIsFocused;
    boolean mIsFullScreenView;
    boolean mIsStub;
    boolean mClipViewInStack;
    AnimateableViewBounds mViewBounds;
    Paint mLayerPaint = new Paint();

    TaskThumbnailView mThumbnailView;
    TaskBarView mBarView;
    TaskFooterView mFooterView;
    TaskViewCallbacks mCb;

    // Optimizations
    ValueAnimator.AnimatorUpdateListener mUpdateDimListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateDimOverlayFromScale();
                }
            };


    public TaskView(Context context) {
        this(context, null);
    }

    public TaskView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TaskView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mConfig = RecentsConfiguration.getInstance();
        mMaxDim = mConfig.taskStackMaxDim;
        mClipViewInStack = true;
        mViewBounds = new AnimateableViewBounds(this, mConfig.taskViewRoundedCornerRadiusPx);
        setWillNotDraw(false);
        setDim(getDim());
        setOutlineProvider(mViewBounds);
    }

    /** Set callback */
    void setCallbacks(TaskViewCallbacks cb) {
        mCb = cb;
    }

    /** Gets the task */
    Task getTask() {
        return mTask;
    }

    /** Returns the view bounds. */
    AnimateableViewBounds getViewBounds() {
        return mViewBounds;
    }

    @Override
    protected void onFinishInflate() {
        // Bind the views
        mBarView = (TaskBarView) findViewById(R.id.task_view_bar);
        mThumbnailView = (TaskThumbnailView) findViewById(R.id.task_view_thumbnail);
        mFooterView = (TaskFooterView) findViewById(R.id.lock_to_app);
        mFooterView.setCallbacks(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Measure the bar view, thumbnail, and footer
        mBarView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mConfig.taskBarHeight, MeasureSpec.EXACTLY));
        mFooterView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mConfig.taskViewLockToAppButtonHeight,
                        MeasureSpec.EXACTLY));
        if (mIsFullScreenView) {
            // Measure the thumbnail height to be the full dimensions
            mThumbnailView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else {
            // Measure the thumbnail to be square
            mThumbnailView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY));
        }
        setMeasuredDimension(width, height);
    }

    /** Synchronizes this view's properties with the task's transform */
    void updateViewPropertiesToTaskTransform(TaskViewTransform toTransform, int duration) {
        // Update the bar view
        mBarView.updateViewPropertiesToTaskTransform(toTransform, duration);

        // If we are a full screen view, then only update the Z to keep it in order
        // XXX: Also update/animate the dim as well
        if (mIsFullScreenView) {
            if (Constants.DebugFlags.App.EnableShadows &&
                    toTransform.hasTranslationZChangedFrom(getTranslationZ())) {
                setTranslationZ(toTransform.translationZ);
            }
            return;
        }

        // Apply the transform
        toTransform.applyToTaskView(this, duration, mConfig.fastOutSlowInInterpolator,
                mUpdateDimListener);
    }

    /** Resets this view's properties */
    void resetViewProperties() {
        setDim(0);
        TaskViewTransform.reset(this);
    }

    /**
     * When we are un/filtering, this method will set up the transform that we are animating to,
     * in order to hide the task.
     */
    void prepareTaskTransformForFilterTaskHidden(TaskViewTransform toTransform) {
        // Fade the view out and slide it away
        toTransform.alpha = 0f;
        toTransform.translationY += 200;
        toTransform.translationZ = 0;
    }

    /**
     * When we are un/filtering, this method will setup the transform that we are animating from,
     * in order to show the task.
     */
    void prepareTaskTransformForFilterTaskVisible(TaskViewTransform fromTransform) {
        // Fade the view in
        fromTransform.alpha = 0f;
    }

    /** Prepares this task view for the enter-recents animations.  This is called earlier in the
     * first layout because the actual animation into recents may take a long time. */
    public void prepareEnterRecentsAnimation(boolean isTaskViewLaunchTargetTask, int offscreenY) {
        if (mConfig.launchedFromAppWithScreenshot) {
            if (isTaskViewLaunchTargetTask) {
                // Also hide the front most task bar view so we can animate it in
                mBarView.prepareEnterRecentsAnimation();
            } else {
                // Don't do anything for the side views
            }

        } else if (mConfig.launchedFromAppWithThumbnail) {
            if (isTaskViewLaunchTargetTask) {
                // Hide the front most task bar view so we can animate it in
                mBarView.prepareEnterRecentsAnimation();
                // Set the dim to 0 so we can animate it in
                setDim(0);
            }

        } else if (mConfig.launchedFromHome) {
            // Move the task view off screen (below) so we can animate it in
            setTranslationY(offscreenY);
            if (Constants.DebugFlags.App.EnableShadows) {
                setTranslationZ(0);
            }
            setScaleX(1f);
            setScaleY(1f);
        }
    }

    /** Animates this task view as it enters recents */
    public void startEnterRecentsAnimation(final ViewAnimation.TaskViewEnterContext ctx) {
        TaskViewTransform transform = ctx.currentTaskTransform;
        Rect taskRect = ctx.currentTaskRect;

        if (mConfig.launchedFromAppWithScreenshot) {
            if (mTask.isLaunchTarget) {
                // XXX: We would have to animate the trasnlationY of the task view bar along with the clip and
                // reset it at the bottom

                // XXX: This should actually be the inset on the current app...
                mViewBounds.animateClipTop(taskRect.top, mConfig.taskViewEnterFromHomeDuration * 5);
                mViewBounds.animateClipBottom(getMeasuredHeight() - taskRect.bottom, mConfig.taskViewEnterFromHomeDuration * 5);

                animate().scaleX(((float) taskRect.width() / getMeasuredWidth()) * transform.scale)
                        .scaleY(((float) taskRect.width() / getMeasuredWidth()) * transform.scale)
                        .translationY(taskRect.top + transform.translationY)
                        .setDuration(mConfig.taskViewEnterFromHomeDuration * 5)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                // Animate the task bar of the first task view
                                mBarView.startEnterRecentsAnimation(0, mThumbnailView.enableTaskBarClipAsRunnable(mBarView));
                                // Animate the footer into view (if it is the front most task)
                                animateFooterVisibility(true, mConfig.taskBarEnterAnimDuration);
                                // Decrement the post animation trigger
                                ctx.postAnimationTrigger.decrement();

                                // XXX Request layout and only start hte next animation after the next
                                // layout

                                setIsFullScreen(false);
                                mThumbnailView.unbindFromScreenshot();

                                // Recycle the full screen screenshot
                                AlternateRecentsComponent.consumeLastScreenshot();
                            }
                        })
                        .withLayer()
                        .start();
            } else {
                // Otherwise, just enable the thumbnail clip
                mThumbnailView.enableTaskBarClip(mBarView);

                // Animate the footer into view
                animateFooterVisibility(true, 0);
            }
            ctx.postAnimationTrigger.increment();

        } else if (mConfig.launchedFromAppWithThumbnail) {
            if (mTask.isLaunchTarget) {
                // Animate the task bar of the first task view
                mBarView.startEnterRecentsAnimation(mConfig.taskBarEnterAnimDelay,
                        mThumbnailView.enableTaskBarClipAsRunnable(mBarView));

                // Animate the dim into view as well
                ObjectAnimator anim = ObjectAnimator.ofInt(this, "dim", getDimOverlayFromScale());
                anim.setStartDelay(mConfig.taskBarEnterAnimDelay);
                anim.setDuration(mConfig.taskBarEnterAnimDuration);
                anim.setInterpolator(mConfig.fastOutLinearInInterpolator);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Decrement the post animation trigger
                        ctx.postAnimationTrigger.decrement();
                    }
                });
                anim.start();
                ctx.postAnimationTrigger.increment();

                // Animate the footer into view
                animateFooterVisibility(true, mConfig.taskBarEnterAnimDuration);
            } else {
                mThumbnailView.enableTaskBarClip(mBarView);
            }

        } else if (mConfig.launchedFromHome) {
            // Animate the tasks up
            int frontIndex = (ctx.currentStackViewCount - ctx.currentStackViewIndex - 1);
            int delay = mConfig.taskBarEnterAnimDelay +
                    frontIndex * mConfig.taskViewEnterFromHomeDelay;
            if (Constants.DebugFlags.App.EnableShadows) {
                animate().translationZ(transform.translationZ);
            }
            animate()
                    .scaleX(transform.scale)
                    .scaleY(transform.scale)
                    .translationY(transform.translationY)
                    .setStartDelay(delay)
                    .setUpdateListener(null)
                    .setInterpolator(mConfig.quintOutInterpolator)
                    .setDuration(mConfig.taskViewEnterFromHomeDuration)
                    .withLayer()
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mThumbnailView.enableTaskBarClip(mBarView);
                            // Decrement the post animation trigger
                            ctx.postAnimationTrigger.decrement();
                        }
                    })
                    .start();
            ctx.postAnimationTrigger.increment();

            // Animate the footer into view
            animateFooterVisibility(true, mConfig.taskViewEnterFromHomeDuration);

        } else {
            // Otherwise, just enable the thumbnail clip
            mThumbnailView.enableTaskBarClip(mBarView);

            // Animate the footer into view
            animateFooterVisibility(true, 0);
        }
    }

    /** Animates this task view as it leaves recents by pressing home. */
    public void startExitToHomeAnimation(ViewAnimation.TaskViewExitContext ctx) {
        animate()
                .translationY(ctx.offscreenTranslationY)
                .setStartDelay(0)
                .setUpdateListener(null)
                .setInterpolator(mConfig.fastOutLinearInInterpolator)
                .setDuration(mConfig.taskViewExitToHomeDuration)
                .withLayer()
                .withEndAction(ctx.postAnimationTrigger.decrementAsRunnable())
                .start();
        ctx.postAnimationTrigger.increment();
    }

    /** Animates this task view as it exits recents */
    public void startLaunchTaskAnimation(final Runnable r, boolean isLaunchingTask) {
        if (isLaunchingTask) {
            // Disable the thumbnail clip and animate the bar out
            mBarView.startLaunchTaskAnimation(mThumbnailView.disableTaskBarClipAsRunnable(), r);

            // Animate the dim
            if (mDim > 0) {
                ObjectAnimator anim = ObjectAnimator.ofInt(this, "dim", 0);
                anim.setDuration(mConfig.taskBarExitAnimDuration);
                anim.setInterpolator(mConfig.fastOutLinearInInterpolator);
                anim.start();
            }
        } else {
            // Hide the dismiss button
            mBarView.startLaunchTaskDismissAnimation();
        }
    }

    /** Animates the deletion of this task view */
    public void startDeleteTaskAnimation(final Runnable r) {
        // Disabling clipping with the stack while the view is animating away
        setClipViewInStack(false);

        animate().translationX(mConfig.taskViewRemoveAnimTranslationXPx)
            .alpha(0f)
            .setStartDelay(0)
            .setUpdateListener(null)
            .setInterpolator(mConfig.fastOutSlowInInterpolator)
            .setDuration(mConfig.taskViewRemoveAnimDuration)
            .withLayer()
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    // We just throw this into a runnable because starting a view property
                    // animation using layers can cause inconsisten results if we try and
                    // update the layers while the animation is running.  In some cases,
                    // the runnabled passed in may start an animation which also uses layers
                    // so we defer all this by posting this.
                    r.run();

                    // Re-enable clipping with the stack (we will reuse this view)
                    setClipViewInStack(true);
                }
            })
            .start();
    }

    /** Animates this task view if the user does not interact with the stack after a certain time. */
    public void startNoUserInteractionAnimation() {
        mBarView.startNoUserInteractionAnimation();
    }

    /** Mark this task view that the user does has not interacted with the stack after a certain time. */
    public void setNoUserInteractionState() {
        mBarView.setNoUserInteractionState();
    }

    /** Sets whether this task view is full screen or not. */
    void setIsFullScreen(boolean isFullscreen) {
        mIsFullScreenView = isFullscreen;
        mBarView.setIsFullscreen(isFullscreen);
        if (isFullscreen) {
            // If we are full screen, then disable the bottom outline clip for the footer
            mViewBounds.setOutlineClipBottom(0);
        }
    }

    /** Returns whether this task view should currently be drawn as a full screen view. */
    boolean isFullScreenView() {
        return mIsFullScreenView;
    }

    /** Sets the stubbed state of this task view. */
    void setStubState(boolean isStub) {
        mIsStub = isStub;
    }

    /**
     * Returns whether this view should be clipped, or any views below should clip against this
     * view.
     */
    boolean shouldClipViewInStack() {
        return mClipViewInStack && !mIsFullScreenView && (getVisibility() == View.VISIBLE);
    }

    /** Sets whether this view should be clipped, or clipped against. */
    void setClipViewInStack(boolean clip) {
        if (clip != mClipViewInStack) {
            mClipViewInStack = clip;
            mCb.onTaskViewClipStateChanged(this);
        }
    }

    /** Gets the max footer height. */
    public int getMaxFooterHeight() {
        return mFooterView.mMaxFooterHeight;
    }

    /** Animates the footer into and out of view. */
    void animateFooterVisibility(boolean visible, int duration) {
        // Hide the footer if we are a full screen view
        if (mIsFullScreenView) return;
        // Hide the footer if the current task can not be locked to
        if (!mTask.lockToTaskEnabled || !mTask.lockToThisTask) return;
        // Otherwise, animate the visibility
        mFooterView.animateFooterVisibility(visible, duration);
    }

    /** Returns the current dim. */
    public void setDim(int dim) {
        mDim = dim;
        postInvalidateOnAnimation();
    }

    /** Returns the current dim. */
    public int getDim() {
        return mDim;
    }

    /** Compute the dim as a function of the scale of this view. */
    int getDimOverlayFromScale() {
        float minScale = TaskStackViewLayoutAlgorithm.StackPeekMinScale;
        float scaleRange = 1f - minScale;
        float dim = (1f - getScaleX()) / scaleRange;
        dim = mDimInterpolator.getInterpolation(Math.min(dim, 1f));
        return Math.max(0, Math.min(mMaxDim, (int) (dim * 255)));
    }

    /** Update the dim as a function of the scale of this view. */
    void updateDimOverlayFromScale() {
        setDim(getDimOverlayFromScale());
    }

    /**** View drawing ****/

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // Apply the dim if necessary
        if (mDim > 0) {
            canvas.drawColor(mDim << 24);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (mIsStub && (child != mBarView)) {
            // Skip the thumbnail view if we are in stub mode
            return false;
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    /** Enable the hw layers on this task view */
    void enableHwLayers() {
        mThumbnailView.setLayerType(View.LAYER_TYPE_HARDWARE, mLayerPaint);
        mBarView.enableHwLayers();
        mFooterView.setLayerType(View.LAYER_TYPE_HARDWARE, mLayerPaint);
    }

    /** Disable the hw layers on this task view */
    void disableHwLayers() {
        mThumbnailView.setLayerType(View.LAYER_TYPE_NONE, mLayerPaint);
        mBarView.disableHwLayers();
        mFooterView.setLayerType(View.LAYER_TYPE_NONE, mLayerPaint);
    }

    /**** View focus state ****/

    /**
     * Sets the focused task explicitly. We need a separate flag because requestFocus() won't happen
     * if the view is not currently visible, or we are in touch state (where we still want to keep
     * track of focus).
     */
    public void setFocusedTask() {
        mIsFocused = true;
        // Workaround, we don't always want it focusable in touch mode, but we want the first task
        // to be focused after the enter-recents animation, which can be triggered from either touch
        // or keyboard
        setFocusableInTouchMode(true);
        requestFocus();
        setFocusableInTouchMode(false);
        invalidate();
    }

    /**
     * Updates the explicitly focused state when the view focus changes.
     */
    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (!gainFocus) {
            mIsFocused = false;
            invalidate();
        }
    }

    /**
     * Returns whether we have explicitly been focused.
     */
    public boolean isFocusedTask() {
        return mIsFocused || isFocused();
    }

    /**** TaskCallbacks Implementation ****/

    /** Binds this task view to the task */
    public void onTaskBound(Task t) {
        mTask = t;
        mTask.setCallbacks(this);
        if (getMeasuredWidth() == 0) {
            // If we haven't yet measured, we should just set the footer height with any animation
            animateFooterVisibility(t.lockToThisTask, 0);
        } else {
            animateFooterVisibility(t.lockToThisTask, mConfig.taskViewLockToAppLongAnimDuration);
        }
    }

    @Override
    public void onTaskDataLoaded() {
        if (mThumbnailView != null && mBarView != null) {
            // Bind each of the views to the new task data
            if (mIsFullScreenView) {
                mThumbnailView.bindToScreenshot(AlternateRecentsComponent.getLastScreenshot());
            } else {
                mThumbnailView.rebindToTask(mTask);
            }
            mBarView.rebindToTask(mTask);
            // Rebind any listeners
            if (Constants.DebugFlags.App.EnableTaskFiltering) {
                mBarView.mApplicationIcon.setOnClickListener(this);
            }
            mBarView.mDismissButton.setOnClickListener(this);
            mFooterView.setOnClickListener(this);
            if (Constants.DebugFlags.App.EnableDevAppInfoOnLongPress) {
                if (mConfig.developerOptionsEnabled) {
                    mBarView.mApplicationIcon.setOnLongClickListener(this);
                }
            }
        }
        mTaskDataLoaded = true;
    }

    @Override
    public void onTaskDataUnloaded() {
        if (mThumbnailView != null && mBarView != null) {
            // Unbind each of the views from the task data and remove the task callback
            mTask.setCallbacks(null);
            mThumbnailView.unbindFromTask();
            mBarView.unbindFromTask();
            // Unbind any listeners
            if (Constants.DebugFlags.App.EnableTaskFiltering) {
                mBarView.mApplicationIcon.setOnClickListener(null);
            }
            mBarView.mDismissButton.setOnClickListener(null);
            mFooterView.setOnClickListener(null);
            if (Constants.DebugFlags.App.EnableDevAppInfoOnLongPress) {
                mBarView.mApplicationIcon.setOnLongClickListener(null);
            }
        }
        mTaskDataLoaded = false;
    }

    /** Enables/disables handling touch on this task view. */
    void setTouchEnabled(boolean enabled) {
        setOnClickListener(enabled ? this : null);
    }

    /**** TaskFooterView.TaskFooterViewCallbacks ****/

    @Override
    public void onTaskFooterHeightChanged(int height, int maxHeight) {
        if (mIsFullScreenView) {
            // Disable the bottom outline clip when fullscreen
            mViewBounds.setOutlineClipBottom(0);
        } else {
            // Update the bottom clip in our outline provider
            mViewBounds.setOutlineClipBottom(maxHeight - height);
        }
    }

    /**** View.OnClickListener Implementation ****/

    @Override
    public void onClick(final View v) {
        // We purposely post the handler delayed to allow for the touch feedback to draw
        final TaskView tv = this;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (v == mBarView.mApplicationIcon) {
                    mCb.onTaskViewAppIconClicked(tv);
                } else if (v == mBarView.mDismissButton) {
                    // Animate out the view and call the callback
                    startDeleteTaskAnimation(new Runnable() {
                        @Override
                        public void run() {
                            mCb.onTaskViewDismissed(tv);
                        }
                    });
                    // Hide the footer
                    tv.animateFooterVisibility(false, mConfig.taskViewRemoveAnimDuration);
                } else if (v == tv || v == mFooterView) {
                    mCb.onTaskViewClicked(tv, tv.getTask(), (v == mFooterView));
                }
            }
        }, 125);
    }

    /**** View.OnLongClickListener Implementation ****/

    @Override
    public boolean onLongClick(View v) {
        if (v == mBarView.mApplicationIcon) {
            mCb.onTaskViewAppInfoClicked(this);
            return true;
        }
        return false;
    }
}