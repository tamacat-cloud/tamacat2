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
package cloud.tamacat2.httpd.error;

import org.apache.hc.core5.http.HttpStatus;

/**
 * <p>Throws 401 Unauthorized
 */
public class UnauthorizedException extends HttpStatusException {

	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE = "This server could not verify that you are authorized to access the document requested. Either you supplied the wrong credentials (e.g., bad password), or your browser doesn't understand how to supply the credentials required.";

	public UnauthorizedException() {
		super(HttpStatus.SC_UNAUTHORIZED, MESSAGE);
	}
	
	public UnauthorizedException(final String message) {
		super(HttpStatus.SC_UNAUTHORIZED, message);
	}
	
	public UnauthorizedException(final Throwable cause) {
		super(HttpStatus.SC_UNAUTHORIZED, cause);
	}
	
	public UnauthorizedException(final String message, final Throwable cause) {
		super(HttpStatus.SC_UNAUTHORIZED, message, cause);
	}
}