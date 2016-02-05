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
package com.stormpath.sdk.oauth;

import com.stormpath.sdk.lang.Classes;

/**
 * Static utility/helper class serving {@link com.stormpath.sdk.oauth.Oauth2AuthenticatorFactory Oauth2AuthenticatorFactory}s. For example, to
 * construct a {@link com.stormpath.sdk.oauth.PasswordGrantRequest PasswordGrantRequest}:
 * <pre>
 *      PasswordGrantRequest createRequest = Oauth2Requests.PASSWORD_GRANT_REQUEST.builder()
 *              .setLogin(email)
 *              .setPassword(password)
 *              .build();
 *      Oauth2AuthenticationResult result = Authenticators.PASSWORD_GRANT_AUTHENTICATOR.forApplication(app).authenticate(createRequest);
 * </pre>
 * Once your application receives the result, the first thing to do is to validate that the token is valid. There are different ways you can complete this task.
 * The benefit of using Stormpath to validate the token through the REST API is that Stormpath can validate the token against the state of your application
 * and account. To illustrate the difference:
 * <table summary="JWT validation">
 *   <tr>
 *      <td>Validation Criteria</td><td>Locally</td><td>Stormpath</td>
 *   <tr/>
 *   <tr>
 *      <td>Token hasn’t been tampered with</td><td>yes</td><td>yes</td>
 *   </tr>
 *   <tr>
 *      <td>Token hasn’t expired</td><td>yes</td><td>yes</td>
 *   </tr>
 *   <tr>
 *      <td>Token hasn’t been revoked</td><td>no</td><td>yes</td>
 *   </tr>
 *   <tr>
 *      <td>Account hasn’t been disabled, and hasn’t been deleted</td><td>no</td><td>yes</td>
 *   </tr>
 *   <tr>
 *      <td>Issuer is Stormpath</td><td>yes</td><td>yes</td>
 *   </tr>
 *   <tr>
 *      <td>Issuing application is still enabled, and hasn’t been deleted</td><td>no</td><td>yes</td>
 *   </tr>
 *   <tr>
 *      <td>Account is still in an account store for the issuing application</td><td>no</td><td>yes</td>
 *   </tr>
 * </table>
 * <h2>Using Stormpath to Validate Tokens</h2>
 * <pre>
 * JwtAuthenticationRequest authRequest = Oauth2Requests.JWT_AUTHENTICATION_REQUEST.builder().setJwt(grantResult.getAccessTokenString()).build();
 * JwtAuthenticationResult authResultRemote = Authenticators.JWT_AUTHENTICATOR.forApplication(app).authenticate(authRequest);
 * </pre>
 * <h2>Validating the Token Locally</h2>
 * <pre>
 * JwtAuthenticationRequest authRequest = Oauth2Requests.JWT_AUTHENTICATION_REQUEST.builder().setJwt(grantResult.getAccessTokenString()).build();
 * JwtAuthenticationResult authResultRemote = Authenticators.JWT_AUTHENTICATOR.forApplication(app).withLocalValidation().authenticate(authRequest);
 * </pre>
 * <h2>Refreshing Access Tokens</h2>
 * <p>
 * Passing access tokens allows access to resources in your application. But what happens when the Access Token expires? You could require the user to authenticate again,
 * or use the Refresh Token to get a new Access Token without requiring credentials.
 * </p>
 * <p>To get a new Access Token to for a Refresh Token, you must first make sure that the application {@link com.stormpath.sdk.oauth.OauthPolicy#setRefreshTokenTtl(String)
 * has been configured to generate a Refresh Token} in the OAuth 2.0 Access Token Response.</p>
 * <p>A refresh token is obtained this way:</p>
 * <pre>
 * RefreshGrantRequest request = Oauth2Requests.REFRESH_GRANT_REQUEST.builder().setRefreshToken(result.getRefreshTokenString()).build();
 * OauthGrantAuthenticationResult result = Authenticators.REFRESH_GRANT_AUTHENTICATOR.forApplication(app).authenticate(request);
 * </pre>
 *
 * @see com.stormpath.sdk.oauth.OauthPolicy
 *
 * @since 1.0.RC7
 */
public class Authenticators {

    private Authenticators() {
    }

    /**
     * Constructs {@link PasswordGrantAuthenticator}s.
     */
    public static final PasswordGrantAuthenticatorFactory PASSWORD_GRANT_AUTHENTICATOR =
            (PasswordGrantAuthenticatorFactory) Classes.newInstance("com.stormpath.sdk.impl.oauth.DefaultPasswordGrantAuthenticatorFactory");

    /**
     * Constructs {@link RefreshGrantAuthenticator}s.
     */
    public static final RefreshGrantAuthenticatorFactory REFRESH_GRANT_AUTHENTICATOR =
            (RefreshGrantAuthenticatorFactory) Classes.newInstance("com.stormpath.sdk.impl.oauth.DefaultRefreshGrantAuthenticatorFactory");

    /**
     * Constructs {@link JwtAuthenticator}s.
     */
    public static final JwtAuthenticatorFactory JWT_AUTHENTICATOR =
            (JwtAuthenticatorFactory) Classes.newInstance("com.stormpath.sdk.impl.oauth.DefaultJwtAuthenticatorFactory");

    /**
     * Constructs {@link IdSiteAuthenticator}s.
     * @since 1.0.RC8.2
     */
    public static final IdSiteAuthenticatorFactory ID_SITE_AUTHENTICATOR =
            (IdSiteAuthenticatorFactory) Classes.newInstance("com.stormpath.sdk.impl.oauth.DefaultIdSiteAuthenticatorFactory");

}

