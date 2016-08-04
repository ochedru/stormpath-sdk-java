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
package com.stormpath.sdk.servlet.config.filter;

import com.stormpath.sdk.servlet.config.Config;
import com.stormpath.sdk.servlet.config.ConfigSingletonFactory;

import javax.servlet.Filter;
import javax.servlet.ServletContext;

/**
 * @param <T>
 * @since 1.0.0
 */
public abstract class FilterFactory<T extends Filter> extends ConfigSingletonFactory<T> {

    @Override
    protected T createInstance(ServletContext servletContext) throws Exception {
        return createInstance(servletContext, getConfig());
    }

    protected abstract T createInstance(ServletContext servletContext, Config config) throws Exception;
}
