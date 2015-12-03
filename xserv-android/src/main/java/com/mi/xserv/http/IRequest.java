package com.mi.xserv.http;

import java.util.Set;

public interface IRequest {

    String getUserAgent();

    void setUserAgent(String user_agent);

    String getUrl();

    void setUrl(String url);

    String getMethod();

    void setMethod(String method);

    String getContentType();

    void setContentType(String content_type);

    void setParam(String key, String value);

    String getParam(String key);

    Set<String> getParamsKey();

}
