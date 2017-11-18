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

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.recents.BakedBezierInterpolator;
import com.android.systemui.recents.Constants;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.model.Task;


/* A task view */
public class TaskView extends FrameLayout implements View.OnClickListener,
        Task.TaskCallbacks {
    /** The TaskView callbacks */
    interface TaskViewCallbacks {
        public void onTaskIconClicked(TaskView tv);
        public void onTaskInfoPanelShown(TaskView tv);
        public void onTaskInfoPanelHidden(TaskView tv);
        public void onTaskAppInfoClicked(TaskView tv);

        // public void onTaskViewReboundToTask(TaskView tv, Task t);
    }

    int mDim;
    int mMaxDim;
    TimeInterpolator mDimInterpolator = new AccelerateInterpolator();

    Task mTask;
    boolean mTaskDataLoaded;
    boolean mTaskInfoPaneVisible;
    Point mLastTouchDown = new Point();
    Path mRoundedRectClipPath = new Path();

    TaskThumbnailView mThumbnailView;
    TaskBarView mBarView;
    TaskInfoView mInfoView;
    TaskViewCallbacks mCb;


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
        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        RecentsConfiguration config = RecentsConfiguration.getInstance();
        mMaxDim = config.taskStackMaxDim;

        // Bind the views
        mThumbnailView = (TaskThumbnailView) findViewById(R.id.task_view_thumbnail);
        mBarView = (TaskBarView) findViewById(R.id.task_view_bar);
        mInfoView = (TaskInfoView) findViewById(R.id.task_view_info_pane);

        if (mTaskDataLoaded) {
            onTaskDataLoaded(false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Update the rounded rect clip path
        RecentsConfiguration config = RecentsConfiguration.getInstance();
        float radius = config.taskViewRoundedCornerRadiusPx;
        mRoundedRectClipPath.reset();
        mRoundedRectClipPath.addRoundRect(new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight()),
                radius, radius, Path.Direction.CW);

        // Update the outline
        Outline o = new Outline();
        o.setRoundRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), radius);
        setOutline(o);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mLastTouchDown.set((int) ev.getX(), (int) ev.getY());
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /** Set callback */
    void setCallbacks(TaskViewCallbacks cb) {
        mCb = cb;
    }

    /** Gets the task */
    Task getTask() {
        return mTask;
    }

    /** Synchronizes this view's properties with the task's transform */
    void updateViewPropertiesToTaskTransform(TaskViewTransform animateFromTransform,
                                             TaskViewTransform toTransform, int duration) {
        RecentsConfiguration config = RecentsConfiguration.getInstance();
        int minZ = config.taskViewTranslationZMinPx;
        int incZ = config.taskViewTranslationZIncrementPx;

        if (duration > 0) {
            if (animateFromTransform != null) {
                setTranslationY(animateFromTransform.translationY);
                if (Constants.DebugFlags.App.EnableShadows) {
                    setTranslationZ(Math.max(minZ, minZ + (animateFromTransform.t * incZ)));
                }
                setScaleX(animateFromTransform.scale);
                setScaleY(animateFromTransform.scale);
                setAlpha(animateFromTransform.alpha);
            }
            if (Constants.DebugFlags.App.EnableShadows) {
                animate().translationZ(Math.max(minZ, minZ + (toTransform.t * incZ)));
            }
            animate().translationY(toTransform.translationY)
                    .scaleX(toTransform.scale)
                    .scaleY(toTransform.scale)
                    .alpha(toTransform.alpha)
                    .setDuration(duration)
                    .setInterpolator(BakedBezierInterpolator.INSTANCE)
                    .withLayer()
                    .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            updateDimOverlayFromScale();
                        }
                    })
                    .start();
        } else {
            setTranslationY(toTransform.translationY);
            if (Constants.DebugFlags.App.EnableShadows) {
                setTranslationZ(Math.max(minZ, minZ + (toTransform.t * incZ)));
            }
            setScaleX(toTransform.scale);
            setScaleY(toTransform.scale);
            setAlpha(toTransform.alpha);
        }
        updateDimOverlayFromScale();
        invalidate();
    }

    /** Resets this view's properties */
    void resetViewProperties() {
        setTranslationX(0f);
        setTranslationY(0f);
        if (Constants.DebugFlags.App.EnableShadows) {
            setTranslationZ(0f);
        }
        setScaleX(1f);
        setScaleY(1f);
        setAlpha(1f);
        invalidate();
    }

    /**
     * When we are un/filtering, this method will set up the transform that we are animating to,
     * in order to hide the task.
     */
    void prepareTaskTransformForFilterTaskHidden(TaskViewTransform toTransform) {
        // Fade the view out and slide it away
        toTransform.alpha = 0f;
        toTransform.translationY += 200;
    }

    /**
     * When we are un/filtering, this method will setup the transform that we are animating from,
     * in order to show the task.
     */
    void prepareTaskTransformForFilterTaskVisible(TaskViewTransform fromTransform) {
        // Fade the view in
        fromTransform.alpha = 0f;
    }

    /** Animates this task view as it enters recents */
    public void animateOnEnterRecents() {
        RecentsConfiguration config = RecentsConfiguration.getInstance();
        mBarView.setAlpha(0f);
        mBarView.animate()
                .alpha(1f)
                .setStartDelay(235)
                .setInterpolator(BakedBezierInterpolator.INSTANCE)
                .setDuration(config.taskBarEnterAnimDuration)
                .withLayer()
                .start();
    }

    /** Animates this task view as it exits recents */
    public void animateOnLeavingRecents(final Runnable r) {
        RecentsConfiguration config = RecentsConfiguration.getInstance();
        mBarView.animate()
            .alpha(0f)
            .setStartDelay(0)
            .setInterpolator(BakedBezierInterpolator.INSTANCE)
            .setDuration(config.taskBarExitAnimDuration)
            .withLayer()
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    post(r);
                }
            })
            .start();
    }

    /** Returns the rect we want to clip (it may not be the full rect) */
    Rect getClippingRect(Rect outRect) {
        getHitRect(outRect);
        // XXX: We should get the hit rect of the thumbnail view and intersect, but this is faster
        outRect.right = outRect.left + mThumbnailView.getRight();
        outRect.bottom = outRect.top + mThumbnailView.getBottom();
        return outRect;
    }

    /** Returns whether this task has an info pane visible */
    boolean isInfoPaneVisible() {
        return mTaskInfoPaneVisible;
    }

    /** Shows the info pane if it is not visible. */
    void showInfoPane(Rect taskVisibleRect) {
        if (mTaskInfoPaneVisible) return;

        // Remove the bar view from the visible rect and update the info pane contents
        taskVisibleRect.top += mBarView.getMeasuredHeight();
        mInfoView.updateContents(taskVisibleRect);

        // Show the info pane and animate it into view
        mInfoView.setVisibility(View.VISIBLE);
        mInfoView.animateCircularClip(mLastTouchDown, 0f, 1f, null, true);
        mInfoView.setOnClickListener(this);
        mTaskInfoPaneVisible = true;

        // Notify any callbacks
        if (mCb != null) {
            mCb.onTaskInfoPanelShown(this);
        }
    }

    /** Hides the info pane if it is visible. */
    void hideInfoPane() {
        if (!mTaskInfoPaneVisible) return;
        RecentsConfiguration config = RecentsConfiguration.getInstance();

        // Cancel any circular clip animation
        mInfoView.cancelCircularClipAnimation();

        // Animate the info pane out
        mInfoView.animate()
                .alpha(0f)
                .setDuration(config.taskViewInfoPaneAnimDuration)
                .setInterpolator(BakedBezierInterpolator.INSTANCE)
                .withLayer()
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mInfoView.setVisibility(View.INVISIBLE);
                        mInfoView.setOnClickListener(null);

                        mInfoView.setAlpha(1f);
                    }
                })
                .start();
        mTaskInfoPaneVisible = false;

        // Notify any callbacks
        if (mCb != null) {
            mCb.onTaskInfoPanelHidden(this);
        }
    }

    /** Enable the hw layers on this task view */
    void enableHwLayers() {
        mThumbnailView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    /** Disable the hw layers on this task view */
    void disableHwLayers() {
        mThumbnailView.setLayerType(View.LAYER_TYPE_NONE, null);
    }

    /** Update the dim as a function of the scale of this view. */
    void updateDimOverlayFromScale() {
        float minScale = Constants.Values.TaskStackView.StackPeekMinScale;
        float scaleRange = 1f - minScale;
        float dim = (1f - getScaleX()) / scaleRange;
        dim = mDimInterpolator.getInterpolation(Math.min(dim, 1f));
        mDim = Math.max(0, Math.min(mMaxDim, (int) (dim * 255)));
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        // Apply the rounded rect clip path on the whole view
        canvas.clipPath(mRoundedRectClipPath);

        super.draw(canvas);

        // Apply the dim if necessary
        if (mDim > 0) {
            canvas.drawColor(mDim << 24);
        }
    }

    /**** TaskCallbacks Implementation ****/

    /** Binds this task view to the task */
    public void onTaskBound(Task t) {
        mTask = t;
        mTask.setCallbacks(this);
    }

    @Override
    public void onTaskDataLoaded(boolean reloadingTaskData) {
        if (mThumbnailView != null && mBarView != null && mInfoView != null) {
            // Bind each of the views to the new task data
            mThumbnailView.rebindToTask(mTask, reloadingTaskData);
            mBarView.rebindToTask(mTask, reloadingTaskData);
            mInfoView.rebindToTask(mTask, reloadingTaskData);
            // Rebind any listeners
            mBarView.mApplicationIcon.setOnClickListener(this);
            mInfoView.mAppInfoButton.setOnClickListener(this);
        }
        mTaskDataLoaded = true;
    }

    @Override
    public void onTaskDataUnloaded() {
        if (mThumbnailView != null && mBarView != null && mInfoView != null) {
            // Unbind each of the views from the task data and remove the task callback
            mTask.setCallbacks(null);
            mThumbnailView.unbindFromTask();
            mBarView.unbindFromTask();
            // Unbind any listeners
            mBarView.mApplicationIcon.setOnClickListener(null);
            mInfoView.mAppInfoButton.setOnClickListener(null);
        }
        mTaskDataLoaded = false;
    }

    @Override
    public void onClick(View v) {
        if (v == mInfoView) {
            hideInfoPane();
        } else if (v == mBarView.mApplicationIcon) {
            mCb.onTaskIconClicked(this);
        } else if (v == mInfoView.mAppInfoButton) {
            mCb.onTaskAppInfoClicked(this);
        }
    }
}