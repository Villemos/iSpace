package com.villemos.ispace.ktreecrawler;

import org.apache.camel.Exchange;

public class StatusHolder {

	protected String status = "";
	
	synchronized public void process(Exchange exchange) {
		status += (String) exchange.getIn().getBody() + "\n";
	}
	
	synchronized public String getStatus() {
		return status;
	}	
}
