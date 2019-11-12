package com.datastax.alexott.dsefs

import java.io.{BufferedInputStream, File, FileInputStream}

import org.apache.commons.io.IOUtils
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SparkSession

object DsefsUploader {
  def getBool(name: String): Boolean = {
    java.lang.Boolean.getBoolean(name)
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: DsefsUploader fileToUpload destination")
      System.exit(1)
    }
    val spark = SparkSession.builder().getOrCreate()

    val infile = new File(args(0))
    if (!infile.exists()) {
      println("File '" + args(0) + " doesn't exist!")
    }

    val fileSystem = FileSystem.get(spark.sparkContext.hadoopConfiguration)

    val path = new Path(args(1))
    if (fileSystem.exists(path)) {
      if (getBool("overwriteMode")) {
        fileSystem.delete(path, false)
      } else {
        println("File '" + args(1) + "' exists on DSEFS! Remove it, or pass -DoverwriteMode=true to the job!")
        System.exit(1)
      }
    }

    val out = fileSystem.create(path)
    val in = new BufferedInputStream(new FileInputStream(infile))

    IOUtils.copy(in, out)
    in.close()
    out.close()

    System.exit(0)
  }
}
