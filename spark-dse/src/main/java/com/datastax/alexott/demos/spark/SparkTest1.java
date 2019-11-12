package com.datastax.alexott.demos.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkTest1 {

  public static void main(String[] args) {
    SparkSession spark = SparkSession
    .builder()
    .appName("CassandraSpark")
    .getOrCreate();
    
    Dataset<Row> sqlDF = spark.sql("select * from datastax.vehicle limit 1000");
    sqlDF.printSchema();
    sqlDF.show();
  }

}
