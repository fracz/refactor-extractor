/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.liulishuo.filedownloader;

import android.app.Notification;
import android.content.Context;

import com.liulishuo.filedownloader.model.FileDownloadHeader;

/**
 * Created by Jacksgong on 4/17/16.
 * <p/>
 * The interface to access the FileDownloadService.
 */
public interface IFileDownloadServiceProxy {
    boolean start(final String url, final String path,
                  final int callbackProgressTimes,
                  final int callbackProgressMinIntervalMillis,
                  final int autoRetryTimes,
                  final FileDownloadHeader header);

    boolean pause(final int id);

    boolean isDownloading(final String url, final String path);

    long getSofar(final int downloadId);

    long getTotal(final int downloadId);

    byte getStatus(final int downloadId);

    void pauseAllTasks();

    boolean isIdle();

    boolean isConnected();

    void bindStartByContext(final Context context);

    void bindStartByContext(final Context context, final Runnable connectedRunnable);

    void unbindByContext(final Context context);

    void startForeground(int id, Notification notification);

    void stopForeground(boolean removeNotification);

    boolean setMaxNetworkThreadCount(int count);
}