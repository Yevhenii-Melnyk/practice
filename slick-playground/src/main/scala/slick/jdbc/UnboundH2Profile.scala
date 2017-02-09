package slick.jdbc

import slick.jdbc.H2Profile.api._
import slick.lifted
import slick.lifted.{AbstractTable, TableQuery}

object UnboundH2Profile {

	def db(s: String): _root_.slick.jdbc.H2Profile.backend.DatabaseDef = Database.forConfig(s)

	def add[E <: AbstractTable[_]](tableQuery: TableQuery[E], value: E#TableElementType) = tableQuery += value

//	def result()

	//	def create(tableQuery: TableQuery[_]) = tableQuery.schema.create


}
