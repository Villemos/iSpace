package com.villemos.sdms.doccrawler;

import java.io.File;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.semanticdesktop.aperture.accessor.impl.DefaultDataAccessorRegistry;
import org.semanticdesktop.aperture.crawler.CrawlerHandler;
import org.semanticdesktop.aperture.crawler.filesystem.FileSystemCrawler;
import org.semanticdesktop.aperture.datasource.filesystem.FileSystemDataSource;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class DocumentProcessor {

	@Autowired
	protected CrawlerHandler handler = null; 
	
	@Handler
	public void processDocument(@Body File file) {
		
		RDFContainerFactoryImpl rdfFactory = new RDFContainerFactoryImpl();
		RDFContainer configuration = rdfFactory.newInstance("source:testsource");
		
		/** Note that the configuratuion must be set prior to setting the root folder. */
		FileSystemDataSource source = new FileSystemDataSource();
		source.setConfiguration(configuration);
		source.setRootFolder(file.getAbsolutePath());
		

		// create an AccessData facilitating incremental crawling
		// FileAccessData accessData = new FileAccessData();
		// accessData.setDataFile(file);
				
		// create a Crawler
		final FileSystemCrawler crawler = new FileSystemCrawler();
		crawler.setDataSource(source);		
		crawler.setCrawlerHandler(handler);
		crawler.setDataAccessorRegistry(new DefaultDataAccessorRegistry());
		// crawler.setAccessData(accessData);
		crawler.crawl();	
	}
}
