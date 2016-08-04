/*
 * Copyright 2015 Stormpath, Inc.
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
package com.stormpath.sdk.servlet.filter;

import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.lang.Collections;
import com.stormpath.sdk.lang.Strings;
import com.stormpath.sdk.servlet.config.Factory;
import com.stormpath.sdk.servlet.util.ServletContextInitializable;
import io.jsonwebtoken.lang.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default {@link FilterChainManager} implementation maintaining a map of {@link javax.servlet.Filter Filter} instances
 * (key: filter name, value: Filter) as well as a map of chains created from these {@code Filter}s (key: filter chain
 * name, value: List&lt;Filter&gt;).  The {@code NamedFilterList} is essentially a {@link javax.servlet.FilterChain}
 * that also has a name property by which it can be looked up.
 *
 * @since 1.0.RC3
 */
public class DefaultFilterChainManager implements FilterChainManager {

    private static transient final Logger log = LoggerFactory.getLogger(DefaultFilterChainManager.class);

    private final ServletContext servletContext;
    private final Map<String, List<Filter>> filterChains; //key: chain name, value: chain
    private final Map<String, Object> configuredFilters;

    public DefaultFilterChainManager(ServletContext servletContext) {
        Assert.notNull(servletContext, "ServletContext argument cannot be null.");
        this.servletContext = servletContext;
        this.filterChains = new LinkedHashMap<>(); //iteration order is important
        this.configuredFilters = new LinkedHashMap<>();
    }

    //key: filter name, value: filter class
    public void addFilterClasses(Map<String, Class<? extends Filter>> classes) {
        if (Collections.isEmpty(classes)) {
            return;
        }
        this.configuredFilters.putAll(classes);
    }

    /**
     * @param name         the name of the filter
     * @param filteryThing a Filter instance, a Filter implementation class, a Factory&lt;Filter&gt; instance or a Factory&lt;Filter&gt; implementation class.
     */
    public void addFilter(String name, Object filteryThing) {
        if (filteryThing instanceof Class) {
            Assert.isTrue(!Filter.class.equals(filteryThing), "Cannot specify the Filter class directly.  Specify a class that implements the Filter interface.");
        }
        this.configuredFilters.put(name, filteryThing);
    }

    @SuppressWarnings("unchecked")
    protected Filter createFilter(String name, String config) throws ServletException {

        Object o = configuredFilters.get(name);

        if (o == null) {
            String msg = "There is no configured filter for filter name '" + name + "'";
            throw new IllegalArgumentException(msg);
        }

        if (o instanceof Filter) {
            return (Filter) o;
        }

        FilterBuilder builder = Filters.builder().setServletContext(servletContext).setName(name);
        boolean hasPathConfig = Strings.hasText(config);
        if (hasPathConfig) {
            builder.setPathConfig(config);
        }

        if (o instanceof Class) {
            Class c = (Class) o;
            if (Filter.class.isAssignableFrom(c)) {
                builder.setFilterClass((Class<? extends Filter>) c);
            } else if (Factory.class.isAssignableFrom(c)) {
                o = Classes.newInstance(c);
            }
        }

        if (o instanceof Factory) {
            Factory<Filter> factory = (Factory<Filter>) o;
            if (factory instanceof ServletContextInitializable) {
                ((ServletContextInitializable) factory).init(servletContext);
            }
            Filter filter = factory.getInstance();
            builder.setFilter(filter);
        }

        return builder.build();
    }

    public void createChain(String chainName, String chainDefinition) throws ServletException {
        if (!Strings.hasText(chainName)) {
            throw new NullPointerException("chainName cannot be null or empty.");
        }
        if (!Strings.hasText(chainDefinition)) {
            throw new NullPointerException("chainDefinition cannot be null or empty.");
        }

        log.debug("Creating chain [{}] from String definition [{}]", chainName, chainDefinition);

        //parse the value by tokenizing it to get the resulting filter-specific config entries
        //
        //e.g. for a value of
        //
        //     "authc, roles(admin,user), perms(file:edit)"
        //
        // the resulting token array would equal
        //
        //     { "authc", "roles(admin,user)", "perms(file:edit)" }
        //
        String[] filterTokens = splitChainDefinition(chainDefinition);

        //each token is specific to each filter.
        //strip the name and extract any filter-specific config between parenthesis ( )
        for (String token : filterTokens) {
            String[] nameConfigPair = toNameConfigPair(token);

            //now we have the filter name, path and (possibly null) path-specific config.  Let's apply them:
            addToChain(chainName, nameConfigPair[0], nameConfigPair[1]);
        }
    }

    /**
     * Splits the comma-delimited filter chain definition line into individual filter definition tokens. <p/> Example
     * Input:
     * <pre>
     *     foo, bar(baz), blah(x, y)
     * </pre>
     * Resulting Output:
     * <pre>
     *     output[0] == foo
     *     output[1] == bar(baz)
     *     output[2] == blah(x, y)
     * </pre>
     *
     * @param chainDefinition the comma-delimited filter chain definition.
     * @return an array of filter definition tokens
     * @see <a href="https://issues.apache.org/jira/browse/SHIRO-205">SHIRO-205</a>
     */
    protected String[] splitChainDefinition(String chainDefinition) {
        return Strings.split(chainDefinition, Strings.DEFAULT_DELIMITER_CHAR, '(', ')', true, true);
    }

    /**
     * Based on the given filter chain definition token (e.g. 'foo' or 'foo(bar, baz)'), this will return the token as a
     * name/value pair, removing parenthesis as necessary.  Examples: <table> <tr> <th>Input</th> <th>Result</th> </tr>
     * <tr> <td>{@code foo}</td> <td>returned[0] == {@code foo}<br/>returned[1] == {@code null}</td> </tr> <tr>
     * <td>{@code foo(bar, baz)}</td> <td>returned[0] == {@code foo}<br/>returned[1] == {@code bar, baz}</td> </tr>
     * </table>
     *
     * @param token the filter chain definition token
     * @return A name/value pair representing the filter name and a (possibly null) config value.
     * @throws java.lang.IllegalArgumentException if the token cannot be parsed
     */
    protected String[] toNameConfigPair(String token) throws IllegalArgumentException {

        try {
            String[] pair = token.split("\\(", 2);
            String name = Strings.clean(pair[0]);

            if (name == null) {
                throw new IllegalArgumentException("Filter name not found for filter chain definition token: " + token);
            }
            String config = null;

            if (pair.length == 2) {
                config = Strings.clean(pair[1]);
                //if there was an open bracket, it assumed there is a closing bracket, so strip it too:
                config = config.substring(0, config.length() - 1);
                config = Strings.clean(config);
            }

            return new String[]{name, config};

        } catch (Exception e) {
            String msg = "Unable to parse filter chain definition token: " + token;
            throw new IllegalArgumentException(msg, e);
        }
    }

    public void addToChain(String chainName, String filterName, String config) throws ServletException {
        if (!Strings.hasText(chainName)) {
            throw new IllegalArgumentException("chainName cannot be null or empty.");
        }

        Filter filter = createFilter(filterName, config);
        List<Filter> chain = ensureChain(chainName);
        chain.add(filter);
    }

    protected List<Filter> ensureChain(String chainName) {
        List<Filter> chain = getChain(chainName);
        if (chain == null) {
            chain = new ArrayList<>();
            this.filterChains.put(chainName, chain);
        }
        return chain;
    }

    public List<Filter> getChain(String chainName) {
        return this.filterChains.get(chainName);
    }

    public boolean hasChains() {
        return !com.stormpath.sdk.lang.Collections.isEmpty(this.filterChains);
    }

    public Set<String> getChainNames() {
        return this.filterChains.keySet();
    }

    public FilterChain proxy(FilterChain original, String chainName) {
        List<Filter> configured = getChain(chainName);
        if (configured == null) {
            String msg = "There is no configured chain under the name/key [" + chainName + "].";
            throw new IllegalArgumentException(msg);
        }

        return new ProxiedFilterChain(original, configured);
    }
}
