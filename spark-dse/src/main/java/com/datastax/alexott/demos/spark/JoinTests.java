package com.datastax.alexott.demos.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.spark_project.guava.collect.ImmutableMap;
import scala.Tuple1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// create table if not exists test.jtest (id int primary key, v text);

public class JoinTests {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("CassandraSparkWithJoin")
//                .config("spark.cassandra.connection.host", "192.168.0.10")
                .getOrCreate();

//        Dataset<Row> df = spark.sql("select * from test.jtest");
//        df.show();
        Dataset<Row> toJoin = spark.range(1, 1000).selectExpr("cast(id as int) as id");

        toJoin.printSchema();
//        toJoin.show();

        Dataset<Row> dataset = spark.read()
                .format("org.apache.spark.sql.cassandra")
                .options(ImmutableMap.of("table", "jtest", "keyspace", "test"))
                .load();

        Dataset<Row> joined = toJoin.join(dataset,
                dataset.col("id").equalTo(toJoin.col("id")));
        joined.printSchema();
        joined.explain();
        joined.show(10);
    }
}
