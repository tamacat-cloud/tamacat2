import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.reverse.ReverseProxy;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;
import cloud.tamacat2.reverse.config.ReverseConfig;

public class ReverseProxyExamples {
	public static void main(String[] args) {
		new ReverseProxy().startup(HttpConfig.create().port(80)
			.urlConfig(ReverseUrlConfig.create().path("/examples/")
				.reverse(ReverseConfig.create().url("http://localhost:8080/examples/"))
			)
		);
	}
}