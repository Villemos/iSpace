package com.villemos.ispace.directoryassembler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import com.villemos.ispace.httpcrawler.HttpClientConfigurer;
import com.villemos.ispace.httpcrawler.HttpCrawlerConsumer;
import com.villemos.ispace.ktree.KtreeAccessor;
import com.villemos.ispace.ktree.folder.Item;
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
public class DocumentRetriever extends KtreeAccessor {

	private static final Log LOG = LogFactory.getLog(DocumentRetriever.class);

	protected Exchange exchange = null;

	public DocumentRetriever(Endpoint endpoint, HttpCrawlerConsumer consumer) {
		super(endpoint, consumer);
	}	

	public void doPoll(Exchange exchange) throws Exception {
		this.exchange = exchange;
		this.poll();
	}

	/** Method called by the iSpace httpcrawler when the initial request has
	 * been performed and succeeded. The page corresponds to the first
	 * page AFTER the login. */
	@Override
	protected void processSite(URI uri, HttpResponse response) throws IOException {

		String page = HttpClientConfigurer.readFully(response.getEntity().getContent());

		session = new Session();
		xstream.fromXML(page, session);
		
		/** Check whether files already exist. */
		Map<String, List<Item>> documents = (Map<String, List<Item>>) exchange.getIn().getBody();

		String rootFolder = ((DocumentAssemblerEndpoint)getEndpoint()).getRootFolder() + File.separator;

		long test = 0;
		
		
		Iterator<Entry<String, List<Item>>> it = documents.entrySet().iterator();
		while (it.hasNext() && test < 10) {
			Entry<String, List<Item>> entry = it.next();

			if (entry.getKey().equals("Statistics")) {
				continue;
			}
			
			/** See if the root folder exist. */
			File folder = new File(rootFolder + entry.getKey());
			if (folder.exists()) {

				/** Iterate through all items and see if they exist. */
				for (Item doc : entry.getValue()) {
					test++;
					
					File file = new File(rootFolder + entry.getKey() + File.separator + doc.filename);
					if (file.exists() == false) {
						getDocument(rootFolder + entry.getKey(), entry.getKey(), doc);
					}
					else {
						/** See if the file haev changed, using the file size. */
						if (file.length() != Long.parseLong(doc.filesize)) {
							getDocument(rootFolder + entry.getKey(), entry.getKey(), doc);
						}
						else {
							LOG.info("File '" + entry.getKey() + "/" + doc.filename + "' already exist. Has same size.");
							doc.metadata.put("accessibleThrough", new URL("file:." + File.separator + entry.getKey() + File.separator + doc.filename));
						}
					}
				}
			}
			else {
				/** Create folder. */
				File newDir = new File(rootFolder + entry.getKey());
				newDir.mkdirs();

				/** Iterate through the files and get each. */
				for (Item doc : entry.getValue()) {
					getDocument(rootFolder + entry.getKey(), entry.getKey(), doc);
				}
			}
		}
	}

	protected void getDocument(String downloadFolder, String parentFolder, Item doc) {

		try {
			LOG.info("Retrieveing document " + doc.absoluteFilename);
			HttpGet get = new HttpGet("https://om.eo.esa.int/oem/kt/action.php?kt_path_info=ktcore.actions.document.view&fDocumentId=" + doc.id + "&session_id=" + session.results);
			HttpResponse response = client.execute(get);

			/** Create the file. */
			File newFile = new File(downloadFolder + File.separator + doc.filename);

			/** Write to the file. */
			writeFile(response.getEntity().getContent(), newFile);

			doc.metadata.put("accessibleThrough", "file:." + File.separator + parentFolder + File.separator + doc.filename);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void writeFile(InputStream input, File file) throws IOException {

		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		byte[] buffer = new byte[4 * 1024];
		int charsRead;
		while ((charsRead = input.read(buffer)) != -1) {
			out.write(buffer, 0, charsRead);
		}
		input.close();
		out.close();
	}

	protected Field findUriField(Object object) {
		for (Field field : object.getClass().getFields()) {
			if (field.getType() == URL.class) {
				return field;
			}
		}

		return null;
	}

	protected Field findFilenameField(Object object) {
		for (Field field : object.getClass().getFields()) {
			if (field.getType() == String.class) {
				return field;
			}
		}

		return null;
	}
}
