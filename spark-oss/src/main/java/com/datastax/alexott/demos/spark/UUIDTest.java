package com.datastax.alexott.demos.spark;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.datastax.spark.connector.japi.RDDJavaFunctions;
import com.datastax.spark.connector.japi.rdd.CassandraJavaPairRDD;
import com.datastax.spark.connector.japi.rdd.CassandraJavaRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.SparkSession;
import scala.Tuple1;
import scala.Tuple2;

import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowTo;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapRowToTuple;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapTupleToRow;
import static com.datastax.spark.connector.japi.CassandraJavaUtil.someColumns;

// create table if not exists test.utest (id int primary key, u uuid);
public class UUIDTest {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("UUIDTest")
                .getOrCreate();

        CassandraJavaRDD<UUIDData> uuids = javaFunctions(spark.sparkContext())
                .cassandraTable("test", "utest", mapRowTo(UUIDData.class));

        uuids.collect().forEach(System.out::println);

        JavaRDD<UUIDData> uuids2 = uuids.map(x -> new UUIDData(x.getId() + 10, x.getU()));

        CassandraJavaUtil.javaFunctions(uuids2)
                .writerBuilder("test", "utest", mapToRow(UUIDData.class))
                .saveToCassandra();
    }
}
