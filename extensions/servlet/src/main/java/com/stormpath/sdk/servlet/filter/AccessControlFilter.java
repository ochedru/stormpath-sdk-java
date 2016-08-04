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
package com.stormpath.sdk.servlet.filter;

import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.servlet.util.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @since 1.0.RC3
 */
public abstract class AccessControlFilter extends HttpFilter {

    private String loginUrl;
    private String accessTokenUrl;
    private UnauthenticatedHandler unauthenticatedHandler;
    private UnauthorizedHandler unauthorizedHandler;

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    public void setUnauthenticatedHandler(UnauthenticatedHandler unauthenticatedHandler) {
        this.unauthenticatedHandler = unauthenticatedHandler;
    }

    public void setUnauthorizedHandler(UnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Override
    protected void onInit() throws Exception {
        super.onInit();
        Assert.hasText(loginUrl, "loginUrl cannot be null or empty.");
        Assert.hasText(accessTokenUrl, "accessTokenUrl cannot be null or empty.");
        Assert.notNull(this.unauthenticatedHandler, "unauthenticatedHandler cannot be null.");
        Assert.notNull(this.unauthorizedHandler, "unauthorizedHandler cannot be null.");
    }

    public UnauthenticatedHandler getUnauthenticatedHandler() {
        return this.unauthenticatedHandler;
    }

    public UnauthorizedHandler getUnauthorizedHandler() {
        return this.unauthorizedHandler;
    }

    /**
     * Returns <code>true</code> if the request is allowed to proceed through the filter normally, or <code>false</code>
     * if the request should be handled by the {@link #onAccessDenied(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse) onAccessDenied} method instead.
     *
     * @param request  the incoming <code>ServletRequest</code>
     * @param response the outgoing <code>ServletResponse</code>
     * @return <code>true</code> if the request should proceed through the filter normally, <code>false</code> if the
     * request should be processed by this filter's {@link #onAccessDenied(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse) onAccessDenied} method instead.
     * @throws Exception if an error occurs during processing.
     */
    protected abstract boolean isAccessAllowed(HttpServletRequest request, HttpServletResponse response)
        throws Exception;

    /**
     * Processes requests where the subject was denied access as determined by the {@link
     * #isAccessAllowed(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     * isAccessAllowed}.
     *
     * @param request  the incoming <code>ServletRequest</code>
     * @param response the outgoing <code>ServletResponse</code>
     * @return <code>true</code> if the request should continue to be processed; false if the subclass will
     * handle/render the response directly.
     * @throws Exception if there is an error processing the request.
     * @since 1.0
     */
    protected abstract boolean onAccessDenied(HttpServletRequest request, HttpServletResponse response)
        throws Exception;

    @Override
    protected boolean isContinue(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return isAccessAllowed(request, response) || onAccessDenied(request, response);
    }

    protected boolean isLoginRequest(HttpServletRequest request) {
        String contextRelativeUri = ServletUtils.getContextRelativeUri(request);
        return loginUrl.equals(contextRelativeUri) || accessTokenUrl.equals(contextRelativeUri);
    }
}
