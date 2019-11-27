package com.datastax.alexott.demos;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;

public class FRvsNonFRUDTMapping {
    public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        try(Cluster cluster = Cluster.builder().addContactPoint(server).build();
            Session session = cluster.connect("test")) {
            MappingManager manager = new MappingManager(session);
            Mapper<UDTTestTableNonFR> mapperNonFR = manager.mapper(UDTTestTableNonFR.class);
            Mapper<UDTTestTableFR> mapperFR = manager.mapper(UDTTestTableFR.class);

            UDTTestTableFR t1 = mapperFR.get(1, 1);
            System.out.print("t1=" + t1);
            UDTTestTableNonFR t2 = mapperNonFR.get(1, 1);
            System.out.print("t2=" + t2);

            t1.setId(2);
            t2.setId(2);

            mapperFR.save(t1);
            mapperNonFR.save(t2);

            ResultSet rs = session.execute("select * from test.udt_test where id = 1 and cid = 1");
            Row row = rs.one();
            UDTValue udt = row.getUDTValue("udt");
            System.out.println("udt=" + udt.getInt("id") + ", " + udt.getString("t"));
            PreparedStatement updateNonFr = session.prepare("update test.udt_test set udt = ? where id = 1 and cid = 1");
            udt.setInt("id", udt.getInt("id") + 1);
            session.execute(updateNonFr.bind(udt));

            rs = session.execute("select * from test.udt_test_fr where id = 1 and cid = 1");
            row = rs.one();
            udt = row.getUDTValue("udt");
            System.out.println("udt=" + udt.getInt("id") + ", " + udt.getString("t"));
            PreparedStatement updateFr = session.prepare("update test.udt_test_fr set udt = ? where id = 1 and cid = 1");
            udt.setInt("id", udt.getInt("id") + 1);
            session.execute(updateFr.bind(udt));
        }
    }
}
