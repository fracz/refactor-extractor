/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.statusbar.tablet;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.ActivityManagerNative;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Slog;
import android.view.Gravity;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.StatusBarNotification;

import com.android.systemui.R;
import com.android.systemui.statusbar.*;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.recent.RecentApplicationsActivity;

public class TabletStatusBar extends StatusBar implements
        HeightReceiver.OnBarHeightChangedListener,
        InputMethodsPanel.OnHardKeyboardEnabledChangeListener {
    public static final boolean DEBUG = false;
    public static final String TAG = "TabletStatusBar";

    public static final int MAX_NOTIFICATION_ICONS = 5;
    // IME switcher icon is big and occupy width of two icons
    public static final int MAX_NOTIFICATION_ICONS_IME_BUTTON_VISIBLE = MAX_NOTIFICATION_ICONS - 1;

    public static final int MSG_OPEN_NOTIFICATION_PANEL = 1000;
    public static final int MSG_CLOSE_NOTIFICATION_PANEL = 1001;
    public static final int MSG_OPEN_NOTIFICATION_PEEK = 1002;
    public static final int MSG_CLOSE_NOTIFICATION_PEEK = 1003;
    public static final int MSG_OPEN_RECENTS_PANEL = 1020;
    public static final int MSG_CLOSE_RECENTS_PANEL = 1021;
    public static final int MSG_SHOW_CHROME = 1030;
    public static final int MSG_HIDE_CHROME = 1031;
    public static final int MSG_OPEN_INPUT_METHODS_PANEL = 1040;
    public static final int MSG_CLOSE_INPUT_METHODS_PANEL = 1041;
    public static final int MSG_STOP_TICKER = 2000;

    // Fitts' Law assistance for LatinIME; TODO: replace with a more general approach
    private static final boolean FAKE_SPACE_BAR = true;

    // The height of the bar, as definied by the build.  It may be taller if we're plugged
    // into hdmi.
    int mNaturalBarHeight = -1;
    int mIconSize = -1;
    int mIconHPadding = -1;

    H mHandler = new H();

    IWindowManager mWindowManager;

    // tracking all current notifications
    private NotificationData mNotns = new NotificationData();

    TabletStatusBarView mStatusBarView;
    View mNotificationArea;
    View mNotificationTrigger;
    NotificationIconArea mNotificationIconArea;
    View mNavigationArea;

    ImageView mBackButton;
    View mHomeButton;
    View mMenuButton;
    View mRecentButton;

    ViewGroup mNotificationAndImeArea;
    InputMethodButton mInputMethodSwitchButton;

    NotificationPanel mNotificationPanel;
    NotificationPeekPanel mNotificationPeekWindow;
    ViewGroup mNotificationPeekRow;
    int mNotificationPeekIndex;
    IBinder mNotificationPeekKey;
    LayoutTransition mNotificationPeekScrubLeft, mNotificationPeekScrubRight;

    int mNotificationPeekTapDuration;
    int mNotificationFlingVelocity;

    ViewGroup mPile;

    HeightReceiver mHeightReceiver;
    BatteryController mBatteryController;
    BluetoothController mBluetoothController;
    LocationController mLocationController;
    NetworkController mNetworkController;

    View mBarContents;

    // hide system chrome ("lights out") support
    View mShadow;

    NotificationIconArea.IconLayout mIconLayout;

    TabletTicker mTicker;

    View mFakeSpaceBar;
    KeyEvent mSpaceBarKeyEvent = null;

    // for disabling the status bar
    int mDisabled = 0;

    boolean mNotificationsOn = true;
    private RecentAppsPanel mRecentsPanel;
    private InputMethodsPanel mInputMethodsPanel;

    public Context getContext() { return mContext; }

    protected void addPanelWindows() {
        final Context context = mContext;

        // Notification Panel
        mNotificationPanel = (NotificationPanel)View.inflate(context,
                R.layout.status_bar_notification_panel, null);
        mNotificationPanel.show(false, false);
        mNotificationPanel.setOnTouchListener(
                new TouchOutsideListener(MSG_CLOSE_NOTIFICATION_PANEL, mNotificationPanel));

        // the battery and network icons
        mBatteryController.addIconView((ImageView)mNotificationPanel.findViewById(R.id.battery));
        mBatteryController.addLabelView(
                (TextView)mNotificationPanel.findViewById(R.id.battery_text));
        mBluetoothController.addIconView(
                (ImageView)mNotificationPanel.findViewById(R.id.bluetooth));
        mNetworkController.addCombinedSignalIconView(
                (ImageView)mNotificationPanel.findViewById(R.id.network_signal));
        mNetworkController.addDataTypeIconView(
                (ImageView)mNotificationPanel.findViewById(R.id.network_type));
        mNetworkController.addDataDirectionOverlayIconView(
                (ImageView)mNotificationPanel.findViewById(R.id.network_direction));
        mNetworkController.addLabelView(
                (TextView)mNotificationPanel.findViewById(R.id.network_text));
        mNetworkController.addLabelView(
                (TextView)mBarContents.findViewById(R.id.network_text));

        mStatusBarView.setIgnoreChildren(0, mNotificationTrigger, mNotificationPanel);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                512, // ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        lp.setTitle("NotificationPanel");
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        lp.windowAnimations = com.android.internal.R.style.Animation; // == no animation
//        lp.windowAnimations = com.android.internal.R.style.Animation_ZoomButtons; // simple fade

        WindowManagerImpl.getDefault().addView(mNotificationPanel, lp);

        // Notification preview window
        mNotificationPeekWindow = (NotificationPeekPanel) View.inflate(context,
                R.layout.status_bar_notification_peek, null);
        mNotificationPeekRow = (ViewGroup) mNotificationPeekWindow.findViewById(R.id.content);
        mNotificationPeekWindow.setVisibility(View.GONE);
        mNotificationPeekWindow.setOnTouchListener(
                new TouchOutsideListener(MSG_CLOSE_NOTIFICATION_PEEK, mNotificationPeekWindow));
        mNotificationPeekScrubRight = new LayoutTransition();
        mNotificationPeekScrubRight.setAnimator(LayoutTransition.APPEARING,
                ObjectAnimator.ofInt(null, "left", -512, 0));
        mNotificationPeekScrubRight.setAnimator(LayoutTransition.DISAPPEARING,
                ObjectAnimator.ofInt(null, "left", -512, 0));
        mNotificationPeekScrubRight.setDuration(500);

        mNotificationPeekScrubLeft = new LayoutTransition();
        mNotificationPeekScrubLeft.setAnimator(LayoutTransition.APPEARING,
                ObjectAnimator.ofInt(null, "left", 512, 0));
        mNotificationPeekScrubLeft.setAnimator(LayoutTransition.DISAPPEARING,
                ObjectAnimator.ofInt(null, "left", 512, 0));
        mNotificationPeekScrubLeft.setDuration(500);

        // XXX: setIgnoreChildren?
        lp = new WindowManager.LayoutParams(
                512, // ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        lp.setTitle("NotificationPeekWindow");
        lp.windowAnimations = com.android.internal.R.style.Animation_Toast;

        WindowManagerImpl.getDefault().addView(mNotificationPeekWindow, lp);

        // Recents Panel
        mRecentsPanel = (RecentAppsPanel) View.inflate(context,
                R.layout.status_bar_recent_panel, null);
        mRecentsPanel.setVisibility(View.GONE);
        mRecentsPanel.setOnTouchListener(new TouchOutsideListener(MSG_CLOSE_RECENTS_PANEL,
                mRecentsPanel));
        mStatusBarView.setIgnoreChildren(2, mRecentButton, mRecentsPanel);

        lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.setTitle("RecentsPanel");
        lp.windowAnimations = R.style.Animation_RecentPanel;

        WindowManagerImpl.getDefault().addView(mRecentsPanel, lp);
        mRecentsPanel.setBar(this);

        // Input methods Panel
        mInputMethodsPanel = (InputMethodsPanel) View.inflate(context,
                R.layout.status_bar_input_methods_panel, null);
        mInputMethodsPanel.setHardKeyboardEnabledChangeListener(this);
        mInputMethodsPanel.setOnTouchListener(new TouchOutsideListener(
                MSG_CLOSE_INPUT_METHODS_PANEL, mInputMethodsPanel));
        mInputMethodsPanel.setImeSwitchButton(mInputMethodSwitchButton);
        mStatusBarView.setIgnoreChildren(3, mInputMethodSwitchButton, mInputMethodsPanel);
        lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        lp.setTitle("InputMethodsPanel");
        lp.windowAnimations = R.style.Animation_RecentPanel;

        WindowManagerImpl.getDefault().addView(mInputMethodsPanel, lp);
    }

    @Override
    public void start() {
        super.start(); // will add the main bar view
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        loadDimens();
    }

    protected void loadDimens() {
        final Resources res = mContext.getResources();

        mNaturalBarHeight = res.getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_height);

        int newIconSize = res.getDimensionPixelSize(
            com.android.internal.R.dimen.status_bar_icon_size);
        int newIconHPadding = res.getDimensionPixelSize(
            R.dimen.status_bar_icon_padding);

        if (newIconHPadding != mIconHPadding || newIconSize != mIconSize) {
//            Slog.d(TAG, "size=" + newIconSize + " padding=" + newIconHPadding);
            mIconHPadding = newIconHPadding;
            mIconSize = newIconSize;
            reloadAllNotificationIcons(); // reload the tray
        }
    }

    protected View makeStatusBarView() {
        final Context context = mContext;

        mWindowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));

        // This guy will listen for HDMI plugged broadcasts so we can resize the
        // status bar as appropriate.
        mHeightReceiver = new HeightReceiver(mContext);
        mHeightReceiver.registerReceiver();
        loadDimens();

        final TabletStatusBarView sb = (TabletStatusBarView)View.inflate(
                context, R.layout.status_bar, null);
        mStatusBarView = sb;

        sb.setHandler(mHandler);

        mBarContents = sb.findViewById(R.id.bar_contents);

        // the whole right-hand side of the bar
        mNotificationArea = sb.findViewById(R.id.notificationArea);

        // the button to open the notification area
        mNotificationTrigger = sb.findViewById(R.id.notificationTrigger);
        mNotificationTrigger.setOnClickListener(mOnClickListener);

        // the more notifications icon
        mNotificationIconArea = (NotificationIconArea)sb.findViewById(R.id.notificationIcons);

        // where the icons go
        mIconLayout = (NotificationIconArea.IconLayout) sb.findViewById(R.id.icons);
        mIconLayout.setOnTouchListener(new NotificationIconTouchListener());

        ViewConfiguration vc = ViewConfiguration.get(context);
        mNotificationPeekTapDuration = vc.getTapTimeout();
        mNotificationFlingVelocity = 300; // px/s

        mTicker = new TabletTicker(this);

        // The icons
        mLocationController = new LocationController(mContext); // will post a notification

        mBatteryController = new BatteryController(mContext);
        mBatteryController.addIconView((ImageView)sb.findViewById(R.id.battery));
        mBluetoothController = new BluetoothController(mContext);
        mBluetoothController.addIconView((ImageView)sb.findViewById(R.id.bluetooth));
        mNetworkController = new NetworkController(mContext);
        mNetworkController.addCombinedSignalIconView(
                (ImageView)sb.findViewById(R.id.network_signal));
        mNetworkController.addDataTypeIconView(
                (ImageView)sb.findViewById(R.id.network_type));
        mNetworkController.addDataDirectionOverlayIconView(
                (ImageView)sb.findViewById(R.id.network_direction));

        // The navigation buttons
        mBackButton = (ImageView)sb.findViewById(R.id.back);
        mNavigationArea = sb.findViewById(R.id.navigationArea);
        mHomeButton = mNavigationArea.findViewById(R.id.home);
        mMenuButton = mNavigationArea.findViewById(R.id.menu);
        mRecentButton = mNavigationArea.findViewById(R.id.recent_apps);
        mRecentButton.setOnClickListener(mOnClickListener);

        // The bar contents buttons
        mNotificationAndImeArea = (ViewGroup)sb.findViewById(R.id.notificationAndImeArea);
        mInputMethodSwitchButton = (InputMethodButton) sb.findViewById(R.id.imeSwitchButton);
        // Overwrite the lister
        mInputMethodSwitchButton.setOnClickListener(mOnClickListener);

        // for redirecting errant bar taps to the IME
        mFakeSpaceBar = sb.findViewById(R.id.fake_space_bar);

        // "shadows" of the status bar features, for lights-out mode
        mShadow = sb.findViewById(R.id.bar_shadow);
        mShadow.setOnTouchListener(
            new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent ev) {
                    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                        // even though setting the systemUI visibility below will turn these views
                        // on, we need them to come up faster so that they can catch this motion
                        // event
                        mShadow.setVisibility(View.GONE);
                        mBarContents.setVisibility(View.VISIBLE);

                        try {
                            mBarService.setSystemUiVisibility(View.STATUS_BAR_VISIBLE);
                        } catch (RemoteException ex) {
                            // system process dead
                        }
                    }
                    return false;
                }
            });

        // tuning parameters
        final int LIGHTS_GOING_OUT_SYSBAR_DURATION = 600;
        final int LIGHTS_GOING_OUT_SHADOW_DURATION = 1000;
        final int LIGHTS_GOING_OUT_SHADOW_DELAY    = 500;

        final int LIGHTS_COMING_UP_SYSBAR_DURATION = 200;
//        final int LIGHTS_COMING_UP_SYSBAR_DELAY    = 50;
        final int LIGHTS_COMING_UP_SHADOW_DURATION = 0;

        LayoutTransition xition = new LayoutTransition();
        xition.setAnimator(LayoutTransition.APPEARING,
               ObjectAnimator.ofFloat(null, "alpha", 0.5f, 1f));
        xition.setDuration(LayoutTransition.APPEARING, LIGHTS_COMING_UP_SYSBAR_DURATION);
        xition.setStartDelay(LayoutTransition.APPEARING, 0);
        xition.setAnimator(LayoutTransition.DISAPPEARING,
               ObjectAnimator.ofFloat(null, "alpha", 1f, 0f));
        xition.setDuration(LayoutTransition.DISAPPEARING, LIGHTS_GOING_OUT_SYSBAR_DURATION);
        xition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        ((ViewGroup)sb.findViewById(R.id.bar_contents_holder)).setLayoutTransition(xition);

        xition = new LayoutTransition();
        xition.setAnimator(LayoutTransition.APPEARING,
               ObjectAnimator.ofFloat(null, "alpha", 0f, 1f));
        xition.setDuration(LayoutTransition.APPEARING, LIGHTS_GOING_OUT_SHADOW_DURATION);
        xition.setStartDelay(LayoutTransition.APPEARING, LIGHTS_GOING_OUT_SHADOW_DELAY);
        xition.setAnimator(LayoutTransition.DISAPPEARING,
               ObjectAnimator.ofFloat(null, "alpha", 1f, 0f));
        xition.setDuration(LayoutTransition.DISAPPEARING, LIGHTS_COMING_UP_SHADOW_DURATION);
        xition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        ((ViewGroup)sb.findViewById(R.id.bar_shadow_holder)).setLayoutTransition(xition);

        // set the initial view visibility
        setAreThereNotifications();

        // Add the windows
        addPanelWindows();

        mPile = (ViewGroup)mNotificationPanel.findViewById(R.id.content);
        mPile.removeAllViews();

        ScrollView scroller = (ScrollView)mPile.getParent();
        scroller.setFillViewport(true);

        mHeightReceiver.addOnBarHeightChangedListener(this);

        return sb;
    }

    public int getStatusBarHeight() {
        return mHeightReceiver.getHeight();
    }

    protected int getStatusBarGravity() {
        return Gravity.BOTTOM | Gravity.FILL_HORIZONTAL;
    }

    public void onBarHeightChanged(int height) {
        final WindowManager.LayoutParams lp
                = (WindowManager.LayoutParams)mStatusBarView.getLayoutParams();
        if (lp == null) {
            // haven't been added yet
            return;
        }
        if (lp.height != height) {
            lp.height = height;
            final WindowManager wm = WindowManagerImpl.getDefault();
            wm.updateViewLayout(mStatusBarView, lp);
        }
    }

    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_OPEN_NOTIFICATION_PEEK:
                    if (DEBUG) Slog.d(TAG, "opening notification peek window; arg=" + m.arg1);
                    if (m.arg1 >= 0) {
                        final int N = mNotns.size();
                        if (mNotificationPeekIndex >= 0 && mNotificationPeekIndex < N) {
                            NotificationData.Entry entry = mNotns.get(N-1-mNotificationPeekIndex);
                            entry.icon.setBackgroundColor(0);
                            mNotificationPeekIndex = -1;
                            mNotificationPeekKey = null;
                        }

                        final int peekIndex = m.arg1;
                        if (peekIndex < N) {
                            //Slog.d(TAG, "loading peek: " + peekIndex);
                            NotificationData.Entry entry = mNotns.get(N-1-peekIndex);
                            NotificationData.Entry copy = new NotificationData.Entry(
                                    entry.key,
                                    entry.notification,
                                    entry.icon);
                            inflateViews(copy, mNotificationPeekRow);

                            entry.icon.setBackgroundColor(0x20FFFFFF);

//                          mNotificationPeekRow.setLayoutTransition(
//                              peekIndex < mNotificationPeekIndex
//                                  ? mNotificationPeekScrubLeft
//                                  : mNotificationPeekScrubRight);

                            mNotificationPeekRow.removeAllViews();
                            mNotificationPeekRow.addView(copy.row);

                            mNotificationPeekWindow.setVisibility(View.VISIBLE);
                            mNotificationPanel.show(false, true);

                            mNotificationPeekIndex = peekIndex;
                            mNotificationPeekKey = entry.key;
                        }
                    }
                    break;
                case MSG_CLOSE_NOTIFICATION_PEEK:
                    if (DEBUG) Slog.d(TAG, "closing notification peek window");
                    mNotificationPeekWindow.setVisibility(View.GONE);
                    mNotificationPeekRow.removeAllViews();
                    final int N = mNotns.size();
                    if (mNotificationPeekIndex >= 0 && mNotificationPeekIndex < N) {
                        NotificationData.Entry entry = mNotns.get(N-1-mNotificationPeekIndex);
                        entry.icon.setBackgroundColor(0);
                    }

                    mNotificationPeekIndex = -1;
                    mNotificationPeekKey = null;
                    break;
                case MSG_OPEN_NOTIFICATION_PANEL:
                    if (DEBUG) Slog.d(TAG, "opening notifications panel");
                    if (!mNotificationPanel.isShowing()) {
                        mNotificationPeekWindow.setVisibility(View.GONE);
                        mNotificationPanel.show(true, true);
                        mNotificationArea.setVisibility(View.GONE);
                        mTicker.halt();
                    }
                    break;
                case MSG_CLOSE_NOTIFICATION_PANEL:
                    if (DEBUG) Slog.d(TAG, "closing notifications panel");
                    if (mNotificationPanel.isShowing()) {
                        mNotificationPanel.show(false, true);
                        mNotificationArea.setVisibility(View.VISIBLE);
                    }
                    break;
                case MSG_OPEN_RECENTS_PANEL:
                    if (DEBUG) Slog.d(TAG, "opening recents panel");
                    if (mRecentsPanel != null) {
                        mRecentsPanel.setVisibility(View.VISIBLE);
                        mRecentsPanel.show(true, true);
                    }
                    break;
                case MSG_CLOSE_RECENTS_PANEL:
                    if (DEBUG) Slog.d(TAG, "closing recents panel");
                    if (mRecentsPanel != null && mRecentsPanel.isShowing()) {
                        mRecentsPanel.show(false, true);
                    }
                    break;
                case MSG_OPEN_INPUT_METHODS_PANEL:
                    if (DEBUG) Slog.d(TAG, "opening input methods panel");
                    if (mInputMethodsPanel != null) mInputMethodsPanel.openPanel();
                    break;
                case MSG_CLOSE_INPUT_METHODS_PANEL:
                    if (DEBUG) Slog.d(TAG, "closing input methods panel");
                    if (mInputMethodsPanel != null) mInputMethodsPanel.closePanel(false);
                    break;
                case MSG_SHOW_CHROME:
                    if (DEBUG) Slog.d(TAG, "hiding shadows (lights on)");
                    mBarContents.setVisibility(View.VISIBLE);
                    mShadow.setVisibility(View.GONE);
                    notifyLightsChanged(true);
                    break;
                case MSG_HIDE_CHROME:
                    if (DEBUG) Slog.d(TAG, "showing shadows (lights out)");
                    animateCollapse();
                    mBarContents.setVisibility(View.GONE);
                    mShadow.setVisibility(View.VISIBLE);
                    notifyLightsChanged(false);
                    break;
                case MSG_STOP_TICKER:
                    mTicker.halt();
                    break;
            }
        }
    }

    private void notifyLightsChanged(boolean shown) {
        try {
            Slog.d(TAG, "lights " + (shown?"on":"out"));
            mWindowManager.statusBarVisibilityChanged(
                    shown ? View.STATUS_BAR_VISIBLE : View.STATUS_BAR_HIDDEN);
        } catch (RemoteException ex) {
        }
    }

    public void addIcon(String slot, int index, int viewIndex, StatusBarIcon icon) {
        if (DEBUG) Slog.d(TAG, "addIcon(" + slot + ") -> " + icon);
    }

    public void updateIcon(String slot, int index, int viewIndex,
            StatusBarIcon old, StatusBarIcon icon) {
        if (DEBUG) Slog.d(TAG, "updateIcon(" + slot + ") -> " + icon);
    }

    public void removeIcon(String slot, int index, int viewIndex) {
        if (DEBUG) Slog.d(TAG, "removeIcon(" + slot + ")");
    }

    public void addNotification(IBinder key, StatusBarNotification notification) {
        if (DEBUG) Slog.d(TAG, "addNotification(" + key + " -> " + notification + ")");
        addNotificationViews(key, notification);

        final boolean immersive = isImmersive();
        if (false && immersive) {
            // TODO: immersive mode popups for tablet
        } else if (notification.notification.fullScreenIntent != null) {
            // not immersive & a full-screen alert should be shown
            Slog.w(TAG, "Notification has fullScreenIntent and activity is not immersive;"
                    + " sending fullScreenIntent");
            try {
                notification.notification.fullScreenIntent.send();
            } catch (PendingIntent.CanceledException e) {
            }
        } else {
            tick(key, notification, true);
        }

        setAreThereNotifications();
    }

    public void updateNotification(IBinder key, StatusBarNotification notification) {
        if (DEBUG) Slog.d(TAG, "updateNotification(" + key + " -> " + notification + ") // TODO");

        final NotificationData.Entry oldEntry = mNotns.findByKey(key);
        if (oldEntry == null) {
            Slog.w(TAG, "updateNotification for unknown key: " + key);
            return;
        }

        final StatusBarNotification oldNotification = oldEntry.notification;
        final RemoteViews oldContentView = oldNotification.notification.contentView;

        final RemoteViews contentView = notification.notification.contentView;

        if (DEBUG) {
            Slog.d(TAG, "old notification: when=" + oldNotification.notification.when
                    + " ongoing=" + oldNotification.isOngoing()
                    + " expanded=" + oldEntry.expanded
                    + " contentView=" + oldContentView);
            Slog.d(TAG, "new notification: when=" + notification.notification.when
                    + " ongoing=" + oldNotification.isOngoing()
                    + " contentView=" + contentView);
        }

        // Can we just reapply the RemoteViews in place?  If when didn't change, the order
        // didn't change.
        boolean contentsUnchanged = oldEntry.expanded != null
                && contentView != null && oldContentView != null
                && contentView.getPackage() != null
                && oldContentView.getPackage() != null
                && oldContentView.getPackage().equals(contentView.getPackage())
                && oldContentView.getLayoutId() == contentView.getLayoutId();
        ViewGroup rowParent = (ViewGroup) oldEntry.row.getParent();
        boolean orderUnchanged = notification.notification.when==oldNotification.notification.when
                && notification.priority == oldNotification.priority;
                // priority now encompasses isOngoing()
        boolean isLastAnyway = rowParent.indexOfChild(oldEntry.row) == rowParent.getChildCount()-1;
        if (contentsUnchanged && (orderUnchanged || isLastAnyway)) {
            if (DEBUG) Slog.d(TAG, "reusing notification for key: " + key);
            oldEntry.notification = notification;
            try {
                // Reapply the RemoteViews
                contentView.reapply(mContext, oldEntry.content);
                // update the contentIntent
                final PendingIntent contentIntent = notification.notification.contentIntent;
                if (contentIntent != null) {
                    oldEntry.content.setOnClickListener(new NotificationClicker(contentIntent,
                                notification.pkg, notification.tag, notification.id));
                } else {
                    oldEntry.content.setOnClickListener(null);
                }
                // Update the icon.
                final StatusBarIcon ic = new StatusBarIcon(notification.pkg,
                        notification.notification.icon, notification.notification.iconLevel,
                        notification.notification.number);
                if (!oldEntry.icon.set(ic)) {
                    handleNotificationError(key, notification, "Couldn't update icon: " + ic);
                    return;
                }
                // Update the large icon
                if (notification.notification.largeIcon != null) {
                    oldEntry.largeIcon.setImageBitmap(notification.notification.largeIcon);
                } else {
                    oldEntry.largeIcon.getLayoutParams().width = 0;
                    oldEntry.largeIcon.setVisibility(View.INVISIBLE);
                }

                if (key == mNotificationPeekKey) {
                    // must update the peek window
                    Message peekMsg = mHandler.obtainMessage(MSG_OPEN_NOTIFICATION_PEEK);
                    peekMsg.arg1 = mNotificationPeekIndex;
                    mHandler.removeMessages(MSG_OPEN_NOTIFICATION_PEEK);
                    mHandler.sendMessage(peekMsg);
                }
            }
            catch (RuntimeException e) {
                // It failed to add cleanly.  Log, and remove the view from the panel.
                Slog.w(TAG, "Couldn't reapply views for package " + contentView.getPackage(), e);
                removeNotificationViews(key);
                addNotificationViews(key, notification);
            }
        } else {
            if (DEBUG) Slog.d(TAG, "not reusing notification for key: " + key);
            removeNotificationViews(key);
            addNotificationViews(key, notification);
        }
        // fullScreenIntent doesn't happen on updates.  You need to clear & repost a new
        // notification.
        final boolean immersive = isImmersive();
        if (false && immersive) {
            // TODO: immersive mode
        } else {
            tick(key, notification, false);
        }

        setAreThereNotifications();
    }

    public void removeNotification(IBinder key) {
        if (DEBUG) Slog.d(TAG, "removeNotification(" + key + ") // TODO");
        removeNotificationViews(key);
        mTicker.remove(key);
        setAreThereNotifications();
    }

    public void showClock(boolean show) {
        View clock = mBarContents.findViewById(R.id.clock);
        View network_text = mBarContents.findViewById(R.id.network_text);
        if (clock != null) {
            clock.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (network_text != null) {
            network_text.setVisibility((!show) ? View.VISIBLE : View.GONE);
        }
    }

    public void disable(int state) {
        int old = mDisabled;
        int diff = state ^ old;
        mDisabled = state;

        // act accordingly
        if ((diff & StatusBarManager.DISABLE_CLOCK) != 0) {
            boolean show = (state & StatusBarManager.DISABLE_CLOCK) == 0;
            Slog.i(TAG, "DISABLE_CLOCK: " + (show ? "no" : "yes"));
            showClock(show);
        }
        if ((diff & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) {
            boolean show = (state & StatusBarManager.DISABLE_SYSTEM_INFO) == 0;
            Slog.i(TAG, "DISABLE_SYSTEM_INFO: " + (show ? "no" : "yes"));
            mNotificationTrigger.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
            if ((state & StatusBarManager.DISABLE_EXPAND) != 0) {
                Slog.i(TAG, "DISABLE_EXPAND: yes");
                animateCollapse();
            }
        }
        if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
            if ((state & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                Slog.i(TAG, "DISABLE_NOTIFICATION_ICONS: yes");
                // synchronize with current shadow state
                mNotificationIconArea.setVisibility(View.GONE);
                mTicker.halt();
            } else {
                Slog.i(TAG, "DISABLE_NOTIFICATION_ICONS: no");
                // synchronize with current shadow state
                mNotificationIconArea.setVisibility(View.VISIBLE);
            }
        } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
            if ((state & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                mTicker.halt();
            }
        }
        if ((diff & StatusBarManager.DISABLE_NAVIGATION) != 0) {
            if ((state & StatusBarManager.DISABLE_NAVIGATION) != 0) {
                Slog.i(TAG, "DISABLE_NAVIGATION: yes");
                mNavigationArea.setVisibility(View.GONE);
                mInputMethodSwitchButton.setScreenLocked(true);
            } else {
                Slog.i(TAG, "DISABLE_NAVIGATION: no");
                mNavigationArea.setVisibility(View.VISIBLE);
                mInputMethodSwitchButton.setScreenLocked(false);
            }
        }
        if ((diff & StatusBarManager.DISABLE_BACK) != 0) {
            if ((state & StatusBarManager.DISABLE_BACK) != 0) {
                Slog.i(TAG, "DISABLE_BACK: yes");
                mBackButton.setVisibility(View.INVISIBLE);
                mInputMethodSwitchButton.setScreenLocked(true);
            } else {
                Slog.i(TAG, "DISABLE_BACK: no");
                mBackButton.setVisibility(View.VISIBLE);
                mInputMethodSwitchButton.setScreenLocked(false);
            }
        }

    }

    private boolean hasTicker(Notification n) {
        return n.tickerView != null || !TextUtils.isEmpty(n.tickerText);
    }

    private void tick(IBinder key, StatusBarNotification n, boolean firstTime) {
        // Don't show the ticker when the windowshade is open.
        if (mNotificationPanel.isShowing()) {
            return;
        }
        // If they asked for FLAG_ONLY_ALERT_ONCE, then only show this notification
        // if it's a new notification.
        if (!firstTime && (n.notification.flags & Notification.FLAG_ONLY_ALERT_ONCE) != 0) {
            return;
        }
        // Show the ticker if one is requested. Also don't do this
        // until status bar window is attached to the window manager,
        // because...  well, what's the point otherwise?  And trying to
        // run a ticker without being attached will crash!
        if (hasTicker(n.notification) && mStatusBarView.getWindowToken() != null) {
            if (0 == (mDisabled & (StatusBarManager.DISABLE_NOTIFICATION_ICONS
                            | StatusBarManager.DISABLE_NOTIFICATION_TICKER))) {
                mTicker.add(key, n);
                mNotificationAndImeArea.setVisibility(View.GONE);
            }
        }
    }

    // called by TabletTicker when it's done with all queued ticks
    public void doneTicking() {
        mNotificationAndImeArea.setVisibility(View.VISIBLE);
    }

    public void animateExpand() {
        mHandler.removeMessages(MSG_OPEN_NOTIFICATION_PANEL);
        mHandler.sendEmptyMessage(MSG_OPEN_NOTIFICATION_PANEL);
    }

    public void animateCollapse() {
        mHandler.removeMessages(MSG_CLOSE_NOTIFICATION_PANEL);
        mHandler.sendEmptyMessage(MSG_CLOSE_NOTIFICATION_PANEL);
        mHandler.removeMessages(MSG_CLOSE_RECENTS_PANEL);
        mHandler.sendEmptyMessage(MSG_CLOSE_RECENTS_PANEL);
        mHandler.removeMessages(MSG_CLOSE_INPUT_METHODS_PANEL);
        mHandler.sendEmptyMessage(MSG_CLOSE_INPUT_METHODS_PANEL);
    }

    // called by StatusBar
    @Override
    public void setLightsOn(boolean on) {
        // Policy note: if the frontmost activity needs the menu key, we assume it is a legacy app
        // that can't handle lights-out mode.
        if (mMenuButton.getVisibility() == View.VISIBLE) {
            on = true;
        }
        mHandler.removeMessages(MSG_HIDE_CHROME);
        mHandler.removeMessages(MSG_SHOW_CHROME);
        mHandler.sendEmptyMessage(on ? MSG_SHOW_CHROME : MSG_HIDE_CHROME);
    }

    public void setMenuKeyVisible(boolean visible) {
        if (DEBUG) {
            Slog.d(TAG, (visible?"showing":"hiding") + " the MENU button");
        }
        mMenuButton.setVisibility(visible ? View.VISIBLE : View.GONE);

        // See above re: lights-out policy for legacy apps.
        if (visible) setLightsOn(true);
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        mInputMethodSwitchButton.setImeWindowStatus(token,
                (vis & InputMethodService.IME_ACTIVE) != 0);
        updateNotificationIcons();
        mInputMethodsPanel.setImeToken(token);
        int res;
        switch (backDisposition) {
            case InputMethodService.BACK_DISPOSITION_WILL_NOT_DISMISS:
                res = R.drawable.ic_sysbar_back;
                break;
            case InputMethodService.BACK_DISPOSITION_WILL_DISMISS:
                res = R.drawable.ic_sysbar_back_ime;
                break;
            case InputMethodService.BACK_DISPOSITION_DEFAULT:
            default:
                if ((vis & InputMethodService.IME_VISIBLE) != 0) {
                    res = R.drawable.ic_sysbar_back_ime;
                } else {
                    res = R.drawable.ic_sysbar_back;
                }
                break;
        }
        mBackButton.setImageResource(res);
        if (FAKE_SPACE_BAR) {
            mFakeSpaceBar.setVisibility(((vis & InputMethodService.IME_VISIBLE) != 0)
                    ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setHardKeyboardStatus(boolean available, boolean enabled) {
        if (DEBUG) {
            Slog.d(TAG, "Set hard keyboard status: available=" + available
                    + ", enabled=" + enabled);
        }
        mInputMethodSwitchButton.setHardKeyboardStatus(available);
        updateNotificationIcons();
        mInputMethodsPanel.setHardKeyboardStatus(available, enabled);
    }

    @Override
    public void onHardKeyboardEnabledChange(boolean enabled) {
        try {
            mBarService.setHardKeyboardEnabled(enabled);
        } catch (RemoteException ex) {
        }
    }

    private boolean isImmersive() {
        try {
            return ActivityManagerNative.getDefault().isTopActivityImmersive();
            //Slog.d(TAG, "Top activity is " + (immersive?"immersive":"not immersive"));
        } catch (RemoteException ex) {
            // the end is nigh
            return false;
        }
    }

    private void setAreThereNotifications() {
        final boolean hasClearable = mNotns.hasClearableItems();
    }

    /**
     * Cancel this notification and tell the status bar service about the failure. Hold no locks.
     */
    void handleNotificationError(IBinder key, StatusBarNotification n, String message) {
        removeNotification(key);
        try {
            mBarService.onNotificationError(n.pkg, n.tag, n.id, n.uid, n.initialPid, message);
        } catch (RemoteException ex) {
            // The end is nigh.
        }
    }

    private void sendKey(KeyEvent key) {
        try {
            if (DEBUG) Slog.d(TAG, "injecting key event: " + key);
            mWindowManager.injectInputEventNoWait(key);
        } catch (RemoteException ex) {
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == mNotificationTrigger) {
                onClickNotificationTrigger();
            } else if (v == mRecentButton) {
                onClickRecentButton();
            } else if (v == mInputMethodSwitchButton) {
                onClickInputMethodSwitchButton();
            }
        }
    };

    public void onClickNotificationTrigger() {
        if (DEBUG) Slog.d(TAG, "clicked notification icons; disabled=" + mDisabled);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) == 0) {
            if (!mNotificationsOn) {
                mNotificationsOn = true;
                mIconLayout.setVisibility(View.VISIBLE); // TODO: animation
            } else {
                int msg = !mNotificationPanel.isShowing()
                    ? MSG_OPEN_NOTIFICATION_PANEL
                    : MSG_CLOSE_NOTIFICATION_PANEL;
                mHandler.removeMessages(msg);
                mHandler.sendEmptyMessage(msg);
            }
        }
    }

    public void onClickRecentButton() {
        if (DEBUG) Slog.d(TAG, "clicked recent apps; disabled=" + mDisabled);
        if (mRecentsPanel == null) {
            Intent intent = new Intent();
            intent.setClass(mContext, RecentApplicationsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            mContext.startActivity(intent);
        } else {
            if ((mDisabled & StatusBarManager.DISABLE_EXPAND) == 0) {
                int msg = (mRecentsPanel.getVisibility() == View.GONE)
                    ? MSG_OPEN_RECENTS_PANEL
                    : MSG_CLOSE_RECENTS_PANEL;
                mHandler.removeMessages(msg);
                mHandler.sendEmptyMessage(msg);
            }
        }
    }

    public void onClickInputMethodSwitchButton() {
        if (DEBUG) Slog.d(TAG, "clicked input methods panel; disabled=" + mDisabled);
        int msg = (mInputMethodsPanel.getVisibility() == View.GONE) ?
                MSG_OPEN_INPUT_METHODS_PANEL : MSG_CLOSE_INPUT_METHODS_PANEL;
        mHandler.removeMessages(msg);
        mHandler.sendEmptyMessage(msg);
    }

    public NotificationClicker makeClicker(PendingIntent intent, String pkg, String tag, int id) {
        return new NotificationClicker(intent, pkg, tag, id);
    }

    private class NotificationClicker implements View.OnClickListener {
        private PendingIntent mIntent;
        private String mPkg;
        private String mTag;
        private int mId;

        NotificationClicker(PendingIntent intent, String pkg, String tag, int id) {
            mIntent = intent;
            mPkg = pkg;
            mTag = tag;
            mId = id;
        }

        public void onClick(View v) {
            try {
                // The intent we are sending is for the application, which
                // won't have permission to immediately start an activity after
                // the user switches to home.  We know it is safe to do at this
                // point, so make sure new activity switches are now allowed.
                ActivityManagerNative.getDefault().resumeAppSwitches();
            } catch (RemoteException e) {
            }

            if (mIntent != null) {
                int[] pos = new int[2];
                v.getLocationOnScreen(pos);
                Intent overlay = new Intent();
                overlay.setSourceBounds(
                        new Rect(pos[0], pos[1], pos[0]+v.getWidth(), pos[1]+v.getHeight()));
                try {
                    mIntent.send(mContext, 0, overlay);
                } catch (PendingIntent.CanceledException e) {
                    // the stack trace isn't very helpful here.  Just log the exception message.
                    Slog.w(TAG, "Sending contentIntent failed: " + e);
                }
            }

            try {
                mBarService.onNotificationClick(mPkg, mTag, mId);
            } catch (RemoteException ex) {
                // system process is dead if we're here.
            }

            // close the shade if it was open
            animateCollapse();

            // If this click was on the intruder alert, hide that instead
//            mHandler.sendEmptyMessage(MSG_HIDE_INTRUDER);
        }
    }

    StatusBarNotification removeNotificationViews(IBinder key) {
        NotificationData.Entry entry = mNotns.remove(key);
        if (entry == null) {
            Slog.w(TAG, "removeNotification for unknown key: " + key);
            return null;
        }
        // Remove the expanded view.
        ViewGroup rowParent = (ViewGroup)entry.row.getParent();
        if (rowParent != null) rowParent.removeView(entry.row);

        if (key == mNotificationPeekKey) {
            // must close the peek as well, since it's gone
            mHandler.sendEmptyMessage(MSG_CLOSE_NOTIFICATION_PEEK);
        }
        // Remove the icon.
//        ViewGroup iconParent = (ViewGroup)entry.icon.getParent();
//        if (iconParent != null) iconParent.removeView(entry.icon);
        updateNotificationIcons();

        return entry.notification;
    }

    private class NotificationIconTouchListener implements View.OnTouchListener {
        VelocityTracker mVT;

        public NotificationIconTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            boolean peeking = mNotificationPeekWindow.getVisibility() != View.GONE;
            boolean panelShowing = mNotificationPanel.isShowing();
            if (panelShowing) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mVT = VelocityTracker.obtain();

                    // fall through
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_MOVE:
                    // peek and switch icons if necessary
                    int numIcons = mIconLayout.getChildCount();
                    int peekIndex = (int)((float)event.getX() * numIcons / mIconLayout.getWidth());
                    if (peekIndex > numIcons - 1) peekIndex = numIcons - 1;
                    else if (peekIndex < 0) peekIndex = 0;

                    if (!peeking || mNotificationPeekIndex != peekIndex) {
                        if (DEBUG) Slog.d(TAG, "will peek at notification #" + peekIndex);
                        Message peekMsg = mHandler.obtainMessage(MSG_OPEN_NOTIFICATION_PEEK);
                        peekMsg.arg1 = peekIndex;

                        mHandler.removeMessages(MSG_OPEN_NOTIFICATION_PEEK);

                        // no delay if we're scrubbing left-right
                        mHandler.sendMessage(peekMsg);
                    }

                    // check for fling
                    if (mVT != null) {
                        mVT.addMovement(event);
                        mVT.computeCurrentVelocity(1000);
                        // require a little more oomph once we're already in peekaboo mode
                        if (!panelShowing && (
                               (peeking && mVT.getYVelocity() < -mNotificationFlingVelocity*3)
                            || (mVT.getYVelocity() < -mNotificationFlingVelocity))) {
                            mHandler.removeMessages(MSG_OPEN_NOTIFICATION_PEEK);
                            mHandler.removeMessages(MSG_OPEN_NOTIFICATION_PANEL);
                            mHandler.sendEmptyMessage(MSG_CLOSE_NOTIFICATION_PEEK);
                            mHandler.sendEmptyMessage(MSG_OPEN_NOTIFICATION_PANEL);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mHandler.removeMessages(MSG_OPEN_NOTIFICATION_PEEK);
                    if (peeking) {
                        mHandler.sendEmptyMessageDelayed(MSG_CLOSE_NOTIFICATION_PEEK, 5000);
                    }
                    mVT.recycle();
                    mVT = null;
                    return true;
            }
            return false;
        }
    }

    StatusBarIconView addNotificationViews(IBinder key, StatusBarNotification notification) {
        if (DEBUG) {
            Slog.d(TAG, "addNotificationViews(key=" + key + ", notification=" + notification);
        }
        // Construct the icon.
        final StatusBarIconView iconView = new StatusBarIconView(mContext,
                notification.pkg + "/0x" + Integer.toHexString(notification.id));
        iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        final StatusBarIcon ic = new StatusBarIcon(notification.pkg,
                    notification.notification.icon,
                    notification.notification.iconLevel,
                    notification.notification.number);
        if (!iconView.set(ic)) {
            handleNotificationError(key, notification, "Couldn't attach StatusBarIcon: " + ic);
            return null;
        }
        // Construct the expanded view.
        NotificationData.Entry entry = new NotificationData.Entry(key, notification, iconView);
        if (!inflateViews(entry, mPile)) {
            handleNotificationError(key, notification, "Couldn't expand RemoteViews for: "
                    + notification);
            return null;
        }

        // Add the icon.
        int pos = mNotns.add(entry);
        if (DEBUG) {
            Slog.d(TAG, "addNotificationViews: added at " + pos);
        }
        updateNotificationIcons();

        return iconView;
    }

    private void reloadAllNotificationIcons() {
        if (mIconLayout == null) return;
        mIconLayout.removeAllViews();
        updateNotificationIcons();
    }

    private void updateNotificationIcons() {
        // XXX: need to implement a new limited linear layout class
        // to avoid removing & readding everything

        if (mIconLayout == null) return;

        final LinearLayout.LayoutParams params
            = new LinearLayout.LayoutParams(mIconSize + 2*mIconHPadding, mNaturalBarHeight);

        int N = mNotns.size();

        if (DEBUG) {
            Slog.d(TAG, "refreshing icons: " + N + " notifications, mIconLayout=" + mIconLayout);
        }

        ArrayList<View> toShow = new ArrayList<View>();

        // When IME button is visible, the number of notification icons should be decremented
        // to fit the upper limit.
        final int maxNotificationIconsCount =
                (mInputMethodSwitchButton.getVisibility() != View.GONE) ?
                        MAX_NOTIFICATION_ICONS_IME_BUTTON_VISIBLE : MAX_NOTIFICATION_ICONS;
        for (int i=0; i< maxNotificationIconsCount; i++) {
            if (i>=N) break;
            toShow.add(mNotns.get(N-i-1).icon);
        }

        ArrayList<View> toRemove = new ArrayList<View>();
        for (int i=0; i<mIconLayout.getChildCount(); i++) {
            View child = mIconLayout.getChildAt(i);
            if (!toShow.contains(child)) {
                toRemove.add(child);
            }
        }

        for (View remove : toRemove) {
            mIconLayout.removeView(remove);
        }

        for (int i=0; i<toShow.size(); i++) {
            View v = toShow.get(i);
            v.setPadding(mIconHPadding, 0, mIconHPadding, 0);
            if (v.getParent() == null) {
                mIconLayout.addView(v, i, params);
            }
        }

        loadNotificationPanel();
    }

    private void loadNotificationPanel() {
        int N = mNotns.size();

        ArrayList<View> toShow = new ArrayList<View>();

        for (int i=0; i<N; i++) {
            View row = mNotns.get(N-i-1).row;
            toShow.add(row);
        }

        ArrayList<View> toRemove = new ArrayList<View>();
        for (int i=0; i<mPile.getChildCount(); i++) {
            View child = mPile.getChildAt(i);
            if (!toShow.contains(child)) {
                toRemove.add(child);
            }
        }

        for (View remove : toRemove) {
            mPile.removeView(remove);
        }

        for (int i=0; i<toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
                mPile.addView(v, N-1-i); // the notification panel has newest at the bottom
            }
        }

        mNotificationPanel.setNotificationCount(N);
    }

    void workAroundBadLayerDrawableOpacity(View v) {
        LayerDrawable d = (LayerDrawable)v.getBackground();
        if (d == null) return;
        v.setBackgroundDrawable(null);
        d.setOpacity(PixelFormat.TRANSLUCENT);
        v.setBackgroundDrawable(d);
    }

    private boolean inflateViews(NotificationData.Entry entry, ViewGroup parent) {
        StatusBarNotification sbn = entry.notification;
        RemoteViews remoteViews = sbn.notification.contentView;
        if (remoteViews == null) {
            return false;
        }

        // create the row view
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.status_bar_notification_row, parent, false);
        workAroundBadLayerDrawableOpacity(row);
        View vetoButton = row.findViewById(R.id.veto);
        if (entry.notification.isClearable()) {
            final String _pkg = sbn.pkg;
            final String _tag = sbn.tag;
            final int _id = sbn.id;
            vetoButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            mBarService.onNotificationClear(_pkg, _tag, _id);
                        } catch (RemoteException ex) {
                            // system process is dead if we're here.
                        }
                    }
                });
        } else {
            if ((sbn.notification.flags & Notification.FLAG_ONGOING_EVENT) == 0) {
                vetoButton.setVisibility(View.INVISIBLE);
            } else {
                vetoButton.setVisibility(View.GONE);
            }
        }

        // the large icon
        ImageView largeIcon = (ImageView)row.findViewById(R.id.large_icon);
        if (sbn.notification.largeIcon != null) {
            largeIcon.setImageBitmap(sbn.notification.largeIcon);
        } else {
            largeIcon.getLayoutParams().width = 0;
            largeIcon.setVisibility(View.INVISIBLE);
        }

        // bind the click event to the content area
        ViewGroup content = (ViewGroup)row.findViewById(R.id.content);
        // XXX: update to allow controls within notification views
        content.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
//        content.setOnFocusChangeListener(mFocusChangeListener);
        PendingIntent contentIntent = sbn.notification.contentIntent;
        if (contentIntent != null) {
            content.setOnClickListener(new NotificationClicker(contentIntent,
                        sbn.pkg, sbn.tag, sbn.id));
        } else {
            content.setOnClickListener(null);
        }

        View expanded = null;
        Exception exception = null;
        try {
            expanded = remoteViews.apply(mContext, content);
        }
        catch (RuntimeException e) {
            exception = e;
        }
        if (expanded == null) {
            final String ident = sbn.pkg + "/0x" + Integer.toHexString(sbn.id);
            Slog.e(TAG, "couldn't inflate view for notification " + ident, exception);
            return false;
        } else {
            content.addView(expanded);
            row.setDrawingCacheEnabled(true);
        }

        entry.row = row;
        entry.content = content;
        entry.expanded = expanded;
        entry.largeIcon = largeIcon;

        return true;
    }

/*
    public class ShadowController {
        boolean mShowShadows;
        Map<View, View> mShadowsForElements = new IdentityHashMap<View, View>(7);
        Map<View, View> mElementsForShadows = new IdentityHashMap<View, View>(7);
        LayoutTransition mElementTransition, mShadowTransition;

        View mTouchTarget;

        ShadowController(boolean showShadows) {
            mShowShadows = showShadows;
            mTouchTarget = null;

            mElementTransition = new LayoutTransition();
//            AnimatorSet s = new AnimatorSet();
//            s.play(ObjectAnimator.ofInt(null, "top", 48, 0))
//                .with(ObjectAnimator.ofFloat(null, "scaleY", 0.5f, 1f))
//                .with(ObjectAnimator.ofFloat(null, "alpha", 0.5f, 1f))
//                ;
            mElementTransition.setAnimator(LayoutTransition.APPEARING, //s);
                   ObjectAnimator.ofInt(null, "top", 48, 0));
            mElementTransition.setDuration(LayoutTransition.APPEARING, 100);
            mElementTransition.setStartDelay(LayoutTransition.APPEARING, 0);

//            s = new AnimatorSet();
//            s.play(ObjectAnimator.ofInt(null, "top", 0, 48))
//                .with(ObjectAnimator.ofFloat(null, "scaleY", 1f, 0.5f))
//                .with(ObjectAnimator.ofFloat(null, "alpha", 1f, 0.5f))
//                ;
            mElementTransition.setAnimator(LayoutTransition.DISAPPEARING, //s);
                    ObjectAnimator.ofInt(null, "top", 0, 48));
            mElementTransition.setDuration(LayoutTransition.DISAPPEARING, 400);

            mShadowTransition = new LayoutTransition();
            mShadowTransition.setAnimator(LayoutTransition.APPEARING,
                    ObjectAnimator.ofFloat(null, "alpha", 0f, 1f));
            mShadowTransition.setDuration(LayoutTransition.APPEARING, 200);
            mShadowTransition.setStartDelay(LayoutTransition.APPEARING, 100);
            mShadowTransition.setAnimator(LayoutTransition.DISAPPEARING,
                    ObjectAnimator.ofFloat(null, "alpha", 1f, 0f));
            mShadowTransition.setDuration(LayoutTransition.DISAPPEARING, 100);

            ViewGroup bar = (ViewGroup) TabletStatusBar.this.mBarContents;
            bar.setLayoutTransition(mElementTransition);
            ViewGroup nav = (ViewGroup) TabletStatusBar.this.mNavigationArea;
            nav.setLayoutTransition(mElementTransition);
            ViewGroup shadowGroup = (ViewGroup) bar.findViewById(R.id.shadows);
            shadowGroup.setLayoutTransition(mShadowTransition);
        }

        public void add(View element, View shadow) {
            shadow.setOnTouchListener(makeTouchListener());
            mShadowsForElements.put(element, shadow);
            mElementsForShadows.put(shadow, element);
        }

        public boolean getShadowState() {
            return mShowShadows;
        }

        public View.OnTouchListener makeTouchListener() {
            return new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent ev) {
                    final int action = ev.getAction();

                    if (DEBUG) Slog.d(TAG, "ShadowController: v=" + v + ", ev=" + ev);

                    // currently redirecting events?
                    if (mTouchTarget == null) {
                        mTouchTarget = mElementsForShadows.get(v);
                    }

                    if (mTouchTarget != null && mTouchTarget.getVisibility() != View.GONE) {
                        boolean last = false;
                        switch (action) {
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP:
                                mHandler.removeMessages(MSG_RESTORE_SHADOWS);
                                if (mShowShadows) {
                                    mHandler.sendEmptyMessageDelayed(MSG_RESTORE_SHADOWS,
                                            v == mNotificationShadow ? 5000 : 500);
                                }
                                last = true;
                                break;
                            case MotionEvent.ACTION_DOWN:
                                mHandler.removeMessages(MSG_RESTORE_SHADOWS);
                                setElementShadow(mTouchTarget, false);
                                break;
                        }
                        mTouchTarget.dispatchTouchEvent(ev);
                        if (last) mTouchTarget = null;
                        return true;
                    }

                    return false;
                }
            };
        }

        public void refresh() {
            for (View element : mShadowsForElements.keySet()) {
                setElementShadow(element, mShowShadows);
            }
        }

        public void showAllShadows() {
            mShowShadows = true;
            refresh();
        }

        public void hideAllShadows() {
            mShowShadows = false;
            refresh();
        }

        // Use View.INVISIBLE for things hidden due to shadowing, and View.GONE for things that are
        // disabled (and should not be shadowed or re-shown)
        public void setElementShadow(View button, boolean shade) {
            View shadow = mShadowsForElements.get(button);
            if (shadow != null) {
                if (button.getVisibility() != View.GONE) {
                    shadow.setVisibility(shade ? View.VISIBLE : View.INVISIBLE);
                    button.setVisibility(shade ? View.INVISIBLE : View.VISIBLE);
                }
            }
        }

        // Hide both element and shadow, using default layout animations.
        public void hideElement(View button) {
            Slog.d(TAG, "hiding: " + button);
            View shadow = mShadowsForElements.get(button);
            if (shadow != null) {
                shadow.setVisibility(View.GONE);
            }
            button.setVisibility(View.GONE);
        }

        // Honoring the current shadow state.
        public void showElement(View button) {
            Slog.d(TAG, "showing: " + button);
            View shadow = mShadowsForElements.get(button);
            if (shadow != null) {
                shadow.setVisibility(mShowShadows ? View.VISIBLE : View.INVISIBLE);
            }
            button.setVisibility(mShowShadows ? View.INVISIBLE : View.VISIBLE);
        }
    }
    */

    public class TouchOutsideListener implements View.OnTouchListener {
        private int mMsg;
        private StatusBarPanel mPanel;

        public TouchOutsideListener(int msg, StatusBarPanel panel) {
            mMsg = msg;
            mPanel = panel;
        }

        public boolean onTouch(View v, MotionEvent ev) {
            final int action = ev.getAction();
            if (action == MotionEvent.ACTION_OUTSIDE
                    || (action == MotionEvent.ACTION_DOWN
                        && !mPanel.isInContentArea((int)ev.getX(), (int)ev.getY()))) {
                mHandler.removeMessages(mMsg);
                mHandler.sendEmptyMessage(mMsg);
                return true;
            }
            return false;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("mDisabled=0x");
        pw.println(Integer.toHexString(mDisabled));
        pw.println("mNetworkController:");
        mNetworkController.dump(fd, pw, args);
    }
}

