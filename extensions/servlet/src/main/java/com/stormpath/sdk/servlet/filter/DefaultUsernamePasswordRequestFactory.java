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

import com.stormpath.sdk.authc.AuthenticationRequest;
import com.stormpath.sdk.authc.UsernamePasswordRequestBuilder;
import com.stormpath.sdk.authc.UsernamePasswordRequests;
import com.stormpath.sdk.directory.AccountStore;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.servlet.http.authc.AccountStoreResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @since 1.0.RC3
 */
public class DefaultUsernamePasswordRequestFactory implements UsernamePasswordRequestFactory {

    private AccountStoreResolver accountStoreResolver;

    public DefaultUsernamePasswordRequestFactory(
            AccountStoreResolver accountStoreResolver) {
        Assert.notNull(accountStoreResolver, "AccountStoreResolver cannot be null.");
        this.accountStoreResolver = accountStoreResolver;
    }

    protected AccountStoreResolver getAccountStoreResolver() {
        return this.accountStoreResolver;
    }

    @Override
    public AuthenticationRequest createUsernamePasswordRequest(HttpServletRequest request, HttpServletResponse response,
                                                               String username, String password) {
        AccountStore accountStore =
                getAccountStoreResolver().getAccountStore(request, response);

        UsernamePasswordRequestBuilder builder = UsernamePasswordRequests.builder()
                .setUsernameOrEmail(username)
                .setPassword(password)
                .setHost(request.getRemoteHost());

        if (accountStore != null) {
            builder.inAccountStore(accountStore);
        }

        return builder.build();
    }
}
