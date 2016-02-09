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
package com.stormpath.sdk.servlet.mvc;

import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.authc.AuthenticationRequest;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.oauth.AccessTokenResult;
import com.stormpath.sdk.oauth.Authenticators;
import com.stormpath.sdk.oauth.OauthGrantAuthenticationResult;
import com.stormpath.sdk.oauth.PasswordGrantRequest;
import com.stormpath.sdk.oauth.RefreshGrantRequest;
import com.stormpath.sdk.resource.ResourceException;
import com.stormpath.sdk.servlet.authc.FailedAuthenticationRequestEvent;
import com.stormpath.sdk.servlet.authc.SuccessfulAuthenticationRequestEvent;
import com.stormpath.sdk.servlet.authc.impl.DefaultFailedAuthenticationRequestEvent;
import com.stormpath.sdk.servlet.authc.impl.DefaultSuccessfulAuthenticationRequestEvent;
import com.stormpath.sdk.servlet.authz.RequestAuthorizer;
import com.stormpath.sdk.servlet.event.RequestEvent;
import com.stormpath.sdk.servlet.event.impl.Publisher;
import com.stormpath.sdk.servlet.filter.oauth.AccessTokenAuthenticationRequestFactory;
import com.stormpath.sdk.servlet.filter.oauth.AccessTokenResultFactory;
import com.stormpath.sdk.servlet.filter.oauth.OauthErrorCode;
import com.stormpath.sdk.servlet.filter.oauth.OauthException;
import com.stormpath.sdk.servlet.filter.oauth.RefreshTokenAuthenticationRequestFactory;
import com.stormpath.sdk.servlet.filter.oauth.RefreshTokenResultFactory;
import com.stormpath.sdk.servlet.http.Saver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @since 1.0.RC4
 */
public class AccessTokenController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(AccessTokenController.class);

    private static final String PASSWORD_GRANT_TYPE = "password";

    private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";

    private static final String GRANT_TYPE_PARAM_NAME = "grant_type";

    private RefreshTokenResultFactory refreshTokenResultFactory;
    private RefreshTokenAuthenticationRequestFactory refreshTokenAuthenticationRequestFactory;
    private RequestAuthorizer requestAuthorizer;
    private AccessTokenAuthenticationRequestFactory authenticationRequestFactory;
    private AccessTokenResultFactory resultFactory;
    private Saver<AuthenticationResult> accountSaver;
    private Publisher<RequestEvent> eventPublisher;

    public RequestAuthorizer getRequestAuthorizer() {
        return requestAuthorizer;
    }

    public void setRequestAuthorizer(RequestAuthorizer requestAuthorizer) {
        this.requestAuthorizer = requestAuthorizer;
    }

    public AccessTokenAuthenticationRequestFactory getAccessTokenAuthenticationRequestFactory() {
        return authenticationRequestFactory;
    }

    public void setAccessTokenAuthenticationRequestFactory(AccessTokenAuthenticationRequestFactory authenticationRequestFactory) {
        this.authenticationRequestFactory = authenticationRequestFactory;
    }

    public AccessTokenResultFactory getAccessTokenResultFactory() {
        return resultFactory;
    }

    public void setAccessTokenResultFactory(AccessTokenResultFactory resultFactory) {
        this.resultFactory = resultFactory;
    }

    public Saver<AuthenticationResult> getAccountSaver() {
        return accountSaver;
    }

    public void setAccountSaver(Saver<AuthenticationResult> accountSaver) {
        this.accountSaver = accountSaver;
    }

    public Publisher<RequestEvent> getEventPublisher() {
        return eventPublisher;
    }

    public void setEventPublisher(Publisher<RequestEvent> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * @since 1.0.RC8.3
     */
    public RefreshTokenResultFactory getRefreshTokenResultFactory() {
        return refreshTokenResultFactory;
    }

    /**
     * @since 1.0.RC8.3
     */
    public void setRefreshTokenResultFactory(RefreshTokenResultFactory refreshTokenResultFactory) {
        this.refreshTokenResultFactory = refreshTokenResultFactory;
    }

    /**
     * @since 1.0.RC8.3
     */
    public RefreshTokenAuthenticationRequestFactory getRefreshTokenAuthenticationRequestFactory() {
        return refreshTokenAuthenticationRequestFactory;
    }

    /**
     * @since 1.0.RC8.3
     */
    public void setRefreshTokenAuthenticationRequestFactory(RefreshTokenAuthenticationRequestFactory refreshTokenAuthenticationRequestFactory) {
        this.refreshTokenAuthenticationRequestFactory = refreshTokenAuthenticationRequestFactory;
    }

    public void init() {
        Assert.notNull(refreshTokenResultFactory, "refreshTokenResultFactory cannot be null.");
        Assert.notNull(refreshTokenAuthenticationRequestFactory, "refreshTokenAuthenticationRequestFactory cannot be null.");
        Assert.notNull(requestAuthorizer, "requestAuthorizer cannot be null.");
        Assert.notNull(authenticationRequestFactory, "accessTokenAuthenticationRequestFactory cannot be null.");
        Assert.notNull(resultFactory, "accessTokenResultFactory cannot be null.");
        Assert.notNull(accountSaver, "accountSaver cannot be null.");
        Assert.notNull(eventPublisher, "eventPublisher cannot be null.");
    }

    protected void publish(RequestEvent e) {
        getEventPublisher().publish(e);
    }

    protected Application getApplication(HttpServletRequest request) {
        Application application = (Application)request.getAttribute(Application.class.getName());
        Assert.notNull(application, "request must have an application attribute.");
        return application;
    }

    /**
     * @since 1.0.RC8.3
     */
    private AccessTokenResult tokenAuthenticationRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        OauthGrantAuthenticationResult authenticationResult;

        try {
            Application app = getApplication(request);
            PasswordGrantRequest passwordGrantRequest = createPasswordGrantAuthenticationRequest(request);

            authenticationResult = Authenticators.PASSWORD_GRANT_AUTHENTICATOR
                    .forApplication(app)
                    .authenticate(passwordGrantRequest);
        } catch (ResourceException e) {
            log.debug("Unable to authenticate access token request: " + e.getMessage(), e);
            throw new OauthException(OauthErrorCode.INVALID_REQUEST, "Unable to authenticate access token request: ", e.getDeveloperMessage());
        }

        return createAccessTokenResult(request, response, authenticationResult);
    }

    /**
     * @since 1.0.RC8.3
     */
    private AccessTokenResult refreshTokenAuthenticationRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        OauthGrantAuthenticationResult authenticationResult;

        try {
            Application app = getApplication(request);
            RefreshGrantRequest refreshGrantRequest = createRefreshTokenAuthenticationRequest(request);

            authenticationResult = Authenticators.REFRESH_GRANT_AUTHENTICATOR
                    .forApplication(app)
                    .authenticate(refreshGrantRequest);
        } catch (ResourceException e) {
            log.debug("Unable to authenticate refresh token request: " + e.getMessage(), e);
            throw new OauthException(OauthErrorCode.INVALID_REQUEST, "Unable to authenticate refresh token request: ", e.getDeveloperMessage());
        }

        return createRefreshTokenResult(request, response, authenticationResult);
    }

    @Override
    protected ViewModel doPost(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String json;

        AuthenticationRequest authcRequest = null;
        AccessTokenResult result = null;

        try {

            assertAuthorized(request, response);
            String grantType = null;

            try {
                grantType = request.getParameter(GRANT_TYPE_PARAM_NAME);
                Assert.hasText(grantType, "grant_type must not be null or empty.");
            } catch (IllegalArgumentException e){
                throw new OauthException(OauthErrorCode.INVALID_GRANT);
            }

            if (grantType.equals(PASSWORD_GRANT_TYPE)) {
                result = this.tokenAuthenticationRequest(request, response);
            } else if (grantType.equals(REFRESH_TOKEN_GRANT_TYPE)) {
                result = this.refreshTokenAuthenticationRequest(request, response);
            } else {
                throw new OauthException(OauthErrorCode.UNSUPPORTED_GRANT_TYPE);
            }

            saveResult(request, response, result);

            json = result.getTokenResponse().toJson();

            response.setStatus(HttpServletResponse.SC_OK);

            SuccessfulAuthenticationRequestEvent e = createSuccessEvent(request, response, authcRequest, result);
            publish(e);

        } catch (OauthException e) {

            log.debug("OAuth Access Token request failed.", e);

            json = e.toJson();

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            try {
                FailedAuthenticationRequestEvent evt =
                        new DefaultFailedAuthenticationRequestEvent(request, response, authcRequest, e);
                publish(evt);
            } catch (Throwable t) {
                log.warn("Unable to publish failed authentication request event due to exception: {}.  " +
                        "Ignoring and handling original authentication exception {}.", t, e);
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Content-Length", String.valueOf(json.length()));
        response.getWriter().print(json);
        response.getWriter().flush();

        //we rendered the response directly - no need for a view to be resolved, so return null:
        return null;
    }

    protected SuccessfulAuthenticationRequestEvent createSuccessEvent(HttpServletRequest request,
                                                                      HttpServletResponse response,
                                                                      AuthenticationRequest authcRequest,
                                                                      AuthenticationResult result) {
        return new DefaultSuccessfulAuthenticationRequestEvent(request, response, authcRequest, result);
    }

    protected void saveResult(HttpServletRequest request, HttpServletResponse response, AuthenticationResult result) {
        getAccountSaver().set(request, response, result);
    }

    /**
     * @since 1.0.RC8.3
     */
    protected RefreshGrantRequest createRefreshTokenAuthenticationRequest(HttpServletRequest request) throws OauthException {
        return getRefreshTokenAuthenticationRequestFactory().createRefreshTokenAuthenticationRequest(request);
    }

    /**
     * @since 1.0.RC8.3
     */
    protected PasswordGrantRequest createPasswordGrantAuthenticationRequest(HttpServletRequest request) throws OauthException {
        return getAccessTokenAuthenticationRequestFactory().createAccessTokenAuthenticationRequest(request);
    }

    /**
     * @since 1.0.RC8.3
     */
    protected AccessTokenResult createRefreshTokenResult(final HttpServletRequest request,
                                                         final HttpServletResponse response,
                                                         final OauthGrantAuthenticationResult result) {
        return getRefreshTokenResultFactory().createRefreshTokenResult(request, response, result);
    }

    protected void assertAuthorized(HttpServletRequest request, HttpServletResponse response)
        throws OauthException {
        getRequestAuthorizer().assertAuthorized(request, response);
    }

    /**
     * @since 1.0.RC8.3
     */
    protected AccessTokenResult createAccessTokenResult(final HttpServletRequest request,
                                                        final HttpServletResponse response,
                                                        final OauthGrantAuthenticationResult result) {
        return getAccessTokenResultFactory().createAccessTokenResult(request, response, result);
    }

}
