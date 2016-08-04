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

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.ds.DataStore;
import com.stormpath.sdk.impl.account.DefaultAccount;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.oauth.AccessToken;
import com.stormpath.sdk.oauth.OAuthBearerRequestAuthentication;
import com.stormpath.sdk.oauth.OAuthBearerRequestAuthenticationResult;
import com.stormpath.sdk.oauth.OAuthBearerRequestAuthenticator;
import com.stormpath.sdk.oauth.OAuthRequestAuthentication;
import com.stormpath.sdk.resource.ResourceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.0.RC7
 */
public class DefaultOAuthBearerRequestAuthenticator extends AbstractOAuthRequestAuthenticator implements OAuthBearerRequestAuthenticator {

    protected final static String APPLICATION_PATH = "/applications/";
    protected final static String OAUTH_TOKEN_PATH = "/authTokens/";
    protected final static String ACCESS_TOKEN_PATH = "/accessTokens/";

    protected Boolean isLocalValidation = false;

    public DefaultOAuthBearerRequestAuthenticator(Application application, DataStore dataStore) {
        super(application, dataStore);
    }

    public OAuthBearerRequestAuthenticator withLocalValidation() {
        this.isLocalValidation = Boolean.TRUE;
        return this;
    }

    @Override
    public OAuthBearerRequestAuthenticationResult authenticate(OAuthRequestAuthentication authenticationRequest) {
        Assert.notNull(application, "application cannot be null or empty");
        Assert.isInstanceOf(OAuthBearerRequestAuthentication.class, authenticationRequest, "authenticationRequest must be an instance of JwtAuthenticationRequest.");

        OAuthBearerRequestAuthentication bearerRequest = (OAuthBearerRequestAuthentication) authenticationRequest;

        if (this.isLocalValidation) {
            String apiKeySecret = dataStore.getApiKey().getSecret();
            try {

                // During parsing, the JWT is validated for expiration, signature and tampering
                Claims claims = Jwts.parser()
                        .setSigningKey(apiKeySecret.getBytes("UTF-8"))
                        .parseClaimsJws(bearerRequest.getJwt()).getBody();
                Assert.isTrue(claims.getIssuer().equals(application.getHref()));

                Map<String, Object> properties = new HashMap<String, Object>();

                Account account = dataStore.getResource(claims.getSubject(), Account.class);

                String accessTokenHref = application.getHref().replace(APPLICATION_PATH, ACCESS_TOKEN_PATH);
                int accessTokenIdStartingPoint = accessTokenHref.lastIndexOf("/") + 1;
                accessTokenHref = accessTokenHref.substring(0, accessTokenIdStartingPoint);
                accessTokenHref = accessTokenHref + claims.getId();

                properties.put(DefaultAccount.HREF_PROP_NAME, accessTokenHref);
                properties.put(DefaultAccessToken.ACCOUNT_PROP_NAME, account);
                properties.put(DefaultAccessToken.APPLICATION_PROP_NAME, application);
                properties.put(DefaultAccessToken.JWT_PROP_NAME, bearerRequest.getJwt());
                properties.put(DefaultAccessToken.TENANT_PROP_NAME, application.getTenant());

                AccessToken accessToken = new DefaultAccessToken(dataStore, properties);

                OAuthBearerRequestAuthenticationResultBuilder builder = new DefaultOAuthBearerRequestAuthenticationResultBuilder(accessToken);
                return builder.build();

            } catch (Exception e) {
                throw new JwtException("JWT failed validation; it cannot be trusted.");
            }
        }

        StringBuilder stringBuilder = new StringBuilder(application.getHref());
        stringBuilder.append(OAUTH_TOKEN_PATH);
        stringBuilder.append(bearerRequest.getJwt());
        AccessToken accessToken = null;

        try {
            accessToken = dataStore.getResource(stringBuilder.toString(), AccessToken.class);
        } catch (ResourceException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("JWT failed validation; it cannot be trusted.");
        }

        OAuthBearerRequestAuthenticationResultBuilder builder = new DefaultOAuthBearerRequestAuthenticationResultBuilder(accessToken);
        return builder.build();
    }

}
