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

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.ui.DismissTaskViewEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartInitializeDropTargetsEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.FULLSCREEN_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.INVALID_STACK_ID;


/* The visual representation of a task stack view */
public class TaskStackView extends FrameLayout implements TaskStack.TaskStackCallbacks,
        TaskView.TaskViewCallbacks, TaskStackViewScroller.TaskStackViewScrollerCallbacks,
        ViewPool.ViewPoolConsumer<TaskView, Task> {

    private final static String TAG = "TaskStackView";
    private final static boolean DEBUG = false;

    /** The TaskView callbacks */
    interface TaskStackViewCallbacks {
        public void onTaskViewClicked(TaskStackView stackView, TaskView tv, TaskStack stack, Task t,
                boolean lockToTask, Rect bounds, int destinationStack);
        public void onAllTaskViewsDismissed(ArrayList<Task> removedTasks);
        public void onTaskStackFilterTriggered();
        public void onTaskStackUnfilterTriggered();
    }

    TaskStack mStack;
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    TaskStackViewFilterAlgorithm mFilterAlgorithm;
    TaskStackViewScroller mStackScroller;
    TaskStackViewTouchHandler mTouchHandler;
    TaskStackViewCallbacks mCb;
    ColorDrawable mFreeformWorkspaceBackground;
    ViewPool<TaskView, Task> mViewPool;
    ArrayList<TaskViewTransform> mCurrentTaskTransforms = new ArrayList<>();
    DozeTrigger mUIDozeTrigger;
    int mFocusedTaskIndex = -1;
    // Optimizations
    int mStackViewsAnimationDuration;
    boolean mStackViewsDirty = true;
    boolean mStackViewsClipDirty = true;
    boolean mAwaitingFirstLayout = true;
    boolean mStartEnterAnimationRequestedAfterLayout;
    ViewAnimation.TaskViewEnterContext mStartEnterAnimationContext;

    Rect mTaskStackBounds = new Rect();
    int[] mTmpVisibleRange = new int[2];
    Rect mTmpRect = new Rect();
    RectF mTmpTaskRect = new RectF();
    TaskViewTransform mTmpTransform = new TaskViewTransform();
    TaskViewTransform mTmpStackBackTransform = new TaskViewTransform();
    TaskViewTransform mTmpStackFrontTransform = new TaskViewTransform();
    HashMap<Task, TaskView> mTmpTaskViewMap = new HashMap<>();
    ArrayList<TaskView> mTaskViews = new ArrayList<>();
    List<TaskView> mImmutableTaskViews = new ArrayList<>();
    List<TaskView> mTmpTaskViews = new ArrayList<>();
    LayoutInflater mInflater;
    boolean mLayersDisabled;
    boolean mTouchExplorationEnabled;

    Interpolator mFastOutSlowInInterpolator;

    // A convenience update listener to request updating clipping of tasks
    private ValueAnimator.AnimatorUpdateListener mRequestUpdateClippingListener =
            new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            requestUpdateStackViewsClip();
        }
    };

    // The drop targets for a task drag
    private DropTarget mFreeformWorkspaceDropTarget = new DropTarget() {
        @Override
        public boolean acceptsDrop(int x, int y, int width, int height) {
            return mLayoutAlgorithm.mFreeformRect.contains(x, y);
        }
    };

    private DropTarget mStackDropTarget = new DropTarget() {
        @Override
        public boolean acceptsDrop(int x, int y, int width, int height) {
            return mLayoutAlgorithm.mCurrentStackRect.contains(x, y);
        }
    };

    public TaskStackView(Context context, TaskStack stack) {
        super(context);
        // Set the stack first
        setStack(stack);
        mViewPool = new ViewPool<>(context, this);
        mInflater = LayoutInflater.from(context);
        mLayoutAlgorithm = new TaskStackLayoutAlgorithm(context);
        mFilterAlgorithm = new TaskStackViewFilterAlgorithm(this, mViewPool);
        mStackScroller = new TaskStackViewScroller(context, mLayoutAlgorithm);
        mStackScroller.setCallbacks(this);
        mTouchHandler = new TaskStackViewTouchHandler(context, this, mStackScroller);
        mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.fast_out_slow_in);

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

        mFreeformWorkspaceBackground = new ColorDrawable(0x33000000);
    }

    /** Sets the callbacks */
    void setCallbacks(TaskStackViewCallbacks cb) {
        mCb = cb;
    }

    @Override
    protected void onAttachedToWindow() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        mTouchExplorationEnabled = ssp.isTouchExplorationEnabled();
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

    /**
     * Returns the front most task view.
     *
     * @param stackTasksOnly if set, will return the front most task view in the stack (by default
     *                       the front most task view will be freeform since they are placed above
     *                       stack tasks)
     */
    private TaskView getFrontMostTaskView(boolean stackTasksOnly) {
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();
        for (int i = taskViewCount - 1; i >= 0; i--) {
            TaskView tv = taskViews.get(i);
            Task task = tv.getTask();
            if (stackTasksOnly && task.isFreeformTask()) {
                continue;
            }
            return tv;
        }
        return null;
    }

    /**
     * Finds the child view given a specific {@param task}.
     */
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
        List<TaskView> poolViews = mViewPool.getViews();
        for (TaskView tv : poolViews) {
            tv.reset();
        }

        // Reset the stack state
        mStack.reset();
        mStackViewsDirty = true;
        mStackViewsClipDirty = true;
        mAwaitingFirstLayout = true;
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

    /** Returns the stack algorithm for this task stack. */
    public TaskStackLayoutAlgorithm getStackAlgorithm() {
        return mLayoutAlgorithm;
    }

    /**
     * Gets the stack transforms of a list of tasks, and returns the visible range of tasks.
     * This call ignores freeform tasks.
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
        TaskViewTransform frontTransform = null;
        for (int i = taskCount - 1; i >= 0; i--) {
            Task task = tasks.get(i);
            if (task.isFreeformTask()) {
                continue;
            }

            TaskViewTransform transform = mLayoutAlgorithm.getStackTransform(task, stackScroll,
                    taskTransforms.get(i), frontTransform);
            if (DEBUG) {
                Log.d(TAG, "updateStackTransform: " + i + ", " + transform.visible);
            }
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
                        mLayoutAlgorithm.mCurrentStackRect.bottom);
            }
            frontTransform = transform;
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
            // Get all the task transforms
            ArrayList<Task> tasks = mStack.getTasks();
            float stackScroll = mStackScroller.getStackScroll();
            int[] visibleStackRange = mTmpVisibleRange;
            boolean isValidVisibleStackRange = updateStackTransforms(mCurrentTaskTransforms, tasks,
                    stackScroll, visibleStackRange, false);
            boolean hasStackBackTransform = false;
            boolean hasStackFrontTransform = false;
            if (DEBUG) {
                Log.d(TAG, "visibleRange: " + visibleStackRange[0] + " to " + visibleStackRange[1]);
            }

            // Return all the invisible children to the pool
            mTmpTaskViewMap.clear();
            List<TaskView> taskViews = getTaskViews();
            boolean wasLastFocusedTaskAnimated = false;
            int lastFocusedTaskIndex = -1;
            int taskViewCount = taskViews.size();
            for (int i = taskViewCount - 1; i >= 0; i--) {
                TaskView tv = taskViews.get(i);
                Task task = tv.getTask();
                int taskIndex = mStack.indexOfTask(task);
                if (task.isFreeformTask() ||
                        visibleStackRange[1] <= taskIndex && taskIndex <= visibleStackRange[0]) {
                    mTmpTaskViewMap.put(task, tv);
                } else {
                    if (mTouchExplorationEnabled && tv.isFocusedTask()) {
                        wasLastFocusedTaskAnimated = tv.isFocusAnimated();
                        lastFocusedTaskIndex = taskIndex;
                        resetFocusedTask();
                    }
                    if (DEBUG) {
                        Log.d(TAG, "returning to pool: " + task.key);
                    }
                    mViewPool.returnViewToPool(tv);
                }
            }

            // Pick up all the freeform tasks
            int firstVisStackIndex = isValidVisibleStackRange ? visibleStackRange[0] : 0;
            for (int i = mStack.getTaskCount() - 1; i >= firstVisStackIndex; i--) {
                Task task = tasks.get(i);
                if (!task.isFreeformTask()) {
                    continue;
                }
                TaskViewTransform transform = mLayoutAlgorithm.getStackTransform(task, stackScroll,
                        mCurrentTaskTransforms.get(i), null);
                TaskView tv = mTmpTaskViewMap.get(task);
                if (tv == null) {
                    if (DEBUG) {
                        Log.d(TAG, "picking up from pool: " + task.key);
                    }
                    tv = mViewPool.pickUpViewFromPool(task, task);
                    if (mLayersDisabled) {
                        tv.disableLayersForOneFrame();
                    }
                }

                // Animate the task into place
                tv.updateViewPropertiesToTaskTransform(transform,
                        mStackViewsAnimationDuration, mRequestUpdateClippingListener);

                // Reattach it in the right z order
                detachViewFromParent(tv);
                int insertIndex = -1;
                int taskIndex = mStack.indexOfTask(task);
                taskViews = getTaskViews();
                taskViewCount = taskViews.size();
                for (int j = 0; j < taskViewCount; j++) {
                    Task tvTask = taskViews.get(j).getTask();
                    if (taskIndex < mStack.indexOfTask(tvTask)) {
                        insertIndex = j;
                        break;
                    }
                }
                attachViewToParent(tv, insertIndex, tv.getLayoutParams());

                // Update the task views list after adding the new task view
                updateTaskViewsList();
            }

            // Pick up all the newly visible children and update all the existing children
            for (int i = visibleStackRange[0]; isValidVisibleStackRange && i >= visibleStackRange[1]; i--) {
                Task task = tasks.get(i);
                TaskViewTransform transform = mCurrentTaskTransforms.get(i);
                TaskView tv = mTmpTaskViewMap.get(task);

                if (tv == null) {
                    tv = mViewPool.pickUpViewFromPool(task, task);
                    if (mLayersDisabled) {
                        tv.disableLayersForOneFrame();
                    }
                    if (mStackViewsAnimationDuration > 0) {
                        // For items in the list, put them in start animating them from the
                        // approriate ends of the list where they are expected to appear
                        if (Float.compare(transform.p, 0f) <= 0) {
                            if (!hasStackBackTransform) {
                                hasStackBackTransform = true;
                                mLayoutAlgorithm.getStackTransform(0f, 0f, mTmpStackBackTransform,
                                        null);
                            }
                            tv.updateViewPropertiesToTaskTransform(mTmpStackBackTransform, 0);
                        } else {
                            if (!hasStackFrontTransform) {
                                hasStackFrontTransform = true;
                                mLayoutAlgorithm.getStackTransform(1f, 0f, mTmpStackFrontTransform,
                                        null);
                            }
                            tv.updateViewPropertiesToTaskTransform(mTmpStackFrontTransform, 0);
                        }
                    }
                }

                // Animate the task into place
                tv.updateViewPropertiesToTaskTransform(transform,
                        mStackViewsAnimationDuration, mRequestUpdateClippingListener);
            }

            // Update the focus if the previous focused task was returned to the view pool
            if (lastFocusedTaskIndex != -1) {
                if (lastFocusedTaskIndex < visibleStackRange[1]) {
                    setFocusedTask(visibleStackRange[1], false, wasLastFocusedTaskAnimated);
                } else {
                    setFocusedTask(visibleStackRange[0], false, wasLastFocusedTaskAnimated);
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

    /**
     * Updates the clip for each of the task views from back to front.
     */
    void clipTaskViews(boolean forceUpdate) {
        // Update the clip on each task child
        List<TaskView> taskViews = getTaskViews();
        TaskView tmpTv = null;
        int taskViewCount = taskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = taskViews.get(i);
            TaskView frontTv = null;
            int clipBottom = 0;
            if (i < (taskViewCount - 1) && tv.shouldClipViewInStack()) {
                // Find the next view to clip against
                for (int j = i + 1; j < taskViewCount; j++) {
                    tmpTv = taskViews.get(j);
                    if (tmpTv.shouldClipViewInStack()) {
                        frontTv = tmpTv;
                        break;
                    }
                }

                // Clip against the next view, this is just an approximation since we are
                // stacked and we can make assumptions about the visibility of the this
                // task relative to the ones in front of it.
                if (frontTv != null) {
                    mTmpTaskRect.set(mLayoutAlgorithm.mTaskRect);
                    mTmpTaskRect.offset(0, tv.getTranslationY());
                    Utilities.scaleRectAboutCenter(mTmpTaskRect, tv.getScaleX());
                    float taskBottom = mTmpTaskRect.bottom;
                    mTmpTaskRect.set(mLayoutAlgorithm.mTaskRect);
                    mTmpTaskRect.offset(0, frontTv.getTranslationY());
                    Utilities.scaleRectAboutCenter(mTmpTaskRect, frontTv.getScaleX());
                    float frontTaskTop = mTmpTaskRect.top;
                    if (frontTaskTop < taskBottom) {
                        // Map the stack view space coordinate (the rects) to view space
                        clipBottom = (int) ((taskBottom - frontTaskTop) / tv.getScaleX()) - 1;
                    }
                }
            }
            tv.getViewBounds().setClipBottom(clipBottom, forceUpdate);
        }
        mStackViewsClipDirty = false;
    }

    /** Updates the min and max virtual scroll bounds */
    void updateLayout(boolean boundScrollToNewMinMax) {
        // Compute the min and max scroll values
        mLayoutAlgorithm.update(mStack);

        // Update the freeform workspace
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.hasFreeformWorkspaceSupport()) {
            mTmpRect.set(mLayoutAlgorithm.mFreeformRect);
            mFreeformWorkspaceBackground.setAlpha(255);
            mFreeformWorkspaceBackground.setBounds(mTmpRect);
        }

        // Debug logging
        if (boundScrollToNewMinMax) {
            mStackScroller.boundScroll();
        }
    }

    /** Returns the scroller. */
    public TaskStackViewScroller getScroller() {
        return mStackScroller;
    }

    /**
     * Sets the focused task to the provided (bounded taskIndex).
     */
    private void setFocusedTask(int taskIndex, boolean scrollToTask, final boolean animated) {
        setFocusedTask(taskIndex, scrollToTask, animated, true);
    }

    /**
     * Sets the focused task to the provided (bounded taskIndex).
     */
    private void setFocusedTask(int taskIndex, boolean scrollToTask, final boolean animated,
                                final boolean requestViewFocus) {
        // Find the next task to focus
        int newFocusedTaskIndex = mStack.getTaskCount() > 0 ?
                Math.max(0, Math.min(mStack.getTaskCount() - 1, taskIndex)) : -1;
        final Task newFocusedTask = (newFocusedTaskIndex != -1) ?
                mStack.getTasks().get(newFocusedTaskIndex) : null;

        // Reset the last focused task state if changed
        if (mFocusedTaskIndex != -1) {
            Task focusedTask = mStack.getTasks().get(mFocusedTaskIndex);
            if (focusedTask != newFocusedTask) {
                resetFocusedTask();
            }
        }

        mFocusedTaskIndex = newFocusedTaskIndex;
        if (mFocusedTaskIndex != -1) {
            Runnable focusTaskRunnable = new Runnable() {
                @Override
                public void run() {
                    TaskView tv = getChildViewForTask(newFocusedTask);
                    if (tv != null) {
                        tv.setFocusedState(true, animated, requestViewFocus);
                    }
                }
            };

            if (scrollToTask) {
                // TODO: Center the newly focused task view, only if not freeform
                float newScroll = mLayoutAlgorithm.getStackScrollForTask(newFocusedTask) - 0.5f;
                newScroll = mStackScroller.getBoundedStackScroll(newScroll);
                mStackScroller.animateScroll(mStackScroller.getStackScroll(), newScroll,
                        focusTaskRunnable);
            } else {
                focusTaskRunnable.run();
            }
        }
    }

    /**
     * Sets the focused task relative to the currently focused task.
     *
     * @param stackTasksOnly if set, will ensure that the traversal only goes along stack tasks, and
     *                       if the currently focused task is not a stack task, will set the focus
     *                       to the first visible stack task
     * @param animated determines whether to actually draw the highlight along with the change in
     *                            focus.
     */
    public void setRelativeFocusedTask(boolean forward, boolean stackTasksOnly, boolean animated) {
        int newIndex = -1;
        if (mFocusedTaskIndex != -1) {
            if (stackTasksOnly) {
                List<Task> tasks =  mStack.getTasks();
                newIndex = mFocusedTaskIndex;
                Task task = tasks.get(mFocusedTaskIndex);
                if (task.isFreeformTask()) {
                    // Try and focus the front most stack task
                    TaskView tv = getFrontMostTaskView(stackTasksOnly);
                    if (tv != null) {
                        newIndex = mStack.indexOfTask(tv.getTask());
                    }
                } else {
                    // Try the next task if it is a stack task
                    int tmpNewIndex = mFocusedTaskIndex + (forward ? -1 : 1);
                    if (0 <= tmpNewIndex && tmpNewIndex < tasks.size()) {
                        Task t = tasks.get(tmpNewIndex);
                        if (!t.isFreeformTask()) {
                            newIndex = tmpNewIndex;
                        }
                    }
                }
            } else {
                // No restrictions, lets just move to the new task
                newIndex = mFocusedTaskIndex + (forward ? -1 : 1);
            }
        } else {
            // We don't have a focused task, so focus the first visible task view
            TaskView tv = getFrontMostTaskView(stackTasksOnly);
            if (tv != null) {
                newIndex = mStack.indexOfTask(tv.getTask());
            }
        }
        if (newIndex != -1) {
            setFocusedTask(newIndex, true, animated);
        }
    }

    /**
     * Resets the focused task.
     */
    void resetFocusedTask() {
        if (mFocusedTaskIndex != -1) {
            Task t = mStack.getTasks().get(mFocusedTaskIndex);
            TaskView tv = getChildViewForTask(t);
            if (tv != null) {
                tv.setFocusedState(false, false /* animated */, false /* requestViewFocus */);
            }
        }
        mFocusedTaskIndex = -1;
    }

    /**
     * Returns the focused task.
     */
    Task getFocusedTask() {
        if (mFocusedTaskIndex != -1) {
            return mStack.getTasks().get(mFocusedTaskIndex);
        }
        return null;
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
        if (taskViewCount > 1 && mFocusedTaskIndex != -1) {
            info.setScrollable(true);
            if (mFocusedTaskIndex > 0) {
                info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            if (mFocusedTaskIndex < mStack.getTaskCount() - 1) {
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
        switch (action) {
            case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
                setRelativeFocusedTask(true, false /* stackTasksOnly */, false /* animated */);
                return true;
            }
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
                setRelativeFocusedTask(false, false /* stackTasksOnly */, false /* animated */);
                return true;
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
        clipTaskViews(false /* forceUpdate */);
        // Notify accessibility
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED);
    }

    /** Computes the stack and task rects */
    public void computeRects(Rect taskStackBounds) {
        // Compute the rects in the stack algorithm
        mLayoutAlgorithm.initialize(taskStackBounds);

        // Update the scroll bounds
        updateLayout(false);
    }

    /**
     * This is ONLY used from the Recents component to update the dummy stack view for purposes
     * of getting the task rect to animate to.
     */
    public void updateLayoutForStack(TaskStack stack) {
        mStack = stack;
        updateLayout(false);
    }

    /**
     * Computes the maximum number of visible tasks and thumbnails.  Requires that
     * updateLayoutForStack() is called first.
     */
    public TaskStackLayoutAlgorithm.VisibilityReport computeStackVisibilityReport() {
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
        computeRects(mTaskStackBounds);

        // If this is the first layout, then scroll to the front of the stack and synchronize the
        // stack views immediately to load all the views
        if (mAwaitingFirstLayout) {
            mStackScroller.setStackScrollToInitialState();
            requestSynchronizeStackViewsWithModel();
            synchronizeStackViewsWithModel();
        }

        // Measure each of the TaskViews
        mTmpTaskViews.clear();
        mTmpTaskViews.addAll(getTaskViews());
        mTmpTaskViews.addAll(mViewPool.getViews());
        int taskViewCount = mTmpTaskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = mTmpTaskViews.get(i);
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
        // Layout each of the TaskViews
        mTmpTaskViews.clear();
        mTmpTaskViews.addAll(getTaskViews());
        mTmpTaskViews.addAll(mViewPool.getViews());
        int taskViewCount = mTmpTaskViews.size();
        for (int i = 0; i < taskViewCount; i++) {
            TaskView tv = mTmpTaskViews.get(i);
            if (tv.getBackground() != null) {
                tv.getBackground().getPadding(mTmpRect);
            } else {
                mTmpRect.setEmpty();
            }
            Rect taskRect = mLayoutAlgorithm.mTaskRect;
            tv.layout(taskRect.left - mTmpRect.left, taskRect.top - mTmpRect.top,
                    taskRect.right + mTmpRect.right, taskRect.bottom + mTmpRect.bottom);
        }

        if (mAwaitingFirstLayout) {
            mAwaitingFirstLayout = false;
            onFirstLayout();
        }

        if (changed) {
            if (mStackScroller.isScrollOutOfBounds()) {
                mStackScroller.boundScroll();
            }
            requestSynchronizeStackViewsWithModel();
            synchronizeStackViewsWithModel();
            clipTaskViews(true /* forceUpdate */);
        }
    }

    /** Handler for the first layout. */
    void onFirstLayout() {
        int offscreenY = mLayoutAlgorithm.mCurrentStackRect.bottom;

        // Find the launch target task
        Task launchTargetTask = mStack.getLaunchTarget();
        List<TaskView> taskViews = getTaskViews();
        int taskViewCount = taskViews.size();

        // Prepare the first view for its enter animation
        for (int i = taskViewCount - 1; i >= 0; i--) {
            TaskView tv = taskViews.get(i);
            Task task = tv.getTask();
            boolean occludesLaunchTarget = (launchTargetTask != null) &&
                    launchTargetTask.group.isTaskAboveTask(task, launchTargetTask);
            tv.prepareEnterRecentsAnimation(task.isLaunchTarget, occludesLaunchTarget,
                    offscreenY);
        }

        // If the enter animation started already and we haven't completed a layout yet, do the
        // enter animation now
        if (mStartEnterAnimationRequestedAfterLayout) {
            startEnterRecentsAnimation(mStartEnterAnimationContext);
            mStartEnterAnimationRequestedAfterLayout = false;
            mStartEnterAnimationContext = null;
        }

        // Set the task focused state without requesting view focus, and leave the focus animations
        // until after the enter-animation
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        int focusedTaskIndex = launchState.getInitialFocusTaskIndex(mStack.getTaskCount());
        if (focusedTaskIndex != -1) {
            setFocusedTask(focusedTaskIndex, false /* scrollToTask */, false /* animated */,
                    false /* requestViewFocus */);
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
            Task launchTargetTask = mStack.getLaunchTarget();
            List<TaskView> taskViews = getTaskViews();
            int taskViewCount = taskViews.size();

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
                mLayoutAlgorithm.getStackTransform(task, mStackScroller.getStackScroll(),
                        ctx.currentTaskTransform, null);
                tv.startEnterRecentsAnimation(ctx);
            }

            // Add a runnable to the post animation ref counter to clear all the views
            ctx.postAnimationTrigger.addLastDecrementRunnable(new Runnable() {
                @Override
                public void run() {
                    // Poke the dozer to restart the trigger after the animation completes
                    mUIDozeTrigger.poke();

                    // Update the focused state here -- since we only set the focused task without
                    // requesting view focus in onFirstLayout(), actually request view focus and
                    // animate the focused state if we are alt-tabbing now, after the window enter
                    // animation is completed
                    if (mFocusedTaskIndex != -1) {
                        RecentsConfiguration config = Recents.getConfiguration();
                        RecentsActivityLaunchState launchState = config.getLaunchState();
                        setFocusedTask(mFocusedTaskIndex, false /* scrollToTask */,
                                launchState.launchedWithAltTab);
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
        ctx.offscreenTranslationY = mLayoutAlgorithm.mCurrentStackRect.bottom;

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

    public boolean isTransformedTouchPointInView(float x, float y, View child) {
        return isTransformedTouchPointInView(x, y, child, null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mLayersDisabled = false;

        // Draw the freeform workspace background
        if (mFreeformWorkspaceBackground.getAlpha() > 0) {
            mFreeformWorkspaceBackground.draw(canvas);
        }

        super.dispatchDraw(canvas);
    }

    public void disableLayersForOneFrame() {
        mLayersDisabled = true;
        List<TaskView> taskViews = getTaskViews();
        for (int i = 0; i < taskViews.size(); i++) {
            taskViews.get(i).disableLayersForOneFrame();
        }
    }

    /**
     * Launches the freeform tasks.
     */
    public boolean launchFreeformTasks() {
        Task frontTask = mStack.getFrontMostTask();
        if (frontTask != null && frontTask.isFreeformTask()) {
            onTaskViewClicked(getChildViewForTask(frontTask), frontTask, false);
            return true;
        }
        return false;
    }

    /**** TaskStackCallbacks Implementation ****/

    @Override
    public void onStackTaskAdded(TaskStack stack, Task t) {
        requestSynchronizeStackViewsWithModel();
    }

    @Override
    public void onStackTaskRemoved(TaskStack stack, Task removedTask, boolean wasFrontMostTask,
            Task newFrontMostTask) {
        if (!removedTask.isFreeformTask()) {
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
            updateLayout(true);

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
        } else {
            // Remove the view associated with this task, we can't rely on updateTransforms
            // to work here because the task is no longer in the list
            TaskView tv = getChildViewForTask(removedTask);
            if (tv != null) {
                mViewPool.returnViewToPool(tv);
            }

            // Update the min/max scroll and animate other task views into their new positions
            updateLayout(true);

            // Animate all the tasks into place
            requestSynchronizeStackViewsWithModel(200);
        }

        // Update the new front most task
        if (newFrontMostTask != null) {
            TaskView frontTv = getChildViewForTask(newFrontMostTask);
            if (frontTv != null) {
                frontTv.onTaskBound(newFrontMostTask);
                frontTv.fadeInActionButton(getResources().getInteger(
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
        updateLayout(false);
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
        updateLayout(false);
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

        // Report that this tasks's data is no longer being used
        Recents.getTaskLoader().unloadTaskData(task);

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
        Recents.getTaskLoader().loadTaskData(task);

        // If the doze trigger has already fired, then update the state for this task view
        tv.setNoUserInteractionState();

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
            mCb.onTaskViewClicked(this, tv, mStack, task, lockToTask, null, INVALID_STACK_ID);
        }
    }

    @Override
    public void onTaskViewClipStateChanged(TaskView tv) {
        if (!mStackViewsDirty) {
            invalidate();
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
        HashSet<ComponentName> removedComponents = mStack.computeComponentsRemoved(
                event.packageName, event.userId);

        // For other tasks, just remove them directly if they no longer exist
        ArrayList<Task> tasks = mStack.getTasks();
        for (int i = tasks.size() - 1; i >= 0; i--) {
            final Task t = tasks.get(i);
            if (removedComponents.contains(t.key.getComponent())) {
                final TaskView tv = getChildViewForTask(t);
                if (tv != null) {
                    // For visible children, defer removing the task until after the animation
                    tv.startDeleteTaskAnimation(new Runnable() {
                        @Override
                        public void run() {
                            removeTaskViewFromStack(tv);
                        }
                    }, 0);
                } else {
                    // Otherwise, remove the task from the stack immediately
                    mStack.removeTask(t);
                }
            }
        }
    }

    public final void onBusEvent(DismissTaskViewEvent event) {
        removeTaskViewFromStack(event.taskView);
    }

    public final void onBusEvent(FocusNextTaskViewEvent event) {
        setRelativeFocusedTask(true, false /* stackTasksOnly */, true /* animated */);
    }

    public final void onBusEvent(FocusPreviousTaskViewEvent event) {
        setRelativeFocusedTask(false, false /* stackTasksOnly */, true /* animated */);
    }

    public final void onBusEvent(DismissFocusedTaskViewEvent event) {
        if (mFocusedTaskIndex != -1) {
            Task t = mStack.getTasks().get(mFocusedTaskIndex);
            TaskView tv = getChildViewForTask(t);
            tv.dismissTask();
        }
    }

    public final void onBusEvent(UserInteractionEvent event) {
        // Poke the doze trigger on user interaction
        mUIDozeTrigger.poke();
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent event) {
        if (!event.visible) {
            reset();
        }
    }

    public final void onBusEvent(DragStartEvent event) {
        if (event.task.isFreeformTask()) {
            // Animate to the front of the stack
            mStackScroller.animateScroll(mStackScroller.getStackScroll(),
                    mLayoutAlgorithm.mInitialScrollP, null);
        }
    }

    public final void onBusEvent(DragStartInitializeDropTargetsEvent event) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.hasFreeformWorkspaceSupport()) {
            event.handler.registerDropTargetForCurrentDrag(mStackDropTarget);
            event.handler.registerDropTargetForCurrentDrag(mFreeformWorkspaceDropTarget);
        }
    }

    public final void onBusEvent(DragDropTargetChangedEvent event) {
        // TODO: Animate the freeform workspace background etc.
    }

    public final void onBusEvent(final DragEndEvent event) {
        if (event.dropTarget != mFreeformWorkspaceDropTarget &&
                event.dropTarget != mStackDropTarget) {
            return;
        }
        if (event.task.isFreeformTask() && event.dropTarget == mFreeformWorkspaceDropTarget) {
            // TODO: Animate back into view
            return;
        }
        if (!event.task.isFreeformTask() && event.dropTarget == mStackDropTarget) {
            // TODO: Animate back into view
            return;
        }

        // Move the task to the right position in the stack (ie. the front of the stack if freeform
        // or the front of the stack if fullscreen).  Note, we MUST move the tasks before we update
        // their stack ids, otherwise, the keys will have changed.
        if (event.dropTarget == mFreeformWorkspaceDropTarget) {
            mStack.moveTaskToStack(event.task, FREEFORM_WORKSPACE_STACK_ID);
            updateLayout(true);
        } else if (event.dropTarget == mStackDropTarget) {
            mStack.moveTaskToStack(event.task, FULLSCREEN_WORKSPACE_STACK_ID);
            updateLayout(true);
        }

        event.postAnimationTrigger.increment();
        event.postAnimationTrigger.addLastDecrementRunnable(new Runnable() {
            @Override
            public void run() {
                SystemServicesProxy ssp = Recents.getSystemServices();
                ssp.moveTaskToStack(event.task.key.id, event.task.key.stackId);
            }
        });

        // Animate the drag view to the new position
        mLayoutAlgorithm.getStackTransform(event.task, mStackScroller.getStackScroll(),
                mTmpTransform, null);
        event.dragView.animate()
                .scaleX(mTmpTransform.scale)
                .scaleY(mTmpTransform.scale)
                .translationX((mLayoutAlgorithm.mTaskRect.left - event.dragView.getLeft())
                        + mTmpTransform.translationX)
                .translationY((mLayoutAlgorithm.mTaskRect.top - event.dragView.getTop())
                        + mTmpTransform.translationY)
                .setDuration(175)
                .setInterpolator(mFastOutSlowInInterpolator)
                .withEndAction(event.postAnimationTrigger.decrementAsRunnable())
                .start();

        // Animate the other views into place
        requestSynchronizeStackViewsWithModel(175);
    }

    /**
     * Removes the task from the stack, and updates the focus to the next task in the stack if the
     * removed TaskView was focused.
     */
    private void removeTaskViewFromStack(TaskView tv) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        Task task = tv.getTask();
        int taskIndex = mStack.indexOfTask(task);
        boolean taskWasFocused = tv.isFocusedTask();

        // Reset the previously focused task before it is removed from the stack
        resetFocusedTask();

        // Announce for accessibility
        tv.announceForAccessibility(getContext().getString(
                R.string.accessibility_recents_item_dismissed, tv.getTask().activityLabel));

        // Remove the task from the stack
        mStack.removeTask(task);

        if (taskWasFocused || ssp.isTouchExplorationEnabled()) {
            // If the dismissed task was focused or if we are in touch exploration mode, then focus
            // the next task
            RecentsConfiguration config = Recents.getConfiguration();
            RecentsActivityLaunchState launchState = config.getLaunchState();
            boolean isFreeformTask = taskIndex > 0 ?
                    mStack.getTasks().get(taskIndex - 1).isFreeformTask() : false;
            setFocusedTask(taskIndex - 1, !isFreeformTask /* scrollToTask */,
                    launchState.launchedWithAltTab);
        }
    }
}