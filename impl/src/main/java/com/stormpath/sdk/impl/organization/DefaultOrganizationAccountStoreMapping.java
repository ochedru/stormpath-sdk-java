/*
* Copyright 2015 Stormpath, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.stormpath.sdk.impl.organization;

import com.stormpath.sdk.impl.application.AbstractAccountStoreMapping;
import com.stormpath.sdk.impl.ds.InternalDataStore;
import com.stormpath.sdk.impl.resource.ResourceReference;
import com.stormpath.sdk.impl.resource.Property;
import com.stormpath.sdk.organization.Organization;
import com.stormpath.sdk.organization.OrganizationAccountStoreMapping;

import java.util.Map;

/**
 * @since 1.0.RC7
 */
public class DefaultOrganizationAccountStoreMapping extends AbstractAccountStoreMapping<OrganizationAccountStoreMapping> implements OrganizationAccountStoreMapping {

    // INSTANCE RESOURCE REFERENCES:
    static final ResourceReference<Organization> ORGANIZATION = new ResourceReference<Organization>("organization", Organization.class);

    static final Map<String, Property> PROPERTY_DESCRIPTORS = createPropertyDescriptorMap(
            LIST_INDEX, DEFAULT_ACCOUNT_STORE, DEFAULT_GROUP_STORE, ACCOUNT_STORE, ORGANIZATION);

    public DefaultOrganizationAccountStoreMapping(InternalDataStore dataStore) {
        super(dataStore);
    }

    public DefaultOrganizationAccountStoreMapping(InternalDataStore dataStore, Map<String, Object> properties) {
        super(dataStore, properties);
    }

    @Override
    public Map<String, Property> getPropertyDescriptors() {
        return PROPERTY_DESCRIPTORS;
    }

    public Organization getOrganization() {
        return getResourceProperty(ORGANIZATION);
    }

    public com.stormpath.sdk.organization.OrganizationAccountStoreMapping setOrganization(Organization organization) {
        setResourceProperty(ORGANIZATION, organization);
        return this;
    }
}
