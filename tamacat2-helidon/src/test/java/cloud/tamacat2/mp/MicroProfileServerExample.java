package cloud.tamacat2.mp;

import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.reverse.ReverseProxy;
import cloud.tamacat2.reverse.config.ReverseConfig;
import cloud.tamacat2.reverse.config.ReverseUrlConfig;

public class MicroProfileServerExample extends ReverseProxy {

	//http://localhost:8080/examples/test
	public static void main(String[] args) {
		new MicroProfileServer().startup(HttpConfig.create().port(8080)
			.urlConfig(ReverseUrlConfig.create().path("/examples/")
				.reverse(ReverseConfig.create().url("http://localhost:8081/"))
			)
		);
	}
}
