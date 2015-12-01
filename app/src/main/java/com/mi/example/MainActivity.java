package com.mi.example;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private final static String BIND = "bind";
    private final static String UNBIND = "unbind";
    private final static String HISTORY = "history";
    private final static String HISTORY_ID = "id";
    private final static String HISTORY_TIMESTAMP = "timestamp";

    private Future<WebSocket> webSocket;
    private TextView messages;
    private Integer app_id = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        messages = (TextView) findViewById(R.id.messages);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bind("test", "all");
                bind("test", "milano");
                // bind("test", "roma");
                // bind("pippo", "all");
                // unbind("test");
                // unbind("pippo");
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                unbind("test");
                //unbind("pippo");

            }
        });

        connect();
    }

    private void historyByID(String topic, String event, Integer value) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", app_id);
            data.put("op", HISTORY);
            data.put("topic", topic);
            data.put("event", event);
            data.put("arg1", HISTORY_ID);
            data.put("arg2", String.valueOf(value));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

    private void historyByTimestamp(String topic, String event, Integer value) {
        JSONObject data = new JSONObject();
        try {
            data.put("app_id", app_id);
            data.put("op", HISTORY);
            data.put("topic", topic);
            data.put("event", event);
            data.put("arg1", HISTORY_TIMESTAMP);
            data.put("arg2", String.valueOf(value));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(data.toString());
    }

    private void bind(String topic, String event) {
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

    private void unbind(String topic) {
        unbind(topic, "");
    }

    private void unbind(String topic, String event) {
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

    private void send(String message) {
        try {
            webSocket.get().send(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        webSocket = AsyncHttpClient.getDefaultInstance().websocket("ws://192.168.1.10:4321/ws", null,
                new AsyncHttpClient.WebSocketConnectCallback() {

                    @Override
                    public void onCompleted(Exception e, WebSocket ws) {
                        if (e == null) {
                            historyByID("test", "all", 95);

                            ws.setClosedCallback(new CompletedCallback() {
                                @Override
                                public void onCompleted(Exception e) {
                                    Log.e("TEST", "Closed");
                                }
                            });

                            ws.setStringCallback(new WebSocket.StringCallback() {
                                @Override
                                public void onStringAvailable(final String s) {
                                    Log.d("TEST", "I got a string: " + s);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            messages.setText(messages.getText() + s + "\n");
                                        }
                                    });
                                }
                            });
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
