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

import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.organization.CreateOrganizationRequest;
import com.stormpath.sdk.organization.CreateOrganizationRequestBuilder;
import com.stormpath.sdk.organization.Organization;

/**
 * @since 1.0.RC7
 */
public class DefaultCreateOrganizationRequestBuilder implements CreateOrganizationRequestBuilder {

    private Organization organization;
    private boolean createDirectory;
    private String directoryName;

    public DefaultCreateOrganizationRequestBuilder(Organization organization) {
        Assert.notNull(organization, "organization cannot be null.");
        this.organization = organization;
    }

    @Override
    public CreateOrganizationRequest build() {
        if (createDirectory) {
            return new CreateOrganizationAndDirectoryRequest(organization, directoryName);
        }
        return new DefaultCreateOrganizationRequest(this.organization);
    }

    @Override
    public CreateOrganizationRequestBuilder createDirectory() {
        this.createDirectory = true;
        return this;
    }

    @Override
    public CreateOrganizationRequestBuilder createDirectoryNamed(String directoryName) {
        if (directoryName != null) {
            this.createDirectory = true;
        }
        this.directoryName = directoryName;
        return this;
    }
}
