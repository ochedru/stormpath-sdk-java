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
package com.stormpath.sdk.impl.oauth.authc;

import com.stormpath.sdk.api.ApiAuthenticationResult;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.authc.AuthenticationRequest;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.http.HttpRequest;
import com.stormpath.sdk.impl.application.DefaultApplication;
import com.stormpath.sdk.impl.authc.ApiAuthenticationRequestFactory;
import com.stormpath.sdk.impl.oauth.http.OauthHttpServletRequest;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.oauth.OauthRefreshGrantAuthenticator;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0.RC7
 */
public class DefaultOauthRefreshGrantAuthenticator implements OauthRefreshGrantAuthenticator {

    protected Application application;

    private static final ApiAuthenticationRequestFactory FACTORY = new ApiAuthenticationRequestFactory();

    public DefaultOauthRefreshGrantAuthenticator (DefaultApplication application){
        this.application = application;
    }

    private static final String HTTP_REQUEST_NOT_SUPPORTED_MSG = "HttpRequest class [%s] is not supported. Supported class: [%s].";

    private HttpServletRequest httpServletRequest;

    @Override
    public ApiAuthenticationResult authenticate(HttpRequest httpRequest) {

        Class httpRequestClass = httpRequest.getClass();

        if (HttpServletRequest.class.isAssignableFrom(httpRequestClass)) {
            this.httpServletRequest = (HttpServletRequest) httpRequest;
        } else if (HttpRequest.class.isAssignableFrom(httpRequestClass)) {
            this.httpServletRequest = new OauthHttpServletRequest(httpRequest);
        } else {
            throw new IllegalArgumentException(String.format(HTTP_REQUEST_NOT_SUPPORTED_MSG, httpRequest.getClass(), HttpRequest.class.getName(), HttpServletRequest.class.getName()));
        }

        application.validateJwtSignature(httpServletRequest.getParameter("refresh_token"));

        AuthenticationRequest request = FACTORY.createFrom(httpRequest);
        AuthenticationResult result = application.authenticateAccount(request);
        Assert.isInstanceOf(ApiAuthenticationResult.class, result);
        return (ApiAuthenticationResult) result;
    }

    @Override
    public ApiAuthenticationResult execute() {
        throw new UnsupportedOperationException("execute() method is not supported for this class");
    }
}
