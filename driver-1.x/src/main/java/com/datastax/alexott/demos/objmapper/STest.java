package com.datastax.alexott.demos;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "test",name = "stest")
public class STest {
	@PartitionKey
	private int id;
	private String t;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}

}
