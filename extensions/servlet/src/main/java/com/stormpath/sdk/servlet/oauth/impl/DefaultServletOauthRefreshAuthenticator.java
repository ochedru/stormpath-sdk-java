/*
* Copyright 2015 Stormpath, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.stormpath.sdk.servlet.oauth.impl;

import com.stormpath.sdk.api.ApiAuthenticationResult;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.application.Applications;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.servlet.oauth.ServletOauthRefreshAuthenticator;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0.RC8
 */
public class DefaultServletOauthRefreshAuthenticator implements ServletOauthRefreshAuthenticator {

    private final Application application;

    private static final String HTTP_SERVLET_REQUEST_FQCN = "javax.servlet.http.HttpServletRequest";

    public DefaultServletOauthRefreshAuthenticator(Application application) {
        this.application = application;
    }

    @Override
    public ApiAuthenticationResult authenticate(HttpServletRequest httpRequest) {
        Assert.notNull(httpRequest, "httpRequest argument cannot be null.");
        Assert.isInstanceOf(javax.servlet.http.HttpServletRequest.class, httpRequest,
                "The specified httpRequest argument must be an instance of " + HTTP_SERVLET_REQUEST_FQCN);

        com.stormpath.sdk.impl.http.ServletHttpRequest stmpHttpRequest = new com.stormpath.sdk.impl.http.ServletHttpRequest(httpRequest);
        return Applications.oauthRefreshGrantAuthenticator(application).authenticate(stmpHttpRequest);
    }
}
