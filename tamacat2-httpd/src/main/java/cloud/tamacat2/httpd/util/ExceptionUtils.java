/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat2.httpd.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

	public static String getStackTrace(Throwable e) {
		StringWriter out = new StringWriter();
		PrintWriter w = new PrintWriter(out);
		e.printStackTrace(w);
		w.flush();
		return out.toString();
	}
	
	public static String getStackTrace(Throwable e, int endIndex) {
		String stackTrace = getStackTrace(e);
		if (stackTrace != null && stackTrace.length() > endIndex) {
			stackTrace = stackTrace.substring(0, endIndex) + "...";
		}
		return stackTrace;
	}
	
	public static boolean isRuntime(Exception e) {
		return e != null && e instanceof RuntimeException;
	}
	
	public static Throwable getCauseException(Exception e) {
		if (e == null) return null;
		Throwable cause = e.getCause();
		if (cause != null) return cause;
		else return e;
	}
}
