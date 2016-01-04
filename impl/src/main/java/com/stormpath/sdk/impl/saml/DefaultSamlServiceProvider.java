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
package com.stormpath.sdk.impl.saml;

import com.stormpath.sdk.impl.ds.InternalDataStore;
import com.stormpath.sdk.impl.resource.AbstractInstanceResource;
import com.stormpath.sdk.impl.resource.DateProperty;
import com.stormpath.sdk.impl.resource.Property;
import com.stormpath.sdk.impl.resource.ResourceReference;
import com.stormpath.sdk.impl.resource.StringProperty;
import com.stormpath.sdk.saml.SamlServiceProvider;
import com.stormpath.sdk.saml.SsoInitiationEndpoint;

import java.util.Date;
import java.util.Map;

/**
 * @since 1.0.RC8
 */
public class DefaultSamlServiceProvider extends AbstractInstanceResource implements SamlServiceProvider {

    // SIMPLE PROPERTIES
    public static final DateProperty CREATED_AT = new DateProperty("createdAt");
    public static final DateProperty MODIFIED_AT = new DateProperty("modifiedAt");

    static final ResourceReference<SsoInitiationEndpoint> SSO_INITIALIZATION_ENDPOINT = new ResourceReference<SsoInitiationEndpoint>("ssoInitiationEndpoint", SsoInitiationEndpoint.class);

    static final Map<String,Property> PROPERTY_DESCRIPTORS = createPropertyDescriptorMap(SSO_INITIALIZATION_ENDPOINT, CREATED_AT, MODIFIED_AT);

    public DefaultSamlServiceProvider(InternalDataStore dataStore, Map<String, Object> properties) {
        super(dataStore, properties);
    }

    public DefaultSamlServiceProvider(InternalDataStore dataStore) {
        super(dataStore);
    }

    @Override
    public Map<String, Property> getPropertyDescriptors() {
        return PROPERTY_DESCRIPTORS;
    }

    @Override
    public SsoInitiationEndpoint getSsoInitiationEndpoint() {
        return getResourceProperty(SSO_INITIALIZATION_ENDPOINT);
    }

    @Override
    public Date getCreatedAt() {
        return getDateProperty(CREATED_AT);
    }

    @Override
    public Date getModifiedAt() {
        return getDateProperty(MODIFIED_AT);
    }
}
