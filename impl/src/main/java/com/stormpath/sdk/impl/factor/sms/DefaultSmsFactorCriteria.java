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
package com.stormpath.sdk.impl.factor.sms;

import com.stormpath.sdk.factor.FactorCriteria;
import com.stormpath.sdk.factor.FactorType;
import com.stormpath.sdk.factor.sms.SmsFactorCriteria;
import com.stormpath.sdk.factor.sms.SmsFactorOptions;
import com.stormpath.sdk.impl.factor.DefaultFactorCriteria;

/**
 * @since 1.1.0
 */
public class DefaultSmsFactorCriteria extends DefaultFactorCriteria<FactorCriteria, SmsFactorOptions> implements SmsFactorCriteria {

    String type = FactorType.SMS.getName();

    public DefaultSmsFactorCriteria(SmsFactorOptions options) {
        super(options);
        getCustomAttributes().put("type", FactorType.SMS.getName());
    }

    @Override
    public SmsFactorCriteria withPhone() {
        getOptions().withPhone();
        return this;
    }
}
