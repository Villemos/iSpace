package com.villemos.ispace.solr;

import org.apache.solr.common.SolrDocument;

import com.villemos.ispace.core.search.ICallback;

public class DummyCallback implements ICallback {

	@Override
	public void receive(SolrDocument document) {
		int test = 0;		
	}
}
