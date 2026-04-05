package cloud.tamacat2.httpd.error;

import java.util.Locale;

import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;

import cloud.tamacat2.httpd.util.HtmlUtils;

public class ErrorPageTemplate {

	String html = """
			<!DOCTYPE html>
			<html>
			<head>
			  <meta charset=\"UTF-8\" />
			  <title>${status} ${error}</title>
			</head>
			<body>
			  <h1>${status} ${error}</h1>
			  <p>${message}</p>
			  <style>
			    * {margin: 0;padding: 0;}
			    html {background:#fff;font-size:1em;}
			    body {font-family:Ubuntu,'Lucida Grande','Meiryo UI',sans-serif;font-size:12px;padding:1em;background:#fff;}
			    h1 {font-size:1.2em;}
			    p {font-size:1em;margin-top:4px;}
			  </style>
			</body>
			</html>
			""";
	
	String json = """
			{
			  "status":${status},
			  "error":"${error}",
			  "message":"${message}"
			}
			""";
	
	public static ErrorPageTemplate create() {
		return new ErrorPageTemplate();
	}
	
	public ErrorPageTemplate html(final String html) {
		this.html = html;
		return this;
	}
	
	public ErrorPageTemplate json(final String json) {
		this.json = json;
		return this;
	}
	
	public String getHtml(final HttpStatusException exception) {
		final String statusCode = String.valueOf(exception.getHttpStatus());
		final String error = EnglishReasonPhraseCatalog.INSTANCE.getReason(exception.getHttpStatus(), Locale.US);
		final String message = exception.getMessage();
		return html.replace("${status}", statusCode)
			.replace("${error}", HtmlUtils.escapeHtml(error != null ? error : ""))
			.replace("${message}", HtmlUtils.escapeHtml(message != null ? message : ""));
	}

	public String getJson(final HttpStatusException exception) {
		final String statusCode = String.valueOf(exception.getHttpStatus());
		final String error = EnglishReasonPhraseCatalog.INSTANCE.getReason(exception.getHttpStatus(), Locale.US);
		final String message = exception.getMessage();
		return json.replace("${status}", statusCode)
			.replace("${error}", escapeJson(error != null ? error : ""))
			.replace("${message}", escapeJson(message != null ? message : ""));
	}

	private static String escapeJson(final String s) {
		return s.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r", "\\r")
				.replace("\n", "\\n")
				.replace("\t", "\\t");
	}
}