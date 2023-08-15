package cloud.tamacat2.mp;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.plugin.PluginServer;
import cloud.tamacat2.reverse.ReverseProxy;
import cloud.tamacat2.reverse.config.ReverseConfig;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.WebServer;

public class MicroProfileServerExamples extends ReverseProxy {

	public MicroProfileServerExamples() {
		addPluginServer(
			new PluginServer() {
				@Override
				public void start() {
					WebServer.builder().port(8081).addRouting(
						Routing.builder()
						        .get("/hello", (req, res)
						             -> res.send("Hello World!")
						        )
						        .get("/test", handle())
						        .build())
					.build()
					.start();
				}
			}
		);
	}
	
	Handler handle() {
		return new Handler() {

			@Override
			public void accept(ServerRequest req, ServerResponse res) {
				res.addHeader("Content-Type", "text/html");
				res.send("<html>test</html>");
			}
		};
	}
	
	//http://localhost:8080/examples/test
	public static void main(String[] args) {
		new MicroProfileServer().startup(HttpConfig.create().port(8080)
			.urlConfig(ReverseUrlConfig.create().path("/examples/")
				.reverse(ReverseConfig.create().url("http://localhost:8081/"))
			)
		);
	}
}
