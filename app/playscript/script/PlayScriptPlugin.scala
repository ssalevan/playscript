package playscript
package script

import play.api.{Application, Plugin}


class PlayScriptPlugin(app: Application) extends Plugin {

  override def onStart() {
    PlayScriptRegistry.initializeAll(app)
  }

  override def onStop() {
    PlayScriptRegistry.shutdown
  }

}
