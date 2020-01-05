package com.datastax.alexott.demos;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.loadbalancing.LoadBalancingPolicy;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.api.core.session.Session;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is just an exercise, before discovering of the JAVA-2459 that adds DcInferringLoadBalancingPolicy - use it instead
 */
public class DCDetectingLBPolicy implements LoadBalancingPolicy {
  Map<UUID, Node> nodesMap;

  public DCDetectingLBPolicy(@NonNull DriverContext context, @NonNull String profileName) {
  }

  @Override
  public void init(@NonNull Map<UUID, Node> map, @NonNull DistanceReporter distanceReporter) {
    nodesMap = map;
  }

  public String getDCName(String... contactPoints) {
    if (nodesMap == null) {
      throw new RuntimeException("DCDetectingLBPolicy wasn't initialized yet!");
    }
    Map<String, String> nodesToDcs = new HashMap<>(nodesMap.size());
    for (Node node: nodesMap.values()) {
      if (node.getBroadcastRpcAddress().isPresent()) {
        InetSocketAddress address = node.getBroadcastRpcAddress().get();
        nodesToDcs.put(address.getHostString(), node.getDatacenter());
        nodesToDcs.put(address.getAddress().getHostAddress(), node.getDatacenter());
      } else if (node.getBroadcastAddress().isPresent()) {
        InetSocketAddress address = node.getBroadcastAddress().get();
        nodesToDcs.put(address.getHostString(), node.getDatacenter());
        nodesToDcs.put(address.getAddress().getHostAddress(), node.getDatacenter());
      } else if (node.getListenAddress().isPresent()) {
        InetSocketAddress address = node.getListenAddress().get();
        nodesToDcs.put(address.getHostString(), node.getDatacenter());
        nodesToDcs.put(address.getAddress().getHostAddress(), node.getDatacenter());
      } else {
        throw new RuntimeException("Can't get hostname or IP for a node " + node.getHostId());
      }
    }

    Set<String> dcs = new TreeSet<>();
    for (String cp: contactPoints) {
      String dc = nodesToDcs.get(cp);
      if (dc != null) {
        dcs.add(dc);
      }
    }
    if (dcs.size() > 1) {
      throw new RuntimeException("Contact points belong to different DCs: " + dcs.stream().collect(Collectors.joining(",")));
    }
    if (dcs.isEmpty()) {
      throw new RuntimeException("Can't detect DC from the contact points provided");
    }

    return dcs.iterator().next();
  }

  public String getDCName(final String contactPoints) {
    return getDCName(contactPoints.split(","));
  }

  @NonNull
  @Override
  public Queue<Node> newQueryPlan(@Nullable Request request, @Nullable Session session) {
    return null;
  }

  @Override
  public void onAdd(@NonNull Node node) {
  }

  @Override
  public void onUp(@NonNull Node node) {
  }

  @Override
  public void onDown(@NonNull Node node) {
  }

  @Override
  public void onRemove(@NonNull Node node) {
  }

  @Override
  public void close() {
  }

  public static String detectDcName(String... contactPoints) {
    ProgrammaticDriverConfigLoaderBuilder configBuilder = DriverConfigLoader.programmaticBuilder();
    configBuilder.withClass(DefaultDriverOption.LOAD_BALANCING_POLICY_CLASS, DCDetectingLBPolicy.class);

    DriverConfigLoader loader = configBuilder.endProfile().build();
    try(CqlSession session = CqlSession.builder()
            .addContactPoints(Arrays.stream(contactPoints)
                    .map(x -> new InetSocketAddress(x, 9042)).collect(Collectors.toList()))
            .withConfigLoader(loader)
            .build()) {

      DCDetectingLBPolicy lbp = (DCDetectingLBPolicy) session.getContext().getLoadBalancingPolicies().values().iterator().next();

      return lbp.getDCName(contactPoints);
    }
  }

  public static String detectDcName(String contactPoints) {
    return detectDcName(contactPoints.split(","));
  }
}
