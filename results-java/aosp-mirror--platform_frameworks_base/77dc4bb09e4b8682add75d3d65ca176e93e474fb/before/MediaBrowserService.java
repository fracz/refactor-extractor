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

package android.service.media;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SdkConstant;
import android.annotation.SdkConstant.SdkConstantType;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.media.browse.MediaBrowser;
import android.media.browse.MediaBrowserUtils;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.service.media.IMediaBrowserService;
import android.service.media.IMediaBrowserServiceCallbacks;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Base class for media browse services.
 * <p>
 * Media browse services enable applications to browse media content provided by an application
 * and ask the application to start playing it. They may also be used to control content that
 * is already playing by way of a {@link MediaSession}.
 * </p>
 *
 * To extend this class, you must declare the service in your manifest file with
 * an intent filter with the {@link #SERVICE_INTERFACE} action.
 *
 * For example:
 * </p><pre>
 * &lt;service android:name=".MyMediaBrowserService"
 *          android:label="&#64;string/service_name" >
 *     &lt;intent-filter>
 *         &lt;action android:name="android.media.browse.MediaBrowserService" />
 *     &lt;/intent-filter>
 * &lt;/service>
 * </pre>
 *
 */
public abstract class MediaBrowserService extends Service {
    private static final String TAG = "MediaBrowserService";
    private static final boolean DBG = false;

    /**
     * The {@link Intent} that must be declared as handled by the service.
     */
    @SdkConstant(SdkConstantType.SERVICE_ACTION)
    public static final String SERVICE_INTERFACE = "android.media.browse.MediaBrowserService";

    /**
     * A key for passing the MediaItem to the ResultReceiver in getItem.
     *
     * @hide
     */
    public static final String KEY_MEDIA_ITEM = "media_item";

    private static final int RESULT_FLAG_OPTION_NOT_HANDLED = 0x00000001;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag=true, value = { RESULT_FLAG_OPTION_NOT_HANDLED })
    private @interface ResultFlags { }

    private final ArrayMap<IBinder, ConnectionRecord> mConnections = new ArrayMap<>();
    private final Handler mHandler = new Handler();
    private ServiceBinder mBinder;
    MediaSession.Token mSession;

    /**
     * All the info about a connection.
     */
    private class ConnectionRecord {
        String pkg;
        Bundle rootHints;
        IMediaBrowserServiceCallbacks callbacks;
        BrowserRoot root;
        HashMap<String, List<Bundle>> subscriptions = new HashMap<>();
    }

    /**
     * Completion handler for asynchronous callback methods in {@link MediaBrowserService}.
     * <p>
     * Each of the methods that takes one of these to send the result must call
     * {@link #sendResult} to respond to the caller with the given results. If those
     * functions return without calling {@link #sendResult}, they must instead call
     * {@link #detach} before returning, and then may call {@link #sendResult} when
     * they are done. If more than one of those methods is called, an exception will
     * be thrown.
     *
     * @see MediaBrowserService#onLoadChildren
     * @see MediaBrowserService#onGetMediaItem
     */
    public class Result<T> {
        private Object mDebug;
        private boolean mDetachCalled;
        private boolean mSendResultCalled;
        private int mFlags;

        Result(Object debug) {
            mDebug = debug;
        }

        /**
         * Send the result back to the caller.
         */
        public void sendResult(T result) {
            if (mSendResultCalled) {
                throw new IllegalStateException("sendResult() called twice for: " + mDebug);
            }
            mSendResultCalled = true;
            onResultSent(result, mFlags);
        }

        /**
         * Detach this message from the current thread and allow the {@link #sendResult}
         * call to happen later.
         */
        public void detach() {
            if (mDetachCalled) {
                throw new IllegalStateException("detach() called when detach() had already"
                        + " been called for: " + mDebug);
            }
            if (mSendResultCalled) {
                throw new IllegalStateException("detach() called when sendResult() had already"
                        + " been called for: " + mDebug);
            }
            mDetachCalled = true;
        }

        boolean isDone() {
            return mDetachCalled || mSendResultCalled;
        }

        void setFlags(@ResultFlags int flags) {
            mFlags = flags;
        }

        /**
         * Called when the result is sent, after assertions about not being called twice
         * have happened.
         */
        void onResultSent(T result, @ResultFlags int flags) {
        }
    }

    private class ServiceBinder extends IMediaBrowserService.Stub {
        @Override
        public void connect(final String pkg, final Bundle rootHints,
                final IMediaBrowserServiceCallbacks callbacks) {

            final int uid = Binder.getCallingUid();
            if (!isValidPackage(pkg, uid)) {
                throw new IllegalArgumentException("Package/uid mismatch: uid=" + uid
                        + " package=" + pkg);
            }

            mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final IBinder b = callbacks.asBinder();

                        // Clear out the old subscriptions. We are getting new ones.
                        mConnections.remove(b);

                        final ConnectionRecord connection = new ConnectionRecord();
                        connection.pkg = pkg;
                        connection.rootHints = rootHints;
                        connection.callbacks = callbacks;

                        connection.root = MediaBrowserService.this.onGetRoot(pkg, uid, rootHints);

                        // If they didn't return something, don't allow this client.
                        if (connection.root == null) {
                            Log.i(TAG, "No root for client " + pkg + " from service "
                                    + getClass().getName());
                            try {
                                callbacks.onConnectFailed();
                            } catch (RemoteException ex) {
                                Log.w(TAG, "Calling onConnectFailed() failed. Ignoring. "
                                        + "pkg=" + pkg);
                            }
                        } else {
                            try {
                                mConnections.put(b, connection);
                                if (mSession != null) {
                                    callbacks.onConnect(connection.root.getRootId(),
                                            mSession, connection.root.getExtras());
                                }
                            } catch (RemoteException ex) {
                                Log.w(TAG, "Calling onConnect() failed. Dropping client. "
                                        + "pkg=" + pkg);
                                mConnections.remove(b);
                            }
                        }
                    }
                });
        }

        @Override
        public void disconnect(final IMediaBrowserServiceCallbacks callbacks) {
            mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final IBinder b = callbacks.asBinder();

                        // Clear out the old subscriptions. We are getting new ones.
                        final ConnectionRecord old = mConnections.remove(b);
                        if (old != null) {
                            // TODO
                        }
                    }
                });
        }

        @Override
        public void addSubscription(final String id,
                final IMediaBrowserServiceCallbacks callbacks) {
            addSubscriptionWithOptions(id, null, callbacks);
        }

        @Override
        public void addSubscriptionWithOptions(final String id, final Bundle options,
                final IMediaBrowserServiceCallbacks callbacks) {
            mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final IBinder b = callbacks.asBinder();

                        // Get the record for the connection
                        final ConnectionRecord connection = mConnections.get(b);
                        if (connection == null) {
                            Log.w(TAG, "addSubscription for callback that isn't registered id="
                                + id);
                            return;
                        }

                        MediaBrowserService.this.addSubscription(id, connection, options);
                    }
                });
        }

        @Override
        public void removeSubscription(final String id,
                final IMediaBrowserServiceCallbacks callbacks) {
            removeSubscriptionWithOptions(id, null, callbacks);
        }

        @Override
        public void removeSubscriptionWithOptions(final String id, final Bundle options,
                final IMediaBrowserServiceCallbacks callbacks) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final IBinder b = callbacks.asBinder();

                    ConnectionRecord connection = mConnections.get(b);
                    if (connection == null) {
                        Log.w(TAG, "removeSubscription for callback that isn't registered id="
                                + id);
                        return;
                    }
                    if (!MediaBrowserService.this.removeSubscription(id, connection, options)) {
                        Log.w(TAG, "removeSubscription called for " + id
                                + " which is not subscribed");
                    }
                }
            });
        }

        @Override
        public void getMediaItem(final String mediaId, final ResultReceiver receiver) {
            if (TextUtils.isEmpty(mediaId) || receiver == null) {
                return;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    performLoadItem(mediaId, receiver);
                }
            });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new ServiceBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return mBinder;
        }
        return null;
    }

    @Override
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    /**
     * Called to get the root information for browsing by a particular client.
     * <p>
     * The implementation should verify that the client package has permission
     * to access browse media information before returning the root id; it
     * should return null if the client is not allowed to access this
     * information.
     * </p>
     *
     * @param clientPackageName The package name of the application which is
     *            requesting access to browse media.
     * @param clientUid The uid of the application which is requesting access to
     *            browse media.
     * @param rootHints An optional bundle of service-specific arguments to send
     *            to the media browse service when connecting and retrieving the
     *            root id for browsing, or null if none. The contents of this
     *            bundle may affect the information returned when browsing.
     * @return The {@link BrowserRoot} for accessing this app's content or null.
     * @see BrowserRoot#EXTRA_RECENT
     * @see BrowserRoot#EXTRA_OFFLINE
     * @see BrowserRoot#EXTRA_SUGGESTED
     */
    public abstract @Nullable BrowserRoot onGetRoot(@NonNull String clientPackageName,
            int clientUid, @Nullable Bundle rootHints);

    /**
     * Called to get information about the children of a media item.
     * <p>
     * Implementations must call {@link Result#sendResult result.sendResult}
     * with the list of children. If loading the children will be an expensive
     * operation that should be performed on another thread,
     * {@link Result#detach result.detach} may be called before returning from
     * this function, and then {@link Result#sendResult result.sendResult}
     * called when the loading is complete.
     *
     * @param parentId The id of the parent media item whose children are to be
     *            queried.
     * @param result The Result to send the list of children to, or null if the
     *            id is invalid.
     */
    public abstract void onLoadChildren(@NonNull String parentId,
            @NonNull Result<List<MediaBrowser.MediaItem>> result);

    /**
     * Called to get information about the children of a media item.
     * <p>
     * Implementations must call {@link Result#sendResult result.sendResult}
     * with the list of children. If loading the children will be an expensive
     * operation that should be performed on another thread,
     * {@link Result#detach result.detach} may be called before returning from
     * this function, and then {@link Result#sendResult result.sendResult}
     * called when the loading is complete.
     *
     * @param parentId The id of the parent media item whose children are to be
     *            queried.
     * @param result The Result to send the list of children to, or null if the
     *            id is invalid.
     * @param options A bundle of service-specific arguments sent from the media
     *            browse. The information returned through the result should be
     *            affected by the contents of this bundle.
     */
    public void onLoadChildren(@NonNull String parentId,
            @NonNull Result<List<MediaBrowser.MediaItem>> result, @NonNull Bundle options) {
        // To support backward compatibility, when the implementation of MediaBrowserService doesn't
        // override onLoadChildren() with options, onLoadChildren() without options will be used
        // instead, and the options will be applied in the implementation of result.onResultSent().
        result.setFlags(RESULT_FLAG_OPTION_NOT_HANDLED);
        onLoadChildren(parentId, result);
    }

    /**
     * Called to get information about a specific media item.
     * <p>
     * Implementations must call {@link Result#sendResult result.sendResult}. If
     * loading the item will be an expensive operation {@link Result#detach
     * result.detach} may be called before returning from this function, and
     * then {@link Result#sendResult result.sendResult} called when the item has
     * been loaded.
     * <p>
     * The default implementation sends a null result.
     *
     * @param itemId The id for the specific
     *            {@link android.media.browse.MediaBrowser.MediaItem}.
     * @param result The Result to send the item to, or null if the id is
     *            invalid.
     */
    public void onLoadItem(String itemId, Result<MediaBrowser.MediaItem> result) {
        result.sendResult(null);
    }

    /**
     * Call to set the media session.
     * <p>
     * This should be called as soon as possible during the service's startup.
     * It may only be called once.
     *
     * @param token The token for the service's {@link MediaSession}.
     */
    public void setSessionToken(final MediaSession.Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Session token may not be null.");
        }
        if (mSession != null) {
            throw new IllegalStateException("The session token has already been set.");
        }
        mSession = token;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (IBinder key : mConnections.keySet()) {
                    ConnectionRecord connection = mConnections.get(key);
                    try {
                        connection.callbacks.onConnect(connection.root.getRootId(), token,
                                connection.root.getExtras());
                    } catch (RemoteException e) {
                        Log.w(TAG, "Connection for " + connection.pkg + " is no longer valid.");
                        mConnections.remove(key);
                    }
                }
            }
        });
    }

    /**
     * Gets the session token, or null if it has not yet been created
     * or if it has been destroyed.
     */
    public @Nullable MediaSession.Token getSessionToken() {
        return mSession;
    }

    /**
     * Notifies all connected media browsers that the children of
     * the specified parent id have changed in some way.
     * This will cause browsers to fetch subscribed content again.
     *
     * @param parentId The id of the parent media item whose
     * children changed.
     */
    public void notifyChildrenChanged(@NonNull String parentId) {
        notifyChildrenChangedInternal(parentId, null);
    }

    /**
     * Notifies all connected media browsers that the children of
     * the specified parent id have changed in some way.
     * This will cause browsers to fetch subscribed content again.
     *
     * @param parentId The id of the parent media item whose
     *            children changed.
     * @param options A bundle of service-specific arguments to send
     *            to the media browse. The contents of this bundle may
     *            contain the information about the change.
     */
    public void notifyChildrenChanged(@NonNull String parentId, @NonNull Bundle options) {
        if (options == null) {
            throw new IllegalArgumentException("options cannot be null in notifyChildrenChanged");
        }
        notifyChildrenChangedInternal(parentId, options);
    }

    private void notifyChildrenChangedInternal(final String parentId, final Bundle options) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (IBinder binder : mConnections.keySet()) {
                    ConnectionRecord connection = mConnections.get(binder);
                    List<Bundle> optionsList = connection.subscriptions.get(parentId);
                    if (optionsList != null) {
                        for (Bundle bundle : optionsList) {
                            if (MediaBrowserUtils.hasDuplicatedItems(options, bundle)) {
                                performLoadChildren(parentId, connection, bundle);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Return whether the given package is one of the ones that is owned by the uid.
     */
    private boolean isValidPackage(String pkg, int uid) {
        if (pkg == null) {
            return false;
        }
        final PackageManager pm = getPackageManager();
        final String[] packages = pm.getPackagesForUid(uid);
        final int N = packages.length;
        for (int i=0; i<N; i++) {
            if (packages[i].equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Save the subscription and if it is a new subscription send the results.
     */
    private void addSubscription(String id, ConnectionRecord connection, Bundle options) {
        // Save the subscription
        List<Bundle> optionsList = connection.subscriptions.get(id);
        if (optionsList == null) {
            optionsList = new ArrayList<>();
        }
        for (Bundle bundle : optionsList) {
            if (MediaBrowserUtils.areSameOptions(options, bundle)) {
                return;
            }
        }
        optionsList.add(options);
        connection.subscriptions.put(id, optionsList);
        // send the results
        performLoadChildren(id, connection, options);
    }

    /**
     * Remove the subscription.
     */
    private boolean removeSubscription(String id, ConnectionRecord connection, Bundle options) {
        boolean removed = false;
        List<Bundle> optionsList = connection.subscriptions.get(id);
        if (optionsList != null) {
            for (Bundle bundle : optionsList) {
                if (MediaBrowserUtils.areSameOptions(options, bundle)) {
                    removed = true;
                    optionsList.remove(bundle);
                    break;
                }
            }
            if (optionsList.size() == 0) {
                connection.subscriptions.remove(id);
            }
        }
        return removed;
    }

    /**
     * Call onLoadChildren and then send the results back to the connection.
     * <p>
     * Callers must make sure that this connection is still connected.
     */
    private void performLoadChildren(final String parentId, final ConnectionRecord connection,
            final Bundle options) {
        final Result<List<MediaBrowser.MediaItem>> result
                = new Result<List<MediaBrowser.MediaItem>>(parentId) {
            @Override
            void onResultSent(List<MediaBrowser.MediaItem> list, @ResultFlags int flag) {
                if (mConnections.get(connection.callbacks.asBinder()) != connection) {
                    if (DBG) {
                        Log.d(TAG, "Not sending onLoadChildren result for connection that has"
                                + " been disconnected. pkg=" + connection.pkg + " id=" + parentId);
                    }
                    return;
                }

                List<MediaBrowser.MediaItem> filteredList =
                        (flag & RESULT_FLAG_OPTION_NOT_HANDLED) != 0
                        ? applyOptions(list, options) : list;
                final ParceledListSlice<MediaBrowser.MediaItem> pls =
                        filteredList == null ? null : new ParceledListSlice<>(filteredList);
                try {
                    // NOTE: Do not call onLoadChildrenWithOptions when options are null. Otherwise,
                    // it will break the action of support library which expects onLoadChildren will
                    // be called when options are null.
                    if (options == null) {
                        connection.callbacks.onLoadChildren(parentId, pls);
                    } else {
                        connection.callbacks.onLoadChildrenWithOptions(parentId, pls, options);
                    }
                } catch (RemoteException ex) {
                    // The other side is in the process of crashing.
                    Log.w(TAG, "Calling onLoadChildren() failed for id=" + parentId
                            + " package=" + connection.pkg);
                }
            }
        };

        if (options == null) {
            onLoadChildren(parentId, result);
        } else {
            onLoadChildren(parentId, result, options);
        }

        if (!result.isDone()) {
            throw new IllegalStateException("onLoadChildren must call detach() or sendResult()"
                    + " before returning for package=" + connection.pkg + " id=" + parentId);
        }
    }

    private List<MediaBrowser.MediaItem> applyOptions(List<MediaBrowser.MediaItem> list,
            final Bundle options) {
        int page = options.getInt(MediaBrowser.EXTRA_PAGE, -1);
        int pageSize = options.getInt(MediaBrowser.EXTRA_PAGE_SIZE, -1);
        if (page == -1 && pageSize == -1) {
            return list;
        }
        int fromIndex = pageSize * (page - 1);
        int toIndex = fromIndex + pageSize;
        if (page < 1 || pageSize < 1 || fromIndex >= list.size()) {
            return null;
        }
        if (toIndex > list.size()) {
            toIndex = list.size();
        }
        return list.subList(fromIndex, toIndex);
    }

    private void performLoadItem(String itemId, final ResultReceiver receiver) {
        final Result<MediaBrowser.MediaItem> result =
                new Result<MediaBrowser.MediaItem>(itemId) {
            @Override
            void onResultSent(MediaBrowser.MediaItem item, @ResultFlags int flag) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_MEDIA_ITEM, item);
                receiver.send(0, bundle);
            }
        };

        MediaBrowserService.this.onLoadItem(itemId, result);

        if (!result.isDone()) {
            throw new IllegalStateException("onLoadItem must call detach() or sendResult()"
                    + " before returning for id=" + itemId);
        }
    }

    /**
     * Contains information that the browser service needs to send to the client
     * when first connected.
     */
    public static final class BrowserRoot {
        /**
         * The lookup key for a boolean that indicates whether the browser service should return a
         * browser root for recently played media items.
         *
         * <p>When creating a media browser for a given media browser service, this key can be
         * supplied as a root hint for retrieving media items that are recently played.
         * If the media browser service can provide such media items, the implementation must return
         * the key in the root hint when {@link #onGetRoot(String, int, Bundle)} is called back.
         *
         * <p>The root hint may contain multiple keys.
         *
         * @see #EXTRA_OFFLINE
         * @see #EXTRA_SUGGESTED
         */
        public static final String EXTRA_RECENT = "android.service.media.extra.RECENT";

        /**
         * The lookup key for a boolean that indicates whether the browser service should return a
         * browser root for offline media items.
         *
         * <p>When creating a media browser for a given media browser service, this key can be
         * supplied as a root hint for retrieving media items that are can be played without an
         * internet connection.
         * If the media browser service can provide such media items, the implementation must return
         * the key in the root hint when {@link #onGetRoot(String, int, Bundle)} is called back.
         *
         * <p>The root hint may contain multiple keys.
         *
         * @see #EXTRA_RECENT
         * @see #EXTRA_SUGGESTED
         */
        public static final String EXTRA_OFFLINE = "android.service.media.extra.OFFLINE";

        /**
         * The lookup key for a boolean that indicates whether the browser service should return a
         * browser root for suggested media items.
         *
         * <p>When creating a media browser for a given media browser service, this key can be
         * supplied as a root hint for retrieving the media items suggested by the media browser
         * service. The list of media items passed in {@link android.media.browse.MediaBrowser.SubscriptionCallback#onChildrenLoaded(String, List)}
         * is considered ordered by relevance, first being the top suggestion.
         * If the media browser service can provide such media items, the implementation must return
         * the key in the root hint when {@link #onGetRoot(String, int, Bundle)} is called back.
         *
         * <p>The root hint may contain multiple keys.
         *
         * @see #EXTRA_RECENT
         * @see #EXTRA_OFFLINE
         */
        public static final String EXTRA_SUGGESTED = "android.service.media.extra.SUGGESTED";

        final private String mRootId;
        final private Bundle mExtras;

        /**
         * Constructs a browser root.
         * @param rootId The root id for browsing.
         * @param extras Any extras about the browser service.
         */
        public BrowserRoot(@NonNull String rootId, @Nullable Bundle extras) {
            if (rootId == null) {
                throw new IllegalArgumentException("The root id in BrowserRoot cannot be null. " +
                        "Use null for BrowserRoot instead.");
            }
            mRootId = rootId;
            mExtras = extras;
        }

        /**
         * Gets the root id for browsing.
         */
        public String getRootId() {
            return mRootId;
        }

        /**
         * Gets any extras about the browser service.
         */
        public Bundle getExtras() {
            return mExtras;
        }
    }
}