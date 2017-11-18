/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.server;

import android.bluetooth.BluetoothTetheringDataTracker;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DummyDataStateTracker;
import android.net.EthernetDataTracker;
import android.net.IConnectivityManager;
import android.net.LinkProperties;
import android.net.MobileDataStateTracker;
import android.net.NetworkConfig;
import android.net.NetworkInfo;
import android.net.NetworkStateTracker;
import android.net.NetworkUtils;
import android.net.Proxy;
import android.net.ProxyProperties;
import android.net.RouteInfo;
import android.net.vpn.VpnManager;
import android.net.wifi.WifiStateTracker;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Slog;

import com.android.internal.telephony.Phone;
import com.android.server.connectivity.Tethering;

import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @hide
 */
public class ConnectivityService extends IConnectivityManager.Stub {

    private static final boolean DBG = true;
    private static final String TAG = "ConnectivityService";

    // how long to wait before switching back to a radio's default network
    private static final int RESTORE_DEFAULT_NETWORK_DELAY = 1 * 60 * 1000;
    // system property that can override the above value
    private static final String NETWORK_RESTORE_DELAY_PROP_NAME =
            "android.telephony.apn-restore";

    // used in recursive route setting to add gateways for the host for which
    // a host route was requested.
    private static final int MAX_HOSTROUTE_CYCLE_COUNT = 10;

    private Tethering mTethering;
    private boolean mTetheringConfigValid = false;

    /**
     * Sometimes we want to refer to the individual network state
     * trackers separately, and sometimes we just want to treat them
     * abstractly.
     */
    private NetworkStateTracker mNetTrackers[];

    /**
     * A per Net list of the PID's that requested access to the net
     * used both as a refcount and for per-PID DNS selection
     */
    private List mNetRequestersPids[];

    private WifiWatchdogService mWifiWatchdogService;

    // priority order of the nettrackers
    // (excluding dynamically set mNetworkPreference)
    // TODO - move mNetworkTypePreference into this
    private int[] mPriorityList;

    private Context mContext;
    private int mNetworkPreference;
    private int mActiveDefaultNetwork = -1;
    // 0 is full bad, 100 is full good
    private int mDefaultInetCondition = 0;
    private int mDefaultInetConditionPublished = 0;
    private boolean mInetConditionChangeInFlight = false;
    private int mDefaultConnectionSequence = 0;

    private int mNumDnsEntries;

    private boolean mTestMode;
    private static ConnectivityService sServiceInstance;

    private AtomicBoolean mBackgroundDataEnabled = new AtomicBoolean(true);

    private static final int ENABLED  = 1;
    private static final int DISABLED = 0;

    // Share the event space with NetworkStateTracker (which can't see this
    // internal class but sends us events).  If you change these, change
    // NetworkStateTracker.java too.
    private static final int MIN_NETWORK_STATE_TRACKER_EVENT = 1;
    private static final int MAX_NETWORK_STATE_TRACKER_EVENT = 100;

    /**
     * used internally as a delayed event to make us switch back to the
     * default network
     */
    private static final int EVENT_RESTORE_DEFAULT_NETWORK =
            MAX_NETWORK_STATE_TRACKER_EVENT + 1;

    /**
     * used internally to change our mobile data enabled flag
     */
    private static final int EVENT_CHANGE_MOBILE_DATA_ENABLED =
            MAX_NETWORK_STATE_TRACKER_EVENT + 2;

    /**
     * used internally to change our network preference setting
     * arg1 = networkType to prefer
     */
    private static final int EVENT_SET_NETWORK_PREFERENCE =
            MAX_NETWORK_STATE_TRACKER_EVENT + 3;

    /**
     * used internally to synchronize inet condition reports
     * arg1 = networkType
     * arg2 = condition (0 bad, 100 good)
     */
    private static final int EVENT_INET_CONDITION_CHANGE =
            MAX_NETWORK_STATE_TRACKER_EVENT + 4;

    /**
     * used internally to mark the end of inet condition hold periods
     * arg1 = networkType
     */
    private static final int EVENT_INET_CONDITION_HOLD_END =
            MAX_NETWORK_STATE_TRACKER_EVENT + 5;

    /**
     * used internally to set the background data preference
     * arg1 = TRUE for enabled, FALSE for disabled
     */
    private static final int EVENT_SET_BACKGROUND_DATA =
            MAX_NETWORK_STATE_TRACKER_EVENT + 6;

    /**
     * used internally to set enable/disable cellular data
     * arg1 = ENBALED or DISABLED
     */
    private static final int EVENT_SET_MOBILE_DATA =
            MAX_NETWORK_STATE_TRACKER_EVENT + 7;

    /**
     * used internally to clear a wakelock when transitioning
     * from one net to another
     */
    private static final int EVENT_CLEAR_NET_TRANSITION_WAKELOCK =
            MAX_NETWORK_STATE_TRACKER_EVENT + 8;

    /**
     * used internally to reload global proxy settings
     */
    private static final int EVENT_APPLY_GLOBAL_HTTP_PROXY =
            MAX_NETWORK_STATE_TRACKER_EVENT + 9;

    /**
     * used internally to set external dependency met/unmet
     * arg1 = ENABLED (met) or DISABLED (unmet)
     * arg2 = NetworkType
     */
    private static final int EVENT_SET_DEPENDENCY_MET =
            MAX_NETWORK_STATE_TRACKER_EVENT + 10;

    private Handler mHandler;

    // list of DeathRecipients used to make sure features are turned off when
    // a process dies
    private List mFeatureUsers;

    private boolean mSystemReady;
    private Intent mInitialBroadcast;

    private PowerManager.WakeLock mNetTransitionWakeLock;
    private String mNetTransitionWakeLockCausedBy = "";
    private int mNetTransitionWakeLockSerialNumber;
    private int mNetTransitionWakeLockTimeout;

    private InetAddress mDefaultDns;

    // used in DBG mode to track inet condition reports
    private static final int INET_CONDITION_LOG_MAX_SIZE = 15;
    private ArrayList mInetLog;

    // track the current default http proxy - tell the world if we get a new one (real change)
    private ProxyProperties mDefaultProxy = null;
    // track the global proxy.
    private ProxyProperties mGlobalProxy = null;
    private final Object mGlobalProxyLock = new Object();

    private SettingsObserver mSettingsObserver;

    NetworkConfig[] mNetConfigs;
    int mNetworksDefined;

    private static class RadioAttributes {
        public int mSimultaneity;
        public int mType;
        public RadioAttributes(String init) {
            String fragments[] = init.split(",");
            mType = Integer.parseInt(fragments[0]);
            mSimultaneity = Integer.parseInt(fragments[1]);
        }
    }
    RadioAttributes[] mRadioAttributes;

    public static synchronized ConnectivityService getInstance(Context context) {
        if (sServiceInstance == null) {
            sServiceInstance = new ConnectivityService(context);
        }
        return sServiceInstance;
    }

    private ConnectivityService(Context context) {
        if (DBG) log("ConnectivityService starting up");

        HandlerThread handlerThread = new HandlerThread("ConnectivityServiceThread");
        handlerThread.start();
        mHandler = new MyHandler(handlerThread.getLooper());

        mBackgroundDataEnabled.set(Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BACKGROUND_DATA, 1) == 1);

        // setup our unique device name
        if (TextUtils.isEmpty(SystemProperties.get("net.hostname"))) {
            String id = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (id != null && id.length() > 0) {
                String name = new String("android_").concat(id);
                SystemProperties.set("net.hostname", name);
            }
        }

        // read our default dns server ip
        String dns = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.DEFAULT_DNS_SERVER);
        if (dns == null || dns.length() == 0) {
            dns = context.getResources().getString(
                    com.android.internal.R.string.config_default_dns_server);
        }
        try {
            mDefaultDns = NetworkUtils.numericToInetAddress(dns);
        } catch (IllegalArgumentException e) {
            loge("Error setting defaultDns using " + dns);
        }

        mContext = context;

        PowerManager powerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mNetTransitionWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mNetTransitionWakeLockTimeout = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_networkTransitionTimeout);

        mNetTrackers = new NetworkStateTracker[
                ConnectivityManager.MAX_NETWORK_TYPE+1];

        mNetworkPreference = getPersistedNetworkPreference();

        mRadioAttributes = new RadioAttributes[ConnectivityManager.MAX_RADIO_TYPE+1];
        mNetConfigs = new NetworkConfig[ConnectivityManager.MAX_NETWORK_TYPE+1];

        // Load device network attributes from resources
        String[] raStrings = context.getResources().getStringArray(
                com.android.internal.R.array.radioAttributes);
        for (String raString : raStrings) {
            RadioAttributes r = new RadioAttributes(raString);
            if (r.mType > ConnectivityManager.MAX_RADIO_TYPE) {
                loge("Error in radioAttributes - ignoring attempt to define type " + r.mType);
                continue;
            }
            if (mRadioAttributes[r.mType] != null) {
                loge("Error in radioAttributes - ignoring attempt to redefine type " +
                        r.mType);
                continue;
            }
            mRadioAttributes[r.mType] = r;
        }

        String[] naStrings = context.getResources().getStringArray(
                com.android.internal.R.array.networkAttributes);
        for (String naString : naStrings) {
            try {
                NetworkConfig n = new NetworkConfig(naString);
                if (n.type > ConnectivityManager.MAX_NETWORK_TYPE) {
                    loge("Error in networkAttributes - ignoring attempt to define type " +
                            n.type);
                    continue;
                }
                if (mNetConfigs[n.type] != null) {
                    loge("Error in networkAttributes - ignoring attempt to redefine type " +
                            n.type);
                    continue;
                }
                if (mRadioAttributes[n.radio] == null) {
                    loge("Error in networkAttributes - ignoring attempt to use undefined " +
                            "radio " + n.radio + " in network type " + n.type);
                    continue;
                }
                mNetConfigs[n.type] = n;
                mNetworksDefined++;
            } catch(Exception e) {
                // ignore it - leave the entry null
            }
        }

        // high priority first
        mPriorityList = new int[mNetworksDefined];
        {
            int insertionPoint = mNetworksDefined-1;
            int currentLowest = 0;
            int nextLowest = 0;
            while (insertionPoint > -1) {
                for (NetworkConfig na : mNetConfigs) {
                    if (na == null) continue;
                    if (na.priority < currentLowest) continue;
                    if (na.priority > currentLowest) {
                        if (na.priority < nextLowest || nextLowest == 0) {
                            nextLowest = na.priority;
                        }
                        continue;
                    }
                    mPriorityList[insertionPoint--] = na.type;
                }
                currentLowest = nextLowest;
                nextLowest = 0;
            }
        }

        mNetRequestersPids = new ArrayList[ConnectivityManager.MAX_NETWORK_TYPE+1];
        for (int i : mPriorityList) {
            mNetRequestersPids[i] = new ArrayList();
        }

        mFeatureUsers = new ArrayList();

        mNumDnsEntries = 0;

        mTestMode = SystemProperties.get("cm.test.mode").equals("true")
                && SystemProperties.get("ro.build.type").equals("eng");
        /*
         * Create the network state trackers for Wi-Fi and mobile
         * data. Maybe this could be done with a factory class,
         * but it's not clear that it's worth it, given that
         * the number of different network types is not going
         * to change very often.
         */
        for (int netType : mPriorityList) {
            switch (mNetConfigs[netType].radio) {
            case ConnectivityManager.TYPE_WIFI:
                if (DBG) log("Starting Wifi Service.");
                WifiStateTracker wst = new WifiStateTracker();
                WifiService wifiService = new WifiService(context);
                ServiceManager.addService(Context.WIFI_SERVICE, wifiService);
                wifiService.checkAndStartWifi();
                mNetTrackers[ConnectivityManager.TYPE_WIFI] = wst;
                wst.startMonitoring(context, mHandler);

                //TODO: as part of WWS refactor, create only when needed
                mWifiWatchdogService = new WifiWatchdogService(context);

                break;
            case ConnectivityManager.TYPE_MOBILE:
                mNetTrackers[netType] = new MobileDataStateTracker(netType,
                        mNetConfigs[netType].name);
                mNetTrackers[netType].startMonitoring(context, mHandler);
                break;
            case ConnectivityManager.TYPE_DUMMY:
                mNetTrackers[netType] = new DummyDataStateTracker(netType,
                        mNetConfigs[netType].name);
                mNetTrackers[netType].startMonitoring(context, mHandler);
                break;
            case ConnectivityManager.TYPE_BLUETOOTH:
                mNetTrackers[netType] = BluetoothTetheringDataTracker.getInstance();
                mNetTrackers[netType].startMonitoring(context, mHandler);
                break;
            case ConnectivityManager.TYPE_ETHERNET:
                mNetTrackers[netType] = EthernetDataTracker.getInstance();
                mNetTrackers[netType].startMonitoring(context, mHandler);
                break;
            default:
                loge("Trying to create a DataStateTracker for an unknown radio type " +
                        mNetConfigs[netType].radio);
                continue;
            }
        }

        IBinder b = ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE);
        INetworkManagementService nmService = INetworkManagementService.Stub.asInterface(b);

        mTethering = new Tethering(mContext, nmService, mHandler.getLooper());
        mTetheringConfigValid = (((mNetTrackers[ConnectivityManager.TYPE_MOBILE_DUN] != null) ||
                                  !mTethering.isDunRequired()) &&
                                 (mTethering.getTetherableUsbRegexs().length != 0 ||
                                  mTethering.getTetherableWifiRegexs().length != 0 ||
                                  mTethering.getTetherableBluetoothRegexs().length != 0) &&
                                 mTethering.getUpstreamIfaceRegexs().length != 0);

        if (DBG) {
            mInetLog = new ArrayList();
        }

        mSettingsObserver = new SettingsObserver(mHandler, EVENT_APPLY_GLOBAL_HTTP_PROXY);
        mSettingsObserver.observe(mContext);

        loadGlobalProxy();

        VpnManager.startVpnService(context);
    }


    /**
     * Sets the preferred network.
     * @param preference the new preference
     */
    public void setNetworkPreference(int preference) {
        enforceChangePermission();

        mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_NETWORK_PREFERENCE, preference, 0));
    }

    public int getNetworkPreference() {
        enforceAccessPermission();
        int preference;
        synchronized(this) {
            preference = mNetworkPreference;
        }
        return preference;
    }

    private void handleSetNetworkPreference(int preference) {
        if (ConnectivityManager.isNetworkTypeValid(preference) &&
                mNetConfigs[preference] != null &&
                mNetConfigs[preference].isDefault()) {
            if (mNetworkPreference != preference) {
                final ContentResolver cr = mContext.getContentResolver();
                Settings.Secure.putInt(cr, Settings.Secure.NETWORK_PREFERENCE, preference);
                synchronized(this) {
                    mNetworkPreference = preference;
                }
                enforcePreference();
            }
        }
    }

    private int getPersistedNetworkPreference() {
        final ContentResolver cr = mContext.getContentResolver();

        final int networkPrefSetting = Settings.Secure
                .getInt(cr, Settings.Secure.NETWORK_PREFERENCE, -1);
        if (networkPrefSetting != -1) {
            return networkPrefSetting;
        }

        return ConnectivityManager.DEFAULT_NETWORK_PREFERENCE;
    }

    /**
     * Make the state of network connectivity conform to the preference settings
     * In this method, we only tear down a non-preferred network. Establishing
     * a connection to the preferred network is taken care of when we handle
     * the disconnect event from the non-preferred network
     * (see {@link #handleDisconnect(NetworkInfo)}).
     */
    private void enforcePreference() {
        if (mNetTrackers[mNetworkPreference].getNetworkInfo().isConnected())
            return;

        if (!mNetTrackers[mNetworkPreference].isAvailable())
            return;

        for (int t=0; t <= ConnectivityManager.MAX_RADIO_TYPE; t++) {
            if (t != mNetworkPreference && mNetTrackers[t] != null &&
                    mNetTrackers[t].getNetworkInfo().isConnected()) {
                if (DBG) {
                    log("tearing down " + mNetTrackers[t].getNetworkInfo() +
                            " in enforcePreference");
                }
                teardown(mNetTrackers[t]);
            }
        }
    }

    private boolean teardown(NetworkStateTracker netTracker) {
        if (netTracker.teardown()) {
            netTracker.setTeardownRequested(true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return NetworkInfo for the active (i.e., connected) network interface.
     * It is assumed that at most one network is active at a time. If more
     * than one is active, it is indeterminate which will be returned.
     * @return the info for the active network, or {@code null} if none is
     * active
     */
    public NetworkInfo getActiveNetworkInfo() {
        enforceAccessPermission();
        if (mActiveDefaultNetwork != -1) {
            return mNetTrackers[mActiveDefaultNetwork].getNetworkInfo();
        }
        return null;
    }

    public NetworkInfo getNetworkInfo(int networkType) {
        enforceAccessPermission();
        if (ConnectivityManager.isNetworkTypeValid(networkType)) {
            NetworkStateTracker t = mNetTrackers[networkType];
            if (t != null)
                return t.getNetworkInfo();
        }
        return null;
    }

    public NetworkInfo[] getAllNetworkInfo() {
        enforceAccessPermission();
        NetworkInfo[] result = new NetworkInfo[mNetworksDefined];
        int i = 0;
        for (NetworkStateTracker t : mNetTrackers) {
            if(t != null) result[i++] = t.getNetworkInfo();
        }
        return result;
    }

    /**
     * Return LinkProperties for the active (i.e., connected) default
     * network interface.  It is assumed that at most one default network
     * is active at a time. If more than one is active, it is indeterminate
     * which will be returned.
     * @return the ip properties for the active network, or {@code null} if
     * none is active
     */
    public LinkProperties getActiveLinkProperties() {
        enforceAccessPermission();
        for (int type=0; type <= ConnectivityManager.MAX_NETWORK_TYPE; type++) {
            if (mNetConfigs[type] == null || !mNetConfigs[type].isDefault()) {
                continue;
            }
            NetworkStateTracker t = mNetTrackers[type];
            NetworkInfo info = t.getNetworkInfo();
            if (info.isConnected()) {
                return t.getLinkProperties();
            }
        }
        return null;
    }

    public LinkProperties getLinkProperties(int networkType) {
        enforceAccessPermission();
        if (ConnectivityManager.isNetworkTypeValid(networkType)) {
            NetworkStateTracker t = mNetTrackers[networkType];
            if (t != null) return t.getLinkProperties();
        }
        return null;
    }

    public boolean setRadios(boolean turnOn) {
        boolean result = true;
        enforceChangePermission();
        for (NetworkStateTracker t : mNetTrackers) {
            if (t != null) result = t.setRadio(turnOn) && result;
        }
        return result;
    }

    public boolean setRadio(int netType, boolean turnOn) {
        enforceChangePermission();
        if (!ConnectivityManager.isNetworkTypeValid(netType)) {
            return false;
        }
        NetworkStateTracker tracker = mNetTrackers[netType];
        return tracker != null && tracker.setRadio(turnOn);
    }

    /**
     * Used to notice when the calling process dies so we can self-expire
     *
     * Also used to know if the process has cleaned up after itself when
     * our auto-expire timer goes off.  The timer has a link to an object.
     *
     */
    private class FeatureUser implements IBinder.DeathRecipient {
        int mNetworkType;
        String mFeature;
        IBinder mBinder;
        int mPid;
        int mUid;
        long mCreateTime;

        FeatureUser(int type, String feature, IBinder binder) {
            super();
            mNetworkType = type;
            mFeature = feature;
            mBinder = binder;
            mPid = getCallingPid();
            mUid = getCallingUid();
            mCreateTime = System.currentTimeMillis();

            try {
                mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        void unlinkDeathRecipient() {
            mBinder.unlinkToDeath(this, 0);
        }

        public void binderDied() {
            log("ConnectivityService FeatureUser binderDied(" +
                    mNetworkType + ", " + mFeature + ", " + mBinder + "), created " +
                    (System.currentTimeMillis() - mCreateTime) + " mSec ago");
            stopUsingNetworkFeature(this, false);
        }

        public void expire() {
            log("ConnectivityService FeatureUser expire(" +
                    mNetworkType + ", " + mFeature + ", " + mBinder +"), created " +
                    (System.currentTimeMillis() - mCreateTime) + " mSec ago");
            stopUsingNetworkFeature(this, false);
        }

        public String toString() {
            return "FeatureUser("+mNetworkType+","+mFeature+","+mPid+","+mUid+"), created " +
                    (System.currentTimeMillis() - mCreateTime) + " mSec ago";
        }
    }

    // javadoc from interface
    public int startUsingNetworkFeature(int networkType, String feature,
            IBinder binder) {
        if (DBG) {
            log("startUsingNetworkFeature for net " + networkType + ": " + feature);
        }
        enforceChangePermission();
        if (!ConnectivityManager.isNetworkTypeValid(networkType) ||
                mNetConfigs[networkType] == null) {
            return Phone.APN_REQUEST_FAILED;
        }

        FeatureUser f = new FeatureUser(networkType, feature, binder);

        // TODO - move this into the MobileDataStateTracker
        int usedNetworkType = networkType;
        if(networkType == ConnectivityManager.TYPE_MOBILE) {
            usedNetworkType = convertFeatureToNetworkType(feature);
            if (usedNetworkType < 0) {
                Slog.e(TAG, "Can't match any netTracker!");
                usedNetworkType = networkType;
            }
        }
        NetworkStateTracker network = mNetTrackers[usedNetworkType];
        if (network != null) {
            Integer currentPid = new Integer(getCallingPid());
            if (usedNetworkType != networkType) {
                NetworkStateTracker radio = mNetTrackers[networkType];
                NetworkInfo ni = network.getNetworkInfo();

                if (ni.isAvailable() == false) {
                    if (DBG) log("special network not available");
                    if (!TextUtils.equals(feature,Phone.FEATURE_ENABLE_DUN_ALWAYS)) {
                        return Phone.APN_TYPE_NOT_AVAILABLE;
                    } else {
                        // else make the attempt anyway - probably giving REQUEST_STARTED below
                    }
                }

                synchronized(this) {
                    mFeatureUsers.add(f);
                    if (!mNetRequestersPids[usedNetworkType].contains(currentPid)) {
                        // this gets used for per-pid dns when connected
                        mNetRequestersPids[usedNetworkType].add(currentPid);
                    }
                }

                int restoreTimer = getRestoreDefaultNetworkDelay(usedNetworkType);

                if (restoreTimer >= 0) {
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(EVENT_RESTORE_DEFAULT_NETWORK, f), restoreTimer);
                }

                if ((ni.isConnectedOrConnecting() == true) &&
                        !network.isTeardownRequested()) {
                    if (ni.isConnected() == true) {
                        // add the pid-specific dns
                        handleDnsConfigurationChange(networkType);
                        if (DBG) log("special network already active");
                        return Phone.APN_ALREADY_ACTIVE;
                    }
                    if (DBG) log("special network already connecting");
                    return Phone.APN_REQUEST_STARTED;
                }

                // check if the radio in play can make another contact
                // assume if cannot for now

                if (DBG) log("reconnecting to special network");
                network.reconnect();
                return Phone.APN_REQUEST_STARTED;
            } else {
                // need to remember this unsupported request so we respond appropriately on stop
                synchronized(this) {
                    mFeatureUsers.add(f);
                    if (!mNetRequestersPids[usedNetworkType].contains(currentPid)) {
                        // this gets used for per-pid dns when connected
                        mNetRequestersPids[usedNetworkType].add(currentPid);
                    }
                }
                return -1;
            }
        }
        return Phone.APN_TYPE_NOT_AVAILABLE;
    }

    // javadoc from interface
    public int stopUsingNetworkFeature(int networkType, String feature) {
        enforceChangePermission();

        int pid = getCallingPid();
        int uid = getCallingUid();

        FeatureUser u = null;
        boolean found = false;

        synchronized(this) {
            for (int i = 0; i < mFeatureUsers.size() ; i++) {
                u = (FeatureUser)mFeatureUsers.get(i);
                if (uid == u.mUid && pid == u.mPid &&
                        networkType == u.mNetworkType &&
                        TextUtils.equals(feature, u.mFeature)) {
                    found = true;
                    break;
                }
            }
        }
        if (found && u != null) {
            // stop regardless of how many other time this proc had called start
            return stopUsingNetworkFeature(u, true);
        } else {
            // none found!
            if (DBG) log("ignoring stopUsingNetworkFeature - not a live request");
            return 1;
        }
    }

    private int stopUsingNetworkFeature(FeatureUser u, boolean ignoreDups) {
        int networkType = u.mNetworkType;
        String feature = u.mFeature;
        int pid = u.mPid;
        int uid = u.mUid;

        NetworkStateTracker tracker = null;
        boolean callTeardown = false;  // used to carry our decision outside of sync block

        if (DBG) {
            log("stopUsingNetworkFeature for net " + networkType +
                    ": " + feature);
        }

        if (!ConnectivityManager.isNetworkTypeValid(networkType)) {
            return -1;
        }

        // need to link the mFeatureUsers list with the mNetRequestersPids state in this
        // sync block
        synchronized(this) {
            // check if this process still has an outstanding start request
            if (!mFeatureUsers.contains(u)) {
                if (DBG) log("ignoring - this process has no outstanding requests");
                return 1;
            }
            u.unlinkDeathRecipient();
            mFeatureUsers.remove(mFeatureUsers.indexOf(u));
            // If we care about duplicate requests, check for that here.
            //
            // This is done to support the extension of a request - the app
            // can request we start the network feature again and renew the
            // auto-shutoff delay.  Normal "stop" calls from the app though
            // do not pay attention to duplicate requests - in effect the
            // API does not refcount and a single stop will counter multiple starts.
            if (ignoreDups == false) {
                for (int i = 0; i < mFeatureUsers.size() ; i++) {
                    FeatureUser x = (FeatureUser)mFeatureUsers.get(i);
                    if (x.mUid == u.mUid && x.mPid == u.mPid &&
                            x.mNetworkType == u.mNetworkType &&
                            TextUtils.equals(x.mFeature, u.mFeature)) {
                        if (DBG) log("ignoring stopUsingNetworkFeature as dup is found");
                        return 1;
                    }
                }
            }

            // TODO - move to MobileDataStateTracker
            int usedNetworkType = networkType;
            if (networkType == ConnectivityManager.TYPE_MOBILE) {
                usedNetworkType = convertFeatureToNetworkType(feature);
                if (usedNetworkType < 0) {
                    usedNetworkType = networkType;
                }
            }
            tracker =  mNetTrackers[usedNetworkType];
            if (tracker == null) {
                if (DBG) log("ignoring - no known tracker for net type " + usedNetworkType);
                return -1;
            }
            if (usedNetworkType != networkType) {
                Integer currentPid = new Integer(pid);
                mNetRequestersPids[usedNetworkType].remove(currentPid);
                reassessPidDns(pid, true);
                if (mNetRequestersPids[usedNetworkType].size() != 0) {
                    if (DBG) log("not tearing down special network - " +
                           "others still using it");
                    return 1;
                }
                callTeardown = true;
            } else {
                if (DBG) log("not a known feature - dropping");
            }
        }
        if (DBG) log("Doing network teardown");
        if (callTeardown) {
            tracker.teardown();
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * @deprecated use requestRouteToHostAddress instead
     *
     * Ensure that a network route exists to deliver traffic to the specified
     * host via the specified network interface.
     * @param networkType the type of the network over which traffic to the
     * specified host is to be routed
     * @param hostAddress the IP address of the host to which the route is
     * desired
     * @return {@code true} on success, {@code false} on failure
     */
    public boolean requestRouteToHost(int networkType, int hostAddress) {
        InetAddress inetAddress = NetworkUtils.intToInetAddress(hostAddress);

        if (inetAddress == null) {
            return false;
        }

        return requestRouteToHostAddress(networkType, inetAddress.getAddress());
    }

    /**
     * Ensure that a network route exists to deliver traffic to the specified
     * host via the specified network interface.
     * @param networkType the type of the network over which traffic to the
     * specified host is to be routed
     * @param hostAddress the IP address of the host to which the route is
     * desired
     * @return {@code true} on success, {@code false} on failure
     */
    public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) {
        enforceChangePermission();
        if (!ConnectivityManager.isNetworkTypeValid(networkType)) {
            return false;
        }
        NetworkStateTracker tracker = mNetTrackers[networkType];

        if (tracker == null || !tracker.getNetworkInfo().isConnected() ||
                tracker.isTeardownRequested()) {
            if (DBG) {
                log("requestRouteToHostAddress on down network " +
                           "(" + networkType + ") - dropped");
            }
            return false;
        }
        try {
            InetAddress addr = InetAddress.getByAddress(hostAddress);
            return addHostRoute(tracker, addr, 0);
        } catch (UnknownHostException e) {}
        return false;
    }

    /**
     * Ensure that a network route exists to deliver traffic to the specified
     * host via the mobile data network.
     * @param hostAddress the IP address of the host to which the route is desired,
     * in network byte order.
     * TODO - deprecate
     * @return {@code true} on success, {@code false} on failure
     */
    private boolean addHostRoute(NetworkStateTracker nt, InetAddress hostAddress, int cycleCount) {
        if (nt.getNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
            return false;
        }

        LinkProperties lp = nt.getLinkProperties();
        if ((lp == null) || (hostAddress == null)) return false;

        String interfaceName = lp.getInterfaceName();
        if (DBG) {
            log("Requested host route to " + hostAddress + "(" + interfaceName + "), cycleCount=" +
                    cycleCount);
        }
        if (interfaceName == null) {
            if (DBG) loge("addHostRoute failed due to null interface name");
            return false;
        }

        RouteInfo bestRoute = RouteInfo.selectBestRoute(lp.getRoutes(), hostAddress);
        InetAddress gateway = null;
        if (bestRoute != null) {
            gateway = bestRoute.getGateway();
            // if the best route is ourself, don't relf-reference, just add the host route
            if (hostAddress.equals(gateway)) gateway = null;
        }
        if (gateway != null) {
            if (cycleCount > MAX_HOSTROUTE_CYCLE_COUNT) {
                loge("Error adding hostroute - too much recursion");
                return false;
            }
            if (!addHostRoute(nt, gateway, cycleCount+1)) return false;
        }
        return NetworkUtils.addHostRoute(interfaceName, hostAddress, gateway);
    }

    // TODO support the removal of single host routes.  Keep a ref count of them so we
    // aren't over-zealous
    private boolean removeHostRoute(NetworkStateTracker nt, InetAddress hostAddress) {
        return false;
    }

    /**
     * @see ConnectivityManager#getBackgroundDataSetting()
     */
    public boolean getBackgroundDataSetting() {
        return mBackgroundDataEnabled.get();
    }

    /**
     * @see ConnectivityManager#setBackgroundDataSetting(boolean)
     */
    public void setBackgroundDataSetting(boolean allowBackgroundDataUsage) {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CHANGE_BACKGROUND_DATA_SETTING,
                "ConnectivityService");

        mBackgroundDataEnabled.set(allowBackgroundDataUsage);

        mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_BACKGROUND_DATA,
                (allowBackgroundDataUsage ? ENABLED : DISABLED), 0));
    }

    private void handleSetBackgroundData(boolean enabled) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.BACKGROUND_DATA, enabled ? 1 : 0);
        Intent broadcast = new Intent(
                ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
        mContext.sendBroadcast(broadcast);
    }

    /**
     * @see ConnectivityManager#getMobileDataEnabled()
     */
    public boolean getMobileDataEnabled() {
        // TODO: This detail should probably be in DataConnectionTracker's
        //       which is where we store the value and maybe make this
        //       asynchronous.
        enforceAccessPermission();
        boolean retVal = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.MOBILE_DATA, 1) == 1;
        if (DBG) log("getMobileDataEnabled returning " + retVal);
        return retVal;
    }

    public void setDataDependency(int networkType, boolean met) {
        enforceChangePermission();
        if (DBG) {
            log("setDataDependency(" + networkType + ", " + met + ")");
        }
        mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_DEPENDENCY_MET,
                (met ? ENABLED : DISABLED), networkType));
    }

    private void handleSetDependencyMet(int networkType, boolean met) {
        if (mNetTrackers[networkType] != null) {
            if (DBG) {
                log("handleSetDependencyMet(" + networkType + ", " + met + ")");
            }
            mNetTrackers[networkType].setDependencyMet(met);
        }
    }

    /**
     * @see ConnectivityManager#setMobileDataEnabled(boolean)
     */
    public void setMobileDataEnabled(boolean enabled) {
        enforceChangePermission();
        if (DBG) log("setMobileDataEnabled(" + enabled + ")");

        mHandler.sendMessage(mHandler.obtainMessage(EVENT_SET_MOBILE_DATA,
                (enabled ? ENABLED : DISABLED), 0));
    }

    private void handleSetMobileData(boolean enabled) {
        if (mNetTrackers[ConnectivityManager.TYPE_MOBILE] != null) {
            if (DBG) {
                Slog.d(TAG, mNetTrackers[ConnectivityManager.TYPE_MOBILE].toString() + enabled);
            }
            mNetTrackers[ConnectivityManager.TYPE_MOBILE].setDataEnable(enabled);
        }
    }

    private void enforceAccessPermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                "ConnectivityService");
    }

    private void enforceChangePermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CHANGE_NETWORK_STATE,
                "ConnectivityService");
    }

    // TODO Make this a special check when it goes public
    private void enforceTetherChangePermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CHANGE_NETWORK_STATE,
                "ConnectivityService");
    }

    private void enforceTetherAccessPermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                "ConnectivityService");
    }

    private void enforceConnectivityInternalPermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CONNECTIVITY_INTERNAL,
                "ConnectivityService");
    }

    /**
     * Handle a {@code DISCONNECTED} event. If this pertains to the non-active
     * network, we ignore it. If it is for the active network, we send out a
     * broadcast. But first, we check whether it might be possible to connect
     * to a different network.
     * @param info the {@code NetworkInfo} for the network
     */
    private void handleDisconnect(NetworkInfo info) {

        int prevNetType = info.getType();

        mNetTrackers[prevNetType].setTeardownRequested(false);
        /*
         * If the disconnected network is not the active one, then don't report
         * this as a loss of connectivity. What probably happened is that we're
         * getting the disconnect for a network that we explicitly disabled
         * in accordance with network preference policies.
         */
        if (!mNetConfigs[prevNetType].isDefault()) {
            List pids = mNetRequestersPids[prevNetType];
            for (int i = 0; i<pids.size(); i++) {
                Integer pid = (Integer)pids.get(i);
                // will remove them because the net's no longer connected
                // need to do this now as only now do we know the pids and
                // can properly null things that are no longer referenced.
                reassessPidDns(pid.intValue(), false);
            }
        }

        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        intent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, info);
        if (info.isFailover()) {
            intent.putExtra(ConnectivityManager.EXTRA_IS_FAILOVER, true);
            info.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra(ConnectivityManager.EXTRA_REASON, info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra(ConnectivityManager.EXTRA_EXTRA_INFO,
                    info.getExtraInfo());
        }

        if (mNetConfigs[prevNetType].isDefault()) {
            tryFailover(prevNetType);
            if (mActiveDefaultNetwork != -1) {
                NetworkInfo switchTo = mNetTrackers[mActiveDefaultNetwork].getNetworkInfo();
                intent.putExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO, switchTo);
            } else {
                mDefaultInetConditionPublished = 0; // we're not connected anymore
                intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);
            }
        }
        intent.putExtra(ConnectivityManager.EXTRA_INET_CONDITION, mDefaultInetConditionPublished);
        // do this before we broadcast the change
        handleConnectivityChange(prevNetType);

        sendStickyBroadcast(intent);
        /*
         * If the failover network is already connected, then immediately send
         * out a followup broadcast indicating successful failover
         */
        if (mActiveDefaultNetwork != -1) {
            sendConnectedBroadcast(mNetTrackers[mActiveDefaultNetwork].getNetworkInfo());
        }
    }

    private void tryFailover(int prevNetType) {
        /*
         * If this is a default network, check if other defaults are available.
         * Try to reconnect on all available and let them hash it out when
         * more than one connects.
         */
        if (mNetConfigs[prevNetType].isDefault()) {
            if (mActiveDefaultNetwork == prevNetType) {
                mActiveDefaultNetwork = -1;
            }

            // don't signal a reconnect for anything lower or equal priority than our
            // current connected default
            // TODO - don't filter by priority now - nice optimization but risky
//            int currentPriority = -1;
//            if (mActiveDefaultNetwork != -1) {
//                currentPriority = mNetConfigs[mActiveDefaultNetwork].mPriority;
//            }
            for (int checkType=0; checkType <= ConnectivityManager.MAX_NETWORK_TYPE; checkType++) {
                if (checkType == prevNetType) continue;
                if (mNetConfigs[checkType] == null) continue;
                if (!mNetConfigs[checkType].isDefault()) continue;

// Enabling the isAvailable() optimization caused mobile to not get
// selected if it was in the middle of error handling. Specifically
// a moble connection that took 30 seconds to complete the DEACTIVATE_DATA_CALL
// would not be available and we wouldn't get connected to anything.
// So removing the isAvailable() optimization below for now. TODO: This
// optimization should work and we need to investigate why it doesn't work.
// This could be related to how DEACTIVATE_DATA_CALL is reporting its
// complete before it is really complete.
//                if (!mNetTrackers[checkType].isAvailable()) continue;

//                if (currentPriority >= mNetConfigs[checkType].mPriority) continue;

                NetworkStateTracker checkTracker = mNetTrackers[checkType];
                NetworkInfo checkInfo = checkTracker.getNetworkInfo();
                if (!checkInfo.isConnectedOrConnecting() || checkTracker.isTeardownRequested()) {
                    checkInfo.setFailover(true);
                    checkTracker.reconnect();
                }
                if (DBG) log("Attempting to switch to " + checkInfo.getTypeName());
            }
        }
    }

    private void sendConnectedBroadcast(NetworkInfo info) {
        sendGeneralBroadcast(info, ConnectivityManager.CONNECTIVITY_ACTION);
    }

    private void sendInetConditionBroadcast(NetworkInfo info) {
        sendGeneralBroadcast(info, ConnectivityManager.INET_CONDITION_ACTION);
    }

    private void sendGeneralBroadcast(NetworkInfo info, String bcastType) {
        Intent intent = new Intent(bcastType);
        intent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, info);
        if (info.isFailover()) {
            intent.putExtra(ConnectivityManager.EXTRA_IS_FAILOVER, true);
            info.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra(ConnectivityManager.EXTRA_REASON, info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra(ConnectivityManager.EXTRA_EXTRA_INFO,
                    info.getExtraInfo());
        }
        intent.putExtra(ConnectivityManager.EXTRA_INET_CONDITION, mDefaultInetConditionPublished);
        sendStickyBroadcast(intent);
    }

    /**
     * Called when an attempt to fail over to another network has failed.
     * @param info the {@link NetworkInfo} for the failed network
     */
    private void handleConnectionFailure(NetworkInfo info) {
        mNetTrackers[info.getType()].setTeardownRequested(false);

        String reason = info.getReason();
        String extraInfo = info.getExtraInfo();

        String reasonText;
        if (reason == null) {
            reasonText = ".";
        } else {
            reasonText = " (" + reason + ").";
        }
        loge("Attempt to connect to " + info.getTypeName() + " failed" + reasonText);

        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        intent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, info);
        if (getActiveNetworkInfo() == null) {
            intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);
        }
        if (reason != null) {
            intent.putExtra(ConnectivityManager.EXTRA_REASON, reason);
        }
        if (extraInfo != null) {
            intent.putExtra(ConnectivityManager.EXTRA_EXTRA_INFO, extraInfo);
        }
        if (info.isFailover()) {
            intent.putExtra(ConnectivityManager.EXTRA_IS_FAILOVER, true);
            info.setFailover(false);
        }

        if (mNetConfigs[info.getType()].isDefault()) {
            tryFailover(info.getType());
            if (mActiveDefaultNetwork != -1) {
                NetworkInfo switchTo = mNetTrackers[mActiveDefaultNetwork].getNetworkInfo();
                intent.putExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO, switchTo);
            } else {
                mDefaultInetConditionPublished = 0;
                intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);
            }
        }

        intent.putExtra(ConnectivityManager.EXTRA_INET_CONDITION, mDefaultInetConditionPublished);
        sendStickyBroadcast(intent);
        /*
         * If the failover network is already connected, then immediately send
         * out a followup broadcast indicating successful failover
         */
        if (mActiveDefaultNetwork != -1) {
            sendConnectedBroadcast(mNetTrackers[mActiveDefaultNetwork].getNetworkInfo());
        }
    }

    private void sendStickyBroadcast(Intent intent) {
        synchronized(this) {
            if (!mSystemReady) {
                mInitialBroadcast = new Intent(intent);
            }
            intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
            mContext.sendStickyBroadcast(intent);
        }
    }

    void systemReady() {
        synchronized(this) {
            mSystemReady = true;
            if (mInitialBroadcast != null) {
                mContext.sendStickyBroadcast(mInitialBroadcast);
                mInitialBroadcast = null;
            }
        }
        // load the global proxy at startup
        mHandler.sendMessage(mHandler.obtainMessage(EVENT_APPLY_GLOBAL_HTTP_PROXY));
    }

    private void handleConnect(NetworkInfo info) {
        int type = info.getType();

        // snapshot isFailover, because sendConnectedBroadcast() resets it
        boolean isFailover = info.isFailover();
        NetworkStateTracker thisNet = mNetTrackers[type];

        // if this is a default net and other default is running
        // kill the one not preferred
        if (mNetConfigs[type].isDefault()) {
            if (mActiveDefaultNetwork != -1 && mActiveDefaultNetwork != type) {
                if ((type != mNetworkPreference &&
                        mNetConfigs[mActiveDefaultNetwork].priority >
                        mNetConfigs[type].priority) ||
                        mNetworkPreference == mActiveDefaultNetwork) {
                        // don't accept this one
                        if (DBG) {
                            log("Not broadcasting CONNECT_ACTION " +
                                "to torn down network " + info.getTypeName());
                        }
                        teardown(thisNet);
                        return;
                } else {
                    // tear down the other
                    NetworkStateTracker otherNet =
                            mNetTrackers[mActiveDefaultNetwork];
                    if (DBG) {
                        log("Policy requires " + otherNet.getNetworkInfo().getTypeName() +
                            " teardown");
                    }
                    if (!teardown(otherNet)) {
                        loge("Network declined teardown request");
                        teardown(thisNet);
                        return;
                    }
                }
            }
            synchronized (ConnectivityService.this) {
                // have a new default network, release the transition wakelock in a second
                // if it's held.  The second pause is to allow apps to reconnect over the
                // new network
                if (mNetTransitionWakeLock.isHeld()) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(
                            EVENT_CLEAR_NET_TRANSITION_WAKELOCK,
                            mNetTransitionWakeLockSerialNumber, 0),
                            1000);
                }
            }
            mActiveDefaultNetwork = type;
            // this will cause us to come up initially as unconnected and switching
            // to connected after our normal pause unless somebody reports us as reall
            // disconnected
            mDefaultInetConditionPublished = 0;
            mDefaultConnectionSequence++;
            mInetConditionChangeInFlight = false;
            // Don't do this - if we never sign in stay, grey
            //reportNetworkCondition(mActiveDefaultNetwork, 100);
        }
        thisNet.setTeardownRequested(false);
        updateNetworkSettings(thisNet);
        handleConnectivityChange(type);
        sendConnectedBroadcast(info);
    }

    /**
     * After a change in the connectivity state of a network. We're mainly
     * concerned with making sure that the list of DNS servers is set up
     * according to which networks are connected, and ensuring that the
     * right routing table entries exist.
     */
    private void handleConnectivityChange(int netType) {
        /*
         * If a non-default network is enabled, add the host routes that
         * will allow it's DNS servers to be accessed.
         */
        handleDnsConfigurationChange(netType);

        if (mNetTrackers[netType].getNetworkInfo().isConnected()) {
            if (mNetConfigs[netType].isDefault()) {
                handleApplyDefaultProxy(netType);
                addDefaultRoute(mNetTrackers[netType]);
            } else {
                // many radios add a default route even when we don't want one.
                // remove the default route unless we need it for our active network
                if (mActiveDefaultNetwork != -1) {
                    LinkProperties defaultLinkProperties =
                            mNetTrackers[mActiveDefaultNetwork].getLinkProperties();
                    LinkProperties newLinkProperties =
                            mNetTrackers[netType].getLinkProperties();
                    String defaultIface = defaultLinkProperties.getInterfaceName();
                    if (defaultIface != null &&
                            !defaultIface.equals(newLinkProperties.getInterfaceName())) {
                        removeDefaultRoute(mNetTrackers[netType]);
                    }
                }
                addPrivateDnsRoutes(mNetTrackers[netType]);
            }
        } else {
            if (mNetConfigs[netType].isDefault()) {
                removeDefaultRoute(mNetTrackers[netType]);
            } else {
                removePrivateDnsRoutes(mNetTrackers[netType]);
            }
        }
    }

    private void addPrivateDnsRoutes(NetworkStateTracker nt) {
        boolean privateDnsRouteSet = nt.isPrivateDnsRouteSet();
        LinkProperties p = nt.getLinkProperties();
        if (p == null) return;
        String interfaceName = p.getInterfaceName();

        if (DBG) {
            log("addPrivateDnsRoutes for " + nt +
                    "(" + interfaceName + ") - mPrivateDnsRouteSet = " + privateDnsRouteSet);
        }
        if (interfaceName != null && !privateDnsRouteSet) {
            Collection<InetAddress> dnsList = p.getDnses();
            for (InetAddress dns : dnsList) {
                if (DBG) log("  adding " + dns);
                addHostRoute(nt, dns, 0);
            }
            nt.privateDnsRouteSet(true);
        }
    }

    private void removePrivateDnsRoutes(NetworkStateTracker nt) {
        // TODO - we should do this explicitly but the NetUtils api doesnt
        // support this yet - must remove all.  No worse than before
        LinkProperties p = nt.getLinkProperties();
        if (p == null) return;
        String interfaceName = p.getInterfaceName();
        boolean privateDnsRouteSet = nt.isPrivateDnsRouteSet();
        if (interfaceName != null && privateDnsRouteSet) {
            if (DBG) {
                log("removePrivateDnsRoutes for " + nt.getNetworkInfo().getTypeName() +
                        " (" + interfaceName + ")");
            }
            NetworkUtils.removeHostRoutes(interfaceName);
            nt.privateDnsRouteSet(false);
        }
    }


    private void addDefaultRoute(NetworkStateTracker nt) {
        LinkProperties p = nt.getLinkProperties();
        if (p == null) return;
        String interfaceName = p.getInterfaceName();
        if (TextUtils.isEmpty(interfaceName)) return;
        for (RouteInfo route : p.getRoutes()) {

            //TODO - handle non-default routes
            if (route.isDefaultRoute()) {
                InetAddress gateway = route.getGateway();
                if (addHostRoute(nt, gateway, 0) &&
                        NetworkUtils.addDefaultRoute(interfaceName, gateway)) {
                    if (DBG) {
                        NetworkInfo networkInfo = nt.getNetworkInfo();
                        log("addDefaultRoute for " + networkInfo.getTypeName() +
                                " (" + interfaceName + "), GatewayAddr=" +
                                gateway.getHostAddress());
                    }
                }
            }
        }
    }


    public void removeDefaultRoute(NetworkStateTracker nt) {
        LinkProperties p = nt.getLinkProperties();
        if (p == null) return;
        String interfaceName = p.getInterfaceName();

        if (interfaceName != null) {
            if (NetworkUtils.removeDefaultRoute(interfaceName) >= 0) {
                if (DBG) {
                    NetworkInfo networkInfo = nt.getNetworkInfo();
                    log("removeDefaultRoute for " + networkInfo.getTypeName() + " (" +
                            interfaceName + ")");
                }
            }
        }
    }

   /**
     * Reads the network specific TCP buffer sizes from SystemProperties
     * net.tcp.buffersize.[default|wifi|umts|edge|gprs] and set them for system
     * wide use
     */
   public void updateNetworkSettings(NetworkStateTracker nt) {
        String key = nt.getTcpBufferSizesPropName();
        String bufferSizes = SystemProperties.get(key);

        if (bufferSizes.length() == 0) {
            loge(key + " not found in system properties. Using defaults");

            // Setting to default values so we won't be stuck to previous values
            key = "net.tcp.buffersize.default";
            bufferSizes = SystemProperties.get(key);
        }

        // Set values in kernel
        if (bufferSizes.length() != 0) {
            if (DBG) {
                log("Setting TCP values: [" + bufferSizes
                        + "] which comes from [" + key + "]");
            }
            setBufferSize(bufferSizes);
        }
    }

   /**
     * Writes TCP buffer sizes to /sys/kernel/ipv4/tcp_[r/w]mem_[min/def/max]
     * which maps to /proc/sys/net/ipv4/tcp_rmem and tcpwmem
     *
     * @param bufferSizes in the format of "readMin, readInitial, readMax,
     *        writeMin, writeInitial, writeMax"
     */
    private void setBufferSize(String bufferSizes) {
        try {
            String[] values = bufferSizes.split(",");

            if (values.length == 6) {
              final String prefix = "/sys/kernel/ipv4/tcp_";
                stringToFile(prefix + "rmem_min", values[0]);
                stringToFile(prefix + "rmem_def", values[1]);
                stringToFile(prefix + "rmem_max", values[2]);
                stringToFile(prefix + "wmem_min", values[3]);
                stringToFile(prefix + "wmem_def", values[4]);
                stringToFile(prefix + "wmem_max", values[5]);
            } else {
                loge("Invalid buffersize string: " + bufferSizes);
            }
        } catch (IOException e) {
            loge("Can't set tcp buffer sizes:" + e);
        }
    }

   /**
     * Writes string to file. Basically same as "echo -n $string > $filename"
     *
     * @param filename
     * @param string
     * @throws IOException
     */
    private void stringToFile(String filename, String string) throws IOException {
        FileWriter out = new FileWriter(filename);
        try {
            out.write(string);
        } finally {
            out.close();
        }
    }


    /**
     * Adjust the per-process dns entries (net.dns<x>.<pid>) based
     * on the highest priority active net which this process requested.
     * If there aren't any, clear it out
     */
    private void reassessPidDns(int myPid, boolean doBump)
    {
        if (DBG) log("reassessPidDns for pid " + myPid);
        for(int i : mPriorityList) {
            if (mNetConfigs[i].isDefault()) {
                continue;
            }
            NetworkStateTracker nt = mNetTrackers[i];
            if (nt.getNetworkInfo().isConnected() &&
                    !nt.isTeardownRequested()) {
                LinkProperties p = nt.getLinkProperties();
                if (p == null) continue;
                List pids = mNetRequestersPids[i];
                for (int j=0; j<pids.size(); j++) {
                    Integer pid = (Integer)pids.get(j);
                    if (pid.intValue() == myPid) {
                        Collection<InetAddress> dnses = p.getDnses();
                        writePidDns(dnses, myPid);
                        if (doBump) {
                            bumpDns();
                        }
                        return;
                    }
                }
           }
        }
        // nothing found - delete
        for (int i = 1; ; i++) {
            String prop = "net.dns" + i + "." + myPid;
            if (SystemProperties.get(prop).length() == 0) {
                if (doBump) {
                    bumpDns();
                }
                return;
            }
            SystemProperties.set(prop, "");
        }
    }

    // return true if results in a change
    private boolean writePidDns(Collection <InetAddress> dnses, int pid) {
        int j = 1;
        boolean changed = false;
        for (InetAddress dns : dnses) {
            String dnsString = dns.getHostAddress();
            if (changed || !dnsString.equals(SystemProperties.get("net.dns" + j + "." + pid))) {
                changed = true;
                SystemProperties.set("net.dns" + j++ + "." + pid, dns.getHostAddress());
            }
        }
        return changed;
    }

    private void bumpDns() {
        /*
         * Bump the property that tells the name resolver library to reread
         * the DNS server list from the properties.
         */
        String propVal = SystemProperties.get("net.dnschange");
        int n = 0;
        if (propVal.length() != 0) {
            try {
                n = Integer.parseInt(propVal);
            } catch (NumberFormatException e) {}
        }
        SystemProperties.set("net.dnschange", "" + (n+1));
        /*
         * Tell the VMs to toss their DNS caches
         */
        Intent intent = new Intent(Intent.ACTION_CLEAR_DNS_CACHE);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        /*
         * Connectivity events can happen before boot has completed ...
         */
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        mContext.sendBroadcast(intent);
    }

    private void handleDnsConfigurationChange(int netType) {
        // add default net's dns entries
        NetworkStateTracker nt = mNetTrackers[netType];
        if (nt != null && nt.getNetworkInfo().isConnected() && !nt.isTeardownRequested()) {
            LinkProperties p = nt.getLinkProperties();
            if (p == null) return;
            Collection<InetAddress> dnses = p.getDnses();
            boolean changed = false;
            if (mNetConfigs[netType].isDefault()) {
                int j = 1;
                if (dnses.size() == 0 && mDefaultDns != null) {
                    String dnsString = mDefaultDns.getHostAddress();
                    if (!dnsString.equals(SystemProperties.get("net.dns1"))) {
                        if (DBG) {
                            log("no dns provided - using " + dnsString);
                        }
                        changed = true;
                        SystemProperties.set("net.dns1", dnsString);
                    }
                    j++;
                } else {
                    for (InetAddress dns : dnses) {
                        String dnsString = dns.getHostAddress();
                        if (!changed && dnsString.equals(SystemProperties.get("net.dns" + j))) {
                            j++;
                            continue;
                        }
                        if (DBG) {
                            log("adding dns " + dns + " for " +
                                    nt.getNetworkInfo().getTypeName());
                        }
                        changed = true;
                        SystemProperties.set("net.dns" + j++, dnsString);
                    }
                }
                for (int k=j ; k<mNumDnsEntries; k++) {
                    if (changed || !TextUtils.isEmpty(SystemProperties.get("net.dns" + k))) {
                        if (DBG) log("erasing net.dns" + k);
                        changed = true;
                        SystemProperties.set("net.dns" + k, "");
                    }
                }
                mNumDnsEntries = j;
            } else {
                // set per-pid dns for attached secondary nets
                List pids = mNetRequestersPids[netType];
                for (int y=0; y< pids.size(); y++) {
                    Integer pid = (Integer)pids.get(y);
                    changed = writePidDns(dnses, pid.intValue());
                }
            }
            if (changed) bumpDns();
        }
    }

    private int getRestoreDefaultNetworkDelay(int networkType) {
        String restoreDefaultNetworkDelayStr = SystemProperties.get(
                NETWORK_RESTORE_DELAY_PROP_NAME);
        if(restoreDefaultNetworkDelayStr != null &&
                restoreDefaultNetworkDelayStr.length() != 0) {
            try {
                return Integer.valueOf(restoreDefaultNetworkDelayStr);
            } catch (NumberFormatException e) {
            }
        }
        // if the system property isn't set, use the value for the apn type
        int ret = RESTORE_DEFAULT_NETWORK_DELAY;

        if ((networkType <= ConnectivityManager.MAX_NETWORK_TYPE) &&
                (mNetConfigs[networkType] != null)) {
            ret = mNetConfigs[networkType].restoreTime;
        }
        return ret;
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(
                android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump ConnectivityService " +
                    "from from pid=" + Binder.getCallingPid() + ", uid=" +
                    Binder.getCallingUid());
            return;
        }
        pw.println();
        for (NetworkStateTracker nst : mNetTrackers) {
            if (nst != null) {
                if (nst.getNetworkInfo().isConnected()) {
                    pw.println("Active network: " + nst.getNetworkInfo().
                            getTypeName());
                }
                pw.println(nst.getNetworkInfo());
                pw.println(nst);
                pw.println();
            }
        }

        pw.println("Network Requester Pids:");
        for (int net : mPriorityList) {
            String pidString = net + ": ";
            for (Object pid : mNetRequestersPids[net]) {
                pidString = pidString + pid.toString() + ", ";
            }
            pw.println(pidString);
        }
        pw.println();

        pw.println("FeatureUsers:");
        for (Object requester : mFeatureUsers) {
            pw.println(requester.toString());
        }
        pw.println();

        synchronized (this) {
            pw.println("NetworkTranstionWakeLock is currently " +
                    (mNetTransitionWakeLock.isHeld() ? "" : "not ") + "held.");
            pw.println("It was last requested for "+mNetTransitionWakeLockCausedBy);
        }
        pw.println();

        mTethering.dump(fd, pw, args);

        if (mInetLog != null) {
            pw.println();
            pw.println("Inet condition reports:");
            for(int i = 0; i < mInetLog.size(); i++) {
                pw.println(mInetLog.get(i));
            }
        }
    }

    // must be stateless - things change under us.
    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            NetworkInfo info;
            switch (msg.what) {
                case NetworkStateTracker.EVENT_STATE_CHANGED:
                    info = (NetworkInfo) msg.obj;
                    int type = info.getType();
                    NetworkInfo.State state = info.getState();

                    if (DBG) log("ConnectivityChange for " +
                            info.getTypeName() + ": " +
                            state + "/" + info.getDetailedState());

                    // Connectivity state changed:
                    // [31-13] Reserved for future use
                    // [12-9] Network subtype (for mobile network, as defined
                    //         by TelephonyManager)
                    // [8-3] Detailed state ordinal (as defined by
                    //         NetworkInfo.DetailedState)
                    // [2-0] Network type (as defined by ConnectivityManager)
                    int eventLogParam = (info.getType() & 0x7) |
                            ((info.getDetailedState().ordinal() & 0x3f) << 3) |
                            (info.getSubtype() << 9);
                    EventLog.writeEvent(EventLogTags.CONNECTIVITY_STATE_CHANGED,
                            eventLogParam);

                    if (info.getDetailedState() ==
                            NetworkInfo.DetailedState.FAILED) {
                        handleConnectionFailure(info);
                    } else if (state == NetworkInfo.State.DISCONNECTED) {
                        handleDisconnect(info);
                    } else if (state == NetworkInfo.State.SUSPENDED) {
                        // TODO: need to think this over.
                        // the logic here is, handle SUSPENDED the same as
                        // DISCONNECTED. The only difference being we are
                        // broadcasting an intent with NetworkInfo that's
                        // suspended. This allows the applications an
                        // opportunity to handle DISCONNECTED and SUSPENDED
                        // differently, or not.
                        handleDisconnect(info);
                    } else if (state == NetworkInfo.State.CONNECTED) {
                        handleConnect(info);
                    }
                    break;
                case NetworkStateTracker.EVENT_CONFIGURATION_CHANGED:
                    info = (NetworkInfo) msg.obj;
                    handleConnectivityChange(info.getType());
                    break;
                case EVENT_CLEAR_NET_TRANSITION_WAKELOCK:
                    String causedBy = null;
                    synchronized (ConnectivityService.this) {
                        if (msg.arg1 == mNetTransitionWakeLockSerialNumber &&
                                mNetTransitionWakeLock.isHeld()) {
                            mNetTransitionWakeLock.release();
                            causedBy = mNetTransitionWakeLockCausedBy;
                        }
                    }
                    if (causedBy != null) {
                        log("NetTransition Wakelock for " + causedBy + " released by timeout");
                    }
                    break;
                case EVENT_RESTORE_DEFAULT_NETWORK:
                    FeatureUser u = (FeatureUser)msg.obj;
                    u.expire();
                    break;
                case EVENT_INET_CONDITION_CHANGE:
                {
                    int netType = msg.arg1;
                    int condition = msg.arg2;
                    handleInetConditionChange(netType, condition);
                    break;
                }
                case EVENT_INET_CONDITION_HOLD_END:
                {
                    int netType = msg.arg1;
                    int sequence = msg.arg2;
                    handleInetConditionHoldEnd(netType, sequence);
                    break;
                }
                case EVENT_SET_NETWORK_PREFERENCE:
                {
                    int preference = msg.arg1;
                    handleSetNetworkPreference(preference);
                    break;
                }
                case EVENT_SET_BACKGROUND_DATA:
                {
                    boolean enabled = (msg.arg1 == ENABLED);
                    handleSetBackgroundData(enabled);
                    break;
                }
                case EVENT_SET_MOBILE_DATA:
                {
                    boolean enabled = (msg.arg1 == ENABLED);
                    handleSetMobileData(enabled);
                    break;
                }
                case EVENT_APPLY_GLOBAL_HTTP_PROXY:
                {
                    handleDeprecatedGlobalHttpProxy();
                    break;
                }
                case EVENT_SET_DEPENDENCY_MET:
                {
                    boolean met = (msg.arg1 == ENABLED);
                    handleSetDependencyMet(msg.arg2, met);
                    break;
                }
            }
        }
    }

    // javadoc from interface
    public int tether(String iface) {
        enforceTetherChangePermission();

        if (isTetheringSupported()) {
            return mTethering.tether(iface);
        } else {
            return ConnectivityManager.TETHER_ERROR_UNSUPPORTED;
        }
    }

    // javadoc from interface
    public int untether(String iface) {
        enforceTetherChangePermission();

        if (isTetheringSupported()) {
            return mTethering.untether(iface);
        } else {
            return ConnectivityManager.TETHER_ERROR_UNSUPPORTED;
        }
    }

    // javadoc from interface
    public int getLastTetherError(String iface) {
        enforceTetherAccessPermission();

        if (isTetheringSupported()) {
            return mTethering.getLastTetherError(iface);
        } else {
            return ConnectivityManager.TETHER_ERROR_UNSUPPORTED;
        }
    }

    // TODO - proper iface API for selection by property, inspection, etc
    public String[] getTetherableUsbRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return mTethering.getTetherableUsbRegexs();
        } else {
            return new String[0];
        }
    }

    public String[] getTetherableWifiRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return mTethering.getTetherableWifiRegexs();
        } else {
            return new String[0];
        }
    }

    public String[] getTetherableBluetoothRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return mTethering.getTetherableBluetoothRegexs();
        } else {
            return new String[0];
        }
    }

    // TODO - move iface listing, queries, etc to new module
    // javadoc from interface
    public String[] getTetherableIfaces() {
        enforceTetherAccessPermission();
        return mTethering.getTetherableIfaces();
    }

    public String[] getTetheredIfaces() {
        enforceTetherAccessPermission();
        return mTethering.getTetheredIfaces();
    }

    public String[] getTetheringErroredIfaces() {
        enforceTetherAccessPermission();
        return mTethering.getErroredIfaces();
    }

    // if ro.tether.denied = true we default to no tethering
    // gservices could set the secure setting to 1 though to enable it on a build where it
    // had previously been turned off.
    public boolean isTetheringSupported() {
        enforceTetherAccessPermission();
        int defaultVal = (SystemProperties.get("ro.tether.denied").equals("true") ? 0 : 1);
        boolean tetherEnabledInSettings = (Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.TETHER_SUPPORTED, defaultVal) != 0);
        return tetherEnabledInSettings && mTetheringConfigValid;
    }

    // An API NetworkStateTrackers can call when they lose their network.
    // This will automatically be cleared after X seconds or a network becomes CONNECTED,
    // whichever happens first.  The timer is started by the first caller and not
    // restarted by subsequent callers.
    public void requestNetworkTransitionWakelock(String forWhom) {
        enforceConnectivityInternalPermission();
        synchronized (this) {
            if (mNetTransitionWakeLock.isHeld()) return;
            mNetTransitionWakeLockSerialNumber++;
            mNetTransitionWakeLock.acquire();
            mNetTransitionWakeLockCausedBy = forWhom;
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(
                EVENT_CLEAR_NET_TRANSITION_WAKELOCK,
                mNetTransitionWakeLockSerialNumber, 0),
                mNetTransitionWakeLockTimeout);
        return;
    }

    // 100 percent is full good, 0 is full bad.
    public void reportInetCondition(int networkType, int percentage) {
        if (DBG) log("reportNetworkCondition(" + networkType + ", " + percentage + ")");
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.STATUS_BAR,
                "ConnectivityService");

        if (DBG) {
            int pid = getCallingPid();
            int uid = getCallingUid();
            String s = pid + "(" + uid + ") reports inet is " +
                (percentage > 50 ? "connected" : "disconnected") + " (" + percentage + ") on " +
                "network Type " + networkType + " at " + GregorianCalendar.getInstance().getTime();
            mInetLog.add(s);
            while(mInetLog.size() > INET_CONDITION_LOG_MAX_SIZE) {
                mInetLog.remove(0);
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(
            EVENT_INET_CONDITION_CHANGE, networkType, percentage));
    }

    private void handleInetConditionChange(int netType, int condition) {
        if (DBG) {
            log("Inet connectivity change, net=" +
                    netType + ", condition=" + condition +
                    ",mActiveDefaultNetwork=" + mActiveDefaultNetwork);
        }
        if (mActiveDefaultNetwork == -1) {
            if (DBG) log("no active default network - aborting");
            return;
        }
        if (mActiveDefaultNetwork != netType) {
            if (DBG) log("given net not default - aborting");
            return;
        }
        mDefaultInetCondition = condition;
        int delay;
        if (mInetConditionChangeInFlight == false) {
            if (DBG) log("starting a change hold");
            // setup a new hold to debounce this
            if (mDefaultInetCondition > 50) {
                delay = Settings.Secure.getInt(mContext.getContentResolver(),
                        Settings.Secure.INET_CONDITION_DEBOUNCE_UP_DELAY, 500);
            } else {
                delay = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.INET_CONDITION_DEBOUNCE_DOWN_DELAY, 3000);
            }
            mInetConditionChangeInFlight = true;
            mHandler.sendMessageDelayed(mHandler.obtainMessage(EVENT_INET_CONDITION_HOLD_END,
                    mActiveDefaultNetwork, mDefaultConnectionSequence), delay);
        } else {
            // we've set the new condition, when this hold ends that will get
            // picked up
            if (DBG) log("currently in hold - not setting new end evt");
        }
    }

    private void handleInetConditionHoldEnd(int netType, int sequence) {
        if (DBG) {
            log("Inet hold end, net=" + netType +
                    ", condition =" + mDefaultInetCondition +
                    ", published condition =" + mDefaultInetConditionPublished);
        }
        mInetConditionChangeInFlight = false;

        if (mActiveDefaultNetwork == -1) {
            if (DBG) log("no active default network - aborting");
            return;
        }
        if (mDefaultConnectionSequence != sequence) {
            if (DBG) log("event hold for obsolete network - aborting");
            return;
        }
        if (mDefaultInetConditionPublished == mDefaultInetCondition) {
            if (DBG) log("no change in condition - aborting");
            return;
        }
        NetworkInfo networkInfo = mNetTrackers[mActiveDefaultNetwork].getNetworkInfo();
        if (networkInfo.isConnected() == false) {
            if (DBG) log("default network not connected - aborting");
            return;
        }
        mDefaultInetConditionPublished = mDefaultInetCondition;
        sendInetConditionBroadcast(networkInfo);
        return;
    }

    public synchronized ProxyProperties getProxy() {
        if (mGlobalProxy != null) return mGlobalProxy;
        if (mDefaultProxy != null) return mDefaultProxy;
        return null;
    }

    public void setGlobalProxy(ProxyProperties proxyProperties) {
        enforceChangePermission();
        synchronized (mGlobalProxyLock) {
            if (proxyProperties == mGlobalProxy) return;
            if (proxyProperties != null && proxyProperties.equals(mGlobalProxy)) return;
            if (mGlobalProxy != null && mGlobalProxy.equals(proxyProperties)) return;

            String host = "";
            int port = 0;
            String exclList = "";
            if (proxyProperties != null && !TextUtils.isEmpty(proxyProperties.getHost())) {
                mGlobalProxy = new ProxyProperties(proxyProperties);
                host = mGlobalProxy.getHost();
                port = mGlobalProxy.getPort();
                exclList = mGlobalProxy.getExclusionList();
            } else {
                mGlobalProxy = null;
            }
            ContentResolver res = mContext.getContentResolver();
            Settings.Secure.putString(res, Settings.Secure.GLOBAL_HTTP_PROXY_HOST, host);
            Settings.Secure.putInt(res, Settings.Secure.GLOBAL_HTTP_PROXY_PORT, port);
            Settings.Secure.putString(res, Settings.Secure.GLOBAL_HTTP_PROXY_EXCLUSION_LIST,
                    exclList);
        }

        if (mGlobalProxy == null) {
            proxyProperties = mDefaultProxy;
        }
        sendProxyBroadcast(proxyProperties);
    }

    private void loadGlobalProxy() {
        ContentResolver res = mContext.getContentResolver();
        String host = Settings.Secure.getString(res, Settings.Secure.GLOBAL_HTTP_PROXY_HOST);
        int port = Settings.Secure.getInt(res, Settings.Secure.GLOBAL_HTTP_PROXY_PORT, 0);
        String exclList = Settings.Secure.getString(res,
                Settings.Secure.GLOBAL_HTTP_PROXY_EXCLUSION_LIST);
        if (!TextUtils.isEmpty(host)) {
            ProxyProperties proxyProperties = new ProxyProperties(host, port, exclList);
            synchronized (mGlobalProxyLock) {
                mGlobalProxy = proxyProperties;
            }
        }
    }

    public ProxyProperties getGlobalProxy() {
        synchronized (mGlobalProxyLock) {
            return mGlobalProxy;
        }
    }

    private void handleApplyDefaultProxy(int type) {
        // check if new default - push it out to all VM if so
        ProxyProperties proxy = mNetTrackers[type].getLinkProperties().getHttpProxy();
        synchronized (this) {
            if (mDefaultProxy != null && mDefaultProxy.equals(proxy)) return;
            if (mDefaultProxy == proxy) return;
            if (!TextUtils.isEmpty(proxy.getHost())) {
                mDefaultProxy = proxy;
            } else {
                mDefaultProxy = null;
            }
        }
        if (DBG) log("changing default proxy to " + proxy);
        if ((proxy == null && mGlobalProxy == null) || proxy.equals(mGlobalProxy)) return;
        if (mGlobalProxy != null) return;
        sendProxyBroadcast(proxy);
    }

    private void handleDeprecatedGlobalHttpProxy() {
        String proxy = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.HTTP_PROXY);
        if (!TextUtils.isEmpty(proxy)) {
            String data[] = proxy.split(":");
            String proxyHost =  data[0];
            int proxyPort = 8080;
            if (data.length > 1) {
                try {
                    proxyPort = Integer.parseInt(data[1]);
                } catch (NumberFormatException e) {
                    return;
                }
            }
            ProxyProperties p = new ProxyProperties(data[0], proxyPort, "");
            setGlobalProxy(p);
        }
    }

    private void sendProxyBroadcast(ProxyProperties proxy) {
        if (proxy == null) proxy = new ProxyProperties("", 0, "");
        log("sending Proxy Broadcast for " + proxy);
        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING |
            Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.putExtra(Proxy.EXTRA_PROXY_INFO, proxy);
        mContext.sendStickyBroadcast(intent);
    }

    private static class SettingsObserver extends ContentObserver {
        private int mWhat;
        private Handler mHandler;
        SettingsObserver(Handler handler, int what) {
            super(handler);
            mHandler = handler;
            mWhat = what;
        }

        void observe(Context context) {
            ContentResolver resolver = context.getContentResolver();
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.HTTP_PROXY), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.obtainMessage(mWhat).sendToTarget();
        }
    }

    private void log(String s) {
        Slog.d(TAG, s);
    }

    private void loge(String s) {
        Slog.e(TAG, s);
    }
    int convertFeatureToNetworkType(String feature){
        int networkType = -1;
        if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_MMS)) {
            networkType = ConnectivityManager.TYPE_MOBILE_MMS;
        } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_SUPL)) {
            networkType = ConnectivityManager.TYPE_MOBILE_SUPL;
        } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_DUN) ||
                TextUtils.equals(feature, Phone.FEATURE_ENABLE_DUN_ALWAYS)) {
            networkType = ConnectivityManager.TYPE_MOBILE_DUN;
        } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_HIPRI)) {
            networkType = ConnectivityManager.TYPE_MOBILE_HIPRI;
        } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_FOTA)) {
            networkType = ConnectivityManager.TYPE_MOBILE_FOTA;
        } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_IMS)) {
            networkType = ConnectivityManager.TYPE_MOBILE_IMS;
        } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_CBS)) {
            networkType = ConnectivityManager.TYPE_MOBILE_CBS;
        }
        return networkType;
    }
}