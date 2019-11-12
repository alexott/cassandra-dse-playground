package com.datastax.alexott.spark

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

object JoinTestsScala {
  def main(args: Array[String]): Unit = {

    val sc = new SparkContext()
    val spark = SparkSession.builder().config(sc.getConf).getOrCreate()
    import spark.implicits._

    val toJoin = spark.range(1, 1000).map(x => x.intValue).withColumnRenamed("value", "id")

    val dataset = spark.read
        .format("org.apache.spark.sql.cassandra")
        .options(Map("table" -> "jtest", "keyspace" -> "test"))
        .load
    val joined = toJoin.join(dataset, dataset("id") === toJoin("id"))
    joined.explain
    joined.show(10)
  }

}
