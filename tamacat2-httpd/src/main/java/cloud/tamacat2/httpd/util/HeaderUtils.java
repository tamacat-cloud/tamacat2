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

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.HttpRequest;


/**
 * <p>The utility class for HTTP request and response Headers.
 */
public final class HeaderUtils {

	static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

	/** Cannot instantiate. */
	HeaderUtils() {}

	/**
	 * <p>Get the first header value.
	 * @see {@link org.apache.http.HttpMessage#getFirstHeader}
	 * @param message
	 * @param name
	 * @return first header value.
	 */
	public static String getHeader(
			HttpMessage message, String name) {
		Header header = message.getFirstHeader(name);
		return header != null ? header.getValue() : null;
	}

	/**
	 * <p>Get the first header value.
	 * When header is null, returns default value.
	 * @see {@link org.apache.http.HttpMessage#getFirstHeader}
	 * @param message
	 * @param name
	 * @param defaultValue
	 * @return first header value.
	 */
	public static String getHeader(
			HttpMessage message, String name, String defaultValue) {
		Header header = message.getFirstHeader(name);
		return header != null ? header.getValue() : deleteCRLF(defaultValue);
	}

	/**
	 * <p>when each other's header names are equal returns true.
	 * The header name does not distinguish a capital letter, a small letter.
	 * @param target target header.
	 * @param other other one.
	 * @return true, header names are equals.
	 */
	public static boolean equalsName(Header target, Header other) {
		if (target == null || other == null) {
			return false;
		} else {
			return target.getName().equalsIgnoreCase(other.getName());
		}
	}

	/**
	 * <p>Get the Cookie value from Cookie header line.
	 * @param cookie header line.
	 * @param name Cookie name
	 * @return value of Cookie name in header line.
	 */
	public static List<HttpCookie> getCookies(String cookie) {
		List<HttpCookie> cookies = new ArrayList<>();
		if (StringUtils.isEmpty(cookie)) return cookies;
		StringTokenizer token = new StringTokenizer(deleteCRLF(cookie), ";");
		if (token != null) {
			while (token.hasMoreTokens()) {
				String line = token.nextToken();
				String[] nameValue = line.split("=");
				if (nameValue != null && nameValue.length > 0) {
					String key = nameValue[0].trim();
					StringBuilder sb = new StringBuilder();
					for (int i=1; i<nameValue.length; i++) {
						if (sb.length()>0) {
							sb.append("=");
						}
						sb.append(nameValue[i]);
					}
					String value = sb.toString().replaceAll("^\"|\"$", "").trim();
					HttpCookie c = new HttpCookie(key, value);
					cookies.add(c);
				}
			}
		}
		return cookies;
	}

	public static String getCookieValue(HttpRequest request, String name) {
		return getCookieValue(getHeader(request, "Cookie", ""), name);
	}

	/**
	 * <p>Get the Cookie value from Cookie header line.
	 * @param cookie header line.
	 * @param name Cookie name
	 * @return value of Cookie name in header line.
	 */
	public static String getCookieValue(String cookie, String name) {
		if (StringUtils.isEmpty(cookie)) return null;
		StringTokenizer token = new StringTokenizer(deleteCRLF(cookie), ";");
		if (token != null) {
			while (token.hasMoreTokens()) {
				String line = token.nextToken();
				String[] nameValue = line.split("=");
				String key = nameValue[0].trim();
				if (name.equalsIgnoreCase(key)) {
					StringBuilder sb = new StringBuilder();
					for (int i=1; i<nameValue.length; i++) {
						if (sb.length()>0) {
							sb.append("=");
						}
						sb.append(nameValue[i]);
					}
					return sb.toString().replaceAll("^\"|\"$", "").trim();
				}
			}
		}
		return null;
	}
	
	public static String getSetCookieValue(HttpCookie cookie, boolean isHttpOnlyCookie, boolean isSecureCookie) {
		StringBuilder value = new StringBuilder();
		value.append(cookie.getName()+"="+cookie.getValue());
		String path = cookie.getPath();
		if (StringUtils.isNotEmpty(path)) {
			value.append("; Path="+cookie.getPath());
		}
		String domain = cookie.getDomain();
		if (StringUtils.isNotEmpty(domain)) {
			value.append("; Domain="+cookie.getDomain());
		}
		if (isHttpOnlyCookie) {
			value.append("; HttpOnly");
		}
		if (isSecureCookie) {
			value.append("; Secure");
		}
		Date expire = new Date((System.currentTimeMillis()/1000)+cookie.getMaxAge());
		if (expire != null) {
			value.append("; Max-Age="+cookie.getMaxAge());
			value.append("; Expires="+DateUtils.getTime(expire,
				"EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH, TimeZone.getTimeZone("GMT")));
		}
		return value.toString();
	}
	
	public static String getSetCookieValue(HttpCookie cookie) {
		StringBuilder value = new StringBuilder();
		value.append(cookie.getName()+"="+cookie.getValue());
		String path = cookie.getPath();
		if (StringUtils.isNotEmpty(path)) {
			value.append("; Path="+cookie.getPath());
		}
		String domain = cookie.getDomain();
		if (StringUtils.isNotEmpty(domain)) {
			value.append("; Domain="+cookie.getDomain());
		}
		if (cookie.isHttpOnly()) {
			value.append("; HttpOnly");
		}
		if (cookie.getSecure()) {
			value.append("; Secure");
		}
		long maxAge = cookie.getMaxAge();
		if (maxAge >= 0) {
			value.append("; Max-Age="+maxAge);
		}
		return value.toString();
	}

	/**
	 * <p>Check for use link convert.
	 * @param contentType
	 * @return true use link convert.
	 */
	public static boolean inContentType(Set<String> contentTypes, Header contentType) {
		if (contentType == null) return false;
		String type = contentType.getValue();
		if (contentTypes.contains(type)) {
			return true;
		} else {
			//Get the content sub type. (text/html; charset=UTF-8 -> html)
			String[] types = type != null ? StringUtils.split(type, ";")[0].split("/") : new String[0];
			if (types.length >= 2 && contentTypes.contains(types[1])) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isFormUrlEncoded(String line) {
		return line != null && line.toLowerCase().startsWith(CONTENT_TYPE_FORM_URLENCODED);

	}
	
	public static boolean isMultipart(String line) {
		return line != null && line.toLowerCase().startsWith("multipart/");
	}
	
	/**
	 * delete CRLF
	 * @param str
	 */
	public static String deleteCRLF(String str) {
		if (str != null && str.length() > 0 ) {
			return str.replace("\r", "").replace("\n","");
		} else {
			return str;
		}
	}
}
