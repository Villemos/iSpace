package com.villemos.sdms.doccrawler;

import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Starter {

	public static void main(String[] args) {
		
		/** Read the configuration file as the first argument. If not set, then we try the default name. */
		String assemblyFile = System.getProperty("sdms.assembly") == null ? "classpath:assembly.xml" : System.getProperty("sdms.assembly");

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

	
}
