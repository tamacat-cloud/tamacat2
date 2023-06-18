package cloud.tamacat2.jetty.config;

import cloud.tamacat2.reverse.config.ReverseUrlConfig;

public class JettyUrlConfig extends ReverseUrlConfig {
	
	public static JettyUrlConfig create() {
		return new JettyUrlConfig();
	}
}
