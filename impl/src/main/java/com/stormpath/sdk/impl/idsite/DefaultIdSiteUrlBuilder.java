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
package com.stormpath.sdk.impl.idsite;

import com.stormpath.sdk.api.ApiKey;
import com.stormpath.sdk.idsite.IdSiteUrlBuilder;
import com.stormpath.sdk.impl.ds.InternalDataStore;
import com.stormpath.sdk.impl.http.QueryString;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.lang.Strings;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.UUID;

import static com.stormpath.sdk.impl.idsite.IdSiteClaims.JWT_REQUEST;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 1.0.RC
 */
public class DefaultIdSiteUrlBuilder implements IdSiteUrlBuilder {

    // The base URL is the application URL with the last 3 segments rem
    private static final Pattern HREF_PATTERN = Pattern.compile("^(.*)(?:\\/[\\w-]+){3}$");

    public static String SSO_LOGOUT_SUFFIX = "/logout";
    public final String ssoEndpoint;
    private final InternalDataStore internalDataStore;
    private final String applicationHref;
    private final IdSiteClaims claims;

    private boolean logout = false;

    public DefaultIdSiteUrlBuilder(InternalDataStore internalDataStore, String applicationHref) {
        Assert.notNull(internalDataStore, "internalDataStore cannot be null.");
        Assert.hasText(applicationHref, "applicationHref cannot be null or empty");

        //qualify the application HREF
        try {
            Method ensureFullyQualifiedMethod = internalDataStore.getClass().getDeclaredMethod("ensureFullyQualified", String.class);
            ensureFullyQualifiedMethod.setAccessible(true);
            applicationHref = (String) ensureFullyQualifiedMethod.invoke(internalDataStore, applicationHref);
        } catch (Exception e) {

        }
        this.ssoEndpoint = getBaseUrl(applicationHref) + "/sso";
        this.internalDataStore = internalDataStore;
        this.applicationHref = applicationHref;
        this.claims = new IdSiteClaims();
    }

    @Override
    public IdSiteUrlBuilder setCallbackUri(String callbackUri) {
        claims.setCallbackUri(callbackUri);
        return this;
    }

    @Override
    public IdSiteUrlBuilder setState(String state) {
        claims.setState(state);
        return this;
    }

    @Override
    public IdSiteUrlBuilder setPath(String path) {
        claims.setPath(path);
        return this;
    }

    @Override
    public IdSiteUrlBuilder setOrganizationNameKey(String organizationNameKey) {
        claims.setOrganizationNameKey(organizationNameKey);
        return this;
    }

    @Override
    public IdSiteUrlBuilder setUseSubdomain(boolean useSubdomain) {
        claims.setUseSubdomain(useSubdomain);
        return this;
    }

    @Override
    public IdSiteUrlBuilder setShowOrganizationField(boolean showOrganizationField) {
        claims.setShowOrganizationField(showOrganizationField);
        return this;
    }

    @Override
    public IdSiteUrlBuilder setSpToken(String spToken) {
        claims.setSpToken(spToken);
        return this;
    }

    @Override
    public IdSiteUrlBuilder addProperty(String name, Object value) {
        claims.put(name, value);
        return this;
    }

    @Override
    public IdSiteUrlBuilder forLogout() {
        this.logout = true;
        return this;
    }

    @Override
    public String build() {
        Assert.state(Strings.hasText(claims.getCallbackUri()), "callbackUri cannot be null or empty.");

        String jti = UUID.randomUUID().toString();

        Date now = new Date();

        final ApiKey apiKey = this.internalDataStore.getApiKey();

        JwtBuilder jwtBuilder = Jwts.builder().setClaims(claims).setId(jti).setIssuedAt(now).setIssuer(apiKey.getId())
                .setSubject(this.applicationHref);

        byte[] secret = apiKey.getSecret().getBytes(Strings.UTF_8);

        String jwt = jwtBuilder.setHeaderParam(JwsHeader.TYPE, JwsHeader.JWT_TYPE).signWith(SignatureAlgorithm.HS256, secret).compact();

        QueryString queryString = new QueryString();
        queryString.put(JWT_REQUEST, jwt);

        StringBuilder urlBuilder = new StringBuilder(ssoEndpoint);

        if (logout) {
            urlBuilder.append(SSO_LOGOUT_SUFFIX);
        }

        return urlBuilder.append('?').append(queryString.toString()).toString();
    }

    /**
     * Fix for https://github.com/stormpath/stormpath-sdk-java/issues/184. Base
     * URL for IDSite is constructed from the applicationHref received in the
     * constructor.
     *
     * @since 1.0.RC4.2
     */
    protected String getBaseUrl(String href) {
        Matcher matcher = HREF_PATTERN.matcher(href);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("IDSite base URL could not be constructed.");

    }

}
