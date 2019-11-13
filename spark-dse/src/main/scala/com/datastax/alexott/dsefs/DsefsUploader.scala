package com.datastax.alexott.dsefs

import java.io.File

import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.spark.sql.SparkSession

object DsefsUploader {
  def getBool(name: String): Boolean = {
    java.lang.Boolean.getBoolean(name)
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: DsefsUploader fileOrDirectoryToUpload destination")
      System.exit(1)
    }
    val spark = SparkSession.builder().getOrCreate()

    val infile = new File(args(0))
    if (!infile.exists()) {
      println("File '" + args(0) + " doesn't exist!")
    }

    val fileSystem = FileSystem.get(spark.sparkContext.hadoopConfiguration)

    val name = if ("/".equals(args(1))) {
      "/" + infile.getName
    } else{
      args(1)
    }
    val path = new Path(name)
    if (fileSystem.exists(path)) {
      if (getBool("overwriteMode")) {
        fileSystem.delete(path, true)
      } else {
        println("File or directory '" + args(1) + "' exists on DSEFS! Remove it, or pass -DoverwriteMode=true to the job!")
        System.exit(1)
      }
    }

    FileUtil.copy(infile, fileSystem, path, false, spark.sparkContext.hadoopConfiguration)

    System.exit(0)
  }
}
