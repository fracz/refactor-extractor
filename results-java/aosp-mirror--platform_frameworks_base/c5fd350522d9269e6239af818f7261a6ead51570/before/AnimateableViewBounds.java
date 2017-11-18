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

import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;

/* An outline provider that has a clip and outline that can be animated. */
public class AnimateableViewBounds extends ViewOutlineProvider {

    View mSourceView;
    Rect mClipRect = new Rect();
    Rect mClipBounds = new Rect();
    Rect mLastClipBounds = new Rect();
    int mCornerRadius;
    float mAlpha = 1f;
    final float mMinAlpha = 0.25f;

    public AnimateableViewBounds(View source, int cornerRadius) {
        mSourceView = source;
        mCornerRadius = cornerRadius;
    }

    /**
     * Resets the right and bottom clip for this view.
     */
    public void reset() {
        mClipRect.set(-1, -1, -1, -1);
        updateClipBounds();
    }

    @Override
    public void getOutline(View view, Outline outline) {
        outline.setAlpha(mMinAlpha + mAlpha / (1f - mMinAlpha));
        outline.setRoundRect(mClipRect.left, mClipRect.top,
                mSourceView.getWidth() - mClipRect.right,
                mSourceView.getHeight() - mClipRect.bottom,
                mCornerRadius);
    }

    /** Sets the view outline alpha. */
    void setAlpha(float alpha) {
        if (Float.compare(alpha, mAlpha) != 0) {
            mAlpha = alpha;
            mSourceView.invalidateOutline();
        }
    }

    /** Sets the bottom clip. */
    public void setClipBottom(int bottom) {
        mClipRect.bottom = bottom;
        updateClipBounds();
    }

    /** Returns the bottom clip. */
    public int getClipBottom() {
        return mClipRect.bottom;
    }

    private void updateClipBounds() {
        mClipBounds.set(Math.max(0, mClipRect.left), Math.max(0, mClipRect.top),
                mSourceView.getWidth() - Math.max(0, mClipRect.right),
                mSourceView.getHeight() - Math.max(0, mClipRect.bottom));
        if (!mLastClipBounds.equals(mClipBounds)) {
            mSourceView.setClipBounds(mClipBounds);
            mSourceView.invalidateOutline();
            mLastClipBounds.set(mClipBounds);
        }
    }
}