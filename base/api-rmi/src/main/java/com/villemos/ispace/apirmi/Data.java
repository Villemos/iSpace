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

import com.villemos.ispace.api.ICallback;
import com.villemos.ispace.api.IData;
import com.villemos.ispace.api.ResultSet;

public class Data implements IData {

	protected String host = "localhost";

	protected String binding = "server/data";

	protected IData stub = null;

	protected void connect() {
		if (stub == null) {
			try {
				Registry registry = LocateRegistry.getRegistry(host);
				stub = (IData) registry.lookup(binding);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean storeComment(String uriOfParent, String comment) throws RemoteException {
		connect();
		return stub.storeComment(uriOfParent, comment);
	}

	@Override
	public ResultSet search(String keywords) throws RemoteException {
		connect();
		return stub.search(keywords);
	}

	@Override
	public void search(String keywords, ICallback callback) throws RemoteException {
		connect();
		stub.search(keywords, callback);
	}

	@Override
	public ResultSet search(String keywords, boolean facets) throws RemoteException {
		connect();
		return stub.search(keywords, facets);
	}

	@Override
	public void search(String keywords, boolean facets, ICallback callback) throws RemoteException {
		connect();
		stub.search(keywords, facets, callback);
	}

	@Override
	public ResultSet search(String keywords, int offset) throws RemoteException {
		connect();
		return stub.search(keywords, offset);
	}

	@Override
	public void search(String keywords, ICallback callback, int offset) throws RemoteException {
		connect();
		stub.search(keywords, callback, offset);
	}

	@Override
	public ResultSet search(String keywords, boolean facets, int offset) throws RemoteException {
		connect();
		return stub.search(keywords, facets, offset);	
	}

	@Override
	public void search(String keywords, boolean facets, ICallback callback, int offset) throws RemoteException {
		connect();
		stub.search(keywords, facets, callback, offset);
	}
}
