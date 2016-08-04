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

import com.stormpath.sdk.lang.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @since 1.0.RC4
 */
public class LogoutController extends AbstractController {

    private boolean invalidateHttpSession = true;

    public boolean isInvalidateHttpSession() {
        return invalidateHttpSession;
    }

    public void setInvalidateHttpSession(boolean invalidateHttpSession) {
        this.invalidateHttpSession = invalidateHttpSession;
    }

    public void init() {
        Assert.hasText(nextUri, "nextUri must be configured.");
        Assert.notNull(produces, "produces cannot be null.");
    }

    @Override
    public boolean isNotAllowedIfAuthenticated() {
        return false;
    }

    @Override
    protected ViewModel doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //This Logout Controller does not respond to GET requests (only POST) but
        //IDSIte Logout is a GET request, therefore we need to respond to it.
        if (request.getAttribute("idSiteResult.FILTERED") != null) {
            return processRequest(request, response);
        }
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return null;
    }

    @Override
    protected ViewModel doPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return processRequest(request, response);

    }

    protected ViewModel processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //clear out any authentication/account state:
        request.logout();

        //it is a security risk to not terminate a session (if one exists) on logout:
        HttpSession session = request.getSession(false);
        if (session != null && isInvalidateHttpSession()) {
            session.invalidate();
        }

        if (isHtmlPreferred(request, response)) {
            return new DefaultViewModel(nextUri).setRedirect(true);
        } else {
            //probably an ajax or non-browser client - return 200 ok:
            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        }
    }
}
