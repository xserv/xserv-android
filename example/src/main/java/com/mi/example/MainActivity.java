package com.mi.example;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mi.xserv.OnXservEventListener;
import com.mi.xserv.Xserv;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnXservEventListener {
    private final static String TAG = "Example";

    private Xserv mXserv;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private ArrayList<JSONObject> mDataSource;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDataSource = new ArrayList<>();

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(mDataSource);
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mXserv.historyById("@milano", 0);
                // mXserv.disconnect();
                // mXserv.connect();
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mXserv.publish("@milano","test messaggio android privato");
                mXserv.publish("milano", "test messaggio android");
            }
        });

        mXserv = new Xserv("9Pf80-3");
        mXserv.setOnEventListener(this);

        mXserv.connect();
    }

    @Override
    public void OnOpenConnection() {
        Log.d(TAG, "Connected");

        JSONObject auth_endpoint = new JSONObject();
        try {
            auth_endpoint.put("user", "amatig");
            auth_endpoint.put("pass", "amatig");
        } catch (JSONException ignored) {
            // e.printStackTrace();
        }
        mXserv.subscribe("@milano", auth_endpoint);

        mXserv.subscribe("milano");
    }

    @Override
    public void OnCloseConnection(Exception e) {
        Log.d(TAG, "Disconnected");
    }

    @Override
    public void OnErrorConnection(Exception e) {

    }

    @Override
    public void OnReceiveMessages(final JSONObject json) {
        //Log.d(TAG, "EVENT " + json.toString());

        mDataSource.add(0, json);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnReceiveOpsResponse(JSONObject json) {
        Log.d(TAG, "OP " + json.toString());
    }

}
