package com.datastax.alexott.demos;

import com.codahale.metrics.MetricRegistry;
import com.datastax.oss.driver.api.core.CqlSession;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;

public class MetricsWithPrometheus {
    public static void main(String[] args) throws InterruptedException {
        String contactPoint = System.getProperty("contactPoint", "127.0.0.1");
        // init default prometheus stuff
        DefaultExports.initialize();
        // setup Prometheus HTTP server
        Optional<HTTPServer> prometheusServer = Optional.empty();
        try {
            prometheusServer = Optional.of(new HTTPServer(Integer.getInteger("prometheusPort", 9095)));
        } catch (IOException e) {
            System.out.println("Exception when creating HTTP server for Prometheus: " + e.getMessage());
        }

        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(contactPoint, 9042))
                .withLocalDatacenter(System.getProperty("dcName"))
                .build()) {

            MetricRegistry registry = session.getMetrics()
                    .orElseThrow(() -> new IllegalStateException("Metrics are disabled"))
                    .getRegistry();
            CollectorRegistry.defaultRegistry.register(new DropwizardExports(registry));

            session.execute("create keyspace if not exists test with replication = {'class': 'SimpleStrategy', 'replication_factor': 1};");
            session.execute("create table if not exists test.abc (id int, t1 text, t2 text, primary key (id, t1));");
            session.execute("truncate test.abc;");

            for(int i = 0; i < 3000; i++) {
                for(int j = 0; j < 1000; j++) {
                    session.executeAsync(String.format("insert into test.abc (id, t1, t2) values (%d, 't1-%d', 't2-%d');",
                            i+j, i, j));
                }
                Thread.sleep(5000);
            }
        }
        prometheusServer.ifPresent(HTTPServer::stop);
    }

}
