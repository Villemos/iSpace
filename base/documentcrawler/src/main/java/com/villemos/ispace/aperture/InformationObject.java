package com.villemos.ispace.aperture;

import java.util.HashMap;
import java.util.Map;

public class InformationObject {

	public String hasTitle;
	public String hasUri;
	public String ofMimeType;
	public String fromSource;
	public String withRawText;
	
	public Map<String, Object> metadata = new HashMap<String, Object>();
}
