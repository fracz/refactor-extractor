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

import static android.app.ActivityManager.INVALID_STACK_ID;

import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.events.ui.DismissTaskEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/* The visual representation of a task stack view */
public class TaskStackView extends FrameLayout implements TaskStack.TaskStackCallbacks,
        TaskView.TaskViewCallbacks, TaskStackViewScroller.TaskStackViewScrollerCallbacks,
        ViewPool.ViewPoolConsumer<TaskView, Task> {

    /** The TaskView callbacks */
    interface TaskStackViewCallbacks {
        public void onTaskViewClicked(TaskStackView stackView, TaskView tv, TaskStack stack, Task t,
                boolean lockToTask, boolean boundsValid, Rect bounds, int destinationStack);
        public void onAllTaskViewsDismissed(ArrayList<Task> removedTasks);
        public void onTaskStackFilterTriggered();
        public void onTaskStackUnfilterTriggered();
    }
    RecentsConfiguration mConfig;

    TaskStack mStack;
    TaskStackViewLayoutAlgorithm mLayoutAlgorithm;
    TaskStackViewFilterAlgorithm mFilterAlgorithm;
    TaskStackViewScroller mStackScroller;
    TaskStackViewTouchHandler mTouchHandler;
    TaskStackViewCallbacks mCb;
    ViewPool<TaskView, Task> mViewPool;
    ArrayList<TaskViewTransform> mCurrentTaskTransforms = new ArrayList<>();
    DozeTrigger mUIDozeTrigger;
    int mFocusedTaskIndex = -1;
    int mPrevAccessibilityFocusedIndex = -1;
    // Optimizations
    int mStackViewsAnimationDuration;
    boolean mStackViewsDirty = true;
    boolean mStackViewsClipDirty = true;
    boolean mAwaitingFirstLayout = true;
    boolean mStartEnterAnimationRequestedAfterLayout;
    boolean mStartEnterAnimationCompleted;
    ViewAnimation.TaskViewEnterContext mStartEnterAnimationContext;
    Rect mTaskStackBounds = new Rect();
    int[] mTmpVisibleRange = new int[2];
    float[] mTmpCoord = new float[2];
    Matrix mTmpMatrix = new Matrix();
    Rect mTmpRect = new Rect();
    TaskViewTransform mTmpTransform = new TaskViewTransform();
    HashMap<Task, TaskView> mTmpTaskViewMap = new HashMap<>();
    ArrayList<TaskView> mTaskViews = new ArrayList<>();
    List<TaskView> mImmutableTaskViews = new ArrayList<>();
    LayoutInflater mInflater;
    boolean mLayersDisabled;

    // A convenience update listener to request updating clipping of tasks
    ValueAnimator.AnimatorUpdateListener mRequestUpdateClippingListener =
            new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            requestUpdateStackViewsClip();
        }
    };

    public TaskStackView(Context context, TaskStack stack) {
        super(context);
        // Set the stack first
        setStack(stack);
        mConfig = RecentsConfiguration.getInstance();
        mViewPool = new ViewPool<>(context, this);
        mInflater = LayoutInflater.from(context);
        mLayoutAlgorithm = new TaskStackViewLayoutAlgorithm(context, mConfig);
        mFilterAlgorithm = new TaskStackViewFilterAlgorithm(this, mViewPool);
        mStackScroller = new TaskStackViewScroller(context, mLayoutAlgorithm);
        mStackScroller.setCallbacks(this);
        mTouchHandler = new TaskStackViewTouchHandler(context, this, mStackScroller);

        int taskBarDismissDozeDelaySeconds = getResources().getInteger(
                R.integer.recents_task_bar_dismiss_delay_seconds);
        mUIDozeTrigger = new DozeTrigger(taskBarDismissDozeDelaySeconds, new Runnable() {
            @Override
            public void run() {
                // Show the task bar dismiss buttons
                List<TaskView> taskViews = getTaskViews();
                int taskViewCount = taskViews.size();
                for (int i = 0; i < taskViewCount; i++) {
                    TaskView tv = taskViews.get(i);
                    tv.startNoUserInteractionAnimation();
                }
            }
        });
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    /** Sets the callbacks */
    void setCallbacks(TaskStackViewCallbacks cb) {
        mCb = cb;
    }

    @Override
    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, RecentsActivity.EVENT_BUS_PRIORITY + 1);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    /** Sets the task stack */
    void setStack(TaskStack stack) {
        // Set the new stack
        mStack = stack;
        if (mStack != null) {
            mStack.setCallbacks(this);
        }
        // Layout again with the new stack
        requestLayout();
    }

    /** Returns the task stack. */
    TaskStack getStack() {
        return mStack;
    }

    /** Updates the list of task views */
    void updateTaskViewsList() {
        mTaskViews.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof TaskView) {
                mTaskViews.add((TaskView) v);
            }
        }
        mImmutableTaskViews = Collections.unmodifiableList(mTaskViews);
    }

    /** Gets the list of task views */
    List<TaskView> getTaskViews() {
        return mImmutableTaskViews;
    }

    /** Resets this TaskStackView for reuse. */
    void reset() {
        // Reset the focused task
        resetFocusedTask();

        // Return all the views to the pool
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = taskViewCount - 1; i >= 0; i--) {
            mViewPool.returnViewToPool(taskViews.get(i));
        }

        // Mark each task view for relayout
        if (mViewPool != null) {
            Iterator<TaskView> iter = mViewPool.poolViewIterator();
            if (iter != null) {
                while (iter.hasNext()) {
                    TaskView tv = iter.next();
                    tv.reset();
                }
            }
        }

        // Reset the stack state
        mStack.reset();
        mStackViewsDirty = true;
        mStackViewsClipDirty = true;
        mAwaitingFirstLayout = true;
        mPrevAccessibilityFocusedIndex = -1;
        if (mUIDozeTrigger != null) {
            mUIDozeTrigger.stopDozing();
            mUIDozeTrigger.resetTrigger();
        }
        mStackScroller.reset();
    }

    /** Requests that the views be synchronized with the model */
    void requestSynchronizeStackViewsWithModel() {
        requestSynchronizeStackViewsWithModel(0);
    }
    void requestSynchronizeStackViewsWithModel(int duration) {
        if (!mStackViewsDirty) {
            invalidate();
            mStackViewsDirty = true;
        }
        if (mAwaitingFirstLayout) {
            // Skip the animation if we are awaiting first layout
            mStackViewsAnimationDuration = 0;
        } else {
            mStackViewsAnimationDuration = Math.max(mStackViewsAnimationDuration, duration);
        }
    }

    /** Requests that the views clipping be updated. */
    void requestUpdateStackViewsClip() {
        if (!mStackViewsClipDirty) {
            invalidate();
            mStackViewsClipDirty = true;
        }
    }

    /** Finds the child view given a specific task. */
    public TaskView getChildViewForTask(Task t) {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            if (tv.getTask() == t) {
                return tv;
            }
        }
        return null;
    }

    /** Returns the stack algorithm for this task stack. */
    public TaskStackViewLayoutAlgorithm getStackAlgorithm() {
        return mLayoutAlgorithm;
    }

    /**
     * Gets the stack transforms of a list of tasks, and returns the visible range of tasks.
     */
    private boolean updateStackTransforms(ArrayList<TaskViewTransform> taskTransforms,
                                       ArrayList<Task> tasks,
                                       float stackScroll,
                                       int[] visibleRangeOut,
                                       boolean boundTranslationsToRect) {
        int taskTransformCount = taskTransforms.size();
        int taskCount = tasks.size();
        int frontMostVisibleIndex = -1;
        int backMostVisibleIndex = -1;

        // We can reuse the task transforms where possible to reduce object allocation
        if (taskTransformCount < taskCount) {
            // If there are less transforms than tasks, then add as many transforms as necessary
            for (int i = taskTransformCount; i < taskCount; i++) {
                taskTransforms.add(new TaskViewTransform());
            }
        } else if (taskTransformCount > taskCount) {
            // If there are more transforms than tasks, then just subset the transform list
            taskTransforms.subList(0, taskCount);
        }

        // Update the stack transforms
        TaskViewTransform prevTransform = null;
        for (int i = taskCount - 1; i >= 0; i--) {
            TaskViewTransform transform = mLayoutAlgorithm.getStackTransform(tasks.get(i),
                    stackScroll, taskTransforms.get(i), prevTransform);
            if (transform.visible) {
                if (frontMostVisibleIndex < 0) {
                    frontMostVisibleIndex = i;
                }
                backMostVisibleIndex = i;
            } else {
                if (backMostVisibleIndex != -1) {
                    // We've reached the end of the visible range, so going down the rest of the
                    // stack, we can just reset the transforms accordingly
                    while (i >= 0) {
                        taskTransforms.get(i).reset();
                        i--;
                    }
                    break;
                }
            }

            if (boundTranslationsToRect) {
                transform.translationY = Math.min(transform.translationY,
                        mLayoutAlgorithm.mStackRect.bottom);
            }
            prevTransform = transform;
        }
        if (visibleRangeOut != null) {
            visibleRangeOut[0] = frontMostVisibleIndex;
            visibleRangeOut[1] = backMostVisibleIndex;
        }
        return frontMostVisibleIndex != -1 && backMostVisibleIndex != -1;
    }

    /** Synchronizes the views with the model */
    boolean synchronizeStackViewsWithModel() {
        if (mStackViewsDirty) {
            RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
            SystemServicesProxy ssp = loader.getSystemServicesProxy();

            // Get all the task transforms
            ArrayList<Task> tasks = mStack.getTasks();
            float stackScroll = mStackScroller.getStackScroll();
            int[] visibleRange = mTmpVisibleRange;
            boolean isValidVisibleRange = updateStackTransforms(mCurrentTaskTransforms, tasks,
                    stackScroll, visibleRange, false);

            // Return all the invisible children to the pool
            mTmpTaskViewMap.clear();
            List<TaskView> taskViews = getTaskViews();
            int taskViewCount = taskViews.size();
            boolean reaquireAccessibilityFocus = false;
            for (int i = taskViewCount - 1; i >= 0; i--) {
                TaskView tv = taskViews.get(i);
                Task task = tv.getTask();
                int taskIndex = mStack.indexOfTask(task);
                if (visibleRange[1] <= taskIndex && taskIndex <= visibleRange[0]) {
                    mTmpTaskViewMap.put(task, tv);
                } else {
                    mViewPool.returnViewToPool(tv);
                    reaquireAccessibilityFocus |= (i == mPrevAccessibilityFocusedIndex);
                }
            }

            // Pick up all the newly visible children and update all the existing children
            for (int i = visibleRange[0]; isValidVisibleRange && i >= visibleRange[1]; i--) {
                Task task = tasks.get(i);
                TaskViewTransform transform = mCurrentTaskTransforms.get(i);
                TaskView tv = mTmpTaskViewMap.get(task);
                int taskIndex = mStack.indexOfTask(task);

                if (tv == null) {
                    tv = mViewPool.pickUpViewFromPool(task, task);
                    if (mLayersDisabled) {
                        tv.disableLayersForOneFrame();
                    }
                    if (mStackViewsAnimationDuration > 0) {
                        // For items in the list, put them in start animating them from the
                        // approriate ends of the list where they are expected to appear
                        if (Float.compare(transform.p, 0f) <= 0) {
                            mLayoutAlgorithm.getStackTransform(0f, 0f, mTmpTransform, null);
                        } else {
                            mLayoutAlgorithm.getStackTransform(1f, 0f, mTmpTransform, null);
                        }
                        tv.updateViewPropertiesToTaskTransform(mTmpTransform, 0);
                    }
                }

                // Animate the task into place
                tv.updateViewPropertiesToTaskTransform(mCurrentTaskTransforms.get(taskIndex),
                        mStackViewsAnimationDuration, mRequestUpdateClippingListener);

                // Request accessibility focus on the next view if we removed the task
                // that previously held accessibility focus
                if (reaquireAccessibilityFocus) {
                    taskViews = getTaskViews();
                    taskViewCount = taskViews.size();
                    if (taskViewCount > 0 && ssp.isTouchExplorationEnabled() &&
                            mPrevAccessibilityFocusedIndex != -1) {
                        TaskView atv = taskViews.get(taskViewCount - 1);
                        int indexOfTask = mStack.indexOfTask(atv.getTask());
                        if (mPrevAccessibilityFocusedIndex != indexOfTask) {
                            tv.requestAccessibilityFocus();
                            mPrevAccessibilityFocusedIndex = indexOfTask;
                        }
                    }
                }
            }

            // Reset the request-synchronize params
            mStackViewsAnimationDuration = 0;
            mStackViewsDirty = false;
            mStackViewsClipDirty = true;
            return true;
        }
        return false;
    }

    /** Updates the clip for each of the task views. */
    void clipTaskViews() {
        // Update the clip on each task child
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount - 1; i++) {
            TaskView tv = taskViews.get(i);
            TaskView nextTv = null;
            TaskView tmpTv = null;
            int clipBottom = 0;
            if (tv.shouldClipViewInStack()) {
                // Find the next view to clip against
                int nextIndex = i;
                while (nextIndex < (taskViewCount - 1)) {
                    tmpTv = taskViews.get(++nextIndex);
                    if (tmpTv != null && tmpTv.shouldClipViewInStack()) {
                        nextTv = tmpTv;
                        break;
                    }
                }

                // Clip against the next view, this is just an approximation since we are
                // stacked and we can make assumptions about the visibility of the this
                // task relative to the ones in front of it.
                if (nextTv != null) {
                    // Map the top edge of next task view into the local space of the current
                    // task view to find the clip amount in local space
                    mTmpCoord[0] = mTmpCoord[1] = 0;
                    Utilities.mapCoordInDescendentToSelf(nextTv, this, mTmpCoord, false);
                    Utilities.mapCoordInSelfToDescendent(tv, this, mTmpCoord, mTmpMatrix);
                    clipBottom = (int) Math.floor(tv.getMeasuredHeight() - mTmpCoord[1]
                            - nextTv.getPaddingTop() - 1);
                }
            }
            tv.getViewBounds().setClipBottom(clipBottom);
        }
        if (taskViewCount > 0) {
            // The front most task should never be clipped
            TaskView tv = taskViews.get(taskViewCount - 1);
            tv.getViewBounds().setClipBottom(0);
        }
        mStackViewsClipDirty = false;
    }

    /** Updates the min and max virtual scroll bounds */
    void updateMinMaxScroll(boolean boundScrollToNewMinMax) {
        // Compute the min and max scroll values
        mLayoutAlgorithm.computeMinMaxScroll(mStack.getTasks());

        // Debug logging
        if (boundScrollToNewMinMax) {
            mStackScroller.boundScroll();
        }
    }

    /** Returns the scroller. */
    public TaskStackViewScroller getScroller() {
        return mStackScroller;
    }

    /** Focuses the task at the specified index in the stack */
    void focusTask(int taskIndex, boolean scrollToNewPosition, final boolean animateFocusedState) {
        // Return early if the task is already focused
        if (taskIndex == mFocusedTaskIndex) return;

        if (0 <= taskIndex && taskIndex < mStack.getTaskCount()) {
            mFocusedTaskIndex = taskIndex;
            mPrevAccessibilityFocusedIndex = taskIndex;

            // Focus the view if possible, otherwise, focus the view after we scroll into position
            final Task t = mStack.getTasks().get(mFocusedTaskIndex);
            Runnable postScrollRunnable = new Runnable() {
                @Override
                public void run() {
                    TaskView tv = getChildViewForTask(t);
                    if (tv != null) {
                        tv.setFocusedTask(animateFocusedState);
                        tv.requestAccessibilityFocus();
                    }
                }
            };

            // Scroll the view into position (just center it in the curve)
            if (scrollToNewPosition) {
                float newScroll = mLayoutAlgorithm.getStackScrollForTask(t) - 0.5f;
                newScroll = mStackScroller.getBoundedStackScroll(newScroll);
                mStackScroller.animateScroll(mStackScroller.getStackScroll(), newScroll, postScrollRunnable);
            } else {
                if (postScrollRunnable != null) {
                    postScrollRunnable.run();
                }
            }

        }
    }

    /**
     * Ensures that there is a task focused, if nothing is focused, then we will use the task
     * at the center of the visible stack.
     */
    public boolean ensureFocusedTask(boolean findClosestToCenter) {
        if (mFocusedTaskIndex < 0) {
            List<TaskView> taskViews = getTaskViews();
            int taskViewCount = taskViews.size();
            if (findClosestToCenter) {
                // If there is no task focused, then find the task that is closes to the center
                // of the screen and use that as the currently focused task
                int x = mLayoutAlgorithm.mStackRect.centerX();
                int y = mLayoutAlgorithm.mStackRect.centerY();
                for (int i = taskViewCount - 1; i >= 0; i--) {
                    TaskView tv = taskViews.get(i);
                    tv.getHitRect(mTmpRect);
                    if (mTmpRect.contains(x, y)) {
                        mFocusedTaskIndex = mStack.indexOfTask(tv.getTask());
                        mPrevAccessibilityFocusedIndex = mFocusedTaskIndex;
                        break;
                    }
                }
            }
            // If we can't find the center task, then use the front most index
            if (mFocusedTaskIndex < 0 && taskViewCount > 0) {
                TaskView tv = taskViews.get(taskViewCount - 1);
                mFocusedTaskIndex = mStack.indexOfTask(tv.getTask());
                mPrevAccessibilityFocusedIndex = mFocusedTaskIndex;
            }
        }
        return mFocusedTaskIndex >= 0;
    }

    /**
     * Focuses the next task in the stack.
     * @param animateFocusedState determines whether to actually draw the highlight along with
     *                            the change in focus, as well as whether to scroll to fit the
     *                            task into view.
     */
    public void focusNextTask(boolean forward, boolean animateFocusedState) {
        // Find the next index to focus
        int numTasks = mStack.getTaskCount();
        if (numTasks == 0) return;

        int direction = (forward ? -1 : 1);
        int newIndex = mFocusedTaskIndex + direction;
        if (newIndex >= 0 && newIndex <= (numTasks - 1)) {
            newIndex = Math.max(0, Math.min(numTasks - 1, newIndex));
            focusTask(newIndex, true, animateFocusedState);
        }
    }

    /** Dismisses the focused task. */
    public void dismissFocusedTask() {
        // Return early if the focused task index is invalid
        if (mFocusedTaskIndex < 0 || mFocusedTaskIndex >= mStack.getTaskCount()) {
            mFocusedTaskIndex = -1;
            return;
        }

        Task t = mStack.getTasks().get(mFocusedTaskIndex);
        TaskView tv = getChildViewForTask(t);
        tv.dismissTask();
    }

    /** Resets the focused task. */
    void resetFocusedTask() {
        if ((0 <= mFocusedTaskIndex) && (mFocusedTaskIndex < mStack.getTaskCount())) {
            Task t = mStack.getTasks().get(mFocusedTaskIndex);
            TaskView tv = getChildViewForTask(t);
            if (tv != null) {
                tv.unsetFocusedTask();
            }
        }
        mFocusedTaskIndex = -1;
        mPrevAccessibilityFocusedIndex = -1;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        if (taskViewCount > 0) {
            TaskView backMostTask = taskViews.get(0);
            TaskView frontMostTask = taskViews.get(taskViewCount - 1);
            event.setFromIndex(mStack.indexOfTask(backMostTask.getTask()));
            event.setToIndex(mStack.indexOfTask(frontMostTask.getTask()));
            event.setContentDescription(frontMostTask.getTask().activityLabel);
        }
        event.setItemCount(mStack.getTaskCount());
        event.setScrollY(mStackScroller.mScroller.getCurrY());
        event.setMaxScrollY(mStackScroller.progressToScrollRange(mLayoutAlgorithm.mMaxScrollP));
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        if (taskViewCount > 1 && mPrevAccessibilityFocusedIndex != -1) {
            info.setScrollable(true);
            if (mPrevAccessibilityFocusedIndex > 0) {
                info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            if (mPrevAccessibilityFocusedIndex < mStack.getTaskCount() - 1) {
                info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
        }
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return TaskStackView.class.getName();
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }
        if (ensureFocusedTask(false)) {
            switch (action) {
                case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
                    if (mPrevAccessibilityFocusedIndex > 0) {
                        focusNextTask(true, false);
                        return true;
                    }
                }
                break;
                case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                    if (mPrevAccessibilityFocusedIndex < mStack.getTaskCount() - 1) {
                        focusNextTask(false, false);
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mTouchHandler.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mTouchHandler.onTouchEvent(ev);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent ev) {
        return mTouchHandler.onGenericMotionEvent(ev);
    }

    @Override
    public void computeScroll() {
        mStackScroller.computeScroll();
        // Synchronize the views
        synchronizeStackViewsWithModel();
        clipTaskViews();
        // Notify accessibility
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED);
    }

    /** Computes the stack and task rects */
    public void computeRects(int windowWidth, int windowHeight, Rect taskStackBounds,
            boolean launchedWithAltTab, boolean launchedFromHome) {
        // Compute the rects in the stack algorithm
        mLayoutAlgorithm.computeRects(windowWidth, windowHeight, taskStackBounds);

        // Update the scroll bounds
        updateMinMaxScroll(false);
    }

    /**
     * This is ONLY used from AlternateRecentsComponent to update the dummy stack view for purposes
     * of getting the task rect to animate to.
     */
    public void updateMinMaxScrollForStack(TaskStack stack) {
        mStack = stack;
        updateMinMaxScroll(false);
    }

    /**
     * Computes the maximum number of visible tasks and thumbnails.  Requires that
     * updateMinMaxScrollForStack() is called first.
     */
    public TaskStackViewLayoutAlgorithm.VisibilityReport computeStackVisibilityReport() {
        return mLayoutAlgorithm.computeStackVisibilityReport(mStack.getTasks());
    }

    public void setTaskStackBounds(Rect taskStackBounds, Rect systemInsets) {
        mTaskStackBounds.set(taskStackBounds);
        mLayoutAlgorithm.setSystemInsets(systemInsets);
    }

    /**
     * This is called with the full window width and height to allow stack view children to
     * perform the full screen transition down.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Compute our stack/task rects
        RecentsActivityLaunchState launchState = mConfig.getLaunchState();
        computeRects(width, height, mTaskStackBounds, launchState.launchedWithAltTab,
                launchState.launchedFromHome);

        // If this is the first layout, then scroll to the front of the stack and synchronize the
        // stack views immediately to load all the views
        if (mAwaitingFirstLayout) {
            mStackScroller.setStackScrollToInitialState();
            requestSynchronizeStackViewsWithModel();
            synchronizeStackViewsWithModel();
        }

        // Measure each of the TaskViews
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            if (tv.getBackground() != null) {
                tv.getBackground().getPadding(mTmpRect);
            } else {
                mTmpRect.setEmpty();
            }
            tv.measure(
                MeasureSpec.makeMeasureSpec(
                        mLayoutAlgorithm.mTaskRect.width() + mTmpRect.left + mTmpRect.right,
                        MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(
                        mLayoutAlgorithm.mTaskRect.height() + mTmpRect.top + mTmpRect.bottom,
                        MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(width, height);
    }

    /**
     * This is called with the size of the space not including the top or right insets, or the
     * search bar height in portrait (but including the search bar width in landscape, since we want
     * to draw under it.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Layout each of the children
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            if (tv.getBackground() != null) {
                tv.getBackground().getPadding(mTmpRect);
            } else {
                mTmpRect.setEmpty();
            }
            tv.layout(mLayoutAlgorithm.mTaskRect.left - mTmpRect.left,
                    mLayoutAlgorithm.mTaskRect.top - mTmpRect.top,
                    mLayoutAlgorithm.mTaskRect.right + mTmpRect.right,
                    mLayoutAlgorithm.mTaskRect.bottom + mTmpRect.bottom);
        }

        if (mAwaitingFirstLayout) {
            mAwaitingFirstLayout = false;
            onFirstLayout();
        }
    }

    /** Handler for the first layout. */
    void onFirstLayout() {
        int offscreenY = mLayoutAlgorithm.mStackRect.bottom;

        // Find the launch target task
        Task launchTargetTask = null;
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = taskViewCount - 1; i >= 0; i--) {
            TaskView tv = taskViews.get(i);
            Task task = tv.getTask();
            if (task.isLaunchTarget) {
                launchTargetTask = task;
                break;
            }
        }

        // Prepare the first view for its enter animation
        for (int i = taskViewCount - 1; i >= 0; i--) {
            TaskView tv = taskViews.get(i);
            Task task = tv.getTask();
            boolean occludesLaunchTarget = (launchTargetTask != null) &&
                    launchTargetTask.group.isTaskAboveTask(task, launchTargetTask);
            tv.prepareEnterRecentsAnimation(task.isLaunchTarget, occludesLaunchTarget, offscreenY);
        }

        // If the enter animation started already and we haven't completed a layout yet, do the
        // enter animation now
        if (mStartEnterAnimationRequestedAfterLayout) {
            startEnterRecentsAnimation(mStartEnterAnimationContext);
            mStartEnterAnimationRequestedAfterLayout = false;
            mStartEnterAnimationContext = null;
        }

        // When Alt-Tabbing, focus the previous task (but leave the animation until we finish the
        // enter animation).
        RecentsActivityLaunchState launchState = mConfig.getLaunchState();
        if (launchState.launchedWithAltTab) {
            if (launchState.launchedFromAppWithThumbnail) {
                focusTask(Math.max(0, mStack.getTaskCount() - 2), false,
                        launchState.launchedHasConfigurationChanged);
            } else {
                focusTask(Math.max(0, mStack.getTaskCount() - 1), false,
                        launchState.launchedHasConfigurationChanged);
            }
        }

        // Start dozing
        mUIDozeTrigger.startDozing();
    }

    /** Requests this task stacks to start it's enter-recents animation */
    public void startEnterRecentsAnimation(ViewAnimation.TaskViewEnterContext ctx) {
        // If we are still waiting to layout, then just defer until then
        if (mAwaitingFirstLayout) {
            mStartEnterAnimationRequestedAfterLayout = true;
            mStartEnterAnimationContext = ctx;
            return;
        }

        if (mStack.getTaskCount() > 0) {
            // Find the launch target task
            Task launchTargetTask = null;
            List<TaskView> taskViews = getTaskViews();
            int taskViewCount = taskViews.size();
            for (int i = taskViewCount - 1; i >= 0; i--) {
                TaskView tv = taskViews.get(i);
                Task task = tv.getTask();
                if (task.isLaunchTarget) {
                    launchTargetTask = task;
                    break;
                }
            }

            // Animate all the task views into view
            for (int i = taskViewCount - 1; i >= 0; i--) {
                TaskView tv = taskViews.get(i);
                Task task = tv.getTask();
                ctx.currentTaskTransform = new TaskViewTransform();
                ctx.currentStackViewIndex = i;
                ctx.currentStackViewCount = taskViewCount;
                ctx.currentTaskRect = mLayoutAlgorithm.mTaskRect;
                ctx.currentTaskOccludesLaunchTarget = (launchTargetTask != null) &&
                        launchTargetTask.group.isTaskAboveTask(task, launchTargetTask);
                ctx.updateListener = mRequestUpdateClippingListener;
                mLayoutAlgorithm.getStackTransform(task, mStackScroller.getStackScroll(), ctx.currentTaskTransform, null);
                tv.startEnterRecentsAnimation(ctx);
            }

            // Add a runnable to the post animation ref counter to clear all the views
            ctx.postAnimationTrigger.addLastDecrementRunnable(new Runnable() {
                @Override
                public void run() {
                    mStartEnterAnimationCompleted = true;
                    // Poke the dozer to restart the trigger after the animation completes
                    mUIDozeTrigger.poke();

                    RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
                    SystemServicesProxy ssp = loader.getSystemServicesProxy();
                    List<TaskView> taskViews = getTaskViews();
                    int taskViewCount = taskViews.size();
                    if (taskViewCount > 0) {
                        // Focus the first view if accessibility is enabled
                        if (ssp.isTouchExplorationEnabled()) {
                            TaskView tv = taskViews.get(taskViewCount - 1);
                            tv.requestAccessibilityFocus();
                            mPrevAccessibilityFocusedIndex = mStack.indexOfTask(tv.getTask());
                        }
                    }

                    // Start the focus animation when alt-tabbing
                    ArrayList<Task> tasks = mStack.getTasks();
                    RecentsActivityLaunchState launchState = mConfig.getLaunchState();
                    if (launchState.launchedWithAltTab &&
                            !launchState.launchedHasConfigurationChanged &&
                            0 <= mFocusedTaskIndex && mFocusedTaskIndex < tasks.size()) {
                        TaskView tv = getChildViewForTask(tasks.get(mFocusedTaskIndex));
                        if (tv != null) {
                            tv.setFocusedTask(true);
                        }
                    }
                }
            });
        }
    }

    /** Requests this task stack to start it's exit-recents animation. */
    public void startExitToHomeAnimation(ViewAnimation.TaskViewExitContext ctx) {
        // Stop any scrolling
        mStackScroller.stopScroller();
        mStackScroller.stopBoundScrollAnimation();
        // Animate all the task views out of view
        ctx.offscreenTranslationY = mLayoutAlgorithm.mStackRect.bottom;

        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            tv.startExitToHomeAnimation(ctx);
        }
    }

    /** Requests this task stack to start it's dismiss-all animation. */
    public void startDismissAllAnimation(final Runnable postAnimationRunnable) {
        // Clear the focused task
        resetFocusedTask();
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        int count = 0;
        for (int i = taskViewCount - 1; i >= 0; i--) {
            TaskView tv = taskViews.get(i);
            tv.startDeleteTaskAnimation(i > 0 ? null : postAnimationRunnable, count * 50);
            count++;
        }
    }

    /** Animates a task view in this stack as it launches. */
    public void startLaunchTaskAnimation(TaskView tv, Runnable r, boolean lockToTask) {
        Task launchTargetTask = tv.getTask();
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView t = taskViews.get(i);
            if (t == tv) {
                t.setClipViewInStack(false);
                t.startLaunchTaskAnimation(r, true, true, lockToTask);
            } else {
                boolean occludesLaunchTarget = launchTargetTask.group.isTaskAboveTask(t.getTask(),
                        launchTargetTask);
                t.startLaunchTaskAnimation(null, false, occludesLaunchTarget, lockToTask);
            }
        }
    }

    /** Final callback after Recents is finally hidden. */
    void onRecentsHidden() {
        reset();
    }

    public boolean isTransformedTouchPointInView(float x, float y, View child) {
        return isTransformedTouchPointInView(x, y, child, null);
    }

    /** Pokes the dozer on user interaction. */
    void onUserInteraction() {
        // Poke the doze trigger if it is dozing
        mUIDozeTrigger.poke();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mLayersDisabled = false;
        super.dispatchDraw(canvas);
    }

    public void disableLayersForOneFrame() {
        mLayersDisabled = true;
        List<TaskView> taskViews = getTaskViews();
        for (int i = 0; i < taskViews.size(); i++) {
            taskViews.get(i).disableLayersForOneFrame();
        }
    }

    /**** TaskStackCallbacks Implementation ****/

    @Override
    public void onStackTaskAdded(TaskStack stack, Task t) {
        requestSynchronizeStackViewsWithModel();
    }

    @Override
    public void onStackTaskRemoved(TaskStack stack, Task removedTask, boolean wasFrontMostTask,
            Task newFrontMostTask) {
        // Remove the view associated with this task, we can't rely on updateTransforms
        // to work here because the task is no longer in the list
        TaskView tv = getChildViewForTask(removedTask);
        if (tv != null) {
            mViewPool.returnViewToPool(tv);
        }

        // Get the stack scroll of the task to anchor to (since we are removing something, the front
        // most task will be our anchor task)
        Task anchorTask = null;
        float prevAnchorTaskScroll = 0;
        boolean pullStackForward = stack.getTaskCount() > 0;
        if (pullStackForward) {
            anchorTask = mStack.getFrontMostTask();
            prevAnchorTaskScroll = mLayoutAlgorithm.getStackScrollForTask(anchorTask);
        }

        // Update the min/max scroll and animate other task views into their new positions
        updateMinMaxScroll(true);

        if (wasFrontMostTask) {
            // Since the max scroll progress is offset from the bottom of the stack, just scroll
            // to ensure that the new front most task is now fully visible
            mStackScroller.setStackScroll(mLayoutAlgorithm.mMaxScrollP);
        } else if (pullStackForward) {
            // Otherwise, offset the scroll by half the movement of the anchor task to allow the
            // tasks behind the removed task to move forward, and the tasks in front to move back
            float anchorTaskScroll = mLayoutAlgorithm.getStackScrollForTask(anchorTask);
            mStackScroller.setStackScroll(mStackScroller.getStackScroll() + (anchorTaskScroll
                    - prevAnchorTaskScroll) / 2);
            mStackScroller.boundScroll();
        }

        // Animate all the tasks into place
        requestSynchronizeStackViewsWithModel(200);

        // Update the new front most task
        if (newFrontMostTask != null) {
            TaskView frontTv = getChildViewForTask(newFrontMostTask);
            if (frontTv != null) {
                frontTv.onTaskBound(newFrontMostTask);
                frontTv.fadeInActionButton(0, getResources().getInteger(
                        R.integer.recents_task_enter_from_app_duration));
            }
        }

        // If there are no remaining tasks, then either unfilter the current stack, or just close
        // the activity if there are no filtered stacks
        if (mStack.getTaskCount() == 0) {
            boolean shouldFinishActivity = true;
            if (mStack.hasFilteredTasks()) {
                mStack.unfilterTasks();
                shouldFinishActivity = (mStack.getTaskCount() == 0);
            }
            if (shouldFinishActivity) {
                mCb.onAllTaskViewsDismissed(null);
            }
        }
    }

    @Override
    public void onStackAllTasksRemoved(TaskStack stack, final ArrayList<Task> removedTasks) {
        // Announce for accessibility
        String msg = getContext().getString(R.string.accessibility_recents_all_items_dismissed);
        announceForAccessibility(msg);

        startDismissAllAnimation(new Runnable() {
            @Override
            public void run() {
                // Notify that all tasks have been removed
                mCb.onAllTaskViewsDismissed(removedTasks);
            }
        });
    }

    @Override
    public void onStackFiltered(TaskStack newStack, final ArrayList<Task> curTasks,
                                Task filteredTask) {
        /*
        // Stash the scroll and filtered task for us to restore to when we unfilter
        mStashedScroll = getStackScroll();

        // Calculate the current task transforms
        ArrayList<TaskViewTransform> curTaskTransforms =
                getStackTransforms(curTasks, getStackScroll(), null, true);

        // Update the task offsets
        mLayoutAlgorithm.updateTaskOffsets(mStack.getTasks());

        // Scroll the item to the top of the stack (sans-peek) rect so that we can see it better
        updateMinMaxScroll(false);
        float overlapHeight = mLayoutAlgorithm.getTaskOverlapHeight();
        setStackScrollRaw((int) (newStack.indexOfTask(filteredTask) * overlapHeight));
        boundScrollRaw();

        // Compute the transforms of the items in the new stack after setting the new scroll
        final ArrayList<Task> tasks = mStack.getTasks();
        final ArrayList<TaskViewTransform> taskTransforms =
                getStackTransforms(mStack.getTasks(), getStackScroll(), null, true);

        // Animate
        mFilterAlgorithm.startFilteringAnimation(curTasks, curTaskTransforms, tasks, taskTransforms);

        // Notify any callbacks
        mCb.onTaskStackFilterTriggered();
        */
    }

    @Override
    public void onStackUnfiltered(TaskStack newStack, final ArrayList<Task> curTasks) {
        /*
        // Calculate the current task transforms
        final ArrayList<TaskViewTransform> curTaskTransforms =
                getStackTransforms(curTasks, getStackScroll(), null, true);

        // Update the task offsets
        mLayoutAlgorithm.updateTaskOffsets(mStack.getTasks());

        // Restore the stashed scroll
        updateMinMaxScroll(false);
        setStackScrollRaw(mStashedScroll);
        boundScrollRaw();

        // Compute the transforms of the items in the new stack after restoring the stashed scroll
        final ArrayList<Task> tasks = mStack.getTasks();
        final ArrayList<TaskViewTransform> taskTransforms =
                getStackTransforms(tasks, getStackScroll(), null, true);

        // Animate
        mFilterAlgorithm.startFilteringAnimation(curTasks, curTaskTransforms, tasks, taskTransforms);

        // Clear the saved vars
        mStashedScroll = 0;

        // Notify any callbacks
        mCb.onTaskStackUnfilterTriggered();
        */
    }

    /**** ViewPoolConsumer Implementation ****/

    @Override
    public TaskView createView(Context context) {
        return (TaskView) mInflater.inflate(R.layout.recents_task_view, this, false);
    }

    @Override
    public void prepareViewToEnterPool(TaskView tv) {
        Task task = tv.getTask();

        // Clear the accessibility focus for that view
        if (tv.isAccessibilityFocused()) {
            tv.clearAccessibilityFocus();
        }

        // Report that this tasks's data is no longer being used
        RecentsTaskLoader.getInstance().unloadTaskData(task);

        // Detach the view from the hierarchy
        detachViewFromParent(tv);
        // Update the task views list after removing the task view
        updateTaskViewsList();

        // Reset the view properties
        tv.resetViewProperties();

        // Reset the clip state of the task view
        tv.setClipViewInStack(false);
    }

    @Override
    public void prepareViewToLeavePool(TaskView tv, Task task, boolean isNewView) {
        // It is possible for a view to be returned to the view pool before it is laid out,
        // which means that we will need to relayout the view when it is first used next.
        boolean requiresRelayout = tv.getWidth() <= 0 && !isNewView;

        // Rebind the task and request that this task's data be filled into the TaskView
        tv.onTaskBound(task);

        // Load the task data
        RecentsTaskLoader.getInstance().loadTaskData(task);

        // If the doze trigger has already fired, then update the state for this task view
        tv.setNoUserInteractionState();

        // If we've finished the start animation, then ensure we always enable the focus animations
        if (mStartEnterAnimationCompleted) {
            tv.enableFocusAnimations();
        }

        // Find the index where this task should be placed in the stack
        int insertIndex = -1;
        int taskIndex = mStack.indexOfTask(task);
        if (taskIndex != -1) {

            List<TaskView> taskViews = getTaskViews();
            int taskViewCount = taskViews.size();
            for (int i = 0; i < taskViewCount; i++) {
                Task tvTask = taskViews.get(i).getTask();
                if (taskIndex < mStack.indexOfTask(tvTask)) {
                    insertIndex = i;
                    break;
                }
            }
        }

        // Add/attach the view to the hierarchy
        if (isNewView) {
            addView(tv, insertIndex);
        } else {
            attachViewToParent(tv, insertIndex, tv.getLayoutParams());
            if (requiresRelayout) {
                tv.requestLayout();
            }
        }
        // Update the task views list after adding the new task view
        updateTaskViewsList();

        // Set the new state for this view, including the callbacks and view clipping
        tv.setCallbacks(this);
        tv.setTouchEnabled(true);
        tv.setClipViewInStack(true);
    }

    @Override
    public boolean hasPreferredData(TaskView tv, Task preferredData) {
        return (tv.getTask() == preferredData);
    }

    /**** TaskViewCallbacks Implementation ****/

    @Override
    public void onTaskViewClicked(TaskView tv, Task task, boolean lockToTask) {
        // Cancel any doze triggers
        mUIDozeTrigger.stopDozing();

        if (mCb != null) {
            mCb.onTaskViewClicked(this, tv, mStack, task, lockToTask, false, null,
                    INVALID_STACK_ID);
        }
    }

    @Override
    public void onTaskViewClipStateChanged(TaskView tv) {
        if (!mStackViewsDirty) {
            invalidate();
        }
    }

    @Override
    public void onTaskViewFocusChanged(TaskView tv, boolean focused) {
        if (focused) {
            mFocusedTaskIndex = mStack.indexOfTask(tv.getTask());
        }
    }

    /**** TaskStackViewScroller.TaskStackViewScrollerCallbacks ****/

    @Override
    public void onScrollChanged(float p) {
        mUIDozeTrigger.poke();
        requestSynchronizeStackViewsWithModel();
        postInvalidateOnAnimation();
    }

    /**** EventBus Events ****/

    public final void onBusEvent(PackagesChangedEvent event) {
        // Compute which components need to be removed
        HashSet<ComponentName> removedComponents = event.monitor.computeComponentsRemoved(
                mStack.getTaskKeys(), event.packageName, event.userId);

        // For other tasks, just remove them directly if they no longer exist
        ArrayList<Task> tasks = mStack.getTasks();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            final Task t = tasks.get(i);
            if (removedComponents.contains(t.key.baseIntent.getComponent())) {
                TaskView tv = getChildViewForTask(t);
                if (tv != null) {
                    // For visible children, defer removing the task until after the animation
                    tv.startDeleteTaskAnimation(new Runnable() {
                        @Override
                        public void run() {
                            mStack.removeTask(t);
                        }
                    }, 0);
                } else {
                    // Otherwise, remove the task from the stack immediately
                    mStack.removeTask(t);
                }
            }
        }
    }

    public final void onBusEvent(DismissTaskEvent event) {
        TaskView tv = event.taskView;
        Task task = tv.getTask();
        int taskIndex = mStack.indexOfTask(task);
        boolean taskWasFocused = tv.isFocusedTask();

        // Announce for accessibility
        tv.announceForAccessibility(getContext().getString(R.string.accessibility_recents_item_dismissed,
                tv.getTask().activityLabel));

        // Remove the task from the view
        mStack.removeTask(task);

        // If the dismissed task was focused, then we should focus the new task in the same index
        if (taskWasFocused) {
            ArrayList<Task> tasks = mStack.getTasks();
            int nextTaskIndex = Math.min(tasks.size() - 1, taskIndex - 1);
            if (nextTaskIndex >= 0) {
                Task nextTask = tasks.get(nextTaskIndex);
                TaskView nextTv = getChildViewForTask(nextTask);
                if (nextTv != null) {
                    // Focus the next task, and only animate the visible state if we are launched
                    // from Alt-Tab
                    RecentsActivityLaunchState launchState = mConfig.getLaunchState();
                    nextTv.setFocusedTask(launchState.launchedWithAltTab);
                }
            }
        }
    }
}