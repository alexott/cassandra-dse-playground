This repository contains different code samples that are related to the usage of DataStax
Enterprise (DSE).  This repository is successor of
[dse-java-playground](https://github.com/alexott/dse-java-playground) repository, but it's
restructured to be more modular, so we can use examples with different versions of Java
driver, separate artifact for Spark code, etc.

The code is organized as following:

* `driver-1.x` - samples that use DSE Java driver 1.x (mostly compatible with DataStax
  Java driver 3.x);
* `driver-4.x` - samples that use DataStax Java driver 2.x (mostly compatible with DSE
  Java driver 2.x);
* `spark-dse` - samples that use DSE Analytics (should be mostly compatible with OSS
  Spark, but there are some differences, like, support for DSE Direct Join for data
  frames);
* `spark-oss` - samples that demonstrate the use of Spark with OSS Spark Cassandra
  Connector, version < 2.5.0
* `scc-2.5` - samples that  demonstrate the use of Spark with OSS Spark Cassandra
  Connector, version = 2.5.x
* `scc-3.0` - samples that  demonstrate the use of Spark with OSS Spark Cassandra
  Connector, version >= 3.0


