/*
 * Copyright 2023 tamacat.org
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
package cloud.tamacat2.httpd.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class StringUtils {
	
	private static final String EMPTY = "";

	public static boolean isNotEmpty(Object value) {
		return value != null && !EMPTY.equals(value);
	}

	public static boolean isEmpty(Object value) {
		return value == null || EMPTY.equals(value);
	}
	
	/**
	 * split and trim to String array.
	 * 
	 * @param value
	 * @param sep
	 *            ex) "," ,"\t"
	 * @return String[]
	 * @since 1.2-20150417
	 */
	public static String[] split(String value, String sep) {
		String val = value != null ? value.trim() : "";
		if (val.indexOf(sep) >= 0) {
			return val.split("\\s*" + Pattern.quote(sep) + "\\s*");
		} else {
			if (val.length() > 0) {
				return new String[] { val };
			} else {
				return new String[0];
			}
		}
	}
	
	/**
	 * <p>
	 * Returns value of type. when data is {@code null}, returns default value.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parse(String data, T defaultValue) {
		if (data == null)
			return defaultValue;
		try {
			if (ClassUtils.isTypeOf(defaultValue.getClass(), String.class)) {
				return (T) data;
			} else if (ClassUtils.isTypeOf(defaultValue.getClass(), Integer.class)) {
				return (T) Integer.valueOf(data);
			} else if (ClassUtils.isTypeOf(defaultValue.getClass(), Long.class)) {
				return (T) Long.valueOf(data);
			} else if (ClassUtils.isTypeOf(defaultValue.getClass(), Float.class)) {
				return (T) Float.valueOf(data);
			} else if (ClassUtils.isTypeOf(defaultValue.getClass(), Double.class)) {
				return (T) Double.valueOf(data);
			} else if (ClassUtils.isTypeOf(defaultValue.getClass(), BigDecimal.class)) {
				return (T) new BigDecimal(data);
			} else if (ClassUtils.isTypeOf(defaultValue.getClass(), Boolean.class)) {
				return (T) Boolean.valueOf(data);
			}
		} catch (Exception e) {
		}
		return defaultValue;
	}
}
