/*
 * Copyright (c) 2010 tamacat.org
 * All rights reserved.
 */
package cloud.tamacat2.httpd.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat2.httpd.config.UrlConfig;
import cloud.tamacat2.httpd.error.ForbiddenException;
import cloud.tamacat2.httpd.util.IpAddressMatcher;
import cloud.tamacat2.httpd.util.RequestUtils;
import cloud.tamacat2.httpd.util.StringUtils;

public class ClientIPAccessControlFilter extends HttpFilter {
	
	private List<IpAddressMatcher> allowMatchers = new ArrayList<>();
	private List<IpAddressMatcher> denyMatchers = new ArrayList<>();

	protected UrlConfig urlConfig;

	protected boolean useForwardHeader;
	protected String forwardHeader = "X-Forwarded-For";
	
	public void setUseForwardHeader(final boolean forwardHeader) {
		this.useForwardHeader = forwardHeader;
	}

	public void setForwardHeader(final String forwardHeader) {
		this.forwardHeader = forwardHeader;
	}
	
	@Override
	public void handleRequest(final ClassicHttpRequest request, final HttpContext context) {
		final String client = RequestUtils.getRemoteIPAddress(request, context, useForwardHeader, forwardHeader);
		boolean isAllow = false;
		for (IpAddressMatcher allow : allowMatchers) {
			if ("0.0.0.0/0".equals(allow.getIpAddress()) || allow.matches(client)) {
				isAllow = true;
				break;
			}
		}
		if (isAllow == false) {
			//allows only -> denied all.
			if (denyMatchers.size() == 0) {
				throw new ForbiddenException();
			}
			for (IpAddressMatcher deny : denyMatchers) {
				if ("0.0.0.0/0".equals(deny.getIpAddress()) || deny.matches(client)) {
					throw new ForbiddenException();
				}
			}
		}
	}
	
	public void setAllow(final String address) {
		setPattern(address, true);
	}
	
	public void setDeny(final String address) {
		setPattern(address, false);
	}

	private void setPattern(final String address, final boolean isAllow) {
		String ipAddress = address;
		if (address.indexOf(".*") >= 0 && address.indexOf('/')==-1) {
			final String[] ip = StringUtils.split(address, ".");
			final StringBuilder pattern = new StringBuilder();
			int num = 0;
			for (int i=0; i<4; i++) {
				if (pattern.length() > 0) {
					pattern.append(".");
				}
				if (ip.length > i && "*".equals(ip[i])==false) {
					pattern.append(ip[i]);
				} else {
					pattern.append("0");
					num++;
				}
			}
			if (num >= 1) {
				pattern.append("/"+(32-(8*num)));
			}
			ipAddress = pattern.toString();
		} else if ("*".equals(address)) {
			ipAddress = "0.0.0.0/0";
		}
		final IpAddressMatcher matcher = new IpAddressMatcher(ipAddress);
		if (isAllow) {
			allowMatchers.add(matcher);
		} else {
			denyMatchers.add(matcher);
		}
	}
}
