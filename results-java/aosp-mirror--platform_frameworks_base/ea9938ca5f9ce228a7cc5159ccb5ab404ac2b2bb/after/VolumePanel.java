/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.systemui.volume;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.AudioService;
import android.media.AudioSystem;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.media.VolumeProvider;
import android.media.session.MediaController;
import android.media.session.MediaController.VolumeInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.internal.R;
import com.android.systemui.statusbar.policy.ZenModeController;

import java.util.HashMap;

/**
 * Handles the user interface for the volume keys.
 *
 * @hide
 */
public class VolumePanel extends Handler {
    private static final String TAG = "VolumePanel";
    private static boolean LOGD = Log.isLoggable(TAG, Log.DEBUG);

    private static final int PLAY_SOUND_DELAY = AudioService.PLAY_SOUND_DELAY;

    /**
     * The delay before vibrating. This small period exists so if the user is
     * moving to silent mode, it will not emit a short vibrate (it normally
     * would since vibrate is between normal mode and silent mode using hardware
     * keys).
     */
    public static final int VIBRATE_DELAY = 300;

    private static final int VIBRATE_DURATION = 300;
    private static final int BEEP_DURATION = 150;
    private static final int MAX_VOLUME = 100;
    private static final int FREE_DELAY = 10000;
    private static final int TIMEOUT_DELAY = 3000;
    private static final int TIMEOUT_DELAY_SHORT = 1500;
    private static final int TIMEOUT_DELAY_COLLAPSED = 4500;
    private static final int TIMEOUT_DELAY_EXPANDED = 10000;

    private static final int MSG_VOLUME_CHANGED = 0;
    private static final int MSG_FREE_RESOURCES = 1;
    private static final int MSG_PLAY_SOUND = 2;
    private static final int MSG_STOP_SOUNDS = 3;
    private static final int MSG_VIBRATE = 4;
    private static final int MSG_TIMEOUT = 5;
    private static final int MSG_RINGER_MODE_CHANGED = 6;
    private static final int MSG_MUTE_CHANGED = 7;
    private static final int MSG_REMOTE_VOLUME_CHANGED = 8;
    private static final int MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN = 9;
    private static final int MSG_SLIDER_VISIBILITY_CHANGED = 10;
    private static final int MSG_DISPLAY_SAFE_VOLUME_WARNING = 11;
    private static final int MSG_LAYOUT_DIRECTION = 12;
    private static final int MSG_ZEN_MODE_CHANGED = 13;
    private static final int MSG_USER_ACTIVITY = 14;

    // Pseudo stream type for master volume
    private static final int STREAM_MASTER = -100;
    // Pseudo stream type for remote volume is defined in AudioService.STREAM_REMOTE_MUSIC

    private final String mTag;
    protected final Context mContext;
    private final AudioManager mAudioManager;
    private final ZenModeController mZenController;
    private boolean mRingIsSilent;
    private boolean mVoiceCapable;
    private boolean mZenModeCapable;
    private boolean mZenPanelExpanded;
    private int mTimeoutDelay = TIMEOUT_DELAY;

    // True if we want to play tones on the system stream when the master stream is specified.
    private final boolean mPlayMasterStreamTones;


    /** Volume panel content view */
    private final View mView;
    /** Dialog hosting the panel, if not embedded */
    private final Dialog mDialog;
    /** Parent view hosting the panel, if embedded */
    private final ViewGroup mParent;

    /** The visible portion of the volume overlay */
    private final ViewGroup mPanel;
    /** Contains the slider and its touchable icons */
    private final ViewGroup mSliderPanel;
    /** The zen mode configuration panel view stub */
    private final ViewStub mZenPanelStub;
    /** The zen mode configuration panel view, once inflated */
    private ZenModePanel mZenPanel;

    private Callback mCallback;

    /** Currently active stream that shows up at the top of the list of sliders */
    private int mActiveStreamType = -1;
    /** All the slider controls mapped by stream type */
    private HashMap<Integer,StreamControl> mStreamControls;

    private enum StreamResources {
        BluetoothSCOStream(AudioManager.STREAM_BLUETOOTH_SCO,
                R.string.volume_icon_description_bluetooth,
                R.drawable.ic_audio_bt,
                R.drawable.ic_audio_bt,
                false),
        RingerStream(AudioManager.STREAM_RING,
                R.string.volume_icon_description_ringer,
                com.android.systemui.R.drawable.ic_ringer_audible,
                com.android.systemui.R.drawable.ic_ringer_silent,
                false),
        VoiceStream(AudioManager.STREAM_VOICE_CALL,
                R.string.volume_icon_description_incall,
                R.drawable.ic_audio_phone,
                R.drawable.ic_audio_phone,
                false),
        AlarmStream(AudioManager.STREAM_ALARM,
                R.string.volume_alarm,
                R.drawable.ic_audio_alarm,
                R.drawable.ic_audio_alarm_mute,
                false),
        MediaStream(AudioManager.STREAM_MUSIC,
                R.string.volume_icon_description_media,
                R.drawable.ic_audio_vol,
                R.drawable.ic_audio_vol_mute,
                true),
        NotificationStream(AudioManager.STREAM_NOTIFICATION,
                R.string.volume_icon_description_notification,
                com.android.systemui.R.drawable.ic_ringer_audible,
                com.android.systemui.R.drawable.ic_ringer_silent,
                true),
        // for now, use media resources for master volume
        MasterStream(STREAM_MASTER,
                R.string.volume_icon_description_media, //FIXME should have its own description
                R.drawable.ic_audio_vol,
                R.drawable.ic_audio_vol_mute,
                false),
        RemoteStream(AudioService.STREAM_REMOTE_MUSIC,
                R.string.volume_icon_description_media, //FIXME should have its own description
                R.drawable.ic_media_route_on_holo_dark,
                R.drawable.ic_media_route_disabled_holo_dark,
                false);// will be dynamically updated

        int streamType;
        int descRes;
        int iconRes;
        int iconMuteRes;
        // RING, VOICE_CALL & BLUETOOTH_SCO are hidden unless explicitly requested
        boolean show;

        StreamResources(int streamType, int descRes, int iconRes, int iconMuteRes, boolean show) {
            this.streamType = streamType;
            this.descRes = descRes;
            this.iconRes = iconRes;
            this.iconMuteRes = iconMuteRes;
            this.show = show;
        }
    }

    // List of stream types and their order
    private static final StreamResources[] STREAMS = {
        StreamResources.BluetoothSCOStream,
        StreamResources.RingerStream,
        StreamResources.VoiceStream,
        StreamResources.MediaStream,
        StreamResources.NotificationStream,
        StreamResources.AlarmStream,
        StreamResources.MasterStream,
        StreamResources.RemoteStream
    };

    /** Object that contains data for each slider */
    private class StreamControl {
        int streamType;
        MediaController controller;
        ViewGroup group;
        ImageView icon;
        SeekBar seekbarView;
        int iconRes;
        int iconMuteRes;
    }

    // Synchronize when accessing this
    private ToneGenerator mToneGenerators[];
    private Vibrator mVibrator;

    private static AlertDialog sConfirmSafeVolumeDialog;
    private static Object sConfirmSafeVolumeLock = new Object();

    private static class WarningDialogReceiver extends BroadcastReceiver
            implements DialogInterface.OnDismissListener {
        private final Context mContext;
        private final Dialog mDialog;
        private final VolumePanel mVolumePanel;

        WarningDialogReceiver(Context context, Dialog dialog, VolumePanel volumePanel) {
            mContext = context;
            mDialog = dialog;
            mVolumePanel = volumePanel;
            IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mDialog.cancel();
            cleanUp();
        }

        @Override
        public void onDismiss(DialogInterface unused) {
            mContext.unregisterReceiver(this);
            cleanUp();
        }

        private void cleanUp() {
            synchronized (sConfirmSafeVolumeLock) {
                sConfirmSafeVolumeDialog = null;
            }
            mVolumePanel.forceTimeout(0);
            mVolumePanel.updateStates();
        }
    }


    public VolumePanel(Context context, ViewGroup parent, ZenModeController zenController) {
        mTag = String.format("%s.%s.%08x", TAG, parent == null ? "Dialog" : "Embed", hashCode());
        mContext = context;
        mParent = parent;
        mZenController = zenController;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // For now, only show master volume if master volume is supported
        final Resources res = context.getResources();
        final boolean useMasterVolume = res.getBoolean(R.bool.config_useMasterVolume);
        if (useMasterVolume) {
            for (int i = 0; i < STREAMS.length; i++) {
                StreamResources streamRes = STREAMS[i];
                streamRes.show = (streamRes.streamType == STREAM_MASTER);
            }
        }
        if (LOGD) Log.d(mTag, String.format("new VolumePanel hasParent=%s", parent != null));
        if (parent == null) {
            // dialog mode
            mDialog = new Dialog(context) {
                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    if (isShowing() && event.getAction() == MotionEvent.ACTION_OUTSIDE &&
                            sConfirmSafeVolumeDialog == null) {
                        forceTimeout(0);
                        return true;
                    }
                    return false;
                }
            };

            // Change some window properties
            final Window window = mDialog.getWindow();
            final LayoutParams lp = window.getAttributes();
            lp.token = null;
            // Offset from the top
            lp.y = res.getDimensionPixelOffset(com.android.systemui.R.dimen.volume_panel_top);
            lp.type = LayoutParams.TYPE_STATUS_BAR_PANEL;
            lp.format = PixelFormat.TRANSLUCENT;
            lp.windowAnimations = com.android.systemui.R.style.VolumePanelAnimation;
            lp.gravity = Gravity.TOP;
            window.setAttributes(lp);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.addFlags(LayoutParams.FLAG_NOT_FOCUSABLE
                    | LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | LayoutParams.FLAG_HARDWARE_ACCELERATED);
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.setContentView(com.android.systemui.R.layout.volume_dialog);
            mDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mActiveStreamType = -1;
                    mAudioManager.forceVolumeControlStream(mActiveStreamType);
                    setZenPanelVisible(false);
                }
            });

            mDialog.create();
            // temporary workaround, until we support window-level shadows
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));

            mView = window.findViewById(R.id.content);
            mView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    resetTimeout();
                    return false;
                }
            });

        } else {
            // embedded mode
            mDialog = null;
            mView = LayoutInflater.from(mContext).inflate(
                    com.android.systemui.R.layout.volume_panel, parent, false);
        }
        mPanel = (ViewGroup) mView.findViewById(com.android.systemui.R.id.visible_panel);
        mSliderPanel = (ViewGroup) mView.findViewById(com.android.systemui.R.id.slider_panel);
        mZenPanelStub = (ViewStub)mView.findViewById(com.android.systemui.R.id.zen_panel_stub);

        mToneGenerators = new ToneGenerator[AudioSystem.getNumStreamTypes()];
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mVoiceCapable = context.getResources().getBoolean(R.bool.config_voice_capable);

        mZenModeCapable = !useMasterVolume && mZenController != null;
        updateZenMode(mZenController != null ? mZenController.getZen() : Global.ZEN_MODE_OFF);
        mZenController.addCallback(mZenCallback);

        final boolean masterVolumeOnly = res.getBoolean(R.bool.config_useMasterVolume);
        final boolean masterVolumeKeySounds = res.getBoolean(R.bool.config_useVolumeKeySounds);
        mPlayMasterStreamTones = masterVolumeOnly && masterVolumeKeySounds;

        listenToRingerMode();
    }

    public View getContentView() {
        return mView;
    }

    private void setLayoutDirection(int layoutDirection) {
        mPanel.setLayoutDirection(layoutDirection);
        updateStates();
    }

    private void listenToRingerMode() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                    removeMessages(MSG_RINGER_MODE_CHANGED);
                    sendMessage(obtainMessage(MSG_RINGER_MODE_CHANGED));
                }
            }
        }, filter);
    }

    private boolean isMuted(int streamType) {
        if (streamType == STREAM_MASTER) {
            return mAudioManager.isMasterMute();
        } else if (streamType == AudioService.STREAM_REMOTE_MUSIC) {
            // TODO do we need to support a distinct mute property for remote?
            return false;
        } else {
            return mAudioManager.isStreamMute(streamType);
        }
    }

    private int getStreamMaxVolume(int streamType) {
        if (streamType == STREAM_MASTER) {
            return mAudioManager.getMasterMaxVolume();
        } else if (streamType == AudioService.STREAM_REMOTE_MUSIC) {
            if (mStreamControls != null) {
                StreamControl sc = mStreamControls.get(streamType);
                if (sc != null && sc.controller != null) {
                    VolumeInfo vi = sc.controller.getVolumeInfo();
                    return vi.getMaxVolume();
                }
            }
            return -1;
        } else {
            return mAudioManager.getStreamMaxVolume(streamType);
        }
    }

    private int getStreamVolume(int streamType) {
        if (streamType == STREAM_MASTER) {
            return mAudioManager.getMasterVolume();
        } else if (streamType == AudioService.STREAM_REMOTE_MUSIC) {
            if (mStreamControls != null) {
                StreamControl sc = mStreamControls.get(streamType);
                if (sc != null && sc.controller != null) {
                    VolumeInfo vi = sc.controller.getVolumeInfo();
                    return vi.getCurrentVolume();
                }
            }
            return -1;
        } else {
            return mAudioManager.getStreamVolume(streamType);
        }
    }

    private void setStreamVolume(StreamControl sc, int index, int flags) {
        if (sc.streamType == AudioService.STREAM_REMOTE_MUSIC) {
            if (sc.controller != null) {
                sc.controller.setVolumeTo(index, flags);
            } else {
                Log.w(mTag, "Adjusting remote volume without a controller!");
            }
        } else if (getStreamVolume(sc.streamType) != index) {
            if (sc.streamType == STREAM_MASTER) {
                mAudioManager.setMasterVolume(index, flags);
            } else {
                mAudioManager.setStreamVolume(sc.streamType, index, flags);
            }
        }
    }

    private void createSliders() {
        final Resources res = mContext.getResources();
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        mStreamControls = new HashMap<Integer, StreamControl>(STREAMS.length);

        for (int i = 0; i < STREAMS.length; i++) {
            StreamResources streamRes = STREAMS[i];

            final int streamType = streamRes.streamType;

            final StreamControl sc = new StreamControl();
            sc.streamType = streamType;
            sc.group = (ViewGroup) inflater.inflate(
                    com.android.systemui.R.layout.volume_panel_item, null);
            sc.group.setTag(sc);
            sc.icon = (ImageView) sc.group.findViewById(com.android.systemui.R.id.stream_icon);
            sc.icon.setTag(sc);
            sc.icon.setContentDescription(res.getString(streamRes.descRes));
            sc.iconRes = streamRes.iconRes;
            sc.iconMuteRes = streamRes.iconMuteRes;
            sc.icon.setImageResource(sc.iconRes);
            sc.icon.setClickable(isNotificationOrRing(streamType));
            if (sc.icon.isClickable()) {
                sc.icon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resetTimeout();
                        toggle(sc);
                    }
                });
            }
            sc.seekbarView = (SeekBar) sc.group.findViewById(com.android.systemui.R.id.seekbar);
            final int plusOne = (streamType == AudioSystem.STREAM_BLUETOOTH_SCO ||
                    streamType == AudioSystem.STREAM_VOICE_CALL) ? 1 : 0;
            sc.seekbarView.setMax(getStreamMaxVolume(streamType) + plusOne);
            sc.seekbarView.setOnSeekBarChangeListener(mSeekListener);
            sc.seekbarView.setTag(sc);
            mStreamControls.put(streamType, sc);
        }
    }

    private void toggle(StreamControl sc) {
        if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            postVolumeChanged(sc.streamType, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_VIBRATE);
        } else {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            postVolumeChanged(sc.streamType, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    private void reorderSliders(int activeStreamType) {
        mSliderPanel.removeAllViews();

        final StreamControl active = mStreamControls.get(activeStreamType);
        if (active == null) {
            Log.e(TAG, "Missing stream type! - " + activeStreamType);
            mActiveStreamType = -1;
        } else {
            mSliderPanel.addView(active.group);
            mActiveStreamType = activeStreamType;
            active.group.setVisibility(View.VISIBLE);
            updateSlider(active);
            updateTimeoutDelay();
            setZenPanelVisible(isNotificationOrRing(mActiveStreamType));
        }
    }

    /** Update the mute and progress state of a slider */
    private void updateSlider(StreamControl sc) {
        sc.seekbarView.setProgress(getStreamVolume(sc.streamType));
        final boolean muted = isMuted(sc.streamType);
        // Force reloading the image resource
        sc.icon.setImageDrawable(null);
        sc.icon.setImageResource(muted ? sc.iconMuteRes : sc.iconRes);
        if (isNotificationOrRing(sc.streamType) &&
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            sc.icon.setImageResource(com.android.systemui.R.drawable.ic_ringer_vibrate);
        }
        updateSliderEnabled(sc, muted, false);
    }

    private void updateSliderEnabled(final StreamControl sc, boolean muted, boolean fixedVolume) {
        final boolean wasEnabled = sc.seekbarView.isEnabled();
        final boolean isRinger = isNotificationOrRing(sc.streamType);
        if (sc.streamType == AudioService.STREAM_REMOTE_MUSIC) {
            // never disable touch interactions for remote playback, the muting is not tied to
            // the state of the phone.
            sc.seekbarView.setEnabled(!fixedVolume);
        } else if (fixedVolume ||
                        (sc.streamType != mAudioManager.getMasterStreamType() && muted) ||
                        (sConfirmSafeVolumeDialog != null)) {
            sc.seekbarView.setEnabled(false);
        } else if (isRinger && mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            sc.seekbarView.setEnabled(false);
            sc.icon.setEnabled(false);
            sc.icon.setClickable(false);
        } else {
            sc.seekbarView.setEnabled(true);
            sc.icon.setEnabled(true);
        }
        // show the silent hint when the disabled slider is touched in silent mode
        if (isRinger && wasEnabled != sc.seekbarView.isEnabled()) {
            if (sc.seekbarView.isEnabled()) {
                sc.group.setOnTouchListener(null);
                sc.icon.setClickable(true);
            } else {
                final View.OnTouchListener showHintOnTouch = new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        resetTimeout();
                        showSilentHint();
                        return false;
                    }
                };
                sc.group.setOnTouchListener(showHintOnTouch);
            }
        }
    }

    private void showSilentHint() {
        if (mZenPanel != null) {
            mZenPanel.showSilentHint();
        }
    }

    private static boolean isNotificationOrRing(int streamType) {
        return streamType == AudioManager.STREAM_RING
                || streamType == AudioManager.STREAM_NOTIFICATION;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void updateTimeoutDelay() {
        mTimeoutDelay = mActiveStreamType == AudioManager.STREAM_MUSIC ? TIMEOUT_DELAY_SHORT
                : mZenPanelExpanded ? TIMEOUT_DELAY_EXPANDED
                : isZenPanelVisible() ? TIMEOUT_DELAY_COLLAPSED
                : TIMEOUT_DELAY;
    }

    private boolean isZenPanelVisible() {
        return mZenPanel != null && mZenPanel.getVisibility() == View.VISIBLE;
    }

    private void setZenPanelVisible(boolean visible) {
        if (LOGD) Log.d(mTag, "setZenPanelVisible " + visible + " mZenPanel=" + mZenPanel);
        final boolean changing = visible != isZenPanelVisible();
        if (visible) {
            if (mZenPanel == null) {
                mZenPanel = (ZenModePanel) mZenPanelStub.inflate();
                mZenPanel.init(mZenController, mDialog != null ? 'D' : 'E');
                mZenPanel.setCallback(new ZenModePanel.Callback() {
                    @Override
                    public void onMoreSettings() {
                        if (mCallback != null) {
                            mCallback.onZenSettings();
                        }
                    }

                    @Override
                    public void onInteraction() {
                        resetTimeout();
                    }

                    @Override
                    public void onExpanded(boolean expanded) {
                        if (mZenPanelExpanded == expanded) return;
                        mZenPanelExpanded = expanded;
                        updateTimeoutDelay();
                        resetTimeout();
                    }
                });
            }
            mZenPanel.setVisibility(View.VISIBLE);
            resetTimeout();
        } else {
            if (mZenPanel != null) {
                mZenPanel.setVisibility(View.GONE);
            }
        }
        if (changing) {
            updateTimeoutDelay();
            resetTimeout();
        }
    }

    public void updateStates() {
        final int count = mSliderPanel.getChildCount();
        for (int i = 0; i < count; i++) {
            StreamControl sc = (StreamControl) mSliderPanel.getChildAt(i).getTag();
            updateSlider(sc);
        }
    }

    private void updateZenMode(int zen) {
        final boolean show = mZenModeCapable && isNotificationOrRing(mActiveStreamType);
        setZenPanelVisible(show);
    }

    public void postZenModeChanged(int zen) {
        removeMessages(MSG_ZEN_MODE_CHANGED);
        obtainMessage(MSG_ZEN_MODE_CHANGED, zen).sendToTarget();
    }

    public void postVolumeChanged(int streamType, int flags) {
        if (hasMessages(MSG_VOLUME_CHANGED)) return;
        synchronized (this) {
            if (mStreamControls == null) {
                createSliders();
            }
        }
        removeMessages(MSG_FREE_RESOURCES);
        obtainMessage(MSG_VOLUME_CHANGED, streamType, flags).sendToTarget();
    }

    public void postRemoteVolumeChanged(MediaController controller, int flags) {
        if (hasMessages(MSG_REMOTE_VOLUME_CHANGED)) return;
        synchronized (this) {
            if (mStreamControls == null) {
                createSliders();
            }
        }
        removeMessages(MSG_FREE_RESOURCES);
        obtainMessage(MSG_REMOTE_VOLUME_CHANGED, flags, 0, controller).sendToTarget();
    }

    public void postRemoteSliderVisibility(boolean visible) {
        obtainMessage(MSG_SLIDER_VISIBILITY_CHANGED,
                AudioService.STREAM_REMOTE_MUSIC, visible ? 1 : 0).sendToTarget();
    }

    /**
     * Called by AudioService when it has received new remote playback information that
     * would affect the VolumePanel display (mainly volumes). The difference with
     * {@link #postRemoteVolumeChanged(int, int)} is that the handling of the posted message
     * (MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN) will only update the volume slider if it is being
     * displayed.
     * This special code path is due to the fact that remote volume updates arrive to AudioService
     * asynchronously. So after AudioService has sent the volume update (which should be treated
     * as a request to update the volume), the application will likely set a new volume. If the UI
     * is still up, we need to refresh the display to show this new value.
     */
    public void postHasNewRemotePlaybackInfo() {
        if (hasMessages(MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN)) return;
        // don't create or prevent resources to be freed, if they disappear, this update came too
        //   late and shouldn't warrant the panel to be displayed longer
        obtainMessage(MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN).sendToTarget();
    }

    public void postMasterVolumeChanged(int flags) {
        postVolumeChanged(STREAM_MASTER, flags);
    }

    public void postMuteChanged(int streamType, int flags) {
        if (hasMessages(MSG_VOLUME_CHANGED)) return;
        synchronized (this) {
            if (mStreamControls == null) {
                createSliders();
            }
        }
        removeMessages(MSG_FREE_RESOURCES);
        obtainMessage(MSG_MUTE_CHANGED, streamType, flags).sendToTarget();
    }

    public void postMasterMuteChanged(int flags) {
        postMuteChanged(STREAM_MASTER, flags);
    }

    public void postDisplaySafeVolumeWarning(int flags) {
        if (hasMessages(MSG_DISPLAY_SAFE_VOLUME_WARNING)) return;
        obtainMessage(MSG_DISPLAY_SAFE_VOLUME_WARNING, flags, 0).sendToTarget();
    }

    public void postDismiss(long delay) {
        forceTimeout(delay);
    }

    public void postLayoutDirection(int layoutDirection) {
        removeMessages(MSG_LAYOUT_DIRECTION);
        obtainMessage(MSG_LAYOUT_DIRECTION, layoutDirection, 0).sendToTarget();
    }

    /**
     * Override this if you have other work to do when the volume changes (for
     * example, vibrating, playing a sound, etc.). Make sure to call through to
     * the superclass implementation.
     */
    protected void onVolumeChanged(int streamType, int flags) {

        if (LOGD) Log.d(mTag, "onVolumeChanged(streamType: " + streamType + ", flags: " + flags + ")");

        if ((flags & AudioManager.FLAG_SHOW_UI) != 0) {
            synchronized (this) {
                if (mActiveStreamType != streamType) {
                    reorderSliders(streamType);
                }
                onShowVolumeChanged(streamType, flags, null);
            }
        }

        if ((flags & AudioManager.FLAG_PLAY_SOUND) != 0 && ! mRingIsSilent) {
            removeMessages(MSG_PLAY_SOUND);
            sendMessageDelayed(obtainMessage(MSG_PLAY_SOUND, streamType, flags), PLAY_SOUND_DELAY);
        }

        if ((flags & AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE) != 0) {
            removeMessages(MSG_PLAY_SOUND);
            removeMessages(MSG_VIBRATE);
            onStopSounds();
        }

        removeMessages(MSG_FREE_RESOURCES);
        sendMessageDelayed(obtainMessage(MSG_FREE_RESOURCES), FREE_DELAY);
        resetTimeout();
    }

    protected void onMuteChanged(int streamType, int flags) {

        if (LOGD) Log.d(mTag, "onMuteChanged(streamType: " + streamType + ", flags: " + flags + ")");

        StreamControl sc = mStreamControls.get(streamType);
        if (sc != null) {
            sc.icon.setImageResource(isMuted(sc.streamType) ? sc.iconMuteRes : sc.iconRes);
        }

        onVolumeChanged(streamType, flags);
    }

    protected void onShowVolumeChanged(int streamType, int flags, MediaController controller) {
        int index = getStreamVolume(streamType);

        mRingIsSilent = false;

        if (LOGD) {
            Log.d(mTag, "onShowVolumeChanged(streamType: " + streamType
                    + ", flags: " + flags + "), index: " + index);
        }

        // get max volume for progress bar

        int max = getStreamMaxVolume(streamType);
        StreamControl sc = mStreamControls.get(streamType);

        switch (streamType) {

            case AudioManager.STREAM_RING: {
//                setRingerIcon();
                Uri ringuri = RingtoneManager.getActualDefaultRingtoneUri(
                        mContext, RingtoneManager.TYPE_RINGTONE);
                if (ringuri == null) {
                    mRingIsSilent = true;
                }
                break;
            }

            case AudioManager.STREAM_MUSIC: {
                // Special case for when Bluetooth is active for music
                if ((mAudioManager.getDevicesForStream(AudioManager.STREAM_MUSIC) &
                        (AudioManager.DEVICE_OUT_BLUETOOTH_A2DP |
                        AudioManager.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES |
                        AudioManager.DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER)) != 0) {
                    setMusicIcon(R.drawable.ic_audio_bt, R.drawable.ic_audio_bt_mute);
                } else {
                    setMusicIcon(R.drawable.ic_audio_vol, R.drawable.ic_audio_vol_mute);
                }
                break;
            }

            case AudioManager.STREAM_VOICE_CALL: {
                /*
                 * For in-call voice call volume, there is no inaudible volume.
                 * Rescale the UI control so the progress bar doesn't go all
                 * the way to zero and don't show the mute icon.
                 */
                index++;
                max++;
                break;
            }

            case AudioManager.STREAM_ALARM: {
                break;
            }

            case AudioManager.STREAM_NOTIFICATION: {
                Uri ringuri = RingtoneManager.getActualDefaultRingtoneUri(
                        mContext, RingtoneManager.TYPE_NOTIFICATION);
                if (ringuri == null) {
                    mRingIsSilent = true;
                }
                break;
            }

            case AudioManager.STREAM_BLUETOOTH_SCO: {
                /*
                 * For in-call voice call volume, there is no inaudible volume.
                 * Rescale the UI control so the progress bar doesn't go all
                 * the way to zero and don't show the mute icon.
                 */
                index++;
                max++;
                break;
            }

            case AudioService.STREAM_REMOTE_MUSIC: {
                if (controller == null && sc != null) {
                    // If we weren't passed one try using the last one set.
                    controller = sc.controller;
                }
                if (controller == null) {
                    // We still don't have one, ignore the command.
                    Log.w(mTag, "sent remote volume change without a controller!");
                } else {
                    VolumeInfo vi = controller.getVolumeInfo();
                    index = vi.getCurrentVolume();
                    max = vi.getMaxVolume();
                    if ((vi.getVolumeControl() & VolumeProvider.VOLUME_CONTROL_FIXED) != 0) {
                        // if the remote volume is fixed add the flag for the UI
                        flags |= AudioManager.FLAG_FIXED_VOLUME;
                    }
                }
                if (LOGD) { Log.d(mTag, "showing remote volume "+index+" over "+ max); }
                break;
            }
        }

        if (sc != null) {
            if (streamType == AudioService.STREAM_REMOTE_MUSIC && controller != sc.controller) {
                if (sc.controller != null) {
                    sc.controller.removeCallback(mMediaControllerCb);
                }
                sc.controller = controller;
                if (controller != null) {
                    sc.controller.addCallback(mMediaControllerCb);
                }
            }
            if (sc.seekbarView.getMax() != max) {
                sc.seekbarView.setMax(max);
            }

            sc.seekbarView.setProgress(index);
            updateSliderEnabled(sc, isMuted(streamType),
                    (flags & AudioManager.FLAG_FIXED_VOLUME) != 0);
        }

        if (!isShowing()) {
            int stream = (streamType == AudioService.STREAM_REMOTE_MUSIC) ? -1 : streamType;
            // when the stream is for remote playback, use -1 to reset the stream type evaluation
            mAudioManager.forceVolumeControlStream(stream);

            if (mDialog != null) {
                mDialog.show();
            }
        }

        // Do a little vibrate if applicable (only when going into vibrate mode)
        if ((streamType != AudioService.STREAM_REMOTE_MUSIC) &&
                ((flags & AudioManager.FLAG_VIBRATE) != 0) &&
                mAudioManager.isStreamAffectedByRingerMode(streamType) &&
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            sendMessageDelayed(obtainMessage(MSG_VIBRATE), VIBRATE_DELAY);
        }

        // Pulse the slider icon if an adjustment was suppressed due to silent mode.
        if ((flags & AudioManager.FLAG_SHOW_SILENT_HINT) != 0) {
            showSilentHint();
        }
    }

    private boolean isShowing() {
        return mDialog != null ? mDialog.isShowing() : mParent.isAttachedToWindow();
    }

    protected void onPlaySound(int streamType, int flags) {

        if (hasMessages(MSG_STOP_SOUNDS)) {
            removeMessages(MSG_STOP_SOUNDS);
            // Force stop right now
            onStopSounds();
        }

        synchronized (this) {
            ToneGenerator toneGen = getOrCreateToneGenerator(streamType);
            if (toneGen != null) {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP);
                sendMessageDelayed(obtainMessage(MSG_STOP_SOUNDS), BEEP_DURATION);
            }
        }
    }

    protected void onStopSounds() {

        synchronized (this) {
            int numStreamTypes = AudioSystem.getNumStreamTypes();
            for (int i = numStreamTypes - 1; i >= 0; i--) {
                ToneGenerator toneGen = mToneGenerators[i];
                if (toneGen != null) {
                    toneGen.stopTone();
                }
            }
        }
    }

    protected void onVibrate() {

        // Make sure we ended up in vibrate ringer mode
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
            return;
        }

        mVibrator.vibrate(VIBRATE_DURATION, AudioManager.STREAM_SYSTEM);
    }

    protected void onRemoteVolumeChanged(MediaController controller, int flags) {
        if (LOGD) Log.d(mTag, "onRemoteVolumeChanged(controller:" + controller + ", flags: " + flags
                    + ")");

        if (((flags & AudioManager.FLAG_SHOW_UI) != 0) || isShowing()) {
            synchronized (this) {
                if (mActiveStreamType != AudioService.STREAM_REMOTE_MUSIC) {
                    reorderSliders(AudioService.STREAM_REMOTE_MUSIC);
                }
                onShowVolumeChanged(AudioService.STREAM_REMOTE_MUSIC, flags, controller);
            }
        } else {
            if (LOGD) Log.d(mTag, "not calling onShowVolumeChanged(), no FLAG_SHOW_UI or no UI");
        }

        removeMessages(MSG_FREE_RESOURCES);
        sendMessageDelayed(obtainMessage(MSG_FREE_RESOURCES), FREE_DELAY);
        resetTimeout();
    }

    protected void onRemoteVolumeUpdateIfShown() {
        if (LOGD) Log.d(mTag, "onRemoteVolumeUpdateIfShown()");
        if (isShowing()
                && (mActiveStreamType == AudioService.STREAM_REMOTE_MUSIC)
                && (mStreamControls != null)) {
            onShowVolumeChanged(AudioService.STREAM_REMOTE_MUSIC, 0, null);
        }
    }

    /**
     * Clear the current remote stream controller.
     */
    private void clearRemoteStreamController() {
        if (mStreamControls != null) {
            StreamControl sc = mStreamControls.get(AudioService.STREAM_REMOTE_MUSIC);
            if (sc != null) {
                if (sc.controller != null) {
                    sc.controller.removeCallback(mMediaControllerCb);
                    sc.controller = null;
                }
            }
        }
    }

    /**
     * Handler for MSG_SLIDER_VISIBILITY_CHANGED
     * Hide or show a slider
     * @param streamType can be a valid stream type value, or VolumePanel.STREAM_MASTER,
     *                   or AudioService.STREAM_REMOTE_MUSIC
     * @param visible
     */
    synchronized protected void onSliderVisibilityChanged(int streamType, int visible) {
        if (LOGD) Log.d(mTag, "onSliderVisibilityChanged(stream="+streamType+", visi="+visible+")");
        boolean isVisible = (visible == 1);
        for (int i = STREAMS.length - 1 ; i >= 0 ; i--) {
            StreamResources streamRes = STREAMS[i];
            if (streamRes.streamType == streamType) {
                streamRes.show = isVisible;
                if (!isVisible && (mActiveStreamType == streamType)) {
                    mActiveStreamType = -1;
                }
                break;
            }
        }
    }

    protected void onDisplaySafeVolumeWarning(int flags) {
        if ((flags & AudioManager.FLAG_SHOW_UI) != 0 || isShowing()) {
            synchronized (sConfirmSafeVolumeLock) {
                if (sConfirmSafeVolumeDialog != null) {
                    return;
                }
                sConfirmSafeVolumeDialog = new AlertDialog.Builder(mContext)
                        .setMessage(com.android.internal.R.string.safe_media_volume_warning)
                        .setPositiveButton(com.android.internal.R.string.yes,
                                            new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAudioManager.disableSafeMediaVolume();
                            }
                        })
                        .setNegativeButton(com.android.internal.R.string.no, null)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .create();
                final WarningDialogReceiver warning = new WarningDialogReceiver(mContext,
                        sConfirmSafeVolumeDialog, this);

                sConfirmSafeVolumeDialog.setOnDismissListener(warning);
                sConfirmSafeVolumeDialog.getWindow().setType(
                                                WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
                sConfirmSafeVolumeDialog.show();
            }
            updateStates();
        }
        resetTimeout();
    }

    /**
     * Lock on this VolumePanel instance as long as you use the returned ToneGenerator.
     */
    private ToneGenerator getOrCreateToneGenerator(int streamType) {
        if (streamType == STREAM_MASTER) {
            // For devices that use the master volume setting only but still want to
            // play a volume-changed tone, direct the master volume pseudostream to
            // the system stream's tone generator.
            if (mPlayMasterStreamTones) {
                streamType = AudioManager.STREAM_SYSTEM;
            } else {
                return null;
            }
        }
        synchronized (this) {
            if (mToneGenerators[streamType] == null) {
                try {
                    mToneGenerators[streamType] = new ToneGenerator(streamType, MAX_VOLUME);
                } catch (RuntimeException e) {
                    if (LOGD) {
                        Log.d(mTag, "ToneGenerator constructor failed with "
                                + "RuntimeException: " + e);
                    }
                }
            }
            return mToneGenerators[streamType];
        }
    }


    /**
     * Switch between icons because Bluetooth music is same as music volume, but with
     * different icons.
     */
    private void setMusicIcon(int resId, int resMuteId) {
        StreamControl sc = mStreamControls.get(AudioManager.STREAM_MUSIC);
        if (sc != null) {
            sc.iconRes = resId;
            sc.iconMuteRes = resMuteId;
            sc.icon.setImageResource(isMuted(sc.streamType) ? sc.iconMuteRes : sc.iconRes);
        }
    }

    protected void onFreeResources() {
        synchronized (this) {
            for (int i = mToneGenerators.length - 1; i >= 0; i--) {
                if (mToneGenerators[i] != null) {
                    mToneGenerators[i].release();
                }
                mToneGenerators[i] = null;
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {

            case MSG_VOLUME_CHANGED: {
                onVolumeChanged(msg.arg1, msg.arg2);
                break;
            }

            case MSG_MUTE_CHANGED: {
                onMuteChanged(msg.arg1, msg.arg2);
                break;
            }

            case MSG_FREE_RESOURCES: {
                onFreeResources();
                break;
            }

            case MSG_STOP_SOUNDS: {
                onStopSounds();
                break;
            }

            case MSG_PLAY_SOUND: {
                onPlaySound(msg.arg1, msg.arg2);
                break;
            }

            case MSG_VIBRATE: {
                onVibrate();
                break;
            }

            case MSG_TIMEOUT: {
                if (isShowing()) {
                    if (mDialog != null) {
                        mDialog.dismiss();
                        clearRemoteStreamController();
                        mActiveStreamType = -1;
                    }
                }
                synchronized (sConfirmSafeVolumeLock) {
                    if (sConfirmSafeVolumeDialog != null) {
                        sConfirmSafeVolumeDialog.dismiss();
                    }
                }
                break;
            }
            case MSG_RINGER_MODE_CHANGED: {
                if (isShowing()) {
                    updateStates();
                }
                break;
            }

            case MSG_REMOTE_VOLUME_CHANGED: {
                onRemoteVolumeChanged((MediaController) msg.obj, msg.arg1);
                break;
            }

            case MSG_REMOTE_VOLUME_UPDATE_IF_SHOWN:
                onRemoteVolumeUpdateIfShown();
                break;

            case MSG_SLIDER_VISIBILITY_CHANGED:
                onSliderVisibilityChanged(msg.arg1, msg.arg2);
                break;

            case MSG_DISPLAY_SAFE_VOLUME_WARNING:
                onDisplaySafeVolumeWarning(msg.arg1);
                break;

            case MSG_LAYOUT_DIRECTION:
                setLayoutDirection(msg.arg1);
                break;

            case MSG_ZEN_MODE_CHANGED:
                updateZenMode(msg.arg1);
                break;

            case MSG_USER_ACTIVITY:
                if (mCallback != null) {
                    mCallback.onInteraction();
                }
                break;
        }
    }

    private void resetTimeout() {
        if (LOGD) Log.d(mTag, "resetTimeout at " + System.currentTimeMillis()
                + " delay=" + mTimeoutDelay);
        removeMessages(MSG_TIMEOUT);
        sendEmptyMessageDelayed(MSG_TIMEOUT, mTimeoutDelay);
        removeMessages(MSG_USER_ACTIVITY);
        sendEmptyMessage(MSG_USER_ACTIVITY);
    }

    private void forceTimeout(long delay) {
        removeMessages(MSG_TIMEOUT);
        sendEmptyMessageDelayed(MSG_TIMEOUT, delay);
    }

    public ZenModeController getZenController() {
        return mZenController;
    }

    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            final Object tag = seekBar.getTag();
            if (fromUser && tag instanceof StreamControl) {
                StreamControl sc = (StreamControl) tag;
                setStreamVolume(sc, progress, 0);
            }
            resetTimeout();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private final ZenModeController.Callback mZenCallback = new ZenModeController.Callback() {
        public void onZenChanged(int zen) {
            postZenModeChanged(zen);
        }
    };

    private final MediaController.Callback mMediaControllerCb = new MediaController.Callback() {
        public void onVolumeInfoChanged(VolumeInfo info) {
            onRemoteVolumeUpdateIfShown();
        }
    };

    public interface Callback {
        void onZenSettings();
        void onInteraction();
    }
}