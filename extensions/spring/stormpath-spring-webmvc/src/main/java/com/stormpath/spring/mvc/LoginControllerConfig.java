/*
 * Copyright 2016 Stormpath, Inc.
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
package com.stormpath.spring.mvc;

import com.stormpath.sdk.servlet.mvc.FormFieldsFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @since 1.0.0
 */
public class LoginControllerConfig extends AbstractSpringControllerConfig implements FormFieldsFactory {

    @Value("#{ @environment['stormpath.web.login.enabled'] ?: true }")
    private boolean loginEnabled;

    @Value("#{ @environment['stormpath.web.login.uri'] ?: '/login' }")
    private String loginUri;

    @Value("#{ @environment['stormpath.web.login.nextUri'] ?: '/' }")
    private String loginNextUri;

    @Value("#{ @environment['stormpath.web.login.view'] ?: 'stormpath/login' }")
    private String loginView;

    public LoginControllerConfig() {
        super("login");
        setDefaultFieldNames("login", "password");
    }

    @Override
    public String getView() {
        return loginView;
    }

    @Override
    public String getUri() {
        return loginUri;
    }

    @Override
    public String getNextUri() {
        return loginNextUri;
    }

    @Override
    public boolean isEnabled() {
        return loginEnabled;
    }
}
