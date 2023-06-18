package cloud.tamacat2.httpd.error;

import java.util.Locale;

import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;

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
		return getResponseBody(exception, html);
	}
	
	public String getJson(final HttpStatusException exception) {
		return getResponseBody(exception, json);
	}
	
	protected String getResponseBody(final HttpStatusException exception, final String template) {
		final String statusCode = String.valueOf(exception.getHttpStatus());
		final String error = EnglishReasonPhraseCatalog.INSTANCE.getReason(exception.getHttpStatus(), Locale.US);
		final String message = exception.getMessage();
		return template.replace("${status}", statusCode)
			.replace("${error}", error)
			.replace("${message}", message != null ? message : "");
	}
}