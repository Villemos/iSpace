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

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.villemos.ispace.api.ResultSet;

public interface IData extends Remote {

	/**
	 * Stores a comment to an existing entry. The entry being commented is called 
	 * the 'parent'.
	 * 
	 * @param uriOfParent The URI (Fields.hasUri) of the parent entry.
	 * @param comment The comment
	 * @return Boolean flag indicating success of the storage.
	 */
	public boolean storeComment(String uriOfParent, String comment)  throws RemoteException;
	
	/**
	 * A simple keyword search, using mainly default values and no streaming. 
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries plus the related facets.  
	 * 
	 * @param search The keyword(s) of the search
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public ResultSet search(String search) throws RemoteException;

	/**
	 * A simple keyword search, using mainly default values and streaming.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries plus the related facets.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param callback The consumer to be called for each found document / facet.
	 */
	public void search(String search, ICallback callback) throws RemoteException;

	/**
	 * A simple keyword search, using mainly default values and no streaming, with the
	 * option to configure whether facets are returned. Disabling facet retrieval provides
	 * a (small) performance boost.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries plus the related facets.  
	 * 
	 * @param search The keyword(s) of the search.
	 * @param facets Flag setting whether the facets should be retrieved.
	 * @return A result set holding all documents (including highlighting and score) and facets (optional).
	 */
	public ResultSet search(String search, boolean facets) throws RemoteException;

	/**
	 * A simple keyword search, using mainly default values and streaming, with the
	 * option to configure whether facets are returned. Disabling facet retrieval provides
	 * a (small) performance boost.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries plus the related facets.  
	 * 
	 * @param search The keyword(s) of the search.
	 * @param facets Flag setting whether the facets should be retrieved.
	 */
	public void search(String search, boolean facets, ICallback callback) throws RemoteException;
	
	/**
	 * A simple keyword search, starting from an offset and no streaming. 
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries starting from the offset plus the related facets. If
	 * for example the repository holds 200 matches, then setting offset=50 and 
	 * using the default rows=100 will retrieve matches 50 to 150.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public ResultSet search(String search, int offset) throws RemoteException;

	/**
	 * A simple keyword search, starting from an offset and streaming. 
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries starting from the offset plus the related facets. If
	 * for example the repository holds 200 matches, then setting offset=50 and 
	 * using the default rows=100 will retrieve matches 50 to 150.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param callback The consumer to be called for each found document / facet.
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public void search(String search, ICallback callback, int offset) throws RemoteException;

	/**
	 * A simple keyword search, using mainly default values and no streaming, with the
	 * option to configure whether facets are returned. Disabling facet retrieval provides
	 * a (small) performance boost.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries starting from the offset plus the related facets. If
	 * for example the repository holds 200 matches, then setting offset=50 and 
	 * using the default rows=100 will retrieve matches 50 to 150.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param facets The consumer to be called for each found document / facet.
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public ResultSet search(String search, boolean facets, int offset) throws RemoteException;

	/**
	 * A simple keyword search, using mainly default values and streaming, with the
	 * option to configure whether facets are returned. Disabling facet retrieval provides
	 * a (small) performance boost.
	 * 
	 * The search will retrieve the first 100 (can be changed by setting 'rows' 
	 * on the API) entries starting from the offset plus the related facets. If
	 * for example the repository holds 200 matches, then setting offset=50 and 
	 * using the default rows=100 will retrieve matches 50 to 150.  
	 * 
	 * @param search The keyword(s) of the search
	 * @param facets The consumer to be called for each found document / facet.
	 * @param callback The consumer to be called for each found document / facet.
	 * @param offset The offset of the first entry to be retrieved. Can be used to 'continue' a search.
	 * @return A result set holding all documents (including highlighting and score) and facets.
	 */
	public void search(String search, boolean facets, ICallback callback, int offset) throws RemoteException;		
}
