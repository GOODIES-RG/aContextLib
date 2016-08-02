/*
 * Copyright 2016 Middlesex University
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

package uk.ac.mdx.cs.ie.acontextlib.hardware;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import java.util.HashMap;
import java.util.Map;

import uk.ac.mdx.cs.ie.acontextlib.BroadcastContext;

/**
 * Handles telephone related contexts including:
 * The state of the connection (disconnected, connecting, connected etc)
 * Whether the device is currently roaming
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class TelephonyContext extends BroadcastContext {

    private boolean mRoaming = false;
    private int mConnectionState = -1;
    public TelephonyManager mPhoneManager;
    public static final String RECEIVER_TELEPHONY_ROAMING = "sensor.telephony_roaming";
    public static final String RECEIVER_TELEPHONY_CONSTATE = "sensor.telephone_connectionstate";

    public TelephonyContext(Context c) {
        super(c, ConnectivityManager.CONNECTIVITY_ACTION, "TelephonyContext");
        mPhoneManager = (TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public boolean start() {
        Map<String, String> contextValues = new HashMap<String, String>();
        contextValues.put(RECEIVER_TELEPHONY_ROAMING, String.valueOf(mPhoneManager.isNetworkRoaming()));
        checkConnectionState(contextValues);
        sendToContextReceivers(contextValues);
        return super.start();
    }

    @Override
    protected void checkContext(Bundle data) {
        Map<String, String> contextValues = new HashMap<String, String>();
        checkRoaming(contextValues);
        checkConnectionState(contextValues);
        sendToContextReceivers(contextValues);
    }

    private void checkRoaming(Map<String, String> values) {
        if (mRoaming != mPhoneManager.isNetworkRoaming()) {
            mRoaming = !mRoaming;
            values.put(RECEIVER_TELEPHONY_ROAMING, String.valueOf(mRoaming));
        }
    }

    private void checkConnectionState(Map<String, String> values) {
        int v = mPhoneManager.getDataState();
        if (v != mConnectionState) {
            mConnectionState = v;
            values.put(RECEIVER_TELEPHONY_CONSTATE, getConnectionValue(mConnectionState));
        }
    }

    private static String getConnectionValue(int connectionState) {

        String state;

        switch (connectionState) {
            case TelephonyManager.DATA_CONNECTING:
                state = "CONNECTING";
                break;
            case TelephonyManager.DATA_CONNECTED:
                state = "CONNECTED";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                state = "SUSPENDED";
                break;
            default:
                state = "DISCONNECTED";
                break;
        }

        return state;
    }

    public int getConnectionState() {
        return mConnectionState;
    }

    public boolean isRoaming() {
        return mRoaming;
    }
}
