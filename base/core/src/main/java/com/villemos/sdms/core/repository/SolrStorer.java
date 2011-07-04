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
package com.villemos.sdms.core.repository;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.villemos.sdms.core.accessors.AbstractAccessor;
import com.villemos.sdms.core.io.InformationObject;



public class SolrStorer extends SolrAccessor {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SolrStorer.class);

	protected Pattern solrField = Pattern.compile("doc.(\\w+)");

	@Handler
	public void store(@Body List<InformationObject> iObjects) {
		try {
			for (InformationObject iObject : iObjects) {
				SolrInputDocument document = toSolr(iObject);

				UpdateResponse response = AbstractAccessor.getAccessor().getServer().add(document);
				if (response.getStatus() == 500) {
					logger.error("");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			AbstractAccessor.getAccessor().getServer().commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected SolrInputDocument toSolr(InformationObject iObject) {
		SolrInputDocument document = new SolrInputDocument();

		iObject.hasTimestamp = (new Date()).getTime();

		try {
			for (Field field : getAllFields(iObject.getClass())) {
				if (field.get(iObject) != null) {
					if (field.getClass().isArray()) {
						int length = Array.getLength(field);
						for (int i = 0; i < length; i ++) {
							document.addField(field.getName(), Array.get(field, i));
						}
					}
					else {
						document.setField(field.getName(), field.get(iObject));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 

		return document;
	}
}
