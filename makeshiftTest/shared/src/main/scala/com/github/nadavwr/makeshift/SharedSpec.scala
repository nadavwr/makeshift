package com.github.nadavwr.makeshift

//noinspection TypeAnnotation
trait SharedSpec extends Spec {

  object FileFixture {
    var counter: () => Int = {
      var next = 0
      () => { val result = next; next += 1; result }
    }
  }

  trait FileFixture extends Fixture {
    import java.io.File
    lazy val filename = "file" + FileFixture.counter()
    lazy val file = new File(filename)
      .withCleanup(s"File('$filename')") {
        file => if (file.exists()) file.delete()
      }
  }

  trait FileOutputStreamFixture extends FileFixture {
    import java.io.FileOutputStream
    def beforeFileCleanup(): Unit = ()
    lazy val fileOutputStream = new FileOutputStream(file)
      .withCleanup(s"FileOutputStream('$filename')") {
        fileOutputStream =>
          beforeFileCleanup()
          fileOutputStream.close()
      }
  }

  test("java.io file I/O support") runWith new FileOutputStreamFixture {
    val str = "hello"
    val strBytes = str.toArray.map(_.toByte)
    fileOutputStream.write(strBytes)

    override def beforeFileCleanup(): Unit = {
      import java.io.FileInputStream
      val fileInputStream = new FileInputStream(file)
      try {
        val content = Iterator.continually(fileInputStream.read()).takeWhile(_ != -1)
          .map(_.toByte).toArray
        assertThat(content sameElements strBytes, s"file content should match expected ('$str')")
      } finally fileInputStream.close()
    }
  }
}

