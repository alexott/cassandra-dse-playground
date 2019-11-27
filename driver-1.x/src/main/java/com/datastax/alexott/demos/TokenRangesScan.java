package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Token;
import com.datastax.driver.core.TokenRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// create table test.range_scan(id bigint, col1 int, col2 bigint, primary key(id, col1));

public class TokenRangesScan {
    public static void main(String[] args) {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        Cluster cluster = Cluster.builder()
                .addContactPoint(server)
                .build();
        Session session = cluster.connect();

        Metadata metadata = cluster.getMetadata();
        List<TokenRange> ranges = new ArrayList(metadata.getTokenRanges());
        Collections.sort(ranges);
        System.out.println("Processing " + (ranges.size() + 1) + " token ranges...");

        Token minToken = ranges.get(0).getStart();
        String baseQuery = "SELECT id, col1 FROM test.range_scan WHERE ";
        Map<String, Token> queries = new HashMap<>();
        // generate queries for every range
        for (int i = 0; i < ranges.size(); i++) {
            TokenRange range = ranges.get(i);
            Token rangeStart = range.getStart();
            Token rangeEnd = range.getEnd();
            System.out.println("i=" + i + ", start=" + rangeStart + ", end=" + rangeEnd);
            if (rangeStart.equals(rangeEnd)) {
                queries.put(baseQuery + "token(id) >= " + minToken, minToken);
            } else if (i == 0) {
                queries.put(baseQuery + "token(id) <= " + minToken, minToken);
                queries.put(baseQuery + "token(id) > " + rangeStart + " AND token(id) <= " + rangeEnd, rangeEnd);
            } else if (rangeEnd.equals(minToken)) {
                queries.put(baseQuery + "token(id) > " + rangeStart, rangeEnd);
            } else {
                queries.put(baseQuery + "token(id) > " + rangeStart + " AND token(id) <= " + rangeEnd, rangeEnd);
            }
        }

        // Note: It could be speedup by using async queries, but for illustration it's ok
        long rowCount = 0;
        // That is needed if OSS driver is used...
        // ProtocolVersion protocolVersion = cluster.getConfiguration().getProtocolOptions().getProtocolVersion();
        for (Map.Entry<String, Token> entry : queries.entrySet()) {
            SimpleStatement statement = new SimpleStatement(entry.getKey());
            // !!! This function is available only in Java DSE driver, not OSS !!!
            statement.setRoutingToken(entry.getValue());
            // for OSS driver, following code should be used
            // statement.setRoutingKey(entry.getValue().serialize(protocolVersion));
            ResultSet rs = session.execute(statement);
            long rangeCount = 0;
            for (Row row : rs) {
                rangeCount++;
            }
            System.out.println("Processed range ending at " + entry.getValue() + ". Row count: "
                    + rangeCount + ", query: \"" + entry.getKey() + "\"");
            rowCount += rangeCount;
        }
        System.out.println("Total row count: " + rowCount);
    }
}
