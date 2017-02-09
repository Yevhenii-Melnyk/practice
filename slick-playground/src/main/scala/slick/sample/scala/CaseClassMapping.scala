package slick.sample.scala

import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object CaseClassMapping extends App {

	// the base query for the Users table
	val users = TableQuery[Users]

	val db: H2Profile.backend.Database = Database.forConfig("h2mem1")
	try {
		Await.result(db.run(DBIO.seq(
			// create the schema
			users.schema.create,

			// insert two User instances
			users += User("John Doe"),
			users += User("Fred Smith"),

			// print the users (select * from USERS)
			users.result.map(println)
		)), Duration.Inf)
	} finally db.close
}

case class User(name: String, id: Option[Int] = None)
