package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;

public class STestMain {
	public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
		Cluster cluster = Cluster.builder().addContactPoint(server).build();
		Session session = cluster.connect();

		MappingManager manager = new MappingManager(session);

		STestAccessor sa = manager.createAccessor(STestAccessor.class);
		Result<STest> rs = sa.getViaSolr("*:*");

		for (STest sTest : rs) {
			System.out.println("id=" + sTest.getId() + ", text=" + sTest.getT());
		}

		session.close();
		cluster.close();
	}

}
