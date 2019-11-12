package com.datastax.alexott

import com.datastax.driver.core.{Cluster, Row, TypeCodec}
import com.datastax.driver.extras.codecs.jdk8.OptionalCodec

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._


object Optionals {
  private val intCodec =  TypeCodec.cint()
  private val optionalIntCodec = new OptionalCodec[java.lang.Integer](intCodec)
  private val javaIntType = optionalIntCodec.getJavaType()

  def registerCodecs(cluster: Cluster): Unit = {
    val codecRegistry = cluster.getConfiguration.getCodecRegistry

    codecRegistry.register(optionalIntCodec)
  }

  def getInt(row: Row, col: String): Option[java.lang.Integer] = {
    row.get(col, javaIntType).asScala
  }
  def getInt(row: Row, col: Int): Option[java.lang.Integer] = {
    row.get(col, javaIntType).asScala
  }
}

object CodecsTest {
  def main(args: Array[String]): Unit = {

    val cluster = Cluster.builder().addContactPoint("10.200.176.39").build()
    Optionals.registerCodecs(cluster)
    val session = cluster.connect()

    for (row <- session.execute("select id, c1, v1 from test.st1 where id = 2").all().asScala) {
      println("id=" + Optionals.getInt(row, "id")
        + ", c1=" + Optionals.getInt(row, "c1")
        + ", v1=" + Optionals.getInt(row, "v1"))
    }
    session.close()
    cluster.close()

  }
}
