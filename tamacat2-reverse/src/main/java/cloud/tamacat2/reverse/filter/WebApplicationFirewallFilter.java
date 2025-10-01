/*
 * Copyright 2025 tamacat.org
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
package cloud.tamacat2.reverse.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.tamacat2.httpd.error.ForbiddenException;
import cloud.tamacat2.httpd.util.IOUtils;

public class WebApplicationFirewallFilter implements HttpRequestInterceptor {

	final Map<String, List<Pattern>> rules = new HashMap<>();

	@Override
	public void process(final HttpRequest request, final EntityDetails entity, final HttpContext context)
			throws HttpException, IOException {
        if (isMalicious(request)) {
        		context.setAttribute("X-WAF", "Blocked");
            throw new ForbiddenException();
        }
	}

    boolean isMalicious(final HttpRequest request) {
    		final Header[] headers = request.getHeaders();
        for (final Header header : headers) {
        		final String value = header.getValue();
            if (value != null && matchesRule(value)) {
                return true;
            }
        }
        return false;
    }
    
    boolean matchesRule(final String value) {
        for (final List<Pattern> patList : rules.values()) {
            for (final Pattern p : patList) {
                if (p.matcher(value).find()) {
                    return true;
                }
            }
        }
        return false;
    }
	
    void init() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode root = mapper.readTree(IOUtils.getInputStream("waf-rules.json"));
        for (final Iterator<String> it = root.fieldNames(); it.hasNext();) {
           final  String key = it.next();
            final List<Pattern> patList = new ArrayList<>();
            root.get(key).forEach(node -> patList.add(Pattern.compile(node.asText())));
            rules.put(key, patList);
            System.out.println(key+"="+patList);
        }
    }


	public static void main(final String... args) throws Exception {
		new WebApplicationFirewallFilter().init();
	}
}
