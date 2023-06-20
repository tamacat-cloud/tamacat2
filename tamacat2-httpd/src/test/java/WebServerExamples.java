import cloud.tamacat2.httpd.WebServer;
import cloud.tamacat2.httpd.config.HttpConfig;
import cloud.tamacat2.httpd.config.UrlConfig;

public class WebServerExamples {
	public static void main(String[] args) {
		new WebServer().startup(HttpConfig.create().port(8080)
			.urlConfig(UrlConfig.create().path("/examples/")
				.docsRoot("${server.home}/htdocs/")
			)
			.contentEncoding("gzip")
		);
	}
}