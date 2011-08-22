package com.villemos.ispace.httpcrawler;

import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class HttpClientConfigurer {

	public static HttpClient setupClient(boolean ignoreAuthenticationFailure, String domain, Integer port, String proxyHost, Integer proxyPort, String authUser, String authPassword, CookieStore cookieStore) throws NoSuchAlgorithmException, KeyManagementException {
		
		DefaultHttpClient client = null;
		
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
		if (authUser != null && authPassword != null) {
			client.getCredentialsProvider().setCredentials(
					new AuthScope(domain, port), 
					new UsernamePasswordCredentials(authUser, authPassword));			
		}


		/** Set default cookie policy and store. Can be overridden for a specific method using for example;
		 *    method.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY); 
		 */
		client.setCookieStore(cookieStore);
		// client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2965);		
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);

		return client;
	}
}