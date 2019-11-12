package com.datastax.alexott

import com.datastax.driver.core.Cluster

import scala.collection.JavaConverters

object GetDCNames {

  def main(args: Array[String]): Unit = {

    val cluster = Cluster.builder()
      .addContactPoint(System.getProperty("contactPoint", "127.0.0.1"))
      .build();
    val session = cluster.connect()

    val metadata = cluster.getMetadata
    val hosts = JavaConverters.collectionAsScalaIterableConverter(metadata.getAllHosts).asScala.toSeq
    val dcs = hosts.map{host => host.getDatacenter}.toSet

    println("All DCs: " + dcs)
  }
}
