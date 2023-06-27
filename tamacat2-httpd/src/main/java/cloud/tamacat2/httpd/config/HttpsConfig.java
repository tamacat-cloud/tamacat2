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
	
	public String getKeyPassword() {
		return keyPassword;
	}
	
	public HttpsConfig keyPassword(final String keyPassword) {
		this.keyPassword = keyPassword;
		return this;
	}
	
	public String getKeyStoreType() {
		return keyStoreType;
	}
	
	public HttpsConfig keyStoreType(final String keyStoreType) {
		this.keyStoreType = keyStoreType;
		return this;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public HttpsConfig protocol(final String protocol) {
		this.protocol = protocol;
		return this;
	}
	
	public String getSupportProtocol() {
		return supportProtocol;
	}
	
	public HttpsConfig supportProtocol(final String supportProtocol) {
		this.supportProtocol = supportProtocol;
		return this;
	}
	
	public String getDefaultAlias() {
		return defaultAlias;
	}
	
	public HttpsConfig defaultAlias(final String defaultAlias) {
		this.defaultAlias = defaultAlias;
		return this;
	}
	
	public boolean useClientAuth() {
		return clientAuth;
	}
	
	public HttpsConfig clientAuth(final boolean clientAuth) {
		this.clientAuth = clientAuth;
		return this;
	}

	public String getCaKeyStoreFile() {
		return caKeyStoreFile;
	}

	public HttpsConfig caKeyStoreFile(final String caKeyStoreFile) {
		this.caKeyStoreFile = caKeyStoreFile;
		return this;
	}
	
	public String getCaKeyPassword() {
		return caKeyPassword;
	}
	
	public HttpsConfig caKeyPassword(final String caKeyPassword) {
		this.caKeyPassword = caKeyPassword;
		return this;
	}
	
	public String getCrl() {
		return crl;
	}
	
	public HttpsConfig crl(final String crl) {
		this.crl = crl;
		return this;
	}

	@Override
	public String toString() {
		return "HttpsConig [keyStoreFile=" + keyStoreFile + ", keyPassword=" + keyPassword + ", keyStoreType="
				+ keyStoreType + ", protocol=" + protocol + ", supportProtocol=" + supportProtocol + ", defaultAlias="
				+ defaultAlias + ", clientAuth=" + clientAuth + "]";
	}
}
