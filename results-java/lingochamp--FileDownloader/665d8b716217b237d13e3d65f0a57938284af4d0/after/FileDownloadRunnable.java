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

package com.liulishuo.filedownloader.services;

import android.os.Build;
import android.os.Process;
import android.os.StatFs;
import android.text.TextUtils;

import com.liulishuo.filedownloader.BuildConfig;
import com.liulishuo.filedownloader.event.DownloadTransferEvent;
import com.liulishuo.filedownloader.model.FileDownloadHeader;
import com.liulishuo.filedownloader.model.FileDownloadModel;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.model.FileDownloadTransferModel;
import com.liulishuo.filedownloader.util.FileDownloadLog;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jacksgong on 9/24/15.
 */
class FileDownloadRunnable implements Runnable {

    private static final int BUFFER_SIZE = 1024 * 4;
    private final FileDownloadTransferModel transferModel;

    private long maxNotifyBytes;
    private int maxNotifyCounts = 0;
    private boolean isResumeDownloadAvailable;

    private FileDownloadModel model;

    private volatile boolean isRunning = false;
    private volatile boolean isPending = false;

    private final IFileDownloadDBHelper helper;
    private final OkHttpClient client;
    private final int autoRetryTimes;

    private final FileDownloadHeader header;

    public FileDownloadRunnable(final OkHttpClient client, final FileDownloadModel model,
                                final IFileDownloadDBHelper helper, final int autoRetryTimes,
                                final FileDownloadHeader header) {
        isPending = true;
        isRunning = false;

        this.client = client;
        this.helper = helper;
        this.header = header;

        transferModel = new FileDownloadTransferModel(model);
        maxNotifyCounts = model.getCallbackProgressTimes();
        maxNotifyCounts = maxNotifyCounts <= 0 ? 0 : maxNotifyCounts;

        this.isResumeDownloadAvailable = false;

        this.model = model;

        this.autoRetryTimes = autoRetryTimes;
    }

    public int getId() {
        return model.getId();
    }

    public boolean isExist() {
        return isPending || isRunning;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        isPending = false;
        isRunning = true;

        try {
            // Step 1, check model
            if (model == null) {
                FileDownloadLog.e(this, "start runnable but model == null?? %s", getId());

                this.model = helper.find(getId());

                if (this.model == null) {
                    FileDownloadLog.e(this, "start runnable but downloadMode == null?? %s", getId());
                    return;
                }
            }

            // Step 2, check status
            if (model.getStatus() != FileDownloadStatus.pending) {
                FileDownloadLog.e(this, "start runnable but status err %s", model.getStatus());

                // 极低概率事件，相同url与path的任务被放到了线程池中(目前在入池之前是有检测的，但是还是存在极低概率的同步问题) 执行的时候有可能会遇到
                onError(new RuntimeException(String.format("start runnable but status err %s", model.getStatus())));

                return;
            }

            // Step 3, start download
            loop(model);

        } finally {
            isRunning = false;
        }


    }

    private void loop(FileDownloadModel model) {
        int retryingTimes = 0;

        do {
            // loop for retry
            Response response = null;
            long soFar = 0;
            try {

                // Step 1, check is paused
                if (isCancelled()) {
                    if (FileDownloadLog.NEED_LOG) {
                        FileDownloadLog.d(this, "already canceled %d %d", model.getId(), model.getStatus());
                    }
                    onPause();
                    break;
                }

                if (FileDownloadLog.NEED_LOG) {
                    FileDownloadLog.d(FileDownloadRunnable.class, "start download %s %s", getId(), model.getUrl());
                }

                // Step 2, handle resume from breakpoint
                checkIsResumeAvailable();

                Request.Builder requestBuilder = new Request.Builder().url(model.getUrl());
                addHeader(requestBuilder);
                requestBuilder.tag(this.getId());
                // 目前没有指定cache，下载任务非普通REST请求，用户已经有了存储的地方
                requestBuilder.cacheControl(CacheControl.FORCE_NETWORK);

                // start download----------------
                // Step 3, init request
                final Request request = requestBuilder.get().build();
                if (FileDownloadLog.NEED_LOG) {
                    FileDownloadLog.d(this, "%s request header %s", getId(), request.headers());
                }

                Call call = client.newCall(request);

                // Step 4, build connect
                response = call.execute();

                final boolean isSucceedStart = response.code() == HttpURLConnection.HTTP_OK;
                final boolean isSucceedResume = response.code() == HttpURLConnection.HTTP_PARTIAL &&
                        isResumeDownloadAvailable;

                if (isResumeDownloadAvailable && !isSucceedResume) {
                    FileDownloadLog.w(this, "tried to resume from the break point[%d], but the " +
                                    "response code is %d, not 206(PARTIAL).", model.getSoFar(),
                            response.code());
                }

                if (isSucceedStart || isSucceedResume) {
                    long total = model.getTotal();
                    final String transferEncoding = response.header("Transfer-Encoding");

                    // Step 5, check response's header
                    if (isSucceedStart || total <= 0) {
                        if (transferEncoding == null) {
                            total = response.body().contentLength();
                        } else {
                            // if transfer not nil, ignore content-length
                            total = -1;
                        }
                    }

                    // TODO consider if not is chunked & http 1.0/(>=http1.1 & connect not be keep live) may not give content-length
                    if (total < 0) {
                        // invalid total length
                        final boolean isEncodingChunked = transferEncoding != null
                                && transferEncoding.equals("chunked");
                        if (!isEncodingChunked) {
                            // not chunked transfer encoding data
                            if (BuildConfig.HTTP_LENIENT) {
                                // do not response content-length either not chunk transfer encoding,
                                // but HTTP lenient is true, so handle as the case of transfer encoding chunk
                                total = -1;
                                if (FileDownloadLog.NEED_LOG) {
                                    FileDownloadLog.d(this, "%d response header is not legal but " +
                                            "HTTP lenient is true, so handle as the case of " +
                                            "transfer encoding chunk", getId());
                                }
                            } else {
                                throw new GiveUpRetryException("can't know the size of the " +
                                        "download file, and its Transfer-Encoding is not Chunked " +
                                        "either.\nyou can ignore such exception by add " +
                                        "http.lenient=true to the filedownloader.properties");
                            }
                        }
                    }

                    if (isSucceedResume) {
                        soFar = model.getSoFar();
                    }

                    // Step 6, update header to db. for save etag.
                    updateHeader(response);
                    // Step 7, callback on connected.
                    onConnected(isSucceedResume, soFar, total);

                    // Step 8, start fetch datum from input stream & write to file
                    if (fetch(response, isSucceedResume, soFar, total)) {
                        break;
                    }

                } else {
                    throw new HttpRequestException(request, response);
                }


            } catch (Throwable ex) {
                // TODO 决策是否需要重试，是否是用户决定，或者根据错误码处理
                if (autoRetryTimes > retryingTimes++
                        && !(ex instanceof GiveUpRetryException)) {
                    // retry
                    onRetry(ex, retryingTimes, soFar);
                    continue;
                } else {
                    // error
                    onError(ex);
                    break;
                }
            } finally {
                if (response != null && response.body() != null) {
                    response.body().close();
                }
            }

        } while (true);
    }

    private boolean fetch(Response response, boolean isSucceedContinue,
                          long soFar, long total) throws Throwable {
        // fetching datum
        InputStream inputStream = null;
        final RandomAccessFile accessFile = getRandomAccessFile(isSucceedContinue, total);
        try {
            // Step 1, get input stream
            inputStream = response.body().byteStream();
            byte[] buff = new byte[BUFFER_SIZE];
            maxNotifyBytes = maxNotifyCounts <= 0 ? -1 : total / maxNotifyCounts;

            // enter fetching loop(Step 2->6)
            do {
                // Step 2, read from input stream.
                int byteCount = inputStream.read(buff);
                if (byteCount == -1) {
                    break;
                }

                // Step 3, writ to file
                accessFile.write(buff, 0, byteCount);

                // Step 4, adapter sofar
                soFar += byteCount;

                // Step 5, check whether file is changed by others
                if (accessFile.length() < soFar) {
                    throw new RuntimeException(String.format("the file was changed by others when" +
                            " downloading. %d %d", accessFile.length(), soFar));
                } else {
                    // callback on progressing
                    onProgress(soFar, total);
                }

                // Step 6, check pause
                if (isCancelled()) {
                    // callback on paused
                    onPause();
                    return true;
                }

            } while (true);


            // Step 7, adapter chuncked transfer encoding
            if (total == -1) {
                total = soFar;
            }

            // Step 8, complte download
            if (soFar == total) {
                // callback on completed
                onComplete(total);

                // 成功
                return true;
            } else {
                throw new RuntimeException(
                        String.format("sofar[%d] not equal total[%d]", soFar, total));
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (accessFile != null) {
                accessFile.close();
            }
        }
    }

    private void addHeader(Request.Builder builder) {
        if (header != null && header.getNamesAndValues() != null) {
            if (FileDownloadLog.NEED_LOG) {
                FileDownloadLog.v(this, "%d add outside header: %s", getId(), header);
            }
            builder.headers(Headers.of(header.getNamesAndValues()));
        }

        if (isResumeDownloadAvailable) {
            if (!TextUtils.isEmpty(model.getETag())) {
                builder.addHeader("If-Match", model.getETag());
            }
            builder.addHeader("Range", String.format("bytes=%d-", model.getSoFar()));
        }
    }

    private void updateHeader(Response response) {
        if (response == null) {
            throw new RuntimeException("response is null when updateHeader");
        }

        boolean needRefresh = false;
        final String oldEtag = model.getETag();
        final String newEtag = response.header("Etag");

        if (FileDownloadLog.NEED_LOG) {
            FileDownloadLog.d(this, "etag find by header %d %s", getId(), newEtag);
        }

        if (oldEtag == null && newEtag != null) {
            needRefresh = true;
        } else if (oldEtag != null && newEtag != null && !oldEtag.equals(newEtag)) {
            needRefresh = true;
        }

        if (needRefresh) {
            helper.updateHeader(getId(), newEtag);
        }

    }

    private void onConnected(final boolean resuming, final long soFar, final long total) {
        helper.update(getId(), FileDownloadStatus.connected, soFar, total);

        transferModel.setSoFarBytes(soFar);
        transferModel.setTotalBytes(total);
        transferModel.setEtag(model.getETag());
        transferModel.setResuming(resuming);
        transferModel.setStatus(FileDownloadStatus.connected);

        onStatusChanged(model.getStatus());
    }

    private long lastNotifiedSoFar = 0;
    private final DownloadTransferEvent event = new DownloadTransferEvent(null);

    private void onProgress(final long soFar, final long total) {
        helper.update(getId(), FileDownloadStatus.progress, soFar, total);

        transferModel.setSoFarBytes(soFar);
        transferModel.setTotalBytes(total);
        transferModel.setStatus(FileDownloadStatus.progress);

        if (maxNotifyBytes < 0 || soFar - lastNotifiedSoFar < maxNotifyBytes) {
            return;
        }

        lastNotifiedSoFar = soFar;
        if (FileDownloadLog.NEED_LOG) {
            FileDownloadLog.d(this, "On progress %d %d %d", getId(), soFar, total);
        }

        onStatusChanged(model.getStatus());

    }

    private void onRetry(Throwable ex, final int retryTimes, final long soFarBytes) {
        if (FileDownloadLog.NEED_LOG) {
            FileDownloadLog.d(this, "On retry %d %s %d %d", getId(), ex,
                    retryTimes, autoRetryTimes);
        }

        ex = exFiltrate(ex);
        helper.updateRetry(getId(), ex.getMessage(), retryTimes, soFarBytes);

        transferModel.setStatus(FileDownloadStatus.retry);
        transferModel.setThrowable(ex);
        transferModel.setRetryingTimes(retryTimes);
        transferModel.setSoFarBytes(soFarBytes);

        onStatusChanged(model.getStatus());
    }

    private void onError(Throwable ex) {
        if (FileDownloadLog.NEED_LOG) {
            FileDownloadLog.d(this, "On error %d %s", getId(), ex);
        }

        ex = exFiltrate(ex);
        helper.updateError(getId(), ex.getMessage());

        transferModel.setStatus(FileDownloadStatus.error);
        transferModel.setThrowable(ex);


        onStatusChanged(model.getStatus());
    }

    private void onComplete(final long total) {
        if (FileDownloadLog.NEED_LOG) {
            FileDownloadLog.d(this, "On completed %d %d %B", getId(), total, isCancelled());
        }
        helper.updateComplete(getId(), total);

        transferModel.setStatus(FileDownloadStatus.completed);
        transferModel.setSoFarBytes(total);
        transferModel.setTotalBytes(total);

        onStatusChanged(model.getStatus());
    }

    private void onPause() {
        this.isRunning = false;
        if (FileDownloadLog.NEED_LOG) {
            FileDownloadLog.d(this, "On paused %d %d %d", getId(),
                    model.getSoFar(), model.getTotal());
        }

        helper.updatePause(getId());

        transferModel.setStatus(FileDownloadStatus.paused);

        // 这边没有必要从服务端再回调，由于直接调pause看是否已经成功
//        onStatusChanged(model.getStatus());
    }

    public void onResume() {
        if (FileDownloadLog.NEED_LOG) {
            FileDownloadLog.d(this, "On resume %d", getId());
        }

        this.isPending = true;

        helper.updatePending(getId());

        model.setStatus(FileDownloadStatus.pending);

        onStatusChanged(model.getStatus());
    }

    private void onStatusChanged(int status){
        if (status == FileDownloadStatus.progress || FileDownloadStatus.isOver(status)) {
            FileDownloadProcessEventPool.getImpl().
                    asyncPublishInNewThread(event.setTransfer(transferModel));
        }else {
            FileDownloadProcessEventPool.getImpl().
                    asyncPublishInNewThread(new DownloadTransferEvent(transferModel.copy()));
        }
    }

    private boolean isCancelled() {
        return this.model.isCanceled();
    }

    // ----------------------------------
    private RandomAccessFile getRandomAccessFile(final boolean append, final long totalBytes)
            throws Throwable {
        final String path = model.getPath();
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("found invalid internal destination path, empty");
        }

        if (!FileDownloadUtils.isFilenameValid(path)) {
            throw new RuntimeException(String.format("found invalid internal destination filename" +
                    " %s", path));
        }

        File file = new File(path);

        if (file.exists() && file.isDirectory()) {
            throw new RuntimeException(String.format("found invalid internal destination path[%s]," +
                    " & path is directory[%B]", path, file.isDirectory()));
        }
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException(String.format("create new file error  %s", file.getAbsolutePath()));
            }
        }

        RandomAccessFile outFd = new RandomAccessFile(file, "rw");

        // check the available space bytes whether enough or not.
        if (totalBytes > 0) {
            final long curSize = outFd.length();
            final long needAvailableSpace = totalBytes - curSize;

            long availableBytes;
            final StatFs statFs = new StatFs(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableBytes = statFs.getAvailableBytes();
            } else {
                availableBytes = statFs.getAvailableBlocks() * statFs.getBlockSize();
            }

            if (availableBytes < needAvailableSpace) {
                outFd.close();
                // throw io exception.
                throw new IOException(
                        String.format("The file is too large to store, the downloaded size: " +
                                " %d, requirements: %d, but the available space size: " +
                                "%d", curSize, needAvailableSpace, availableBytes));
            } else {
                // pre allocate.
                outFd.setLength(totalBytes);
            }
        }

        if (append) {
            outFd.seek(model.getSoFar());
        }

        return outFd;
    }

    private void checkIsResumeAvailable() {
        if (FileDownloadMgr.checkBreakpointAvailable(getId(), this.model)) {
            this.isResumeDownloadAvailable = true;
        } else {
            this.isResumeDownloadAvailable = false;
            // delete dirty file
            File file = new File(model.getPath());
            file.delete();
        }
    }

    private Throwable exFiltrate(Throwable ex) {
        if (TextUtils.isEmpty(ex.getMessage())) {
            if (ex instanceof SocketTimeoutException) {
                ex = new RuntimeException(ex.getClass().getSimpleName(), ex);
            }
        }

        return ex;
    }

    public static class GiveUpRetryException extends RuntimeException {
        public GiveUpRetryException(final String detailMessage) {
            super(detailMessage);
        }
    }

    public static class HttpRequestException extends RuntimeException {
        public HttpRequestException(final Request request, final Response response) {
            super(String.format("response code error: %d, \n request headers: %s \n " +
                    "response headers: %s", response.code(), request.headers(), response.headers()));
        }
    }
}