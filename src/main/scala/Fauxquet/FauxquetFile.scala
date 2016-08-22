package main.scala.Fauxquet

import java.io.{File, PrintWriter}
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

import scala.tools.nsc.classpath.FileUtils.FileOps

/**
  * Created by james on 8/5/16.
  */
class FauxquetFile(val file: String) {
  type Schema = Vector[String]
  type Fields = Vector[String]

  def Schema(schema: List[String]) = schema.toVector

  val MAGIC = "PAR1".getBytes(Charset.forName("ASCII"))

  lazy val array = new SeekableArray[Byte](Files.readAllBytes(Paths.get(file)))
  lazy val table: Map[String, List[String]] = ???
  val fileMetaData: FileMetadata = new FileMetadata()
  var schema: Schema = _
  var fields: Fields = _

  def init() = {
    if (!isParquetFile) throw new Error(s"$file is not a valid Parquet file.")

    fileMetaData read array

    //val out = new PrintWriter(new File("./resources/loader2test.txt"))

    schema = Schema(fileMetaData.schema.filter(x => x.name != "m").map(x => x.name).toList)

    println("Done reading fileMetaData")
  }

  def isParquetFile: Boolean = {
    val l = array length

    val MAGIC = "PAR1".getBytes(Charset.forName("ASCII"))

    val footerLengthIndex = l - 4 - MAGIC.length

    val footerLength = {
      val x1 = array(footerLengthIndex)
      val x2 = array(footerLengthIndex + 1)
      val x3 = array(footerLengthIndex + 2)
      val x4 = array(footerLengthIndex + 3)

      if ((x1 | x2 | x3 | x4) < 0) throw new Error("Hit EOF early")

      (x4 << 24) + (x3 << 16) + (x2 << 8) + (x1 << 0)
    }

    val magic = new Array[Byte](MAGIC.length)

    for (i <- 0 until MAGIC.length) {
      magic(i) = array(footerLengthIndex + 4 + i)
    }

    array.pos = footerLengthIndex - footerLength

    magic.sameElements(MAGIC)
  }
}
