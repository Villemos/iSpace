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
package com.villemos.ispace.enricher;

import java.lang.reflect.Field;

import org.apache.camel.Body;
import org.apache.camel.Exchange;

import com.villemos.ispace.api.Fields;
import com.villemos.ispace.api.InformationObject;

public class ExpressionBasedTransformer {

	protected String expression = "\\<.*?\\>";	

	protected String fieldName = Fields.withRawText;

	protected String replacement = "";

	public void transform(@Body InformationObject io) {
		try {
			Field field = io.getClass().getField(fieldName);
			String oldText = (String) field.get(io);
			String newText = oldText.replaceAll(expression, replacement);
			field.set(io, newText);
		}
		catch (NoSuchFieldException e) {
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
