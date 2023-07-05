package cloud.tamacat2.jetty;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.jetty.server.handler.ErrorHandler;

import cloud.tamacat2.httpd.error.ErrorPageTemplate;
import cloud.tamacat2.httpd.error.HttpStatusExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;

public class DefaultErrorHandler extends ErrorHandler {

    protected void writeErrorPage(final HttpServletRequest request, final Writer writer, 
    		final int code, final String message, final boolean showStacks) throws IOException {
    	writer.append(ErrorPageTemplate.create().getHtml(HttpStatusExceptionHandler.getException(code)));
	}
}
