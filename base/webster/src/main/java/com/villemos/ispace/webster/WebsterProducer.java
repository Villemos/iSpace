/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.villemos.ispace.webster;

import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
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
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.villemos.ispace.Fields;
import com.villemos.ispace.httpcrawler.EasyX509TrustManager;
import com.villemos.ispace.httpcrawler.HttpClientConfigurer;

public class WebsterProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(WebsterProducer.class);

	private WebsterEndpoint endpoint;

	protected DefaultHttpClient client = null;
	protected CookieStore cookieStore = new BasicCookieStore(); 
	protected HttpHost target = null;
	protected HttpContext localContext = null;

	protected boolean ignoreAuthenticationFailure = true;
	
	protected Pattern pattern = Pattern.compile("<!--INFOLINKS_ON-->(.?*)<!--INFOLINKS_OFF-->");
	
	public WebsterProducer(WebsterEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
	}

	public void process(Exchange exchange) throws Exception {

		/** Always ignore authentication protocol errors. */
		if (ignoreAuthenticationFailure) {
			SSLContext sslContext = SSLContext.getInstance("SSL");

			// set up a TrustManager that trusts everything
			sslContext.init(null, new TrustManager[] {new EasyX509TrustManager()}, new SecureRandom());

			SchemeRegistry schemeRegistry = new SchemeRegistry();
			
			SSLSocketFactory sf = new SSLSocketFactory(sslContext);
			Scheme httpsScheme = new Scheme("https", sf, 443);
			schemeRegistry.register(httpsScheme);
			
			SocketFactory sfa = new PlainSocketFactory();
			Scheme httpScheme = new Scheme("http", sfa, 80);
			schemeRegistry.register(httpScheme);

			HttpParams params = new BasicHttpParams();
			ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
			
			client = new DefaultHttpClient(cm, params);
		}
		else {
			client = new DefaultHttpClient();
		}

		String proxyHost = getWebsterEndpoint().getProxyHost();
		Integer proxyPort = getWebsterEndpoint().getProxyPort();

		if (proxyHost != null && proxyPort != null) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		else {
			ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
					client.getConnectionManager().getSchemeRegistry(),
					ProxySelector.getDefault());  
			client.setRoutePlanner(routePlanner);
		}


		/** The target location may demand authentication. We setup preemptive authentication. */
		if (getWebsterEndpoint().getAuthenticationUser() != null && getWebsterEndpoint().getAuthenticationPassword() != null) {
			client.getCredentialsProvider().setCredentials(
					new AuthScope(getWebsterEndpoint().getDomain(), getWebsterEndpoint().getPort()), 
					new UsernamePasswordCredentials(getWebsterEndpoint().getAuthenticationUser(), getWebsterEndpoint().getAuthenticationPassword()));			
		}


		/** Set default cookie policy and store. Can be overridden for a specific method using for example;
		 *    method.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY); 
		 */
		client.setCookieStore(cookieStore);		
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		
		String uriStr = getWebsterEndpoint().getProtocol() + "://" + getWebsterEndpoint().getDomain();
		if (getWebsterEndpoint().getPort() != 80) {
			uriStr += ":" + getWebsterEndpoint().getPort() + "/" + getWebsterEndpoint().getPath();
		}
		uriStr += "dictionary/" + exchange.getIn().getHeader(Fields.query);
		URI uri = new URI(uriStr);
		
		if (getWebsterEndpoint().getPort() != 80) {
			target = new HttpHost(getWebsterEndpoint().getDomain(), getWebsterEndpoint().getPort(), getWebsterEndpoint().getProtocol());
		}
		else {
			target = new HttpHost(getWebsterEndpoint().getDomain());
		}
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		
		HttpUriRequest method = new HttpGet(uri);			
		HttpResponse response = client.execute(target, method, localContext);
		
		if (response.getStatusLine().getStatusCode() == 200) {
			/** Extract result. */
			String page = HttpClientConfigurer.readFully(response.getEntity().getContent());
			
			Matcher matcher = pattern.matcher(page);
			if (matcher.find()) {
				String result = matcher.group(1).replaceAll("\\<.*?\\>", "");
				System.out.println("result=" + result);
			}
		}
		else {
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();
			String page = HttpClientConfigurer.readFully(response.getEntity().getContent());
			System.out.println(page);
		}
	}
	
	protected WebsterEndpoint getWebsterEndpoint() {
		return (WebsterEndpoint) endpoint;
	}
}
