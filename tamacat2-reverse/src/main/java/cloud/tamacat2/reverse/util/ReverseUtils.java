/*
 * Copyright 2009 tamacat.org
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
package cloud.tamacat2.reverse.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.httpd.util.HeaderUtils;
import cloud.tamacat2.httpd.util.PropertyUtils;
import cloud.tamacat2.httpd.util.RequestUtils;
import cloud.tamacat2.httpd.util.StringUtils;
import cloud.tamacat2.reverse.config.ReverseConfig;

/**
 * <p>The utility class for reverse proxy.<br>
 *  When customize a request/response header to remove in reverse
 *  to the origin server, create the "reverse-header.properties" in CLASSPATH.
 * <pre>Configuration: reverse-header.properties
 * {@code
 * request.removeHeaders=Content-Length,Transfer-Encoding,Accept-Encoding,...
 * response.removeHeaders=Content-Type,Content-Encoding,Content-Length,...
 * }</pre>
 */
public class ReverseUtils {

	static final Logger LOG = LoggerFactory.getLogger(ReverseUtils.class);

	static Pattern PATTERN = Pattern.compile(
		"<[^<]*\\s+(href|src|action)=('|\")([^('|\")]*)('|\")[^>]*>",
		Pattern.CASE_INSENSITIVE
	);

	static final String HEADER_PROPERTIES = "reverse-header.properties";
	static final String DEFAULT_HEADER_PROPERTIES = "cloud/tamacat2/reverse/util/reverse-header.properties";
	//Remove hop-by-hop request headers.
	static final String DEFAULT_REMOVE_REQUEST_HEADERS = "removeHeaders=Content-Length,Transfer-Encoding,Accept-Encoding,Connection,Keep-Alive,Proxy-Authenticate,Proxy-Authorization,TE,Trailers,Upgrade,Range";

	//Remove hop-by-hop response headers.
	static final String DEFAULT_REMOVE_RESPONSE_HEADERS = "Content-Type,Content-Encoding,Content-Length,Transfer-Encoding,Connection,Keep-Alive,TE,Trailers,Upgrade,Content-MD5";
	
	private static final Set<String> removeRequestHeaders = new HashSet<>();
	private static final Set<String> removeResponseHeaders = new HashSet<>();

	//Configuration of remove request/response headers.
	static {
		Properties props = null;
		try {
			props = PropertyUtils.getProperties(HEADER_PROPERTIES);
		} catch (Exception e) {
			try {
				props = PropertyUtils.getProperties(DEFAULT_HEADER_PROPERTIES);
			} catch (Exception e2) {
				props = new Properties();
				props.setProperty("response.removeHeaders", DEFAULT_REMOVE_REQUEST_HEADERS);
				props.setProperty("request.removeHeaders", DEFAULT_REMOVE_RESPONSE_HEADERS);
			}
		}
		if (props != null) {
			final String removeHeaders1 = props.getProperty("request.removeHeaders");
			final String[] headers1 = removeHeaders1.split(",");
			for (final String h : headers1) {
				removeRequestHeaders.add(h.trim());
			}
			final String removeHeaders2 = props.getProperty("response.removeHeaders");
			final String[]headers2 = removeHeaders2.split(",");
			for (final String h : headers2) {
				removeResponseHeaders.add(h.trim());
			}
		}
	}
	
	public static String getReverseTargetPath(final ReverseConfig reverseConfig, final String incomingRequestPath) {
		return reverseConfig != null ? reverseConfig.getReverseUrl(incomingRequestPath).getFile() : incomingRequestPath;
	}

	/**
	 * <p>Remove hop-by-hop headers.
	 * @param request
	 */
	public static void removeRequestHeaders(final HttpRequest request) {
		for (final String h : removeRequestHeaders) {
			if (LOG.isTraceEnabled()) LOG.trace("remove:"+h);
			request.removeHeaders(h);
		}
	}

	/**
	 * <p>Copy the response headers.
	 * @param targetResponse
	 * @param response
	 */
	public static void copyHttpResponse(final HttpResponse targetResponse, final HttpResponse response) {
		// Remove hop-by-hop headers
		for (final String h : removeResponseHeaders) {
			targetResponse.removeHeaders(h);
		}

		response.setCode(targetResponse.getCode());
		response.setReasonPhrase(targetResponse.getReasonPhrase());
		response.setVersion(targetResponse.getVersion());
		
		final Header[] headers = response.getHeaders("Set-Cookie"); //backup Set-Cookie header.
		response.setHeaders(targetResponse.getHeaders()); //clean and reset all headers.
		for (final Header h : headers) { //add Set-Cookie headers.
			response.addHeader(h);
		}
	}

	/**
	 * Rewrite a response HTTP version in status line from reuested version.
	 * @param request
	 * @param response
	 * @since 1.0.4
	 */
	public static void rewriteStatusLine(final HttpRequest request, final HttpResponse response) {
		response.setVersion(request.getVersion());
	}

	/**
	 * <p>Rewrite the Content-Location response headers.
	 * @param response
	 * @param reverseUrl
	 */
	public static void rewriteContentLocationHeader(
			final HttpRequest request, final HttpResponse response, final ReverseConfig reverseUrl) {
		final Header[] locationHeaders = response.getHeaders("Content-Location");
		response.removeHeaders("Content-Location");
		for (final Header location : locationHeaders) {
			final String value = deleteCRLF(location.getValue());
			final String convertUrl = reverseUrl.getConvertRequestedUrl(value);
			if (convertUrl != null) {
				response.addHeader("Content-Location", convertUrl);
			}
		}
	}

	/**
	 * <p>Rewrite the Location response headers.
	 * @param response
	 * @param reverseUrl
	 */
	public static void rewriteLocationHeader(
			final HttpRequest request, final HttpResponse response, final ReverseConfig reverseUrl) {
		final Header[] locationHeaders = response.getHeaders("Location");
		response.removeHeaders("Location");
		for (final Header location : locationHeaders) {
			final String value = deleteCRLF(location.getValue());
			final String convertUrl = reverseUrl.getConvertRequestedUrl(value);
			if (convertUrl != null) {
				response.addHeader("Location", convertUrl);
			}
		}
	}

	/**
	 * <p>Rewrite the Set-Cookie response headers.
	 * @param response
	 * @param reverseUrl
	 */
	public static void rewriteSetCookieHeader(
			final HttpRequest request, final HttpResponse response, final ReverseConfig reverseUrl) {
		final Header[] cookies = response.getHeaders("Set-Cookie");
		final List<String> newValues = new ArrayList<>();
		for (final Header h : cookies) {
			final String value = h.getValue();
			final String newValue = ReverseUtils.getConvertedSetCookieHeader(request, reverseUrl, value);
			if (StringUtils.isNotEmpty(newValue)) {
				newValues.add(newValue);
				response.removeHeader(h);
			}
		}
		for (final String newValue : newValues) {
			response.addHeader("Set-Cookie", newValue);
			LOG.trace("[after] Set-Cookie: "+newValue);
		}
	}

	public static void rewriteServerHeader(final HttpResponse response, final ReverseConfig reverseUrl) {
		final UrlConfig serviceUrl = reverseUrl.getUrlConfig();
		if (serviceUrl != null) {
			response.setHeader(HttpHeaders.SERVER, serviceUrl.getHttpConfig().getServerName());
		}
	}

	/**
	 * <p>Set the remote IP address to {@code X-Forwarded-For} request header
	 * for origin server.
	 * @param request
	 * @param context
	 * @param useForwardHeader
	 * @param forwardHeader "X-Forwarded-For"
	 * @since 1.3
	 */
	public static void setXForwardedFor(final HttpRequest request, final HttpContext context,
			final boolean useForwardHeader, final String forwardHeader) {
		request.setHeader(forwardHeader, RequestUtils.getRemoteIPAddress(request, context, useForwardHeader, forwardHeader));
	}
	
	/**
	 * <p>Set the forwarded Host request header for origin server.
	 * @param request
	 */
	public static void setXForwardedHost(final HttpRequest request) {
		final Header hostHeader = request.getFirstHeader(HttpHeaders.HOST);
		if (hostHeader != null) {
			request.setHeader("X-Forwarded-Host", hostHeader.getValue());
		}
	}
	
	/**
	 * <p>Set the forwarded proto request header for origin server.
	 * @param request
	 * @param config
	 * @since 1.4-20190416
	 */
	public static void setXForwardedProto(final HttpRequest request, final HttpConfig config) {
		final String proto = HeaderUtils.getHeader(request, "X-Forwarded-Proto");
		if (StringUtils.isEmpty(proto)) {
			if (config.useHttps()) {
				request.setHeader("X-Forwarded-Proto", "https");
			} else {
				request.setHeader("X-Forwarded-Proto", "http");
			}
		}
	}

	/**
	 * <p>Set the forwarded port request header for origin server.
	 * @param request
	 * @param config
	 * @since 1.4-20190416
	 */
	public static void setXForwardedPort(final HttpRequest request, final HttpConfig config) {
		final String port = HeaderUtils.getHeader(request, "X-Forwarded-Port");
		if (StringUtils.isEmpty(port)) {
			final int serverPort = config.getPort();
			if (serverPort > 0) {
				request.setHeader("X-Forwarded-Port", String.valueOf(serverPort));
			} else {
				if (config.useHttps()) {
					request.setHeader("X-Forwarded-Port", "443");
				} else {
					request.setHeader("X-Forwarded-Port", "80");
				}
			}
		}
	}

	/**
	 * <p>Set the remote username to request header.
	 * @param request
	 * @param context
	 * @param headerName
	 */
	public static void setReverseProxyAuthorization(final HttpRequest request, final HttpContext context, final String headerName) {
		if (StringUtils.isNotEmpty(headerName)) {
			final Object user = context.getAttribute("REMOTE_USER"); //TODO
			if (user != null && user instanceof String) {
				request.setHeader(headerName, (String)user);
			} else {
				request.removeHeaders(headerName);
			}
		}
	}
	
	public static void appendHostHeader(final HttpRequest request, final ReverseConfig reverseUrl) {
		if (request.getVersion().lessEquals(HttpVersion.HTTP_1_0)
		 && StringUtils.isEmpty(HeaderUtils.getHeader(request, HttpHeaders.HOST))) {
			request.setHeader(HttpHeaders.HOST, reverseUrl.getTarget().getHostName());
			LOG.debug("Host(add): "+HeaderUtils.getHeader(request, HttpHeaders.HOST));
		}
	}
	
	//rewrite Host Header
	public static void rewriteHostHeader(final HttpRequest request, final HttpContext context, final ReverseConfig reverseConfig) {
		final Header[] hostHeaders = request.getHeaders(HttpHeaders.HOST);
		for (final Header hostHeader : hostHeaders) {
			final String value = hostHeader.getValue();
			final URL host = RequestUtils.getRequestURL(request, context, reverseConfig.getUrlConfig());
			reverseConfig.setHost(host);
			String before = host.getAuthority();
			final int beforePort = host.getPort();
			if (beforePort != 80 && beforePort > 0) {
				before = before + ":" + beforePort;
			}
			String after = reverseConfig.getReverse().getHost();
			final int afterPort = reverseConfig.getReverse().getPort();
			if (afterPort != 80 && afterPort > 0) {
				after = after + ":" + afterPort;
			}
			final String newValue = value.replace(before, after);

			LOG.trace("Host: " + value + " >> " + newValue);
			final Header newHeader = new BasicHeader(hostHeader.getName(), newValue);
			request.removeHeader(hostHeader);
			request.addHeader(newHeader);
		}
	}

	/**
	 * <p>Convert backend hostname to original hostname.
	 * @param reverseUrl
	 * @param line cookie header line.
	 * @return converted Set-Cookie response header line.
	 */
	public static String getConvertedSetCookieHeader(
			final HttpRequest request, final ReverseConfig reverseUrl, final String line) {
		if (line == null) return "";
		final String dist = reverseUrl.getReverse().getHost();
		final URL url = RequestUtils.getRequestURL(request, null);
		if (url == null) return "";
		final String src = url.getHost();
		return getConvertedSetCookieHeader(
				reverseUrl.getReverse().getPath(),
				reverseUrl.getUrlConfig().getPath(),
				Pattern.compile("domain=" + dist, Pattern.CASE_INSENSITIVE)
					.matcher(line).replaceAll("domain=" + src)
		);
	}

	/**
	 * <p>Convert cookie path.
	 * <pre>
	 *   BEFORE: JSESSIONID=1234567890ABCDEFGHIJKLMNOPQRSTUV; Path=/dist
	 *   AFTER : JSESSIONID=1234567890ABCDEFGHIJKLMNOPQRSTUV; Path=/src
	 * </pre>
	 */
	static String getConvertedSetCookieHeader(final String dist, final String src, final String line) {
		if (line != null) {
			final String d = stripEnd(dist, "/");
			final String s = stripEnd(src, "/");
			return Pattern.compile(";\\s*Path=" + d, Pattern.CASE_INSENSITIVE)
					.matcher(line).replaceAll("; Path=" + s);
		} else {
			return line;
		}
	}

	static String stripEnd(final String str, final String stripChars) {
		int end;
		if (str == null || (end = str.length()) == 0) {
			return str;
		}
		if (stripChars == null) {
			while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		} else if (stripChars.length() == 0) {
			return str;
		} else {
			while ((end != 0)
					&& (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
				end--;
			}
		}
		return str.substring(0, end);
	}
	/**
	 * delete CRLF
	 * @param str
	 * @since 1.1
	 */
	static String deleteCRLF(final String str) {
		if (str != null && str.length() > 0 ) {
			return str.replace("\r", "").replace("\n","");
		} else {
			return str;
		}
	}
	
//	public static Socket createSSLSocket(ReverseUrl reverseUrl, String protocol, HttpProxyConfig proxyConfig) {
//		if (proxyConfig == null || proxyConfig.isDirect()) {
//			return createSSLSocket(reverseUrl, protocol);
//		}
//		try {
//			InetSocketAddress address = reverseUrl.getTargetAddress();
//			SSLContext ssl = SSLContext.getInstance(protocol);
//			ssl.init(null, new TrustManager[]{createGenerousTrustManager()}, null);
//			SSLSocketFactory factory = ssl.getSocketFactory();
//			Socket socket = proxyConfig.tunnel(reverseUrl.getTargetHost());
//			return factory.createSocket(socket, address.getHostName(), address.getPort(), true);
//		} catch (Exception e) {
//			LOG.warn(e.getMessage());
//			return null;
//		}
//	}
//	
//	/**
//	 * Create SSL Socket for connect to backend server.
//	 * @param reverseUrl
//	 * @param protocol "TLS" or "SSL"
//	 */
//	public static Socket createSSLSocket(ReverseUrl reverseUrl, String protocol) {
//		try {
//			InetSocketAddress address = reverseUrl.getTargetAddress();
//			return createSSLSocketFactory(protocol).createLayeredSocket(
//				new Socket(address.getHostName(), address.getPort()), 
//				address.getHostName(), address.getPort(),
//				new BasicHttpContext()
//			);
//		} catch (Exception e) {
//			LOG.warn(e.getMessage());
//			return null;
//		}
//	}
//	
//	public static SSLConnectionSocketFactory createSSLSocketFactory(String protocol) {
//		SSLContext sslContext;
//		try {
//			sslContext = SSLContext.getInstance(protocol);
//			sslContext.init(null, new TrustManager[] { createGenerousTrustManager() }, new SecureRandom());
//		} catch (Exception e) {
//			return null;
//		}
//		return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
//	}
//
//	public static X509TrustManager createGenerousTrustManager() {
//		return new X509TrustManager() {
//			@Override
//			public void checkClientTrusted(X509Certificate[] cert, String s) throws CertificateException {
//			}
//
//			@Override
//			public void checkServerTrusted(X509Certificate[] cert, String s) throws CertificateException {
//			}
//
//			@Override
//			public X509Certificate[] getAcceptedIssuers() {
//				return null;
//			}
//		};
//	}
}
