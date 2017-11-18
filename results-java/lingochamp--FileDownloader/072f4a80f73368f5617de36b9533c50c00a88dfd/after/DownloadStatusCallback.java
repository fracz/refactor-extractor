/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liulishuo.filedownloader.download;

import android.database.sqlite.SQLiteFullException;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;

import com.liulishuo.filedownloader.exception.FileDownloadGiveUpRetryException;
import com.liulishuo.filedownloader.exception.FileDownloadOutOfSpaceException;
import com.liulishuo.filedownloader.message.MessageSnapshotFlow;
import com.liulishuo.filedownloader.message.MessageSnapshotTaker;
import com.liulishuo.filedownloader.model.FileDownloadModel;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.services.FileDownloadDatabase;
import com.liulishuo.filedownloader.stream.FileDownloadOutputStream;
import com.liulishuo.filedownloader.util.FileDownloadLog;
import com.liulishuo.filedownloader.util.FileDownloadProperties;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.io.IOException;

import static com.liulishuo.filedownloader.download.FetchDataTask.BUFFER_SIZE;
import static com.liulishuo.filedownloader.model.FileDownloadModel.TOTAL_VALUE_IN_CHUNKED_RESOURCE;

/**
 * handle all events sync to DB/filesystem and callback to user.
 */
public class DownloadStatusCallback implements Handler.Callback {

    private final FileDownloadModel model;
    private final FileDownloadDatabase database;
    private final ProcessParams processParams;
    private final int maxRetryTimes;


    private static final int CALLBACK_SAFE_MIN_INTERVAL_BYTES = 1;//byte
    private static final int CALLBACK_SAFE_MIN_INTERVAL_MILLIS = 5;//ms
    private static final int NO_ANY_PROGRESS_CALLBACK = -1;

    private final int callbackProgressMinInterval;
    private final int callbackProgressMaxCount;
    private long callbackMinIntervalBytes;

    private Handler handler;
    private HandlerThread handlerThread;

    DownloadStatusCallback(FileDownloadModel model,
                           int maxRetryTimes, final int minIntervalMillis, int callbackProgressMaxCount) {
        this.model = model;
        this.database = CustomComponentHolder.getImpl().getDatabaseInstance();
        this.callbackProgressMinInterval = minIntervalMillis < CALLBACK_SAFE_MIN_INTERVAL_MILLIS
                ? CALLBACK_SAFE_MIN_INTERVAL_MILLIS : minIntervalMillis;
        this.callbackProgressMaxCount = callbackProgressMaxCount;
        this.processParams = new ProcessParams();
        this.maxRetryTimes = maxRetryTimes;
    }

    public boolean isAlive() {
        return handlerThread != null && handlerThread.isAlive();
    }

    public void onPending() {
        // direct
        database.updatePending(model);
        onStatusChanged(FileDownloadStatus.pending);
    }

    void onStartThread() {
        // direct
        model.setStatus(FileDownloadStatus.started);
        onStatusChanged(FileDownloadStatus.started);
    }

    void onConnected(boolean isResume, long totalLength, String etag, String fileName) {
        // direct
        processParams.setResuming(isResume);

        database.updateConnected(model, totalLength, etag, fileName);
        onStatusChanged(FileDownloadStatus.connected);

        callbackMinIntervalBytes = calculateCallbackMinIntervalBytes(totalLength, callbackProgressMaxCount);
    }

    void onMultiConnection() {
        handlerThread = new HandlerThread("source-status-callback");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), this);
    }

    private volatile long lastCallbackBytes = 0;
    private volatile long lastCallbackTimestamp = 0;

    private volatile long lastUpdateBytes = 0;
    private volatile long lastUpdateTimestamp = 0;

    private volatile long increaseBytes = 0;
    private final Object increaseLock = new Object();

    void onProgress(FileDownloadOutputStream outputStream, long increaseBytes) {
        final long currentProcessing;
        if (handler != null) {
            // flow
            synchronized (increaseLock) {
                this.increaseBytes += increaseBytes;
            }
            currentProcessing = model.getSoFar() + this.increaseBytes;
        } else {
            currentProcessing = model.getSoFar() + increaseBytes;
        }

        final long now = SystemClock.elapsedRealtime();

        final boolean isNeedSync = isNeedSync(now, currentProcessing);
        final boolean isNeedCallbackToUser = isNeedCallbackToUser(now, currentProcessing);

        if (isNeedCallbackToUser || isNeedSync) {
            // flow
            if (handler != null) {
                if (!handlerThread.isAlive()) {
                    if (FileDownloadLog.NEED_LOG) {
                        FileDownloadLog.d(this, "callback progress %d but it has been over", increaseBytes);
                    }
                    return;
                }
                final Message message = handler.obtainMessage(FileDownloadStatus.progress,
                        isNeedSync ? 1 : 0, isNeedCallbackToUser ? 1 : 0, outputStream);
                handler.sendMessage(message);
            } else {
                // direct
                handleProgress(outputStream, increaseBytes, now, isNeedSync, isNeedCallbackToUser);
            }
        }
    }

    void onRetry(Exception exception, int remainRetryTimes) {
        if (handler == null) {
            // direct
            handleRetry(exception, remainRetryTimes);
        } else {
            // flow
            final Message message = handler.obtainMessage(FileDownloadStatus.retry, remainRetryTimes, 0, exception);
            handler.sendMessage(message);
        }
    }

    void onPaused() {
        if (handler == null) {
            // direct
            handlePaused();
        } else {
            // flow
            handler.sendEmptyMessage(FileDownloadStatus.paused);
        }
    }

    void onError(Exception exception) {
        if (handler == null) {
            // direct
            handleError(exception);
        } else {
            // flow
            final Message message = handler.obtainMessage(FileDownloadStatus.error, exception);
            handler.sendMessage(message);
        }
    }

    void onCompleted() throws IOException {
        if (handler == null) {
            // direct
            if (interceptBeforeCompleted(0)) {
                return;
            }

            handleCompleted();
        } else {
            // flow
            handler.sendEmptyMessage(FileDownloadStatus.completed);
        }
    }


    private static long calculateCallbackMinIntervalBytes(final long contentLength,
                                                          final long callbackProgressMaxCount) {
        if (callbackProgressMaxCount <= 0) {
            return NO_ANY_PROGRESS_CALLBACK;
        } else if (contentLength == TOTAL_VALUE_IN_CHUNKED_RESOURCE) {
            return CALLBACK_SAFE_MIN_INTERVAL_BYTES;
        } else {
            final long minIntervalBytes = contentLength / (callbackProgressMaxCount + 1);
            return minIntervalBytes <= 0 ? CALLBACK_SAFE_MIN_INTERVAL_BYTES : minIntervalBytes;
        }
    }

    private Exception exFiltrate(Exception ex) {
        final String tempPath = model.getTempFilePath();
        /**
         * Only handle the case of Chunked resource, if it is not chunked, has already been handled
         * in {@link #getOutputStream(boolean, long)}.
         */
        if ((model.isChunked() ||
                FileDownloadProperties.getImpl().FILE_NON_PRE_ALLOCATION)
                && ex instanceof IOException &&
                new File(tempPath).exists()) {
            // chunked
            final long freeSpaceBytes = FileDownloadUtils.getFreeSpaceBytes(tempPath);
            if (freeSpaceBytes <= BUFFER_SIZE) {
                // free space is not enough.
                long downloadedSize = 0;
                final File file = new File(tempPath);
                if (!file.exists()) {
                    FileDownloadLog.e(this, ex, "Exception with: free " +
                            "space isn't enough, and the target file not exist.");
                } else {
                    downloadedSize = file.length();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    ex = new FileDownloadOutOfSpaceException(freeSpaceBytes, BUFFER_SIZE,
                            downloadedSize, ex);
                } else {
                    ex = new FileDownloadOutOfSpaceException(freeSpaceBytes, BUFFER_SIZE,
                            downloadedSize);
                }

            }
        }

        return ex;
    }

    private void handleSQLiteFullException(final SQLiteFullException sqLiteFullException) {
        final int id = model.getId();
        if (FileDownloadLog.NEED_LOG) {
            FileDownloadLog.d(this, "the data of the task[%d] is dirty, because the SQLite " +
                            "full exception[%s], so remove it from the database directly.",
                    id, sqLiteFullException.toString());
        }

        model.setErrMsg(sqLiteFullException.toString());
        model.setStatus(FileDownloadStatus.error);

        database.remove(id);
        database.removeConnections(id);
    }

    private void renameTempFile() throws IOException {
        final String tempPath = model.getTempFilePath();
        final String targetPath = model.getTargetFilePath();

        final File tempFile = new File(tempPath);
        try {
            final File targetFile = new File(targetPath);

            if (targetFile.exists()) {
                final long oldTargetFileLength = targetFile.length();
                if (!targetFile.delete()) {
                    throw new IOException(FileDownloadUtils.formatString(
                            "Can't delete the old file([%s], [%d]), " +
                                    "so can't replace it with the new downloaded one.",
                            targetPath, oldTargetFileLength
                    ));
                } else {
                    FileDownloadLog.w(this, "The target file([%s], [%d]) will be replaced with" +
                                    " the new downloaded file[%d]",
                            targetPath, oldTargetFileLength, tempFile.length());
                }
            }

            if (!tempFile.renameTo(targetFile)) {
                throw new IOException(FileDownloadUtils.formatString(
                        "Can't rename the  temp downloaded file(%s) to the target file(%s)",
                        tempPath, targetPath
                ));
            }
        } finally {
            if (tempFile.exists()) {
                if (!tempFile.delete()) {
                    FileDownloadLog.w(this, "delete the temp file(%s) failed, on completed downloading.",
                            tempPath);
                }
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        final int status = msg.what;

        switch (status) {
            case FileDownloadStatus.progress:
                final FileDownloadOutputStream outputStream = (FileDownloadOutputStream) msg.obj;
                final long increase;
                synchronized (increaseLock) {
                    increase = increaseBytes;
                    increaseBytes -= increase;
                }

                if (increase == 0) break;

                handleProgress(outputStream, increase, SystemClock.elapsedRealtime(),
                        msg.arg1 == 1, msg.arg2 == 1);
                break;
            case FileDownloadStatus.completed:
                if (interceptBeforeCompleted(this.increaseBytes)) {
                    return true;
                }

                try {
                    handleCompleted();
                } catch (IOException e) {
                    onError(e);
                    return true;
                }
                break;
            case FileDownloadStatus.retry:
                handleRetry((Exception) msg.obj, msg.arg1);
                break;
            case FileDownloadStatus.paused:
                handlePaused();
                break;
            case FileDownloadStatus.error:
                handleError((Exception) msg.obj);
                break;
        }

        if (FileDownloadStatus.isOver(status)) {
            handlerThread.quit();
        }

        return true;
    }

    private void handleProgress(final FileDownloadOutputStream outputStream,
                                final long increaseBytes, final long now,
                                final boolean isNeedSync, final boolean isNeedCallbackToUser) {
        final long currentProcessing = model.getSoFar() + increaseBytes;
        model.setSoFar(currentProcessing);

        if (currentProcessing == model.getTotal()) {
            database.updateProgress(model, currentProcessing);
            return;
        }

        model.setStatus(FileDownloadStatus.progress);

        if (isNeedSync) {
            try {
                outputStream.sync();
            } catch (IOException e) {
                e.printStackTrace();
            }

            database.updateProgress(model, currentProcessing);

            lastUpdateBytes = currentProcessing;
            lastUpdateTimestamp = now;
        }

        if (isNeedCallbackToUser) {
            lastCallbackTimestamp = now;
            lastCallbackBytes = currentProcessing;
            onStatusChanged(FileDownloadStatus.progress);
        }
    }

    private void handleCompleted() throws IOException {
        renameTempFile();

        database.updateComplete(model, model.getTotal());
        database.removeConnections(model.getId());

        onStatusChanged(FileDownloadStatus.completed);
    }

    private boolean interceptBeforeCompleted(long increaseBytes) {
        if (increaseBytes < 0) throw new IllegalArgumentException();
        if (increaseBytes > 0) model.setSoFar(model.getSoFar() + increaseBytes);

        if (model.isChunked()) {
            model.setTotal(model.getSoFar());
        } else if (model.getSoFar() != model.getTotal()) {
            onError(new FileDownloadGiveUpRetryException(
                    FileDownloadUtils.formatString("sofar[%d] not equal total[%d]",
                            model.getSoFar(), model.getTotal())));
            return true;
        }

        return false;
    }

    private void handleRetry(final Exception exception, final int remainRetryTimes) {
        Exception processEx = exFiltrate(exception);
        processParams.setException(processEx);
        processParams.setRetryingTimes(maxRetryTimes - remainRetryTimes);

        database.updateRetry(model, processEx);
        onStatusChanged(FileDownloadStatus.retry);
    }

    private void handlePaused() {
        database.updatePause(model, model.getSoFar());
        onStatusChanged(FileDownloadStatus.paused);
    }

    private void handleError(Exception exception) {
        Exception errProcessEx = exFiltrate(exception);

        if (errProcessEx instanceof SQLiteFullException) {
            // If the error is sqLite full exception already, no need to  update it to the database
            // again.
            handleSQLiteFullException((SQLiteFullException) errProcessEx);
        } else {
            // Normal case.
            try {
                database.updateError(model, errProcessEx, model.getSoFar());
            } catch (SQLiteFullException fullException) {
                errProcessEx = fullException;
                handleSQLiteFullException((SQLiteFullException) errProcessEx);
            }
        }

        processParams.setException(errProcessEx);
        onStatusChanged(FileDownloadStatus.error);
    }

    private boolean isNeedSync(final long now, final long currentProcessing) {
        final long bytesDelta = currentProcessing - lastUpdateBytes;
        final long timeDelta = now - lastUpdateTimestamp;

        return FileDownloadUtils.isNeedSync(bytesDelta, timeDelta);
    }

    private boolean isNeedCallbackToUser(final long now, final long currentProcessing) {
        final long callbackBytesDelta = currentProcessing - lastCallbackBytes;
        final long callbackTimeDelta = now - lastCallbackTimestamp;


        return (callbackMinIntervalBytes != NO_ANY_PROGRESS_CALLBACK && callbackBytesDelta >= callbackMinIntervalBytes)
                && (callbackTimeDelta >= callbackProgressMinInterval);
    }

    private void onStatusChanged(final byte status) {
        // In current situation, it maybe invoke this method simultaneously between #onPause() and
        // others.
        if (model.getStatus() == FileDownloadStatus.paused) {
            if (FileDownloadLog.NEED_LOG) {
                /**
                 * Already paused or the current status is paused.
                 *
                 * We don't need to call-back to Task in here, because the pause status has
                 * already handled by {@link BaseDownloadTask#pause()} manually.
                 *
                 * In some case, it will arrive here by High concurrent cause.  For performance
                 * more make sense.
                 *
                 * High concurrent cause.
                 */
                FileDownloadLog.d(this, "High concurrent cause, Already paused and we don't " +
                        "need to call-back to Task in here, %d", model.getId());
            }
            return;
        }

        MessageSnapshotFlow.getImpl().inflow(MessageSnapshotTaker.take(status, model, processParams));
    }

    public static class ProcessParams {
        private boolean isResuming;
        private Exception exception;
        private int retryingTimes;

        void setResuming(boolean isResuming) {
            this.isResuming = isResuming;
        }

        public boolean isResuming() {
            return this.isResuming;
        }

        void setException(Exception exception) {
            this.exception = exception;
        }

        void setRetryingTimes(int retryingTimes) {
            this.retryingTimes = retryingTimes;
        }

        public Exception getException() {
            return exception;
        }

        public int getRetryingTimes() {
            return retryingTimes;
        }
    }
}