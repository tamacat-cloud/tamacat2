/*
 * Copyright 2023 tamacat.org
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
package cloud.tamacat2.httpd.config;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.filter.HttpFilter;
import cloud.tamacat2.httpd.util.ServerUtils;
import cloud.tamacat2.httpd.util.StringUtils;

public class UrlConfig {
	
    static final Logger LOG = LoggerFactory.getLogger(UrlConfig.class);

	protected String host;
	protected String path;
	protected String docsRoot;
		
	protected HttpConfig httpConfig;
	protected String serverHome;

	final protected Collection<HttpFilter> httpFilters = new ArrayList<>();
	
	public static UrlConfig create() {
		return new UrlConfig();
	}
	
	public UrlConfig httpConfig(final HttpConfig httpConfig) {
		this.httpConfig = httpConfig;
		return this;
	}
	
	public HttpConfig getHttpConfig() {
		return httpConfig;
	}
	
	public String getHostname() {
		return host;
	}
	
	public URL getHost() {
		try {
			return new URI(host).toURL();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Configure Virtual Host
	 * @param host
	 */
	public UrlConfig host(final String host) {
		this.host = host;
		return this;
	}
	
	public String getPath() {
		return path;
	}

	public UrlConfig path(final String path) {
		this.path = path;
		return this;
	}
	
	/**
	 * Get a docsRoot.
	 * Replaces docRoot if environment variable ${server.home} is set.
	 * If docsRoot is null then always return 404 Not Found. 
	 */
	public String getDocsRoot() {
		String docsRoot = this.docsRoot;
		final String serverHome = getServerHome();
		if (docsRoot == null) {
			return null;
		}
		if (docsRoot.indexOf("${server.home}") >= 0) {
			docsRoot = docsRoot.replace("${server.home}", serverHome);
		}
		return docsRoot;
	}

	/**
	 * For docsRoot, specify the location where static web content such as HTML and CSS files are placed.
	 * @param docsRoot
	 */
	public UrlConfig docsRoot(final String docsRoot) {
		this.docsRoot = docsRoot;
		return this;
	}
	
	protected String getServerHome() {
		if (StringUtils.isEmpty(serverHome)) {
			serverHome = ServerUtils.getServerHome();
		}
		return serverHome;
	}
	
	public Collection<HttpFilter> getHttpFilters() {
		return httpFilters;
	}
	
	public UrlConfig filter(HttpFilter filter) {
		httpFilters.add(filter);
		return this;
	}
}
