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

import com.koushikdutta.async.http.AsyncHttpClient;
import com.mi.xserv.http.SimpleHttpRequest;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class XservBase {
    protected final Handler mHandler = new Handler(Looper.getMainLooper());
    protected WeakReference<OnXservEventListener> mDelegate;
    private TrustManagerFactory mTmf;
    private SSLContext mSSLContext;

    public XservBase() {
        mDelegate = new WeakReference<>(null);
    }

    public void setOnEventListener(OnXservEventListener onEventListener) {
        mDelegate = new WeakReference<>(onEventListener);
    }

    private void fixAuthority() {
        OnXservEventListener delegate = mDelegate.get();

        if (delegate != null && (mTmf == null || mSSLContext == null)) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = ((Context) delegate).getResources().openRawResource(R.raw.lets_encrypt_x1_cross_signed_pem);
                Certificate ca = cf.generateCertificate(caInput);
                caInput.close();

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                mTmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                mTmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
                mSSLContext = SSLContext.getInstance("TLS");
                mSSLContext.init(null, mTmf.getTrustManagers(), null);
            } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException |
                    KeyManagementException | IOException e) {

                e.printStackTrace();
            }
        }
    }

    public AsyncHttpClient getWebSocketClient(boolean securiry) {
        AsyncHttpClient as = AsyncHttpClient.getDefaultInstance();

        if (securiry) {
            fixAuthority();

            if (mTmf != null && mSSLContext != null) {
                as.getSSLSocketMiddleware().setTrustManagers(mTmf.getTrustManagers());
                as.getSSLSocketMiddleware().setSSLContext(mSSLContext);
            }
        }

        return as;
    }

    public SimpleHttpRequest getHttpClient(String url, boolean securiry) {
        SimpleHttpRequest request = new SimpleHttpRequest(SimpleHttpRequest.POST, url);

        if (securiry) {
            request.setSecure(true);

            fixAuthority();

            if (mSSLContext != null) {
                request.setSSLContext(mSSLContext);
            }
        }

        return request;
    }

    protected String getDeviceID() {
        OnXservEventListener delegate = mDelegate.get();

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
        final OnXservEventListener delegate = mDelegate.get();

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
        final OnXservEventListener delegate = mDelegate.get();

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
        final OnXservEventListener delegate = mDelegate.get();

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
        final OnXservEventListener delegate = mDelegate.get();

        if (delegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    delegate.OnReceiveMessages(json);
                }
            });
        }
    }

    protected void onReceiveOpsResponse(final JSONObject json) {
        final OnXservEventListener delegate = mDelegate.get();

        if (delegate != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    delegate.OnReceiveOpsResponse(json);
                }
            });
        }
    }

}
