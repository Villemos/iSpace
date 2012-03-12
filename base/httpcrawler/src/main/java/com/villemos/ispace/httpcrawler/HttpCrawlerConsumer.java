/**
 * villemos solutions [space^] (http://www.villemos.com) 
 * Probe. Send. Act. Emergent solution. 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * 
 * Released under the Apache license, version 2.0 (do what ever
 * you want, just dont claim ownership).
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of villemos solutions, and its suppliers
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to villemos solutions
 * and its suppliers and may be covered by European and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from villemos solutions.
 * 
 * And it wouldn't be nice either.
 * 
 */
package com.villemos.ispace.httpcrawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import com.villemos.ispace.api.InformationObject;


public class HttpCrawlerConsumer extends ScheduledPollConsumer {

	private static final Log LOG = LogFactory.getLog(HttpCrawlerConsumer.class);

	protected HttpAccessor accessor = null;
	
	public HttpCrawlerConsumer(Endpoint endpoint, Processor processor) {
		super(endpoint, processor);
		accessor = new HttpAccessor(endpoint, this);	
	}

	public HttpCrawlerConsumer(Endpoint endpoint, Processor processor, ScheduledExecutorService executor) {
		super(endpoint, processor, executor);
		accessor = new HttpAccessor(endpoint, this);
	}

	protected HttpCrawlerEndpoint getHttpCrawlerEndpoint() {
		return ((HttpCrawlerEndpoint) getEndpoint());
	}


	@Override
	protected int poll() throws Exception {
		return accessor.poll();
	}
	
	public void submitPage(String url, String page) {

		// Detect the title
		String title = url;
		Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
		Matcher titleMatcher = titlePattern.matcher(page);
		if (titleMatcher.find() == true) {
			title = titleMatcher.group(1).trim();
		}
		
		InformationObject io = new InformationObject(url, title, "text/html", getHttpCrawlerEndpoint().getSourceName(), page); 
		Exchange exchange = new DefaultExchange(getEndpoint().getCamelContext());
		exchange.getIn().setBody(io);

		getAsyncProcessor().process(exchange, new AsyncCallback() {
			public void done(boolean doneSync) {
				LOG.trace("Done processing URL");
			}
		});
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();	
	}
}
