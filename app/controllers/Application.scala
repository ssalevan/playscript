package controllers

import models.PhilCollins

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    // Displays all the Phil Collins drum solos listed in our database.
    Ok(views.html.index("Phil Collins?", PhilCollins.findAll))
  }
  
}