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
package com.villemos.ispace.apirmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import com.villemos.ispace.api.IMetaData;
import com.villemos.ispace.api.Synonym;
import com.villemos.ispace.api.Taxonomy;

public class MetaData implements IMetaData {

	protected String host = "localhost";

	protected String binding = "server/metadata";

	protected IMetaData stub = null;

	protected void connect() {
		if (stub == null) {
			try {
				Registry registry = LocateRegistry.getRegistry(host);
				stub = (IMetaData) registry.lookup(binding);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public List<Synonym> getSynonyms() throws RemoteException {
		connect();
		return stub.getSynonyms();
	}

	@Override
	public List<Synonym> getSynonyms(String rootName) throws RemoteException {
		connect();
		return stub.getSynonyms(rootName);
	}

	@Override
	public boolean storeSynonym(Synonym synonym) throws RemoteException {
		connect();
		return stub.storeSynonym(synonym);
	}

	@Override
	public List<Taxonomy> getTaxonomies() throws RemoteException {
		connect();
		return stub.getTaxonomies();
	}

	@Override
	public List<Taxonomy> getTaxonomy(String parentName) throws RemoteException {
		connect();
		return stub.getTaxonomy(parentName);
	}

	@Override
	public boolean storeTaxonomy(Taxonomy taxonomy) throws RemoteException {
		connect();
		return stub.storeTaxonomy(taxonomy);
	}
}
