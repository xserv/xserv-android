package com.mi.xserv;

import org.json.JSONObject;

public interface OnEventListener {

    void OnOpen(JSONObject json);

    void OnClose(JSONObject json);

    void OnError(JSONObject json);

    void OnEvents(JSONObject json);

    void OnEventsOp(JSONObject json);
}
