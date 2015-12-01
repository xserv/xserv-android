package com.mi.xserv;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Xserv {
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

    private final static String TAG = "Xserv";
    private final static String URL = "ws://192.168.130.153:4321/ws";
    private final static String DEFAULT_AUTH_URL = "http://192.168.130.153:4321/auth_user/";
    private final static int DEFAULT_RI = 5000;
    private final static String OP_SEP = ":";

    private String mAppId;
    private Future<WebSocket> mConn;
    private boolean is_finish_ops;
    private ArrayList<JSONObject> mListeners;
    private ArrayList<JSONObject> mOps;
    private int reconnectInterval;
    private boolean autoreconnect;
    private boolean isConnect;

    public Xserv(String app_id) {
        mAppId = app_id;
        mConn = null;
        is_finish_ops = false;
        mListeners = new ArrayList<>();
        mOps = new ArrayList<>();
        reconnectInterval = DEFAULT_RI;
        autoreconnect = false;
        isConnect = false;
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
                                Log.d(TAG, "open");
                                isConnect = true;

                                ws.setClosedCallback(new CompletedCallback() {
                                    @Override
                                    public void onCompleted(Exception e) {
                                        Log.d(TAG, "close");
                                        is_finish_ops = false;
                                        isConnect = false;

                                        if (autoreconnect) {
                                            setTimeout();
                                        }
                                    }
                                });

                                ws.setStringCallback(new WebSocket.StringCallback() {
                                    @Override
                                    public void onStringAvailable(final String s) {

                                    }
                                });

                                is_finish_ops = true;
                            } else {
                                // eccezione, error socket
                                if (autoreconnect) {
                                    setTimeout();
                                }
                            }
                        }
                    });
        }
    }

    private void setTimeout() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "try reconnect");

                connect();
            }
        }, reconnectInterval);
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

    private void send(JSONObject json) {
        if (isConnected()) {
            int op = 0;
            String topic = "";
            try {
                op = json.getInt("op");
                topic = json.getString("topic");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (op == Xserv.BIND && Xserv.isPrivateTopic(topic)) {

            } else {
                try {
                    mConn.get().send(json.toString());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void add_op(JSONObject json) {
        int op = 0;
        try {
            op = json.getInt("op");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // salva tutte op da ripetere su riconnessione
        if (op == Xserv.BIND || op == Xserv.UNBIND) {
            mOps.add(json);
        }

        if (is_finish_ops) {
            send(json);
        }
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
        if (is_finish_ops) {
            JSONObject data = new JSONObject();
            try {
                data.put("app_id", mAppId);
                data.put("op", Xserv.TRIGGER);
                data.put("topic", topic);
                data.put("event", event);
                data.put("arg1", message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            send(data);
        }
    }

    public void bind(String topic, String event) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", Xserv.BIND);
            data.put("topic", topic);
            data.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        add_op(data);
    }

    public void unbind(String topic) {
        unbind(topic, "");
    }

    public void unbind(String topic, String event) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", Xserv.UNBIND);
            data.put("topic", topic);
            data.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        add_op(data);
    }

    public void historyById(String topic, String event, Integer value, Integer limit) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", Xserv.HISTORY);
            data.put("topic", topic);
            data.put("event", event);
            data.put("arg1", Xserv.HISTORY_ID);
            data.put("arg2", String.valueOf(value));
            data.put("arg3", String.valueOf(limit));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        add_op(data);
    }

    public void historyByTimestamp(String topic, String event, Integer value, Integer limit) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", Xserv.HISTORY);
            data.put("topic", topic);
            data.put("event", event);
            data.put("arg1", Xserv.HISTORY_TIMESTAMP);
            data.put("arg2", String.valueOf(value));
            data.put("arg3", String.valueOf(limit));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        add_op(data);
    }

    public void presence(String topic, String event) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", mAppId);
            data.put("op", Xserv.PRESENCE);
            data.put("topic", topic);
            data.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        add_op(data);
    }

}
