/*
 * Copyright 2021 tamacat.org
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
package cloud.tamacat2.jetty;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.plugin.PluginServer;
import cloud.tamacat2.httpd.util.StringUtils;

public class JettyManager implements PluginServer {

	static final Logger LOG = LoggerFactory.getLogger(JettyManager.class);
	
	static final Map<Integer, Server> MANAGER = new HashMap<>();
	
	static final JettyManager SELF = new JettyManager();
	
	public static JettyManager getInstance() {
		return SELF;
	}
	
	final ReentrantLock lock = new ReentrantLock();
	
	/**
	 * The instance corresponding to a port is returned. 
	 * @param port
	 * @return Server instance
	 */
	public Server getServer(final int port) {
		return getServer(null, port);
	}
	
	/**
	 * The instance corresponding to a port is returned. 
	 * @param port
	 * @return Server instance
	 */
	public Server getServer(final String host, final int port) {
		lock.lock();
		try {
			Server instance = MANAGER.get(port);
			if (instance == null) {
				if (StringUtils.isNotEmpty(host)) {
					instance = new Server(InetSocketAddress.createUnresolved(host, port));
				} else {
					instance = new Server(port);
				}
				MANAGER.put(port, instance);
			}
			return instance;
		} finally {
			lock.unlock();
		}
	}
	
	
	/**
	 * Start the all Server instances.
	 */
	public void start() {
		for (final Server server : MANAGER.values()) {
			final JettyThread jetty = new JettyThread(server);
			jetty.start();
		}
	}
	
	/**
	 * Stop the all Server instances.
	 */
	public void stop() {
		for (final Server instance : MANAGER.values()) {
			try {
				instance.stop();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	private JettyManager() {}

	/**
	 * Thread of Jetty inscanse.
	 */
    static class JettyThread extends Thread {
    	final Server server;
    	JettyThread(final Server server) {
    		this.server = server;
    	}
    	
    	public void run() {
    		try {
    			server.start();
    			server.join();
    			LOG.info(server.dump());
    		} catch (Exception e) {
				LOG.error(e.getMessage(), e);
    		}
    	}
    }
}
