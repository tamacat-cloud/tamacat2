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
package cloud.tamacat2.tomcat;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.bootstrap.CustomServerBootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.reverse.ReverseProxy;
import cloud.tamacat2.reverse.ReverseProxyHandler;
import cloud.tamacat2.reverse.config.ReverseConfig;
import cloud.tamacat2.tomcat.config.TomcatConfig;

/**
 * Embedded Classic I/O Tomcat with Reverse Proxy server.
 */
public class TomcatServer extends ReverseProxy {

	static final Logger LOG = LoggerFactory.getLogger(TomcatServer.class);
	
	public TomcatServer() {
		addPluginServer(TomcatManager.getInstance());
	}

	@Override
	protected void register(final UrlConfig urlConfig, final CustomServerBootstrap bootstrap) {
		if (urlConfig instanceof TomcatConfig) {
			registerJettyEmbedded((TomcatConfig)urlConfig, bootstrap);
		} else {
			super.register(urlConfig, bootstrap);
		}
	}
	
	protected void registerJettyEmbedded(final TomcatConfig urlConfig, final CustomServerBootstrap bootstrap) {
		try {
			final TomcatDeployment jettyDeploy = new TomcatDeployment();
			jettyDeploy.deploy(urlConfig);

			final HttpHost targetHost = urlConfig.getHttpHost();
			urlConfig.reverse(ReverseConfig.create().url(targetHost.toURI()+urlConfig.getPath())); //add reverse config to jetty
			
			LOG.info("register: VirtualHost="+getVirtualHost(urlConfig)+", path="+urlConfig.getPath() + "* ReverseProxy+TomcatEmbedded to " + targetHost);
			register(urlConfig, bootstrap, new ReverseProxyHandler(targetHost, urlConfig));
			
			//httpResponseInterceptors.add(new HtmlLinkConvertInterceptor());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
