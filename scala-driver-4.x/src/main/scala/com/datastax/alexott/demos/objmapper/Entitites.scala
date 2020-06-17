package com.datastax.alexott.demos.objmapper

import com.datastax.oss.driver.api.mapper.annotations.{CqlName, Entity, PartitionKey}

import scala.annotation.meta.field

/*

    CREATE TYPE test.udt (
      id int,
      t1 int,
      t2 int,
      a2 int
    );
    CREATE TABLE test.u2 (
      id int PRIMARY KEY,
      u udt
    );
     */


@Entity
case class udt(@(CqlName @field)("id") id: java.lang.Integer,
               @(CqlName @field)("t1") t1: java.lang.Integer,
               @(CqlName @field)("t2") t2: java.lang.Integer,
               @(CqlName @field)("a2") a2: java.lang.Integer) {
  def this() {
    this(0,0,0,0)
  }
}

@Entity
case class u2(@(PartitionKey @field) id: java.lang.Integer,
              @(CqlName @field)(value = "udt") udt: udt) {
  def this() {
    this(0, new udt)
  }
}