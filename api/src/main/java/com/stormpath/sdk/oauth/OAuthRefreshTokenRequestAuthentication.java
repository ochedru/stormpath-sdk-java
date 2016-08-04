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
package com.stormpath.sdk.oauth;

/**
 * This class represents a request for Stormpath to create new OAuth 2.0 access tokens for a previously authenticated account.
 *
 * @since 1.0.RC7
 */
public interface OAuthRefreshTokenRequestAuthentication extends OAuthGrantRequestAuthentication {

    /**
     * Returns the String denoting the <a href="https://en.wikipedia.org/wiki/JSON_Web_Token">Json Web Token</a> used to generate new Oauth 2.0 access tokens.
     *
     * @return a String value denoting the <a href="https://en.wikipedia.org/wiki/JSON_Web_Token">Json Web Token</a> used to generate new Oauth 2.0 access tokens.
     */
    String getRefreshToken();

}
