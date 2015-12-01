package com.mi.xserv;

import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Xserv {
    private final static String TAG = "Xserv";

    public final static int TRIGGER = 200;
    public final static int BIND = 201;
    public final static int UNBIND = 202;
    public final static int HISTORY = 203;
    public final static int PRESENCE = 204;

    // in uso in presence
    public final static int PRESENCE_IN = BIND + 200;
    public final static int PRESENCE_OUT = UNBIND + 200;

    // in uso in history
    public final static String HISTORY_ID = "id";
    public final static String HISTORY_TIMESTAMP = "timestamp";

    // events:op result_code
    public final static int RC_OK = 1;
    public final static int RC_GENERIC_ERROR = 0;
    public final static int RC_ARGS_ERROR = -1;
    public final static int RC_ALREADY_BINDED = -2;
    public final static int RC_UNAUTHORIZED = -3;
    public final static int RC_NO_EVENT = -4;
    public final static int RC_NO_DATA = -5;
    public final static int RC_NOT_PRIVATE = -6;

    private final static String URL = "ws://192.168.130.153:4321/ws";
    private final static String DEFAULT_AUTH_URL = "http://192.168.130.153:4321/auth_user/";
    private final static int DEFAULT_RI = 5000;
    private final static String OP_SEP = ":";

    private Future<WebSocket> mConn;
    private boolean isConnect;
    private String mAppId;
    private int reconnectInterval;
    private boolean autoreconnect;

    public Xserv(String app_id) {
        mAppId = app_id;
        mConn = null;
        isConnect = false;
        reconnectInterval = DEFAULT_RI;
        autoreconnect = false;
    }

    public static boolean isPrivateTopic(String topic) {
        return topic.startsWith("@");
    }

    public boolean isConnected() {
        return mConn != null && isConnect;
    }

    public void connect() {
        autoreconnect = true;

        if (!isConnected()) {
            mConn = AsyncHttpClient.getDefaultInstance().websocket(URL, null,
                    new AsyncHttpClient.WebSocketConnectCallback() {

                        @Override
                        public void onCompleted(Exception e, WebSocket ws) {
                            if (e == null) {
                                isConnect = true;
                                Log.d(TAG, "open");

                                ws.setClosedCallback(new CompletedCallback() {
                                    @Override
                                    public void onCompleted(Exception e) {
                                        isConnect = false;
                                        Log.d(TAG, "close");
                                    }
                                });

                                ws.setStringCallback(new WebSocket.StringCallback() {
                                    @Override
                                    public void onStringAvailable(final String s) {

                                    }
                                });
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public void disconnect() {
        autoreconnect = false;

        if (isConnect) {
            try {
                mConn.get().close();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void setReconnectInterval(Integer value) {
        this.reconnectInterval = value;
    }

    private void send(String message) {
        if (isConnected()) {
            try {
                mConn.get().send(message);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void add_op() {

    }

    private void add_user_data() {

    }

    private String stringify_op(int code) {
        if (code == Xserv.BIND) {
            return "bind";
        } else if (code == Xserv.UNBIND) {
            return "unbind";
        } else if (code == Xserv.HISTORY) {
            return "history";
        } else if (code == Xserv.PRESENCE) {
            return "presence";
        } else if (code == Xserv.PRESENCE_IN) {
            return "presence_in";
        } else if (code == Xserv.PRESENCE_OUT) {
            return "presence_out";
        } else if (code == Xserv.TRIGGER) {
            return "trigger";
        }
        return "";
    }

    public void addEventListener() {

    }

    public void trigger(String topic, String event, String message) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", TRIGGER);
            data.put("topic", topic);
            data.put("event", event);
            data.put("arg1", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

    public void bind(String topic, String event) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", BIND);
            data.put("topic", topic);
            data.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

    public void unbind(String topic) {
        unbind(topic, "");
    }

    public void unbind(String topic, String event) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", UNBIND);
            data.put("topic", topic);
            data.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

    public void historyById(String topic, String event, Integer value, Integer limit) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", HISTORY);
            data.put("topic", topic);
            data.put("event", event);
            data.put("arg1", HISTORY_ID);
            data.put("arg2", String.valueOf(value));
            data.put("arg3", String.valueOf(limit));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

    public void historyByTimestamp(String topic, String event, Integer value, Integer limit) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", HISTORY);
            data.put("topic", topic);
            data.put("event", event);
            data.put("arg1", HISTORY_TIMESTAMP);
            data.put("arg2", String.valueOf(value));
            data.put("arg3", String.valueOf(limit));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

    public void presence(String topic, String event) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", PRESENCE);
            data.put("topic", topic);
            data.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

}
