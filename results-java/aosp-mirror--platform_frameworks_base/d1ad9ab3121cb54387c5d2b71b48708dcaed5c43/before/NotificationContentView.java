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
 * limitations under the License
 */

package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.android.systemui.R;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.notification.HybridNotificationViewManager;
import com.android.systemui.statusbar.notification.NotificationCustomViewWrapper;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.RemoteInputView;

/**
 * A frame layout containing the actual payload of the notification, including the contracted,
 * expanded and heads up layout. This class is responsible for clipping the content and and
 * switching between the expanded, contracted and the heads up view depending on its clipped size.
 */
public class NotificationContentView extends FrameLayout {

    private static final int VISIBLE_TYPE_CONTRACTED = 0;
    private static final int VISIBLE_TYPE_EXPANDED = 1;
    private static final int VISIBLE_TYPE_HEADSUP = 2;
    private static final int VISIBLE_TYPE_SINGLELINE = 3;
    private static final int UNDEFINED = -1;

    private final Rect mClipBounds = new Rect();
    private final int mMinContractedHeight;
    private final int mNotificationContentMarginEnd;
    private final OnLayoutChangeListener mLayoutUpdater = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                int oldLeft,
                int oldTop, int oldRight, int oldBottom) {
            selectLayout(false /* animate */, false /* force */);
        }
    };


    private View mContractedChild;
    private View mExpandedChild;
    private View mHeadsUpChild;
    private HybridNotificationView mSingleLineView;

    private NotificationViewWrapper mContractedWrapper;
    private NotificationViewWrapper mExpandedWrapper;
    private NotificationViewWrapper mHeadsUpWrapper;
    private HybridNotificationViewManager mHybridViewManager;
    private int mClipTopAmount;
    private int mContentHeight;
    private int mUnrestrictedContentHeight;
    private int mVisibleType = VISIBLE_TYPE_CONTRACTED;
    private boolean mDark;
    private boolean mAnimate;
    private boolean mIsHeadsUp;
    private boolean mShowingLegacyBackground;
    private boolean mIsChildInGroup;
    private int mSmallHeight;
    private int mHeadsUpHeight;
    private int mNotificationMaxHeight;
    private StatusBarNotification mStatusBarNotification;
    private NotificationGroupManager mGroupManager;
    private RemoteInputController mRemoteInputController;

    private final ViewTreeObserver.OnPreDrawListener mEnableAnimationPredrawListener
            = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            mAnimate = true;
            getViewTreeObserver().removeOnPreDrawListener(this);
            return true;
        }
    };

    private OnClickListener mExpandClickListener;
    private boolean mBeforeN;
    private boolean mExpandable;
    private boolean mClipToActualHeight = true;
    private ExpandableNotificationRow mContainingNotification;
    private int mTransformationStartVisibleType;
    private boolean mUserExpanding;

    public NotificationContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHybridViewManager = new HybridNotificationViewManager(getContext(), this);
        mMinContractedHeight = getResources().getDimensionPixelSize(
                R.dimen.min_notification_layout_height);
        mNotificationContentMarginEnd = getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.notification_content_margin_end);
        reset(true);
    }

    public void setHeights(int smallHeight, int headsUpMaxHeight, int maxHeight) {
        mSmallHeight = smallHeight;
        mHeadsUpHeight = headsUpMaxHeight;
        mNotificationMaxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean hasFixedHeight = heightMode == MeasureSpec.EXACTLY;
        boolean isHeightLimited = heightMode == MeasureSpec.AT_MOST;
        int maxSize = Integer.MAX_VALUE;
        if (hasFixedHeight || isHeightLimited) {
            maxSize = MeasureSpec.getSize(heightMeasureSpec);
        }
        int maxChildHeight = 0;
        if (mExpandedChild != null) {
            int size = Math.min(maxSize, mNotificationMaxHeight);
            ViewGroup.LayoutParams layoutParams = mExpandedChild.getLayoutParams();
            if (layoutParams.height >= 0) {
                // An actual height is set
                size = Math.min(maxSize, layoutParams.height);
            }
            int spec = size == Integer.MAX_VALUE
                    ? MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    : MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST);
            mExpandedChild.measure(widthMeasureSpec, spec);
            maxChildHeight = Math.max(maxChildHeight, mExpandedChild.getMeasuredHeight());
        }
        if (mContractedChild != null) {
            int heightSpec;
            int size = Math.min(maxSize, mSmallHeight);
            if (shouldContractedBeFixedSize()) {
                heightSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST);
            }
            mContractedChild.measure(widthMeasureSpec, heightSpec);
            int measuredHeight = mContractedChild.getMeasuredHeight();
            if (measuredHeight < mMinContractedHeight) {
                heightSpec = MeasureSpec.makeMeasureSpec(mMinContractedHeight, MeasureSpec.EXACTLY);
                mContractedChild.measure(widthMeasureSpec, heightSpec);
            }
            maxChildHeight = Math.max(maxChildHeight, measuredHeight);
            if (updateContractedHeaderWidth()) {
                mContractedChild.measure(widthMeasureSpec, heightSpec);
            }
        }
        if (mHeadsUpChild != null) {
            int size = Math.min(maxSize, mHeadsUpHeight);
            ViewGroup.LayoutParams layoutParams = mHeadsUpChild.getLayoutParams();
            if (layoutParams.height >= 0) {
                // An actual height is set
                size = Math.min(size, layoutParams.height);
            }
            mHeadsUpChild.measure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST));
            maxChildHeight = Math.max(maxChildHeight, mHeadsUpChild.getMeasuredHeight());
        }
        if (mSingleLineView != null) {
            mSingleLineView.measure(widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.AT_MOST));
            maxChildHeight = Math.max(maxChildHeight, mSingleLineView.getMeasuredHeight());
        }
        int ownHeight = Math.min(maxChildHeight, maxSize);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, ownHeight);
    }

    private boolean updateContractedHeaderWidth() {
        // We need to update the expanded and the collapsed header to have exactly the same with to
        // have the expand buttons laid out at the same location.
        NotificationHeaderView contractedHeader = mContractedWrapper.getNotificationHeader();
        if (contractedHeader != null) {
            if (mExpandedChild != null
                    && mExpandedWrapper.getNotificationHeader() != null) {
                NotificationHeaderView expandedHeader = mExpandedWrapper.getNotificationHeader();
                int expandedSize = expandedHeader.getMeasuredWidth()
                        - expandedHeader.getPaddingEnd();
                int collapsedSize = contractedHeader.getMeasuredWidth()
                        - expandedHeader.getPaddingEnd();
                if (expandedSize != collapsedSize) {
                    int paddingEnd = contractedHeader.getMeasuredWidth() - expandedSize;
                    contractedHeader.setPadding(
                            contractedHeader.isLayoutRtl()
                                    ? paddingEnd
                                    : contractedHeader.getPaddingLeft(),
                            contractedHeader.getPaddingTop(),
                            contractedHeader.isLayoutRtl()
                                    ? contractedHeader.getPaddingLeft()
                                    : paddingEnd,
                            contractedHeader.getPaddingBottom());
                    contractedHeader.setShowWorkBadgeAtEnd(true);
                    return true;
                }
            } else {
                int paddingEnd = mNotificationContentMarginEnd;
                if (contractedHeader.getPaddingEnd() != paddingEnd) {
                    contractedHeader.setPadding(
                            contractedHeader.isLayoutRtl()
                                    ? paddingEnd
                                    : contractedHeader.getPaddingLeft(),
                            contractedHeader.getPaddingTop(),
                            contractedHeader.isLayoutRtl()
                                    ? contractedHeader.getPaddingLeft()
                                    : paddingEnd,
                            contractedHeader.getPaddingBottom());
                    contractedHeader.setShowWorkBadgeAtEnd(false);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldContractedBeFixedSize() {
        return mBeforeN && mContractedWrapper instanceof NotificationCustomViewWrapper;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateClipping();
        invalidateOutline();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateVisibility();
    }

    public void reset(boolean resetActualHeight) {
        if (mContractedChild != null) {
            mContractedChild.animate().cancel();
            removeView(mContractedChild);
        }
        if (mExpandedChild != null) {
            mExpandedChild.animate().cancel();
            removeView(mExpandedChild);
        }
        if (mHeadsUpChild != null) {
            mHeadsUpChild.animate().cancel();
            removeView(mHeadsUpChild);
        }
        mContractedChild = null;
        mExpandedChild = null;
        mHeadsUpChild = null;
        mVisibleType = VISIBLE_TYPE_CONTRACTED;
        if (resetActualHeight) {
            mContentHeight = mSmallHeight;
        }
    }

    public View getContractedChild() {
        return mContractedChild;
    }

    public View getExpandedChild() {
        return mExpandedChild;
    }

    public View getHeadsUpChild() {
        return mHeadsUpChild;
    }

    public void setContractedChild(View child) {
        if (mContractedChild != null) {
            mContractedChild.animate().cancel();
            mContractedChild.removeOnLayoutChangeListener(mLayoutUpdater);
            removeView(mContractedChild);
        }
        addView(child);
        mContractedChild = child;
        mContractedChild.addOnLayoutChangeListener(mLayoutUpdater);
        mContractedWrapper = NotificationViewWrapper.wrap(getContext(), child);
        selectLayout(false /* animate */, true /* force */);
        mContractedWrapper.setDark(mDark, false /* animate */, 0 /* delay */);
    }

    public void setExpandedChild(View child) {
        if (mExpandedChild != null) {
            mExpandedChild.animate().cancel();
            mExpandedChild.removeOnLayoutChangeListener(mLayoutUpdater);
            removeView(mExpandedChild);
        }
        addView(child);
        mExpandedChild = child;
        mExpandedChild.addOnLayoutChangeListener(mLayoutUpdater);
        mExpandedWrapper = NotificationViewWrapper.wrap(getContext(), child);
        selectLayout(false /* animate */, true /* force */);
    }

    public void setHeadsUpChild(View child) {
        if (mHeadsUpChild != null) {
            mHeadsUpChild.animate().cancel();
            mHeadsUpChild.removeOnLayoutChangeListener(mLayoutUpdater);
            removeView(mHeadsUpChild);
        }
        addView(child);
        mHeadsUpChild = child;
        mHeadsUpChild.addOnLayoutChangeListener(mLayoutUpdater);
        mHeadsUpWrapper = NotificationViewWrapper.wrap(getContext(), child);
        selectLayout(false /* animate */, true /* force */);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateVisibility();
    }

    private void updateVisibility() {
        setVisible(isShown());
    }

    private void setVisible(final boolean isVisible) {
        if (isVisible) {

            // We only animate if we are drawn at least once, otherwise the view might animate when
            // it's shown the first time
            getViewTreeObserver().addOnPreDrawListener(mEnableAnimationPredrawListener);
        } else {
            getViewTreeObserver().removeOnPreDrawListener(mEnableAnimationPredrawListener);
            mAnimate = false;
        }
    }

    public void setContentHeight(int contentHeight) {
        mContentHeight = Math.max(Math.min(contentHeight, getHeight()), getMinHeight());;
        mUnrestrictedContentHeight = Math.max(contentHeight, getMinHeight());
        selectLayout(mAnimate /* animate */, false /* force */);
        updateClipping();
        invalidateOutline();
    }

    private void updateContentTransformation() {
        int visibleType = calculateVisibleType();
        if (visibleType != mVisibleType) {
            // A new transformation starts
            mTransformationStartVisibleType = mVisibleType;
            final TransformableView shownView = getTransformableViewForVisibleType(visibleType);
            final TransformableView hiddenView = getTransformableViewForVisibleType(
                    mTransformationStartVisibleType);
            shownView.transformFrom(hiddenView, 0.0f);
            getViewForVisibleType(visibleType).setVisibility(View.VISIBLE);
            hiddenView.transformTo(shownView, 0.0f);
            mVisibleType = visibleType;
        }
        if (mTransformationStartVisibleType != UNDEFINED
                && mVisibleType != mTransformationStartVisibleType) {
            final TransformableView shownView = getTransformableViewForVisibleType(mVisibleType);
            final TransformableView hiddenView = getTransformableViewForVisibleType(
                    mTransformationStartVisibleType);
            float transformationAmount = calculateTransformationAmount();
            shownView.transformFrom(hiddenView, transformationAmount);
            hiddenView.transformTo(shownView, transformationAmount);
        } else {
            updateViewVisibilities(visibleType);
        }
    }

    private float calculateTransformationAmount() {
        int startHeight = getViewForVisibleType(mTransformationStartVisibleType).getHeight();
        int endHeight = getViewForVisibleType(mVisibleType).getHeight();
        int progress = Math.abs(mContentHeight - startHeight);
        int totalDistance = Math.abs(endHeight - startHeight);
        float amount = (float) progress / (float) totalDistance;
        return Math.min(1.0f, amount);
    }

    public int getContentHeight() {
        return mContentHeight;
    }

    public int getMaxHeight() {
        if (mExpandedChild != null) {
            return mExpandedChild.getHeight();
        } else if (mIsHeadsUp && mHeadsUpChild != null) {
            return mHeadsUpChild.getHeight();
        }
        return mContractedChild.getHeight();
    }

    public int getMinHeight() {
        return getMinHeight(false /* likeGroupExpanded */);
    }

    public int getMinHeight(boolean likeGroupExpanded) {
        if (likeGroupExpanded || !mIsChildInGroup || isGroupExpanded()) {
            return mContractedChild.getHeight();
        } else {
            return mSingleLineView.getHeight();
        }
    }

    private boolean isGroupExpanded() {
        return mGroupManager.isGroupExpanded(mStatusBarNotification);
    }

    public void setClipTopAmount(int clipTopAmount) {
        mClipTopAmount = clipTopAmount;
        updateClipping();
    }

    private void updateClipping() {
        if (mClipToActualHeight) {
            mClipBounds.set(0, mClipTopAmount, getWidth(), mContentHeight);
            setClipBounds(mClipBounds);
        } else {
            setClipBounds(null);
        }
    }

    public void setClipToActualHeight(boolean clipToActualHeight) {
        mClipToActualHeight = clipToActualHeight;
        updateClipping();
    }

    private void selectLayout(boolean animate, boolean force) {
        if (mContractedChild == null) {
            return;
        }
        if (mUserExpanding) {
            updateContentTransformation();
            return;
        }
        int visibleType = calculateVisibleType();
        if (visibleType != mVisibleType || force) {
            if (animate && ((visibleType == VISIBLE_TYPE_EXPANDED && mExpandedChild != null)
                    || (visibleType == VISIBLE_TYPE_HEADSUP && mHeadsUpChild != null)
                    || (visibleType == VISIBLE_TYPE_SINGLELINE && mSingleLineView != null)
                    || visibleType == VISIBLE_TYPE_CONTRACTED)) {
                animateToVisibleType(visibleType);
            } else {
                updateViewVisibilities(visibleType);
            }
            mVisibleType = visibleType;
        }
    }

    private void updateViewVisibilities(int visibleType) {
        boolean contractedVisible = visibleType == VISIBLE_TYPE_CONTRACTED;
        mContractedWrapper.setVisible(contractedVisible);
        if (mExpandedChild != null) {
            boolean expandedVisible = visibleType == VISIBLE_TYPE_EXPANDED;
            mExpandedWrapper.setVisible(expandedVisible);
        }
        if (mHeadsUpChild != null) {
            boolean headsUpVisible = visibleType == VISIBLE_TYPE_HEADSUP;
            mHeadsUpWrapper.setVisible(headsUpVisible);
        }
        if (mSingleLineView != null) {
            boolean singleLineVisible = visibleType == VISIBLE_TYPE_SINGLELINE;
            mSingleLineView.setVisible(singleLineVisible);
        }
    }

    private void animateToVisibleType(int visibleType) {
        final TransformableView shownView = getTransformableViewForVisibleType(visibleType);
        final TransformableView hiddenView = getTransformableViewForVisibleType(mVisibleType);
        shownView.transformFrom(hiddenView);
        getViewForVisibleType(visibleType).setVisibility(View.VISIBLE);
        hiddenView.transformTo(shownView, new Runnable() {
            @Override
            public void run() {
                hiddenView.setVisible(false);
            }
        });
    }

    /**
     * @param visibleType one of the static enum types in this view
     * @return the corresponding transformable view according to the given visible type
     */
    private TransformableView getTransformableViewForVisibleType(int visibleType) {
        switch (visibleType) {
            case VISIBLE_TYPE_EXPANDED:
                return mExpandedWrapper;
            case VISIBLE_TYPE_HEADSUP:
                return mHeadsUpWrapper;
            case VISIBLE_TYPE_SINGLELINE:
                return mSingleLineView;
            default:
                return mContractedWrapper;
        }
    }

    /**
     * @param visibleType one of the static enum types in this view
     * @return the corresponding view according to the given visible type
     */
    private View getViewForVisibleType(int visibleType) {
        switch (visibleType) {
            case VISIBLE_TYPE_EXPANDED:
                return mExpandedChild;
            case VISIBLE_TYPE_HEADSUP:
                return mHeadsUpChild;
            case VISIBLE_TYPE_SINGLELINE:
                return mSingleLineView;
            default:
                return mContractedChild;
        }
    }

    private NotificationViewWrapper getCurrentVisibleWrapper() {
        switch (mVisibleType) {
            case VISIBLE_TYPE_EXPANDED:
                return mExpandedWrapper;
            case VISIBLE_TYPE_HEADSUP:
                return mHeadsUpWrapper;
            case VISIBLE_TYPE_CONTRACTED:
                return mContractedWrapper;
            default:
                return null;
        }
    }

    /**
     * @return one of the static enum types in this view, calculated form the current state
     */
    private int calculateVisibleType() {
        if (mUserExpanding) {
            int height = !mIsChildInGroup || isGroupExpanded()
                    || mContainingNotification.isExpanded()
                    ? mContainingNotification.getMaxContentHeight()
                    : mContainingNotification.getShowingLayout().getMinHeight();
            int expandedVisualType = getVisualTypeForHeight(height);
            int collapsedVisualType = getVisualTypeForHeight(
                    mContainingNotification.getMinExpandHeight());
            return mTransformationStartVisibleType == collapsedVisualType
                    ? expandedVisualType
                    : collapsedVisualType;
        }
        int viewHeight = Math.min(mContentHeight, mContainingNotification.getIntrinsicHeight());
        return getVisualTypeForHeight(viewHeight);
    }

    private int getVisualTypeForHeight(float viewHeight) {
        boolean noExpandedChild = mExpandedChild == null;
        if (!noExpandedChild && viewHeight == mExpandedChild.getHeight()) {
            return VISIBLE_TYPE_EXPANDED;
        }
        if (!mUserExpanding && mIsChildInGroup && !isGroupExpanded()) {
            return VISIBLE_TYPE_SINGLELINE;
        }

        if (mIsHeadsUp && mHeadsUpChild != null) {
            if (viewHeight <= mHeadsUpChild.getHeight() || noExpandedChild) {
                return VISIBLE_TYPE_HEADSUP;
            } else {
                return VISIBLE_TYPE_EXPANDED;
            }
        } else {
            if (noExpandedChild || (viewHeight <= mContractedChild.getHeight()
                    && (!mIsChildInGroup || !mContainingNotification.isExpanded()))) {
                return VISIBLE_TYPE_CONTRACTED;
            } else {
                return VISIBLE_TYPE_EXPANDED;
            }
        }
    }

    public boolean isContentExpandable() {
        return mExpandedChild != null;
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        if (mContractedChild == null) {
            return;
        }
        mDark = dark;
        dark = dark && !mShowingLegacyBackground;
        if (mVisibleType == VISIBLE_TYPE_CONTRACTED || !dark) {
            mContractedWrapper.setDark(dark, fade, delay);
        }
        if (mVisibleType == VISIBLE_TYPE_EXPANDED || (mExpandedChild != null && !dark)) {
            mExpandedWrapper.setDark(dark, fade, delay);
        }
        if (mVisibleType == VISIBLE_TYPE_HEADSUP || (mHeadsUpChild != null && !dark)) {
            mHeadsUpWrapper.setDark(dark, fade, delay);
        }
        if (mSingleLineView != null && (mVisibleType == VISIBLE_TYPE_SINGLELINE || !dark)) {
            mSingleLineView.setDark(dark, fade, delay);
        }
    }

    public void setHeadsUp(boolean headsUp) {
        mIsHeadsUp = headsUp;
        selectLayout(false /* animate */, true /* force */);
        updateExpandButtons(mExpandable);
    }

    @Override
    public boolean hasOverlappingRendering() {

        // This is not really true, but good enough when fading from the contracted to the expanded
        // layout, and saves us some layers.
        return false;
    }

    public void setShowingLegacyBackground(boolean showing) {
        mShowingLegacyBackground = showing;
    }

    public void setIsChildInGroup(boolean isChildInGroup) {
        mIsChildInGroup = isChildInGroup;
        updateSingleLineView();
    }

    public void onNotificationUpdated(NotificationData.Entry entry) {
        mStatusBarNotification = entry.notification;
        mBeforeN = entry.targetSdk < Build.VERSION_CODES.N;
        updateSingleLineView();
        applyRemoteInput(entry);
        selectLayout(false /* animate */, true /* force */);
        if (mContractedChild != null) {
            mContractedWrapper.notifyContentUpdated(entry.notification);
        }
        if (mExpandedChild != null) {
            mExpandedWrapper.notifyContentUpdated(entry.notification);
        }
        if (mHeadsUpChild != null) {
            mHeadsUpWrapper.notifyContentUpdated(entry.notification);
        }
        setDark(mDark, false /* animate */, 0 /* delay */);
    }

    private void updateSingleLineView() {
        if (mIsChildInGroup) {
            mSingleLineView = mHybridViewManager.bindFromNotification(
                    mSingleLineView, mStatusBarNotification.getNotification());
        } else if (mSingleLineView != null) {
            removeView(mSingleLineView);
            mSingleLineView = null;
        }
    }

    private void applyRemoteInput(final NotificationData.Entry entry) {
        if (mRemoteInputController == null) {
            return;
        }

        boolean hasRemoteInput = false;

        Notification.Action[] actions = entry.notification.getNotification().actions;
        if (actions != null) {
            for (Notification.Action a : actions) {
                if (a.getRemoteInputs() != null) {
                    for (RemoteInput ri : a.getRemoteInputs()) {
                        if (ri.getAllowFreeFormInput()) {
                            hasRemoteInput = true;
                            break;
                        }
                    }
                }
            }
        }

        View bigContentView = mExpandedChild;
        if (bigContentView != null) {
            applyRemoteInput(bigContentView, entry, hasRemoteInput);
        }
        View headsUpContentView = mHeadsUpChild;
        if (headsUpContentView != null) {
            applyRemoteInput(headsUpContentView, entry, hasRemoteInput);
        }
    }

    private void applyRemoteInput(View view, NotificationData.Entry entry, boolean hasRemoteInput) {
        View actionContainerCandidate = view.findViewById(
                com.android.internal.R.id.actions_container);
        if (actionContainerCandidate instanceof FrameLayout) {
            RemoteInputView existing = (RemoteInputView)
                    view.findViewWithTag(RemoteInputView.VIEW_TAG);

            if (existing != null) {
                existing.onNotificationUpdate();
            }

            if (existing == null && hasRemoteInput) {
                ViewGroup actionContainer = (FrameLayout) actionContainerCandidate;
                RemoteInputView riv = RemoteInputView.inflate(
                        mContext, actionContainer, entry, mRemoteInputController);

                riv.setVisibility(View.INVISIBLE);
                actionContainer.addView(riv, new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                );
                int color = entry.notification.getNotification().color;
                if (color == Notification.COLOR_DEFAULT) {
                    color = mContext.getColor(R.color.default_remote_input_background);
                }
                riv.setBackgroundColor(color);
            }
        }
    }

    public void setGroupManager(NotificationGroupManager groupManager) {
        mGroupManager = groupManager;
    }

    public void setRemoteInputController(RemoteInputController r) {
        mRemoteInputController = r;
    }

    public void setExpandClickListener(OnClickListener expandClickListener) {
        mExpandClickListener = expandClickListener;
    }

    public void updateExpandButtons(boolean expandable) {
        mExpandable = expandable;
        // if the expanded child has the same height as the collapsed one we hide it.
        if (mExpandedChild != null && mExpandedChild.getHeight() != 0 &&
                ((mIsHeadsUp && mExpandedChild.getHeight() == mHeadsUpChild.getHeight()) ||
                (!mIsHeadsUp && mExpandedChild.getHeight() == mContractedChild.getHeight()))) {
            expandable = false;
        }
        if (mExpandedChild != null) {
            mExpandedWrapper.updateExpandability(expandable, mExpandClickListener);
        }
        if (mContractedChild != null) {
            mContractedWrapper.updateExpandability(expandable, mExpandClickListener);
        }
        if (mHeadsUpChild != null) {
            mHeadsUpWrapper.updateExpandability(expandable,  mExpandClickListener);
        }
    }

    public NotificationHeaderView getNotificationHeader() {
        NotificationHeaderView header = null;
        if (mContractedChild != null) {
            header = mContractedWrapper.getNotificationHeader();
        }
        if (header == null && mExpandedChild != null) {
            header = mExpandedWrapper.getNotificationHeader();
        }
        if (header == null && mHeadsUpChild != null) {
            header = mHeadsUpWrapper.getNotificationHeader();
        }
        return header;
    }

    public NotificationHeaderView getVisibleNotificationHeader() {
        NotificationViewWrapper wrapper = getCurrentVisibleWrapper();
        return wrapper == null ? null : wrapper.getNotificationHeader();
    }

    public void setContainingNotification(ExpandableNotificationRow containingNotification) {
        mContainingNotification = containingNotification;
    }

    public void requestSelectLayout(boolean needsAnimation) {
        selectLayout(needsAnimation, false);
    }

    public void reInflateViews() {
        if (mIsChildInGroup && mSingleLineView != null) {
            removeView(mSingleLineView);
            mSingleLineView = null;
            updateSingleLineView();
        }
    }

    public void setUserExpanding(boolean userExpanding) {
        mUserExpanding = userExpanding;
        if (userExpanding) {
            mTransformationStartVisibleType = mVisibleType;
        } else {
            mTransformationStartVisibleType = UNDEFINED;
            mVisibleType = calculateVisibleType();
            updateViewVisibilities(mVisibleType);
        }
    }
}