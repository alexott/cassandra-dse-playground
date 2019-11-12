package com.datastax.alexott.streaming

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.cassandra._

object StructuredStreamingDSE {

  def main(args: Array[String]): Unit = {

    val sc = new SparkContext()
    val spark = SparkSession.builder().config(sc.getConf).getOrCreate()
    import spark.implicits._


    val inputPath = if (args.isEmpty) {
      "webhdfs://192.168.0.10:5598/sttest/"
    } else {
      args(0)
    }

    // Input data are from DSE distribution, file: demos/weather_sensors/resources/daily.csv
    // stationid,metric,date,location,max,mean,median,min,percentile1,percentile5,percentile95,percentile99,total
    // LAE,barometricpressure,2014-01-01 00:00:00+0000,Nadzab,950,944,944,940,940,940,948,950,1360374

    val csvSchema = new StructType().add("stationid", StringType)
      .add("metric", StringType).add("date", TimestampType)
      .add("location", StringType).add("", IntegerType)
      .add("max", IntegerType).add("mean", IntegerType)
      .add("median", IntegerType).add("min", IntegerType)
      .add("percentile1", IntegerType).add("percentile5", IntegerType)
      .add("percentile95", IntegerType).add("percentile99", IntegerType)
      .add("total", IntegerType)

    val streamingInputDF = spark.readStream
        .schema(csvSchema)
        .option("maxFilesPerTrigger", 1)
        .option("header", true)
        .csv(inputPath)

    val streamingCountsDF =  streamingInputDF
      .where(col("location").isNotNull)
      .groupBy($"location")
      .count()

    // need to have table created with following CQL:
    // create table test.sttest(location text primary key, count int);

    // This works only with Spark 2.2 (if BYOS 6.0.4 is used)
    val query = streamingCountsDF.writeStream
      .outputMode(OutputMode.Update)
      .format("org.apache.spark.sql.cassandra")
      .option("checkpointLocation", "webhdfs://192.168.0.10:5598/checkpoint")
      .option("keyspace", "test")
      .option("table", "sttest")
      .start()

    /* val query = streamingCountsDF.writeStream
          .outputMode("complete")
          .format("console")
          .start()*/

    query.awaitTermination()

  }
}
