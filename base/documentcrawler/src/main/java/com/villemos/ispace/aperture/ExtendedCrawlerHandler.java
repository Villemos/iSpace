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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.URI;
import org.semanticdesktop.aperture.accessor.DataObject;
import org.semanticdesktop.aperture.accessor.FileDataObject;
import org.semanticdesktop.aperture.accessor.RDFContainerFactory;
import org.semanticdesktop.aperture.crawler.Crawler;
import org.semanticdesktop.aperture.crawler.CrawlerHandler;
import org.semanticdesktop.aperture.crawler.ExitCode;
import org.semanticdesktop.aperture.extractor.Extractor;
import org.semanticdesktop.aperture.extractor.ExtractorException;
import org.semanticdesktop.aperture.extractor.ExtractorFactory;
import org.semanticdesktop.aperture.extractor.ExtractorRegistry;
import org.semanticdesktop.aperture.extractor.FileExtractor;
import org.semanticdesktop.aperture.extractor.FileExtractorFactory;
import org.semanticdesktop.aperture.extractor.impl.DefaultExtractorRegistry;
import org.semanticdesktop.aperture.extractor.util.ThreadedExtractorWrapper;
import org.semanticdesktop.aperture.extractor.xmp.XMPExtractorFactory;
import org.semanticdesktop.aperture.mime.identifier.MimeTypeIdentifier;
import org.semanticdesktop.aperture.mime.identifier.magic.MagicMimeTypeIdentifier;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerImpl;
import org.semanticdesktop.aperture.util.IOUtil;
import org.semanticdesktop.aperture.vocabulary.NFO;
import org.semanticdesktop.aperture.vocabulary.NID3;
import org.semanticdesktop.aperture.vocabulary.NIE;
import org.semanticdesktop.aperture.vocabulary.NMO;
import org.semanticdesktop.aperture.subcrawler.SubCrawler;
import org.semanticdesktop.aperture.subcrawler.SubCrawlerException;
import org.semanticdesktop.aperture.subcrawler.SubCrawlerFactory;
import org.semanticdesktop.aperture.subcrawler.SubCrawlerRegistry;
import org.semanticdesktop.aperture.subcrawler.impl.DefaultSubCrawlerRegistry;

import com.villemos.ispace.aperture.processor.IProcessor;
import com.villemos.ispace.api.Fields;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class ExtendedCrawlerHandler implements CrawlerHandler, RDFContainerFactory {

	private static org.apache.log4j.Logger Logger = org.apache.log4j.Logger.getLogger(ExtendedCrawlerHandler.class);

	protected List<Message> messages = null;
	
	protected SubCrawlerRegistry subCrawlerRegistry = new DefaultSubCrawlerRegistry();
	protected ExtractorRegistry extractorRegistry = new DefaultExtractorRegistry();
	protected MimeTypeIdentifier mimeTypeIdentifier = new MagicMimeTypeIdentifier();

	protected XMPExtractorFactory xmpExtractorFactory = new XMPExtractorFactory();

	// Max allowed file size in bytes
	protected long maxSize = 50000000;

	/** List of specific processors. */
	private List<IProcessor> processors = new ArrayList<IProcessor>();
	
	public ExtendedCrawlerHandler(List<Message> messages) {
		this.messages = messages;
	}
	
	public void accessingObject(Crawler crawler, String url) {
		// TODO Auto-generated method stub

	}

	public void clearFinished(Crawler crawler, ExitCode exitCode) {
		// TODO Auto-generated method stub

	}

	public void clearStarted(Crawler crawler) {
		// TODO Auto-generated method stub

	}

	public void clearingObject(Crawler crawler, String url) {
		// TODO Auto-generated method stub

	}

	public void crawlStarted(Crawler crawler) {
		// TODO Auto-generated method stub

	}

	public void crawlStopped(Crawler crawler, ExitCode exitCode) {
		// TODO Auto-generated method stub

	}

	public RDFContainerFactory getRDFContainerFactory(Crawler crawler,
			String url) {
		return this;
	}

	public void objectChanged(Crawler crawler, DataObject object) {
		if (object instanceof FileDataObject) {
			process(crawler, (FileDataObject) object);		
		}

		object.getMetadata().dispose();
		object.dispose();
	}

	public void objectNew(Crawler crawler, DataObject object) {

		String mimetype = process(crawler, (FileDataObject) object);

		String fullText = "";
		Collection fullTexts = object.getMetadata().getAll(NIE.plainTextContent);
		fullTexts.addAll(object.getMetadata().getAll(NMO.plainTextMessageContent));
		fullTexts.addAll(object.getMetadata().getAll(NID3.unsynchronizedTextContent));
		if (!fullTexts.isEmpty()) {
			for (Object fullTextObject : fullTexts) {
				fullText += fullTextObject.toString();
			}
		}

		/** Build a message and set it on the message list returned from the processor. */
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put(Fields.fromSource, "File System");
		headers.put(Fields.ofMimeType, mimetype);
		
		if (((FileDataObject) object).getFile() != null) {
			headers.put(Fields.hasTitle, ((FileDataObject) object).getFile().getName());
		} else {
			headers.put(Fields.hasTitle, ((FileDataObject) object).getID());
		}
		headers.put(Fields.hasUri, ((FileDataObject) object).getID());

		Message message = new DefaultMessage();
		message.setHeaders(headers);
		message.setBody(fullText);
		messages.add(message);

		/** Add all messages that the specific processors may be able to extract from this file. */
		for (IProcessor processor : processors ) {
			messages.addAll(processor.process(message, ((FileDataObject) object).getFile()));
		}		
		
		/** Clean up. */
		object.getMetadata().dispose();
		object.dispose();
	}


	private boolean applyExtractor(URI id, InputStream contentStream, String mimeType, RDFContainer metadata)
	throws ExtractorException, IOException {
		Set extractors = extractorRegistry.getExtractorFactories(mimeType);
		boolean supportedByXmp = xmpExtractorFactory.getSupportedMimeTypes().contains(mimeType);
		boolean result = false;
		byte [] buffer = null;

		if (!extractors.isEmpty() && supportedByXmp) {
			buffer = IOUtil.readBytes(contentStream);
		}

		if (!extractors.isEmpty()) {
			ExtractorFactory factory = (ExtractorFactory) extractors.iterator().next();
			Extractor extractor = factory.get();
			ThreadedExtractorWrapper wrapper = new ThreadedExtractorWrapper(extractor);
			if (buffer != null) {
				contentStream = new BufferedInputStream(new ByteArrayInputStream(buffer));
			}
			try {
				wrapper.extract(id, contentStream, null, mimeType, metadata);
				result = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (supportedByXmp) {
			Extractor extractor = xmpExtractorFactory.get();
			ThreadedExtractorWrapper wrapper = new ThreadedExtractorWrapper(extractor);			if (buffer != null) {
				contentStream = new BufferedInputStream(new ByteArrayInputStream(buffer));
			}
			try {
				wrapper.extract(id, contentStream, null, mimeType, metadata);
				result = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public String process(Crawler crawler, FileDataObject object) {
		String mimetype = null;
		
		if (object.getFile() != null && object.getFile().length() > maxSize) {
			Logger.info("Ignoring file " + object.getFile().getAbsolutePath() + " as it is above the configured maxSize (" + maxSize + "). File size is " + object.getFile().length());
		}		
		else {

			// String mimeType = identifyMimeType(crawler, object);


			try {
				URI id = object.getID();

				// Create a buffer around the object's stream large enough to be able to reset the stream
				// after MIME type identification has taken place. Add some extra to the minimum array
				// length required by the MimeTypeIdentifier for safety.
				int minimumArrayLength = mimeTypeIdentifier.getMinArrayLength();
				// we don't specify our own buffer size anymore, I commented this out (Antoni Mylka)
				//int bufferSize = Math.max(minimumArrayLength, 8192);			

				InputStream contentStream = object.getContent();
				contentStream.mark(minimumArrayLength + 10); // add some for safety

				// apply the MimeTypeIdentifier
				byte[] bytes = IOUtil.readBytes(contentStream, minimumArrayLength);
				mimetype = mimeTypeIdentifier.identify(bytes, object.getMetadata().getString(NFO.fileName), id);
				if (mimetype != null) {
					// add the MIME type to the metadata
					RDFContainer metadata = object.getMetadata();
					metadata.add(NIE.mimeType, mimetype);

					contentStream.reset();

					// apply an Extractor if available
					boolean done = applyExtractor(id, contentStream, mimetype, metadata);
					if (done) {
						return mimetype;
					}

					// else try to apply a FileExtractor
					done = applyFileExtractor(object, id, mimetype, metadata);
					if (done) {
						return mimetype;
					}

					// or maybe apply a SubCrawler
					done = applySubCrawler(id, contentStream, mimetype, object, crawler);
				}	
			}
			catch (Exception e) {
				Logger.error("Caurght exception");
				e.printStackTrace();
			}
		}	        
		
		return mimetype;
	}

	@SuppressWarnings("unchecked")
	private boolean applyFileExtractor(FileDataObject object, URI id, String mimeType, RDFContainer metadata)
	throws ExtractorException, IOException {
		Set fileextractors = extractorRegistry.getFileExtractorFactories(mimeType);
		if (!fileextractors.isEmpty()) {
			FileExtractorFactory factory = (FileExtractorFactory) fileextractors.iterator().next();
			FileExtractor extractor = factory.get();
			File originalFile = object.getFile();
			if (originalFile != null) {
				System.out.print("|fex:" + extractor.getClass().getName());
				extractor.extract(id, originalFile, null, mimeType, metadata);
				return true;
			}
			else {
				File tempFile = object.downloadContent();
				try {
					System.out.print("|fexd:" + extractor.getClass().getName());
					extractor.extract(id, tempFile, null, mimeType, metadata);
					return true;
				}
				finally {
					if (tempFile != null) {
						tempFile.delete();
					}
				}
			}
		}
		else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean applySubCrawler(URI id, InputStream contentStream, String mimeType, DataObject object,
			Crawler crawler) throws SubCrawlerException {
		Set subCrawlers = subCrawlerRegistry.get(mimeType);
		if (!subCrawlers.isEmpty()) {
			SubCrawlerFactory factory = (SubCrawlerFactory) subCrawlers.iterator().next();
			SubCrawler subCrawler = factory.get();
			System.out.print("|sc:" + subCrawler.getClass().getName());
			crawler.runSubCrawler(subCrawler, object, contentStream, null, mimeType);
			return true;
		}
		else {
			return false;
		}
	}

	public void objectNotModified(Crawler crawler, String url) {
		// DO NULL.
	}

	public void objectRemoved(Crawler crawler, String url) {
		// TODO
		// Create delete message to the repository.
	}

	public RDFContainer getRDFContainer(URI uri) {
		Logger.trace("Creating RDF container for Information Element " + uri);
		Model model = RDF2Go.getModelFactory().createModel(uri);
		model.open();

		return new RDFContainerImpl(model, uri);
	}

	public long getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}

	public List<IProcessor> getProcessors() {
		return processors;
	}

	public void setProcessors(List<IProcessor> processors) {
		this.processors = processors;
	}
}
