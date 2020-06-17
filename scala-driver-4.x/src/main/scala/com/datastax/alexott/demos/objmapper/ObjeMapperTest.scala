package com.datastax.alexott.demos.objmapper

import java.net.InetSocketAddress
import java.util

import com.datastax.oss.driver.api.core.CqlSession

import collection.JavaConverters._

object ObjeMapperTest {

  val CQL_PORT: Int = System.getProperty("cqlPort", "9042").toInt

  def getContactPoints(contactPoints: String): util.Collection[InetSocketAddress] = {
    contactPoints.split(",")
      .map(host => InetSocketAddress.createUnresolved(host, CQL_PORT))
      .toSeq.asJava
  }

  def main(args: Array[String]): Unit = {
    val session = CqlSession.builder.addContactPoints(
      getContactPoints("10.101.34.176")).build

    session.execute("select * from system_auth.roles")
      .all().asScala.foreach(x => println(x.getFormattedContents))


    session.close()
  }

}
