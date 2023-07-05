package cloud.tamacat2.jetty.config;

import org.apache.hc.core5.http.HttpHost;
import org.eclipse.jetty.server.handler.ErrorHandler;

import cloud.tamacat2.jetty.DefaultErrorHandler;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;

public class JettyUrlConfig extends ReverseUrlConfig {
	
	String protocol = "http";
	String hostname = "127.0.0.1";
	int port = 8080;
	ErrorHandler errorHandler = new DefaultErrorHandler();
	boolean useJSP;
	
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
	
	public boolean useErrorHandler() {
		return errorHandler != null;
	}
	
	public JettyUrlConfig errorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
		return this;
	}
	
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}
	
	public JettyUrlConfig useJSP(boolean useJSP) {
		this.useJSP = useJSP;
		return this;
	}
	
	public boolean useJSP() {
		return useJSP;
	}
}
