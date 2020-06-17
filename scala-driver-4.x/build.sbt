scalaVersion := "2.11.12"

lazy val library = new {
  val mapperRuntime = "com.datastax.oss" % "java-driver-mapper-runtime" % "4.7.0"
  val mapperProcessor = "com.datastax.oss" % "java-driver-mapper-processor" % "4.7.0" % "provided"
  val queryBuilder = "com.datastax.oss" % "java-driver-query-builder" % "4.7.0"
}

lazy val processAnnotations = taskKey[Unit]("Process annotations")

processAnnotations := {
  val log = streams.value.log
  log.info("Processing annotations ...")

  val classpath = ((products in Compile).value ++ ((dependencyClasspath in Compile).value.files)) mkString ":"
  val destinationDirectory = (classDirectory in Compile).value

  val processor = "com.datastax.oss.driver.internal.mapper.processor.MapperProcessor"
  val classesToProcess = Seq("com.datastax.alexott.demos.objmapper.u2",
    "com.datastax.alexott.demos.objmapper.udt") mkString " "

  val command = s"javac -cp $classpath -proc:only -processor $processor -XprintRounds -d $destinationDirectory $classesToProcess"

  runCommand(command, "Failed to process annotations.", log)
  log.info("Done processing annotations.")
}

def runCommand(command: String, message: => String, log: Logger) = {
  import scala.sys.process._

  val result = command !

  if (result != 0) {
    log.error(message)
    sys.error("Failed running command: " + command)
  }
}

packageBin in Compile := (packageBin in Compile dependsOn (processAnnotations in Compile)).value

organization := "com.datastax.alexott"
version := "1.0"
name := "demos"
scalacOptions += "-target:jvm-1.8"
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
libraryDependencies ++= Seq(
  library.mapperRuntime,
  library.queryBuilder,
  library.mapperProcessor
)
