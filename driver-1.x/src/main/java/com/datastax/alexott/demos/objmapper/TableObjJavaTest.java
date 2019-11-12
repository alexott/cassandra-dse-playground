package com.datastax.alexott.demos.objmapper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

public class TableObjJavaTest {
    public static void main(String[] args) {
        try (Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
             Session session = cluster.connect()) {
            MappingManager manager = new MappingManager(session);
            Mapper<TableObjJava> mapper = manager.mapper(TableObjJava.class);

            TableObjJava obj = mapper.get(1);
            System.out.println("Obj(1)=" + obj);
        }
    }

}
