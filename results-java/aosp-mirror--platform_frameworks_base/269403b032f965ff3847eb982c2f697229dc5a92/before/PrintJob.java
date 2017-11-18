/*
 * Copyright (C) 2013 The Android Open Source Project
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

package android.printservice;

import android.os.RemoteException;
import android.print.PrintJobInfo;
import android.util.Log;

/**
 * This class represents a print job from the perspective of a print
 * service. It provides APIs for observing the print job state and
 * performing operations on the print job.
 */
public final class PrintJob {

    private static final String LOG_TAG = "PrintJob";

    private final IPrintServiceClient mPrintServiceClient;

    private final PrintDocument mDocument;

    private PrintJobInfo mCachedInfo;

    PrintJob(PrintJobInfo jobInfo, IPrintServiceClient client) {
        mCachedInfo = jobInfo;
        mPrintServiceClient = client;
        mDocument = new PrintDocument(mCachedInfo.getId(), client,
                jobInfo.getDocumentInfo());
    }

    /**
     * Gets the unique print job id.
     *
     * @return The id.
     */
    public int getId() {
        return mCachedInfo.getId();
    }

    /**
     * Gets the {@link PrintJobInfo} that describes this job.
     * <p>
     * <strong>Node:</strong>The returned info object is a snapshot of the
     * current print job state. Every call to this method returns a fresh
     * info object that reflects the current print job state.
     * </p>
     *
     * @return The print job info.
     */
    public PrintJobInfo getInfo() {
        if (isInImmutableState()) {
            return mCachedInfo;
        }
        PrintJobInfo info = null;
        try {
            info = mPrintServiceClient.getPrintJobInfo(mCachedInfo.getId());
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Couldn't get info for job: " + mCachedInfo.getId(), re);
        }
        if (info != null) {
            mCachedInfo = info;
        }
        return mCachedInfo;
    }

    /**
     * Gets the printed document.
     *
     * @return The document.
     */
    public PrintDocument getDocument() {
        return mDocument;
    }

    /**
     * Gets whether this print job is queued. Such a print job is
     * ready to be printed and can be started or cancelled.
     *
     * @return Whether the print job is queued.
     *
     * @see #start()
     * @see #cancel()
     */
    public boolean isQueued() {
        return getInfo().getState() == PrintJobInfo.STATE_QUEUED;
    }

    /**
     * Gets whether this print job is started. Such a print job is
     * being printed and can be completed or canceled or failed.
     *
     * @return Whether the print job is started.
     *
     * @see #complete()
     * @see #cancel()
     * @see #fail(CharSequence)
     */
    public boolean isStarted() {
        return getInfo().getState() == PrintJobInfo.STATE_STARTED;
    }

    /**
     * Gets whether this print job is completed. Such a print job
     * is successfully printed. This is a final state.
     *
     * @return Whether the print job is completed.
     *
     * @see #complete()
     */
    public boolean isCompleted() {
        return getInfo().getState() == PrintJobInfo.STATE_COMPLETED;
    }

    /**
     * Gets whether this print job is failed. Such a print job is
     * not successfully printed due to an error. This is a final state.
     *
     * @return Whether the print job is failed.
     *
     * @see #fail(CharSequence)
     */
    public boolean isFailed() {
        return getInfo().getState() == PrintJobInfo.STATE_FAILED;
    }

    /**
     * Gets whether this print job is cancelled. Such a print job was
     * cancelled as a result of a user request. This is a final state.
     *
     * @return Whether the print job is cancelled.
     *
     * @see #cancel()
     */
    public boolean isCancelled() {
        return getInfo().getState() == PrintJobInfo.STATE_FAILED;
    }

    /**
     * Starts the print job. You should call this method if {@link
     * #isQueued()} returns true and you started printing.
     *
     * @return Whether the job as started.
     *
     * @see #isQueued()
     */
    public boolean start() {
        if (isQueued()) {
            return setState(PrintJobInfo.STATE_STARTED, null);
        }
        return false;
    }

    /**
     * Completes the print job. You should call this method if {@link
     * #isStarted()} returns true and you are done printing.
     *
     * @return Whether the job as completed.
     *
     * @see #isStarted()
     */
    public boolean complete() {
        if (isStarted()) {
            return setState(PrintJobInfo.STATE_COMPLETED, null);
        }
        return false;
    }

    /**
     * Fails the print job. You should call this method if {@link
     * #isQueued()} or {@link #isStarted()} returns true you failed
     * while printing.
     *
     * @param error The human readable, short, and translated reason
     * for the failure.
     * @return Whether the job was failed.
     *
     * @see #isQueued()
     * @see #isStarted()
     */
    public boolean fail(CharSequence error) {
        if (isQueued() || isStarted()) {
            return setState(PrintJobInfo.STATE_FAILED, error);
        }
        return false;
    }

    /**
     * Cancels the print job. You should call this method if {@link
     * #isQueued()} or {@link #isStarted()} returns true and you canceled
     * the print job as a response to a call to {@link
     * PrintService#onRequestCancelPrintJob(PrintJob)}.
     *
     * @return Whether the job is canceled.
     *
     * @see #isStarted()
     * @see #isQueued()
     */
    public boolean cancel() {
        if (isQueued() || isStarted()) {
            return setState(PrintJobInfo.STATE_CANCELED, null);
        }
        return false;
    }

    /**
     * Sets a tag that is valid in the context of a {@link PrintService}
     * and is not interpreted by the system. For example, a print service
     * may set as a tag the key of the print job returned by a remote
     * print server, if the printing is off handed to a cloud based service.
     *
     * @param tag The tag.
     * @return True if the tag was set, false otherwise.
     */
    public boolean setTag(String tag) {
        if (isInImmutableState()) {
            return false;
        }
        try {
            return mPrintServiceClient.setPrintJobTag(mCachedInfo.getId(), tag);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error setting tag for job: " + mCachedInfo.getId(), re);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PrintJob other = (PrintJob) obj;
        return (mCachedInfo.getId() == other.mCachedInfo.getId());
    }

    @Override
    public int hashCode() {
        return mCachedInfo.getId();
    }

    private boolean isInImmutableState() {
        final int state = mCachedInfo.getState();
        return state == PrintJobInfo.STATE_COMPLETED
                || state == PrintJobInfo.STATE_CANCELED;
    }

    private boolean setState(int state, CharSequence error) {
        try {
            if (mPrintServiceClient.setPrintJobState(mCachedInfo.getId(), state, error)) {
                // Best effort - update the state of the cached info since
                // we may not be able to re-fetch it later if the job gets
                // removed from the spooler as a result of the state change.
                mCachedInfo.setState(state);
                mCachedInfo.setFailureReason(error);
                return true;
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error setting the state of job: " + mCachedInfo.getId(), re);
        }
        return false;
    }
}