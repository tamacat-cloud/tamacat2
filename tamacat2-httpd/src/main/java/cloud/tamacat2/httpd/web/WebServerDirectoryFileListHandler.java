/*
 * Copyright 2024 tamacat.org
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
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.httpd.error.ForbiddenException;
import cloud.tamacat2.httpd.error.HttpStatusException;
import cloud.tamacat2.httpd.error.NotFoundException;
import cloud.tamacat2.httpd.util.MimeUtils;
import cloud.tamacat2.httpd.util.StringUtils;

/**
 * Embedded HTTP/1.1 file server using classic I/O.
 * 
 * @see https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/ClassicFileServerExample.java
 */
public class WebServerDirectoryFileListHandler extends WebServerHandler {

	static final Logger ACCESS = LoggerFactory.getLogger("Access");
	static final Logger LOG = LoggerFactory.getLogger(WebServerDirectoryFileListHandler.class);
	
	public WebServerDirectoryFileListHandler(final UrlConfig urlConfig) {
		super(urlConfig);
	}

	public WebServerDirectoryFileListHandler(final String docsRoot) {
		super(docsRoot);
	}
	
	@Override
	public void handle(
            final ClassicHttpRequest request,
            final ClassicHttpResponse response,
            final HttpContext context) throws HttpException, IOException {
		try {
			//If docsRoot is null then always return 404 Not Found.
			if (docsRoot == null) {
				throw new NotFoundException();
			}
			final URI requestUri = request.getUri();			
			final HttpCoreContext coreContext = HttpCoreContext.cast(context);
			final EndpointDetails endpoint = coreContext.getEndpointDetails();
			
			final String path = requestUri.getPath();
			if (StringUtils.isEmpty(path)) {
				throw new NotFoundException();
			}
			// Normalize the path and ensure it remains within docsRoot
			final File file = new File(docsRoot, getDecodeUri(path).replace(urlConfig.getPath(), "/")).getCanonicalFile();
			if (!file.getPath().startsWith(new File(docsRoot).getCanonicalPath() + File.separator)) {
				throw new NotFoundException();
			}
			ContentType contentType = ContentType.DEFAULT_BINARY;
			if (!file.exists()) {
				LOG.debug(endpoint + ": Not found. file=" + file.getPath());
				throw new NotFoundException();
			} else if (!file.canRead()) {
				if (LOG.isTraceEnabled()) {
					LOG.trace(endpoint + ": Forbidden. file=" + file.getPath());
				}
				throw new ForbiddenException();
			} else if (file.isDirectory()) {
				final File[] listFiles = file.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return ! pathname.isHidden()
							&& ! pathname.getName().startsWith(".");
					}
				});
				Arrays.sort(listFiles, new FileSort());
				final Collection<File> files = Arrays.asList(listFiles);
				final String html = new DirectoryFileListHtmlGenerator().html(files);
				setEntity(response, new StringEntity(html, ContentType.TEXT_HTML));
				response.setCode(HttpStatus.SC_OK);
				ACCESS.info(request+" 200 [OK]");
				return;
			}
			
			final String mime = MimeUtils.getContentType(path);
			if (contentType != null) {
				contentType = ContentType.parse(mime);
			}
			if (LOG.isTraceEnabled()) {
				LOG.trace(endpoint + ": read file " + file.getAbsolutePath()+ ", Content-Type="+contentType);
			}
			setEntity(response, new FileEntity(file, contentType));
			response.setCode(HttpStatus.SC_OK);
			ACCESS.info(request+" 200 [OK]");
		} catch (HttpStatusException e) {
			handleException(request, response, context, e);
		} catch (Exception e) {
			e.printStackTrace();
			handleException(request, response, context, defaultException);
		}
	}
	
	static class FileSort implements Comparator<File> {
		public int compare(final File src, final File target) {
			if (src.isDirectory() && target.isFile()) return -1;
			if (src.isFile() && target.isDirectory()) return 1;
			int diff = src.getName().compareTo(target.getName());
			return diff;
		}
	}
}
