package com.mi.xserv.http;

import java.util.HashMap;
import java.util.Set;

public class SimpleHttpRequest implements IRequest {
    public static final String POST = "post";
    public static final String GET = "get";
    private final HashMap<String, String> params;
    private String mUserAgent;
    private String mMethod;
    private String mContentType;
    private String mUrl;

    private SimpleHttpRequest() {
        this.params = new HashMap<>();
    }

    public SimpleHttpRequest(String type, String url) {
        this();
        this.setUserAgent("");
        this.setMethod(type);
        this.setUrl(url);
    }

    @Override
    public String getUserAgent() {
        return this.mUserAgent;
    }

    @Override
    public void setUserAgent(String user_agent) {
        this.mUserAgent = user_agent;
    }

    @Override
    public String getUrl() {
        return this.mUrl;
    }

    @Override
    public void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    public String getMethod() {
        return this.mMethod;
    }

    @Override
    public void setMethod(String method) {
        this.mMethod = method;
    }

    @Override
    public String getContentType() {
        return this.mContentType;
    }

    @Override
    public void setContentType(String content_type) {
        this.mContentType = content_type;
    }

    @Override
    public void setParam(String key, String value) {
        this.params.put(key, value);
    }

    @Override
    public String getParam(String key) {
        return this.params.get(key);
    }

    @Override
    public Set<String> getParamsKey() {
        return this.params.keySet();
    }

}
