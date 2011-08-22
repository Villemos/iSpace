package com.villemos.ispace.core.search;


import com.villemos.ispace.core.io.Facet;
import com.villemos.ispace.core.io.InformationObject;

public interface ICallback {

	public void receive(InformationObject document);
	public void receive(Facet document);
}
