package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.WhiteListPolicy;

import javax.xml.transform.Result;
import java.net.InetSocketAddress;
import java.util.Collections;

public class WhiteListPolicyExample {
    static final int DSE_PORT = 9042;

    public static void main(String[] args) throws InterruptedException {
        String server = System.getProperty("contactPoint", "127.0.0.1");

        // Notice, that in the driver 3.6, DSE driver 1.7, there is a static function 'ofHosts'
        // for easier construction of white list policies
        LoadBalancingPolicy lbpolicy = new WhiteListPolicy(new RoundRobinPolicy(),
                Collections.singletonList(new InetSocketAddress(server, DSE_PORT)));
        Cluster.Builder builder = Cluster.builder().addContactPoint(server)
                .withLoadBalancingPolicy(lbpolicy);

        Cluster cluster = builder.build();
        Session session = cluster.connect();

        String[] commands = {"drop keyspace if exists whtest;",
                "create keyspace whtest WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};",
                "create table whtest.whtest(id int primary key, t text);",
                "create table whtest.whtest2(id int primary key, t text);"};

        Metadata metadata = cluster.getMetadata();
        for (int i = 0; i < commands.length; i++) {
            System.out.println("Executing '" + commands[i] + "'");
            ResultSet rs = session.execute(commands[i]);
            if (!rs.getExecutionInfo().isSchemaInAgreement()) {
                while (!metadata.checkSchemaAgreement()) {
                    System.out.println("Schema isn't in agreement, sleep 1 second...");
                    Thread.sleep(1000);
                }
            }
        }
        // just to be sure, and to show that it could be done via Metadata as well
        for (int i = 0; i < 5; i++) {
            session.execute(String.format("insert into whtest.whtest(id, t) values(%d, 'test %d');",i, i));
        }

        ResultSet rs = session.execute("select count(*) from whtest.whtest;");
        Row row = rs.one();
        System.out.println("There are " + row.getLong(0) + " rows in the whtest table...");

        session.close();
        cluster.close();
    }

}
