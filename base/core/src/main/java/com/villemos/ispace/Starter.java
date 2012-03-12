/**
 * villemos solutions [space^] (http://www.villemos.com) 
 * Probe. Send. Act. Emergent solution.
 * 
 * Copyright 2011 Gert Villemos
 * All Rights Reserved.
 * 
 * Released under the Apache license, version 2.0.
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
package com.villemos.ispace;

import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Starter {

	public static void main(String[] args) {

		System.out.println("Starting iSpace.");

		try {
			/** Read the configuration file as the first argument. If not set, then we try the default name. */
			String assemblyFile = System.getProperty("ispace.assembly") == null ? "assembly.xml" : System.getProperty("ispace.assembly");

			System.out.println("Using assembly file " + assemblyFile);

			new FileSystemXmlApplicationContext(assemblyFile);

			while(true) {
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
