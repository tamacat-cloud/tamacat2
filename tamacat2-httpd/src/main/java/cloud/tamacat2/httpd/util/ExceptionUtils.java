/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat2.httpd.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

	public static String getStackTrace(final Throwable e) {
		final StringWriter out = new StringWriter();
		final PrintWriter w = new PrintWriter(out);
		e.printStackTrace(w);
		w.flush();
		return out.toString();
	}
	
	public static String getStackTrace(final Throwable e, final int endIndex) {
		String stackTrace = getStackTrace(e);
		if (stackTrace != null && stackTrace.length() > endIndex) {
			stackTrace = stackTrace.substring(0, endIndex) + "...";
		}
		return stackTrace;
	}
	
	public static String jsonEscape(final String value) {
		if (value != null) {
			return value.replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
		}
		return "";
	}
	
	public static String getJsonStackTrace(final Throwable e, final int endIndex) {
		final String message = jsonEscape(e.getMessage());
		final String stackTrace = jsonEscape(getStackTrace(e, endIndex));
		return """
			{\"message\":\"%s\",\"stackTrace\":\"%s\"}
			""".formatted(message, stackTrace);
	}
	
	public static boolean isRuntime(final Exception e) {
		return e != null && e instanceof RuntimeException;
	}
	
	public static Throwable getCauseException(final Exception e) {
		if (e == null) return null;
		final Throwable cause = e.getCause();
		if (cause != null) return cause;
		else return e;
	}
}
