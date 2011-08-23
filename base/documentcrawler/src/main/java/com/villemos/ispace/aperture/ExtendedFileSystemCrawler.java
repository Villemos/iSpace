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
package com.villemos.ispace.aperture;

import org.semanticdesktop.aperture.accessor.DataAccessorFactory;
import org.semanticdesktop.aperture.accessor.DataObject;
import org.semanticdesktop.aperture.accessor.RDFContainerFactory;
import org.semanticdesktop.aperture.crawler.ExitCode;
import org.semanticdesktop.aperture.crawler.base.CrawlerBase;
import org.semanticdesktop.aperture.datasource.filesystem.FileSystemDataSource;

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
}
