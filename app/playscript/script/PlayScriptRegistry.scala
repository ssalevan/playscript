package playscript
package script

import conversions._

import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
import java.util.concurrent.locks.ReentrantReadWriteLock
import play.api.Application
import play.api.Logger
import scala.collection.JavaConversions._
import scala.tools.nsc.io.AbstractFile

import com.googlecode.scalascriptengine.{CodeVersion, Config, FromClasspathFirst, ScalaScriptEngine}
import com.googlecode.scalascriptengine.RefreshAsynchronously


/**
 * A trait which provides for compiling and executing arbitrary Scala scripts,
 * providing access to the demo app's namespace at runtime.
 */
sealed trait PlayScriptEngine {

  protected val logger = Logger("PlayScriptEngine")
  protected val refreshLock: ReentrantReadWriteLock =
    new ReentrantReadWriteLock()

  protected var engine = createEngine

  protected var lastRefreshMillis: AtomicLong = new AtomicLong(0)

  /**
   * Calls a PlayScript method specified on an Object as a string,
   * using the supplied arguments to determine the manner in which it gets
   * called.
   *
   * @param method a String containing a PlayScript method to call.
   * @param args the arguments to pass to the PlayScript method.
   * @return the results of the method call.
   */
  def callMethod(method: String, args: AnyRef*): AnyRef = {
    logger.debug("PlayScript method call: %s, args: %s".format(method,
        args.mkString(", ")))
    if (!enabled) {
      logger.warn("PlayScript is not enabled but callMethod(%s) called."
          .format(method))
      return None
    }
    tryRefresh
    val argumentClasses = args.map{ arg => arg.getClass }
    val methodSplit = method.split("\\.")
    val objectClass = methodSplit.slice(0, methodSplit.length - 1)
      .mkString(".")
    val classMethod = methodSplit(methodSplit.length - 1)
    try {
      engine.get[PlayScript](objectClass).getMethod(classMethod,
          argumentClasses : _*).invoke(this, args : _*)
    } catch {
      case e => {
        logger.error("PLAYSCRIPT EXECUTION ERROR:\n%s".format(
            e.getTraceAsString))
        None
      }
    }
  }

  protected def createEngine() = new ScalaScriptEngine(
      Config(Set(sourceDir), getAppClasspath, getAppClasspath, outputDir))
      with FromClasspathFirst {}

  protected def enabled = true

  /**
   * Returns the classpath used by the PlayScript application.
   *
   * @return a Set of File objects representing all search locations on the
   *   classpath
   */
  protected def getAppClasspath: Set[File] = {
    try {
      // Derives classpath as supplied by Play.
      import play.api.Play.current
      current.classloader.asInstanceOf[URLClassLoader].getURLs()
        .map{ url => new File(url.getPath) }.toSet
    } catch {
      // Failing this, derives classpath from PlayScript context.
      case e => classOf[PlayScript].getClassLoader.asInstanceOf[URLClassLoader]
        .getURLs().map{ url => new File(url.getPath) }.toSet
    }
  }

  protected def outputDir = new File(System.getProperty("java.io.tmpdir"),
      "playscript-classes")

  protected def refreshPeriodMillis = 5000

  protected def sourceDir = new File("conf/scripts")

  /**
   * Attempts to refresh code that has been changed on the filesystem,
   * defaulting to the latest successfully-compiled code version if an error
   * occurs.
   */
  def tryRefresh: Unit = {
    try {
      // If the time of last refresh is less than the refresh threshold, don't
      // refresh the code unless we're in a startup state.
      if (System.currentTimeMillis - lastRefreshMillis.get <
          refreshPeriodMillis) {
        return
      }
      logger.debug("Refreshing PlayScript engine...")
      // Engine is not threadsafe, so refresh by way of write locks.
      refreshLock.writeLock().lock()
      // Triggers FS check/recompilation.
      engine.refresh
      refreshLock.writeLock().unlock()
      lastRefreshMillis.set(System.currentTimeMillis)
    } catch {
      case e => {
        logger.error("PLAYSCRIPT COMPILATION ERROR:\n%s".format(
           e.getTraceAsString))
      }
    }
  }

}


object PlayScriptRegistry extends PlayScriptEngine {

  def initializeAll(app: Application) = {
    outputDir.mkdir
    engine.deleteAllClassesInOutputDirectory
    tryRefresh
  }

  def shutdown = {
    // Recursively deletes output class directory.
    AbstractFile.getFile(outputDir).delete
  }

}
