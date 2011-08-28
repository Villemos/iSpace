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
package com.villemos.ispace.api;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple callback implementation, buffering all data received.
 *
 */
public class BufferedCallbackClient implements ICallback {

	/** List holding all received information objects resulting from a query. */
	protected List<InformationObject> informationObjects = new ArrayList<InformationObject>();
	
	/** List holding all facets resulting from a query. */
	protected List<Facet> facets = new ArrayList<Facet>();
	
	/** List holding all suggestions to alternative words in a query. */
	protected List<Suggestion> suggestions = new ArrayList<Suggestion>();
	
	protected Statistics statistics;
	
	@Override
	public void receive(InformationObject document) throws RemoteException {
		informationObjects.add(document);
	}

	@Override
	public void receive(Facet document) throws RemoteException {
		facets.add(document);
	}

	@Override
	public void receive(Suggestion document) throws RemoteException {
		suggestions.add(document);
	}

	@Override
	public void receive(Statistics statistics) throws RemoteException {
		this.statistics = statistics;		
	}

	public List<InformationObject> getInformationObjects() {
		return informationObjects;
	}

	public List<Facet> getFacets() {
		return facets;
	}

	public List<Suggestion> getSuggestions() {
		return suggestions;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public void clear() {
		informationObjects.clear();
		facets.clear();
		suggestions.clear();
	}
}
