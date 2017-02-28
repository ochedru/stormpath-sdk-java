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
package com.stormpath.spring.config;

import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.servlet.filter.ContentNegotiationResolver;
import com.stormpath.sdk.servlet.filter.account.AccountResolverFilter;
import com.stormpath.sdk.servlet.http.MediaType;
import com.stormpath.sdk.servlet.http.Saver;
import com.stormpath.sdk.servlet.http.UnresolvedMediaTypeException;
import com.stormpath.sdk.servlet.mvc.ProviderAccountRequestFactory;
import com.stormpath.sdk.servlet.mvc.WebHandler;
import com.stormpath.spring.filter.ContentNegotiationSpringSecurityAuthenticationFilter;
import com.stormpath.spring.filter.StormpathSecurityContextPersistenceFilter;
import com.stormpath.spring.filter.StormpathWrapperFilter;
import com.stormpath.spring.security.provider.SocialCallbackSpringSecurityProcessingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0.RC5
 */
@EnableStormpathWebSecurity
public class StormpathSecurityConfigurerAdapter extends AbstractStormpathSecurityConfigurerAdapter  {

    private static final Logger log = LoggerFactory.getLogger(StormpathSecurityConfigurerAdapter.class);

    @Autowired
    SocialCallbackSpringSecurityProcessingFilter socialCallbackSpringSecurityProcessingFilter;

    /**
     * @since 1.3.0
     */
    @Autowired
    ContentNegotiationSpringSecurityAuthenticationFilter contentNegotiationSpringSecurityAuthenticationFilter;

    @Autowired
    AccountResolverFilter springSecurityResolvedAccountFilter;

    //Based on http://docs.spring.io/spring-security/site/docs/4.2.0.RELEASE/reference/htmlsingle/#filter-ordering
    //we are introducing a new filter in order to place the Stormpath Account in context.
    //This is required when a user is logged in (cookie in browser) and then the Web App is restarted. In that case
    //Spring security will deny access at some point and redirect you to login. Stormpath will see your cookie, will do an
    //automatic login and will forward you to the original URL but Spring Security will not have its security context set
    //by Stormpath and therefore it will redirect you back to login -> Consequence: redirection loop! -> This Filter fixes that :^)
    @Autowired
    StormpathSecurityContextPersistenceFilter stormpathSecurityContextPersistenceFilter;

    /**
     * This filter adds Client and Application as attributes to every request in order for subsequent Filters to have access to them.
     * For example, a filter trying to validate an access token will need to have access to the Application (see AuthorizationHeaderAccountResolver)
     *
     * @since 1.3.0
     */
    @Autowired
    protected StormpathWrapperFilter stormpathWrapperFilter;

    @Autowired
    AuthenticationEntryPoint stormpathAuthenticationEntryPoint;

    @Autowired
    protected Client client;

    @Autowired
    @Qualifier("stormpathLogoutHandler")
    protected LogoutHandler logoutHandler;

    @Autowired
    @Qualifier("stormpathAuthenticationSuccessHandler")
    protected AuthenticationSuccessHandler successHandler;

    @Autowired
    @Qualifier("stormpathCsrfTokenRepository")
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    @Qualifier("stormpathAuthenticationFailureHandler")
    protected AuthenticationFailureHandler failureHandler;

    @Autowired
    @Qualifier("stormpathAuthenticationManager")
    AuthenticationManager stormpathAuthenticationManager; // provided by stormpath-spring-security

    @Autowired(required = false) //required = false when stormpath.web.enabled = false
    @Qualifier("stormpathAuthenticationResultSaver")
    protected Saver<AuthenticationResult> authenticationResultSaver; //provided by stormpath-spring-webmvc

    /**
     * @since 1.3.0
     */
    @Autowired(required = false)
    ProviderAccountRequestFactory stormpathProviderAccountRequestFactory; //provided by stormpath-spring-webmvc

    @Value("#{ @environment['stormpath.web.logout.nextUri'] ?: '/' }")
    protected String logoutNextUri;

    @Value("#{ @environment['stormpath.web.produces'] ?: 'application/json, text/html' }")
    protected String produces;

    @Value("#{ @environment['stormpath.web.oauth2.enabled'] ?: true }")
    protected boolean accessTokenEnabled;

    @Value("#{ @environment['stormpath.web.oauth2.uri'] ?: '/oauth/token' }")
    protected String accessTokenUri;

    @Value("#{ @environment['stormpath.web.oauth2.revoke.uri'] ?: '/oauth/revoke' }")
    protected String revokeTokenUri;

    @Value("#{ @environment['stormpath.web.oauth2.revokeOnLogout'] ?: true }")
    protected boolean accessTokenRevokeOnLogout;

    @Value("#{ @environment['stormpath.web.resendVerification.uri'] ?: '/resendVerification' }")
    protected String resendVerificationUri;

    @Value("#{ @environment['stormpath.spring.security.fullyAuthenticated.enabled'] ?: true }")
    protected boolean fullyAuthenticatedEnabled;

    @Value("#{ @environment['stormpath.web.idSite.enabled'] ?: false }")
    protected boolean idSiteEnabled;

    @Value("#{ @environment['stormpath.web.callback.enabled'] ?: true }")
    protected boolean callbackEnabled;

    // both idSiteResultUri and samlResultUri default to `/stormpathCallback`
    // this is a fix for https://github.com/stormpath/stormpath-sdk-java/issues/1254
    // TODO - for 2.0.0 release remove stormpath.web.idSite.resultUri and use stormpath.web.callback.uri for both id site and saml
    @Value("#{ @environment['stormpath.web.idSite.resultUri'] ?: '/stormpathCallback' }")
    protected String idSiteResultUri;

    // TODO - for 2.0.0 release rename variable webCallbackUri
    @Value("#{ @environment['stormpath.web.callback.uri'] ?: '/stormpathCallback' }")
    protected String samlResultUri;

    @Value("#{ @environment['stormpath.web.social.google.uri'] ?: '/callbacks/google' }")
    protected String googleCallbackUri;

    @Value("#{ @environment['stormpath.web.social.facebook.uri'] ?: '/callbacks/facebook' }")
    protected String facebookCallbackUri;

    @Value("#{ @environment['stormpath.web.social.linkedin.uri'] ?: '/callbacks/linkedin' }")
    protected String linkedinCallbackUri;

    @Value("#{ @environment['stormpath.web.social.github.uri'] ?: '/callbacks/github' }")
    protected String githubCallbackUri;

    @Value("#{ @environment['stormpath.web.me.enabled'] ?: true }")
    protected boolean meEnabled;

    @Value("#{ @environment['stormpath.web.me.uri'] ?: '/me' }")
    protected String meUri;

    @Value("#{ @environment['stormpath.web.cors.enabled'] ?: true }")
    protected boolean corsEnabled;

    @Autowired(required = false)
    @Qualifier("loginPreHandler")
    protected WebHandler loginPreHandler;

    @Autowired(required = false)
    @Qualifier("loginPostHandler")
    protected WebHandler loginPostHandler;

    /**
     * The pre-defined Stormpath access control settings are defined here.
     *
     * @param http the {@link HttpSecurity} to be modified
     * @throws Exception if an error occurs
     */
    @Override
    public void init(HttpSecurity http) throws Exception {
        // autowire this bean
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        context.getAutowireCapableBeanFactory().autowireBean(this);
        http.servletApi().rolePrefix(""); //Fix for https://github.com/stormpath/stormpath-sdk-java/issues/325

        if (loginEnabled) {
            http.addFilterBefore(stormpathWrapperFilter, SecurityContextPersistenceFilter.class);

            // We need to add the springSecurityResolvedAccountFilter whenever we have our login enabled in order to
            // fix https://github.com/stormpath/stormpath-sdk-java/issues/450
            http.addFilterBefore(springSecurityResolvedAccountFilter, LogoutFilter.class);

            // Fix for redirection loop when Cookie is present but WebApp is restarted and '/' is locked down to authenticated users (Bare Bones example)
            http.addFilterBefore(stormpathSecurityContextPersistenceFilter, UsernamePasswordAuthenticationFilter.class);

            http.addFilterBefore(socialCallbackSpringSecurityProcessingFilter, UsernamePasswordAuthenticationFilter.class);

            // This filter replaces http.formLogin() so that we can properly handle content negotiation
            // If it's an HTML request, it delegates to the default UsernamePasswordAuthenticationFilter behavior
            // refer to: https://github.com/stormpath/stormpath-sdk-java/issues/682
            http.addFilterBefore(contentNegotiationSpringSecurityAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        if (corsEnabled) {
            http.cors(); // Let's add Spring Security's built-in support for CORs
        }

        if (idSiteEnabled && loginEnabled) {
            String permittedResultPath = (idSiteEnabled) ? idSiteResultUri : samlResultUri;

            http.authorizeRequests()
                .antMatchers(loginUri).permitAll()
                .antMatchers(permittedResultPath).permitAll()
                .and().exceptionHandling().authenticationEntryPoint(stormpathAuthenticationEntryPoint); //Fix for https://github.com/stormpath/stormpath-sdk-java/issues/714
        } else if (stormpathWebEnabled) {
            if (loginEnabled) {
                // make sure that /login and /login?status=... is permitted
                String loginUriMatch = (loginUri.endsWith("*")) ? loginUri : loginUri + "*";

                http.authorizeRequests()
                    .antMatchers(loginUriMatch).permitAll()
                    .antMatchers(samlUri).permitAll()
                    .antMatchers(samlResultUri).permitAll()
                    .antMatchers(googleCallbackUri).permitAll()
                    .antMatchers(githubCallbackUri).permitAll()
                    .antMatchers(facebookCallbackUri).permitAll()
                    .antMatchers(linkedinCallbackUri).permitAll()
                    .and().exceptionHandling().authenticationEntryPoint(stormpathAuthenticationEntryPoint); //Fix for https://github.com/stormpath/stormpath-sdk-java/issues/714
            }

            if (meEnabled) {
                http.authorizeRequests().antMatchers(meUri).fullyAuthenticated();
            }

            http.authorizeRequests()
                .antMatchers("/assets/css/stormpath.css").permitAll()
                .antMatchers("/assets/css/custom.stormpath.css").permitAll()
                .antMatchers("/assets/js/stormpath.js").permitAll()
                // fix for https://github.com/stormpath/stormpath-sdk-java/issues/822
                .antMatchers("/WEB-INF/jsp/stormpath/**").permitAll();
        }

        if (idSiteEnabled || callbackEnabled || stormpathWebEnabled) {
            if (logoutEnabled) {
                LogoutConfigurer<HttpSecurity> httpSecurityLogoutConfigurer = http
                    .logout()
                    .invalidateHttpSession(true)
                    .logoutUrl(logoutUri);

                if (!idSiteEnabled) {
                    httpSecurityLogoutConfigurer.logoutSuccessUrl(logoutNextUri);
                }

                httpSecurityLogoutConfigurer
                    .addLogoutHandler(logoutHandler).and()
                    .authorizeRequests().antMatchers(logoutUri).permitAll();
            }

            if (forgotEnabled) {
                http.authorizeRequests().antMatchers(forgotUri).permitAll();
            }
            if (changeEnabled) {
                http.authorizeRequests().antMatchers(changeUri).permitAll();
            }
            if (registerEnabled) {
                http.authorizeRequests().antMatchers(registerUri).permitAll();
            }
            if (verifyEnabled) {
                http.authorizeRequests().antMatchers(verifyUri).permitAll();
            }
            if (accessTokenEnabled) {
                http.authorizeRequests().antMatchers(accessTokenUri).permitAll()
                     .and().authorizeRequests().antMatchers(revokeTokenUri).permitAll();
            }

            if (fullyAuthenticatedEnabled) {
                http.authorizeRequests().anyRequest().fullyAuthenticated();
            }

            if (!csrfTokenEnabled) {
                http.csrf().disable();
            } else {
                http.csrf().csrfTokenRepository(csrfTokenRepository);
                if (accessTokenEnabled) {
                    http.csrf().ignoringAntMatchers(accessTokenUri);
                }
                if (logoutEnabled) {
                    http.csrf().ignoringAntMatchers(logoutUri);
                }

                // @since 1.0.0
                // Refer to: https://github.com/stormpath/stormpath-sdk-java/pull/701
                http.csrf().requireCsrfProtectionMatcher(new RequestMatcher() {

                    @Override
                    public boolean matches(HttpServletRequest request) {
                        if ("GET".equals(request.getMethod())) {
                            return false;
                        }
                        try {
                            MediaType mediaType = ContentNegotiationResolver.INSTANCE.getContentType(
                                    request, null, MediaType.parseMediaTypes(produces)
                            );
                            // if it's a JSON request, disable csrf
                            return !MediaType.APPLICATION_JSON.equals(mediaType);
                        } catch (UnresolvedMediaTypeException e) {
                            log.error("Couldn't resolve media type: {}", e.getMessage(), e);
                            return csrfTokenEnabled;
                        }
                    }
                });
            }
        }
    }
}
