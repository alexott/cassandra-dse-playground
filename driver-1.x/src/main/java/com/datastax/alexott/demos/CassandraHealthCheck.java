package com.datastax.alexott.demos;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metrics;
import com.datastax.driver.core.Session;

import java.util.Set;
import java.util.stream.Collectors;

public class CassandraHealthCheck {

    static boolean clusterCheckMetrics(Cluster cluster) {
        Set<Host> hosts = cluster.getMetadata().getAllHosts();
        Metrics metrics = cluster.getMetrics();
        return metrics.getConnectedToHosts().getValue() > (hosts.size() /2);
    }

    static boolean clusterCheckMetrics(Cluster cluster, String dcName) {
        Set<Host> hosts = cluster.getMetadata().getAllHosts()
                .stream().filter(h -> h.getDatacenter().equals(dcName))
                .collect(Collectors.toSet());
        Metrics metrics = cluster.getMetrics();
        return metrics.getConnectedToHosts().getValue() > (hosts.size() /2);
    }

    static boolean clusterCheckMetadata(Cluster cluster, String dcName) {
        Set<Host> hosts = cluster.getMetadata().getAllHosts()
                .stream().filter(h -> h.getDatacenter().equals(dcName))
                .collect(Collectors.toSet());
        Set<Host> aliveHosts = hosts.stream()
                .filter(h -> h.isUp())
                .collect(Collectors.toSet());
        return aliveHosts.size() > (hosts.size() /2);
    }

    static boolean clusterCheckMetadata(Cluster cluster) {
        Set<Host> hosts = cluster.getMetadata().getAllHosts();
        Set<Host> aliveHosts = hosts.stream()
                .filter(h -> h.isUp())
                .collect(Collectors.toSet());
        return aliveHosts.size() > (hosts.size() /2);
    }

    public static void main(String[] args) throws InterruptedException {
        String server = System.getProperty("contactPoint", "127.0.0.1");
        String dcName = System.getProperty("dcName", "");
        try (Cluster cluster = Cluster.builder().addContactPoint(server).build();
             Session session = cluster.connect()) {

            while(true) {
                System.out.println("Metrics,  global. Is DSE Alive? " + clusterCheckMetrics(cluster));
                System.out.println("Metadata, global. Is DSE Alive? " + clusterCheckMetadata(cluster));
                if (!dcName.isEmpty()) {
                    System.out.println("Metrics,  with DC. Is DSE Alive? " + clusterCheckMetrics(cluster, dcName));
                    System.out.println("Metadata, with DC. Is DSE Alive? " + clusterCheckMetadata(cluster, dcName));
                }
                Thread.sleep(10000);
            }

        }
    }
}
