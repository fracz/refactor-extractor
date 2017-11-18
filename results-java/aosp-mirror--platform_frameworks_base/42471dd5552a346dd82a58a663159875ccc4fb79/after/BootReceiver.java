/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.FileUtils;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Performs a number of miscellaneous, non-system-critical actions
 * after the system has finished booting.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    // Negative meaning capture the *last* 64K of the file
    // (passed to FileUtils.readTextFile)
    private static final int LOG_SIZE = -65536;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            logBootEvents(context);
        } catch (Exception e) {
            Log.e(TAG, "Can't log boot events", e);
        }

        try {
            RecoverySystem.handleAftermath();
        } catch (Exception e) {
            Log.e(TAG, "Can't handle recovery aftermath", e);
        }

        try {
            // Start the load average overlay, if activated
            ContentResolver res = context.getContentResolver();
            if (Settings.System.getInt(res, Settings.System.SHOW_PROCESSES, 0) != 0) {
                Intent loadavg = new Intent(context, com.android.server.LoadAverageService.class);
                context.startService(loadavg);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't start load average service", e);
        }
    }

    private void logBootEvents(Context context) throws IOException {
        DropBoxManager db = (DropBoxManager) context.getSystemService(Context.DROPBOX_SERVICE);

        StringBuilder props = new StringBuilder();
        props.append("Build: ").append(Build.FINGERPRINT).append("\n");
        props.append("Hardware: ").append(Build.BOARD).append("\n");
        props.append("Bootloader: ").append(Build.BOOTLOADER).append("\n");
        props.append("Radio: ").append(Build.RADIO).append("\n");
        props.append("Kernel: ");
        props.append(FileUtils.readTextFile(new File("/proc/version"), 1024, "...\n"));

        if (SystemProperties.getLong("ro.runtime.firstboot", 0) == 0) {
            String now = Long.toString(System.currentTimeMillis());
            SystemProperties.set("ro.runtime.firstboot", now);
            if (db != null) db.addText("SYSTEM_BOOT", props.toString());
        } else {
            if (db != null) db.addText("SYSTEM_RESTART", props.toString());
            return;  // Subsequent boot, don't log kernel boot log
        }

        ContentResolver cr = context.getContentResolver();
        logBootFile(cr, db, "/cache/recovery/log", "SYSTEM_RECOVERY_LOG");
        logBootFile(cr, db, "/proc/last_kmsg", "SYSTEM_LAST_KMSG");
        logBootFile(cr, db, "/data/dontpanic/apanic_console", "APANIC_CONSOLE");
        logBootFile(cr, db, "/data/dontpanic/apanic_threads", "APANIC_THREADS");
    }

    private void logBootFile(ContentResolver cr, DropBoxManager db, String filename, String tag)
            throws IOException {
        if (cr == null || db == null || !db.isTagEnabled(tag)) return;  // Logging disabled

        File file = new File(filename);
        long fileTime = file.lastModified();
        if (fileTime <= 0) return;  // File does not exist

        String setting = "logfile:" + filename;
        long lastTime = Settings.Secure.getLong(cr, setting, 0);
        if (lastTime == fileTime) return;  // Already logged this particular file
        Settings.Secure.putLong(cr, setting, fileTime);

        StringBuilder report = new StringBuilder();
        report.append("Build: ").append(Build.FINGERPRINT).append("\n");
        report.append("Kernel: ");
        report.append(FileUtils.readTextFile(new File("/proc/version"), 1024, "...\n"));
        report.append(FileUtils.readTextFile(new File(filename), LOG_SIZE, "[[TRUNCATED]]\n"));
        db.addText(tag, report.toString());
    }
}