package com.logica.oam.ktree.enricher;

import java.util.List;
import java.util.Map;

import org.apache.camel.Body;

import com.villemos.ispace.ktree.folder.Item;

public class SetFolderUrl {
	public void process(@Body Map<String, List<Object>> documents) {

		for (Object object : documents.get("documents")) {
			Item item = (Item) object;
			item.metadata.put("folder_url", "https://om.eo.esa.int/oem/kt/browse.php?fFolderId=" + item.inParentFolderId);
		}
	}
}
