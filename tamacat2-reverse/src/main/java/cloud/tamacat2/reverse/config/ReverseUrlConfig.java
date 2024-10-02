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
package cloud.tamacat2.reverse.config;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.hc.core5.http.impl.Http1StreamListener;

import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.reverse.listener.TraceHttp1StreamListener;

public class ReverseUrlConfig extends UrlConfig {

	protected ReverseConfig reverse = new ReverseConfig();
	protected Collection<ReverseConfig> reverses = new ArrayList<>();
	protected Http1StreamListener streamListener = new TraceHttp1StreamListener();
	
	public static ReverseUrlConfig create() {
		return new ReverseUrlConfig();
	}
	
	public ReverseConfig getReverse() {
		if (reverse.getUrlConfig() == null) {
			reverse.setUrlConfig(this);
		}
		return reverse;
	}
	
	public ReverseUrlConfig reverse(final ReverseConfig reverse) {
		if (reverse != null) {
			this.reverse = reverse;
		}
		return this;
	}
	
	@Override
	public ReverseUrlConfig path(final String path) {
		this.path = path;
		return this;
	}
	
    public ReverseUrlConfig streamListener(final Http1StreamListener streamListener) {
        this.streamListener = streamListener;
        return this;
    }
    
    public Http1StreamListener getStreamListener() {
        return streamListener;
    }
}
