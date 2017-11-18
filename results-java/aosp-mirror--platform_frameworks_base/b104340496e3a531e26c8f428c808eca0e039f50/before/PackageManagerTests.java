/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.unit_tests;

import android.os.IMountService.Stub;

import android.net.Uri;
import android.os.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageStats;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Suppress;
import android.util.DisplayMetrics;
import android.util.Log;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMountService;
import android.os.MountServiceResultCode;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;

public class PackageManagerTests extends AndroidTestCase {
    private static final boolean localLOGV = true;
    public static final String TAG="PackageManagerTests";
    public final long MAX_WAIT_TIME=120*1000;
    public final long WAIT_TIME_INCR=20*1000;

    void failStr(String errMsg) {
        Log.w(TAG, "errMsg="+errMsg);
        fail(errMsg);
    }
    void failStr(Exception e) {
        Log.w(TAG, "e.getMessage="+e.getMessage());
        Log.w(TAG, "e="+e);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    private class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public int returnCode;
        private boolean doneFlag = false;

        public void packageInstalled(String packageName, int returnCode) {
            synchronized(this) {
                this.returnCode = returnCode;
                doneFlag = true;
                notifyAll();
            }
        }

        public boolean isDone() {
            return doneFlag;
        }
    }

    abstract class GenericReceiver extends BroadcastReceiver {
        private boolean doneFlag = false;
        boolean received = false;
        Intent intent;
        IntentFilter filter;
        abstract boolean notifyNow(Intent intent);
        @Override
        public void onReceive(Context context, Intent intent) {
            if (notifyNow(intent)) {
                synchronized (this) {
                    received = true;
                    doneFlag = true;
                    this.intent = intent;
                    notifyAll();
                }
            }
        }

        public boolean isDone() {
            return doneFlag;
        }

        public void setFilter(IntentFilter filter) {
            this.filter = filter;
        }
    }

    class InstallReceiver extends GenericReceiver {
        String pkgName;

        InstallReceiver(String pkgName) {
            this.pkgName = pkgName;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addDataScheme("package");
            super.setFilter(filter);
        }

        public boolean notifyNow(Intent intent) {
            String action = intent.getAction();
            if (!Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                return false;
            }
            Uri data = intent.getData();
            String installedPkg = data.getEncodedSchemeSpecificPart();
            if (pkgName.equals(installedPkg)) {
                return true;
            }
            return false;
        }
    }

    PackageManager getPm() {
        return mContext.getPackageManager();
    }

    public boolean invokeInstallPackage(Uri packageURI, int flags,
            final String pkgName, GenericReceiver receiver) throws Exception {
        PackageInstallObserver observer = new PackageInstallObserver();
        final boolean received = false;
        mContext.registerReceiver(receiver, receiver.filter);
        try {
            // Wait on observer
            synchronized(observer) {
                synchronized (receiver) {
                    getPm().installPackage(packageURI, observer, flags, null);
                    long waitTime = 0;
                    while((!observer.isDone()) && (waitTime < MAX_WAIT_TIME) ) {
                        observer.wait(WAIT_TIME_INCR);
                        waitTime += WAIT_TIME_INCR;
                    }
                    if(!observer.isDone()) {
                        throw new Exception("Timed out waiting for packageInstalled callback");
                    }
                    if (observer.returnCode != PackageManager.INSTALL_SUCCEEDED) {
                        return false;
                    }
                    // Verify we received the broadcast
                    waitTime = 0;
                    while((!receiver.isDone()) && (waitTime < MAX_WAIT_TIME) ) {
                        receiver.wait(WAIT_TIME_INCR);
                        waitTime += WAIT_TIME_INCR;
                    }
                    if(!receiver.isDone()) {
                        throw new Exception("Timed out waiting for PACKAGE_ADDED notification");
                    }
                    return receiver.received;
                }
            }
        } finally {
            mContext.unregisterReceiver(receiver);
        }
    }

    Uri getInstallablePackage(int fileResId, File outFile) {
        Resources res = mContext.getResources();
        InputStream is = null;
        try {
            is = res.openRawResource(fileResId);
        } catch (NotFoundException e) {
            failStr("Failed to load resource with id: " + fileResId);
        }
        FileUtils.setPermissions(outFile.getPath(),
                FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO,
                -1, -1);
        assertTrue(FileUtils.copyToFile(is, outFile));
        FileUtils.setPermissions(outFile.getPath(),
                FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO,
                -1, -1);
        return Uri.fromFile(outFile);
    }

    private PackageParser.Package parsePackage(Uri packageURI) {
        final String archiveFilePath = packageURI.getPath();
        PackageParser packageParser = new PackageParser(archiveFilePath);
        File sourceFile = new File(archiveFilePath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        return packageParser.parsePackage(sourceFile, archiveFilePath, metrics, 0);
    }

    private void assertInstall(String pkgName, int flags) {
        try {
        ApplicationInfo info = getPm().getApplicationInfo(pkgName, 0);
        assertNotNull(info);
        assertEquals(pkgName, info.packageName);
        File dataDir = Environment.getDataDirectory();
        String appInstallPath = new File(dataDir, "app").getPath();
        String drmInstallPath = new File(dataDir, "app-private").getPath();
        File srcDir = new File(info.sourceDir);
        String srcPath = srcDir.getParent();
        File publicSrcDir = new File(info.publicSourceDir);
        String publicSrcPath = publicSrcDir.getParent();

        if ((flags & PackageManager.INSTALL_FORWARD_LOCK) != 0) {
            assertTrue((info.flags & ApplicationInfo.FLAG_FORWARD_LOCK) != 0);
            assertEquals(srcPath, drmInstallPath);
            assertEquals(publicSrcPath, appInstallPath);
        } else {
            assertFalse((info.flags & ApplicationInfo.FLAG_FORWARD_LOCK) != 0);
            if ((flags & PackageManager.INSTALL_ON_SDCARD) != 0) {
                assertTrue((info.flags & ApplicationInfo.FLAG_ON_SDCARD) != 0);
                // Hardcoded for now
                assertTrue(srcPath.startsWith("/asec"));
                assertTrue(publicSrcPath.startsWith("/asec"));
            } else {
                assertEquals(srcPath, appInstallPath);
                assertEquals(publicSrcPath, appInstallPath);
                assertFalse((info.flags & ApplicationInfo.FLAG_ON_SDCARD) != 0);
            }
        }
        } catch (NameNotFoundException e) {
            failStr("failed with exception : " + e);
        }
    }

    class InstallParams {
        String outFileName;
        Uri packageURI;
        PackageParser.Package pkg;
        InstallParams(PackageParser.Package pkg, String outFileName, Uri packageURI) {
            this.outFileName = outFileName;
            this.packageURI = packageURI;
            this.pkg = pkg;
        }
    }

    /*
     * Utility function that reads a apk bundled as a raw resource
     * copies it into own data directory and invokes
     * PackageManager api to install it.
     */
    public InstallParams installFromRawResource(int flags, boolean cleanUp) {
        String outFileName = "install.apk";
        File filesDir = mContext.getFilesDir();
        File outFile = new File(filesDir, outFileName);
        Uri packageURI = getInstallablePackage(R.raw.install, outFile);
        PackageParser.Package pkg = parsePackage(packageURI);
        assertNotNull(pkg);
        InstallReceiver receiver = new InstallReceiver(pkg.packageName);
        InstallParams ip = null;
        try {
            try {
                assertTrue(invokeInstallPackage(packageURI, flags,
                        pkg.packageName, receiver));
            } catch (Exception e) {
                failStr("Failed with exception : " + e);
            }
            // Verify installed information
            assertInstall(pkg.packageName, flags);
            ip = new InstallParams(pkg, outFileName, packageURI);
            return ip;
        } finally {
            if (cleanUp) {
                cleanUpInstall(ip);
            }
        }
    }

    @MediumTest
    public void testInstallNormalInternal() {
        installFromRawResource(0, true);
    }

    @MediumTest
    public void testInstallFwdLockedInternal() {
        installFromRawResource(PackageManager.INSTALL_FORWARD_LOCK, true);
    }

    @MediumTest
    public void testInstallSdcard() {
        installFromRawResource(PackageManager.INSTALL_ON_SDCARD, true);
    }

    /* ------------------------- Test replacing packages --------------*/
    class ReplaceReceiver extends GenericReceiver {
        String pkgName;
        final static int INVALID = -1;
        final static int REMOVED = 1;
        final static int ADDED = 2;
        final static int REPLACED = 3;
        int removed = INVALID;
        // for updated system apps only
        boolean update = false;

        ReplaceReceiver(String pkgName) {
            this.pkgName = pkgName;
            filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            if (update) {
                filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            }
            filter.addDataScheme("package");
            super.setFilter(filter);
        }

        public boolean notifyNow(Intent intent) {
            String action = intent.getAction();
            Uri data = intent.getData();
            String installedPkg = data.getEncodedSchemeSpecificPart();
            if (pkgName == null || !pkgName.equals(installedPkg)) {
                return false;
            }
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                removed = REMOVED;
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (removed != REMOVED) {
                    return false;
                }
                boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (!replacing) {
                    return false;
                }
                removed = ADDED;
                if (!update) {
                    return true;
                }
            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                if (removed != ADDED) {
                    return false;
                }
                removed = REPLACED;
                return true;
            }
            return false;
        }
    }

    /*
     * Utility function that reads a apk bundled as a raw resource
     * copies it into own data directory and invokes
     * PackageManager api to install first and then replace it
     * again.
     */
    public void replaceFromRawResource(int flags) {
        InstallParams ip = installFromRawResource(flags, false);
        boolean replace = ((flags & PackageManager.INSTALL_REPLACE_EXISTING) != 0);
        GenericReceiver receiver;
        if (replace) {
            receiver = new ReplaceReceiver(ip.pkg.packageName);
            Log.i(TAG, "Creating replaceReceiver");
        } else {
            receiver = new InstallReceiver(ip.pkg.packageName);
        }
        try {
            try {
                assertEquals(invokeInstallPackage(ip.packageURI, flags,
                        ip.pkg.packageName, receiver), replace);
                if (replace) {
                    assertInstall(ip.pkg.packageName, flags);
                }
            } catch (Exception e) {
                failStr("Failed with exception : " + e);
            }
        } finally {
            cleanUpInstall(ip);
        }
    }

    @MediumTest
    public void testReplaceFailNormalInternal() {
        replaceFromRawResource(0);
    }

    @MediumTest
    public void testReplaceFailFwdLockedInternal() {
        replaceFromRawResource(PackageManager.INSTALL_FORWARD_LOCK);
    }

    @MediumTest
    public void testReplaceFailSdcard() {
        replaceFromRawResource(PackageManager.INSTALL_ON_SDCARD);
    }

    @MediumTest
    public void testReplaceNormalInternal() {
        replaceFromRawResource(PackageManager.INSTALL_REPLACE_EXISTING);
    }

    @MediumTest
    public void testReplaceFwdLockedInternal() {
        replaceFromRawResource(PackageManager.INSTALL_REPLACE_EXISTING |
                PackageManager.INSTALL_FORWARD_LOCK);
    }

    @MediumTest
    public void testReplaceSdcard() {
        replaceFromRawResource(PackageManager.INSTALL_REPLACE_EXISTING |
                PackageManager.INSTALL_ON_SDCARD);
    }

    /* -------------- Delete tests ---*/
    class DeleteObserver extends IPackageDeleteObserver.Stub {

        public boolean succeeded;
        private boolean doneFlag = false;

        public boolean isDone() {
            return doneFlag;
        }

        public void packageDeleted(boolean succeeded) throws RemoteException {
            synchronized(this) {
                this.succeeded = succeeded;
                doneFlag = true;
                notifyAll();
            }
        }
    }

    class DeleteReceiver extends GenericReceiver {
        String pkgName;

        DeleteReceiver(String pkgName) {
            this.pkgName = pkgName;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            filter.addDataScheme("package");
            super.setFilter(filter);
        }

        public boolean notifyNow(Intent intent) {
            String action = intent.getAction();
            if (!Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                return false;
            }
            Uri data = intent.getData();
            String installedPkg = data.getEncodedSchemeSpecificPart();
            if (pkgName.equals(installedPkg)) {
                return true;
            }
            return false;
        }
    }

    public boolean invokeDeletePackage(Uri packageURI, int flags,
            final String pkgName, GenericReceiver receiver) throws Exception {
        DeleteObserver observer = new DeleteObserver();
        final boolean received = false;
        mContext.registerReceiver(receiver, receiver.filter);
        try {
            // Wait on observer
            synchronized(observer) {
                synchronized (receiver) {
                    getPm().deletePackage(pkgName, observer, flags);
                    long waitTime = 0;
                    while((!observer.isDone()) && (waitTime < MAX_WAIT_TIME) ) {
                        observer.wait(WAIT_TIME_INCR);
                        waitTime += WAIT_TIME_INCR;
                    }
                    if(!observer.isDone()) {
                        throw new Exception("Timed out waiting for packageInstalled callback");
                    }
                    // Verify we received the broadcast
                    waitTime = 0;
                    while((!receiver.isDone()) && (waitTime < MAX_WAIT_TIME) ) {
                        receiver.wait(WAIT_TIME_INCR);
                        waitTime += WAIT_TIME_INCR;
                    }
                    if(!receiver.isDone()) {
                        throw new Exception("Timed out waiting for PACKAGE_ADDED notification");
                    }
                    return receiver.received;
                }
            }
        } finally {
            mContext.unregisterReceiver(receiver);
        }
    }

    public void deleteFromRawResource(int iFlags, int dFlags) {
        InstallParams ip = installFromRawResource(iFlags, false);
        boolean retainData = ((dFlags & PackageManager.DONT_DELETE_DATA) != 0);
        GenericReceiver receiver = new DeleteReceiver(ip.pkg.packageName);
        DeleteObserver observer = new DeleteObserver();
        try {
            assertTrue(invokeDeletePackage(ip.packageURI, dFlags,
                    ip.pkg.packageName, receiver));
            ApplicationInfo info = null;
            Log.i(TAG, "okay4");
            try {
            info = getPm().getApplicationInfo(ip.pkg.packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            } catch (NameNotFoundException e) {
                info = null;
            }
            if (retainData) {
                assertNotNull(info);
                assertEquals(info.packageName, ip.pkg.packageName);
                File file = new File(info.dataDir);
                assertTrue(file.exists());
            } else {
                assertNull(info);
            }
        } catch (Exception e) {
            failStr(e);
        } finally {
            cleanUpInstall(ip);
        }
    }

    @MediumTest
    public void testDeleteNormalInternal() {
        deleteFromRawResource(0, 0);
    }

    @MediumTest
    public void testDeleteFwdLockedInternal() {
        deleteFromRawResource(PackageManager.INSTALL_FORWARD_LOCK, 0);
    }

    @MediumTest
    public void testDeleteSdcard() {
        deleteFromRawResource(PackageManager.INSTALL_ON_SDCARD, 0);
    }

    @MediumTest
    public void testDeleteNormalInternalRetainData() {
        deleteFromRawResource(0, PackageManager.DONT_DELETE_DATA);
    }

    @MediumTest
    public void testDeleteFwdLockedInternalRetainData() {
        deleteFromRawResource(PackageManager.INSTALL_FORWARD_LOCK, PackageManager.DONT_DELETE_DATA);
    }

    @MediumTest
    public void testDeleteSdcardRetainData() {
        deleteFromRawResource(PackageManager.INSTALL_ON_SDCARD, PackageManager.DONT_DELETE_DATA);
    }

    /* sdcard mount/unmount tests ******/

    class SdMountReceiver extends GenericReceiver {
        String pkgNames[];
        boolean status = true;

        SdMountReceiver(String[] pkgNames) {
            this.pkgNames = pkgNames;
            IntentFilter filter = new IntentFilter(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            super.setFilter(filter);
        }

        public boolean notifyNow(Intent intent) {
            Log.i(TAG, "okay 1");
            String action = intent.getAction();
            if (!Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
                return false;
            }
            String rpkgList[] = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            for (String pkg : pkgNames) {
                boolean found = false;
                for (String rpkg : rpkgList) {
                    if (rpkg.equals(pkg)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    status = false;
                    return true;
                }
            }
            return true;
        }
    }

    class SdUnMountReceiver extends GenericReceiver {
        String pkgNames[];
        boolean status = true;

        SdUnMountReceiver(String[] pkgNames) {
            this.pkgNames = pkgNames;
            IntentFilter filter = new IntentFilter(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            super.setFilter(filter);
        }

        public boolean notifyNow(Intent intent) {
            String action = intent.getAction();
            if (!Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                return false;
            }
            String rpkgList[] = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            for (String pkg : pkgNames) {
                boolean found = false;
                for (String rpkg : rpkgList) {
                    if (rpkg.equals(pkg)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    status = false;
                    return true;
                }
            }
            return true;
        }
    }

    IMountService getMs() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        } else {
            Log.e(TAG, "Can't get mount service");
        }
        return null;
    }

    boolean getMediaState() {
        try {
        String mPath = Environment.getExternalStorageDirectory().toString();
        String state = getMs().getVolumeState(mPath);
        return Environment.MEDIA_MOUNTED.equals(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    boolean mountMedia() {
        if (getMediaState()) {
            return true;
        }
        try {
        String mPath = Environment.getExternalStorageDirectory().toString();
        int ret = getMs().mountVolume(mPath);
        return ret == MountServiceResultCode.OperationSucceeded;
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean unmountMedia() {
        if (!getMediaState()) {
            return true;
        }
        try {
        String mPath = Environment.getExternalStorageDirectory().toString();
        int ret = getMs().unmountVolume(mPath);
        return ret == MountServiceResultCode.OperationSucceeded;
        } catch (RemoteException e) {
            return true;
        }
    }

    /*
     * Install package on sdcard. Unmount and then mount the media.
     * (Use PackageManagerService private api for now)
     * Make sure the installed package is available.
     * STOPSHIP will uncomment when MountService api's to mount/unmount
     * are made asynchronous.
     */
    public void xxxtestMountSdNormalInternal() {
        assertTrue(mountFromRawResource());
    }

    private boolean mountFromRawResource() {
        // Install pkg on sdcard
        InstallParams ip = installFromRawResource(PackageManager.INSTALL_ON_SDCARD |
                PackageManager.INSTALL_REPLACE_EXISTING, false);
        if (localLOGV) Log.i(TAG, "Installed pkg on sdcard");
        boolean origState = getMediaState();
        SdMountReceiver receiver = new SdMountReceiver(new String[]{ip.pkg.packageName});
        try {
            if (localLOGV) Log.i(TAG, "Unmounting media");
            // Unmount media
            assertTrue(unmountMedia());
            if (localLOGV) Log.i(TAG, "Unmounted media");
            try {
                if (localLOGV) Log.i(TAG, "Sleeping for 10 second");
                Thread.sleep(10*1000);
            } catch (InterruptedException e) {
                failStr(e);
            }
            // Register receiver here
            PackageManager pm = getPm();
            mContext.registerReceiver(receiver, receiver.filter);

            // Wait on receiver
            synchronized (receiver) {
                if (localLOGV) Log.i(TAG, "Mounting media");
                // Mount media again
                assertTrue(mountMedia());
                if (localLOGV) Log.i(TAG, "Mounted media");
                if (localLOGV) Log.i(TAG, "Waiting for notification");
                long waitTime = 0;
                // Verify we received the broadcast
                waitTime = 0;
                while((!receiver.isDone()) && (waitTime < MAX_WAIT_TIME) ) {
                    receiver.wait(WAIT_TIME_INCR);
                    waitTime += WAIT_TIME_INCR;
                }
                if(!receiver.isDone()) {
                    failStr("Timed out waiting for EXTERNAL_APPLICATIONS notification");
                }
                return receiver.received;
            }
        } catch (InterruptedException e) {
            failStr(e);
            return false;
        } finally {
            mContext.unregisterReceiver(receiver);
            // Restore original media state
            if (origState) {
                mountMedia();
            } else {
                unmountMedia();
            }
            if (localLOGV) Log.i(TAG, "Cleaning up install");
            cleanUpInstall(ip);
        }
    }

    void cleanUpInstall(InstallParams ip) {
        if (ip == null) {
            return;
        }
        Runtime.getRuntime().gc();
        Log.i(TAG, "Deleting package : " + ip.pkg.packageName);
        getPm().deletePackage(ip.pkg.packageName, null, 0);
        File outFile = new File(ip.outFileName);
        if (outFile != null && outFile.exists()) {
            outFile.delete();
        }
    }
    /*
     * TODO's
     * check version numbers for upgrades
     * check permissions of installed packages
     * how to do tests on updated system apps?
     * verify updates to system apps cannot be installed on the sdcard.
     */
}