package com.villemos.ispace.httpcrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.java.browser.net.ProxyInfo;
import com.sun.java.browser.net.ProxyService;


public class HttpCrawlerConsumer extends ScheduledPollConsumer {

	protected HttpClient client = new HttpClient();

	protected boolean ignoreCertificateFailures;

	protected long processed = 0;
	protected long failed = 0;

	protected List<String> crawledPages = new ArrayList<String>();
	protected Set<String> uncrawledPages = new HashSet<String>();
	protected List<String> ignoredPages = new ArrayList<String>();
	protected List<String> failedPages = new ArrayList<String>();

	protected Pattern urlPattern = Pattern.compile("<a href=(\"|\')(.*?)(\'|\")");

	/** Strings defining what a URL must begin with to be within the boundary. */
	protected List<String> boundaries = null;	

	private static final Log LOG = LogFactory.getLog(HttpCrawlerConsumer.class);

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

		/** 
		 * Detect whether we sit behind a proxy. If yes, then get the proxy information
		 * and use it to hop to the target URL.  
		 * 
		 * Warning: This use undocumented code, i.e. may not work on some JVS. */
		String url = getHttpCrawlerEndpoint().getProtocol() + "://" + getHttpCrawlerEndpoint().getDomain() + ":" + getHttpCrawlerEndpoint().getPort() + "/" + getHttpCrawlerEndpoint().getPath();

		String proxyHost = getHttpCrawlerEndpoint().getProxyHost();
		Integer proxyPort = getHttpCrawlerEndpoint().getProxyPort();

		if (proxyHost != null && proxyPort != null) {
			client.getHostConfiguration().setProxy(proxyHost, proxyPort);
		}
		else {
			try {
				ProxyInfo info[] = ProxyService.getProxyInfo(new URL(url));
				if(info != null && info.length>0) {
					proxyHost = info[0].getHost();
					proxyPort = info[0].getPort();

					client.getHostConfiguration().setProxy(proxyHost, proxyPort);
				}
			}
			catch (Exception ex) {
				System.err.println(
				"could not retrieve proxy configuration, attempting direct connection.");
			}
		}
		
		if (getHttpCrawlerEndpoint().getProxyUser() != null && getHttpCrawlerEndpoint().getProxyPassword() != null) {
			client.getState().setProxyCredentials(
					new AuthScope("http://" + proxyHost, proxyPort, AuthScope.ANY_REALM),
					new UsernamePasswordCredentials(getHttpCrawlerEndpoint().getProxyUser(), getHttpCrawlerEndpoint().getProxyPassword()));
		}

		/** The target location may demand authentication. We setup preemptive authentication. */
		if (getHttpCrawlerEndpoint().getAuthenticationUser() != null && getHttpCrawlerEndpoint().getAuthenticationPassword() != null) {
			client.getParams().setAuthenticationPreemptive(true);



			client.getState().setCredentials(
					new AuthScope(getHttpCrawlerEndpoint().getDomain(), getHttpCrawlerEndpoint().getPort(), AuthScope.ANY_REALM),
					new UsernamePasswordCredentials(getHttpCrawlerEndpoint().getAuthenticationUser(), getHttpCrawlerEndpoint().getAuthenticationPassword()));
		}

		/** Always ignore authentication protocol errors. */
		ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory( );
		Protocol easyhttps = new Protocol( "https", socketFactory, 443);
		Protocol.registerProtocol( "https", easyhttps );

		try {
			URI uri = new URI(url, true);

			/** Build request from headers. */
			GetMethod method = new GetMethod(uri.toString());
			method.setDoAuthentication(false);
			int status = client.executeMethod(method);

			String page = readFully(method.getResponseBodyAsStream());

			/** Detect URLs */
			detectUrls(page);

			/** Index this page*/
			submitPage("", page);

			/** Process all URLs */
			while (uncrawledPages.size() > 0) {
				String newUrl = uncrawledPages.iterator().next();
				uncrawledPages.remove(newUrl);

				/** Register this URL as crawled. We do this before we crawl, as no matter whether we succeed or not in
				 * the crawl of the page, we should not crawl the page again. */
				crawledPages.add(newUrl);
				processUrl(newUrl);
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// release any connection resources used by the method
			// authpost.releaseConnection();
		}

		return 0;
	}


	public static String readFully(InputStream input) throws IOException {

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
		StringBuffer result = new StringBuffer();
		char[] buffer = new char[4 * 1024];
		int charsRead;
		while ((charsRead = bufferedReader.read(buffer)) != -1) {
			result.append(buffer, 0, charsRead);
		}
		input.close();
		bufferedReader.close();

		return result.toString();
	}


	public void processUrl(String url) {
		/** Get the page. */
		String page = "";
		int status = 0;
		try {
			GetMethod get = new GetMethod(url);
			get.setDoAuthentication( true );
			status = client.executeMethod(get); 
			page = readFully(get.getResponseBodyAsStream());
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
			if (boundaries != null) {
				for (String boundary : boundaries) {
					if (entryUrl.startsWith(boundary) == true) {
						within = true;
						break;
					}
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
		exchange.getIn().setHeader("solr.field.title", title);
		exchange.getIn().setHeader("solr.field.url", url);

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
