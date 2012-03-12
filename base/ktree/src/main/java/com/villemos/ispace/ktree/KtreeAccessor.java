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

package com.villemos.ispace.ktree;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.thoughtworks.xstream.XStream;
import com.villemos.ispace.httpcrawler.HttpAccessor;
import com.villemos.ispace.httpcrawler.HttpClientConfigurer;
import com.villemos.ispace.httpcrawler.HttpCrawlerConsumer;
import com.villemos.ispace.ktree.folder.Folder;
import com.villemos.ispace.ktree.folder.Item;
import com.villemos.ispace.ktree.folder.Items;
import com.villemos.ispace.ktree.folder.Result;
import com.villemos.ispace.ktree.metadata.MetadataField;
import com.villemos.ispace.ktree.metadata.MetadataItem;
import com.villemos.ispace.ktree.session.Session;

/**
 * Specialized crawler for crawling the ktree website.
 * 
 * The crawler will access a initial folder, thereafter iterate through all found folders
 * collecting folder and document information as it goes. 
 * 
 * The end result is a Map, containing a set of lists describing the found data.
 * 
 */
public class KtreeAccessor extends HttpAccessor {

	private static final Log Logger = LogFactory.getLog(KtreeAccessor.class);

	/** Collection of folders crawled / to be crawled. Will dynamically change as the 
	 * crawler runs. */
	protected Map<String, String> uncrawledFolders = new HashMap<String, String>();
	protected List<String> crawledFolders = new ArrayList<String>();

	/** The data to be forwarded in the route. The documents is a list of all
	 * documents found. The components is a list of all folders found ending with
	 * 'Components'. Releases is a list of all folers found ending with 'Release [ID]'. */
	protected List<Item> documents = new ArrayList<Item>();	

	/** Statistics*/
	protected long entriesProcessed = 0;

	//protected KtreeCrawlerEndpoint endpoint = null;

	protected XStream xstream = new XStream();
	{
		xstream.processAnnotations(Session.class);

		xstream.processAnnotations(Folder.class);
		xstream.processAnnotations(Result.class);
		xstream.processAnnotations(Items.class);
		xstream.processAnnotations(Item.class);

		xstream.processAnnotations(MetadataField.class);
		xstream.processAnnotations(MetadataItem.class);
	}
	protected Session session = null;

	protected String baseRestUrl = "";

	protected CamelContext camelContext = null;

	public KtreeAccessor(Endpoint endpoint, Object object, CamelContext camelContext) {
		super(endpoint);
		this.camelContext = camelContext;
	}

	public KtreeAccessor(Endpoint endpoint, HttpCrawlerConsumer consumer) {
		super(endpoint, consumer);
	}

	public void doPoll() throws Exception {
		sendStatusMessage("Starting crawl of Ktree.");

		/** Initialize. */
		if (((KtreeCrawlerEndpoint)endpoint).isAppendMode() == false) {
			uncrawledFolders = new HashMap<String, String>();
			crawledFolders = new ArrayList<String>();
			documents = new ArrayList<Item>();	
			entriesProcessed = 0;
		}

		this.poll();
		sendStatusMessage("Crawl of Ktree completed.");
	}


	/** Setup the initial login form request. This method is called by
	 * the main method in the iSpace http crawler, to create the form
	 * login. The framework will thereafter call 'processSite'. 
	 * 
	 * The authentication is managed by the iSpace framework.
	 *  
	 */
	@Override
	protected HttpUriRequest createInitialRequest(URI uri) {
		baseRestUrl = uri + "/ktwebservice/KTWebService.php?";
		sendStatusMessage("Accessing Ktree site '" + uri + "'.");
		return new HttpGet(baseRestUrl + "method=login&password=" + ((KtreeCrawlerEndpoint) endpoint).getPassword() + "&username=" + ((KtreeCrawlerEndpoint) endpoint).getUser());
	}

	/** Method called by the iSpace httpcrawler when the initial request has
	 * been performed and succeeded. The page corresponds to the first
	 * page AFTER the login. */
	@Override
	protected void processSite(URI uri, HttpResponse response) throws IOException {

		/** read the complete page. */
		String page = HttpClientConfigurer.readFully(response.getEntity().getContent());

		/** Iteratively crawl the site. */
		processPage(page);

	}

	public Map<String, List> getResults() {
		Map<String, List> results = new HashMap<String, List>();

		results.put("documents", documents);

		return results;
	}

	/**
	 * Method to iteratively crawl the ktree site.
	 * 
	 * The method will first go to the root folder, then in a loop
	 * collect and process all founds subfolders (URLs).
	 * 
	 * @param firstPage
	 */
	protected void processPage(String firstPage) {

		/** Get the session ID. */
		session = new Session();
		xstream.fromXML(firstPage, session);

		/** Entry is the dashboard. Get the relevant folder. */
		try {			

			/** Add root folders to the list of uncrawled folders. */
			for (String folder : ((KtreeCrawlerEndpoint) getEndpoint()).getInitialFolders()) {
				uncrawledFolders.put(folder, "/");
			}

			/** Iteratively process all URLs */
			while (uncrawledFolders.size() > 0 && (documents.size() < ((KtreeCrawlerEndpoint)getEndpoint()).getMaxNumberOfDocuments() || ((KtreeCrawlerEndpoint)getEndpoint()).getMaxNumberOfDocuments() == -1)) {
				Iterator<Entry<String, String>> it = uncrawledFolders.entrySet().iterator();
				Entry<String, String> entry = it.next();

				String folderId = entry.getKey();
				String path = entry.getValue();
				it.remove();

				/** Register this URL as crawled. We do this before we crawl, as no matter whether we succeed or not in
				 * the crawl of the page, we should not crawl the page again. */
				crawledFolders.add(folderId);
				processFolder(folderId, path);
				sendStatusMessage("Folders crawled: " + crawledFolders.size() + ", Folders pending: " + uncrawledFolders.size() + ", Found documents: " + processed);
				Logger.info("Folders crawled: " + crawledFolders.size() + ", Folders pending: " + uncrawledFolders.size() + ", Processed documents: " + processed + ". Processing folder ID '" + folderId + "'.");
				entriesProcessed++;
			}

			Logger.info("Crawled '" + crawledFolders.size() + "' folders.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Process the page of a folder. 
	 * 
	 * @param url
	 * @param path
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void processFolder(String folderId, String path) throws ClientProtocolException, IOException {
		/** Get the page. */
		HttpGet get = new HttpGet(baseRestUrl + "method=get_folder_contents&session_id=" + session.results + "&folder_id=" + folderId);

		HttpResponse response = client.execute(get);
		String page = HttpClientConfigurer.readFully(response.getEntity().getContent());

		/** Detect URLs */
		detectUrls(folderId, page, path);
	}


	protected void detectUrls(String url, String page, String path) {

		/** Read the folder. */
		Folder folder = new Folder();
		xstream.fromXML(page.replaceAll("&", "&amp;"), folder);


		String ignorePattern = ((KtreeCrawlerEndpoint)getEndpoint()).getIgnorePattern();

		/** Iterate through the items and find all folders. */
		if (folder.results != null && folder.results.items != null && folder.results.items.items != null) {
			for (Item item : folder.results.items.items) {

				if (folder.results.full_path.equals("/")) {
					item.absoluteFilename = folder.results.full_path + item.filename;
				}
				else {
					item.absoluteFilename = "/" + folder.results.full_path + "/" + item.filename;
				}

				/** If this is a folder... */
				if (item.item_type.equals("F")) {

					/** Check if we should ignore the folder. */
					if (ignorePattern != null) {
						if (item.absoluteFilename.matches(ignorePattern) == true) {
							Logger.info("Ignoring folder '" + item.absoluteFilename);
							continue;
						}
					}

					if (crawledFolders.contains(item.id) == false) {
						uncrawledFolders.put(item.id, item.absoluteFilename + "/");
					}
					else {
						Logger.warn("Duplicate folder ID '" + path + "/" + item.id + "'.");
					}
				}
				else {

					/** Get the metadata for this entry. */
					MetadataItem metadata = new MetadataItem();
					try {
						HttpGet get = new HttpGet(baseRestUrl + "method=get_document_metadata&session_id=" + session.results + "&document_id=" + item.id);
						HttpResponse response = client.execute(get);
						String page2 = HttpClientConfigurer.readFully(response.getEntity().getContent());

						/** TODO Do this properly. XStream cant handle the usage of 'item' two
						 * places in the response XML schema. Therefore we have to manually extract one. */
						int startIndex = page2.indexOf("<fields>");
						int endIndex = page2.indexOf("</fields>");
						if (startIndex != -1) {
							String substring = "<response>" + page2.substring(startIndex, endIndex + 9) + "</response>";
							xstream.fromXML(substring.replaceAll("&", "&amp;"), metadata);
						}
						else {
							Logger.warn("Failed to retrieve metadata for document '" + item.id + "'.");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					/** Set additional item information. */
					item.inPath = folder.results.full_path;
					item.inParentFolderId = folder.results.folder_id;
					item.inParentFolder = folder.results.folder_name;

					/** Set the metadata. */
					item.metadata = new HashMap<String, Object>();
					for (MetadataField field : metadata.fields) {
						item.metadata.put(field.name, field.value);
					}

					/** Add the document item to the documents found. */
					documents.add(item);
					sendStatusMessage("Added document '" + item.absoluteFilename + "'.");
					Logger.info("Added document '" + item.absoluteFilename + "'.");
					processed++;
				}
			}
		}
	}

	protected void sendStatusMessage(String message) {

		if (camelContext != null && ((KtreeCrawlerEndpoint)endpoint).isSendStatus() == true) {
			Exchange exchange = new DefaultExchange(camelContext);
			exchange.getIn().setBody(message);
			camelContext.createProducerTemplate().send("direct:status", exchange);
		}
	}
}
