/*
 * Copyright 2015 Stormpath, Inc.
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
package com.stormpath.sdk.servlet.filter.oauth;

import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.impl.oauth.authz.DefaultTokenResponse;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.oauth.AccessTokenResult;
import com.stormpath.sdk.oauth.OAuthGrantRequestAuthenticationResult;
import com.stormpath.sdk.oauth.TokenResponse;
import org.apache.oltu.oauth2.common.message.types.TokenType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @since 1.0.RC3
 */
public class DefaultAccessTokenResultFactory implements AccessTokenResultFactory {

    private final Application application;

    public DefaultAccessTokenResultFactory(Application application) {
        Assert.notNull(application, "Application argument cannot be null.");
        this.application = application;
    }

    protected Application getApplication() {
        return this.application;
    }

    @Override
    public AccessTokenResult createAccessTokenResult(HttpServletRequest request, HttpServletResponse response, OAuthGrantRequestAuthenticationResult result) {
        final TokenResponse tokenResponse =
                DefaultTokenResponse.tokenType(TokenType.BEARER)
                        .accessToken(result.getAccessTokenString())
                        .refreshToken(result.getRefreshTokenString())
                        .applicationHref(application.getHref())
                        .expiresIn(String.valueOf(result.getExpiresIn())).build();
        return new PasswordGrantAccessTokenResult(result.getAccessToken().getAccount(), tokenResponse);
    }
}
