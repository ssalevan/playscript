package playscript
package script

import java.io.PrintWriter
import java.io.StringWriter


package object conversions {

  implicit def Throwable2RichThrowable(cause: Throwable): RichThrowable =
    new RichThrowable(cause)

  class RichThrowable(cause: Throwable) extends Throwable(cause) {
    def getTraceAsString: String = {
      val stringWriter = new StringWriter()
      val printWriter = new PrintWriter(stringWriter)
      cause.printStackTrace(printWriter)
      return stringWriter.toString()
    }
  }  

}