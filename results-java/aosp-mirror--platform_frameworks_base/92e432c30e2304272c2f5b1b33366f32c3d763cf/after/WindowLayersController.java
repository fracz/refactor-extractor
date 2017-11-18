package com.android.server.wm;

import static android.view.WindowManager.LayoutParams.TYPE_DOCK_DIVIDER;
import static com.android.server.wm.WindowManagerDebugConfig.DEBUG_LAYERS;
import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;
import static com.android.server.wm.WindowManagerService.WINDOW_LAYER_MULTIPLIER;

import android.app.ActivityManager.StackId;
import android.util.Slog;
import android.view.Display;

import java.io.PrintWriter;

/**
 * Controller for assigning layers to windows on the display.
 *
 * This class encapsulates general algorithm for assigning layers and special rules that we need to
 * apply on top. The general algorithm goes through windows from bottom to the top and the higher
 * the window is, the higher layer is assigned. The final layer is equal to base layer +
 * adjustment from the order. This means that the window list is assumed to be ordered roughly by
 * the base layer (there are exceptions, e.g. due to keyguard and wallpaper and they need to be
 * handled with care, because they break the algorithm).
 *
 * On top of the general algorithm we add special rules, that govern such amazing things as:
 * <li>IME (which has higher base layer, but will be positioned above application windows)</li>
 * <li>docked/pinned windows (that need to be lifted above other application windows, including
 * animations)
 * <li>dock divider (which needs to live above applications, but below IME)</li>
 * <li>replaced windows, which need to live above their normal level, because they anticipate
 * an animation</li>.
 */
public class WindowLayersController {
    private final WindowManagerService mService;

    private int mInputMethodAnimLayerAdjustment;

    public WindowLayersController(WindowManagerService service) {
        mService = service;
    }

    private int mHighestApplicationLayer = 0;
    private WindowState mPinnedWindow = null;
    private WindowState mDockedWindow = null;
    private WindowState mDockDivider = null;
    private WindowState mImeWindow = null;
    private WindowState mReplacingWindow = null;

    final void assignLayersLocked(WindowList windows) {
        if (DEBUG_LAYERS) Slog.v(TAG_WM, "Assigning layers based on windows=" + windows,
                new RuntimeException("here").fillInStackTrace());

        clear();
        int curBaseLayer = 0;
        int curLayer = 0;
        boolean anyLayerChanged = false;
        for (int i = 0, windowCount = windows.size(); i < windowCount; i++) {
            final WindowState w = windows.get(i);
            boolean layerChanged = false;

            int oldLayer = w.mLayer;
            if (w.mBaseLayer == curBaseLayer || w.mIsImWindow || (i > 0 && w.mIsWallpaper)) {
                curLayer += WINDOW_LAYER_MULTIPLIER;
                w.mLayer = curLayer;
            } else {
                curBaseLayer = curLayer = w.mBaseLayer;
                w.mLayer = curLayer;
            }
            if (w.mLayer != oldLayer) {
                layerChanged = true;
                anyLayerChanged = true;
            }

            final WindowStateAnimator winAnimator = w.mWinAnimator;
            oldLayer = winAnimator.mAnimLayer;
            winAnimator.mAnimLayer = w.mLayer + w.getAnimLayerAdjustment() +
                    getSpecialWindowAnimLayerAdjustment(w);
            if (winAnimator.mAnimLayer != oldLayer) {
                layerChanged = true;
                anyLayerChanged = true;
            }

            if (w.mAppToken != null) {
                mHighestApplicationLayer = Math.max(mHighestApplicationLayer,
                        winAnimator.mAnimLayer);
            }
            collectSpecialWindows(w);

            if (layerChanged) {
                w.scheduleAnimationIfDimming();
            }
        }

        adjustSpecialWindows();

        //TODO (multidisplay): Magnification is supported only for the default display.
        if (mService.mAccessibilityController != null && anyLayerChanged
                && windows.get(windows.size() - 1).getDisplayId() == Display.DEFAULT_DISPLAY) {
            mService.mAccessibilityController.onWindowLayersChangedLocked();
        }

        if (DEBUG_LAYERS) logDebugLayers(windows);
    }

    void setInputMethodAnimLayerAdjustment(int adj) {
        if (DEBUG_LAYERS) Slog.v(TAG_WM, "Setting im layer adj to " + adj);
        mInputMethodAnimLayerAdjustment = adj;
        final WindowState imw = mService.mInputMethodWindow;
        if (imw != null) {
            imw.mWinAnimator.mAnimLayer = imw.mLayer + adj;
            if (DEBUG_LAYERS) Slog.v(TAG_WM, "IM win " + imw
                    + " anim layer: " + imw.mWinAnimator.mAnimLayer);
            for (int i = imw.mChildWindows.size() - 1; i >= 0; i--) {
                final WindowState childWindow = imw.mChildWindows.get(i);
                childWindow.mWinAnimator.mAnimLayer = childWindow.mLayer + adj;
                if (DEBUG_LAYERS) Slog.v(TAG_WM, "IM win " + childWindow
                        + " anim layer: " + childWindow.mWinAnimator.mAnimLayer);
            }
        }
        for (int i = mService.mInputMethodDialogs.size(); i >= 0; i--) {
            final WindowState dialog = mService.mInputMethodDialogs.get(i);
            dialog.mWinAnimator.mAnimLayer = dialog.mLayer + adj;
            if (DEBUG_LAYERS) Slog.v(TAG_WM, "IM win " + imw
                    + " anim layer: " + dialog.mWinAnimator.mAnimLayer);
        }
    }

    int getSpecialWindowAnimLayerAdjustment(WindowState win) {
        if (win.mIsImWindow) {
            return mInputMethodAnimLayerAdjustment;
        } else if (win.mIsWallpaper) {
            return mService.mWallpaperControllerLocked.getAnimLayerAdjustment();
        }
        return 0;
    }

    private void logDebugLayers(WindowList windows) {
        for (int i = 0, n = windows.size(); i < n; i++) {
            final WindowState w = windows.get(i);
            final WindowStateAnimator winAnimator = w.mWinAnimator;
            Slog.v(TAG_WM, "Assign layer " + w + ": " + "mBase=" + w.mBaseLayer
                    + " mLayer=" + w.mLayer + (w.mAppToken == null
                    ? "" : " mAppLayer=" + w.mAppToken.mAppAnimator.animLayerAdjustment)
                    + " =mAnimLayer=" + winAnimator.mAnimLayer);
        }
    }

    private void clear() {
        mHighestApplicationLayer = 0;
        mImeWindow = null;
        mPinnedWindow = null;
        mDockedWindow = null;
        mDockDivider = null;
    }

    private void collectSpecialWindows(WindowState w) {
        if (w.mIsImWindow) {
            mImeWindow = w;
        } else if (w.mAttrs.type == TYPE_DOCK_DIVIDER) {
            mDockDivider = w;
        } else {
            final TaskStack stack = w.getStack();
            if (stack.mStackId == StackId.PINNED_STACK_ID) {
                mPinnedWindow = w;
            } else if (stack.mStackId == StackId.DOCKED_STACK_ID) {
                mDockedWindow = w;
            }
        }
    }

    private void adjustSpecialWindows() {
        int layer = mHighestApplicationLayer + 1;
        // For pinned and docked stack window, we want to make them above other windows
        // also when these windows are animating.
        layer = assignAndIncreaseLayerIfNeeded(mDockedWindow, layer);
        layer = assignAndIncreaseLayerIfNeeded(mDockDivider, layer);
        // We know that we will be animating a relaunching window in the near future,
        // which will receive a z-order increase. We want the replaced window to
        // immediately receive the same treatment, e.g. to be above the dock divider.
        layer = assignAndIncreaseLayerIfNeeded(mReplacingWindow, layer);
        layer = assignAndIncreaseLayerIfNeeded(mPinnedWindow, layer);
        layer = assignAndIncreaseLayerIfNeeded(mImeWindow, layer);
    }

    private int assignAndIncreaseLayerIfNeeded(WindowState win, int layer) {
        if (win != null) {
            win.mLayer = layer;
            win.mWinAnimator.mAnimLayer = layer;
            layer++;
        }
        return layer;
    }

    void dump(PrintWriter pw, String s) {
        if (mInputMethodAnimLayerAdjustment != 0 ||
                mService.mWallpaperControllerLocked.getAnimLayerAdjustment() != 0) {
            pw.print("  mInputMethodAnimLayerAdjustment=");
            pw.print(mInputMethodAnimLayerAdjustment);
            pw.print("  mWallpaperAnimLayerAdjustment=");
            pw.println(mService.mWallpaperControllerLocked.getAnimLayerAdjustment());
        }
    }
}