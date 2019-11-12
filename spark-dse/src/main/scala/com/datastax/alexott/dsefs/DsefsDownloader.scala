package com.datastax.alexott.dsefs

import java.io._

import com.datastax.alexott.dsefs.DsefsUploader.getBool
import org.apache.commons.io.IOUtils
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileSystem, Path}
import org.apache.spark.sql.{SparkSession, _}

object DsefsDownloader {
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: DsefsDownloader fileToDownload destination")
      System.exit(1)
    }
    val spark = SparkSession.builder().getOrCreate()

    //    import spark.implicits._

    val outfile = new File(args(1))
    if (outfile.exists()) {
      if (getBool("overwriteMode")) {
        outfile.delete()
      } else {
        println("File '" + args(1) + "' exists on disk! Remove it, or pass -DoverwriteMode=true to the job!")
        System.exit(1)
      }
    }

    val fileSystem = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val path = new Path(args(0))
    if (!fileSystem.isFile(path)) {
      println("The '" + args(0) + "' is not a file!")
      System.exit(1)
    }
    val in = fileSystem.open(path)
    val out = new BufferedOutputStream(new FileOutputStream(outfile))
    IOUtils.copy(in, out)
    out.close()
    in.close()
    System.exit(0)
  }
}
