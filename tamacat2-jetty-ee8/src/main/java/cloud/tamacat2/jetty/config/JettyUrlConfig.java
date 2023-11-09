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
package cloud.tamacat2.jetty.config;

import org.apache.hc.core5.http.HttpHost;

import cloud.tamacat2.reverse.config.ReverseUrlConfig;

public class JettyUrlConfig extends ReverseUrlConfig {
	
	String protocol = "http";
	String hostname = "127.0.0.1";
	int port = 8080;
	boolean useJSP = true;
	
	public static JettyUrlConfig create() {
		return new JettyUrlConfig();
	}
	
	public JettyUrlConfig port(int port) {
		this.port = port;
		return this;
	}
	
	public int getPort() {
		return port;
	}
	
	public HttpHost getHttpHost() {
		 return new HttpHost(protocol, hostname, port);
	}
	
	public JettyUrlConfig useJSP(boolean useJSP) {
		this.useJSP = useJSP;
		return this;
	}
	
	public boolean useJSP() {
		return useJSP;
	}
}
