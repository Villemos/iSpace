package com.villemos.ispace.core.utilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointConfigurer {

	private static final transient Logger LOG = LoggerFactory.getLogger(EndpointConfigurer.class);

	public static void configure(Map<String, Object> headers, Endpoint endpoint, String prefix) {
		
		Map<String, Field> fields = new HashMap<String, Field>();
		getAllFields(endpoint.getClass(), fields);
		
		/** Extract the  header configuration fields and set them on the
		 * endpoint. This can be used to reconfigure any endpoint fields 
		 * dynamically. */
		Iterator<Entry<String, Object>> it = headers.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			String headerEntryName = entry.getKey();

			/** If the field has the format 'excell.[field name]', then set the endpoint
			 * field accordingly. */
			if (headerEntryName.startsWith(prefix)) {
				
				String fieldName = headerEntryName.replaceAll(prefix, "");
				try {
					Field field = fields.get(fieldName);
					field.setAccessible(true);
					field.set(endpoint, entry.getValue());
				}
				catch (Exception e) {
					LOG.error("The exchange contains the header field '" + entry.getKey() + "'. The field '" + fieldName + "' is not a valid configuration field of this Component.");
				}
			}
		}
	}
	
	protected static Map<String, Field> getAllFields(Class clazz, Map<String, Field> fields) {
		/** See if there is a super class. */
		if (clazz.getSuperclass() != null) {
			getAllFields(clazz.getSuperclass(), fields);
		}
		
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			fields.put(field.getName(), field);
		}
		
		return fields;
	}
}
