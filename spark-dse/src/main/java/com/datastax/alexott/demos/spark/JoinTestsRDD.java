package com.datastax.alexott.demos.spark;

import com.datastax.spark.connector.ColumnSelector;
import com.datastax.spark.connector.japi.RDDJavaFunctions;
import com.datastax.spark.connector.japi.rdd.CassandraJavaPairRDD;
import com.datastax.spark.connector.japi.rdd.CassandraJavaRDD;
import com.datastax.spark.connector.writer.RowWriterFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple1;
import scala.Tuple2;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowToTuple;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapTupleToRow;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.someColumns;


// create table if not exists test.jtest (id int primary key, v text);

public class JoinTestsRDD {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("CassandraSparkWithJoin")
//                .config("spark.cassandra.connection.host", "192.168.0.10")
                .getOrCreate();

//        Dataset<Row> df = spark.sql("select * from test.jtest");
//        df.show();
        JavaRDD<Tuple1<Integer>> toJoinRDD = spark
                .range(1, 100)
                .javaRDD()
                .map(x -> new Tuple1<Integer>(x.intValue()));

//        CassandraJavaRDD<Tuple2<Integer, String>> rdd = javaFunctions(spark.sparkContext())
//                .cassandraTable("test", "jtest", mapRowToTuple(Integer.class, String.class))
//                        .select("id", "v");
//
//        System.out.println(rdd.take(10));


        RDDJavaFunctions<Tuple1<Integer>> trdd = new RDDJavaFunctions<Tuple1<Integer>>(toJoinRDD.rdd());
        CassandraJavaPairRDD<Tuple1<Integer>, Tuple2<Integer, String>> joinedRDD =
                trdd.joinWithCassandraTable("test", "jtest",
                someColumns("id", "v"), someColumns("id"),
                mapRowToTuple(Integer.class, String.class), mapTupleToRow(Integer.class));
        System.out.println("Plan: " + joinedRDD.toDebugString());
        joinedRDD.cache();
        System.out.print("Count: " + joinedRDD.count());
        System.out.println("Data: " + joinedRDD.take(10));

    }
}
