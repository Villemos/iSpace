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
import java.io.InputStreamReader;
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
import org.apache.camel.impl.DefaultEndpoint;
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

import com.villemos.ispace.Fields;


public class HttpCrawlerConsumer extends ScheduledPollConsumer {

	private static final Log LOG = LogFactory.getLog(HttpCrawlerConsumer.class);

	protected DefaultHttpClient client = null;
	protected CookieStore cookieStore = new BasicCookieStore(); 
	
	protected boolean ignoreAuthenticationFailure = true;
	
	protected long processed = 0;
	protected long failed = 0;

	protected List<String> crawledPages = new ArrayList<String>();
	protected Set<String> uncrawledPages = new HashSet<String>();
	protected List<String> ignoredPages = new ArrayList<String>();
	protected List<String> failedPages = new ArrayList<String>();

	protected Pattern urlPattern = Pattern.compile("<a href=(\"|\')(.*?)(\'|\")");

	/** Strings defining what a URL must begin with to be within the boundary. */
	protected List<String> boundaries = new ArrayList<String>();	

	protected HttpHost target = null;
	protected HttpContext localContext = null;

	public HttpCrawlerConsumer(DefaultEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		// TODO Auto-generated constructor stub
	}



	public HttpCrawlerConsumer(Endpoint endpoint, Processor processor,
			ScheduledExecutorService executor) {
		super(endpoint, processor, executor);
		// TODO Auto-generated constructor stub
	}

	protected HttpCrawlerEndpoint getHttpCrawlerEndpoint() {
		return ((HttpCrawlerEndpoint) getEndpoint());
	}


	@Override
	protected int poll() throws Exception {

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

		String proxyHost = getHttpCrawlerEndpoint().getProxyHost();
		Integer proxyPort = getHttpCrawlerEndpoint().getProxyPort();

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
		if (getHttpCrawlerEndpoint().getAuthenticationUser() != null && getHttpCrawlerEndpoint().getAuthenticationPassword() != null) {
			client.getCredentialsProvider().setCredentials(
					new AuthScope(getHttpCrawlerEndpoint().getDomain(), getHttpCrawlerEndpoint().getPort()), 
					new UsernamePasswordCredentials(getHttpCrawlerEndpoint().getAuthenticationUser(), getHttpCrawlerEndpoint().getAuthenticationPassword()));			
		}


		/** Set default cookie policy and store. Can be overridden for a specific method using for example;
		 *    method.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY); 
		 */
		client.setCookieStore(cookieStore);		
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		
		String uriStr = getHttpCrawlerEndpoint().getProtocol() + "://" + getHttpCrawlerEndpoint().getDomain();
		if (getHttpCrawlerEndpoint().getPort() != 80) {
			uriStr += ":" + getHttpCrawlerEndpoint().getPort() + "/" + getHttpCrawlerEndpoint().getPath();
		}		
		URI uri = new URI(uriStr);
		
		if (getHttpCrawlerEndpoint().getPort() != 80) {
			target = new HttpHost(getHttpCrawlerEndpoint().getDomain(), getHttpCrawlerEndpoint().getPort(), getHttpCrawlerEndpoint().getProtocol());
		}
		else {
			target = new HttpHost(getHttpCrawlerEndpoint().getDomain());
		}
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		
		/** Default boundary is the domain. */
		boundaries.add(getHttpCrawlerEndpoint().getProtocol() + "://" + getHttpCrawlerEndpoint().getDomain());
		
		HttpUriRequest method = createInitialRequest(uri);			
		HttpResponse response = client.execute(target, method, localContext);
		
		if (response.getStatusLine().getStatusCode() == 200) {
			processSite(uri, response);
		}
		else if (response.getStatusLine().getStatusCode() == 302) {
			HttpHost target = (HttpHost) localContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			HttpGet get = new HttpGet(target.toURI());
			// HttpGet get = new HttpGet("https://om.eo.esa.int/oem/kt/dashboard.php");
			

			/** Read the response fully, to clear it. */
			HttpEntity entity = response.getEntity();
			HttpClientConfigurer.readFully(entity.getContent());
			
			response = client.execute(target, get, localContext);
			processSite(uri, response);
			System.out.println("Final target: " + target);
		}
		else {
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();
			System.out.println(HttpClientConfigurer.readFully(instream));
		}

		return 0;
	}


	/**
	 * Default method for processing a site, with no special processing. URLs will be detected
	 * and iterativly parsed.
	 * 
	 * Override this method to process a specific site.
	 * 
	 * @param method
	 * @throws IOException
	 */
	protected void processSite(URI uri, HttpResponse response) throws IOException {

		/** read the complete page. */
		String page = HttpClientConfigurer.readFully(response.getEntity().getContent());

		/** Detect URLs */
		detectUrls(page);

		/** Index this page*/
		submitPage(uri.toString(), page);

		/** Process all URLs */
		while (uncrawledPages.size() > 0) {			
			String newUrl = uncrawledPages.iterator().next();
			LOG.info("Crawling url " + newUrl + ". Processed " + crawledPages.size() + "/" + (uncrawledPages.size() + crawledPages.size()) + ".");
			
			uncrawledPages.remove(newUrl);

			/** Register this URL as crawled. We do this before we crawl, as no matter whether we succeed or not in
			 * the crawl of the page, we should not crawl the page again. */
			crawledPages.add(newUrl);
			processUrl(newUrl);
		}
	}



	/**
	 * Method to create the first get call. Per default this simply get the 
	 * front page. However the method can be overridden to provide more advanced
	 * processing, such as submitting an initial form with tokens.
	 * 
	 * @param uri
	 * @return
	 */
	protected HttpUriRequest createInitialRequest(URI uri) {
		return new HttpGet(uri.toString());
	}




	public void processUrl(String url) {
		/** Get the page. */
		String page = "";
		int status = 0;
		try {
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(target, get, localContext);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				page = HttpClientConfigurer.readFully(entity.getContent());
			}
			else {
				System.out.println(HttpClientConfigurer.readFully(entity.getContent()));
			}
		} catch(Exception e) {
			e.printStackTrace();
			failedPages.add(url);
		}

		/** Detect URLs */
		detectUrls(page);

		/** Index this page*/
		submitPage(url, page);		
	}

	protected void detectUrls(String page) {
		Matcher matcher = urlPattern.matcher(page);

		while (matcher.find() == true) {
			String entryUrl = matcher.group(2).trim();

			/** Add the full address if relative address. */
			if (entryUrl.contains("http") == false) {
				if (entryUrl.startsWith("/") == false) {
					entryUrl = "/" + entryUrl;
				}

				if (getHttpCrawlerEndpoint().getPath().equals("") == false) {
					entryUrl = getHttpCrawlerEndpoint().getProtocol() + "://" + getHttpCrawlerEndpoint().getDomain() + "/" + getHttpCrawlerEndpoint().getPath() + entryUrl;
				}
				else {
					entryUrl = getHttpCrawlerEndpoint().getProtocol() + "://" + getHttpCrawlerEndpoint().getDomain() + entryUrl;
				}
			}

			/** Ignore URLs in javascripts*/
			if (entryUrl.contains("javascript") == true) {
				ignoredPages.add(entryUrl);
				continue;
			}

			/** Check it against the boundaries of the crawl. */
			boolean within = false;
				for (String boundary : boundaries) {
					if (entryUrl.startsWith(boundary) == true) {
						within = true;
						break;
					}
				}
			if (within == false) {
				ignoredPages.add(entryUrl);
				continue;
			}

			/** Has it already been crawled? */
			boolean found = false;
			for (String entry : crawledPages) {
				if (entry.equals(entryUrl) == true) {								
					found = true;
					break;
				}
			}

			if (found == false) {
				if (uncrawledPages.contains(entryUrl) == false) {
					uncrawledPages.add(entryUrl);
				}
			}
		}
	} 

	protected void submitPage(String url, String page) {

		// Detect the title
		String title = url;
		Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
		Matcher titleMatcher = titlePattern.matcher(page);
		if (titleMatcher.find() == true) {
			title = titleMatcher.group(1).trim();
		}

		Exchange exchange = getEndpoint().createExchange();

		exchange.getIn().setBody(page);
		exchange.getIn().setHeader(Fields.prefix + Fields.hasTitle, title);
		exchange.getIn().setHeader(Fields.prefix + Fields.hasUri, url);
		exchange.getIn().setHeader(Fields.prefix + Fields.ofMimeType, "text/html");
		
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
