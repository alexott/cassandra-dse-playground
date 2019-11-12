package com.datastax.alexott.demos.product;

import java.util.List;
import java.util.Map;

import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "test", name = "product")
public class Product {
	@PartitionKey
	String id;
	
	@FrozenValue
	@Frozen
	Map<String, Information> details;
	
	@Frozen
	List<Information> moreDetails;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, Information> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Information> details) {
		this.details = details;
	}

	public List<Information> getMoreDetails() {
		return moreDetails;
	}

	public void setMoreDetails(List<Information> moreDetails) {
		this.moreDetails = moreDetails;
	}
	
	@Override
	public String toString() {
		return "Product [id=" + id + ", details=" + details + ", moreDetails=" + moreDetails + "]";
	}
}
