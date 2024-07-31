/*
 * Copyright 2019 tamacat.org
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
package cloud.tamacat2.httpd.util;

import java.net.InetSocketAddress;

import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessLogUtils {
	
	static final Logger ACCESS = LoggerFactory.getLogger("Access");

	public static String getRemoteAddress(final HttpContext httpContext) {
		EndpointDetails ip = (EndpointDetails) httpContext.getAttribute("http.connection-endpoint");
		if (ip != null) {
			return ((InetSocketAddress)ip.getRemoteAddress()).getAddress().getHostAddress();
		} else {
			return "";
		}
	}
	
	public static void log(final HttpRequest req, final HttpResponse resp, final HttpContext context, final long responseTime) {
		ACCESS.info(format(req, resp, context, responseTime));
	}
	
	public static void log(final Logger log, final HttpRequest req, final HttpResponse resp, final HttpContext context, final long responseTime) {
		log.info(format(req, resp, context, responseTime));
	}
	
	public static void trace(final Logger log, final HttpRequest req, final HttpResponse resp, final HttpContext context, final long responseTime) {
		for (final Header h : req.getHeaders()) {
			log.trace("[req] "+h.toString());
		}
		for (final Header h : resp.getHeaders()) {
			log.trace("[resp] "+h.toString());
		}
		log.trace(format(req, resp, context, responseTime));
	}
	
	public static void debug(final Logger log, final HttpRequest req, final HttpResponse resp, final HttpContext context, final long responseTime) {
		if (log.isDebugEnabled()) {
			log.debug(format(req, resp, context, responseTime));
		}
	}
	
	public static void warn(final Logger log, final HttpRequest req, final HttpResponse resp, final HttpContext context, final long responseTime) {
		log.warn(format(req, resp, context, responseTime));
	}
	
	public static void error(final Logger log, final HttpRequest req, final HttpResponse resp, final HttpContext context, final long responseTime) {
		log.error(format(req, resp, context, responseTime));
	}
	
	public static String format(final HttpRequest req, final HttpResponse resp, final HttpContext context, final long responseTime) {
		return getRemoteAddress(context) +" "+ req+ " "+resp.getCode() + " "+ responseTime+"ms";
	}
}
