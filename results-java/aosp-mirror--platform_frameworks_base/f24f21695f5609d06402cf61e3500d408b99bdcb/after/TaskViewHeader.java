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
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.recents.Constants;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;

import static android.app.ActivityManager.StackId.FREEFORM_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.FULLSCREEN_WORKSPACE_STACK_ID;
import static android.app.ActivityManager.StackId.INVALID_STACK_ID;


/* The task bar view */
public class TaskViewHeader extends FrameLayout
        implements View.OnClickListener, View.OnLongClickListener {

    Task mTask;

    // Header views
    ImageView mMoveTaskButton;
    ImageView mDismissButton;
    ImageView mIconView;
    TextView mTitleView;
    int mMoveTaskTargetStackId = INVALID_STACK_ID;

    // Header drawables
    Rect mTaskViewRect = new Rect();
    int mCornerRadius;
    int mHighlightHeight;
    Drawable mLightDismissDrawable;
    Drawable mDarkDismissDrawable;
    RippleDrawable mBackground;
    GradientDrawable mBackgroundColorDrawable;
    String mDismissContentDescription;

    // Static highlight that we draw at the top of each view
    static Paint sHighlightPaint;

    // Header dim, which is only used when task view hardware layers are not used
    Paint mDimLayerPaint = new Paint();

    Interpolator mFastOutSlowInInterpolator;
    Interpolator mFastOutLinearInInterpolator;

    public TaskViewHeader(Context context) {
        this(context, null);
    }

    public TaskViewHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskViewHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TaskViewHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);

        // Load the dismiss resources
        mDimLayerPaint.setColor(Color.argb(0, 0, 0, 0));
        mLightDismissDrawable = context.getDrawable(R.drawable.recents_dismiss_light);
        mDarkDismissDrawable = context.getDrawable(R.drawable.recents_dismiss_dark);
        mDismissContentDescription =
                context.getString(R.string.accessibility_recents_item_will_be_dismissed);
        mCornerRadius = getResources().getDimensionPixelSize(
                R.dimen.recents_task_view_rounded_corners_radius);
        mHighlightHeight = getResources().getDimensionPixelSize(
                R.dimen.recents_task_view_highlight);
        mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.fast_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context,
                com.android.internal.R.interpolator.fast_out_linear_in);

        // Configure the highlight paint
        if (sHighlightPaint == null) {
            sHighlightPaint = new Paint();
            sHighlightPaint.setStyle(Paint.Style.STROKE);
            sHighlightPaint.setStrokeWidth(mHighlightHeight);
            sHighlightPaint.setColor(context.getColor(R.color.recents_task_bar_highlight_color));
            sHighlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            sHighlightPaint.setAntiAlias(true);
        }
    }

    @Override
    protected void onFinishInflate() {
        // Initialize the icon and description views
        mIconView = (ImageView) findViewById(R.id.icon);
        mIconView.setOnLongClickListener(this);
        mTitleView = (TextView) findViewById(R.id.title);
        mDismissButton = (ImageView) findViewById(R.id.dismiss_task);
        mDismissButton.setOnClickListener(this);
        mMoveTaskButton = (ImageView) findViewById(R.id.move_task);

        // Hide the backgrounds if they are ripple drawables
        if (mIconView.getBackground() instanceof RippleDrawable) {
            mIconView.setBackground(null);
        }

        mBackgroundColorDrawable = (GradientDrawable) getContext().getDrawable(
                R.drawable.recents_task_view_header_bg_color);
        // Copy the ripple drawable since we are going to be manipulating it
        mBackground = (RippleDrawable)
                getContext().getDrawable(R.drawable.recents_task_view_header_bg);
        mBackground = (RippleDrawable) mBackground.mutate().getConstantState().newDrawable();
        mBackground.setColor(ColorStateList.valueOf(0));
        mBackground.setDrawableByLayerId(mBackground.getId(0), mBackgroundColorDrawable);
        setBackground(mBackground);
    }

    /**
     * Called when the task view frame changes, allowing us to move the contents of the header
     * to match the frame changes.
     */
    public void onTaskViewSizeChanged(int width, int height) {
        mTaskViewRect.set(0, 0, width, height);
        boolean updateMoveTaskButton = mMoveTaskButton.getVisibility() != View.GONE;
        int appIconWidth = mIconView.getMeasuredWidth();
        int activityDescWidth = mTitleView.getMeasuredWidth();
        int dismissIconWidth = mDismissButton.getMeasuredWidth();
        int moveTaskIconWidth = mMoveTaskButton.getVisibility() == View.VISIBLE
                ? mMoveTaskButton.getMeasuredWidth()
                : 0;

        // Priority-wise, we show the activity icon first, the dismiss icon if there is room, the
        // move-task icon if there is room, and then finally, the activity label if there is room
        if (width < (appIconWidth + dismissIconWidth)) {
            mTitleView.setVisibility(View.INVISIBLE);
            if (updateMoveTaskButton) {
                mMoveTaskButton.setVisibility(View.INVISIBLE);
            }
            mDismissButton.setVisibility(View.INVISIBLE);
        } else if (width < (appIconWidth + dismissIconWidth + moveTaskIconWidth)) {
            mTitleView.setVisibility(View.INVISIBLE);
            if (updateMoveTaskButton) {
                mMoveTaskButton.setVisibility(View.INVISIBLE);
            }
            mDismissButton.setVisibility(View.VISIBLE);
        } else if (width < (appIconWidth + dismissIconWidth + moveTaskIconWidth +
                activityDescWidth)) {
            mTitleView.setVisibility(View.INVISIBLE);
            if (updateMoveTaskButton) {
                mMoveTaskButton.setVisibility(View.VISIBLE);
            }
            mDismissButton.setVisibility(View.VISIBLE);
        } else {
            mTitleView.setVisibility(View.VISIBLE);
            if (updateMoveTaskButton) {
                mMoveTaskButton.setVisibility(View.VISIBLE);
            }
            mDismissButton.setVisibility(View.VISIBLE);
        }
        if (updateMoveTaskButton) {
            mMoveTaskButton.setTranslationX(width - getMeasuredWidth());
        }
        mDismissButton.setTranslationX(width - getMeasuredWidth());
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        onTaskViewSizeChanged(mTaskViewRect.width(), mTaskViewRect.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the highlight at the top edge (but put the bottom edge just out of view)
        float offset = (float) Math.ceil(mHighlightHeight / 2f);
        float radius = mCornerRadius;
        int count = canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(0, 0, mTaskViewRect.width(), getMeasuredHeight());
        canvas.drawRoundRect(-offset, 0f, (float) mTaskViewRect.width() + offset,
                getMeasuredHeight() + radius, radius, radius, sHighlightPaint);
        canvas.restoreToCount(count);
    }

    /**
     * Sets the dim alpha, only used when we are not using hardware layers.
     * (see RecentsConfiguration.useHardwareLayers)
     */
    void setDimAlpha(int alpha) {
        mDimLayerPaint.setColor(Color.argb(alpha, 0, 0, 0));
        invalidate();
    }

    /** Binds the bar view to the task */
    public void rebindToTask(Task t) {
        SystemServicesProxy ssp = Recents.getSystemServices();
        mTask = t;

        // If an activity icon is defined, then we use that as the primary icon to show in the bar,
        // otherwise, we fall back to the application icon
        if (t.icon != null) {
            mIconView.setImageDrawable(t.icon);
        }
        if (!mTitleView.getText().toString().equals(t.title)) {
            mTitleView.setText(t.title);
        }
        mTitleView.setContentDescription(t.contentDescription);

        // Try and apply the system ui tint
        int existingBgColor = (getBackground() instanceof ColorDrawable) ?
                ((ColorDrawable) getBackground()).getColor() : 0;
        if (existingBgColor != t.colorPrimary) {
            mBackgroundColorDrawable.setColor(t.colorPrimary);
        }

        int taskBarViewLightTextColor = getResources().getColor(
                R.color.recents_task_bar_light_text_color);
        int taskBarViewDarkTextColor = getResources().getColor(
                R.color.recents_task_bar_dark_text_color);
        mTitleView.setTextColor(t.useLightOnPrimaryColor ?
                taskBarViewLightTextColor : taskBarViewDarkTextColor);
        mDismissButton.setImageDrawable(t.useLightOnPrimaryColor ?
                mLightDismissDrawable : mDarkDismissDrawable);
        mDismissButton.setContentDescription(String.format(mDismissContentDescription,
                t.contentDescription));

        // When freeform workspaces are enabled, then update the move-task button depending on the
        // current task
        if (ssp.hasFreeformWorkspaceSupport()) {
            if (t.isFreeformTask()) {
                mMoveTaskTargetStackId = FULLSCREEN_WORKSPACE_STACK_ID;
                mMoveTaskButton.setImageResource(t.useLightOnPrimaryColor
                        ? R.drawable.recents_move_task_fullscreen_light
                        : R.drawable.recents_move_task_fullscreen_dark);
            } else {
                mMoveTaskTargetStackId = FREEFORM_WORKSPACE_STACK_ID;
                mMoveTaskButton.setImageResource(t.useLightOnPrimaryColor
                        ? R.drawable.recents_move_task_freeform_light
                        : R.drawable.recents_move_task_freeform_dark);
            }
            mMoveTaskButton.setVisibility(View.VISIBLE);
            mMoveTaskButton.setOnClickListener(this);
        }

        // In accessibility, a single click on the focused app info button will show it
        if (ssp.isTouchExplorationEnabled()) {
            mIconView.setOnClickListener(this);
        }
    }

    /** Unbinds the bar view from the task */
    void unbindFromTask() {
        mTask = null;
        mIconView.setImageDrawable(null);
        mIconView.setOnClickListener(null);
        mMoveTaskButton.setOnClickListener(null);
    }

    /** Animates this task bar if the user does not interact with the stack after a certain time. */
    void startNoUserInteractionAnimation() {
        if (mDismissButton.getVisibility() != View.VISIBLE) {
            mDismissButton.setVisibility(View.VISIBLE);
            mDismissButton.setAlpha(0f);
            mDismissButton.animate()
                    .alpha(1f)
                    .setStartDelay(0)
                    .setInterpolator(mFastOutLinearInInterpolator)
                    .setDuration(getResources().getInteger(
                            R.integer.recents_task_enter_from_app_duration))
                    .start();
        }
    }

    /** Mark this task view that the user does has not interacted with the stack after a certain time. */
    void setNoUserInteractionState() {
        if (mDismissButton.getVisibility() != View.VISIBLE) {
            mDismissButton.animate().cancel();
            mDismissButton.setVisibility(View.VISIBLE);
            mDismissButton.setAlpha(1f);
        }
    }

    /** Resets the state tracking that the user has not interacted with the stack after a certain time. */
    void resetNoUserInteractionState() {
        mDismissButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {

        // Don't forward our state to the drawable - we do it manually in onTaskViewFocusChanged.
        // This is to prevent layer trashing when the view is pressed.
        return new int[] {};
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // Draw the dim layer with the rounded corners
        canvas.drawRoundRect(0, 0, mTaskViewRect.width(), getHeight(),
                mCornerRadius, mCornerRadius, mDimLayerPaint);
    }

    @Override
    public void onClick(View v) {
        if (v == mIconView) {
            // In accessibility, a single click on the focused app info button will show it
            EventBus.getDefault().send(new ShowApplicationInfoEvent(mTask));
        } else if (v == mDismissButton) {
            TaskView tv = Utilities.findParent(this, TaskView.class);
            tv.dismissTask();

            // Keep track of deletions by the dismiss button
            MetricsLogger.histogram(getContext(), "overview_task_dismissed_source",
                    Constants.Metrics.DismissSourceHeaderButton);
        } else if (v == mMoveTaskButton) {
            TaskView tv = Utilities.findParent(this, TaskView.class);
            Rect bounds = mMoveTaskTargetStackId == FREEFORM_WORKSPACE_STACK_ID
                    ? new Rect(mTaskViewRect)
                    : new Rect();
            EventBus.getDefault().send(new LaunchTaskEvent(tv, mTask, bounds,
                    mMoveTaskTargetStackId, false));
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == mIconView) {
            EventBus.getDefault().send(new ShowApplicationInfoEvent(mTask));
            return true;
        }
        return false;
    }
}