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
package com.villemos.ispace.core.io;

import java.util.ArrayList;
import java.util.List;

/**
 * @author villemosg
 *
 */
public abstract class InformationObject {

	/** The type of the entry. Used to map from Solr entry to Java objects. */
	public String isOfType;
	
	/** Unique identifier of the object. May be a logical reference or a URL. */
	public String hasUri;
	
	/** The time the entry was last stored.*/
	public Long hasTimestamp;
	
	/** A log of updates to this entry. */
	public List<String> hasLog = new ArrayList<String>();

	/** The name/title of this entry. */
	public String hasName;

	/** The following attribute ensures that any information object can be 
	 * part of a taxonomy, i.e. instances can be ordered hierarchical. The hierarchy
	 * is defined by each element 'pointing' to its parent. */
	
	/** The name of the parent taxonomy entry. */
	public String hasParent;
	

	/** Per default the IO's text field will hold the description of the entry. */
	public String hasText;
}
