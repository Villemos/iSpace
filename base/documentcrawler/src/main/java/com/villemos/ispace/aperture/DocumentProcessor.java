package com.villemos.ispace.aperture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Message;
import org.semanticdesktop.aperture.accessor.impl.DefaultDataAccessorRegistry;
import org.semanticdesktop.aperture.crawler.CrawlerHandler;
import org.semanticdesktop.aperture.crawler.filesystem.FileSystemCrawler;
import org.semanticdesktop.aperture.datasource.filesystem.FileSystemDataSource;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class DocumentProcessor {

	protected List<Message> messages = new ArrayList<Message>();

	protected CrawlerHandler handler = new ExtendedCrawlerHandler(messages);
	
	@Handler
	public synchronized List<Message> processDocument(@Body File file) {
		
		/** Make sure we have no messages left from previous run. */
		messages.clear();
		
		RDFContainerFactoryImpl rdfFactory = new RDFContainerFactoryImpl();
		RDFContainer configuration = rdfFactory.newInstance("source:testsource");
		
		/** Note that the configuration must be set prior to setting the root folder. */
		FileSystemDataSource source = new FileSystemDataSource();
		source.setConfiguration(configuration);
		source.setRootFolder(file.getAbsolutePath());
		
		/** create a Crawler */
		final FileSystemCrawler crawler = new FileSystemCrawler();
		crawler.setDataSource(source);		
		crawler.setCrawlerHandler(handler);
		crawler.setDataAccessorRegistry(new DefaultDataAccessorRegistry());
		crawler.crawl();
		
		/** Return the messages. The route must contain a splitter to process
		 * each document. */
		return messages;
	}
	
	public void addMessage(Message message) {
		messages.add(message);
	}
}
