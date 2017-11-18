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

package com.android.server;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * The base class for services running in the system process. Override and implement
 * the lifecycle event callback methods as needed.
 *
 * The lifecycle of a SystemService:
 *
 * {@link #onCreate(android.content.Context)} is called to initialize the
 * service.
 *
 * {@link #onStart()} is called to get the service running. It is common
 * for services to publish their Binder interface at this point. All required
 * dependencies are also assumed to be ready to use.
 *
 * Then {@link #onBootPhase(int)} is called as many times as there are boot phases
 * until {@link #PHASE_BOOT_COMPLETE} is sent, which is the last boot phase. Each phase
 * is an opportunity to do special work, like acquiring optional service dependencies,
 * waiting to see if SafeMode is enabled, or registering with a service that gets
 * started after this one.
 *
 * NOTE: All lifecycle methods are called from the same thread that created the
 * SystemService.
 *
 * {@hide}
 */
public abstract class SystemService {
    /*
     * Boot Phases
     */
    public static final int PHASE_WAIT_FOR_DEFAULT_DISPLAY = 100; // maybe should be a dependency?
    public static final int PHASE_LOCK_SETTINGS_READY = 480;
    public static final int PHASE_SYSTEM_SERVICES_READY = 500;
    public static final int PHASE_THIRD_PARTY_APPS_CAN_START = 600;
    public static final int PHASE_BOOT_COMPLETE = 1000;

    private SystemServiceManager mManager;
    private Context mContext;

    final void init(Context context, SystemServiceManager manager) {
        mContext = context;
        mManager = manager;
        onCreate(context);
    }

    public final boolean isSafeMode() {
        return mManager.isSafeMode();
    }

    /**
     * Services are not yet available. This is a good place to do setup work that does
     * not require other services.
     *
     * @param context The system context.
     */
    public void onCreate(Context context) {}

    /**
     * Called when the dependencies listed in the @Service class-annotation are available
     * and after the chosen start phase.
     * When this method returns, the service should be published.
     */
    public abstract void onStart();

    /**
     * Called on each phase of the boot process. Phases before the service's start phase
     * (as defined in the @Service annotation) are never received.
     *
     * @param phase The current boot phase.
     */
    public void onBootPhase(int phase) {}

    /**
     * Publish the service so it is accessible to other services and apps.
     */
    protected final void publishBinderService(String name, IBinder service) {
        publishBinderService(name, service, false);
    }

    /**
     * Publish the service so it is accessible to other services and apps.
     */
    protected final void publishBinderService(String name, IBinder service,
            boolean allowIsolated) {
        ServiceManager.addService(name, service, allowIsolated);
    }

    /**
     * Get a binder service by its name.
     */
    protected final IBinder getBinderService(String name) {
        return ServiceManager.getService(name);
    }

    /**
     * Publish the service so it is only accessible to the system process.
     */
    protected final <T> void publishLocalService(Class<T> type, T service) {
        LocalServices.addService(type, service);
    }

    /**
     * Get a local service by interface.
     */
    protected final <T> T getLocalService(Class<T> type) {
        return LocalServices.getService(type);
    }

    public final Context getContext() {
        return mContext;
    }

//    /**
//     * Called when a new user has been created. If your service deals with multiple users, this
//     * method should be overridden.
//     *
//     * @param userHandle The user that was created.
//     */
//    public void onUserCreated(int userHandle) {
//    }
//
//    /**
//     * Called when an existing user has started a new session. If your service deals with multiple
//     * users, this method should be overridden.
//     *
//     * @param userHandle The user who started a new session.
//     */
//    public void onUserStarted(int userHandle) {
//    }
//
//    /**
//     * Called when a background user session has entered the foreground. If your service deals with
//     * multiple users, this method should be overridden.
//     *
//     * @param userHandle The user who's session entered the foreground.
//     */
//    public void onUserForeground(int userHandle) {
//    }
//
//    /**
//     * Called when a foreground user session has entered the background. If your service deals with
//     * multiple users, this method should be overridden;
//     *
//     * @param userHandle The user who's session entered the background.
//     */
//    public void onUserBackground(int userHandle) {
//    }
//
//    /**
//     * Called when a user's active session has stopped. If your service deals with multiple users,
//     * this method should be overridden.
//     *
//     * @param userHandle The user who's session has stopped.
//     */
//    public void onUserStopped(int userHandle) {
//    }
//
//    /**
//     * Called when a user has been removed from the system. If your service deals with multiple
//     * users, this method should be overridden.
//     *
//     * @param userHandle The user who has been removed.
//     */
//    public void onUserRemoved(int userHandle) {
//    }
}