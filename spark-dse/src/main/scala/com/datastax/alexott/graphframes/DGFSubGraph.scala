package com.datastax.alexott.graphframes

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
//import com.datastax.bdp.graph.spark.graphframe._

object DGFSubGraph {
    def main(args: Array[String]): Unit = {
      val sc = new SparkContext()
      val spark = SparkSession.builder().config(sc.getConf).getOrCreate()
      import spark.implicits._

//      val graphBuilder = spark.dseGraph("GRAPH_NAME")


  }

}
