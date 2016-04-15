/***
 * XservBase
 * <p/>
 * Copyright (C) 2015 Giovanni Amati
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/

package com.mi.xserv;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class XservBase {
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private WeakReference<Xserv.OnXservEventListener> mDelegate;
    private TrustManagerFactory mTmf;
    private SSLContext mSSLContext;

    public XservBase() {
        mDelegate = new WeakReference<>(null);
    }

    public void setOnEventListener(Xserv.OnXservEventListener onEventListener) {
        mDelegate = new WeakReference<>(onEventListener);
    }

    protected Handler getMainLooper() {
        return mHandler;
    }

    protected String urlEncodedJSON(JSONObject params) {
        String url_encode = "";
        if (params != null) {
            Iterator<?> keys = params.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = "";
                try {
                    value = params.get(key).toString();
                } catch (JSONException ignored) {
                }
                try {
                    if (url_encode.length() > 0) {
                        url_encode += "&";
                    }
                    url_encode += URLEncoder.encode(key, "UTF-8") + "=" +
                            URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {
                }
            }
        }
        return url_encode;
    }

    protected String getDeviceID() {
        Xserv.OnXservEventListener delegate = mDelegate.get();

        String deviceID = null;
        if (delegate != null) {
            try {
                deviceID = Settings.Secure.getString(((Context) delegate).getContentResolver(),
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

    protected String getLanguage() {
        // it-IT
        return Locale.getDefault().toString().replace("_", "-");
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
        final Xserv.OnXservEventListener delegate = mDelegate.get();

        if (delegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    delegate.OnOpenConnection();
                }
            });
        }
    }

    protected void onCloseConnection(final Exception e) {
        final Xserv.OnXservEventListener delegate = mDelegate.get();

        if (delegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    delegate.OnCloseConnection(e);
                }
            });
        }
    }

    protected void onErrorConnection(final Exception e) {
        final Xserv.OnXservEventListener delegate = mDelegate.get();

        if (delegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    delegate.OnErrorConnection(e);
                }
            });
        }
    }

    protected void onReceiveMessages(final JSONObject json) {
        final Xserv.OnXservEventListener delegate = mDelegate.get();

        if (delegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    delegate.OnReceiveMessages(json);
                }
            });
        }
    }

    protected void OnReceiveOperations(final JSONObject json) {
        final Xserv.OnXservEventListener delegate = mDelegate.get();

        if (delegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    delegate.OnReceiveOperations(json);
                }
            });
        }
    }

}
