package com.datastax.alexott.demos;

import java.time.Instant;
import java.util.Date;
import org.apache.solr.client.solrj.beans.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class DTest {
	@Field("id")
	private int id;

	private Instant t;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getT() {
		return new Date(t.toEpochMilli());
	}

	@Field("t")
	public void setT(Date t) {
		this.t = t.toInstant();
	}

	@JsonIgnore
	public void setInstant(Instant t) {
		this.t = t;
	}

	@JsonIgnore
	public Instant getInstant() {
		return t;
	}
}
