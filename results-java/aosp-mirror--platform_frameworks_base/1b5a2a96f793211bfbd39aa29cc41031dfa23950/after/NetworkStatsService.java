/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.server.net;

import static android.Manifest.permission.CONNECTIVITY_INTERNAL;
import static android.Manifest.permission.DUMP;
import static android.Manifest.permission.READ_NETWORK_USAGE_HISTORY;
import static android.Manifest.permission.SHUTDOWN;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.net.NetworkStats.IFACE_ALL;
import static android.net.NetworkStats.TAG_NONE;
import static android.net.NetworkStats.UID_ALL;
import static android.provider.Settings.Secure.NETSTATS_NETWORK_BUCKET_DURATION;
import static android.provider.Settings.Secure.NETSTATS_NETWORK_MAX_HISTORY;
import static android.provider.Settings.Secure.NETSTATS_PERSIST_THRESHOLD;
import static android.provider.Settings.Secure.NETSTATS_POLL_INTERVAL;
import static android.provider.Settings.Secure.NETSTATS_UID_BUCKET_DURATION;
import static android.provider.Settings.Secure.NETSTATS_UID_MAX_HISTORY;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static com.android.internal.util.Preconditions.checkNotNull;

import android.app.AlarmManager;
import android.app.IAlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.IConnectivityManager;
import android.net.INetworkStatsService;
import android.net.NetworkIdentity;
import android.net.NetworkInfo;
import android.net.NetworkState;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.NtpTrustedTime;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TrustedTime;

import com.android.internal.os.AtomicFile;
import com.google.android.collect.Maps;
import com.google.android.collect.Sets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import libcore.io.IoUtils;

/**
 * Collect and persist detailed network statistics, and provide this data to
 * other system services.
 */
public class NetworkStatsService extends INetworkStatsService.Stub {
    private static final String TAG = "NetworkStats";
    private static final boolean LOGD = true;
    private static final boolean LOGV = false;

    /** File header magic number: "ANET" */
    private static final int FILE_MAGIC = 0x414E4554;
    private static final int VERSION_NETWORK_INIT = 1;
    private static final int VERSION_UID_INIT = 1;
    private static final int VERSION_UID_WITH_IDENT = 2;

    private final Context mContext;
    private final INetworkManagementService mNetworkManager;
    private final IAlarmManager mAlarmManager;
    private final TrustedTime mTime;
    private final NetworkStatsSettings mSettings;

    private IConnectivityManager mConnManager;

    // @VisibleForTesting
    public static final String ACTION_NETWORK_STATS_POLL =
            "com.android.server.action.NETWORK_STATS_POLL";
    public static final String ACTION_NETWORK_STATS_UPDATED =
            "com.android.server.action.NETWORK_STATS_UPDATED";

    private PendingIntent mPollIntent;

    // TODO: listen for kernel push events through netd instead of polling
    // TODO: watch for UID uninstall, and transfer stats into single bucket

    private static final long KB_IN_BYTES = 1024;
    private static final long MB_IN_BYTES = 1024 * KB_IN_BYTES;
    private static final long GB_IN_BYTES = 1024 * MB_IN_BYTES;

    /**
     * Settings that can be changed externally.
     */
    public interface NetworkStatsSettings {
        public long getPollInterval();
        public long getPersistThreshold();
        public long getNetworkBucketDuration();
        public long getNetworkMaxHistory();
        public long getUidBucketDuration();
        public long getUidMaxHistory();
        public long getTimeCacheMaxAge();
    }

    private final Object mStatsLock = new Object();

    /** Set of currently active ifaces. */
    private HashMap<String, NetworkIdentitySet> mActiveIfaces = Maps.newHashMap();
    /** Set of historical stats for known networks. */
    private HashMap<NetworkIdentitySet, NetworkStatsHistory> mNetworkStats = Maps.newHashMap();
    /** Set of historical stats for known UIDs. */
    private HashMap<NetworkIdentitySet, SparseArray<NetworkStatsHistory>> mUidStats =
            Maps.newHashMap();

    /** Flag if {@link #mUidStats} have been loaded from disk. */
    private boolean mUidStatsLoaded = false;

    private NetworkStats mLastNetworkPoll;
    private NetworkStats mLastNetworkPersist;

    private NetworkStats mLastUidPoll;

    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    private final AtomicFile mNetworkFile;
    private final AtomicFile mUidFile;

    // TODO: collect detailed uid stats, storing tag-granularity data until next
    // dropbox, and uid summary for a specific bucket count.

    // TODO: periodically compile statistics and send to dropbox.

    public NetworkStatsService(
            Context context, INetworkManagementService networkManager, IAlarmManager alarmManager) {
        // TODO: move to using cached NtpTrustedTime
        this(context, networkManager, alarmManager, new NtpTrustedTime(), getSystemDir(),
                new DefaultNetworkStatsSettings(context));
    }

    private static File getSystemDir() {
        return new File(Environment.getDataDirectory(), "system");
    }

    public NetworkStatsService(Context context, INetworkManagementService networkManager,
            IAlarmManager alarmManager, TrustedTime time, File systemDir,
            NetworkStatsSettings settings) {
        mContext = checkNotNull(context, "missing Context");
        mNetworkManager = checkNotNull(networkManager, "missing INetworkManagementService");
        mAlarmManager = checkNotNull(alarmManager, "missing IAlarmManager");
        mTime = checkNotNull(time, "missing TrustedTime");
        mSettings = checkNotNull(settings, "missing NetworkStatsSettings");

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mNetworkFile = new AtomicFile(new File(systemDir, "netstats.bin"));
        mUidFile = new AtomicFile(new File(systemDir, "netstats_uid.bin"));
    }

    public void bindConnectivityManager(IConnectivityManager connManager) {
        mConnManager = checkNotNull(connManager, "missing IConnectivityManager");
    }

    public void systemReady() {
        synchronized (mStatsLock) {
            // read historical network stats from disk, since policy service
            // might need them right away. we delay loading detailed UID stats
            // until actually needed.
            readNetworkStatsLocked();
        }

        // watch for network interfaces to be claimed
        final IntentFilter ifaceFilter = new IntentFilter();
        ifaceFilter.addAction(CONNECTIVITY_ACTION);
        mContext.registerReceiver(mIfaceReceiver, ifaceFilter, CONNECTIVITY_INTERNAL, mHandler);

        // listen for periodic polling events
        final IntentFilter pollFilter = new IntentFilter(ACTION_NETWORK_STATS_POLL);
        mContext.registerReceiver(mPollReceiver, pollFilter, READ_NETWORK_USAGE_HISTORY, mHandler);

        // persist stats during clean shutdown
        final IntentFilter shutdownFilter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        mContext.registerReceiver(mShutdownReceiver, shutdownFilter, SHUTDOWN, null);

        try {
            registerPollAlarmLocked();
        } catch (RemoteException e) {
            Slog.w(TAG, "unable to register poll alarm");
        }
    }

    private void shutdownLocked() {
        mContext.unregisterReceiver(mIfaceReceiver);
        mContext.unregisterReceiver(mPollReceiver);
        mContext.unregisterReceiver(mShutdownReceiver);

        writeNetworkStatsLocked();
        if (mUidStatsLoaded) {
            writeUidStatsLocked();
        }
        mNetworkStats.clear();
        mUidStats.clear();
        mUidStatsLoaded = false;
    }

    /**
     * Clear any existing {@link #ACTION_NETWORK_STATS_POLL} alarms, and
     * reschedule based on current {@link NetworkStatsSettings#getPollInterval()}.
     */
    private void registerPollAlarmLocked() throws RemoteException {
        if (mPollIntent != null) {
            mAlarmManager.remove(mPollIntent);
        }

        mPollIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent(ACTION_NETWORK_STATS_POLL), 0);

        final long currentRealtime = SystemClock.elapsedRealtime();
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, currentRealtime,
                mSettings.getPollInterval(), mPollIntent);
    }

    @Override
    public NetworkStatsHistory getHistoryForNetwork(NetworkTemplate template) {
        mContext.enforceCallingOrSelfPermission(READ_NETWORK_USAGE_HISTORY, TAG);

        synchronized (mStatsLock) {
            // combine all interfaces that match template
            final NetworkStatsHistory combined = new NetworkStatsHistory(
                    mSettings.getNetworkBucketDuration(), estimateNetworkBuckets());
            for (NetworkIdentitySet ident : mNetworkStats.keySet()) {
                if (templateMatches(template, ident)) {
                    final NetworkStatsHistory history = mNetworkStats.get(ident);
                    combined.recordEntireHistory(history);
                }
            }
            return combined;
        }
    }

    @Override
    public NetworkStatsHistory getHistoryForUid(NetworkTemplate template, int uid) {
        mContext.enforceCallingOrSelfPermission(READ_NETWORK_USAGE_HISTORY, TAG);

        synchronized (mStatsLock) {
            ensureUidStatsLoadedLocked();

            // combine all interfaces that match template
            final NetworkStatsHistory combined = new NetworkStatsHistory(
                    mSettings.getUidBucketDuration(), estimateUidBuckets());
            for (NetworkIdentitySet ident : mUidStats.keySet()) {
                if (templateMatches(template, ident)) {
                    final NetworkStatsHistory history = mUidStats.get(ident).get(uid);
                    if (history != null) {
                        combined.recordEntireHistory(history);
                    }
                }
            }
            return combined;
        }
    }

    @Override
    public NetworkStats getSummaryForNetwork(NetworkTemplate template, long start, long end) {
        mContext.enforceCallingOrSelfPermission(READ_NETWORK_USAGE_HISTORY, TAG);

        synchronized (mStatsLock) {
            long rx = 0;
            long tx = 0;
            long[] networkTotal = new long[2];

            // combine total from all interfaces that match template
            for (NetworkIdentitySet ident : mNetworkStats.keySet()) {
                if (templateMatches(template, ident)) {
                    final NetworkStatsHistory history = mNetworkStats.get(ident);
                    networkTotal = history.getTotalData(start, end, networkTotal);
                    rx += networkTotal[0];
                    tx += networkTotal[1];
                }
            }

            final NetworkStats stats = new NetworkStats(end - start, 1);
            stats.addEntry(IFACE_ALL, UID_ALL, TAG_NONE, rx, tx);
            return stats;
        }
    }

    @Override
    public NetworkStats getSummaryForAllUid(NetworkTemplate template, long start, long end) {
        mContext.enforceCallingOrSelfPermission(READ_NETWORK_USAGE_HISTORY, TAG);

        synchronized (mStatsLock) {
            ensureUidStatsLoadedLocked();

            final NetworkStats stats = new NetworkStats(end - start, 24);
            long[] total = new long[2];

            for (NetworkIdentitySet ident : mUidStats.keySet()) {
                if (templateMatches(template, ident)) {
                    final SparseArray<NetworkStatsHistory> uidStats = mUidStats.get(ident);
                    for (int i = 0; i < uidStats.size(); i++) {
                        final int uid = uidStats.keyAt(i);
                        final NetworkStatsHistory history = uidStats.valueAt(i);
                        total = history.getTotalData(start, end, total);
                        stats.combineEntry(IFACE_ALL, uid, TAG_NONE, total[0], total[1]);
                    }
                }
            }

            return stats;
        }
    }

    /**
     * Receiver that watches for {@link IConnectivityManager} to claim network
     * interfaces. Used to associate {@link TelephonyManager#getSubscriberId()}
     * with mobile interfaces.
     */
    private BroadcastReceiver mIfaceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // on background handler thread, and verified CONNECTIVITY_INTERNAL
            // permission above.
            synchronized (mStatsLock) {
                updateIfacesLocked();
            }
        }
    };

    private BroadcastReceiver mPollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // on background handler thread, and verified UPDATE_DEVICE_STATS
            // permission above.
            synchronized (mStatsLock) {
                // TODO: acquire wakelock while performing poll
                performPollLocked(true, false);
            }
        }
    };

    private BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // verified SHUTDOWN permission above.
            synchronized (mStatsLock) {
                shutdownLocked();
            }
        }
    };

    /**
     * Inspect all current {@link NetworkState} to derive mapping from {@code
     * iface} to {@link NetworkStatsHistory}. When multiple {@link NetworkInfo}
     * are active on a single {@code iface}, they are combined under a single
     * {@link NetworkIdentitySet}.
     */
    private void updateIfacesLocked() {
        if (LOGV) Slog.v(TAG, "updateIfacesLocked()");

        // take one last stats snapshot before updating iface mapping. this
        // isn't perfect, since the kernel may already be counting traffic from
        // the updated network.
        performPollLocked(false, false);

        final NetworkState[] states;
        try {
            states = mConnManager.getAllNetworkState();
        } catch (RemoteException e) {
            Slog.w(TAG, "problem reading network state");
            return;
        }

        // rebuild active interfaces based on connected networks
        mActiveIfaces.clear();

        for (NetworkState state : states) {
            if (state.networkInfo.isConnected()) {
                // collect networks under their parent interfaces
                final String iface = state.linkProperties.getInterfaceName();

                NetworkIdentitySet ident = mActiveIfaces.get(iface);
                if (ident == null) {
                    ident = new NetworkIdentitySet();
                    mActiveIfaces.put(iface, ident);
                }

                ident.add(NetworkIdentity.buildNetworkIdentity(mContext, state));
            }
        }
    }

    /**
     * Periodic poll operation, reading current statistics and recording into
     * {@link NetworkStatsHistory}.
     *
     * @param detailedPoll Indicate if detailed UID stats should be collected
     *            during this poll operation.
     */
    private void performPollLocked(boolean detailedPoll, boolean forcePersist) {
        if (LOGV) Slog.v(TAG, "performPollLocked()");

        // try refreshing time source when stale
        if (mTime.getCacheAge() > mSettings.getTimeCacheMaxAge()) {
            mTime.forceRefresh();
        }

        // TODO: consider marking "untrusted" times in historical stats
        final long currentTime = mTime.hasCache() ? mTime.currentTimeMillis()
                : System.currentTimeMillis();

        final NetworkStats ifaceStats;
        final NetworkStats uidStats;
        try {
            ifaceStats = mNetworkManager.getNetworkStatsSummary();
            uidStats = detailedPoll ? mNetworkManager.getNetworkStatsDetail() : null;
        } catch (RemoteException e) {
            Slog.w(TAG, "problem reading network stats");
            return;
        }

        performNetworkPollLocked(ifaceStats, currentTime);
        if (detailedPoll) {
            performUidPollLocked(uidStats, currentTime);
        }

        // decide if enough has changed to trigger persist
        final NetworkStats persistDelta = computeStatsDelta(mLastNetworkPersist, ifaceStats);
        final long persistThreshold = mSettings.getPersistThreshold();
        for (String iface : persistDelta.getUniqueIfaces()) {
            final int index = persistDelta.findIndex(iface, UID_ALL, TAG_NONE);
            if (forcePersist || persistDelta.rx[index] > persistThreshold
                    || persistDelta.tx[index] > persistThreshold) {
                writeNetworkStatsLocked();
                if (mUidStatsLoaded) {
                    writeUidStatsLocked();
                }
                mLastNetworkPersist = ifaceStats;
                break;
            }
        }

        // finally, dispatch updated event to any listeners
        final Intent updatedIntent = new Intent(ACTION_NETWORK_STATS_UPDATED);
        updatedIntent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        mContext.sendBroadcast(updatedIntent, READ_NETWORK_USAGE_HISTORY);
    }

    /**
     * Update {@link #mNetworkStats} historical usage.
     */
    private void performNetworkPollLocked(NetworkStats networkStats, long currentTime) {
        final HashSet<String> unknownIface = Sets.newHashSet();

        final NetworkStats delta = computeStatsDelta(mLastNetworkPoll, networkStats);
        final long timeStart = currentTime - delta.elapsedRealtime;
        final long maxHistory = mSettings.getNetworkMaxHistory();
        for (int i = 0; i < delta.size; i++) {
            final String iface = delta.iface[i];
            final NetworkIdentitySet ident = mActiveIfaces.get(iface);
            if (ident == null) {
                unknownIface.add(iface);
                continue;
            }

            final long rx = delta.rx[i];
            final long tx = delta.tx[i];

            final NetworkStatsHistory history = findOrCreateNetworkStatsLocked(ident);
            history.recordData(timeStart, currentTime, rx, tx);
            history.removeBucketsBefore(currentTime - maxHistory);
        }
        mLastNetworkPoll = networkStats;

        if (LOGD && unknownIface.size() > 0) {
            Slog.w(TAG, "unknown interfaces " + unknownIface.toString() + ", ignoring those stats");
        }
    }

    /**
     * Update {@link #mUidStats} historical usage.
     */
    private void performUidPollLocked(NetworkStats uidStats, long currentTime) {
        ensureUidStatsLoadedLocked();

        final NetworkStats delta = computeStatsDelta(mLastUidPoll, uidStats);
        final long timeStart = currentTime - delta.elapsedRealtime;
        final long maxHistory = mSettings.getUidMaxHistory();

        // NOTE: historical UID stats ignore tags, and simply records all stats
        // entries into a single UID bucket.

        for (int i = 0; i < delta.size; i++) {
            final String iface = delta.iface[i];
            final NetworkIdentitySet ident = mActiveIfaces.get(iface);
            if (ident == null) {
                continue;
            }

            final int uid = delta.uid[i];
            final long rx = delta.rx[i];
            final long tx = delta.tx[i];

            final NetworkStatsHistory history = findOrCreateUidStatsLocked(ident, uid);
            history.recordData(timeStart, currentTime, rx, tx);
            history.removeBucketsBefore(currentTime - maxHistory);
        }

        mLastUidPoll = uidStats;
    }

    private NetworkStatsHistory findOrCreateNetworkStatsLocked(NetworkIdentitySet ident) {
        final long bucketDuration = mSettings.getNetworkBucketDuration();
        final NetworkStatsHistory existing = mNetworkStats.get(ident);

        // update when no existing, or when bucket duration changed
        NetworkStatsHistory updated = null;
        if (existing == null) {
            updated = new NetworkStatsHistory(bucketDuration, 10);
        } else if (existing.bucketDuration != bucketDuration) {
            updated = new NetworkStatsHistory(
                    bucketDuration, estimateResizeBuckets(existing, bucketDuration));
            updated.recordEntireHistory(existing);
        }

        if (updated != null) {
            mNetworkStats.put(ident, updated);
            return updated;
        } else {
            return existing;
        }
    }

    private NetworkStatsHistory findOrCreateUidStatsLocked(NetworkIdentitySet ident, int uid) {
        // find bucket for identity first, then find uid
        SparseArray<NetworkStatsHistory> uidStats = mUidStats.get(ident);
        if (uidStats == null) {
            uidStats = new SparseArray<NetworkStatsHistory>();
            mUidStats.put(ident, uidStats);
        }

        final long bucketDuration = mSettings.getUidBucketDuration();
        final NetworkStatsHistory existing = uidStats.get(uid);

        // update when no existing, or when bucket duration changed
        NetworkStatsHistory updated = null;
        if (existing == null) {
            updated = new NetworkStatsHistory(bucketDuration, 10);
        } else if (existing.bucketDuration != bucketDuration) {
            updated = new NetworkStatsHistory(
                    bucketDuration, estimateResizeBuckets(existing, bucketDuration));
            updated.recordEntireHistory(existing);
        }

        if (updated != null) {
            uidStats.put(uid, updated);
            return updated;
        } else {
            return existing;
        }
    }

    private void readNetworkStatsLocked() {
        if (LOGV) Slog.v(TAG, "readNetworkStatsLocked()");

        // clear any existing stats and read from disk
        mNetworkStats.clear();

        FileInputStream fis = null;
        try {
            fis = mNetworkFile.openRead();
            final DataInputStream in = new DataInputStream(fis);

            // verify file magic header intact
            final int magic = in.readInt();
            if (magic != FILE_MAGIC) {
                throw new ProtocolException("unexpected magic: " + magic);
            }

            final int version = in.readInt();
            switch (version) {
                case VERSION_NETWORK_INIT: {
                    // network := size *(NetworkIdentitySet NetworkStatsHistory)
                    final int size = in.readInt();
                    for (int i = 0; i < size; i++) {
                        final NetworkIdentitySet ident = new NetworkIdentitySet(in);
                        final NetworkStatsHistory history = new NetworkStatsHistory(in);
                        mNetworkStats.put(ident, history);
                    }
                    break;
                }
                default: {
                    throw new ProtocolException("unexpected version: " + version);
                }
            }
        } catch (FileNotFoundException e) {
            // missing stats is okay, probably first boot
        } catch (IOException e) {
            Slog.e(TAG, "problem reading network stats", e);
        } finally {
            IoUtils.closeQuietly(fis);
        }
    }

    private void ensureUidStatsLoadedLocked() {
        if (!mUidStatsLoaded) {
            readUidStatsLocked();
            mUidStatsLoaded = true;
        }
    }

    private void readUidStatsLocked() {
        if (LOGV) Slog.v(TAG, "readUidStatsLocked()");

        // clear any existing stats and read from disk
        mUidStats.clear();

        FileInputStream fis = null;
        try {
            fis = mUidFile.openRead();
            final DataInputStream in = new DataInputStream(fis);

            // verify file magic header intact
            final int magic = in.readInt();
            if (magic != FILE_MAGIC) {
                throw new ProtocolException("unexpected magic: " + magic);
            }

            final int version = in.readInt();
            switch (version) {
                case VERSION_UID_INIT: {
                    // uid := size *(UID NetworkStatsHistory)

                    // drop this data version, since we don't have a good
                    // mapping into NetworkIdentitySet.
                    break;
                }
                case VERSION_UID_WITH_IDENT: {
                    // uid := size *(NetworkIdentitySet size *(UID NetworkStatsHistory))
                    final int ifaceSize = in.readInt();
                    for (int i = 0; i < ifaceSize; i++) {
                        final NetworkIdentitySet ident = new NetworkIdentitySet(in);

                        final int uidSize = in.readInt();
                        final SparseArray<NetworkStatsHistory> uidStats = new SparseArray<
                                NetworkStatsHistory>(uidSize);
                        for (int j = 0; j < uidSize; j++) {
                            final int uid = in.readInt();
                            final NetworkStatsHistory history = new NetworkStatsHistory(in);
                            uidStats.put(uid, history);
                        }

                        mUidStats.put(ident, uidStats);
                    }
                    break;
                }
                default: {
                    throw new ProtocolException("unexpected version: " + version);
                }
            }
        } catch (FileNotFoundException e) {
            // missing stats is okay, probably first boot
        } catch (IOException e) {
            Slog.e(TAG, "problem reading uid stats", e);
        } finally {
            IoUtils.closeQuietly(fis);
        }
    }

    private void writeNetworkStatsLocked() {
        if (LOGV) Slog.v(TAG, "writeNetworkStatsLocked()");

        // TODO: consider duplicating stats and releasing lock while writing

        FileOutputStream fos = null;
        try {
            fos = mNetworkFile.startWrite();
            final DataOutputStream out = new DataOutputStream(fos);

            out.writeInt(FILE_MAGIC);
            out.writeInt(VERSION_NETWORK_INIT);

            out.writeInt(mNetworkStats.size());
            for (NetworkIdentitySet ident : mNetworkStats.keySet()) {
                final NetworkStatsHistory history = mNetworkStats.get(ident);
                ident.writeToStream(out);
                history.writeToStream(out);
            }

            mNetworkFile.finishWrite(fos);
        } catch (IOException e) {
            if (fos != null) {
                mNetworkFile.failWrite(fos);
            }
        }
    }

    private void writeUidStatsLocked() {
        if (LOGV) Slog.v(TAG, "writeUidStatsLocked()");

        // TODO: consider duplicating stats and releasing lock while writing

        FileOutputStream fos = null;
        try {
            fos = mUidFile.startWrite();
            final DataOutputStream out = new DataOutputStream(fos);

            out.writeInt(FILE_MAGIC);
            out.writeInt(VERSION_UID_WITH_IDENT);

            out.writeInt(mUidStats.size());
            for (NetworkIdentitySet ident : mUidStats.keySet()) {
                final SparseArray<NetworkStatsHistory> uidStats = mUidStats.get(ident);
                ident.writeToStream(out);

                final int size = uidStats.size();
                out.writeInt(size);
                for (int i = 0; i < size; i++) {
                    final int uid = uidStats.keyAt(i);
                    final NetworkStatsHistory history = uidStats.valueAt(i);
                    out.writeInt(uid);
                    history.writeToStream(out);
                }
            }

            mUidFile.finishWrite(fos);
        } catch (IOException e) {
            if (fos != null) {
                mUidFile.failWrite(fos);
            }
        }
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        mContext.enforceCallingOrSelfPermission(DUMP, TAG);

        final HashSet<String> argSet = new HashSet<String>();
        for (String arg : args) {
            argSet.add(arg);
        }

        synchronized (mStatsLock) {
            // TODO: remove this testing code, since it corrupts stats
            if (argSet.contains("generate")) {
                generateRandomLocked();
                pw.println("Generated stub stats");
                return;
            }

            if (argSet.contains("poll")) {
                performPollLocked(true, true);
                pw.println("Forced poll");
                return;
            }

            pw.println("Active interfaces:");
            for (String iface : mActiveIfaces.keySet()) {
                final NetworkIdentitySet ident = mActiveIfaces.get(iface);
                pw.print("  iface="); pw.print(iface);
                pw.print(" ident="); pw.println(ident.toString());
            }

            pw.println("Known historical stats:");
            for (NetworkIdentitySet ident : mNetworkStats.keySet()) {
                final NetworkStatsHistory history = mNetworkStats.get(ident);
                pw.print("  ident="); pw.println(ident.toString());
                history.dump("  ", pw);
            }

            if (argSet.contains("detail")) {
                // since explicitly requested with argument, we're okay to load
                // from disk if not already in memory.
                ensureUidStatsLoadedLocked();

                pw.println("Detailed UID stats:");
                for (NetworkIdentitySet ident : mUidStats.keySet()) {
                    pw.print("  ident="); pw.println(ident.toString());

                    final SparseArray<NetworkStatsHistory> uidStats = mUidStats.get(ident);
                    for (int i = 0; i < uidStats.size(); i++) {
                        final int uid = uidStats.keyAt(i);
                        final NetworkStatsHistory history = uidStats.valueAt(i);
                        pw.print("    UID="); pw.println(uid);
                        history.dump("    ", pw);
                    }
                }
            }
        }
    }

    /**
     * @deprecated only for temporary testing
     */
    @Deprecated
    private void generateRandomLocked() {
        long networkEnd = System.currentTimeMillis();
        long networkStart = networkEnd - mSettings.getNetworkMaxHistory();
        long networkRx = 3 * GB_IN_BYTES;
        long networkTx = 2 * GB_IN_BYTES;

        long uidEnd = System.currentTimeMillis();
        long uidStart = uidEnd - mSettings.getUidMaxHistory();
        long uidRx = 500 * MB_IN_BYTES;
        long uidTx = 100 * MB_IN_BYTES;

        final List<ApplicationInfo> installedApps = mContext
                .getPackageManager().getInstalledApplications(0);

        mNetworkStats.clear();
        mUidStats.clear();
        for (NetworkIdentitySet ident : mActiveIfaces.values()) {
            findOrCreateNetworkStatsLocked(ident).generateRandom(
                    networkStart, networkEnd, networkRx, networkTx);

            for (ApplicationInfo info : installedApps) {
                final int uid = info.uid;
                findOrCreateUidStatsLocked(ident, uid).generateRandom(
                        uidStart, uidEnd, uidRx, uidTx);
            }
        }
    }

    /**
     * Return the delta between two {@link NetworkStats} snapshots, where {@code
     * before} can be {@code null}.
     */
    private static NetworkStats computeStatsDelta(NetworkStats before, NetworkStats current) {
        if (before != null) {
            return current.subtractClamped(before);
        } else {
            return current;
        }
    }

    private int estimateNetworkBuckets() {
        return (int) (mSettings.getNetworkMaxHistory() / mSettings.getNetworkBucketDuration());
    }

    private int estimateUidBuckets() {
        return (int) (mSettings.getUidMaxHistory() / mSettings.getUidBucketDuration());
    }

    private static int estimateResizeBuckets(NetworkStatsHistory existing, long newBucketDuration) {
        return (int) (existing.bucketCount * existing.bucketDuration / newBucketDuration);
    }

    /**
     * Test if given {@link NetworkTemplate} matches any {@link NetworkIdentity}
     * in the given {@link NetworkIdentitySet}.
     */
    private static boolean templateMatches(NetworkTemplate template, NetworkIdentitySet identSet) {
        for (NetworkIdentity ident : identSet) {
            if (template.matches(ident)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Default external settings that read from {@link Settings.Secure}.
     */
    private static class DefaultNetworkStatsSettings implements NetworkStatsSettings {
        private final ContentResolver mResolver;

        public DefaultNetworkStatsSettings(Context context) {
            mResolver = checkNotNull(context.getContentResolver());
            // TODO: adjust these timings for production builds
        }

        private long getSecureLong(String name, long def) {
            return Settings.Secure.getLong(mResolver, name, def);
        }

        public long getPollInterval() {
            return getSecureLong(NETSTATS_POLL_INTERVAL, 15 * MINUTE_IN_MILLIS);
        }
        public long getPersistThreshold() {
            return getSecureLong(NETSTATS_PERSIST_THRESHOLD, 16 * KB_IN_BYTES);
        }
        public long getNetworkBucketDuration() {
            return getSecureLong(NETSTATS_NETWORK_BUCKET_DURATION, HOUR_IN_MILLIS);
        }
        public long getNetworkMaxHistory() {
            return getSecureLong(NETSTATS_NETWORK_MAX_HISTORY, 90 * DAY_IN_MILLIS);
        }
        public long getUidBucketDuration() {
            return getSecureLong(NETSTATS_UID_BUCKET_DURATION, 2 * HOUR_IN_MILLIS);
        }
        public long getUidMaxHistory() {
            return getSecureLong(NETSTATS_UID_MAX_HISTORY, 90 * DAY_IN_MILLIS);
        }
        public long getTimeCacheMaxAge() {
            return DAY_IN_MILLIS;
        }
    }

}