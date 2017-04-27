package com.github.nadavwr.makeshift

abstract class Fixture(implicit cleanupContext: CleanupContext) {
  protected implicit class Cleanable[A](a: A) {
    def withCleanup(cleanup: A => Unit): A = {
      cleanupContext.cleanups.prepend(CleanupEntry(None, () => cleanup(a)))
      a
    }
    def withCleanup(label: String)(cleanup: (A) => Unit): A = {
      println(s"$label created")
      cleanupContext.cleanups.prepend(CleanupEntry(Some(label), () => cleanup(a)))
      a
    }
  }
}

