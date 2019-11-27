package com.datastax.alexott.demos;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.apache.ivy.util.StringUtils;

// create table test.btest (id int, c1 int, t text, primary key (id, c1));

public class TestBatches {
    public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        try(Cluster cluster = Cluster.builder().addContactPoint(server).build();
        Session session = cluster.connect()) {

            PreparedStatement pStmt = session.prepare("insert into test.btest(id, c1, t) values(?, ?, ?);");
            String str = StringUtils.repeat("x", 1024);
            { // unlogged batch into single partition - it shouldn't fail
                BatchStatement batchStatement = new BatchStatement();
                for (int j = 0; j < 5000; j++) {
                    BoundStatement boundStatement = pStmt.bind(1, j, str);
                    batchStatement.add(boundStatement);
                }
                try {
                    session.execute(batchStatement);
                    System.out.println("Single-partition batch executed");
                } catch (Exception ex) {
                    System.out.println("Got exception for single-partition batch: " + ex.getMessage());
                }
            }
            { // unlogged batch into single partition - it should fail with big mutation error
                BatchStatement batchStatement = new BatchStatement();
                for (int j = 0; j < 50000; j++) {
                    BoundStatement boundStatement = pStmt.bind(1, j, str);
                    batchStatement.add(boundStatement);
                }
                try {
                    session.execute(batchStatement);
                    System.out.println("Big Single-partition batch executed");
                } catch (Exception ex) {
                    System.out.println("Got exception for big single-partition batch: " + ex.getMessage());
                }
            }
            { // unlogged batch into single partition - it should fail
                BatchStatement batchStatement = new BatchStatement();
                for (int j = 0; j < 5000; j++) {
                    BoundStatement boundStatement = pStmt.bind(j, j, str);
                    batchStatement.add(boundStatement);
                }
                try {
                    session.execute(batchStatement);
                    System.out.println("Multi-partition batch executed");
                } catch (Exception ex) {
                    System.out.println("Got exception for multi-partition batch: " + ex.getMessage());
                }
            }

        }
    }

}
