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
package cloud.tamacat2.httpd.error;

import org.apache.hc.core5.http.HttpStatus;

/**
 * <p>Throws 500 Internal Server Error.
 */
public class InternalServerErrorException extends HttpStatusException {

	private static final long serialVersionUID = 1L;

	public static final String MESSAGE = "The server encountered an internal error or misconfiguration and was unable to complete your request.";
	
	public InternalServerErrorException() {
		super(HttpStatus.SC_INTERNAL_SERVER_ERROR, MESSAGE);
	}

	public InternalServerErrorException(final Throwable cause) {
		super(HttpStatus.SC_INTERNAL_SERVER_ERROR, cause);
	}

	public InternalServerErrorException(final String message) {
		super(HttpStatus.SC_INTERNAL_SERVER_ERROR, message);
	}

	public InternalServerErrorException(final String message, final Throwable cause) {
		super(HttpStatus.SC_INTERNAL_SERVER_ERROR, message, cause);
	}
}