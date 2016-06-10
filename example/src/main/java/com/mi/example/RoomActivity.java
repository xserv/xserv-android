package com.mi.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class RoomActivity extends AppCompatActivity {
    private final static String TAG = "RoomActivity";
    private EditText mRoomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(getString(R.string.app_name_full));

        mRoomName = (EditText) findViewById(R.id.roomName);
    }

    public void joinRoom(View view) {
        String roomName = mRoomName.getText().toString();

        if (roomName.length() > 0) {
            Intent intent = new Intent(this, MainActivity.class);
            Bundle b = new Bundle();
            b.putString("roomName", roomName);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

}
