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
package cloud.tamacat2.reverse;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.bootstrap.CustomServerBootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.WebServer;
import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;

public class ReverseProxy extends WebServer {

    static final Logger LOG = LoggerFactory.getLogger(ReverseProxy.class);
	
	protected void register(final UrlConfig urlConfig, final CustomServerBootstrap bootstrap) {
		if (urlConfig instanceof ReverseUrlConfig) {
			registerReverseProxy((ReverseUrlConfig)urlConfig, bootstrap);
		} else {
			registerWebServer(urlConfig, bootstrap);
		}
	}
	protected void registerReverseProxy(final ReverseUrlConfig urlConfig, final CustomServerBootstrap bootstrap) {
		try {
			final HttpHost targetHost = HttpHost.create(urlConfig.getReverse().getTarget().toURI());
			LOG.info("register: VirtualHost="+getVirtualHost(urlConfig)+", path="+urlConfig.getPath()+"* ReverseProxy to "+targetHost);
			register(urlConfig, bootstrap, new ReverseProxyHandler(targetHost, urlConfig));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
