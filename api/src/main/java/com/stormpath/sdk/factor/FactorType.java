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
package com.stormpath.sdk.factor;

/**
 * Represents the type for a specific {@link Factor}.
 *
 * @see com.stormpath.sdk.factor.sms.SmsFactor
 */
public enum FactorType {
    SMS("SMS"),
    GOOGLE_AUTHENTICATOR("google-authenticator");

    private String name;

    FactorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static FactorType fromName(String name) {
        for (FactorType factorType : values()) {
            if (factorType.getName().equalsIgnoreCase(name) || factorType.name().equals(name)) {
                return factorType;
            }
        }

        throw new IllegalArgumentException("No FactorType named '" + name + "'");
    }
}
