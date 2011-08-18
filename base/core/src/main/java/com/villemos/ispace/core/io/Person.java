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
public class Person extends InformationObject {

	{
		isOfType = Person.class.getName();
	}
	
	/** The root name of the person. Synonym / acronym entries should be used to record other names. */
	public String hasName;
	
	/** Ways of contacting this person. Each entry should be a valid URI
	 * - mailto:[email address]
	 * - http://[site, facebook, XING, linkedin, ... profile link]. 
	 * 
	 * This information will also be used to crawl sites for additional information on the person. */	
	public List<String> furtherInformation = new ArrayList<String>();
	
	/** The IO's text field can be used to insert a description of the person. */
}
