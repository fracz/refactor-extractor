/*
 * Copyright (C) 2006-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.server;

import com.android.internal.content.PackageMonitor;
import com.android.internal.os.HandlerCaller;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethod;
import com.android.internal.view.IInputMethodCallback;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.InputBindResult;

import com.android.server.EventLogTags;

import org.xmlpull.v1.XmlPullParserException;

import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.inputmethodservice.InputMethodService;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Pair;
import android.util.Slog;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class provides a system service that manages input methods.
 */
public class InputMethodManagerService extends IInputMethodManager.Stub
        implements ServiceConnection, Handler.Callback {
    static final boolean DEBUG = false;
    static final String TAG = "InputManagerService";

    static final int MSG_SHOW_IM_PICKER = 1;
    static final int MSG_SHOW_IM_SUBTYPE_PICKER = 2;
    static final int MSG_SHOW_IM_SUBTYPE_ENABLER = 3;
    static final int MSG_SHOW_IM_CONFIG = 4;

    static final int MSG_UNBIND_INPUT = 1000;
    static final int MSG_BIND_INPUT = 1010;
    static final int MSG_SHOW_SOFT_INPUT = 1020;
    static final int MSG_HIDE_SOFT_INPUT = 1030;
    static final int MSG_ATTACH_TOKEN = 1040;
    static final int MSG_CREATE_SESSION = 1050;

    static final int MSG_START_INPUT = 2000;
    static final int MSG_RESTART_INPUT = 2010;

    static final int MSG_UNBIND_METHOD = 3000;
    static final int MSG_BIND_METHOD = 3010;

    static final long TIME_TO_RECONNECT = 10*1000;

    private static final int NOT_A_SUBTYPE_ID = -1;
    private static final String NOT_A_SUBTYPE_ID_STR = String.valueOf(NOT_A_SUBTYPE_ID);
    private static final String SUBTYPE_MODE_KEYBOARD = "keyboard";
    private static final String SUBTYPE_MODE_VOICE = "voice";

    // TODO: Will formalize this value as API
    private static final String SUBTYPE_EXTRAVALUE_EXCLUDE_FROM_LAST_IME =
            "excludeFromLastInputMethod";

    final Context mContext;
    final Resources mRes;
    final Handler mHandler;
    final InputMethodSettings mSettings;
    final SettingsObserver mSettingsObserver;
    final StatusBarManagerService mStatusBar;
    final IWindowManager mIWindowManager;
    final HandlerCaller mCaller;

    final InputBindResult mNoBinding = new InputBindResult(null, null, -1);

    // All known input methods.  mMethodMap also serves as the global
    // lock for this class.
    final ArrayList<InputMethodInfo> mMethodList = new ArrayList<InputMethodInfo>();
    final HashMap<String, InputMethodInfo> mMethodMap = new HashMap<String, InputMethodInfo>();

    class SessionState {
        final ClientState client;
        final IInputMethod method;
        final IInputMethodSession session;

        @Override
        public String toString() {
            return "SessionState{uid " + client.uid + " pid " + client.pid
                    + " method " + Integer.toHexString(
                            System.identityHashCode(method))
                    + " session " + Integer.toHexString(
                            System.identityHashCode(session))
                    + "}";
        }

        SessionState(ClientState _client, IInputMethod _method,
                IInputMethodSession _session) {
            client = _client;
            method = _method;
            session = _session;
        }
    }

    class ClientState {
        final IInputMethodClient client;
        final IInputContext inputContext;
        final int uid;
        final int pid;
        final InputBinding binding;

        boolean sessionRequested;
        SessionState curSession;

        @Override
        public String toString() {
            return "ClientState{" + Integer.toHexString(
                    System.identityHashCode(this)) + " uid " + uid
                    + " pid " + pid + "}";
        }

        ClientState(IInputMethodClient _client, IInputContext _inputContext,
                int _uid, int _pid) {
            client = _client;
            inputContext = _inputContext;
            uid = _uid;
            pid = _pid;
            binding = new InputBinding(null, inputContext.asBinder(), uid, pid);
        }
    }

    final HashMap<IBinder, ClientState> mClients
            = new HashMap<IBinder, ClientState>();

    /**
     * Set once the system is ready to run third party code.
     */
    boolean mSystemReady;

    /**
     * Id of the currently selected input method.
     */
    String mCurMethodId;

    /**
     * The current binding sequence number, incremented every time there is
     * a new bind performed.
     */
    int mCurSeq;

    /**
     * The client that is currently bound to an input method.
     */
    ClientState mCurClient;

    /**
     * The last window token that gained focus.
     */
    IBinder mCurFocusedWindow;

    /**
     * The input context last provided by the current client.
     */
    IInputContext mCurInputContext;

    /**
     * The attributes last provided by the current client.
     */
    EditorInfo mCurAttribute;

    /**
     * The input method ID of the input method service that we are currently
     * connected to or in the process of connecting to.
     */
    String mCurId;

    /**
     * The current subtype of the current input method.
     */
    private InputMethodSubtype mCurrentSubtype;

    // This list contains the pairs of InputMethodInfo and InputMethodSubtype.
    private final HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>>
            mShortcutInputMethodsAndSubtypes =
                new HashMap<InputMethodInfo, ArrayList<InputMethodSubtype>>();

    /**
     * Set to true if our ServiceConnection is currently actively bound to
     * a service (whether or not we have gotten its IBinder back yet).
     */
    boolean mHaveConnection;

    /**
     * Set if the client has asked for the input method to be shown.
     */
    boolean mShowRequested;

    /**
     * Set if we were explicitly told to show the input method.
     */
    boolean mShowExplicitlyRequested;

    /**
     * Set if we were forced to be shown.
     */
    boolean mShowForced;

    /**
     * Set if we last told the input method to show itself.
     */
    boolean mInputShown;

    /**
     * The Intent used to connect to the current input method.
     */
    Intent mCurIntent;

    /**
     * The token we have made for the currently active input method, to
     * identify it in the future.
     */
    IBinder mCurToken;

    /**
     * If non-null, this is the input method service we are currently connected
     * to.
     */
    IInputMethod mCurMethod;

    /**
     * Time that we last initiated a bind to the input method, to determine
     * if we should try to disconnect and reconnect to it.
     */
    long mLastBindTime;

    /**
     * Have we called mCurMethod.bindInput()?
     */
    boolean mBoundToMethod;

    /**
     * Currently enabled session.  Only touched by service thread, not
     * protected by a lock.
     */
    SessionState mEnabledSession;

    /**
     * True if the screen is on.  The value is true initially.
     */
    boolean mScreenOn = true;

    int mBackDisposition = InputMethodService.BACK_DISPOSITION_DEFAULT;
    int mImeWindowVis;
    long mOldSystemSettingsVersion;

    AlertDialog.Builder mDialogBuilder;
    AlertDialog mSwitchingDialog;
    InputMethodInfo[] mIms;
    CharSequence[] mItems;
    int[] mSubtypeIds;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.DEFAULT_INPUT_METHOD), false, this);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.ENABLED_INPUT_METHODS), false, this);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE), false, this);
        }

        @Override public void onChange(boolean selfChange) {
            synchronized (mMethodMap) {
                updateFromSettingsLocked();
            }
        }
    }

    class ScreenOnOffReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                mScreenOn = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mScreenOn = false;
            } else if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                hideInputMethodMenu();
                return;
            } else {
                Slog.w(TAG, "Unexpected intent " + intent);
            }

            // Inform the current client of the change in active status
            try {
                if (mCurClient != null && mCurClient.client != null) {
                    mCurClient.client.setActive(mScreenOn);
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Got RemoteException sending 'screen on/off' notification to pid "
                        + mCurClient.pid + " uid " + mCurClient.uid);
            }
        }
    }

    class MyPackageMonitor extends PackageMonitor {

        @Override
        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            synchronized (mMethodMap) {
                String curInputMethodId = Settings.Secure.getString(mContext
                        .getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                final int N = mMethodList.size();
                if (curInputMethodId != null) {
                    for (int i=0; i<N; i++) {
                        InputMethodInfo imi = mMethodList.get(i);
                        if (imi.getId().equals(curInputMethodId)) {
                            for (String pkg : packages) {
                                if (imi.getPackageName().equals(pkg)) {
                                    if (!doit) {
                                        return true;
                                    }
                                    resetSelectedInputMethodAndSubtypeLocked("");
                                    chooseNewDefaultIMELocked();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void onSomePackagesChanged() {
            synchronized (mMethodMap) {
                InputMethodInfo curIm = null;
                String curInputMethodId = Settings.Secure.getString(mContext
                        .getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                final int N = mMethodList.size();
                if (curInputMethodId != null) {
                    for (int i=0; i<N; i++) {
                        InputMethodInfo imi = mMethodList.get(i);
                        if (imi.getId().equals(curInputMethodId)) {
                            curIm = imi;
                        }
                        int change = isPackageDisappearing(imi.getPackageName());
                        if (change == PACKAGE_TEMPORARY_CHANGE
                                || change == PACKAGE_PERMANENT_CHANGE) {
                            Slog.i(TAG, "Input method uninstalled, disabling: "
                                    + imi.getComponent());
                            setInputMethodEnabledLocked(imi.getId(), false);
                        }
                    }
                }

                buildInputMethodListLocked(mMethodList, mMethodMap);

                boolean changed = false;

                if (curIm != null) {
                    int change = isPackageDisappearing(curIm.getPackageName());
                    if (change == PACKAGE_TEMPORARY_CHANGE
                            || change == PACKAGE_PERMANENT_CHANGE) {
                        ServiceInfo si = null;
                        try {
                            si = mContext.getPackageManager().getServiceInfo(
                                    curIm.getComponent(), 0);
                        } catch (PackageManager.NameNotFoundException ex) {
                        }
                        if (si == null) {
                            // Uh oh, current input method is no longer around!
                            // Pick another one...
                            Slog.i(TAG, "Current input method removed: " + curInputMethodId);
                            mImeWindowVis = 0;
                            mStatusBar.setImeWindowStatus(mCurToken, mImeWindowVis,
                                    mBackDisposition);
                            if (!chooseNewDefaultIMELocked()) {
                                changed = true;
                                curIm = null;
                                Slog.i(TAG, "Unsetting current input method");
                                resetSelectedInputMethodAndSubtypeLocked("");
                            }
                        }
                    }
                }

                if (curIm == null) {
                    // We currently don't have a default input method... is
                    // one now available?
                    changed = chooseNewDefaultIMELocked();
                }

                if (changed) {
                    updateFromSettingsLocked();
                }
            }
        }
    }

    class MethodCallback extends IInputMethodCallback.Stub {
        final IInputMethod mMethod;

        MethodCallback(IInputMethod method) {
            mMethod = method;
        }

        public void finishedEvent(int seq, boolean handled) throws RemoteException {
        }

        public void sessionCreated(IInputMethodSession session) throws RemoteException {
            onSessionCreated(mMethod, session);
        }
    }

    public InputMethodManagerService(Context context, StatusBarManagerService statusBar) {
        mContext = context;
        mRes = context.getResources();
        mHandler = new Handler(this);
        mIWindowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
        mCaller = new HandlerCaller(context, new HandlerCaller.Callback() {
            public void executeMessage(Message msg) {
                handleMessage(msg);
            }
        });
        // Initialize the system settings version to undefined.
        mOldSystemSettingsVersion = -1;

        (new MyPackageMonitor()).register(mContext, true);

        IntentFilter screenOnOffFilt = new IntentFilter();
        screenOnOffFilt.addAction(Intent.ACTION_SCREEN_ON);
        screenOnOffFilt.addAction(Intent.ACTION_SCREEN_OFF);
        screenOnOffFilt.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mContext.registerReceiver(new ScreenOnOffReceiver(), screenOnOffFilt);

        mStatusBar = statusBar;
        statusBar.setIconVisibility("ime", false);

        // mSettings should be created before buildInputMethodListLocked
        mSettings = new InputMethodSettings(
                mRes, context.getContentResolver(), mMethodMap, mMethodList);
        buildInputMethodListLocked(mMethodList, mMethodMap);
        mSettings.enableAllIMEsIfThereIsNoEnabledIME();

        if (TextUtils.isEmpty(Settings.Secure.getString(
                mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
            InputMethodInfo defIm = null;
            for (InputMethodInfo imi: mMethodList) {
                if (defIm == null && imi.getIsDefaultResourceId() != 0) {
                    try {
                        Resources res = context.createPackageContext(
                                imi.getPackageName(), 0).getResources();
                        if (res.getBoolean(imi.getIsDefaultResourceId())) {
                            defIm = imi;
                            Slog.i(TAG, "Selected default: " + imi.getId());
                        }
                    } catch (PackageManager.NameNotFoundException ex) {
                    } catch (Resources.NotFoundException ex) {
                    }
                }
            }
            if (defIm == null && mMethodList.size() > 0) {
                defIm = mMethodList.get(0);
                Slog.i(TAG, "No default found, using " + defIm.getId());
            }
            if (defIm != null) {
                setSelectedInputMethodAndSubtypeLocked(defIm, NOT_A_SUBTYPE_ID, false);
            }
        }

        mSettingsObserver = new SettingsObserver(mHandler);
        updateFromSettingsLocked();
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            // The input method manager only throws security exceptions, so let's
            // log all others.
            if (!(e instanceof SecurityException)) {
                Slog.e(TAG, "Input Method Manager Crash", e);
            }
            throw e;
        }
    }

    public void systemReady() {
        synchronized (mMethodMap) {
            if (!mSystemReady) {
                mSystemReady = true;
                try {
                    startInputInnerLocked();
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Unexpected exception", e);
                }
            }
        }
    }

    public List<InputMethodInfo> getInputMethodList() {
        synchronized (mMethodMap) {
            return new ArrayList<InputMethodInfo>(mMethodList);
        }
    }

    public List<InputMethodInfo> getEnabledInputMethodList() {
        synchronized (mMethodMap) {
            return mSettings.getEnabledInputMethodListLocked();
        }
    }

    private HashMap<InputMethodInfo, List<InputMethodSubtype>>
            getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked() {
        HashMap<InputMethodInfo, List<InputMethodSubtype>> enabledInputMethodAndSubtypes =
                new HashMap<InputMethodInfo, List<InputMethodSubtype>>();
        for (InputMethodInfo imi: getEnabledInputMethodList()) {
            enabledInputMethodAndSubtypes.put(
                    imi, getEnabledInputMethodSubtypeListLocked(imi, true));
        }
        return enabledInputMethodAndSubtypes;
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeListLocked(InputMethodInfo imi,
            boolean allowsImplicitlySelectedSubtypes) {
        if (imi == null && mCurMethodId != null) {
            imi = mMethodMap.get(mCurMethodId);
        }
        List<InputMethodSubtype> enabledSubtypes =
                mSettings.getEnabledInputMethodSubtypeListLocked(imi);
        if (allowsImplicitlySelectedSubtypes && enabledSubtypes.isEmpty()) {
            enabledSubtypes = getApplicableSubtypesLocked(mRes, getSubtypes(imi));
        }
        return InputMethodSubtype.sort(mContext, 0, imi, enabledSubtypes);
    }

    public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(InputMethodInfo imi,
            boolean allowsImplicitlySelectedSubtypes) {
        synchronized (mMethodMap) {
            return getEnabledInputMethodSubtypeListLocked(imi, allowsImplicitlySelectedSubtypes);
        }
    }

    public void addClient(IInputMethodClient client,
            IInputContext inputContext, int uid, int pid) {
        synchronized (mMethodMap) {
            mClients.put(client.asBinder(), new ClientState(client,
                    inputContext, uid, pid));
        }
    }

    public void removeClient(IInputMethodClient client) {
        synchronized (mMethodMap) {
            mClients.remove(client.asBinder());
        }
    }

    void executeOrSendMessage(IInterface target, Message msg) {
         if (target.asBinder() instanceof Binder) {
             mCaller.sendMessage(msg);
         } else {
             handleMessage(msg);
             msg.recycle();
         }
    }

    void unbindCurrentClientLocked() {
        if (mCurClient != null) {
            if (DEBUG) Slog.v(TAG, "unbindCurrentInputLocked: client = "
                    + mCurClient.client.asBinder());
            if (mBoundToMethod) {
                mBoundToMethod = false;
                if (mCurMethod != null) {
                    executeOrSendMessage(mCurMethod, mCaller.obtainMessageO(
                            MSG_UNBIND_INPUT, mCurMethod));
                }
            }
            executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                    MSG_UNBIND_METHOD, mCurSeq, mCurClient.client));
            mCurClient.sessionRequested = false;

            // Call setActive(false) on the old client
            try {
                mCurClient.client.setActive(false);
            } catch (RemoteException e) {
                Slog.w(TAG, "Got RemoteException sending setActive(false) notification to pid "
                        + mCurClient.pid + " uid " + mCurClient.uid);
            }
            mCurClient = null;

            hideInputMethodMenuLocked();
        }
    }

    private int getImeShowFlags() {
        int flags = 0;
        if (mShowForced) {
            flags |= InputMethod.SHOW_FORCED
                    | InputMethod.SHOW_EXPLICIT;
        } else if (mShowExplicitlyRequested) {
            flags |= InputMethod.SHOW_EXPLICIT;
        }
        return flags;
    }

    private int getAppShowFlags() {
        int flags = 0;
        if (mShowForced) {
            flags |= InputMethodManager.SHOW_FORCED;
        } else if (!mShowExplicitlyRequested) {
            flags |= InputMethodManager.SHOW_IMPLICIT;
        }
        return flags;
    }

    InputBindResult attachNewInputLocked(boolean initial, boolean needResult) {
        if (!mBoundToMethod) {
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageOO(
                    MSG_BIND_INPUT, mCurMethod, mCurClient.binding));
            mBoundToMethod = true;
        }
        final SessionState session = mCurClient.curSession;
        if (initial) {
            executeOrSendMessage(session.method, mCaller.obtainMessageOOO(
                    MSG_START_INPUT, session, mCurInputContext, mCurAttribute));
        } else {
            executeOrSendMessage(session.method, mCaller.obtainMessageOOO(
                    MSG_RESTART_INPUT, session, mCurInputContext, mCurAttribute));
        }
        if (mShowRequested) {
            if (DEBUG) Slog.v(TAG, "Attach new input asks to show input");
            showCurrentInputLocked(getAppShowFlags(), null);
        }
        return needResult
                ? new InputBindResult(session.session, mCurId, mCurSeq)
                : null;
    }

    InputBindResult startInputLocked(IInputMethodClient client,
            IInputContext inputContext, EditorInfo attribute,
            boolean initial, boolean needResult) {
        // If no method is currently selected, do nothing.
        if (mCurMethodId == null) {
            return mNoBinding;
        }

        ClientState cs = mClients.get(client.asBinder());
        if (cs == null) {
            throw new IllegalArgumentException("unknown client "
                    + client.asBinder());
        }

        try {
            if (!mIWindowManager.inputMethodClientHasFocus(cs.client)) {
                // Check with the window manager to make sure this client actually
                // has a window with focus.  If not, reject.  This is thread safe
                // because if the focus changes some time before or after, the
                // next client receiving focus that has any interest in input will
                // be calling through here after that change happens.
                Slog.w(TAG, "Starting input on non-focused client " + cs.client
                        + " (uid=" + cs.uid + " pid=" + cs.pid + ")");
                return null;
            }
        } catch (RemoteException e) {
        }

        if (mCurClient != cs) {
            // If the client is changing, we need to switch over to the new
            // one.
            unbindCurrentClientLocked();
            if (DEBUG) Slog.v(TAG, "switching to client: client = "
                    + cs.client.asBinder());

            // If the screen is on, inform the new client it is active
            if (mScreenOn) {
                try {
                    cs.client.setActive(mScreenOn);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Got RemoteException sending setActive notification to pid "
                            + cs.pid + " uid " + cs.uid);
                }
            }
        }

        // Bump up the sequence for this client and attach it.
        mCurSeq++;
        if (mCurSeq <= 0) mCurSeq = 1;
        mCurClient = cs;
        mCurInputContext = inputContext;
        mCurAttribute = attribute;

        // Check if the input method is changing.
        if (mCurId != null && mCurId.equals(mCurMethodId)) {
            if (cs.curSession != null) {
                // Fast case: if we are already connected to the input method,
                // then just return it.
                return attachNewInputLocked(initial, needResult);
            }
            if (mHaveConnection) {
                if (mCurMethod != null) {
                    if (!cs.sessionRequested) {
                        cs.sessionRequested = true;
                        if (DEBUG) Slog.v(TAG, "Creating new session for client " + cs);
                        executeOrSendMessage(mCurMethod, mCaller.obtainMessageOO(
                                MSG_CREATE_SESSION, mCurMethod,
                                new MethodCallback(mCurMethod)));
                    }
                    // Return to client, and we will get back with it when
                    // we have had a session made for it.
                    return new InputBindResult(null, mCurId, mCurSeq);
                } else if (SystemClock.uptimeMillis()
                        < (mLastBindTime+TIME_TO_RECONNECT)) {
                    // In this case we have connected to the service, but
                    // don't yet have its interface.  If it hasn't been too
                    // long since we did the connection, we'll return to
                    // the client and wait to get the service interface so
                    // we can report back.  If it has been too long, we want
                    // to fall through so we can try a disconnect/reconnect
                    // to see if we can get back in touch with the service.
                    return new InputBindResult(null, mCurId, mCurSeq);
                } else {
                    EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME,
                            mCurMethodId, SystemClock.uptimeMillis()-mLastBindTime, 0);
                }
            }
        }

        return startInputInnerLocked();
    }

    InputBindResult startInputInnerLocked() {
        if (mCurMethodId == null) {
            return mNoBinding;
        }

        if (!mSystemReady) {
            // If the system is not yet ready, we shouldn't be running third
            // party code.
            return new InputBindResult(null, mCurMethodId, mCurSeq);
        }

        InputMethodInfo info = mMethodMap.get(mCurMethodId);
        if (info == null) {
            throw new IllegalArgumentException("Unknown id: " + mCurMethodId);
        }

        unbindCurrentMethodLocked(false);

        mCurIntent = new Intent(InputMethod.SERVICE_INTERFACE);
        mCurIntent.setComponent(info.getComponent());
        mCurIntent.putExtra(Intent.EXTRA_CLIENT_LABEL,
                com.android.internal.R.string.input_method_binding_label);
        mCurIntent.putExtra(Intent.EXTRA_CLIENT_INTENT, PendingIntent.getActivity(
                mContext, 0, new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0));
        if (mContext.bindService(mCurIntent, this, Context.BIND_AUTO_CREATE)) {
            mLastBindTime = SystemClock.uptimeMillis();
            mHaveConnection = true;
            mCurId = info.getId();
            mCurToken = new Binder();
            try {
                if (DEBUG) Slog.v(TAG, "Adding window token: " + mCurToken);
                mIWindowManager.addWindowToken(mCurToken,
                        WindowManager.LayoutParams.TYPE_INPUT_METHOD);
            } catch (RemoteException e) {
            }
            return new InputBindResult(null, mCurId, mCurSeq);
        } else {
            mCurIntent = null;
            Slog.w(TAG, "Failure connecting to input method service: "
                    + mCurIntent);
        }
        return null;
    }

    public InputBindResult startInput(IInputMethodClient client,
            IInputContext inputContext, EditorInfo attribute,
            boolean initial, boolean needResult) {
        synchronized (mMethodMap) {
            final long ident = Binder.clearCallingIdentity();
            try {
                return startInputLocked(client, inputContext, attribute,
                        initial, needResult);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void finishInput(IInputMethodClient client) {
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (mMethodMap) {
            if (mCurIntent != null && name.equals(mCurIntent.getComponent())) {
                mCurMethod = IInputMethod.Stub.asInterface(service);
                if (mCurToken == null) {
                    Slog.w(TAG, "Service connected without a token!");
                    unbindCurrentMethodLocked(false);
                    return;
                }
                if (DEBUG) Slog.v(TAG, "Initiating attach with token: " + mCurToken);
                executeOrSendMessage(mCurMethod, mCaller.obtainMessageOO(
                        MSG_ATTACH_TOKEN, mCurMethod, mCurToken));
                if (mCurClient != null) {
                    if (DEBUG) Slog.v(TAG, "Creating first session while with client "
                            + mCurClient);
                    executeOrSendMessage(mCurMethod, mCaller.obtainMessageOO(
                            MSG_CREATE_SESSION, mCurMethod,
                            new MethodCallback(mCurMethod)));
                }
            }
        }
    }

    void onSessionCreated(IInputMethod method, IInputMethodSession session) {
        synchronized (mMethodMap) {
            if (mCurMethod != null && method != null
                    && mCurMethod.asBinder() == method.asBinder()) {
                if (mCurClient != null) {
                    mCurClient.curSession = new SessionState(mCurClient,
                            method, session);
                    mCurClient.sessionRequested = false;
                    InputBindResult res = attachNewInputLocked(true, true);
                    if (res.method != null) {
                        executeOrSendMessage(mCurClient.client, mCaller.obtainMessageOO(
                                MSG_BIND_METHOD, mCurClient.client, res));
                    }
                }
            }
        }
    }

    void unbindCurrentMethodLocked(boolean reportToClient) {
        if (mHaveConnection) {
            mContext.unbindService(this);
            mHaveConnection = false;
        }

        if (mCurToken != null) {
            try {
                if (DEBUG) Slog.v(TAG, "Removing window token: " + mCurToken);
                mIWindowManager.removeWindowToken(mCurToken);
            } catch (RemoteException e) {
            }
            mCurToken = null;
        }

        mCurId = null;
        clearCurMethodLocked();

        if (reportToClient && mCurClient != null) {
            executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                    MSG_UNBIND_METHOD, mCurSeq, mCurClient.client));
        }
    }

    private void finishSession(SessionState sessionState) {
        if (sessionState != null && sessionState.session != null) {
            try {
                sessionState.session.finishSession();
            } catch (RemoteException e) {
                Slog.w(TAG, "Session failed to close due to remote exception", e);
            }
        }
    }

    void clearCurMethodLocked() {
        if (mCurMethod != null) {
            for (ClientState cs : mClients.values()) {
                cs.sessionRequested = false;
                finishSession(cs.curSession);
                cs.curSession = null;
            }

            finishSession(mEnabledSession);
            mEnabledSession = null;
            mCurMethod = null;
        }
        mStatusBar.setIconVisibility("ime", false);
    }

    public void onServiceDisconnected(ComponentName name) {
        synchronized (mMethodMap) {
            if (DEBUG) Slog.v(TAG, "Service disconnected: " + name
                    + " mCurIntent=" + mCurIntent);
            if (mCurMethod != null && mCurIntent != null
                    && name.equals(mCurIntent.getComponent())) {
                clearCurMethodLocked();
                // We consider this to be a new bind attempt, since the system
                // should now try to restart the service for us.
                mLastBindTime = SystemClock.uptimeMillis();
                mShowRequested = mInputShown;
                mInputShown = false;
                if (mCurClient != null) {
                    executeOrSendMessage(mCurClient.client, mCaller.obtainMessageIO(
                            MSG_UNBIND_METHOD, mCurSeq, mCurClient.client));
                }
            }
        }
    }

    public void updateStatusIcon(IBinder token, String packageName, int iconId) {
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            if (token == null || mCurToken != token) {
                Slog.w(TAG, "Ignoring setInputMethod of uid " + uid + " token: " + token);
                return;
            }

            synchronized (mMethodMap) {
                if (iconId == 0) {
                    if (DEBUG) Slog.d(TAG, "hide the small icon for the input method");
                    mStatusBar.setIconVisibility("ime", false);
                } else if (packageName != null) {
                    if (DEBUG) Slog.d(TAG, "show a small icon for the input method");
                    mStatusBar.setIcon("ime", packageName, iconId, 0);
                    mStatusBar.setIconVisibility("ime", true);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            if (token == null || mCurToken != token) {
                Slog.w(TAG, "Ignoring setImeWindowStatus of uid " + uid + " token: " + token);
                return;
            }

            synchronized (mMethodMap) {
                mImeWindowVis = vis;
                mBackDisposition = backDisposition;
                mStatusBar.setImeWindowStatus(token, vis, backDisposition);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    // TODO: Investigate and fix why are settings changes getting processed before the settings seq
    // number is updated?
    // TODO: Change this stuff to not rely on modifying settings for normal user interactions.
    void updateFromSettingsLocked() {
        long newSystemSettingsVersion = getSystemSettingsVersion();
        // This is a workaround to avoid a situation that old cached value in Settings.Secure
        // will be handled.
        if (newSystemSettingsVersion == mOldSystemSettingsVersion) return;

        // We are assuming that whoever is changing DEFAULT_INPUT_METHOD and
        // ENABLED_INPUT_METHODS is taking care of keeping them correctly in
        // sync, so we will never have a DEFAULT_INPUT_METHOD that is not
        // enabled.
        String id = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        // There is no input method selected, try to choose new applicable input method.
        if (TextUtils.isEmpty(id) && chooseNewDefaultIMELocked()) {
            id = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
        }
        if (!TextUtils.isEmpty(id)) {
            try {
                setInputMethodLocked(id, getSelectedInputMethodSubtypeId(id));
            } catch (IllegalArgumentException e) {
                Slog.w(TAG, "Unknown input method from prefs: " + id, e);
                mCurMethodId = null;
                unbindCurrentMethodLocked(true);
            }
            mShortcutInputMethodsAndSubtypes.clear();
        } else {
            // There is no longer an input method set, so stop any current one.
            mCurMethodId = null;
            unbindCurrentMethodLocked(true);
        }
    }

    /* package */ void setInputMethodLocked(String id, int subtypeId) {
        InputMethodInfo info = mMethodMap.get(id);
        if (info == null) {
            throw new IllegalArgumentException("Unknown id: " + id);
        }

        if (id.equals(mCurMethodId)) {
            InputMethodSubtype subtype = null;
            if (subtypeId >= 0 && subtypeId < info.getSubtypeCount()) {
                subtype = info.getSubtypeAt(subtypeId);
            }
            if (subtype != mCurrentSubtype) {
                synchronized (mMethodMap) {
                    if (subtype != null) {
                        setSelectedInputMethodAndSubtypeLocked(info, subtypeId, true);
                    }
                    if (mCurMethod != null) {
                        try {
                            final Configuration conf = mRes.getConfiguration();
                            final boolean haveHardKeyboard = conf.keyboard
                                    != Configuration.KEYBOARD_NOKEYS;
                            final boolean hardKeyShown = haveHardKeyboard
                                    && conf.hardKeyboardHidden
                                            != Configuration.HARDKEYBOARDHIDDEN_YES;
                            mImeWindowVis = (mInputShown || hardKeyShown) ? (
                                    InputMethodService.IME_ACTIVE | InputMethodService.IME_VISIBLE)
                                    : 0;
                            mStatusBar.setImeWindowStatus(mCurToken, mImeWindowVis,
                                    mBackDisposition);
                            // If subtype is null, try to find the most applicable one from
                            // getCurrentInputMethodSubtype.
                            if (subtype == null) {
                                subtype = getCurrentInputMethodSubtype();
                            }
                            mCurMethod.changeInputMethodSubtype(subtype);
                        } catch (RemoteException e) {
                            return;
                        }
                    }
                }
            }
            return;
        }

        final long ident = Binder.clearCallingIdentity();
        try {
            // Set a subtype to this input method.
            // subtypeId the name of a subtype which will be set.
            setSelectedInputMethodAndSubtypeLocked(info, subtypeId, false);
            // mCurMethodId should be updated after setSelectedInputMethodAndSubtypeLocked()
            // because mCurMethodId is stored as a history in
            // setSelectedInputMethodAndSubtypeLocked().
            mCurMethodId = id;

            if (ActivityManagerNative.isSystemReady()) {
                Intent intent = new Intent(Intent.ACTION_INPUT_METHOD_CHANGED);
                intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
                intent.putExtra("input_method_id", id);
                mContext.sendBroadcast(intent);
            }
            unbindCurrentClientLocked();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean showSoftInput(IInputMethodClient client, int flags,
            ResultReceiver resultReceiver) {
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mMethodMap) {
                if (mCurClient == null || client == null
                        || mCurClient.client.asBinder() != client.asBinder()) {
                    try {
                        // We need to check if this is the current client with
                        // focus in the window manager, to allow this call to
                        // be made before input is started in it.
                        if (!mIWindowManager.inputMethodClientHasFocus(client)) {
                            Slog.w(TAG, "Ignoring showSoftInput of uid " + uid + ": " + client);
                            return false;
                        }
                    } catch (RemoteException e) {
                        return false;
                    }
                }

                if (DEBUG) Slog.v(TAG, "Client requesting input be shown");
                return showCurrentInputLocked(flags, resultReceiver);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean showCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        mShowRequested = true;
        if ((flags&InputMethodManager.SHOW_IMPLICIT) == 0) {
            mShowExplicitlyRequested = true;
        }
        if ((flags&InputMethodManager.SHOW_FORCED) != 0) {
            mShowExplicitlyRequested = true;
            mShowForced = true;
        }

        if (!mSystemReady) {
            return false;
        }

        boolean res = false;
        if (mCurMethod != null) {
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageIOO(
                    MSG_SHOW_SOFT_INPUT, getImeShowFlags(), mCurMethod,
                    resultReceiver));
            mInputShown = true;
            res = true;
        } else if (mHaveConnection && SystemClock.uptimeMillis()
                < (mLastBindTime+TIME_TO_RECONNECT)) {
            // The client has asked to have the input method shown, but
            // we have been sitting here too long with a connection to the
            // service and no interface received, so let's disconnect/connect
            // to try to prod things along.
            EventLog.writeEvent(EventLogTags.IMF_FORCE_RECONNECT_IME, mCurMethodId,
                    SystemClock.uptimeMillis()-mLastBindTime,1);
            mContext.unbindService(this);
            mContext.bindService(mCurIntent, this, Context.BIND_AUTO_CREATE);
        }

        return res;
    }

    public boolean hideSoftInput(IInputMethodClient client, int flags,
            ResultReceiver resultReceiver) {
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mMethodMap) {
                if (mCurClient == null || client == null
                        || mCurClient.client.asBinder() != client.asBinder()) {
                    try {
                        // We need to check if this is the current client with
                        // focus in the window manager, to allow this call to
                        // be made before input is started in it.
                        if (!mIWindowManager.inputMethodClientHasFocus(client)) {
                            if (DEBUG) Slog.w(TAG, "Ignoring hideSoftInput of uid "
                                    + uid + ": " + client);
                            mImeWindowVis = 0;
                            mStatusBar.setImeWindowStatus(mCurToken, mImeWindowVis,
                                    mBackDisposition);
                            return false;
                        }
                    } catch (RemoteException e) {
                        mImeWindowVis = 0;
                        mStatusBar.setImeWindowStatus(mCurToken, mImeWindowVis, mBackDisposition);
                        return false;
                    }
                }

                if (DEBUG) Slog.v(TAG, "Client requesting input be hidden");
                return hideCurrentInputLocked(flags, resultReceiver);
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    boolean hideCurrentInputLocked(int flags, ResultReceiver resultReceiver) {
        if ((flags&InputMethodManager.HIDE_IMPLICIT_ONLY) != 0
                && (mShowExplicitlyRequested || mShowForced)) {
            if (DEBUG) Slog.v(TAG,
                    "Not hiding: explicit show not cancelled by non-explicit hide");
            return false;
        }
        if (mShowForced && (flags&InputMethodManager.HIDE_NOT_ALWAYS) != 0) {
            if (DEBUG) Slog.v(TAG,
                    "Not hiding: forced show not cancelled by not-always hide");
            return false;
        }
        boolean res;
        if (mInputShown && mCurMethod != null) {
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageOO(
                    MSG_HIDE_SOFT_INPUT, mCurMethod, resultReceiver));
            res = true;
        } else {
            res = false;
        }
        mInputShown = false;
        mShowRequested = false;
        mShowExplicitlyRequested = false;
        mShowForced = false;
        return res;
    }

    public void windowGainedFocus(IInputMethodClient client, IBinder windowToken,
            boolean viewHasFocus, boolean isTextEditor, int softInputMode,
            boolean first, int windowFlags) {
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (mMethodMap) {
                if (DEBUG) Slog.v(TAG, "windowGainedFocus: " + client.asBinder()
                        + " viewHasFocus=" + viewHasFocus
                        + " isTextEditor=" + isTextEditor
                        + " softInputMode=#" + Integer.toHexString(softInputMode)
                        + " first=" + first + " flags=#"
                        + Integer.toHexString(windowFlags));

                if (mCurClient == null || client == null
                        || mCurClient.client.asBinder() != client.asBinder()) {
                    try {
                        // We need to check if this is the current client with
                        // focus in the window manager, to allow this call to
                        // be made before input is started in it.
                        if (!mIWindowManager.inputMethodClientHasFocus(client)) {
                            Slog.w(TAG, "Client not active, ignoring focus gain of: " + client);
                            return;
                        }
                    } catch (RemoteException e) {
                    }
                }

                if (mCurFocusedWindow == windowToken) {
                    Slog.w(TAG, "Window already focused, ignoring focus gain of: " + client);
                    return;
                }
                mCurFocusedWindow = windowToken;

                // Should we auto-show the IME even if the caller has not
                // specified what should be done with it?
                // We only do this automatically if the window can resize
                // to accommodate the IME (so what the user sees will give
                // them good context without input information being obscured
                // by the IME) or if running on a large screen where there
                // is more room for the target window + IME.
                final boolean doAutoShow =
                        (softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                                == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        || mRes.getConfiguration().isLayoutSizeAtLeast(
                                Configuration.SCREENLAYOUT_SIZE_LARGE);

                switch (softInputMode&WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE) {
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED:
                        if (!isTextEditor || !doAutoShow) {
                            if (WindowManager.LayoutParams.mayUseInputMethod(windowFlags)) {
                                // There is no focus view, and this window will
                                // be behind any soft input window, so hide the
                                // soft input window if it is shown.
                                if (DEBUG) Slog.v(TAG, "Unspecified window will hide input");
                                hideCurrentInputLocked(InputMethodManager.HIDE_NOT_ALWAYS, null);
                            }
                        } else if (isTextEditor && doAutoShow && (softInputMode &
                                WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) != 0) {
                            // There is a focus view, and we are navigating forward
                            // into the window, so show the input window for the user.
                            // We only do this automatically if the window an resize
                            // to accomodate the IME (so what the user sees will give
                            // them good context without input information being obscured
                            // by the IME) or if running on a large screen where there
                            // is more room for the target window + IME.
                            if (DEBUG) Slog.v(TAG, "Unspecified window will show input");
                            showCurrentInputLocked(InputMethodManager.SHOW_IMPLICIT, null);
                        }
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED:
                        // Do nothing.
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN:
                        if ((softInputMode &
                                WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) != 0) {
                            if (DEBUG) Slog.v(TAG, "Window asks to hide input going forward");
                            hideCurrentInputLocked(0, null);
                        }
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN:
                        if (DEBUG) Slog.v(TAG, "Window asks to hide input");
                        hideCurrentInputLocked(0, null);
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE:
                        if ((softInputMode &
                                WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) != 0) {
                            if (DEBUG) Slog.v(TAG, "Window asks to show input going forward");
                            showCurrentInputLocked(InputMethodManager.SHOW_IMPLICIT, null);
                        }
                        break;
                    case WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE:
                        if (DEBUG) Slog.v(TAG, "Window asks to always show input");
                        showCurrentInputLocked(InputMethodManager.SHOW_IMPLICIT, null);
                        break;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void showInputMethodPickerFromClient(IInputMethodClient client) {
        synchronized (mMethodMap) {
            if (mCurClient == null || client == null
                    || mCurClient.client.asBinder() != client.asBinder()) {
                Slog.w(TAG, "Ignoring showInputMethodPickerFromClient of uid "
                        + Binder.getCallingUid() + ": " + client);
            }

            // Always call subtype picker, because subtype picker is a superset of input method
            // picker.
            mHandler.sendEmptyMessage(MSG_SHOW_IM_SUBTYPE_PICKER);
        }
    }

    public void setInputMethod(IBinder token, String id) {
        setInputMethodWithSubtypeId(token, id, NOT_A_SUBTYPE_ID);
    }

    public void setInputMethodAndSubtype(IBinder token, String id, InputMethodSubtype subtype) {
        synchronized (mMethodMap) {
            if (subtype != null) {
                setInputMethodWithSubtypeId(token, id, getSubtypeIdFromHashCode(
                        mMethodMap.get(id), subtype.hashCode()));
            } else {
                setInputMethod(token, id);
            }
        }
    }

    public void showInputMethodAndSubtypeEnablerFromClient(
            IInputMethodClient client, String inputMethodId) {
        synchronized (mMethodMap) {
            if (mCurClient == null || client == null
                || mCurClient.client.asBinder() != client.asBinder()) {
                Slog.w(TAG, "Ignoring showInputMethodAndSubtypeEnablerFromClient of: " + client);
            }
            executeOrSendMessage(mCurMethod, mCaller.obtainMessageO(
                    MSG_SHOW_IM_SUBTYPE_ENABLER, inputMethodId));
        }
    }

    public boolean switchToLastInputMethod(IBinder token) {
        synchronized (mMethodMap) {
            final Pair<String, String> lastIme = mSettings.getLastInputMethodAndSubtypeLocked();
            if (lastIme == null) return false;
            final InputMethodInfo lastImi = mMethodMap.get(lastIme.first);
            if (lastImi == null) return false;

            final boolean imiIdIsSame = lastImi.getId().equals(mCurMethodId);
            final int lastSubtypeHash = Integer.valueOf(lastIme.second);
            // If the last IME is the same as the current IME and the last subtype is not defined,
            // there is no need to switch to the last IME.
            if (imiIdIsSame && lastSubtypeHash == NOT_A_SUBTYPE_ID) return false;

            int currentSubtypeHash = mCurrentSubtype == null ? NOT_A_SUBTYPE_ID
                    : mCurrentSubtype.hashCode();
            if (!imiIdIsSame || lastSubtypeHash != currentSubtypeHash) {
                if (DEBUG) {
                    Slog.d(TAG, "Switch to: " + lastImi.getId() + ", " + lastIme.second + ", from: "
                            + mCurMethodId + ", " + currentSubtypeHash);
                }
                setInputMethodWithSubtypeId(token, lastIme.first, getSubtypeIdFromHashCode(
                        lastImi, lastSubtypeHash));
                return true;
            }
            return false;
        }
    }

    private void setInputMethodWithSubtypeId(IBinder token, String id, int subtypeId) {
        synchronized (mMethodMap) {
            if (token == null) {
                if (mContext.checkCallingOrSelfPermission(
                        android.Manifest.permission.WRITE_SECURE_SETTINGS)
                        != PackageManager.PERMISSION_GRANTED) {
                    throw new SecurityException(
                            "Using null token requires permission "
                            + android.Manifest.permission.WRITE_SECURE_SETTINGS);
                }
            } else if (mCurToken != token) {
                Slog.w(TAG, "Ignoring setInputMethod of uid " + Binder.getCallingUid()
                        + " token: " + token);
                return;
            }

            long ident = Binder.clearCallingIdentity();
            try {
                setInputMethodLocked(id, subtypeId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void hideMySoftInput(IBinder token, int flags) {
        synchronized (mMethodMap) {
            if (token == null || mCurToken != token) {
                if (DEBUG) Slog.w(TAG, "Ignoring hideInputMethod of uid "
                        + Binder.getCallingUid() + " token: " + token);
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                hideCurrentInputLocked(flags, null);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void showMySoftInput(IBinder token, int flags) {
        synchronized (mMethodMap) {
            if (token == null || mCurToken != token) {
                Slog.w(TAG, "Ignoring showMySoftInput of uid "
                        + Binder.getCallingUid() + " token: " + token);
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                showCurrentInputLocked(flags, null);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    void setEnabledSessionInMainThread(SessionState session) {
        if (mEnabledSession != session) {
            if (mEnabledSession != null) {
                try {
                    if (DEBUG) Slog.v(TAG, "Disabling: " + mEnabledSession);
                    mEnabledSession.method.setSessionEnabled(
                            mEnabledSession.session, false);
                } catch (RemoteException e) {
                }
            }
            mEnabledSession = session;
            try {
                if (DEBUG) Slog.v(TAG, "Enabling: " + mEnabledSession);
                session.method.setSessionEnabled(
                        session.session, true);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean handleMessage(Message msg) {
        HandlerCaller.SomeArgs args;
        switch (msg.what) {
            case MSG_SHOW_IM_PICKER:
                showInputMethodMenu();
                return true;

            case MSG_SHOW_IM_SUBTYPE_PICKER:
                showInputMethodSubtypeMenu();
                return true;

            case MSG_SHOW_IM_SUBTYPE_ENABLER:
                args = (HandlerCaller.SomeArgs)msg.obj;
                showInputMethodAndSubtypeEnabler((String)args.arg1);
                return true;

            case MSG_SHOW_IM_CONFIG:
                showConfigureInputMethods();
                return true;

            // ---------------------------------------------------------

            case MSG_UNBIND_INPUT:
                try {
                    ((IInputMethod)msg.obj).unbindInput();
                } catch (RemoteException e) {
                    // There is nothing interesting about the method dying.
                }
                return true;
            case MSG_BIND_INPUT:
                args = (HandlerCaller.SomeArgs)msg.obj;
                try {
                    ((IInputMethod)args.arg1).bindInput((InputBinding)args.arg2);
                } catch (RemoteException e) {
                }
                return true;
            case MSG_SHOW_SOFT_INPUT:
                args = (HandlerCaller.SomeArgs)msg.obj;
                try {
                    ((IInputMethod)args.arg1).showSoftInput(msg.arg1,
                            (ResultReceiver)args.arg2);
                } catch (RemoteException e) {
                }
                return true;
            case MSG_HIDE_SOFT_INPUT:
                args = (HandlerCaller.SomeArgs)msg.obj;
                try {
                    ((IInputMethod)args.arg1).hideSoftInput(0,
                            (ResultReceiver)args.arg2);
                } catch (RemoteException e) {
                }
                return true;
            case MSG_ATTACH_TOKEN:
                args = (HandlerCaller.SomeArgs)msg.obj;
                try {
                    if (DEBUG) Slog.v(TAG, "Sending attach of token: " + args.arg2);
                    ((IInputMethod)args.arg1).attachToken((IBinder)args.arg2);
                } catch (RemoteException e) {
                }
                return true;
            case MSG_CREATE_SESSION:
                args = (HandlerCaller.SomeArgs)msg.obj;
                try {
                    ((IInputMethod)args.arg1).createSession(
                            (IInputMethodCallback)args.arg2);
                } catch (RemoteException e) {
                }
                return true;
            // ---------------------------------------------------------

            case MSG_START_INPUT:
                args = (HandlerCaller.SomeArgs)msg.obj;
                try {
                    SessionState session = (SessionState)args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.startInput((IInputContext)args.arg2,
                            (EditorInfo)args.arg3);
                } catch (RemoteException e) {
                }
                return true;
            case MSG_RESTART_INPUT:
                args = (HandlerCaller.SomeArgs)msg.obj;
                try {
                    SessionState session = (SessionState)args.arg1;
                    setEnabledSessionInMainThread(session);
                    session.method.restartInput((IInputContext)args.arg2,
                            (EditorInfo)args.arg3);
                } catch (RemoteException e) {
                }
                return true;

            // ---------------------------------------------------------

            case MSG_UNBIND_METHOD:
                try {
                    ((IInputMethodClient)msg.obj).onUnbindMethod(msg.arg1);
                } catch (RemoteException e) {
                    // There is nothing interesting about the last client dying.
                }
                return true;
            case MSG_BIND_METHOD:
                args = (HandlerCaller.SomeArgs)msg.obj;
                try {
                    ((IInputMethodClient)args.arg1).onBindMethod(
                            (InputBindResult)args.arg2);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Client died receiving input method " + args.arg2);
                }
                return true;
        }
        return false;
    }

    private boolean isSystemIme(InputMethodInfo inputMethod) {
        return (inputMethod.getServiceInfo().applicationInfo.flags
                & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private static ArrayList<InputMethodSubtype> getSubtypes(InputMethodInfo imi) {
        ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
        final int subtypeCount = imi.getSubtypeCount();
        for (int i = 0; i < subtypeCount; ++i) {
            subtypes.add(imi.getSubtypeAt(i));
        }
        return subtypes;
    }

    private boolean chooseNewDefaultIMELocked() {
        List<InputMethodInfo> enabled = mSettings.getEnabledInputMethodListLocked();
        if (enabled != null && enabled.size() > 0) {
            // We'd prefer to fall back on a system IME, since that is safer.
            int i=enabled.size();
            while (i > 0) {
                i--;
                if ((enabled.get(i).getServiceInfo().applicationInfo.flags
                        & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    break;
                }
            }
            InputMethodInfo imi = enabled.get(i);
            if (DEBUG) {
                Slog.d(TAG, "New default IME was selected: " + imi.getId());
            }
            resetSelectedInputMethodAndSubtypeLocked(imi.getId());
            return true;
        }

        return false;
    }

    void buildInputMethodListLocked(ArrayList<InputMethodInfo> list,
            HashMap<String, InputMethodInfo> map) {
        list.clear();
        map.clear();

        PackageManager pm = mContext.getPackageManager();
        final Configuration config = mRes.getConfiguration();
        final boolean haveHardKeyboard = config.keyboard == Configuration.KEYBOARD_QWERTY;
        String disabledSysImes = Settings.Secure.getString(mContext.getContentResolver(),
                Secure.DISABLED_SYSTEM_INPUT_METHODS);
        if (disabledSysImes == null) disabledSysImes = "";

        List<ResolveInfo> services = pm.queryIntentServices(
                new Intent(InputMethod.SERVICE_INTERFACE),
                PackageManager.GET_META_DATA);

        for (int i = 0; i < services.size(); ++i) {
            ResolveInfo ri = services.get(i);
            ServiceInfo si = ri.serviceInfo;
            ComponentName compName = new ComponentName(si.packageName, si.name);
            if (!android.Manifest.permission.BIND_INPUT_METHOD.equals(
                    si.permission)) {
                Slog.w(TAG, "Skipping input method " + compName
                        + ": it does not require the permission "
                        + android.Manifest.permission.BIND_INPUT_METHOD);
                continue;
            }

            if (DEBUG) Slog.d(TAG, "Checking " + compName);

            try {
                InputMethodInfo p = new InputMethodInfo(mContext, ri);
                list.add(p);
                final String id = p.getId();
                map.put(id, p);

                // System IMEs are enabled by default, unless there's a hard keyboard
                // and the system IME was explicitly disabled
                if (isSystemIme(p) && (!haveHardKeyboard || disabledSysImes.indexOf(id) < 0)) {
                    setInputMethodEnabledLocked(id, true);
                }

                if (DEBUG) {
                    Slog.d(TAG, "Found a third-party input method " + p);
                }

            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Unable to load input method " + compName, e);
            } catch (IOException e) {
                Slog.w(TAG, "Unable to load input method " + compName, e);
            }
        }

        String defaultIme = Settings.Secure.getString(mContext
                .getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (!TextUtils.isEmpty(defaultIme) && !map.containsKey(defaultIme)) {
            if (chooseNewDefaultIMELocked()) {
                updateFromSettingsLocked();
            }
        }
    }

    // ----------------------------------------------------------------------

    private void showInputMethodMenu() {
        showInputMethodMenuInternal(false);
    }

    private void showInputMethodSubtypeMenu() {
        showInputMethodMenuInternal(true);
    }

    private void showInputMethodAndSubtypeEnabler(String inputMethodId) {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (!TextUtils.isEmpty(inputMethodId)) {
            intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, inputMethodId);
        }
        mContext.startActivity(intent);
    }

    private void showConfigureInputMethods() {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);
    }

    private void showInputMethodMenuInternal(boolean showSubtypes) {
        if (DEBUG) Slog.v(TAG, "Show switching menu");

        final Context context = mContext;

        final PackageManager pm = context.getPackageManager();

        String lastInputMethodId = Settings.Secure.getString(context
                .getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        int lastInputMethodSubtypeId = getSelectedInputMethodSubtypeId(lastInputMethodId);
        if (DEBUG) Slog.v(TAG, "Current IME: " + lastInputMethodId);

        synchronized (mMethodMap) {
            final HashMap<InputMethodInfo, List<InputMethodSubtype>> immis =
                    getExplicitlyOrImplicitlyEnabledInputMethodsAndSubtypeListLocked();
            if (immis == null || immis.size() == 0) {
                return;
            }

            hideInputMethodMenuLocked();

            final Map<CharSequence, Pair<InputMethodInfo, Integer>> imMap =
                new TreeMap<CharSequence, Pair<InputMethodInfo, Integer>>(Collator.getInstance());

            for (InputMethodInfo imi: immis.keySet()) {
                if (imi == null) continue;
                List<InputMethodSubtype> explicitlyOrImplicitlyEnabledSubtypeList = immis.get(imi);
                HashSet<String> enabledSubtypeSet = new HashSet<String>();
                for (InputMethodSubtype subtype: explicitlyOrImplicitlyEnabledSubtypeList) {
                    enabledSubtypeSet.add(String.valueOf(subtype.hashCode()));
                }
                ArrayList<InputMethodSubtype> subtypes = getSubtypes(imi);
                CharSequence label = imi.loadLabel(pm);
                if (showSubtypes && enabledSubtypeSet.size() > 0) {
                    final int subtypeCount = imi.getSubtypeCount();
                    for (int j = 0; j < subtypeCount; ++j) {
                        InputMethodSubtype subtype = imi.getSubtypeAt(j);
                        if (enabledSubtypeSet.contains(String.valueOf(subtype.hashCode()))) {
                            CharSequence title;
                            int nameResId = subtype.getNameResId();
                            String mode = subtype.getMode();
                            if (nameResId != 0) {
                                title = pm.getText(imi.getPackageName(), nameResId,
                                        imi.getServiceInfo().applicationInfo);
                            } else {
                                CharSequence language = subtype.getLocale();
                                // TODO: Use more friendly Title and UI
                                title = label + "," + (mode == null ? "" : mode) + ","
                                        + (language == null ? "" : language);
                            }
                            imMap.put(title, new Pair<InputMethodInfo, Integer>(imi, j));
                        }
                    }
                } else {
                    imMap.put(label,
                            new Pair<InputMethodInfo, Integer>(imi, NOT_A_SUBTYPE_ID));
                }
            }

            final int N = imMap.size();
            mItems = imMap.keySet().toArray(new CharSequence[N]);
            mIms = new InputMethodInfo[N];
            mSubtypeIds = new int[N];
            int checkedItem = 0;
            for (int i = 0; i < N; ++i) {
                Pair<InputMethodInfo, Integer> value = imMap.get(mItems[i]);
                mIms[i] = value.first;
                mSubtypeIds[i] = value.second;
                if (mIms[i].getId().equals(lastInputMethodId)) {
                    int subtypeId = mSubtypeIds[i];
                    if ((subtypeId == NOT_A_SUBTYPE_ID)
                            || (lastInputMethodSubtypeId == NOT_A_SUBTYPE_ID && subtypeId == 0)
                            || (subtypeId == lastInputMethodSubtypeId)) {
                        checkedItem = i;
                    }
                }
            }

            AlertDialog.OnClickListener adocl = new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    hideInputMethodMenu();
                }
            };

            TypedArray a = context.obtainStyledAttributes(null,
                    com.android.internal.R.styleable.DialogPreference,
                    com.android.internal.R.attr.alertDialogStyle, 0);
            mDialogBuilder = new AlertDialog.Builder(context)
                    .setTitle(com.android.internal.R.string.select_input_method)
                    .setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            hideInputMethodMenu();
                        }
                    })
                    .setIcon(a.getDrawable(
                            com.android.internal.R.styleable.DialogPreference_dialogTitle));
            a.recycle();

            mDialogBuilder.setSingleChoiceItems(mItems, checkedItem,
                    new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            synchronized (mMethodMap) {
                                if (mIms == null || mIms.length <= which
                                        || mSubtypeIds == null || mSubtypeIds.length <= which) {
                                    return;
                                }
                                InputMethodInfo im = mIms[which];
                                int subtypeId = mSubtypeIds[which];
                                hideInputMethodMenu();
                                if (im != null) {
                                    if ((subtypeId < 0)
                                            || (subtypeId >= im.getSubtypeCount())) {
                                        subtypeId = NOT_A_SUBTYPE_ID;
                                    }
                                    setInputMethodLocked(im.getId(), subtypeId);
                                }
                            }
                        }
                    });

            if (showSubtypes) {
                mDialogBuilder.setPositiveButton(
                        com.android.internal.R.string.configure_input_methods,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                showConfigureInputMethods();
                            }
                        });
            }
            mSwitchingDialog = mDialogBuilder.create();
            mSwitchingDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_INPUT_METHOD_DIALOG);
            mSwitchingDialog.show();
        }
    }

    void hideInputMethodMenu() {
        synchronized (mMethodMap) {
            hideInputMethodMenuLocked();
        }
    }

    void hideInputMethodMenuLocked() {
        if (DEBUG) Slog.v(TAG, "Hide switching menu");

        if (mSwitchingDialog != null) {
            mSwitchingDialog.dismiss();
            mSwitchingDialog = null;
        }

        mDialogBuilder = null;
        mItems = null;
        mIms = null;
    }

    // ----------------------------------------------------------------------

    public boolean setInputMethodEnabled(String id, boolean enabled) {
        synchronized (mMethodMap) {
            if (mContext.checkCallingOrSelfPermission(
                    android.Manifest.permission.WRITE_SECURE_SETTINGS)
                    != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException(
                        "Requires permission "
                        + android.Manifest.permission.WRITE_SECURE_SETTINGS);
            }

            long ident = Binder.clearCallingIdentity();
            try {
                return setInputMethodEnabledLocked(id, enabled);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    boolean setInputMethodEnabledLocked(String id, boolean enabled) {
        // Make sure this is a valid input method.
        InputMethodInfo imm = mMethodMap.get(id);
        if (imm == null) {
            throw new IllegalArgumentException("Unknown id: " + mCurMethodId);
        }

        List<Pair<String, ArrayList<String>>> enabledInputMethodsList = mSettings
                .getEnabledInputMethodsAndSubtypeListLocked();

        if (enabled) {
            for (Pair<String, ArrayList<String>> pair: enabledInputMethodsList) {
                if (pair.first.equals(id)) {
                    // We are enabling this input method, but it is already enabled.
                    // Nothing to do. The previous state was enabled.
                    return true;
                }
            }
            mSettings.appendAndPutEnabledInputMethodLocked(id, false);
            // Previous state was disabled.
            return false;
        } else {
            StringBuilder builder = new StringBuilder();
            if (mSettings.buildAndPutEnabledInputMethodsStrRemovingIdLocked(
                    builder, enabledInputMethodsList, id)) {
                // Disabled input method is currently selected, switch to another one.
                String selId = Settings.Secure.getString(mContext.getContentResolver(),
                        Settings.Secure.DEFAULT_INPUT_METHOD);
                if (id.equals(selId) && !chooseNewDefaultIMELocked()) {
                    Slog.i(TAG, "Can't find new IME, unsetting the current input method.");
                    resetSelectedInputMethodAndSubtypeLocked("");
                }
                // Previous state was enabled.
                return true;
            } else {
                // We are disabling the input method but it is already disabled.
                // Nothing to do.  The previous state was disabled.
                return false;
            }
        }
    }

    private boolean canAddToLastInputMethod(InputMethodSubtype subtype) {
        if (subtype == null) return true;
        return !subtype.containsExtraValueKey(SUBTYPE_EXTRAVALUE_EXCLUDE_FROM_LAST_IME);
    }

    private void saveCurrentInputMethodAndSubtypeToHistory() {
        String subtypeId = NOT_A_SUBTYPE_ID_STR;
        if (mCurrentSubtype != null) {
            subtypeId = String.valueOf(mCurrentSubtype.hashCode());
        }
        if (canAddToLastInputMethod(mCurrentSubtype)) {
            mSettings.addSubtypeToHistory(mCurMethodId, subtypeId);
        }
    }

    private void setSelectedInputMethodAndSubtypeLocked(InputMethodInfo imi, int subtypeId,
            boolean setSubtypeOnly) {
        mOldSystemSettingsVersion = getSystemSettingsVersion();
        // Update the history of InputMethod and Subtype
        saveCurrentInputMethodAndSubtypeToHistory();

        // Set Subtype here
        if (imi == null || subtypeId < 0) {
            mSettings.putSelectedSubtype(NOT_A_SUBTYPE_ID);
            mCurrentSubtype = null;
        } else {
            if (subtypeId < imi.getSubtypeCount()) {
                InputMethodSubtype subtype = imi.getSubtypeAt(subtypeId);
                mSettings.putSelectedSubtype(subtype.hashCode());
                mCurrentSubtype = subtype;
            } else {
                mSettings.putSelectedSubtype(NOT_A_SUBTYPE_ID);
                mCurrentSubtype = null;
            }
        }

        if (!setSubtypeOnly) {
            // Set InputMethod here
            mSettings.putSelectedInputMethod(imi != null ? imi.getId() : "");
        }
    }

    private void resetSelectedInputMethodAndSubtypeLocked(String newDefaultIme) {
        InputMethodInfo imi = mMethodMap.get(newDefaultIme);
        int lastSubtypeId = NOT_A_SUBTYPE_ID;
        // newDefaultIme is empty when there is no candidate for the selected IME.
        if (imi != null && !TextUtils.isEmpty(newDefaultIme)) {
            String subtypeHashCode = mSettings.getLastSubtypeForInputMethodLocked(newDefaultIme);
            if (subtypeHashCode != null) {
                try {
                    lastSubtypeId = getSubtypeIdFromHashCode(
                            imi, Integer.valueOf(subtypeHashCode));
                } catch (NumberFormatException e) {
                    Slog.w(TAG, "HashCode for subtype looks broken: " + subtypeHashCode, e);
                }
            }
        }
        setSelectedInputMethodAndSubtypeLocked(imi, lastSubtypeId, false);
    }

    private int getSelectedInputMethodSubtypeId(String id) {
        InputMethodInfo imi = mMethodMap.get(id);
        if (imi == null) {
            return NOT_A_SUBTYPE_ID;
        }
        int subtypeId;
        try {
            subtypeId = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE);
        } catch (SettingNotFoundException e) {
            return NOT_A_SUBTYPE_ID;
        }
        return getSubtypeIdFromHashCode(imi, subtypeId);
    }

    private int getSubtypeIdFromHashCode(InputMethodInfo imi, int subtypeHashCode) {
        if (imi != null) {
            final int subtypeCount = imi.getSubtypeCount();
            for (int i = 0; i < subtypeCount; ++i) {
                InputMethodSubtype ims = imi.getSubtypeAt(i);
                if (subtypeHashCode == ims.hashCode()) {
                    return i;
                }
            }
        }
        return NOT_A_SUBTYPE_ID;
    }

    private static ArrayList<InputMethodSubtype> getApplicableSubtypesLocked(
            Resources res, List<InputMethodSubtype> subtypes) {
        final String systemLocale = res.getConfiguration().locale.toString();
        if (TextUtils.isEmpty(systemLocale)) return new ArrayList<InputMethodSubtype>();
        HashMap<String, InputMethodSubtype> applicableModeAndSubtypesMap =
                new HashMap<String, InputMethodSubtype>();
        final int N = subtypes.size();
        boolean containsKeyboardSubtype = false;
        for (int i = 0; i < N; ++i) {
            InputMethodSubtype subtype = subtypes.get(i);
            final String locale = subtype.getLocale();
            final String mode = subtype.getMode();
            // When system locale starts with subtype's locale, that subtype will be applicable
            // for system locale
            // For instance, it's clearly applicable for cases like system locale = en_US and
            // subtype = en, but it is not necessarily considered applicable for cases like system
            // locale = en and subtype = en_US.
            // We just call systemLocale.startsWith(locale) in this function because there is no
            // need to find applicable subtypes aggressively unlike
            // findLastResortApplicableSubtypeLocked.
            if (systemLocale.startsWith(locale)) {
                InputMethodSubtype applicableSubtype = applicableModeAndSubtypesMap.get(mode);
                // If more applicable subtypes are contained, skip.
                if (applicableSubtype != null
                        && systemLocale.equals(applicableSubtype.getLocale())) continue;
                applicableModeAndSubtypesMap.put(mode, subtype);
                if (!containsKeyboardSubtype
                        && SUBTYPE_MODE_KEYBOARD.equalsIgnoreCase(subtype.getMode())) {
                    containsKeyboardSubtype = true;
                }
            }
        }
        ArrayList<InputMethodSubtype> applicableSubtypes = new ArrayList<InputMethodSubtype>(
                applicableModeAndSubtypesMap.values());
        if (!containsKeyboardSubtype) {
            InputMethodSubtype lastResortKeyboardSubtype = findLastResortApplicableSubtypeLocked(
                    res, subtypes, SUBTYPE_MODE_KEYBOARD, systemLocale, true);
            if (lastResortKeyboardSubtype != null) {
                applicableSubtypes.add(lastResortKeyboardSubtype);
            }
        }
        return applicableSubtypes;
    }

    /**
     * If there are no selected subtypes, tries finding the most applicable one according to the
     * given locale.
     * @param subtypes this function will search the most applicable subtype in subtypes
     * @param mode subtypes will be filtered by mode
     * @param locale subtypes will be filtered by locale
     * @param canIgnoreLocaleAsLastResort if this function can't find the most applicable subtype,
     * it will return the first subtype matched with mode
     * @return the most applicable subtypeId
     */
    private static InputMethodSubtype findLastResortApplicableSubtypeLocked(
            Resources res, List<InputMethodSubtype> subtypes, String mode, String locale,
            boolean canIgnoreLocaleAsLastResort) {
        if (subtypes == null || subtypes.size() == 0) {
            return null;
        }
        if (TextUtils.isEmpty(locale)) {
            locale = res.getConfiguration().locale.toString();
        }
        final String language = locale.substring(0, 2);
        boolean partialMatchFound = false;
        InputMethodSubtype applicableSubtype = null;
        InputMethodSubtype firstMatchedModeSubtype = null;
        final int N = subtypes.size();
        for (int i = 0; i < N; ++i) {
            InputMethodSubtype subtype = subtypes.get(i);
            final String subtypeLocale = subtype.getLocale();
            // An applicable subtype should match "mode". If mode is null, mode will be ignored,
            // and all subtypes with all modes can be candidates.
            if (mode == null || subtypes.get(i).getMode().equalsIgnoreCase(mode)) {
                if (firstMatchedModeSubtype == null) {
                    firstMatchedModeSubtype = subtype;
                }
                if (locale.equals(subtypeLocale)) {
                    // Exact match (e.g. system locale is "en_US" and subtype locale is "en_US")
                    applicableSubtype = subtype;
                    break;
                } else if (!partialMatchFound && subtypeLocale.startsWith(language)) {
                    // Partial match (e.g. system locale is "en_US" and subtype locale is "en")
                    applicableSubtype = subtype;
                    partialMatchFound = true;
                }
            }
        }

        if (applicableSubtype == null && canIgnoreLocaleAsLastResort) {
            return firstMatchedModeSubtype;
        }

        // The first subtype applicable to the system locale will be defined as the most applicable
        // subtype.
        if (DEBUG) {
            if (applicableSubtype != null) {
                Slog.d(TAG, "Applicable InputMethodSubtype was found: "
                        + applicableSubtype.getMode() + "," + applicableSubtype.getLocale());
            }
        }
        return applicableSubtype;
    }

    // If there are no selected shortcuts, tries finding the most applicable ones.
    private Pair<InputMethodInfo, InputMethodSubtype>
            findLastResortApplicableShortcutInputMethodAndSubtypeLocked(String mode) {
        List<InputMethodInfo> imis = mSettings.getEnabledInputMethodListLocked();
        InputMethodInfo mostApplicableIMI = null;
        InputMethodSubtype mostApplicableSubtype = null;
        boolean foundInSystemIME = false;

        // Search applicable subtype for each InputMethodInfo
        for (InputMethodInfo imi: imis) {
            final String imiId = imi.getId();
            if (foundInSystemIME && !imiId.equals(mCurMethodId)) {
                continue;
            }
            InputMethodSubtype subtype = null;
            final List<InputMethodSubtype> enabledSubtypes =
                    getEnabledInputMethodSubtypeList(imi, true);
            // 1. Search by the current subtype's locale from enabledSubtypes.
            if (mCurrentSubtype != null) {
                subtype = findLastResortApplicableSubtypeLocked(
                        mRes, enabledSubtypes, mode, mCurrentSubtype.getLocale(), false);
            }
            // 2. Search by the system locale from enabledSubtypes.
            // 3. Search the first enabled subtype matched with mode from enabledSubtypes.
            if (subtype == null) {
                subtype = findLastResortApplicableSubtypeLocked(
                        mRes, enabledSubtypes, mode, null, true);
            }
            // 4. Search by the current subtype's locale from all subtypes.
            if (subtype == null && mCurrentSubtype != null) {
                subtype = findLastResortApplicableSubtypeLocked(
                        mRes, getSubtypes(imi), mode, mCurrentSubtype.getLocale(), false);
            }
            // 5. Search by the system locale from all subtypes.
            // 6. Search the first enabled subtype matched with mode from all subtypes.
            if (subtype == null) {
                subtype = findLastResortApplicableSubtypeLocked(
                        mRes, getSubtypes(imi), mode, null, true);
            }
            if (subtype != null) {
                if (imiId.equals(mCurMethodId)) {
                    // The current input method is the most applicable IME.
                    mostApplicableIMI = imi;
                    mostApplicableSubtype = subtype;
                    break;
                } else if (!foundInSystemIME) {
                    // The system input method is 2nd applicable IME.
                    mostApplicableIMI = imi;
                    mostApplicableSubtype = subtype;
                    if ((imi.getServiceInfo().applicationInfo.flags
                            & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        foundInSystemIME = true;
                    }
                }
            }
        }
        if (DEBUG) {
            if (mostApplicableIMI != null) {
                Slog.w(TAG, "Most applicable shortcut input method was:"
                        + mostApplicableIMI.getId());
                if (mostApplicableSubtype != null) {
                    Slog.w(TAG, "Most applicable shortcut input method subtype was:"
                            + "," + mostApplicableSubtype.getMode() + ","
                            + mostApplicableSubtype.getLocale());
                }
            }
        }
        if (mostApplicableIMI != null) {
            return new Pair<InputMethodInfo, InputMethodSubtype> (mostApplicableIMI,
                    mostApplicableSubtype);
        } else {
            return null;
        }
    }

    private static long getSystemSettingsVersion() {
        return SystemProperties.getLong(Settings.Secure.SYS_PROP_SETTING_VERSION, 0);
    }

    /**
     * @return Return the current subtype of this input method.
     */
    public InputMethodSubtype getCurrentInputMethodSubtype() {
        boolean subtypeIsSelected = false;
        try {
            subtypeIsSelected = Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE) != NOT_A_SUBTYPE_ID;
        } catch (SettingNotFoundException e) {
        }
        synchronized (mMethodMap) {
            if (!subtypeIsSelected || mCurrentSubtype == null) {
                String lastInputMethodId = Settings.Secure.getString(
                        mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                int subtypeId = getSelectedInputMethodSubtypeId(lastInputMethodId);
                if (subtypeId == NOT_A_SUBTYPE_ID) {
                    InputMethodInfo imi = mMethodMap.get(lastInputMethodId);
                    if (imi != null) {
                        // If there are no selected subtypes, the framework will try to find
                        // the most applicable subtype from explicitly or implicitly enabled
                        // subtypes.
                        List<InputMethodSubtype> explicitlyOrImplicitlyEnabledSubtypes =
                                getEnabledInputMethodSubtypeList(imi, true);
                        // If there is only one explicitly or implicitly enabled subtype,
                        // just returns it.
                        if (explicitlyOrImplicitlyEnabledSubtypes.size() == 1) {
                            mCurrentSubtype = explicitlyOrImplicitlyEnabledSubtypes.get(0);
                        } else if (explicitlyOrImplicitlyEnabledSubtypes.size() > 1) {
                            mCurrentSubtype = findLastResortApplicableSubtypeLocked(
                                    mRes, explicitlyOrImplicitlyEnabledSubtypes,
                                    SUBTYPE_MODE_KEYBOARD, null, true);
                            if (mCurrentSubtype == null) {
                                mCurrentSubtype = findLastResortApplicableSubtypeLocked(
                                        mRes, explicitlyOrImplicitlyEnabledSubtypes, null, null,
                                        true);
                            }
                        }
                    }
                } else {
                    mCurrentSubtype =
                            getSubtypes(mMethodMap.get(lastInputMethodId)).get(subtypeId);
                }
            }
            return mCurrentSubtype;
        }
    }

    private void addShortcutInputMethodAndSubtypes(InputMethodInfo imi,
            InputMethodSubtype subtype) {
        if (mShortcutInputMethodsAndSubtypes.containsKey(imi)) {
            mShortcutInputMethodsAndSubtypes.get(imi).add(subtype);
        } else {
            ArrayList<InputMethodSubtype> subtypes = new ArrayList<InputMethodSubtype>();
            subtypes.add(subtype);
            mShortcutInputMethodsAndSubtypes.put(imi, subtypes);
        }
    }

    // TODO: We should change the return type from List to List<Parcelable>
    public List getShortcutInputMethodsAndSubtypes() {
        synchronized (mMethodMap) {
            ArrayList<Object> ret = new ArrayList<Object>();
            if (mShortcutInputMethodsAndSubtypes.size() == 0) {
                // If there are no selected shortcut subtypes, the framework will try to find
                // the most applicable subtype from all subtypes whose mode is
                // SUBTYPE_MODE_VOICE. This is an exceptional case, so we will hardcode the mode.
                Pair<InputMethodInfo, InputMethodSubtype> info =
                    findLastResortApplicableShortcutInputMethodAndSubtypeLocked(
                            SUBTYPE_MODE_VOICE);
                if (info != null) {
                    ret.add(info.first);
                    ret.add(info.second);
                }
                return ret;
            }
            for (InputMethodInfo imi: mShortcutInputMethodsAndSubtypes.keySet()) {
                ret.add(imi);
                for (InputMethodSubtype subtype: mShortcutInputMethodsAndSubtypes.get(imi)) {
                    ret.add(subtype);
                }
            }
            return ret;
        }
    }

    public boolean setCurrentInputMethodSubtype(InputMethodSubtype subtype) {
        synchronized (mMethodMap) {
            if (subtype != null && mCurMethodId != null) {
                InputMethodInfo imi = mMethodMap.get(mCurMethodId);
                int subtypeId = getSubtypeIdFromHashCode(imi, subtype.hashCode());
                if (subtypeId != NOT_A_SUBTYPE_ID) {
                    setInputMethodLocked(mCurMethodId, subtypeId);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Utility class for putting and getting settings for InputMethod
     * TODO: Move all putters and getters of settings to this class.
     */
    private static class InputMethodSettings {
        // The string for enabled input method is saved as follows:
        // example: ("ime0;subtype0;subtype1;subtype2:ime1:ime2;subtype0")
        private static final char INPUT_METHOD_SEPARATER = ':';
        private static final char INPUT_METHOD_SUBTYPE_SEPARATER = ';';
        private final TextUtils.SimpleStringSplitter mInputMethodSplitter =
                new TextUtils.SimpleStringSplitter(INPUT_METHOD_SEPARATER);

        private final TextUtils.SimpleStringSplitter mSubtypeSplitter =
                new TextUtils.SimpleStringSplitter(INPUT_METHOD_SUBTYPE_SEPARATER);

        private final Resources mRes;
        private final ContentResolver mResolver;
        private final HashMap<String, InputMethodInfo> mMethodMap;
        private final ArrayList<InputMethodInfo> mMethodList;

        private String mEnabledInputMethodsStrCache;

        private static void buildEnabledInputMethodsSettingString(
                StringBuilder builder, Pair<String, ArrayList<String>> pair) {
            String id = pair.first;
            ArrayList<String> subtypes = pair.second;
            builder.append(id);
            // Inputmethod and subtypes are saved in the settings as follows:
            // ime0;subtype0;subtype1:ime1;subtype0:ime2:ime3;subtype0;subtype1
            for (String subtypeId: subtypes) {
                builder.append(INPUT_METHOD_SUBTYPE_SEPARATER).append(subtypeId);
            }
        }

        public InputMethodSettings(
                Resources res, ContentResolver resolver,
                HashMap<String, InputMethodInfo> methodMap, ArrayList<InputMethodInfo> methodList) {
            mRes = res;
            mResolver = resolver;
            mMethodMap = methodMap;
            mMethodList = methodList;
        }

        public List<InputMethodInfo> getEnabledInputMethodListLocked() {
            return createEnabledInputMethodListLocked(
                    getEnabledInputMethodsAndSubtypeListLocked());
        }

        public List<Pair<InputMethodInfo, ArrayList<String>>>
                getEnabledInputMethodAndSubtypeHashCodeListLocked() {
            return createEnabledInputMethodAndSubtypeHashCodeListLocked(
                    getEnabledInputMethodsAndSubtypeListLocked());
        }

        public List<InputMethodSubtype> getEnabledInputMethodSubtypeListLocked(
                InputMethodInfo imi) {
            List<Pair<String, ArrayList<String>>> imsList =
                    getEnabledInputMethodsAndSubtypeListLocked();
            ArrayList<InputMethodSubtype> enabledSubtypes =
                    new ArrayList<InputMethodSubtype>();
            if (imi != null) {
                for (Pair<String, ArrayList<String>> imsPair : imsList) {
                    InputMethodInfo info = mMethodMap.get(imsPair.first);
                    if (info != null && info.getId().equals(imi.getId())) {
                        final int subtypeCount = info.getSubtypeCount();
                        for (int i = 0; i < subtypeCount; ++i) {
                            InputMethodSubtype ims = info.getSubtypeAt(i);
                            for (String s: imsPair.second) {
                                if (String.valueOf(ims.hashCode()).equals(s)) {
                                    enabledSubtypes.add(ims);
                                }
                            }
                        }
                        break;
                    }
                }
            }
            return enabledSubtypes;
        }

        // At the initial boot, the settings for input methods are not set,
        // so we need to enable IME in that case.
        public void enableAllIMEsIfThereIsNoEnabledIME() {
            if (TextUtils.isEmpty(getEnabledInputMethodsStr())) {
                StringBuilder sb = new StringBuilder();
                final int N = mMethodList.size();
                for (int i = 0; i < N; i++) {
                    InputMethodInfo imi = mMethodList.get(i);
                    Slog.i(TAG, "Adding: " + imi.getId());
                    if (i > 0) sb.append(':');
                    sb.append(imi.getId());
                }
                putEnabledInputMethodsStr(sb.toString());
            }
        }

        private List<Pair<String, ArrayList<String>>> getEnabledInputMethodsAndSubtypeListLocked() {
            ArrayList<Pair<String, ArrayList<String>>> imsList
                    = new ArrayList<Pair<String, ArrayList<String>>>();
            final String enabledInputMethodsStr = getEnabledInputMethodsStr();
            if (TextUtils.isEmpty(enabledInputMethodsStr)) {
                return imsList;
            }
            mInputMethodSplitter.setString(enabledInputMethodsStr);
            while (mInputMethodSplitter.hasNext()) {
                String nextImsStr = mInputMethodSplitter.next();
                mSubtypeSplitter.setString(nextImsStr);
                if (mSubtypeSplitter.hasNext()) {
                    ArrayList<String> subtypeHashes = new ArrayList<String>();
                    // The first element is ime id.
                    String imeId = mSubtypeSplitter.next();
                    while (mSubtypeSplitter.hasNext()) {
                        subtypeHashes.add(mSubtypeSplitter.next());
                    }
                    imsList.add(new Pair<String, ArrayList<String>>(imeId, subtypeHashes));
                }
            }
            return imsList;
        }

        public void appendAndPutEnabledInputMethodLocked(String id, boolean reloadInputMethodStr) {
            if (reloadInputMethodStr) {
                getEnabledInputMethodsStr();
            }
            if (TextUtils.isEmpty(mEnabledInputMethodsStrCache)) {
                // Add in the newly enabled input method.
                putEnabledInputMethodsStr(id);
            } else {
                putEnabledInputMethodsStr(
                        mEnabledInputMethodsStrCache + INPUT_METHOD_SEPARATER + id);
            }
        }

        /**
         * Build and put a string of EnabledInputMethods with removing specified Id.
         * @return the specified id was removed or not.
         */
        public boolean buildAndPutEnabledInputMethodsStrRemovingIdLocked(
                StringBuilder builder, List<Pair<String, ArrayList<String>>> imsList, String id) {
            boolean isRemoved = false;
            boolean needsAppendSeparator = false;
            for (Pair<String, ArrayList<String>> ims: imsList) {
                String curId = ims.first;
                if (curId.equals(id)) {
                    // We are disabling this input method, and it is
                    // currently enabled.  Skip it to remove from the
                    // new list.
                    isRemoved = true;
                } else {
                    if (needsAppendSeparator) {
                        builder.append(INPUT_METHOD_SEPARATER);
                    } else {
                        needsAppendSeparator = true;
                    }
                    buildEnabledInputMethodsSettingString(builder, ims);
                }
            }
            if (isRemoved) {
                // Update the setting with the new list of input methods.
                putEnabledInputMethodsStr(builder.toString());
            }
            return isRemoved;
        }

        private List<InputMethodInfo> createEnabledInputMethodListLocked(
                List<Pair<String, ArrayList<String>>> imsList) {
            final ArrayList<InputMethodInfo> res = new ArrayList<InputMethodInfo>();
            for (Pair<String, ArrayList<String>> ims: imsList) {
                InputMethodInfo info = mMethodMap.get(ims.first);
                if (info != null) {
                    res.add(info);
                }
            }
            return res;
        }

        private List<Pair<InputMethodInfo, ArrayList<String>>>
                createEnabledInputMethodAndSubtypeHashCodeListLocked(
                        List<Pair<String, ArrayList<String>>> imsList) {
            final ArrayList<Pair<InputMethodInfo, ArrayList<String>>> res
                    = new ArrayList<Pair<InputMethodInfo, ArrayList<String>>>();
            for (Pair<String, ArrayList<String>> ims : imsList) {
                InputMethodInfo info = mMethodMap.get(ims.first);
                if (info != null) {
                    res.add(new Pair<InputMethodInfo, ArrayList<String>>(info, ims.second));
                }
            }
            return res;
        }

        private void putEnabledInputMethodsStr(String str) {
            Settings.Secure.putString(mResolver, Settings.Secure.ENABLED_INPUT_METHODS, str);
            mEnabledInputMethodsStrCache = str;
        }

        private String getEnabledInputMethodsStr() {
            mEnabledInputMethodsStrCache = Settings.Secure.getString(
                    mResolver, Settings.Secure.ENABLED_INPUT_METHODS);
            if (DEBUG) {
                Slog.d(TAG, "getEnabledInputMethodsStr: " + mEnabledInputMethodsStrCache);
            }
            return mEnabledInputMethodsStrCache;
        }

        private void saveSubtypeHistory(
                List<Pair<String, String>> savedImes, String newImeId, String newSubtypeId) {
            StringBuilder builder = new StringBuilder();
            boolean isImeAdded = false;
            if (!TextUtils.isEmpty(newImeId) && !TextUtils.isEmpty(newSubtypeId)) {
                builder.append(newImeId).append(INPUT_METHOD_SUBTYPE_SEPARATER).append(
                        newSubtypeId);
                isImeAdded = true;
            }
            for (Pair<String, String> ime: savedImes) {
                String imeId = ime.first;
                String subtypeId = ime.second;
                if (TextUtils.isEmpty(subtypeId)) {
                    subtypeId = NOT_A_SUBTYPE_ID_STR;
                }
                if (isImeAdded) {
                    builder.append(INPUT_METHOD_SEPARATER);
                } else {
                    isImeAdded = true;
                }
                builder.append(imeId).append(INPUT_METHOD_SUBTYPE_SEPARATER).append(
                        subtypeId);
            }
            // Remove the last INPUT_METHOD_SEPARATER
            putSubtypeHistoryStr(builder.toString());
        }

        public void addSubtypeToHistory(String imeId, String subtypeId) {
            List<Pair<String, String>> subtypeHistory = loadInputMethodAndSubtypeHistoryLocked();
            for (Pair<String, String> ime: subtypeHistory) {
                if (ime.first.equals(imeId)) {
                    if (DEBUG) {
                        Slog.v(TAG, "Subtype found in the history: " + imeId + ", "
                                + ime.second);
                    }
                    // We should break here
                    subtypeHistory.remove(ime);
                    break;
                }
            }
            if (DEBUG) {
                Slog.v(TAG, "Add subtype to the history: " + imeId + ", " + subtypeId);
            }
            saveSubtypeHistory(subtypeHistory, imeId, subtypeId);
        }

        private void putSubtypeHistoryStr(String str) {
            if (DEBUG) {
                Slog.d(TAG, "putSubtypeHistoryStr: " + str);
            }
            Settings.Secure.putString(
                    mResolver, Settings.Secure.INPUT_METHODS_SUBTYPE_HISTORY, str);
        }

        public Pair<String, String> getLastInputMethodAndSubtypeLocked() {
            // Gets the first one from the history
            return getLastSubtypeForInputMethodLockedInternal(null);
        }

        public String getLastSubtypeForInputMethodLocked(String imeId) {
            Pair<String, String> ime = getLastSubtypeForInputMethodLockedInternal(imeId);
            if (ime != null) {
                return ime.second;
            } else {
                return null;
            }
        }

        private Pair<String, String> getLastSubtypeForInputMethodLockedInternal(String imeId) {
            List<Pair<String, ArrayList<String>>> enabledImes =
                    getEnabledInputMethodsAndSubtypeListLocked();
            List<Pair<String, String>> subtypeHistory = loadInputMethodAndSubtypeHistoryLocked();
            for (Pair<String, String> imeAndSubtype: subtypeHistory) {
                final String imeInTheHistory = imeAndSubtype.first;
                // If imeId is empty, returns the first IME and subtype in the history
                if (TextUtils.isEmpty(imeId) || imeInTheHistory.equals(imeId)) {
                    final String subtypeInTheHistory = imeAndSubtype.second;
                    final String subtypeHashCode =
                            getEnabledSubtypeHashCodeForInputMethodAndSubtypeLocked(
                                    enabledImes, imeInTheHistory, subtypeInTheHistory);
                    if (!TextUtils.isEmpty(subtypeHashCode)) {
                        if (DEBUG) {
                            Slog.d(TAG, "Enabled subtype found in the history: " + subtypeHashCode);
                        }
                        return new Pair<String, String>(imeInTheHistory, subtypeHashCode);
                    }
                }
            }
            if (DEBUG) {
                Slog.d(TAG, "No enabled IME found in the history");
            }
            return null;
        }

        private String getEnabledSubtypeHashCodeForInputMethodAndSubtypeLocked(List<Pair<String,
                ArrayList<String>>> enabledImes, String imeId, String subtypeHashCode) {
            for (Pair<String, ArrayList<String>> enabledIme: enabledImes) {
                if (enabledIme.first.equals(imeId)) {
                    final ArrayList<String> explicitlyEnabledSubtypes = enabledIme.second;
                    if (explicitlyEnabledSubtypes.size() == 0) {
                        // If there are no explicitly enabled subtypes, applicable subtypes are
                        // enabled implicitly.
                        InputMethodInfo ime = mMethodMap.get(imeId);
                        // If IME is enabled and no subtypes are enabled, applicable subtypes
                        // are enabled implicitly, so needs to treat them to be enabled.
                        if (ime != null && ime.getSubtypeCount() > 0) {
                            List<InputMethodSubtype> implicitlySelectedSubtypes =
                                    getApplicableSubtypesLocked(mRes, getSubtypes(ime));
                            if (implicitlySelectedSubtypes != null) {
                                final int N = implicitlySelectedSubtypes.size();
                                for (int i = 0; i < N; ++i) {
                                    final InputMethodSubtype st = implicitlySelectedSubtypes.get(i);
                                    if (String.valueOf(st.hashCode()).equals(subtypeHashCode)) {
                                        return subtypeHashCode;
                                    }
                                }
                            }
                        }
                    } else {
                        for (String s: explicitlyEnabledSubtypes) {
                            if (s.equals(subtypeHashCode)) {
                                // If both imeId and subtypeId are enabled, return subtypeId.
                                return s;
                            }
                        }
                    }
                    // If imeId was enabled but subtypeId was disabled.
                    return NOT_A_SUBTYPE_ID_STR;
                }
            }
            // If both imeId and subtypeId are disabled, return null
            return null;
        }

        private List<Pair<String, String>> loadInputMethodAndSubtypeHistoryLocked() {
            ArrayList<Pair<String, String>> imsList = new ArrayList<Pair<String, String>>();
            final String subtypeHistoryStr = getSubtypeHistoryStr();
            if (TextUtils.isEmpty(subtypeHistoryStr)) {
                return imsList;
            }
            mInputMethodSplitter.setString(subtypeHistoryStr);
            while (mInputMethodSplitter.hasNext()) {
                String nextImsStr = mInputMethodSplitter.next();
                mSubtypeSplitter.setString(nextImsStr);
                if (mSubtypeSplitter.hasNext()) {
                    String subtypeId = NOT_A_SUBTYPE_ID_STR;
                    // The first element is ime id.
                    String imeId = mSubtypeSplitter.next();
                    while (mSubtypeSplitter.hasNext()) {
                        subtypeId = mSubtypeSplitter.next();
                        break;
                    }
                    imsList.add(new Pair<String, String>(imeId, subtypeId));
                }
            }
            return imsList;
        }

        private String getSubtypeHistoryStr() {
            if (DEBUG) {
                Slog.d(TAG, "getSubtypeHistoryStr: " + Settings.Secure.getString(
                        mResolver, Settings.Secure.INPUT_METHODS_SUBTYPE_HISTORY));
            }
            return Settings.Secure.getString(
                    mResolver, Settings.Secure.INPUT_METHODS_SUBTYPE_HISTORY);
        }

        public void putSelectedInputMethod(String imeId) {
            Settings.Secure.putString(mResolver, Settings.Secure.DEFAULT_INPUT_METHOD, imeId);
        }

        public void putSelectedSubtype(int subtypeId) {
            Settings.Secure.putInt(
                    mResolver, Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE, subtypeId);
        }
    }

    // ----------------------------------------------------------------------

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {

            pw.println("Permission Denial: can't dump InputMethodManager from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }

        IInputMethod method;
        ClientState client;

        final Printer p = new PrintWriterPrinter(pw);

        synchronized (mMethodMap) {
            p.println("Current Input Method Manager state:");
            int N = mMethodList.size();
            p.println("  Input Methods:");
            for (int i=0; i<N; i++) {
                InputMethodInfo info = mMethodList.get(i);
                p.println("  InputMethod #" + i + ":");
                info.dump(p, "    ");
            }
            p.println("  Clients:");
            for (ClientState ci : mClients.values()) {
                p.println("  Client " + ci + ":");
                p.println("    client=" + ci.client);
                p.println("    inputContext=" + ci.inputContext);
                p.println("    sessionRequested=" + ci.sessionRequested);
                p.println("    curSession=" + ci.curSession);
            }
            p.println("  mCurMethodId=" + mCurMethodId);
            client = mCurClient;
            p.println("  mCurClient=" + client + " mCurSeq=" + mCurSeq);
            p.println("  mCurFocusedWindow=" + mCurFocusedWindow);
            p.println("  mCurId=" + mCurId + " mHaveConnect=" + mHaveConnection
                    + " mBoundToMethod=" + mBoundToMethod);
            p.println("  mCurToken=" + mCurToken);
            p.println("  mCurIntent=" + mCurIntent);
            method = mCurMethod;
            p.println("  mCurMethod=" + mCurMethod);
            p.println("  mEnabledSession=" + mEnabledSession);
            p.println("  mShowRequested=" + mShowRequested
                    + " mShowExplicitlyRequested=" + mShowExplicitlyRequested
                    + " mShowForced=" + mShowForced
                    + " mInputShown=" + mInputShown);
            p.println("  mSystemReady=" + mSystemReady + " mScreenOn=" + mScreenOn);
        }

        p.println(" ");
        if (client != null) {
            pw.flush();
            try {
                client.client.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                p.println("Input method client dead: " + e);
            }
        } else {
            p.println("No input method client.");
        }

        p.println(" ");
        if (method != null) {
            pw.flush();
            try {
                method.asBinder().dump(fd, args);
            } catch (RemoteException e) {
                p.println("Input method service dead: " + e);
            }
        } else {
            p.println("No input method service.");
        }
    }
}