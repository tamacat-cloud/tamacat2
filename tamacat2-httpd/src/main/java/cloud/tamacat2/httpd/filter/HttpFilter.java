/*
 * Copyright 2020 tamacat.org
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
package cloud.tamacat2.httpd.filter;

import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.io.HttpFilterHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat2.httpd.config.UrlConfig;

/**
 * Request/Response Filter
 */
public abstract class HttpFilter implements HttpFilterHandler {

	protected UrlConfig urlConfig;
	protected String path;
	
	public void setServerConfig(final UrlConfig urlConfig) {
		this.urlConfig = urlConfig;
		this.path = urlConfig.getPath();
	}
	
	public HttpFilter serverConfig(final UrlConfig urlConfig) {
		setServerConfig(urlConfig);
		return this;
	}
	
	@Override
	public void handle(final ClassicHttpRequest request, final HttpFilterChain.ResponseTrigger responseTrigger, 
			final HttpContext context, HttpFilterChain chain) throws HttpException, IOException {
		
		if (request.getPath().startsWith(urlConfig.getPath()) == false) {
			chain.proceed(request, responseTrigger, context);
		} else {
			handleRequest(request, context);
			
			chain.proceed(request, new HttpFilterChain.ResponseTrigger() {
				@Override
				public void sendInformation(final ClassicHttpResponse response) throws HttpException, IOException {
					handleSendInformation(response, context);
					responseTrigger.sendInformation(response);
				}
	
				@Override
				public void submitResponse(final ClassicHttpResponse response) throws HttpException, IOException {
					handleSubmitResponse(response, context);
					responseTrigger.submitResponse(response);
				}
			}, context);
		}
	}
	
	protected void handleRequest(final ClassicHttpRequest request, final HttpContext context) throws HttpException, IOException {
	}
	
	protected void handleSendInformation(final ClassicHttpResponse response, final HttpContext context) throws HttpException, IOException {
	}

	protected void handleSubmitResponse(final ClassicHttpResponse response, final HttpContext context) throws HttpException, IOException {
	}
}
