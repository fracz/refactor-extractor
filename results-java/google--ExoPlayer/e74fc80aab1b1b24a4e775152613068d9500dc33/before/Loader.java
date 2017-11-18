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
package com.google.android.exoplayer.upstream;

import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.TraceUtil;
import com.google.android.exoplayer.util.Util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Manages the background loading of {@link Loadable}s.
 */
public final class Loader {

  /**
   * Thrown when an unexpected exception is encountered during loading.
   */
  public static final class UnexpectedLoaderException extends IOException {

    public UnexpectedLoaderException(Exception cause) {
      super("Unexpected " + cause.getClass().getSimpleName() + ": " + cause.getMessage(), cause);
    }

  }

  /**
   * Interface definition of an object that can be loaded using a {@link Loader}.
   */
  public interface Loadable {

    /**
     * Cancels the load.
     */
    void cancelLoad();

    /**
     * Whether the load has been canceled.
     *
     * @return True if the load has been canceled. False otherwise.
     */
    boolean isLoadCanceled();

    /**
     * Performs the load, returning on completion or cancelation.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    void load() throws IOException, InterruptedException;

  }

  /**
   * Interface definition for a callback to be notified of {@link Loader} events.
   */
  public interface Callback {

    /**
     * Invoked when a load has been canceled.
     *
     * @param loadable The loadable whose load has been canceled.
     */
    void onLoadCanceled(Loadable loadable);

    /**
     * Invoked when a load has completed.
     *
     * @param loadable The loadable whose load has completed.
     */
    void onLoadCompleted(Loadable loadable);

    /**
     * Invoked when a load encounters an error.
     *
     * @param loadable The loadable whose load has encountered an error.
     * @param exception The error.
     * @return The desired retry action. One of {@link Loader#DONT_RETRY}, {@link Loader#RETRY} and
     *     {@link Loader#RETRY_RESET_ERROR_COUNT}.
     */
    int onLoadError(Loadable loadable, IOException exception);

  }

  public static final int DONT_RETRY = 0;
  public static final int RETRY = 1;
  public static final int RETRY_RESET_ERROR_COUNT = 2;

  private static final int MSG_START = 0;
  private static final int MSG_CANCEL = 1;
  private static final int MSG_END_OF_SOURCE = 2;
  private static final int MSG_IO_EXCEPTION = 3;
  private static final int MSG_FATAL_ERROR = 4;

  private final ExecutorService downloadExecutorService;

  private int minRetryCount;
  private LoadTask currentTask;

  /**
   * @param threadName A name for the loader's thread.
   * @param minRetryCount The minimum retry count.
   */
  public Loader(String threadName, int minRetryCount) {
    this.downloadExecutorService = Util.newSingleThreadExecutor(threadName);
    this.minRetryCount = minRetryCount;
  }

  /**
   * Start loading a {@link Loadable}.
   * <p>
   * The calling thread must be a {@link Looper} thread, which is the thread on which the
   * {@link Callback} will be invoked.
   *
   * @param loadable The {@link Loadable} to load.
   * @param callback A callback to invoke when the load ends.
   * @throws IllegalStateException If the calling thread does not have an associated {@link Looper}.
   */
  public void startLoading(Loadable loadable, Callback callback) {
    Looper looper = Looper.myLooper();
    Assertions.checkState(looper != null);
    new LoadTask(looper, loadable, callback).start(0);
  }

  /**
   * Whether the {@link Loader} is currently loading a {@link Loadable}.
   *
   * @return Whether the {@link Loader} is currently loading a {@link Loadable}.
   */
  public boolean isLoading() {
    return currentTask != null;
  }

  /**
   * Sets the minimum retry count.
   *
   * @param minRetryCount The minimum retry count.
   */
  public void setMinRetryCount(int minRetryCount) {
    this.minRetryCount = minRetryCount;
  }

  /**
   * If the current {@link Loadable} has incurred a number of errors greater than the minimum
   * number of retries and if the load is currently backed off, then the most recent error is
   * thrown. Else does nothing.
   *
   * @throws IOException The most recent error encountered by the current {@link Loadable}.
   */
  public void maybeThrowError() throws IOException {
    if (currentTask != null) {
      currentTask.maybeThrowError(minRetryCount);
    }
  }

  /**
   * Cancels the current load.
   * <p>
   * This method should only be called when a load is in progress.
   */
  public void cancelLoading() {
    currentTask.cancel();
  }

  /**
   * Releases the {@link Loader}.
   * <p>
   * This method should be called when the {@link Loader} is no longer required.
   */
  public void release() {
    if (currentTask != null) {
      cancelLoading();
    }
    downloadExecutorService.shutdown();
  }

  @SuppressLint("HandlerLeak")
  private final class LoadTask extends Handler implements Runnable {

    private static final String TAG = "LoadTask";

    private final Loadable loadable;
    private final Loader.Callback callback;

    private IOException currentError;
    private int errorCount;

    private volatile Thread executorThread;

    public LoadTask(Looper looper, Loadable loadable, Loader.Callback callback) {
      super(looper);
      this.loadable = loadable;
      this.callback = callback;
    }

    public void maybeThrowError(int minRetryCount) throws IOException {
      if (currentError != null && errorCount > minRetryCount) {
        throw currentError;
      }
    }

    public void start(long delayMillis) {
      Assertions.checkState(currentTask == null);
      currentTask = this;
      if (delayMillis > 0) {
        sendEmptyMessageDelayed(MSG_START, delayMillis);
      } else {
        submitToExecutor();
      }
    }

    public void cancel() {
      currentError = null;
      if (hasMessages(MSG_START)) {
        removeMessages(MSG_START);
        sendEmptyMessage(MSG_CANCEL);
      } else {
        loadable.cancelLoad();
        if (executorThread != null) {
          executorThread.interrupt();
        }
      }
    }

    @Override
    public void run() {
      try {
        executorThread = Thread.currentThread();
        if (!loadable.isLoadCanceled()) {
          TraceUtil.beginSection(loadable.getClass().getSimpleName() + ".load()");
          try {
            loadable.load();
          } finally {
            TraceUtil.endSection();
          }
        }
        sendEmptyMessage(MSG_END_OF_SOURCE);
      } catch (IOException e) {
        obtainMessage(MSG_IO_EXCEPTION, e).sendToTarget();
      } catch (InterruptedException e) {
        // The load was canceled.
        Assertions.checkState(loadable.isLoadCanceled());
        sendEmptyMessage(MSG_END_OF_SOURCE);
      } catch (Exception e) {
        // This should never happen, but handle it anyway.
        Log.e(TAG, "Unexpected exception loading stream", e);
        obtainMessage(MSG_IO_EXCEPTION, new UnexpectedLoaderException(e)).sendToTarget();
      } catch (Error e) {
        // We'd hope that the platform would kill the process if an Error is thrown here, but the
        // executor may catch the error (b/20616433). Throw it here, but also pass and throw it from
        // the handler thread so that the process dies even if the executor behaves in this way.
        Log.e(TAG, "Unexpected error loading stream", e);
        obtainMessage(MSG_FATAL_ERROR, e).sendToTarget();
        throw e;
      }
    }

    @Override
    public void handleMessage(Message msg) {
      if (msg.what == MSG_START) {
        submitToExecutor();
        return;
      }
      if (msg.what == MSG_FATAL_ERROR) {
        throw (Error) msg.obj;
      }
      finish();
      if (loadable.isLoadCanceled()) {
        callback.onLoadCanceled(loadable);
        return;
      }
      switch (msg.what) {
        case MSG_CANCEL:
          callback.onLoadCanceled(loadable);
          break;
        case MSG_END_OF_SOURCE:
          callback.onLoadCompleted(loadable);
          break;
        case MSG_IO_EXCEPTION:
          currentError = (IOException) msg.obj;
          int retryAction = callback.onLoadError(loadable, currentError);
          if (retryAction != DONT_RETRY) {
            errorCount = retryAction == RETRY_RESET_ERROR_COUNT ? 1 : errorCount + 1;
            start(getRetryDelayMillis());
          }
          break;
      }
    }

    private void submitToExecutor() {
      currentError = null;
      downloadExecutorService.submit(currentTask);
    }

    private void finish() {
      currentTask = null;
    }

    private long getRetryDelayMillis() {
      return Math.min((errorCount - 1) * 1000, 5000);
    }

  }

}