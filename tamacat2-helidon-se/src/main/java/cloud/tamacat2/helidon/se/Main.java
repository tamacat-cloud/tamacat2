package cloud.tamacat2.helidon.se;

import java.util.Map;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.plugin.PluginServer;
import cloud.tamacat2.reverse.ReverseProxy;
import cloud.tamacat2.reverse.config.ReverseConfig;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

public class Main extends ReverseProxy {
	
	static int port = 8081;
	public Main() {
	
		addPluginServer(
			new PluginServer() {
				@Override
				public void start() {
			        WebServer.builder().port(port)
			        .routing(it -> it
			                .get("/hello", (req, res) -> new RestHandler().handle(req, res))) 
			        .build().start();
				}
			}
		);
	}

	class RestHandler implements Handler {
		static final JsonBuilderFactory JSON_FACTORY = Json.createBuilderFactory(Map.of()); 

		@Override
		public void handle(ServerRequest req, ServerResponse res) throws Exception {
			String value = req.query().first("key").get();

		    JsonObject responseEntity = JSON_FACTORY.createObjectBuilder()
		    		.add("key", value)
		    		.add("message", "Hello " + req.query().get("name")).build();
			res.send(responseEntity);
		}
		
	}
	
	public static void main(String[] args) {
		new Main().startup(HttpConfig.create().port(8080)
			.urlConfig(ReverseUrlConfig.create().path("/examples/")
				.reverse(ReverseConfig.create().url("http://localhost:"+port+"/"))
			)
		);
	}
}
