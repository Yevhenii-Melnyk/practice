package book.scala.messages

case class SetRequest(key: String, value: Any)

case class SetIfNotExists(key: String, value: Any)

case class GetRequest(key: String)

case class DeleteRequest(key: String)

case class KeyNotFoundException(key: String) extends Exception(s"Key $key not found in database")

case class KeyAlreadyExistsException(key: String) extends Exception(s"Key $key already exists in database")
