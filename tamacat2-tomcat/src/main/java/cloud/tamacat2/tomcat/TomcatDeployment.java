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

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.RemoteCIDRValve;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.util.ServerUtils;
import cloud.tamacat2.httpd.util.StringUtils;
import cloud.tamacat2.tomcat.config.TomcatConfig;

/**
 * Deployment configuration for Tomcat Embedded.
 */
public class TomcatDeployment {

	static final Logger LOG = LoggerFactory.getLogger(TomcatDeployment.class);

	protected String serverHome;
	protected String hostname = "127.0.0.1";
	protected String bindAddress = "127.0.0.1/32";
	protected String allowRemoteCIDRValve;
	protected String webapps = "./webapps";
	protected String contextPath;
	protected String work = "${server.home}";
	protected Tomcat tomcat;
	protected boolean useWarDeploy = false;
	protected String uriEncoding;
	protected Boolean useBodyEncodingForURI;

	// JarScanner
	protected boolean scanBootstrapClassPath = false;
	protected boolean scanClassPath = true;
	protected boolean scanManifest = false;
	protected boolean scanAllDirectories = true;
	protected boolean scanAllFiles = false;

	/**
	 * Deployment Web Applications for Tomcat Embedded
	 * 
	 * @param tomcatConfig
	 */
	protected void deploy(TomcatConfig tomcatConfig) {
		tomcat = TomcatManager.getInstance().getTomcat(tomcatConfig.getPort());
		tomcat.setBaseDir(getWork());
		// tomcat.getServer().getCatalina().setParentClassLoader(getClassLoader());

		// Tomcat bind address default: 127.0.0.1
		if (StringUtils.isNotEmpty(bindAddress)) {
			tomcat.getConnector().setProperty("address", bindAddress);
		}
		if (StringUtils.isNotEmpty(uriEncoding)) {
			tomcat.getConnector().setURIEncoding(uriEncoding);
		}
		if (useBodyEncodingForURI != null) {
			tomcat.getConnector().setUseBodyEncodingForURI(useBodyEncodingForURI.booleanValue());
		}
		if (useWarDeploy) {
			deployWarFiles(tomcatConfig);
		}

		deployWebapps(tomcatConfig);
	}

	/**
	 * Deployment for webapps/app
	 * 
	 * @param tomcatConfig
	 */
	protected void deployWebapps(TomcatConfig tomcatConfig) {
		try {
			String path = tomcatConfig.getPath().replaceAll("/$", "");
			String contextPath = tomcatConfig.getContextPath();
			if (StringUtils.isNotEmpty(contextPath)) {
				contextPath = path;
			}
			// check already add webapp.
			if (tomcat.getHost().findChild(path) != null) {
				return; // skip
			}
			File baseDir = new File(getWebapps() + contextPath);
			Context ctx = tomcat.addWebapp(path, baseDir.getAbsolutePath());
			// ctx.setParentClassLoader(getClassLoader());
			ctx.setJarScanner(createJarScanner());
			LOG.info("Tomcat port=" + tomcatConfig.getPort() + ", path=" + path + ", dir=" + baseDir.getAbsolutePath());
			setAllowRemoteCIDRValve(tomcatConfig.getAllowRemoteCIDRValve());
			allowRemoteCIDRValve(ctx);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
	}

	/**
	 * Auto deployment for war files in webapps.
	 * 
	 * @param tomcatConfig
	 */
	protected void deployWarFiles(TomcatConfig tomcatConfig) {
		try {
			File webappsRoot = new File(getWebapps());
			File[] warfiles = webappsRoot.listFiles(new WarFileFilter());
			if (warfiles == null) {
				return;
			}
			for (File war : warfiles) {
				String contextRoot = "/" + war.getName().replace(".war", "");
				// Skip already added webapp.
				if (tomcat.getHost().findChild(contextRoot) != null) {
					continue;
				}
				// Skip already exists extract directory.
				if (Files.isDirectory(Paths.get(webappsRoot.getAbsolutePath(), contextRoot))) {
					LOG.info("[skip] war deploy: " + war.getAbsolutePath());
					continue;
				}

				Context ctx = tomcat.addWebapp(contextRoot, war.getAbsolutePath());
				// ctx.setParentClassLoader(getClassLoader());
				ctx.setJarScanner(createJarScanner());
				LOG.info("Tomcat port=" + tomcatConfig.getPort() + ", path=" + contextRoot + ", war="
						+ war.getAbsolutePath());
				
				setAllowRemoteCIDRValve(tomcatConfig.getAllowRemoteCIDRValve());
				allowRemoteCIDRValve(ctx);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
	}

	/**
	 * Create new JarScanner instance.
	 * 
	 * @return StandardJarScanner
	 */
	protected JarScanner createJarScanner() {
		StandardJarScanner scanner = new StandardJarScanner();
		scanner.setScanBootstrapClassPath(scanBootstrapClassPath);
		scanner.setScanClassPath(scanClassPath);
		scanner.setScanManifest(scanManifest);
		scanner.setScanAllDirectories(scanAllDirectories);
		scanner.setScanAllFiles(scanAllFiles);
		LOG.debug("create new StandardJarScanner() [scanBootstrapClassPath=" + scanBootstrapClassPath
				+ ", scanClassPath=" + scanClassPath + ", scanManifest=" + scanManifest + ", scanAllDiredtories="
				+ scanAllDirectories + ", scanAllFiles=" + scanAllFiles + "]");
		return scanner;
	}

	/**
	 * Denied Tomcat direct access -> HTTP Status 403 – Forbidden
	 * 
	 * @param ctx
	 */
	protected void allowRemoteCIDRValve(Context ctx) {
		if (StringUtils.isNotEmpty(allowRemoteCIDRValve)) {
			RemoteCIDRValve valve = new RemoteCIDRValve();
			valve.setAllow(allowRemoteCIDRValve);
			ctx.getPipeline().addValve(valve);
		}
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setServerHome(String serverHome) {
		this.serverHome = serverHome;
	}

	protected String getServerHome() {
		if (StringUtils.isEmpty(serverHome)) {
			serverHome = ServerUtils.getServerHome();
		}
		return serverHome;
	}

	public void setWebapps(String webapps) {
		if (work.indexOf("${server.home}") >= 0) {
			this.webapps = webapps.replace("${server.home}", getServerHome()).replace("\\", "/");
		} else {
			this.webapps = webapps;
		}
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	protected String getWebapps() {
		return webapps;
	}

	public void setWork(String work) {
		this.work = work;
	}

	protected String getWork() {
		if (work.indexOf("${server.home}") >= 0) {
			this.work = work.replace("${server.home}", getServerHome()).replace("\\", "/");// .replaceAll("/work$", "");
		}
		return work;
	}

	public void setAllowRemoteCIDRValve(String allowRemoteCIDRValve) {
		this.allowRemoteCIDRValve = allowRemoteCIDRValve;
	}

	/**
	 * Auto Deployment for war files. (default: true)
	 * 
	 * @param useWarDeploy
	 */
	public void setUseWarDeploy(boolean useWarDeploy) {
		this.useWarDeploy = useWarDeploy;
	}

	/**
	 * Tomcat Connector#setURIEncoding(String) default: UTF-8
	 * 
	 * @see org.apache.catalina.connector.Connector#setURIEncoding(String)
	 */
	public void setUriEncoding(String uriEncoding) {
		this.uriEncoding = uriEncoding;
	}

	/**
	 * Tomcat Connector#seUseBodyEncodingForURI(boolean) default: false (unset/null)
	 * 
	 * @see org.apache.catalina.connector.Connector#setUseBodyEncodingForURI(boolean)
	 */
	public void seUseBodyEncodingForURI(String useBodyEncodingForURI) {
		this.useBodyEncodingForURI = Boolean.valueOf(useBodyEncodingForURI);
	}

	/**
	 * Tomcat Connector#seUseBodyEncodingForURI(boolean) default: false (unset/null)
	 * 
	 * @see org.apache.catalina.connector.Connector#setUseBodyEncodingForURI(boolean)
	 */
	public void seUseBodyEncodingForURI(boolean useBodyEncodingForURI) {
		this.useBodyEncodingForURI = useBodyEncodingForURI;
	}

	/**
	 * Tomcat Connector#setProperty("address", bindAddress)
	 * 
	 * @param bindAddress default: 127.0.0.1
	 * @since 1.5-20220113
	 */
	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}

	/**
	 * Tomcat Context StandardJarScanner#setScanBootstrapClassPath(boolean) Controls
	 * the testing of the bootstrap classpath which consists of the runtime classes
	 * provided by the JVM and any installed system extensions.
	 * 
	 * @param scanBootstrapClassPath default: false
	 * @since 1.5-20220128
	 */
	public void setScanBootstrapClassPath(boolean scanBootstrapClassPath) {
		this.scanBootstrapClassPath = scanBootstrapClassPath;
	}

	/**
	 * Tomcat Context StandardJarScanner#setScanClassPath(boolean) Controls the
	 * classpath scanning extension.
	 * 
	 * @param scanClassPath default: true
	 * @since 1.5-20220128
	 */
	public void setScanClassPath(boolean scanClassPath) {
		this.scanClassPath = scanClassPath;
	}

	/**
	 * Tomcat Context StandardJarScanner#setScanManifest(boolean) Controls the JAR
	 * file Manifest scanning extension.
	 * 
	 * @param scanManifest default: false
	 * @since 1.5-20220128
	 */
	public void setScanManifest(boolean scanManifest) {
		this.scanManifest = scanManifest;
	}

	/**
	 * Tomcat Context StandardJarScanner#setScanAllDirectories(boolean) Controls the
	 * testing all directories to see of they are exploded JAR files extension.
	 * 
	 * @param scanAllDirectories default: true
	 */
	public void setScanAllDirectories(boolean scanAllDirectories) {
		this.scanAllDirectories = scanAllDirectories;
	}

	/**
	 * Tomcat Context JarScanner#setScanAllFiles(boolean) Controls the testing all
	 * files to see of they are JAR files extension.
	 * 
	 * @param scanAllFiles default: false
	 */
	public void setScanAllFiles(boolean scanAllFiles) {
		this.scanAllFiles = scanAllFiles;
	}

	/**
	 * FileFilter for .war file
	 */
	static class WarFileFilter implements FileFilter {

		@Override
		public boolean accept(File file) {
			return file.isFile() && file.getName().endsWith(".war");
		}
	}
}
