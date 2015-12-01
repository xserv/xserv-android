package com.mi.xserv;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Xserv {
    private final static int TRIGGER = 200;
    private final static int BIND = 201;
    private final static int UNBIND = 202;
    private final static int HISTORY = 203;
    private final static int PRESENCE = 204;

    // in uso in presence
    private final static int PRESENCE_IN = BIND + 200;
    private final static int PRESENCE_OUT = UNBIND + 200;

    // in uso in history
    private final static String HISTORY_ID = "id";
    private final static String HISTORY_TIMESTAMP = "timestamp";

    // events:op result_code
    private final static int RC_OK = 1;
    private final static int RC_GENERIC_ERROR = 0;
    private final static int RC_ARGS_ERROR = -1;
    private final static int RC_ALREADY_BINDED = -2;
    private final static int RC_UNAUTHORIZED = -3;
    private final static int RC_NO_EVENT = -4;
    private final static int RC_NO_DATA = -5;
    private final static int RC_NOT_PRIVATE = -6;

    private final static String URL = "ws://192.168.130.153:4321/ws";
    private final static String DEFAULT_AUTH_URL = "http://192.168.130.153:4321/auth_user/";
    private final static int DEFAULT_RI = 5000;
    private final static String OP_SEP = ":";

    private Future<WebSocket> conn;
    private String app_id;
    private int reconnectInterval;
    private boolean autoreconnect;

    public Xserv(String app_id) {
        this.app_id = app_id;
        this.conn = null;
        this.reconnectInterval = DEFAULT_RI;
        this.autoreconnect = false;
    }

    public static boolean isPrivateTopic(String topic) {
        return topic.startsWith("@");
    }

    public boolean isConnected() {
        // return this.conn && this.conn.readyState == WebSocket.OPEN;
        return true;
    }

    public void connect() {
        conn = AsyncHttpClient.getDefaultInstance().websocket(URL, null,
                new AsyncHttpClient.WebSocketConnectCallback() {

                    @Override
                    public void onCompleted(Exception e, WebSocket ws) {
                        if (e == null) {

                            ws.setClosedCallback(new CompletedCallback() {
                                @Override
                                public void onCompleted(Exception e) {

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

    public void disconnect() {

    }

    public void setReconnectInterval(Integer value) {
        this.reconnectInterval = value;
    }

    private void send(String message) {
        try {
            conn.get().send(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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
            data.put("app_id", app_id);
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
            data.put("app_id", app_id);
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
            data.put("app_id", app_id);
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
            data.put("app_id", app_id);
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
            data.put("app_id", app_id);
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
            data.put("app_id", app_id);
            data.put("op", PRESENCE);
            data.put("topic", topic);
            data.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

}
