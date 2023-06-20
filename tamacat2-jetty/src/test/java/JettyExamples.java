import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.jetty.config.JettyUrlConfig;
import cloud.tamacat2.jetty.JettyServer;
import cloud.tamacat2.reverse.config.ReverseConfig;

public class JettyExamples {
	public static void main(String[] args) {
		new JettyServer().startup(HttpConfig.create().port(80)
			.urlConfig(JettyUrlConfig.create().path("/examples/")
				.reverse(ReverseConfig.create().url("http://127.0.0.1:8080/examples/"))
			)
		);
	}
}