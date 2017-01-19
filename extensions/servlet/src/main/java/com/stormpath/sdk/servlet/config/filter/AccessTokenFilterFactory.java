/*
 * Copyright 2016 Stormpath, Inc.
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
package com.stormpath.sdk.servlet.config.filter;

import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.servlet.authz.RequestAuthorizer;
import com.stormpath.sdk.servlet.config.Config;
import com.stormpath.sdk.servlet.event.RequestEvent;
import com.stormpath.sdk.servlet.event.impl.Publisher;
import com.stormpath.sdk.servlet.filter.oauth.AccessTokenAuthenticationRequestFactory;
import com.stormpath.sdk.servlet.filter.oauth.AccessTokenResultFactory;
import com.stormpath.sdk.servlet.filter.oauth.RefreshTokenAuthenticationRequestFactory;
import com.stormpath.sdk.servlet.filter.oauth.RefreshTokenResultFactory;
import com.stormpath.sdk.servlet.http.Saver;
import com.stormpath.sdk.servlet.http.authc.BasicAuthenticationScheme;
import com.stormpath.sdk.servlet.mvc.AccessTokenController;
import com.stormpath.sdk.servlet.util.GrantTypeValidator;

/**
 * @since 1.0.0
 */
public class AccessTokenFilterFactory extends ControllerFilterFactory<AccessTokenController> {

    protected static final String ACCESS_TOKEN_AUTHENTICATION_REQUEST_FACTORY =
        "stormpath.web.oauth2.authenticationRequestFactory";
    protected static final String ACCESS_TOKEN_RESULT_FACTORY = "stormpath.web.oauth2.resultFactory";
    protected static final String REFRESH_TOKEN_AUTHENTICATION_REQUEST_FACTORY =
        "stormpath.web.refreshToken.authenticationRequestFactory";
    protected static final String REFRESH_TOKEN_RESULT_FACTORY = "stormpath.web.refreshToken.resultFactory";
    protected static final String REQUEST_AUTHORIZER = "stormpath.web.oauth2.authorizer";
    protected static final String ACCOUNT_SAVER = "stormpath.web.authc.saver";
    protected static final String EVENT_PUBLISHER = "stormpath.web.request.event.publisher";
    protected static final String BASIC_AUTHENTICATION_REQUEST_FACTORY = "stormpath.web.http.authc.schemes.basic";

    @Override
    protected AccessTokenController newController() {
        return new AccessTokenController();
    }

    @Override
    protected void configure(AccessTokenController controller, Config config) throws Exception {

        AccessTokenAuthenticationRequestFactory accessTokenAuthenticationRequestFactory =
            config.getInstance(ACCESS_TOKEN_AUTHENTICATION_REQUEST_FACTORY);

        RefreshTokenAuthenticationRequestFactory refreshTokenAuthenticationRequestFactory =
            config.getInstance(REFRESH_TOKEN_AUTHENTICATION_REQUEST_FACTORY);

        RequestAuthorizer requestAuthorizer = config.getInstance(REQUEST_AUTHORIZER);
        AccessTokenResultFactory accessTokenResultFactory = config.getInstance(ACCESS_TOKEN_RESULT_FACTORY);
        RefreshTokenResultFactory refreshTokenResultFactory = config.getInstance(REFRESH_TOKEN_RESULT_FACTORY);
        Saver<AuthenticationResult> accountSaver = config.getInstance(ACCOUNT_SAVER);
        Publisher<RequestEvent> eventPublisher = config.getInstance(EVENT_PUBLISHER);
        BasicAuthenticationScheme basicAuthenticationScheme = config.getInstance(BASIC_AUTHENTICATION_REQUEST_FACTORY);
        GrantTypeValidator grantTypeValidator = config.getGrantTypeStatusValidator();

        controller.setEventPublisher(eventPublisher);
        controller.setAccessTokenAuthenticationRequestFactory(accessTokenAuthenticationRequestFactory);
        controller.setAccessTokenResultFactory(accessTokenResultFactory);
        controller.setRefreshTokenAuthenticationRequestFactory(refreshTokenAuthenticationRequestFactory);
        controller.setRefreshTokenResultFactory(refreshTokenResultFactory);
        controller.setAccountSaver(accountSaver);
        controller.setRequestAuthorizer(requestAuthorizer);
        controller.setBasicAuthenticationScheme(basicAuthenticationScheme);
        controller.setGrantTypeValidator(grantTypeValidator);
    }
}
