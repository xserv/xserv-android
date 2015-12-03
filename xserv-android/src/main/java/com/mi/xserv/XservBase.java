package com.mi.xserv;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

public class XservBase {
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected OnXservEventListener mListeners;

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

    protected void onClose() {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnClose();
                }
            });
        }
    }

    protected void onError() {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnError();
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

    protected void onEventsOp(final JSONObject json) {
        if (mListeners != null) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mListeners.OnEventsOp(json);
                }
            });
        }
    }

}
