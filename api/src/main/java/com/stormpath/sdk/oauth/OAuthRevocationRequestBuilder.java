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
package com.stormpath.sdk.oauth;

/**
 * Builder class to create {@link OAuthRevocationRequest} instances.
 *
 * @since 1.2.0
 */
public interface OAuthRevocationRequestBuilder {

    /**
     * Sets the token (access_token or refresh_token) to be revoked.
     *
     * @param token to be revoked.
     */
    OAuthRevocationRequestBuilder setToken(String token);

    /**
     * Sets the optional {@link TokenTypeHint} parameter.
     *
     * @param tokenTypeHint optional parameter.
     */
    OAuthRevocationRequestBuilder setTokenTypeHint(TokenTypeHint tokenTypeHint);

    /**
     * Returns a new {@link OAuthRevocationRequest} based on the current builder state.
     *
     * @return a new {@link OAuthRevocationRequest}  based on the current builder state.
     */
    OAuthRevocationRequest build();
}
