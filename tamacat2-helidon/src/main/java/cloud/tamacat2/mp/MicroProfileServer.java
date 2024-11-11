package cloud.tamacat2.mp;

import org.slf4j.bridge.SLF4JBridgeHandler;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.plugin.PluginServer;
import cloud.tamacat2.reverse.ReverseProxy;
import cloud.tamacat2.reverse.config.ReverseConfig;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;
import io.helidon.microprofile.server.Server;

public class MicroProfileServer extends ReverseProxy {

	static int port = 8081;
	public MicroProfileServer() {
	
		addPluginServer(
			new PluginServer() {
				@Override
				public void start() {
					SLF4JBridgeHandler.removeHandlersForRootLogger();
			        SLF4JBridgeHandler.install();
			        
					Server.builder().port(port).build().start();
				}
			}
		);
	}
	
	//http://localhost:8080/examples/test
	public static void main(String[] args) {
		new MicroProfileServer().startup(HttpConfig.create().port(8080)
			.urlConfig(ReverseUrlConfig.create().path("/examples/")
				.reverse(ReverseConfig.create().url("http://localhost:"+port+"/"))
			)
		);
	}
}
