package com.villemos.ispace.api;

import java.rmi.RemoteException;

public interface IData {

	public boolean storeComment(String uriOfParent, String comment) throws RemoteException;

	public ResultSet search(String keywords) throws RemoteException;

	public void search(String keywords, ICallback callback) throws RemoteException;

	public ResultSet search(String keywords, boolean facets) throws RemoteException;

	public void search(String keywords, boolean facets, ICallback callback) throws RemoteException;

	public ResultSet search(String keywords, int offset) throws RemoteException;

	public void search(String keywords, ICallback callback, int offset) throws RemoteException;

	public ResultSet search(String keywords, boolean facets, int offset) throws RemoteException;

	public void search(String keywords, boolean facets, ICallback callback, int offset) throws RemoteException;

}
