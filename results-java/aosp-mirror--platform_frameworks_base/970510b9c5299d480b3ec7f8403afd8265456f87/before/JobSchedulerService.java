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
 * limitations under the License
 */

package com.android.server.job;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.IUidObserver;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.job.IJobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import android.util.SparseArray;

import android.util.SparseBooleanArray;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.ArrayUtils;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.job.JobStore.JobStatusFunctor;
import com.android.server.job.controllers.AppIdleController;
import com.android.server.job.controllers.BatteryController;
import com.android.server.job.controllers.ConnectivityController;
import com.android.server.job.controllers.ContentObserverController;
import com.android.server.job.controllers.IdleController;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.StateController;
import com.android.server.job.controllers.TimeController;

import libcore.util.EmptyArray;

/**
 * Responsible for taking jobs representing work to be performed by a client app, and determining
 * based on the criteria specified when that job should be run against the client application's
 * endpoint.
 * Implements logic for scheduling, and rescheduling jobs. The JobSchedulerService knows nothing
 * about constraints, or the state of active jobs. It receives callbacks from the various
 * controllers and completed jobs and operates accordingly.
 *
 * Note on locking: Any operations that manipulate {@link #mJobs} need to lock on that object.
 * Any function with the suffix 'Locked' also needs to lock on {@link #mJobs}.
 * @hide
 */
public final class JobSchedulerService extends com.android.server.SystemService
        implements StateChangedListener, JobCompletedListener {
    static final String TAG = "JobSchedulerService";
    public static final boolean DEBUG = false;

    /** The number of concurrent jobs we run at one time. */
    private static final int MAX_JOB_CONTEXTS_COUNT
            = ActivityManager.isLowRamDeviceStatic() ? 3 : 6;
    /** Enforce a per-app limit on scheduled jobs? */
    private static final boolean ENFORCE_MAX_JOBS = false;
    /** The maximum number of jobs that we allow an unprivileged app to schedule */
    private static final int MAX_JOBS_PER_APP = 100;

    /** Global local for all job scheduler state. */
    final Object mLock = new Object();
    /** Master list of jobs. */
    final JobStore mJobs;

    static final int MSG_JOB_EXPIRED = 0;
    static final int MSG_CHECK_JOB = 1;
    static final int MSG_STOP_JOB = 2;
    static final int MSG_CHECK_JOB_GREEDY = 3;

    // Policy constants
    /**
     * Minimum # of idle jobs that must be ready in order to force the JMS to schedule things
     * early.
     */
    static final int MIN_IDLE_COUNT = 1;
    /**
     * Minimum # of charging jobs that must be ready in order to force the JMS to schedule things
     * early.
     */
    static final int MIN_CHARGING_COUNT = 1;
    /**
     * Minimum # of connectivity jobs that must be ready in order to force the JMS to schedule
     * things early.
     */
    static final int MIN_CONNECTIVITY_COUNT = 1;  // Run connectivity jobs as soon as ready.
    /**
     * Minimum # of content trigger jobs that must be ready in order to force the JMS to schedule
     * things early.
     */
    static final int MIN_CONTENT_COUNT = 1;
    /**
     * Minimum # of jobs (with no particular constraints) for which the JMS will be happy running
     * some work early.
     * This is correlated with the amount of batching we'll be able to do.
     */
    static final int MIN_READY_JOBS_COUNT = 2;

    /**
     * Track Services that have currently active or pending jobs. The index is provided by
     * {@link JobStatus#getServiceToken()}
     */
    final List<JobServiceContext> mActiveServices = new ArrayList<>();
    /** List of controllers that will notify this service of updates to jobs. */
    List<StateController> mControllers;
    /**
     * Queue of pending jobs. The JobServiceContext class will receive jobs from this list
     * when ready to execute them.
     */
    final ArrayList<JobStatus> mPendingJobs = new ArrayList<>();

    int[] mStartedUsers = EmptyArray.INT;

    final JobHandler mHandler;
    final JobSchedulerStub mJobSchedulerStub;

    IBatteryStats mBatteryStats;
    PowerManager mPowerManager;
    DeviceIdleController.LocalService mLocalDeviceIdleController;

    /**
     * Set to true once we are allowed to run third party apps.
     */
    boolean mReadyToRock;

    /**
     * True when in device idle mode, so we don't want to schedule any jobs.
     */
    boolean mDeviceIdleMode;

    /**
     * What we last reported to DeviceIdleController about whether we are active.
     */
    boolean mReportedActive;

    /**
     * Which uids are currently in the foreground.
     */
    final SparseBooleanArray mForegroundUids = new SparseBooleanArray();

    /**
     * Cleans up outstanding jobs when a package is removed. Even if it's being replaced later we
     * still clean up. On reinstall the package will have a new uid.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Slog.d(TAG, "Receieved: " + intent.getAction());
            if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                // If this is an outright uninstall rather than the first half of an
                // app update sequence, cancel the jobs associated with the app.
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    int uidRemoved = intent.getIntExtra(Intent.EXTRA_UID, -1);
                    if (DEBUG) {
                        Slog.d(TAG, "Removing jobs for uid: " + uidRemoved);
                    }
                    cancelJobsForUid(uidRemoved, true);
                }
            } else if (Intent.ACTION_USER_REMOVED.equals(intent.getAction())) {
                final int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, 0);
                if (DEBUG) {
                    Slog.d(TAG, "Removing jobs for user: " + userId);
                }
                cancelJobsForUser(userId);
            } else if (PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED.equals(intent.getAction())
                    || PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED.equals(intent.getAction())) {
                updateIdleMode(mPowerManager != null
                        ? (mPowerManager.isDeviceIdleMode()
                        || mPowerManager.isLightDeviceIdleMode())
                        : false);
            }
        }
    };

    final private IUidObserver mUidObserver = new IUidObserver.Stub() {
        @Override public void onUidStateChanged(int uid, int procState) throws RemoteException {
            updateUidState(uid, procState);
        }

        @Override public void onUidGone(int uid) throws RemoteException {
            updateUidState(uid, ActivityManager.PROCESS_STATE_CACHED_EMPTY);
        }

        @Override public void onUidActive(int uid) throws RemoteException {
        }

        @Override public void onUidIdle(int uid) throws RemoteException {
            cancelJobsForUid(uid, false);
        }
    };

    public Object getLock() {
        return mLock;
    }

    @Override
    public void onStartUser(int userHandle) {
        mStartedUsers = ArrayUtils.appendInt(mStartedUsers, userHandle);
        // Let's kick any outstanding jobs for this user.
        mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    @Override
    public void onUnlockUser(int userHandle) {
        // Let's kick any outstanding jobs for this user.
        mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    @Override
    public void onStopUser(int userHandle) {
        mStartedUsers = ArrayUtils.removeInt(mStartedUsers, userHandle);
    }

    /**
     * Entry point from client to schedule the provided job.
     * This cancels the job if it's already been scheduled, and replaces it with the one provided.
     * @param job JobInfo object containing execution parameters
     * @param uId The package identifier of the application this job is for.
     * @return Result of this operation. See <code>JobScheduler#RESULT_*</code> return codes.
     */
    public int schedule(JobInfo job, int uId) {
        return scheduleAsPackage(job, uId, null, -1, null);
    }

    public int scheduleAsPackage(JobInfo job, int uId, String packageName, int userId,
            String tag) {
        JobStatus jobStatus = JobStatus.createFromJobInfo(job, uId, packageName, userId, tag);
        try {
            if (ActivityManagerNative.getDefault().getAppStartMode(uId,
                    job.getService().getPackageName()) == ActivityManager.APP_START_MODE_DISABLED) {
                Slog.w(TAG, "Not scheduling job " + uId + ":" + job.toString()
                        + " -- package not allowed to start");
                return JobScheduler.RESULT_FAILURE;
            }
        } catch (RemoteException e) {
        }
        if (DEBUG) Slog.d(TAG, "SCHEDULE: " + jobStatus.toShortString());
        JobStatus toCancel;
        synchronized (mLock) {
            // Jobs on behalf of others don't apply to the per-app job cap
            if (ENFORCE_MAX_JOBS && packageName == null) {
                if (mJobs.countJobsForUid(uId) > MAX_JOBS_PER_APP) {
                    Slog.w(TAG, "Too many jobs for uid " + uId);
                    throw new IllegalStateException("Apps may not schedule more than "
                                + MAX_JOBS_PER_APP + " distinct jobs");
                }
            }

            toCancel = mJobs.getJobByUidAndJobId(uId, job.getId());
        }
        startTrackingJob(jobStatus, toCancel);
        if (toCancel != null) {
            cancelJobImpl(toCancel);
        }
        mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
        return JobScheduler.RESULT_SUCCESS;
    }

    public List<JobInfo> getPendingJobs(int uid) {
        synchronized (mLock) {
            List<JobStatus> jobs = mJobs.getJobsByUid(uid);
            ArrayList<JobInfo> outList = new ArrayList<JobInfo>(jobs.size());
            for (int i = jobs.size() - 1; i >= 0; i--) {
                JobStatus job = jobs.get(i);
                outList.add(job.getJob());
            }
            return outList;
        }
    }

    void cancelJobsForUser(int userHandle) {
        List<JobStatus> jobsForUser;
        synchronized (mLock) {
            jobsForUser = mJobs.getJobsByUser(userHandle);
        }
        for (int i=0; i<jobsForUser.size(); i++) {
            JobStatus toRemove = jobsForUser.get(i);
            cancelJobImpl(toRemove);
        }
    }

    /**
     * Entry point from client to cancel all jobs originating from their uid.
     * This will remove the job from the master list, and cancel the job if it was staged for
     * execution or being executed.
     * @param uid Uid to check against for removal of a job.
     * @param forceAll If true, all jobs for the uid will be canceled; if false, only those
     * whose apps are stopped.
     */
    public void cancelJobsForUid(int uid, boolean forceAll) {
        List<JobStatus> jobsForUid;
        synchronized (mLock) {
            jobsForUid = mJobs.getJobsByUid(uid);
        }
        for (int i=0; i<jobsForUid.size(); i++) {
            JobStatus toRemove = jobsForUid.get(i);
            if (!forceAll) {
                String packageName = toRemove.getServiceComponent().getPackageName();
                try {
                    if (ActivityManagerNative.getDefault().getAppStartMode(uid, packageName)
                            != ActivityManager.APP_START_MODE_DISABLED) {
                        continue;
                    }
                } catch (RemoteException e) {
                }
            }
            cancelJobImpl(toRemove);
        }
    }

    /**
     * Entry point from client to cancel the job corresponding to the jobId provided.
     * This will remove the job from the master list, and cancel the job if it was staged for
     * execution or being executed.
     * @param uid Uid of the calling client.
     * @param jobId Id of the job, provided at schedule-time.
     */
    public void cancelJob(int uid, int jobId) {
        JobStatus toCancel;
        synchronized (mLock) {
            toCancel = mJobs.getJobByUidAndJobId(uid, jobId);
        }
        if (toCancel != null) {
            cancelJobImpl(toCancel);
        }
    }

    private void cancelJobImpl(JobStatus cancelled) {
        if (DEBUG) Slog.d(TAG, "CANCEL: " + cancelled.toShortString());
        stopTrackingJob(cancelled, true /* writeBack */);
        synchronized (mLock) {
            // Remove from pending queue.
            mPendingJobs.remove(cancelled);
            // Cancel if running.
            stopJobOnServiceContextLocked(cancelled, JobParameters.REASON_CANCELED);
            reportActive();
        }
    }

    void updateUidState(int uid, int procState) {
        synchronized (mLock) {
            boolean foreground = procState <= ActivityManager.PROCESS_STATE_FOREGROUND_SERVICE;
            boolean changed = false;
            if (foreground) {
                if (!mForegroundUids.get(uid)) {
                    changed = true;
                    mForegroundUids.put(uid, true);
                }
            } else {
                int index = mForegroundUids.indexOfKey(uid);
                if (index >= 0) {
                    mForegroundUids.removeAt(index);
                    changed = true;
                }
            }
        }
    }

    void updateIdleMode(boolean enabled) {
        boolean changed = false;
        boolean rocking;
        synchronized (mLock) {
            if (mDeviceIdleMode != enabled) {
                changed = true;
            }
            rocking = mReadyToRock;
        }
        if (changed) {
            if (rocking) {
                for (int i=0; i<mControllers.size(); i++) {
                    mControllers.get(i).deviceIdleModeChanged(enabled);
                }
            }
            synchronized (mLock) {
                mDeviceIdleMode = enabled;
                if (enabled) {
                    // When becoming idle, make sure no jobs are actively running.
                    for (int i=0; i<mActiveServices.size(); i++) {
                        JobServiceContext jsc = mActiveServices.get(i);
                        final JobStatus executing = jsc.getRunningJob();
                        if (executing != null) {
                            jsc.cancelExecutingJob(JobParameters.REASON_DEVICE_IDLE);
                        }
                    }
                } else {
                    // When coming out of idle, allow thing to start back up.
                    if (rocking) {
                        if (mLocalDeviceIdleController != null) {
                            if (!mReportedActive) {
                                mReportedActive = true;
                                mLocalDeviceIdleController.setJobsActive(true);
                            }
                        }
                    }
                    mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
                }
            }
        }
    }

    void reportActive() {
        // active is true if pending queue contains jobs OR some job is running.
        boolean active = mPendingJobs.size() > 0;
        if (mPendingJobs.size() <= 0) {
            for (int i=0; i<mActiveServices.size(); i++) {
                JobServiceContext jsc = mActiveServices.get(i);
                if (jsc.getRunningJob() != null) {
                    active = true;
                    break;
                }
            }
        }

        if (mReportedActive != active) {
            mReportedActive = active;
            if (mLocalDeviceIdleController != null) {
                mLocalDeviceIdleController.setJobsActive(active);
            }
        }
    }

    /**
     * Initializes the system service.
     * <p>
     * Subclasses must define a single argument constructor that accepts the context
     * and passes it to super.
     * </p>
     *
     * @param context The system server context.
     */
    public JobSchedulerService(Context context) {
        super(context);
        // Create the controllers.
        mControllers = new ArrayList<StateController>();
        mControllers.add(ConnectivityController.get(this));
        mControllers.add(TimeController.get(this));
        mControllers.add(IdleController.get(this));
        mControllers.add(BatteryController.get(this));
        mControllers.add(AppIdleController.get(this));
        mControllers.add(ContentObserverController.get(this));

        mHandler = new JobHandler(context.getMainLooper());
        mJobSchedulerStub = new JobSchedulerStub();
        mJobs = JobStore.initAndGet(this);
    }

    @Override
    public void onStart() {
        publishBinderService(Context.JOB_SCHEDULER_SERVICE, mJobSchedulerStub);
    }

    @Override
    public void onBootPhase(int phase) {
        if (PHASE_SYSTEM_SERVICES_READY == phase) {
            // Register br for package removals and user removals.
            final IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
            filter.addDataScheme("package");
            getContext().registerReceiverAsUser(
                    mBroadcastReceiver, UserHandle.ALL, filter, null, null);
            final IntentFilter userFilter = new IntentFilter(Intent.ACTION_USER_REMOVED);
            userFilter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
            userFilter.addAction(PowerManager.ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED);
            getContext().registerReceiverAsUser(
                    mBroadcastReceiver, UserHandle.ALL, userFilter, null, null);
            mPowerManager = (PowerManager)getContext().getSystemService(Context.POWER_SERVICE);
            try {
                ActivityManagerNative.getDefault().registerUidObserver(mUidObserver,
                        ActivityManager.UID_OBSERVER_PROCSTATE | ActivityManager.UID_OBSERVER_GONE
                        | ActivityManager.UID_OBSERVER_IDLE);
            } catch (RemoteException e) {
                // ignored; both services live in system_server
            }
        } else if (phase == PHASE_THIRD_PARTY_APPS_CAN_START) {
            synchronized (mLock) {
                // Let's go!
                mReadyToRock = true;
                mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService(
                        BatteryStats.SERVICE_NAME));
                mLocalDeviceIdleController
                        = LocalServices.getService(DeviceIdleController.LocalService.class);
                // Create the "runners".
                for (int i = 0; i < MAX_JOB_CONTEXTS_COUNT; i++) {
                    mActiveServices.add(
                            new JobServiceContext(this, mBatteryStats,
                                    getContext().getMainLooper()));
                }
                // Attach jobs to their controllers.
                mJobs.forEachJob(new JobStatusFunctor() {
                    @Override
                    public void process(JobStatus job) {
                        for (int controller = 0; controller < mControllers.size(); controller++) {
                            final StateController sc = mControllers.get(controller);
                            sc.deviceIdleModeChanged(mDeviceIdleMode);
                            sc.maybeStartTrackingJobLocked(job, null);
                        }
                    }
                });
                // GO GO GO!
                mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
            }
        }
    }

    /**
     * Called when we have a job status object that we need to insert in our
     * {@link com.android.server.job.JobStore}, and make sure all the relevant controllers know
     * about.
     */
    private void startTrackingJob(JobStatus jobStatus, JobStatus lastJob) {
        synchronized (mLock) {
            final boolean update = mJobs.add(jobStatus);
            if (mReadyToRock) {
                for (int i = 0; i < mControllers.size(); i++) {
                    StateController controller = mControllers.get(i);
                    if (update) {
                        controller.maybeStopTrackingJobLocked(jobStatus, true);
                    }
                    controller.maybeStartTrackingJobLocked(jobStatus, lastJob);
                }
            }
        }
    }

    /**
     * Called when we want to remove a JobStatus object that we've finished executing. Returns the
     * object removed.
     */
    private boolean stopTrackingJob(JobStatus jobStatus, boolean writeBack) {
        synchronized (mLock) {
            // Remove from store as well as controllers.
            final boolean removed = mJobs.remove(jobStatus, writeBack);
            if (removed && mReadyToRock) {
                for (int i=0; i<mControllers.size(); i++) {
                    StateController controller = mControllers.get(i);
                    controller.maybeStopTrackingJobLocked(jobStatus, false);
                }
            }
            return removed;
        }
    }

    private boolean stopJobOnServiceContextLocked(JobStatus job, int reason) {
        for (int i=0; i<mActiveServices.size(); i++) {
            JobServiceContext jsc = mActiveServices.get(i);
            final JobStatus executing = jsc.getRunningJob();
            if (executing != null && executing.matches(job.getUid(), job.getJobId())) {
                jsc.cancelExecutingJob(reason);
                return true;
            }
        }
        return false;
    }

    /**
     * @param job JobStatus we are querying against.
     * @return Whether or not the job represented by the status object is currently being run or
     * is pending.
     */
    private boolean isCurrentlyActiveLocked(JobStatus job) {
        for (int i=0; i<mActiveServices.size(); i++) {
            JobServiceContext serviceContext = mActiveServices.get(i);
            final JobStatus running = serviceContext.getRunningJob();
            if (running != null && running.matches(job.getUid(), job.getJobId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reschedules the given job based on the job's backoff policy. It doesn't make sense to
     * specify an override deadline on a failed job (the failed job will run even though it's not
     * ready), so we reschedule it with {@link JobStatus#NO_LATEST_RUNTIME}, but specify that any
     * ready job with {@link JobStatus#numFailures} > 0 will be executed.
     *
     * @param failureToReschedule Provided job status that we will reschedule.
     * @return A newly instantiated JobStatus with the same constraints as the last job except
     * with adjusted timing constraints.
     *
     * @see JobHandler#maybeQueueReadyJobsForExecutionLockedH
     */
    private JobStatus getRescheduleJobForFailure(JobStatus failureToReschedule) {
        final long elapsedNowMillis = SystemClock.elapsedRealtime();
        final JobInfo job = failureToReschedule.getJob();

        final long initialBackoffMillis = job.getInitialBackoffMillis();
        final int backoffAttempts = failureToReschedule.getNumFailures() + 1;
        long delayMillis;

        switch (job.getBackoffPolicy()) {
            case JobInfo.BACKOFF_POLICY_LINEAR:
                delayMillis = initialBackoffMillis * backoffAttempts;
                break;
            default:
                if (DEBUG) {
                    Slog.v(TAG, "Unrecognised back-off policy, defaulting to exponential.");
                }
            case JobInfo.BACKOFF_POLICY_EXPONENTIAL:
                delayMillis =
                        (long) Math.scalb(initialBackoffMillis, backoffAttempts - 1);
                break;
        }
        delayMillis =
                Math.min(delayMillis, JobInfo.MAX_BACKOFF_DELAY_MILLIS);
        JobStatus newJob = new JobStatus(failureToReschedule, elapsedNowMillis + delayMillis,
                JobStatus.NO_LATEST_RUNTIME, backoffAttempts);
        for (int ic=0; ic<mControllers.size(); ic++) {
            StateController controller = mControllers.get(ic);
            controller.rescheduleForFailure(newJob, failureToReschedule);
        }
        return newJob;
    }

    /**
     * Called after a periodic has executed so we can reschedule it. We take the last execution
     * time of the job to be the time of completion (i.e. the time at which this function is
     * called).
     * This could be inaccurate b/c the job can run for as long as
     * {@link com.android.server.job.JobServiceContext#EXECUTING_TIMESLICE_MILLIS}, but will lead
     * to underscheduling at least, rather than if we had taken the last execution time to be the
     * start of the execution.
     * @return A new job representing the execution criteria for this instantiation of the
     * recurring job.
     */
    private JobStatus getRescheduleJobForPeriodic(JobStatus periodicToReschedule) {
        final long elapsedNow = SystemClock.elapsedRealtime();
        // Compute how much of the period is remaining.
        long runEarly = 0L;

        // If this periodic was rescheduled it won't have a deadline.
        if (periodicToReschedule.hasDeadlineConstraint()) {
            runEarly = Math.max(periodicToReschedule.getLatestRunTimeElapsed() - elapsedNow, 0L);
        }
        long flex = periodicToReschedule.getJob().getFlexMillis();
        long period = periodicToReschedule.getJob().getIntervalMillis();
        long newLatestRuntimeElapsed = elapsedNow + runEarly + period;
        long newEarliestRunTimeElapsed = newLatestRuntimeElapsed - flex;

        if (DEBUG) {
            Slog.v(TAG, "Rescheduling executed periodic. New execution window [" +
                    newEarliestRunTimeElapsed/1000 + ", " + newLatestRuntimeElapsed/1000 + "]s");
        }
        return new JobStatus(periodicToReschedule, newEarliestRunTimeElapsed,
                newLatestRuntimeElapsed, 0 /* backoffAttempt */);
    }

    // JobCompletedListener implementations.

    /**
     * A job just finished executing. We fetch the
     * {@link com.android.server.job.controllers.JobStatus} from the store and depending on
     * whether we want to reschedule we readd it to the controllers.
     * @param jobStatus Completed job.
     * @param needsReschedule Whether the implementing class should reschedule this job.
     */
    @Override
    public void onJobCompleted(JobStatus jobStatus, boolean needsReschedule) {
        if (DEBUG) {
            Slog.d(TAG, "Completed " + jobStatus + ", reschedule=" + needsReschedule);
        }
        // Do not write back immediately if this is a periodic job. The job may get lost if system
        // shuts down before it is added back.
        if (!stopTrackingJob(jobStatus, !jobStatus.getJob().isPeriodic())) {
            if (DEBUG) {
                Slog.d(TAG, "Could not find job to remove. Was job removed while executing?");
            }
            // We still want to check for jobs to execute, because this job may have
            // scheduled a new job under the same job id, and now we can run it.
            mHandler.obtainMessage(MSG_CHECK_JOB_GREEDY).sendToTarget();
            return;
        }
        // Note: there is a small window of time in here where, when rescheduling a job,
        // we will stop monitoring its content providers.  This should be fixed by stopping
        // the old job after scheduling the new one, but since we have no lock held here
        // that may cause ordering problems if the app removes jobStatus while in here.
        if (needsReschedule) {
            JobStatus rescheduled = getRescheduleJobForFailure(jobStatus);
            startTrackingJob(rescheduled, jobStatus);
        } else if (jobStatus.getJob().isPeriodic()) {
            JobStatus rescheduledPeriodic = getRescheduleJobForPeriodic(jobStatus);
            startTrackingJob(rescheduledPeriodic, jobStatus);
        }
        reportActive();
        mHandler.obtainMessage(MSG_CHECK_JOB_GREEDY).sendToTarget();
    }

    // StateChangedListener implementations.

    /**
     * Posts a message to the {@link com.android.server.job.JobSchedulerService.JobHandler} that
     * some controller's state has changed, so as to run through the list of jobs and start/stop
     * any that are eligible.
     */
    @Override
    public void onControllerStateChanged() {
        mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    @Override
    public void onRunJobNow(JobStatus jobStatus) {
        mHandler.obtainMessage(MSG_JOB_EXPIRED, jobStatus).sendToTarget();
    }

    private class JobHandler extends Handler {

        public JobHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            synchronized (mLock) {
                if (!mReadyToRock) {
                    return;
                }
            }
            switch (message.what) {
                case MSG_JOB_EXPIRED:
                    synchronized (mLock) {
                        JobStatus runNow = (JobStatus) message.obj;
                        // runNow can be null, which is a controller's way of indicating that its
                        // state is such that all ready jobs should be run immediately.
                        if (runNow != null && !mPendingJobs.contains(runNow)
                                && mJobs.containsJob(runNow)) {
                            mPendingJobs.add(runNow);
                        }
                        queueReadyJobsForExecutionLockedH();
                    }
                    break;
                case MSG_CHECK_JOB:
                    synchronized (mLock) {
                        if (mReportedActive) {
                            // if jobs are currently being run, queue all ready jobs for execution.
                            queueReadyJobsForExecutionLockedH();
                        } else {
                            // Check the list of jobs and run some of them if we feel inclined.
                            maybeQueueReadyJobsForExecutionLockedH();
                        }
                    }
                    break;
                case MSG_CHECK_JOB_GREEDY:
                    synchronized (mLock) {
                        queueReadyJobsForExecutionLockedH();
                    }
                    break;
                case MSG_STOP_JOB:
                    cancelJobImpl((JobStatus)message.obj);
                    break;
            }
            maybeRunPendingJobsH();
            // Don't remove JOB_EXPIRED in case one came along while processing the queue.
            removeMessages(MSG_CHECK_JOB);
        }

        /**
         * Run through list of jobs and execute all possible - at least one is expired so we do
         * as many as we can.
         */
        private void queueReadyJobsForExecutionLockedH() {
            if (DEBUG) {
                Slog.d(TAG, "queuing all ready jobs for execution:");
            }
            mPendingJobs.clear();
            mJobs.forEachJob(mReadyQueueFunctor);
            mReadyQueueFunctor.postProcess();

            if (DEBUG) {
                final int queuedJobs = mPendingJobs.size();
                if (queuedJobs == 0) {
                    Slog.d(TAG, "No jobs pending.");
                } else {
                    Slog.d(TAG, queuedJobs + " jobs queued.");
                }
            }
        }

        class ReadyJobQueueFunctor implements JobStatusFunctor {
            ArrayList<JobStatus> newReadyJobs;

            @Override
            public void process(JobStatus job) {
                if (isReadyToBeExecutedLocked(job)) {
                    if (DEBUG) {
                        Slog.d(TAG, "    queued " + job.toShortString());
                    }
                    if (newReadyJobs == null) {
                        newReadyJobs = new ArrayList<JobStatus>();
                    }
                    newReadyJobs.add(job);
                } else if (areJobConstraintsNotSatisfiedLocked(job)) {
                    stopJobOnServiceContextLocked(job,
                            JobParameters.REASON_CONSTRAINTS_NOT_SATISFIED);
                }
            }

            public void postProcess() {
                if (newReadyJobs != null) {
                    mPendingJobs.addAll(newReadyJobs);
                }
                newReadyJobs = null;
            }
        }
        private final ReadyJobQueueFunctor mReadyQueueFunctor = new ReadyJobQueueFunctor();

        /**
         * The state of at least one job has changed. Here is where we could enforce various
         * policies on when we want to execute jobs.
         * Right now the policy is such:
         * If >1 of the ready jobs is idle mode we send all of them off
         * if more than 2 network connectivity jobs are ready we send them all off.
         * If more than 4 jobs total are ready we send them all off.
         * TODO: It would be nice to consolidate these sort of high-level policies somewhere.
         */
        class MaybeReadyJobQueueFunctor implements JobStatusFunctor {
            int chargingCount;
            int idleCount;
            int backoffCount;
            int connectivityCount;
            int contentCount;
            List<JobStatus> runnableJobs;

            public MaybeReadyJobQueueFunctor() {
                reset();
            }

            // Functor method invoked for each job via JobStore.forEachJob()
            @Override
            public void process(JobStatus job) {
                if (isReadyToBeExecutedLocked(job)) {
                    try {
                        if (ActivityManagerNative.getDefault().getAppStartMode(job.getUid(),
                                job.getJob().getService().getPackageName())
                                == ActivityManager.APP_START_MODE_DISABLED) {
                            Slog.w(TAG, "Aborting job " + job.getUid() + ":"
                                    + job.getJob().toString() + " -- package not allowed to start");
                            mHandler.obtainMessage(MSG_STOP_JOB, job).sendToTarget();
                            return;
                        }
                    } catch (RemoteException e) {
                    }
                    if (job.getNumFailures() > 0) {
                        backoffCount++;
                    }
                    if (job.hasIdleConstraint()) {
                        idleCount++;
                    }
                    if (job.hasConnectivityConstraint() || job.hasUnmeteredConstraint()) {
                        connectivityCount++;
                    }
                    if (job.hasChargingConstraint()) {
                        chargingCount++;
                    }
                    if (job.hasContentTriggerConstraint()) {
                        contentCount++;
                    }
                    if (runnableJobs == null) {
                        runnableJobs = new ArrayList<>();
                    }
                    runnableJobs.add(job);
                } else if (areJobConstraintsNotSatisfiedLocked(job)) {
                    stopJobOnServiceContextLocked(job,
                            JobParameters.REASON_CONSTRAINTS_NOT_SATISFIED);
                }
            }

            public void postProcess() {
                if (backoffCount > 0 ||
                        idleCount >= MIN_IDLE_COUNT ||
                        connectivityCount >= MIN_CONNECTIVITY_COUNT ||
                        chargingCount >= MIN_CHARGING_COUNT ||
                        contentCount  >= MIN_CONTENT_COUNT ||
                        (runnableJobs != null && runnableJobs.size() >= MIN_READY_JOBS_COUNT)) {
                    if (DEBUG) {
                        Slog.d(TAG, "maybeQueueReadyJobsForExecutionLockedH: Running jobs.");
                    }
                    mPendingJobs.addAll(runnableJobs);
                } else {
                    if (DEBUG) {
                        Slog.d(TAG, "maybeQueueReadyJobsForExecutionLockedH: Not running anything.");
                    }
                }

                // Be ready for next time
                reset();
            }

            private void reset() {
                chargingCount = 0;
                idleCount =  0;
                backoffCount = 0;
                connectivityCount = 0;
                contentCount = 0;
                runnableJobs = null;
            }
        }
        private final MaybeReadyJobQueueFunctor mMaybeQueueFunctor = new MaybeReadyJobQueueFunctor();

        private void maybeQueueReadyJobsForExecutionLockedH() {
            if (DEBUG) Slog.d(TAG, "Maybe queuing ready jobs...");

            mPendingJobs.clear();
            mJobs.forEachJob(mMaybeQueueFunctor);
            mMaybeQueueFunctor.postProcess();
        }

        /**
         * Criteria for moving a job into the pending queue:
         *      - It's ready.
         *      - It's not pending.
         *      - It's not already running on a JSC.
         *      - The user that requested the job is running.
         *      - The component is enabled and runnable.
         */
        private boolean isReadyToBeExecutedLocked(JobStatus job) {
            final boolean jobReady = job.isReady();
            final boolean jobPending = mPendingJobs.contains(job);
            final boolean jobActive = isCurrentlyActiveLocked(job);

            final int userId = job.getUserId();
            final boolean userStarted = ArrayUtils.contains(mStartedUsers, userId);
            final boolean componentPresent;
            try {
                componentPresent = (AppGlobals.getPackageManager().getServiceInfo(
                        job.getServiceComponent(), PackageManager.MATCH_DEBUG_TRIAGED_MISSING,
                        userId) != null);
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            }

            if (DEBUG) {
                Slog.v(TAG, "isReadyToBeExecutedLocked: " + job.toShortString()
                        + " ready=" + jobReady + " pending=" + jobPending
                        + " active=" + jobActive + " userStarted=" + userStarted
                        + " componentPresent=" + componentPresent);
            }
            return userStarted && componentPresent && jobReady && !jobPending && !jobActive;
        }

        /**
         * Criteria for cancelling an active job:
         *      - It's not ready
         *      - It's running on a JSC.
         */
        private boolean areJobConstraintsNotSatisfiedLocked(JobStatus job) {
            return !job.isReady() && isCurrentlyActiveLocked(job);
        }

        /**
         * Reconcile jobs in the pending queue against available execution contexts.
         * A controller can force a job into the pending queue even if it's already running, but
         * here is where we decide whether to actually execute it.
         */
        private void maybeRunPendingJobsH() {
            synchronized (mLock) {
                if (mDeviceIdleMode) {
                    // If device is idle, we will not schedule jobs to run.
                    return;
                }
                if (DEBUG) {
                    Slog.d(TAG, "pending queue: " + mPendingJobs.size() + " jobs.");
                }
                assignJobsToContextsLocked();
                reportActive();
            }
        }
    }

    private int evaluateJobPriorityLocked(JobStatus job) {
        int priority = job.getPriority();
        if (priority >= JobInfo.PRIORITY_FOREGROUND_APP) {
            return priority;
        }
        if (mForegroundUids.get(job.getSourceUid())) {
            return JobInfo.PRIORITY_FOREGROUND_APP;
        }
        return priority;
    }

    /**
     * Takes jobs from pending queue and runs them on available contexts.
     * If no contexts are available, preempts lower priority jobs to
     * run higher priority ones.
     * Lock on mJobs before calling this function.
     */
    private void assignJobsToContextsLocked() {
        if (DEBUG) {
            Slog.d(TAG, printPendingQueue());
        }

        // This array essentially stores the state of mActiveServices array.
        // ith index stores the job present on the ith JobServiceContext.
        // We manipulate this array until we arrive at what jobs should be running on
        // what JobServiceContext.
        JobStatus[] contextIdToJobMap = new JobStatus[MAX_JOB_CONTEXTS_COUNT];
        // Indicates whether we need to act on this jobContext id
        boolean[] act = new boolean[MAX_JOB_CONTEXTS_COUNT];
        int[] preferredUidForContext = new int[MAX_JOB_CONTEXTS_COUNT];
        for (int i=0; i<mActiveServices.size(); i++) {
            contextIdToJobMap[i] = mActiveServices.get(i).getRunningJob();
            preferredUidForContext[i] = mActiveServices.get(i).getPreferredUid();
        }
        if (DEBUG) {
            Slog.d(TAG, printContextIdToJobMap(contextIdToJobMap, "running jobs initial"));
        }
        Iterator<JobStatus> it = mPendingJobs.iterator();
        while (it.hasNext()) {
            JobStatus nextPending = it.next();

            // If job is already running, go to next job.
            int jobRunningContext = findJobContextIdFromMap(nextPending, contextIdToJobMap);
            if (jobRunningContext != -1) {
                continue;
            }

            nextPending.lastEvaluatedPriority = evaluateJobPriorityLocked(nextPending);

            // Find a context for nextPending. The context should be available OR
            // it should have lowest priority among all running jobs
            // (sharing the same Uid as nextPending)
            int minPriority = Integer.MAX_VALUE;
            int minPriorityContextId = -1;
            for (int i=0; i<mActiveServices.size(); i++) {
                JobStatus job = contextIdToJobMap[i];
                int preferredUid = preferredUidForContext[i];
                if (job == null && (preferredUid == nextPending.getUid() ||
                        preferredUid == JobServiceContext.NO_PREFERRED_UID) ) {
                    minPriorityContextId = i;
                    break;
                }
                if (job == null) {
                    // No job on this context, but nextPending can't run here because
                    // the context has a preferred Uid.
                    continue;
                }
                if (job.getUid() != nextPending.getUid()) {
                    continue;
                }
                if (evaluateJobPriorityLocked(job) >= nextPending.lastEvaluatedPriority) {
                    continue;
                }
                if (minPriority > nextPending.lastEvaluatedPriority) {
                    minPriority = nextPending.lastEvaluatedPriority;
                    minPriorityContextId = i;
                }
            }
            if (minPriorityContextId != -1) {
                contextIdToJobMap[minPriorityContextId] = nextPending;
                act[minPriorityContextId] = true;
            }
        }
        if (DEBUG) {
            Slog.d(TAG, printContextIdToJobMap(contextIdToJobMap, "running jobs final"));
        }
        for (int i=0; i<mActiveServices.size(); i++) {
            boolean preservePreferredUid = false;
            if (act[i]) {
                JobStatus js = mActiveServices.get(i).getRunningJob();
                if (js != null) {
                    if (DEBUG) {
                        Slog.d(TAG, "preempting job: " + mActiveServices.get(i).getRunningJob());
                    }
                    // preferredUid will be set to uid of currently running job.
                    mActiveServices.get(i).preemptExecutingJob();
                    preservePreferredUid = true;
                } else {
                    final JobStatus pendingJob = contextIdToJobMap[i];
                    if (DEBUG) {
                        Slog.d(TAG, "About to run job on context "
                                + String.valueOf(i) + ", job: " + pendingJob);
                    }
                    for (int ic=0; ic<mControllers.size(); ic++) {
                        mControllers.get(ic).prepareForExecutionLocked(pendingJob);
                    }
                    if (!mActiveServices.get(i).executeRunnableJob(pendingJob)) {
                        Slog.d(TAG, "Error executing " + pendingJob);
                    }
                    mPendingJobs.remove(pendingJob);
                }
            }
            if (!preservePreferredUid) {
                mActiveServices.get(i).clearPreferredUid();
            }
        }
    }

    int findJobContextIdFromMap(JobStatus jobStatus, JobStatus[] map) {
        for (int i=0; i<map.length; i++) {
            if (map[i] != null && map[i].matches(jobStatus.getUid(), jobStatus.getJobId())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Binder stub trampoline implementation
     */
    final class JobSchedulerStub extends IJobScheduler.Stub {
        /** Cache determination of whether a given app can persist jobs
         * key is uid of the calling app; value is undetermined/true/false
         */
        private final SparseArray<Boolean> mPersistCache = new SparseArray<Boolean>();

        // Enforce that only the app itself (or shared uid participant) can schedule a
        // job that runs one of the app's services, as well as verifying that the
        // named service properly requires the BIND_JOB_SERVICE permission
        private void enforceValidJobRequest(int uid, JobInfo job) {
            final IPackageManager pm = AppGlobals.getPackageManager();
            final ComponentName service = job.getService();
            try {
                ServiceInfo si = pm.getServiceInfo(service,
                        PackageManager.MATCH_DEBUG_TRIAGED_MISSING, UserHandle.getUserId(uid));
                if (si == null) {
                    throw new IllegalArgumentException("No such service " + service);
                }
                if (si.applicationInfo.uid != uid) {
                    throw new IllegalArgumentException("uid " + uid +
                            " cannot schedule job in " + service.getPackageName());
                }
                if (!JobService.PERMISSION_BIND.equals(si.permission)) {
                    throw new IllegalArgumentException("Scheduled service " + service
                            + " does not require android.permission.BIND_JOB_SERVICE permission");
                }
            } catch (RemoteException e) {
                // Can't happen; the Package Manager is in this same process
            }
        }

        private boolean canPersistJobs(int pid, int uid) {
            // If we get this far we're good to go; all we need to do now is check
            // whether the app is allowed to persist its scheduled work.
            final boolean canPersist;
            synchronized (mPersistCache) {
                Boolean cached = mPersistCache.get(uid);
                if (cached != null) {
                    canPersist = cached.booleanValue();
                } else {
                    // Persisting jobs is tantamount to running at boot, so we permit
                    // it when the app has declared that it uses the RECEIVE_BOOT_COMPLETED
                    // permission
                    int result = getContext().checkPermission(
                            android.Manifest.permission.RECEIVE_BOOT_COMPLETED, pid, uid);
                    canPersist = (result == PackageManager.PERMISSION_GRANTED);
                    mPersistCache.put(uid, canPersist);
                }
            }
            return canPersist;
        }

        // IJobScheduler implementation
        @Override
        public int schedule(JobInfo job) throws RemoteException {
            if (DEBUG) {
                Slog.d(TAG, "Scheduling job: " + job.toString());
            }
            final int pid = Binder.getCallingPid();
            final int uid = Binder.getCallingUid();

            enforceValidJobRequest(uid, job);
            if (job.isPersisted()) {
                if (!canPersistJobs(pid, uid)) {
                    throw new IllegalArgumentException("Error: requested job be persisted without"
                            + " holding RECEIVE_BOOT_COMPLETED permission.");
                }
            }

            long ident = Binder.clearCallingIdentity();
            try {
                return JobSchedulerService.this.schedule(job, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        @Override
        public int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag)
                throws RemoteException {
            final int callerUid = Binder.getCallingUid();
            if (DEBUG) {
                Slog.d(TAG, "Caller uid " + callerUid + " scheduling job: " + job.toString()
                        + " on behalf of " + packageName);
            }

            if (packageName == null) {
                throw new NullPointerException("Must specify a package for scheduleAsPackage()");
            }

            int mayScheduleForOthers = getContext().checkCallingOrSelfPermission(
                    android.Manifest.permission.UPDATE_DEVICE_STATS);
            if (mayScheduleForOthers != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException("Caller uid " + callerUid
                        + " not permitted to schedule jobs for other apps");
            }

            long ident = Binder.clearCallingIdentity();
            try {
                return JobSchedulerService.this.scheduleAsPackage(job, callerUid,
                        packageName, userId, tag);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        @Override
        public List<JobInfo> getAllPendingJobs() throws RemoteException {
            final int uid = Binder.getCallingUid();

            long ident = Binder.clearCallingIdentity();
            try {
                return JobSchedulerService.this.getPendingJobs(uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        @Override
        public void cancelAll() throws RemoteException {
            final int uid = Binder.getCallingUid();

            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJobsForUid(uid, true);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        @Override
        public void cancel(int jobId) throws RemoteException {
            final int uid = Binder.getCallingUid();

            long ident = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.cancelJob(uid, jobId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /**
         * "dumpsys" infrastructure
         */
        @Override
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            getContext().enforceCallingOrSelfPermission(android.Manifest.permission.DUMP, TAG);

            long identityToken = Binder.clearCallingIdentity();
            try {
                JobSchedulerService.this.dumpInternal(pw);
            } finally {
                Binder.restoreCallingIdentity(identityToken);
            }
        }
    };

    private String printContextIdToJobMap(JobStatus[] map, String initial) {
        StringBuilder s = new StringBuilder(initial + ": ");
        for (int i=0; i<map.length; i++) {
            s.append("(")
                    .append(map[i] == null? -1: map[i].getJobId())
                    .append(map[i] == null? -1: map[i].getUid())
                    .append(")" );
        }
        return s.toString();
    }

    private String printPendingQueue() {
        StringBuilder s = new StringBuilder("Pending queue: ");
        Iterator<JobStatus> it = mPendingJobs.iterator();
        while (it.hasNext()) {
            JobStatus js = it.next();
            s.append("(")
                    .append(js.getJob().getId())
                    .append(", ")
                    .append(js.getUid())
                    .append(") ");
        }
        return s.toString();
    }

    void dumpInternal(final PrintWriter pw) {
        final long now = SystemClock.elapsedRealtime();
        synchronized (mLock) {
            pw.println("Started users: " + Arrays.toString(mStartedUsers));
            pw.println("Registered jobs:");
            if (mJobs.size() > 0) {
                mJobs.forEachJob(new JobStatusFunctor() {
                    private int index = 0;

                    @Override
                    public void process(JobStatus job) {
                        pw.print("  Job #"); pw.print(index++); pw.print(": ");
                        pw.println(job.toShortString());
                        job.dump(pw, "    ");
                        pw.print("    Ready: ");
                        pw.print(mHandler.isReadyToBeExecutedLocked(job));
                        pw.print(" (job=");
                        pw.print(job.isReady());
                        pw.print(" pending=");
                        pw.print(mPendingJobs.contains(job));
                        pw.print(" active=");
                        pw.print(isCurrentlyActiveLocked(job));
                        pw.print(" user=");
                        pw.print(ArrayUtils.contains(mStartedUsers, job.getUserId()));
                        pw.println(")");
                    }
                });
            } else {
                pw.println("  None.");
            }
            for (int i=0; i<mControllers.size(); i++) {
                pw.println();
                mControllers.get(i).dumpControllerStateLocked(pw);
            }
            pw.println();
            pw.println("Foreground uids:");
            for (int i=0; i<mForegroundUids.size(); i++) {
                pw.print("  "); pw.println(UserHandle.formatUid(mForegroundUids.keyAt(i)));
            }
            pw.println();
            pw.println("Pending queue:");
            for (int i=0; i<mPendingJobs.size(); i++) {
                JobStatus job = mPendingJobs.get(i);
                pw.print("  Pending #"); pw.print(i); pw.print(": ");
                pw.println(job.toShortString());
                int priority = evaluateJobPriorityLocked(job);
                if (priority != JobInfo.PRIORITY_DEFAULT) {
                    pw.print("    Evaluated priority: "); pw.println(priority);
                }
                pw.print("    Tag: "); pw.println(job.getTag());
            }
            pw.println();
            pw.println("Active jobs:");
            for (int i=0; i<mActiveServices.size(); i++) {
                JobServiceContext jsc = mActiveServices.get(i);
                if (jsc.getRunningJob() == null) {
                    continue;
                } else {
                    final long timeout = jsc.getTimeoutElapsed();
                    pw.print("Running for: ");
                    pw.print((now - jsc.getExecutionStartTimeElapsed())/1000);
                    pw.print("s timeout=");
                    pw.print(timeout);
                    pw.print(" fromnow=");
                    pw.println(timeout-now);
                    jsc.getRunningJob().dump(pw, "  ");
                    int priority = evaluateJobPriorityLocked(jsc.getRunningJob());
                    if (priority != JobInfo.PRIORITY_DEFAULT) {
                        pw.print("  Evaluated priority: "); pw.println(priority);
                    }
                }
            }
            pw.println();
            pw.print("mReadyToRock="); pw.println(mReadyToRock);
            pw.print("mDeviceIdleMode="); pw.println(mDeviceIdleMode);
            pw.print("mReportedActive="); pw.println(mReportedActive);
        }
        pw.println();
    }
}