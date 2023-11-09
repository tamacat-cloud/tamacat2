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
package cloud.tamacat2.jetty.ee8;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.jetty.server.handler.ErrorHandler;

import cloud.tamacat2.httpd.error.ErrorPageTemplate;
import cloud.tamacat2.httpd.error.HttpStatusExceptionHandler;
import javax.servlet.http.HttpServletRequest;

public class DefaultErrorHandler extends ErrorHandler {

    protected void writeErrorPage(final HttpServletRequest request, final Writer writer, 
    		final int code, final String message, final boolean showStacks) throws IOException {
    	writer.append(ErrorPageTemplate.create().getHtml(HttpStatusExceptionHandler.getException(code)));
	}
}
