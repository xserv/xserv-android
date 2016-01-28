/***
 XservBase

 Copyright (C) 2015 Giovanni Amati

 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/

package com.mi.xserv;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class XservBase {
    final protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected OnXservEventListener mDelegate;

    public XservBase() {
        mDelegate = null;
    }

    public void setOnEventListener(OnXservEventListener onEventListener) {
        mDelegate = onEventListener;
    }

    protected String getDeviceID() {
        String deviceID = null;
        if (mDelegate != null) {
            try {
                deviceID = Settings.Secure.getString(((Context) mDelegate).getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (deviceID == null) {
            deviceID = UUID.randomUUID().toString();
        }
        return deviceID;
    }

    protected int getTimeZoneOffset() {
        // GMT es. italia +1
        TimeZone timezone = TimeZone.getDefault();
        int seconds = timezone.getOffset(Calendar.ZONE_OFFSET) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        return (int) hours;
    }

    protected int getTimeZoneDst() {
        // Daylight savings
        Date today = new Date();
        TimeZone timezone = TimeZone.getDefault();
        boolean isDST = timezone.inDaylightTime(today);
        return isDST ? 1 : 0;
    }

    protected void onOpenConnection() {
        if (mDelegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mDelegate.OnOpenConnection();
                }
            });
        }
    }

    protected void onCloseConnection(final Exception e) {
        if (mDelegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mDelegate.OnCloseConnection(e);
                }
            });
        }
    }

    protected void onErrorConnection(final Exception e) {
        if (mDelegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mDelegate.OnErrorConnection(e);
                }
            });
        }
    }

    protected void onReceiveEvents(final JSONObject json) {
        if (mDelegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mDelegate.OnReceiveEvents(json);
                }
            });
        }
    }

    protected void onReceiveOpsResponse(final JSONObject json) {
        if (mDelegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mDelegate.OnReceiveOpsResponse(json);
                }
            });
        }
    }

}
