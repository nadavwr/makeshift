package com.github.nadavwr.makeshift

case class CleanupEntry(labelOpt: Option[String], handler: () => Any)
