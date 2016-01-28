/***
 IRequest

 Copyright (C) 2015 Giovanni Amati

 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/

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
