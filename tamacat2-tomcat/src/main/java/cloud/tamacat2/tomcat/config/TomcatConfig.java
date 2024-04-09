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
package cloud.tamacat2.tomcat.config;

import org.apache.hc.core5.http.HttpHost;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;

public class TomcatConfig extends ReverseUrlConfig {
	
	String protocol = "http";
	String hostname = "127.0.0.1";
	int port = 8080;
	String contextPath;
		
	public static TomcatConfig create() {
		return new TomcatConfig();
	}
	
	public TomcatConfig protocol(String protocol) {
		this.protocol = protocol;
		return this;
	}
	
	public TomcatConfig hostname(String hostname) {
		this.hostname = hostname;
		return this;
	}
	
	public TomcatConfig port(int port) {
		this.port = port;
		return this;
	}
	
	public int getPort() {
		return port;
	}
	
	public TomcatConfig contextPath(String contextPath) {
		this.contextPath = contextPath;
		return this;
	}

	public TomcatConfig path(String path) {
		super.path(path);
		if (contextPath == null) {
			this.contextPath = path.replace("/$","");
		}
		return this;
	}
	
	public String getContextPath() {
		return contextPath;
	}
	
	public HttpHost getHttpHost() {
		 return new HttpHost(protocol, hostname, port);
	}
}
