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
package com.stormpath.sdk.impl.saml;


import com.stormpath.sdk.impl.query.DefaultOptions;
import com.stormpath.sdk.saml.SamlIdentityProviderOptions;

/**
 * @since 1.3.0
 */
public class DefaultSamlIdentityProviderOptions extends DefaultOptions<SamlIdentityProviderOptions> implements SamlIdentityProviderOptions<SamlIdentityProviderOptions> {

    @Override
    public SamlIdentityProviderOptions withAttributeMappingRules() {
        return expand(DefaultSamlIdentityProvider.ATTRIBUTE_STATEMENT_MAPPING_RULES);
    }

    @Override
    public SamlIdentityProviderOptions withRegisteredSamlServiceProviders() {
        return expand(DefaultSamlIdentityProvider.REGISTERED_SAML_SERVICE_PROVIDERS);
    }

    @Override
    public SamlIdentityProviderOptions withSamlServiceProviderRegistrations() {
        return expand(DefaultSamlIdentityProvider.SAML_SERVICE_PROVIDER_REGISTRATIONS);
    }
}
