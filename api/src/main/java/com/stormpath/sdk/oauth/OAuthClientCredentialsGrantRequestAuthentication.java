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

import com.stormpath.sdk.api.ApiKey;
import com.stormpath.sdk.directory.AccountStore;

/**
 * This class represents a request for Stormpath to authenticate an Account and exchange its apiKey for a valid OAuth 2.0 access token.
 *
 * @since 1.0.0
 */
public interface OAuthClientCredentialsGrantRequestAuthentication extends OAuthGrantRequestAuthentication {

    /**
     * Returns the apiKey specified for the authentication and token creation.
     *
     * @return the apiKey corresponding to the apiKey specified to authenticate the account and create the access token.
     */
    ApiKey getApiKey();

    /**
     * Returns the specific {@link AccountStore accountStore} where the provided credentials will be sought in order to authenticate a request.
     *
     * @return the specific {@link AccountStore accountStore} where the provided credentials will be sought in order to authenticate a request.
     */
    AccountStore getAccountStore();

}
