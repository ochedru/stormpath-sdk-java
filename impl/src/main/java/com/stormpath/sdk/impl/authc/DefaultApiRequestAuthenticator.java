/*
 * Copyright 2014 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.impl.authc;

import com.stormpath.sdk.api.ApiAuthenticationResult;
import com.stormpath.sdk.api.ApiRequestAuthenticator;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.authc.AuthenticationRequest;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.http.HttpRequest;
import com.stormpath.sdk.impl.application.DefaultApplication;
import com.stormpath.sdk.lang.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 1.0.RC
 */
public class DefaultApiRequestAuthenticator implements ApiRequestAuthenticator {

    private static final ApiAuthenticationRequestFactory FACTORY = new ApiAuthenticationRequestFactory();

    private final Application application;

    private HttpRequest httpRequest;

    private static final Set<Class> HTTP_REQUEST_SUPPORTED_CLASSES;

    private static final String HTTP_REQUEST_NOT_SUPPORTED_MSG =
            "Class [%s] is not one of the supported http requests classes [%s].";

    static {
        Set<Class> supportedClasses = new HashSet<Class>();
        supportedClasses.add(HttpRequest.class);
        HTTP_REQUEST_SUPPORTED_CLASSES = supportedClasses;
    }

    public DefaultApiRequestAuthenticator(Application application, HttpRequest httpRequest) {

        Assert.notNull(application, "application argument cannot be null.");
        Assert.notNull(httpRequest, "httpRequest argument cannot be null.");

        this.application = application;
        this.httpRequest = httpRequest;
    }

    /**
     * @since 1.0.RC4.6
     */
    public DefaultApiRequestAuthenticator(DefaultApplication application) {
        Assert.notNull(application, "application argument cannot be null.");
        this.application = application;
    }

    protected ApiAuthenticationResult execute() {
        AuthenticationRequest request = FACTORY.createFrom(httpRequest);
        AuthenticationResult result = application.authenticateAccount(request);
        Assert.isInstanceOf(ApiAuthenticationResult.class, result);
        return (ApiAuthenticationResult) result;
    }

    /**
     * @since 1.0.RC4.6
     */
    @Override
    public ApiAuthenticationResult authenticate(HttpRequest httpRequest) {

        Assert.notNull(httpRequest, "httpRequest argument cannot be null.");
        this.httpRequest = httpRequest;

        return this.execute();
    }
}
