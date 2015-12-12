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

    protected void onOpen() {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnOpen();
                }
            });
        }
    }

    protected void onClose(final Exception e) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnClose(e);
                }
            });
        }
    }

    protected void onError(final Exception e) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnError(e);
                }
            });
        }
    }

    protected void onEvents(final JSONObject json) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnEvents(json);
                }
            });
        }
    }

    protected void onOps(final JSONObject json) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnOps(json);
                }
            });
        }
    }

}
