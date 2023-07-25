/*
 * Copyright 2023 tamacat.org
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
import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat2.httpd.WebServer;
import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.httpd.filter.HttpFilter;

public class WebServerWithFilterExamples {
	public static void main(String[] args) {
		new WebServer().startup(HttpConfig.create().port(8080)
			.urlConfig(UrlConfig.create().path("/examples/")
				.docsRoot("${server.home}/htdocs/")
				.filter(new HttpFilter() {
					protected void handleRequest(ClassicHttpRequest req, HttpContext context)
						throws HttpException, IOException {
					}
						
					protected void handleSubmitResponse(ClassicHttpResponse resp, HttpContext context)
						throws HttpException, IOException {
						resp.setHeader("Server", "httpd");
					}
				})
			)
			.contentEncoding("gzip")
		);
	}
}