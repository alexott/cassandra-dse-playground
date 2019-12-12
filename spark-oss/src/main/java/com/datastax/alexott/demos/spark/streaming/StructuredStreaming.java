package com.datastax.alexott.demos.spark.streaming;

import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.StructType;
import org.spark_project.guava.collect.ImmutableMap;

import static org.apache.spark.sql.types.DataTypes.IntegerType;
import static org.apache.spark.sql.types.DataTypes.StringType;
import static org.apache.spark.sql.types.DataTypes.TimestampType;
import static org.apache.spark.sql.functions.col;

public class StructuredStreaming {
    public static void main(String[] args) throws StreamingQueryException {
        SparkSession spark = SparkSession
                .builder()
                .appName("CassandraTableCreate")
                .getOrCreate();

        String sourceData = "webhdfs://127.0.0.1:5598/sttest/";


        StructType csvSchema = new StructType()
                .add("stationid", StringType)
                .add("metric", StringType).add("date", TimestampType)
                .add("location", StringType).add("", IntegerType)
                .add("max", IntegerType).add("mean", IntegerType)
                .add("median", IntegerType).add("min", IntegerType)
                .add("percentile1", IntegerType).add("percentile5", IntegerType)
                .add("percentile95", IntegerType).add("percentile99", IntegerType)
                .add("total", IntegerType);

        Dataset<Row> inputDF =  spark.readStream()
                .schema(csvSchema)
                .option("maxFilesPerTrigger", 1)
                .option("header", true)
                .csv(sourceData);
        Dataset<Row> groupedDF = inputDF
                .where(col("location").isNotNull())
                .groupBy(col("location"))
                .count();

        StreamingQuery query = groupedDF.writeStream()
                .outputMode(OutputMode.Update())
                .option("checkpointLocation", "webhdfs://127.0.0.1:5598/checkpoint")
                .foreachBatch((VoidFunction2<Dataset<Row>, Long>) (df, batchId) ->
                        df.write()
                                .format("org.apache.spark.sql.cassandra")
                                .options(ImmutableMap.of("table", "sttest", "keyspace", "test"))
                                .mode(SaveMode.Append)
                                .save()
                )
                .start();

        query.awaitTermination();


    }
}
