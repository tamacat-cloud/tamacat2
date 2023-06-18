/*
 * Copyright 2014 tamacat.org
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
package cloud.tamacat2.httpd.ssl;

import java.net.URL;
import java.security.KeyStore;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.HttpsConfig;
import cloud.tamacat2.httpd.error.InternalServerErrorException;
import cloud.tamacat2.httpd.util.ClassUtils;
import cloud.tamacat2.httpd.util.StringUtils;

/**
 * SSLContext for SNI (Multiple domain support)
 * "Server Name Indication" of TLS Extensions (RFC 6066).
 *
 * Add default domain in server.properties.
 * ex) https.defaultAlias=www.examples.com
 */
public class SSLSNIContextCreator extends DefaultSSLContextCreator {

	static final Logger LOG = LoggerFactory.getLogger(SSLSNIContextCreator.class);

	protected String defaultAlias;

	public SSLSNIContextCreator() {}

	public SSLSNIContextCreator(final HttpsConfig httpsConfig) {
		super(httpsConfig);
	}

	@Override
	public void setHttpsConfig(final HttpsConfig httpsConfig) {
		super.setHttpsConfig(httpsConfig);
		setDefaultAlias(httpsConfig.getDefaultAlias());
	}

	public SSLContext getSSLContext() {
		final String defaultAlias = getDefaultAlias();
		if (StringUtils.isEmpty(defaultAlias)) {
			return super.getSSLContext();
		}
		try {
			final URL url = ClassUtils.getURL(keyStoreFile);
			if (url == null) {
				throw new IllegalArgumentException("https.keyStoreFile ["+keyStoreFile+"] file not found.");
			}
			final KeyStore keystore = KeyStore.getInstance(type.name());
			keystore.load(url.openStream(), keyPassword.toCharArray());
			final KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmfactory.init(keystore, keyPassword.toCharArray());

			X509ExtendedKeyManager x509KeyManager = null;
			final KeyManager[] keymanagers = kmfactory.getKeyManagers();
			for (KeyManager keyManager : keymanagers) {
				if (keyManager instanceof X509ExtendedKeyManager) {
					x509KeyManager = (X509ExtendedKeyManager) keyManager;
					break;
				}
			}
			final SSLContext sslcontext = SSLContext.getInstance(protocol.version.getProtocol());
			if (x509KeyManager == null) {
				sslcontext.init(keymanagers, getTrustManager(), null);
			} else {
				final SNIKeyManager sniKeyManager = new SNIKeyManager(x509KeyManager, defaultAlias);
				sslcontext.init(new KeyManager[] { sniKeyManager }, getTrustManager(), null);
				if (LOG.isDebugEnabled()) {
					LOG.debug("TLS/SNI default=" + defaultAlias);
					final Enumeration<String> en = keystore.aliases();
					while (en.hasMoreElements()) {
						LOG.debug("TLS/SNI alias=" + en.nextElement());
					}
				}
			}
			return sslcontext;
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	public void setDefaultAlias(final String defaultAlias) {
		if (StringUtils.isNotEmpty(defaultAlias)) {
			this.defaultAlias = defaultAlias;
		}
	}

	public String getDefaultAlias() {
		return defaultAlias;
	}

	@Override
	public String toString() {
		return "SSLSNIContextCreator [defaultAlias=" + defaultAlias + ", keyStoreFile=" + keyStoreFile
				+ ", keyPassword=" + keyPassword + ", type=" + type + ", protocol=" + protocol + "]";
	}
}
