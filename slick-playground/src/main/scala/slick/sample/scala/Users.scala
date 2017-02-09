package slick.sample.scala

import slick.jdbc.H2Profile.api._

class Users(tag: Tag) extends Table[User](tag, "USERS") {
	// Auto Increment the id primary key column
	def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

	// The name can't be null
	def name = column[String]("NAME")

	// the * projection (e.g. select * ...) auto-transforms the tupled
	// column values to / from a User
	def * = {
		val tuple = (name, id.?)
		tuple <> (User.tupled, User.unapply)
	}

}

