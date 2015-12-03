package com.mi.xserv.http;

public interface ITaskListener {

    void setOnResponseListener(OnResponseListener listener);

    void execute(IRequest req);

    interface OnResponseListener {
        void onResponse(String output);

        void onFail(String output);
    }
}
