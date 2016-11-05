package big.data.university


import java.io.File

import big.data.university.util.{Env, Files}
import org.apache.spark.SparkContext

object WordCount extends App {

  Env.setHadoopPath

  val inPath = WordCount.getClass.getClassLoader.getResource("data/antony_cleopatra").getPath
  val outPath = "res"
  Files.rmrf(outPath)
  val sc = new SparkContext("local[*]", "Word count")

  try {
    val text = sc.textFile(inPath)
    val wordCount = text
      .map(_.toLowerCase)
      .flatMap(_.split("\\W+"))
      .groupBy(word => word)
      .mapValues(group => group.size)
      .sortBy(_._2, ascending = false)

    wordCount.saveAsTextFile(outPath)
  } finally {
    sc.stop()
  }


}
