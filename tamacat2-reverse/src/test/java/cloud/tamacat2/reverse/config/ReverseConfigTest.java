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
package cloud.tamacat2.reverse.config;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.apache.hc.core5.http.HttpHost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cloud.tamacat2.httpd.config.UrlConfig;

class ReverseConfigTest {

	ReverseConfig reverse;

	@BeforeEach
	public void setUp() throws Exception {
		UrlConfig url = UrlConfig.create().host("localhost").path("/test/");
		
		reverse = new ReverseConfig();
		reverse.setUrl("http://localhost:8080/test2/");
		reverse.setUrlConfig(url);
	}

	@Test
	public void testGetPath() {
	}


	@Test
	public void testGetTargetHost() throws Exception {

	}

	@Test
	void testCreate() {
	}

	@Test
	void testGetHttpConfig() {
	}

	@Test
	void testSetUrlConfig() {
	}

	@Test
	void testGetUrlConfig() {
	}

	@Test
	void testSetUrl() {
		reverse.setUrl("http://localhost/test/");
		assertEquals("http://localhost/test/", reverse.getUrl());
	}

	@Test
	void testUrl() {
		assertEquals("http://localhost/test/", reverse.url("http://localhost/test/").getUrl());
	}

	@Test
	void testGetUrl() {
		reverse.url("http://localhost/test/");
		assertEquals("http://localhost/test/", reverse.getUrl());
	}

	@Test
	void testGetTarget() {
		assertEquals("localhost", reverse.getTarget().getHostName());
		assertEquals(8080, reverse.getTarget().getPort());
	}

	@Test
	void testGetHost() throws Exception {
		reverse.setHost(new URI("http://localhost/").toURL());
		assertEquals("http://localhost", reverse.getHost().toString());	
	}

	@Test
	void testSetHost() {
		reverse.setHost(null);
		
		HttpHost host = reverse.getTarget();
		assertEquals("http", host.getSchemeName());
		assertEquals("localhost", host.getHostName());
		assertEquals(8080, host.getPort());
	}

	@Test
	void testGetReverse() {
		assertEquals(
				"http://localhost:8080/test2/",
				reverse.getReverse().toString()
			);
	}

	@Test
	void testGetReverseUrl() throws Exception {
		reverse.url("http://localhost:8080/test2/");
		assertEquals(
			"http://localhost:8080/test2/",
			reverse.getUrl()
		);
		
		reverse.setHost(new URI("http://localhost/").toURL());
		assertEquals(
			"http://localhost",
			reverse.getHost().toURI().toString()
		);
		
		assertEquals(
			"http://localhost:8080/test2/abc.html",
			reverse.getReverseUrl("/test/abc.html").toString()
		);

		assertNull(reverse.getReverseUrl(null));

		assertNull(reverse.getReverseUrl("te://*@\\({}[]st test"));
	}

	@Test
	void testGetConvertRequestedUrl() throws Exception {

		reverse.setHost(new URI("http://localhost/test/").toURL());
		assertEquals(
			"http://localhost/test/abc.html",
			reverse.getConvertRequestedUrl("http://localhost:8080/test2/abc.html")
		);

		assertEquals(
			"http://localhost/test/abc.html?abc=123&test=true",
			reverse.getConvertRequestedUrl("http://localhost:8080/test2/abc.html?abc=123&test=true")
		);
	}

	@Test
	void testToString() {
	}

}
