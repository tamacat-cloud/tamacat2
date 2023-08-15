/*
 * Copyright 2023 tamacat.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.tamacat2.tomcat;

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.plugin.PluginServer;
import cloud.tamacat2.httpd.util.StringUtils;

public class TomcatManager implements PluginServer {

	static final Logger LOG = LoggerFactory.getLogger(TomcatManager.class);
	
	static final Map<Integer, Tomcat> MANAGER = new HashMap<>();
	
	static final TomcatManager SELF = new TomcatManager();
	
	public static TomcatManager getInstance() {
		return SELF;
	}
		
	/**
	 * The instance corresponding to a port is returned. 
	 * @param port
	 * @return Server instance
	 */
	public synchronized Tomcat getTomcat(int port) {
		return getTomcat(null, port);
	}
	
	/**
	 * The instance corresponding to a port is returned. 
	 * @param port
	 * @return Server instance
	 */
	public synchronized Tomcat getTomcat(String host, int port) {
		Tomcat instance = MANAGER.get(port);
		if (instance == null) {
			if (StringUtils.isNotEmpty(host)) {
				instance = new Tomcat();//(InetSocketAddress.createUnresolved(host, port));
				instance.setHostname(host);
				instance.setPort(port);
			} else {
				instance = new Tomcat();
				instance.setPort(port);
			}
			MANAGER.put(port, instance);
		}
		return instance;
	}
	
	
	/**
	 * Start the all Server instances.
	 */
	public void start() {
		for (Tomcat instance : MANAGER.values()) {
			TomcatThread tomcat = new TomcatThread(instance);
			tomcat.start();
		}
	}
	
	/**
	 * Stop the all Server instances.
	 */
	public void stop() {
		for (Tomcat instance : MANAGER.values()) {
			try {
				instance.stop();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	private TomcatManager() {}

	/**
	 * Thread of Jetty inscanse.
	 */
    static class TomcatThread extends Thread {
    	final Tomcat tomcat;
    	TomcatThread(Tomcat tomcat) {
    		this.tomcat = tomcat;
    	}
    	
    	public void run() {
    		try {
    			tomcat.start();
    			LOG.info(tomcat.toString());
    		} catch (Exception e) {
				LOG.error(e.getMessage(), e);
    		}
    	}
    }
}
