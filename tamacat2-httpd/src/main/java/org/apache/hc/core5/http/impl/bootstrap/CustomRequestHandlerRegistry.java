/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.core5.http.impl.bootstrap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestMapper;
import org.apache.hc.core5.http.MisdirectedRequestException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.LookupRegistry;
import org.apache.hc.core5.http.protocol.UriPatternMatcher;
import org.apache.hc.core5.http.protocol.UriPatternType;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;

/**
 * Override httpcore5-5.2.2 org.apache.hc.core5.http.impl.bootstrap.RequestHandlerRegistry.java
 *   Override resolve(request, context): patternMatcher is null then throws "Not authoritative" -> return primary.lookup(path)
 * 
 * Generic registry of request handlers that can be resolved by properties of request messages.
 *
 * @param <T> request handler type.
 *
 * @since 5.0
 */
@Deprecated
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
public class CustomRequestHandlerRegistry<T> implements HttpRequestMapper<T> {

    private final static String LOCALHOST = "localhost";
    private final static String IP_127_0_0_1 = "127.0.0.1";

    private final String canonicalHostName;
    private final Supplier<LookupRegistry<T>> registrySupplier;
    private final LookupRegistry<T> primary;
    private final ConcurrentMap<String, LookupRegistry<T>> virtualMap;

    public CustomRequestHandlerRegistry(final String canonicalHostName, final Supplier<LookupRegistry<T>> registrySupplier) {
        this.canonicalHostName = TextUtils.toLowerCase(Args.notNull(canonicalHostName, "Canonical hostname"));
        this.registrySupplier = registrySupplier != null ? registrySupplier : UriPatternMatcher::new;
        this.primary = this.registrySupplier.get();
        this.virtualMap = new ConcurrentHashMap<>();
    }

    public CustomRequestHandlerRegistry(final String canonicalHostName, final UriPatternType patternType) {
        this(canonicalHostName, () -> UriPatternType.newMatcher(patternType));
    }

    public CustomRequestHandlerRegistry(final UriPatternType patternType) {
        this(LOCALHOST, patternType);
    }

    public CustomRequestHandlerRegistry() {
        this(LOCALHOST, UriPatternType.URI_PATTERN);
    }

    private LookupRegistry<T> getPatternMatcher(final String hostname) {
        if (hostname == null ||
                hostname.equals(canonicalHostName) || hostname.equals(LOCALHOST) || hostname.equals(IP_127_0_0_1)) {
            return primary;
        }
        return virtualMap.get(hostname);
    }

    @Override
    public T resolve(final HttpRequest request, final HttpContext context) throws MisdirectedRequestException {
        final URIAuthority authority = request.getAuthority();
        final String key = authority != null ? TextUtils.toLowerCase(authority.getHostName()) : null;
        final LookupRegistry<T> patternMatcher = getPatternMatcher(key);
        if (patternMatcher == null) {
            //throw new MisdirectedRequestException("Not authoritative");
            //throw new NotFoundException();
        }
        String path = request.getPath();
        final int i = path.indexOf('?');
        if (i != -1) {
            path = path.substring(0, i);
        }
        if (patternMatcher == null) {
        	return primary.lookup(path);
        }
        return patternMatcher.lookup(path);
    }

    public void register(final String hostname, final String uriPattern, final T object) {
        Args.notBlank(uriPattern, "URI pattern");
        if (object == null) {
            return;
        }
        final String key = TextUtils.toLowerCase(hostname);
        if (hostname == null || hostname.equals(canonicalHostName) || hostname.equals(LOCALHOST)) {
            primary.register(uriPattern, object);
        } else {
            LookupRegistry<T> patternMatcher = virtualMap.get(key);
            if (patternMatcher == null) {
                final LookupRegistry<T> newPatternMatcher = registrySupplier.get();
                patternMatcher = virtualMap.putIfAbsent(key, newPatternMatcher);
                if (patternMatcher == null) {
                    patternMatcher = newPatternMatcher;
                }
            }
            patternMatcher.register(uriPattern, object);
        }
    }
}
