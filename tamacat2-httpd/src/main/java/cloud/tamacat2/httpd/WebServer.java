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
package cloud.tamacat2.httpd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.CustomServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.protocol.HttpProcessorBuilder;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ssl.TlsCiphers;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.config.HttpsConfig;
import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.httpd.filter.TraceExceptionListener;
import cloud.tamacat2.httpd.filter.TraceHttp1StreamListener;
import cloud.tamacat2.httpd.plugin.PluginServer;
import cloud.tamacat2.httpd.ssl.SSLContextCreator;
import cloud.tamacat2.httpd.util.StringUtils;
import cloud.tamacat2.httpd.web.GzipContentEncodingInterceptor;
import cloud.tamacat2.httpd.web.WebServerDirectoryFileListHandler;
import cloud.tamacat2.httpd.web.WebServerHandler;

public class WebServer {

    static final Logger LOG = LoggerFactory.getLogger(WebServer.class);
	
	public WebServer() {
		Locale.setDefault(Locale.US);
	}

	protected final Collection<HttpRequestInterceptor> httpRequestInterceptors = new ArrayList<>();
	protected final Collection<HttpResponseInterceptor> httpResponseInterceptors = new ArrayList<>();

	protected final Collection<PluginServer> pluginServers = new ArrayList<>();
	
	public void startup(final HttpConfig config) {
		final int port = config.getPort();
		final HttpServer server = createHttpServer(config);

		startPluginServers();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
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
	
	public void addPluginServer(final PluginServer pluginServer) {
		pluginServers.add(pluginServer);
	}
	
	protected void startPluginServers() {
		for (final PluginServer plugin : pluginServers) {
			plugin.start();
		}
	}
	
	public HttpServer createHttpServer(final HttpConfig config) {
		final Collection<UrlConfig> configs = config.getUrlConfigs();

		final CustomServerBootstrap bootstrap = CustomServerBootstrap.bootstrap()
				.setHttpProcessor(HttpProcessors.customServer(config.getServerName()).build())
				.setCanonicalHostName(config.getCanonicalHostName()) //Not authoritative
				.setListenerPort(config.getPort())
				//.setStreamListener(new TraceHttp1StreamListener("client<-httpd"))
				//.setSocketConfig(SocketConfig.custom()
				//.setSoKeepAlive(config.keepAlive())
				//.setSoReuseAddress(config.soReuseAddress())
				//.setSoTimeout(config.getSoTimeout(), TimeUnit.SECONDS).build()
				;
		
		// HTTPS
		if (config.useHttps()) {
			final HttpsConfig https = config.getHttpsConfig();
			final SSLContext sslContext = new SSLContextCreator(https).getSSLContext();
			bootstrap.setSslSetupHandler(sslParameters -> {
				sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
				sslParameters.setCipherSuites(TlsCiphers.excludeWeak(sslParameters.getCipherSuites()));
				if (https.useClientAuth()) {
					sslParameters.setNeedClientAuth(true);
				}
			});
			bootstrap.setSslContext(sslContext);
		}

		for (final UrlConfig urlConfig : configs) {
			register(urlConfig.httpConfig(config), bootstrap);
			
			//add HttpFilters
			urlConfig.getHttpFilters().forEach((filter) -> {
				filter.setUrlConfig(urlConfig);
				bootstrap.addFilterFirst(filter.toString(), filter);
			});
		}
		
		//support Content-Encoding: gzip
		if ("gzip".equalsIgnoreCase(config.getContentEncoding())) {
			addHttpRequestInterceptor(new GzipContentEncodingInterceptor());
			addHttpResponseInterceptor(new GzipContentEncodingInterceptor());
		}
		
		final HttpProcessorBuilder httpProcessorBuilder = HttpProcessors.customServer(config.getServerName());
		httpRequestInterceptors.forEach(i-> httpProcessorBuilder.add(i));
		httpResponseInterceptors.forEach(i-> httpProcessorBuilder.add(i));
		
		bootstrap.setHttpProcessor(httpProcessorBuilder.build());
		bootstrap.setStreamListener(new TraceHttp1StreamListener())
				 .setExceptionListener(new TraceExceptionListener());

		final HttpServer server = bootstrap.create();
		return server;
	}
	
	protected void register(final UrlConfig urlConfig, final CustomServerBootstrap bootstrap) {
		registerWebServer(urlConfig, bootstrap);
	}

	protected void registerWebServer(final UrlConfig urlConfig, final CustomServerBootstrap bootstrap) {
		if (urlConfig.useDirectoryListing()) {
			register(urlConfig, bootstrap, new WebServerDirectoryFileListHandler(urlConfig));
		} else {
			register(urlConfig, bootstrap, new WebServerHandler(urlConfig));
		}
	}

	protected void register(final UrlConfig urlConfig, final CustomServerBootstrap bootstrap, final HttpRequestHandler handler) {
		try {
			if (StringUtils.isNotEmpty(urlConfig.getHostname())) {
				LOG.info("register: VirtualHost="+getVirtualHost(urlConfig)+", path="+urlConfig.getPath() +"* WebServer");
				bootstrap.register(urlConfig.getHostname(), urlConfig.getPath() + "*", handler);
			} else {
				LOG.info("register: path="+urlConfig.getPath() +"* WebServer");
				bootstrap.register(urlConfig.getPath() + "*", handler);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
	}
	
	protected String getVirtualHost(final UrlConfig serviceConfig) {
		return StringUtils.isNotEmpty(serviceConfig.getHostname()) ? serviceConfig.getHostname() : "default";
	}
	
//	protected void registerRedirect(final ServiceConfig serviceConfig, final ServerBootstrap bootstrap) {
//		try {
//			final HttpHost targetHost = HttpHost.create(serviceConfig.getReverse().getTarget().toURI());
//			LOG.info("register: VirtualHost="+getVirtualHost(serviceConfig)+", path="+serviceConfig.getPath()+"* Redirect to "+targetHost);
//			register(serviceConfig, bootstrap, new RedirectHandler(targetHost, serviceConfig));
//		} catch (Exception e) {
//			LOG.error(e.getMessage(), e);
//		}
//	}
//	
//	protected String getVirtualHost(final ServiceConfig serviceConfig) {
//		return StringUtils.isNotEmpty(serviceConfig.getHostname()) ? serviceConfig.getHostname() : "default";
//	}

	public WebServer addHttpRequestInterceptor(final HttpRequestInterceptor interceptor) {
		httpRequestInterceptors.add(interceptor);
		return this;
	}

	public WebServer addHttpResponseInterceptor(final HttpResponseInterceptor interceptor) {
		httpResponseInterceptors.add(interceptor);
		return this;
	}
}
