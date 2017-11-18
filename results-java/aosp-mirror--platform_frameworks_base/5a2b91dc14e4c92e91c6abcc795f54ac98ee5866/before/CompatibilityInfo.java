/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.content.res;

import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * CompatibilityInfo class keeps the information about compatibility mode that the application is
 * running under.
 *
 *  {@hide}
 */
public class CompatibilityInfo {
    private static final boolean DBG = false;
    private static final String TAG = "CompatibilityInfo";

    /** default compatibility info object for compatible applications */
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo();

    /**
     * The default width of the screen in portrait mode.
     */
    public static final int DEFAULT_PORTRAIT_WIDTH = 320;

    /**
     * The default height of the screen in portrait mode.
     */
    public static final int DEFAULT_PORTRAIT_HEIGHT = 480;

    /**
     *  A compatibility flags
     */
    private int mCompatibilityFlags;

    /**
     * A flag mask to tell if the application needs scaling (when mApplicationScale != 1.0f)
     * {@see compatibilityFlag}
     */
    private static final int SCALING_REQUIRED = 1;

    /**
     * A flag mask to indicates that the application can expand over the original size.
     * The flag is set to true if
     * 1) Application declares its expandable in manifest file using <expandable /> or
     * 2) The screen size is same as (320 x 480) * density.
     * {@see compatibilityFlag}
     */
    private static final int EXPANDABLE = 2;

    /**
     * A flag mask to tell if the application is configured to be expandable. This differs
     * from EXPANDABLE in that the application that is not expandable will be
     * marked as expandable if it runs in (320x 480) * density screen size.
     */
    private static final int CONFIGURED_EXPANDABLE = 4;

    private static final int SCALING_EXPANDABLE_MASK = SCALING_REQUIRED | EXPANDABLE;

    /**
     * Application's scale.
     */
    public final float applicationScale;

    /**
     * Application's inverted scale.
     */
    public final float applicationInvertedScale;

    /**
     * The flags from ApplicationInfo.
     */
    public final int appFlags;

    public CompatibilityInfo(ApplicationInfo appInfo) {
        appFlags = appInfo.flags;

        if ((appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS) != 0) {
            mCompatibilityFlags = EXPANDABLE | CONFIGURED_EXPANDABLE;
        }

        float packageDensityScale = -1.0f;
        if (appInfo.supportsDensities != null) {
            int minDiff = Integer.MAX_VALUE;
            for (int density : appInfo.supportsDensities) {
                if (density == ApplicationInfo.ANY_DENSITY) {
                    packageDensityScale = 1.0f;
                    break;
                }
                int tmpDiff = Math.abs(DisplayMetrics.DEVICE_DENSITY - density);
                if (tmpDiff == 0) {
                    packageDensityScale = 1.0f;
                    break;
                }
                // prefer higher density (appScale>1.0), unless that's only option.
                if (tmpDiff < minDiff && packageDensityScale < 1.0f) {
                    packageDensityScale = DisplayMetrics.DEVICE_DENSITY / (float) density;
                    minDiff = tmpDiff;
                }
            }
        }
        if (packageDensityScale > 0.0f) {
            applicationScale = packageDensityScale;
        } else {
            applicationScale =
                    DisplayMetrics.DEVICE_DENSITY / (float) DisplayMetrics.DEFAULT_DENSITY;
        }

        applicationInvertedScale = 1.0f / applicationScale;
        if (applicationScale != 1.0f) {
            mCompatibilityFlags |= SCALING_REQUIRED;
        }
    }

    private CompatibilityInfo(int appFlags, int compFlags, float scale, float invertedScale) {
        this.appFlags = appFlags;
        mCompatibilityFlags = compFlags;
        applicationScale = scale;
        applicationInvertedScale = invertedScale;
    }

    private CompatibilityInfo() {
        this(ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS
                | ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS
                | ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS,
                EXPANDABLE | CONFIGURED_EXPANDABLE,
                1.0f,
                1.0f);
    }

    /**
     * Returns the copy of this instance.
     */
    public CompatibilityInfo copy() {
        CompatibilityInfo info = new CompatibilityInfo(appFlags, mCompatibilityFlags,
                applicationScale, applicationInvertedScale);
        return info;
    }

    /**
     * Sets expandable bit in the compatibility flag.
     */
    public void setExpandable(boolean expandable) {
        if (expandable) {
            mCompatibilityFlags |= CompatibilityInfo.EXPANDABLE;
        } else {
            mCompatibilityFlags &= ~CompatibilityInfo.EXPANDABLE;
        }
    }

    /**
     * @return true if the application is configured to be expandable.
     */
    public boolean isConfiguredExpandable() {
        return (mCompatibilityFlags & CompatibilityInfo.CONFIGURED_EXPANDABLE) != 0;
    }

    /**
     * @return true if the scaling is required
     */
    public boolean isScalingRequired() {
        return (mCompatibilityFlags & SCALING_REQUIRED) != 0;
    }

    public boolean supportsScreen() {
        return (mCompatibilityFlags & CompatibilityInfo.EXPANDABLE) != 0;
    }

    @Override
    public String toString() {
        return "CompatibilityInfo{scale=" + applicationScale +
                ", compatibility flag=" + mCompatibilityFlags + "}";
    }

    /**
     * Returns the translator which can translate the coordinates of the window.
     * There are five different types of Translator.
     * @param params the window's parameter
     */
    public Translator getTranslator(WindowManager.LayoutParams params) {
        if ( (mCompatibilityFlags & CompatibilityInfo.SCALING_EXPANDABLE_MASK)
                == CompatibilityInfo.EXPANDABLE) {
            if (DBG) Log.d(TAG, "no translation required");
            return null;
        }
        if (!isScalingRequired() ||
            (params.flags & WindowManager.LayoutParams.FLAG_NO_COMPATIBILITY_SCALING) != 0) {
            return null;
        }
        return new Translator();
    }

    /**
     * A helper object to translate the screen and window coordinates back and forth.
     * @hide
     */
    public class Translator {
        final public float applicationScale;
        final public float applicationInvertedScale;

        private Rect mContentInsetsBuffer = null;
        private Rect mVisibleInsetsBuffer = null;

        Translator(float applicationScale, float applicationInvertedScale) {
            this.applicationScale = applicationScale;
            this.applicationInvertedScale = applicationInvertedScale;
        }

        Translator() {
            this(CompatibilityInfo.this.applicationScale,
                    CompatibilityInfo.this.applicationInvertedScale);
        }

        /**
         * Translate the screen rect to the application frame.
         */
        public void translateRectInScreenToAppWinFrame(Rect rect) {
            rect.scale(applicationInvertedScale);
        }

        /**
         * Translate the region in window to screen.
         */
        public void translateRegionInWindowToScreen(Region transparentRegion) {
            transparentRegion.scale(applicationScale);
        }

        /**
         * Apply translation to the canvas that is necessary to draw the content.
         */
        public void translateCanvas(Canvas canvas) {
            canvas.scale(applicationScale, applicationScale);
        }

        /**
         * Translate the motion event captured on screen to the application's window.
         */
        public void translateEventInScreenToAppWindow(MotionEvent event) {
            event.scale(applicationInvertedScale);
        }

        /**
         * Translate the window's layout parameter, from application's view to
         * Screen's view.
         */
        public void translateWindowLayout(WindowManager.LayoutParams params) {
            params.scale(applicationScale);
        }

        /**
         * Translate a Rect in application's window to screen.
         */
        public void translateRectInAppWindowToScreen(Rect rect) {
            rect.scale(applicationScale);
        }

        /**
         * Translate a Rect in screen coordinates into the app window's coordinates.
         */
        public void translateRectInScreenToAppWindow(Rect rect) {
            rect.scale(applicationInvertedScale);
        }

        /**
         * Translate the location of the sub window.
         * @param params
         */
        public void translateLayoutParamsInAppWindowToScreen(LayoutParams params) {
            params.scale(applicationScale);
        }

        /**
         * Translate the content insets in application window to Screen. This uses
         * the internal buffer for content insets to avoid extra object allocation.
         */
        public Rect getTranslatedContentInsets(Rect contentInsets) {
            if (mContentInsetsBuffer == null) mContentInsetsBuffer = new Rect();
            mContentInsetsBuffer.set(contentInsets);
            translateRectInAppWindowToScreen(mContentInsetsBuffer);
            return mContentInsetsBuffer;
        }

        /**
         * Translate the visible insets in application window to Screen. This uses
         * the internal buffer for content insets to avoid extra object allocation.
         */
        public Rect getTranslatedVisbileInsets(Rect visibleInsets) {
            if (mVisibleInsetsBuffer == null) mVisibleInsetsBuffer = new Rect();
            mVisibleInsetsBuffer.set(visibleInsets);
            translateRectInAppWindowToScreen(mVisibleInsetsBuffer);
            return mVisibleInsetsBuffer;
        }
    }

    /**
     * Returns the frame Rect for applications runs under compatibility mode.
     *
     * @param dm the display metrics used to compute the frame size.
     * @param orientation the orientation of the screen.
     * @param outRect the output parameter which will contain the result.
     */
    public static void updateCompatibleScreenFrame(DisplayMetrics dm, int orientation,
            Rect outRect) {
        int width = dm.widthPixels;
        int portraitHeight = (int) (DEFAULT_PORTRAIT_HEIGHT * dm.density);
        int portraitWidth = (int) (DEFAULT_PORTRAIT_WIDTH * dm.density);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int xOffset = (width - portraitHeight) / 2 ;
            outRect.set(xOffset, 0, xOffset + portraitHeight, portraitWidth);
        } else {
            int xOffset = (width - portraitWidth) / 2 ;
            outRect.set(xOffset, 0, xOffset + portraitWidth, portraitHeight);
        }
    }
}