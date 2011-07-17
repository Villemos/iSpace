package com.villemos.ispace.solr;

import org.apache.solr.common.SolrDocument;

public interface ICallback {

	public void receive(SolrDocument document);
}
