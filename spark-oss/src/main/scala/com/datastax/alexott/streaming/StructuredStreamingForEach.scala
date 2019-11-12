package com.datastax.alexott.streaming

import java.util.concurrent.ConcurrentHashMap

import com.datastax.driver.core.PreparedStatement
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types._
import org.apache.spark.sql.{ForeachWriter, SparkSession}

// need to have table created with following CQL:
// create table test.sttest(location text primary key, count int);

class CassandraSinkForeach() extends ForeachWriter[org.apache.spark.sql.Row] {
  val insertStatement: String = "insert into test.sttest(location, count) values(?,?)"

  @transient
  var cassandraConnector: CassandraConnector = null;
  @transient
  var preparedStatements: ConcurrentHashMap[String, PreparedStatement] = null

  def open(partitionId: Long, version: Long): Boolean = {
    if (cassandraConnector == null) {
      cassandraConnector = CassandraConnector(SparkSession.builder.getOrCreate.sparkContext.getConf)
    }
    if (preparedStatements == null) {
      preparedStatements = new ConcurrentHashMap[String, PreparedStatement]()
    }
    true
  }

  def process(record: org.apache.spark.sql.Row) = {
    val pstmt = getStatement(insertStatement)
    val bstmt = pstmt.bind(record.getString(0), new java.lang.Integer(record.getLong(1).toInt))
    cassandraConnector.withSessionDo(session =>  session.execute(bstmt))
  }

  def close(errorOrNull: Throwable): Unit = {
  }

  def getStatement(stmt: String): PreparedStatement = {
    if (preparedStatements.contains(stmt)) {
      preparedStatements.get(stmt)
    } else {
      val pstmt = cassandraConnector.withSessionDo(session => session.prepare(stmt))
      preparedStatements.putIfAbsent(stmt, pstmt)
      pstmt
    }
  }
}

object StructuredStreamingForEach {

  def main(args: Array[String]): Unit = {

    val sc = new SparkContext()
    val spark = SparkSession.builder().config(sc.getConf).getOrCreate()
    import spark.implicits._


    val inputPath = if (args.isEmpty) {
      "webhdfs://127.0.0.1:5598/sttest/"
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

    val query = streamingCountsDF.writeStream
      .outputMode(OutputMode.Update)
      .option("checkpointLocation", "webhdfs://127.0.0.1:5598/checkpoint")
      .foreach(new CassandraSinkForeach())
      .start()


    query.awaitTermination()

  }
}
