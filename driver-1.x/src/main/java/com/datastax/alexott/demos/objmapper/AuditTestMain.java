package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.WhiteListPolicy;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;

public class AuditTestMain {
    public static void main(String[] args) {
        LoadBalancingPolicy lbpolicy = new WhiteListPolicy(new RoundRobinPolicy(),
                Collections.singletonList(new InetSocketAddress("10.200.180.207", 9042)));
        try (Cluster cluster = Cluster.builder().addContactPoint("10.200.180.207")
                .withLoadBalancingPolicy(lbpolicy)
                .build();
             Session session = cluster.connect()) {

            MappingManager manager = new MappingManager(session);
            Mapper<AuditTestTable> mapper = manager.mapper(AuditTestTable.class);

            Map<Integer, String> m = Maps.newHashMap();
            m.put(1, "m 1");
            m.put(2, "m 2");
            mapper.save(new AuditTestTable(2, new AuditTestType(2, "test 2"),
                    Sets.newHashSet("s 1", " s 2"), Lists.newArrayList("l 1", "l 2"),
                    m));

            System.out.println(mapper.get(2));
        }
    }

}
