/**
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
package com.villemos.ispace.webster;

import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Arrays;
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

import com.villemos.ispace.api.Fields;
import com.villemos.ispace.api.InformationObject;
import com.villemos.ispace.api.ResultSet;
import com.villemos.ispace.api.Suggestion;
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

	protected Pattern pattern = Pattern.compile("<!--INFOLINKS_ON-->(.*?)<!--INFOLINKS_OFF-->");

	protected Pattern spellPattern = Pattern.compile("<ol id=\"franklin_spelling_help\" class=\"franklin-spelling-help\">(.*?)<div class=\"franklin-promo\"><br />");

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

		String uriStr = getWebsterEndpoint().getProtocol() + "://" + getWebsterEndpoint().getDomain() + "/" + getWebsterEndpoint().getPath();
		if (getWebsterEndpoint().getPort() != 80) {
			uriStr += ":" + getWebsterEndpoint().getPort() + "/" + getWebsterEndpoint().getPath();
		}
		String word = (String) exchange.getIn().getHeader(Fields.query);
		uriStr += "/" + word;
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

			ResultSet set = new ResultSet();

			Matcher matcher = pattern.matcher(page);
			if (matcher.find()) {
				String result = matcher.group(1).replaceAll("\\<.*?\\>", "").replaceAll("\\s+", " ");

				/** Create ResultSet*/
				InformationObject io = new InformationObject();
				io.values.put(Fields.hasUri, Arrays.asList((Object) uriStr));
				io.values.put(Fields.fromSource, Arrays.asList((Object) "Webster"));
				io.values.put(Fields.hasTitle, Arrays.asList((Object) ("Webster definition of '" + word + "'.")));
				io.values.put(Fields.ofEntityType, Arrays.asList((Object) "Definition"));
				io.values.put(Fields.ofMimeType, Arrays.asList((Object) "text/html"));
				io.values.put(Fields.withRawText, Arrays.asList((Object) result));
				io.values.put(Fields.score, Arrays.asList((Object) 20));		
				set.informationobjects.add(io);
			}
			else {
				matcher = spellPattern.matcher(page);
				if (matcher.find()) {
					String result = matcher.group(1);
					String[] elements = result.split("<li><a href=.*?>");
					
					for (String element : elements) {
						if (element.trim().equals("") == false) {
							set.suggestions.add(new Suggestion(element.replaceAll("<.*?>", "").trim()));
						}
					}
				}
			}
			exchange.getOut().setBody(set);
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
