package cloud.tamacat2.jetty;

import cloud.tamacat2.jetty.config.JettyUrlConfig;

public interface JettyDeployment {

	/**
	 * Deployment Web Applications for Jetty Embedded
	 * 
	 * @param serviceUrl
	 */
	void deploy(JettyUrlConfig jettyUrlConfig);
}
