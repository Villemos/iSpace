package com.villemos.ispace.databasecrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;

/**
 * Splitter intended to be used in conjunction with a Camel JDBC component.
 * 
 * The JDBC component will forward a Body containing a ArrayList<HashMap<String, Object>>
 * result set of the SQL query executed. This class parses this structure and creates
 * a Camel message for each result entry.
 * 
 * The name of the DB field is likely different from the fields on the repository. The field
 * names are therefore mapped.
 */
public class SqlResultsetMapper {

	protected Map<String, String> fieldMap = new HashMap<String, String>();

	@Handler
	public List<Message> map(@Body HashMap<String, Object> result) {
		List<Message> messages = new ArrayList<Message>();

		/** Iterate through the result set, map the fields and create a Message per result. */
		Iterator<Entry<String, Object>> it = result.entrySet().iterator();
		while (it.hasNext()) {
			Message message = new DefaultMessage();

			Entry<String, Object> entry = it.next();

			String mappedValue = fieldMap.get(entry.getKey());
			if (mappedValue != null) {
				message.setHeader(mappedValue, entry.getValue());					
			}

			messages.add(message);
		}

		return messages;
	}

	public Map<String, String> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, String> fieldMap) {
		this.fieldMap = fieldMap;
	} 
}
