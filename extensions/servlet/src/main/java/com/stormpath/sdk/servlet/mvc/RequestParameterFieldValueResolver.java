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
package com.stormpath.sdk.servlet.mvc;

import com.stormpath.sdk.lang.Strings;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 1.0.0
 */
public class RequestParameterFieldValueResolver implements RequestFieldValueResolver {

    @Override
    public String getValue(HttpServletRequest request, String fieldName) {
        String val = request.getParameter(fieldName);
        return Strings.clean(val);
    }

    @Override
    public Map<String, Object> getAllFields(HttpServletRequest request) {
        //If it was a form POST we don't need to know all the fields that where posted
        return new LinkedHashMap<String, Object>();
    }
}
