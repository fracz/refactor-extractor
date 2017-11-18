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

import android.annotation.Nullable;
import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.util.Log;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsDebugFlags;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskStartedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.TaskViewDismissedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;

import java.util.ArrayList;
import java.util.List;

import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.FULLSCREEN_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.INVALID_STACK_ID;

/**
 * A helper class to create transitions to/from Recents
 */
public class RecentsTransitionHelper {

    private static final String TAG = "RecentsTransitionHelper";
    private static final boolean DEBUG = false;

    /**
     * Special value for {@link #mAppTransitionAnimationSpecs}: Indicate that we are currently
     * waiting for the specs to be retrieved.
     */
    private static final List<AppTransitionAnimationSpec> SPECS_WAITING = new ArrayList<>();

    @GuardedBy("this")
    private List<AppTransitionAnimationSpec> mAppTransitionAnimationSpecs = SPECS_WAITING;

    private Context mContext;
    private Handler mHandler;
    private TaskViewTransform mTmpTransform = new TaskViewTransform();

    private Runnable mStartScreenPinningRunnable = new Runnable() {
        @Override
        public void run() {
            EventBus.getDefault().send(new ScreenPinningRequestEvent(mContext));
        }
    };

    public RecentsTransitionHelper(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    /**
     * Launches the specified {@link Task}.
     */
    public void launchTaskFromRecents(final TaskStack stack, @Nullable final Task task,
            final TaskStackView stackView, final TaskView taskView,
            final boolean screenPinningRequested, final Rect bounds, int destinationStack) {
        final ActivityOptions opts = ActivityOptions.makeBasic();
        if (bounds != null) {
            opts.setLaunchBounds(bounds.isEmpty() ? null : bounds);
        }

        final ActivityOptions.OnAnimationStartedListener animStartedListener;
        final IAppTransitionAnimationSpecsFuture transitionFuture;
        if (task.thumbnail != null && task.thumbnail.getWidth() > 0 &&
                task.thumbnail.getHeight() > 0) {
            transitionFuture = getAppTransitionFuture(task, stackView, destinationStack);
            animStartedListener = new ActivityOptions.OnAnimationStartedListener() {
                @Override
                public void onAnimationStarted() {
                    // If we are launching into another task, cancel the previous task's
                    // window transition
                    EventBus.getDefault().send(new CancelEnterRecentsWindowAnimationEvent(task));
                    EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());

                    if (screenPinningRequested) {
                        // Request screen pinning after the animation runs
                        mHandler.postDelayed(mStartScreenPinningRunnable, 350);
                    }
                }
            };
        } else {
            // This is only the case if the task is not on screen (scrolled offscreen for example)
            transitionFuture = null;
            animStartedListener = new ActivityOptions.OnAnimationStartedListener() {
                @Override
                public void onAnimationStarted() {
                    EventBus.getDefault().send(new ExitRecentsWindowFirstAnimationFrameEvent());
                }
            };
        }

        if (taskView == null) {
            // If there is no task view, then we do not need to worry about animating out occluding
            // task views, and we can launch immediately
            startTaskActivity(stack, task, taskView, opts, transitionFuture, animStartedListener);
        } else {
            LaunchTaskStartedEvent launchStartedEvent = new LaunchTaskStartedEvent(taskView,
                    screenPinningRequested);
            if (task.group != null && !task.group.isFrontMostTask(task)) {
                launchStartedEvent.addPostAnimationCallback(new Runnable() {
                    @Override
                    public void run() {
                        startTaskActivity(stack, task, taskView, opts, transitionFuture,
                                animStartedListener);
                    }
                });
                EventBus.getDefault().send(launchStartedEvent);
            } else {
                EventBus.getDefault().send(launchStartedEvent);
                startTaskActivity(stack, task, taskView, opts, transitionFuture,
                        animStartedListener);
            }
        }
    }

    /**
     * Starts the activity for the launch task.
     *
     * @param taskView this is the {@link TaskView} that we are launching from. This can be null if
     *                 we are toggling recents and the launch-to task is now offscreen.
     */
    private void startTaskActivity(TaskStack stack, Task task, @Nullable TaskView taskView,
            ActivityOptions opts, IAppTransitionAnimationSpecsFuture transitionFuture,
            final ActivityOptions.OnAnimationStartedListener animStartedListener) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (ssp.startActivityFromRecents(mContext, task.key.id, task.title, opts)) {
            // Keep track of the index of the task launch
            int taskIndexFromFront = 0;
            int taskIndex = stack.indexOfStackTask(task);
            if (taskIndex > -1) {
                taskIndexFromFront = stack.getStackTaskCount() - taskIndex - 1;
            }
            EventBus.getDefault().send(new LaunchTaskSucceededEvent(taskIndexFromFront));
        } else {
            // Dismiss the task if we fail to launch it
            taskView.dismissTask();

            // Keep track of failed launches
            EventBus.getDefault().send(new LaunchTaskFailedEvent());
        }
        if (transitionFuture != null) {
            IRemoteCallback.Stub callback = null;
            if (animStartedListener != null) {
                callback = new IRemoteCallback.Stub() {
                    @Override
                    public void sendResult(Bundle data) throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (animStartedListener != null) {
                                    animStartedListener.onAnimationStarted();
                                }
                            }
                        });
                    }
                };
            }
            try {
                synchronized (this) {
                    mAppTransitionAnimationSpecs = SPECS_WAITING;
                }
                WindowManagerGlobal.getWindowManagerService()
                        .overridePendingAppTransitionMultiThumbFuture(transitionFuture,
                                callback, true /* scaleUp */);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to override transition: " + e);
            }
        }
    }

    /**
     * Creates a future which will later be queried for animation specs for this current transition.
     */
    private IAppTransitionAnimationSpecsFuture getAppTransitionFuture(final Task task,
            final TaskStackView stackView, final int destinationStack) {
        return new IAppTransitionAnimationSpecsFuture.Stub() {
            @Override
            public AppTransitionAnimationSpec[] get() throws RemoteException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (RecentsTransitionHelper.this) {
                            mAppTransitionAnimationSpecs = composeAnimationSpecs(task, stackView,
                                    destinationStack);
                            RecentsTransitionHelper.this.notifyAll();
                        }
                    }
                });
                synchronized (RecentsTransitionHelper.this) {
                    while (mAppTransitionAnimationSpecs == SPECS_WAITING) {
                        try {
                            RecentsTransitionHelper.this.wait();
                        } catch (InterruptedException e) {}
                    }
                    if (mAppTransitionAnimationSpecs == null) {
                        return null;
                    }
                    AppTransitionAnimationSpec[] specs
                            = new AppTransitionAnimationSpec[mAppTransitionAnimationSpecs.size()];
                    mAppTransitionAnimationSpecs.toArray(specs);
                    mAppTransitionAnimationSpecs = SPECS_WAITING;
                    return specs;
                }
            }
        };
    }

    /**
     * Composes the animation specs for all the tasks in the target stack.
     */
    private List<AppTransitionAnimationSpec> composeAnimationSpecs(final Task task,
            final TaskStackView stackView, final int destinationStack) {
        // Ensure we have a valid target stack id
        final int targetStackId = destinationStack != INVALID_STACK_ID ?
                destinationStack : task.key.stackId;
        if (!StackId.useAnimationSpecForAppTransition(targetStackId)) {
            return null;
        }

        // Calculate the offscreen task rect (for tasks that are not backed by views)
        float stackScroll = stackView.getScroller().getStackScroll();
        TaskView taskView = stackView.getChildViewForTask(task);
        TaskStackLayoutAlgorithm layoutAlgorithm = stackView.getStackAlgorithm();
        Rect offscreenTaskRect = new Rect(layoutAlgorithm.mTaskRect);
        offscreenTaskRect.offsetTo(offscreenTaskRect.left,
                layoutAlgorithm.mStackRect.bottom);

        // If this is a full screen stack, the transition will be towards the single, full screen
        // task. We only need the transition spec for this task.
        List<AppTransitionAnimationSpec> specs = new ArrayList<>();
        if (targetStackId == FULLSCREEN_WORKSPACE_STACK_ID) {
            if (taskView == null) {
                specs.add(composeOffscreenAnimationSpec(task, offscreenTaskRect));
            } else {
                layoutAlgorithm.getStackTransform(task, stackScroll, mTmpTransform, null);
                specs.add(composeAnimationSpec(taskView, mTmpTransform, true /* addHeaderBitmap */));
            }
            return specs;
        }

        // Otherwise, for freeform tasks, create a new animation spec for each task we have to
        // launch
        TaskStack stack = stackView.getStack();
        ArrayList<Task> tasks = stack.getStackTasks();
        int taskCount = tasks.size();
        for (int i = taskCount - 1; i >= 0; i--) {
            Task t = tasks.get(i);
            if (t.isFreeformTask() || targetStackId == FREEFORM_WORKSPACE_STACK_ID) {
                TaskView tv = stackView.getChildViewForTask(t);
                if (tv == null) {
                    // TODO: Create a different animation task rect for this case (though it should
                    //       never happen)
                    specs.add(composeOffscreenAnimationSpec(t, offscreenTaskRect));
                } else {
                    layoutAlgorithm.getStackTransform(task, stackScroll, mTmpTransform, null);
                    specs.add(composeAnimationSpec(tv, mTmpTransform, true /* addHeaderBitmap */));
                }
            }
        }

        return specs;
    }

    /**
     * Composes a single animation spec for the given {@link Task}
     */
    private static AppTransitionAnimationSpec composeOffscreenAnimationSpec(Task task,
            Rect taskRect) {
        return new AppTransitionAnimationSpec(task.key.id, null, taskRect);
    }

    /**
     * Composes a single animation spec for the given {@link TaskView}
     */
    private static AppTransitionAnimationSpec composeAnimationSpec(TaskView taskView,
            TaskViewTransform transform, boolean addHeaderBitmap) {
        Bitmap b = null;
        if (addHeaderBitmap) {
            float scale = transform.scale;
            int fromHeaderWidth = (int) (transform.rect.width());
            int fromHeaderHeight = (int) (taskView.mHeaderView.getMeasuredHeight() * scale);
            b = Bitmap.createBitmap(fromHeaderWidth, fromHeaderHeight,
                    Bitmap.Config.ARGB_8888);

            if (RecentsDebugFlags.Static.EnableTransitionThumbnailDebugMode) {
                b.eraseColor(0xFFff0000);
            } else {
                Canvas c = new Canvas(b);
                c.scale(scale, scale);
                taskView.mHeaderView.draw(c);
                c.setBitmap(null);
            }
            b = b.createAshmemBitmap();
        }

        Rect taskRect = new Rect();
        transform.rect.round(taskRect);
        return new AppTransitionAnimationSpec(taskView.getTask().key.id, b, taskRect);
    }
}