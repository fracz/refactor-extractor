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

package com.android.systemui.recents.model;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.RectEvaluator;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import android.view.animation.Interpolator;
import com.android.internal.policy.DockedDividerUtils;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.RecentsDebugFlags;
import com.android.systemui.recents.misc.NamedCounter;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.views.DropTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static android.app.ActivityManager.DOCKED_STACK_CREATE_MODE_BOTTOM_OR_RIGHT;
import static android.app.ActivityManager.DOCKED_STACK_CREATE_MODE_TOP_OR_LEFT;
import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.FULLSCREEN_WORKSPACE_STACK_ID;
import static android.view.WindowManager.DOCKED_BOTTOM;
import static android.view.WindowManager.DOCKED_INVALID;
import static android.view.WindowManager.DOCKED_LEFT;
import static android.view.WindowManager.DOCKED_RIGHT;
import static android.view.WindowManager.DOCKED_TOP;


/**
 * An interface for a task filter to query whether a particular task should show in a stack.
 */
interface TaskFilter {
    /** Returns whether the filter accepts the specified task */
    public boolean acceptTask(SparseArray<Task> taskIdMap, Task t, int index);
}

/**
 * A list of filtered tasks.
 */
class FilteredTaskList {

    ArrayList<Task> mTasks = new ArrayList<>();
    ArrayList<Task> mFilteredTasks = new ArrayList<>();
    ArrayMap<Task.TaskKey, Integer> mTaskIndices = new ArrayMap<>();
    TaskFilter mFilter;

    /** Sets the task filter, saving the current touch state */
    boolean setFilter(TaskFilter filter) {
        ArrayList<Task> prevFilteredTasks = new ArrayList<>(mFilteredTasks);
        mFilter = filter;
        updateFilteredTasks();
        if (!prevFilteredTasks.equals(mFilteredTasks)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resets the task list, but does not remove the filter.
     */
    void reset() {
        mTasks.clear();
        mFilteredTasks.clear();
        mTaskIndices.clear();
    }

    /** Removes the task filter and returns the previous touch state */
    void removeFilter() {
        mFilter = null;
        updateFilteredTasks();
    }

    /** Adds a new task to the task list */
    void add(Task t) {
        mTasks.add(t);
        updateFilteredTasks();
    }

    /**
     * Moves the given task.
     */
    public void moveTaskToStack(Task task, int insertIndex, int newStackId) {
        int taskIndex = indexOf(task);
        if (taskIndex != insertIndex) {
            mTasks.remove(taskIndex);
            if (taskIndex < insertIndex) {
                insertIndex--;
            }
            mTasks.add(insertIndex, task);
        }

        // Update the stack id now, after we've moved the task, and before we update the
        // filtered tasks
        task.setStackId(newStackId);
        updateFilteredTasks();
    }

    /** Sets the list of tasks */
    void set(List<Task> tasks) {
        mTasks.clear();
        mTasks.addAll(tasks);
        updateFilteredTasks();
    }

    /** Removes a task from the base list only if it is in the filtered list */
    boolean remove(Task t) {
        if (mFilteredTasks.contains(t)) {
            boolean removed = mTasks.remove(t);
            updateFilteredTasks();
            return removed;
        }
        return false;
    }

    /** Returns the index of this task in the list of filtered tasks */
    int indexOf(Task t) {
        if (t != null && mTaskIndices.containsKey(t.key)) {
            return mTaskIndices.get(t.key);
        }
        return -1;
    }

    /** Returns the size of the list of filtered tasks */
    int size() {
        return mFilteredTasks.size();
    }

    /** Returns whether the filtered list contains this task */
    boolean contains(Task t) {
        return mTaskIndices.containsKey(t.key);
    }

    /** Updates the list of filtered tasks whenever the base task list changes */
    private void updateFilteredTasks() {
        mFilteredTasks.clear();
        if (mFilter != null) {
            // Create a sparse array from task id to Task
            SparseArray<Task> taskIdMap = new SparseArray<>();
            int taskCount = mTasks.size();
            for (int i = 0; i < taskCount; i++) {
                Task t = mTasks.get(i);
                taskIdMap.put(t.key.id, t);
            }

            for (int i = 0; i < taskCount; i++) {
                Task t = mTasks.get(i);
                if (mFilter.acceptTask(taskIdMap, t, i)) {
                    mFilteredTasks.add(t);
                }
            }
        } else {
            mFilteredTasks.addAll(mTasks);
        }
        updateFilteredTaskIndices();
    }

    /** Updates the mapping of tasks to indices. */
    private void updateFilteredTaskIndices() {
        int taskCount = mFilteredTasks.size();
        mTaskIndices.clear();
        for (int i = 0; i < taskCount; i++) {
            Task t = mFilteredTasks.get(i);
            mTaskIndices.put(t.key, i);
        }
    }

    /** Returns whether this task list is filtered */
    boolean hasFilter() {
        return (mFilter != null);
    }

    /** Returns the list of filtered tasks */
    ArrayList<Task> getTasks() {
        return mFilteredTasks;
    }
}

/**
 * The task stack contains a list of multiple tasks.
 */
public class TaskStack {

    /** Task stack callbacks */
    public interface TaskStackCallbacks {
        /**
         * Notifies when a new task has been added to the stack.
         */
        void onStackTaskAdded(TaskStack stack, Task newTask);

        /**
         * Notifies when a task has been removed from the stack.
         */
        void onStackTaskRemoved(TaskStack stack, Task removedTask, boolean wasFrontMostTask,
            Task newFrontMostTask);

        /**
         * Notifies when a task has been removed from the history.
         */
        void onHistoryTaskRemoved(TaskStack stack, Task removedTask);
    }

    /**
     * The various possible dock states when dragging and dropping a task.
     */
    public static class DockState implements DropTarget {

        private static final int DOCK_AREA_ALPHA = 192;
        public static final DockState NONE = new DockState(DOCKED_INVALID, -1, 80, null, null, null);
        public static final DockState LEFT = new DockState(DOCKED_LEFT,
                DOCKED_STACK_CREATE_MODE_TOP_OR_LEFT, DOCK_AREA_ALPHA,
                new RectF(0, 0, 0.125f, 1), new RectF(0, 0, 0.125f, 1),
                new RectF(0, 0, 0.5f, 1));
        public static final DockState TOP = new DockState(DOCKED_TOP,
                DOCKED_STACK_CREATE_MODE_TOP_OR_LEFT, DOCK_AREA_ALPHA,
                new RectF(0, 0, 1, 0.125f), new RectF(0, 0, 1, 0.125f),
                new RectF(0, 0, 1, 0.5f));
        public static final DockState RIGHT = new DockState(DOCKED_RIGHT,
                DOCKED_STACK_CREATE_MODE_BOTTOM_OR_RIGHT, DOCK_AREA_ALPHA,
                new RectF(0.875f, 0, 1, 1), new RectF(0.875f, 0, 1, 1),
                new RectF(0.5f, 0, 1, 1));
        public static final DockState BOTTOM = new DockState(DOCKED_BOTTOM,
                DOCKED_STACK_CREATE_MODE_BOTTOM_OR_RIGHT, DOCK_AREA_ALPHA,
                new RectF(0, 0.875f, 1, 1), new RectF(0, 0.875f, 1, 1),
                new RectF(0, 0.5f, 1, 1));

        @Override
        public boolean acceptsDrop(int x, int y, int width, int height, boolean isCurrentTarget) {
            return isCurrentTarget
                    ? areaContainsPoint(expandedTouchDockArea, width, height, x, y)
                    : areaContainsPoint(touchArea, width, height, x, y);
        }

        // Represents the view state of this dock state
        public class ViewState {
            public final int dockAreaAlpha;
            public final ColorDrawable dockAreaOverlay;
            private AnimatorSet dockAreaOverlayAnimator;

            private ViewState(int alpha) {
                dockAreaAlpha = alpha;
                dockAreaOverlay = new ColorDrawable(0xFFffffff);
                dockAreaOverlay.setAlpha(0);
            }

            /**
             * Creates a new bounds and alpha animation.
             */
            public void startAnimation(Rect bounds, int alpha, int duration,
                    Interpolator interpolator, boolean animateAlpha, boolean animateBounds) {
                if (dockAreaOverlayAnimator != null) {
                    dockAreaOverlayAnimator.cancel();
                }

                ArrayList<Animator> animators = new ArrayList<>();
                if (dockAreaOverlay.getAlpha() != alpha) {
                    if (animateAlpha) {
                        animators.add(ObjectAnimator.ofInt(dockAreaOverlay,
                                Utilities.DRAWABLE_ALPHA, dockAreaOverlay.getAlpha(), alpha));
                    } else {
                        dockAreaOverlay.setAlpha(alpha);
                    }
                }
                if (bounds != null && !dockAreaOverlay.getBounds().equals(bounds)) {
                    if (animateBounds) {
                        PropertyValuesHolder prop = PropertyValuesHolder.ofObject(
                                Utilities.DRAWABLE_RECT, new RectEvaluator(new Rect()),
                                dockAreaOverlay.getBounds(), bounds);
                        animators.add(ObjectAnimator.ofPropertyValuesHolder(dockAreaOverlay, prop));
                    } else {
                        dockAreaOverlay.setBounds(bounds);
                    }
                }
                if (!animators.isEmpty()) {
                    dockAreaOverlayAnimator = new AnimatorSet();
                    dockAreaOverlayAnimator.playTogether(animators);
                    dockAreaOverlayAnimator.setDuration(duration);
                    dockAreaOverlayAnimator.setInterpolator(interpolator);
                    dockAreaOverlayAnimator.start();
                }
            }
        }

        public final int dockSide;
        public final int createMode;
        public final ViewState viewState;
        private final RectF touchArea;
        private final RectF dockArea;
        private final RectF expandedTouchDockArea;

        /**
         * @param createMode used to pass to ActivityManager to dock the task
         * @param touchArea the area in which touch will initiate this dock state
         * @param dockArea the visible dock area
         * @param expandedTouchDockArea the areain which touch will continue to dock after entering
         *                              the initial touch area.  This is also the new dock area to
         *                              draw.
         */
        DockState(int dockSide, int createMode, int dockAreaAlpha, RectF touchArea, RectF dockArea,
                  RectF expandedTouchDockArea) {
            this.dockSide = dockSide;
            this.createMode = createMode;
            this.viewState = new ViewState(dockAreaAlpha);
            this.dockArea = dockArea;
            this.touchArea = touchArea;
            this.expandedTouchDockArea = expandedTouchDockArea;
        }

        /**
         * Returns whether {@param x} and {@param y} are contained in the area scaled to the
         * given {@param width} and {@param height}.
         */
        public boolean areaContainsPoint(RectF area, int width, int height, float x, float y) {
            int left = (int) (area.left * width);
            int top = (int) (area.top * height);
            int right = (int) (area.right * width);
            int bottom = (int) (area.bottom * height);
            return x >= left && y >= top && x <= right && y <= bottom;
        }

        /**
         * Returns the docked task bounds with the given {@param width} and {@param height}.
         */
        public Rect getPreDockedBounds(int width, int height) {
            return new Rect((int) (dockArea.left * width), (int) (dockArea.top * height),
                    (int) (dockArea.right * width), (int) (dockArea.bottom * height));
        }

        /**
         * Returns the expanded docked task bounds with the given {@param width} and
         * {@param height}.
         */
        public Rect getDockedBounds(int width, int height, int dividerSize, Rect insets,
                Resources res) {
            // Calculate the docked task bounds
            boolean isHorizontalDivision =
                    res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            int position = DockedDividerUtils.calculateMiddlePosition(isHorizontalDivision,
                    insets, width, height, dividerSize);
            Rect newWindowBounds = new Rect();
            DockedDividerUtils.calculateBoundsForPosition(position, dockSide, newWindowBounds,
                    width, height, dividerSize);
            return newWindowBounds;
        }

        /**
         * Returns the task stack bounds with the given {@param width} and
         * {@param height}.
         */
        public Rect getDockedTaskStackBounds(int width, int height, int dividerSize, Rect insets,
                Resources res) {
            RecentsConfiguration config = Recents.getConfiguration();

            // Calculate the inverse docked task bounds
            boolean isHorizontalDivision =
                    res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            int position = DockedDividerUtils.calculateMiddlePosition(isHorizontalDivision,
                    insets, width, height, dividerSize);
            Rect newWindowBounds = new Rect();
            DockedDividerUtils.calculateBoundsForPosition(position,
                    DockedDividerUtils.invertDockSide(dockSide), newWindowBounds, width, height,
                    dividerSize);

            // Calculate the task stack bounds from the new window bounds
            Rect searchBarSpaceBounds = new Rect();
            Rect taskStackBounds = new Rect();
            config.getTaskStackBounds(newWindowBounds, insets.top, insets.right,
                    searchBarSpaceBounds, taskStackBounds);
            return taskStackBounds;
        }
    }

    // A comparator that sorts tasks by their last active time
    private Comparator<Task> LAST_ACTIVE_TIME_COMPARATOR = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            return Long.compare(o1.key.lastActiveTime, o2.key.lastActiveTime);
        }
    };

    // A comparator that sorts tasks by their last active time and freeform state
    private Comparator<Task> FREEFORM_LAST_ACTIVE_TIME_COMPARATOR = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            if (o1.isFreeformTask() && !o2.isFreeformTask()) {
                return 1;
            } else if (o2.isFreeformTask() && !o1.isFreeformTask()) {
                return -1;
            }
            return Long.compare(o1.key.lastActiveTime, o2.key.lastActiveTime);
        }
    };


    // The task offset to apply to a task id as a group affiliation
    static final int IndividualTaskIdOffset = 1 << 16;

    ArrayList<Task> mRawTaskList = new ArrayList<>();
    FilteredTaskList mStackTaskList = new FilteredTaskList();
    FilteredTaskList mHistoryTaskList = new FilteredTaskList();
    TaskStackCallbacks mCb;

    ArrayList<TaskGrouping> mGroups = new ArrayList<>();
    ArrayMap<Integer, TaskGrouping> mAffinitiesGroups = new ArrayMap<>();

    public TaskStack() {
        // Ensure that we only show non-docked tasks
        mStackTaskList.setFilter(new TaskFilter() {
            @Override
            public boolean acceptTask(SparseArray<Task> taskIdMap, Task t, int index) {
                if (t.isAffiliatedTask()) {
                    // If this task is affiliated with another parent in the stack, then the
                    // historical state of this task depends on the state of the parent task
                    Task parentTask = taskIdMap.get(t.affiliationTaskId);
                    if (parentTask != null) {
                        t = parentTask;
                    }
                }
                return !t.isHistorical;
            }
        });
        mHistoryTaskList.setFilter(new TaskFilter() {
            @Override
            public boolean acceptTask(SparseArray<Task> taskIdMap, Task t, int index) {
                if (t.isAffiliatedTask()) {
                    // If this task is affiliated with another parent in the stack, then the
                    // historical state of this task depends on the state of the parent task
                    Task parentTask = taskIdMap.get(t.affiliationTaskId);
                    if (parentTask != null) {
                        t = parentTask;
                    }
                }
                return t.isHistorical;
            }
        });
    }

    /** Sets the callbacks for this task stack. */
    public void setCallbacks(TaskStackCallbacks cb) {
        mCb = cb;
    }

    /** Resets this TaskStack. */
    public void reset() {
        mCb = null;
        mStackTaskList.reset();
        mHistoryTaskList.reset();
        mGroups.clear();
        mAffinitiesGroups.clear();
    }

    /**
     * Moves the given task to either the front of the freeform workspace or the stack.
     */
    public void moveTaskToStack(Task task, int newStackId) {
        // Find the index to insert into
        ArrayList<Task> taskList = mStackTaskList.getTasks();
        int taskCount = taskList.size();
        if (!task.isFreeformTask() && (newStackId == FREEFORM_WORKSPACE_STACK_ID)) {
            // Insert freeform tasks at the front
            mStackTaskList.moveTaskToStack(task, taskCount, newStackId);
        } else if (task.isFreeformTask() && (newStackId == FULLSCREEN_WORKSPACE_STACK_ID)) {
            // Insert after the first stacked task
            int insertIndex = 0;
            for (int i = taskCount - 1; i >= 0; i--) {
                if (!taskList.get(i).isFreeformTask()) {
                    insertIndex = i + 1;
                    break;
                }
            }
            mStackTaskList.moveTaskToStack(task, insertIndex, newStackId);
        }
    }

    /** Does the actual work associated with removing the task. */
    void removeTaskImpl(FilteredTaskList taskList, Task t) {
        // Remove the task from the list
        taskList.remove(t);
        // Remove it from the group as well, and if it is empty, remove the group
        TaskGrouping group = t.group;
        if (group != null) {
            group.removeTask(t);
            if (group.getTaskCount() == 0) {
                removeGroup(group);
            }
        }
    }

    /** Removes a task */
    public void removeTask(Task t) {
        if (mStackTaskList.contains(t)) {
            boolean wasFrontMostTask = (getStackFrontMostTask() == t);
            removeTaskImpl(mStackTaskList, t);
            Task newFrontMostTask = getStackFrontMostTask();
            if (mCb != null) {
                // Notify that a task has been removed
                mCb.onStackTaskRemoved(this, t, wasFrontMostTask, newFrontMostTask);
            }
        } else if (mHistoryTaskList.contains(t)) {
            removeTaskImpl(mHistoryTaskList, t);
            if (mCb != null) {
                // Notify that a task has been removed
                mCb.onHistoryTaskRemoved(this, t);
            }
        }
        mRawTaskList.remove(t);
    }

    /**
     * Sets a few tasks in one go, without calling any callbacks.
     *
     * @param tasks the new set of tasks to replace the current set.
     * @param notifyStackChanges whether or not to callback on specific changes to the list of tasks.
     */
    public void setTasks(List<Task> tasks, boolean notifyStackChanges) {
        // Compute a has set for each of the tasks
        ArrayMap<Task.TaskKey, Task> currentTasksMap = createTaskKeyMapFromList(mRawTaskList);
        ArrayMap<Task.TaskKey, Task> newTasksMap = createTaskKeyMapFromList(tasks);

        ArrayList<Task> newTasks = new ArrayList<>();

        // Disable notifications if there are no callbacks
        if (mCb == null) {
            notifyStackChanges = false;
        }

        // Remove any tasks that no longer exist
        int taskCount = mRawTaskList.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = mRawTaskList.get(i);
            if (!newTasksMap.containsKey(task.key)) {
                if (notifyStackChanges) {
                    mCb.onStackTaskRemoved(this, task, i == (taskCount - 1), null);
                }
            }
            task.setGroup(null);
        }

        // Add any new tasks
        taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            if (!currentTasksMap.containsKey(task.key)) {
                if (notifyStackChanges) {
                    mCb.onStackTaskAdded(this, task);
                }
                newTasks.add(task);
            } else {
                newTasks.add(currentTasksMap.get(task.key));
            }
        }

        // Sort all the tasks to ensure they are ordered correctly
        Collections.sort(newTasks, FREEFORM_LAST_ACTIVE_TIME_COMPARATOR);

        // Filter out the historical tasks from this new list
        ArrayList<Task> stackTasks = new ArrayList<>();
        ArrayList<Task> historyTasks = new ArrayList<>();
        int newTaskCount = newTasks.size();
        for (int i = 0; i < newTaskCount; i++) {
            Task task = newTasks.get(i);
            if (task.isHistorical) {
                historyTasks.add(task);
            } else {
                stackTasks.add(task);
            }
        }

        mStackTaskList.set(stackTasks);
        mHistoryTaskList.set(historyTasks);
        mRawTaskList.clear();
        mRawTaskList.addAll(newTasks);
        mGroups.clear();
        mAffinitiesGroups.clear();
    }

    /**
     * Gets the front-most task in the stack.
     */
    public Task getStackFrontMostTask() {
        ArrayList<Task> stackTasks = mStackTaskList.getTasks();
        if (stackTasks.isEmpty()) {
            return null;
        }
        for (int i = stackTasks.size() - 1; i >= 0; i--) {
            Task task = stackTasks.get(i);
            if (!task.isFreeformTask()) {
                return task;
            }
        }
        return null;
    }

    /** Gets the task keys */
    public ArrayList<Task.TaskKey> getTaskKeys() {
        ArrayList<Task.TaskKey> taskKeys = new ArrayList<>();
        ArrayList<Task> tasks = computeAllTasksList();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            taskKeys.add(task.key);
        }
        return taskKeys;
    }

    /**
     * Returns the set of "active" (non-historical) tasks in the stack that have been used recently.
     */
    public ArrayList<Task> getStackTasks() {
        return mStackTaskList.getTasks();
    }

    /**
     * Returns the set of tasks that are inactive. These tasks will be presented in a separate
     * history view.
     */
    public ArrayList<Task> getHistoricalTasks() {
        return mHistoryTaskList.getTasks();
    }

    /**
     * Returns the set of "freeform" tasks in the stack.
     */
    public ArrayList<Task> getFreeformTasks() {
        ArrayList<Task> freeformTasks = new ArrayList<>();
        ArrayList<Task> tasks = mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            if (task.isFreeformTask()) {
                freeformTasks.add(task);
            }
        }
        return freeformTasks;
    }

    /**
     * Computes a set of all the active and historical tasks ordered by their last active time.
     */
    public ArrayList<Task> computeAllTasksList() {
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.addAll(mStackTaskList.getTasks());
        tasks.addAll(mHistoryTaskList.getTasks());
        Collections.sort(tasks, LAST_ACTIVE_TIME_COMPARATOR);
        return tasks;
    }

    /**
     * Returns the number of stack and freeform tasks.
     */
    public int getTaskCount() {
        return mStackTaskList.size();
    }

    /**
     * Returns the number of stack tasks.
     */
    public int getStackTaskCount() {
        ArrayList<Task> tasks = mStackTaskList.getTasks();
        int stackCount = 0;
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            if (!task.isFreeformTask()) {
                stackCount++;
            }
        }
        return stackCount;
    }

    /**
     * Returns the number of freeform tasks.
     */
    public int getFreeformTaskCount() {
        ArrayList<Task> tasks = mStackTaskList.getTasks();
        int freeformCount = 0;
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            if (task.isFreeformTask()) {
                freeformCount++;
            }
        }
        return freeformCount;
    }

    /**
     * Returns the task in stack tasks which is the launch target.
     */
    public Task getLaunchTarget() {
        ArrayList<Task> tasks = mStackTaskList.getTasks();
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            if (task.isLaunchTarget) {
                return task;
            }
        }
        return null;
    }

    /** Returns the index of this task in this current task stack */
    public int indexOfStackTask(Task t) {
        return mStackTaskList.indexOf(t);
    }

    /** Finds the task with the specified task id. */
    public Task findTaskWithId(int taskId) {
        ArrayList<Task> tasks = computeAllTasksList();
        for (Task task : tasks) {
            if (task.key.id == taskId) {
                return task;
            }
        }
        return null;
    }

    /******** Grouping ********/

    /** Adds a group to the set */
    public void addGroup(TaskGrouping group) {
        mGroups.add(group);
        mAffinitiesGroups.put(group.affiliation, group);
    }

    public void removeGroup(TaskGrouping group) {
        mGroups.remove(group);
        mAffinitiesGroups.remove(group.affiliation);
    }

    /** Returns the group with the specified affiliation. */
    public TaskGrouping getGroupWithAffiliation(int affiliation) {
        return mAffinitiesGroups.get(affiliation);
    }

    /**
     * Temporary: This method will simulate affiliation groups by
     */
    public void createAffiliatedGroupings(Context context) {
        if (RecentsDebugFlags.Static.EnableSimulatedTaskGroups) {
            ArrayMap<Task.TaskKey, Task> taskMap = new ArrayMap<>();
            // Sort all tasks by increasing firstActiveTime of the task
            ArrayList<Task> tasks = mStackTaskList.getTasks();
            Collections.sort(tasks, new Comparator<Task>() {
                @Override
                public int compare(Task task, Task task2) {
                    return Long.compare(task.key.firstActiveTime, task2.key.firstActiveTime);
                }
            });
            // Create groups when sequential packages are the same
            NamedCounter counter = new NamedCounter("task-group", "");
            int taskCount = tasks.size();
            String prevPackage = "";
            int prevAffiliation = -1;
            Random r = new Random();
            int groupCountDown = RecentsDebugFlags.Static.TaskAffiliationsGroupCount;
            for (int i = 0; i < taskCount; i++) {
                Task t = tasks.get(i);
                String packageName = t.key.getComponent().getPackageName();
                packageName = "pkg";
                TaskGrouping group;
                if (packageName.equals(prevPackage) && groupCountDown > 0) {
                    group = getGroupWithAffiliation(prevAffiliation);
                    groupCountDown--;
                } else {
                    int affiliation = IndividualTaskIdOffset + t.key.id;
                    group = new TaskGrouping(affiliation);
                    addGroup(group);
                    prevAffiliation = affiliation;
                    prevPackage = packageName;
                    groupCountDown = RecentsDebugFlags.Static.TaskAffiliationsGroupCount;
                }
                group.addTask(t);
                taskMap.put(t.key, t);
            }
            // Sort groups by increasing latestActiveTime of the group
            Collections.sort(mGroups, new Comparator<TaskGrouping>() {
                @Override
                public int compare(TaskGrouping taskGrouping, TaskGrouping taskGrouping2) {
                    return Long.compare(taskGrouping.latestActiveTimeInGroup,
                            taskGrouping2.latestActiveTimeInGroup);
                }
            });
            // Sort group tasks by increasing firstActiveTime of the task, and also build a new list
            // of tasks
            int taskIndex = 0;
            int groupCount = mGroups.size();
            for (int i = 0; i < groupCount; i++) {
                TaskGrouping group = mGroups.get(i);
                Collections.sort(group.mTaskKeys, new Comparator<Task.TaskKey>() {
                    @Override
                    public int compare(Task.TaskKey taskKey, Task.TaskKey taskKey2) {
                        return Long.compare(taskKey.firstActiveTime, taskKey2.firstActiveTime);
                    }
                });
                ArrayList<Task.TaskKey> groupTasks = group.mTaskKeys;
                int groupTaskCount = groupTasks.size();
                for (int j = 0; j < groupTaskCount; j++) {
                    tasks.set(taskIndex, taskMap.get(groupTasks.get(j)));
                    taskIndex++;
                }
            }
            mStackTaskList.set(tasks);
        } else {
            // Create the task groups
            ArrayMap<Task.TaskKey, Task> tasksMap = new ArrayMap<>();
            ArrayList<Task> tasks = mStackTaskList.getTasks();
            int taskCount = tasks.size();
            for (int i = 0; i < taskCount; i++) {
                Task t = tasks.get(i);
                TaskGrouping group;
                int affiliation = t.affiliationTaskId > 0 ? t.affiliationTaskId :
                        IndividualTaskIdOffset + t.key.id;
                if (mAffinitiesGroups.containsKey(affiliation)) {
                    group = getGroupWithAffiliation(affiliation);
                } else {
                    group = new TaskGrouping(affiliation);
                    addGroup(group);
                }
                group.addTask(t);
                tasksMap.put(t.key, t);
            }
            // Update the task colors for each of the groups
            float minAlpha = context.getResources().getFloat(
                    R.dimen.recents_task_affiliation_color_min_alpha_percentage);
            int taskGroupCount = mGroups.size();
            for (int i = 0; i < taskGroupCount; i++) {
                TaskGrouping group = mGroups.get(i);
                taskCount = group.getTaskCount();
                // Ignore the groups that only have one task
                if (taskCount <= 1) continue;
                // Calculate the group color distribution
                int affiliationColor = tasksMap.get(group.mTaskKeys.get(0)).affiliationColor;
                float alphaStep = (1f - minAlpha) / taskCount;
                float alpha = 1f;
                for (int j = 0; j < taskCount; j++) {
                    Task t = tasksMap.get(group.mTaskKeys.get(j));
                    t.colorPrimary = Utilities.getColorWithOverlay(affiliationColor, Color.WHITE,
                            alpha);
                    alpha -= alphaStep;
                }
            }
        }
    }

    /**
     * Computes the components of tasks in this stack that have been removed as a result of a change
     * in the specified package.
     */
    public ArraySet<ComponentName> computeComponentsRemoved(String packageName, int userId) {
        // Identify all the tasks that should be removed as a result of the package being removed.
        // Using a set to ensure that we callback once per unique component.
        SystemServicesProxy ssp = Recents.getSystemServices();
        ArraySet<ComponentName> existingComponents = new ArraySet<>();
        ArraySet<ComponentName> removedComponents = new ArraySet<>();
        ArrayList<Task.TaskKey> taskKeys = getTaskKeys();
        for (Task.TaskKey t : taskKeys) {
            // Skip if this doesn't apply to the current user
            if (t.userId != userId) continue;

            ComponentName cn = t.getComponent();
            if (cn.getPackageName().equals(packageName)) {
                if (existingComponents.contains(cn)) {
                    // If we know that the component still exists in the package, then skip
                    continue;
                }
                if (ssp.getActivityInfo(cn, userId) != null) {
                    existingComponents.add(cn);
                } else {
                    removedComponents.add(cn);
                }
            }
        }
        return removedComponents;
    }

    @Override
    public String toString() {
        String str = "Stack Tasks:\n";
        for (Task t : mStackTaskList.getTasks()) {
            str += "  " + t.toString() + "\n";
        }
        str += "Historical Tasks:\n";
        for (Task t : mHistoryTaskList.getTasks()) {
            str += "  " + t.toString() + "\n";
        }
        return str;
    }

    /**
     * Given a list of tasks, returns a map of each task's key to the task.
     */
    private ArrayMap<Task.TaskKey, Task> createTaskKeyMapFromList(List<Task> tasks) {
        ArrayMap<Task.TaskKey, Task> map = new ArrayMap<>(tasks.size());
        int taskCount = tasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks.get(i);
            map.put(task.key, task);
        }
        return map;
    }
}