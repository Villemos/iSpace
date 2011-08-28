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

import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a direct endpoint that synchronously invokes the consumers of the
 * endpoint when a producer sends a message to it.
 * 
 * @version 
 */
public class HttpCrawlerEndpoint extends ScheduledPollEndpoint {

	private static final Log LOG = LogFactory.getLog(HttpCrawlerEndpoint.class);

	boolean allowMultipleConsumers = true;

	protected String url = "";

	protected String sourceName = "Http Site";
	
	/** Must be set if a proxy is in use. */
	protected String proxyHost = null;
	protected Integer proxyPort = null;
	protected String proxyUser = null;
	protected String proxyPassword = null;

	/** Must be set of the site being accessed has restrictions. */
	protected String authenticationUser;
	protected String authenticationPassword;

	protected String protocol = "http";
	protected String domain;
	protected String path = "";
	protected int port = 80;

	protected String consumerClassName = "com.villemos.ispace.httpcrawler.HttpCrawlerConsumer";

	public HttpCrawlerEndpoint(String uri, HttpCrawlerComponent component) {
		super(uri, component);
	}

	public Producer createProducer() throws Exception {
		throw new UnsupportedOperationException("Producer not supported for HttpCrawler endpoint. Sorry!");
	}

	public Consumer createConsumer(Processor processor) throws Exception {

		/** Get the class specification of the class to use. May be a specialized class. */
		Class cls = Class.forName(consumerClassName);
		
		/** Find the appropiate constructor. */
		Class partypes[] = new Class[2];
		partypes[0] = DefaultEndpoint.class;
		partypes[1] = Processor.class;		
		Constructor ct = cls.getConstructor(partypes);
		
		/** Build the argument list and invoke the constructor. */
		Object arglist[] = new Object[2];
		arglist[0] = this;
		arglist[1] = processor;
		Consumer consumer = (Consumer) ct.newInstance(arglist);
		
		/** Configure the new consumer. */
		configureConsumer(consumer);        	

		return consumer;
	}

	public boolean isAllowMultipleConsumers() {
		return allowMultipleConsumers;
	}

	public void setAllowMultipleConsumers(boolean allowMutlipleConsumers) {
		this.allowMultipleConsumers = allowMutlipleConsumers;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;

		Pattern pattern = Pattern.compile("\\b(https?|ftp|file)*(://)*([-a-zA-Z0-9+&@#%?=~_|!:,.;]*[-a-zA-Z0-9+&@#%=~_|]+)(/(.+))*");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			if (matcher.group(1) != null) {
				protocol = matcher.group(1); 
			}
			if (matcher.group(3) != null) {
				domain = matcher.group(3); 
			}
			if (matcher.group(4) != null) {
				path = matcher.group(4); 
			}

		}
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public String getAuthenticationUser() {
		return authenticationUser;
	}

	public void setAuthenticationUser(String authenticationUser) {
		this.authenticationUser = authenticationUser;
	}

	public String getAuthenticationPassword() {
		return authenticationPassword;
	}

	public void setAuthenticationPassword(String authenticationPassword) {
		this.authenticationPassword = authenticationPassword;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String siteProtocol) {
		this.protocol = siteProtocol;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String siteServer) {
		this.domain = siteServer;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String siteServerpath) {
		this.path = siteServerpath;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int sitePort) {
		this.port = sitePort;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getConsumerClassName() {
		return consumerClassName;
	}

	public void setConsumerClassName(String consumerClassName) {
		this.consumerClassName = consumerClassName;
	}

	public String getSourceName() {
		return sourceName;
	}
	
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	
}
