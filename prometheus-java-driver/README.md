This directory contains an example of how we can export metrics from DataStax Java driver
4.x to Prometheus via [Prometheus Java Client](https://github.com/prometheus/client_java).

Exporting of [Java driver metrics](https://docs.datastax.com/en/developer/java-driver/4.3/manual/core/metrics/) is simple, we just need to add following lines:

```java
MetricRegistry registry = session.getMetrics()
        .orElseThrow(() -> new IllegalStateException("Metrics are disabled"))
        .getRegistry();
CollectorRegistry.defaultRegistry.register(new DropwizardExports(registry));
```

and then expose metrics to Prometheus by specific implementation - this example uses
Prometheus's `HTTPServer`, running on the port 9095 (overridable via `prometheusPort` Java
property).

Run example with following command:

```sh
mvn clean compile exec:java -Dexec.mainClass="com.datastax.alexott.demos.MetricsWithPrometheus" \
    -DcontactPoint=10.101.34.241 -DdcName=dc_datastax
```

You need to pass contact point & data center name as Java properties (`contactPoint` and
`dcName` correspondingly).
