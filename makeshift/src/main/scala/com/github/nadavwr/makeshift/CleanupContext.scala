package com.github.nadavwr.makeshift

import scala.collection.mutable
import scala.scalanative.native._
import scala.util.control.NonFatal

class CleanupContext {
  lazy val cleanups: mutable.Buffer[CleanupEntry] = mutable.ListBuffer.empty
  def cleanupAll(): Unit =
    while (cleanups.nonEmpty) {
      val CleanupEntry(labelOpt, cleanup) = cleanups.remove(0)
      labelOpt.foreach(label => println(s"cleaning up $label"))
      cleanup()
    }
}
