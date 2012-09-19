package playscript
package script

import models.PhilCollins

import play.api.Logger
import play.api.mvc.Content
import play.api.templates.Html


object CollinsDrumSolos extends PlayScript {

  private val logger = Logger("CollinsDrumSolos")

  def getSoloRow(pc: PhilCollins): Content = {
    logger.warn("getSoloRow called")
    Html("<td>%s</td><td>%s</td>".format(pc.drumsolo, pc.album))
  }

  def testEcho(aString: String): Content = {
    logger.warn("testEcho called")
    Html(aString)
  }

}
