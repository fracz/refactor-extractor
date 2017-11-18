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

package com.android.systemui.statusbar.stack;

import android.graphics.Outline;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.systemui.statusbar.ExpandableView;

import java.util.HashMap;
import java.util.Map;

/**
 * A state of a {@link com.android.systemui.statusbar.stack.NotificationStackScrollLayout} which
 * can be applied to a viewGroup.
 */
public class StackScrollState {

    private static final String CHILD_NOT_FOUND_TAG = "StackScrollStateNoSuchChild";

    private final ViewGroup mHostView;
    private Map<ExpandableView, ViewState> mStateMap;
    private int mScrollY;
    private final Rect mClipRect = new Rect();
    private int mBackgroundRoundedRectCornerRadius;
    private final Outline mChildOutline = new Outline();

    public int getScrollY() {
        return mScrollY;
    }

    public void setScrollY(int scrollY) {
        this.mScrollY = scrollY;
    }

    public StackScrollState(ViewGroup hostView) {
        mHostView = hostView;
        mStateMap = new HashMap<ExpandableView, ViewState>();
        mBackgroundRoundedRectCornerRadius = hostView.getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.notification_quantum_rounded_rect_radius);
    }

    public ViewGroup getHostView() {
        return mHostView;
    }

    public void resetViewStates() {
        int numChildren = mHostView.getChildCount();
        for (int i = 0; i < numChildren; i++) {
            ExpandableView child = (ExpandableView) mHostView.getChildAt(i);
            ViewState viewState = mStateMap.get(child);
            if (viewState == null) {
                viewState = new ViewState();
                mStateMap.put(child, viewState);
            }
            // initialize with the default values of the view
            viewState.height = child.getActualHeight();
            viewState.alpha = 1;
            viewState.gone = child.getVisibility() == View.GONE;
        }
    }


    public ViewState getViewStateForView(View requestedView) {
        return mStateMap.get(requestedView);
    }

    public void removeViewStateForView(View child) {
        mStateMap.remove(child);
    }

    /**
     * Apply the properties saved in {@link #mStateMap} to the children of the {@link #mHostView}.
     * The properties are only applied if they effectively changed.
     */
    public void apply() {
        int numChildren = mHostView.getChildCount();
        float previousNotificationEnd = 0;
        float previousNotificationStart = 0;
        for (int i = 0; i < numChildren; i++) {
            ExpandableView child = (ExpandableView) mHostView.getChildAt(i);
            ViewState state = mStateMap.get(child);
            if (state == null) {
                Log.wtf(CHILD_NOT_FOUND_TAG, "No child state was found when applying this state " +
                        "to the hostView");
                continue;
            }
            if (!state.gone) {
                float alpha = child.getAlpha();
                float yTranslation = child.getTranslationY();
                float zTranslation = child.getTranslationZ();
                int height = child.getActualHeight();
                float newAlpha = state.alpha;
                float newYTranslation = state.yTranslation;
                float newZTranslation = state.zTranslation;
                int newHeight = state.height;
                boolean becomesInvisible = newAlpha == 0.0f;
                if (alpha != newAlpha) {
                    // apply layer type
                    boolean becomesFullyVisible = newAlpha == 1.0f;
                    boolean newLayerTypeIsHardware = !becomesInvisible && !becomesFullyVisible;
                    int layerType = child.getLayerType();
                    int newLayerType = newLayerTypeIsHardware
                            ? View.LAYER_TYPE_HARDWARE
                            : View.LAYER_TYPE_NONE;
                    if (layerType != newLayerType) {
                        child.setLayerType(newLayerType, null);
                    }

                    // apply alpha
                    if (!becomesInvisible) {
                        child.setAlpha(newAlpha);
                    }
                }

                // apply visibility
                int oldVisibility = child.getVisibility();
                int newVisibility = becomesInvisible ? View.INVISIBLE : View.VISIBLE;
                if (newVisibility != oldVisibility) {
                    child.setVisibility(newVisibility);
                }

                // apply yTranslation
                if (yTranslation != newYTranslation) {
                    child.setTranslationY(newYTranslation);
                }

                // apply zTranslation
                if (zTranslation != newZTranslation) {
                    child.setTranslationZ(newZTranslation);
                }

                // apply height
                if (height != newHeight) {
                    child.setActualHeight(newHeight);
                }

                // apply clipping and shadow
                float newNotificationEnd = newYTranslation + newHeight;
                updateChildClippingAndBackground(child, newHeight,
                        newNotificationEnd - (previousNotificationEnd),
                        (int) (newHeight - (previousNotificationStart - newYTranslation)));

                previousNotificationStart = newYTranslation;
                previousNotificationEnd = newNotificationEnd;
            }
        }
    }

    /**
     * Updates the shadow outline and the clipping for a view.
     *
     * @param child the view to update
     * @param realHeight the currently applied height of the view
     * @param clipHeight the desired clip height, the rest of the view will be clipped from the top
     * @param backgroundHeight the desired background height. The shadows of the view will be
     *                         based on this height and the content will be clipped from the top
     */
    private void updateChildClippingAndBackground(ExpandableView child, int realHeight,
            float clipHeight, int backgroundHeight) {
        if (realHeight > clipHeight) {
            updateChildClip(child, realHeight, clipHeight);
        } else {
            child.setClipBounds(null);
        }
        if (realHeight > backgroundHeight) {
            child.setClipTopAmount(realHeight - backgroundHeight);
        } else {
            child.setClipTopAmount(0);
        }
    }

    /**
     * Updates the clipping of a view
     *
     * @param child the view to update
     * @param height the currently applied height of the view
     * @param clipHeight the desired clip height, the rest of the view will be clipped from the top
     */
    private void updateChildClip(View child, int height, float clipHeight) {
        int clipInset = (int) (height - clipHeight);
        mClipRect.set(0,
                clipInset,
                child.getWidth(),
                height);
        child.setClipBounds(mClipRect);
    }

    public static class ViewState {

        // These are flags such that we can create masks for filtering.

        public static final int LOCATION_UNKNOWN = 0x00;
        public static final int LOCATION_FIRST_CARD = 0x01;
        public static final int LOCATION_TOP_STACK_HIDDEN = 0x02;
        public static final int LOCATION_TOP_STACK_PEEKING = 0x04;
        public static final int LOCATION_MAIN_AREA = 0x08;
        public static final int LOCATION_BOTTOM_STACK_PEEKING = 0x10;
        public static final int LOCATION_BOTTOM_STACK_HIDDEN = 0x20;

        float alpha;
        float yTranslation;
        float zTranslation;
        int height;
        boolean gone;

        /**
         * The location this view is currently rendered at.
         *
         * <p>See <code>LOCATION_</code> flags.</p>
         */
        int location;
    }
}