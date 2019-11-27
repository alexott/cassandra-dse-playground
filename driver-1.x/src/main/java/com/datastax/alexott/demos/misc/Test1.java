package com.datastax.alexott.demos.misc;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Set;

public class Test1 {
	public static void main(String[] args) throws JsonProcessingException {
        String server = System.getProperty("contactPoint", "127.0.0.1");
		Cluster cluster = Cluster.builder().addContactPoint(server).build();
		Session session = cluster.connect();

		ResultSet rs = session.execute("select * from test.ftest ;");
		System.out.print("[");
		for (Row row : rs) {
		  for (Definition key : row.getColumnDefinitions()) {
            System.out.println(key.getName() + ", type=" + key.getType());
            if (key.getType().equals(DataType.frozenSet(DataType.varchar()))) {
              System.out.println("\tbingo!");
              Set<String> ts = row.getSet(key.getName(), String.class);
              for (String string : ts) {
                System.out.println("\tval=" + string);
              }
            }
		  }
		}
		
		session.close();
		cluster.close();
	}

}
