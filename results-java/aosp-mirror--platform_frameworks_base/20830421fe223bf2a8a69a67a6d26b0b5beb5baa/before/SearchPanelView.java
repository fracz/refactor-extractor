/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.systemui;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.internal.widget.multiwaveview.MultiWaveView;
import com.android.internal.widget.multiwaveview.MultiWaveView.OnTriggerListener;
import com.android.systemui.R;
import com.android.systemui.recent.StatusBarTouchProxy;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.tablet.StatusBarPanel;
import com.android.systemui.statusbar.tablet.TabletStatusBar;

public class SearchPanelView extends FrameLayout implements
        StatusBarPanel, Animator.AnimatorListener {
    static final String TAG = "SearchPanelView";
    static final boolean DEBUG = TabletStatusBar.DEBUG || PhoneStatusBar.DEBUG || false;
    private Context mContext;
    private BaseStatusBar mBar;
    private StatusBarTouchProxy mStatusBarTouchProxy;

    private boolean mShowing;
    private View mSearchTargetsContainer;
    private MultiWaveView mMultiWaveView;

    public SearchPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mSearchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        if (mSearchManager == null) {
            Slog.w(TAG, "Search manager not available");
        }
    }

    private SearchManager mSearchManager;

    public boolean isAssistantAvailable() {
        return mSearchManager != null && mSearchManager.getGlobalSearchActivity() != null;
    }

    private void startAssistActivity() {
        if (mSearchManager != null) {
            ComponentName globalSearchActivity = mSearchManager.getGlobalSearchActivity();
            if (globalSearchActivity != null) {
                Intent intent = new Intent(Intent.ACTION_ASSIST);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage(globalSearchActivity.getPackageName());
                try {
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Slog.w(TAG, "Activity not found for " + intent.getAction());
                }
            } else {
                Slog.w(TAG, "No global search activity");
            }
        }
    }

    final MultiWaveView.OnTriggerListener mMultiWaveViewListener
            = new MultiWaveView.OnTriggerListener() {

        public void onGrabbed(View v, int handle) {
        }

        public void onReleased(View v, int handle) {
        }

        public void onGrabbedStateChange(View v, int handle) {
            if (OnTriggerListener.NO_HANDLE == handle) {
                mBar.hideSearchPanel();
            }
        }

        public void onTrigger(View v, int target) {
            final int resId = mMultiWaveView.getResourceIdForTarget(target);
            switch (resId) {
                case com.android.internal.R.drawable.ic_lockscreen_search:
                    startAssistActivity();
                break;
            }
            mBar.hideSearchPanel();
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSearchTargetsContainer = (ViewGroup) findViewById(R.id.search_panel_container);
        mStatusBarTouchProxy = (StatusBarTouchProxy) findViewById(R.id.status_bar_touch_proxy);
        // TODO: fetch views
        mMultiWaveView = (MultiWaveView) findViewById(R.id.multi_wave_view);
        mMultiWaveView.setOnTriggerListener(mMultiWaveViewListener);
    }

    private boolean pointInside(int x, int y, View v) {
        final int l = v.getLeft();
        final int r = v.getRight();
        final int t = v.getTop();
        final int b = v.getBottom();
        return x >= l && x < r && y >= t && y < b;
    }

    public boolean isInContentArea(int x, int y) {
        if (pointInside(x, y, mSearchTargetsContainer)) {
            return true;
        } else if (mStatusBarTouchProxy != null &&
                pointInside(x, y, mStatusBarTouchProxy)) {
            return true;
        } else {
            return false;
        }
    }

    public void show(final boolean show, boolean animate) {
        if (animate) {
            if (mShowing != show) {
                mShowing = show;
                // TODO: start animating ring
            }
        } else {
            mShowing = show;
            onAnimationEnd(null);
        }
        postDelayed(new Runnable() {
            public void run() {
                setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                if (show) {
                    setFocusable(true);
                    setFocusableInTouchMode(true);
                    requestFocus();
                }
            }
        }, show ? 0 : 100);
    }

    public void hide(boolean animate) {
        if (mBar != null) {
            // This will indirectly cause show(false, ...) to get called
            mBar.animateCollapse();
        } else {
            setVisibility(View.INVISIBLE);
        }
    }

    public void onAnimationCancel(Animator animation) {
    }

    public void onAnimationEnd(Animator animation) {
        if (mShowing) {
            final LayoutTransition transitioner = new LayoutTransition();
            ((ViewGroup)mSearchTargetsContainer).setLayoutTransition(transitioner);
            createCustomAnimations(transitioner);
        } else {
            ((ViewGroup)mSearchTargetsContainer).setLayoutTransition(null);
        }
    }

    public void onAnimationRepeat(Animator animation) {
    }

    public void onAnimationStart(Animator animation) {
    }

    /**
     * We need to be aligned at the bottom.  LinearLayout can't do this, so instead,
     * let LinearLayout do all the hard work, and then shift everything down to the bottom.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // setPanelHeight(mSearchTargetsContainer.getHeight());
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        // Ignore hover events outside of this panel bounds since such events
        // generate spurious accessibility events with the panel content when
        // tapping outside of it, thus confusing the user.
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }

    /**
     * Whether the panel is showing, or, if it's animating, whether it will be
     * when the animation is done.
     */
    public boolean isShowing() {
        return mShowing;
    }

    public void setBar(BaseStatusBar bar) {
        mBar = bar;
    }

    public void setStatusBarView(final View statusBarView) {
        if (mStatusBarTouchProxy != null) {
            mStatusBarTouchProxy.setStatusBar(statusBarView);
//            mMultiWaveView.setOnTouchListener(new OnTouchListener() {
//                public boolean onTouch(View v, MotionEvent event) {
//                    return statusBarView.onTouchEvent(event);
//                }
//            });
        }
    }

    private void createCustomAnimations(LayoutTransition transitioner) {
        transitioner.setDuration(200);
        transitioner.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, null);
    }
}