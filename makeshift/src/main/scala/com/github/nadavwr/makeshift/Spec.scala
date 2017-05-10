package com.github.nadavwr.makeshift
import scala.util.Try

trait Spec {
  protected implicit lazy val cleanupContext: CleanupContext = new CleanupContext
  def test(description: String): TestBuilder = new TestBuilder(description)
  class TestBuilder(description: String) {
    def runWith(fixture: => Fixture): Unit = {
      val message = s"▏$description"
      println(Array.fill[Char](message.length)('_').mkString)
      println(message)
      try {
        fixture
        println("👍 ")
      } finally {
        cleanupContext.cleanupAll()
      }
    }
  }
  def assertThat(assertion: => Boolean, message: => String): Unit = {
    val result = Try(assert(assertion, message))
    val sign = if (result.isSuccess) "👍 " else "❗ "
    println(s"$sign$message")
    if (result.isFailure) throw result.failed.get
  }
}

