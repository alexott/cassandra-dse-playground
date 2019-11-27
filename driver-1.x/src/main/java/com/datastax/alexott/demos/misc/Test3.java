package com.datastax.alexott.demos.misc;
import java.util.Collections;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Test3 {
	public static void main(String[] args) throws JsonProcessingException {
        String server = System.getProperty("contactPoint", "127.0.0.1");
		Cluster cluster = Cluster.builder().addContactPoint(server).withCredentials("user.0", "password").build();
		Session session = cluster.connect();

		PreparedStatement prepared = session.prepare("UPDATE test.st SET cities = cities + ? WHERE zip = ? and state = ?");
			
		BoundStatement bound = prepared.bind(Collections.singleton("t2"), "2", "1");
		session.execute(bound);
		
		BoundStatement bound2 = prepared.bind();
		bound2.setSet(0, Collections.singleton("t3"));
		bound2.setString(1, "2");
		bound2.setString(2, "1");
        session.execute(bound2);

		session.close();
		cluster.close();
	}

}
