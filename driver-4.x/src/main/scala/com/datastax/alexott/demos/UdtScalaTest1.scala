package com.datastax.alexott.demos

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.`type`.UserDefinedType
import com.datastax.oss.driver.api.core.`type`.codec.registry.MutableCodecRegistry
import com.datastax.oss.driver.api.core.`type`.codec.{MappingCodec, TypeCodec}
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.api.core.data.UdtValue

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

case class UDTTest(id: Integer, t1: Integer, t2: Integer, a2: Integer)

class UDTTestCodec(innerCodec: TypeCodec[UdtValue])
  extends MappingCodec[UdtValue, UDTTest](innerCodec, GenericType.of(classOf[UDTTest])) {

  override def getCqlType: UserDefinedType = super.getCqlType.asInstanceOf[UserDefinedType]

  override protected def innerToOuter(value: UdtValue): UDTTest = {
    if (value == null)
      null
    else
      UDTTest(value.getInt("id"), value.getInt("t1"),
        value.getInt("t2"), value.getInt("a2"))
  }

  override protected def outerToInner(value: UDTTest): UdtValue = {
    if (value == null)
      null
    else
      getCqlType.newValue(value.id, value.t1, value.t2, value.a2)
  }

}

object UdtScalaTest1 {

  def getInnerCodec(session: CqlSession,
                    keyspace: String,
                    typeName: String): TypeCodec[UdtValue] = {
    val udtTypeOption = session.getMetadata.getKeyspace(keyspace).asScala
      .flatMap(ks => ks.getUserDefinedType(typeName).asScala)

    udtTypeOption match {
      case None => throw new RuntimeException(s"No UDT $typeName in keyspace $keyspace")
      case Some(udtType) =>
        session.getContext.getCodecRegistry.codecFor(udtType)
    }
  }


  def main(args: Array[String]): Unit = {
    val session = CqlSession.builder()
        .addContactPoints(Commons.getContactPoints("10.101.34.176"))
        .build();

    val codecRegistry = session.getContext.getCodecRegistry
    val innerCodec = getInnerCodec(session, "test", "udt")
    val udtCodec = new UDTTestCodec(innerCodec)

    codecRegistry.asInstanceOf[MutableCodecRegistry].register(udtCodec)

    session.execute("select * from test.u2")
      .all().asScala
      .foreach(x => println(x.getInt("id"),
        x.get("u", GenericType.of(classOf[UDTTest]))))


    session.close()


  }

}
