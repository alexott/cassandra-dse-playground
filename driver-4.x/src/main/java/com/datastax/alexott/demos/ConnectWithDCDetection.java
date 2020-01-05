package com.datastax.alexott.demos;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ConnectWithDCDetection {

  public static void main(String[] args) {
    String contactPointsStr = System.getProperty("contactPoints", "");
    if (contactPointsStr.isEmpty()) {
      System.err.println("Please pass Cassandra contact points as Java system property with name 'contactPoints'");
      System.exit(1);
    }

    // this is not really necessary, because of the existence of the DcInferringLoadBalancingPolicy implemented as
    // part of JAVA-2459
    String[] contactPoints = contactPointsStr.split(",");
    String dcName = DCDetectingLBPolicy.detectDcName(contactPoints);
    System.out.println("Detected DC Name: '" + dcName + "'");

    try(CqlSession session = CqlSession.builder()
            .addContactPoints(Arrays.stream(contactPoints)
                    .map(x -> new InetSocketAddress(x, 9042)).collect(Collectors.toList()))
            .withLocalDatacenter(dcName)
            .build()) {
      ResultSet rs = session.execute("select data_center, host_id from system.peers");
      for (Row row: rs) {
        System.out.println(String.format("Host ID: %s, DC: %s", row.getUuid("host_id"), row.getString("data_center")));
      }
    }
  }
}
