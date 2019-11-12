package com.datastax.alexott.dsefs

import java.io._

import com.datastax.alexott.dsefs.DsefsUploader.getBool
import org.apache.commons.io.IOUtils
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SparkSession

object DsefsGetMerge {
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: DsefsGetMerge directoryToDownload destination")
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

    val appendNewline = getBool("addNewLine")
    val skipEmptyFiles = getBool("skipEmptyFiles")

    val fileSystem = FileSystem.get(spark.sparkContext.hadoopConfiguration)
    val path = new Path(args(0))
    if (!fileSystem.isDirectory(path)) {
      println("The '" + args(0) + "' is not a directory!")
      System.exit(1)
    }

    val out = new BufferedOutputStream(new FileOutputStream(outfile))
    val fileList = fileSystem.listFiles(path, false) // TODO: sort entries by file name?
    while(fileList.hasNext) {
      val it = fileList.next()
      val fpath = it.getPath()
      if (!fpath.getName.startsWith("part-")) {
        println("Skipping non-part file... " + fpath)
      } else {
        if (skipEmptyFiles && it.getLen == 0) {
          println("Skipping empty file file... " + fpath)
        } else {
          println("Copying data from " + fpath)
          val in = fileSystem.open(fpath)
          IOUtils.copy(in, out)
          in.close()
          if (appendNewline) {
            out.write('\n')
          }
        }
      }
    }

    out.close()
    System.exit(0)
  }
}
