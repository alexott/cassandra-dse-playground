package com.datastax.alexott.demos;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DTestMain {
	public static void main(String[] args) throws SolrServerException, IOException {
		String url = "http://localhost:8983/solr/test.dtest";

		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		mapper.registerModule(new JavaTimeModule());

		SolrClient client = new HttpSolrClient(url);
		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");
		query.addFilterQuery("id:1");
		query.setRows(10);
		QueryResponse response = client.query(query);
		SolrDocumentList list = response.getResults();
		DocumentObjectBinder binder = new DocumentObjectBinder();

		List<DTest> lst = binder.getBeans(DTest.class, list);
		for (DTest dTest : lst) {
			System.out.println("id=" + dTest.getId() + ", t=" + dTest.getT());
		}

	}

}
