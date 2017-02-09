package slick.jdbc

import slick.lifted.{Query, TableQuery, Tag}
import slick.relational.RelationalProfile

object UnboundRelationalTableComponent extends H2Profile {

	abstract class UnboundTable[T](_tableTag: Tag, _schemaName: Option[String], _tableName: String) extends Table[T](_tableTag, _schemaName, _tableName) {
		def this(_tableTag: Tag, _tableName: String) = this(_tableTag, None, _tableName)
	}

	class UnboundTableQueryExtensionMethods[T <: RelationalProfile#Table[_], U](val qq: Query[T, U, Seq] with TableQuery[T])
		extends TableQueryExtensionMethods(qq) {
	}

}