package com.datastax.alexott.demos;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.dse.driver.api.core.data.geometry.Point;

public class TestPointType {
    public static void main(String[] args) {
        try (CqlSession session = CqlSession.builder()
                .addContactPoints(Commons.getContactPoints("10.101.34.176,10.101.34.94"))
                .build()) {
            ResultSet rs = session.execute("select point from test.gen_events1");
            for (Row row: rs) {
                Point point = row.get("point", Point.class);
                if (point != null)
                    System.out.println("point = " + point);
            }
        }

    }
}
