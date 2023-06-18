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
package cloud.tamacat2.httpd;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.config.UrlConfig;

public class WebServer_test {

	public static void main(String[] args) {
		new WebServer().startup(
			HttpConfig.create().port(8080)
				.urlConfig(UrlConfig.create().path("/test/")
					.docsRoot("${server.home}/src/test/resources/htdocs/")
				)
				.contentEncoding("gzip")
		);
	}
}
