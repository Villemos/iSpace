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
 * A like is a ++ on the importance / interest of something. It is used when
 * ranking results, i.e. entries with many like entries attached will score
 * higher.
 */
public class Like extends InformationObject {

	{
		isOfType = Like.class.getName();
	}
	
	/** The URI of the entry being liked... */
	public List<String> associatedTo = new ArrayList<String>();
	
	/** The name of the person liking this. */
	public String raisedBy;

	/** The IO's text field can contain a reason for the liking. */
}
