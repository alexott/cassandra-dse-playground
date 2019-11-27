package com.datastax.alexott.demos.metrics;

import com.codahale.metrics.JmxReporter;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metrics;
import com.datastax.driver.core.Session;

public class Metrics1 {

    public static void main(String[] args) throws InterruptedException {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        try (Cluster cluster = Cluster.builder().addContactPoint(server).build();
             Session session = cluster.connect()) {

            for(int i = 0; i < 3000; i++) {
                for(int j = 0; j < 1000; j++) {
                    session.executeAsync(String.format("insert into test.abc (id, t1, t2) values (%d, 't1-%d', 't2-%d');",
                            i+j, i, j));
                }
                Metrics metrics = cluster.getMetrics();
                System.out.println("Doing iteration " + (i+1));
                System.out.println("BlockingExecutorQueueDepth: " + metrics.getBlockingExecutorQueueDepth().getValue() );
                System.out.println("BytesReceived: " + metrics.getBytesReceived().getCount() );
                System.out.println("BytesSent: " + metrics.getBytesSent().getCount() );
                System.out.println("ConnectedToHosts: " + metrics.getConnectedToHosts().getValue() );
//                System.out.println("ErrorMetrics: " + metrics.getErrorMetrics(). );
                System.out.println("ExecutorQueueDepth: " + metrics.getExecutorQueueDepth().getValue() );
                System.out.println("InFlightRequests: " + metrics.getInFlightRequests().getValue() );
                System.out.println("KnownHosts: " + metrics.getKnownHosts().getValue() );
                System.out.println("OpenConnections: " + metrics.getOpenConnections().getValue() );
                System.out.println("ReconnectionSchedulerQueueSize: " + metrics.getReconnectionSchedulerQueueSize().getValue() );
                System.out.println("RequestsTimer: " + metrics.getRequestsTimer().getMeanRate() );
                System.out.println("TrashedConnections: " + metrics.getTrashedConnections().getValue() );

                Thread.sleep(5000);
            }
        }
    }

}
