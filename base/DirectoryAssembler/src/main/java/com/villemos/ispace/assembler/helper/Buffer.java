package com.villemos.ispace.assembler.helper;

import org.apache.camel.Body;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.aperture.InformationObject;

public class Buffer {

	private static final Log LOG = LogFactory.getLog(Buffer.class);
	
	public InformationObject io = null;
	
	public void process(@Body InformationObject io) {
		if (this.io != null) {
			LOG.error("Second IO received in buffer.");
		}
		
		this.io = io;
	}
	
	public void clear() {
		this.io = null;
	}	
}
