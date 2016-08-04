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
package com.stormpath.spring.config

import com.stormpath.sdk.servlet.authc.FailedAuthenticationRequestEvent
import com.stormpath.sdk.servlet.event.RequestEventListener
import com.stormpath.sdk.servlet.event.RequestEventListenerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

import static com.stormpath.spring.config.StormpathWebSecurityConfigurer.stormpath

/**
 * @since 1.0.RC9
 */
@Configuration
@EnableStormpathWebSecurity
class CustomRequestEventListenerConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.apply(stormpath())
    }

    @Bean
    public RequestEventListener stormpathRequestEventListener() {
        return new CustomRequestEventListener()
    }

    static class CustomRequestEventListener extends RequestEventListenerAdapter {

        boolean failedInvoked = false

        @Override
        void on(FailedAuthenticationRequestEvent e) {
            failedInvoked = true
        }
    }
}
