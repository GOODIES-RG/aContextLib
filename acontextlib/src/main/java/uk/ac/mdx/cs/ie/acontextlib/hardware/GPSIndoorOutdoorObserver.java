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
import android.content.res.Configuration;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.os.Looper;

import uk.ac.mdx.cs.ie.acontextlib.LocationObserver;

/**
 * Calculates if the device is outside comparing the SNR for all visible GPS Satellites.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class GPSIndoorOutdoorObserver extends LocationObserver {

    private final GpsListener gpsListener = new GpsListener();
    private GpsStatus gpsStatus;
    private boolean mCurrentValue = false;
    private int signalNeeded;
    private boolean mFirstTime = true;
    public static final String RECEIVER_INDOOR_OUTDOOR = "sensor.gps_indoor_outdoor";

    public GPSIndoorOutdoorObserver(Context c) {
        super(c, 3000, 0, "GPSIndoorOutdoorObserver");
        gpsStatus = mLocationManager.getGpsStatus(null);
        if (isTablet(c)) {
            signalNeeded = 30;
        } else {
            signalNeeded = 26;
        }
    }

    public void checkContext(Object data) {
        // TODO Auto-generated method stub
    }

    class GpsListener implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
            getSatData();
        }
    }

    public void getSatData() {
        Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
        float snr = 0;
        int usedSnrs = 0;
        for (GpsSatellite sat : sats) {
            float aSnr = sat.getSnr();

            if (aSnr > signalNeeded) {
                snr += aSnr;
                usedSnrs++;
            }
        }

        snr = snr / usedSnrs;

        //Need to make this more accurate
        if ((snr > signalNeeded) && (usedSnrs > 4)) {
            if (!mCurrentValue) {
                sendToContextReceivers(RECEIVER_INDOOR_OUTDOOR, false);
                mCurrentValue = !mCurrentValue;
            }

        } else {
            if (mCurrentValue) {
                sendToContextReceivers(RECEIVER_INDOOR_OUTDOOR, true);
                mCurrentValue = !mCurrentValue;
            }

            if (mFirstTime) {
                sendToContextReceivers(RECEIVER_INDOOR_OUTDOOR, true);
                mFirstTime = false;
            }
        }

        gpsStatus = mLocationManager.getGpsStatus(gpsStatus);
    }

    @Override
    public boolean start() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                GPSIndoorOutdoorObserver.super.start();
                mLocationManager.addGpsStatusListener(gpsListener);
                Looper.loop();
            }
        }).start();

        return true;
    }

    @Override
    public boolean stop() {
        mLocationManager.removeGpsStatusListener(gpsListener);
        super.stop();
        return true;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
