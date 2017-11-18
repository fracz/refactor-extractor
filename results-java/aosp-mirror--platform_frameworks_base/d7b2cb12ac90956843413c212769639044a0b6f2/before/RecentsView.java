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

import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.recents.Console;
import com.android.systemui.recents.Constants;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.model.RecentsPackageMonitor;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.SpaceNode;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;

import java.util.ArrayList;
import java.util.Set;

/**
 * This view is the the top level layout that contains TaskStacks (which are laid out according
 * to their SpaceNode bounds.
 */
public class RecentsView extends FrameLayout implements TaskStackView.TaskStackViewCallbacks,
        RecentsPackageMonitor.PackageCallbacks {

    /** The RecentsView callbacks */
    public interface RecentsViewCallbacks {
        public void onTaskLaunching();
        public void onExitToHomeAnimationTriggered();
    }

    RecentsConfiguration mConfig;
    LayoutInflater mInflater;
    Paint mDebugModePaint;

    // The space partitioning root of this container
    SpaceNode mBSP;
    // Whether there are any tasks
    boolean mHasTasks;
    // Search bar view
    View mSearchBar;
    // Recents view callbacks
    RecentsViewCallbacks mCb;

    public RecentsView(Context context) {
        super(context);
    }

    public RecentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecentsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mConfig = RecentsConfiguration.getInstance();
        mInflater = LayoutInflater.from(context);
    }

    /** Sets the callbacks */
    public void setCallbacks(RecentsViewCallbacks cb) {
        mCb = cb;
    }

    /** Set/get the bsp root node */
    public void setBSP(SpaceNode n) {
        mBSP = n;

        // Create and add all the stacks for this partition of space.
        mHasTasks = false;
        removeAllViews();
        ArrayList<TaskStack> stacks = mBSP.getStacks();
        for (TaskStack stack : stacks) {
            TaskStackView stackView = new TaskStackView(getContext(), stack);
            stackView.setCallbacks(this);
            addView(stackView);
            mHasTasks |= (stack.getTaskCount() > 0);
        }

        // Enable debug mode drawing
        if (mConfig.debugModeEnabled) {
            mDebugModePaint = new Paint();
            mDebugModePaint.setColor(0xFFff0000);
            mDebugModePaint.setStyle(Paint.Style.STROKE);
            mDebugModePaint.setStrokeWidth(5f);
            setWillNotDraw(false);
        }
    }

    /** Launches the focused task from the first stack if possible */
    public boolean launchFocusedTask() {
        // Get the first stack view
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TaskStackView) {
                TaskStackView stackView = (TaskStackView) child;
                TaskStack stack = stackView.mStack;
                // Iterate the stack views and try and find the focused task
                int taskCount = stackView.getChildCount();
                for (int j = 0; j < taskCount; j++) {
                    TaskView tv = (TaskView) stackView.getChildAt(j);
                    Task task = tv.getTask();
                    if (tv.isFocusedTask()) {
                        if (Console.Enabled) {
                            Console.log(Constants.Log.UI.Focus, "[RecentsView|launchFocusedTask]",
                                    "Found focused Task");
                        }
                        onTaskLaunched(stackView, tv, stack, task);
                        return true;
                    }
                }
            }
        }
        if (Console.Enabled) {
            Console.log(Constants.Log.UI.Focus, "[RecentsView|launchFocusedTask]",
                    "No Tasks focused");
        }
        return false;
    }

    /** Launches the first task from the first stack if possible */
    public boolean launchFirstTask() {
        // Get the first stack view
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TaskStackView) {
                TaskStackView stackView = (TaskStackView) child;
                TaskStack stack = stackView.mStack;
                ArrayList<Task> tasks = stack.getTasks();

                // Get the first task in the stack
                if (!tasks.isEmpty()) {
                    Task task = tasks.get(tasks.size() - 1);
                    TaskView tv = null;

                    // Try and use the first child task view as the source of the launch animation
                    if (stackView.getChildCount() > 0) {
                        TaskView stv = (TaskView) stackView.getChildAt(stackView.getChildCount() - 1);
                        if (stv.getTask() == task) {
                            tv = stv;
                        }
                    }
                    onTaskLaunched(stackView, tv, stack, task);
                    return true;
                }
            }
        }
        return false;
    }

    /** Requests all task stacks to start their enter-recents animation */
    public void startEnterRecentsAnimation(ViewAnimation.TaskViewEnterContext ctx) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TaskStackView) {
                TaskStackView stackView = (TaskStackView) child;
                stackView.startEnterRecentsAnimation(ctx);
            }
        }
    }

    /** Requests all task stacks to start their exit-recents animation */
    public void startExitToHomeAnimation(ViewAnimation.TaskViewExitContext ctx) {
        // Handle the case when there are no views by incrementing and decrementing after all
        // animations are started.
        ctx.postAnimationTrigger.increment();

        if (Constants.DebugFlags.App.EnableHomeTransition) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child instanceof TaskStackView) {
                    TaskStackView stackView = (TaskStackView) child;
                    stackView.startExitToHomeAnimation(ctx);
                }
            }
        }

        // Handle the case when there are no views by incrementing and decrementing after all
        // animations are started.
        ctx.postAnimationTrigger.decrement();

        // Notify of the exit animation
        mCb.onExitToHomeAnimationTriggered();
    }

    /** Adds the search bar */
    public void setSearchBar(View searchBar) {
        // Create the search bar (and hide it if we have no recent tasks)
        if (Constants.DebugFlags.App.EnableSearchLayout) {
            // Remove the previous search bar if one exists
            if (mSearchBar != null && indexOfChild(mSearchBar) > -1) {
                removeView(mSearchBar);
            }
            // Add the new search bar
            if (searchBar != null) {
                mSearchBar = searchBar;
                mSearchBar.setVisibility(mHasTasks ? View.VISIBLE : View.GONE);
                addView(mSearchBar);

                if (Console.Enabled) {
                    Console.log(Constants.Log.App.SystemUIHandshake, "[RecentsView|setSearchBar]",
                            "" + (mSearchBar.getVisibility() == View.VISIBLE),
                            Console.AnsiBlue);
                }
            }
        }
    }

    /**
     * This is called with the full size of the window since we are handling our own insets.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (Console.Enabled) {
            Console.log(Constants.Log.UI.MeasureAndLayout, "[RecentsView|measure]",
                    "width: " + width + " height: " + height, Console.AnsiGreen);
            Console.logTraceTime(Constants.Log.App.TimeRecentsStartup,
                    Constants.Log.App.TimeRecentsStartupKey, "RecentsView.onMeasure");
        }

        // Get the search bar bounds and measure the search bar layout
        if (mSearchBar != null) {
            Rect searchBarSpaceBounds = new Rect();
            mConfig.getSearchBarBounds(width, height - mConfig.systemInsets.top, searchBarSpaceBounds);
            mSearchBar.measure(
                    MeasureSpec.makeMeasureSpec(searchBarSpaceBounds.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(searchBarSpaceBounds.height(), MeasureSpec.EXACTLY));
        }

        // We give the full width of the space, not including the right nav bar insets in landscape,
        // to the stack view, since we want the tasks to render under the search bar in landscape.
        // In addition, we give it the full height, not including the top inset or search bar space,
        // since we want the tasks to render under the navigation buttons in portrait.
        Rect taskStackBounds = new Rect();
        mConfig.getTaskStackBounds(width, height, taskStackBounds);
        int childWidth = width - mConfig.systemInsets.right;
        int childHeight = taskStackBounds.height() - mConfig.systemInsets.top;

        // Measure each TaskStackView
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TaskStackView && child.getVisibility() != GONE) {
                child.measure(MeasureSpec.makeMeasureSpec(childWidth, widthMode),
                        MeasureSpec.makeMeasureSpec(childHeight, heightMode));
            }
        }

        setMeasuredDimension(width, height);
    }

    /**
     * This is called with the full size of the window since we are handling our own insets.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (Console.Enabled) {
            Console.log(Constants.Log.UI.MeasureAndLayout, "[RecentsView|layout]",
                    new Rect(left, top, right, bottom) + " changed: " + changed, Console.AnsiGreen);
            Console.logTraceTime(Constants.Log.App.TimeRecentsStartup,
                    Constants.Log.App.TimeRecentsStartupKey, "RecentsView.onLayout");
        }

        // Get the search bar bounds so that we lay it out
        if (mSearchBar != null) {
            Rect searchBarSpaceBounds = new Rect();
            mConfig.getSearchBarBounds(getMeasuredWidth(), getMeasuredHeight(), searchBarSpaceBounds);
            mSearchBar.layout(mConfig.systemInsets.left + searchBarSpaceBounds.left,
                    mConfig.systemInsets.top + searchBarSpaceBounds.top,
                    mConfig.systemInsets.left + mSearchBar.getMeasuredWidth(),
                    mConfig.systemInsets.top + mSearchBar.getMeasuredHeight());
        }

        // We offset the stack view by the left inset (if any), but lay it out under the search bar.
        // In addition, we offset our stack views by the top inset and search bar height, but not
        // the bottom insets because we want it to render under the navigation buttons.
        Rect taskStackBounds = new Rect();
        mConfig.getTaskStackBounds(getMeasuredWidth(), getMeasuredHeight(), taskStackBounds);
        left += mConfig.systemInsets.left;
        top += mConfig.systemInsets.top + taskStackBounds.top;

        // Layout each child
        // XXX: Based on the space node for that task view
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TaskStackView && child.getVisibility() != GONE) {
                TaskStackView tsv = (TaskStackView) child;
                child.layout(left, top, left + tsv.getMeasuredWidth(), top + tsv.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Debug mode drawing
        if (mConfig.debugModeEnabled) {
            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mDebugModePaint);
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Console.Enabled) {
            Console.log(Constants.Log.UI.MeasureAndLayout,
                    "[RecentsView|fitSystemWindows]", "insets: " + insets, Console.AnsiGreen);
        }

        // Update the configuration with the latest system insets and trigger a relayout
        mConfig.updateSystemInsets(insets.getSystemWindowInsets());
        requestLayout();

        return insets.consumeSystemWindowInsets(false, false, false, true);
    }

    /** Notifies each task view of the user interaction. */
    public void onUserInteraction() {
        // Get the first stack view
        TaskStackView stackView = null;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TaskStackView) {
                stackView = (TaskStackView) child;
                stackView.onUserInteraction();
            }
        }
    }

    /** Focuses the next task in the first stack view */
    public void focusNextTask(boolean forward) {
        // Get the first stack view
        TaskStackView stackView = null;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TaskStackView) {
                stackView = (TaskStackView) child;
                break;
            }
        }

        if (stackView != null) {
            stackView.focusNextTask(forward);
        }
    }

    /** Unfilters any filtered stacks */
    public boolean unfilterFilteredStacks() {
        if (mBSP != null) {
            // Check if there are any filtered stacks and unfilter them before we back out of Recents
            boolean stacksUnfiltered = false;
            ArrayList<TaskStack> stacks = mBSP.getStacks();
            for (TaskStack stack : stacks) {
                if (stack.hasFilteredTasks()) {
                    stack.unfilterTasks();
                    stacksUnfiltered = true;
                }
            }
            return stacksUnfiltered;
        }
        return false;
    }

    /**** TaskStackView.TaskStackCallbacks Implementation ****/

    @Override
    public void onTaskLaunched(final TaskStackView stackView, final TaskView tv,
                               final TaskStack stack, final Task task) {
        // Notify any callbacks of the launching of a new task
        if (mCb != null) {
            mCb.onTaskLaunching();
        }

        // Upfront the processing of the thumbnail
        TaskViewTransform transform;
        View sourceView = tv;
        int offsetX = 0;
        int offsetY = 0;
        int stackScroll = stackView.getStackScroll();
        if (tv == null) {
            // If there is no actual task view, then use the stack view as the source view
            // and then offset to the expected transform rect, but bound this to just
            // outside the display rect (to ensure we don't animate from too far away)
            sourceView = stackView;
            transform = stackView.getStackTransform(stack.indexOfTask(task), stackScroll);
            offsetX = transform.rect.left;
            offsetY = Math.min(transform.rect.top, mConfig.displayRect.height());
        } else {
            transform = stackView.getStackTransform(stack.indexOfTask(task), stackScroll);
        }

        // Compute the thumbnail to scale up from
        ActivityOptions opts = null;
        int thumbnailWidth = transform.rect.width();
        int thumbnailHeight = transform.rect.height();
        if (task.thumbnail != null && thumbnailWidth > 0 && thumbnailHeight > 0 &&
                task.thumbnail.getWidth() > 0 && task.thumbnail.getHeight() > 0) {
            // Resize the thumbnail to the size of the view that we are animating from
            Bitmap b = Bitmap.createBitmap(thumbnailWidth, thumbnailHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            c.drawBitmap(task.thumbnail,
                    new Rect(0, 0, task.thumbnail.getWidth(), task.thumbnail.getHeight()),
                    new Rect(0, 0, thumbnailWidth, thumbnailHeight), null);
            c.setBitmap(null);
            opts = ActivityOptions.makeThumbnailScaleUpAnimation(sourceView,
                    b, offsetX, offsetY);
        }

        final ActivityOptions launchOpts = opts;
        final Runnable launchRunnable = new Runnable() {
            @Override
            public void run() {
                if (Console.Enabled) {
                    Console.logTraceTime(Constants.Log.App.TimeRecentsLaunchTask,
                            Constants.Log.App.TimeRecentsLaunchKey, "preStartActivity");
                }

                if (task.isActive) {
                    // Bring an active task to the foreground
                    RecentsTaskLoader.getInstance().getSystemServicesProxy()
                            .moveTaskToFront(task.key.id, launchOpts);
                } else {
                    // Launch the activity anew with the desired animation
                    Intent i = new Intent(task.key.baseIntent);
                    i.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                            | Intent.FLAG_ACTIVITY_TASK_ON_HOME
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        UserHandle taskUser = new UserHandle(task.userId);
                        if (launchOpts != null) {
                            getContext().startActivityAsUser(i, launchOpts.toBundle(), taskUser);
                        } else {
                            getContext().startActivityAsUser(i, taskUser);
                        }
                    } catch (ActivityNotFoundException anfe) {
                        Console.logError(getContext(), "Could not start Activity");
                    }

                    // And clean up the old task
                    onTaskRemoved(task);
                }

                if (Console.Enabled) {
                    Console.logTraceTime(Constants.Log.App.TimeRecentsLaunchTask,
                            Constants.Log.App.TimeRecentsLaunchKey, "startActivity");
                }
            }
        };

        if (Console.Enabled) {
            Console.logTraceTime(Constants.Log.App.TimeRecentsLaunchTask,
                    Constants.Log.App.TimeRecentsLaunchKey, "onTaskLaunched");
        }

        // Launch the app right away if there is no task view, otherwise, animate the icon out first
        if (tv == null) {
            post(launchRunnable);
        } else {
            stackView.animateOnLaunchingTask(tv, launchRunnable);
        }
    }

    @Override
    public void onTaskAppInfoLaunched(Task t) {
        // Create a new task stack with the application info details activity
        Intent baseIntent = t.key.baseIntent;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", baseIntent.getComponent().getPackageName(), null));
        intent.setComponent(intent.resolveActivity(getContext().getPackageManager()));
        TaskStackBuilder.create(getContext())
                .addNextIntentWithParentStack(intent).startActivities();
    }

    @Override
    public void onTaskRemoved(Task t) {
        // Remove any stored data from the loader.  We currently don't bother notifying the views
        // that the data has been unloaded because at the point we call onTaskRemoved(), the views
        // either don't need to be updated, or have already been removed.
        RecentsTaskLoader loader = RecentsTaskLoader.getInstance();
        loader.deleteTaskData(t, false);

        // Remove the old task from activity manager
        int flags = t.key.baseIntent.getFlags();
        boolean isDocument = (flags & Intent.FLAG_ACTIVITY_NEW_DOCUMENT) ==
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        RecentsTaskLoader.getInstance().getSystemServicesProxy().removeTask(t.key.id,
                isDocument);
    }

    @Override
    public void onTaskStackFilterTriggered() {
        // Hide the search bar
        if (mSearchBar != null) {
            mSearchBar.animate()
                    .alpha(0f)
                    .setStartDelay(0)
                    .setInterpolator(mConfig.fastOutSlowInInterpolator)
                    .setDuration(mConfig.filteringCurrentViewsAnimDuration)
                    .withLayer()
                    .start();
        }
    }

    @Override
    public void onTaskStackUnfilterTriggered() {
        // Show the search bar
        if (mSearchBar != null) {
            mSearchBar.animate()
                    .alpha(1f)
                    .setStartDelay(0)
                    .setInterpolator(mConfig.fastOutSlowInInterpolator)
                    .setDuration(mConfig.filteringNewViewsAnimDuration)
                    .withLayer()
                    .start();
        }
    }

    /**** RecentsPackageMonitor.PackageCallbacks Implementation ****/

    @Override
    public void onComponentRemoved(Set<ComponentName> cns) {
        // Propagate this event down to each task stack view
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TaskStackView) {
                TaskStackView stackView = (TaskStackView) child;
                stackView.onComponentRemoved(cns);
            }
        }
    }
}