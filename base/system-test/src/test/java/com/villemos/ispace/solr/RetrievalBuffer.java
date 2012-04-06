package com.villemos.ispace.solr;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;

import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.InformationObject;
import com.villemos.ispace.api.Statistics;
import com.villemos.ispace.api.Suggestion;

public class RetrievalBuffer {

	public static RetrievalBuffer buffer = null;
	
	public RetrievalBuffer() {
		RetrievalBuffer.buffer = this;
	}
	
	List<InformationObject> io = new ArrayList<InformationObject>();
	List<Facet> facet = new ArrayList<Facet>();
	List<Suggestion> suggestion = new ArrayList<Suggestion>();
	List<Statistics> statistics = new ArrayList<Statistics>();
	
	int totalReceived = 0;
	
	public void process(Exchange exchange) {
		Object object = exchange.getIn().getBody();
		
		totalReceived++;
		
		if (object instanceof InformationObject) {
			io.add((InformationObject)object);
		}
		else if (object instanceof Facet) {
			facet.add((Facet)object);
		}
		else if (object instanceof Suggestion) {
			suggestion.add((Suggestion)object);
		}
		else if (object instanceof Statistics) {
			statistics.add((Statistics)object);
		}		
	}
	
	public void clear() {
		io.clear();
		facet.clear();
		suggestion.clear();
		statistics.clear();
		this.totalReceived = 0;
	}
}
