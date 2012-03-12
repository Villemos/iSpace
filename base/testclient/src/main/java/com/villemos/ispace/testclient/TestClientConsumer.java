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
package com.villemos.ispace.testclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.direct.DirectConsumer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.api.InformationObject;
import com.villemos.ispace.api.SolrOptions;

public class TestClientConsumer extends DirectConsumer {

	private static final Log LOG = LogFactory.getLog(TestClientConsumer.class);

	public TestClientConsumer(DefaultEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
	}

	protected TestClientEndpoint getTestClientEndpoint() {
		return ((TestClientEndpoint) getEndpoint());
	}
	
	public void start() throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		boolean exit = false;
		while (exit == false) {
			
			System.out.println("Main");
			System.out.println("1. Create new IO.");
			System.out.println("2. Search.");
			System.out.println("3. Autocomplete.");
			System.out.println("4. Print overview.");
			System.out.println("5. Print one.");
			System.out.println("6. Print all.");
			System.out.println("e. Exit.");
			
			System.out.print("Choice:");
			String command = br.readLine();
			
			if (command.equals("1")) {
				createInformationObject(br);
			}
			else if (command.equals("2")) {
				performSearch(br);
				System.out.println("Results will arrive in a second... Press any key to continue...");
				int t = br.read();
			}
			else if (command.equals("3")) {
				autocomplete(br);
				System.out.println("Results will arrive in a second... Press any key to continue...");
				int t = br.read();
			}
			else if (command.equals("4")) {
				((TestClientEndpoint) getEndpoint()).getPrinter().printOverview();
			}			
			else if (command.equals("5")) {
				System.out.print("Index:");
				int index = Integer.parseInt(br.readLine());				
				
				((TestClientEndpoint) getEndpoint()).getPrinter().printObjectDetails(index);
			}			
			
			else if (command.equals("6")) {
				((TestClientEndpoint) getEndpoint()).getPrinter().printAllObjectDetail();
			}			
			else if (command.equals("e")) {
				exit = true;
			}
			else {
				System.out.println("Command not recognized.");
			}
		}
	}

	private void autocomplete(BufferedReader br) {
		System.out.println("Type a piece of text, then press enter. A set of auto completion suggestions will  be provided. Enter an empty string to return.");
		
		String search = "";
		while (true) {
			System.out.println("Search (complete):");
			
			try {
				search = br.readLine();
				
				if (search.equals("")) {
					break;
				}
				
				Map<String, String> completionSuggestions = getTestClientEndpoint().getAutoCompletionProxy().getSuggestions(search, null, 5);
				
				Iterator<Entry<String, String>> it = completionSuggestions.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					System.out.print("(" + entry.getKey() + ", " + entry.getValue() + ") ");
				}
				System.out.println("");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void performSearch(BufferedReader br) throws IOException {

		System.out.print("Search: ");
		String search = br.readLine();

		Exchange exchange = new DefaultExchange(getEndpoint().getCamelContext());
		exchange.getIn().getHeaders().put(SolrOptions.query, search);
		exchange.getIn().getHeaders().put(SolrOptions.stream, "direct:return");
		exchange.getIn().getHeaders().put(SolrOptions.facets, "true");
		
		try {
			getAsyncProcessor().process(exchange);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createInformationObject(BufferedReader br) throws IOException {
		InformationObject io = new InformationObject();
		
		boolean exit = false;
		while (true) {
			printIo(io);
			
			System.out.println("1. Set value.");
			System.out.println("2. Send.");
			System.out.println("e. Exit.");

			String command = br.readLine();
			
			if (command.equals("1")) {
				System.out.print("Field: ");
				String field = br.readLine();
				System.out.print("Value: ");
				String value = br.readLine();				
			}
			else if (command.equals("2")) {
				
			}
			else if (command.equals("e")) {
				exit = true;
			}
			else {
				System.out.println("Command not recognized.");
			}			
		}
	}

	private void printIo(InformationObject io) {
		
	}
}
