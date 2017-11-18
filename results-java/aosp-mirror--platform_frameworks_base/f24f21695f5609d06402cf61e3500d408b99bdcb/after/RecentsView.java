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
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowInsets;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.android.internal.logging.MetricsLogger;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsAppWidgetHostView;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.RecentsDebugFlags;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.DebugFlagsChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.HideHistoryButtonEvent;
import com.android.systemui.recents.events.activity.HideHistoryEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.ShowHistoryButtonEvent;
import com.android.systemui.recents.events.activity.ShowHistoryEvent;
import com.android.systemui.recents.events.activity.TaskStackUpdatedEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.FlingAnimationUtils;

import java.util.ArrayList;
import java.util.List;

import static android.app.ActivityManager.StackId.INVALID_STACK_ID;

/**
 * This view is the the top level layout that contains TaskStacks (which are laid out according
 * to their SpaceNode bounds.
 */
public class RecentsView extends FrameLayout {

    private static final String TAG = "RecentsView";
    private static final boolean DEBUG = false;

    private final Handler mHandler;

    private TaskStack mStack;
    private TaskStackView mTaskStackView;
    private RecentsAppWidgetHostView mSearchBar;
    private TextView mHistoryButton;
    private View mEmptyView;
    private boolean mAwaitingFirstLayout = true;
    private boolean mLastTaskLaunchedWasFreeform;
    private Rect mSystemInsets = new Rect();

    private RecentsTransitionHelper mTransitionHelper;
    private RecentsViewTouchHandler mTouchHandler;
    private TaskStack.DockState[] mVisibleDockStates = {
            TaskStack.DockState.LEFT,
            TaskStack.DockState.TOP,
            TaskStack.DockState.RIGHT,
            TaskStack.DockState.BOTTOM,
    };

    private final Interpolator mFastOutSlowInInterpolator;
    private final Interpolator mFastOutLinearInInterpolator;
    private final FlingAnimationUtils mFlingAnimationUtils;

    public RecentsView(Context context) {
        this(context, null);
    }

    public RecentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
        mHandler = new Handler();
        mTransitionHelper = new RecentsTransitionHelper(getContext(), mHandler);
        mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.fast_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.fast_out_linear_in);
        mTouchHandler = new RecentsViewTouchHandler(this);
        mFlingAnimationUtils = new FlingAnimationUtils(context, 0.3f);

        LayoutInflater inflater = LayoutInflater.from(context);
        mHistoryButton = (TextView) inflater.inflate(R.layout.recents_history_button, this, false);
        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().send(new ShowHistoryEvent());
            }
        });
        addView(mHistoryButton);
        mEmptyView = inflater.inflate(R.layout.recents_empty, this, false);
        addView(mEmptyView);
    }

    /** Set/get the bsp root node */
    public void setTaskStack(TaskStack stack) {
        RecentsConfiguration config = Recents.getConfiguration();
        RecentsActivityLaunchState launchState = config.getLaunchState();
        mStack = stack;
        // Disable reusing task stack views until the visibility bug is fixed. b/25998134
        if (false && launchState.launchedReuseTaskStackViews) {
            if (mTaskStackView != null) {
                // If onRecentsHidden is not triggered, we need to the stack view again here
                mTaskStackView.reset();
                mTaskStackView.setStack(stack);
                removeView(mTaskStackView);
                addView(mTaskStackView);
            } else {
                mTaskStackView = new TaskStackView(getContext(), stack);
                addView(mTaskStackView);
            }
        } else {
            if (mTaskStackView != null) {
                removeView(mTaskStackView);
            }
            mTaskStackView = new TaskStackView(getContext(), stack);
            addView(mTaskStackView);
        }

        // Update the top level view's visibilities
        if (stack.getStackTaskCount() > 0) {
            hideEmptyView();
        } else {
            showEmptyView();
        }

        // Trigger a new layout
        requestLayout();
    }

    /**
     * Returns whether the last task launched was in the freeform stack or not.
     */
    public boolean isLastTaskLaunchedFreeform() {
        return mLastTaskLaunchedWasFreeform;
    }

    /**
     * Returns the currently set task stack.
     */
    public TaskStack getTaskStack() {
        return mStack;
    }

    /** Gets the next task in the stack - or if the last - the top task */
    public Task getNextTaskOrTopTask(Task taskToSearch) {
        Task returnTask = null;
        boolean found = false;
        if (mTaskStackView != null) {
            TaskStack stack = mTaskStackView.getStack();
            ArrayList<Task> taskList = stack.getStackTasks();
            // Iterate the stack views and try and find the focused task
            for (int j = taskList.size() - 1; j >= 0; --j) {
                Task task = taskList.get(j);
                // Return the next task in the line.
                if (found)
                    return task;
                // Remember the first possible task as the top task.
                if (returnTask == null)
                    returnTask = task;
                if (task == taskToSearch)
                    found = true;
            }
        }
        return returnTask;
    }

    /** Launches the focused task from the first stack if possible */
    public boolean launchFocusedTask() {
        if (mTaskStackView != null) {
            TaskStack stack = mTaskStackView.getStack();
            Task task = mTaskStackView.getFocusedTask();
            if (task != null) {
                TaskView taskView = mTaskStackView.getChildViewForTask(task);
                EventBus.getDefault().send(new LaunchTaskEvent(taskView, task, null,
                        INVALID_STACK_ID, false));
                return true;
            }
        }
        return false;
    }

    /** Launches the task that recents was launched from if possible */
    public boolean launchPreviousTask() {
        if (mTaskStackView != null) {
            TaskStack stack = mTaskStackView.getStack();
            Task task = stack.getLaunchTarget();
            if (task != null) {
                TaskView taskView = mTaskStackView.getChildViewForTask(task);
                EventBus.getDefault().send(new LaunchTaskEvent(taskView, task, null,
                        INVALID_STACK_ID, false));
                return true;
            }
        }
        return false;
    }

    /** Launches a given task. */
    public boolean launchTask(Task task, Rect taskBounds, int destinationStack) {
        if (mTaskStackView != null) {
            TaskStack stack = mTaskStackView.getStack();
            // Iterate the stack views and try and find the given task.
            List<TaskView> taskViews = mTaskStackView.getTaskViews();
            int taskViewCount = taskViews.size();
            for (int j = 0; j < taskViewCount; j++) {
                TaskView tv = taskViews.get(j);
                if (tv.getTask() == task) {
                    EventBus.getDefault().send(new LaunchTaskEvent(tv, task, taskBounds,
                            destinationStack, false));
                    return true;
                }
            }
        }
        return false;
    }

    /** Adds the search bar */
    public void setSearchBar(RecentsAppWidgetHostView searchBar) {
        // Remove the previous search bar if one exists
        if (mSearchBar != null && indexOfChild(mSearchBar) > -1) {
            removeView(mSearchBar);
        }
        // Add the new search bar
        if (searchBar != null) {
            mSearchBar = searchBar;
            addView(mSearchBar);
        }
    }

    /** Returns whether there is currently a search bar */
    public boolean hasValidSearchBar() {
        return mSearchBar != null && !mSearchBar.isReinflateRequired();
    }

    /**
     * Hides the task stack and shows the empty view.
     */
    public void showEmptyView() {
        if (RecentsDebugFlags.Static.EnableSearchBar && (mSearchBar != null)) {
            mSearchBar.setVisibility(View.INVISIBLE);
        }
        mTaskStackView.setVisibility(View.INVISIBLE);
        mEmptyView.setVisibility(View.VISIBLE);
        mEmptyView.bringToFront();
        mHistoryButton.bringToFront();
    }

    /**
     * Shows the task stack and hides the empty view.
     */
    public void hideEmptyView() {
        mEmptyView.setVisibility(View.INVISIBLE);
        mTaskStackView.setVisibility(View.VISIBLE);
        if (RecentsDebugFlags.Static.EnableSearchBar && (mSearchBar != null)) {
            mSearchBar.setVisibility(View.VISIBLE);
        }
        mTaskStackView.bringToFront();
        if (mSearchBar != null) {
            mSearchBar.bringToFront();
        }
        mHistoryButton.bringToFront();
    }

    /**
     * Returns the last known system insets.
     */
    public Rect getSystemInsets() {
        return mSystemInsets;
    }

    @Override
    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, RecentsActivity.EVENT_BUS_PRIORITY + 1);
        EventBus.getDefault().register(mTouchHandler, RecentsActivity.EVENT_BUS_PRIORITY + 1);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(mTouchHandler);
    }

    /**
     * This is called with the full size of the window since we are handling our own insets.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        RecentsConfiguration config = Recents.getConfiguration();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Get the search bar bounds and measure the search bar layout
        Rect searchBarSpaceBounds = new Rect();
        if (mSearchBar != null) {
            config.getSearchBarBounds(new Rect(0, 0, width, height), mSystemInsets.top,
                    searchBarSpaceBounds);
            mSearchBar.measure(
                    MeasureSpec.makeMeasureSpec(searchBarSpaceBounds.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(searchBarSpaceBounds.height(), MeasureSpec.EXACTLY));
        }

        Rect taskStackBounds = new Rect();
        config.getTaskStackBounds(new Rect(0, 0, width, height), mSystemInsets.top,
                mSystemInsets.right, searchBarSpaceBounds, taskStackBounds);
        if (mTaskStackView != null && mTaskStackView.getVisibility() != GONE) {
            mTaskStackView.setTaskStackBounds(taskStackBounds, mSystemInsets);
            mTaskStackView.measure(widthMeasureSpec, heightMeasureSpec);
        }

        // Measure the empty view
        measureChild(mEmptyView, MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        // Measure the history button with the full space above the stack, but width-constrained
        // to the stack
        Rect historyButtonRect = mTaskStackView.mLayoutAlgorithm.mHistoryButtonRect;
        measureChild(mHistoryButton,
                MeasureSpec.makeMeasureSpec(historyButtonRect.width(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(historyButtonRect.height(),
                        MeasureSpec.EXACTLY));

        setMeasuredDimension(width, height);
    }

    /**
     * This is called with the full size of the window since we are handling our own insets.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        RecentsConfiguration config = Recents.getConfiguration();

        // Get the search bar bounds so that we lay it out
        Rect measuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
        Rect searchBarSpaceBounds = new Rect();
        if (mSearchBar != null) {
            config.getSearchBarBounds(measuredRect,
                    mSystemInsets.top, searchBarSpaceBounds);
            mSearchBar.layout(searchBarSpaceBounds.left, searchBarSpaceBounds.top,
                    searchBarSpaceBounds.right, searchBarSpaceBounds.bottom);
        }

        if (mTaskStackView != null && mTaskStackView.getVisibility() != GONE) {
            mTaskStackView.layout(left, top, left + getMeasuredWidth(), top + getMeasuredHeight());
        }

        // Layout the empty view
        mEmptyView.layout(left, top, right, bottom);

        // Layout the history button left-aligned with the stack, but offset from the top of the
        // view
        Rect historyButtonRect = mTaskStackView.mLayoutAlgorithm.mHistoryButtonRect;
        mHistoryButton.layout(historyButtonRect.left, historyButtonRect.top,
                historyButtonRect.right, historyButtonRect.bottom);

        if (mAwaitingFirstLayout) {
            mAwaitingFirstLayout = false;

            // If launched via dragging from the nav bar, then we should translate the whole view
            // down offscreen
            RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
            if (launchState.launchedViaDragGesture) {
                setTranslationY(getMeasuredHeight());
            } else {
                setTranslationY(0f);
            }
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mSystemInsets.set(insets.getSystemWindowInsets());
        requestLayout();
        return insets;
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
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for (int i = mVisibleDockStates.length - 1; i >= 0; i--) {
            Drawable d = mVisibleDockStates[i].viewState.dockAreaOverlay;
            if (d.getAlpha() > 0) {
                d.draw(canvas);
            }
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        for (int i = mVisibleDockStates.length - 1; i >= 0; i--) {
            Drawable d = mVisibleDockStates[i].viewState.dockAreaOverlay;
            if (d == who) {
                return true;
            }
        }
        return super.verifyDrawable(who);
    }

    /**** EventBus Events ****/

    public final void onBusEvent(LaunchTaskEvent event) {
        mLastTaskLaunchedWasFreeform = event.task.isFreeformTask();
        mTransitionHelper.launchTaskFromRecents(mStack, event.task, mTaskStackView, event.taskView,
                event.screenPinningRequested, event.targetTaskBounds, event.targetTaskStack);
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted event) {
        // Hide the history button
        int taskViewExitToHomeDuration = getResources().getInteger(
                R.integer.recents_task_exit_to_home_duration);
        hideHistoryButton(taskViewExitToHomeDuration);

        // If we are going home, cancel the previous task's window transition
        EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(null));
    }

    public final void onBusEvent(DragStartEvent event) {
        updateVisibleDockRegions(mTouchHandler.getDockStatesForCurrentOrientation(),
                TaskStack.DockState.NONE.viewState.dockAreaAlpha);
    }

    public final void onBusEvent(DragDropTargetChangedEvent event) {
        if (event.dropTarget == null || !(event.dropTarget instanceof TaskStack.DockState)) {
            updateVisibleDockRegions(mTouchHandler.getDockStatesForCurrentOrientation(),
                    TaskStack.DockState.NONE.viewState.dockAreaAlpha);
        } else {
            final TaskStack.DockState dockState = (TaskStack.DockState) event.dropTarget;
            updateVisibleDockRegions(new TaskStack.DockState[] {dockState}, -1);
        }
    }

    public final void onBusEvent(final DragEndEvent event) {
        // Animate the overlay alpha back to 0
        updateVisibleDockRegions(null, -1);

        // Handle the case where we drop onto a dock region
        if (event.dropTarget instanceof TaskStack.DockState) {
            TaskStack.DockState dockState = (TaskStack.DockState) event.dropTarget;
            TaskStackLayoutAlgorithm stackLayout = mTaskStackView.getStackAlgorithm();
            TaskStackViewScroller stackScroller = mTaskStackView.getScroller();
            TaskViewTransform tmpTransform = new TaskViewTransform();

            // Remove the task view after it is docked
            stackLayout.getStackTransform(event.task, stackScroller.getStackScroll(), tmpTransform,
                    null);
            tmpTransform.scale = event.taskView.getScaleX();
            tmpTransform.rect.offset(event.taskView.getTranslationX(),
                    event.taskView.getTranslationY());
            mTaskStackView.updateTaskViewToTransform(event.taskView, tmpTransform,
                    new TaskViewAnimation(150, mFastOutLinearInInterpolator,
                            new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mTaskStackView.getStack().removeTask(event.task);
                                }
                            }));

            // Dock the task and launch it
            SystemServicesProxy ssp = Recents.getSystemServices();
            ssp.startTaskInDockedMode(event.task.key.id, dockState.createMode);
            launchTask(event.task, null, INVALID_STACK_ID);

            MetricsLogger.action(mContext,
                    MetricsLogger.ACTION_WINDOW_DOCK_DRAG_DROP);
        }
    }

    public final void onBusEvent(DraggingInRecentsEvent event) {
        if (mTaskStackView.getTaskViews().size() > 0) {
            setTranslationY(event.distanceFromTop - mTaskStackView.getTaskViews().get(0).getY());
        }
    }

    public final void onBusEvent(DraggingInRecentsEndedEvent event) {
        ViewPropertyAnimator animator = animate();
        if (event.velocity > mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            animator.translationY(getHeight());
            animator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    WindowManagerProxy.getInstance().maximizeDockedStack();
                }
            });
            mFlingAnimationUtils.apply(animator, getTranslationY(), getHeight(), event.velocity);
        } else {
            animator.translationY(0f);
            animator.setListener(null);
            mFlingAnimationUtils.apply(animator, getTranslationY(), 0, event.velocity);
        }
        animator.start();
    }

    public final void onBusEvent(ShowHistoryEvent event) {
        // Hide the history button when the history view is shown
        hideHistoryButton(getResources().getInteger(R.integer.recents_history_transition_duration),
                event.getAnimationTrigger());
        event.addPostAnimationCallback(new Runnable() {
            @Override
            public void run() {
                setAlpha(0f);
            }
        });
    }

    public final void onBusEvent(HideHistoryEvent event) {
        // Show the history button when the history view is hidden
        setAlpha(1f);
        showHistoryButton(getResources().getInteger(R.integer.recents_history_transition_duration),
                event.getAnimationTrigger());
    }

    public final void onBusEvent(ShowHistoryButtonEvent event) {
        showHistoryButton(150);
    }

    public final void onBusEvent(HideHistoryButtonEvent event) {
        hideHistoryButton(100);
    }

    public final void onBusEvent(TaskStackUpdatedEvent event) {
        mStack.setTasks(event.stack.computeAllTasksList(), true /* notifyStackChanges */);
        mStack.createAffiliatedGroupings(getContext());
    }

    /**
     * Shows the history button.
     */
    private void showHistoryButton(final int duration) {
        ReferenceCountedTrigger postAnimationTrigger = new ReferenceCountedTrigger();
        showHistoryButton(duration, postAnimationTrigger);
        postAnimationTrigger.flushLastDecrementRunnables();
    }

    private void showHistoryButton(final int duration,
            final ReferenceCountedTrigger postHideHistoryAnimationTrigger) {
        mHistoryButton.setText(getContext().getString(R.string.recents_history_label_format,
                mStack.getHistoricalTasks().size()));
        if (mHistoryButton.getVisibility() == View.INVISIBLE) {
            mHistoryButton.setVisibility(View.VISIBLE);
            mHistoryButton.setAlpha(0f);
            postHideHistoryAnimationTrigger.addLastDecrementRunnable(new Runnable() {
                @Override
                public void run() {
                    mHistoryButton.animate()
                            .alpha(1f)
                            .setDuration(duration)
                            .setInterpolator(mFastOutSlowInInterpolator)
                            .withLayer()
                            .start();
                }
            });
        }
    }

    /**
     * Hides the history button.
     */
    private void hideHistoryButton(int duration) {
        ReferenceCountedTrigger postAnimationTrigger = new ReferenceCountedTrigger();
        hideHistoryButton(duration, postAnimationTrigger);
        postAnimationTrigger.flushLastDecrementRunnables();
    }

    private void hideHistoryButton(int duration,
            final ReferenceCountedTrigger postHideStackAnimationTrigger) {
        if (mHistoryButton.getVisibility() == View.VISIBLE) {
            mHistoryButton.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setInterpolator(mFastOutLinearInInterpolator)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mHistoryButton.setVisibility(View.INVISIBLE);
                            postHideStackAnimationTrigger.decrement();
                        }
                    })
                    .withLayer()
                    .start();
            postHideStackAnimationTrigger.increment();
        }
    }

    /**
     * Updates the dock region to match the specified dock state.
     */
    private void updateVisibleDockRegions(TaskStack.DockState[] newDockStates, int overrideAlpha) {
        ArraySet<TaskStack.DockState> newDockStatesSet = new ArraySet<>();
        if (newDockStates != null) {
            for (TaskStack.DockState dockState : newDockStates) {
                newDockStatesSet.add(dockState);
            }
        }
        for (TaskStack.DockState dockState : mVisibleDockStates) {
            TaskStack.DockState.ViewState viewState = dockState.viewState;
            if (newDockStates == null || !newDockStatesSet.contains(dockState)) {
                // This is no longer visible, so hide it
                viewState.startAlphaAnimation(0, 150);
            } else {
                // This state is now visible, update the bounds and show it
                int alpha = (overrideAlpha != -1 ? overrideAlpha : viewState.dockAreaAlpha);
                viewState.dockAreaOverlay.setBounds(
                        dockState.getDockedBounds(getMeasuredWidth(), getMeasuredHeight()));
                viewState.dockAreaOverlay.setCallback(this);
                viewState.startAlphaAnimation(alpha, 150);
            }
        }
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent event) {
        if (!event.visible) {
            // Reset the view state
            mAwaitingFirstLayout = true;
            mLastTaskLaunchedWasFreeform = false;
        }
    }
}