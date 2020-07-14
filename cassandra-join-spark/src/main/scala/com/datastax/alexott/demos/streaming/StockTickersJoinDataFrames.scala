package com.datastax.alexott.demos.streaming

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object StockTickersJoinDataFrames {

  /*
   * Code need to be executed with
   * --conf spark.sql.extensions=com.datastax.spark.connector.CassandraSparkExtensions
   * to enable Direct Join optimization
   */

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: StockTickersJoinDataFrames kafka-servers topic-name")
      System.exit(1)
    }
    val kafkaServes = args(0)
    val topicName = args(1)
    val sc = new SparkContext()
    val spark = SparkSession.builder()
      .config(sc.getConf)
      .getOrCreate()
    import spark.implicits._

    val schema = StructType(List(
      StructField("symbol", StringType),
      StructField("value", DoubleType),
      StructField("datetime", TimestampType)
    ))

    val streamingInputDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaServes)
      .option("subscribe", topicName)
      .load()

    val parsed = streamingInputDF.selectExpr("CAST(value AS STRING)")
      .select(from_json($"value", schema).as("stock"))
      .select("stock.*")
      .withColumnRenamed("symbol", "ticker")

    // get the dataset from Cassandra
    // if it's "stable" then we can also cache it to speedup processing
    val cassandra = spark.read
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "stock_info", "keyspace" -> "test"))
      .load

    // we can use left join to detect what data is incorrect - if we don't have some data in the
    // Cassandra, then symbol field will be null, so we can detect such entries, and do something with that
    // we can omit the joinType parameter, in that case, we'll process only data that are in the Cassandra
    val joined = parsed.join(cassandra, cassandra("symbol") === parsed("ticker"), "left")
        .drop("ticker")

    joined.explain
    joined.printSchema

    val query = joined.writeStream
          .outputMode("update")
          .format("console")
          .start()

    query.awaitTermination()
    //Thread.sleep(10000)
    //query.stop()

  }
}
