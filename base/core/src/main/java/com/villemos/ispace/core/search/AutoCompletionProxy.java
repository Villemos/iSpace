package com.villemos.ispace.core.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultExchange;
import org.springframework.beans.factory.annotation.Autowired;

import com.villemos.ispace.api.Facet;
import com.villemos.ispace.api.ResultSet;
import com.villemos.ispace.api.SolrOptions;

public class AutoCompletionProxy {

	/** The Camel context used to send the request.*/
	@Autowired
	protected CamelContext context = null;

	protected int rows = 10;

	protected List<Facet> facets = new ArrayList<Facet>();

	protected int minimumLength = 3;

	protected String boldStart = "<b>";
	protected String boldEnd = "</b>";

	protected String lastSearch = null;
	protected String lastFacet = null;

	public Map<String, String> getSuggestions(String search, String facet, long count) {

		/** The results to be returned to the user. */
		Map<String, String> results = new LinkedHashMap<String, String>();

		if (search == null || search.length() < minimumLength) {
			facets.clear();
			return results;
		}        

		String[] tokens = search.split("\\s+");
		String token = tokens[tokens.length - 1].trim();

		String initial = "";
		for (int index = 0; index < tokens.length - 1; index++) {
			initial += tokens[index] + " ";
		}

		if (token.length() < minimumLength) {
			results.clear();
			return results;
		}

		if (facet == null || facet.equals("") == true) {
			facet = "spell";
		}

		results.clear();

		long counter = 0;

		// Determine whether to search again.
		if (lastSearch == null || token.contains(lastSearch) == false || lastFacet == null || facet.contains(lastFacet) == false) {

			lastSearch = search;
			lastFacet = facet;

			Exchange exchange = new DefaultExchange(context, ExchangePattern.InOut);
			exchange.getIn().setHeader(SolrOptions.query, "spell:*");
			exchange.getIn().setHeader(SolrOptions.stream, null);
			exchange.getIn().setHeader(SolrOptions.facets, true);
			exchange.getIn().setHeader(SolrOptions.offset, 0);
			exchange.getIn().setHeader(SolrOptions.rows, rows);
			exchange.getIn().setHeader(SolrOptions.facetsort, "count");
			exchange.getIn().setHeader(SolrOptions.facetlimit, -1);
			exchange.getIn().setHeader(SolrOptions.facetprefix, token);
			exchange.getIn().setHeader(SolrOptions.facetfield, facet);

			context.createProducerTemplate().send("direct:solrSearch", exchange);

			ResultSet data = (ResultSet) exchange.getOut().getBody();

			facets = data.facets;

			if (facets != null) {
				for (Facet field : facets) {
					if (field.values != null) {

						Iterator<Entry<String, Long>> it = field.values.entrySet().iterator();
						while (it.hasNext()) {
							Entry<String, Long> entry = it.next();

							results.put(initial + boldStart + token + boldEnd + entry.getKey().substring(token.length(), entry.getKey().length()) + " (" + entry.getValue() + ")", entry.getKey());

							if (++counter >= count) {
								break;
							}
						}
					}
				}     
			}
		}
		// No need to search again. Remove all entries that does not match the new search.
		else {
			if (facets != null) {
				for (Facet field : facets) {

					Iterator<Entry<String, Long>> it = field.values.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, Long> entry = it.next();

						if (entry.getKey().startsWith(token) == true) {
							results.put(initial + boldStart + token + boldEnd + entry.getKey().substring(token.length(), entry.getKey().length()) + " (" + entry.getValue() + ")", entry.getKey());

							if (++counter >= count) {
								break;
							}
						}
					}
				}        
			}
		}

		return results;
	}

	public List <Facet> getFacets() {
		return facets;
	}

	public void setFacets(List <Facet> facets) {
		this.facets = facets;
	}

	public int getMinimumLength() {
		return minimumLength;
	}

	public void setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
	}

	public String getBoldStart() {
		return boldStart;
	}

	public void setBoldStart(String boldStart) {
		this.boldStart = boldStart;
	}

	public String getBoldEnd() {
		return boldEnd;
	}

	public void setBoldEnd(String boldEnd) {
		this.boldEnd = boldEnd;
	}

	public String getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(String lastSearch) {
		this.lastSearch = lastSearch;
	}

	public String getLastFacet() {
		return lastFacet;
	}

	public void setLastFacet(String lastFacet) {
		this.lastFacet = lastFacet;
	}
}
