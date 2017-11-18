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

package android.net;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.Downloads;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The download manager is a system service that handles long-running HTTP downloads. Clients may
 * request that a URI be downloaded to a particular destination file. The download manager will
 * conduct the download in the background, taking care of HTTP interactions and retrying downloads
 * after failures or across connectivity changes and system reboots.
 *
 * Instances of this class should be obtained through
 * {@link android.content.Context#getSystemService(String)} by passing
 * {@link android.content.Context#DOWNLOAD_SERVICE}.
 */
public class DownloadManager {
    /**
     * An identifier for a particular download, unique across the system.  Clients use this ID to
     * make subsequent calls related to the download.
     */
    public final static String COLUMN_ID = BaseColumns._ID;

    /**
     * The client-supplied title for this download.  This will be displayed in system notifications.
     * Defaults to the empty string.
     */
    public final static String COLUMN_TITLE = "title";

    /**
     * The client-supplied description of this download.  This will be displayed in system
     * notifications.  Defaults to the empty string.
     */
    public final static String COLUMN_DESCRIPTION = "description";

    /**
     * URI to be downloaded.
     */
    public final static String COLUMN_URI = "uri";

    /**
     * Internet Media Type of the downloaded file.  If no value is provided upon creation, this will
     * initially be null and will be filled in based on the server's response once the download has
     * started.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1590.txt">RFC 1590, defining Media Types</a>
     */
    public final static String COLUMN_MEDIA_TYPE = "media_type";

    /**
     * Total size of the download in bytes.  This will initially be -1 and will be filled in once
     * the download starts.
     */
    public final static String COLUMN_TOTAL_SIZE_BYTES = "total_size";

    /**
     * Uri where downloaded file will be stored.  If a destination is supplied by client, that URI
     * will be used here.  Otherwise, the value will initially be null and will be filled in with a
     * generated URI once the download has started.
     */
    public final static String COLUMN_LOCAL_URI = "local_uri";

    /**
     * Current status of the download, as one of the STATUS_* constants.
     */
    public final static String COLUMN_STATUS = "status";

    /**
     * Indicates the type of error that occurred, when {@link #COLUMN_STATUS} is
     * {@link #STATUS_FAILED}.  If an HTTP error occurred, this will hold the HTTP status code as
     * defined in RFC 2616.  Otherwise, it will hold one of the ERROR_* constants.
     *
     * If {@link #COLUMN_STATUS} is not {@link #STATUS_FAILED}, this column's value is undefined.
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1.1">RFC 2616
     * status codes</a>
     */
    public final static String COLUMN_ERROR_CODE = "error_code";

    /**
     * Number of bytes download so far.
     */
    public final static String COLUMN_BYTES_DOWNLOADED_SO_FAR = "bytes_so_far";

    /**
     * Timestamp when the download was last modified, in {@link System#currentTimeMillis
     * System.currentTimeMillis()} (wall clock time in UTC).
     */
    public final static String COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified_timestamp";


    /**
     * Value of {@link #COLUMN_STATUS} when the download is waiting to start.
     */
    public final static int STATUS_PENDING = 1 << 0;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is currently running.
     */
    public final static int STATUS_RUNNING = 1 << 1;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is waiting to retry or resume.
     */
    public final static int STATUS_PAUSED = 1 << 2;

    /**
     * Value of {@link #COLUMN_STATUS} when the download has successfully completed.
     */
    public final static int STATUS_SUCCESSFUL = 1 << 3;

    /**
     * Value of {@link #COLUMN_STATUS} when the download has failed (and will not be retried).
     */
    public final static int STATUS_FAILED = 1 << 4;


    /**
     * Value of COLUMN_ERROR_CODE when the download has completed with an error that doesn't fit
     * under any other error code.
     */
    public final static int ERROR_UNKNOWN = 1000;

    /**
     * Value of {@link #COLUMN_ERROR_CODE} when a storage issue arises which doesn't fit under any
     * other error code. Use the more specific {@link #ERROR_INSUFFICIENT_SPACE} and
     * {@link #ERROR_DEVICE_NOT_FOUND} when appropriate.
     */
    public final static int ERROR_FILE_ERROR = 1001;

    /**
     * Value of {@link #COLUMN_ERROR_CODE} when an HTTP code was received that download manager
     * can't handle.
     */
    public final static int ERROR_UNHANDLED_HTTP_CODE = 1002;

    /**
     * Value of {@link #COLUMN_ERROR_CODE} when an error receiving or processing data occurred at
     * the HTTP level.
     */
    public final static int ERROR_HTTP_DATA_ERROR = 1004;

    /**
     * Value of {@link #COLUMN_ERROR_CODE} when there were too many redirects.
     */
    public final static int ERROR_TOO_MANY_REDIRECTS = 1005;

    /**
     * Value of {@link #COLUMN_ERROR_CODE} when there was insufficient storage space. Typically,
     * this is because the SD card is full.
     */
    public final static int ERROR_INSUFFICIENT_SPACE = 1006;

    /**
     * Value of {@link #COLUMN_ERROR_CODE} when no external storage device was found. Typically,
     * this is because the SD card is not mounted.
     */
    public final static int ERROR_DEVICE_NOT_FOUND = 1007;

    /**
     * Value of {@link #COLUMN_ERROR_CODE} when some possibly transient error occurred but we can't
     * resume the download.
     */
    public final static int ERROR_CANNOT_RESUME = 1008;

    /**
     * Value of {@link #COLUMN_ERROR_CODE} when the requested destination file already exists (the
     * download manager will not overwrite an existing file).
     */
    public final static int ERROR_FILE_ALREADY_EXISTS = 1009;

    /**
     * Broadcast intent action sent by the download manager when a download completes.
     */
    public final static String ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";

    /**
     * Broadcast intent action sent by the download manager when a running download notification is
     * clicked.
     */
    public final static String ACTION_NOTIFICATION_CLICKED =
            "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";

    /**
     * Intent extra included with {@link #ACTION_DOWNLOAD_COMPLETE} intents, indicating the ID (as a
     * long) of the download that just completed.
     */
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";

    // this array must contain all public columns
    private static final String[] COLUMNS = new String[] {
        COLUMN_ID,
        COLUMN_TITLE,
        COLUMN_DESCRIPTION,
        COLUMN_URI,
        COLUMN_MEDIA_TYPE,
        COLUMN_TOTAL_SIZE_BYTES,
        COLUMN_LOCAL_URI,
        COLUMN_STATUS,
        COLUMN_ERROR_CODE,
        COLUMN_BYTES_DOWNLOADED_SO_FAR,
        COLUMN_LAST_MODIFIED_TIMESTAMP
    };

    // columns to request from DownloadProvider
    private static final String[] UNDERLYING_COLUMNS = new String[] {
        Downloads.Impl._ID,
        Downloads.COLUMN_TITLE,
        Downloads.COLUMN_DESCRIPTION,
        Downloads.COLUMN_URI,
        Downloads.COLUMN_MIME_TYPE,
        Downloads.COLUMN_TOTAL_BYTES,
        Downloads.COLUMN_STATUS,
        Downloads.COLUMN_CURRENT_BYTES,
        Downloads.COLUMN_LAST_MODIFICATION,
        Downloads.COLUMN_DESTINATION,
        Downloads.Impl.COLUMN_FILE_NAME_HINT,
    };

    private static final Set<String> LONG_COLUMNS = new HashSet<String>(
            Arrays.asList(COLUMN_ID, COLUMN_TOTAL_SIZE_BYTES, COLUMN_STATUS, COLUMN_ERROR_CODE,
                          COLUMN_BYTES_DOWNLOADED_SO_FAR, COLUMN_LAST_MODIFIED_TIMESTAMP));

    /**
     * This class contains all the information necessary to request a new download.  The URI is the
     * only required parameter.
     */
    public static class Request {
        /**
         * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
         * {@link ConnectivityManager#TYPE_MOBILE}.
         */
        public static final int NETWORK_MOBILE = 1 << 0;

        /**
         * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
         * {@link ConnectivityManager#TYPE_WIFI}.
         */
        public static final int NETWORK_WIFI = 1 << 1;

        /**
         * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
         * {@link ConnectivityManager#TYPE_WIMAX}.
         */
        public static final int NETWORK_WIMAX = 1 << 2;

        private Uri mUri;
        private Uri mDestinationUri;
        private Map<String, String> mRequestHeaders = new HashMap<String, String>();
        private String mTitle;
        private String mDescription;
        private boolean mShowNotification = true;
        private String mMediaType;
        private boolean mRoamingAllowed = true;
        private int mAllowedNetworkTypes = ~0; // default to all network types allowed
        private boolean mIsVisibleInDownloadsUi = true;

        /**
         * @param uri the HTTP URI to download.
         */
        public Request(Uri uri) {
            if (uri == null) {
                throw new NullPointerException();
            }
            String scheme = uri.getScheme();
            if (scheme == null || !scheme.equals("http")) {
                throw new IllegalArgumentException("Can only download HTTP URIs: " + uri);
            }
            mUri = uri;
        }

        /**
         * Set the local destination for the downloaded data. Must be a file URI to a path on
         * external storage, and the calling application must have the WRITE_EXTERNAL_STORAGE
         * permission.
         *
         *  By default, downloads are saved to a generated file in the download cache and may be
         * deleted by the download manager at any time.
         *
         * @return this object
         */
        public Request setDestinationUri(Uri uri) {
            mDestinationUri = uri;
            return this;
        }

        /**
         * Set an HTTP header to be included with the download request.
         * @param header HTTP header name
         * @param value header value
         * @return this object
         */
        public Request setRequestHeader(String header, String value) {
            mRequestHeaders.put(header, value);
            return this;
        }

        /**
         * Set the title of this download, to be displayed in notifications (if enabled)
         * @return this object
         */
        public Request setTitle(String title) {
            mTitle = title;
            return this;
        }

        /**
         * Set a description of this download, to be displayed in notifications (if enabled)
         * @return this object
         */
        public Request setDescription(String description) {
            mDescription = description;
            return this;
        }

        /**
         * Set the Internet Media Type of this download.  This will override the media type declared
         * in the server's response.
         * @see <a href="http://www.ietf.org/rfc/rfc1590.txt">RFC 1590, defining Media Types</a>
         * @return this object
         */
        public Request setMediaType(String mediaType) {
            mMediaType = mediaType;
            return this;
        }

        /**
         * Control whether a system notification is posted by the download manager while this
         * download is running. If enabled, the download manager posts notifications about downloads
         * through the system {@link android.app.NotificationManager}. By default, a notification is
         * shown.
         *
         * If set to false, this requires the permission
         * android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.
         *
         * @param show whether the download manager should show a notification for this download.
         * @return this object
         */
        public Request setShowRunningNotification(boolean show) {
            mShowNotification = show;
            return this;
        }

        /**
         * Restrict the types of networks over which this download may proceed.  By default, all
         * network types are allowed.
         * @param flags any combination of the NETWORK_* bit flags.
         * @return this object
         */
        public Request setAllowedNetworkTypes(int flags) {
            mAllowedNetworkTypes = flags;
            return this;
        }

        /**
         * Set whether this download may proceed over a roaming connection.  By default, roaming is
         * allowed.
         * @param allowed whether to allow a roaming connection to be used
         * @return this object
         */
        public Request setAllowedOverRoaming(boolean allowed) {
            mRoamingAllowed = allowed;
            return this;
        }

        /**
         * Set whether this download should be displayed in the system's Downloads UI. True by
         * default.
         * @param isVisible whether to display this download in the Downloads UI
         * @return this object
         */
        public Request setVisibleInDownloadsUi(boolean isVisible) {
            mIsVisibleInDownloadsUi = isVisible;
            return this;
        }

        /**
         * @return ContentValues to be passed to DownloadProvider.insert()
         */
        ContentValues toContentValues(String packageName) {
            ContentValues values = new ContentValues();
            assert mUri != null;
            values.put(Downloads.COLUMN_URI, mUri.toString());
            values.put(Downloads.Impl.COLUMN_IS_PUBLIC_API, true);
            values.put(Downloads.COLUMN_NOTIFICATION_PACKAGE, packageName);

            if (mDestinationUri != null) {
                values.put(Downloads.COLUMN_DESTINATION, Downloads.Impl.DESTINATION_FILE_URI);
                values.put(Downloads.COLUMN_FILE_NAME_HINT, mDestinationUri.toString());
            } else {
                values.put(Downloads.COLUMN_DESTINATION,
                           Downloads.DESTINATION_CACHE_PARTITION_PURGEABLE);
            }

            if (!mRequestHeaders.isEmpty()) {
                encodeHttpHeaders(values);
            }

            putIfNonNull(values, Downloads.COLUMN_TITLE, mTitle);
            putIfNonNull(values, Downloads.COLUMN_DESCRIPTION, mDescription);
            putIfNonNull(values, Downloads.COLUMN_MIME_TYPE, mMediaType);

            values.put(Downloads.COLUMN_VISIBILITY,
                    mShowNotification ? Downloads.VISIBILITY_VISIBLE
                            : Downloads.VISIBILITY_HIDDEN);

            values.put(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, mAllowedNetworkTypes);
            values.put(Downloads.Impl.COLUMN_ALLOW_ROAMING, mRoamingAllowed);
            values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, mIsVisibleInDownloadsUi);

            return values;
        }

        private void encodeHttpHeaders(ContentValues values) {
            int index = 0;
            for (Map.Entry<String, String> entry : mRequestHeaders.entrySet()) {
                String headerString = entry.getKey() + ": " + entry.getValue();
                values.put(Downloads.Impl.RequestHeaders.INSERT_KEY_PREFIX + index, headerString);
                index++;
            }
        }

        private void putIfNonNull(ContentValues contentValues, String key, String value) {
            if (value != null) {
                contentValues.put(key, value);
            }
        }
    }

    /**
     * This class may be used to filter download manager queries.
     */
    public static class Query {
        /**
         * Constant for use with {@link #orderBy}
         * @hide
         */
        public static final int ORDER_ASCENDING = 1;

        /**
         * Constant for use with {@link #orderBy}
         * @hide
         */
        public static final int ORDER_DESCENDING = 2;

        private Long mId = null;
        private Integer mStatusFlags = null;
        private String mOrderByColumn = Downloads.COLUMN_LAST_MODIFICATION;
        private int mOrderDirection = ORDER_DESCENDING;
        private boolean mOnlyIncludeVisibleInDownloadsUi = false;

        /**
         * Include only the download with the given ID.
         * @return this object
         */
        public Query setFilterById(long id) {
            mId = id;
            return this;
        }

        /**
         * Include only downloads with status matching any the given status flags.
         * @param flags any combination of the STATUS_* bit flags
         * @return this object
         */
        public Query setFilterByStatus(int flags) {
            mStatusFlags = flags;
            return this;
        }

        /**
         * Controls whether this query includes downloads not visible in the system's Downloads UI.
         * @param value if true, this query will only include downloads that should be displayed in
         *            the system's Downloads UI; if false (the default), this query will include
         *            both visible and invisible downloads.
         * @return this object
         * @hide
         */
        public Query setOnlyIncludeVisibleInDownloadsUi(boolean value) {
            mOnlyIncludeVisibleInDownloadsUi = value;
            return this;
        }

        /**
         * Change the sort order of the returned Cursor.
         *
         * @param column one of the COLUMN_* constants; currently, only
         *         {@link #COLUMN_LAST_MODIFIED_TIMESTAMP} and {@link #COLUMN_TOTAL_SIZE_BYTES} are
         *         supported.
         * @param direction either {@link #ORDER_ASCENDING} or {@link #ORDER_DESCENDING}
         * @return this object
         * @hide
         */
        public Query orderBy(String column, int direction) {
            if (direction != ORDER_ASCENDING && direction != ORDER_DESCENDING) {
                throw new IllegalArgumentException("Invalid direction: " + direction);
            }

            if (column.equals(COLUMN_LAST_MODIFIED_TIMESTAMP)) {
                mOrderByColumn = Downloads.COLUMN_LAST_MODIFICATION;
            } else if (column.equals(COLUMN_TOTAL_SIZE_BYTES)) {
                mOrderByColumn = Downloads.COLUMN_TOTAL_BYTES;
            } else {
                throw new IllegalArgumentException("Cannot order by " + column);
            }
            mOrderDirection = direction;
            return this;
        }

        /**
         * Run this query using the given ContentResolver.
         * @param projection the projection to pass to ContentResolver.query()
         * @return the Cursor returned by ContentResolver.query()
         */
        Cursor runQuery(ContentResolver resolver, String[] projection, Uri baseUri) {
            Uri uri = baseUri;
            List<String> selectionParts = new ArrayList<String>();

            if (mId != null) {
                uri = ContentUris.withAppendedId(uri, mId);
            }

            if (mStatusFlags != null) {
                List<String> parts = new ArrayList<String>();
                if ((mStatusFlags & STATUS_PENDING) != 0) {
                    parts.add(statusClause("=", Downloads.STATUS_PENDING));
                }
                if ((mStatusFlags & STATUS_RUNNING) != 0) {
                    parts.add(statusClause("=", Downloads.STATUS_RUNNING));
                }
                if ((mStatusFlags & STATUS_PAUSED) != 0) {
                    parts.add(statusClause("=", Downloads.STATUS_PENDING_PAUSED));
                    parts.add(statusClause("=", Downloads.STATUS_RUNNING_PAUSED));
                }
                if ((mStatusFlags & STATUS_SUCCESSFUL) != 0) {
                    parts.add(statusClause("=", Downloads.STATUS_SUCCESS));
                }
                if ((mStatusFlags & STATUS_FAILED) != 0) {
                    parts.add("(" + statusClause(">=", 400)
                              + " AND " + statusClause("<", 600) + ")");
                }
                selectionParts.add(joinStrings(" OR ", parts));
            }

            if (mOnlyIncludeVisibleInDownloadsUi) {
                selectionParts.add(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI + " != '0'");
            }

            String selection = joinStrings(" AND ", selectionParts);
            String orderDirection = (mOrderDirection == ORDER_ASCENDING ? "ASC" : "DESC");
            String orderBy = mOrderByColumn + " " + orderDirection;

            return resolver.query(uri, projection, selection, null, orderBy);
        }

        private String joinStrings(String joiner, Iterable<String> parts) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (String part : parts) {
                if (!first) {
                    builder.append(joiner);
                }
                builder.append(part);
                first = false;
            }
            return builder.toString();
        }

        private String statusClause(String operator, int value) {
            return Downloads.COLUMN_STATUS + operator + "'" + value + "'";
        }
    }

    private ContentResolver mResolver;
    private String mPackageName;
    private Uri mBaseUri = Downloads.Impl.CONTENT_URI;

    /**
     * @hide
     */
    public DownloadManager(ContentResolver resolver, String packageName) {
        mResolver = resolver;
        mPackageName = packageName;
    }

    /**
     * Makes this object access the download provider through /all_downloads URIs rather than
     * /my_downloads URIs, for clients that have permission to do so.
     * @hide
     */
    public void setAccessAllDownloads(boolean accessAllDownloads) {
        if (accessAllDownloads) {
            mBaseUri = Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI;
        } else {
            mBaseUri = Downloads.Impl.CONTENT_URI;
        }
    }

    /**
     * Enqueue a new download.  The download will start automatically once the download manager is
     * ready to execute it and connectivity is available.
     *
     * @param request the parameters specifying this download
     * @return an ID for the download, unique across the system.  This ID is used to make future
     * calls related to this download.
     */
    public long enqueue(Request request) {
        ContentValues values = request.toContentValues(mPackageName);
        Uri downloadUri = mResolver.insert(Downloads.CONTENT_URI, values);
        long id = Long.parseLong(downloadUri.getLastPathSegment());
        return id;
    }

    /**
     * Cancel a download and remove it from the download manager.  The download will be stopped if
     * it was running, and it will no longer be accessible through the download manager.  If a file
     * was already downloaded, it will not be deleted.
     *
     * @param id the ID of the download
     */
    public void remove(long id) {
        int numDeleted = mResolver.delete(getDownloadUri(id), null, null);
        if (numDeleted == 0) {
            throw new IllegalArgumentException("Download " + id + " does not exist");
        }
    }

    /**
     * Query the download manager about downloads that have been requested.
     * @param query parameters specifying filters for this query
     * @return a Cursor over the result set of downloads, with columns consisting of all the
     * COLUMN_* constants.
     */
    public Cursor query(Query query) {
        Cursor underlyingCursor = query.runQuery(mResolver, UNDERLYING_COLUMNS, mBaseUri);
        if (underlyingCursor == null) {
            return null;
        }
        return new CursorTranslator(underlyingCursor, mBaseUri);
    }

    /**
     * Open a downloaded file for reading.  The download must have completed.
     * @param id the ID of the download
     * @return a read-only {@link ParcelFileDescriptor}
     * @throws FileNotFoundException if the destination file does not already exist
     */
    public ParcelFileDescriptor openDownloadedFile(long id) throws FileNotFoundException {
        return mResolver.openFileDescriptor(getDownloadUri(id), "r");
    }

    /**
     * Restart the given download, which must have already completed (successfully or not).  This
     * method will only work when called from within the download manager's process.
     * @param id the ID of the download
     * @hide
     */
    public void restartDownload(long id) {
        Cursor cursor = query(new Query().setFilterById(id));
        try {
            if (!cursor.moveToFirst()) {
                throw new IllegalArgumentException("No download with id " + id);
            }
            int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
            if (status != STATUS_SUCCESSFUL && status != STATUS_FAILED) {
                throw new IllegalArgumentException("Cannot restart incomplete download: " + id);
            }
        } finally {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, 0);
        values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, -1);
        values.putNull(Downloads.Impl._DATA);
        values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PENDING);
        mResolver.update(getDownloadUri(id), values, null, null);
    }

    /**
     * Get the DownloadProvider URI for the download with the given ID.
     */
    Uri getDownloadUri(long id) {
        return ContentUris.withAppendedId(mBaseUri, id);
    }

    /**
     * This class wraps a cursor returned by DownloadProvider -- the "underlying cursor" -- and
     * presents a different set of columns, those defined in the DownloadManager.COLUMN_* constants.
     * Some columns correspond directly to underlying values while others are computed from
     * underlying data.
     */
    private static class CursorTranslator extends CursorWrapper {
        private Uri mBaseUri;

        public CursorTranslator(Cursor cursor, Uri baseUri) {
            super(cursor);
            mBaseUri = baseUri;
        }

        @Override
        public int getColumnIndex(String columnName) {
            return Arrays.asList(COLUMNS).indexOf(columnName);
        }

        @Override
        public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
            int index = getColumnIndex(columnName);
            if (index == -1) {
                throw new IllegalArgumentException("No such column: " + columnName);
            }
            return index;
        }

        @Override
        public String getColumnName(int columnIndex) {
            int numColumns = COLUMNS.length;
            if (columnIndex < 0 || columnIndex >= numColumns) {
                throw new IllegalArgumentException("Invalid column index " + columnIndex + ", "
                                                   + numColumns + " columns exist");
            }
            return COLUMNS[columnIndex];
        }

        @Override
        public String[] getColumnNames() {
            String[] returnColumns = new String[COLUMNS.length];
            System.arraycopy(COLUMNS, 0, returnColumns, 0, COLUMNS.length);
            return returnColumns;
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public byte[] getBlob(int columnIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getDouble(int columnIndex) {
            return getLong(columnIndex);
        }

        private boolean isLongColumn(String column) {
            return LONG_COLUMNS.contains(column);
        }

        @Override
        public float getFloat(int columnIndex) {
            return (float) getDouble(columnIndex);
        }

        @Override
        public int getInt(int columnIndex) {
            return (int) getLong(columnIndex);
        }

        @Override
        public long getLong(int columnIndex) {
            return translateLong(getColumnName(columnIndex));
        }

        @Override
        public short getShort(int columnIndex) {
            return (short) getLong(columnIndex);
        }

        @Override
        public String getString(int columnIndex) {
            return translateString(getColumnName(columnIndex));
        }

        private String translateString(String column) {
            if (isLongColumn(column)) {
                return Long.toString(translateLong(column));
            }
            if (column.equals(COLUMN_TITLE)) {
                return getUnderlyingString(Downloads.COLUMN_TITLE);
            }
            if (column.equals(COLUMN_DESCRIPTION)) {
                return getUnderlyingString(Downloads.COLUMN_DESCRIPTION);
            }
            if (column.equals(COLUMN_URI)) {
                return getUnderlyingString(Downloads.COLUMN_URI);
            }
            if (column.equals(COLUMN_MEDIA_TYPE)) {
                return getUnderlyingString(Downloads.COLUMN_MIME_TYPE);
            }

            assert column.equals(COLUMN_LOCAL_URI);
            return getLocalUri();
        }

        private String getLocalUri() {
            long destinationType = getUnderlyingLong(Downloads.Impl.COLUMN_DESTINATION);
            if (destinationType == Downloads.Impl.DESTINATION_FILE_URI) {
                // return client-provided file URI for external download
                return getUnderlyingString(Downloads.Impl.COLUMN_FILE_NAME_HINT);
            }

            // return content URI for cache download
            long downloadId = getUnderlyingLong(Downloads.Impl._ID);
            return ContentUris.withAppendedId(mBaseUri, downloadId).toString();
        }

        private long translateLong(String column) {
            if (!isLongColumn(column)) {
                // mimic behavior of underlying cursor -- most likely, throw NumberFormatException
                return Long.valueOf(translateString(column));
            }

            if (column.equals(COLUMN_ID)) {
                return getUnderlyingLong(Downloads.Impl._ID);
            }
            if (column.equals(COLUMN_TOTAL_SIZE_BYTES)) {
                return getUnderlyingLong(Downloads.COLUMN_TOTAL_BYTES);
            }
            if (column.equals(COLUMN_STATUS)) {
                return translateStatus((int) getUnderlyingLong(Downloads.COLUMN_STATUS));
            }
            if (column.equals(COLUMN_ERROR_CODE)) {
                return translateErrorCode((int) getUnderlyingLong(Downloads.COLUMN_STATUS));
            }
            if (column.equals(COLUMN_BYTES_DOWNLOADED_SO_FAR)) {
                return getUnderlyingLong(Downloads.COLUMN_CURRENT_BYTES);
            }
            assert column.equals(COLUMN_LAST_MODIFIED_TIMESTAMP);
            return getUnderlyingLong(Downloads.COLUMN_LAST_MODIFICATION);
        }

        private long translateErrorCode(int status) {
            if (translateStatus(status) != STATUS_FAILED) {
                return 0; // arbitrary value when status is not an error
            }
            if ((400 <= status && status < Downloads.Impl.MIN_ARTIFICIAL_ERROR_STATUS)
                    || (500 <= status && status < 600)) {
                // HTTP status code
                return status;
            }

            switch (status) {
                case Downloads.STATUS_FILE_ERROR:
                    return ERROR_FILE_ERROR;

                case Downloads.STATUS_UNHANDLED_HTTP_CODE:
                case Downloads.STATUS_UNHANDLED_REDIRECT:
                    return ERROR_UNHANDLED_HTTP_CODE;

                case Downloads.STATUS_HTTP_DATA_ERROR:
                    return ERROR_HTTP_DATA_ERROR;

                case Downloads.STATUS_TOO_MANY_REDIRECTS:
                    return ERROR_TOO_MANY_REDIRECTS;

                case Downloads.STATUS_INSUFFICIENT_SPACE_ERROR:
                    return ERROR_INSUFFICIENT_SPACE;

                case Downloads.STATUS_DEVICE_NOT_FOUND_ERROR:
                    return ERROR_DEVICE_NOT_FOUND;

                case Downloads.Impl.STATUS_CANNOT_RESUME:
                    return ERROR_CANNOT_RESUME;

                case Downloads.Impl.STATUS_FILE_ALREADY_EXISTS_ERROR:
                    return ERROR_FILE_ALREADY_EXISTS;

                default:
                    return ERROR_UNKNOWN;
            }
        }

        private long getUnderlyingLong(String column) {
            return super.getLong(super.getColumnIndex(column));
        }

        private String getUnderlyingString(String column) {
            return super.getString(super.getColumnIndex(column));
        }

        private long translateStatus(int status) {
            switch (status) {
                case Downloads.STATUS_PENDING:
                    return STATUS_PENDING;

                case Downloads.STATUS_RUNNING:
                    return STATUS_RUNNING;

                case Downloads.STATUS_PENDING_PAUSED:
                case Downloads.STATUS_RUNNING_PAUSED:
                    return STATUS_PAUSED;

                case Downloads.STATUS_SUCCESS:
                    return STATUS_SUCCESSFUL;

                default:
                    assert Downloads.isStatusError(status);
                    return STATUS_FAILED;
            }
        }
    }
}