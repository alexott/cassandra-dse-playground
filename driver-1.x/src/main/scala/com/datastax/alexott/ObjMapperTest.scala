import java.time.Instant

import com.datastax.driver.core.Cluster
import com.datastax.driver.mapping.{MappingManager, Result}
import com.datastax.driver.mapping.annotations.{Accessor, ClusteringColumn, Column, Field, Param, PartitionKey, Query, Table, UDT}

import scala.annotation.meta.field
import scala.collection.JavaConverters

// tables and data definition...
// create table test.scala_test(id int primary key, t text, tm timestamp);
// insert into test.scala_test(id,t,tm) values (1,'t1','2018-11-07T00:00:00Z') ;

// create table test.scala_test_complex(p1 int, p2 int, c1 int, c2 int, t text, tm timestamp, primary key ((p1,p2), c1, c2));
// insert into test.scala_test_complex(p1, p2, c1, c2, t,tm) values (0,1,0,1,'t1','2018-11-07T00:00:00Z') ;
// insert into test.scala_test_complex(p1, p2, c1, c2, t,tm) values (0,1,1,1,'t1','2018-11-08T10:00:00Z') ;

// create type test.scala_udt(id int, t text);
// create table test.scala_test_udt(id int primary key, udt frozen<test.scala_udt>);
// insert into test.scala_test_udt (id, udt) values (1, {id: 1, t: 't1'});


@Table(name = "scala_test")
class TableObject {
  @PartitionKey
  var id: Integer = 0;
  var t: String = "";
  var tm: java.util.Date = new java.util.Date();

  def this(idval: Integer, tval: String, tmval: java.util.Date) = {
    this();
    this.id = idval;
    this.t = tval;
    this.tm = tmval;
  }

  override def toString: String = {
    "{id=" + id + ", t='" + t + "', tm='" + tm + "'}"
  }
}

@Table(name = "scala_test")
class TableObjectImmutable(@PartitionKey id: Integer, t: String, tm: java.util.Date) {
  override def toString: String = {
    "{id=" + id + ", t='" + t + "', tm='" + tm + "'}"
  }
}


@Table(name = "scala_test")
case class TableObjectCaseClass(@(PartitionKey @field) id: Integer, t: String, tm: java.util.Date) {
  def this() {
    this(0, "", new java.util.Date())
  }
}

// case class with renamed field
@Table(name = "scala_test")
case class TableObjectCaseClassRenamed(@(PartitionKey @field) id: Integer,
                                       @(Column @field)(name = "t") text: String, tm: java.util.Date) {
  def this() {
    this(0, "", new java.util.Date())
  }
}

@Table(name = "scala_test_complex", keyspace = "test")
case class TableObjectCaseClassClustered(@(PartitionKey @field)(value = 0) p1: Integer,
                                         @(PartitionKey @field)(value = 1) p2: Integer,
                                         @(ClusteringColumn @field)(value = 0) c1: java.lang.Integer,
                                         @(ClusteringColumn @field)(value = 1) c2: java.lang.Integer,
                                         t: String,
                                         tm: java.util.Date) {
  def this() {
    this(0, 1, 0, 1, "", new java.util.Date())
  }
}

@UDT(name = "scala_udt")
case class UdtCaseClass(id: Integer, @(Field @field)(name = "t") text: String) {
  def this() {
    this(0, "")
  }
}

@Table(name = "scala_test_udt")
case class TableObjectCaseClassWithUDT(@(PartitionKey @field) id: Integer,
                                       udt: UdtCaseClass) {
  def this() {
    this(0, UdtCaseClass(0, ""))
  }
}


@Accessor
trait ObjectAccessor {
  @Query("SELECT * from scala_test_complex where p1 = :p1 and p2 = :p2")
  def getByPartKey(@Param p1: Integer, @Param p2: Integer): Result[TableObjectCaseClassClustered]

  @Query("DELETE from scala_test_complex where p1 = :p1 and p2 = :p2")
  def deleteByPartKey(@Param p1: Integer, @Param p2: Integer)
}


object ObjMapperTest {

  def main(args: Array[String]): Unit = {

    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
    val session = cluster.connect("test")
    val manager = new MappingManager(session)

    val mapperClass = manager.mapper(classOf[TableObject])
    val objClass = mapperClass.get(new Integer(1))
    println("Obj(1)='" + objClass + "'")
    mapperClass.save(new TableObject(2, "t2", java.util.Date.from(Instant.now())))
    val objClass2 = mapperClass.get(new Integer(2))
    println("Obj(2)='" + objClass2 + "'")
    mapperClass.delete(objClass2)

    val mapperClassImmutable = manager.mapper(classOf[TableObject])
    val objClassImm = mapperClassImmutable.get(new Integer(1))
    println("ObjImm(1)='" + objClassImm + "'")

    val mapperCaseClass = manager.mapper(classOf[TableObjectCaseClass])
    val objCaseClass = mapperCaseClass.get(new Integer(1))
    println("Obj(1)='" + objCaseClass + "'")

    val mapperCaseClassRenamed = manager.mapper(classOf[TableObjectCaseClassRenamed])
    val objCaseClassRenamed = mapperCaseClassRenamed.get(new Integer(1))
    println("Obj(1)='" + objCaseClassRenamed + "'")

    mapperCaseClassRenamed.save(TableObjectCaseClassRenamed(2, "test 2", new java.util.Date()))

    val mapperCaseClassClustered = manager.mapper(classOf[TableObjectCaseClassClustered])
    val objCaseClass2 = mapperCaseClassClustered.get(new Integer(0), new Integer(1),
      new Integer(0), new Integer(1))
    println("Obj2((0,1),0,1)='" + objCaseClass2 + "'")

    val mapperForUdtCaseClass = manager.mapper(classOf[TableObjectCaseClassWithUDT])
    val objectCaseClassWithUDT = mapperForUdtCaseClass.get(new Integer(1))
    println("ObjWithUdt(1)='" + objectCaseClassWithUDT + "'")

    val accessor = manager.createAccessor(classOf[ObjectAccessor])
    val rs = accessor.getByPartKey(0, 1)
    for (r <- JavaConverters.asScalaIteratorConverter(rs.iterator()).asScala) {
      println("r=" + r)
    }

    accessor.deleteByPartKey(0,0)

    session.close()
    cluster.close()

  }

}