/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import com.android.internal.widget.SizeAdaptiveLayout;
import com.android.systemui.R;

public class ExpandableNotificationRow extends ActivatableNotificationView {
    private int mRowMinHeight;
    private int mRowMaxHeight;

    /** Does this row contain layouts that can adapt to row expansion */
    private boolean mExpandable;
    /** Has the user actively changed the expansion state of this row */
    private boolean mHasUserChangedExpansion;
    /** If {@link #mHasUserChangedExpansion}, has the user expanded this row */
    private boolean mUserExpanded;
    /** Is the user touching this row */
    private boolean mUserLocked;
    /** Are we showing the "public" version */
    private boolean mShowingPublic;

    /**
     * Is this notification expanded by the system. The expansion state can be overridden by the
     * user expansion.
     */
    private boolean mIsSystemExpanded;
    private SizeAdaptiveLayout mPublicLayout;
    private SizeAdaptiveLayout mPrivateLayout;
    private int mMaxExpandHeight;
    private boolean mMaxHeightNeedsUpdate;
    private NotificationActivator mActivator;
    private boolean mSelfInitiatedLayout;

    public ExpandableNotificationRow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPublicLayout = (SizeAdaptiveLayout) findViewById(R.id.expandedPublic);
        mPrivateLayout = (SizeAdaptiveLayout) findViewById(R.id.expanded);

        mActivator = new NotificationActivator(this);
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (super.onRequestSendAccessibilityEvent(child, event)) {
            // Add a record for the entire layout since its content is somehow small.
            // The event comes from a leaf view that is interacted with.
            AccessibilityEvent record = AccessibilityEvent.obtain();
            onInitializeAccessibilityEvent(record);
            dispatchPopulateAccessibilityEvent(record);
            event.appendRecord(record);
            return true;
        }
        return false;
    }

    public void setHeightRange(int rowMinHeight, int rowMaxHeight) {
        mRowMinHeight = rowMinHeight;
        mRowMaxHeight = rowMaxHeight;
        mMaxHeightNeedsUpdate = true;
    }

    public boolean isExpandable() {
        return mExpandable;
    }

    public void setExpandable(boolean expandable) {
        mExpandable = expandable;
    }

    /**
     * @return whether the user has changed the expansion state
     */
    public boolean hasUserChangedExpansion() {
        return mHasUserChangedExpansion;
    }

    public boolean isUserExpanded() {
        return mUserExpanded;
    }

    /**
     * Set this notification to be expanded by the user
     *
     * @param userExpanded whether the user wants this notification to be expanded
     */
    public void setUserExpanded(boolean userExpanded) {
        mHasUserChangedExpansion = true;
        mUserExpanded = userExpanded;
    }

    public boolean isUserLocked() {
        return mUserLocked;
    }

    public void setUserLocked(boolean userLocked) {
        mUserLocked = userLocked;
    }

    /**
     * @return has the system set this notification to be expanded
     */
    public boolean isSystemExpanded() {
        return mIsSystemExpanded;
    }

    /**
     * Set this notification to be expanded by the system.
     *
     * @param expand whether the system wants this notification to be expanded.
     */
    public void setSystemExpanded(boolean expand) {
        mIsSystemExpanded = expand;
        applyExpansionToLayout(expand);
    }

    /**
     * Apply an expansion state to the layout.
     *
     * @param expand should the layout be in the expanded state
     */
    public void applyExpansionToLayout(boolean expand) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (expand && mExpandable) {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            lp.height = mRowMinHeight;
        }
        setLayoutParams(lp);
    }

    /**
     * If {@link #isExpanded()} then this is the greatest possible height this view can
     * get and otherwise it is {@link #mRowMinHeight}.
     *
     * @return the maximum allowed expansion height of this view.
     */
    public int getMaximumAllowedExpandHeight() {
        boolean inExpansionState = isExpanded();
        if (!inExpansionState) {
            // not expanded, so we return the collapsed size
            return mRowMinHeight;
        }

        return mShowingPublic ? mRowMinHeight : getMaxExpandHeight();
    }

    private void updateMaxExpandHeight() {

        // We don't want this method to trigger a layout of the whole view hierarchy,
        // as the layout parameters in the end are the same which they were in the beginning.
        // Otherwise a loop may occur if this method is called on the layout of a parent.
        mSelfInitiatedLayout = true;
        ViewGroup.LayoutParams lp = getLayoutParams();
        int oldHeight = lp.height;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(lp);
        measure(View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mRowMaxHeight, View.MeasureSpec.AT_MOST));
        lp.height = oldHeight;
        setLayoutParams(lp);
        mMaxExpandHeight = getMeasuredHeight();
        mSelfInitiatedLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mSelfInitiatedLayout) {
            super.requestLayout();
        }
    }

    /**
     * Check whether the view state is currently expanded. This is given by the system in {@link
     * #setSystemExpanded(boolean)} and can be overridden by user expansion or
     * collapsing in {@link #setUserExpanded(boolean)}. Note that the visual appearance of this
     * view can differ from this state, if layout params are modified from outside.
     *
     * @return whether the view state is currently expanded.
     */
    private boolean isExpanded() {
        return !hasUserChangedExpansion() && isSystemExpanded() || isUserExpanded();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mMaxHeightNeedsUpdate = true;
    }

    public void setShowingPublic(boolean show) {
        mShowingPublic = show;

        // bail out if no public version
        if (mPublicLayout.getChildCount() == 0) return;

        // TODO: animation?
        mPublicLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        mPrivateLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Sets the notification as dimmed, meaning that it will appear in a more gray variant.
     */
    public void setDimmed(boolean dimmed) {
        super.setDimmed(dimmed);
        mActivator.setDimmed(dimmed);
    }

    public int getMaxExpandHeight() {
        if (mMaxHeightNeedsUpdate) {
            updateMaxExpandHeight();
            mMaxHeightNeedsUpdate = false;
        }
        return mMaxExpandHeight;
    }

    public NotificationActivator getActivator() {
        return mActivator;
    }

    /**
     * @return the potential height this view could expand in addition.
     */
    public int getExpandPotential() {
        return getMaximumAllowedExpandHeight() - getHeight();
    }
}