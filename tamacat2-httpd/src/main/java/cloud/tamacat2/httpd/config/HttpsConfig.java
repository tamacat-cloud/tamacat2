/*
 * Copyright 2020 tamacat.org
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
package cloud.tamacat2.httpd.config;

public class HttpsConfig {

	protected String keyStoreFile;
	protected String keyPassword;
	protected String keyStoreType = "PKCS12";
	protected String protocol = "TLSv1_2";
	protected String supportProtocol = "TLSv1_2";
	protected String defaultAlias;
	protected String caKeyStoreFile;
	protected String caKeyPassword;
	protected String crl;
	protected boolean clientAuth;

	public static HttpsConfig create() {
		return new HttpsConfig();
	}
	
	public String getKeyStoreFile() {
		return keyStoreFile;
	}
	
	public HttpsConfig keyStoreFile(final String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
		return this;
	}
	
	public void setKeyStoreFile(final String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}
	
	public String getKeyPassword() {
		return keyPassword;
	}
	
	public HttpsConfig keyPassword(final String keyPassword) {
		this.keyPassword = keyPassword;
		return this;
	}
	
	public void setKeyPassword(final String keyPassword) {
		this.keyPassword = keyPassword;
	}
	
	public String getKeyStoreType() {
		return keyStoreType;
	}
	
	public void setKeyStoreType(final String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public void setProtocol(final String protocol) {
		this.protocol = protocol;
	}
	
	public String getSupportProtocol() {
		return supportProtocol;
	}
	
	public void setSupportProtocol(final String supportProtocol) {
		this.supportProtocol = supportProtocol;
	}
	
	public String getDefaultAlias() {
		return defaultAlias;
	}
	
	public void setDefaultAlias(final String defaultAlias) {
		this.defaultAlias = defaultAlias;
	}
	
	public boolean useClientAuth() {
		return clientAuth;
	}

	public void setClientAuth(final boolean clientAuth) {
		this.clientAuth = clientAuth;
	}

	public String getCaKeyStoreFile() {
		return caKeyStoreFile;
	}

	public void setCaKeyStoreFile(final String caKeyStoreFile) {
		this.caKeyStoreFile = caKeyStoreFile;
	}
	
	public String getCaKeyPassword() {
		return caKeyPassword;
	}
	
	public void setCaKeyPassword(final String caKeyPassword) {
		this.caKeyPassword = caKeyPassword;
	}
	
	public String getCrl() {
		return crl;
	}
	
	public void setCrl(final String crl) {
		this.crl = crl;
	}

	@Override
	public String toString() {
		return "HttpsConig [keyStoreFile=" + keyStoreFile + ", keyPassword=" + keyPassword + ", keyStoreType="
				+ keyStoreType + ", protocol=" + protocol + ", supportProtocol=" + supportProtocol + ", defaultAlias="
				+ defaultAlias + ", clientAuth=" + clientAuth + "]";
	}
}
