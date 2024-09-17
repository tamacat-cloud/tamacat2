package cloud.tamacat2.reverse.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.util.HeaderUtils;
import cloud.tamacat2.httpd.util.StringUtils;
import cloud.tamacat2.reverse.config.ReverseConfig;

public class HtmlLinkConvertInterceptor implements HttpResponseInterceptor {

    static final Logger LOG = LoggerFactory.getLogger(HtmlLinkConvertInterceptor.class);

	protected Set<String> contentTypes = new HashSet<>();
	protected List<Pattern> linkPatterns = new ArrayList<>();

	public HtmlLinkConvertInterceptor() {
		contentTypes.add("html");
	}

	/**
	 * Add link convert pattern.
	 * 
	 * @param regex The expression to be compiled.(case insensitive)
	 */
	public void setLinkPattern(final String regex) {
		this.linkPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
	}


	@Override
	public void process(final HttpResponse response, final EntityDetails entity, final HttpContext context)
			throws HttpException, IOException {
		
		if (context == null) {
			throw new IllegalArgumentException("HTTP context may not be null");
		}
		
		final ReverseConfig reverseConfig = (ReverseConfig)context.getAttribute(ReverseConfig.class.getName());
		if (reverseConfig != null) {
			final Header header = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
			if (header != null && HeaderUtils.inContentType(contentTypes, header)) {
				final String before = reverseConfig.getReverse().getPath();
				final String after = reverseConfig.getUrlConfig().getPath();
				LOG.debug(before + "->" + after);
				if (before.equals(after)) {
					//none
					LOG.debug("convert=skip");
				} else if (entity != null && response instanceof ClassicHttpResponse && entity instanceof HttpEntity) {
					LOG.debug("convert=true. response="+response.getClass()+", entity="+entity.getClass());
					response.setHeader(HttpHeaders.TRANSFER_ENCODING, "chunked"); //Transfer-Encoding:chunked
					response.removeHeaders(HttpHeaders.CONTENT_LENGTH);
					((ClassicHttpResponse)response).setEntity(
						new LinkConvertingEntity((HttpEntity)entity, before, after, linkPatterns)
					);
				} else {
					LOG.debug("convert=skip. response="+response.getClass()+", entity="+entity.getClass());
				}
			}
		}
	}

	/**
	 * <p>
	 * Set the content type of the link convertion.<br>
	 * default are "text/html" content types to convert.
	 * </p>
	 * <p>
	 * The {@code contentType} value is case insensitive,<br>
	 * and the white space of before and after is trimmed.
	 * </p>
	 * 
	 * <p>
	 * Examples: {@code contentType="html, css, javascript, xml" }
	 * <ul>
	 * <li>text/html</li>
	 * <li>text/css</li>
	 * <li>text/javascript</li>
	 * <li>application/xml</li>
	 * <li>text/xml</li>
	 * </ul>
	 * 
	 * @param contentType Comma Separated Value of content-type or sub types.
	 */
	public void setContentType(final String contentType) {
		if (StringUtils.isNotEmpty(contentType)) {
			final String[] csv = contentType.split(",");
			for (String t : csv) {
				contentTypes.add(t.trim().toLowerCase());
				final String[] types = t.split(";")[0].split("/");
				if (types.length >= 2) {
					contentTypes.add(types[1].trim().toLowerCase());
				}
			}
		}
	}
}
