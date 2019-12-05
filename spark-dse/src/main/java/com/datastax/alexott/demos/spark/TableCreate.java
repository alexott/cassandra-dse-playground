package com.datastax.alexott.demos.spark;


import com.datastax.spark.connector.DatasetFunctions;
import com.datastax.spark.connector.cql.CassandraConnector;
import com.datastax.spark.connector.cql.CassandraConnectorConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.spark_project.guava.collect.ImmutableMap;
import scala.Option;
import scala.Some;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import java.util.Arrays;

public class TableCreate {
    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName("CassandraTableCreate")
                .getOrCreate();

        /*
        CREATE TABLE test.widerows4 (
    part text,
    clust text,
    col2 text,
    data text,
    PRIMARY KEY (part, clust));
         */

        Dataset<Row> dataset = spark.read()
                .format("org.apache.spark.sql.cassandra")
                .options(ImmutableMap.of("table", "widerows4", "keyspace", "test"))
                .load();
        dataset.printSchema();

        DatasetFunctions<Row> dfFunctions = new DatasetFunctions<>(dataset, RowEncoder.apply(dataset.schema()));
        Option<Seq<String>> partitionSeqlist = new Some<>(JavaConversions.asScalaBuffer(
                Arrays.asList("part")).seq());
        Option<Seq<String>> clusteringSeqlist = new Some<>(JavaConversions.asScalaBuffer(
                Arrays.asList("clust", "col2")).seq());
        CassandraConnector connector = new CassandraConnector(CassandraConnectorConf.apply(spark.sparkContext().getConf()));
        dfFunctions.createCassandraTable("test", "widerows6",
                partitionSeqlist, clusteringSeqlist, connector);
        dataset.write().format("org.apache.spark.sql.cassandra")
                .options(ImmutableMap.of("table", "widerows6", "keyspace", "test"))
                .save();
    }


}
