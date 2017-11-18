/*
 * Copyright (C) 2010 The Android Open Source Project
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

package android.net.wifi;

import com.android.internal.util.AsyncChannel;
import com.android.internal.util.HierarchicalState;
import com.android.internal.util.HierarchicalStateMachine;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiStateMachine.StateChangeResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

/**
 * Manages a WPS connection.
 *
 * WPS consists as a series of EAP exchange triggered
 * by a user action that leads to a successful connection
 * after automatic creation of configuration in the
 * supplicant. We currently support the following methods
 * of WPS setup
 * 1. Pin method: Pin can be either obtained from the device
 *    or from the access point to connect to.
 * 2. Push button method: This involves pushing a button on
 *    the access point and the device
 *
 * After a successful WPS setup, the state machine
 * reloads the configuration and updates the IP and proxy
 * settings, if any.
 */
class WpsStateMachine extends HierarchicalStateMachine {

    private static final String TAG = "WpsStateMachine";
    private static final boolean DBG = false;

    private WifiStateMachine mWifiStateMachine;

    private WpsConfiguration mWpsConfig;

    private Context mContext;
    AsyncChannel mReplyChannel = new AsyncChannel();

    private HierarchicalState mDefaultState = new DefaultState();
    private HierarchicalState mInactiveState = new InactiveState();
    private HierarchicalState mActiveState = new ActiveState();

    public WpsStateMachine(Context context, WifiStateMachine wsm, Handler target) {
        super(TAG, target.getLooper());

        mContext = context;
        mWifiStateMachine = wsm;
        addState(mDefaultState);
            addState(mInactiveState, mDefaultState);
            addState(mActiveState, mDefaultState);

        setInitialState(mInactiveState);

        //start the state machine
        start();
    }


    /********************************************************
     * HSM states
     *******************************************************/

    class DefaultState extends HierarchicalState {
        @Override
         public void enter() {
             if (DBG) Log.d(TAG, getName() + "\n");
         }
        @Override
        public boolean processMessage(Message message) {
            if (DBG) Log.d(TAG, getName() + message.toString() + "\n");
            WpsConfiguration wpsConfig;
            switch (message.what) {
                case WifiStateMachine.CMD_START_WPS:
                    mWpsConfig = (WpsConfiguration) message.obj;
                    boolean success = false;
                    switch (mWpsConfig.setup) {
                        case PBC:
                            success = WifiConfigStore.startWpsPbc(mWpsConfig);
                            break;
                        case PIN_FROM_ACCESS_POINT:
                            success = WifiConfigStore.startWpsWithPinFromAccessPoint(mWpsConfig);
                            break;
                        case PIN_FROM_DEVICE:
                            String pin = WifiConfigStore.startWpsWithPinFromDevice(mWpsConfig);
                            success = (pin != null);
                            mReplyChannel.replyToMessage(message, message.what, pin);
                            break;
                        default:
                            Log.e(TAG, "Invalid setup for WPS");
                            break;
                    }
                    if (success) {
                        transitionTo(mActiveState);
                    } else {
                        Log.e(TAG, "Failed to start WPS with config " + mWpsConfig.toString());
                    }
                    break;
                default:
                    Log.e(TAG, "Failed to handle " + message);
                    break;
            }
            return HANDLED;
        }
    }

    class ActiveState extends HierarchicalState {
        @Override
         public void enter() {
             if (DBG) Log.d(TAG, getName() + "\n");
         }

        @Override
        public boolean processMessage(Message message) {
            boolean retValue = HANDLED;
            if (DBG) Log.d(TAG, getName() + message.toString() + "\n");
            switch (message.what) {
                case WifiStateMachine.SUPPLICANT_STATE_CHANGE_EVENT:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    SupplicantState supState = (SupplicantState) stateChangeResult.state;
                    switch (supState) {
                        case COMPLETED:
                            /* During WPS setup, all other networks are disabled. After
                             * a successful connect a new config is created in the supplicant.
                             *
                             * We need to enable all networks after a successful connection
                             * and the configuration list needs to be reloaded from the supplicant.
                             */
                            Log.d(TAG, "WPS set up successful");
                            WifiConfigStore.enableAllNetworks();
                            WifiConfigStore.loadConfiguredNetworks();
                            WifiConfigStore.updateIpAndProxyFromWpsConfig(
                                    stateChangeResult.networkId, mWpsConfig);
                            mWifiStateMachine.sendMessage(WifiStateMachine.WPS_COMPLETED_EVENT);
                            transitionTo(mInactiveState);
                            break;
                        case INACTIVE:
                            /* A failed WPS connection */
                            Log.d(TAG, "WPS set up failed, enabling other networks");
                            WifiConfigStore.enableAllNetworks();
                            mWifiStateMachine.sendMessage(WifiStateMachine.WPS_COMPLETED_EVENT);
                            transitionTo(mInactiveState);
                            break;
                        default:
                            if (DBG) Log.d(TAG, "Ignoring supplicant state " + supState.name());
                            break;
                    }
                    break;
                case WifiStateMachine.CMD_START_WPS:
                    deferMessage(message);
                    break;
                default:
                    retValue = NOT_HANDLED;
            }
            return retValue;
        }
    }

    class InactiveState extends HierarchicalState {
        @Override
        public void enter() {
            if (DBG) Log.d(TAG, getName() + "\n");
        }

        @Override
        public boolean processMessage(Message message) {
            boolean retValue = HANDLED;
            if (DBG) Log.d(TAG, getName() + message.toString() + "\n");
            switch (message.what) {
                //Ignore supplicant state changes
                case WifiStateMachine.SUPPLICANT_STATE_CHANGE_EVENT:
                    break;
                default:
                    retValue = NOT_HANDLED;
            }
            return retValue;
        }
    }

}