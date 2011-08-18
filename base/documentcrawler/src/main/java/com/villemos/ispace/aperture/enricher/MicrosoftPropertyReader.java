package com.villemos.ispace.aperture.enricher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Headers;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;

import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.util.HexDump;

import com.villemos.ispace.Fields;

public class MicrosoftPropertyReader implements POIFSReaderListener {

	private static org.apache.log4j.Logger Logger = org.apache.log4j.Logger.getLogger(MicrosoftPropertyReader.class);

	public Map<String, String> msProperties = new HashMap<String, String>();;

	@Handler
	public void addMSProperties(@Body File file, @Headers Map<String, Object> headers) {
		POIFSReader r = new POIFSReader();				
		r.registerListener(this);
		try {
			FileInputStream inStream = new FileInputStream(file);
			r.read(inStream);
			
			List<Object> values = new ArrayList<Object>();
			Iterator<Entry<String, String>> it = msProperties.entrySet().iterator();
			while (it.hasNext()) {
				values.add(it.next().getValue());
			}			
			headers.put(Fields.prefix, values);
			inStream.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processPOIFSReaderEvent(final POIFSReaderEvent event) { 
		PropertySet ps = null;
		try {
			ps = PropertySetFactory.create(event.getStream());
		}
		catch (NoPropertySetStreamException ex) {
			Logger.debug("No property set stream: \"" + event.getPath() + event.getName() + "\"");
			return;
		}
		catch (Exception ex) {
			Logger.error("Exception while processing microsoft property set " + ex);
		}

		/* Print the name of the property set stream: */
		Logger.debug("Property set stream \"" + event.getPath() + event.getName() + "\":");

		/* Print the list of sections: */
		List<Section> sections = ps.getSections();
		int nr = 0;
		for (Section sec : sections) {
			String s = HexDump.dump(sec.getFormatID().getBytes(), 0L, 0); 
			s = s.substring(0, s.length() - 1);
			/* Print the number of properties in this section. */
			int propertyCount = sec.getPropertyCount();
			/* Print the properties: */
			Property[] properties = sec.getProperties();
			for (int i2 = 0; i2 < properties.length; i2++) {
				/* Print a single property: */
				Property p = properties[i2];
				long id = p.getID();
				long type = p.getType();
				Object value = p.getValue();

				String propertyName = sec.getPIDString(id);

				if (msProperties.containsKey(propertyName) == false) {
					String valueStr = value.toString();
					if (valueStr.equals("") == false) {
						msProperties.put(propertyName, valueStr);
					}
				}
			}
		}
	}
}
