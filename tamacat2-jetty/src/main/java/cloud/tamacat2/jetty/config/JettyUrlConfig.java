package cloud.tamacat2.jetty.config;

import org.apache.hc.core5.http.HttpHost;

import cloud.tamacat2.reverse.config.ReverseUrlConfig;

public class JettyUrlConfig extends ReverseUrlConfig {
	
	String protocol = "http";
	String hostname = "127.0.0.1";
	int port = 8080;
	
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
}
