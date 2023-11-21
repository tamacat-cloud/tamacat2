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
import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.reverse.ReverseProxy;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;
import cloud.tamacat2.reverse.config.ReverseConfig;

public class ReverseProxyExamples {
	public static void main(String[] args) {
		new ReverseProxy().startup(HttpConfig.create().port(8080)
			.urlConfig(ReverseUrlConfig.create().path(args[0])
				.reverse(ReverseConfig.create().url(args[1]))
			)
		);
	}
}