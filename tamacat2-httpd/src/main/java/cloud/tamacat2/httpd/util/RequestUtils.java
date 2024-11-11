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
package cloud.tamacat2.httpd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.httpd.error.HttpStatusException;

public class RequestUtils {
	
	static final Logger LOG = LoggerFactory.getLogger(RequestUtils.class);
	
	static final String HTTP_REQUEST_PARAMETERS = "http.request.parameters";
	
	public static final String X_FORWARDED_FOR = "X-Forwarded-For";
	public static final String REMOTE_ADDRESS = "remote_address";

	static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

	public static String getRequestLine(final HttpRequest request) {
		return request.getMethod() + " "
			+ request.getPath() + " "
			+ request.getVersion();
	}
	
	public static RequestLine getRequestLine(final RequestLine requestline) {
		final String uri = requestline.getUri();
		final String path = getRequestPathWithQuery(uri);
		if (uri.equals(path)) {
			return requestline;
		} else {
			return new RequestLine(requestline.getMethod(),
				path.substring(path.indexOf("/"), path.length()),
				requestline.getProtocolVersion());
		}
	}

	/**
	 * Get request absolute URI to Path (With Query)
	 * @param uri
	 */
	public static String getRequestPathWithQuery(final String uri) {
		try {
			if (uri.indexOf("http")==0 && uri.indexOf("://")>0) {
				final int idx = uri.indexOf("://");
				final String path = uri.substring(idx+3, uri.length());
				if (path.indexOf("/")>0) {
					return path.substring(path.indexOf("/"), path.length());
				}
			}
		} catch (RuntimeException e) {
			LOG.warn(e.getMessage());
		}
		return uri;
	}

	public static String getPath(final String uri) {
		final int indexQ = uri.indexOf('?');
		if (indexQ != -1) {
			return uri.substring(0, indexQ);
		}
		
		final int indexHash = uri.indexOf('#');
		if (indexHash != -1) {
			return uri.substring(0, indexHash);
		}
		return uri;
	}

	final static ReentrantLock lock = new ReentrantLock();
	
	@Deprecated
	public static RequestParameters parseParameters(
			final HttpRequest request, final HttpEntity entity,
			final HttpContext context, final Charset encoding) {
		lock.lock();
		try {
			final RequestParameters parameters = (RequestParameters) context.getAttribute(HTTP_REQUEST_PARAMETERS);
			if (parameters == null) {
				try {
					RequestParameters parsed = parseParameters(request, entity, encoding);
					context.setAttribute(HTTP_REQUEST_PARAMETERS, parsed);
					return parsed;
				} catch (Exception e) {
					//BAD REQUEST.
					LOG.warn(e.getMessage());
				}
			}
			return parameters;
		} finally {
			lock.unlock();
		}
	}
	
	public static RequestParameters parseParameters(
			final HttpRequest request, final HttpEntity entity, final Charset encoding) {
		final RequestParameters parameters = new RequestParameters();
		final String path = request.getPath();
		if (path.indexOf('?') >= 0) {
			final String[] requestParams = StringUtils.split(path, "?");
			//set request parameters for Custom HttpRequest.
			if (requestParams.length >= 2) {
				final String params = requestParams[1];
				final String[] param = StringUtils.split(params, "&");
				for (String kv : param) {
					final String[] p = StringUtils.split(kv, "=");
					if (p.length >=2) {
						try {
							parameters.setParameter(p[0], URLDecoder.decode(p[1], encoding));
						} catch (Exception e) {
						}
					} else if (p.length == 1) {
						parameters.setParameter(p[0], "");
					}
				}
			}
		}
		if (entity != null && RequestUtils.isFormUrlEncoded(request)) {
			if (entity != null) {
				try (final BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
					String s;
					final StringBuilder sb = new StringBuilder();
					while ((s = reader.readLine()) != null) {
						sb.append(s);
					}
					final String requestBody = sb.toString();
					//for Reuse handler
					
					//getHttpEntityEnclosingRequest(request).setEntity(new StringEntity(requestBody, encoding));
					
					final String[] params = StringUtils.split(requestBody, "&");
					for (final String param : params) {
						final String[] keyValue = StringUtils.split(param, "=");
						if (keyValue.length >= 2) {
							try {
								parameters.setParameter(keyValue[0],
									URLDecoder.decode(keyValue[1], encoding));
							} catch (Exception e) {
							}
						} else if (keyValue.length==1) {
							parameters.setParameter(keyValue[0], "");
						}
					}
				} catch (IOException e) {
					throw new HttpStatusException(HttpStatus.SC_BAD_REQUEST, e);
				}
			}
		}
		return parameters;
	}
	
	public static void setParameter(final HttpContext context, final String name, final String... values) {
		final RequestParameters parameters = getParameters(context);
		parameters.setParameter(name, values);
	}

	public static void setParameters(final HttpContext context, final RequestParameters parameters) {
		context.setAttribute(HTTP_REQUEST_PARAMETERS, parameters);
	}

	/**
	 * Get Request parameters
	 * @since 1.4
	 * @deprecated
	 */
	public static RequestParameters getParameters(
			final HttpRequest request, final HttpEntity entity,
			final HttpContext context, final Charset encoding) {
		parseParameters(request, entity, context, encoding);
		return getParameters(context);
	}
	
	public static RequestParameters getParameters(final HttpContext context) {
		return (RequestParameters) context.getAttribute(HTTP_REQUEST_PARAMETERS);
	}

	public static String getParameter(final HttpContext context, final String name) {
		final RequestParameters params = getParameters(context);
		return params != null ? params.getParameter(name) : null;
	}

	public static String[] getParameters(final HttpContext context, final String name) {
		final RequestParameters params = getParameters(context);
		return params != null ? params.getParameters(name) : null;
	}

	public static Set<String> getParameterNames(final HttpContext context) {
		final RequestParameters params = getParameters(context);
		return params != null ? params.getParameterNames() : null;
	}

	/**
	 * Set the remote IP address to {@code HttpContext}.
	 * @param context
	 * @param conn instance of HttpInetConnection
	 */
	public static void setRemoteAddress(final HttpContext context, final HttpServerConnection conn) {
		context.setAttribute(REMOTE_ADDRESS, conn.getRemoteAddress());
	}

	/**
	 * Get the remote IP address in {@code HttpContext} or X-Forwarded-For.
	 * @param request
	 * @param context
	 * @param useXFF Using X-Forwarded-For request header.
	 * @return
	 */
	public static String getRemoteIPAddressfinal(
			final HttpRequest request, final HttpContext context, final boolean useXFF) {
		return getRemoteIPAddress(request, context, useXFF, X_FORWARDED_FOR);
	}
	
	/**
	 * Get the remote IP address in {@code HttpContext} or X-Forwarded-For.
	 * @param request
	 * @param context
	 * @param useXFF Using X-Forwarded-For request header.
	 * @param forwardHeader ("X-Forwarded-For")
	 * @return
	 */
	public static String getRemoteIPAddress(
			final HttpRequest request, final HttpContext context,
			final boolean useXFF, final String forwardHeader) {
		String ip = null;
		if (useXFF) {
			ip = getForwardedForLastValue(request, forwardHeader);
		}
		if (StringUtils.isEmpty(ip)) {
			ip = getRemoteIPAddress(context);
		}
		return ip != null ? ip : "";
	}
	
	/**
	 * Get the remote IP address in {@code HttpContext}.
	 * @param context
	 * @return
	 */
	public static String getRemoteIPAddress(final HttpContext context) {
		final InetAddress address = (InetAddress) context.getAttribute(REMOTE_ADDRESS);
		if (address != null) return address.getHostAddress();
		else return "";
	}

	public static boolean isRemoteIPv6Address(final HttpContext context) {
		final InetAddress address = (InetAddress) context.getAttribute(REMOTE_ADDRESS);
		if (address != null && address instanceof Inet6Address) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get a X-Forwarded-For value. (original)
	 * @param request
	 * @param forwardHeader
	 * @since 1.5-20230629
	 */
	public static String getForwardedForValue(final HttpRequest request, final String forwardHeader) {
		return HeaderUtils.getHeader(request, StringUtils.isNotEmpty(forwardHeader)? forwardHeader : X_FORWARDED_FOR);
	}

	/**
	 * Get a X-Forwarded-For first value.
	 * @param request
	 * @param forwardHeader
	 * @since 1.5-20230629
	 */
	public static String getForwardedForFirstValue(final HttpRequest request, final String forwardHeader) {
		final String value = getForwardedForValue(request, forwardHeader);
		if (StringUtils.isNotEmpty(value)) {
			final String[] address = StringUtils.split(value, ",");
			if (address.length >= 1) {
				return address[0];
			}
		}
		return value;
	}
	
	/**
	 * Get a X-Forwarded-For last value.
	 * @param request
	 * @param forwardHeader
	 * @since 1.5-20230629
	 */
	public static String getForwardedForLastValue(final HttpRequest request, final String forwardHeader) {
		final String value = getForwardedForValue(request, forwardHeader);
		if (StringUtils.isNotEmpty(value)) {
			final String[] address = StringUtils.split(value, ",");
			if (address.length >= 1) {
				return address[address.length -1];
			}
		}
		return value;
	}
	
	/**
	 * Get hostname from Host request header.
	 * @param request
	 * @param context
	 */
	public static String getRequestHost(final HttpRequest request, final HttpContext context) {
		final Header hostHeader = request.getFirstHeader(HttpHeaders.HOST);
		if (hostHeader != null) {
			final String hostName = hostHeader.getValue();
			if (hostName != null && hostName.indexOf(':') >= 0) {
				String[] hostAndPort = StringUtils.split(hostName, ":");
				if (hostAndPort.length >= 2) {
					return hostAndPort[0];
				}
			}
			return hostName;
		}
		return null;
	}

	public static String getRequestHostURL(
			final HttpRequest request, final HttpContext context, final UrlConfig url) {
		final URL host = getRequestURL(request, context, url);
		return host != null ? host.getProtocol()
				+ "://" + host.getAuthority() : null;
	}

	public static URL getRequestURL(final HttpRequest request, final HttpContext context) {
		return getRequestURL(request, context, null);
	}

	public static URL getRequestURL(final HttpRequest request, final HttpContext context, final UrlConfig url) {
		String protocol = "http";
		String hostName = null;
		int port = -1;
		final Header hostHeader = request.getFirstHeader(HttpHeaders.HOST);
		if (hostHeader != null) {
			hostName = hostHeader.getValue();
			if (hostName != null && hostName.indexOf(':') >= 0) {
				String[] hostAndPort = StringUtils.split(hostName, ":");
				if (hostAndPort.length >= 2) {
					hostName = hostAndPort[0];
					port = StringUtils.parse(hostAndPort[1],-1);
				}
			}
		}
		if (url != null) {
			final URL configureHost = url.getHost();
			if (configureHost != null) {
				protocol = configureHost.getProtocol();
				if (hostName == null) {
					hostName = configureHost.getHost();
				}
			}
			if (url.getHttpConfig().useHttps()) {
				protocol = "https";
			}
			if (hostName != null && hostName.indexOf(':') >= 0) {
				final String[] hostAndPort = StringUtils.split(hostName, ":");
				if (hostAndPort.length >= 2) {
					hostName = hostAndPort[0];
					port = StringUtils.parse(hostAndPort[1],-1);
				}
			} else {
				port = url.getHttpConfig().getPort();
			}
			/* TODO
			if (context != null) {
				HttpConnection con = getHttpConnection(context);
				SocketAddress addr = con.getLocalAddress();
				if (addr != null && addr instanceof InetSocketAddress) {
					port = ((InetSocketAddress)addr).getPort();
				}
				if (hostName == null) {
					hostName = ((InetSocketAddress)addr).getHostName();
				}
			}*/
		}
		if (("http".equalsIgnoreCase(protocol) && port == 80)
			|| ("https".equalsIgnoreCase(protocol) && port == 443)){
			port = -1;
		}
		if (hostName != null) {
			try {
				final URI path = new URI(request.getPath());
				return new URI(protocol, null, hostName, port, path.getPath(), path.getQuery(), path.getFragment()).toURL();
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * UnsupportedEncodingException -> value returns.
	 * @param value
	 * @param encoding
	 * @return
	 */
	static String decode(final String value, final String encoding) {
		String decode = null;
		try {
			decode = URLDecoder.decode(value, encoding);
		} catch (UnsupportedEncodingException e) {
			decode = value;
		}
		return decode;
	}

	public static InputStream getInputStream(final HttpEntity entity) throws IOException {
		//HttpEntity entity = getEntity(request);
		return entity != null? entity.getContent() : null;
	}

	public static boolean isFormUrlEncoded(final HttpRequest request) {
		return HeaderUtils.isFormUrlEncoded(
				HeaderUtils.getHeader(request, HttpHeaders.CONTENT_TYPE));
	}
	
	public static boolean isMultipart(final HttpRequest request) {
		if ("post".equalsIgnoreCase(request.getMethod())) {
			return HeaderUtils.isMultipart(
				HeaderUtils.getHeader(request, HttpHeaders.CONTENT_TYPE));
		}
		return false;
	}

	public static String getPathPrefix(final HttpRequest request) {
		final String path = request.getPath();
		int idx = path.lastIndexOf("/");
		if (idx >=0) {
			return path.substring(0, idx) + "/";
		}
		return path;
	}
}
