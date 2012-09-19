package models
 
import play.api.db._
import play.api.Play.current
 
import anorm._
import anorm.SqlParser._
 
case class PhilCollins(id: Pk[Long], drumsolo: String, album: String)
 
object PhilCollins {

  val simple = {
    get[Pk[Long]]("id") ~
    get[String]("drumsolo") ~
    get[String]("album") map {
      case id~drumsolo~album => PhilCollins(id, drumsolo, album)
    }
  }

  def findAll(): Seq[PhilCollins] = {
    DB.withConnection { implicit connection =>
      SQL("select * from philcollins").as(PhilCollins.simple *)
    }
  }

  def create(pc: PhilCollins): Unit = {
    DB.withConnection { implicit connection =>
      SQL("insert into philcollins(drumsolo, album) values ({drumsolo}, {album})").on(
        "drumsolo" -> pc.drumsolo,
        "album" -> pc.album
      ).executeUpdate()
    }
  }
 
}
