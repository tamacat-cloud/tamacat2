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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.config.UrlConfig;

public class ReverseConfig {
	
	static final Logger LOG = LoggerFactory.getLogger(ReverseConfig.class);
	
	String url;
	
	HttpHost target;
	
	UrlConfig urlConfig;
	
	private URL host;
	
	public static ReverseConfig create() {
		return new ReverseConfig();
	}
	
	public HttpConfig getHttpConfig() {
		return urlConfig.getHttpConfig();
	}
	
	public void setUrlConfig(UrlConfig urlConfig) {
		this.urlConfig = urlConfig;
	}
	
	public UrlConfig getUrlConfig() {
		return urlConfig;
	}
	
	public void setUrl(String url) {
		try {
			URL targetUrl = new URL(url);
			this.target = new HttpHost(targetUrl.getHost(), targetUrl.getPort());
			this.url = url;
		} catch (MalformedURLException e) {
			LOG.warn(e.getMessage());
		}
	}
	
	public ReverseConfig url(String url) {
		setUrl(url);
		return this;
	}
	
	public String getUrl() {
		return url;
	}
	
	public HttpHost getTarget() {
		if (this.url != null && target == null) {
			setUrl(this.url);
		}
		return target;
	}
	
	public URL getHost() {
		return host;
	}
	
	public void setHost(URL host) {
		if (host != null) {
			try {
				this.host = new URL(host.getProtocol(), host.getHost(), host.getPort(), "");
			} catch (MalformedURLException e) {
				LOG.warn(e.getMessage());
			}
		}
	}

	public URL getReverse() {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			LOG.warn(e.getMessage());
		}
		return null;
	}
	
	public URL getReverseUrl(String path) {
		String p = urlConfig.getPath();
		if (path != null && p != null && path.startsWith(p)) {
			URL reverseUrl = getReverse();
			String distUrl = path.replaceFirst(urlConfig.getPath(), reverseUrl.getPath());
			try {
				int port = reverseUrl.getPort();
				if (port == -1) {
					port = reverseUrl.getDefaultPort();
				}
				return new URL(reverseUrl.getProtocol(), reverseUrl.getHost(), port, distUrl);
			} catch (MalformedURLException e) {
				LOG.warn(e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * path: http://localhost:8080/examples/servlet
	 *   =>  http://localhost/examples2/servlet
	 */
	public String getConvertRequestedUrl(String path) {
		URL reverseUrl = getReverse();
		URL host = getHost(); // requested URL (path is deleted)
		if (path != null && host != null) {
			return path.replaceFirst(
				reverseUrl.getProtocol() + "://" + reverseUrl.getAuthority(), host.toString())
					.replace(reverseUrl.getPath(), urlConfig.getPath());
		} else {
			return path;
		}
	}

	@Override
	public String toString() {
		return "ReverseConfig [url=" + url + "]";
	}
}
