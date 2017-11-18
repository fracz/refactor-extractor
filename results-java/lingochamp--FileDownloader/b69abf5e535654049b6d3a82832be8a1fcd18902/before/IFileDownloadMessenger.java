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

import com.liulishuo.filedownloader.model.FileDownloadHeader;
import com.liulishuo.filedownloader.services.FileDownloadRunnable;

import java.io.FileDescriptor;

/**
 * Created by Jacksgong on 12/21/15.
 *
 * @see com.liulishuo.filedownloader.model.FileDownloadStatus
 */
interface IFileDownloadMessenger {

    /**
     * The task is just received to handle.
     * <p/>
     * FileDownloader accept the task.
     */
    void notifyBegin();

    /**
     * The task is pending.
     * <p/>
     * enqueue, and pending, waiting.
     *
     * @see com.liulishuo.filedownloader.services.FileDownloadThreadPool
     */
    void notifyPending();

    /**
     * The download runnable of the task has started running.
     * <p/>
     * Finish pending, and start download runnable.
     *
     * @see FileDownloadRunnable#onStarted()
     */
    void notifyStarted();

    /**
     * The task is running.
     * <p/>
     * Already connected to the server, and received the Http-response.
     *
     * @see FileDownloadRunnable#onConnected(boolean, long, long)
     */
    void notifyConnected();

    /**
     * The task is running.
     * <p/>
     * Fetching datum, and write to local disk.
     *
     * @see FileDownloadRunnable#onProgress(long, long, FileDescriptor)
     */
    void notifyProgress();

    /**
     * The task is running.
     * <p/>
     * Already completed download, and block the current thread to do something, such as unzip,etc.
     *
     * @see FileDownloadRunnable#onComplete(long)
     * @see FileDownloadList#removeByCompleted(BaseDownloadTask)
     */
    void notifyBlockComplete();

    /**
     * The task over.
     * <p/>
     * Occur a exception when downloading, but has retry
     * chance {@link BaseDownloadTask#setAutoRetryTimes(int)}, so retry(re-connect,re-download).
     */
    void notifyRetry();

    /**
     * The task over.
     * <p/>
     * There has already had some same Tasks(Same-URL & Same-SavePath) in Pending-Queue or is
     * running.
     *
     * @see com.liulishuo.filedownloader.services.FileDownloadMgr#start(String, String, int, int, FileDownloadHeader)
     * @see com.liulishuo.filedownloader.services.FileDownloadMgr#checkDownloading(String, String)
     */
    void notifyWarn();

    /**
     * The task over.
     * <p/>
     * Occur a exception, but don't has any chance to retry.
     *
     * @see FileDownloadRunnable#onError(Throwable)
     * @see com.liulishuo.filedownloader.exception.FileDownloadHttpException
     * @see com.liulishuo.filedownloader.exception.FileDownloadOutOfSpaceException
     * @see com.liulishuo.filedownloader.exception.FileDownloadGiveUpRetryException
     */
    void notifyError();

    /**
     * The task over.
     * <p/>
     * Pause manually by {@link BaseDownloadTask#pause()}.
     *
     * @see BaseDownloadTask#pause()
     */
    void notifyPaused();

    /**
     * The task over.
     * <p/>
     * Achieve complete ceremony.
     *
     * @see FileDownloadRunnable#onComplete(long)
     * @see FileDownloadList#removeByCompleted(BaseDownloadTask)
     */
    void notifyCompleted();

    /**
     * handover a message to {@link FileDownloadListener}.
     */
    void handoverMessage();

    /**
     * @return Whether handover a message to {@link FileDownloadListener} directly, do not need post
     * to UI thread.
     * @see BaseDownloadTask#syncCallback
     */
    boolean handoverDirectly();

    /**
     * @return Whether has receiver(bound task has listener) to receiver messages.
     * @see BaseDownloadTask#getListener()
     */
    boolean hasReceiver();

    /**
     * @param task Re-appointment for this task, when this messenger has already accomplished the
     *             old one.
     */
    void reAppointment(BaseDownloadTask task);

    /**
     * The 'block completed'(status) message will be handover in the non-UI thread and block the
     * 'completed'(status) message.
     *
     * @return Whether the status of the current message is
     * {@link com.liulishuo.filedownloader.model.FileDownloadStatus#blockComplete}.
     */
    boolean isBlockingCompleted();
}