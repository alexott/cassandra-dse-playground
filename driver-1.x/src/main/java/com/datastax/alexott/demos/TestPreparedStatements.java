package com.datastax.alexott.demos;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class TestPreparedStatements {

    public static BoundStatement nullsToUnset(BoundStatement statement) {
        ColumnDefinitions variables = statement.preparedStatement().getVariables();
        int numPlaceholders = variables.size();
        for (int i = 0; i < numPlaceholders; i++) {
            if (statement.isNull(i)) {
                System.out.println("Found null in position " + i + ", column: " + variables.getName(i));
                statement.unset(i);
            }
        }
        return statement;
    }

    public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        try (Cluster cluster = Cluster.builder().addContactPoint(server).build();
             Session session = cluster.connect()) {

            session.execute("create keyspace if not exists test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};");
            session.execute("create table if not exists test.unsets(id int primary key, t text, i int);");
            session.execute("truncate test.unsets;");

            PreparedStatement preparedStatement = session.prepare("insert into test.unsets(id, t, i) values(?,?,?);");

            // this will insert tombstone
            BoundStatement b1 = preparedStatement.bind(1, null, 2);
            session.execute(b1);

            // this will not
            BoundStatement b2 = preparedStatement.bind(2, null, 3);
            session.execute(nullsToUnset(b2));

            ResultSet rs = session.execute("select * from test.unsets;");
            for (Row row: rs) {
                System.out.println(row);
            }
        }
    }
}
