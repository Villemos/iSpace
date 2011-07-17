package com.villemos.ispace.aperture.processor;

import java.io.File;
import java.util.List;

import org.apache.camel.Message;



/**
 * A file parser reads files in specific formats and extract the data embedded within. 
 */
public interface IProcessor {
	List<Message> process(Message message, File file);
}
