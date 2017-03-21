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
package com.stormpath.sdk.servlet.config.filter;

import com.stormpath.sdk.servlet.config.Config;
import com.stormpath.sdk.servlet.mvc.IdSiteController;
import com.stormpath.sdk.servlet.mvc.IdSiteLoginController;

/**
 * @since 1.5.0
 */
public class IDSiteLoginFilterFactory extends AbstractIDSiteFilterFactory<IdSiteLoginController> {

    @Override
    protected IdSiteLoginController newController() {
        return new IdSiteLoginController();
    }

    public void doConfigure(IdSiteLoginController controller, Config config) {
        controller.setIdSiteUri(config.get("stormpath.web.idSite.loginUri"));
        controller.setNextUri(config.get("stormpath.web.login.nextUri"));
        controller.setPreLoginHandler(config.getLoginPreHandler());
    }
}