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

import com.stormpath.sdk.query.Criteria;

/**
 * A {@link Factor}-specific {@link Criteria} class, enabling a Factor-specific
 * <a href="http://en.wikipedia.org/wiki/Fluent_interface">fluent</a>query DSL. FactorCriteria instances can be
 * constructed by using the {@link Factors} utility class, for example:
 * <pre>
 * Factors.SMS.where(Factors.SMS.status().eq(FactorStatus.DISABLED)
 *     .and(Factors.SMS.verificationStatus().eq(FactorVerificationStatus.VERIFIED))
 *     .limitTo(10));
 * </pre>
 * <h2>Sort Order</h2>
 * <p/>
 * All of the {@code orderBy*} methods append an {@code orderBy} clause to the query, ensuring the query results reflect
 * a particular sort order.
 * <p/>
 * The default sort order is always {@code ascending}, but can be changed to {@code descending} by calling the
 * {@link #descending()} method <em>immediately</em> after the {@code orderBy} method call.  For example:
 * <pre>
 * ...criteria.orderByStatus()<b>.descending()</b>...
 * </pre>
 * <h3>Multiple Order Statements</h3>
 * You may specify multiple {@code orderBy} clauses and the query results will ordered, reflecting {@code orderBy}
 * statements <em>in the order they are declared</em>.  For example, to order the results by status (descending),
 * you would chain {@code orderBy} statements:
 * <pre>
 * ...criteria
 *     .orderByStatus().descending()
 *     ...
 * </pre>
 *
 * @since 1.1.0
 */
public interface FactorCriteria extends Criteria<FactorCriteria>,  FactorOptions<FactorCriteria>{


    /**
     * Ensures that the query results are ordered by status {@link Factor#getStatus() status}.
     * <p/>
     * Please see the {@link FactorCriteria class-level documentation} for controlling sort order (ascending or
     * descending) and chaining multiple {@code orderBy} clauses.
     *
     * @return this instance for method chaining
     */
    FactorCriteria orderByStatus();

    /**
     * Ensures that the query results are ordered by verificationStatus {@link Factor#getFactorVerificationStatus() verificationStatus}.
     * <p/>
     * Please see the {@link FactorCriteria class-level documentation} for controlling sort order (ascending or
     * descending) and chaining multiple {@code orderBy} clauses.
     *
     * @return this instance for method chaining
     */
    FactorCriteria orderByVerificationStatus();

    /**
     * Ensures that the query results are ordered by createdAt Date {@link Factor#getCreatedAt() createdAt}.
     * <p/>
     * Please see the {@link FactorCriteria class-level documentation} for controlling sort order (ascending or
     * descending).
     *
     * @return this instance for method chaining
     */
    FactorCriteria orderByCreatedAt();
}
