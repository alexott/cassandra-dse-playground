package com.datastax.alexott.demos;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;

public class Test4 {
    public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        try(Cluster cluster = Cluster.builder().addContactPoint(server).build();
        Session session = cluster.connect()) {
            MappingManager manager = new MappingManager(session);
            Mapper<Test4Data> mapper = manager.mapper(Test4Data.class);

            for (int i = 0; i < 2; i++) {
                BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.UNLOGGED);
                for (int j = 0; j < 5; j++) {
                    Statement statement = mapper.saveQuery(new Test4Data(i, j, "t " + i + "," + j));
                    System.out.println(statement.getClass());
                    batchStatement.add(statement);
                }
                session.execute(batchStatement);
            }

            Test4Data test4Data = mapper.get(0, 1);
            System.out.println(test4Data);
        }
    }

}
