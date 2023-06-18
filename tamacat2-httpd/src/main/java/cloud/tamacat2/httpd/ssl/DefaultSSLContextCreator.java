/*
 * Copyright 2009 tamacat.org
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

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CertPathParameters;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.hc.core5.http.ssl.TLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.config.HttpsConfig;
import cloud.tamacat2.httpd.error.InternalServerErrorException;
import cloud.tamacat2.httpd.util.ClassUtils;
import cloud.tamacat2.httpd.util.DateUtils;
import cloud.tamacat2.httpd.util.StringUtils;

/**
 * <p>
 * The {@link SSLContext} create from {@link ServerConfig} or setter methods.
 */
public class DefaultSSLContextCreator implements SSLContextCreator {

	static final Logger LOG = LoggerFactory.getLogger(DefaultSSLContextCreator.class);

	protected String keyStoreFile = "default/localhost.p12";
	protected String keyPassword = "changeit";
	protected KeyStoreType type = KeyStoreType.PKCS12;
	protected TLS protocol = TLS.V_1_2;
	protected String defaultProtocols = "TLSv1.2,TLSv1.3";
	
	protected String caKeyStoreFile ="default/cacerts";
	protected String caKeyPassword = "changeit";
	protected KeyStoreType caKeyStoreType = KeyStoreType.PKCS12;
	protected String crlFile;

	/**
	 * <p>
	 * Default constructor.
	 */
	public DefaultSSLContextCreator() {
	}

	/**
	 * <p>
	 * The constructor of setting values from {@code httpsConfig}.
	 */
	public DefaultSSLContextCreator(final HttpsConfig httpsConfig) {
		setHttpsConfig(httpsConfig);
	}

	public void setHttpsConfig(final HttpsConfig httpsConfig) {
		setKeyStoreFile(httpsConfig.getKeyStoreFile());
		setKeyPassword(httpsConfig.getKeyPassword());
		setKeyStoreType(httpsConfig.getKeyStoreType());
		setSSLProtocol(httpsConfig.getProtocol());
		
		setCAKeyStoreFile(httpsConfig.getCaKeyStoreFile());
		setCAKeyPassword(httpsConfig.getCaKeyPassword());
		setCrlFile(httpsConfig.getCrl());
	}
	
	public void setKeyStoreFile(final String keyStoreFile) {
		if (keyStoreFile != null) {
			this.keyStoreFile = keyStoreFile;
		}
	}

	public void setKeyPassword(final String keyPassword) {
		if (keyPassword != null) {
			this.keyPassword = keyPassword;
		}
	}

	public void setKeyStoreType(final String type) {
		this.type = KeyStoreType.valueOf(type);
	}

	public void setKeyStoreType(final KeyStoreType type) {
		this.type = type;
	}

	public void setSSLProtocol(final String protocol) {
		this.protocol = TLS.valueOf(protocol.replace("TLSv", "V_").replace(".", "_"));
	}

	public void setSSLProtocol(final TLS protocol) {
		this.protocol = protocol;
	}

	public void setCAKeyStoreFile(final String caKeyStoreFile) {
		this.caKeyStoreFile = caKeyStoreFile;
	}

	public void setCAKeyPassword(final String caKeyPassword) {
		this.caKeyPassword = caKeyPassword;
	}
	
	public void setCrlFile(final String crlFile) {
		this.crlFile = crlFile;
	}
	
	public SSLContext getSSLContext() {
		try {
			final URL url = ClassUtils.getURL(keyStoreFile);
			if (url == null) {
				throw new IllegalArgumentException("https.keyStoreFile ["+keyStoreFile+"] file not found.");
			}
			final KeyStore keystore = KeyStore.getInstance(type.name());
			keystore.load(url.openStream(), keyPassword.toCharArray());

			final KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmfactory.init(keystore, keyPassword.toCharArray());
			final KeyManager[] keymanagers = kmfactory.getKeyManagers();
			final SSLContext sslcontext = SSLContext.getInstance(protocol.version.getProtocol());
			sslcontext.init(keymanagers, getTrustManager(), null);
			return sslcontext;
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}
	
	protected TrustManager[] getTrustManager() throws Exception {
		if (StringUtils.isNotEmpty(crlFile)) {
			//CA certs (trustcacerts keystore)
			final KeyStore ca = KeyStore.getInstance(caKeyStoreType.name());
			final URL caUrl = getCAKeyStoreFile();
			if (caUrl != null) {
				ca.load(caUrl.openStream(), caKeyPassword.toCharArray());
				final TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
				final CertPathParameters pkixParams = new PKIXBuilderParameters(ca, new X509CertSelector());
				((PKIXBuilderParameters) pkixParams).setRevocationEnabled(true);
				final CertificateFactory factory = CertificateFactory.getInstance("X.509");
				final URL crlUrl = getCRLFile();
				final X509CRL x509crl = (X509CRL)factory.generateCRL(crlUrl.openStream());
				Collection<CRL> crls = new HashSet<>();
			    crls.add(x509crl);
			    LOG.debug(toStringWithAlgName(x509crl));
			    
			    final List<CertStore> certStores =  new ArrayList<>();
				certStores.add(CertStore.getInstance("Collection", new CollectionCertStoreParameters(crls)));
				((PKIXBuilderParameters) pkixParams).setCertStores(certStores);
				
				tmf.init(new CertPathTrustManagerParameters(pkixParams));
				return tmf.getTrustManagers();
			}

		}
		return null;
	}
	
    public String toStringWithAlgName(final X509CRL x509crl) {
    	final StringBuffer sb = new StringBuffer();
        sb.append("X.509 CRL v" + (x509crl.getVersion()) + "\n");
        if (x509crl.getSigAlgOID() != null)
            sb.append("Signature Algorithm: " + x509crl.getSigAlgName() + ", OID=" + (x509crl.getSigAlgOID()) + "\n");
        if (x509crl.getIssuerX500Principal() != null)
            sb.append("Issuer: " + x509crl.getIssuerX500Principal().getName() + "\n");
        if (x509crl.getThisUpdate() != null)
            sb.append("This Update: " + getDateString(x509crl.getThisUpdate()) + "\n");
        if (x509crl.getNextUpdate() != null)
            sb.append("Next Update: " + getDateString(x509crl.getNextUpdate()) + "\n");
        if (x509crl.getRevokedCertificates() == null || x509crl.getRevokedCertificates().isEmpty())
            sb.append("NO certificates have been revoked\n");
        else {
            sb.append("Revoked Certificates: " + x509crl.getRevokedCertificates().size());
            int i = 1;
            for (X509CRLEntry entry: x509crl.getRevokedCertificates()) {
                sb.append("\n[" + i++ + "] Serial: " + entry.getSerialNumber().toByteArray()
                +", Revocation: "+getDateString(entry.getRevocationDate()));
            }
        }
        return sb.toString();
    }
    
    protected String getDateString(final Date date) {
    	return DateUtils.getTime(date, "yyyy-MM-dd HH:mm:ss z", Locale.getDefault(), TimeZone.getDefault());
    }
    
	protected URL getKeyStoreFile() {
		final URL caUrl = ClassUtils.getURL(keyStoreFile);
		if (caUrl == null) {
			throw new IllegalArgumentException("https.keyStoreFile ["+keyStoreFile+"] file not found.");
		}
		return caUrl;
	}
	
	protected URL getCAKeyStoreFile() {
		if (caKeyStoreFile == null) return null;
		final URL caUrl = ClassUtils.getURL(caKeyStoreFile);
		if (caUrl == null) {
			throw new IllegalArgumentException("https.CA.keyStoreFile ["+caKeyStoreFile+"] file not found.");
		}
		return caUrl;
	}
	
	protected URL getCRLFile() {
		if (StringUtils.isEmpty(crlFile)) {
			throw new IllegalArgumentException("https.CRL ["+crlFile+"] file not found.");
		}
		if (crlFile.startsWith("http://") || crlFile.startsWith("https://")) {
			try {
				return new URL(crlFile);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("https.CRL ["+crlFile+"] file not found.", e);
			}
		} else {
			final URL crlUrl = ClassUtils.getURL(crlFile);
			if (crlUrl == null) {
				throw new IllegalArgumentException("https.CRL ["+crlFile+"] file not found.");
			}
			return crlUrl;
		}
	}
}
