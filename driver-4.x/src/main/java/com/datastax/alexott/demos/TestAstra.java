package com.datastax.alexott.demos;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.nio.file.Paths;

public class TestAstra {
    public static void main(String[] args) {
        try (CqlSession session = CqlSession.builder()
                .withCloudSecureConnectBundle(Paths.get("/Users/ott/Downloads/secure-connect-test.zip"))
                .withAuthCredentials("test", "...")
                .build()) {
            ResultSet rs = session.execute("select id,v from test.t1");
            for (Row row: rs) {
                System.out.println("id=" + row.getInt("id") + ", v=" + row.getInt("v"));
            }
        }
    }
}
