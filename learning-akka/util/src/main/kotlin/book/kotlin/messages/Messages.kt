package book.kotlin.messages

import java.io.Serializable

data class SetRequest(val key: String, val value: Any) : Serializable

data class SetIfNotExists(val key: String, val value: Any) : Serializable

data class GetRequest(val key: String) : Serializable

data class DeleteRequest(val key: String) : Serializable

data class KeyNotFoundException(val key: String) : Exception("Key $key not found in database"), Serializable

data class KeyAlreadyExistsException(val key: String) : Exception("Key $key already exists in database"), Serializable
