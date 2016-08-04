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
package com.stormpath.sdk.servlet.filter.oauth;

import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.lang.Strings;
import com.stormpath.sdk.oauth.OAuthRefreshTokenRequestAuthentication;
import com.stormpath.sdk.oauth.OAuthRequests;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0.RC8.3
 */
public class DefaultRefreshTokenAuthenticationRequestFactory implements RefreshTokenAuthenticationRequestFactory {

    protected static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";

    @Override
    public OAuthRefreshTokenRequestAuthentication createRefreshTokenAuthenticationRequest(HttpServletRequest request) throws OAuthException {

        try {
            String refreshToken = Strings.clean(request.getParameter(REFRESH_TOKEN_GRANT_TYPE));
            Assert.hasText(refreshToken, "refreshToken must not be null or empty.");

            return OAuthRequests.OAUTH_REFRESH_TOKEN_REQUEST.builder()
                    .setRefreshToken(refreshToken)
                    .build();

        } catch (Exception e){
            throw new OAuthException(OAuthErrorCode.INVALID_REQUEST);
        }
    }
}