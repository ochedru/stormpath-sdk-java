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
package com.stormpath.sdk.servlet.filter.account;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.http.Resolver;
import com.stormpath.sdk.servlet.http.impl.StormpathHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @since 1.0.RC3
 */
public class SessionAccountResolver implements Resolver<Account> {

    @Override
    public Account get(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Account account = (Account) session.getAttribute(Account.class.getName());

        if (account != null) {
            request.setAttribute(StormpathHttpServletRequest.AUTH_TYPE_REQUEST_ATTRIBUTE_NAME, HttpServletRequest.FORM_AUTH);
        }

        return account;
    }
}
