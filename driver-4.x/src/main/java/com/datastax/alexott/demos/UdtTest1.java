package com.datastax.alexott.demos;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.type.UserDefinedType;

public class UdtTest1 {
    /*

    CREATE TYPE test.udt (
      id int,
      t1 int,
      t2 int,
      a2 int
    );
    CREATE TABLE test.u2 (
      id int PRIMARY KEY,
      u udt
    );
     */

    public static void main(String[] args) {
        try (CqlSession session = CqlSession.builder()
                .addContactPoints(Commons.getContactPoints())
                .build()) {
            UserDefinedType udtType = session
                    .getMetadata()
                    .getKeyspace("test")
                    .flatMap(ks -> ks.getUserDefinedType("udt"))
                    .orElseThrow(IllegalStateException::new);
            PreparedStatement preparedStatement = session.prepare(
                    "insert into test.u2(id, u) values(?, ?)");
            for (int i = 0; i < 5; i++) {
                BoundStatement boundStatement =
                        preparedStatement.bind(i, udtType.newValue(i, i, i, i));
                session.execute(boundStatement);
            }

        }
    }
}
