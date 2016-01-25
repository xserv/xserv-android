package com.mi.xserv;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

public class XservBase {
    final protected Handler mHandler = new Handler(Looper.getMainLooper());
    private OnXservEventListener mListeners;

    public XservBase() {
        mListeners = null;
    }

    public void setOnEventListener(OnXservEventListener onEventListener) {
        mListeners = onEventListener;
    }

    protected void onOpenConnection() {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnOpenConnection();
                }
            });
        }
    }

    protected void onCloseConnection(final Exception e) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnCloseConnection(e);
                }
            });
        }
    }

    protected void onErrorConnection(final Exception e) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnErrorConnection(e);
                }
            });
        }
    }

    protected void onReceiveEvents(final JSONObject json) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnReceiveEvents(json);
                }
            });
        }
    }

    protected void onReceiveOpsResponse(final JSONObject json) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnReceiveOpsResponse(json);
                }
            });
        }
    }

}
