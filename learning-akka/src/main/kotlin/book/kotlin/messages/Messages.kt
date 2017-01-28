package book.kotlin.messages

data class SetRequest(val key: String, val value: Any)

data class GetRequest(val key: String)

data class KeyNotFoundException(val key: String) : Exception("Key $key not found in database")
