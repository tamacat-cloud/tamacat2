/*
 * Copyright 2022 tamacat.org
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
package cloud.tamacat2.jetty;

import java.io.IOException;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.bootstrap.CustomServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.jetty.config.JettyUrlConfig;
import cloud.tamacat2.reverse.ReverseProxy;
import cloud.tamacat2.reverse.ReverseProxyHandler;
import cloud.tamacat2.reverse.config.ReverseConfig;

/**
 * Embedded Classic I/O Jetty with Reverse Proxy server.
 */
public class JettyServer extends ReverseProxy {

	static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
	
	@Override
	public void startup(final HttpConfig config) {
		final int port = config.getPort();

		final HttpServer server = createHttpServer(config);
		JettyManager.getInstance().start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				JettyManager.getInstance().stop();
				LOG.info(config.getServerName() + ":" + port + " shutting down");
				server.close(CloseMode.GRACEFUL);
			}
		});

		try {
			server.start();
			LOG.info("Listening on port " + port);
			server.awaitTermination(TimeValue.MAX_VALUE);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void register(final UrlConfig urlConfig, final CustomServerBootstrap bootstrap) {
		if (urlConfig instanceof JettyUrlConfig) {
			registerJettyEmbedded((JettyUrlConfig)urlConfig, bootstrap);
		} else {
			super.register(urlConfig, bootstrap);
		}
	}
	
	protected void registerJettyEmbedded(final JettyUrlConfig urlConfig, final CustomServerBootstrap bootstrap) {
		try {
			final JettyDeployment jettyDeploy = new JettyDeployment();
			jettyDeploy.deploy(urlConfig);

			final HttpHost targetHost = urlConfig.getHttpHost();
			urlConfig.reverse(ReverseConfig.create().url(targetHost.toURI()+urlConfig.getPath())); //add reverse config to jetty
			
			LOG.info("register: VirtualHost="+getVirtualHost(urlConfig)+", path="+urlConfig.getPath() + "* ReverseProxy+JettyEmbedded to " + targetHost);
			register(urlConfig, bootstrap, new ReverseProxyHandler(targetHost, urlConfig));
			
			//httpResponseInterceptors.add(new HtmlLinkConvertInterceptor());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
