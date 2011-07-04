/**
 * villemos consulting [space^] (http://www.villemos.de) 
 * Probe. Send. Act. Emergent solution.
 * 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * Released under proprietary license, i.e. not free. But we are friendly.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of villemos consulting, and its suppliers
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to villemos consulting
 * and its suppliers and may be covered by European and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * 
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from villemos consulting Incorporated.
 * 
 * And it wouldn't be nice either.
 * 
 */
package com.villemos.sdms.core.docmodel;

import java.util.List;

public class VirtualFolder {

	/** The URI of the document defining this folder. */
	public String uri;
	
	/** The name of the folder. */
	public String name;
	
	/** A description of the folder. */
	public String description;
	
	/** The URI of documents statically configured to be contained within this folder. */
	public List<String> staticContent;
	
	/** A list of search strings. Alle entries matching the search strings are included dynamically in the folder. */
	public List<String> dynamicContent;
	
	/** List of subfolders. */
	public List<VirtualFolder> subFolders;
	
	/** Reference to the parent folder. */
	public VirtualFolder parent = null;
}
