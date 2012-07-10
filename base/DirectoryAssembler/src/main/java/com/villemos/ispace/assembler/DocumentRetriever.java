package com.villemos.ispace.assembler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.villemos.ispace.aperture.DocumentProcessor;
import com.villemos.ispace.aperture.InformationObject;
import com.villemos.ispace.aperture.enricher.MicrosoftPropertyReader;
import com.villemos.ispace.assembler.helper.Buffer;
import com.villemos.ispace.assembler.helper.LanguageDetector;
import com.villemos.ispace.assembler.helper.ReferenceIdBodyEnricher;
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

	protected DirectoryAssemblerEndpoint endpoint = null;

	protected ProducerTemplate fileparser = null;
	
	protected Buffer buffer = null;
	
	protected void initParser() {
		RouteBuilder builder = new RouteBuilder() {
		    public void configure() {
		    	DocumentProcessor extractor = new DocumentProcessor(); 
		    	MicrosoftPropertyReader property = new MicrosoftPropertyReader();
		    	LanguageDetector languageDetector = new LanguageDetector();
		    	ReferenceIdBodyEnricher bodyEnricher = new ReferenceIdBodyEnricher();
		    	buffer = new Buffer();
		    	
		        from("direct:fileparser").split().method(extractor).bean(property).bean(languageDetector).bean(bodyEnricher).bean(buffer);		        
		    }
		};
		
		try {
			getEndpoint().getCamelContext().addRoutes(builder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fileparser = builder.getContext().createProducerTemplate();
	}
	
	public DocumentRetriever(Endpoint endpoint, HttpCrawlerConsumer consumer) {
		super(endpoint, consumer);
		this.endpoint = (DirectoryAssemblerEndpoint) endpoint;
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

		long count = 0;
		Iterator<Entry<String, List<Item>>> it1 = documents.entrySet().iterator();
		while (it1.hasNext()) {
			Entry<String, List<Item>> entry = it1.next();

			if (entry.getKey().equals("Statistics")) {
				continue;
			}

			count += entry.getValue().size();
		}

		String rootFolder = ((DirectoryAssemblerEndpoint)getEndpoint()).getRootFolder() + File.separator;

		long downloaded = 0;

		Iterator<Entry<String, List<Item>>> it = documents.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Item>> entry = it.next();

			if (entry.getKey().equals("Statistics")) {
				continue;
			}

			/** See if the root folder exist. */
			File folder = new File(rootFolder + entry.getKey());
			if (folder.exists()) {

				/** Iterate through all items and see if they exist. */
				for (Item doc : entry.getValue()) {

					File file = new File(rootFolder + entry.getKey() + File.separator + doc.filename);
					if (file.exists() == false) {
						LOG.info(downloaded + "/" + count + ". Retrieving document '" + doc.absoluteFilename + "'.");
						getDocument(rootFolder + entry.getKey(), entry.getKey(), doc);
					}
					else {
						/** See if the file have changed, using the file size. */
						if (file.length() != Long.parseLong(doc.filesize)) {
							LOG.info(downloaded + "/" + count + ". Retrieving document '" + doc.absoluteFilename + "'.");
							getDocument(rootFolder + entry.getKey(), entry.getKey(), doc);
						}
						else {
							LOG.info(downloaded + "/" + count + ". File '" + entry.getKey() + "/" + doc.filename + "' already exist. Has same size.");
							doc.metadata.put("accessibleThrough", new URL("file:." + File.separator + entry.getKey() + File.separator + doc.filename));
						}
					}

					if (getAssemblerEndpoint().isParseBody()) {
						
						if (fileparser == null) {
							initParser();
						}
						
						Exchange exchange = new DefaultExchange(getEndpoint().getCamelContext());
						exchange.getIn().setBody(file);
						fileparser.send("direct:fileparser", exchange);
						
						if (buffer.io != null) {
							doc.metadata.putAll(buffer.io.metadata);
							buffer.clear();
						}
					}
					
					downloaded++;
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

		if (getAssemblerEndpoint().isDownload()) {

			try {
				HttpGet get = new HttpGet("https://om.eo.esa.int/oem/kt/action.php?kt_path_info=ktcore.actions.document.view&fDocumentId=" + doc.id + "&session_id=" + session.results);
				HttpResponse response = client.execute(get);

				/** Create the file. */
				File newFile = new File(downloadFolder + File.separator + doc.filename);

				/** Write to the file. */
				writeFile(response.getEntity().getContent(), newFile);

				doc.metadata.put("accessibleThrough", new URL("file:." + File.separator + parentFolder + File.separator + doc.filename));			
			}
			catch (Exception e) {
				e.printStackTrace();
			}
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

	protected DirectoryAssemblerEndpoint getAssemblerEndpoint() {
		return endpoint;
	}
}
