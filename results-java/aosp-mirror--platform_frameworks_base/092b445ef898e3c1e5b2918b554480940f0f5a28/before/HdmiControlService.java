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

package com.android.server.hdmi;

import android.annotation.Nullable;
import android.content.Context;
import android.hardware.hdmi.HdmiCec;
import android.hardware.hdmi.HdmiCecDeviceInfo;
import android.hardware.hdmi.HdmiCecMessage;
import android.hardware.hdmi.IHdmiControlCallback;
import android.hardware.hdmi.IHdmiControlService;
import android.hardware.hdmi.IHdmiHotplugEventListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.android.internal.annotations.GuardedBy;
import com.android.server.SystemService;
import com.android.server.hdmi.DeviceDiscoveryAction.DeviceDiscoveryCallback;
import com.android.server.hdmi.HdmiCecController.AllocateAddressCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Provides a service for sending and processing HDMI control messages,
 * HDMI-CEC and MHL control command, and providing the information on both standard.
 */
public final class HdmiControlService extends SystemService {
    private static final String TAG = "HdmiControlService";

    // TODO: Rename the permission to HDMI_CONTROL.
    private static final String PERMISSION = "android.permission.HDMI_CEC";

    static final int SEND_RESULT_SUCCESS = 0;
    static final int SEND_RESULT_NAK = -1;
    static final int SEND_RESULT_FAILURE = -2;

    static final int POLL_STRATEGY_MASK = 0x3;  // first and second bit.
    static final int POLL_STRATEGY_REMOTES_DEVICES = 0x1;
    static final int POLL_STRATEGY_SYSTEM_AUDIO = 0x2;

    static final int POLL_ITERATION_STRATEGY_MASK = 0x30000;  // first and second bit.
    static final int POLL_ITERATION_IN_ORDER = 0x10000;
    static final int POLL_ITERATION_REVERSE_ORDER = 0x20000;

    /**
     * Interface to report send result.
     */
    interface SendMessageCallback {
        /**
         * Called when {@link HdmiControlService#sendCecCommand} is completed.
         *
         * @param error result of send request.
         * @see {@link #SEND_RESULT_SUCCESS}
         * @see {@link #SEND_RESULT_NAK}
         * @see {@link #SEND_RESULT_FAILURE}
         */
        void onSendCompleted(int error);
    }

    /**
     * Interface to get a list of available logical devices.
     */
    interface DevicePollingCallback {
        /**
         * Called when device polling is finished.
         *
         * @param ackedAddress a list of logical addresses of available devices
         */
        void onPollingFinished(List<Integer> ackedAddress);
    }

    // A thread to handle synchronous IO of CEC and MHL control service.
    // Since all of CEC and MHL HAL interfaces processed in short time (< 200ms)
    // and sparse call it shares a thread to handle IO operations.
    private final HandlerThread mIoThread = new HandlerThread("Hdmi Control Io Thread");

    // A collection of FeatureAction.
    // Note that access to this collection should happen in service thread.
    private final LinkedList<FeatureAction> mActions = new LinkedList<>();

    // Used to synchronize the access to the service.
    private final Object mLock = new Object();

    // Type of logical devices hosted in the system.
    @GuardedBy("mLock")
    private final int[] mLocalDevices;

    // List of listeners registered by callers that want to get notified of
    // hotplug events.
    private final ArrayList<IHdmiHotplugEventListener> mHotplugEventListeners = new ArrayList<>();

    // List of records for hotplug event listener to handle the the caller killed in action.
    private final ArrayList<HotplugEventListenerRecord> mHotplugEventListenerRecords =
            new ArrayList<>();

    private final HdmiCecMessageCache mCecMessageCache = new HdmiCecMessageCache();

    @Nullable
    private HdmiCecController mCecController;

    @Nullable
    private HdmiMhlController mMhlController;

    @GuardedBy("mLock")
    // Whether ARC is "enabled" or not.
    // TODO: it may need to hold lock if it's accessed from others.
    private boolean mArcStatusEnabled = false;

    @GuardedBy("mLock")
    // Whether SystemAudioMode is "On" or not.
    private boolean mSystemAudioMode;

    // Handler running on service thread. It's used to run a task in service thread.
    private final Handler mHandler = new Handler();

    public HdmiControlService(Context context) {
        super(context);
        mLocalDevices = getContext().getResources().getIntArray(
                com.android.internal.R.array.config_hdmiCecLogicalDeviceType);
    }

    @Override
    public void onStart() {
        mIoThread.start();
        mCecController = HdmiCecController.create(this);

        if (mCecController != null) {
            initializeLocalDevices(mLocalDevices);
        } else {
            Slog.i(TAG, "Device does not support HDMI-CEC.");
        }

        mMhlController = HdmiMhlController.create(this);
        if (mMhlController == null) {
            Slog.i(TAG, "Device does not support MHL-control.");
        }

        publishBinderService(Context.HDMI_CONTROL_SERVICE, new BinderService());

        // TODO: Read the preference for SystemAudioMode and initialize mSystemAudioMode and
        // start to monitor the preference value and invoke SystemAudioActionFromTv if needed.
    }

    private void initializeLocalDevices(final int[] deviceTypes) {
        // A container for [Logical Address, Local device info].
        final SparseArray<HdmiCecLocalDevice> devices = new SparseArray<>();
        final SparseIntArray finished = new SparseIntArray();
        for (int type : deviceTypes) {
            final HdmiCecLocalDevice localDevice = HdmiCecLocalDevice.create(this, type);
            localDevice.init();
            mCecController.allocateLogicalAddress(type,
                    localDevice.getPreferredAddress(), new AllocateAddressCallback() {
                @Override
                public void onAllocated(int deviceType, int logicalAddress) {
                    if (logicalAddress == HdmiCec.ADDR_UNREGISTERED) {
                        Slog.e(TAG, "Failed to allocate address:[device_type:" + deviceType + "]");
                    } else {
                        HdmiCecDeviceInfo deviceInfo = createDeviceInfo(logicalAddress, deviceType);
                        localDevice.setDeviceInfo(deviceInfo);
                        mCecController.addLocalDevice(deviceType, localDevice);
                        mCecController.addLogicalAddress(logicalAddress);
                        devices.append(logicalAddress, localDevice);
                    }
                    finished.append(deviceType, logicalAddress);

                    // Once finish address allocation for all devices, notify
                    // it to each device.
                    if (deviceTypes.length == finished.size()) {
                        notifyAddressAllocated(devices);
                    }
                }
            });
        }
    }

    private void notifyAddressAllocated(SparseArray<HdmiCecLocalDevice> devices) {
        for (int i = 0; i < devices.size(); ++i) {
            int address = devices.keyAt(i);
            HdmiCecLocalDevice device = devices.valueAt(i);
            device.onAddressAllocated(address);
        }
    }

    /**
     * Returns {@link Looper} for IO operation.
     *
     * <p>Declared as package-private.
     */
    Looper getIoLooper() {
        return mIoThread.getLooper();
    }

    /**
     * Returns {@link Looper} of main thread. Use this {@link Looper} instance
     * for tasks that are running on main service thread.
     *
     * <p>Declared as package-private.
     */
    Looper getServiceLooper() {
        return mHandler.getLooper();
    }

    /**
     * Returns physical address of the device.
     */
    int getPhysicalAddress() {
        return mCecController.getPhysicalAddress();
    }

    /**
     * Returns vendor id of CEC service.
     */
    int getVendorId() {
        return mCecController.getVendorId();
    }

    /**
     * Returns a list of {@link HdmiCecDeviceInfo}.
     *
     * @param includeLocalDevice whether to include local devices
     */
    List<HdmiCecDeviceInfo> getDeviceInfoList(boolean includeLocalDevice) {
        assertRunOnServiceThread();
        return mCecController.getDeviceInfoList(includeLocalDevice);
    }

    /**
     * Add and start a new {@link FeatureAction} to the action queue.
     *
     * @param action {@link FeatureAction} to add and start
     */
    void addAndStartAction(final FeatureAction action) {
        // TODO: may need to check the number of stale actions.
        runOnServiceThread(new Runnable() {
            @Override
            public void run() {
                mActions.add(action);
                action.start();
            }
        });
    }

    void setSystemAudioMode(boolean on) {
        synchronized (mLock) {
            mSystemAudioMode = on;
        }
    }

    boolean getSystemAudioMode() {
        synchronized (mLock) {
            return mSystemAudioMode;
        }
    }

    // See if we have an action of a given type in progress.
    private <T extends FeatureAction> boolean hasAction(final Class<T> clazz) {
        for (FeatureAction action : mActions) {
            if (action.getClass().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the given {@link FeatureAction} object from the action queue.
     *
     * @param action {@link FeatureAction} to remove
     */
    void removeAction(final FeatureAction action) {
        assertRunOnServiceThread();
        mActions.remove(action);
    }

    // Remove all actions matched with the given Class type.
    private <T extends FeatureAction> void removeAction(final Class<T> clazz) {
        removeActionExcept(clazz, null);
    }

    // Remove all actions matched with the given Class type besides |exception|.
    <T extends FeatureAction> void removeActionExcept(final Class<T> clazz,
            final FeatureAction exception) {
        assertRunOnServiceThread();
        Iterator<FeatureAction> iter = mActions.iterator();
        while (iter.hasNext()) {
            FeatureAction action = iter.next();
            if (action != exception && action.getClass().equals(clazz)) {
                action.clear();
                mActions.remove(action);
            }
        }
    }

    private void runOnServiceThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    void runOnServiceThreadAtFrontOfQueue(Runnable runnable) {
        mHandler.postAtFrontOfQueue(runnable);
    }

    private void assertRunOnServiceThread() {
        if (Looper.myLooper() != mHandler.getLooper()) {
            throw new IllegalStateException("Should run on service thread.");
        }
    }

    /**
     * Change ARC status into the given {@code enabled} status.
     *
     * @return {@code true} if ARC was in "Enabled" status
     */
    boolean setArcStatus(boolean enabled) {
        synchronized (mLock) {
            boolean oldStatus = mArcStatusEnabled;
            // 1. Enable/disable ARC circuit.
            // TODO: call set_audio_return_channel of hal interface.

            // 2. Update arc status;
            mArcStatusEnabled = enabled;
            return oldStatus;
        }
    }

    /**
     * Transmit a CEC command to CEC bus.
     *
     * @param command CEC command to send out
     * @param callback interface used to the result of send command
     */
    void sendCecCommand(HdmiCecMessage command, @Nullable SendMessageCallback callback) {
        mCecController.sendCommand(command, callback);
    }

    void sendCecCommand(HdmiCecMessage command) {
        mCecController.sendCommand(command, null);
    }

    boolean handleCecCommand(HdmiCecMessage message) {
        // Cache incoming message. Note that it caches only white-listed one.
        mCecMessageCache.cacheMessage(message);

        // Commands that queries system information replies directly instead
        // of creating FeatureAction because they are state-less.
        switch (message.getOpcode()) {
            case HdmiCec.MESSAGE_GET_MENU_LANGUAGE:
                handleGetMenuLanguage(message);
                return true;
            case HdmiCec.MESSAGE_GIVE_OSD_NAME:
                handleGiveOsdName(message);
                return true;
            case HdmiCec.MESSAGE_GIVE_PHYSICAL_ADDRESS:
                handleGivePhysicalAddress(message);
                return true;
            case HdmiCec.MESSAGE_GIVE_DEVICE_VENDOR_ID:
                handleGiveDeviceVendorId(message);
                return true;
            case HdmiCec.MESSAGE_GET_CEC_VERSION:
                handleGetCecVersion(message);
                return true;
            case HdmiCec.MESSAGE_INITIATE_ARC:
                handleInitiateArc(message);
                return true;
            case HdmiCec.MESSAGE_TERMINATE_ARC:
                handleTerminateArc(message);
                return true;
            case HdmiCec.MESSAGE_REPORT_PHYSICAL_ADDRESS:
                handleReportPhysicalAddress(message);
                return true;
            case HdmiCec.MESSAGE_SET_SYSTEM_AUDIO_MODE:
                handleSetSystemAudioMode(message);
                return true;
            case HdmiCec.MESSAGE_SYSTEM_AUDIO_MODE_STATUS:
                handleSystemAudioModeStatus(message);
                return true;
            default:
                return dispatchMessageToAction(message);
        }
    }

    /**
     * Called when a new hotplug event is issued.
     *
     * @param portNo hdmi port number where hot plug event issued.
     * @param connected whether to be plugged in or not
     */
    void onHotplug(int portNo, boolean connected) {
        // TODO: Start "RequestArcInitiationAction" if ARC port.
    }

    /**
     * Poll all remote devices. It sends &lt;Polling Message&gt; to all remote
     * devices.
     *
     * @param callback an interface used to get a list of all remote devices' address
     * @param pickStrategy strategy how to pick polling candidates
     * @param retryCount the number of retry used to send polling message to remote devices
     * @throw IllegalArgumentException if {@code pickStrategy} is invalid value
     */
    void pollDevices(DevicePollingCallback callback, int pickStrategy, int retryCount) {
        mCecController.pollDevices(callback, checkPollStrategy(pickStrategy), retryCount);
    }

    private int checkPollStrategy(int pickStrategy) {
        int strategy = pickStrategy & POLL_STRATEGY_MASK;
        if (strategy == 0) {
            throw new IllegalArgumentException("Invalid poll strategy:" + pickStrategy);
        }
        int iterationStrategy = pickStrategy & POLL_ITERATION_STRATEGY_MASK;
        if (iterationStrategy == 0) {
            throw new IllegalArgumentException("Invalid iteration strategy:" + pickStrategy);
        }
        return strategy | iterationStrategy;
    }

    /**
     * Launch device discovery sequence. It starts with clearing the existing device info list.
     * Note that it assumes that logical address of all local devices is already allocated.
     *
     * @param sourceAddress a logical address of tv
     */
    void launchDeviceDiscovery(final int sourceAddress) {
        // At first, clear all existing device infos.
        mCecController.clearDeviceInfoList();
        mCecMessageCache.flushAll();

        // TODO: check whether TV is one of local devices.
        DeviceDiscoveryAction action = new DeviceDiscoveryAction(this, sourceAddress,
                new DeviceDiscoveryCallback() {
                    @Override
                    public void onDeviceDiscoveryDone(List<HdmiCecDeviceInfo> deviceInfos) {
                        for (HdmiCecDeviceInfo info : deviceInfos) {
                            addCecDevice(info);
                        }

                        // Add device info of all local devices.
                        for (HdmiCecLocalDevice device : mCecController.getLocalDeviceList()) {
                            addCecDevice(device.getDeviceInfo());
                        }

                        addAndStartAction(new HotplugDetectionAction(HdmiControlService.this,
                                sourceAddress));
                    }
                });
        addAndStartAction(action);
    }

    private HdmiCecDeviceInfo createDeviceInfo(int logicalAddress, int deviceType) {
        // TODO: get device name read from system configuration.
        String displayName = HdmiCec.getDefaultDeviceName(logicalAddress);
        return new HdmiCecDeviceInfo(logicalAddress,
                getPhysicalAddress(), deviceType, getVendorId(), displayName);
    }

    private void handleReportPhysicalAddress(HdmiCecMessage message) {
        // At first, try to consume it.
        if (dispatchMessageToAction(message)) {
            return;
        }

        // Ignore if [Device Discovery Action] is going on.
        if (hasAction(DeviceDiscoveryAction.class)) {
            Slog.i(TAG, "Ignore unrecognizable <Report Physical Address> "
                    + "because Device Discovery Action is on-going:" + message);
            return;
        }

        // TODO: start new device action.
    }

    private void handleInitiateArc(HdmiCecMessage message){
        // In case where <Initiate Arc> is started by <Request ARC Initiation>
        // need to clean up RequestArcInitiationAction.
        removeAction(RequestArcInitiationAction.class);
        SetArcTransmissionStateAction action = new SetArcTransmissionStateAction(this,
                message.getDestination(), message.getSource(), true);
        addAndStartAction(action);
    }

    private void handleTerminateArc(HdmiCecMessage message) {
        // In case where <Terminate Arc> is started by <Request ARC Termination>
        // need to clean up RequestArcInitiationAction.
        // TODO: check conditions of power status by calling is_connected api
        // to be added soon.
        removeAction(RequestArcTerminationAction.class);
        SetArcTransmissionStateAction action = new SetArcTransmissionStateAction(this,
                message.getDestination(), message.getSource(), false);
        addAndStartAction(action);
    }

    private void handleGetCecVersion(HdmiCecMessage message) {
        int version = mCecController.getVersion();
        HdmiCecMessage cecMessage = HdmiCecMessageBuilder.buildCecVersion(message.getDestination(),
                message.getSource(),
                version);
        sendCecCommand(cecMessage);
    }

    private void handleGiveDeviceVendorId(HdmiCecMessage message) {
        int vendorId = mCecController.getVendorId();
        HdmiCecMessage cecMessage = HdmiCecMessageBuilder.buildDeviceVendorIdCommand(
                message.getDestination(), vendorId);
        sendCecCommand(cecMessage);
    }

    private void handleGivePhysicalAddress(HdmiCecMessage message) {
        int physicalAddress = mCecController.getPhysicalAddress();
        int deviceType = HdmiCec.getTypeFromAddress(message.getDestination());
        HdmiCecMessage cecMessage = HdmiCecMessageBuilder.buildReportPhysicalAddressCommand(
                message.getDestination(), physicalAddress, deviceType);
        sendCecCommand(cecMessage);
    }

    private void handleGiveOsdName(HdmiCecMessage message) {
        // TODO: read device name from settings or property.
        String name = HdmiCec.getDefaultDeviceName(message.getDestination());
        HdmiCecMessage cecMessage = HdmiCecMessageBuilder.buildSetOsdNameCommand(
                message.getDestination(), message.getSource(), name);
        if (cecMessage != null) {
            sendCecCommand(cecMessage);
        } else {
            Slog.w(TAG, "Failed to build <Get Osd Name>:" + name);
        }
    }

    private void handleGetMenuLanguage(HdmiCecMessage message) {
        // Only 0 (TV), 14 (specific use) can answer.
        if (message.getDestination() != HdmiCec.ADDR_TV
                && message.getDestination() != HdmiCec.ADDR_SPECIFIC_USE) {
            Slog.w(TAG, "Only TV can handle <Get Menu Language>:" + message.toString());
            sendCecCommand(
                    HdmiCecMessageBuilder.buildFeatureAbortCommand(message.getDestination(),
                            message.getSource(), HdmiCec.MESSAGE_GET_MENU_LANGUAGE,
                            HdmiConstants.ABORT_UNRECOGNIZED_MODE));
            return;
        }

        HdmiCecMessage command = HdmiCecMessageBuilder.buildSetMenuLanguageCommand(
                message.getDestination(),
                Locale.getDefault().getISO3Language());
        // TODO: figure out how to handle failed to get language code.
        if (command != null) {
            sendCecCommand(command);
        } else {
            Slog.w(TAG, "Failed to respond to <Get Menu Language>: " + message.toString());
        }
    }

    private boolean dispatchMessageToAction(HdmiCecMessage message) {
        for (FeatureAction action : mActions) {
            if (action.processCommand(message)) {
                return true;
            }
        }
        Slog.w(TAG, "Unsupported cec command:" + message);
        return false;
    }

    private void handleSetSystemAudioMode(HdmiCecMessage message) {
        if (dispatchMessageToAction(message) || !isMessageForSystemAudio(message)) {
            return;
        }
        SystemAudioActionFromAvr action = new SystemAudioActionFromAvr(this,
                message.getDestination(), message.getSource(),
                HdmiUtils.parseCommandParamSystemAudioStatus(message));
        addAndStartAction(action);
    }

    private void handleSystemAudioModeStatus(HdmiCecMessage message) {
        if (!isMessageForSystemAudio(message)) {
            return;
        }
        setSystemAudioMode(HdmiUtils.parseCommandParamSystemAudioStatus(message));
    }

    private boolean isMessageForSystemAudio(HdmiCecMessage message) {
        if (message.getSource() != HdmiCec.ADDR_AUDIO_SYSTEM
                || message.getDestination() != HdmiCec.ADDR_TV
                || getAvrDeviceInfo() == null) {
            Slog.w(TAG, "Skip abnormal CecMessage: " + message);
            return false;
        }
        return true;
    }

    // Record class that monitors the event of the caller of being killed. Used to clean up
    // the listener list and record list accordingly.
    private final class HotplugEventListenerRecord implements IBinder.DeathRecipient {
        private final IHdmiHotplugEventListener mListener;

        public HotplugEventListenerRecord(IHdmiHotplugEventListener listener) {
            mListener = listener;
        }

        @Override
        public void binderDied() {
            synchronized (mLock) {
                mHotplugEventListenerRecords.remove(this);
                mHotplugEventListeners.remove(mListener);
            }
        }
    }

    void addCecDevice(HdmiCecDeviceInfo info) {
        mCecController.addDeviceInfo(info);
    }

    private void enforceAccessPermission() {
        getContext().enforceCallingOrSelfPermission(PERMISSION, TAG);
    }

    private final class BinderService extends IHdmiControlService.Stub {
        @Override
        public int[] getSupportedTypes() {
            enforceAccessPermission();
            synchronized (mLock) {
                return mLocalDevices;
            }
        }

        @Override
        public void oneTouchPlay(final IHdmiControlCallback callback) {
            enforceAccessPermission();
            runOnServiceThread(new Runnable() {
                @Override
                public void run() {
                    HdmiControlService.this.oneTouchPlay(callback);
                }
            });
        }

        @Override
        public void queryDisplayStatus(final IHdmiControlCallback callback) {
            enforceAccessPermission();
            runOnServiceThread(new Runnable() {
                @Override
                public void run() {
                    HdmiControlService.this.queryDisplayStatus(callback);
                }
            });
        }

        @Override
        public void addHotplugEventListener(final IHdmiHotplugEventListener listener) {
            enforceAccessPermission();
            runOnServiceThread(new Runnable() {
                @Override
                public void run() {
                    HdmiControlService.this.addHotplugEventListener(listener);
                }
            });
        }

        @Override
        public void removeHotplugEventListener(final IHdmiHotplugEventListener listener) {
            enforceAccessPermission();
            runOnServiceThread(new Runnable() {
                @Override
                public void run() {
                    HdmiControlService.this.removeHotplugEventListener(listener);
                }
            });
        }
    }

    private void oneTouchPlay(IHdmiControlCallback callback) {
        if (hasAction(OneTouchPlayAction.class)) {
            Slog.w(TAG, "oneTouchPlay already in progress");
            invokeCallback(callback, HdmiCec.RESULT_ALREADY_IN_PROGRESS);
            return;
        }
        HdmiCecLocalDevice source = mCecController.getLocalDevice(HdmiCec.DEVICE_PLAYBACK);
        if (source == null) {
            Slog.w(TAG, "Local playback device not available");
            invokeCallback(callback, HdmiCec.RESULT_SOURCE_NOT_AVAILABLE);
            return;
        }
        // TODO: Consider the case of multiple TV sets. For now we always direct the command
        //       to the primary one.
        OneTouchPlayAction action = OneTouchPlayAction.create(this,
                source.getDeviceInfo().getLogicalAddress(),
                source.getDeviceInfo().getPhysicalAddress(), HdmiCec.ADDR_TV, callback);
        if (action == null) {
            Slog.w(TAG, "Cannot initiate oneTouchPlay");
            invokeCallback(callback, HdmiCec.RESULT_EXCEPTION);
            return;
        }
        addAndStartAction(action);
    }

    private void queryDisplayStatus(IHdmiControlCallback callback) {
        if (hasAction(DevicePowerStatusAction.class)) {
            Slog.w(TAG, "queryDisplayStatus already in progress");
            invokeCallback(callback, HdmiCec.RESULT_ALREADY_IN_PROGRESS);
            return;
        }
        HdmiCecLocalDevice source = mCecController.getLocalDevice(HdmiCec.DEVICE_PLAYBACK);
        if (source == null) {
            Slog.w(TAG, "Local playback device not available");
            invokeCallback(callback, HdmiCec.RESULT_SOURCE_NOT_AVAILABLE);
            return;
        }
        DevicePowerStatusAction action = DevicePowerStatusAction.create(this,
                source.getDeviceInfo().getLogicalAddress(), HdmiCec.ADDR_TV, callback);
        if (action == null) {
            Slog.w(TAG, "Cannot initiate queryDisplayStatus");
            invokeCallback(callback, HdmiCec.RESULT_EXCEPTION);
            return;
        }
        addAndStartAction(action);
    }

    private void addHotplugEventListener(IHdmiHotplugEventListener listener) {
        HotplugEventListenerRecord record = new HotplugEventListenerRecord(listener);
        try {
            listener.asBinder().linkToDeath(record, 0);
        } catch (RemoteException e) {
            Slog.w(TAG, "Listener already died");
            return;
        }
        synchronized (mLock) {
            mHotplugEventListenerRecords.add(record);
            mHotplugEventListeners.add(listener);
        }
    }

    private void removeHotplugEventListener(IHdmiHotplugEventListener listener) {
        synchronized (mLock) {
            for (HotplugEventListenerRecord record : mHotplugEventListenerRecords) {
                if (record.mListener.asBinder() == listener.asBinder()) {
                    listener.asBinder().unlinkToDeath(record, 0);
                    mHotplugEventListenerRecords.remove(record);
                    break;
                }
            }
            mHotplugEventListeners.remove(listener);
        }
    }

    private void invokeCallback(IHdmiControlCallback callback, int result) {
        try {
            callback.onComplete(result);
        } catch (RemoteException e) {
            Slog.e(TAG, "Invoking callback failed:" + e);
        }
    }

    HdmiCecDeviceInfo getAvrDeviceInfo() {
        return mCecController.getDeviceInfo(HdmiCec.ADDR_AUDIO_SYSTEM);
    }

    void setAudioStatus(boolean mute, int volume) {
        // TODO: Hook up with AudioManager.
    }

    boolean isInPresetInstallationMode() {
        // TODO: Implement this.
        return false;
    }

    /**
     * Called when a device is removed or removal of device is detected.
     *
     * @param address a logical address of a device to be removed
     */
    void removeCecDevice(int address) {
        mCecController.removeDeviceInfo(address);
        mCecMessageCache.flushMessagesFrom(address);
    }

    HdmiCecMessageCache getCecMessageCache() {
        return mCecMessageCache;
    }
}