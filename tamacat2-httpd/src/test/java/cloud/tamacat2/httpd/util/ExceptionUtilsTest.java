package cloud.tamacat2.httpd.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExceptionUtilsTest {

	
	@Test
	void testGetStackTraceThrowable() {
		
	}

	@Test
	void testGetStackTraceThrowableInt() {
		
	}

	@Test
	void testGetJsonStackTrace() {
		assertTrue(ExceptionUtils.getJsonStackTrace(new RuntimeException("error"), 200)
			.startsWith("{\"message\":\"error\",\"stackTrace\":\"java.lang.RuntimeException: error"));
	}

	@Test
	void testIsRuntime() {
		
	}

	@Test
	void testGetCauseException() {
		
	}

}
