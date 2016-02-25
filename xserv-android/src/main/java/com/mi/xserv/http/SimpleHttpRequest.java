/***
 * SimpleHttpRequest
 * <p/>
 * Copyright (C) 2015 Giovanni Amati
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/

package com.mi.xserv.http;

import java.util.HashMap;
import java.util.Set;

import javax.net.ssl.SSLContext;

public class SimpleHttpRequest implements IRequest {
    public static final String POST = "post";
    public static final String GET = "get";
    private final HashMap<String, String> params;
    private String mUserAgent;
    private String mMethod;
    private String mContentType;
    private String mUrl;
    // TLS
    private boolean mSecure;
    private SSLContext mSSLContext;

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

    @Override
    public boolean getSecure() {
        return mSecure;
    }

    @Override
    public void setSecure(boolean secure) {
        mSecure = secure;
    }

    @Override
    public SSLContext getSSLContext() {
        return mSSLContext;
    }

    @Override
    public void setSSLContext(SSLContext sslContext) {
        mSSLContext = sslContext;
    }

}
