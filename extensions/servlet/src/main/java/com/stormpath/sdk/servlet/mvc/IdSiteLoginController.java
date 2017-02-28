/*
 * Copyright 2017 Stormpath, Inc.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @since 1.5.0
 */
public class IdSiteLoginController extends IdSiteController {

    protected WebHandler preLoginHandler;

    public void setPreLoginHandler(WebHandler preLoginHandler) {
        this.preLoginHandler = preLoginHandler;
    }

    @Override
    protected ViewModel doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (preLoginHandler != null) {
            if (!preLoginHandler.handle(request, response, null)) {
                return null;
            }
        }

        return super.doGet(request, response);
    }
}
