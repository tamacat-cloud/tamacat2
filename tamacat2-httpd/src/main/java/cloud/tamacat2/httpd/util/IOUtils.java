/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat2.httpd.util;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URL;

/**
 * Class of Utilities for I/O
 */
public class IOUtils {

	/**
	 * Get the InputStream from Resource in FileInputStream or CLASSPATH.
	 * 1. FileInputStream, 2. CLASSPATH
	 * @param path
	 * @return InputStream
	 */
	public static InputStream getFileOrClasspathInputStream(String path, ClassLoader loader) {
		try {
			return new FileInputStream(path);
		} catch (IOException e) {
			//ignore
		}
		return getInputStream(path, loader);
	}
	
	/**
	 * Get the InputStream from Resource in CLASSPATH.
	 * 
	 * @param path
	 * @return InputStream
	 */
	public static InputStream getInputStream(String path) {
		return getInputStream(path, ClassUtils.getDefaultClassLoader());
	}

	/**
	 * Get the InputStream from Resource in CLASSPATH.
	 * 
	 * @param path
	 *            File path in CLASSPATH
	 * @return InputStream
	 * @since 0.7
	 */
	public static InputStream getInputStream(String path, ClassLoader loader) {
		URL url = ClassUtils.getURL(getClassPathToResourcePath(path), loader);
		InputStream in = null;
		try {
			in = url.openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NullPointerException e) {
			throw new RuntimeException(path + " is not found.");
		}
		return in;
	}

	/**
	 * Convert the format of CLASSPATH('.' seperator) to Resource path('/'
	 * separator)
	 * 
	 * @param path
	 * @return InputStream
	 */
	public static String getClassPathToResourcePath(String path) {
		if (path == null || path.indexOf('/') >= 0)
			return path;
		int idx = path.lastIndexOf(".");
		if (idx >= 0) {
			String name = path.substring(0, idx);
			String ext = path.substring(idx, path.length());
			return name.replace('.', '/') + ext;
		} else {
			return path;
		}
	}

	/**
	 * It performs, when the "close()" method is implemented.
	 * 
	 * @param target
	 */
	public static void close(Object target) {
		if (target != null) {
			if (target instanceof Closeable) {
				close((Closeable) target);
			} else {
				try {
					Method closable = ClassUtils.searchMethod(target.getClass(), "close");
					if (closable != null)
						closable.invoke(target);
				} catch (Exception e) {
					Throwable cause = e.getCause();
					if (cause != null && cause instanceof IOException) {
						throw new RuntimeException(cause);
					}
				}
			}
		}
	}

	/**
	 * When an exception occurs, RuntimeIOException will be given up if it is
	 * OutputStream or Writer.
	 * 
	 * @param AutoCloseable
	 */
	public static void close(AutoCloseable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (Exception e) {
			if (closeable instanceof OutputStream || closeable instanceof Writer) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * When an exception occurs then throws RuntimeException.
	 * @param closeable
	 * @param cause If the cause is null, no exception is thrown.
	 */
	public static void close(AutoCloseable closeable, RuntimeException cause) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (Exception e) {
			if (cause != null) {
				throw cause;
			}
		}
	}
	
	/**
	 * It ignores, even if an exception occurs.
	 * 
	 * @param socket
	 */
	public static void close(Socket socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}
}
