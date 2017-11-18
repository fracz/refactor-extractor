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
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.recents.model.Task;


/** The task thumbnail view */
public class TaskThumbnailView extends FixedSizeImageView {

    // Task bar clipping
    Rect mClipRect = new Rect();

    public TaskThumbnailView(Context context) {
        this(context, null);
    }

    public TaskThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskThumbnailView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TaskThumbnailView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setScaleType(ScaleType.FIT_XY);
    }

    /** Updates the clip rect based on the given task bar. */
    void enableTaskBarClip(View taskBar) {
        int top = (int) Math.max(0, taskBar.getTranslationY() +
                taskBar.getMeasuredHeight() - 1);
        mClipRect.set(0, top, getMeasuredWidth(), getMeasuredHeight());
        setClipBounds(mClipRect);
    }

    /** Convenience method to enable task bar clipping as a runnable. */
    Runnable enableTaskBarClipAsRunnable(final View taskBar) {
        return new Runnable() {
            @Override
            public void run() {
                enableTaskBarClip(taskBar);
            }
        };
    }

    /** Disables the task bar clipping. */
    Runnable disableTaskBarClipAsRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                mClipRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
                setClipBounds(mClipRect);
            }
        };
    }

    /** Binds the thumbnail view to the screenshot. */
    boolean bindToScreenshot(Bitmap ss) {
        if (ss != null) {
            setImageBitmap(ss);
            return true;
        }
        return false;
    }

    /** Unbinds the thumbnail view from the screenshot. */
    void unbindFromScreenshot() {
        setImageBitmap(null);
    }

    /** Binds the thumbnail view to the task */
    void rebindToTask(Task t) {
        if (t.thumbnail != null) {
            setImageBitmap(t.thumbnail);
        }
    }

    /** Unbinds the thumbnail view from the task */
    void unbindFromTask() {
        setImageDrawable(null);
    }
}