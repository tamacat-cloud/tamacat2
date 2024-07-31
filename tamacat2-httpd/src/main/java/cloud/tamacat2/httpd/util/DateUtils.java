/*
 * Copyright 2007 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat2.httpd.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utilities of Date.
 */
public abstract class DateUtils {

	static final Locale currentLocale = Locale.getDefault();

	public static String getTime(final Date date, final String pattern) {
		return getTime(date, pattern, currentLocale);
	}

	public static String getTime(final Date date, final String pattern, final Locale locale) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
		return formatter.format(date);
	}

	public static String getTime(final Date date, final String pattern, final Locale locale, final TimeZone zone) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
		formatter.setTimeZone(zone);
		return formatter.format(date);
	}

	public static String getTimestamp(final String pattern) {
		return getTime(new Date(), pattern, currentLocale);
	}

	public static String getTimestamp(final String pattern, final Locale locale) {
		return getTime(new Date(), pattern, locale);
	}

	public static Date parse(final String date, final String pattern) {
		return parse(date, pattern, currentLocale);
	}

	public static Date parse(final String date, final String pattern, final Locale locale) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date parse(final String date, final String pattern, final Locale locale, final TimeZone zone) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
		formatter.setTimeZone(zone);
		try {
			return formatter.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}
}
