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
package cloud.tamacat2.httpd.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.httpd.error.ErrorPageTemplate;
import cloud.tamacat2.httpd.error.ForbiddenException;
import cloud.tamacat2.httpd.error.HttpStatusException;
import cloud.tamacat2.httpd.error.NotFoundException;
import cloud.tamacat2.httpd.util.AccessLogUtils;
import cloud.tamacat2.httpd.util.ExceptionUtils;
import cloud.tamacat2.httpd.util.HeaderUtils;
import cloud.tamacat2.httpd.util.MimeUtils;
import cloud.tamacat2.httpd.util.StringUtils;

/**
 * Embedded HTTP/1.1 file server using classic I/O.
 * 
 * @see https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/ClassicFileServerExample.java
 */
public class WebServerHandler implements HttpRequestHandler {

	static final Logger LOG = LoggerFactory.getLogger(WebServerHandler.class);

	protected ClassLoader loader;
	protected String welcomeFile = "index.html";
	protected Properties props;

	protected UrlConfig urlConfig;
	protected File docsRoot;
	protected HttpStatusException defaultException = new NotFoundException();
	
	public WebServerHandler(final UrlConfig urlConfig) {
		this(urlConfig.getDocsRoot());
		this.urlConfig = urlConfig;
	}

	public WebServerHandler(final String docsRoot) {
		if (docsRoot != null) {
			this.docsRoot = new File(docsRoot);
		}
	}
	
	@Override
	public void handle(
            final ClassicHttpRequest request,
            final ClassicHttpResponse response,
            final HttpContext context) throws HttpException, IOException {
		long startTime = System.currentTimeMillis();
		try {
			//If docsRoot is null then always return 404 Not Found.
			if (docsRoot == null) {
				throw new NotFoundException();
			}
			final URI requestUri = request.getUri();			
			final HttpCoreContext coreContext = HttpCoreContext.cast(context);
			final EndpointDetails endpoint = coreContext.getEndpointDetails();
			
			String path = requestUri.getPath();
			if (StringUtils.isEmpty(path) || path.contains("..")) {
				throw new NotFoundException();
			}
			if (path.endsWith("/")) {
				path = path + welcomeFile;
			}
			final File file = new File(docsRoot, getDecodeUri(path).replace(urlConfig.getPath(), "/"));
			if (!file.exists()) {
				//if (LOG.isTraceEnabled()) {
					LOG.debug(endpoint + ": Not found. file=" + file.getPath());
				//}
				throw new NotFoundException();
			} else if (!file.canRead() || file.isDirectory()) {
				if (LOG.isTraceEnabled()) {
					LOG.trace(endpoint + ": Forbidden. file=" + file.getPath());
				}
				throw new ForbiddenException();
			}
			ContentType contentType = ContentType.DEFAULT_BINARY;
			final String mime = MimeUtils.getContentType(path);
			if (contentType != null) {
				contentType = ContentType.parse(mime);
			}
			if (LOG.isTraceEnabled()) {
				LOG.trace(endpoint + ": read file " + file.getAbsolutePath()+ ", Content-Type="+contentType);
			}
			setEntity(response, new FileEntity(file, contentType));
			response.setCode(HttpStatus.SC_OK);
		} catch (HttpStatusException e) {
			handleException(request, response, context, e);
		} catch (Exception e) {
			LOG.warn(ExceptionUtils.getStackTrace(e, 100));
			handleException(request, response, context, defaultException);
		} finally {
			AccessLogUtils.log(request, response, context, (System.currentTimeMillis() - startTime));
		}
	}

	protected void handleException(final HttpRequest req, final HttpResponse resp, final HttpContext context,
			final HttpStatusException e) throws HttpException, IOException {
		String accept = HeaderUtils.getHeader(req, HttpHeaders.ACCEPT);
		if (accept != null && accept.startsWith(ContentType.APPLICATION_JSON.getMimeType())) {
			setEntity(resp, new StringEntity(ErrorPageTemplate.create().getJson(e), ContentType.APPLICATION_JSON));	
		} else {
			setEntity(resp, new StringEntity(ErrorPageTemplate.create().getHtml(e), ContentType.TEXT_HTML));
		}
		resp.setCode(e.getHttpStatus());
	}
	
	protected void setEntity(final HttpResponse response, final HttpEntity entity) {
		if (response instanceof HttpEntityContainer) {
			((HttpEntityContainer)response).setEntity(entity);
		}
	}
	
	protected String getDecodeUri(final String uri) {
		String decoded = URLDecoder.decode(uri, StandardCharsets.UTF_8);
		if (StringUtils.isEmpty(decoded) || decoded.contains("..")) {
			throw new NotFoundException();
		}
		return decoded;
	}
	
	protected void setDefaultException(final HttpStatusException exception) {
		this.defaultException = exception;
	}
	
	/**
	 * <p>
	 * Get the ClassLoader, default is getClass().getClassLoader().
	 * @return
	 */
	public ClassLoader getClassLoader() {
		return loader != null ? loader : getClass().getClassLoader();
	}
}
