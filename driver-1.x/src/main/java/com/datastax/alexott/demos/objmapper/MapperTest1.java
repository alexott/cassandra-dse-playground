package com.datastax.alexott.demos;

import java.util.UUID;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

public class MapperTest1 {
	public static void main(String[] args) {
		Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
		Session session = cluster.connect();
		
		MappingManager manager = new MappingManager(session);

		Mapper<TestData> mapper = manager.mapper(TestData.class);

		UUID uuid = UUID.fromString("e7ae5cf3-d358-4d99-b900-85902fda9bb1");
		TestData td = mapper.get(uuid);

		if (td == null) {
			System.out.println("Can't find given UUID");
		} else {
			System.out.println("UUID: " + td.getId() + ", date: " + td.getDdate());
		}
		
		session.close();
		cluster.close();
	}
}
