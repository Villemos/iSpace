package com.villemos.ispace.aperture;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Headers;

import org.semanticdesktop.aperture.accessor.DataAccessorFactory;
import org.semanticdesktop.aperture.accessor.DataObject;
import org.semanticdesktop.aperture.accessor.RDFContainerFactory;
import org.semanticdesktop.aperture.crawler.ExitCode;
import org.semanticdesktop.aperture.crawler.base.CrawlerBase;
import org.semanticdesktop.aperture.datasource.filesystem.FileSystemDataSource;

import org.semanticdesktop.aperture.accessor.base.FileAccessData;
import org.semanticdesktop.aperture.accessor.impl.DefaultDataAccessorRegistry;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;

/**
 * A Crawler implementation for crawling file system sources modeled by a FileSystemDataSource.
 */
public class ExtendedFileSystemCrawler extends CrawlerBase {

	private static org.apache.log4j.Logger Logger = org.apache.log4j.Logger.getLogger(ExtendedFileSystemCrawler.class);

	protected DataAccessorFactory accessorFactory;

	protected FileSystemDataSource source;

	protected long maxSize = 5000000;

	// protected ExtendedCrawlerHandler handler = new ExtendedCrawlerHandler();
	
	protected ExitCode crawlObjects() {

		DataObject dataObject = null;
		try {
			// create an identifier for the file
			// String url = file.toURI().toString();
			String url = "file:localhost/" + source.getRootFolder();

			// register that we're processing this file
			reportAccessingObject(url);

			// fetch a RDFContainer from the handler (note: is done for every
			RDFContainerFactory containerFactory = getRDFContainerFactory(url);

			// dataObject = getAccessorFactory().get().getDataObject(url, source, null, containerFactory);
			reportNewDataObject(dataObject);
		}
		catch (Exception e) {
			Logger.error("Courght exception while processing object" + e);
			e.printStackTrace();
		} 
		catch (Error r) {
			Logger.error("Courght error while processing object " + r);
			r.printStackTrace();			
		}
		finally {
			if (dataObject != null) {
				dataObject.dispose();
			}
		}			
		
		return ExitCode.COMPLETED;
	}

//	private DataAccessorFactory getAccessorFactory() {
//		Set<?> factories = accessorRegistry.get("file");
//
//		if (factories != null && !factories.isEmpty()) {
//			accessorFactory = (DataAccessorFactory) factories.iterator().next();
//		}
//
//		return accessorFactory;
//	}
}
