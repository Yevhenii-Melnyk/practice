package big.data.university.util

import java.io.File

object Files {

  case class FileOperationError(msg: String) extends RuntimeException(msg)

  def rmrf(root: String): Unit = rmrf(new File(root))

  def rmrf(root: File): Unit = {
    if (root.isFile) root.delete()
    else if (root.exists) {
      root.listFiles.foreach(rmrf)
      root.delete()
    }
  }

  def rm(file: String): Unit = rm(new File(file))

  def rm(file: File): Unit =
    if (!file.delete) throw FileOperationError(s"Deleting $file failed!")

  def mkdir(path: String): Unit = new File(path).mkdirs

}

object Env {

  def setHadoopPath(): Unit = {
    val path: File = new File("hadoop")
    System.setProperty("hadoop.home.dir", path.getAbsolutePath)
  }
}