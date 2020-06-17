package com.datastax.alexott.demos;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.internal.mapper.processor.util.generation.PropertyType;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Commons {
    private static final int CQL_PORT = Integer.parseInt(System.getProperty("cqlPort", "9042"));
    public static final int WAIT_TIME = 500;
    public static final int WAIT_CYCLES = 100;

    public static Collection<InetSocketAddress> getContactPoints(final String contactPoints) {
        return Arrays.stream(contactPoints.split(","))
                .map(host -> InetSocketAddress.createUnresolved(host, CQL_PORT))
                .collect(Collectors.toList());

    }


    public static Collection<InetSocketAddress> getContactPoints() {
        return getContactPoints(System.getProperty("contactPoints", "localhost"));

    }

    public static void executeDDL(CqlSession session, SimpleStatement statement) throws InterruptedException {
        ResultSet rs = session.execute(statement);
        if (!rs.getExecutionInfo().isSchemaInAgreement()) {
            int cnt = 0;
            while(!session.checkSchemaAgreement()) {
                Thread.sleep(WAIT_TIME);
                cnt++;
                if (cnt > WAIT_CYCLES) {
                    throw new RuntimeException(String.format("Can't reach schema agreement after %d seconds",
                            WAIT_CYCLES*WAIT_CYCLES/1000));
                }
            }
        }

    }

    public static void executeDDL(CqlSession session, String query) throws InterruptedException {
        executeDDL(session, SimpleStatement.newInstance(query));

    }

    public static Map<String, Integer> getDataCenters(CqlSession session) {
        Map<String, Long> m = session.getMetadata().getNodes().values().stream().map(Node::getDatacenter)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<String, Integer> converted = new TreeMap<>();
        for (Map.Entry<String, Long> e: m.entrySet()) {
            converted.put(e.getKey(), e.getValue().intValue());
        }
        return converted;
    }
}
