package com.datastax.alexott.demos.graph;

import java.util.UUID;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.SimpleGraphStatement;

public class GDTest1 {

	public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
		try (DseCluster dseCluster = DseCluster.builder().addContactPoints(server)
				.withGraphOptions(new GraphOptions().setGraphName("test")).build();
				DseSession session = dseCluster.connect()) {

			long start = System.nanoTime();
			long startL = System.nanoTime();
			for (int i = 1; i <= 1000; i++) {
				// String s = String.format("g.addV(label, 'person' ,'id', '%s' ,"
				// + "'email', 'sample%d@gmail.com')", UUID.randomUUID().toString(), i);
				// session.executeGraph(s).one().asVertex();

				SimpleGraphStatement s = new SimpleGraphStatement(
						"g.addV(label, 'person' ,'id', idV , 'email', emailV)").set("idV", UUID.randomUUID().toString())
								.set("emailV", "sample@gmail.com" + Integer.toString(i));
				session.executeGraph(s).one().asVertex();

				if ((i % 100) == 0) {
					long endL = System.nanoTime();
					System.out.printf("%d time = %d ms\n", i, (endL - startL) / 1000000);
					startL = System.nanoTime();
				}
			}
			long end = System.nanoTime();
			System.out.printf("Total time = %d ms\n", (end - start) / 1000000);
		}
	}
}
