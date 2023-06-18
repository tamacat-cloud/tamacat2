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

import java.util.ArrayList;
import java.util.Collection;

import cloud.tamacat2.httpd.util.StringUtils;

public class HttpConfig {
	
	protected String serverName = "Httpd";
	protected int port = 80;
	protected boolean useHttps;
	protected HttpsConfig httpsConfig;
	
	protected int maxTotal = 100;
	protected int maxParRoute = 20;
	protected int soTimeout = 60;
	protected String contentEncoding;
	
	protected Collection<UrlConfig> urlConfigs = new ArrayList<>();
	
	public static HttpConfig create() {
		return new HttpConfig().urlConfig(UrlConfig.create().path("/"));
	}
	
	public HttpConfig serverName(final String serverName) {
		if (StringUtils.isNotEmpty(serverName)) {
			this.serverName = serverName;
		}
		return this;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public int getPort() {
		return port;
	}
	
	public HttpConfig port(final Integer port) {
		if (port != null) {
			this.port = port.intValue();
		}
		return this;
	}
	
	public boolean useHttps() {
		return httpsConfig != null;
	}

	public HttpConfig https(final HttpsConfig httpsConfig) {
		this.httpsConfig = httpsConfig;
		return this;
	}
	
	public HttpsConfig getHttpsConfig() {
		return httpsConfig;
	}
	
	public Collection<UrlConfig> getUrlConfigs() {
		return urlConfigs;
	}
	
	public HttpConfig urlConfig(final UrlConfig urlConfig) {
		this.urlConfigs.add(urlConfig);
		return this;
	}
	
	public HttpConfig urlConfigs(final Collection<UrlConfig> urlConfigs) {
		this.urlConfigs = urlConfigs;
		return this;
	}
	
	public int getMaxTotal() {
		return maxTotal;
	}

	public int getMaxParRoute() {
		return maxParRoute;
	}
	
	public int getSoTimeout() {
		return soTimeout;
	}
	
	public HttpConfig contentEncoding(final String contentEncoding) {
		this.contentEncoding = contentEncoding;
		return this;
	}
	
	public String getContentEncoding() {
		return contentEncoding;
	}
}
