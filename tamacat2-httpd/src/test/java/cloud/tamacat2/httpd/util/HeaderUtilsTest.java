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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.HttpCookie;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.junit.jupiter.api.Test;

public class HeaderUtilsTest {

	@Test
	public void testHeaderUtils() {
		new HeaderUtils();
	}

	@Test
	public void testGetHeader() {
		HttpRequest request = new BasicHttpRequest("GET", "/test.html");
		request.addHeader("id", "test");
		request.addHeader("test", "");
		assertEquals("test", HeaderUtils.getHeader(request, "id"));
		assertEquals("", HeaderUtils.getHeader(request, "test"));
		assertEquals(null, HeaderUtils.getHeader(request, "abc"));

		assertEquals("default", HeaderUtils.getHeader(request, "abc", "default"));
	}

	@Test
	public void testEqualsName() {
		assertTrue(HeaderUtils.equalsName(new BasicHeader("name", "abc"), new BasicHeader("name", "abc")));
		assertTrue(HeaderUtils.equalsName(new BasicHeader("NAME", "abc"), new BasicHeader("name", "abc")));
		assertFalse(HeaderUtils.equalsName(new BasicHeader("test", "abc"), new BasicHeader("name", "abc")));

		assertFalse(HeaderUtils.equalsName(new BasicHeader("name", "abc"), null));
		assertFalse(HeaderUtils.equalsName(null, new BasicHeader("name", "abc")));
		assertFalse(HeaderUtils.equalsName(null, null));
	}

	@Test
	public void testGetCookiesString() {
		String value = "id=guest; session=1234567890; none=; name=test;";
		List<HttpCookie> cookies = HeaderUtils.getCookies(value);

		assertEquals(4, cookies.size());

		assertEquals("id", cookies.get(0).getName());
		assertEquals("guest", cookies.get(0).getValue());

		assertEquals("session", cookies.get(1).getName());
		assertEquals("1234567890", cookies.get(1).getValue());

		assertEquals("none", cookies.get(2).getName());
		assertEquals("", cookies.get(2).getValue());

		assertEquals("name", cookies.get(3).getName());
		assertEquals("test", cookies.get(3).getValue());
		
		//String b64 = Base64.getUrlEncoder().encodeToString("Encode to Base64 format".getBytes());
		String value2 = "id=guest; session=1234567890; none=; name=test; b64=\"RW5jb2RlIHRvIEJhc2U2NCBmb3JtYXQ=\";";
		List<HttpCookie> cookies2 = HeaderUtils.getCookies(value2);
		assertEquals("Encode to Base64 format", 
			new String(Base64.getUrlDecoder().decode(cookies2.get(4).getValue().getBytes())));
	}

	@Test
	public void testGetCookieValue() {
		String value = "id=guest; session=1234567890; none=; name=test;";
		Header header = new BasicHeader("Cookie", value);
		HttpRequest request = new BasicHttpRequest("GET", "/test.html");
		request.setHeader(header);

		assertEquals("guest", HeaderUtils.getCookieValue(request, "id"));
		assertEquals("1234567890", HeaderUtils.getCookieValue(request, "session"));
		assertEquals("", HeaderUtils.getCookieValue(request, "none"));
		assertEquals("test", HeaderUtils.getCookieValue(request, "name"));
		assertEquals(null, HeaderUtils.getCookieValue(request, "abc"));
	}

	@Test
	public void testGetCookieValueStringString() {
		String cookie = "id=guest; session=1234567890; none=; name=test;";
		assertEquals("guest", HeaderUtils.getCookieValue(cookie, "id"));
		assertEquals("1234567890", HeaderUtils.getCookieValue(cookie, "session"));
		assertEquals("", HeaderUtils.getCookieValue(cookie, "none"));
		assertEquals("test", HeaderUtils.getCookieValue(cookie, "name"));
		assertEquals(null, HeaderUtils.getCookieValue(cookie, "abc"));
	}

	@Test
	public void testGetCookieValueStringStringBase64() {
		//String b64 = Base64.getUrlEncoder().encodeToString("Encode to Base64 format".getBytes());
		String cookie = "id=guest; session=1234567890; none=; name=test; b64=\"RW5jb2RlIHRvIEJhc2U2NCBmb3JtYXQ=\";";
		assertEquals("guest", HeaderUtils.getCookieValue(cookie, "id"));
		assertEquals("1234567890", HeaderUtils.getCookieValue(cookie, "session"));
		assertEquals("", HeaderUtils.getCookieValue(cookie, "none"));
		assertEquals("test", HeaderUtils.getCookieValue(cookie, "name"));
		assertEquals(null, HeaderUtils.getCookieValue(cookie, "abc"));
		
		String b64value = HeaderUtils.getCookieValue(cookie, "b64");
		assertEquals("Encode to Base64 format", 
			new String(Base64.getUrlDecoder().decode(b64value.getBytes())));
	}
	
	@Test
	public void testSetCookieValue() {
		HttpCookie cookie = new HttpCookie("TestSession", "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		//System.out.println(HeaderUtils.getSetCookieValue(cookie, true, true));
		assertTrue(HeaderUtils.getSetCookieValue(cookie, true, true)
			.startsWith("TestSession=; Path=/; HttpOnly; Secure; Max-Age=0; Expires=Wed, 21 Jan 1970"));
	}

	@Test
	public void testSetCookie() {
		HttpCookie cookie = new HttpCookie("TestSession", "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		assertEquals("TestSession=; Path=/; Max-Age=0", HeaderUtils.getSetCookieValue(cookie));
	}

	@Test
	public void testInContentType() {
		Set<String> contentTypes = new HashSet<String>();
		contentTypes.add("html");

		assertEquals(false, HeaderUtils.inContentType(contentTypes, null));

		Header header1 = new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/html");
		assertEquals(true, HeaderUtils.inContentType(contentTypes, header1));

		Header header2 = new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/xml");
		assertEquals(false, HeaderUtils.inContentType(contentTypes, header2));

		contentTypes.add("image/jpeg");
		Header header3 = new BasicHeader(HttpHeaders.CONTENT_TYPE, "image/jpeg");
		assertEquals(true, HeaderUtils.inContentType(contentTypes, header3));

		Header header4 = new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");
		assertEquals(true, HeaderUtils.inContentType(contentTypes, header4));
		Header header5 = new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/html;");
		assertEquals(true, HeaderUtils.inContentType(contentTypes, header5));
		Header header6 = new BasicHeader(HttpHeaders.CONTENT_TYPE, null);
		assertEquals(false, HeaderUtils.inContentType(contentTypes, header6));
	}

	@Test
	public void testIsMultipart() {
		assertTrue(HeaderUtils.isMultipart("multipart/form-data"));
		assertFalse(HeaderUtils.isMultipart("application/x-www-form-urlencoded"));
		assertFalse(HeaderUtils.isMultipart(null));
	}
}