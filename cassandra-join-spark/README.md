This directory contains code that demonstrates joining of data in Cassandra with data in Kafka topic - typical task of enrichment of the streaming data with additional data.  There are 2 versions of the code:

* for Spark Structured Streaming (recommended to use)
* for Spark Streaming

## Setup

### Create table with information about companies.  

The following command will create a `stock_info` table in the `test` keyspace, and will fill it with information about companies, loaded from the CSV file:

```sh
cqlsh -f setup.cql [IP-of-Cassandra-node]
```

### Create a Kafka topic for stock data 

in this case, Kafka is running on the `localhost`:

```sh
ZOOKEEPER=localhost
REP_FACTOR=1
PARTITIONS=10
TOPIC_NAME=tickers-stream-json
kafka-topics --create --zookeeper ${ZOOKEEPER}:2181 --replication-factor $REP_FACTOR \
  --partitions $PARTITIONS --topic $TOPIC_NAME
```

### Compile the code

```sh
mvn package
```

it will generate uberjar in the `target/cassandra-join-spark-0.0.1-jar-with-dependencies.jar`.


## Produce data (for both use cases)

The producer code is mostly taken from the [DataStax example](https://github.com/DataStax-Examples/kafka-connector-sink-json), with some modifications specific for this demo:

1. Information about stocks is loaded into a Cassandra table, that will be used in joins
1. We send only stock ticker, date and price information to Kafka topic

Data could be produced using following command (we can specify how many stocks to use, and how many points generate per stock):

```sh
KAFKA_HOST=localhost
NUMBER_OF_STOCKS=10
POINTS_PER_STOCK=10
mvn exec:java -Dexec.mainClass=json.JsonProducer \
   -Dexec.args="$TOPIC_NAME $NUMBER_OF_STOCKS $POINTS_PER_STOCK ${KAFKA_HOST}:9092"
```

## Spark Structured Streaming

This Spark structured streaming job (source code is in the file `StockTickersJoinDataFrames`) reads the data from Kafka (symbol, price, and timestamp) and performs joins of this data with data in Cassandra table `test.stock_info` to retrieve details about every stock.  The code could be executed as following:

```sh
bin/spark-submit --class com.datastax.alexott.demos.streaming.StockTickersJoinDataFrames \
  --conf spark.sql.extensions=com.datastax.spark.connector.CassandraSparkExtensions \
  --conf spark.cassandra.connection.host=localhost \
  cassandra-join-spark-0.0.1-jar-with-dependencies.jar ${KAFKA_HOST}:9092 $TOPIC_NAME
```

Please note that we need to pass `--conf spark.sql.extensions=com.datastax.spark.connector.CassandraSparkExtensions` to enable so-called [Direct Join optimization](http://www.russellspitzer.com/2018/05/23/DSEDirectJoin/) that converts join on the primary/partition key, into individual requests, instead of reading all data from Cassandra.  This functionality did exist in DSE Analytics for a long time, and was open sourced in the [Spark Cassandra Connector 2.5.0](https://www.datastax.com/blog/2020/05/advanced-apache-cassandra-analytics-now-open-all).  With this optimization enabled we should see the string `Cassandra Direct Join` in the output of the `.explain`, like here:

```
== Physical Plan ==
*(1) Project [value#26, datetime#27, symbol#35, base_price#36, exchange#37, industry#38, name#39]
+- Cassandra Direct Join [symbol = ticker#31] test.stock_info - Reading (symbol, base_price, exchange, industry, name) Pushed {} 
   +- Project [jsontostructs(StructField(symbol,StringType,true), StructField(value,DoubleType,true), StructField(datetime,TimestampType,true), cast(value#8 as string), Some(Europe/Berlin)).symbol AS ticker#31, jsontostructs(StructField(symbol,StringType,true), StructField(value,DoubleType,true), StructField(datetime,TimestampType,true), cast(value#8 as string), Some(Europe/Berlin)).value AS value#26, jsontostructs(StructField(symbol,StringType,true), StructField(value,DoubleType,true), StructField(datetime,TimestampType,true), cast(value#8 as string), Some(Europe/Berlin)).datetime AS datetime#27]
      +- StreamingRelation kafka, [key#7, value#8, topic#9, partition#10, offset#11L, timestamp#12, timestampType#13]
```

And as streaming job will work, we can see that we get stock details together with price & timestamp received from Kafka:

```
+------------------+--------------------+------+----------+--------+--------------+--------------------+
|             value|            datetime|symbol|base_price|exchange|      industry|                name|
+------------------+--------------------+------+----------+--------+--------------+--------------------+
| 254.5442902345344|2020-07-14 14:03:...|  ADBE|     253.0|  NASDAQ|          TECH|       ADOBE SYSTEMS|
| 66.13761365408801|2020-07-14 14:03:...|   LNC|      66.0|    NYSE|    FINANCIALS|    LINCOLN NATIONAL|
| 37.18736354960266|2020-07-14 14:04:...|   AAL|      37.0|  NASDAQ|TRANSPORTATION|AMERICAN TRANSPOR...|
| 83.65862697664453|2020-07-14 14:04:...|    DD|      84.0|    NYSE|     CHEMICALS|              DUPONT|
|34.935756366943224|2020-07-14 14:04:...|   MOS|      35.0|    NYSE|     CHEMICALS|              MOSAIC|
|  26.9411285929182|2020-07-14 14:04:...|  FITB|      27.0|  NASDAQ|    FINANCIALS| FIFTH THIRD BANCORP|
|254.36293137800794|2020-07-14 14:05:...|  ADBE|     253.0|  NASDAQ|          TECH|       ADOBE SYSTEMS|
| 65.72493409297894|2020-07-14 14:05:...|   LNC|      66.0|    NYSE|    FINANCIALS|    LINCOLN NATIONAL|
|  37.1828416326448|2020-07-14 14:06:...|   AAL|      37.0|  NASDAQ|TRANSPORTATION|AMERICAN TRANSPOR...|
| 83.07024839492905|2020-07-14 14:06:...|    DD|      84.0|    NYSE|     CHEMICALS|              DUPONT|
| 35.11617636497214|2020-07-14 14:06:...|   MOS|      35.0|    NYSE|     CHEMICALS|              MOSAIC|
|27.036517140390004|2020-07-14 14:06:...|  FITB|      27.0|  NASDAQ|    FINANCIALS| FIFTH THIRD BANCORP|
|253.78857625605187|2020-07-14 14:06:...|  ADBE|     253.0|  NASDAQ|          TECH|       ADOBE SYSTEMS|
| 66.10635570353556|2020-07-14 14:07:...|   LNC|      66.0|    NYSE|    FINANCIALS|    LINCOLN NATIONAL|
| 36.92264970358925|2020-07-14 14:07:...|   AAL|      37.0|  NASDAQ|TRANSPORTATION|AMERICAN TRANSPOR...|
| 82.36354957721413|2020-07-14 14:08:...|    DD|      84.0|    NYSE|     CHEMICALS|              DUPONT|
| 35.27039845055757|2020-07-14 14:08:...|   MOS|      35.0|    NYSE|     CHEMICALS|              MOSAIC|
|26.823545795187048|2020-07-14 14:08:...|  FITB|      27.0|  NASDAQ|    FINANCIALS| FIFTH THIRD BANCORP|
| 256.0310345916542|2020-07-14 14:08:...|  ADBE|     253.0|  NASDAQ|          TECH|       ADOBE SYSTEMS|
| 66.02753343728398|2020-07-14 14:08:...|   LNC|      66.0|    NYSE|    FINANCIALS|    LINCOLN NATIONAL|
+------------------+--------------------+------+----------+--------+--------------+--------------------+
```

## Spark Streaming

This job is very similar to the previous, but uses  Spark streaming (source code is in the file `StockTickersJoinRDD`). The code could be executed as following:

```
bin/spark-submit --class com.datastax.alexott.demos.streaming.StockTickersJoinRDD \
  --conf spark.cassandra.connection.host=localhost \
  target/cassandra-join-spark-0.0.1-jar-with-dependencies.jar ${KAFKA_HOST}:9092 $TOPIC_NAME
```

This job first parses JSON from topic, then join parsed data with data in Cassandra (via `leftJoinWithCassandraTable`), and generates an instance of the `JoinedData` case class that contains both data from Kafka topic, and from Cassandra.  Right now, this information is just put onto console, like this:

```
There are 0 stock tickers without information in Cassandra
There are 20 stock tickers with information in Cassandra
...
JoinedData(ESND,NASDAQ,ESSENDANT,WHOLESALERS,13.0,2020-07-14T16:19:19.588Z,13.483634952551117)
JoinedData(SWK,NYSE,STANLEY BLACK & DECKER,HOUSEHOLD PRODUCTS,128.0,2020-07-14T16:19:23.588Z,121.58327281753643)
JoinedData(BLK,NYSE,BLACKROCK,FINANCIALS,424.0,2020-07-14T16:19:24.588Z,394.7030616365362)
...
```
