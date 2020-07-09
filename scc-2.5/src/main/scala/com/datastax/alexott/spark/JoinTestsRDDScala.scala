package com.datastax.alexott.spark

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import com.datastax.spark.connector._

object JoinTestsRDDScala {
  def main(args: Array[String]): Unit = {

    val sc = new SparkContext()
    val spark = SparkSession.builder().config(sc.getConf).getOrCreate()
    import spark.implicits._

    val toJoin = spark.range(1, 100).map(x => x.intValue).withColumnRenamed("value", "id").rdd

    val joined = toJoin.joinWithCassandraTable("test","jtest")
    println("Plan: " + joined.toDebugString)
    joined.cache()
    println("Count: " + joined.count())
    print("Data: ")
    joined.take(10).foreach(print)
    println()

  }

}
