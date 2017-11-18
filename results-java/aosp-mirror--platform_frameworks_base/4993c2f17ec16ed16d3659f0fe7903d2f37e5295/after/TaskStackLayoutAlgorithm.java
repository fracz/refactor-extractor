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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivityLaunchState;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.misc.FreePathInterpolator;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Used to describe a visible range that can be normalized to [0, 1].
 */
class Range {
    final float relativeMin;
    final float relativeMax;
    float origin;
    float min;
    float max;

    public Range(float relMin, float relMax) {
        min = relativeMin = relMin;
        max = relativeMax = relMax;
    }

    /**
     * Offsets this range to a given absolute position.
     */
    public void offset(float x) {
        this.origin = x;
        min = x + relativeMin;
        max = x + relativeMax;
    }

    /**
     * Returns x normalized to the range 0 to 1 such that 0 = min, 0.5 = origin and 1 = max
     *
     * @param x is an absolute value in the same domain as origin
     */
    public float getNormalizedX(float x) {
        if (x < origin) {
            return 0.5f + 0.5f * (x - origin) / -relativeMin;
        } else {
            return 0.5f + 0.5f * (x - origin) / relativeMax;
        }
    }

    /**
     * Given a normalized {@param x} value in this range, projected onto the full range to get an
     * absolute value about the given {@param origin}.
     */
    public float getAbsoluteX(float normX) {
        if (normX < 0.5f) {
            return (normX - 0.5f) / 0.5f * -relativeMin;
        } else {
            return (normX - 0.5f) / 0.5f * relativeMax;
        }
    }

    /**
     * Returns whether a value at an absolute x would be within range.
     */
    public boolean isInRange(float absX) {
        return (absX >= Math.floor(min)) && (absX <= Math.ceil(max));
    }
}

/**
 * The layout logic for a TaskStackView.  This layout can have two states focused and unfocused,
 * and in the focused state, there is a task that is displayed more prominently in the stack.
 */
public class TaskStackLayoutAlgorithm {

    private static final String TAG = "TaskStackViewLayoutAlgorithm";
    private static final boolean DEBUG = false;

    // A report of the visibility state of the stack
    public class VisibilityReport {
        public int numVisibleTasks;
        public int numVisibleThumbnails;

        /** Package level ctor */
        VisibilityReport(int tasks, int thumbnails) {
            numVisibleTasks = tasks;
            numVisibleThumbnails = thumbnails;
        }
    }

    Context mContext;

    // The task bounds (untransformed) for layout.  This rect is anchored at mTaskRoot.
    public Rect mTaskRect = new Rect();
    // The freeform workspace bounds, inset from the top by the search bar, and is a fixed height
    public Rect mFreeformRect = new Rect();
    // The freeform stack bounds, inset from the top by the search bar and freeform workspace, and
    // runs to the bottom of the screen
    private Rect mFreeformStackRect = new Rect();
    // The stack bounds, inset from the top by the search bar, and runs to
    // the bottom of the screen
    private Rect mStackRect = new Rect();
    // The current stack rect, can either by mFreeformStackRect or mStackRect depending on whether
    // there is a freeform workspace
    public Rect mCurrentStackRect = new Rect();
    // This is the current system insets
    public Rect mSystemInsets = new Rect();

    // The visible ranges when the stack is focused and unfocused
    private Range mUnfocusedRange;
    private Range mFocusedRange;

    // The offset from the top when scrolled to the top of the stack
    private int mFocusedPeekHeight;

    // The offset from the bottom of the stack to the bottom of the bounds
    private int mStackBottomOffset;

    // The paths defining the motion of the tasks when the stack is focused and unfocused
    private Path mUnfocusedCurve;
    private Path mFocusedCurve;
    private FreePathInterpolator mUnfocusedCurveInterpolator;
    private FreePathInterpolator mFocusedCurveInterpolator;

    // The state of the stack focus (0..1), which controls the transition of the stack from the
    // focused to non-focused state
    private float mFocusState;

    // The smallest scroll progress, at this value, the back most task will be visible
    float mMinScrollP;
    // The largest scroll progress, at this value, the front most task will be visible above the
    // navigation bar
    float mMaxScrollP;
    // The initial progress that the scroller is set when you first enter recents
    float mInitialScrollP;
    // The task progress for the front-most task in the stack
    float mFrontMostTaskP;

    // The last computed task counts
    int mNumStackTasks;
    int mNumFreeformTasks;

    // The min/max z translations
    int mMinTranslationZ;
    int mMaxTranslationZ;

    // Optimization, allows for quick lookup of task -> index
    private HashMap<Task.TaskKey, Integer> mTaskIndexMap = new HashMap<>();

    // The freeform workspace layout
    FreeformWorkspaceLayoutAlgorithm mFreeformLayoutAlgorithm;

    public TaskStackLayoutAlgorithm(Context context) {
        Resources res = context.getResources();

        mFocusedRange = new Range(res.getFloat(R.integer.recents_layout_focused_range_min),
                res.getFloat(R.integer.recents_layout_focused_range_max));
        mUnfocusedRange = new Range(res.getFloat(R.integer.recents_layout_unfocused_range_min),
                res.getFloat(R.integer.recents_layout_unfocused_range_max));
        mFocusedPeekHeight = res.getDimensionPixelSize(R.dimen.recents_layout_focused_peek_size);

        mMinTranslationZ = res.getDimensionPixelSize(R.dimen.recents_task_view_z_min);
        mMaxTranslationZ = res.getDimensionPixelSize(R.dimen.recents_task_view_z_max);
        mContext = context;
        mFreeformLayoutAlgorithm = new FreeformWorkspaceLayoutAlgorithm();
    }

    /**
     * Sets the system insets.
     */
    public void setSystemInsets(Rect systemInsets) {
        mSystemInsets.set(systemInsets);
        if (DEBUG) {
            Log.d(TAG, "setSystemInsets: " + systemInsets);
        }
    }

    /**
     * Sets the focused state.
     */
    public void setFocusState(float focusState) {
        mFocusState = focusState;
    }

    /**
     * Gets the focused state.
     */
    public float getFocusState() {
        return mFocusState;
    }

    /**
     * Computes the stack and task rects.  The given task stack bounds is the whole bounds not
     * including the search bar.
     */
    public void initialize(Rect taskStackBounds) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        RecentsConfiguration config = Recents.getConfiguration();
        int widthPadding = (int) (config.taskStackWidthPaddingPct * taskStackBounds.width());
        int heightPadding = mContext.getResources().getDimensionPixelSize(
                R.dimen.recents_stack_top_padding);
        Rect lastStackRect = new Rect(mCurrentStackRect);

        // The freeform height is the visible height (not including system insets) - padding above
        // freeform and below stack - gap between the freeform and stack
        mStackBottomOffset = mSystemInsets.bottom + heightPadding;
        int ffHeight = (taskStackBounds.height() - 2 * heightPadding - mStackBottomOffset) / 2;
        mFreeformRect.set(taskStackBounds.left + widthPadding,
                taskStackBounds.top + heightPadding,
                taskStackBounds.right - widthPadding,
                taskStackBounds.top + heightPadding + ffHeight);
        mFreeformStackRect.set(taskStackBounds.left + widthPadding,
                taskStackBounds.top + heightPadding + ffHeight + heightPadding,
                taskStackBounds.right - widthPadding,
                taskStackBounds.bottom);
        mStackRect.set(taskStackBounds.left + widthPadding,
                taskStackBounds.top + heightPadding,
                taskStackBounds.right - widthPadding,
                taskStackBounds.bottom);
        // Anchor the task rect to the top-center of the non-freeform stack rect
        int size = mStackRect.width();
        mTaskRect.set(mStackRect.left, mStackRect.top,
                mStackRect.left + size, mStackRect.top + size);
        mCurrentStackRect = ssp.hasFreeformWorkspaceSupport() ? mFreeformStackRect : mStackRect;

        // Short circuit here if the stack rects haven't changed so we don't do all the work below
        if (lastStackRect.equals(mCurrentStackRect)) {
            return;
        }

        // Reinitialize the focused and unfocused curves
        mUnfocusedCurve = constructUnfocusedCurve();
        mUnfocusedCurveInterpolator = new FreePathInterpolator(mUnfocusedCurve);
        mFocusedCurve = constructFocusedCurve();
        mFocusedCurveInterpolator = new FreePathInterpolator(mFocusedCurve);

        if (DEBUG) {
            Log.d(TAG, "initialize");
            Log.d(TAG, "\tmFreeformRect: " + mFreeformRect);
            Log.d(TAG, "\tmFreeformStackRect: " + mFreeformStackRect);
            Log.d(TAG, "\tmStackRect: " + mStackRect);
            Log.d(TAG, "\tmTaskRect: " + mTaskRect);
            Log.d(TAG, "\tmSystemInsets: " + mSystemInsets);
        }
    }

    /**
     * Computes the minimum and maximum scroll progress values and the progress values for each task
     * in the stack.
     */
    void update(TaskStack stack) {
        SystemServicesProxy ssp = Recents.getSystemServices();

        // Clear the progress map
        mTaskIndexMap.clear();

        // Return early if we have no tasks
        ArrayList<Task> tasks = stack.getTasks();
        if (tasks.isEmpty()) {
            mFrontMostTaskP = 0;
            mMinScrollP = mMaxScrollP = 0;
            mNumStackTasks = mNumFreeformTasks = 0;
            return;
        }

        // Filter the set of freeform and stack tasks
        ArrayList<Task> freeformTasks = new ArrayList<>();
        ArrayList<Task> stackTasks = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isFreeformTask()) {
                freeformTasks.add(task);
            } else {
                stackTasks.add(task);
            }
        }
        mNumStackTasks = stackTasks.size();
        mNumFreeformTasks = freeformTasks.size();

        // Put each of the tasks in the progress map at a fixed index (does not need to actually
        // map to a scroll position, just by index)
        int taskCount = stackTasks.size();
        for (int i = 0; i < taskCount; i++) {
            Task task = stackTasks.get(i);
            mTaskIndexMap.put(task.key, i);
        }

        // Calculate the min/max scroll
        if (getDefaultFocusScroll() > 0f) {
            mMinScrollP = 0;
            mMaxScrollP = Math.max(mMinScrollP, mNumStackTasks - 1);
        } else {
            if (!ssp.hasFreeformWorkspaceSupport() && mNumStackTasks == 1) {
                mMinScrollP = mMaxScrollP = 0;
            } else {
                float bottomOffsetPct = (float) (mStackBottomOffset + mTaskRect.height()) /
                        mCurrentStackRect.height();
                float normX = mUnfocusedCurveInterpolator.getX(bottomOffsetPct);
                mMinScrollP = 0;
                mMaxScrollP = Math.max(mMinScrollP,
                        (mNumStackTasks - 1) - mUnfocusedRange.getAbsoluteX(normX));
            }
        }

        if (!freeformTasks.isEmpty()) {
            mFreeformLayoutAlgorithm.update(freeformTasks, this);
            mInitialScrollP = mMaxScrollP;
        } else {
            if (!ssp.hasFreeformWorkspaceSupport() && mNumStackTasks == 1) {
                mInitialScrollP = mMinScrollP;
            } else if (getDefaultFocusScroll() > 0f) {
                RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
                if (launchState.launchedFromHome) {
                    mInitialScrollP = Math.max(mMinScrollP, mNumStackTasks - 1);
                } else {
                    mInitialScrollP = Math.max(mMinScrollP, mNumStackTasks - 2);
                }
            } else {
                float offsetPct = (float) (mTaskRect.height() / 2) / mCurrentStackRect.height();
                float normX = mUnfocusedCurveInterpolator.getX(offsetPct);
                mInitialScrollP = (mNumStackTasks - 1) - mUnfocusedRange.getAbsoluteX(normX);
            }
        }

        if (DEBUG) {
            Log.d(TAG, "mNumStackTasks: " + mNumStackTasks);
            Log.d(TAG, "mNumFreeformTasks: " + mNumFreeformTasks);
            Log.d(TAG, "mMinScrollP: " + mMinScrollP);
            Log.d(TAG, "mMaxScrollP: " + mMaxScrollP);
        }
    }

    /**
     * Computes the maximum number of visible tasks and thumbnails when the scroll is at the initial
     * stack scroll.  Requires that update() is called first.
     */
    public VisibilityReport computeStackVisibilityReport(ArrayList<Task> tasks) {
        // Ensure minimum visibility count
        if (tasks.size() <= 1) {
            return new VisibilityReport(1, 1);
        }

        // Quick return when there are no stack tasks
        if (mNumStackTasks == 0) {
            return new VisibilityReport(Math.max(mNumFreeformTasks, 1),
                    Math.max(mNumFreeformTasks, 1));
        }

        // Otherwise, walk backwards in the stack and count the number of tasks and visible
        // thumbnails and add that to the total freeform task count
        TaskViewTransform tmpTransform = new TaskViewTransform();
        Range currentRange = getDefaultFocusScroll() > 0f ? mFocusedRange : mUnfocusedRange;
        currentRange.offset(mInitialScrollP);
        int taskBarHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.recents_task_bar_height);
        int numVisibleTasks = Math.max(mNumFreeformTasks, 1);
        int numVisibleThumbnails = Math.max(mNumFreeformTasks, 1);
        float prevScreenY = Integer.MAX_VALUE;
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task task = tasks.get(i);

            // Skip freeform
            if (task.isFreeformTask()) {
                continue;
            }

            // Skip invisible
            float taskProgress = getStackScrollForTask(task);
            if (!currentRange.isInRange(taskProgress)) {
                continue;
            }

            boolean isFrontMostTaskInGroup = task.group == null || task.group.isFrontMostTask(task);
            if (isFrontMostTaskInGroup) {
                getStackTransform(taskProgress, mInitialScrollP, tmpTransform, null);
                float screenY = tmpTransform.rect.top;
                boolean hasVisibleThumbnail = (prevScreenY - screenY) > taskBarHeight;
                if (hasVisibleThumbnail) {
                    numVisibleThumbnails++;
                    numVisibleTasks++;
                    prevScreenY = screenY;
                } else {
                    // Once we hit the next front most task that does not have a visible thumbnail,
                    // walk through remaining visible set
                    for (int j = i; j >= 0; j--) {
                        numVisibleTasks++;
                        taskProgress = getStackScrollForTask(tasks.get(j));
                        if (!currentRange.isInRange(taskProgress)) {
                            continue;
                        }
                    }
                    break;
                }
            } else if (!isFrontMostTaskInGroup) {
                // Affiliated task, no thumbnail
                numVisibleTasks++;
            }
        }
        return new VisibilityReport(numVisibleTasks, numVisibleThumbnails);
    }

    /**
     * Returns the transform for the given task.  This transform is relative to the mTaskRect, which
     * is what the view is measured and laid out with.
     */
    public TaskViewTransform getStackTransform(Task task, float stackScroll,
            TaskViewTransform transformOut, TaskViewTransform frontTransform) {
        if (mFreeformLayoutAlgorithm.isTransformAvailable(task, this)) {
            mFreeformLayoutAlgorithm.getTransform(task, transformOut, this);
            return transformOut;
        } else {
            // Return early if we have an invalid index
            if (task == null || !mTaskIndexMap.containsKey(task.key)) {
                transformOut.reset();
                return transformOut;
            }
            return getStackTransform(mTaskIndexMap.get(task.key), stackScroll, transformOut,
                    frontTransform);
        }
    }

    /** Update/get the transform */
    public TaskViewTransform getStackTransform(float taskProgress, float stackScroll,
            TaskViewTransform transformOut, TaskViewTransform frontTransform) {
        SystemServicesProxy ssp = Recents.getSystemServices();

        // Compute the focused and unfocused offset
        mUnfocusedRange.offset(stackScroll);
        float p = mUnfocusedRange.getNormalizedX(taskProgress);
        float yp = mUnfocusedCurveInterpolator.getInterpolation(p);
        float unfocusedP = p;
        int unFocusedY = (int) (Math.max(0f, (1f - yp)) * mCurrentStackRect.height());
        int focusedY = 0;
        if (mFocusState > 0f) {
            mFocusedRange.offset(stackScroll);
            p = mFocusedRange.getNormalizedX(taskProgress);
            yp = mFocusedCurveInterpolator.getInterpolation(p);
            focusedY = (int) (Math.max(0f, (1f - yp)) * mCurrentStackRect.height());
        }

        // Skip calculating this if it is identical to the task in front of it
        if (frontTransform != null) {
            if (Float.compare(p, frontTransform.p) == 0) {
                transformOut.reset();
                return transformOut;
            }
        }

        if (!ssp.hasFreeformWorkspaceSupport() && mNumStackTasks == 1) {
            // When there is exactly one task, then decouple the task from the stack and just move
            // in screen space
            p = (mMinScrollP - stackScroll) / mNumStackTasks;
            int centerYOffset = (mCurrentStackRect.top - mTaskRect.top) +
                    (mCurrentStackRect.height() - mTaskRect.height()) / 2;
            transformOut.translationY = (int) (centerYOffset +
                    (p * mCurrentStackRect.height()));
            transformOut.translationZ = mMaxTranslationZ;
            transformOut.rect.set(mTaskRect);
            transformOut.p = p;

        } else {
            // Otherwise, update the task to the stack layout
            int y = unFocusedY + (int) (mFocusState * (focusedY - unFocusedY));
            transformOut.translationY = (mCurrentStackRect.top - mTaskRect.top) + y;
            transformOut.translationZ = Math.max(mMinTranslationZ, Math.min(mMaxTranslationZ,
                    mMinTranslationZ + (p * (mMaxTranslationZ - mMinTranslationZ))));
            transformOut.rect.set(mTaskRect);
            transformOut.p = unfocusedP;
        }

        transformOut.scale = 1f;
        transformOut.translationX = (mCurrentStackRect.width() - mTaskRect.width()) / 2;
        transformOut.rect.offset(transformOut.translationX, transformOut.translationY);
        Utilities.scaleRectAboutCenter(transformOut.rect, transformOut.scale);
        transformOut.visible = true;
        return transformOut;
    }

    /**
     * Returns the untransformed task view bounds.
     */
    public Rect getUntransformedTaskViewBounds() {
        return new Rect(mTaskRect);
    }

    /**
     * Returns the scroll progress to scroll to such that the top of the task is at the top of the
     * stack.
     */
    float getStackScrollForTask(Task t) {
        if (!mTaskIndexMap.containsKey(t.key)) return 0f;
        return mTaskIndexMap.get(t.key);
    }

    /**
     * Maps a movement in screen y, relative to {@param downY}, to a movement in along the arc
     * length of the curve.  We know the curve is mostly flat, so we just map the length of the
     * screen along the arc-length proportionally (1/arclength).
     */
    public float getDeltaPForY(int downY, int y) {
        float deltaP = (float) (y - downY) / mCurrentStackRect.height() *
                mUnfocusedCurveInterpolator.getArcLength();
        return -deltaP;
    }

    /**
     * This is the inverse of {@link #getDeltaPForY}.  Given a movement along the arc length
     * of the curve, map back to the screen y.
     */
    public int getYForDeltaP(float downScrollP, float p) {
        int y = (int) ((p - downScrollP) * mCurrentStackRect.height() *
                (1f / mUnfocusedCurveInterpolator.getArcLength()));
        return -y;
    }

    /**
     * Returns the default focus state.
     */
    private float getDefaultFocusScroll() {
        return 0f;
    }

    /**
     * Creates a new path for the focused curve.
     */
    private Path constructFocusedCurve() {
        SystemServicesProxy ssp = Recents.getSystemServices();

        // Initialize the focused curve. This curve is a piecewise curve composed of several
        // quadradic beziers that goes from (0,1) through (0.5, peek height offset),
        // (0.667, next task offset), (0.833, bottom task offset), and (1,0).
        float peekHeightPct = 0f;
        if (!ssp.hasFreeformWorkspaceSupport()) {
            peekHeightPct = (float) mFocusedPeekHeight / mCurrentStackRect.height();
        }
        Path p = new Path();
        p.moveTo(0f, 1f);
        p.lineTo(0.5f, 1f - peekHeightPct);
        p.lineTo(0.66666667f, 0.25f);
        p.lineTo(0.83333333f, 0.1f);
        p.lineTo(1f, 0f);
        return p;
    }

    /**
     * Creates a new path for the unfocused curve.
     */
    private Path constructUnfocusedCurve() {
        SystemServicesProxy ssp = Recents.getSystemServices();

        // Initialize the unfocused curve. This curve is a piecewise curve composed of two quadradic
        // beziers that goes from (0,1) through (0.5, peek height offset) and ends at (1,0).  This
        // ensures that we match the range, at which 0.5 represents the stack scroll at the current
        // task progress.  Because the height offset can change depending on a resource, we compute
        // the control point of the second bezier such that between it and a first known point,
        // there is a tangent at (0.5, peek height offset).
        float cpoint1X = 0.4f;
        float cpoint1Y = 1f;
        float peekHeightPct = 0f;
        if (!ssp.hasFreeformWorkspaceSupport()) {
            peekHeightPct = (float) mFocusedPeekHeight / mCurrentStackRect.height();
        }
        float slope = ((1f - peekHeightPct) - cpoint1Y) / (0.5f - cpoint1X);
        float b = 1f - slope * cpoint1X;
        float cpoint2X = 0.75f;
        float cpoint2Y = slope * cpoint2X + b;
        Path p = new Path();
        p.moveTo(0f, 1f);
        p.cubicTo(0f, 1f, cpoint1X, 1f, 0.5f, 1f - peekHeightPct);
        p.cubicTo(0.5f, 1f - peekHeightPct, cpoint2X, cpoint2Y, 1f, 0f);
        return p;
    }
}