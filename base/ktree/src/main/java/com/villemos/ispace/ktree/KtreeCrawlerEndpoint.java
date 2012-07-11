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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.httpcrawler.HttpCrawlerEndpoint;

/**
 * Represents a direct endpoint that synchronously invokes the consumers of the
 * endpoint when a producer sends a message to it.
 * 
 * @version 
 */
public class KtreeCrawlerEndpoint extends HttpCrawlerEndpoint {

	private static final Log LOG = LogFactory.getLog(KtreeCrawlerEndpoint.class);

	boolean allowMultipleConsumers = true;

	protected int maxNumberOfDocuments = -1;
	protected String initialFolder = "1";
	protected String initialFolders = "1";
	
	protected String ignorePattern = null;
	
	protected String user = "";
	protected String password = "";
	
	protected boolean appendMode = false;
	
	protected boolean sendStatus = false;
	
	/** Flag defining whether the documents shall be extracted. */
	protected boolean documents = true;
	protected String documentTabName = "documents";
	
	/** Flag defining whether the folders shall be extracted. */
	protected boolean folders = true;
	protected String folderTabName = "folders";
	
	public KtreeCrawlerEndpoint(String uri, KtreeCrawlerComponent component) {
		super(uri, component);
	}

	public Producer createProducer() throws Exception {
		return new KtreeCrawlerProducer(this, getCamelContext());
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		return new KtreeCrawlerConsumer(this, processor);
	}

	public boolean isAllowMultipleConsumers() {
		return allowMultipleConsumers;
	}

	public void setAllowMultipleConsumers(boolean allowMutlipleConsumers) {
		this.allowMultipleConsumers = allowMutlipleConsumers;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;

		Pattern pattern = Pattern.compile("\\b(https?|ftp|file)*(://)*([-a-zA-Z0-9+&@#%?=~_|!:,.;]*[-a-zA-Z0-9+&@#%=~_|]+)(/(.+))*");
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			if (matcher.group(1) != null) {
				protocol = matcher.group(1); 
			}
			if (matcher.group(3) != null) {
				domain = matcher.group(3); 
			}
			if (matcher.group(4) != null) {
				path = matcher.group(4); 
			}

		}
	}

	public String getInitialFolder() {
		return initialFolder;
	}

	public void setInitialFolder(String initialFolder) {
		this.initialFolders = initialFolder;
	}

	public String getIgnorePattern() {
		return ignorePattern;
	}

	public void setIgnorePattern(String ignorePattern) {
		this.ignorePattern = ignorePattern;
	}

	public int getMaxNumberOfDocuments() {
		return maxNumberOfDocuments;
	}

	public void setMaxNumberOfDocuments(int maxNumberOfDocuments) {
		this.maxNumberOfDocuments = maxNumberOfDocuments;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDocumentTabName() {
		return documentTabName ;
	}

	public void setDocumentTabName(String documentTabName) {
		this.documentTabName = documentTabName;
	}
	
	public void setInitialFolders(String folders) {
		this.initialFolders =folders;		
	}

	public List<String> getInitialFolders() {
		List<String> folders = new ArrayList<String>();
		for (String folder : initialFolders.split(":")) {
			folders.add(folder);
		}

		return folders;
	}

	public boolean isAppendMode() {
		return appendMode;
	}

	public void setAppendMode(boolean appendMode) {
		this.appendMode = appendMode;
	}

	public boolean isSendStatus() {
		return sendStatus;
	}

	public void setSendStatus(boolean sendStatus) {
		this.sendStatus = sendStatus;
	}

	public boolean isDocuments() {
		return documents;
	}

	public void setDocuments(boolean documents) {
		this.documents = documents;
	}

	public boolean isFolders() {
		return folders;
	}

	public void setFolders(boolean folders) {
		this.folders = folders;
	}

	public String getFolderTabName() {
		return folderTabName;
	}

	public void setFolderTabName(String folderTabName) {
		this.folderTabName = folderTabName;
	}
}
