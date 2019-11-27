package com.datastax.alexott.demos;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.BuiltStatement;
import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;

import java.util.Arrays;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

public class QBuilder {

    public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        Cluster cluster = Cluster.builder().addContactPoint(server).build();
        Session session = cluster.connect();

        BuiltStatement selectAll = QueryBuilder.select().all().from("test", "test").limit(10);
        ResultSet rs = session.execute(selectAll);
        for (Row row: rs) {
            System.out.println(row);
        }

        BuiltStatement selectAll2 = QueryBuilder.select().from("test", "test").limit(10);
        rs = session.execute(selectAll2);
        for (Row row: rs) {
            System.out.println(row);
        }

        BuiltStatement selectOne = QueryBuilder.select().from("test")
                .where(QueryBuilder.eq("id", 1)).limit(1).allowFiltering()
                .perPartitionLimit(1).orderBy(desc("id"));
        rs = session.execute(selectOne);
        for (Row row: rs) {
            System.out.println(row);
        }

        BuiltStatement selectOne2 = QueryBuilder.select().from("test", "test")
                .where(eq("id", bindMarker()));
        PreparedStatement preparedStatement = session.prepare(selectOne2);
        session.execute(preparedStatement.bind(1));

        BuiltStatement selectSome = QueryBuilder.select().from("test", "test")
                .where(in("id", 1, 2)).and(in("id", 1, 2));
        rs = session.execute(selectSome);
        for (Row row: rs) {
            System.out.println(row);
        }

        Statement updateStatement = QueryBuilder.update("test").with(set("t", "test 1"))
                .and(set("x", 10)).where(eq("id", 1));

        QueryBuilder.select().cast(column("id"), DataType.varchar());

        BuiltStatement ttlAndWriteTime = QueryBuilder.select().column("id").column("t")
                .ttl("t").as("id_ttl").writeTime("t")
                .from("test", "test");
        rs = session.execute(ttlAndWriteTime);
        for (Row row: rs) {
            System.out.println(row);
        }

        BuiltStatement sum = QueryBuilder.select().fcall("ttl", column("t")).as("sum_id")
                .from("test", "test");
        rs = session.execute(sum);
        for (Row row: rs) {
            System.out.println(row);
        }

        BuiltStatement deleteStatemet = QueryBuilder.delete().from("test", "test")
                .where(eq("id", "1")).and(eq("txt", "test"));
        QueryBuilder.delete("t").from("test");

        QueryBuilder.insertInto("test").values(Arrays.asList("id", "t"), Arrays.asList(4, "test 4"));
        QueryBuilder.insertInto("test").value("id", 4).ifNotExists();

        QueryBuilder.insertInto("test").json("{\"id\":4, \"t\":\"test 4\"}").using(ttl(10)).and(timestamp(1000));


        QueryBuilder.batch(selectAll2, selectOne2);


        session.close();
        cluster.close();
    }
}
