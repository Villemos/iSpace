package com.logica.oam.ktree.types;

import java.net.MalformedURLException;
import java.net.URL;

public class ApplicationData {

	public ApplicationData(String name, String id, String url, boolean inKtree, boolean inCidl) {
		this.name = name;
		this.id = id;
		this.inKtree = inKtree;
		this.inCidl = inCidl;

		if (url != null) {
			try {
				this.url = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	public String name;
	public String id;
	public URL url = null;

	public boolean inKtree = false;
	public boolean inCidl = false;

	public long documents = 0;
	public long filtered = 0;
}
