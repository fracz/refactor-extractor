/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.server.wm;

import static com.android.server.wm.WindowManagerService.SHOW_TRANSACTIONS;
import static com.android.server.wm.WindowManagerService.SHOW_LIGHT_TRANSACTIONS;
import static com.android.server.wm.WindowManagerService.SHOW_SURFACE_ALLOC;
import static com.android.server.wm.WindowManagerService.DEBUG_SURFACE_TRACE;
import static com.android.server.wm.WindowManagerService.HIDE_STACK_CRAWLS;
import static com.android.server.wm.WindowManagerService.DEBUG_VISIBILITY;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.IBinder;
import android.os.Debug;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import android.view.Surface.OutOfResourcesException;

import android.util.Slog;

import java.io.PrintWriter;
import java.util.ArrayList;

class WindowSurfaceController {
    static final String TAG = "WindowSurfaceController";

    final WindowStateAnimator mAnimator;

    private SurfaceControl mSurfaceControl;

    private boolean mSurfaceShown = false;
    private float mSurfaceX = 0;
    private float mSurfaceY = 0;
    private float mSurfaceW = 0;
    private float mSurfaceH = 0;

    private float mSurfaceAlpha = 0;

    private int mSurfaceLayer = 0;
    private int mSurfaceFormat;

    // Surface flinger doesn't support crop rectangles where width or height is non-positive.
    // However, we need to somehow handle the situation where the cropping would completely hide
    // the window. We achieve this by explicitly hiding the surface and not letting it be shown.
    private boolean mHiddenForCrop = false;

    private final String title;

    public WindowSurfaceController(SurfaceSession s,
            String name, int w, int h, int format, int flags, WindowStateAnimator animator) {
        mAnimator = animator;

        mSurfaceW = w;
        mSurfaceH = h;
        mSurfaceFormat = format;

        title = name;

        if (DEBUG_SURFACE_TRACE) {
            mSurfaceControl = new SurfaceTrace(
                    s, name, w, h, format, flags);
        } else {
            mSurfaceControl = new SurfaceControl(
                    s, name, w, h, format, flags);
        }
    }


    void logSurface(String msg, RuntimeException where) {
        String str = "  SURFACE " + msg + ": " + title;
        if (where != null) {
            Slog.i(TAG, str, where);
        } else {
            Slog.i(TAG, str);
        }
    }

    void hideInTransaction(String reason) {
        if (SHOW_TRANSACTIONS) logSurface("HIDE ( " + reason + " )", null);
        if (mSurfaceControl != null) {
            mSurfaceShown = false;
            try {
                mSurfaceControl.hide();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Exception hiding surface in " + this);
            }
        }
    }

    void setPositionAndLayer(float left, float top, int layerStack, int layer) {
        SurfaceControl.openTransaction();
        try {
            mSurfaceX = left;
            mSurfaceY = top;

            try {
                mSurfaceControl.setPosition(left, top);
                mSurfaceControl.setLayerStack(layerStack);

                mSurfaceControl.setLayer(layer);
                mSurfaceControl.setAlpha(0);
                mSurfaceShown = false;
            } catch (RuntimeException e) {
                Slog.w(TAG, "Error creating surface in " + this, e);
                mAnimator.reclaimSomeSurfaceMemory("create-init", true);
            }
        } finally {
            SurfaceControl.closeTransaction();
            if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG,
                    "<<< CLOSE TRANSACTION setPositionAndLayer");
        }
    }

    void destroyInTransaction() {
        if (SHOW_TRANSACTIONS || SHOW_SURFACE_ALLOC) {
            RuntimeException e = null;
            if (!HIDE_STACK_CRAWLS) {
                e = new RuntimeException();
                e.fillInStackTrace();
            }
        }
        try {
            mSurfaceControl.destroy();
            mSurfaceShown = false;
            mSurfaceControl = null;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error destroying surface in: " + this, e);
        }
    }

    void setCropInTransaction(Rect clipRect, boolean recoveringMemory) {
        if (SHOW_TRANSACTIONS) logSurface(
                "CROP " + clipRect.toShortString(), null);
        try {
            if (clipRect.width() > 0 && clipRect.height() > 0) {
                mSurfaceControl.setWindowCrop(clipRect);
                mHiddenForCrop = false;
            } else {
                hideInTransaction("setCrop");
                mHiddenForCrop = true;
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error setting crop surface of " + this
                    + " crop=" + clipRect.toShortString(), e);
            if (!recoveringMemory) {
                mAnimator.reclaimSomeSurfaceMemory("crop", true);
            }
        }
    }

    void setLayer(int layer) {
        if (mSurfaceControl != null) {
            SurfaceControl.openTransaction();
            try {
                mSurfaceControl.setLayer(layer);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    void setPositionInTransaction(float left, float top, boolean recoveringMemory) {
        final boolean surfaceMoved = mSurfaceX != left || mSurfaceY != top;
        if (surfaceMoved) {
            mSurfaceX = left;
            mSurfaceY = top;

            try {
                mSurfaceControl.setPosition(left, top);
            } catch (RuntimeException e) {
                Slog.w(TAG, "Error positioning surface of " + this
                        + " pos=(" + left + "," + top + ")", e);
                if (!recoveringMemory) {
                    mAnimator.reclaimSomeSurfaceMemory("position", true);
                }
            }
        }
    }

    boolean setSizeInTransaction(int width, int height, float dsdx, float dtdx, float dsdy, float dtdy,
            boolean recoveringMemory) {
        final boolean surfaceResized = mSurfaceW != width || mSurfaceH != height;
        if (surfaceResized) {
            mSurfaceW = width;
            mSurfaceH = height;

            try {
                if (SHOW_TRANSACTIONS) logSurface(
                        "SIZE " + width + "x" + height, null);
                mSurfaceControl.setSize(width, height);
                mSurfaceControl.setMatrix(
                        dsdx, dtdx, dsdy, dtdy);
            } catch (RuntimeException e) {
                // If something goes wrong with the surface (such
                // as running out of memory), don't take down the
                // entire system.
                Slog.e(TAG, "Error resizing surface of " + title
                        + " size=(" + width + "x" + height + ")", e);
                if (!recoveringMemory) {
                    mAnimator.reclaimSomeSurfaceMemory("size", true);
                }
                return false;
            }
            return true;
        }
        return false;
    }

    boolean prepareToShowInTransaction(float alpha, int layer, float dsdx, float dtdx, float dsdy,
            float dtdy, boolean recoveringMemory) {
        if (mSurfaceControl != null) {
            try {
                mSurfaceAlpha = alpha;
                mSurfaceControl.setAlpha(alpha);
                mSurfaceLayer = layer;
                mSurfaceControl.setLayer(layer);
                mSurfaceControl.setMatrix(
                        dsdx, dtdx, dsdy, dtdy);

            } catch (RuntimeException e) {
                Slog.w(TAG, "Error updating surface in " + title, e);
                if (!recoveringMemory) {
                    mAnimator.reclaimSomeSurfaceMemory("update", true);
                }
                return false;
            }
        }
        return true;
    }

    void setTransparentRegionHint(final Region region) {
        if (mSurfaceControl == null) {
            Slog.w(TAG, "setTransparentRegionHint: null mSurface after mHasSurface true");
            return;
        }
        if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG, ">>> OPEN TRANSACTION setTransparentRegion");
        SurfaceControl.openTransaction();
        try {
            mSurfaceControl.setTransparentRegionHint(region);
        } finally {
            SurfaceControl.closeTransaction();
            if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG,
                    "<<< CLOSE TRANSACTION setTransparentRegion");
        }
    }

    void setOpaque(boolean isOpaque) {
        if (SHOW_TRANSACTIONS) logSurface("isOpaque=" + isOpaque,
                null);

        if (mSurfaceControl == null) {
            return;
        }
        if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG, ">>> OPEN TRANSACTION setOpaqueLocked");
        SurfaceControl.openTransaction();
        try {
            mSurfaceControl.setOpaque(isOpaque);
        } finally {
            SurfaceControl.closeTransaction();
            if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG, "<<< CLOSE TRANSACTION setOpaqueLocked");
        }
    }

    void setSecure(boolean isSecure) {
        if (SHOW_TRANSACTIONS) logSurface("isSecure=" + isSecure,
                null);

        if (mSurfaceControl == null) {
            return;
        }
        if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG, ">>> OPEN TRANSACTION setSecureLocked");
        SurfaceControl.openTransaction();
        try {
            mSurfaceControl.setSecure(isSecure);
        } finally {
            SurfaceControl.closeTransaction();
            if (SHOW_LIGHT_TRANSACTIONS) Slog.i(TAG, "<<< CLOSE TRANSACTION setSecureLocked");
        }
    }

    boolean showRobustlyInTransaction() {
        if (SHOW_TRANSACTIONS) logSurface(
                "SHOW (performLayout)", null);
        if (DEBUG_VISIBILITY) Slog.v(TAG, "Showing " + this
                + " during relayout");

        if (mHiddenForCrop) {
            return false;
        }

        try {
            mSurfaceShown = true;
            mSurfaceControl.show();
            return true;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Failure showing surface " + mSurfaceControl + " in " + this, e);
        }

        mAnimator.reclaimSomeSurfaceMemory("show", true);

        return false;
    }

    void deferTransactionUntil(IBinder handle, long frame) {
        // TODO: Logging
        mSurfaceControl.deferTransactionUntil(handle, frame);
    }

    boolean clearWindowContentFrameStats() {
        if (mSurfaceControl == null) {
            return false;
        }
        return mSurfaceControl.clearContentFrameStats();
    }

    boolean getWindowContentFrameStats(WindowContentFrameStats outStats) {
        if (mSurfaceControl == null) {
            return false;
        }
        return mSurfaceControl.getContentFrameStats(outStats);
    }


    boolean hasSurface() {
        return mSurfaceControl != null;
    }

    IBinder getHandle() {
        if (mSurfaceControl == null) {
            return null;
        }
        return mSurfaceControl.getHandle();
    }

    void getSurface(Surface outSurface) {
        outSurface.copyFrom(mSurfaceControl);
    }

    int getLayer() {
        return mSurfaceLayer;
    }

    boolean getShown() {
        return mSurfaceShown;
    }

    float getX() {
        return mSurfaceX;
    }

    float getY() {
        return mSurfaceY;
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (dumpAll) {
            pw.print(prefix); pw.print("mSurface="); pw.println(mSurfaceControl);
        }
        pw.print(prefix); pw.print("Surface: shown="); pw.print(mSurfaceShown);
        pw.print(" layer="); pw.print(mSurfaceLayer);
        pw.print(" alpha="); pw.print(mSurfaceAlpha);
        pw.print(" rect=("); pw.print(mSurfaceX);
        pw.print(","); pw.print(mSurfaceY);
        pw.print(") "); pw.print(mSurfaceW);
        pw.print(" x "); pw.println(mSurfaceH);
    }

    static class SurfaceTrace extends SurfaceControl {
        private final static String SURFACE_TAG = "SurfaceTrace";
        private final static boolean LOG_SURFACE_TRACE = DEBUG_SURFACE_TRACE;
        final static ArrayList<SurfaceTrace> sSurfaces = new ArrayList<SurfaceTrace>();

        private float mSurfaceTraceAlpha = 0;
        private int mLayer;
        private final PointF mPosition = new PointF();
        private final Point mSize = new Point();
        private final Rect mWindowCrop = new Rect();
        private boolean mShown = false;
        private int mLayerStack;
        private boolean mIsOpaque;
        private float mDsdx, mDtdx, mDsdy, mDtdy;
        private final String mName;

        public SurfaceTrace(SurfaceSession s,
                       String name, int w, int h, int format, int flags)
                   throws OutOfResourcesException {
            super(s, name, w, h, format, flags);
            mName = name != null ? name : "Not named";
            mSize.set(w, h);
            if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "ctor: " + this + ". Called by "
                    + Debug.getCallers(3));
            synchronized (sSurfaces) {
                sSurfaces.add(0, this);
            }
        }

        @Override
        public void setAlpha(float alpha) {
            if (mSurfaceTraceAlpha != alpha) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "setAlpha(" + alpha + "): OLD:" + this +
                        ". Called by " + Debug.getCallers(3));
                mSurfaceTraceAlpha = alpha;
            }
            super.setAlpha(alpha);
        }

        @Override
        public void setLayer(int zorder) {
            if (zorder != mLayer) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "setLayer(" + zorder + "): OLD:" + this
                        + ". Called by " + Debug.getCallers(3));
                mLayer = zorder;
            }
            super.setLayer(zorder);

            synchronized (sSurfaces) {
                sSurfaces.remove(this);
                int i;
                for (i = sSurfaces.size() - 1; i >= 0; i--) {
                    SurfaceTrace s = sSurfaces.get(i);
                    if (s.mLayer < zorder) {
                        break;
                    }
                }
                sSurfaces.add(i + 1, this);
            }
        }

        @Override
        public void setPosition(float x, float y) {
            if (x != mPosition.x || y != mPosition.y) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "setPosition(" + x + "," + y + "): OLD:"
                        + this + ". Called by " + Debug.getCallers(3));
                mPosition.set(x, y);
            }
            super.setPosition(x, y);
        }

        @Override
        public void setSize(int w, int h) {
            if (w != mSize.x || h != mSize.y) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "setSize(" + w + "," + h + "): OLD:"
                        + this + ". Called by " + Debug.getCallers(3));
                mSize.set(w, h);
            }
            super.setSize(w, h);
        }

        @Override
        public void setWindowCrop(Rect crop) {
            if (crop != null) {
                if (!crop.equals(mWindowCrop)) {
                    if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "setWindowCrop("
                            + crop.toShortString() + "): OLD:" + this + ". Called by "
                            + Debug.getCallers(3));
                    mWindowCrop.set(crop);
                }
            }
            super.setWindowCrop(crop);
        }

        @Override
        public void setLayerStack(int layerStack) {
            if (layerStack != mLayerStack) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "setLayerStack(" + layerStack + "): OLD:"
                        + this + ". Called by " + Debug.getCallers(3));
                mLayerStack = layerStack;
            }
            super.setLayerStack(layerStack);
        }

        @Override
        public void setOpaque(boolean isOpaque) {
            if (isOpaque != mIsOpaque) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "setOpaque(" + isOpaque + "): OLD:"
                        + this + ". Called by " + Debug.getCallers(3));
                mIsOpaque = isOpaque;
            }
            super.setOpaque(isOpaque);
        }

        @Override
        public void setSecure(boolean isSecure) {
            super.setSecure(isSecure);
        }

        @Override
        public void setMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
            if (dsdx != mDsdx || dtdx != mDtdx || dsdy != mDsdy || dtdy != mDtdy) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "setMatrix(" + dsdx + "," + dtdx + ","
                        + dsdy + "," + dtdy + "): OLD:" + this + ". Called by "
                        + Debug.getCallers(3));
                mDsdx = dsdx;
                mDtdx = dtdx;
                mDsdy = dsdy;
                mDtdy = dtdy;
            }
            super.setMatrix(dsdx, dtdx, dsdy, dtdy);
        }

        @Override
        public void hide() {
            if (mShown) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "hide: OLD:" + this + ". Called by "
                        + Debug.getCallers(3));
                mShown = false;
            }
            super.hide();
        }

        @Override
        public void show() {
            if (!mShown) {
                if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "show: OLD:" + this + ". Called by "
                        + Debug.getCallers(3));
                mShown = true;
            }
            super.show();
        }

        @Override
        public void destroy() {
            super.destroy();
            if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "destroy: " + this + ". Called by "
                    + Debug.getCallers(3));
            synchronized (sSurfaces) {
                sSurfaces.remove(this);
            }
        }

        @Override
        public void release() {
            super.release();
            if (LOG_SURFACE_TRACE) Slog.v(SURFACE_TAG, "release: " + this + ". Called by "
                    + Debug.getCallers(3));
            synchronized (sSurfaces) {
                sSurfaces.remove(this);
            }
        }

        static void dumpAllSurfaces(PrintWriter pw, String header) {
            synchronized (sSurfaces) {
                final int N = sSurfaces.size();
                if (N <= 0) {
                    return;
                }
                if (header != null) {
                    pw.println(header);
                }
                pw.println("WINDOW MANAGER SURFACES (dumpsys window surfaces)");
                for (int i = 0; i < N; i++) {
                    SurfaceTrace s = sSurfaces.get(i);
                    pw.print("  Surface #"); pw.print(i); pw.print(": #");
                            pw.print(Integer.toHexString(System.identityHashCode(s)));
                            pw.print(" "); pw.println(s.mName);
                    pw.print("    mLayerStack="); pw.print(s.mLayerStack);
                            pw.print(" mLayer="); pw.println(s.mLayer);
                    pw.print("    mShown="); pw.print(s.mShown); pw.print(" mAlpha=");
                            pw.print(s.mSurfaceTraceAlpha); pw.print(" mIsOpaque=");
                            pw.println(s.mIsOpaque);
                    pw.print("    mPosition="); pw.print(s.mPosition.x); pw.print(",");
                            pw.print(s.mPosition.y);
                            pw.print(" mSize="); pw.print(s.mSize.x); pw.print("x");
                            pw.println(s.mSize.y);
                    pw.print("    mCrop="); s.mWindowCrop.printShortString(pw); pw.println();
                    pw.print("    Transform: ("); pw.print(s.mDsdx); pw.print(", ");
                            pw.print(s.mDtdx); pw.print(", "); pw.print(s.mDsdy);
                            pw.print(", "); pw.print(s.mDtdy); pw.println(")");
                }
            }
        }

        @Override
        public String toString() {
            return "Surface " + Integer.toHexString(System.identityHashCode(this)) + " "
                    + mName + " (" + mLayerStack + "): shown=" + mShown + " layer=" + mLayer
                    + " alpha=" + mSurfaceTraceAlpha + " " + mPosition.x + "," + mPosition.y
                    + " " + mSize.x + "x" + mSize.y
                    + " crop=" + mWindowCrop.toShortString()
                    + " opaque=" + mIsOpaque
                    + " (" + mDsdx + "," + mDtdx + "," + mDsdy + "," + mDtdy + ")";
        }
    }
}