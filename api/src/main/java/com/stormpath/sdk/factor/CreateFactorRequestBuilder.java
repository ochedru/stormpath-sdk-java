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

import com.stormpath.sdk.challenge.Challenge;

/**
 * A Builder to construct {@link CreateFactorRequest}s.
 *
 * @see com.stormpath.sdk.account.Account#createFactor(CreateFactorRequest)
 *
 * @param <T> a subclass of {@link Factor} specifying the kind of {@code Factor} created by this {@code CreateFactorRequest}.
 * @param <O> a subclass of {@link FactorOptions} specifying the kind of {@code FactorOptions} to be used for creating a {@link Factor}.
 * @since 1.1.0
 */
public interface CreateFactorRequestBuilder <T extends Factor, O extends FactorOptions>{

    /**
     * Ensures that after a Factor is created, the creation response is retrieved with the specified factors's
     * options. This enhances performance by leveraging a single request to retrieve multiple related
     * resources you know you will use.
     *
     * @return the builder instance for method chaining.
     * @throws IllegalArgumentException if {@code options} is null.
     */
    CreateFactorRequestBuilder withResponseOptions(O options) throws IllegalArgumentException;

    /**
     * Ensures that once a Factor is created, it is also challenged at the same time.
     * This will also create a {@link Challenge} resource
     *
     * @return the builder instance for method chaining.
     */
    CreateFactorRequestBuilder createChallenge();

    /**
     * Creates a new {@code CreateFactorRequest} instance based on the current builder state.
     *
     * @return a new {@code CreateFactorRequest} instance based on the current builder state.
     */
    CreateFactorRequest<T, O> build();
}
