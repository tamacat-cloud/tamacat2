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
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.io.HttpFilterHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.httpd.error.ErrorPageTemplate;
import cloud.tamacat2.httpd.error.HttpStatusException;

/**
 * Request/Response Filter implements HttpFilterHandler.
 */
public abstract class HttpFilter implements HttpFilterHandler {

	protected UrlConfig urlConfig;
	protected String path;
	
	public void setUrlConfig(final UrlConfig urlConfig) {
		this.urlConfig = urlConfig;
		this.path = urlConfig.getPath();
	}
	
	public HttpFilter urlConfig(final UrlConfig urlConfig) {
		setUrlConfig(urlConfig);
		return this;
	}
	
	@Override
	public void handle(final ClassicHttpRequest req, final HttpFilterChain.ResponseTrigger responseTrigger, 
			final HttpContext context, HttpFilterChain chain) throws HttpException, IOException {
		try {
			if (req.getPath().startsWith(urlConfig.getPath()) == false) {
				chain.proceed(req, responseTrigger, context);
			} else {
				handleRequest(req, context);
				
				chain.proceed(req, new HttpFilterChain.ResponseTrigger() {
					@Override
					public void sendInformation(final ClassicHttpResponse resp) throws HttpException, IOException {
						handleSendInformation(resp, context);
						responseTrigger.sendInformation(resp);
					}
		
					@Override
					public void submitResponse(final ClassicHttpResponse resp) throws HttpException, IOException {
						handleSubmitResponse(resp, context);
						responseTrigger.submitResponse(resp);
					}
				}, context);
			}
		} catch (Exception e) {
			HttpStatusException cause = null;
			if (e instanceof HttpStatusException) {
				cause = (HttpStatusException)e;
			} else {
				cause = new HttpStatusException(503, e.getMessage(), e);
			}
			StringEntity entity = new StringEntity(ErrorPageTemplate.create().getHtml(cause), ContentType.TEXT_HTML);
			chain.proceed(req, new HttpFilterChain.ResponseTrigger() {
				@Override
				public void sendInformation(final ClassicHttpResponse resp) throws HttpException, IOException {
					handleSendInformation(resp, context);
					responseTrigger.sendInformation(resp);
				}
	
				@Override
				public void submitResponse(final ClassicHttpResponse resp) throws HttpException, IOException {
					handleSubmitResponse(resp, context);
					resp.setEntity(entity);
					responseTrigger.submitResponse(resp);
				}
			}, context);
		}
	}
	
	/**
	 * Override this method.
	 */
	protected void handleRequest(final ClassicHttpRequest req, final HttpContext context) throws HttpException, IOException {
	}

	/**
	 * Override this method.
	 */
	protected void handleSendInformation(final ClassicHttpResponse resp, final HttpContext context) throws HttpException, IOException {
	}

	/**
	 * Override this method.
	 */
	protected void handleSubmitResponse(final ClassicHttpResponse resp, final HttpContext context) throws HttpException, IOException {
	}
}
