package com.mi.xserv;

import org.json.JSONObject;

public interface OnXservEventListener {

    void OnOpen();

    void OnClose(Exception e);

    void OnError(Exception e);

    void OnEvents(JSONObject json);

    void OnOps(JSONObject json);
}
