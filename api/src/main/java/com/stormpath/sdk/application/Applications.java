/*
 * Copyright 2013 Stormpath, Inc.
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
package com.stormpath.sdk.application;

import com.stormpath.sdk.account.VerificationEmailRequestBuilder;
import com.stormpath.sdk.api.ApiRequestAuthenticator;
import com.stormpath.sdk.lang.Classes;
import com.stormpath.sdk.oauth.OAuthApiRequestAuthenticator;
import com.stormpath.sdk.query.Criterion;
import com.stormpath.sdk.query.DateExpressionFactory;
import com.stormpath.sdk.query.EqualsExpressionFactory;
import com.stormpath.sdk.query.StringExpressionFactory;

import java.lang.reflect.Constructor;

/**
 * Static utility/helper methods for working with {@link Application} resources.  Most methods are
 * <a href="http://en.wikipedia.org/wiki/Factory_method_pattern">factory method</a>s used for forming
 * Application-specific <a href="http://en.wikipedia.org/wiki/Fluent_interface">fluent DSL</a> queries. For example:
 * <pre>
 * <b>Applications.where(Applications.name()</b>.containsIgnoreCase("Foo")<b>)</b>
 *     .and(<b>Applications.status()</b>.eq(ApplicationStatus.ENABLED))
 *     .orderByName().descending()
 *     .withAccounts(10, 10)
 *     .offsetBy(50)
 *     .limitTo(25));
 * </pre>
 * or, if using static imports:
 * <pre>
 * import static com.stormpath.sdk.application.Applications.*;
 *
 * ...
 *
 * <b>where(name()</b>.containsIgnoreCase("Foo")<b>)</b>
 *     .and(<b>status()</b>.eq(ApplicationStatus.ENABLED))
 *     .orderByName().descending()
 *     .withAccounts(10, 10)
 *     .offsetBy(50)
 *     .limitTo(25));
 * </pre>
 *
 * @since 0.8
 */
public final class Applications {

    @SuppressWarnings("unchecked")
    private static final Class<CreateApplicationRequestBuilder> BUILDER_CLASS =
            Classes.forName("com.stormpath.sdk.impl.application.DefaultCreateApplicationRequestBuilder");

    /**
     * Returns a new {@link ApplicationOptions} instance, used to customize how one or more {@link Application}s are retrieved.
     *
     * @return a new {@link ApplicationOptions} instance, used to customize how one or more {@link Application}s are retrieved.
     */
    public static ApplicationOptions options() {
        return (ApplicationOptions) Classes.newInstance("com.stormpath.sdk.impl.application.DefaultApplicationOptions");
    }

    /**
     * Returns a new {@link ApplicationCriteria} instance to use to formulate an Application query.
     * <p/>
     * Note that it is usually more common to use the {@link #where(com.stormpath.sdk.query.Criterion) where} method
     * instead of this one as the {@code where} method usually lends to better readability.  For example:
     * <pre>
     * Applications.criteria().add(Applications.name().eqIgnoreCase("Foo"))...
     * </pre>
     * versus:
     * <pre>
     * Applications.where(Applications.name().eqIgnoreCase("Foo"))...
     * </pre>
     * or when using static imports:
     * <pre>
     * where(name().eqIgnoreCase("Foo"))...
     * </pre>
     * While all three statements are equivalent, the second and third examples are shorter and probably more readable.
     *
     * @return a new {@link ApplicationCriteria} instance to use to formulate an Application query.
     */
    public static ApplicationCriteria criteria() {
        return (ApplicationCriteria) Classes.newInstance("com.stormpath.sdk.impl.application.DefaultApplicationCriteria");
    }

    /**
     * Creates a new {@link com.stormpath.sdk.query.DateExpressionFactory} instance reflecting the Application {@link com.stormpath.sdk.application.Application#getCreatedAt() createdAt}
     * property, to be used to construct a createdAt Criterion when building an {@link com.stormpath.sdk.application.ApplicationCriteria} query.  For example:
     * <pre>
     * Applications.where(<b>Applications.createdAt()</b>.matches("[,2014-04-05T12:00:00]");
     * </pre>
     * The above example invokes the returned factory's <code>matches("[,2014-04-05T12:00:00]"))</code> method.  This
     * produces a name-specific {@link Criterion} which is added to the criteria query (via the
     * {@link #where(Criterion) where} method).
     * <pre>
     * For example, the following code is equivalent:
     * <pre>
     * ApplicationCriteria criteria = Applications.criteria();
     * DateExpressionFactory createdAt = Applications.createdAt();
     * Criterion createdAtMatches = createdAt.matches("[,2014-04-05T12:00:00]");
     * criteria.add(createdAtMatches);
     * </pre>
     *
     * @return a new {@link com.stormpath.sdk.application.Application#getCreatedAt() createdAt}-specific {@link com.stormpath.sdk.query.DateExpressionFactory} instance, to be
     *         used to construct a criterion when building an {@link com.stormpath.sdk.application.ApplicationCriteria} query.
     * @since 1.0.RC4.6
     */
    public static DateExpressionFactory createdAt(){
        return newDateExpressionFactory("createdAt");
    }

    /**
     * Creates a new {@link DateExpressionFactory} instance reflecting the Application {@link com.stormpath.sdk.application.Application#getModifiedAt() modifiedAt}
     * property, to be used to construct a modifiedAt Criterion when building an {@link com.stormpath.sdk.application.ApplicationCriteria} query.  For example:
     * <pre>
     * Applications.where(<b>Applications.modifiedAt()</b>.matches("[,2014-04-05T12:00:00]");
     * </pre>
     * The above example invokes the returned factory's <code>matches("[,2014-04-05T12:00:00]"))</code> method.  This
     * produces a name-specific {@link Criterion} which is added to the criteria query (via the
     * {@link #where(Criterion) where} method).
     * <pre>
     * For example, the following code is equivalent:
     * <pre>
     * ApplicationCriteria criteria = Applications.criteria();
     * DateExpressionFactory createdAt = Applications.modifiedAt();
     * Criterion modifiedAtMatches = modifiedAt.matches("[,2014-04-05T12:00:00]");
     * criteria.add(modifiedAtMatches);
     * </pre>
     *
     * @return a new {@link com.stormpath.sdk.application.Application#getModifiedAt() modifiedAt}-specific {@link DateExpressionFactory} instance, to be
     *         used to construct a criterion when building an {@link com.stormpath.sdk.application.ApplicationCriteria} query.
     * @since 1.0.RC4.6
     */
    public static DateExpressionFactory modifiedAt(){
        return newDateExpressionFactory("modifiedAt");
    }

    /**
     * Creates a new {@link ApplicationCriteria} instance using the specified {@code criterion} as the first query condition.
     *
     * @return a new {@link ApplicationCriteria} instance using the specified {@code criterion} as the first query condition.
     */
    public static ApplicationCriteria where(Criterion criterion) {
        return criteria().add(criterion);
    }

    /**
     * Creates a new {@link StringExpressionFactory} instance reflecting the Application {@link Application#getName() name}
     * property, to be used to construct a name Criterion when building an {@link ApplicationCriteria} query.  For example:
     * <pre>
     * Applications.where(<b>Applications.name()</b>.startsWithIgnoreCase("foo");
     * </pre>
     * The above example invokes the returned factory's <code>startsWithIgnoreCase("foo")</code> method.  This
     * produces a name-specific {@link Criterion} which is added to the criteria query (via the
     * {@link #where(com.stormpath.sdk.query.Criterion) where} method).  For example, the following code is equivalent:
     * <pre>
     * ApplicationCriteria criteria = Applications.criteria();
     * StringExpressionFactory nameExpressionFactory = Applications.name();
     * Criterion nameStartsWithFoo = nameExpressionFactory.startsWithIgnoreCase("foo");
     * criteria.add(nameStartsWithFoo);
     * </pre>
     * The first code example is clearly more succinct and readable.
     *
     * @return a new {@link Application#getName() name}-specific {@link StringExpressionFactory} instance, to be
     *         used to construct a criterion when building an {@link ApplicationCriteria} query.
     */
    public static StringExpressionFactory name() {
        return newStringExpressionFactory("name");
    }

    /**
     * Creates a new {@link StringExpressionFactory} instance reflecting the Application {@link Application#getDescription() description}
     * property, to be used to construct a description Criterion when building an {@link ApplicationCriteria} query.  For example:
     * <pre>
     * Applications.where(<b>Applications.description()</b>.startsWithIgnoreCase("foo");
     * </pre>
     * The above example invokes the returned factory's <code>startsWithIgnoreCase("foo")</code> method.  This
     * produces a description-specific {@link Criterion} which is added to the criteria query (via the
     * {@link #where(com.stormpath.sdk.query.Criterion) where} method).  For example, the following code is equivalent:
     * <pre>
     * ApplicationCriteria criteria = Applications.criteria();
     * StringExpressionFactory descriptionExpressionFactory = Applications.description();
     * Criterion descriptionStartsWithFoo = descriptionExpressionFactory.startsWithIgnoreCase("foo");
     * criteria.add(descriptionStartsWithFoo);
     * </pre>
     * The first code example is clearly more succinct and readable.
     *
     * @return a new {@link Application#getDescription() description}-specific {@link StringExpressionFactory} instance, to be
     *         used to construct a criterion when building an {@link ApplicationCriteria} query.
     */
    public static StringExpressionFactory description() {
        return newStringExpressionFactory("description");
    }

    /**
     * Creates a new {@link EqualsExpressionFactory} instance reflecting the Application {@link Application#getStatus() status}
     * property, to be used to construct a status Criterion when building an {@link ApplicationCriteria} query.  For example:
     * <pre>
     * Applications.where(<b>Applications.status()</b>.eq(ApplicationStatus.ENABLED);
     * </pre>
     * The above example invokes the returned factory's <code>eq()</code> method.  This
     * produces a status-specific {@link Criterion} which is added to the criteria query (via the
     * {@link #where(com.stormpath.sdk.query.Criterion) where} method).  For example, the following code is equivalent:
     * <pre>
     * ApplicationCriteria criteria = Applications.criteria();
     * StringExpressionFactory statusExpressionFactory = Applications.status();
     * Criterion statusEqualsEnabled = statusExpressionFactory.eq(ApplicationStatus.ENABLED);
     * criteria.add(statusEqualsEnabled);
     * </pre>
     * The first code example is clearly more succinct and readable.
     *
     * @return a new {@link Application#getStatus() status}-specific {@link StringExpressionFactory} instance, to be
     *         used to construct a criterion when building an {@link ApplicationCriteria} query.
     */
    public static EqualsExpressionFactory status() {
        return newEqualsExpressionFactory("status");
    }

    /**
     * Creates a new {@link com.stormpath.sdk.application.CreateApplicationRequestBuilder CreateApplicationRequestBuilder}
     * instance reflecting the specified {@link Application} instance.  The builder can be used to customize any
     * creation request options as necessary.
     *
     * @param application the application to create a new record for within Stormpath
     * @return a new {@link com.stormpath.sdk.application.CreateApplicationRequestBuilder CreateApplicationRequestBuilder}
     *         instance reflecting the specified {@link Application} instance.
     * @see com.stormpath.sdk.tenant.Tenant#createApplication(CreateApplicationRequest) Tenant#createApplication(CreateApplicationRequest)
     */
    public static CreateApplicationRequestBuilder newCreateRequestFor(Application application) {
        Constructor ctor = Classes.getConstructor(BUILDER_CLASS, Application.class);
        return (CreateApplicationRequestBuilder) Classes.instantiate(ctor, application);
    }

    /**
     * Creates a new {@link com.stormpath.sdk.account.VerificationEmailRequestBuilder VerificationEmailRequestBuilder}.
     * The builder is used when the verification email needs to be re-sent.
     *
     * @return a new {@link com.stormpath.sdk.account.VerificationEmailRequestBuilder VerificationEmailRequestBuilder}
     *         instance.
     * @see com.stormpath.sdk.application.Application#sendVerificationEmail(com.stormpath.sdk.account.VerificationEmailRequest)
     * @since 1.0.0
     */
    public static VerificationEmailRequestBuilder verificationEmailBuilder() {
        final String FQCN = "com.stormpath.sdk.impl.account.DefaultVerificationEmailRequestBuilder";
        return (VerificationEmailRequestBuilder) Classes.newInstance(FQCN);
    }

    private static StringExpressionFactory newStringExpressionFactory(String propName) {
        final String FQCN = "com.stormpath.sdk.impl.query.DefaultStringExpressionFactory";
        return (StringExpressionFactory) Classes.newInstance(FQCN, propName);
    }

    private static DateExpressionFactory newDateExpressionFactory(String propName) {
        final String FQCN = "com.stormpath.sdk.impl.query.DefaultDateExpressionFactory";
        return (DateExpressionFactory) Classes.newInstance(FQCN, propName);
    }

    private static EqualsExpressionFactory newEqualsExpressionFactory(String propName) {
        final String FQCN = "com.stormpath.sdk.impl.query.DefaultEqualsExpressionFactory";
        return (EqualsExpressionFactory) Classes.newInstance(FQCN, propName);
    }

    /**
     * Creates a new {@link ApiRequestAuthenticator ApiRequestAuthenticator} instance for authenticating API requests
     * for the given {@link Application application}
     *
     * @param application The {@link Application} for which API requests will be authenticated
     * @return a new {@link ApiRequestAuthenticator} to be used for API requests authentication
     *
     * @since 1.0.RC4.6
     */
    public static ApiRequestAuthenticator apiRequestAuthenticator(Application application) {
        return (ApiRequestAuthenticator) Classes.newInstance("com.stormpath.sdk.impl.authc.DefaultApiRequestAuthenticator", application);
    }

    /**
     * Creates a new {@link com.stormpath.sdk.oauth.OAuthApiRequestAuthenticator OAuthApiRequestAuthenticator} instance for authenticating Oauth-based API requests
     * for the given {@link Application application}
     *
     * @param application The {@link Application} for which Oauth-based API requests will be authenticated
     * @return a new {@link com.stormpath.sdk.oauth.OAuthApiRequestAuthenticator} that acts as a builder to allow you
     *         to customize request processing behavior
     *
     * @since 1.0.RC4.6
     */
    public static OAuthApiRequestAuthenticator oauthRequestAuthenticator(Application application) {
        return (OAuthApiRequestAuthenticator) Classes.newInstance("com.stormpath.sdk.impl.oauth.authc.DefaultOAuthApiRequestAuthenticator", application);
    }

    /**
     * This operation will ask the backend to do a case-insensitive matching query on all viewable attributes in all the resources in the Collection.
     *
     * So the following query:
     *
     * tenant.getApplications(Applications.where(Applications.filter("Java SDK IT App"));
     *
     * Returns all Applications where:
     *
     * Each Application exists inside an ApplicationStore belonging to the Tenant `tenant`
     * The Application’s name equals or contains “Java SDK IT App” (case insensitive) OR
     * The Applications’s description equals or contains “Java SDK IT App” (case insensitive) OR
     * And so on
     *
     * @param value The value to search for
     * @return A Criterion representing the filter query
     * @since 1.2.0
     */
    public static Criterion filter(String value){
        return newStringExpressionFactory("q").eqIgnoreCase(value);
    }

}
