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

package android.os;

import java.io.File;

import android.os.IMountService;

/**
 * Provides access to environment variables.
 */
public class Environment {

    private static final File ROOT_DIRECTORY
            = getDirectory("ANDROID_ROOT", "/system");

    private static final String SYSTEM_PROPERTY_EFS_ENABLED = "persist.security.efs.enabled";

    private static IMountService mMntSvc = null;

    /**
     * Gets the Android root directory.
     */
    public static File getRootDirectory() {
        return ROOT_DIRECTORY;
    }

    /**
     * Gets the system directory available for secure storage.
     * If Encrypted File system is enabled, it returns an encrypted directory (/data/secure/system).
     * Otherwise, it returns the unencrypted /data/system directory.
     * @return File object representing the secure storage system directory.
     * @hide
     */
    public static File getSystemSecureDirectory() {
        if (isEncryptedFilesystemEnabled()) {
            return new File(SECURE_DATA_DIRECTORY, "system");
        } else {
            return new File(DATA_DIRECTORY, "system");
        }
    }

    /**
     * Gets the data directory for secure storage.
     * If Encrypted File system is enabled, it returns an encrypted directory (/data/secure).
     * Otherwise, it returns the unencrypted /data directory.
     * @return File object representing the data directory for secure storage.
     * @hide
     */
    public static File getSecureDataDirectory() {
        if (isEncryptedFilesystemEnabled()) {
            return SECURE_DATA_DIRECTORY;
        } else {
            return DATA_DIRECTORY;
        }
    }

    /**
     * Returns whether the Encrypted File System feature is enabled on the device or not.
     * @return <code>true</code> if Encrypted File System feature is enabled, <code>false</code>
     * if disabled.
     * @hide
     */
    public static boolean isEncryptedFilesystemEnabled() {
        return SystemProperties.getBoolean(SYSTEM_PROPERTY_EFS_ENABLED, false);
    }

    private static final File DATA_DIRECTORY
            = getDirectory("ANDROID_DATA", "/data");

    /**
     * @hide
     */
    private static final File SECURE_DATA_DIRECTORY
            = getDirectory("ANDROID_SECURE_DATA", "/data/secure");

    private static final File EXTERNAL_STORAGE_DIRECTORY
            = getDirectory("EXTERNAL_STORAGE", "/sdcard");

    private static final File DOWNLOAD_CACHE_DIRECTORY
            = getDirectory("DOWNLOAD_CACHE", "/cache");

    /**
     * Gets the Android data directory.
     */
    public static File getDataDirectory() {
        return DATA_DIRECTORY;
    }

    /**
     * Gets the Android external storage directory.
     */
    public static File getExternalStorageDirectory() {
        return EXTERNAL_STORAGE_DIRECTORY;
    }

    /**
     * Gets the Android Download/Cache content directory.
     */
    public static File getDownloadCacheDirectory() {
        return DOWNLOAD_CACHE_DIRECTORY;
    }

    /**
     * getExternalStorageState() returns MEDIA_REMOVED if the media is not present.
     */
    public static final String MEDIA_REMOVED = "removed";

    /**
     * getExternalStorageState() returns MEDIA_UNMOUNTED if the media is present
     * but not mounted.
     */
    public static final String MEDIA_UNMOUNTED = "unmounted";

    /**
     * getExternalStorageState() returns MEDIA_CHECKING if the media is present
     * and being disk-checked
     */
    public static final String MEDIA_CHECKING = "checking";

    /**
     * getExternalStorageState() returns MEDIA_NOFS if the media is present
     * but is blank or is using an unsupported filesystem
     */
    public static final String MEDIA_NOFS = "nofs";

    /**
     * getExternalStorageState() returns MEDIA_MOUNTED if the media is present
     * and mounted at its mount point with read/write access.
     */
    public static final String MEDIA_MOUNTED = "mounted";

    /**
     * getExternalStorageState() returns MEDIA_MOUNTED_READ_ONLY if the media is present
     * and mounted at its mount point with read only access.
     */
    public static final String MEDIA_MOUNTED_READ_ONLY = "mounted_ro";

    /**
     * getExternalStorageState() returns MEDIA_SHARED if the media is present
     * not mounted, and shared via USB mass storage.
     */
    public static final String MEDIA_SHARED = "shared";

    /**
     * getExternalStorageState() returns MEDIA_BAD_REMOVAL if the media was
     * removed before it was unmounted.
     */
    public static final String MEDIA_BAD_REMOVAL = "bad_removal";

    /**
     * getExternalStorageState() returns MEDIA_UNMOUNTABLE if the media is present
     * but cannot be mounted.  Typically this happens if the file system on the
     * media is corrupted.
     */
    public static final String MEDIA_UNMOUNTABLE = "unmountable";

    /**
     * Gets the current state of the external storage device.
     * Note: This call should be deprecated as it doesn't support
     * multiple volumes.
     */
    public static String getExternalStorageState() {
        try {
            if (mMntSvc == null) {
                mMntSvc = IMountService.Stub.asInterface(ServiceManager
                                                         .getService("mount"));
            }
            return mMntSvc.getVolumeState(getExternalStorageDirectory().toString());
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }
    }

    static File getDirectory(String variableName, String defaultPath) {
        String path = System.getenv(variableName);
        return path == null ? new File(defaultPath) : new File(path);
    }
}