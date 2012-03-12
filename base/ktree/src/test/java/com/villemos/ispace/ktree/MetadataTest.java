package com.villemos.ispace.ktree;

import com.thoughtworks.xstream.XStream;
import com.villemos.ispace.ktree.metadata.MetadataField;
import com.villemos.ispace.ktree.metadata.MetadataItem;

import junit.framework.TestCase;

public class MetadataTest extends TestCase {

	public void testConvert() {
		String testString = "<response><fields>" +
				"							<item>" +
				"								<name>Document Author</name>" +
				"								<value>Boris Kartascheff</value>" +
				"							</item>" +
				"							<item>" +
				"								<name>Reference ID</name>" +
				"								<value>OSMV-OPMT-LOGI-RP-11-1402</value>" +
				"							</item>" +
				"						</fields></response>";
		
		MetadataItem metadata = new MetadataItem();
		XStream xstream = new XStream();

		xstream.processAnnotations(MetadataField.class);
		xstream.processAnnotations(MetadataItem.class);
		
		xstream.fromXML(testString, metadata);

		System.out.println(metadata);

	}
	
	public void testToXml() {

		MetadataItem metadata = new MetadataItem();
		XStream xstream = new XStream();

		xstream.processAnnotations(MetadataItem.class);
		xstream.processAnnotations(MetadataField.class);
		
		metadata.fields.add(new MetadataField());
		
		System.out.println(xstream.toXML(metadata));
	}
}
