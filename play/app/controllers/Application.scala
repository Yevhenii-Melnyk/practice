package controllers

import models.{DB, Person}
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import sorm.Persisted

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  val personForm: Form[Person] = Form {
    mapping(
      "name" -> text
    )(Person.apply)(Person.unapply)
  }

  def addPerson = Action { implicit request =>
    val person: Person = personForm.bindFromRequest.get
    DB.save(person)
    Redirect(routes.Application.index)
  }

  def getPeople = Action {
    val people: Stream[Person with Persisted] = DB.query[Person].fetch
    Ok(Json.toJson(people))
  }

}