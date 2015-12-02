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

import com.mi.xserv.OnXservEventListener;
import com.mi.xserv.Xserv;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnXservEventListener {
    private final static String TAG = "Example";
    private TextView mMessagesView;
    private Xserv xserv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMessagesView = (TextView) findViewById(R.id.messages);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // xserv.disconnect();
                xserv.unbind("milano", "all");
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xserv.bind("milano", "all");
            }
        });

        xserv = new Xserv("qLxFC-1");
        // xserv.setDebug(true);

        xserv.setOnEventListener(this);

        xserv.bind("milano", "pippo");

        xserv.connect();
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

    @Override
    public void OnOpen() {
        Log.d(TAG, "Connected");
    }

    @Override
    public void OnClose() {
        Log.d(TAG, "Disconnected");
    }

    @Override
    public void OnError() {

    }

    @Override
    public void OnEvents(JSONObject json) {
        Log.d(TAG, "EVENT " + json.toString());
    }

    @Override
    public void OnEventsOp(JSONObject json) {
        Log.d(TAG, "OP " + json.toString());
    }
}
