package com.datastax.alexott.demos.objmapper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;

public class TableObjAccessorTest {
    public static void main(String[] args) {
        try (Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
             Session session = cluster.connect()) {
            MappingManager manager = new MappingManager(session);
            TableObjAccessor accessor = manager.createAccessor(TableObjAccessor.class);
            Result<TableObjectClustered> objs = accessor.getByPartKey(0, 1);
            for (TableObjectClustered obj: objs) {
                System.out.println("Obj=" + obj);
            }
            accessor.deleteByPartKey(0,0);
        }

    }
}
