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

import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.InformationObject;


/**
 * Callback interface for the reception of results from a query. Each
 * method may be called 0 or more times.
 */
public interface ICallback extends Remote {
	
	public void receive(InformationObject io) throws RemoteException;
	
	public void receive(Facet facet) throws RemoteException;
	
	public void receive(Suggestion suggestion) throws RemoteException;
	
	/**
	 * Method for receiving the statistics of the request. The method will be called
	 * one or more times depending on how many results are found. Each update can be seen
	 * as a status message. 
	 * 
	 * The timestamp of the statistic will remain the same. All other fields will increase,
	 * being the sum of all requests.
	 * 
	 * In the final update the 'statistics.totalFound == statistics.totalReturned'.  
	 * 
	 * @param statistics
	 * @throws RemoteException
	 */
	public void receive(Statistics statistics) throws RemoteException;
}
