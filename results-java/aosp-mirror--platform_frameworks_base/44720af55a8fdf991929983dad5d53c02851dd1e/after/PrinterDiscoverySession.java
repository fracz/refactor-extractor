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

package android.print;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @hide
 */
public final class PrinterDiscoverySession {

    private static final String LOG_TAG ="PrinterDiscoverySession";

    private static final int MSG_PRINTERS_ADDED = 1;
    private static final int MSG_PRINTERS_REMOVED = 2;
    private static final int MSG_PRINTERS_UPDATED = 3;

    private final ArrayMap<PrinterId, PrinterInfo> mPrinters =
            new ArrayMap<PrinterId, PrinterInfo>();

    private final IPrintManager mPrintManager;

    private final int mUserId;

    private final Handler mHandler;

    private IPrinterDiscoveryObserver mObserver;

    private OnPrintersChangeListener mListener;

    private boolean mIsPrinterDiscoveryStarted;

    public static interface OnPrintersChangeListener {
        public void onPrintersChanged();
    }

    PrinterDiscoverySession(IPrintManager printManager, Context context, int userId) {
        mPrintManager = printManager;
        mUserId = userId;
        mHandler = new SessionHandler(context.getMainLooper());
        mObserver = new PrinterDiscoveryObserver(this);
        try {
            mPrintManager.createPrinterDiscoverySession(mObserver, mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error creating printer discovery session", re);
        }
    }

    public final void startPrinterDisovery(List<PrinterId> priorityList) {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring start printers dsicovery - session destroyed");
        }
        if (!mIsPrinterDiscoveryStarted) {
            mIsPrinterDiscoveryStarted = true;
            try {
                mPrintManager.startPrinterDiscovery(mObserver, priorityList, mUserId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error starting printer discovery", re);
            }
        }
    }

    public final void stopPrinterDiscovery() {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring stop printers discovery - session destroyed");
        }
        if (mIsPrinterDiscoveryStarted) {
            mIsPrinterDiscoveryStarted = false;
            try {
                mPrintManager.stopPrinterDiscovery(mObserver, mUserId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error stopping printer discovery", re);
            }
        }
    }

    public final void requestPrinterUpdate(PrinterId printerId) {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring reqeust printer update - session destroyed");
        }
        try {
            mPrintManager.requestPrinterUpdate(printerId, mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error requesting printer update", re);
        }
    }

    public final void destroy() {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring destroy - session destroyed");
        }
        destroyNoCheck();
    }

    public final List<PrinterInfo> getPrinters() {
        if (isDestroyed()) {
            Log.w(LOG_TAG, "Ignoring get printers - session destroyed");
            return Collections.emptyList();
        }
        return new ArrayList<PrinterInfo>(mPrinters.values());
    }

    public final boolean isDestroyed() {
        throwIfNotCalledOnMainThread();
        return isDestroyedNoCheck();
    }

    public final boolean isPrinterDiscoveryStarted() {
        throwIfNotCalledOnMainThread();
        return mIsPrinterDiscoveryStarted;
    }

    public final void setOnPrintersChangeListener(OnPrintersChangeListener listener) {
        throwIfNotCalledOnMainThread();
        mListener = listener;
    }

    @Override
    protected final void finalize() throws Throwable {
        if (!isDestroyedNoCheck()) {
            Log.e(LOG_TAG, "Destroying leaked printer discovery session");
            destroyNoCheck();
        }
        super.finalize();
    }

    private boolean isDestroyedNoCheck() {
        return (mObserver == null);
    }

    private void destroyNoCheck() {
        stopPrinterDiscovery();
        try {
            mPrintManager.destroyPrinterDiscoverySession(mObserver, mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error destroying printer discovery session", re);
        } finally {
            mObserver = null;
            mPrinters.clear();
        }
    }

    private void handlePrintersAdded(List<PrinterInfo> printers) {
        if (isDestroyed()) {
            return;
        }
        boolean printersChanged = false;
        final int addedPrinterCount = printers.size();
        for (int i = 0; i < addedPrinterCount; i++) {
            PrinterInfo addedPrinter = printers.get(i);
            if (mPrinters.get(addedPrinter.getId()) == null) {
                mPrinters.put(addedPrinter.getId(), addedPrinter);
                printersChanged = true;
            }
        }
        if (printersChanged) {
            notifyOnPrintersChanged();
        }
    }

    private void handlePrintersRemoved(List<PrinterId> printerIds) {
        if (isDestroyed()) {
            return;
        }
        boolean printersChanged = false;
        final int removedPrinterIdCount = printerIds.size();
        for (int i = 0; i < removedPrinterIdCount; i++) {
            PrinterId removedPrinterId = printerIds.get(i);
            if (mPrinters.remove(removedPrinterId) != null) {
                printersChanged = true;
            }
        }
        if (printersChanged) {
            notifyOnPrintersChanged();
        }
    }

    private void handlePrintersUpdated(List<PrinterInfo> printers) {
        if (isDestroyed()) {
            return;
        }
        boolean printersChanged = false;
        final int updatedPrinterCount = printers.size();
        for (int i = 0; i < updatedPrinterCount; i++) {
            PrinterInfo updatedPrinter = printers.get(i);
            PrinterInfo oldPrinter = mPrinters.get(updatedPrinter.getId());
            if (oldPrinter != null && !oldPrinter.equals(updatedPrinter)) {
                mPrinters.put(updatedPrinter.getId(), updatedPrinter);
                printersChanged = true;
            }
        }
        if (printersChanged) {
            notifyOnPrintersChanged();
        }
    }

    private void notifyOnPrintersChanged() {
        if (mListener != null) {
            mListener.onPrintersChanged();
        }
    }

    private static void throwIfNotCalledOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new IllegalAccessError("must be called from the main thread");
        }
    }

    private final class SessionHandler extends Handler {

        public SessionHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_PRINTERS_ADDED: {
                    List<PrinterInfo> printers = (List<PrinterInfo>) message.obj;
                    handlePrintersAdded(printers);
                } break;

                case MSG_PRINTERS_REMOVED: {
                    List<PrinterId> printerIds = (List<PrinterId>) message.obj;
                    handlePrintersRemoved(printerIds);
                } break;

                case MSG_PRINTERS_UPDATED: {
                    List<PrinterInfo> printers = (List<PrinterInfo>) message.obj;
                    handlePrintersUpdated(printers);
                } break;
            }
        }
    }

    private static final class PrinterDiscoveryObserver extends IPrinterDiscoveryObserver.Stub {

        private final WeakReference<PrinterDiscoverySession> mWeakSession;

        public PrinterDiscoveryObserver(PrinterDiscoverySession session) {
            mWeakSession = new WeakReference<PrinterDiscoverySession>(session);
        }

        @Override
        public void onPrintersAdded(List<PrinterInfo> printers) {
            PrinterDiscoverySession session = mWeakSession.get();
            if (session != null) {
                session.mHandler.obtainMessage(MSG_PRINTERS_ADDED,
                        printers).sendToTarget();
            }
        }

        @Override
        public void onPrintersRemoved(List<PrinterId> printerIds) {
            PrinterDiscoverySession session = mWeakSession.get();
            if (session != null) {
                session.mHandler.obtainMessage(MSG_PRINTERS_REMOVED,
                        printerIds).sendToTarget();
            }
        }

        @Override
        public void onPrintersUpdated(List<PrinterInfo> printers) {
            PrinterDiscoverySession session = mWeakSession.get();
            if (session != null) {
                session.mHandler.obtainMessage(MSG_PRINTERS_UPDATED,
                        printers).sendToTarget();
            }
        }
    }
}