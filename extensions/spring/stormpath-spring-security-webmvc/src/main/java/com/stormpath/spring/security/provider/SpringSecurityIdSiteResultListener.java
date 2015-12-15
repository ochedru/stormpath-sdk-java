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
package com.stormpath.spring.security.provider;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.idsite.AuthenticationResult;
import com.stormpath.sdk.idsite.IdSiteResultListener;
import com.stormpath.sdk.idsite.LogoutResult;
import com.stormpath.sdk.idsite.RegistrationResult;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.spring.security.token.IdSiteAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @since 1.0.RC7
 */
public class SpringSecurityIdSiteResultListener implements IdSiteResultListener {
    private static final Logger logger = LoggerFactory.getLogger(SpringSecurityIdSiteResultListener.class);

    protected StormpathAuthenticationProvider authenticationProvider;

    public SpringSecurityIdSiteResultListener(AuthenticationProvider stormpathAuthenticationProvider) {
        Assert.isTrue(
            stormpathAuthenticationProvider instanceof StormpathAuthenticationProvider,
            "AuthenticationProvider must be a StormpathAuthenticationProvider"
        );
        this.authenticationProvider = (StormpathAuthenticationProvider) stormpathAuthenticationProvider;
    }

    @Override
    public void onRegistered(RegistrationResult result) {
        doAuthenticate(result.getAccount());
    }

    @Override
    public void onAuthenticated(AuthenticationResult result) {
        doAuthenticate(result.getAccount());
    }

    @Override
    public void onLogout(LogoutResult result) {
        SecurityContextHolder.clearContext();
    }

    private void doAuthenticate(Account account) {
        SecurityContextHolder.clearContext();
        Authentication authentication = new IdSiteAuthenticationToken(account);
        authenticationProvider.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}