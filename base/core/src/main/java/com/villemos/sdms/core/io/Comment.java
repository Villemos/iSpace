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
package com.villemos.sdms.core.io;

import java.util.ArrayList;
import java.util.List;

/**
 * A comment is an information object raised to comment on another information object.
 * The target of the comment can be any type; a person, company, entry, acronym... The 
 * comment is intended to be shown 'along with' the target object and as such doesnt
 * affect the target.
 *
 */
public class Comment extends InformationObject{

	{
		isOfType = Comment.class.getName();
	}
	
	/** The URI of the entry being liked... */
	public List<String> associatedTo = new ArrayList<String>();
	
	/** The name of the person raising this comment. Can be used to build a URI to the person (who might exist). */
	public String raisedBy;
	
	/** The IO's text field will hold the actual comment. */
}
