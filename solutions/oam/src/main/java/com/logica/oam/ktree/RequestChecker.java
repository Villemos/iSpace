package com.logica.oam.ktree;

import org.apache.camel.Exchange;

public class RequestChecker {

	public void process(Exchange exchange) throws Exception {
		
		String message = "";
		boolean valid = true;
		if (exchange.getIn().getHeaders().containsKey("ktreeuser") == false || exchange.getIn().getHeader("ktreeuser").equals("") == true) {
			message += "<p>Error: User not set.</p>";
			valid = false;
		}
		if (exchange.getIn().getHeaders().containsKey("ktreepassword") == false || exchange.getIn().getHeader("ktreepassword").equals("") == true) {
			message += "<p>Error: Password not set.</p>";
			valid = false;
		}

		message += "";
				
		if (valid == false) {		
			exchange.getOut().setBody(message);
		}

		exchange.getIn().getHeaders().put("isValid", valid);

	}
}
