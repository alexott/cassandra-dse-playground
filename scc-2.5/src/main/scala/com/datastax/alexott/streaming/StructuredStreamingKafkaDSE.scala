package com.datastax.alexott.streaming

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types._
import scala.io.Source

object StructuredStreamingKafkaDSE {

  def main(args: Array[String]): Unit = {
    val sc = new SparkContext()
    val spark = SparkSession.builder().config(sc.getConf).getOrCreate()
    import spark.implicits._

    val fileStream = StructuredStreamingKafkaDSE.getClass.getResourceAsStream("/tweets-1.json")
    val jsonSampleString = Source.fromInputStream(fileStream).getLines().next()
    val jsonSampleDS = spark.createDataset(List(jsonSampleString))
    val jsonSample = spark.read.json(jsonSampleDS)
    val schema = jsonSample.schema

    val streamingInputDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "192.168.0.10:9092")
      .option("subscribe", "tweets-txt")
      .load()

    val tweetDF = streamingInputDF.selectExpr("CAST(value AS STRING)")
      .select(from_json($"value", schema).as("tweet"))
      .select(unix_timestamp($"tweet.created_at", "EEE MMM dd HH:mm:ss Z yyyy").as("created_at").cast(TimestampType),
        $"tweet.lang".as("lang"))

    val streamingCountsDF =  tweetDF
      .where(col("lang").isNotNull)
      .groupBy($"lang", window($"created_at", "1 minutes"))
      .count()
      .select($"lang", $"window.start".as("window"), $"count")

    // need to have table created with following CQL:
    // create table test.sttest_tweets(lang text, window timestamp, count int, primary key(lang, window));

    // This works only with Spark 2.2 (if BYOS 6.0.4 is used)
     val query = streamingCountsDF.writeStream
      .outputMode(OutputMode.Update)
      .format("org.apache.spark.sql.cassandra")
      .option("checkpointLocation", "webhdfs://192.168.0.10:5598/checkpoint")
      .option("keyspace", "test")
      .option("table", "sttest_tweets")
      .start()

    /*val query = streamingCountsDF.writeStream
          .outputMode("complete")
          .format("console")
          .start()*/

    query.awaitTermination()

  }
}
