package com.datastax.alexott.demos.streaming

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import org.apache.spark.SparkContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import com.datastax.spark.connector._

import scala.util.parsing.json.JSON

case class StockData(symbol: String, timestamp: Instant, price: Double) extends Serializable
case class StockInfo(symbol: String, exchange: String, name: String, industry: String,
                     base_price: Double) extends Serializable
case class JoinedData(symbol: String, exchange: String, name: String, industry: String,
                      base_price: Double, timestamp: Instant, price: Double) extends Serializable

object StockTickersJoinRDD {

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
    .withZone(ZoneId.of("Europe/Berlin"))

  /*
  Very naive parsing of JSON
   */
  def parseJson(input: String): Seq[StockData] ={
    val result = JSON.parseFull(input)
    result match {
      case Some(map: Map[String, Any]) =>
        Seq(StockData(map.get("symbol").get.asInstanceOf[String],
          Instant.from(formatter.parse(map.get("datetime").get.asInstanceOf[String])),
          map.get("value").get.asInstanceOf[Double]))
      case None => {
        println("Parsing failed")
        Seq()
      }
      case other => {
        println("Unknown data structure: " + other)
        Seq()
      }
    }

  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: StockTickersJoinDataFrames kafka-servers topic-name")
      System.exit(1)
    }
    val kafkaServes = args(0)
    val topicName = args(1)
    val sc = new SparkContext()
    val ssc = new StreamingContext(sc, Seconds(10))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> kafkaServes,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "StockTickersJoinRDD",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )
    val topics = Array(topicName)

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams)
    )

    val parsedData = stream.flatMap(x => parseJson(x.value()))
    val transformedData = parsedData.transform(rdd => {
      // we're using leftJoinWithCassandraTable to be able to find data for which we don't have information
      // in Cassandra - in this case, the second part of tuple will be empty
      val joined = rdd.leftJoinWithCassandraTable[StockInfo]("test", "stock_info")
      joined.persist()
      val missingInfoCount = joined.filter(x => x._2.isEmpty).count()
      val stocksWithInfo = joined.filter(x => x._2.isDefined)
      val existingInfoCount = stocksWithInfo.count()
      println(s"There are $missingInfoCount stock tickers without information in Cassandra")
      println(s"There are $existingInfoCount stock tickers with information in Cassandra")
      val combined = stocksWithInfo.map(x => {
        val i = x._2.get
        val d = x._1
        JoinedData(i.symbol, i.exchange, i.name, i.industry, i.base_price, d.timestamp, d.price)
      })
      joined.unpersist()
      combined
    })
    transformedData.foreachRDD(rdd => rdd.foreach(println))

    ssc.start()
    ssc.awaitTermination()
  }
}
