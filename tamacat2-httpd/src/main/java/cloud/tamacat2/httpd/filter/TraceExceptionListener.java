/*
 * Copyright 2019 tamacat.org
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
package cloud.tamacat2.httpd.filter;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.tamacat2.httpd.util.ExceptionUtils;

public class TraceExceptionListener implements ExceptionListener {
	
	static final Logger LOG = LoggerFactory.getLogger(TraceExceptionListener.class);
	
	@Override
    public void onError(final Exception ex) {
        if (ex instanceof SocketException) {
            LOG.debug("[client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
        } else {
        	LOG.debug("[client->proxy] " + Thread.currentThread()  + " " + ex.getMessage());
        	LOG.debug(ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public void onError(final HttpConnection connection, final Exception ex) {
        if (ex instanceof SocketTimeoutException) {
        	LOG.debug("[client->proxy] " + Thread.currentThread() + " time out");
        } else if (ex instanceof SocketException || ex instanceof ConnectionClosedException) {
        	LOG.debug("[client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
        } else {
        	LOG.debug("[client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
            LOG.debug(ExceptionUtils.getStackTrace(ex));
        }
    }

}
