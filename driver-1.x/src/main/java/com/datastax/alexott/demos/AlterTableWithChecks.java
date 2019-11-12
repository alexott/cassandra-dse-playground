package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.WhiteListPolicy;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlterTableWithChecks {
    static final int DSE_PORT = 9042;

    private static Pattern DDL_PATTERN = Pattern.compile("^(create|alter|drop)\\s+.*",
            Pattern.CASE_INSENSITIVE);
    private static Pattern COL_EXISTS = Pattern.compile("Invalid column name .+ because it conflicts with an existing column",
            Pattern.CASE_INSENSITIVE);
    private static Pattern COL_DOESNT_EXIST = Pattern.compile("Column .+ was not found in table ",
            Pattern.CASE_INSENSITIVE);
    private static Pattern COL_DOESNT_EXIST_RENAME = Pattern.compile("Cannot rename unknown column .+ in ",
            Pattern.CASE_INSENSITIVE);
    private static Pattern COL_EXISTS_RENAME = Pattern.compile("Cannot rename column .+ to .+ in keyspace .+; another column of that name already exist",
            Pattern.CASE_INSENSITIVE);

    public static ResultSet execute(Session session, String stmt) throws InterruptedException {
        Matcher matcher = DDL_PATTERN.matcher(stmt);
        if (matcher.matches()) {
            System.out.println("Executing DDL: " + stmt);
            Metadata metadata = session.getCluster().getMetadata();
            ResultSet rs = null;
            try {
                rs = session.execute(stmt);
            } catch (InvalidQueryException ex) {
                String msg = ex.getMessage();
                // THIS IS NOT RELIABLE!
                if (!(COL_EXISTS.matcher(msg).find() ||
                        COL_DOESNT_EXIST.matcher(msg).find() ||
                        COL_DOESNT_EXIST_RENAME.matcher(msg).find() ||
                        COL_EXISTS_RENAME.matcher(msg).find())) {
                    throw ex;
                }
                System.out.println("Skipping error: " + msg + ", for query: " + stmt);
            }
            if (rs != null && !rs.getExecutionInfo().isSchemaInAgreement()) {
                while (!metadata.checkSchemaAgreement()) {
                    System.out.println("Schema isn't in agreement, sleep 1 second...");
                    Thread.sleep(1000);
                }
            }
            return rs;
        } else {
            return session.execute(stmt);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String server = System.getProperty("contactPoint", "127.0.0.1");

        try(Cluster cluster = Cluster.builder().addContactPoint(server).build();
        Session session = cluster.connect()) {

            String[] commands = {"drop keyspace if exists whtest;",
                    "create keyspace whtest WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};",
                    "create table whtest.whtest(id int primary key, t text);",
                    "alter table whtest.whtest add t int;",
                    "alter table whtest.whtest drop abcd;",
                    "alter table whtest.whtest rename abcd to abc;",
                    "alter table whtest.whtest rename id to t;",
                    "create table whtest.whtest2(id int primary key, t text);"};

            Metadata metadata = cluster.getMetadata();
            for (int i = 0; i < commands.length; i++) {
                System.out.println("Executing '" + commands[i] + "'");
                execute(session, commands[i]);
            }
            // just to be sure, and to show that it could be done via Metadata as well
            for (int i = 0; i < 5; i++) {
                session.execute(String.format("insert into whtest.whtest(id, t) values(%d, 'test %d');", i, i));
            }

            ResultSet rs = session.execute("select count(*) from whtest.whtest;");
            Row row = rs.one();
            System.out.println("There are " + row.getLong(0) + " rows in the whtest table...");

        }
    }

}
