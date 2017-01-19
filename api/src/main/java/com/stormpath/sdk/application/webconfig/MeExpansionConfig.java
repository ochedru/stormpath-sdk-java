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
package com.stormpath.sdk.application.webconfig;

/**
 * The MeExpansionConfig belongs to a {@link MeConfig} and exposes the expandable properties of an {@link com.stormpath.sdk.account.Account}
 * returned when accessing the <code>/me</code> route with the proper assertion.
 *
 * @since 1.2.0
 */
public interface MeExpansionConfig {

    /**
     * Returns whether the account returned when accessing the <code>/me</code> route, should expand the apiKeys ({@code true})
     * or not ({@code false}).
     *
     * @return whether the account returned when accessing the <code>/me</code> route, should expand the apiKeys ({@code true})
     * or not ({@code false}).
     */
    boolean isApiKeys();

    /**
     * Sets the {@code boolean} value to enable {@code true} or disable {@code false} the expansion of the apiKeys of the
     * account returned when accessing the <code>/me</code> route.
     *
     * @param apiKeys value to set whether to expand the account reference or not.
     */
    MeExpansionConfig setApiKeys(boolean apiKeys);

    /**
     * Returns whether the account returned when accessing the <code>/me</code> route, should expand the application ({@code true})
     * or not ({@code false}).
     *
     * @return whether the account returned when accessing the <code>/me</code> route, should expand the application ({@code true})
     * or not ({@code false}).
     */
    boolean isApplications();

    /**
     * Sets the {@code boolean} value to enable {@code true} or disable {@code false} the expansion of the applications of the
     * account returned when accessing the <code>/me</code> route.
     *
     * @param applications value to set whether to expand the account reference or not.
     */
    MeExpansionConfig setApplications(boolean applications);

    /**
     * Returns whether the account returned when accessing the <code>/me</code> route, should expand the customData ({@code true})
     * or not ({@code false}).
     *
     * @return whether the account returned when accessing the <code>/me</code> route, should expand the customData ({@code true})
     * or not ({@code false}).
     */
    boolean isCustomData();

    /**
     * Sets the {@code boolean} value to enable {@code true} or disable {@code false} the expansion of the customData of the
     * account returned when accessing the <code>/me</code> route.
     *
     * @param customData value to set whether to expand the account reference or not.
     */
    MeExpansionConfig setCustomData(boolean customData);

    /**
     * Returns whether the account returned when accessing the <code>/me</code> route, should expand the directory ({@code true})
     * or not ({@code false}).
     *
     * @return whether the account returned when accessing the <code>/me</code> route, should expand the directory ({@code true})
     * or not ({@code false}).
     */
    boolean isDirectory();

    /**
     * Sets the {@code boolean} value to enable {@code true} or disable {@code false} the expansion of the directory in an
     * account returned when accessing the <code>/me</code> route.
     *
     * @param directory value to set whether to expand the account reference or not.
     */
    MeExpansionConfig setDirectory(boolean directory);

    /**
     * Returns whether the account returned when accessing the <code>/me</code> route, should expand the groupMemberships ({@code true})
     * or not ({@code false}).
     *
     * @return whether the account returned when accessing the <code>/me</code> route, should expand the groupMemberships ({@code true})
     * or not ({@code false}).
     */
    boolean isGroupMemberships();

    /**
     * Sets the {@code boolean} value to enable {@code true} or disable {@code false} the expansion of the groupMemberships in an
     * account returned when accessing the <code>/me</code> route.
     *
     * @param groupMemberships value to set whether to expand the account reference or not.
     */
    MeExpansionConfig setGroupMemberships(boolean groupMemberships);

    /**
     * Returns whether the account returned when accessing the <code>/me</code> route, should expand the groups ({@code true})
     * or not ({@code false}).
     *
     * @return whether the account returned when accessing the <code>/me</code> route, should expand the groups ({@code true})
     * or not ({@code false}).
     */
    boolean isGroups();

    /**
     * Sets the {@code boolean} value to enable {@code true} or disable {@code false} the expansion of the groups in an
     * account returned when accessing the <code>/me</code> route.
     *
     * @param groups value to set whether to expand the account reference or not.
     */
    MeExpansionConfig setGroups(boolean groups);

    /**
     * Returns whether the account returned when accessing the <code>/me</code> route, should expand the providerData ({@code true})
     * or not ({@code false}).
     *
     * @return whether the account returned when accessing the <code>/me</code> route, should expand the providerData ({@code true})
     * or not ({@code false}).
     */
    boolean isProviderData();

    /**
     * Sets the {@code boolean} value to enable {@code true} or disable {@code false} the expansion of the providerData in an
     * account returned when accessing the <code>/me</code> route.
     *
     * @param providerData value to set whether to expand the account reference or not.
     */
    MeExpansionConfig setProviderData(boolean providerData);

    /**
     * Returns whether the account returned when accessing the <code>/me</code> route, should expand the tenant ({@code true})
     * or not ({@code false}).
     *
     * @return whether the account returned when accessing the <code>/me</code> route, should expand the tenant ({@code true})
     * or not ({@code false}).
     */
    boolean isTenant();

    /**
     * Sets the {@code boolean} value to enable {@code true} or disable {@code false} the expansion of the tenant in an
     * account returned when accessing the <code>/me</code> route.
     *
     * @param tenant value to set whether to expand the account reference or not.
     */
    MeExpansionConfig setTenant(boolean tenant);

}
