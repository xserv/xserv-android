package com.mi.xserv;

import org.json.JSONObject;

public interface OnXservEventListener {

    void OnOpenConnection();

    void OnCloseConnection(Exception e);

    void OnErrorConnection(Exception e);

    void OnReceiveEvents(JSONObject json);

    void OnReceiveOpsResponse(JSONObject json);
}
