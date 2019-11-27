package com.datastax.alexott.demos.graph;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.SimpleGraphStatement;
import com.datastax.dse.graph.api.DseGraph;
import com.datastax.dse.graph.api.TraversalBatch;
import com.github.javafaker.Faker;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import java.util.Random;
import java.util.UUID;

public class GraphLoad {
	static final int NUM_ORGS = 1000;
	static final int NUM_PEOPLE = 10000;
	static final int NUM_ACCOUNTS = 10000;

	public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
		try (DseCluster dseCluster = DseCluster.builder().addContactPoints(server)
				.withGraphOptions(new GraphOptions().setGraphName("C720")).build();
				DseSession session = dseCluster.connect()) {

			Faker faker = new Faker();
			Random rnd = new Random();

			GraphTraversalSource g = DseGraph.traversal();
			for (int i = 0; i < NUM_ORGS; i++) {
				System.out.println("Step=" + i);
				String istr = Integer.toString(i);
//				TraversalBatch batch = DseGraph.batch();
				GraphTraversal v1 = g.addV("organization").property("name", faker.company().name())
						.property("type", faker.company().buzzword())
						.property("id", istr);
//				System.out.println("v1=" + v1);
				if (v1 == null) {
					System.out.println("v1 == null!");
					continue;
				}
//				batch.add(v1);
				session.executeGraph(DseGraph.statementFromTraversal(v1));


				if (i > 2) {
					String v2str = Integer.toString(rnd.nextInt(i-1));
					session.executeGraph(DseGraph.statementFromTraversal(
							g.V().has("organization", "id", istr)
									.as("v1").V().has("organization", "id", v2str)
									.addE("is_parent").from("v1").property("distance", "1")));
					session.executeGraph(DseGraph.statementFromTraversal(
							g.V().has("organization", "id", istr)
									.as("v1").V().has("organization", "id", v2str)
									.addE("is_direct_parent").from("v1")));

					if (rnd.nextDouble() > 0.8) {
						String v3str = Integer.toString(rnd.nextInt(i-2));
						session.executeGraph(DseGraph.statementFromTraversal(
								g.V().has("organization", "id", istr)
										.as("v1").V().has("organization", "id", v3str)
										.addE("is_parent").from("v1").property("distance", "2")));
						session.executeGraph(DseGraph.statementFromTraversal(
								g.V().has("organization", "id", v2str)
										.as("v1").V().has("organization", "id", v3str)
										.addE("is_parent").from("v1").property("distance", "1")));
						session.executeGraph(DseGraph.statementFromTraversal(
								g.V().has("organization", "id", v2str)
										.as("v1").V().has("organization", "id", v3str)
										.addE("is_direct_parent").from("v1")));
					}
				}
			}
		}
	}
}
