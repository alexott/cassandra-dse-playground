package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

import java.util.Random;

public class Test4_2 {
    public static void main(String[] args) throws InterruptedException {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        QueryOptions queryOptions = new QueryOptions().setDefaultIdempotence(true);
        try(Cluster cluster = Cluster.builder().withProtocolVersion(ProtocolVersion.V4)
                .addContactPoint(server)
                .withQueryOptions(queryOptions)
                // for working with Cassandra 2.1-2.2
                .withProtocolVersion(ProtocolVersion.V3)
                .build();
            Session session = cluster.connect()) {

            session.execute("create KEYSPACE if not exists test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};");
            session.execute("create table if not exists test.t4(id int, c int, t text, primary key(id, c));");

            MappingManager manager = new MappingManager(session);
            Mapper<Test4Data> mapper = manager.mapper(Test4Data.class);

            Random rnd = new Random();

            long c = 0;
            while(true) {
                int i = rnd.nextInt();
                int j = rnd.nextInt();
                try {
                    mapper.save(new Test4Data(i, j, "t " + i + "," + j));
                } catch (Exception ex) {
                    System.out.println("Got exception: " + ex.getMessage());
                }
                Thread.sleep(10);
                c++;
                if ((c % 100) == 0) {
                    System.out.println("Submitted " + c + " requests");
                }
            }
        }
    }

}
