package com.villemos.ispace.testclient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.InformationObject;
import com.villemos.ispace.api.Statistics;
import com.villemos.ispace.api.Suggestion;

public class DataPrinter {

	protected List<Object> data = new ArrayList<Object>();
	
	@Handler
	public void process(Exchange exchange) {		
		/** Store object for later browsing. */
		data.add(exchange.getIn().getBody());
		
		/** Print object. */		
		System.out.println(data.size() + ". " + exchange.getIn().getBody().getClass().getSimpleName());
	}
	
	public void printOverview() {
		/** Iterate through data and display it. */
		int index = 0;
		for (Object object : data) {
			System.out.println(index + ". " + object.getClass().getSimpleName());
			index++;
		}
	}

	public void printAllObjectDetail() {
		/** Iterate through data and display it. */
		int index = 0;
		for (Object object : data) {
			System.out.println(index + ". " + object.getClass().getSimpleName());
			printObjectDetails(index);
			index++;
		}
	}

	public void printObjectDetails(int index) {
		if (data.size() > index) {
			Object object = data.get(index);
			System.out.println(index + ". " + object.getClass().getSimpleName());
			
			if (object instanceof Facet) {
				Facet facet = (Facet) object;
				System.out.println("  field=" + facet.field);
				
				System.out.print("  values=");
				Iterator<Entry<String, Long>> it = facet.values.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Long> entry = it.next();
					System.out.print("(" + entry.getKey() + ", " + entry.getValue() + ") ");
				}	
				System.out.println("");
			}
			else if (object instanceof Statistics) {
				Statistics statistics = (Statistics) object;
				System.out.println("  queryTime=" + statistics.queryTime);
				System.out.println("  totalFound=" + statistics.totalFound);
				System.out.println("  totalRequested=" + statistics.totalRequested);
				System.out.println("  totalReturned=" + statistics.totalReturned);
				System.out.println("  maxScore=" + statistics.maxScore);
				System.out.println("  timestamp=" + statistics.timestamp.toString());				
			}
			else if (object instanceof InformationObject) {
				InformationObject io = (InformationObject) object;
				
				System.out.println("  boost=" + io.boost);
				System.out.println("  fromSource=" + io.fromSource);
				System.out.println("  hasTitle=" + io.hasTitle);
				System.out.println("  hasUri=" + io.hasUri);
				System.out.println("  isPartOf=" + io.isPartOf);
				System.out.println("  ofEntityType=" + io.ofEntityType);
				System.out.println("  ofMimeType=" + io.ofMimeType);
				System.out.println("  score=" + io.score);
				System.out.println("  wasStoredAt=" + io.wasStoredAt);
				System.out.println("  withIssue=" + io.withIssue);
				System.out.println("  withReferenceId=" + io.withReferenceId);
				System.out.println("  withRevision=" + io.withRevision);
				
				for (String value : io.hasPart) {
					System.out.println("  hasPath=" + value);
				}
				for (String value : io.highlight) {
					System.out.println("  highlight=" + value);
				}
				for (String value : io.isAttachedTo) {
					System.out.println("  isAttachedTo=" + value);
				}
				for (String value : io.withAttachedLog) {
					System.out.println("  withAttachedLog=" + value);
				}
				if (io.dynamic != null) {
					System.out.print("  dynamic=");
					Iterator<Entry<String, Object>> it = io.dynamic.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, Object> entry = it.next();
						System.out.println("(" + entry.getValue() + ", " + entry.getValue().toString() + ") ");
					}
				}
				else {
					System.out.print("  no dynamic fields.");
				}
			}
			else if (object instanceof Suggestion) {
				Suggestion suggestion = (Suggestion) object;
				System.out.print("  suggestion=" + suggestion.suggestion);
				System.out.print("  root=" + suggestion.root);
				System.out.print("  fromSource=" + suggestion.fromSource);
			}
		}
	}
	
	public Object getObject(int index) {
		if (data.size() > index) {
			return data.get(index);
		}
		
		return null;
	}
	
	public void clear() {
		data.clear();
	}
}
