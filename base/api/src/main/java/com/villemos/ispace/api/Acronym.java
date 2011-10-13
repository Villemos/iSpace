package com.villemos.ispace.api;

public class Acronym extends InformationObject {

	public Acronym(String abbrivation, String definition, String extractedFrom) {
		this.hasUri = "ispace:acronym://" + abbrivation + "/" + definition;
		this.ofMimeType = "ispace/virtual";
		this.ofEntityType = "Acronym";
		this.hasTitle = definition + " (" + abbrivation + ")";
		this.withRawText = definition;
		this.withAttachedLog.add("Acronym extracted from URI '" + extractedFrom + "'.");
		
		this.dynamic.put("withAbbrivation", abbrivation);
	}

	public void setAbbrivation(String abbrivation) {
		dynamic.put("hasAbbrivation", abbrivation);
	}
	
	public void setDefinition(String definition) {
		this.withRawText = definition;
	}	
}
