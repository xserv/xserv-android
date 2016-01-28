package com.mi.xserv;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class XservBase {
    final protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected OnXservEventListener mDelegate;
    protected String mDeviceID;

    public XservBase() {
        mDelegate = null;
        mDeviceID = "";
    }

    public void setOnEventListener(OnXservEventListener onEventListener) {
        mDelegate = onEventListener;

        if (mDelegate != null) {
            try {
                mDeviceID = Settings.Secure.getString(((Context) mDelegate).getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
