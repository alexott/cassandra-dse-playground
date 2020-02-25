package com.datastax.alexott.demos;

import com.datastax.oss.driver.api.core.CqlSession;

import java.util.Map;
import java.util.TreeMap;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.*;

public class CreateKeyspacesInferTopology {
    private static final String KS_NAME = "my_super_ks";
    private static final int MAX_RF = 3;

    public static void main(String[] args) {
        try (CqlSession session = CqlSession.builder()
                .addContactPoints(Commons.getContactPoints())
                .build()) {
            Commons.executeDDL(session,  dropKeyspace(KS_NAME).ifExists().build());
            Map<String, Integer> rfPerDC = new TreeMap<>();
            for (Map.Entry<String, Integer> e: Commons.getDataCenters(session).entrySet()) {
                rfPerDC.put(e.getKey(), Math.min(e.getValue(), MAX_RF));
            }

            Commons.executeDDL(session,
                    createKeyspace(KS_NAME).ifNotExists()
                    .withNetworkTopologyStrategy(rfPerDC).build());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
