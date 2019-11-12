package com.datastax.alexott.demos;

import java.time.LocalDate;
import java.util.UUID;

import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "test", name = "dtest", readConsistency = "ONE", writeConsistency = "ONE")
public class TestData {
	@PartitionKey
	@Column(name = "id")
	private UUID id;

	@Column(name = "ddate", codec = LocalDateCodec.class)
	LocalDate ddate;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public LocalDate getDdate() {
		return ddate;
	}

	public void setDdate(LocalDate ddate) {
		this.ddate = ddate;
	}
	
}
