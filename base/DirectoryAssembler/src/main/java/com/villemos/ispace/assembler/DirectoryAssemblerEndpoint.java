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
package com.villemos.ispace.assembler;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.ktree.KtreeCrawlerEndpoint;

/**
 * Represents a direct endpoint that synchronously invokes the consumers of the
 * endpoint when a producer sends a message to it.
 * 
 * @version 
 */
public class DirectoryAssemblerEndpoint extends KtreeCrawlerEndpoint {

	private static final Log LOG = LogFactory.getLog(DirectoryAssemblerEndpoint.class);

	protected String rootFolder = "/";
	
	/** Flag defining whether the component should download documents. */
	protected boolean download = true;
	
	protected boolean parseBody = true;
	
	public DirectoryAssemblerEndpoint(String uri, DirectoryAssemblerComponent component) {
		super(uri, component);
	}

	public Producer createProducer() throws Exception {
		return new DirectoryAssemblerProducer(this);
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		throw new Exception();
	}

	public String getRootFolder() {
		return rootFolder;
	}

	public void setRootFolder(String rootFolder) {
		this.rootFolder = rootFolder;
	}

	public boolean isDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	public boolean isParseBody() {
		return parseBody;
	}

	public void setParseBody(boolean parseBody) {
		this.parseBody = parseBody;
	}
	
	
}
