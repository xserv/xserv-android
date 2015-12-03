package com.mi.xserv;

import android.util.Base64;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.mi.xserv.http.ITaskListener;
import com.mi.xserv.http.SimpleHttpRequest;
import com.mi.xserv.http.SimpleHttpTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Xserv extends XservBase {
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
    private final static String SERVER = "192.168.130.153:4321";
    private final static String URL = "ws://" + SERVER + "/ws";
    private final static String DEFAULT_AUTH_URL = "http://" + SERVER + "/auth_user/";
    private final static int DEFAULT_RI = 5000;
    private final static String OP_SEP = ":";

    // attributes
    private String mAppId;
    private Future<WebSocket> mConn;
    private boolean is_finish_ops;
    private ArrayList<JSONObject> mOps;
    private int reconnectInterval;
    private boolean autoreconnect;
    private JSONObject mUserData;
    private boolean isConnect;
    private boolean isDebug;

    public Xserv(String app_id) {
        super();

        mAppId = app_id;
        mConn = null;
        is_finish_ops = false;
        mOps = new ArrayList<>();
        reconnectInterval = DEFAULT_RI;
        autoreconnect = false;
        mUserData = new JSONObject();
        isConnect = false;
        isDebug = false;
    }

    public static boolean isPrivateTopic(String topic) {
        return topic.startsWith("@");
    }

    public void setDebug(boolean flag) {
        isDebug = flag;
    }

    public boolean isConnected() {
        return mConn != null && isConnect;
    }

    public void connect() {
        autoreconnect = true;

        if (!isConnected()) {
            AsyncHttpClient as = AsyncHttpClient.getDefaultInstance();
            mConn = as.websocket(URL, null, new AsyncHttpClient.WebSocketConnectCallback() {

                @Override
                public void onCompleted(final Exception e, final WebSocket ws) {
                    if (e == null) {
                        if (isDebug) {
                            Log.d(TAG, "open");
                        }

                        setOtherWsCallback(ws);

                        isConnect = true;

                        for (JSONObject op : mOps) {
                            send(op);
                        }

                        is_finish_ops = true;

                        onOpen();
                    } else {
                        // eccezione, error socket
                        if (autoreconnect) {
                            setTimeout();
                        }

                        onError();
                    }
                }
            });
        }
    }

    private void setOtherWsCallback(WebSocket ws) {
        ws.setClosedCallback(new CompletedCallback() {

            @Override
            public void onCompleted(Exception ignored) {
                if (isDebug) {
                    Log.d(TAG, "close");
                }

                isConnect = false;
                is_finish_ops = false;

                if (autoreconnect) {
                    setTimeout();
                }

                onClose();
            }
        });

        ws.setStringCallback(new WebSocket.StringCallback() {

            @Override
            public void onStringAvailable(final String event) {
                if (isDebug) {
                    Log.d(TAG, "raw " + event);
                }

                if (!event.startsWith(OP_SEP)) {
                    JSONObject json = null;
                    try {
                        json = new JSONObject(event);
                        try {
                            String message = json.getString("message");
                            Object j = new JSONTokener(message).nextValue();
                            if (j instanceof JSONObject) {
                                json.put("message", new JSONObject(message));
                            }
                        } catch (JSONException ignored) {
                            // e2.printStackTrace();
                        }
                    } catch (JSONException ignored) {
                        // e1.printStackTrace();
                    }

                    onEvents(json);
                } else {
                    String[] arr = event.split(OP_SEP, -1);
                    if (arr.length >= 7) {
                        // data structure json array o object
                        Object data_json = JSONObject.NULL;
                        String data = arr[5];
                        if (data != null) {
                            byte[] b = Base64.decode(data, Base64.DEFAULT);
                            if (b.length > 0) {
                                try {
                                    String raw = new String(b, "UTF-8");
                                    Object j = new JSONTokener(raw).nextValue();
                                    if (j instanceof JSONObject) {
                                        data_json = new JSONObject(raw);
                                    } else if (j instanceof JSONArray) {
                                        data_json = new JSONArray(raw);
                                    }
                                } catch (JSONException | UnsupportedEncodingException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }

                        JSONObject json = new JSONObject();
                        int rc = Integer.parseInt(arr[1]);
                        int op = Integer.parseInt(arr[2]);
                        String topic = arr[3];

                        try {
                            json.put("rc", rc);
                            json.put("op", op);
                            json.put("name", stringifyOp(op));
                            json.put("topic", topic);
                            json.put("event", arr[4]);
                            json.put("data", data_json);
                            json.put("descr", arr[6]);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                        // bind privata ok
                        if (op == BIND && isPrivateTopic(topic) && rc == RC_OK) {
                            setUserData((JSONObject) data_json);
                        }

                        onEventsOp(json);
                    }
                }
            }
        });
    }

    private void setTimeout() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (isDebug) {
                    Log.d(TAG, "try reconnect");
                }

                connect();
            }
        }, reconnectInterval);
    }

    public void disconnect() {
        autoreconnect = false;

        if (isConnected()) {
            try {
                mConn.get().close();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void setReconnectInterval(Integer value) {
        reconnectInterval = value;
    }

    private void send(final JSONObject json) {
        if (isConnected()) {
            int op = 0;
            String topic = "";
            try {
                op = json.getInt("op");
                topic = json.getString("topic");
            } catch (JSONException ignored) {
                // e.printStackTrace();
            }

            if (op == BIND && isPrivateTopic(topic)) {
                JSONObject auth_endpoint = null;
                try {
                    auth_endpoint = json.getJSONObject("auth_endpoint");
                } catch (JSONException ignored) {
                    // e.printStackTrace();
                }

                if (auth_endpoint != null) {
                    String auth_url = DEFAULT_AUTH_URL + mAppId;
                    String auth_user = "";
                    String auth_pass = "";
                    try {
                        auth_url = auth_endpoint.getString("endpoint");
                    } catch (JSONException ignored) {
                    }
                    try {
                        auth_user = auth_endpoint.getString("user");
                        auth_pass = auth_endpoint.getString("pass");
                    } catch (JSONException ignored) {
                    }

                    final SimpleHttpRequest request = new SimpleHttpRequest(SimpleHttpRequest.POST, auth_url);
                    request.setContentType("application/json; charset=UTF-8");
                    request.setParam("topic", topic);
                    request.setParam("user", auth_user);
                    request.setParam("pass", auth_pass);

                    SimpleHttpTask task = new SimpleHttpTask();

                    task.setOnResponseListener(new ITaskListener.OnResponseListener() {

                        @Override
                        public void onResponse(String output) {
                            JSONObject new_json = new JSONObject();
                            try {
                                new_json.put("app_id", json.get("app_id"));
                                new_json.put("op", json.get("op"));
                                new_json.put("topic", json.get("topic"));
                                new_json.put("event", json.get("event"));
                            } catch (JSONException ignored) {
                                // e.printStackTrace();
                            }

                            try {
                                JSONObject data_sign = new JSONObject(output);
                                new_json.put("arg1", request.getParam("user"));
                                new_json.put("arg2", JSONObject.quote(data_sign.getString("data")));
                                new_json.put("arg3", data_sign.getString("sign"));
                            } catch (JSONException ignored) {
                                // e.printStackTrace();
                            }

                            wsSend(new_json);
                        }

                        @Override
                        public void onFail(String output) {
                            wsSend(json);
                        }
                    });

                    task.execute(request);
                } else {
                    wsSend(json);
                }
            } else {
                wsSend(json);
            }
        }
    }

    private void wsSend(final JSONObject json) {
        try {
            mConn.get().send(json.toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void addOp(JSONObject json) {
        int op = 0;
        try {
            op = json.getInt("op");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // salva tutte op da ripetere su riconnessione
        if (op == BIND || op == UNBIND) {
            mOps.add(json);
        }

        if (is_finish_ops) {
            send(json);
        }
    }

    private void setUserData(JSONObject json) {
        mUserData = json;
    }

    public JSONObject getUserData() {
        return mUserData;
    }

    private String stringifyOp(int code) {
        switch (code) {
            case BIND:
                return "bind";
            case UNBIND:
                return "unbind";
            case HISTORY:
                return "history";
            case PRESENCE:
                return "presence";
            case PRESENCE_IN:
                return "presence_in";
            case PRESENCE_OUT:
                return "presence_out";
            case TRIGGER:
                return "trigger";
        }
        return "";
    }

    public void trigger(String topic, String event, JSONObject message) {
        trigger(topic, event, message.toString());
    }

    public void trigger(String topic, String event, String message) {
        if (is_finish_ops) {
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
            send(data);
        }
    }

    public void bind(String topic, String event) {
        bind(topic, event, null);
    }

    public void bind(String topic, String event, JSONObject auth_endpoint) {
        ArrayList<String> topics = new ArrayList<>(Arrays.asList(topic));
        ArrayList<String> events = new ArrayList<>(Arrays.asList(event));
        bind(topics, events, auth_endpoint);
    }

    public void bind(ArrayList<String> topics, ArrayList<String> events) {
        bind(topics, events, null);
    }

    public void bind(ArrayList<String> topics, ArrayList<String> events, JSONObject auth_endpoint) {
        for (String t : topics) {
            for (String e : events) {
                JSONObject data = new JSONObject();
                try {
                    data.put("app_id", mAppId);
                    data.put("op", BIND);
                    data.put("topic", t);
                    data.put("event", e);
                    data.put("auth_endpoint", auth_endpoint);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                addOp(data);
            }
        }
    }

    public void unbind(String topic) {
        unbind(topic, "");
    }

    public void unbind(String topic, String event) {
        ArrayList<String> topics = new ArrayList<>(Arrays.asList(topic));
        ArrayList<String> events = new ArrayList<>(Arrays.asList(event));
        unbind(topics, events);
    }

    public void unbind(ArrayList<String> topics) {
        ArrayList<String> events = new ArrayList<>(Arrays.asList(""));
        unbind(topics, events);
    }

    public void unbind(ArrayList<String> topics, ArrayList<String> events) {
        for (String t : topics) {
            for (String e : events) {
                JSONObject data = new JSONObject();
                try {
                    data.put("app_id", mAppId);
                    data.put("op", UNBIND);
                    data.put("topic", t);
                    data.put("event", e);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                addOp(data);
            }
        }
    }

    public void historyById(String topic, String event, Integer value) {
        historyById(topic, event, value, 0);
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
        addOp(data);
    }

    public void historyByTimestamp(String topic, String event, Integer value) {
        historyByTimestamp(topic, event, value, 0);
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
        addOp(data);
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
        addOp(data);
    }

}
