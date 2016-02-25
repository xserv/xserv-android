/***
 * Xserv
 * <p/>
 * Copyright (C) 2015 Giovanni Amati
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/

package com.mi.xserv;

import android.os.Build;
import android.util.Base64;

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
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Xserv extends XservBase {
    // signal
    public final static int OP_HANDSHAKE = 100;
    // op
    public final static int OP_PUBLISH = 200;
    public final static int OP_SUBSCRIBE = 201;
    public final static int OP_UNSUBSCRIBE = 202;
    public final static int OP_HISTORY = 203;
    public final static int OP_PRESENCE = 204;
    public final static int OP_TOPICS = 205;
    public final static int OP_JOIN = OP_SUBSCRIBE + 200;
    public final static int OP_LEAVE = OP_UNSUBSCRIBE + 200;
    // in uso in history
    public final static String HISTORY_ID = "id";
    public final static String HISTORY_TIMESTAMP = "timestamp";
    // op result_code
    public final static int RC_OK = 1;
    public final static int RC_GENERIC_ERROR = 0;
    public final static int RC_ARGS_ERROR = -1;
    public final static int RC_ALREADY_SUBSCRIBED = -2;
    public final static int RC_UNAUTHORIZED = -3;
    public final static int RC_NO_TOPIC = -4;
    public final static int RC_NO_DATA = -5;
    public final static int RC_NOT_PRIVATE = -6;
    public final static int RC_LIMIT_MESSAGES = -7;

    private final static String TAG = "Xserv";
    // private final static String ADDRESS = "192.168.130.153";
    private final static String ADDRESS = "mobile-italia.com";
    private final static String PORT = "4332";
    private final static String TLS_PORT = "8332";
    private final static String URL = "ws%1$s://%2$s:%3$s/ws/%4$s?version=%5$s";
    private final static String DEFAULT_AUTH_URL = "http%1$s://%2$s:%3$s/app/%4$s/auth_user";
    private final static int DEFAULT_RI = 5000;

    // attributes
    private final String mAppId;
    private Future<WebSocket> mConn;
    private int mReconnectInterval;
    private boolean isAutoReconnect;
    private JSONObject mUserData;
    private boolean isConnected;
    private boolean isSecure;

    /**
     * Return an instance of Xserv connector.
     *
     * @param app_id identifier of your application. You can find it on Xserv Dashboard.
     */
    public Xserv(String app_id, boolean security) {
        super();

        mAppId = app_id;
        mConn = null;
        mUserData = new JSONObject();
        mReconnectInterval = DEFAULT_RI;

        isAutoReconnect = false;
        isConnected = false;

        // TLS
        isSecure = security;
    }

    public static boolean isPrivateTopic(String topic) {
        return topic.startsWith("@");
    }

    public boolean isConnected() {
        return mConn != null && isConnected;
    }

    public void connect() {
        connect(false);
    }

    private void connect(boolean no_ar) {
        if (!no_ar) {
            isAutoReconnect = true;
        }

        if (!isConnected()) {
            String protocol = "";
            String port = PORT;
            if (isSecure) {
                protocol = "s";
                port = TLS_PORT;
            }

            AsyncHttpClient as = getSocketClient(isSecure);

            mConn = as.websocket(String.format(
                            URL, protocol, ADDRESS, port, mAppId, BuildConfig.VERSION_NAME), null,
                    new AsyncHttpClient.WebSocketConnectCallback() {

                        @Override
                        public void onCompleted(Exception e, WebSocket ws) {
                            if (e == null) {
                                setOtherWsCallback(ws);

                                handshake();
                            } else {
                                onErrorConnection(e);

                                // eccezione, error socket in connection non e' la error
                                reConnect();
                            }
                        }
                    });
        }
    }

    private void setOtherWsCallback(WebSocket ws) {
        ws.setClosedCallback(new CompletedCallback() {

            @Override
            public void onCompleted(Exception e) {
                isConnected = false;

                onCloseConnection(e);

                reConnect();
            }
        });

        ws.setStringCallback(new WebSocket.StringCallback() {

            @Override
            public void onStringAvailable(String event) {
                manageMessage(event);
            }
        });
    }

    private void manageMessage(String event) {
        JSONObject json = null;
        try {
            json = new JSONObject(event);
        } catch (JSONException ignored) {
        }

        if (json != null) {
            int op = 0;
            try {
                op = json.getInt("op");
            } catch (JSONException ignored) {
            }

            if (op == 0) {
                try {
                    json.put("data", new JSONObject(json.getString("data")));
                } catch (JSONException ignored) {
                }

                onReceiveMessages(json);
            } else if (op > 0) {
                int rc = 0;
                String topic = "";
                String descr = "";
                try {
                    rc = json.getInt("rc");
                    topic = json.getString("topic");
                    descr = json.getString("descr");

                    json.put("name", stringifyOp(op));
                } catch (JSONException ignored) {
                }

                if (op == OP_HANDSHAKE) { // vera connection
                    if (rc == RC_OK) {
                        try {
                            String data = json.getString("data");
                            byte[] b = Base64.decode(data, Base64.DEFAULT);
                            String raw = new String(b, "UTF-8");
                            Object j = new JSONTokener(raw).nextValue();
                            if (j instanceof JSONObject) {
                                JSONObject data_json = new JSONObject(raw);

                                setUserData(data_json);
                            }
                        } catch (JSONException | UnsupportedEncodingException ignored) {
                        }

                        if (mUserData.length() > 0) {
                            isConnected = true;

                            onOpenConnection();
                        } else {
                            onErrorConnection(new Exception(descr));
                        }
                    } else {
                        onErrorConnection(new Exception(descr));
                    }
                } else {
                    try {
                        String data = json.getString("data");
                        byte[] b = Base64.decode(data, Base64.DEFAULT);
                        String raw = new String(b, "UTF-8");
                        Object j = new JSONTokener(raw).nextValue();
                        if (j instanceof JSONObject) {
                            JSONObject data_json = new JSONObject(raw);
                            json.put("data", data_json);

                            // bind privata ok
                            if (op == OP_SUBSCRIBE && isPrivateTopic(topic) && rc == RC_OK) {
                                setUserData(data_json);
                            }
                        } else if (j instanceof JSONArray) {
                            json.put("data", new JSONArray(raw));
                        }
                    } catch (JSONException | UnsupportedEncodingException ignored) {
                    }

                    onReceiveOpsResponse(json);
                }
            }
        }
    }

    private void reConnect() {
        if (isAutoReconnect) {
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    connect(true);
                }
            }, mReconnectInterval);
        }
    }

    public void disconnect() {
        isAutoReconnect = false;

        if (isConnected()) {
            try {
                mConn.get().close();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer getReconnectInterval() {
        return mReconnectInterval;
    }

    public void setReconnectInterval(Integer milliseconds) {
        mReconnectInterval = milliseconds;
    }

    private void handshake() {
        JSONObject stat = new JSONObject();
        try {
            String model = Build.MODEL;
            if (model.length() > 45) {
                model = model.substring(0, 45);
            }

            stat.put("uuid", getDeviceID());
            stat.put("model", model);
            stat.put("os", "Android " + Build.VERSION.RELEASE);
            stat.put("tz_offset", getTimeZoneOffset());
            stat.put("tz_dst", getTimeZoneDst());
            stat.put("lang", getLanguage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        wsSend(stat);
    }

    private void send(final JSONObject json) {
        if (!isConnected()) return;

        int op = 0;
        String topic = "";
        try {
            op = json.getInt("op");
            topic = json.getString("topic");
        } catch (JSONException ignored) {
        }

        if (op == OP_SUBSCRIBE && isPrivateTopic(topic)) {
            JSONObject auth_endpoint = null;
            try {
                auth_endpoint = json.getJSONObject("auth_endpoint");
            } catch (JSONException ignored) {
            }

            if (auth_endpoint != null) {
                String protocol = "";
                String port = PORT;
                if (isSecure) {
                    protocol = "s";
                    port = TLS_PORT;
                }

                String auth_url = String.format(DEFAULT_AUTH_URL, protocol, ADDRESS, port, mAppId);
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

                final SimpleHttpRequest request =
                        new SimpleHttpRequest(SimpleHttpRequest.POST, auth_url);

                if (isSecure && mSSLContext != null) {
                    request.setSecure(true);
                    request.setSSLContext(mSSLContext);
                }

                request.setContentType("application/json; charset=UTF-8");

                request.setParam("socket_id", getSocketId());
                request.setParam("topic", topic);
                request.setParam("user", auth_user);
                request.setParam("pass", auth_pass);

                SimpleHttpTask task = new SimpleHttpTask();

                task.setOnResponseListener(new ITaskListener.OnResponseListener() {

                    @Override
                    public void onResponse(String output) {
                        JSONObject new_json = null;
                        try {
                            new_json = new JSONObject(json.toString()); // clone
                            new_json.remove("auth_endpoint");
                        } catch (JSONException ignored) {
                        }

                        if (new_json != null) {
                            try {
                                JSONObject data_sign = new JSONObject(output);
                                new_json.put("arg1", request.getParam("user"));
                                new_json.put("arg2", data_sign.getString("data"));
                                new_json.put("arg3", data_sign.getString("sign"));
                            } catch (JSONException ignored) {
                            }

                            wsSend(new_json);
                        } else {
                            // like fail
                            wsSend(json);
                        }
                    }

                    @Override
                    public void onFail() {
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

    private void wsSend(JSONObject json) {
        try {
            mConn.get().send(json.toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public String getSocketId() {
        String socket_id = "";
        try {
            socket_id = mUserData.getString("socket_id");
        } catch (JSONException ignored) {
            // e.printStackTrace();
        }
        return socket_id;
    }

    public JSONObject getUserData() {
        return mUserData;
    }

    private void setUserData(JSONObject json) {
        mUserData = json;
    }

    private String stringifyOp(int code) {
        switch (code) {
            case OP_SUBSCRIBE:
                return "subscribe";
            case OP_UNSUBSCRIBE:
                return "unsubscribe";
            case OP_HISTORY:
                return "history";
            case OP_PRESENCE:
                return "presence";
            case OP_JOIN:
                return "join";
            case OP_LEAVE:
                return "leave";
            case OP_PUBLISH:
                return "publish";
            case OP_HANDSHAKE:
                return "handshake";
            case OP_TOPICS:
                return "topics";
        }
        return "";
    }

    public String publish(String topic, JSONObject data) {
        return publish(topic, data.toString());
    }

    public String publish(String topic, String data) {
        if (!isConnected()) return "";

        String uuid = UUID.randomUUID().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("op", OP_PUBLISH);
            json.put("topic", topic);
            json.put("arg1", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(json);
        return uuid;
    }

    public String subscribe(String topic) {
        return subscribe(topic, null);
    }

    public String subscribe(String topic, JSONObject auth_endpoint) {
        if (!isConnected()) return "";

        String uuid = UUID.randomUUID().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("op", OP_SUBSCRIBE);
            json.put("topic", topic);
            if (auth_endpoint != null) {
                json.put("auth_endpoint", auth_endpoint);
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        send(json);
        return uuid;
    }

    public String unsubscribe(String topic) {
        if (!isConnected()) return "";

        String uuid = UUID.randomUUID().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("op", OP_UNSUBSCRIBE);
            json.put("topic", topic);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        send(json);
        return uuid;
    }

    public String historyById(String topic, Integer offset) {
        return historyById(topic, offset, 0);
    }

    public String historyById(String topic, Integer offset, Integer limit) {
        if (!isConnected()) return "";

        String uuid = UUID.randomUUID().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("op", OP_HISTORY);
            json.put("topic", topic);
            json.put("arg1", HISTORY_ID);
            json.put("arg2", String.valueOf(offset));
            json.put("arg3", String.valueOf(limit));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(json);
        return uuid;
    }

    public String historyByTimestamp(String topic, Integer offset) {
        return historyByTimestamp(topic, offset, 0);
    }

    public String historyByTimestamp(String topic, Integer offset, Integer limit) {
        if (!isConnected()) return "";

        String uuid = UUID.randomUUID().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("op", OP_HISTORY);
            json.put("topic", topic);
            json.put("arg1", HISTORY_TIMESTAMP);
            json.put("arg2", String.valueOf(offset));
            json.put("arg3", String.valueOf(limit));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(json);
        return uuid;
    }

    public String presence(String topic) {
        if (!isConnected()) return "";

        String uuid = UUID.randomUUID().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("op", OP_PRESENCE);
            json.put("topic", topic);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(json);
        return uuid;
    }

    public String topics() {
        if (!isConnected()) return "";

        String uuid = UUID.randomUUID().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("op", OP_TOPICS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(json);
        return uuid;
    }

}
