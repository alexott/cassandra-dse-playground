package com.datastax.alexott.dsefs

import java.io._

import com.datastax.alexott.dsefs.DsefsUploader.getBool
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.spark.sql.SparkSession

object DsefsDownloader {
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: DsefsDownloader fileOrDirectoryToDownload destination")
      System.exit(1)
    }
    val spark = SparkSession.builder().getOrCreate()

    //    import spark.implicits._

    val remoteFS = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val path = new Path(args(0))
    if (!remoteFS.exists(path)) {
      println("The file or directory '" + args(0) + "' doesn't exist!")
      System.exit(1)
    }

    val outfile = new File(args(1))
    if (outfile.exists()) {
      if (getBool("overwriteMode")) {
        outfile.delete()
      } else {
        println("File '" + args(1) + "' exists on disk! Remove it, or pass -DoverwriteMode=true to the job!")
        System.exit(1)
      }
    }

    FileUtil.copy(remoteFS, path, outfile, false, spark.sparkContext.hadoopConfiguration)

    System.exit(0)
  }
}
