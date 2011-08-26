package com.villemos.ispace.apicamel;

import java.rmi.RemoteException;
import java.util.List;

import com.villemos.ispace.api.IMetaData;
import com.villemos.ispace.api.Synonym;
import com.villemos.ispace.api.Taxonomy;

public class MetaData implements IMetaData {

	@Override
	public List<Synonym> getSynonyms() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Synonym> getSynonyms(String rootName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean storeSynonym(Synonym synonym) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Taxonomy> getTaxonomies() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Taxonomy> getTaxonomy(String parentName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean storeTaxonomy(Taxonomy taxonomy) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
