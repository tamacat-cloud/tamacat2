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

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.StandardConstants;
import javax.net.ssl.X509ExtendedKeyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNIKeyManager extends X509ExtendedKeyManager {

	static final Logger LOG = LoggerFactory.getLogger(SNIKeyManager.class);
	
	protected X509ExtendedKeyManager keyManager;
	protected String defaultAlias;
	
	public SNIKeyManager(final X509ExtendedKeyManager keyManager, final String defaultAlias) {
		this.keyManager = keyManager;
		this.defaultAlias = defaultAlias;
	}
	
	@Override
	public String chooseEngineServerAlias(final String keyType, final Principal[] issuers, final SSLEngine engine) {
		final ExtendedSSLSession session = (ExtendedSSLSession) engine.getHandshakeSession();
		for (SNIServerName name : session.getRequestedServerNames()) {
			if (name.getType() == StandardConstants.SNI_HOST_NAME) {
				final String hostname = ((SNIHostName) name).getAsciiName();
				LOG.trace("chooseEngineServerAlias="+hostname);
				return getCertificateHostname(hostname);
			}
		}
		return keyManager.chooseEngineServerAlias(keyType, issuers, engine);
	}
	
	@Override
	public String chooseClientAlias(final String[] arg0, final Principal[] arg1, final Socket arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
		if (socket instanceof SSLSocket) {
			final ExtendedSSLSession session = (ExtendedSSLSession)((SSLSocket)socket).getHandshakeSession();
			for (SNIServerName name : session.getRequestedServerNames()) {
				if (name.getType() == StandardConstants.SNI_HOST_NAME) {
					final String hostname = ((SNIHostName)name).getAsciiName();
					LOG.trace("chooseServerAlias="+hostname);
					return getCertificateHostname(hostname);
				}
			}
		}
		return keyManager.chooseServerAlias(keyType, issuers, socket);
	}

	@Override
	public X509Certificate[] getCertificateChain(final String alias) {
		return keyManager.getCertificateChain(alias);
	}

	@Override
	public String[] getClientAliases(final String keyType, final Principal[] issuers) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PrivateKey getPrivateKey(final String alias) {
		return keyManager.getPrivateKey(alias);
	}

	@Override
	public String[] getServerAliases(final String keyType, final Principal[] issuers) {
		return keyManager.getServerAliases(keyType, issuers);
	}
	
	/**
	 * Can't get a cert and key, use a default alias.
	 * @param hostname
	 */
	protected String getCertificateHostname(final String hostname) {
		if (hostname != null && getCertificateChain(hostname) != null && getPrivateKey(hostname) != null) {
			return hostname;
		} else {
			return defaultAlias;
		}
	}
}
