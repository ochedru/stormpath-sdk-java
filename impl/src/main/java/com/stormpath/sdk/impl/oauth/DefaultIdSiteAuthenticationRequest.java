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
package com.stormpath.sdk.impl.oauth;

import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.oauth.IdSiteAuthenticationRequest;

/**
 * @since 1.0.RC8.2
 */
public class DefaultIdSiteAuthenticationRequest implements IdSiteAuthenticationRequest {

    private final static String grant_type = "stormpath_token";
    private final String token;

    public DefaultIdSiteAuthenticationRequest(String token) {
        Assert.notNull(token, "token argument cannot be null.");
        this.token = token;
    }

    @Override
    public String getGrantType() {
        return grant_type;
    }

    @Override
    public String getToken() {
        return token;
    }
}