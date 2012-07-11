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
package com.villemos.ispace.ktree.folder;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("item") 
public class Item {
	public String id; 
	public String item_type;
	public String custom_document_no;
	public String oem_document_no; 
	public String title;
	public String document_type;
	public String filename;
	public String filesize;
	public String created_by;
	public String created_date;
	public String checked_out_by;
	public String checked_out_date;
	public String modified_by;
	public String modified_date;
	public String owned_by;
	public String version;
	public String content_id;
	public String is_immutable;
	public String permissions;
	public String workflow;
	public String workflow_state;
	public String mime_type;
	public String mime_icon_path;
	public String mime_display;
	public String storage_path;
	
	/** Additional fileds set based on the parent folder. */
	public String absoluteFilename;
	public String inPath;
	public String inParentFolderId;
	public String inParentFolder;
	
	/** Metadata fields. */
	public Map<String, Object> metadata = new HashMap<String, Object>();
	
	
	public String log;
	
	public Object get(String name) {
		Object metadata = this.metadata.get(name);
		
		return metadata == null ? "" : metadata;
	}
}
