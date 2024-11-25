package cloud.tamacat2.httpd.util;

public class HtmlUtils {

	public static String escapeHtml(final String source) {
		if (source == null || source.length()==0) return source;
		return source.replace("&", "&amp;")
					 .replace("\"", "&quot;")
					 .replace("<", "&lt;")
					 .replace(">", "&gt;")
					 .replace("'", "&#39;");
	}
}
